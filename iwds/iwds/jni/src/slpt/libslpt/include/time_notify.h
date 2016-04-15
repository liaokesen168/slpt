#ifndef _TIME_NOTIFY_H_
#define _TIME_NOTIFY_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <rtc_time.h>
#include <list.h>

enum {
	TIME_TICK_SEC,
	TIME_TICK_MIN,
	TIME_TICK_HOUR,
	TIME_TICK_DAY,
	TIME_TICK_MON,
	TIME_TICK_YEAR,

	/* keep last */
	TIME_TICK_NUMS,
};

struct time_notify {
	struct list_head link;
	void(*callback)(struct time_notify *no, struct rtc_time *tm);
};

extern void register_time_notify(struct time_notify *no, unsigned int level);
extern void unregister_time_notify(struct time_notify *no);

extern void set_time_notify_level(unsigned int level);
extern void set_time_notify_tm(struct rtc_time *tm);
extern void time_notify(void);

#ifdef __cplusplus
}
#endif
#endif /* _TIME_NOTIFY_H_ */
