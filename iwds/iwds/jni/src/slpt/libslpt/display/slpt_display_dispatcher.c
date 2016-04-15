#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <time.h>
#include <pthread.h>

#include <common.h>
#include <fb_struct.h>
#include <file_ops.h>

#include <slpt_display_dispatcher.h>

#ifdef DEBUG
int debug_display_dispatcher = 1;
#else
int debug_display_dispatcher = 0;
#endif

#undef pr_debug
#define pr_debug(x...)                          \
	do {                                        \
		if (debug_display_dispatcher)			\
			pr_info(x);                         \
	} while (0)

enum {
	THREAD_IS_STOPED = 0,
	THREAD_IS_RUNNING,
	THREAD_ASK_TO_STOP,
};

enum {
	CMD_NONE = 0,
	CMD_WAIT,
	CMD_QUIT,
	CMD_SYNC,
};

struct slpt_display_dispatcher {
	pthread_mutex_t lock;
	pthread_cond_t cond;
	pthread_t thread;
	unsigned int thread_status;
	int cmd;
	int inited;
};

static struct slpt_display_dispatcher dispatcher;

extern int access_fb_always_on(void);
extern int is_fb_always_on(void);
extern int set_fb_always_on(int on);

extern int access_brightness_always_on(void);
extern int is_brightness_always_on(void);
extern int set_brightness_always_on(int on);
extern int get_brightness_always_on_level(void);
extern int set_brightness_always_on_level(unsigned int level);
extern int set_current_brightness_on_lenvel(void);

extern int access_lcd_power_on(void);
extern int is_lcd_power_on(void);
extern int set_lcd_power_on(int on);

extern int init_before_display(void);
extern void destory_after_display(void);

extern int access_power_state();
extern int test_power_state(void);
extern int request_power_state(void);
extern int release_power_state(void);

extern int access_display_ctrl(void);
extern int get_display_ctrl_state(void);
extern int set_display_ctrl(int on);
extern int is_support_display_ctrl(void);

extern int access_lock_fb_pan_display(void);
extern int is_lock_fb_pan_display(void);
extern int set_lock_fb_pan_display(int on);

extern void slpt_sync_sview(void);
extern int slpt_display_sview(void);

static void timeval_add_usec(struct timeval *time, unsigned int usec) {
	time->tv_usec += usec;
	time->tv_sec += time->tv_usec / (1000 * 1000);
	time->tv_usec = time->tv_usec % (1000 * 1000);
}

static void timeval_to_timespec(struct timespec *timespec, struct timeval *timeval) {
	timespec->tv_sec = timeval->tv_sec;
	timespec->tv_nsec = timeval->tv_usec * 1000;
}

static void thread_cmd(unsigned int cmd) {
	pthread_mutex_lock(&dispatcher.lock);
	dispatcher.cmd = cmd;
	pthread_cond_signal(&dispatcher.cond);
	pthread_mutex_unlock(&dispatcher.lock);
}

static void thread_sleep(void) {
	struct timespec outtime;
	struct timeval nowtime;

	gettimeofday(&nowtime, NULL);
	timeval_add_usec(&nowtime, 50 * 1000);
	timeval_to_timespec(&outtime, &nowtime);

	pthread_cond_timedwait(&dispatcher.cond, &dispatcher.lock, &outtime);
}

static void lock_fb(int on) {
	static int m_state = 0;

	if (m_state != on) {
		set_lock_fb_pan_display(on);
		m_state = on;
	}
}

#define unlock_fb() lock_fb(0)

