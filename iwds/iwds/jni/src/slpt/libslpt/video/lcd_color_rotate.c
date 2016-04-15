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

#include <fb_struct.h>
#include <sin_cos_int.h>

void color_rotate(struct fb_struct *fbs, struct color_mode *colors, int angle) {
	struct fb_region dst, src;
	struct rotate_desc rotate;

	if (!fbs || !colors) {
		pr_err("%s: Invalid args\n", __FUNCTION__);
		return;
	}

	FBS_TO_REGION(*fbs, dst);
	COLORS_TO_REGION(*colors, src);
	ROTATE_REGION_CENTER(rotate, dst, src, angle);

	region_rotate(&dst, &src, &rotate);
}

void color_rotate_alpha(struct fb_struct *fbs, struct color_mode *colors, int angle) {
	struct fb_region dst, src;
	struct rotate_desc rotate;

	if (!fbs || !colors) {
		pr_err("%s: Invalid args\n", __FUNCTION__);
		return;
	}

	FBS_TO_REGION(*fbs, dst);
	COLORS_TO_REGION(*colors, src);
	ROTATE_REGION_CENTER(rotate, dst, src, angle);

	region_rotate_alpha(&dst, &src, &rotate);
}

void color_rotate_alpha_save(struct fb_struct *fbs, struct color_mode *colors, int angle,
	struct color_map **s, struct color_map *s0) {
	struct fb_region dst, src;
	struct rotate_desc rotate;

	if (!fbs || !colors) {
		pr_err("%s: Invalid args\n", __FUNCTION__);
		return;
	}

	FBS_TO_REGION(*fbs, dst);
	COLORS_TO_REGION(*colors, src);
	ROTATE_REGION_CENTER(rotate, dst, src, angle);

	region_rotate_alpha_save(&dst, &src, &rotate, s, s0);
}

void color_rotate_alpha_save_both(struct fb_struct *fbs, struct color_mode *colors, int angle,
	struct color_map **s_src, struct color_map **s_dst,
	struct color_map *s0) {
	struct fb_region dst, src;
	struct rotate_desc rotate;

	if (!fbs || !colors) {
		pr_err("%s: Invalid args\n", __FUNCTION__);
		return;
	}

	FBS_TO_REGION(*fbs, dst);
	COLORS_TO_REGION(*colors, src);
	ROTATE_REGION_CENTER(rotate, dst, src, angle);

	region_rotate_alpha_save_both(&dst, &src, &rotate, s_src, s_dst, s0);
}

void write_color_map_to_fb(struct fb_struct *fbs, struct color_map *s, int quad) {
	struct position pos;
	struct fb_region region;

	if (!fbs || !s) {
		pr_err("%s: Invalid args\n", __FUNCTION__);
		return;
	}

	FBS_TO_REGION(*fbs, region);
	POSTION_REGION_CENTER(pos, region);
	write_color_map_to_region(&region, s, &pos, quad);
}

void save_color_map_of_fb(struct fb_struct *fbs, struct color_map *s_src, struct color_map **s_dst, int quad) {
	struct position pos;
	struct fb_region region;

	if (!fbs || !s_src) {
		pr_err("%s: Invalid args\n", __FUNCTION__);
		return;
	}

	FBS_TO_REGION(*fbs, region);
	POSTION_REGION_CENTER(pos, region);
	save_color_map_of_region(&region, s_src, s_dst, &pos, quad);
}

void write_color_map_to_fb_save(struct fb_struct *fbs, struct color_map *s_src, struct color_map **s_dst, int quad) {
	struct position pos;
	struct fb_region region;

	if (!fbs || !s_src) {
		pr_err("%s: Invalid args\n", __FUNCTION__);
		return;
	}

	FBS_TO_REGION(*fbs, region);
	POSTION_REGION_CENTER(pos, region);
	write_color_map_to_region_save(&region, s_src, s_dst, &pos, quad);
}
