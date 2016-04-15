#include <common.h>
#include <file_ops.h>

static const char *fb_always_on_files[] = {
	"/sys/devices/platform/jz-fb.0/fb_always_on",
	"/sys/devices/platform/jz-fb.0/debug/fb_always_on",
	"/sys/devices/platform/jz-fb.1/fb_always_on",
	"/sys/devices/platform/jz-fb.1/debug/fb_always_on",
	"/sys/slpt/configs/fb_always_on"
};
static const char *select_fb_always_on = NULL;

int access_fb_always_on(void) {
	unsigned int i;

	for (i = 0; i < ARRAY_SIZE(fb_always_on_files); ++i) {
		if (!access(fb_always_on_files[i], R_OK)) {
			select_fb_always_on = fb_always_on_files[i];
			return 0;
		}
	}
	return -ENODEV;
}

int is_fb_always_on(void) {
	unsigned int nr;
	char buf[10];
	int on;

	if (select_fb_always_on) {
		memset(buf, 0, sizeof(buf));
		if (!load_file(select_fb_always_on, buf, sizeof(buf), &nr)) {
			return 1;
		}
		sscanf(buf, "%d", &on);
		return !!on;
	}
	return 1;
}

int set_fb_always_on(int on) {
	char buf[10];

	if (!select_fb_always_on)
		return -ENODEV;

	sprintf(buf, "%u", !!on);

	return write_file(select_fb_always_on, buf, strlen(buf) + 1);
}
