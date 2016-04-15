#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>

#include "slpt.h"
#include <common.h>
#include <fb_struct.h>
#include <file_ops.h>
#include <view.h>

#ifdef CONFIG_SLPT_LINUX_EXECUTABLE
#include <hardware_legacy/uevent.h>
#endif

#include <pthread.h>

#include "slpt_clock.h"

#ifdef DEBUG
int debug_display = 1;
#else
int debug_display = 0;
#endif


#undef pr_debug
#define pr_debug(x...)                          \
	do {                                        \
		if (debug_display)                      \
			pr_info(x);                         \
	} while (0)

#define POWER_STATE_FILE "/sys/devices/platform/power_state.0/power_state"
#define REQUEST_EARLYSUSPEND_FILE "/sys/devices/platform/power_state.0/request_earlysuspend"
#define RELEASE_EARLYSUSPEND_FILE "/sys/devices/platform/power_state.0/release_earlysuspend"

#define STATE_ACTIVE  "active"
#define STATE_EARLY_SUSPEND  "early_suspend"
#define STATE_SUSPEND  "suspend"
#define STATE_ENTER  "enter_suspend"
#define STATE_REALSE  "device_relase"

enum {
	S_ACTIVE,
	S_EARLY_SUSPEND,
	S_SUSPEND,
	S_ENTER,
	S_REALSE,
	S_NONE,
};

static inline int get_state(const char *buf) {
	if (!strncmp(buf, STATE_ACTIVE, strlen(STATE_ACTIVE))) {
		return S_ACTIVE;
	} else if (!strncmp(buf, STATE_EARLY_SUSPEND, strlen(STATE_EARLY_SUSPEND))) {
		return S_EARLY_SUSPEND;
	} else if (!strncmp(buf, STATE_SUSPEND, strlen(STATE_SUSPEND))) {
		return S_SUSPEND;
	} else if (!strncmp(buf, STATE_ENTER, strlen(STATE_ENTER))) {
		return S_ENTER;
	} else if (!strncmp(buf, STATE_REALSE, strlen(STATE_REALSE))) {
		return S_REALSE;
	}

	return S_NONE;
}

extern int display_file(const char *fn, struct position *start);
extern int init_slpt_files(void);
extern int slpt_sync_files(void);
extern int slpt_display_clock(void);
extern int access_fb_always_on(void);
extern int is_fb_always_on(void);
extern void slpt_display_init(void);
extern int slpt_display(void);

#ifdef CONFIG_SLPT_LINUX_EXECUTABLE
#define SLPT_SUSPEND_MOUDLE_0 "/system/etc/slpt-suspend.ko"
#define SLPT_SUSPEND_MOUDLE_1 "/system/vendor/slpt-suspend.ko"
#define SLPT_SUSPEND_MOUDLE_2 "/system/etc/firmware/slpt-suspend.ko"

int load_slpt_moudle(void) {
	char buf[100];
	char *str = NULL;

	if (!access(SLPT_SUSPEND_MOUDLE_0, F_OK)) {
		str = SLPT_SUSPEND_MOUDLE_0;
	} else if (!access(SLPT_SUSPEND_MOUDLE_1, F_OK)) {
		str = SLPT_SUSPEND_MOUDLE_1;
	} else if (!access(SLPT_SUSPEND_MOUDLE_2, F_OK)) {
		str = SLPT_SUSPEND_MOUDLE_2;
	}

	pr_info("display: load moudle at %s\n", str ? str : "error path");

	if (str) {
		sprintf(buf, "insmod %s", str);
		system(buf);
		return 0;
	} else {
		return -ENOENT;
	}
}

pthread_t uevent_thread_t;
pthread_mutex_t uevent_lock;

static int slpt_files_changed = 0;
static struct timeval uevent_up_timeval;
static struct timezone uevent_up_timezone;

static long long cal_time(struct timeval new_timeval, struct timezone new_timezone, \
				struct timeval old_timeval, struct timezone old_timezone)
{
	long long time_usecond = 0;

	time_usecond = (new_timeval.tv_sec * 1000000 + new_timeval.tv_usec - new_timezone.tz_minuteswest * 60 * 1000000) - \
			(old_timeval.tv_sec * 1000000 + old_timeval.tv_usec - old_timezone.tz_minuteswest * 60 * 1000000);
	return time_usecond;
}

static long cal_time_sync_delay(unsigned int *files_changed)
{
	struct timeval new_timeval;
	struct timezone new_timezone;

	long long time_usecond = 0;

	/* get system time */
	assert(!gettimeofday(&new_timeval, &new_timezone));

	pthread_mutex_lock(&uevent_lock);

	time_usecond = cal_time(new_timeval, new_timezone, uevent_up_timeval, uevent_up_timezone);
	*files_changed = slpt_files_changed;

	pthread_mutex_unlock(&uevent_lock);

	return time_usecond;
}

int slpt_files_need_sync(void) {
	int ret = 0;
	long long tmp = 0;
	unsigned int file_change;

	do {
		tmp = cal_time_sync_delay(&file_change);
		if(tmp > 0) {
			if(tmp < 30000 && file_change)
				usleep(30000);
		} else {
			/* if the time update to the previous time, we need to wait 30ms to sync */
			assert(!gettimeofday(&uevent_up_timeval, &uevent_up_timezone));
			file_change = 1;
			tmp = 0;
		}

	} while(tmp >= 0 && tmp < 30000 && file_change);

	pthread_mutex_lock(&uevent_lock);

	ret = file_change;
	slpt_files_changed = 0;

	pthread_mutex_unlock(&uevent_lock);

	return ret;
}

enum {
	SLPT_UEVENT_ATCTION = 0,
	SLPT_UEVENT_TASK,
	SLPT_UEVENT_PATH,
	SLPT_UEVENT_FILE_NAME,
	SLPT_UEVENT_MSG,

