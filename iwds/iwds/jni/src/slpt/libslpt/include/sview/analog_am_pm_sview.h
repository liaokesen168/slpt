#ifndef _ANALOG_AM_PM_SVIEW_H_
#define _ANALOG_AM_PM_SVIEW_H_

#ifdef __cplusplus
extern "C" {
#endif

#include <sview/analog_time_sview.h>

struct analog_am_pm_sview {
	struct analog_time_sview timev;
};

#define to_analog_am_pm_sview(view) ((struct analog_am_pm_sview*) (view))

#if 1  /* use macro to call the inheritance methods directly */
#define analog_am_pm_sview_draw          analog_time_sview_draw
#define analog_am_pm_sview_measure_size  analog_time_sview_measure_size
#define analog_am_pm_sview_sync          analog_time_sview_sync
#define analog_am_pm_sview_free          analog_time_sview_free
#ifdef CONFIG_SLPT
#define slpt_register_analog_am_pm_sview slpt_register_analog_time_sview
#endif
#else
extern void analog_am_pm_sview_draw(struct sview *view);
extern void analog_am_pm_sview_measure_size(struct sview *view);
extern int analog_am_pm_sview_sync(struct sview *view);
extern void analog_am_pm_sview_free(struct sview *view);
#ifdef CONFIG_SLPT
extern struct slpt_app_res *slpt_register_analog_am_pm_sview(struct sview *view, struct slpt_app_res *parent);
#endif
#endif

extern int init_analog_am_pm_sview(struct analog_am_pm_sview *timev, const char *name);
extern struct sview *alloc_analog_am_pm_sview(const char *name);

static inline void analog_am_pm_sview_set_tm(struct sview *view, struct rtc_time *tm) {
	analog_time_sview_set_tm(view, tm);
}

static inline int analog_am_pm_sview_set_pic(struct sview *view, const char *pic_name) {
	return analog_time_sview_set_pic(view, pic_name);
}

static inline void analog_am_pm_sview_set_angle(struct sview *view, unsigned int angle) {
	analog_time_sview_set_angle(view, angle);
}

#ifdef __cplusplus
}
#endif

#endif /* _ANALOG_AM_PM_SVIEW_H_ */
