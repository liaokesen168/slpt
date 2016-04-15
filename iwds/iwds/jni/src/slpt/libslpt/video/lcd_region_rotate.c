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
#include <stdlib.h>
#include <stdio.h>

#include <common.h>
#include <fb_struct.h>
#include <sin_cos_int.h>

static inline void print_region_info(struct fb_region *region) {
	pr_info ("x,y (%d %d)\n", region->xres, region->yres);
	pr_info ("line_len, base(%d %p)\n", region->pixels_per_line, region->base);
}

static inline void print_rotate_info(struct rotate_desc *rotate) {
	pr_info ("dst_x, dst_y, (%d %d)\n", rotate->dst.x, rotate->dst.y);
	pr_info ("src_x, src_y, (%d %d)\n", rotate->src.x, rotate->src.y);
}

void region_rotate(struct fb_region *dst, struct fb_region *src, struct rotate_desc *rotate) {
	int height = dst->yres;
	int width = dst->xres;
	int yres = src->yres;
	int xres = src->xres;
	int pixels_per_line1 = dst->pixels_per_line;
	int pixels_per_line2 = src->pixels_per_line;
	unsigned int *dstp = (void *)dst->base, *srcp = (void *)src->base;

	int after_i, after_j, i, j, pre_i, pre_j;
	int midX_aft = rotate->dst.x, midY_aft = rotate->dst.y;
	int midX_pre = rotate->src.x, midY_pre = rotate->src.y;

	int angle = rotate->angle;

	for(i = 0;i < height;++i) {
		for(j = 0;j < width;++j) {
			after_i = i - midY_aft;
			after_j = j - midX_aft;
			pre_j = (cos_int(angle) * after_i - sin_int(angle) * after_j) / SIN_COS_DIVIDER + midY_pre;
			pre_i = (sin_int(angle) * after_i + cos_int(angle) * after_j) / SIN_COS_DIVIDER + midX_pre;

			if(pre_j >= 0 && pre_j < yres && pre_i >= 0 && pre_i < xres) {
				dstp[i * pixels_per_line1 + j] = srcp[pre_j * pixels_per_line2 + pre_i];
			}
		}
	}
}

void region_rotate_alpha(struct fb_region *dst, struct fb_region *src, struct rotate_desc *rotate) {
	int height = dst->yres;
	int width = dst->xres;
	int yres = src->yres;
	int xres = src->xres;
	int pixels_per_line1 = dst->pixels_per_line;
	int pixels_per_line2 = src->pixels_per_line;
	unsigned int *dstp = (void *)dst->base, *srcp = (void *)src->base;

	int after_i, after_j, i, j, pre_i, pre_j;
	int midX_aft = rotate->dst.x, midY_aft = rotate->dst.y;
	int midX_pre = rotate->src.x, midY_pre = rotate->src.y;

	int angle = rotate->angle;

	for(i = 0;i < height;++i) {
		for(j = 0;j < width;++j) {
			after_i = i - midY_aft;
			after_j = j - midX_aft;
			pre_j = (cos_int(angle) * after_i - sin_int(angle) * after_j) / SIN_COS_DIVIDER + midY_pre;
			pre_i = (sin_int(angle) * after_i + cos_int(angle) * after_j) / SIN_COS_DIVIDER + midX_pre;
			if(pre_j >= 0 && pre_j < yres && pre_i >= 0 && pre_i < xres)
				if (srcp[pre_j * pixels_per_line2 + pre_i] != ALPHA32BIT)
					dstp[i * pixels_per_line1 + j] = srcp[pre_j * pixels_per_line2 + pre_i];
		}
	}
}

void region_rotate_alpha_save(struct fb_region *dst, struct fb_region *src, struct rotate_desc *rotate,
	struct color_map **s, struct color_map *s0) {
	int height = dst->yres;
	int width = dst->xres;
	int yres = src->yres;
	int xres = src->xres;
	int pixels_per_line2 = src->pixels_per_line;
	unsigned int *srcp = (void *)src->base;

	int after_i, after_j, i, j, pre_i, pre_j;
	int midX_aft = rotate->dst.x, midY_aft = rotate->dst.y;
	int midX_pre = rotate->src.x, midY_pre = rotate->src.y;

	int angle = rotate->angle;

	if (!*s) {
		*s = alloc_color_map_by_refer(src, s0);
		if (!*s) {
			pr_err("color-rotate: failed to allocate color save\n");
			return;
		}
	}
	color_map_clear(*s);

	for(i = 0;i < height;++i) {
		for(j = 0;j < width;++j) {
			after_i = i - midY_aft;
			after_j = j - midX_aft;
			pre_j = (cos_int(angle) * after_i - sin_int(angle) * after_j) / SIN_COS_DIVIDER + midY_pre;
			pre_i = (sin_int(angle) * after_i + cos_int(angle) * after_j) / SIN_COS_DIVIDER + midX_pre;
			if(pre_j >= 0 && pre_j < yres && pre_i >= 0 && pre_i < xres)
				if (srcp[pre_j * pixels_per_line2 + pre_i] != ALPHA32BIT)
					color_map_add(*s, j, i, srcp[pre_j * pixels_per_line2 + pre_i]);
		}
	}
}

