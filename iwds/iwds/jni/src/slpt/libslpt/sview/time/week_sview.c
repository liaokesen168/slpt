#include <common.h>
#include <sview/week_sview.h>

/* week_sview */

/*
 * see sview/week_sview.h,
 * those sview methods is inheritance from time_num_sview
 */
#if 0
void week_sview_draw(struct sview *view) {
	time_num_sview_draw(view);
}

void week_sview_measure_size(struct sview *view) {
	time_num_sview_measure_size(view);
}

int week_sview_sync(struct sview *view) {
	return time_num_sview_sync(view);
}

void week_sview_free(struct sview *view) {
	return time_num_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_week_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_time_num_sview(view, parent);
}
#endif
#endif

int init_week_sview(struct week_sview *week, const char *name) {

	init_time_num_sview(&week->timev, name);

	to_sview(week)->is_alloc = 0;
	to_sview(week)->type = SVIEW_WEEK;

	time_num_sview_set_level(to_sview(week), TIME_NUM_WEEK);
	time_num_sview_set_pic_grp(to_sview(week), "week_nums");

	return 0;
}

struct sview *alloc_week_sview(const char *name) {
	struct week_sview *week;
	char *cpy_name;

	week = malloc_with_name(sizeof(*week), name);
	if (!week) {
		pr_err("week_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&week[1];

	init_week_sview(week, cpy_name);

	to_sview(week)->is_alloc = 1;

	return to_sview(week);
}
