#ifndef _ROTATE2_H_
#define _ROTATE2_H_

#ifdef __cplusplus
extern "C" {
#endif

#include <sview/sview_base.h>

#define ROTATE2_SAVE_INDEXS (90 + 1)

struct rotate2 {
	struct color_map *colors[ROTATE2_SAVE_INDEXS];
	unsigned int length;
	unsigned int angle;
	unsigned char quad;
	unsigned char align_center;
	struct position center;
	struct fb_region *region;
};

extern void rotate2_measure_size(struct rotate2 *rt, struct rect *rect);
extern void rotate2_draw(struct rotate2 *rt, struct fb_region *base, struct position *pos);
extern void rotate2_set_angle(struct rotate2 *rt, unsigned int angle);
extern void rotate2_set_center(struct rotate2 *rt, unsigned int center_x, unsigned int center_y);
extern void rotate2_set_align_center(struct rotate2 *rt, unsigned int enable);
extern void rotate2_free_save_colors(struct rotate2 *rt);
extern void rotate2_set_region(struct rotate2 *rt, struct fb_region *region);
extern void init_rotate2(struct rotate2 *rt);

#ifdef __cplusplus
}
#endif

#endif /* _ROTATE2_H_ */
