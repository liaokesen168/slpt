#ifndef _RTC_TIME_H_
#define _RTC_TIME_H_
#ifdef __cplusplus
extern "C" {
#endif

/*
 * The struct used to pass data from the generic interface code to
 * the hardware dependend low-level code ande vice versa. Identical
 * to struct rtc_time used by the Linux kernel.
 *
 * Note that there are small but significant differences to the
 * common "struct time":
 *
 *		struct time:		struct rtc_time:
 * tm_mon	0 ... 11		1 ... 12
 * tm_year	years since 1900	years since 0
 */

struct rtc_time {
	int tm_sec;
	int tm_min;
	int tm_hour;
	int tm_mday;
	int tm_mon;
	int tm_year;
	int tm_wday;
	int tm_yday;
	int tm_isdst;
};

extern const unsigned char rtc_days_in_month[12];

static inline unsigned int is_leap_year(unsigned int year)
{
	return (!(year % 4) && (year % 100)) || !(year % 400);
}

#define LEAPS_THRU_END_OF(y) ((y)/4 - (y)/100 + (y)/400)

/*
 * The number of days in the month.
 * NOTICE: this month is from 0 to 11, not 1 to 12
 */
static inline int rtc_month_days(unsigned int month, unsigned int year)
{
	return rtc_days_in_month[month] + (is_leap_year(year) && month == 1);
}

extern unsigned long
date_to_time(const unsigned int year0, const unsigned int mon0,
			 const unsigned int day, const unsigned int hour,
			 const unsigned int min, const unsigned int sec);

extern void rtc_time_to_tm(unsigned long time, struct rtc_time *tm);

extern int rtc_tm_to_time(struct rtc_time *tm, unsigned int *time);

#ifdef __cplusplus
}
#endif
#endif /* _RTC_TIME_H_ */
