#ifndef _ABSOLUTE_LAYOUT_H_
#define _ABSOLUTE_LAYOUT_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <sview/sview_base.h>


#define POSITION_LEFT 0
#define POSITION_RIGHT 1

#define POSITION_TOP 0
#define POSITION_BOTTOM 1

#define POSITION_CENTER 2

struct absolute_layout {
	struct sview view;
	unsigned char position_of_x_start;
	unsigned char position_of_y_start;
};

#define to_absolute_layout(view) ((struct absolute_layout *) (view))

extern void init_absolute_layout(struct absolute_layout *layout, const char *name);
extern struct sview *alloc_absolute_layout(const char *name);
extern void absolute_layout_draw(struct sview *view);
extern void absolute_layout_measure_size(struct sview *view);
extern int absolute_layout_sync(struct sview *view);
extern void absolute_layout_free(struct sview *view);
#ifdef CONFIG_SLPT
extern struct slpt_app_res *slpt_register_absolute_layout(struct sview *view, struct slpt_app_res *parent);
#endif

extern void absolute_layout_add(struct sview *view, struct sview *child);
extern void absolute_layout_add_array(struct sview *view, struct sview **array, unsigned int size);

#ifdef __cplusplus
}
#endif
#endif /* _ABSOLUTE_LAYOUT_H_ */
