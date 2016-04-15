#include <common.h>
#include <sview/rotate2.h>

void rotate2_measure_size(struct rotate2 *rt, struct rect *rect) {
	if (!rt->region) {
		rect->w = 0;
		rect->h = 0;
		return;
	}

	if (!rt->colors[rt->angle]) {
		rotate_region(rt->region, rt->angle, &rt->center,
					  &rt->length, &rt->colors[rt->angle], rt->colors[0]);
	}

	rect->w = rt->length * 2;
	rect->h = rt->length * 2;
}

void rotate2_draw(struct rotate2 *rt, struct fb_region *base, struct position *pos) {
	if (!rt->colors[rt->angle]) {
		rotate_region(rt->region, rt->angle, &rt->center,
					  &rt->length, &rt->colors[rt->angle], rt->colors[0]);
		if (!rt->colors[rt->angle])
			return;	
	}

	write_color_map(base, rt->colors[rt->angle], rt->length + pos->x, rt->length + pos->y, rt->quad);
}

void rotate2_set_angle(struct rotate2 *rt, unsigned int angle) {
	unsigned int quad;

	angle = angle % 360;
	quad = angle / 90;
	angle = angle % 90;
	if (quad >= 1)
		quad = 4 - quad;
	rt->angle = angle;
	rt->quad = quad;
}

void rotate2_set_center(struct rotate2 *rt, unsigned int center_x, unsigned int center_y) {
	rt->center.x = center_x;
	rt->center.y = center_y;
}

void rotate2_set_align_center(struct rotate2 *rt, unsigned int enable) {
	if (!rt->align_center && enable && rt->region) {
		rt->center.x = rt->region->xres / 2;
		rt->center.y = rt->region->yres / 2;
	}

	rt->align_center = enable;
}

void rotate2_free_save_colors(struct rotate2 *rt) {
	unsigned int i;

	for (i = 0; i < ARRAY_SIZE(rt->colors); ++i) {
		if (rt->colors[i]) {
			free_color_map(rt->colors[i]);
			rt->colors[i] = NULL;
		}
	}
}

void rotate2_set_region(struct rotate2 *rt, struct fb_region *region) {
	if (rt->region != region) {
		rt->region = region;
		rotate2_free_save_colors(rt);
		if (region && rt->align_center) {
			rt->center.x = rt->region->xres / 2;
			rt->center.y = rt->region->yres / 2;
		}
	}
}

void init_rotate2(struct rotate2 *rt) {
	memset(rt, 0, sizeof(*rt));
	rt->align_center = 1;
}
