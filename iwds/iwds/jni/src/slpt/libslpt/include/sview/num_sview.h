#ifndef _NUM_SVIEW_H_
#define _NUM_SVIEW_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <sview/sview_base.h>

struct num_sview {
	struct sview view;
	struct picture_grp *grp;
	unsigned int num;
	char grp_name[MAX_NAME_LEN];
};

#define to_num_sview(view) ((struct num_sview *) (view))

extern void init_num_sview(struct num_sview *nv, const char *name);
extern struct sview *alloc_num_sview(const char *name);
extern void num_sview_draw(struct sview *view);
extern void num_sview_measure_size(struct sview *view);
extern int num_sview_sync(struct sview *view);
extern void num_sview_free(struct sview *view);
#ifdef CONFIG_SLPT
extern struct slpt_app_res *slpt_register_num_sview(struct sview *view, struct slpt_app_res *parent);
#endif

extern void num_sview_set_num(struct sview *view, unsigned int num);
extern int num_sview_set_pic_grp(struct sview *view, const char *grp_name);

#ifdef __cplusplus
}
#endif
#endif /* _NUM_SVIEW_H_ */
