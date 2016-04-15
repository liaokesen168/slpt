#ifndef _FB_STRUCT_H_
#define _FB_STRUCT_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <stdlib.h>
#include <stdio.h>
#include <list.h>
#include <common.h>
#include <color_map.h>

struct fb_struct {
	unsigned int xres;
	unsigned int yres;
	unsigned int bits_per_pixel;
	unsigned int pixels_per_line;
	void *base;
	unsigned long base_phys;
	size_t size;
	unsigned int nums;
};

struct color_mode {
	unsigned int width;
	unsigned int height;
	unsigned int bpp;
	unsigned char base[0];
};

struct fb_region {
	void *base;
	unsigned int xres;
	unsigned int yres;
	unsigned int pixels_per_line;
	unsigned int bpp;
};

struct position {
	int x;
	int y;
};

struct picture;
struct view;

extern int fb_init(void);
extern void fb_exit(void);
extern void lcd_pan_display(unsigned int offset);

extern struct fb_struct* get_default_fb(void);

extern struct fb_region current_fb_region;
extern int global_view_reinit_flag;
extern unsigned int alpha32;

#define ALPHA32BIT 0x00ffffff
/* #define ALPHA32BIT 0x00ff00ff */
#define ALPHA16BIT 0xffff



static inline int position_equal(struct position *pos1, struct position *pos2) {
	return pos1->x == pos2->x && pos1->y == pos2->y;
}

static inline void set_current_fb_region(struct fb_region *region) {
	current_fb_region = *region;
}

static inline struct fb_region *get_current_fb_region(void) {
	return &current_fb_region;
}

extern void set_lcd_mem_base(void *p, int x, int y, int w);
extern void fbs_set_lcd_mem_base(struct fb_struct *fbs);
extern void lcd_draw_line(int x1, int y1, int x2, int y2, int thickness, unsigned int color);
extern void lcd_one_line(int x1, int y1, int x2, int y2, unsigned int color);

static inline unsigned int color_mode_size(struct color_mode *colors) {
	return colors->width * colors->height * (colors->bpp / 8);
}

extern struct color_mode *logo_colors;

struct bmp_logo_info{
	unsigned int type;
	unsigned int compression;
	unsigned int width;
	int height;
	unsigned int bitcount;
	unsigned int nclrs;
	unsigned int *palette;
	unsigned char *databuffer;
};

extern void bmp_to_color(struct bmp_logo_info *bmp, void *base, unsigned int bpp);
extern struct bmp_logo_info* bmp_deal_filebuffer(void *filebuffer);
extern void free_bmp(struct bmp_logo_info *bmp);

extern int bmp_file_to_color_mode(void *file_buf, struct color_mode **colors);
extern struct color_mode* bmp_to_color_mode(struct bmp_logo_info *bmp, unsigned int color_bytes);

extern int bmp_file_to_fb_region(void *file_buf, struct fb_region **region);
extern struct fb_region* bmp_to_fb_region(struct bmp_logo_info *bmp, unsigned int color_bytes);

struct color_icon {
	struct position pos;
	struct color_mode *colors;
};

struct img_display {
	char *img;					/* array of imgs */
	unsigned int img_size;
	unsigned int img_count;

	struct color_mode **colors;	/* colors of imgs */
	unsigned int index;			/* currently display which img */
	unsigned int next;			/* next index to be display */

	struct position pos;

	struct {
		unsigned int inited:1;
	} s;

	struct list_head link;		/* link to children img_display */
	struct list_head handers;	/* link to lcd_img_display */
};

extern int img_display_init(struct img_display *imgd, void *img, unsigned int img_size, unsigned int img_count, struct position *pos);
extern void img_display_set_index(struct img_display *imgd, unsigned int index);
extern int img_display(struct img_display *imgd);
extern int img_display_index(struct img_display *imgd, unsigned int index);
extern int img_display_all(struct img_display *imgd);

extern void lcd_img_display_set_reinit(int reinit);

struct rotate_desc {
	int angle;
	struct position dst;
	struct position src;
};

#define FBS_TO_REGION(fbs, region)                         \
	{                                                      \
		(region).base = (fbs).base;                        \
		(region).xres = (fbs).xres;                        \
		(region).yres = (fbs).yres;                        \
		(region).pixels_per_line = (fbs).pixels_per_line;  \
	}

#define COLORS_TO_REGION(colors, region)                   \
	{                                                      \
		(region).base = (colors).base;                     \
		(region).xres = (colors).width;                    \
		(region).yres = (colors).height;                   \
		(region).pixels_per_line = (colors).width;         \
	}

