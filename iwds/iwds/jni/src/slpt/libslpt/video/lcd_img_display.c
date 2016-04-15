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

#include <config.h>
#include <common.h>
#include <malloc.h>

#include <linux/err.h>

#include <command.h>
#include <slpt_app.h>

#include <fb_struct.h>
#include <linux/pr_info.h>

#include <linux/list.h>

#define INVALID_POSITON {-10000, -10000}
static inline int valid_position(struct fb_struct *fbs, struct position *pos) {
	return (pos->x >= 0 && pos->x < fbs->xres) && (pos->y >= 0 && pos->y < fbs->yres);
}

#define INVALID_IMG_INDEX 0xffffffff

static int lcd_img_display_update_status(void);

struct lcd_img_display {
	struct fb_struct *fbs;
	struct list_head handers;

	struct {
		unsigned int fb_is_ok;
		unsigned int reinit;
	} s;
};

struct lcd_img_display lcd_img_display;

int img_display_init(struct img_display *imgd, void *img, unsigned int img_size, unsigned int img_count, struct position *pos) {
	int i;
	struct position p = INVALID_POSITON;

	imgd->s.inited = 0;

	if (!img || img_size == 0 || img_count == 0) {
		pr_err("img_display: img must not empty\n");
		return -EINVAL;
	}

	imgd->colors = malloc(sizeof(struct color_mode *) * img_count);
	if (!imgd->colors) {
		pr_err("img_display: img must not empty\n");
		return -EINVAL;
	}

	for (i = 0; i < img_count; ++i) {
		imgd->colors[i] = NULL;
	}

	imgd->img = img;
	imgd->img_size =img_size;
	imgd->img_count =img_count;
	imgd->next = imgd->index = 0;
	imgd->pos = pos ? *pos : p;

	INIT_LIST_HEAD(&imgd->handers);

	imgd->s.inited = 1;
	return 0;
}

static inline struct color_mode *img_get_res_color_mode(struct img_display *imgd, unsigned int index, int *update) {
	char *file_buf = imgd->img + imgd->img_size * index;
	struct color_mode *colors;
	struct bmp_logo_info *bmp;

	if (update) *update = 0;

	if (file_buf[0] != 0 && file_buf[1] != 0) {
		bmp = bmp_deal_filebuffer(file_buf);
		if (bmp) {
			colors = bmp_to_color_mode(bmp, 4);
			if (colors) {
				if (imgd->colors[index])
					free(imgd->colors[index]);
				imgd->colors[index] = colors;
				file_buf[0] = 0;
				file_buf[1] = 0;
				if (update) *update = 1;
			}
			free_bmp(bmp);
		}
	}
	return imgd->colors[index];
}

static inline int img_display_internal(struct img_display *imgd, unsigned int index) {
	struct color_mode *colors;
	int update;

	if (!imgd || !imgd->s.inited) {
		pr_err("img_display: imgd must be inited\n");
		return -EINVAL;
	}

	colors = img_get_res_color_mode(imgd, index, &update);
	if (!colors) {
		pr_err("img_display: no vaild img find\n");
		return -ENODEV;
	}

	update = (index != imgd->index) || lcd_img_display.s.reinit || update;
	imgd->index = index;

	if (!lcd_img_display.s.fb_is_ok) {
		lcd_img_display_update_status();
		if (!lcd_img_display.s.fb_is_ok) {
			pr_err("img_display: no vailed fbs\n");
			return -ENODEV;
		}
	}

	if (!valid_position(lcd_img_display.fbs, &imgd->pos)) {
		pr_err("img_display: position not valid\n");
		return -EINVAL;
	}

	if (update)
		write_colors_to_fb_position(lcd_img_display.fbs, &imgd->pos, colors);

	return 0;
}

void img_display_set_index(struct img_display *imgd, unsigned int index) {
	if (!imgd || !imgd->s.inited) {
		pr_err("img_display: imgd must be inited\n");
		return ;
	}
	if (index < imgd->img_count) {
		imgd->next = index;
	}
}

int img_display(struct img_display *imgd) {
	return img_display_internal(imgd, imgd->next);
}

int img_display_index(struct img_display *imgd, unsigned int index) {
	img_display_set_index(imgd, index);
	return img_display_internal(imgd, imgd->next);
}

int img_display_all(struct img_display *imgd) {
	return 0;
}

void lcd_img_display_set_reinit(int reinit) {
	lcd_img_display.s.reinit = reinit;
}

static int lcd_img_display_init(void) {
	lcd_img_display.fbs = NULL;
	lcd_img_display.s.reinit = 1;
	lcd_img_display.s.fb_is_ok = 0;
	INIT_LIST_HEAD(&lcd_img_display.handers);

	return 0;
}
SLPT_CORE_INIT_ONETIME(lcd_img_display_init);

static int lcd_img_display_update_status(void) {
	struct fb_struct *fbs;

	fbs = get_default_fb();
	if (!fbs) {
		pr_err("lcd_img_display: Failed to get fbs\n");
	}
	lcd_img_display.s.fb_is_ok = !(!fbs);

	if (lcd_img_display.fbs != fbs) {
		lcd_img_display.fbs = fbs;
		lcd_img_display.s.reinit = 1;
	}

	return 0;
}
SLPT_APP_INIT_EVERYTIME(lcd_img_display_update_status);
