#include <sys/time.h>
#include <rtc_time.h>
#include <current_time.h>

#include <common.h>

const unsigned char rtc_days_in_month[12] = {
	31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
};

/* Converts Gregorian date to seconds since 1970-01-01 00:00:00.
 * Assumes input in normal date format, i.e. 1980-12-31 23:59:59
 * => year=1980, mon=12, day=31, hour=23, min=59, sec=59.
 *
 * [For the Julian calendar (which was used in Russia before 1917,
 * Britain & colonies before 1752, anywhere else before 1582,
 * and is still in use by some communities) leave out the
 * -year/100+year/400 terms, and add 10.]
 *
 * This algorithm was first published by Gauss (I think).
 *
 * WARNING: this function will overflow on 2106-02-07 06:28:16 on
 * machines where long is 32-bit! (However, as time_t is signed, we
 * will already get problems at other places on 2038-01-19 03:14:08)
 */
unsigned long
date_to_time(const unsigned int year0, const unsigned int mon0,
			 const unsigned int day, const unsigned int hour,
			 const unsigned int min, const unsigned int sec)
{
	unsigned int mon = mon0, year = year0;

	/* 1..12 -> 11,12,1..10 */
	if (0 >= (int) (mon -= 2)) {
		mon += 12;	/* Puts Feb last since it has leap day */
		year -= 1;
	}

	return ((((unsigned long)
		  (year/4 - year/100 + year/400 + 367*mon/12 + day) +
		  year*365 - 719499
		)*24 + hour /* now have hours */
	  )*60 + min /* now have minutes */
	)*60 + sec; /* finally seconds */
}

/*
 * Convert seconds since 01-01-1970 00:00:00 to Gregorian date.
 */

void rtc_time_to_tm(unsigned long time, struct rtc_time *tm)
{
	unsigned int month, year;
	int days;

	days = time / 86400;
	time -= (unsigned int) days * 86400;

	/* day of the week, 1970-01-01 was a Thursday */
	tm->tm_wday = (days + 4) % 7;

	year = 1970 + days / 365;
	days -= (year - 1970) * 365
		+ LEAPS_THRU_END_OF(year - 1)
		- LEAPS_THRU_END_OF(1970 - 1);
	if (days < 0) {
		year -= 1;
		days += 365 + is_leap_year(year);
	}
	tm->tm_year = year - 1900;
	tm->tm_yday = days + 1;

	for (month = 0; month < 11; month++) {
		int newdays;

		newdays = days - rtc_month_days(month, year);
		if (newdays < 0)
			break;
		days = newdays;
	}
	tm->tm_mon = month + 1;
	tm->tm_mday = days + 1;

	tm->tm_hour = time / 3600;
	time -= tm->tm_hour * 3600;
	tm->tm_min = time / 60;
	tm->tm_sec = time - tm->tm_min * 60;
}

void get_display_tm(struct rtc_time *tm, struct timeval *timeval, struct timezone *timezone);

/*
 * Convert Gregorian date to seconds since 01-01-1970 00:00:00.
 */
int rtc_tm_to_time(struct rtc_time *tm, unsigned int *time)
{
	*time = date_to_time(tm->tm_year + 1900, tm->tm_mon + 1, tm->tm_mday,
				tm->tm_hour, tm->tm_min, tm->tm_sec);
	return 0;
}

int slpt_time_tick(void) {
	unsigned int delta;
	struct timeval timeval;
	struct timezone timezone;
	static int otime = 0;
	static int otimezone = -1000000;

	/* get system time */
	assert(!gettimeofday(&timeval, &timezone));

	/* we want rise to next second when [new_sec >= old_sec + 0.95]
	 * error range is : [0 --- 0.1 sec]
	 */
	if ((timeval.tv_sec == otime) &&
		(timezone.tz_minuteswest == otimezone)) {
		delta = 1000000 - timeval.tv_usec;
		pr_debug("%s time: %lu.%lu\n",
				 delta <= ((100 / 2) * 1000) ? "take" : "skip", timeval.tv_sec, timeval.tv_usec);
		if (delta <= ((100 / 2) * 1000)) {
			timeval.tv_sec += 1;
			timeval.tv_usec = 0;
		} else {
			return 0;
		}
	}

	otime = timeval.tv_sec;
	otimezone = timezone.tz_minuteswest;

	set_current_time(timeval.tv_sec - (timezone.tz_minuteswest * 60));

	return 1;
}
