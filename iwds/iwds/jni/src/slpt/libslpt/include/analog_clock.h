#ifndef _ANALOG_CLOCK_H_
#define _ANALOG_CLOCK_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <view.h>
#include <rotate_pic.h>
#include <rtc_time.h>
#include <time_notify.h>

#ifdef CONFIG_SLPT
#include <slpt.h>
#endif

enum {
	A_HOUR,
	A_MINUTE,
	A_SECOND,
	A_HANDER_NUMS,
};

#define ANGLE_HOUR (360 / 12)
#define ANGLE_MINUTE (360 / 60)
#define ANGLE_SECOND (360 / 60)

struct analog_clock {
	struct view view;
	struct time_notify no;
	struct rotate_pic handers[A_HANDER_NUMS];
	unsigned int angle[A_HANDER_NUMS];
	const char *name;
};

extern void analog_clock_set_time(struct analog_clock *clock, struct rtc_time *tm);
extern void restore_analog_clock(struct analog_clock *clock);
extern void save_and_draw_analog_clock(struct analog_clock *clock);
extern int sync_analog_clock(struct analog_clock *clock);
extern int init_analog_clock(struct analog_clock *clock, const char *name);
extern void destory_analog_clock(struct analog_clock *clock);

static inline void analog_clock_set_show(struct analog_clock *clock, int show) {
	view_set_show(&clock->view, show);
}

#ifdef CONFIG_SLPT
static inline struct slpt_app_res *slpt_register_analog_clock(struct analog_clock *clock,
                                                              struct slpt_app_res *parent) {
	return slpt_register_view(&clock->view, parent, NULL, 0);
}
#endif

#ifdef __cplusplus
}
#endif
#endif /* _ANALOG_CLOCK_H_ */
