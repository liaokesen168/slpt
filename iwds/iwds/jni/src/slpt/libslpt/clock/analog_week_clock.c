/*
 * analog_week_clock.c
 *
 *  Created on: May 4, 2015
 *      Author: xblin
 */

#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <current_time.h>
#include <analog_week_clock.h>

#define ANGLE_WEEK (360 / 7)

static void tm_to_angle(struct rtc_time *tm, unsigned int *angle) {
	*angle = ANGLE_WEEK * tm->tm_wday;
}

static void analog_week_clock_view_free(struct view *view) {
	struct analog_base_clock *clock = to_analog_base_clock(view);

	struct analog_week_clock *week_clock = to_analog_week_clock(clock);
	int is_alloc = view_is_alloc(view);

	view->is_alloc = 0;

	week_clock->parent_freev(view);

	if (is_alloc)
		free(week_clock);
}

int init_analog_week_clock(struct analog_week_clock *week_clock, const char *name) {

	view_init_status(&week_clock->clock.view, name, VIEW_ANALOG_WEEK_CLOCK);

	init_analog_base_clock_status(&week_clock->clock, "clock/analog_week_background",
									"clock/analog_week_hander", TIME_TICK_DAY);

	week_clock->clock.tm_to_angle = tm_to_angle;
	week_clock->parent_freev = week_clock->clock.view.freev;
	week_clock->clock.view.freev = analog_week_clock_view_free;
	return 0;
}

struct view *alloc_analog_week_clock(const char *name) {
	struct analog_week_clock *week_clock;
	char *cpy_name;

	week_clock = malloc_with_name(sizeof(*week_clock), name);
	if (!week_clock) {
		pr_err("analog week_clock: failed to alloc\n");
		return NULL;
	}
	cpy_name = (char *)&week_clock[1];

	if (init_analog_week_clock(week_clock, cpy_name)) {
		pr_err("analog week_clock: failed to init\n");
		goto free_week_clock;
	}

	week_clock->clock.view.is_alloc = 1;

	return &week_clock->clock.view;
free_week_clock:
	free(week_clock);
	return NULL;
}
