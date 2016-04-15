/*
 * analog_week_clock.h
 *
 *  Created on: May 4, 2015
 *      Author: xblin
 */

#ifndef _ANALOG_WEEK_CLOCK_H_
#define _ANALOG_WEEK_CLOCK_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <analog_base_clock.h>

#ifdef CONFIG_SLPT
#include <slpt.h>
#endif

struct analog_week_clock {
	struct analog_base_clock clock;

	void (*parent_freev)(struct view *view);

};

#ifdef CONFIG_SLPT
static inline struct slpt_app_res *slpt_register_analog_week_clock(struct analog_week_clock *week_clock,
                                                              struct slpt_app_res *parent) {
	return slpt_register_view(&week_clock->clock.view, parent, NULL, 0);
}
#endif

#ifdef __cplusplus
}
#endif
#endif /* _ANALOG_WEEK_CLOCK_H_ */
