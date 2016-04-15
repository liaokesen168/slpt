/*
 * analog_month_clock.h
 *
 *  Created on: May 19, 2015
 *      Author: xblin
 */

#ifndef ANALOG_MONTH_CLOCK_H_
#define ANALOG_MONTH_CLOCK_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <analog_base_clock.h>

#ifdef CONFIG_SLPT
#include <slpt.h>
#endif

struct analog_month_clock {
	struct analog_base_clock clock;

	void (*parent_freev)(struct view *view);
};

#ifdef CONFIG_SLPT
static inline struct slpt_app_res *slpt_register_analog_month_clock(struct analog_month_clock *month_clock,
                                                              struct slpt_app_res *parent) {
	return slpt_register_view(&month_clock->clock.view, parent, NULL, 0);
}
#endif

#ifdef __cplusplus
}
#endif
#endif /* ANALOG_MONTH_CLOCK_H_ */
