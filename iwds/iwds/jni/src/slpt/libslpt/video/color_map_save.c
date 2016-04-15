#include <common.h>
#include <color_map_save.h>
#include <fb_struct.h>

extern void color_map_save_clear(struct color_map_save *cms) {
	cms->offset = 0;
}

struct color_map_save *alloc_color_map_save(int capacity) {
	struct color_map_save *cms;

	if (capacity < 1)
		capacity = 1;

	cms = malloc(sizeof(*cms));
	if (!cms)
		return NULL;

	cms->colors = malloc(capacity * sizeof(int));
	if (!cms->colors) {
		free(cms);
		return NULL;
	}

	cms->length = capacity;
	cms->offset = 0;

	return cms;
}

struct color_map_save *alloc_color_map_save_by_refer(struct fb_region *region, struct color_map *s) {
	int capacity;

	if (region)
		capacity = region->xres * region->yres / 3;
	else if (s)
		capacity = color_map_capacity(s) / 4;
	else
		capacity = 4;

	return alloc_color_map_save(capacity);
}

void free_color_map_save(struct color_map_save *cms) {
	if (cms) {
		if (cms->colors)
			free(cms->colors);
		free(cms);
	}
}

int color_map_save_ensure_capacity_inner(struct color_map_save *cms, int need_size) {
	int new_size;
	void *buffer;

	new_size = (cms->length * 3) / 2;
	if ((new_size - cms->offset) < need_size)
		new_size = ((cms->length + need_size) * 3) / 2;

	buffer = realloc(cms->colors, new_size * sizeof(int));
	if (buffer == NULL)
		return -ENOMEM;

	cms->colors = buffer;
	cms->length = new_size;

	return 0;
}

#define COLOR_MAP_SAVE_COPY(src,length)                                       \
do {                                                                          \
  struct color_pos_save *mccolors = (struct color_pos_save *) src;            \
  long mctmp = (length), mcn;                                                 \
  if (mctmp < 16) mcn = 0; else { mcn = (mctmp-1)/16; mctmp %= 16; }          \
  switch (mctmp) {                                                            \
  case 0: for(;;) {   *mccolors->addr = mccolors->color; mccolors++;          \
    case 15:          *mccolors->addr = mccolors->color; mccolors++;          \
    case 14:          *mccolors->addr = mccolors->color; mccolors++;          \
    case 13:          *mccolors->addr = mccolors->color; mccolors++;          \
    case 12:          *mccolors->addr = mccolors->color; mccolors++;          \
    case 11:          *mccolors->addr = mccolors->color; mccolors++;          \
    case 10:          *mccolors->addr = mccolors->color; mccolors++;          \
    case 9:           *mccolors->addr = mccolors->color; mccolors++;          \
    case 8:           *mccolors->addr = mccolors->color; mccolors++;          \
    case 7:           *mccolors->addr = mccolors->color; mccolors++;          \
    case 6:           *mccolors->addr = mccolors->color; mccolors++;          \
    case 5:           *mccolors->addr = mccolors->color; mccolors++;          \
    case 4:           *mccolors->addr = mccolors->color; mccolors++;          \
    case 3:           *mccolors->addr = mccolors->color; mccolors++;          \
    case 2:           *mccolors->addr = mccolors->color; mccolors++;          \
    case 1:           *mccolors->addr = mccolors->color; mccolors++; break; } \
  }                                                                           \
  for ( ; mcn > 0; mcn--) {                                                   \
                      *mccolors->addr = mccolors->color; mccolors++;          \
                      *mccolors->addr = mccolors->color; mccolors++;          \
                      *mccolors->addr = mccolors->color; mccolors++;          \
                      *mccolors->addr = mccolors->color; mccolors++;          \
                      *mccolors->addr = mccolors->color; mccolors++;          \
                      *mccolors->addr = mccolors->color; mccolors++;          \
                      *mccolors->addr = mccolors->color; mccolors++;          \
                      *mccolors->addr = mccolors->color; mccolors++;          \
                      *mccolors->addr = mccolors->color; mccolors++;          \
                      *mccolors->addr = mccolors->color; mccolors++;          \
                      *mccolors->addr = mccolors->color; mccolors++;          \
                      *mccolors->addr = mccolors->color; mccolors++;          \
                      *mccolors->addr = mccolors->color; mccolors++;          \
                      *mccolors->addr = mccolors->color; mccolors++;          \
                      *mccolors->addr = mccolors->color; mccolors++;          \
                      *mccolors->addr = mccolors->color; mccolors++;          \
  }                                                                           \
} while(0)

void restore_color_map_save(struct color_map_save *save) {
	COLOR_MAP_SAVE_COPY(save->colors, save->offset);
}
