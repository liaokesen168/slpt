#include <unistd.h>
#include <common.h>
#include <file_ops.h>

#ifdef DEBUG
int debug_early_suspend = 1;
#else
int debug_early_suspend = 0;
#endif

#undef pr_debug
#define pr_debug(x...)                          \
	do {                                        \
		if (debug_early_suspend)				\
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

static const char *power_state_file = POWER_STATE_FILE;
static const char *request_state_file = REQUEST_EARLYSUSPEND_FILE;
static const char *release_state_file = RELEASE_EARLYSUSPEND_FILE;

int access_power_state() {
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

	return 0;
}

/*
 * test if power state is in earlysuspend
 */
int test_power_state(void) {
	unsigned int nr;
	char power_state[30];

	if (load_file(power_state_file, power_state, 20, &nr)) {
		power_state[nr] = '\0';
		pr_debug("display: power state: (%s) (%d)bytes\n", power_state, nr);
		if (!strcmp(power_state, STATE_EARLY_SUSPEND)) {
			return 0;
		}
	} else {
		pr_err("display: failed to read file: %s\n", power_state_file);
	}

	return -1;
}

/*
 * request power state keep in earlysuspend
 */
int request_power_state(void) {
	unsigned int nr;
	static char request_state[30];

	if (!load_file(request_state_file, request_state, 20, &nr)) {
		pr_err("display: failed to read file: %s\n", request_state_file);
		return -1;
	}

	return 0;
}

/*
 * release the request of power state keep earlysuspend
 */
int release_power_state(void) {
	unsigned int nr;
	static char request_state[30];

	if (load_file(release_state_file, request_state, 20, &nr))
		return 0;
	pr_err("display: failed to read file: %s\n", release_state_file);

	return -1;
}