void region_rotate_alpha_save_both(struct fb_region *dst, struct fb_region *src, struct rotate_desc *rotate,
	struct color_map **s_src, struct color_map **s_dst, struct color_map *s0) {
	int height = dst->yres;
	int width = dst->xres;
	int yres = src->yres;
	int xres = src->xres;
	int pixels_per_line1 = dst->pixels_per_line;
	int pixels_per_line2 = src->pixels_per_line;
	unsigned int *dstp = (void *)dst->base, *srcp = (void *)src->base;

	int after_i, after_j, i, j, pre_i, pre_j;
	int midX_aft = rotate->dst.x, midY_aft = rotate->dst.y;
	int midX_pre = rotate->src.x, midY_pre = rotate->src.y;

	int angle = rotate->angle;

	if (!*s_src) {
		*s_src = alloc_color_map_by_refer(src, s0);
		if (!*s_src) {
			pr_err("color-rotate: failed to allocate color save\n");
			return;
		}
	}

	if (!*s_dst) {
		*s_dst = alloc_color_map_by_refer(src, s0);
		if (!*s_dst) {
			pr_err("color-rotate: failed to allocate color save\n");
			return;
		}
	}

	color_map_clear(*s_src);
	color_map_clear(*s_dst);

	for(i = 0;i < height;++i) {
		for(j = 0;j < width;++j) {
			after_i = i - midY_aft;
			after_j = j - midX_aft;
			pre_j = (cos_int(angle) * after_i - sin_int(angle) * after_j) / SIN_COS_DIVIDER + midY_pre;
			pre_i = (sin_int(angle) * after_i + cos_int(angle) * after_j) / SIN_COS_DIVIDER + midX_pre;
			if(pre_j >= 0 && pre_j < yres && pre_i >= 0 && pre_i < xres)
				if (srcp[pre_j * pixels_per_line2 + pre_i] != ALPHA32BIT) {
					color_map_add(*s_src, j, i, srcp[pre_j * pixels_per_line2 + pre_i]);
					color_map_add(*s_dst, j, i, dstp[i * pixels_per_line1 + j]);
				}
		}
	}
}

#define QUAD0(x, y, xoff, yoff, line_len) \
	((y) * (line_len) + (x))

#define QUAD1(x, y, xoff, yoff, line_len) \
	(((xoff) + (yoff) - (x)) * (line_len) + ((xoff) - (yoff)  + (y)))

#define QUAD2(x, y, xoff, yoff, line_len) \
	((2 * (yoff)  - (y)) * (line_len) + (2 * (xoff) - (x)))

#define QUAD3(x, y, xoff, yoff, line_len) \
	(((yoff) - (xoff) + (x)) * (line_len) + ((xoff) + (yoff) - (y)))

void write_color_map_to_region(struct fb_region *region, struct color_map *s, struct position *pos, int quad) {
#if 0
	int i;
	struct color_pos *save = s->save;
	int length = s->real_length;
	unsigned int *dstp = region->base;
	unsigned int pixels_per_line  = region->pixels_per_line;
	int yoff = pos->y;
	int xoff = pos->x;

	switch (quad) {
	case 0:
		for (i = 0; i < length; ++i) {
			dstp[QUAD0(save[i].x, save[i].y, xoff, yoff, pixels_per_line)] = save[i].color;
		}
		break;

	case 1:
		for (i = 0; i < length; ++i) {
			dstp[QUAD1(save[i].x, save[i].y, xoff, yoff, pixels_per_line)] = save[i].color;
		}
		break;

	case 2:
		for (i = 0; i < length; ++i) {
			dstp[QUAD2(save[i].x, save[i].y, xoff, yoff, pixels_per_line)] = save[i].color;
		}
		break;

	case 3:
		for (i = 0; i < length; ++i) {
			dstp[QUAD3(save[i].x, save[i].y, xoff, yoff, pixels_per_line)] = save[i].color;
		}
		break;
	}
#endif
}

