#include <common.h>
#include <file_ops.h>

static const char *brightness_always_on_files[] = {
"/sys/slpt/configs/brightness_always_on"
};
static const char *select_brightness_always_on = NULL;

static const char *brightness_always_on_level_files[] = {
"/sys/slpt/configs/brightness_always_on_level"
};
static const char *select_brightness_always_on_level = NULL;

static int current_brightness_level = 60;

int access_brightness_always_on(void) {
	unsigned int i;
	int ret1 = -ENODEV, ret2 = -ENODEV;

	for (i = 0; i < ARRAY_SIZE(brightness_always_on_level_files); ++i) {
		if (!access(brightness_always_on_level_files[i], R_OK | W_OK)) {
			select_brightness_always_on_level = brightness_always_on_level_files[i];
			ret1 = 0;
		}
	}

	for (i = 0; i < ARRAY_SIZE(brightness_always_on_files); ++i) {
		if (!access(brightness_always_on_files[i], R_OK | W_OK)) {
			select_brightness_always_on = brightness_always_on_files[i];
			ret2 = 0;
		}
	}

	return (ret1 || ret2) ? -ENODEV : 0;
}

int is_brightness_always_on(void) {
	unsigned int nr;
	char buf[10];
	int on = 1;

	if (select_brightness_always_on) {
		memset(buf, 0, sizeof(buf));
		if (!load_file(select_brightness_always_on, buf, sizeof(buf), &nr)) {
			return 1;
		}
		sscanf(buf, "%d", &on);
		return !!on;
	}

	return 1;
}

int set_brightness_always_on(int on) {
	char buf[10];

   	if (!select_brightness_always_on)
		return -ENODEV;

	sprintf(buf, "%u", !!on);

	return write_file(select_brightness_always_on, buf, strlen(buf) + 1);
}

int get_brightness_always_on_level(void) {
	unsigned int nr;
	char buf[10];
	unsigned int level = 102;

	if (select_brightness_always_on_level) {
		memset(buf, 0, sizeof(buf));
		if (!load_file(select_brightness_always_on, buf, sizeof(buf), &nr)) {
			return 1;
		}
		sscanf(buf, "%u", &level);
		return level;
	}

	return 102;
}

int set_brightness_always_on_level(unsigned int level) {
	char buf[10];

	current_brightness_level = level;

	if (!select_brightness_always_on_level)
		return -ENODEV;

	sprintf(buf, "%u", level);

	return write_file(select_brightness_always_on_level, buf, strlen(buf) + 1);
}

int set_current_brightness_on_lenvel(void) {
	return set_brightness_always_on_level(current_brightness_level);
}
