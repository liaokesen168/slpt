#include <common.h>
#include <sview/analog_hour_with_minute_sview.h>

/* analog_hour_with_minute_sview */

/*
 * see sview/analog_hour_with_minute_sview.h,
 * those sview methods is inheritance from analog_time_sview
 */
#if 0
void analog_hour_with_minute_sview_draw(struct sview *view) {
	analog_time_sview_draw(view);
}

void analog_hour_with_minute_sview_measure_size(struct sview *view) {
	analog_time_sview_measure_size(view);
}

int analog_hour_with_minute_sview_sync(struct sview *view) {
	return analog_time_sview_sync(view);
}

void analog_hour_with_minute_sview_free(struct sview *view) {
	return analog_time_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_analog_hour_with_minute_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_analog_time_sview(view, parent);
}
#endif
#endif

int init_analog_hour_with_minute_sview(struct analog_hour_with_minute_sview *hour_with_minute, const char *name) {

	init_analog_time_sview(&hour_with_minute->timev, name);

	to_sview(hour_with_minute)->is_alloc = 0;
	to_sview(hour_with_minute)->type = SVIEW_ANALOG_HOUR_WITH_MINUTE;

	analog_time_sview_set_level(to_sview(hour_with_minute), ANALOG_TIME_HOUR_WITH_MIN);
	analog_time_sview_set_pic(to_sview(hour_with_minute), "clock/hour_with_minute_handler");

	return 0;
}

struct sview *alloc_analog_hour_with_minute_sview(const char *name) {
	struct analog_hour_with_minute_sview *hour_with_minute;
	char *cpy_name;

	hour_with_minute = malloc_with_name(sizeof(*hour_with_minute), name);
	if (!hour_with_minute) {
		pr_err("analog_hour_with_minute_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&hour_with_minute[1];

	init_analog_hour_with_minute_sview(hour_with_minute, cpy_name);

	to_sview(hour_with_minute)->is_alloc = 1;

	return to_sview(hour_with_minute);
}
