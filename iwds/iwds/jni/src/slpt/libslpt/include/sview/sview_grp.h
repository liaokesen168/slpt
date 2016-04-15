#ifndef _SVIEW_GRP_H_
#define _SVIEW_GRP_H_
#ifdef __cplusplus
extern "C" {
#endif

struct list_head;
struct sview;

extern unsigned int sview_grp_size(struct list_head *grp);
extern void sview_grp_add(struct list_head *grp, struct sview *view);
extern void sview_grp_add_array(struct list_head *grp, struct sview **array, unsigned int size);
extern void sview_grp_add_by_level(struct list_head *grp, struct sview *view);
extern void sview_grp_add_array_by_level(struct list_head *grp, struct sview **array, unsigned int size);
extern void sview_grp_sort(struct list_head *grp);
extern int sview_grp_sync(struct list_head *grp);
extern int sview_grp_sync_strictly(struct list_head *grp);
extern void sview_grp_free(struct list_head *grp);
extern struct sview *sview_grp_find(struct list_head *grp, const char *child_name);

#ifdef CONFIG_SLPT
extern struct slpt_app_res *slpt_register_sview_grp(struct list_head *grp, struct slpt_app_res *parent);
#endif

#ifdef __cplusplus
}
#endif
#endif /* _SVIEW_GRP_H_ */