#define ROTATE_REGION_CENTER(rotate, DST, SRC, angle)      \
	{                                                      \
		(rotate).angle = (angle);                          \
		(rotate).dst.x = (DST).xres / 2;                   \
		(rotate).dst.y = (DST).yres / 2;                   \
		(rotate).src.x = (SRC).xres / 2;                   \
		(rotate).src.y = (SRC).yres / 2;                   \
	}

#define POSTION_REGION_CENTER(pos, region)           \
	{                                                \
		(pos).x = (region).xres / 2;                 \
		(pos).y = (region).yres / 2;                 \
	}

#define POSTION_IN_REGION(pos, region) (((pos).x < (region).xres) && ((pos).y < (region).yres))

static inline void region_cat(struct fb_region *dst, struct fb_region *src, struct position *pos) {
	dst->xres = src->xres - pos->x;
	dst->yres = src->yres - pos->y;
	dst->base = ((unsigned int *)src->base) + ((pos->y * src->pixels_per_line) + pos->x);
	dst->bpp = src->bpp;
	dst->pixels_per_line = 	src->pixels_per_line;
}

static inline void region_cat_region(struct fb_region *dst, struct fb_region *src,
                                     struct fb_region *region, struct position *pos) {
	dst->xres = region->xres;
	dst->yres = region->yres;
	dst->base = ((unsigned int *)src->base) + ((pos->y * src->pixels_per_line) + pos->x);
	dst->bpp = src->bpp;
	dst->pixels_per_line = 	src->pixels_per_line;
}

static inline int region_equal(struct fb_region *region0, struct fb_region *region1) {
	return (region0->base == region1->base) &&
		(region0->xres == region1->xres) &&
		(region0->yres == region1->yres) &&
		(region0->pixels_per_line == region0->pixels_per_line);
}

static inline unsigned int region_length(struct fb_region *region) {
	return region->pixels_per_line * region->yres * 4;
}

extern unsigned int rotate_measure_size(struct fb_region *src, struct position *pos);
extern void rotate_region(struct fb_region *src, int angle, struct position *pos,
    unsigned int *length, struct color_map **s, struct color_map *s0);
extern void write_color_map(struct fb_region *region, struct color_map *s, int x_offset, int y_offset, int quad);

extern void region_rotate(struct fb_region *dst, struct fb_region *src, struct rotate_desc *rotate);
extern void region_rotate_alpha(struct fb_region *dst, struct fb_region *src, struct rotate_desc *rotate);
extern void region_rotate_alpha_save(struct fb_region *dst, struct fb_region *src, struct rotate_desc *rotate,
	struct color_map **s, struct color_map *s0);
extern void region_rotate_alpha_save_both(struct fb_region *dst, struct fb_region *src, struct rotate_desc *rotate,
	struct color_map **s_src, struct color_map **s_dst, struct color_map *s0);
extern void save_color_map_of_region(struct fb_region *region, struct color_map *s_src, struct color_map **s_dst, struct position *pos,
	int quad);
extern void write_color_map_to_region(struct fb_region *region, struct color_map *s, struct position *pos, int quad);
extern void write_color_map_to_region_save(struct fb_region *region, struct color_map *s_src, struct color_map **s_dst,
	struct position *pos, int quad);

extern void color_rotate_alpha_save(struct fb_struct *fbs, struct color_mode *colors, int angle,
	struct color_map **s, struct color_map *s0);
extern void color_rotate_alpha_save_both(struct fb_struct *fbs, struct color_mode *colors, int angle,
	struct color_map **s_src, struct color_map **s_dst, struct color_map *s0);
extern void save_color_map_of_fb(struct fb_struct *fbs, struct color_map *s_src, struct color_map **s_dst, int quad);
extern void write_color_map_to_fb(struct fb_struct *fbs, struct color_map *s, int quad);
extern void write_color_map_to_fb_save(struct fb_struct *fbs, struct color_map *s_src, struct color_map **s_dst,
	int quad);

extern struct color_map *color_map_init(struct color_map *s, struct fb_region *region, struct color_map *s0);
extern void color_map_release(struct color_map *s);
extern struct color_map *color_map_alloc(struct fb_region *region, struct color_map *s0);
extern void color_map_free(struct color_map **s);
extern struct color_map *color_map_realloc(struct color_map *s);

extern void write_32_to_32(unsigned int *base0, unsigned int *base1, int x_offset, int y_offset,
	int pixels_line0, int pixels_line1);

extern void write_32_to_32_alpha(unsigned int *base0, unsigned int *base1, int x_offset, int y_offset,
	int pixels_line0, int pixels_line1, unsigned int alpha_color);

