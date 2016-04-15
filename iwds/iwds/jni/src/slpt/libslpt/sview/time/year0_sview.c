#include <common.h>
#include <sview/year0_sview.h>

/* year0_sview */

/*
 * see sview/year0_sview.h,
 * those sview methods is inheritance from time_num_sview
 */
#if 0
void year0_sview_draw(struct sview *view) {
	time_num_sview_draw(view);
}

void year0_sview_measure_size(struct sview *view) {
	time_num_sview_measure_size(view);
}

int year0_sview_sync(struct sview *view) {
	return time_num_sview_sync(view);
}

void year0_sview_free(struct sview *view) {
	return time_num_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_year0_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_time_num_sview(view, parent);
}
#endif
#endif

int init_year0_sview(struct year0_sview *year0, const char *name) {

	init_time_num_sview(&year0->timev, name);

	to_sview(year0)->is_alloc = 0;
	to_sview(year0)->type = SVIEW_YEAR0;

	time_num_sview_set_level(to_sview(year0), TIME_NUM_YEAR_0);
	time_num_sview_set_pic_grp(to_sview(year0), "small_nums");

	return 0;
}

struct sview *alloc_year0_sview(const char *name) {
	struct year0_sview *year0;
	char *cpy_name;

	year0 = malloc_with_name(sizeof(*year0), name);
	if (!year0) {
		pr_err("year0_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&year0[1];

	init_year0_sview(year0, cpy_name);

	to_sview(year0)->is_alloc = 1;

	return to_sview(year0);
}
