#include <common.h>
#include <file_ops.h>

static const char *display_ctrl_files[] = {
"/sys/slpt/configs/display_ctrl"
};
static const char *select_display_ctrl = NULL;

int access_display_ctrl(void) {
	unsigned int i;

	for (i = 0; i < ARRAY_SIZE(display_ctrl_files); ++i) {
		if (!access(display_ctrl_files[i], R_OK | W_OK)) {
			select_display_ctrl = display_ctrl_files[i];
			return 0;
		}
	}

	return -ENODEV;
}

int is_support_display_ctrl(void) {
	return !!select_display_ctrl;
}

int get_display_ctrl_state(void) {
	unsigned int nr;
	char buf[10];
	int on = 1;

	if (select_display_ctrl) {
		memset(buf, 0, sizeof(buf));
		if (!load_file(select_display_ctrl, buf, sizeof(buf), &nr)) {
			return 1;
		}
		sscanf(buf, "%d", &on);
		return !!on;
	}

	return 1;
}

int set_display_ctrl(int on) {
	char buf[10];

   	if (!select_display_ctrl)
		return -ENODEV;

	sprintf(buf, "%u", !!on);

	return write_file(select_display_ctrl, buf, strlen(buf) + 1);
}
