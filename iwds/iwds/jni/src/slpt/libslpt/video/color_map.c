#include <common.h>
#include <malloc.h>
#include <color_map.h>
#include <fb_struct.h>
#include <color_map_save.h>

void color_map_clear(struct color_map *cm) {
	cm->cur_color = (void *)cm->buffer;
	cm->cur_color->x = COLOR_POS_INVALID_X;
	cm->cur_color->y = COLOR_POS_INVALID_Y;
	cm->cur_color->len = 0;
	cm->offset = cm->buffer + sizeof(struct color_pos);
}

struct color_map *alloc_color_map(int capacity) {
	struct color_map *cm;

	if (capacity < 1)
		capacity = 1;

	cm = malloc(sizeof(*cm));
	if (!cm)
		return NULL;

	capacity = sizeof(struct color_pos) + capacity;
	cm->buffer = malloc(capacity);
	if (!cm->buffer) {
		free(cm);
		return NULL;
	}

	cm->end = cm->buffer + capacity;
	color_map_clear(cm);

	return cm;
}

struct color_map *alloc_color_map_by_refer(struct fb_region *region, struct color_map *s0) {
	int capacity;

	if (s0)
		capacity = color_map_capacity(s0);
	else if (region)
		capacity = region->xres * region->yres / 3;
	else
		capacity = 16;

	return alloc_color_map(capacity);
}

void free_color_map(struct color_map *cm) {
	if (cm) {
		if (cm->buffer)
			free(cm->buffer);
		free(cm);
	}
}

int color_map_ensure_capacity_inner(struct color_map *cm, int need_size) {
	int new_size;
	int offset;
	int size;
	int cur_color_offset;
	unsigned char *buffer;

	offset = cm->offset - cm->buffer;
	size = cm->end - cm->buffer;
	cur_color_offset = (char *)cm->cur_color - (char *)cm->buffer;

	new_size = (size * 3) / 2;
	if ((new_size - offset) < need_size)
		new_size = ((size + need_size) * 3 ) / 2;

	buffer = realloc(cm->buffer, new_size);
	if (buffer == NULL)
		return -ENOMEM;

	cm->buffer = buffer;
	cm->offset = buffer + offset;
	cm->end = buffer + new_size;
	cm->cur_color = (void *)(buffer + cur_color_offset);

	return 0;
}

#define color_map_copy_common(nwords, exp)                       \
do {                                                             \
  int mctmp = (nwords), mcn;                                     \
  if (mctmp < 8) mcn = 0; else { mcn = mctmp/8; mctmp %= 8; }    \
  switch (mctmp) {                                               \
    case 7:           exp;                                       \
    case 6:           exp;                                       \
    case 5:           exp;                                       \
    case 4:           exp;                                       \
    case 3:           exp;                                       \
    case 2:           exp;                                       \
    case 1:           exp;                                       \
    case 0:           break;                                     \
  }                                                              \
  for ( ; mcn > 0; mcn--) {                                      \
    exp; exp; exp; exp; exp; exp; exp; exp;                      \
  }                                                              \
} while(0)

#define COLOR_MAP_COPY0(dest,src,nwords)                         \
do {                                                             \
  unsigned int* mcsrc = (unsigned int*) (src);                   \
  unsigned int* mcdst = (unsigned int*) (dest);                  \
                                                                 \
  color_map_copy_common(nwords, {*mcdst++ = *mcsrc++;});         \
} while(0)

#define COLOR_MAP_COPY2(dest,src,nwords)                         \
do {                                                             \
  unsigned int* mcsrc = (unsigned int*) (src);                   \
  unsigned int* mcdst = (unsigned int*) (dest);                  \
                                                                 \
  color_map_copy_common(nwords, {*mcdst++ = *mcsrc--;});         \
} while(0)

#define COLOR_MAP_COPY1(dest,src,nwords,step)                            \
do {                                                                     \
  unsigned int* mcsrc = (unsigned int*) (src);                           \
  unsigned int* mcdst = (unsigned int*) (dest);                          \
  unsigned int mcstep = (unsigned int) (step);                           \
                                                                         \
  color_map_copy_common(nwords, {*mcdst = *mcsrc++; mcdst += mcstep;});  \
} while(0)

#define COLOR_MAP_COPY3(dest,src,nwords,step)                            \
do {                                                                     \
  unsigned int* mcsrc = (unsigned int*) (src);                           \
  unsigned int* mcdst = (unsigned int*) (dest);                          \
  unsigned int mcstep = (unsigned int) (step);                           \
                                                                         \
  color_map_copy_common(nwords, {*mcdst = *mcsrc--; mcdst += mcstep;});  \
} while(0)

#define for_each_color(color_pos, cm) \
    for (color = (void *)cm->buffer; (void *)color != (void *)cm->offset; color = (void *)&color->array[color->len])