extern void write_32_to_32_alpha_clear(unsigned int *base0, unsigned int *base1,int x_offset, int y_offset,
	int pixels_line0, int pixels_line1, unsigned int alpha_color, unsigned int bg_color);

extern void write_32_to_32_alpha_replace(unsigned int *base0, unsigned int *base1, unsigned int *bg_pic,
	int x_offset, int y_offset, int pixels_line0, int pixels_line1, int pixels_line2,
	unsigned int alpha_color, unsigned int replace_color);

extern void write_32_to_32_alpha_replace_src(unsigned int *base0, unsigned int *base1, unsigned int *bg_pic,
	int x_offset, int y_offset, int pixels_line0, int pixels_line1, int pixels_line2,
	unsigned int alpha_color);

extern void clear_mem32(unsigned int *base, int xres, int yres, int pixels_per_line, unsigned int color);

extern void clear_mem32_hor(unsigned int *base, int xres, int yres, int pixels_per_line,
	int hor, unsigned int color1, unsigned int color2);

extern void clear_mem32_ver(unsigned int *base, int xres, int yres, int pixels_per_line,
	int ver, unsigned int color1, unsigned int color2);

extern void dump_mem32_str(unsigned int *base, int xres, int yres, int pixels_per_line, const char *str, int line_len);

static inline void dump_mem32_hex8(unsigned int *base, int xres, int yres, int pixels_per_line) {
	dump_mem32_str(base, xres, yres, pixels_per_line, "%8x, ", xres);
}

static inline void dump_mem32_hex6(unsigned int *base, int xres, int yres, int pixels_per_line) {
	dump_mem32_str(base, xres, yres, pixels_per_line, "%6x, ", xres);
}

static inline void write_colors_to_fb(struct fb_struct *fbs, struct color_mode *colors) {
	int x_offset = min(fbs->xres, colors->width);
	int y_offset = min(fbs->yres, colors->height);
	int pixels_line0 = fbs->pixels_per_line;
	int pixels_line1 = colors->width;

	write_32_to_32((unsigned int *)fbs->base, (unsigned int *)colors->base, x_offset, y_offset, pixels_line0, pixels_line1);
}

static inline void write_colors_to_fb_position(struct fb_struct *fbs, struct position *pos, struct color_mode *colors) {
	int x0;
	int y0;
	int x_offset;
	int y_offset;
	unsigned int xres;
	unsigned int yres;
	unsigned int *base = (unsigned int *)colors->base;
	int pixels_line0 = fbs->pixels_per_line;
	int pixels_line1 = colors->width;

	if (pos->x < 0) {
		base = base - pos->x;
		xres = max(0, ((int)colors->width) + pos->x);
		x0 = 0;
	} else {
		xres = colors->width;
		x0 = pos->x;
	}

	if (pos->y < 0) {
		base = base - (pixels_line1 * pos->y);
		yres = max(0, ((int)colors->height) + pos->y);
		y0 = 0;
	} else {
		yres = colors->height;
		y0 = pos->y;
	}

	if ((unsigned int)x0 >= fbs->xres)
		return;
	if ((unsigned int)y0 >= fbs->yres)
		return;
	if (xres == 0 || yres == 0)
		return;

	x_offset = min(fbs->xres - x0, xres);
	y_offset = min(fbs->yres - y0, yres);

	write_32_to_32(((unsigned int *)fbs->base) + (pixels_line0 * y0 + x0), base,
		x_offset, y_offset, pixels_line0, pixels_line1);
}

static inline void fb_region_write(struct fb_region *dst, struct fb_region *src, struct position *pos) {
	int x0;
	int y0;
	int x_offset;
	int y_offset;
	unsigned int xres;
	unsigned int yres;
	unsigned int *base = src->base;
	int pixels_line0 = dst->pixels_per_line;
	int pixels_line1 = src->pixels_per_line;

	if (pos->x < 0) {
		base = base - pos->x;
		xres = max(0, ((int)src->xres) + pos->x);
		x0 = 0;
	} else {
		xres = src->xres;
		x0 = pos->x;
	}

	if (pos->y < 0) {
		base = base - (pixels_line1 * pos->y);
		yres = max(0, ((int)src->yres) + pos->y);
		y0 = 0;
	} else {
		yres = src->yres;
		y0 = pos->y;
	}

	if ((unsigned int)x0 >= dst->xres)
		return;
	if ((unsigned int)y0 >= dst->yres)
		return;
	if (xres == 0 || yres == 0)
		return;

	x_offset = min(dst->xres - x0, xres);
	y_offset = min(dst->yres - y0, yres);

	write_32_to_32(((unsigned int *)dst->base) + (pixels_line0 * y0 + x0), base,
		x_offset, y_offset, pixels_line0, pixels_line1);
}

