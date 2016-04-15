#ifndef _ROTATE_PIC_SVIEW_H_
#define _ROTATE_PIC_SVIEW_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <sview/sview_base.h>
#include <sview/pic_sview.h>
#include <sview/rotate2.h>

struct rotate_pic_sview {
	struct pic_sview picv;
	struct rotate2 rotate;
	unsigned int angle;
};

#define to_rotate_pic_sview(view) ((struct rotate_pic_sview *) (view))

static inline int rotate_pic_sview_set_pic(struct sview *view, const char *pic_name) {
	return pic_sview_set_pic(view, pic_name);
}

static inline void rotate_pic_sview_set_center(struct sview *view, unsigned int center_x, unsigned int center_y) {
	struct rotate_pic_sview *rpv = to_rotate_pic_sview(view);
	rotate2_set_center(&rpv->rotate, center_x, center_y);
}

static inline void rotate_pic_sview_set_align_center(struct sview *view, unsigned int enable) {
	struct rotate_pic_sview *rpv = to_rotate_pic_sview(view);
	rotate2_set_align_center(&rpv->rotate, enable);
}

static inline void rotate_pic_sview_set_angle(struct sview *view, unsigned int angle) {
	struct rotate_pic_sview *rpv = to_rotate_pic_sview(view);
	rotate2_set_angle(&rpv->rotate, angle);
}

#if 1  /* use macro to call the inheritance methods directly */
#ifdef CONFIG_SLPT
#define slpt_register_rotate_pic_sview  slpt_register_pic_sview
#endif
#else
#ifdef CONFIG_SLPT
extern struct slpt_app_res *slpt_register_rotate_pic_sview(struct sview *view, struct slpt_app_res *parent);
#endif
#endif

extern void rotate_pic_sview_draw(struct sview *view);
extern void rotate_pic_sview_measure_size(struct sview *view);
extern int rotate_pic_sview_sync(struct sview *view);
extern void rotate_pic_sview_free(struct sview *view);
extern void init_rotate_pic_sview(struct rotate_pic_sview *rpv, const char *name);
extern struct sview *alloc_rotate_pic_sview(const char *name);

#endif /* _ROTATE_PIC_SVIEW_H_ */