#define COLOR_ADD_QUAD0(s_dst, x, y, color, xoff, yoff)	\
	color_map_add(s_dst, x, y, color);

#define COLOR_ADD_QUAD1(s_dst, x, y, color, xoff, yoff)						\
	color_map_add(s_dst, (xoff) - (yoff) + y, (xoff) + (yoff) - x, color);

#define COLOR_ADD_QUAD2(s_dst, x, y, color, xoff, yoff)						\
	color_map_add(s_dst, 2 * (xoff) - x, 2 * (yoff) - y, color);

#define COLOR_ADD_QUAD3(s_dst, x, y, color, xoff, yoff)						\
	color_map_add(s_dst, (xoff) + (yoff) - y, (yoff) - (xoff) + x, color);

void save_color_map_of_region(struct fb_region *region, struct color_map *s_src, struct color_map **s_dst, struct position *pos, int quad) {
#if 0
	int i;
	struct color_pos *save = s_src->save;
	int length = s_src->real_length;
	unsigned int *dstp = region->base;
	unsigned int *dstsave;
	unsigned int pixels_per_line  = region->pixels_per_line;
	int yoff = pos->y;
	int xoff = pos->x;

	if (!*s_dst) {
		*s_dst = alloc_color_map_by_refer(NULL, s_src);
		if (!*s_dst) {
			pr_err("color-rotate: failed to allocate color save\n");
			return;
		}
	}

	color_map_clear(*s_dst);

	switch (quad) {
	case 0:
		for (i = 0; i < length; ++i) {
			dstsave = &dstp[QUAD0(save[i].x, save[i].y, xoff, yoff, pixels_per_line)];
			COLOR_ADD_QUAD0(*s_dst, save[i].x, save[i].y, *dstsave, xoff, yoff);
		}
		break;

	case 1:
		for (i = 0; i < length; ++i) {
			dstsave = &dstp[QUAD1(save[i].x, save[i].y, xoff, yoff, pixels_per_line)];
			COLOR_ADD_QUAD1(*s_dst, save[i].x, save[i].y, *dstsave, xoff, yoff);
		}
		break;

	case 2:
		for (i = 0; i < length; ++i) {
			dstsave = &dstp[QUAD2(save[i].x, save[i].y, xoff, yoff, pixels_per_line)];
			COLOR_ADD_QUAD2(*s_dst, save[i].x, save[i].y, *dstsave, xoff, yoff);
		}
		break;

	case 3:
		for (i = 0; i < length; ++i) {
			dstsave = &dstp[QUAD3(save[i].x, save[i].y, xoff, yoff, pixels_per_line)];
			COLOR_ADD_QUAD3(*s_dst, save[i].x, save[i].y, *dstsave, xoff, yoff);
		}
		break;
	}
#endif
}

void write_color_map_to_region_save(struct fb_region *region, struct color_map *s_src, struct color_map **s_dst, struct position *pos, int quad) {
#if 0
	int i;
	struct color_pos *save = s_src->save;
	int length = s_src->real_length;
	unsigned int *dstp = region->base;
	unsigned int *dstsave;
	unsigned int pixels_per_line  = region->pixels_per_line;
	int yoff = pos->y;
	int xoff = pos->x;

	if (!*s_dst) {
		*s_dst = alloc_color_map_by_refer(NULL, s_src);
		if (!*s_dst) {
			pr_err("color-rotate: failed to allocate color save\n");
			return;
		}
	}

	color_map_clear(*s_dst);

	switch (quad) {
	case 0:
		for (i = 0; i < length; ++i) {
			dstsave = &dstp[QUAD0(save[i].x, save[i].y, xoff, yoff, pixels_per_line)];
			COLOR_ADD_QUAD0(*s_dst, save[i].x, save[i].y, *dstsave, xoff, yoff);
			*dstsave = save[i].color;
		}
		break;

	case 1:
		for (i = 0; i < length; ++i) {
			dstsave = &dstp[QUAD1(save[i].x, save[i].y, xoff, yoff, pixels_per_line)];
			COLOR_ADD_QUAD1(*s_dst, save[i].x, save[i].y, *dstsave, xoff, yoff);
			*dstsave = save[i].color;
		}
		break;

	case 2:
		for (i = 0; i < length; ++i) {
			dstsave = &dstp[QUAD2(save[i].x, save[i].y, xoff, yoff, pixels_per_line)];
			COLOR_ADD_QUAD2(*s_dst, save[i].x, save[i].y, *dstsave, xoff, yoff);
			*dstsave = save[i].color;
		}
		break;

	case 3:
		for (i = 0; i < length; ++i) {
			dstsave = &dstp[QUAD3(save[i].x, save[i].y, xoff, yoff, pixels_per_line)];
			COLOR_ADD_QUAD3(*s_dst, save[i].x, save[i].y, *dstsave, xoff, yoff);
			*dstsave = save[i].color;
		}
		break;
	}
#endif
}