static inline void fb_region_write_alpha(struct fb_region *dst, struct fb_region *src, struct position *pos,
	unsigned int alpha_color) {
	int x0;
	int y0;
	int x_offset;
	int y_offset;
	unsigned int xres;
	unsigned int yres;
	unsigned int *base = src->base;
	int pixels_line0 = dst->pixels_per_line;
	int pixels_line1 = src->pixels_per_line;

	if (pos->x < 0) {
		base = base - pos->x;
		xres = max(0, ((int)src->xres) + pos->x);
		x0 = 0;
	} else {
		xres = src->xres;
		x0 = pos->x;
	}

	if (pos->y < 0) {
		base = base - (pixels_line1 * pos->y);
		yres = max(0, ((int)src->yres) + pos->y);
		y0 = 0;
	} else {
		yres = src->yres;
		y0 = pos->y;
	}

	if ((unsigned int)x0 >= dst->xres)
		return;
	if ((unsigned int)y0 >= dst->yres)
		return;
	if (xres == 0 || yres == 0)
		return;

	x_offset = min(dst->xres - x0, xres);
	y_offset = min(dst->yres - y0, yres);

	write_32_to_32_alpha(((unsigned int *)dst->base) + (pixels_line0 * y0 + x0), base,
		x_offset, y_offset, pixels_line0, pixels_line1, alpha_color);
}

static inline void fb_region_write_alpha_clear(struct fb_region *dst, struct fb_region *src, struct position *pos,
	unsigned int alpha_color, unsigned int bg_color) {
	int x0;
	int y0;
	int x_offset;
	int y_offset;
	unsigned int xres;
	unsigned int yres;
	unsigned int *base = src->base;
	int pixels_line0 = dst->pixels_per_line;
	int pixels_line1 = src->pixels_per_line;

	if (pos->x < 0) {
		base = base - pos->x;
		xres = max(0, ((int)src->xres) + pos->x);
		x0 = 0;
	} else {
		xres = src->xres;
		x0 = pos->x;
	}

	if (pos->y < 0) {
		base = base - (pixels_line1 * pos->y);
		yres = max(0, ((int)src->yres) + pos->y);
		y0 = 0;
	} else {
		yres = src->yres;
		y0 = pos->y;
	}

	if ((unsigned int)x0 >= dst->xres)
		return;
	if ((unsigned int)y0 >= dst->yres)
		return;
	if (xres == 0 || yres == 0)
		return;

	x_offset = min(dst->xres - x0, xres);
	y_offset = min(dst->yres - y0, yres);

	write_32_to_32_alpha_clear(((unsigned int *)dst->base) + (pixels_line0 * y0 + x0), base,
		x_offset, y_offset, pixels_line0, pixels_line1, alpha_color, bg_color);
}

static inline void fb_region_write_alpha_replace(struct fb_region *dst, struct fb_region *src,
	struct fb_region *bg_pic, struct position *pos, unsigned int alpha_color, unsigned int replace_color) {
	int x0;
	int y0;
	int x_offset;
	int y_offset;
	unsigned int xres;
	unsigned int yres;
	unsigned int *base = src->base;
	int pixels_line0 = dst->pixels_per_line;
	int pixels_line1 = src->pixels_per_line;
	int pixels_line2 = bg_pic->pixels_per_line;

	if (pos->x < 0) {
		base = base - pos->x;
		xres = max(0, ((int)src->xres) + pos->x);
		x0 = 0;
	} else {
		xres = src->xres;
		x0 = pos->x;
	}

	if (pos->y < 0) {
		base = base - (pixels_line1 * pos->y);
		yres = max(0, ((int)src->yres) + pos->y);
		y0 = 0;
	} else {
		yres = src->yres;
		y0 = pos->y;
	}

	if ((unsigned int)x0 >= dst->xres)
		return;
	if ((unsigned int)y0 >= dst->yres)
		return;
	if (xres == 0 || yres == 0)
		return;

	x_offset = min(dst->xres - x0, xres);
	y_offset = min(dst->yres - y0, yres);

	write_32_to_32_alpha_replace(((unsigned int *)dst->base) + (pixels_line0 * y0 + x0), base,
		((unsigned int *)bg_pic->base) + (pixels_line2 * y0 + x0), x_offset, y_offset,
		pixels_line0, pixels_line1, pixels_line2, alpha_color, replace_color);
}

