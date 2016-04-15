#include <common.h>
#include <file_ops.h>

static const char *lock_fb_pan_display_files[] = {
	"/sys/devices/platform/jz-fb.0/lock_fb_pan_display",
	"/sys/devices/platform/jz-fb.0/debug/lock_fb_pan_display",
	"/sys/devices/platform/jz-fb.1/lock_fb_pan_display",
	"/sys/devices/platform/jz-fb.1/debug/lock_fb_pan_display",
};
static const char *select_lock_fb_pan_display = NULL;

int access_lock_fb_pan_display(void) {
	unsigned int i;

	for (i = 0; i < ARRAY_SIZE(lock_fb_pan_display_files); ++i) {
		if (!access(lock_fb_pan_display_files[i], R_OK)) {
			select_lock_fb_pan_display = lock_fb_pan_display_files[i];
			return 0;
		}
	}
	return -ENODEV;
}

int is_lock_fb_pan_display(void) {
	unsigned int nr;
	char buf[10];
	int on;

	if (select_lock_fb_pan_display) {
		memset(buf, 0, sizeof(buf));
		if (!load_file(select_lock_fb_pan_display, buf, sizeof(buf), &nr)) {
			return 1;
		}
		sscanf(buf, "%d", &on);
		return !!on;
	}
	return 1;
}

int set_lock_fb_pan_display(int on) {
	char buf[10];

	if (!select_lock_fb_pan_display)
		return -ENODEV;

	sprintf(buf, "%u", !!on);

	return write_file(select_lock_fb_pan_display, buf, strlen(buf) + 1);
}
