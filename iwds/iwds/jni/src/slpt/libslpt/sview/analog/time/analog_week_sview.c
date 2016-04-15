#include <common.h>
#include <sview/analog_week_sview.h>

/* analog_week_sview */

/*
 * see sview/analog_week_sview.h,
 * those sview methods is inheritance from analog_time_sview
 */
#if 0
void analog_week_sview_draw(struct sview *view) {
	analog_time_sview_draw(view);
}

void analog_week_sview_measure_size(struct sview *view) {
	analog_time_sview_measure_size(view);
}

int analog_week_sview_sync(struct sview *view) {
	return analog_time_sview_sync(view);
}

void analog_week_sview_free(struct sview *view) {
	return analog_time_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_analog_week_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_analog_time_sview(view, parent);
}
#endif
#endif

int init_analog_week_sview(struct analog_week_sview *week, const char *name) {

	init_analog_time_sview(&week->timev, name);

	to_sview(week)->is_alloc = 0;
	to_sview(week)->type = SVIEW_ANALOG_WEEK;

	analog_time_sview_set_level(to_sview(week), ANALOG_TIME_WEEK);
	analog_time_sview_set_pic(to_sview(week), "clock/week_handler");

	return 0;
}

struct sview *alloc_analog_week_sview(const char *name) {
	struct analog_week_sview *week;
	char *cpy_name;

	week = malloc_with_name(sizeof(*week), name);
	if (!week) {
		pr_err("analog_week_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&week[1];

	init_analog_week_sview(week, cpy_name);

	to_sview(week)->is_alloc = 1;

	return to_sview(week);
}
