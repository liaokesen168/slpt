#ifndef _COLOR_MAP_H_
#define _COLOR_MAP_H_
#ifdef __cplusplus
extern "C" {
#endif

#define TYPICAL_COLOR_POS_SIZE 8
#define COLOR_POS_INVALID_X -3000
#define COLOR_POS_INVALID_Y -3000

struct color_pos {
	int x;
	int y;
	int len;
	unsigned int array[0];
};

struct color_map {
	unsigned char *buffer;
	unsigned char *offset;
	unsigned char *end;
	struct color_pos *cur_color;
};

struct fb_region;

extern void color_map_clear(struct color_map *cm);
extern struct color_map *alloc_color_map(int capacity);
extern struct color_map *alloc_color_map_by_refer(struct fb_region *region, struct color_map *s0);
extern void free_color_map(struct color_map *cm);
extern int color_map_ensure_capacity_inner(struct color_map *cm, int need_size);

static inline int color_map_capacity(struct color_map *cm) {
	return cm->end - cm->buffer;
}

static inline int color_map_size(struct color_map *cm) {
	return cm->offset - cm->buffer;
}

static inline int color_map_ensure_capacity(struct color_map *cm, int need_size) {
	return (((cm->end - cm->offset) < need_size) &&
			color_map_ensure_capacity_inner(cm, need_size));
}

static inline void color_map_add(struct color_map *cm, int x, int y, unsigned int color) {
	if ((y == cm->cur_color->y) && (x == (cm->cur_color->x + cm->cur_color->len))) {
		if (color_map_ensure_capacity(cm, sizeof(int)))
			return;

		cm->cur_color->array[cm->cur_color->len++] = color;
		cm->offset += sizeof(int);
	} else {
		if (color_map_ensure_capacity(cm, sizeof(struct color_pos) + sizeof(int)))
			return;

		cm->cur_color = (void *)cm->offset;
		cm->cur_color->x = x;
		cm->cur_color->y = y;
		cm->cur_color->array[0] = color;
		cm->cur_color->len = 1;
		cm->offset += sizeof(struct color_pos) + sizeof(int);
	}
}

#ifdef __cplusplus
}
#endif
#endif /* _COLOR_MAP_H_ */

