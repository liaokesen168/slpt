#ifndef _ROTATE_H_
#define _ROTATE_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <fb_struct.h>

#define ROTATE_SAVE_INDEXS (90 + 1)

struct rotate {
	struct rotate_desc desc;
	struct color_map *handers[ROTATE_SAVE_INDEXS];
	struct color_map *save;

	struct fb_region *region;
	struct fb_region bg;
};

extern void init_rotate(struct rotate *rt);
extern void rotate_set_region(struct rotate *rt, struct fb_region *region);
extern void rotate_set_bg(struct rotate *rt, struct fb_region *bg);
extern void rotate_set_dst(struct rotate *rt, struct position *dst);
extern void rotate_set_dst_to_center(struct rotate *rt);
extern void rotate_restore(struct rotate *rt);
extern int rotate_save_and_draw(struct rotate *rt, unsigned int angle);
extern void rotate_free_save(struct rotate *rt);
extern void rotate_free_maps(struct rotate *rt);

static inline struct position *rotate_dst_center(struct rotate *rt) {
	return &rt->desc.dst;
}

static inline struct position *rotate_src_center(struct rotate *rt) {
	return &rt->desc.src;
}

#ifdef __cplusplus
}
#endif
#endif /* _ROTATE_H_ */
