#include <common.h>
#include <time_notify.h>

#define init_hander(n) [n] = LIST_HEAD_INIT(time_handlers[n])

struct list_head time_handlers[TIME_TICK_NUMS] = {
	init_hander(TIME_TICK_SEC),
	init_hander(TIME_TICK_MIN),
	init_hander(TIME_TICK_HOUR),
	init_hander(TIME_TICK_DAY),
	init_hander(TIME_TICK_MON),
	init_hander(TIME_TICK_YEAR),
};

static unsigned int notify_level;
static struct rtc_time otm;

void set_time_notify_level(unsigned int level) {
	assert(level < TIME_TICK_NUMS);
	if(notify_level == TIME_TICK_NUMS || level > notify_level)
		notify_level = level;
}

void set_time_notify_tm(struct rtc_time *tm) {
	unsigned int level = TIME_TICK_NUMS;

	if (otm.tm_year != tm->tm_year)
		level = TIME_TICK_YEAR;
	else if (otm.tm_mon != tm->tm_mon)
		level = TIME_TICK_MON;
	else if (otm.tm_mday != tm->tm_mday)
		level = TIME_TICK_DAY;
	else if (otm.tm_hour != tm->tm_hour)
		level = TIME_TICK_HOUR;
	else if (otm.tm_min != tm->tm_min)
		level = TIME_TICK_MIN;
	else if (otm.tm_sec != tm->tm_sec)
		level = TIME_TICK_SEC;

	otm = *tm;
	set_time_notify_level(level);
}

void register_time_notify(struct time_notify *no, unsigned int level) {
	assert(level < TIME_TICK_NUMS);
	list_add_tail(&no->link, &time_handlers[level]);
	no->callback(no, &otm);
}

void unregister_time_notify(struct time_notify *no) {
	list_del(&no->link);
}

static void time_notify_internal(struct rtc_time *tm, unsigned int level) {
	struct list_head *pos, *n;
	unsigned int i;

	for (i = 0; i <= level; ++i) {
		list_for_each_safe(pos, n, &time_handlers[i]) {
			struct time_notify *no = list_entry(pos, struct time_notify, link);
			no->callback(no, tm);
		}
	}
}

void time_notify(void) {
	if (notify_level < TIME_TICK_NUMS) {
		time_notify_internal(&otm, notify_level);
		notify_level = TIME_TICK_NUMS;
	}
}
