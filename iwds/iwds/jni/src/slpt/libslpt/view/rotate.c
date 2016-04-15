#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <rotate.h>

void init_rotate(struct rotate *rt) {
	memset(rt, 0, sizeof(*rt));
}

static void rotate_free_handers(struct rotate *rt) {
	unsigned int i;

	for (i = 0; i < ARRAY_SIZE(rt->handers); ++i) {
		if (rt->handers[i])
			free_color_map(rt->handers[i]);
		rt->handers[i] = NULL;
	}
}

void rotate_free_save(struct rotate *rt) {
	if (rt->save) {
		free_color_map(rt->save);
		rt->save = NULL;
	}
}

void rotate_free_maps(struct rotate *rt) {
	rotate_free_handers(rt);
	rotate_free_save(rt);
}

void rotate_set_region(struct rotate *rt, struct fb_region *region) {
	if (rt->region != region) {
		rt->region = region;
		/* just now only support rotate around the src center */
		POSTION_REGION_CENTER(rt->desc.src, *region)
		rotate_free_handers(rt);
	}
}

void rotate_set_bg(struct rotate *rt, struct fb_region *bg) {
	if (!region_equal(bg, &rt->bg)) {
		rt->bg = *bg;
		rotate_free_handers(rt);
	}
}

void rotate_set_dst(struct rotate *rt, struct position *dst) {
	struct rotate_desc *odesc = &rt->desc;
	if (!position_equal(&odesc->dst, dst)) {
		rt->desc.dst = *dst;
		rotate_free_handers(rt);
	}
}

void rotate_set_dst_to_center(struct rotate *rt) {
	struct position dst;

	POSTION_REGION_CENTER(dst, rt->bg);
	rotate_set_dst(rt, &dst);
}

void rotate_restore(struct rotate *rt) {
	if (rt->save)
		write_color_map_to_region(&rt->bg, rt->save, &rt->desc.dst, 0);
}

int rotate_save_and_draw(struct rotate *rt, unsigned int angle) {
	unsigned int quad;

	quad = angle / 90;
	angle = angle % 90;
	if (quad >= 1) {
		quad = 4 - quad;
	}
	rt->desc.angle = angle;

	if (!rt->handers[angle]) {
		region_rotate_alpha_save(&rt->bg, rt->region, &rt->desc, &rt->handers[angle], rt->save);
		if (!rt->handers[angle])
			return -ENOMEM;
	}

	write_color_map_to_region_save(&rt->bg, rt->handers[angle], &rt->save, &rt->desc.dst, quad);

	return 0;
}
