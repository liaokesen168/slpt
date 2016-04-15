#include <common.h>
#include <sview/analog_second_sview.h>

/* analog_second_sview */

/*
 * see sview/analog_second_sview.h,
 * those sview methods is inheritance from analog_time_sview
 */
#if 0
void analog_second_sview_draw(struct sview *view) {
	analog_time_sview_draw(view);
}

void analog_second_sview_measure_size(struct sview *view) {
	analog_time_sview_measure_size(view);
}

int analog_second_sview_sync(struct sview *view) {
	return analog_time_sview_sync(view);
}

void analog_second_sview_free(struct sview *view) {
	return analog_time_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_analog_second_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_analog_time_sview(view, parent);
}
#endif
#endif

int init_analog_second_sview(struct analog_second_sview *second, const char *name) {

	init_analog_time_sview(&second->timev, name);

	to_sview(second)->is_alloc = 0;
	to_sview(second)->type = SVIEW_ANALOG_SECOND;

	analog_time_sview_set_level(to_sview(second), ANALOG_TIME_SEC);
	analog_time_sview_set_pic(to_sview(second), "clock/second_handler");

	return 0;
}

struct sview *alloc_analog_second_sview(const char *name) {
	struct analog_second_sview *second;
	char *cpy_name;

	second = malloc_with_name(sizeof(*second), name);
	if (!second) {
		pr_err("analog_second_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&second[1];

	init_analog_second_sview(second, cpy_name);

	to_sview(second)->is_alloc = 1;

	return to_sview(second);
}
