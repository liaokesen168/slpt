#include <common.h>
#include <sview/analog_hour_sview.h>

/* analog_hour_sview */

/*
 * see sview/analog_hour_sview.h,
 * those sview methods is inheritance from analog_time_sview
 */
#if 0
void analog_hour_sview_draw(struct sview *view) {
	analog_time_sview_draw(view);
}

void analog_hour_sview_measure_size(struct sview *view) {
	analog_time_sview_measure_size(view);
}

int analog_hour_sview_sync(struct sview *view) {
	return analog_time_sview_sync(view);
}

void analog_hour_sview_free(struct sview *view) {
	return analog_time_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_analog_hour_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_analog_time_sview(view, parent);
}
#endif
#endif

int init_analog_hour_sview(struct analog_hour_sview *hour, const char *name) {

	init_analog_time_sview(&hour->timev, name);

	to_sview(hour)->is_alloc = 0;
	to_sview(hour)->type = SVIEW_ANALOG_HOUR;

	analog_time_sview_set_level(to_sview(hour), ANALOG_TIME_HOUR);
	analog_time_sview_set_pic(to_sview(hour), "clock/hour_handler");

	return 0;
}

struct sview *alloc_analog_hour_sview(const char *name) {
	struct analog_hour_sview *hour;
	char *cpy_name;

	hour = malloc_with_name(sizeof(*hour), name);
	if (!hour) {
		pr_err("analog_hour_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&hour[1];

	init_analog_hour_sview(hour, cpy_name);

	to_sview(hour)->is_alloc = 1;

	return to_sview(hour);
}
