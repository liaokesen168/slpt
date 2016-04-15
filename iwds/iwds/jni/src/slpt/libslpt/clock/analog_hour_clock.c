/*
 * analog_hour_clock.c
 *
 *  Created on: May 4, 2015
 *      Author: xblin
 */

#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <current_time.h>
#include <analog_hour_clock.h>

#define ANGLE_HOUR (360 / 12)

static void tm_to_angle(struct rtc_time *tm, unsigned int *angle) {
	*angle = ANGLE_HOUR * tm->tm_hour;
}

static void analog_hour_clock_view_free(struct view *view) {
	struct analog_base_clock *clock = to_analog_base_clock(view);

	struct analog_hour_clock *hour_clock = to_analog_hour_clock(clock);
	int is_alloc = view_is_alloc(view);

	view->is_alloc = 0;

	hour_clock->parent_freev(view);

	if (is_alloc)
		free(hour_clock);
}

int init_analog_hour_clock(struct analog_hour_clock *hour_clock, const char *name) {

	view_init_status(&hour_clock->clock.view, name, VIEW_ANALOG_HOUR_CLOCK);

	init_analog_base_clock_status(&hour_clock->clock, "clock/analog_hour_background",
									"clock/analog_hour_hander", TIME_TICK_HOUR);

	hour_clock->clock.tm_to_angle = tm_to_angle;
	hour_clock->parent_freev = hour_clock->clock.view.freev;
	hour_clock->clock.view.freev = analog_hour_clock_view_free;
	return 0;
}

struct view *alloc_analog_hour_clock(const char *name) {
	struct analog_hour_clock *hour_clock;
	char *cpy_name;

	hour_clock = malloc_with_name(sizeof(*hour_clock), name);
	if (!hour_clock) {
		pr_err("analog hour_clock: failed to alloc\n");
		return NULL;
	}
	cpy_name = (char *)&hour_clock[1];

	if (init_analog_hour_clock(hour_clock, cpy_name)) {
		pr_err("analog hour_clock: failed to init\n");
		goto free_hour_clock;
	}

	hour_clock->clock.view.is_alloc = 1;

	return &hour_clock->clock.view;
free_hour_clock:
	free(hour_clock);
	return NULL;
}
