#ifndef _FRAME_LAYOUT_H_
#define _FRAME_LAYOUT_H_

#ifdef __cplusplus
extern "C" {
#endif

#include <sview/sview_base.h>

struct frame_layout {
	struct sview view;
};

#define to_frame_layout(view) ((struct frame_layout *) (view))

extern void init_frame_layout(struct frame_layout *layout, const char *name);
extern struct sview *alloc_frame_layout(const char *name);
extern void frame_layout_draw(struct sview *view);
extern void frame_layout_measure_size(struct sview *view);
extern int frame_layout_sync(struct sview *view);
extern void frame_layout_free(struct sview *view);
#ifdef CONFIG_SLPT
extern struct slpt_app_res *slpt_register_frame_layout(struct sview *view, struct slpt_app_res *parent);
#endif

extern void frame_layout_add(struct sview *view, struct sview *child);
extern void frame_layout_add_array(struct sview *view, struct sview **array, unsigned int size);

#ifdef __cplusplus
}
#endif

#endif /* _FRAME_LAYOUT_H_ */
