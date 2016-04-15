#include <common.h>
#include <sview/year1_sview.h>

/* year1_sview */

/*
 * see sview/year1_sview.h,
 * those sview methods is inheritance from time_num_sview
 */
#if 0
void year1_sview_draw(struct sview *view) {
	time_num_sview_draw(view);
}

void year1_sview_measure_size(struct sview *view) {
	time_num_sview_measure_size(view);
}

int year1_sview_sync(struct sview *view) {
	return time_num_sview_sync(view);
}

void year1_sview_free(struct sview *view) {
	return time_num_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_year1_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_time_num_sview(view, parent);
}
#endif
#endif

int init_year1_sview(struct year1_sview *year1, const char *name) {

	init_time_num_sview(&year1->timev, name);

	to_sview(year1)->is_alloc = 0;
	to_sview(year1)->type = SVIEW_YEAR1;

	time_num_sview_set_level(to_sview(year1), TIME_NUM_YEAR_1);
	time_num_sview_set_pic_grp(to_sview(year1), "small_nums");

	return 0;
}

struct sview *alloc_year1_sview(const char *name) {
	struct year1_sview *year1;
	char *cpy_name;

	year1 = malloc_with_name(sizeof(*year1), name);
	if (!year1) {
		pr_err("year1_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&year1[1];

	init_year1_sview(year1, cpy_name);

	to_sview(year1)->is_alloc = 1;

	return to_sview(year1);
}
