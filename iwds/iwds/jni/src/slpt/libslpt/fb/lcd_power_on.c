#include <common.h>
#include <file_ops.h>

static const char *lcd_power_on_files[] = {
	"/sys/devices/platform/jz-fb.0/lcd_power_on",
	"/sys/devices/platform/jz-fb.0/debug/lcd_power_on",
	"/sys/devices/platform/jz-fb.1/lcd_power_on",
	"/sys/devices/platform/jz-fb.1/debug/lcd_power_on",
};
static const char *select_lcd_power_on = NULL;

int access_lcd_power_on(void) {
	unsigned int i;

	for (i = 0; i < ARRAY_SIZE(lcd_power_on_files); ++i) {
		if (!access(lcd_power_on_files[i], R_OK | W_OK)) {
			select_lcd_power_on = lcd_power_on_files[i];
			return 0;
		}
	}

	return -ENODEV;
}

int is_lcd_power_on(void) {
	unsigned int nr;
	char buf[10];
	int on = 1;

	if (select_lcd_power_on) {
		memset(buf, 0, sizeof(buf));
		if (!load_file(select_lcd_power_on, buf, sizeof(buf), &nr)) {
			return 1;
		}
		sscanf(buf, "%d", &on);
		return !!on;
	}

	return 1;
}

int set_lcd_power_on(int on) {
	char buf[10];

   	if (!select_lcd_power_on)
		return -ENODEV;

	sprintf(buf, "%u", !!on);

	return write_file(select_lcd_power_on, buf, strlen(buf) + 1);
}
