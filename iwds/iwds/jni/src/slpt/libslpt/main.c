/*
 *  Copyright (C) 2015 Ingenic Semiconductor
 *
 *  Wu jiao <jiao.wu@ingenic.com wujiaososo@qq.com>
 *  slpt-linux project
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation; either version 2 of the License, or (at your
 *  option) any later version.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

#include <sys/mman.h>
#include <dlfcn.h>
#include <fcntl.h>
#include <asm/errno.h>
#include <sys/ioctl.h>
#include <string.h>
#include <stdlib.h>
#include <linux/fb.h>
#include <linux/errno.h>

#include "slpt.h"
#include <common.h>
#include <call_app.h>

extern int set_timezone_main(int argc, char **argv);
extern int display_test_main(int argc, char **argv);
extern int lcd_density_main(int argc, char **argv);
extern int simple_dirname_main(int argc, char **argv);
extern int view_test_main(int argc, char **argv);
extern int slpt_set_view_int(int argc, char **argv);
extern int slpt_set_view_str(int argc, char **argv);
extern int slpt_set_picgrp(int argc, char **argv);
extern int slpt_set_pic(int argc, char **argv);
extern int slpt_init_view_state(int argc, char **argv);
extern int slpt_get_view_int(int argc, char **argv);
extern int slpt_get_view_str(int argc, char **argv);
extern int slpt_print_the_view(int argc, char **argv);
extern int slpt_clear_fb(int argc, char **argv);
extern int fft_test(int argc, char **argv);
extern int find_pictures_test_main(int argc, char **argv);
extern int slpt_set_bmp(int argc, char **argv);
extern int slpt_set_bmp_grp(int argc, char **argv);
extern int slpt_load_fw(int argc, char **argv);


enum app_list_order {
	APP_SETTIMEZONE,
	APP_DISPLAY_TEST,
	APP_LCD_DENSITY,
	APP_DIRNAME,
	APP_VIEW_TEST,
	APP_SET_VIEW_INT,
	APP_SET_VIEW_STR,
	APP_SET_PIC_GRP,
	APP_SET_BMP_PIC,
	APP_INIT_VIEW_STATE,
	APP_GET_VIEW_INT,
	APP_GET_VIEW_STR,
	APP_PRINT_VIEW,
	APP_CLEAR_FB,
	APP_FFT_TEST,
	APP_FIND_PICTURES_TEST,
	APP_SET_BMP,
	APP_SET_BMP_GRP,
	APP_LOAD_FW,

	/* keep last */
	APP_NUMS,
};

static struct app_struct app_list[APP_NUMS] = {
	APP_FUNC(APP_SETTIMEZONE, "set_timezone", set_timezone_main),
	APP_FUNC(APP_DISPLAY_TEST, "display_test", display_test_main),
	APP_FUNC(APP_FIND_PICTURES_TEST, "find_pictures_test", find_pictures_test_main),
	APP_FUNC(APP_LCD_DENSITY, "lcd_density", lcd_density_main),
	APP_FUNC(APP_DIRNAME, "dirname", simple_dirname_main),
	APP_FUNC(APP_VIEW_TEST, "view_test", view_test_main),
	APP_FUNC(APP_SET_VIEW_INT, "set_view_int", slpt_set_view_int),
	APP_FUNC(APP_SET_VIEW_STR, "set_view_str", slpt_set_view_str),
	APP_FUNC(APP_SET_PIC_GRP, "set_pic_grp", slpt_set_picgrp),
	APP_FUNC(APP_SET_BMP_PIC, "set_pic", slpt_set_pic),
	APP_FUNC(APP_INIT_VIEW_STATE, "init_view", slpt_init_view_state),
	APP_FUNC(APP_GET_VIEW_INT, "get_view_int", slpt_get_view_int),
	APP_FUNC(APP_GET_VIEW_STR, "get_view_str", slpt_get_view_str),
	APP_FUNC(APP_PRINT_VIEW, "print_view", slpt_print_the_view),
	APP_FUNC(APP_CLEAR_FB, "clear_fb", slpt_clear_fb),
	APP_FUNC(APP_FFT_TEST, "fft_test", fft_test),
	APP_FUNC(APP_SET_BMP, "set_bmp", slpt_set_bmp),
	APP_FUNC(APP_SET_BMP_GRP, "set_bmp_grp", slpt_set_bmp_grp),
	APP_FUNC(APP_LOAD_FW, "load_fw", slpt_load_fw),
};

int slpt_main(int argc, char **argv) {
	unsigned int i;
	int argc_app = argc - 1;

	LOGD("%s is called\n", __FUNCTION__);

	if (argc < 2) {
		pr_err("%s need arguments\n", argv[0]);
		return -EINVAL;
	}

	for (i = 0; i < ARRAY_SIZE(app_list); ++i) {
		if (app_list[i].type != 0 && !strcmp(app_list[i].name, argv[1])) {
			break;
		}
	}

	if (i >= ARRAY_SIZE(app_list)) {
		pr_err("unrecognized app %s\n", argv[1]);
		return -ENODEV;
	}
	return call_app(&app_list[i], argc_app, &argv[argc - argc_app]);
}

#ifdef CONFIG_SLPT_LINUX_EXECUTABLE
int main(int argc, char **argv) {
	return slpt_main(argc, argv);
}
#endif