void write_color_map(struct fb_region *region, struct color_map *s, int x_offset, int y_offset, int quad) {
	struct color_pos *color;
	unsigned int *base = region->base;
	unsigned int pixels_per_line  = region->pixels_per_line;
	int xres = region->xres;
	int yres = region->yres;
	int x, y;
	int x_end;
	int y_end;
	int len;
	unsigned int *src;
	unsigned int *dst;

	switch (quad) {
	case 0:
		for_each_color(color, s) {
			y = color->y + y_offset;
			if (y >= 0 && y < yres) {
				x = color->x + x_offset;
				x_end = color->x + color->len + x_offset;
				if (x_end > 0 && x < xres) {
					if (x >= 0) {
						src = color->array;
						dst = base + y * pixels_per_line + x;
					} else {
						src = color->array + (0 - x);
						dst = base + y * pixels_per_line;
					}
					if (x_end <= xres)
						len = color->len;
					else
						len = color->len - (x_end - xres);
					COLOR_MAP_COPY0(dst, src, len);
				}
			}
		}
		pr_debug ("quad 0\n");
		break;

	case 1:
		for_each_color(color, s) {
			x = color->y + x_offset;
			if (x >= 0 && x < xres) {
				y = 0 - (color->x + color->len) + 1 + y_offset;
				y_end = 0 - color->x + 1 + y_offset;
				if (y_end > 0 && y < yres) {
					if (y >= 0) {
						src = color->array + color->len - 1;
						dst = base + y * pixels_per_line + x;
					} else {
						src = color->array + color->len - (0 - y) - 1;
						dst = base + x;
					}
					if (y_end <= yres)
						len = color->len;
					else
						len = color->len - (y_end - yres);
					COLOR_MAP_COPY3(dst, src, len, pixels_per_line);
				}
			}
		}
		pr_debug ("quad 1\n");
		break;

	case 2:
		for_each_color(color, s) {
			y = 0 - color->y + y_offset;
			if (y >= 0 && y < yres) {
				x = 0 - (color->x + color->len) + 1 + x_offset;
				x_end = 0 - color->x + 1 + x_offset;
				if (x_end > 0 && x < xres) {
					if (x >= 0) {
						src = color->array + color->len - 1;
						dst = base + y * pixels_per_line + x;
					} else {
						src = color->array + color->len - 1 - (0 - x);
						dst = base + y * pixels_per_line;
					}
					if (x_end <= xres)
						len = color->len;
					else
						len = color->len - (x_end - xres);
					COLOR_MAP_COPY2(dst, src, len);
				}
			}
		}
		pr_debug ("quad 2\n");
		break;

	case 3:
		for_each_color(color, s) {
			x = 0 - color->y + x_offset;
			if (x >= 0 && x < xres) {
				y = color->x + y_offset;
				y_end = color->x + color->len + y_offset;
				if (y_end > 0 && y < yres) {
					if (y >= 0) {
						src = color->array;
						dst = base + y * pixels_per_line + x;
					} else {
						src = color->array + (0 - y);
						dst = base + x;
					}
					if (y_end <= yres)
						len = color->len;
					else
						len = color->len - (y_end - yres);
					COLOR_MAP_COPY1(dst, src, len, pixels_per_line);
				}
			}
		}
		pr_debug ("quad 3\n");
		break;
	}

}

#define COLOR_MAP_COPY_SAVE0(dest,src,nwords, colors)                                                     \
do {                                                                                                      \
  unsigned int* mcsrc = (unsigned int*) (src);                                                            \
  unsigned int* mcdst = (unsigned int*) (dest);                                                           \
  struct color_pos_save *mccolors = (colors);                                                             \
  color_map_copy_common(nwords,                                                                           \
    {mccolors->addr = mcdst; mccolors->color = *mcdst; mccolors++; *mcdst++ = *mcsrc++;});                \
} while(0)

#define COLOR_MAP_COPY_SAVE2(dest,src,nwords, colors)                                                     \
do {                                                                                                      \
  unsigned int* mcsrc = (unsigned int*) (src);                                                            \
  unsigned int* mcdst = (unsigned int*) (dest);                                                           \
  struct color_pos_save *mccolors = (colors);                                                             \
  color_map_copy_common(nwords,                                                                           \
    {mccolors->addr = mcdst; mccolors->color = *mcdst; mccolors++; *mcdst++ = *mcsrc--;});                \
} while(0)

#define COLOR_MAP_COPY_SAVE1(dest,src,nwords,step,colors)                                                 \
do {                                                                                                      \
  unsigned int* mcsrc = (unsigned int*) (src);                                                            \
  unsigned int* mcdst = (unsigned int*) (dest);                                                           \
  unsigned int mcstep = (unsigned int) (step);                                                            \
  struct color_pos_save *mccolors = (colors);                                                             \
  color_map_copy_common(nwords,                                                                           \
    {mccolors->addr = mcdst; mccolors->color = *mcdst; mccolors++; *mcdst = *mcsrc++; mcdst += mcstep;}); \
} while(0)

