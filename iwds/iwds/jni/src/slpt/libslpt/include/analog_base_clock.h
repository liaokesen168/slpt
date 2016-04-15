/*
 * analog_base_clock.h
 *
 *  Created on: May 18, 2015
 *      Author: xblin
 */

#ifndef ANALOG_BASE_CLOCK_H_
#define ANALOG_BASE_CLOCK_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <view.h>
#include <rotate_pic.h>
#include <rtc_time.h>
#include <time_notify.h>

struct analog_base_clock {
	struct view view;
	struct pic_view bkg; /* background pic*/
	struct time_notify no;
	struct rotate_pic handers[1];
	unsigned int angle;
	const char *name;

	void (*tm_to_angle)(struct rtc_time *tm, unsigned int *angle); /* caculate the anale */
};

extern void init_analog_base_clock_status(struct analog_base_clock *clock, const char *bkg_pic,
													const char *hander_pic, unsigned int time_level);
#ifdef __cplusplus
}
#endif
#endif /* ANALOG_BASE_CLOCK_H_ */
