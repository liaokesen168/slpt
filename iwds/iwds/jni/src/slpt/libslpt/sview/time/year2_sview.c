#include <common.h>
#include <sview/year2_sview.h>

/* year2_sview */

/*
 * see sview/year2_sview.h,
 * those sview methods is inheritance from time_num_sview
 */
#if 0
void year2_sview_draw(struct sview *view) {
	time_num_sview_draw(view);
}

void year2_sview_measure_size(struct sview *view) {
	time_num_sview_measure_size(view);
}

int year2_sview_sync(struct sview *view) {
	return time_num_sview_sync(view);
}

void year2_sview_free(struct sview *view) {
	return time_num_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_year2_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_time_num_sview(view, parent);
}
#endif
#endif

int init_year2_sview(struct year2_sview *year2, const char *name) {

	init_time_num_sview(&year2->timev, name);

	to_sview(year2)->is_alloc = 0;
	to_sview(year2)->type = SVIEW_YEAR2;

	time_num_sview_set_level(to_sview(year2), TIME_NUM_YEAR_2);
	time_num_sview_set_pic_grp(to_sview(year2), "small_nums");

	return 0;
}

struct sview *alloc_year2_sview(const char *name) {
	struct year2_sview *year2;
	char *cpy_name;

	year2 = malloc_with_name(sizeof(*year2), name);
	if (!year2) {
		pr_err("year2_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&year2[1];

	init_year2_sview(year2, cpy_name);

	to_sview(year2)->is_alloc = 1;

	return to_sview(year2);
}
