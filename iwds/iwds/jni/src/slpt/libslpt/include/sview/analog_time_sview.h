#ifndef _ANALOG_TIME_SVIEW_H_
#define _ANALOG_TIME_SVIEW_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <sview/rotate_pic_sview.h>
#include <time_notify.h>

struct analog_time_sview {
	struct rotate_pic_sview rpv;
	struct time_notify no;
	unsigned int level;
};

enum {
	ANALOG_TIME_SEC = 0,
	ANALOG_TIME_MIN,
	ANALOG_TIME_HOUR,
	ANALOG_TIME_DAY,
	ANALOG_TIME_WEEK,
	ANALOG_TIME_MON,

	ANALOG_TIME_HOUR_WITH_MIN,
	ANALOG_TIME_AM_PM,

	ANALOG_TIME_NUMS,
};

#define to_analog_time_sview(view) ((struct analog_time_sview*) (view))

#if 1  /* use macro to call the inheritance methods directly */
#define analog_time_sview_draw          rotate_pic_sview_draw
#define analog_time_sview_measure_size  rotate_pic_sview_measure_size
#define analog_time_sview_sync          rotate_pic_sview_sync
#ifdef CONFIG_SLPT
#define slpt_register_analog_time_sview slpt_register_rotate_pic_sview
#endif
#else
extern void analog_time_sview_draw(struct sview *view);
extern void analog_time_sview_measure_size(struct sview *view);
extern int analog_time_sview_sync(struct sview *view);
#ifdef CONFIG_SLPT
extern struct slpt_app_res *slpt_register_analog_time_sview(struct sview *view, struct slpt_app_res *parent);
#endif
#endif

extern void analog_time_sview_free(struct sview *view);
extern int init_analog_time_sview(struct analog_time_sview *timev, const char *name);
extern struct sview *alloc_analog_time_sview(const char *name);

extern void analog_time_sview_set_level(struct sview *view, unsigned int level);
extern void analog_time_sview_set_tm(struct sview *view, struct rtc_time *tm);

static inline int analog_time_sview_set_pic(struct sview *view, const char *pic_name) {
	return rotate_pic_sview_set_pic(view, pic_name);
}

static inline void analog_time_sview_set_angle(struct sview *view, unsigned int angle) {
	rotate_pic_sview_set_angle(view, angle);
}

#ifdef __cplusplus
}
#endif

#endif /* _ANALOG_TIME_SVIEW_H_ */

