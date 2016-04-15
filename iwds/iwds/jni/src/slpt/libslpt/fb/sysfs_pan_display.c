#include <common.h>
#include <file_ops.h>

static const char *sysfs_pan_display_files[] = {
	"/sys/devices/platform/jz-fb.0/pan_display",
	"/sys/devices/platform/jz-fb.0/debug/pan_display",
	"/sys/devices/platform/jz-fb.1/pan_display",
	"/sys/devices/platform/jz-fb.1/debug/pan_display",
};
static const char *select_sysfs_pan_display = NULL;

int is_support_sysfs_pan_display(void) {
	return !!select_sysfs_pan_display;
}

int access_sysfs_pan_display(void) {
	unsigned int i;

	for (i = 0; i < ARRAY_SIZE(sysfs_pan_display_files); ++i) {
		if (!access(sysfs_pan_display_files[i], R_OK)) {
			select_sysfs_pan_display = sysfs_pan_display_files[i];
			return 0;
		}
	}
	return -ENODEV;
}

int is_sysfs_pan_display(void) {
	unsigned int nr;
	char buf[10];
	int on;

	if (select_sysfs_pan_display) {
		memset(buf, 0, sizeof(buf));
		if (!load_file(select_sysfs_pan_display, buf, sizeof(buf), &nr)) {
			return 1;
		}
		sscanf(buf, "%d", &on);
		return !!on;
	}
	return 1;
}

int set_sysfs_pan_display(int frm) {
	char buf[10];

	if (!select_sysfs_pan_display)
		return -ENODEV;

	sprintf(buf, "%u", frm);

	return write_file(select_sysfs_pan_display, buf, strlen(buf) + 1);
}
