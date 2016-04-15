#include <common.h>
#include <current_time.h>
#include <time_notify.h>

struct cur_time cur_time;

void set_current_time(unsigned long time) {
	if (cur_time.time != time) {
		cur_time.time = time;
		rtc_time_to_tm(time, &cur_time.tm);
		cur_time.tm.tm_year += 1900;

		pr_debug("time: %u-%u-%u (%u) %u:%u %u\n",
			     cur_time.tm.tm_year, cur_time.tm.tm_mon, cur_time.tm.tm_mday, cur_time.tm.tm_wday,
			     cur_time.tm.tm_hour, cur_time.tm.tm_min, cur_time.tm.tm_sec);
		set_time_notify_tm(&cur_time.tm);
	}
}
