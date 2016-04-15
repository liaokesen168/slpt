#ifndef _SVIEW_BACKGROUND_H_
#define _SVIEW_BACKGROUND_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <picture.h>

#define INVALID_COLOR 0xffffffff

struct background {
	char pic_name[MAX_PIC_NAME_LEN];
	struct picture *pic;
	unsigned int color;
};

static inline int background_is_valid(struct background *background) {
	return background->pic != NULL || background->color != INVALID_COLOR;
}

static inline struct picture *background_picture(struct background *background) {
	return background->pic;
}

static inline void background_set_picture(struct background *background, struct picture *pic) {
	background->pic = pic;
}

static inline struct fb_region *background_region(struct background *background) {
	return picture_region(background->pic);
}

static inline void background_set_color(struct background *background, unsigned int color) {
	background->color = color;
}

static inline unsigned int background_color(struct background *background) {
	return background->color;
}

extern int background_set_pic(struct background *background, const char *pic_name);
extern int background_sync_setting(struct background *background);
extern void background_write_to_target(struct background *background,
	   struct fb_region *base, struct position *pos);
extern void background_write_to_target_no_alpha(struct background *background,
	   struct fb_region *base, struct position *pos);

#ifdef __cplusplus
}
#endif
#endif /* _SVIEW_BACKGROUND_H_ */
