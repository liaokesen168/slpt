#include <common.h>
#include <sview/analog_month_sview.h>

/* analog_month_sview */

/*
 * see sview/analog_month_sview.h,
 * those sview methods is inheritance from analog_time_sview
 */
#if 0
void analog_month_sview_draw(struct sview *view) {
	analog_time_sview_draw(view);
}

void analog_month_sview_measure_size(struct sview *view) {
	analog_time_sview_measure_size(view);
}

int analog_month_sview_sync(struct sview *view) {
	return analog_time_sview_sync(view);
}

void analog_month_sview_free(struct sview *view) {
	return analog_time_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_analog_month_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_analog_time_sview(view, parent);
}
#endif
#endif

int init_analog_month_sview(struct analog_month_sview *month, const char *name) {

	init_analog_time_sview(&month->timev, name);

	to_sview(month)->is_alloc = 0;
	to_sview(month)->type = SVIEW_ANALOG_MONTH;

	analog_time_sview_set_level(to_sview(month), ANALOG_TIME_MON);
	analog_time_sview_set_pic(to_sview(month), "clock/month_handler");

	return 0;
}

struct sview *alloc_analog_month_sview(const char *name) {
	struct analog_month_sview *month;
	char *cpy_name;

	month = malloc_with_name(sizeof(*month), name);
	if (!month) {
		pr_err("analog_month_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&month[1];

	init_analog_month_sview(month, cpy_name);

	to_sview(month)->is_alloc = 1;

	return to_sview(month);
}
