#include <common.h>
#include <sview/monthH_sview.h>

/* monthH_sview */

/*
 * see sview/monthH_sview.h,
 * those sview methods is inheritance from time_num_sview
 */
#if 0
void monthH_sview_draw(struct sview *view) {
	time_num_sview_draw(view);
}

void monthH_sview_measure_size(struct sview *view) {
	time_num_sview_measure_size(view);
}

int monthH_sview_sync(struct sview *view) {
	return time_num_sview_sync(view);
}

void monthH_sview_free(struct sview *view) {
	return time_num_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_monthH_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_time_num_sview(view, parent);
}
#endif
#endif

int init_monthH_sview(struct monthH_sview *monthH, const char *name) {

	init_time_num_sview(&monthH->timev, name);

	to_sview(monthH)->is_alloc = 0;
	to_sview(monthH)->type = SVIEW_MONTH_H;

	time_num_sview_set_level(to_sview(monthH), TIME_NUM_MON_H);
	time_num_sview_set_pic_grp(to_sview(monthH), "large_nums");

	return 0;
}

struct sview *alloc_monthH_sview(const char *name) {
	struct monthH_sview *monthH;
	char *cpy_name;

	monthH = malloc_with_name(sizeof(*monthH), name);
	if (!monthH) {
		pr_err("monthH_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&monthH[1];

	init_monthH_sview(monthH, cpy_name);

	to_sview(monthH)->is_alloc = 1;

	return to_sview(monthH);
}