/* we know z*z = x*x + y*y; according to x,y, get the result z */
unsigned int sqrt_xy(unsigned int x, unsigned y) {
	unsigned long long xx;
	unsigned long long yy;
	unsigned long long zz;
	unsigned int z;
	int i = 0;

	if (x == 0)
		return y;

	if (y == 0)
		return x;

	if (x < y) {
		z = x;
		x = y;
		y = z;
	}

	yy = (unsigned long long)y * (unsigned long long)y;
	xx = (unsigned long long)x * (unsigned long long)x;
	zz = xx + yy;

	z = x + (yy / x / 2) - (y / 40); /* choose the best start point */

	if (zz > ((unsigned long long)z * (unsigned long long)z)) {
		while (1) {
			z++;
			if (zz < ((unsigned long long)z * (unsigned long long)z)) {
				return z;
			}
			i++;
		}
	} else if (zz < ((unsigned long long)z * (unsigned long long)z)) {
		while (1) {
			z--;
			if (zz > ((unsigned long long)z * (unsigned long long)z)) {
				return z + 1;
			}
			i++;
		}
	} else {
		return z;
	}
}

unsigned int points_distance(int x0, int y0, int x1, int y1) {
	long long x = (long long)x0 - (long long)x1;
	long long y = (long long)y0 - (long long)y1;

	if (x < 0)
		x = 0 - x;
	if (y < 0)
		y = 0 - y;

	return sqrt_xy(x, y);
}

unsigned int rotate_measure_size(struct fb_region *src, struct position *pos) {
	int yres = src->yres;
	int xres = src->xres;
	unsigned int length;

	if (src->xres == 0 || src->yres == 0) {
		return 0;
	}

	length = sqrt_xy(xres, yres) / 2;
	length = length + points_distance(xres / 2, yres / 2, pos->x, pos->y);

	return length;
}

/* roate a fb region, and save the result, and the result is not relative to a dst region */
void rotate_region(struct fb_region *src, int angle, struct position *pos,
	unsigned int *length, struct color_map **s, struct color_map *s0) {
	int yres = src->yres;
	int xres = src->xres;
	int pixels_per_line2 = src->pixels_per_line;
	unsigned int *srcp = (void *)src->base;

	int height;
	int width;
	int midX_aft, midY_aft;
	int midX_pre, midY_pre;
	int after_i, after_j, i, j, pre_i, pre_j;

	if (!*s) {
		*s = alloc_color_map_by_refer(src, s0);
		if (!*s) {
			if (length)
				*length = 0;
			pr_err("color-rotate: failed to allocate color save\n");
			return;
		}
	}

	color_map_clear(*s);

	if (src->xres == 0 || src->yres == 0) {
		if (length)
			*length = 0;
		return;
	}

	height = sqrt_xy(xres, yres) / 2;
	height = height + points_distance(xres / 2, yres / 2, pos->x, pos->y);
	width = height;
	if (length)
		*length = height;

	midX_aft = 0, midY_aft = 0;
	midX_pre = pos->x, midY_pre = pos->y;

	pr_debug ("xres : %d yres : %d \n", xres, yres);
	pr_debug ("sqrt %d\n", sqrt_xy(xres, yres));
	pr_debug ("height : %d width %d \n", height * 2, width * 2);

	for(i = 0 - height;i < height;++i) {
		for(j = 0 - width;j < width;++j) {
			after_i = i - midY_aft;
			after_j = j - midX_aft;
			pre_j = (cos_int(angle) * after_i - sin_int(angle) * after_j) / SIN_COS_DIVIDER + midY_pre;
			pre_i = (sin_int(angle) * after_i + cos_int(angle) * after_j) / SIN_COS_DIVIDER + midX_pre;
			if(pre_j >= 0 && pre_j < yres && pre_i >= 0 && pre_i < xres)
				if (srcp[pre_j * pixels_per_line2 + pre_i] != ALPHA32BIT)
					color_map_add(*s, j, i, srcp[pre_j * pixels_per_line2 + pre_i]);
		}
	}
}
