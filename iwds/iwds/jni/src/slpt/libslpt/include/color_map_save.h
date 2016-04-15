#ifndef _COLOR_MAP_SAVE_H_
#define _COLOR_MAP_SAVE_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <color_map.h>

struct color_pos_save {
	unsigned int *addr;
	unsigned int color;
};

struct color_map_save {
	int length;
	int offset;
	struct color_pos_save *colors;	
};

extern void color_map_save_clear(struct color_map_save *cms);
extern struct color_map_save *alloc_color_map_save(int capacity);
extern struct color_map_save *alloc_color_map_save_by_refer(struct fb_region *region, struct color_map *s);
extern void free_color_map_save(struct color_map_save *cms);
	extern int color_map_save_ensure_capacity_inner(struct color_map_save *cms, int need_size);

static inline int color_map_save_capacity(struct color_map_save *cms) {
	return cms->length;
}

static inline int color_map_save_size(struct color_map_save *cms) {
	return cms->offset;
}

static inline int color_map_save_ensure_capacity(struct color_map_save *cms, int need_size) {
	return (((cms->length - cms->offset) < need_size) &&
			color_map_save_ensure_capacity_inner(cms, need_size));
}

#ifdef __cplusplus
}
#endif
#endif /* _COLOR_MAP_SAVE_H_ */
