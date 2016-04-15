#include <common.h>
#include <sview/hourL_sview.h>

/* hourL_sview */

/*
 * see sview/hourL_sview.h,
 * those sview methods is inheritance from time_num_sview
 */
#if 0
void hourL_sview_draw(struct sview *view) {
	time_num_sview_draw(view);
}

void hourL_sview_measure_size(struct sview *view) {
	time_num_sview_measure_size(view);
}

int hourL_sview_sync(struct sview *view) {
	return time_num_sview_sync(view);
}

void hourL_sview_free(struct sview *view) {
	return time_num_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_hourL_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_time_num_sview(view, parent);
}
#endif
#endif

int init_hourL_sview(struct hourL_sview *hourL, const char *name) {

	init_time_num_sview(&hourL->timev, name);

	to_sview(hourL)->is_alloc = 0;
	to_sview(hourL)->type = SVIEW_HOUR_L;

	time_num_sview_set_level(to_sview(hourL), TIME_NUM_HOUR_L);
	time_num_sview_set_pic_grp(to_sview(hourL), "large_nums");

	return 0;
}

struct sview *alloc_hourL_sview(const char *name) {
	struct hourL_sview *hourL;
	char *cpy_name;

	hourL = malloc_with_name(sizeof(*hourL), name);
	if (!hourL) {
		pr_err("hourL_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&hourL[1];

	init_hourL_sview(hourL, cpy_name);

	to_sview(hourL)->is_alloc = 1;

	return to_sview(hourL);
}
