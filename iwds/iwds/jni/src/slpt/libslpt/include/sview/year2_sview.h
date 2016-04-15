#ifndef _YEAR2_SVIEW_H_
#define _YEAR2_SVIEW_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <sview/time_num_sview.h>

struct year2_sview {
	struct time_num_sview timev;
};

#define to_year2_sview(view) ((struct year2_sview*) (view))

#if 1  /* use macro to call the inheritance methods directly */
#define year2_sview_draw          time_num_sview_draw
#define year2_sview_measure_size  time_num_sview_measure_size
#define year2_sview_sync          time_num_sview_sync
#define year2_sview_free          time_num_sview_free
#ifdef CONFIG_SLPT
#define slpt_register_year2_sview slpt_register_time_num_sview
#endif
#else
extern void year2_sview_draw(struct sview *view);
extern void year2_sview_measure_size(struct sview *view);
extern int year2_sview_sync(struct sview *view);
extern void year2_sview_free(struct sview *view);
#ifdef CONFIG_SLPT
extern struct slpt_app_res *slpt_register_year2_sview(struct sview *view, struct slpt_app_res *parent);
#endif
#endif

extern int init_year2_sview(struct year2_sview *timev, const char *name);
extern struct sview *alloc_year2_sview(const char *name);

static inline void year2_sview_set_tm(struct sview *view, struct rtc_time *tm) {
	time_num_sview_set_tm(view, tm);
}

static inline int year2_sview_set_pic_grp(struct sview *view, const char *grp_name) {
	return time_num_sview_set_pic_grp(view, grp_name);
}

static inline void year2_sview_set_num(struct sview *view, unsigned int num) {
	time_num_sview_set_num(view, num);
}

#ifdef __cplusplus
}
#endif
#endif /* _YEAR2_SVIEW_H_ */
