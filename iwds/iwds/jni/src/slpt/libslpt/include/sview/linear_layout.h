#ifndef _LINEAR_LAYOUT_H_
#define _LINEAR_LAYOUT_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <sview/sview_base.h>

/* orientation */
#define HORIZONTAL 0
#define VERTICAL   1

struct linear_layout {
	struct sview view;
	unsigned char orientation;
};

#define to_linear_layout(view) ((struct linear_layout *) (view))

extern void init_linear_layout(struct linear_layout *layout, const char *name);
extern struct sview *alloc_linear_layout(const char *name);
extern void linear_layout_draw(struct sview *view);
extern void linear_layout_measure_size(struct sview *view);
extern int linear_layout_sync(struct sview *view);
extern void linear_layout_free(struct sview *view);
#ifdef CONFIG_SLPT
extern struct slpt_app_res *slpt_register_linear_layout(struct sview *view, struct slpt_app_res *parent);
#endif

extern void linear_layout_add(struct sview *view, struct sview *child);
extern void linear_layout_add_array(struct sview *view, struct sview **array, unsigned int size);
extern int linear_layout_get_orientation(struct sview *view);
extern void linear_layout_set_orientation(struct sview *view, int orientation);

#ifdef __cplusplus
}
#endif
#endif /* _LINEAR_LAYOUT_H_ */
