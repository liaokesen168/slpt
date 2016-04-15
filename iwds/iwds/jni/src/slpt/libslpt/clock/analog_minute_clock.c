/*
 * analog_minute_clock.c
 *
 *  Created on: May 4, 2015
 *      Author: xblin
 */

#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <current_time.h>
#include <analog_minute_clock.h>

#define ANGLE_MINUTE (360 / 60)

static void tm_to_angle(struct rtc_time *tm, unsigned int *angle) {
	*angle = ANGLE_MINUTE * tm->tm_min;
}

static void analog_minute_clock_view_free(struct view *view) {
	struct analog_base_clock *clock = to_analog_base_clock(view);

	struct analog_minute_clock *minute_clock = to_analog_minute_clock(clock);
	int is_alloc = view_is_alloc(view);

	view->is_alloc = 0;

	minute_clock->parent_freev(view);

	if (is_alloc)
		free(minute_clock);
}

int init_analog_minute_clock(struct analog_minute_clock *minute_clock, const char *name) {

	view_init_status(&minute_clock->clock.view, name, VIEW_ANALOG_MINUTE_CLOCK);

	init_analog_base_clock_status(&minute_clock->clock, "clock/analog_minute_background",
									"clock/analog_minute_hander", TIME_TICK_MIN);

	minute_clock->clock.tm_to_angle = tm_to_angle;
	minute_clock->parent_freev = minute_clock->clock.view.freev;
	minute_clock->clock.view.freev = analog_minute_clock_view_free;
	return 0;
}

struct view *alloc_analog_minute_clock(const char *name) {
	struct analog_minute_clock *minute_clock;
	char *cpy_name;

	minute_clock = malloc_with_name(sizeof(*minute_clock), name);
	if (!minute_clock) {
		pr_err("analog minute_clock: failed to alloc\n");
		return NULL;
	}
	cpy_name = (char *)&minute_clock[1];

	if (init_analog_minute_clock(minute_clock, cpy_name)) {
		pr_err("analog minute_clock: failed to init\n");
		goto free_minute_clock;
	}

	minute_clock->clock.view.is_alloc = 1;

	return &minute_clock->clock.view;
free_minute_clock:
	free(minute_clock);
	return NULL;
}
