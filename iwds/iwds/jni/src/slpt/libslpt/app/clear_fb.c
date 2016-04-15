#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>

#include "slpt.h"
#include <common.h>
#include <fb_struct.h>
#include <file_ops.h>
#include <view.h>

int slpt_clear_fb(int argc, char **argv) {
	int hor_or_ver = 0;
	int n = 0;
	int color1 = 0;
	int color2 = 0;

	if (argc < 5) {
		pr_err("clear_fb: invalid args\n");
		return -EINVAL;
	}

	if (fb_init()) {
		pr_err("clear_fb: can not init fb device\n");
		return -ENODEV;
	}

	if (!strcmp(argv[1], "hor"))
		hor_or_ver = 1;
	else
		hor_or_ver = 0;

	sscanf(argv[2], "%d", &n);
	sscanf(argv[3], "%x", &color1);
	sscanf(argv[4], "%x", &color2);

	pr_info("fb_clear: %s %d 0x%x 0x%x\n", hor_or_ver ? "hor" : "ver", n, color1, color2);

	if (hor_or_ver)
		fb_clear_hor(n, color1, color2);
	else
		fb_clear_ver(n, color1, color2);

	lcd_pan_display(0);

	usleep(100 * 1000);

	fb_exit();

	return 0;
}
