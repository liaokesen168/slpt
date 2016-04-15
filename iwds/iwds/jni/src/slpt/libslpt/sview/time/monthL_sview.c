#include <common.h>
#include <sview/monthL_sview.h>

/* monthL_sview */

/*
 * see sview/monthL_sview.h,
 * those sview methods is inheritance from time_num_sview
 */
#if 0
void monthL_sview_draw(struct sview *view) {
	time_num_sview_draw(view);
}

void monthL_sview_measure_size(struct sview *view) {
	time_num_sview_measure_size(view);
}

int monthL_sview_sync(struct sview *view) {
	return time_num_sview_sync(view);
}

void monthL_sview_free(struct sview *view) {
	return time_num_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_monthL_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_time_num_sview(view, parent);
}
#endif
#endif

int init_monthL_sview(struct monthL_sview *monthL, const char *name) {

	init_time_num_sview(&monthL->timev, name);

	to_sview(monthL)->is_alloc = 0;
	to_sview(monthL)->type = SVIEW_MONTH_L;

	time_num_sview_set_level(to_sview(monthL), TIME_NUM_MON_L);
	time_num_sview_set_pic_grp(to_sview(monthL), "small_nums");

	return 0;
}

struct sview *alloc_monthL_sview(const char *name) {
	struct monthL_sview *monthL;
	char *cpy_name;

	monthL = malloc_with_name(sizeof(*monthL), name);
	if (!monthL) {
		pr_err("monthL_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&monthL[1];

	init_monthL_sview(monthL, cpy_name);

	to_sview(monthL)->is_alloc = 1;

	return to_sview(monthL);
}
