#ifndef _PIC_SVIEW_H_
#define _PIC_SVIEW_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <sview/sview_base.h>

struct pic_sview {
	struct sview view;
	struct picture *pic;
	char pic_name[MAX_PIC_NAME_LEN];
};

#define to_pic_sview(view) ((struct pic_sview *) (view))

extern void init_pic_sview(struct pic_sview *pv, const char *name);
extern struct sview *alloc_pic_sview(const char *name);
extern void pic_sview_draw(struct sview *view);
extern void pic_sview_measure_size(struct sview *view);
extern int pic_sview_sync(struct sview *view);
extern void pic_sview_free(struct sview *view);
#ifdef CONFIG_SLPT
extern struct slpt_app_res *slpt_register_pic_sview(struct sview *view, struct slpt_app_res *parent);
#endif

extern int pic_sview_set_pic(struct sview *view, const char *pic_name);

static inline struct fb_region *pic_sview_get_region(struct sview *view) {
	struct pic_sview *pv = to_pic_sview(view);

	return pv->pic ? picture_region(pv->pic) : NULL;
}

#ifdef __cplusplus
}
#endif
#endif /* _PIC_SVIEW_H_ */
