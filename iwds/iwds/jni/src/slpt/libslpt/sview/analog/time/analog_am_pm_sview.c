#include <common.h>
#include <sview/analog_am_pm_sview.h>

/* analog_am_pm_sview */

/*
 * see sview/analog_am_pm_sview.h,
 * those sview methods is inheritance from analog_time_sview
 */
#if 0
void analog_am_pm_sview_draw(struct sview *view) {
	analog_time_sview_draw(view);
}

void analog_am_pm_sview_measure_size(struct sview *view) {
	analog_time_sview_measure_size(view);
}

int analog_am_pm_sview_sync(struct sview *view) {
	return analog_time_sview_sync(view);
}

void analog_am_pm_sview_free(struct sview *view) {
	return analog_time_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_analog_am_pm_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_analog_time_sview(view, parent);
}
#endif
#endif

int init_analog_am_pm_sview(struct analog_am_pm_sview *am_pm, const char *name) {

	init_analog_time_sview(&am_pm->timev, name);

	to_sview(am_pm)->is_alloc = 0;
	to_sview(am_pm)->type = SVIEW_ANALOG_AM_PM;

	analog_time_sview_set_level(to_sview(am_pm), ANALOG_TIME_AM_PM);
	analog_time_sview_set_pic(to_sview(am_pm), "clock/am_pm_handler");

	return 0;
}

struct sview *alloc_analog_am_pm_sview(const char *name) {
	struct analog_am_pm_sview *am_pm;
	char *cpy_name;

	am_pm = malloc_with_name(sizeof(*am_pm), name);
	if (!am_pm) {
		pr_err("analog_am_pm_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&am_pm[1];

	init_analog_am_pm_sview(am_pm, cpy_name);

	to_sview(am_pm)->is_alloc = 1;

	return to_sview(am_pm);
}