#define COLOR_MAP_COPY_SAVE3(dest,src,nwords,step,colors)                                                 \
do {                                                                                                      \
  unsigned int* mcsrc = (unsigned int*) (src);                                                            \
  unsigned int* mcdst = (unsigned int*) (dest);                                                           \
  unsigned int mcstep = (unsigned int) (step);                                                            \
  struct color_pos_save *mccolors = (colors);                                                             \
  color_map_copy_common(nwords,                                                                           \
    {mccolors->addr = mcdst; mccolors->color = *mcdst; mccolors++; *mcdst = *mcsrc--; mcdst += mcstep;}); \
} while(0)

void save_and_write_color_map(struct fb_region *region, struct color_map *s, struct color_map_save **cms, int x_offset, int y_offset, int quad) {
	struct color_pos *color;
	unsigned int *base = region->base;
	unsigned int pixels_per_line  = region->pixels_per_line;
	int xres = region->xres;
	int yres = region->yres;
	int x, y;
	int x_end;
	int y_end;
	int len;
	unsigned int *src;
	unsigned int *dst;

	if (*cms == NULL) {
		*cms = alloc_color_map_save_by_refer(region, s);
		if (*cms == NULL)
			return;
	}

	color_map_save_clear(*cms);

	switch (quad) {
	case 0:
		for_each_color(color, s) {
			y = color->y + y_offset;
			if (y >= 0 && y < yres) {
				x = color->x + x_offset;
				x_end = color->x + color->len + x_offset;
				if (x_end > 0 && x < xres) {
					if (x >= 0) {
						src = color->array;
						dst = base + y * pixels_per_line + x;
					} else {
						src = color->array + (0 - x);
						dst = base + y * pixels_per_line;
					}
					if (x_end <= xres)
						len = color->len;
					else
						len = color->len - (x_end - xres);
					if (color_map_save_ensure_capacity(*cms, len))
						return;
					COLOR_MAP_COPY_SAVE0(dst, src, len, (*cms)->colors + (*cms)->offset);
					(*cms)->offset += len;
				}
			}
		}
		pr_debug ("quad 0\n");
		break;

	case 1:
		for_each_color(color, s) {
			x = color->y + x_offset;
			if (x >= 0 && x < xres) {
				y = 0 - (color->x + color->len) + 1 + y_offset;
				y_end = 0 - color->x + 1 + y_offset;
				if (y_end > 0 && y < yres) {
					if (y >= 0) {
						src = color->array + color->len - 1;
						dst = base + y * pixels_per_line + x;
					} else {
						src = color->array + color->len - (0 - y) - 1;
						dst = base + x;
					}
					if (y_end <= yres)
						len = color->len;
					else
						len = color->len - (y_end - yres);
					if (color_map_save_ensure_capacity(*cms, len))
						return;
					COLOR_MAP_COPY_SAVE3(dst, src, len, pixels_per_line, (*cms)->colors + (*cms)->offset);
					(*cms)->offset += len;
				}
			}
		}
		pr_debug ("quad 1\n");
		break;

	case 2:
		for_each_color(color, s) {
			y = 0 - color->y + y_offset;
			if (y >= 0 && y < yres) {
				x = 0 - (color->x + color->len) + 1 + x_offset;
				x_end = 0 - color->x + 1 + x_offset;
				if (x_end > 0 && x < xres) {
					if (x >= 0) {
						src = color->array + color->len - 1;
						dst = base + y * pixels_per_line + x;
					} else {
						src = color->array + color->len - 1 - (0 - x);
						dst = base + y * pixels_per_line;
					}
					if (x_end <= xres)
						len = color->len;
					else
						len = color->len - (x_end - xres);
					if (color_map_save_ensure_capacity(*cms, len))
						return;
					COLOR_MAP_COPY_SAVE2(dst, src, len, (*cms)->colors + (*cms)->offset);
					(*cms)->offset += len;
				}
			}
		}
		pr_debug ("quad 2\n");
		break;

	case 3:
		for_each_color(color, s) {
			x = 0 - color->y + x_offset;
			if (x >= 0 && x < xres) {
				y = color->x + y_offset;
				y_end = color->x + color->len + y_offset;
				if (y_end > 0 && y < yres) {
					if (y >= 0) {
						src = color->array;
						dst = base + y * pixels_per_line + x;
					} else {
						src = color->array + (0 - y);
						dst = base + x;
					}
					if (y_end <= yres)
						len = color->len;
					else
						len = color->len - (y_end - yres);
					if (color_map_save_ensure_capacity(*cms, len))
						return;
					COLOR_MAP_COPY_SAVE1(dst, src, len, pixels_per_line, (*cms)->colors + (*cms)->offset);
					(*cms)->offset += len;
				}
			}
		}
		pr_debug ("quad 3\n");
		break;
	}

}
