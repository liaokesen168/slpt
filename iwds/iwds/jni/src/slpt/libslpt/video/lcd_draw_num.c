/*
 * Copyright (C) 2014 Ingenic Semiconductor Co., Ltd.
 * Authors: Wu jiao  <jwu@ingenic.cn>
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General  Public License as published by the
 * Free Software Foundation;  either version 2 of the License, or (at your
 * option) any later version.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
#include <common.h>
#include <malloc.h>

#include <linux/err.h>

#include <command.h>
#include <slpt_app.h>

#include <rtc.h>
#include <asm/arch/jz47xx_rtc.h>

#include <fb_struct.h>
#include <slpt.h>
#include <linux/pr_info.h>

#define NUM_SIZE_SMALL (2 * 1024)
#define NUM_SIZE_MIDDLE (8 * 1024)
#define NUM_SIZE_LARGE (20 * 1024)

#define BACKGROUND_SIZE (176 * 1024)

char num_small[10][NUM_SIZE_SMALL];
char num_middle[10][NUM_SIZE_MIDDLE];
char num_large[10][NUM_SIZE_LARGE];

struct slpt_app_res num_res[] = {
	SLPT_RES_ARR_DEF("num_large_0", num_large[0]),
	SLPT_RES_ARR_DEF("num_large_1", num_large[1]),
	SLPT_RES_ARR_DEF("num_large_2", num_large[2]),
	SLPT_RES_ARR_DEF("num_large_3", num_large[3]),
	SLPT_RES_ARR_DEF("num_large_4", num_large[4]),
	SLPT_RES_ARR_DEF("num_large_5", num_large[5]),
	SLPT_RES_ARR_DEF("num_large_6", num_large[6]),
	SLPT_RES_ARR_DEF("num_large_7", num_large[7]),
	SLPT_RES_ARR_DEF("num_large_8", num_large[8]),
	SLPT_RES_ARR_DEF("num_large_9", num_large[9]),

	SLPT_RES_ARR_DEF("num_middle_0", num_middle[0]),
	SLPT_RES_ARR_DEF("num_middle_1", num_middle[1]),
	SLPT_RES_ARR_DEF("num_middle_2", num_middle[2]),
	SLPT_RES_ARR_DEF("num_middle_3", num_middle[3]),
	SLPT_RES_ARR_DEF("num_middle_4", num_middle[4]),
	SLPT_RES_ARR_DEF("num_middle_5", num_middle[5]),
	SLPT_RES_ARR_DEF("num_middle_6", num_middle[6]),
	SLPT_RES_ARR_DEF("num_middle_7", num_middle[7]),
	SLPT_RES_ARR_DEF("num_middle_8", num_middle[8]),
	SLPT_RES_ARR_DEF("num_middle_9", num_middle[9]),

	SLPT_RES_ARR_DEF("num_small_0", num_small[0]),
	SLPT_RES_ARR_DEF("num_small_1", num_small[1]),
	SLPT_RES_ARR_DEF("num_small_2", num_small[2]),
	SLPT_RES_ARR_DEF("num_small_3", num_small[3]),
	SLPT_RES_ARR_DEF("num_small_4", num_small[4]),
	SLPT_RES_ARR_DEF("num_small_5", num_small[5]),
	SLPT_RES_ARR_DEF("num_small_6", num_small[6]),
	SLPT_RES_ARR_DEF("num_small_7", num_small[7]),
	SLPT_RES_ARR_DEF("num_small_8", num_small[8]),
	SLPT_RES_ARR_DEF("num_small_9", num_small[9]),
};
SLPT_REGISTER_RES_ARR(slpt_num_res, num_res);

struct num_font_struct {
	void *filebufs;
	unsigned int size;
	struct color_mode *colors[10];
};

#define NUM_FONT_DEF(arr)						\
	{											\
		.filebufs = arr,						\
		.size = sizeof((arr)[0]),				\
	}

struct num_font_struct num_fonts[NUM_FONTS] = {
	[NUM_FONT_LARGE] = NUM_FONT_DEF(num_large),
	[NUM_FONT_MIDDEL] = NUM_FONT_DEF(num_middle),
	[NUM_FONT_SMALL] = NUM_FONT_DEF(num_small),
};

static struct color_mode *num_font_get_color_mode(int num, int font) {
	bmp_file_to_color_mode(num_fonts[font].filebufs + num * num_fonts[font].size, &num_fonts[font].colors[num]);
	return num_fonts[font].colors[num];
}

int draw_num(struct fb_struct *fbs, struct position *pos, int num, int font) {
	struct color_mode *color = NULL;

	if (num < 0 || num > 9 || font < 0 || font >= NUM_FONTS) {
		return -EINVAL;
	}

	color = num_font_get_color_mode(num, font);

	if (color)
		write_colors_to_fb_position_alpha(fbs, pos, color);
	else
		return -ENOMEM;

	return 0;
}

int draw_num_continue(struct fb_struct *fbs, struct position *pos, int num, int font) {
	struct color_mode *color = NULL;

	if (num < 0 || num > 9 || font != 0) {
		return -EINVAL;
	}

	color = num_font_get_color_mode(num, font);

	pr_info("pos: (x, y) : (%d, %d)\n", pos->x, pos->y);

	if (color) {
		write_colors_to_fb_position_alpha(fbs, pos, color);
		position_add_colors(pos, pos, color);
		return 0;
	}
	return -ENOMEM;
}

int draw_int(struct fb_struct *fbs, struct position *pos, int value, int font) {
	int div = value < 100 ? 10 : value < 1000 ? 100 : value < 10000 ? 1000 : 1000000000;
	int max_finded = 0;
	int num;
	int ret;
	struct position p = *pos;

	do {
		num = value / div;
		value = value % div;
		if (num != 0 || max_finded) {
			max_finded = 1;
			ret = draw_num_continue(fbs, &p, num, font);
			if (ret) {
				pr_err("draw_int: Failed to draw num: %d\n", num);
				return ret;
			}
		}
		div = div / 10;
	} while (div >= 1);

	return 0;
}
