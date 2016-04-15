#include <common.h>
#include <sview/dayL_sview.h>

/* dayL_sview */

/*
 * see sview/dayL_sview.h,
 * those sview methods is inheritance from time_num_sview
 */
#if 0
void dayL_sview_draw(struct sview *view) {
	time_num_sview_draw(view);
}

void dayL_sview_measure_size(struct sview *view) {
	time_num_sview_measure_size(view);
}

int dayL_sview_sync(struct sview *view) {
	return time_num_sview_sync(view);
}

void dayL_sview_free(struct sview *view) {
	return time_num_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_dayL_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_time_num_sview(view, parent);
}
#endif
#endif

int init_dayL_sview(struct dayL_sview *dayL, const char *name) {

	init_time_num_sview(&dayL->timev, name);

	to_sview(dayL)->is_alloc = 0;
	to_sview(dayL)->type = SVIEW_DAY_L;

	time_num_sview_set_level(to_sview(dayL), TIME_NUM_DAY_L);
	time_num_sview_set_pic_grp(to_sview(dayL), "small_nums");

	return 0;
}

struct sview *alloc_dayL_sview(const char *name) {
	struct dayL_sview *dayL;
	char *cpy_name;

	dayL = malloc_with_name(sizeof(*dayL), name);
	if (!dayL) {
		pr_err("dayL_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&dayL[1];

	init_dayL_sview(dayL, cpy_name);

	to_sview(dayL)->is_alloc = 1;

	return to_sview(dayL);
}
