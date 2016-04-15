/*
 * analog_second_clock.c
 *
 *  Created on: May 4, 2015
 *      Author: xblin
 */

#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <current_time.h>
#include <analog_second_clock.h>

#define ANGLE_SECOND (360 / 60)

static void tm_to_angle(struct rtc_time *tm, unsigned int *angle) {
	*angle = ANGLE_SECOND * tm->tm_sec;
}

static void analog_second_clock_view_free(struct view *view) {
	struct analog_base_clock *clock = to_analog_base_clock(view);

	struct analog_second_clock *second_clock = to_analog_second_clock(clock);
	int is_alloc = view_is_alloc(view);

	view->is_alloc = 0;

	second_clock->parent_freev(view);

	if (is_alloc)
		free(second_clock);
}

int init_analog_second_clock(struct analog_second_clock *second_clock, const char *name) {

	view_init_status(&second_clock->clock.view, name, VIEW_ANALOG_SECOND_CLOCK);

	init_analog_base_clock_status(&second_clock->clock, "clock/analog_second_background",
									"clock/analog_second_hander", TIME_TICK_SEC);

	second_clock->clock.tm_to_angle = tm_to_angle;
	second_clock->parent_freev = second_clock->clock.view.freev;
	second_clock->clock.view.freev = analog_second_clock_view_free;
	return 0;
}

struct view *alloc_analog_second_clock(const char *name) {
	struct analog_second_clock *second_clock;
	char *cpy_name;

	second_clock = malloc_with_name(sizeof(*second_clock), name);
	if (!second_clock) {
		pr_err("analog second_clock: failed to alloc\n");
		return NULL;
	}
	cpy_name = (char *)&second_clock[1];

	if (init_analog_second_clock(second_clock, cpy_name)) {
		pr_err("analog second_clock: failed to init\n");
		goto free_second_clock;
	}

	second_clock->clock.view.is_alloc = 1;

	return &second_clock->clock.view;
free_second_clock:
	free(second_clock);
	return NULL;
}
