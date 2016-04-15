#include <common.h>
#include <sview/hourH_sview.h>

/* hourH_sview */

/*
 * see sview/hourH_sview.h,
 * those sview methods is inheritance from time_num_sview
 */
#if 0
void hourH_sview_draw(struct sview *view) {
	time_num_sview_draw(view);
}

void hourH_sview_measure_size(struct sview *view) {
	time_num_sview_measure_size(view);
}

int hourH_sview_sync(struct sview *view) {
	return time_num_sview_sync(view);
}

void hourH_sview_free(struct sview *view) {
	return time_num_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_hourH_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_time_num_sview(view, parent);
}
#endif
#endif

int init_hourH_sview(struct hourH_sview *hourH, const char *name) {

	init_time_num_sview(&hourH->timev, name);

	to_sview(hourH)->is_alloc = 0;
	to_sview(hourH)->type = SVIEW_HOUR_H;

	time_num_sview_set_level(to_sview(hourH), TIME_NUM_HOUR_H);
	time_num_sview_set_pic_grp(to_sview(hourH), "large_nums");

	return 0;
}

struct sview *alloc_hourH_sview(const char *name) {
	struct hourH_sview *hourH;
	char *cpy_name;

	hourH = malloc_with_name(sizeof(*hourH), name);
	if (!hourH) {
		pr_err("hourH_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&hourH[1];

	init_hourH_sview(hourH, cpy_name);

	to_sview(hourH)->is_alloc = 1;

	return to_sview(hourH);
}
