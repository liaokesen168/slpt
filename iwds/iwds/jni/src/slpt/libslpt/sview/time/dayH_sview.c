#include <common.h>
#include <sview/dayH_sview.h>

/* dayH_sview */

/*
 * see sview/dayH_sview.h,
 * those sview methods is inheritance from time_num_sview
 */
#if 0
void dayH_sview_draw(struct sview *view) {
	time_num_sview_draw(view);
}

void dayH_sview_measure_size(struct sview *view) {
	time_num_sview_measure_size(view);
}

int dayH_sview_sync(struct sview *view) {
	return time_num_sview_sync(view);
}

void dayH_sview_free(struct sview *view) {
	return time_num_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_dayH_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_time_num_sview(view, parent);
}
#endif
#endif

int init_dayH_sview(struct dayH_sview *dayH, const char *name) {

	init_time_num_sview(&dayH->timev, name);

	to_sview(dayH)->is_alloc = 0;
	to_sview(dayH)->type = SVIEW_DAY_H;

	time_num_sview_set_level(to_sview(dayH), TIME_NUM_DAY_H);
	time_num_sview_set_pic_grp(to_sview(dayH), "small_nums");

	return 0;
}

struct sview *alloc_dayH_sview(const char *name) {
	struct dayH_sview *dayH;
	char *cpy_name;

	dayH = malloc_with_name(sizeof(*dayH), name);
	if (!dayH) {
		pr_err("dayH_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&dayH[1];

	init_dayH_sview(dayH, cpy_name);

	to_sview(dayH)->is_alloc = 1;

	return to_sview(dayH);
}
