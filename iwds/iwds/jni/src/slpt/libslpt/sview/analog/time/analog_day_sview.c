#include <common.h>
#include <sview/analog_day_sview.h>

/* analog_day_sview */

/*
 * see sview/analog_day_sview.h,
 * those sview methods is inheritance from analog_time_sview
 */
#if 0
void analog_day_sview_draw(struct sview *view) {
	analog_time_sview_draw(view);
}

void analog_day_sview_measure_size(struct sview *view) {
	analog_time_sview_measure_size(view);
}

int analog_day_sview_sync(struct sview *view) {
	return analog_time_sview_sync(view);
}

void analog_day_sview_free(struct sview *view) {
	return analog_time_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_analog_day_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_analog_time_sview(view, parent);
}
#endif
#endif

int init_analog_day_sview(struct analog_day_sview *day, const char *name) {

	init_analog_time_sview(&day->timev, name);

	to_sview(day)->is_alloc = 0;
	to_sview(day)->type = SVIEW_ANALOG_DAY;

	analog_time_sview_set_level(to_sview(day), ANALOG_TIME_DAY);
	analog_time_sview_set_pic(to_sview(day), "clock/day_handler");

	return 0;
}

struct sview *alloc_analog_day_sview(const char *name) {
	struct analog_day_sview *day;
	char *cpy_name;

	day = malloc_with_name(sizeof(*day), name);
	if (!day) {
		pr_err("analog_day_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&day[1];

	init_analog_day_sview(day, cpy_name);

	to_sview(day)->is_alloc = 1;

	return to_sview(day);
}