	/* KEEP LAST */
	SLPT_UEVENT_NUMS,
};

const char *slpt_uevnets[SLPT_UEVENT_NUMS] = {
	[SLPT_UEVENT_ATCTION] = "SLPT-ACTION=",
	[SLPT_UEVENT_TASK] = "SLPT-TASK=",
	[SLPT_UEVENT_PATH] = "SLPT-PATH=",
	[SLPT_UEVENT_FILE_NAME] = "SLPT-FILE-NAME=",
	[SLPT_UEVENT_MSG] = "SLPT-MSG=",
};

#define STRNCMP(dst, src) strncmp(dst, src, strlen(src))

void *uevent_observer(void *data) {
	unsigned int i;
	int count;
	char *str, *end;
	char buf[1024];
	char *envp[SLPT_UEVENT_NUMS];

	const char *xxx = slpt_file_names[0];

	memset(buf, 0, sizeof(buf));

	while (1) {
		count = uevent_next_event(buf, sizeof(buf) - 1);
		if (count > 0) {
			str = strstr(buf, "@");
			if (str && !STRNCMP(str, "@/slpt")) {
				memset(envp, 0, sizeof(envp));
				end = buf + count;
				while (str < end) {
					for (i = 0; i < ARRAY_SIZE(slpt_uevnets); ++i) {
						if (!STRNCMP(str, slpt_uevnets[i])) {
							envp[i] = str + strlen(slpt_uevnets[i]);
						}
					}
					str = str + strlen(str) + 1;
				}
				if (envp[SLPT_UEVENT_FILE_NAME] &&
					!(!strcmp(envp[SLPT_UEVENT_FILE_NAME], slpt_file_names[SLPT_CLOCK_SYNC_TIME]) ||
					  !strcmp(envp[SLPT_UEVENT_FILE_NAME], slpt_file_names[SLPT_CLOCK_SYNC_TIME_ENABLE]))) {
					pthread_mutex_lock(&uevent_lock);
					slpt_files_changed = 1;

					/* get system time */
					assert(!gettimeofday(&uevent_up_timeval, &uevent_up_timezone));

					pthread_mutex_unlock(&uevent_lock);
				}
			}
		}
	}

	return NULL;
}
#else
static inline int load_slpt_moudle(void) {
	return 0;
}

int slpt_files_need_sync(void) {
	return 0;
}
#endif

int display_test_main(int argc, char **argv) {
	char power_state[30] = STATE_REALSE;
	char request_state[30] = STATE_REALSE;
	char power_state_file[MAX_FILE_NAME] = POWER_STATE_FILE;
	char request_state_file[MAX_FILE_NAME] = REQUEST_EARLYSUSPEND_FILE;
	char release_state_file[MAX_FILE_NAME] = RELEASE_EARLYSUSPEND_FILE;
	unsigned int nr;

	int ret;

#ifdef CONFIG_SLPT_LINUX_EXECUTABLE
	if (!uevent_init()) {
		pr_err("failed to init uevent\n");
		perror("uevent init:");
		return -ENOMEM;
	}

	ret = pthread_create(&uevent_thread_t, NULL, uevent_observer, NULL);
	if (ret) {
		pr_err("failed to create pthread\n");
		perror("pthread:");
		return ret;
	}
#endif

	if (fb_init()) {
		pr_err("display: can not init fb device\n");
		return -ENODEV;
	}

	pr_debug("dispaly: start slpt early suspend display\n");

	load_slpt_moudle();

	usleep(100 * 1000);

	if (access(power_state_file, F_OK) ||
		access(request_state_file, F_OK) ||
		access(release_state_file, F_OK)) {
		pr_err("display: power state files not exist\n");
		return -ENOENT;
	}

	if (access(power_state_file, R_OK) ||
		access(request_state_file, R_OK) ||
		access(release_state_file, R_OK)) {
		pr_err("display: power state files no read permission\n");
		return -EACCES;
	}

	access_fb_always_on();

	pr_debug("--------------------------");
	slpt_display_init();
	pr_debug("--------------------------");

	while (1) {
		for ( ; ; ) {
		sleep_again:
			usleep(100 * 1000);
			pr_debug("display: request early suspend\n");
			if (!load_file(request_state_file, request_state, 20, &nr)) {
				pr_err("display: failed to read file: %s\n", request_state_file);
				pr_err("display: try again\n");
				continue;
			}

			pr_debug("display: request fb always on\n");
			if (!is_fb_always_on()) {
				pr_debug("display: fb always on is false\n");
				goto sleep_again;
			}

			pr_debug("display: request state: (%s) (%d)bytes\n", request_state, nr);
			if (load_file(power_state_file, power_state, 20, &nr)) {
				pr_debug("display: power state: (%s) (%d)bytes\n", power_state, nr);
				if (!strcmp(power_state, STATE_EARLY_SUSPEND)) {
					break;
				}
			} else {
				pr_err("display: failed to read file: %s\n", power_state_file);
			}
		}

		pr_debug("display: now we can display one frame\n");

		ret = slpt_display();
		if (ret == 1) {
			pr_debug("display: sleep again\n");
			goto sleep_again;
		}

		lcd_pan_display(0);
		pr_debug("display: release early suspend\n");

		for ( ; ; ) {
			if (load_file(release_state_file, request_state, 20, &nr))
				break;
			pr_err("display: failed to read file: %s\n", release_state_file);
			pr_debug("display: try again\n");
		}

		pr_debug("display: done\n");
	}

#ifdef CONFIG_SLPT_LINUX_EXECUTABLE
	system("rmmod slpt-suspend");
#endif

	fb_exit();
}