static void *slpt_display_thread(void *arg) {
	int ret;

	pthread_mutex_lock(&dispatcher.lock);
	dispatcher.thread_status = THREAD_IS_RUNNING;
	while (1) {
	unlock_fb:
		unlock_fb();
	sleep_again:
		thread_sleep();

		switch (dispatcher.cmd) {
		case CMD_WAIT:
			goto sleep_again;
		case CMD_QUIT:
			goto out_thread;
		case CMD_SYNC:
			slpt_sync_sview();
			break;
		default:
			break;
		}
		dispatcher.cmd = CMD_NONE;

		pr_debug("display: test early suspend\n");
		if (test_power_state())
			goto unlock_fb;

		pr_debug("display: request fb always on\n");
		if (!is_fb_always_on()) {
			pr_debug("display: fb always on is false\n");
			goto unlock_fb;
		}

		pr_debug("display: request early suspend\n");
		if (request_power_state()) {
			pr_err("display: try request again\n");
			goto release_early_suspend;
		}

		pr_debug("display: now we can display one frame\n");

		lock_fb(1);
		ret = slpt_display_sview();
		if (ret) {
			pr_debug("display: sleep again\n");
			goto release_early_suspend;
		}

		lcd_pan_display(0);
		pr_debug("display: release early suspend\n");

	release_early_suspend:
		for ( ; ; ) {
			if (!release_power_state())
				break;
			pr_err("display: try release again\n");
		}

		pr_debug("display: done\n");
		goto sleep_again;
	}

out_thread:
	dispatcher.thread_status = THREAD_IS_STOPED;
	pthread_mutex_unlock(&dispatcher.lock);

	return NULL;
}

void slpt_display_dispatcher_enable_fb(void) {
	set_current_brightness_on_lenvel(); /* just in case of not set brightness on level */
	if (is_support_display_ctrl()) {
		set_display_ctrl(1);
	} else {
		set_fb_always_on(1);
		set_brightness_always_on(1);
	}
}

void slpt_display_dispatcher_disable_fb(void) {
	if (is_support_display_ctrl()) {
		set_display_ctrl(0);
	} else {
		set_fb_always_on(0);
		set_brightness_always_on(0);
	}
}

void slpt_display_dispatcher_set_brightness(unsigned int brightness) {
	set_brightness_always_on_level(brightness);
}

void slpt_display_dispatcher_power_on_lcd(void) {
	set_lcd_power_on(1);
}

void slpt_display_dispatcher_power_off_lcd(void) {
	set_lcd_power_on(0);
}

int slpt_display_dispatcher_init(int argc, char **argv) {
	int ret;

	if (dispatcher.inited)
		return 0;

	/* dispatcher.inited = 0; */
	/* dispatcher.cmd = CMD_NONE; */

	if (fb_init()) {
		pr_err("display: can not init fb device\n");
		return -ENODEV;
	}

	if (access_power_state()) {
		pr_err("display: can not access early suspend\n");
		return -ENODEV;
	}

	access_fb_always_on();
	access_brightness_always_on();
	access_lcd_power_on();
	access_display_ctrl();
	access_lock_fb_pan_display();

	/* now we just default set fb/brightness always on enable */

	if (init_before_display()) {
		pr_err("display: failed to init before display\n");
		fb_exit();
		return -EINVAL;
	}

	dispatcher.thread_status = THREAD_IS_STOPED;

	ret = pthread_mutex_init(&dispatcher.lock, NULL);
	assert(!ret);

	ret = pthread_cond_init(&dispatcher.cond, NULL);
	assert(!ret);

	ret = pthread_create(&dispatcher.thread, NULL, slpt_display_thread, &dispatcher);
	assert(!ret);

	dispatcher.inited = 1;

	return 0;
}

void slpt_display_dispatcher_pause(void) {
	if (!dispatcher.inited || dispatcher.thread_status != THREAD_IS_RUNNING)
		return;

	thread_cmd(CMD_WAIT);
}

void slpt_display_dispatcher_resume(void) {
	if (!dispatcher.inited || dispatcher.thread_status != THREAD_IS_RUNNING)
		return;

	thread_cmd(CMD_SYNC);
}

void slpt_display_dispatcher_exit(void) {
	if (!dispatcher.inited || dispatcher.thread_status != THREAD_IS_RUNNING)
		return;

	thread_cmd(CMD_QUIT);
	pthread_join(dispatcher.thread, NULL);

	pthread_mutex_destroy(&dispatcher.lock);
	pthread_cond_destroy(&dispatcher.cond);

	fb_exit();
}
