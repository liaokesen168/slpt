#ifndef _TIME_NUM_SVIEW_H_
#define _TIME_NUM_SVIEW_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <sview/num_sview.h>
#include <time_notify.h>

struct time_num_sview {
	struct num_sview numv;
	struct time_notify no;
	unsigned int level;
};

enum {
	TIME_NUM_SEC_L = 0,
	TIME_NUM_SEC_H,
	TIME_NUM_MIN_L,
	TIME_NUM_MIN_H,
	TIME_NUM_HOUR_L,
	TIME_NUM_HOUR_H,
	TIME_NUM_DAY_L,
	TIME_NUM_DAY_H,
	TIME_NUM_WEEK,
	TIME_NUM_MON_L,
	TIME_NUM_MON_H,
	TIME_NUM_YEAR_0,
	TIME_NUM_YEAR_1,
	TIME_NUM_YEAR_2,
	TIME_NUM_YEAR_3,

	/* keep last */
	TIME_NUM_NUMS,
};

#define to_time_num_sview(view) ((struct time_num_sview*) (view))

#if 1  /* use macro to call the inheritance methods directly */
#define time_num_sview_draw          num_sview_draw
#define time_num_sview_measure_size  num_sview_measure_size
#define time_num_sview_sync          num_sview_sync
#ifdef CONFIG_SLPT
#define slpt_register_time_num_sview slpt_register_num_sview
#endif
#else
extern void time_num_sview_draw(struct sview *view);
extern void time_num_sview_measure_size(struct sview *view);
extern int time_num_sview_sync(struct sview *view);
#ifdef CONFIG_SLPT
extern struct slpt_app_res *slpt_register_time_num_sview(struct sview *view, struct slpt_app_res *parent);
#endif
#endif

extern void time_num_sview_free(struct sview *view);
extern int init_time_num_sview(struct time_num_sview *timev, const char *name);
extern struct sview *alloc_time_num_sview(const char *name);

extern void time_num_sview_set_level(struct sview *view, unsigned int level);
extern void time_num_sview_set_tm(struct sview *view, struct rtc_time *tm);

static inline int time_num_sview_set_pic_grp(struct sview *view, const char *grp_name) {
	return num_sview_set_pic_grp(view, grp_name);
}

static inline void time_num_sview_set_num(struct sview *view, unsigned int num) {
	num_sview_set_num(view, num);
}

#ifdef __cplusplus
}
#endif
#endif /* _TIME_NUM_SVIEW_H_ */