static inline void fb_region_write_alpha_replace_src(struct fb_region *dst, struct fb_region *src,
	struct fb_region *bg_pic, struct position *pos, unsigned int alpha_color) {
	int x0;
	int y0;
	int x_offset;
	int y_offset;
	unsigned int xres;
	unsigned int yres;
	unsigned int *base = src->base;
	int pixels_line0 = dst->pixels_per_line;
	int pixels_line1 = src->pixels_per_line;
	int pixels_line2 = bg_pic->pixels_per_line;

	if (pos->x < 0) {
		base = base - pos->x;
		xres = max(0, ((int)src->xres) + pos->x);
		x0 = 0;
	} else {
		xres = src->xres;
		x0 = pos->x;
	}

	if (pos->y < 0) {
		base = base - (pixels_line1 * pos->y);
		yres = max(0, ((int)src->yres) + pos->y);
		y0 = 0;
	} else {
		yres = src->yres;
		y0 = pos->y;
	}

	if ((unsigned int)x0 >= dst->xres)
		return;
	if ((unsigned int)y0 >= dst->yres)
		return;
	if (xres == 0 || yres == 0)
		return;

	x_offset = min(dst->xres - x0, xres);
	y_offset = min(dst->yres - y0, yres);

	write_32_to_32_alpha_replace_src(((unsigned int *)dst->base) + (pixels_line0 * y0 + x0), base,
		((unsigned int *)bg_pic->base) + (pixels_line2 * y0 + x0), x_offset, y_offset,
		pixels_line0, pixels_line1, pixels_line2, alpha_color);
}

static inline struct fb_region *fb_region_alloc(int xres, int yres, int color_bytes) {
	struct fb_region *region;

	region = (struct fb_region *)malloc(sizeof(struct fb_region));
	if (region == NULL) {
		pr_err( "Failed to allocate region struct\n");
		return NULL;
	}

	region->base = malloc(xres * yres * color_bytes);
	if (!region->base) {
		pr_err( "Failed to allocate region memory\n");
		free(region);
		return NULL;
	}
	region->yres = yres;
	region->xres = xres;
	region->bpp = color_bytes * 8;
	region->pixels_per_line = region->xres;

	return region;
}

static inline int fb_region_resize(struct fb_region *region, int xres, int yres, int color_bytes) {
	void *base = malloc(xres * yres * color_bytes);

	if (!base) {
		pr_err( "Failed to reallocate region memory\n");
		return -ENOMEM;
	}

	if (region->base)
		free(region->base);

	region->base = base;
	region->yres = yres;
	region->xres = xres;
	region->bpp = color_bytes * 8;
	region->pixels_per_line = region->xres;

	return 0;
}

static inline void fb_region_free(struct fb_region *region) {
	if (region) {
		if (region->base)
			free(region->base);
		free(region);
	}
}

static inline void fb_region_clear(struct fb_region *region, unsigned int color) {
	clear_mem32(region->base, region->xres, region->yres, region->pixels_per_line, color);
}

static inline void fb_region_clear_hor(struct fb_region *region, int hor, unsigned int color1, unsigned int color2) {
	clear_mem32_hor(region->base, region->xres, region->yres, region->pixels_per_line, hor, color1, color2);
}

static inline void fb_region_clear_ver(struct fb_region *region, int ver, unsigned int color1, unsigned int color2) {
	clear_mem32_ver(region->base, region->xres, region->yres, region->pixels_per_line, ver, color1, color2);
}

static inline void dump_fb_region6(struct fb_region *region) {
	dump_mem32_hex6(region->base, region->xres, region->yres, region->pixels_per_line);
}

static inline void dump_fb_region8(struct fb_region *region) {
	dump_mem32_hex8(region->base, region->xres, region->yres, region->pixels_per_line);
}

static inline void fb_clear(unsigned int color) {
	struct fb_region *region = get_current_fb_region();
	fb_region_clear(region, color);
}

static inline void fb_clear_hor(int hor, unsigned int color1, unsigned int color2) {
	struct fb_region *region = get_current_fb_region();
	fb_region_clear_hor(region, hor, color1, color2);
}

static inline void fb_clear_ver(int ver, unsigned int color1, unsigned int color2) {
	struct fb_region *region = get_current_fb_region();
	fb_region_clear_ver(region, ver, color1, color2);
}

static inline void fb_write(struct fb_region *src, struct position *pos) {
	struct fb_region *dst = get_current_fb_region();
	fb_region_write(dst, src, pos);
}

#ifdef __cplusplus
}
#endif
#endif /* _FB_STRUCT_H_ */
