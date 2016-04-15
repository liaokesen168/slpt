#include <common.h>
#include <sview/year3_sview.h>

/* year3_sview */

/*
 * see sview/year3_sview.h,
 * those sview methods is inheritance from time_num_sview
 */
#if 0
void year3_sview_draw(struct sview *view) {
	time_num_sview_draw(view);
}

void year3_sview_measure_size(struct sview *view) {
	time_num_sview_measure_size(view);
}

int year3_sview_sync(struct sview *view) {
	return time_num_sview_sync(view);
}

void year3_sview_free(struct sview *view) {
	return time_num_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_year3_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_time_num_sview(view, parent);
}
#endif
#endif

int init_year3_sview(struct year3_sview *year3, const char *name) {

	init_time_num_sview(&year3->timev, name);

	to_sview(year3)->is_alloc = 0;
	to_sview(year3)->type = SVIEW_YEAR3;

	time_num_sview_set_level(to_sview(year3), TIME_NUM_YEAR_3);
	time_num_sview_set_pic_grp(to_sview(year3), "small_nums");

	return 0;
}

struct sview *alloc_year3_sview(const char *name) {
	struct year3_sview *year3;
	char *cpy_name;

	year3 = malloc_with_name(sizeof(*year3), name);
	if (!year3) {
		pr_err("year3_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&year3[1];

	init_year3_sview(year3, cpy_name);

	to_sview(year3)->is_alloc = 1;

	return to_sview(year3);
}
