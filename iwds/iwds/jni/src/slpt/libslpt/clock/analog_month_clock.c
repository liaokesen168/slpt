/*
 * analog_month_clock.c
 *
 *  Created on: May 4, 2015
 *      Author: xblin
 */

#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <current_time.h>
#include <analog_month_clock.h>

#define ANGLE_MONTH (360 / 12)

static void tm_to_angle(struct rtc_time *tm, unsigned int *angle) {
	*angle = ANGLE_MONTH * tm->tm_mon;
}

static void analog_month_clock_view_free(struct view *view) {
	struct analog_base_clock *clock = to_analog_base_clock(view);

	struct analog_month_clock *month_clock = to_analog_month_clock(clock);

	int is_alloc = view_is_alloc(view);

	view->is_alloc = 0;

	month_clock->parent_freev(view);

	if (is_alloc)
		free(month_clock);
}

int init_analog_month_clock(struct analog_month_clock *month_clock, const char *name) {

	view_init_status(&month_clock->clock.view, name, VIEW_ANALOG_WEEK_CLOCK);

	init_analog_base_clock_status(&month_clock->clock, "clock/analog_month_background",
									"clock/analog_month_hander", TIME_TICK_MON);

	month_clock->clock.tm_to_angle = tm_to_angle;
	month_clock->parent_freev = month_clock->clock.view.freev;
	month_clock->clock.view.freev = analog_month_clock_view_free;

	return 0;
}

struct view *alloc_analog_month_clock(const char *name) {

	struct analog_month_clock *month_clock;
	char *cpy_name;

	month_clock = malloc_with_name(sizeof(*month_clock), name);
	if (!month_clock) {
		pr_err("analog month_clock: failed to alloc\n");
		return NULL;
	}
	cpy_name = (char *)&month_clock[1];

	if (init_analog_month_clock(month_clock, cpy_name)) {
		pr_err("analog month_clock: failed to init\n");
		goto free_month_clock;
	}

	month_clock->clock.view.is_alloc = 1;

	return &month_clock->clock.view;
free_month_clock:
	free(month_clock);
	return NULL;
}
