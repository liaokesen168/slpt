#ifndef _BACKGROUND_H_
#define _BACKGROUND_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <view.h>

struct bg_pic {
	struct view *pv;
};

extern struct bg_pic global_bg;

static inline struct fb_region *global_bg_region(void) {
	return pic_view_region(global_bg.pv);
}

static inline void display_global_bg(void) {
	view_display(global_bg.pv);
}

static inline int sync_global_bg(void) {
	return view_sync_setting(global_bg.pv);
}

extern int init_global_bg_pic(void);

#ifdef __cplusplus
}
#endif
#endif /* _BACKGROUND_H_ */
