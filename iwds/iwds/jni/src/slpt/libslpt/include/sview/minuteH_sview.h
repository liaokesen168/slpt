#ifndef _MINUTEH_SVIEW_H_
#define _MINUTEH_SVIEW_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <sview/time_num_sview.h>

struct minuteH_sview {
	struct time_num_sview timev;
};

#define to_minuteH_sview(view) ((struct minuteH_sview*) (view))

#if 1  /* use macro to call the inheritance methods directly */
#define minuteH_sview_draw          time_num_sview_draw
#define minuteH_sview_measure_size  time_num_sview_measure_size
#define minuteH_sview_sync          time_num_sview_sync
#define minuteH_sview_free          time_num_sview_free
#ifdef CONFIG_SLPT
#define slpt_register_minuteH_sview slpt_register_time_num_sview
#endif
#else
extern void minuteH_sview_draw(struct sview *view);
extern void minuteH_sview_measure_size(struct sview *view);
extern int minuteH_sview_sync(struct sview *view);
extern void minuteH_sview_free(struct sview *view);
#ifdef CONFIG_SLPT
extern struct slpt_app_res *slpt_register_minuteH_sview(struct sview *view, struct slpt_app_res *parent);
#endif
#endif

extern int init_minuteH_sview(struct minuteH_sview *timev, const char *name);
extern struct sview *alloc_minuteH_sview(const char *name);

static inline void minuteH_sview_set_tm(struct sview *view, struct rtc_time *tm) {
	time_num_sview_set_tm(view, tm);
}

static inline int minuteH_sview_set_pic_grp(struct sview *view, const char *grp_name) {
	return time_num_sview_set_pic_grp(view, grp_name);
}

static inline void minuteH_sview_set_num(struct sview *view, unsigned int num) {
	time_num_sview_set_num(view, num);
}

#ifdef __cplusplus
}
#endif
#endif /* _MINUTEH_SVIEW_H_ */
