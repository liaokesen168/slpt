#include <common.h>
#include <sview/minuteH_sview.h>

/* minuteH_sview */

/*
 * see sview/minuteH_sview.h,
 * those sview methods is inheritance from time_num_sview
 */
#if 0
void minuteH_sview_draw(struct sview *view) {
	time_num_sview_draw(view);
}

void minuteH_sview_measure_size(struct sview *view) {
	time_num_sview_measure_size(view);
}

int minuteH_sview_sync(struct sview *view) {
	return time_num_sview_sync(view);
}

void minuteH_sview_free(struct sview *view) {
	return time_num_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_minuteH_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_time_num_sview(view, parent);
}
#endif
#endif

int init_minuteH_sview(struct minuteH_sview *minuteH, const char *name) {

	init_time_num_sview(&minuteH->timev, name);

	to_sview(minuteH)->is_alloc = 0;
	to_sview(minuteH)->type = SVIEW_MINUTE_H;

	time_num_sview_set_level(to_sview(minuteH), TIME_NUM_MIN_H);
	time_num_sview_set_pic_grp(to_sview(minuteH), "large_nums");

	return 0;
}

struct sview *alloc_minuteH_sview(const char *name) {
	struct minuteH_sview *minuteH;
	char *cpy_name;

	minuteH = malloc_with_name(sizeof(*minuteH), name);
	if (!minuteH) {
		pr_err("minuteH_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&minuteH[1];

	init_minuteH_sview(minuteH, cpy_name);

	to_sview(minuteH)->is_alloc = 1;

	return to_sview(minuteH);
}
