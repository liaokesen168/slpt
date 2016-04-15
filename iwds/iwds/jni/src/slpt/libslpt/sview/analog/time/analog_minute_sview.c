#include <common.h>
#include <sview/analog_minute_sview.h>

/* analog_minute_sview */

/*
 * see sview/analog_minute_sview.h,
 * those sview methods is inheritance from analog_time_sview
 */
#if 0
void analog_minute_sview_draw(struct sview *view) {
	analog_time_sview_draw(view);
}

void analog_minute_sview_measure_size(struct sview *view) {
	analog_time_sview_measure_size(view);
}

int analog_minute_sview_sync(struct sview *view) {
	return analog_time_sview_sync(view);
}

void analog_minute_sview_free(struct sview *view) {
	return analog_time_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_analog_minute_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_analog_time_sview(view, parent);
}
#endif
#endif

int init_analog_minute_sview(struct analog_minute_sview *minute, const char *name) {

	init_analog_time_sview(&minute->timev, name);

	to_sview(minute)->is_alloc = 0;
	to_sview(minute)->type = SVIEW_ANALOG_MINUTE;

	analog_time_sview_set_level(to_sview(minute), ANALOG_TIME_MIN);
	analog_time_sview_set_pic(to_sview(minute), "clock/minute_handler");

	return 0;
}

struct sview *alloc_analog_minute_sview(const char *name) {
	struct analog_minute_sview *minute;
	char *cpy_name;

	minute = malloc_with_name(sizeof(*minute), name);
	if (!minute) {
		pr_err("analog_minute_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&minute[1];

	init_analog_minute_sview(minute, cpy_name);

	to_sview(minute)->is_alloc = 1;

	return to_sview(minute);
}
