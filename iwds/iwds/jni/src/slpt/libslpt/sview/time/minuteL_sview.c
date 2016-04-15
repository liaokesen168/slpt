#include <common.h>
#include <sview/minuteL_sview.h>

/* minuteL_sview */

/*
 * see sview/minuteL_sview.h,
 * those sview methods is inheritance from time_num_sview
 */
#if 0
void minuteL_sview_draw(struct sview *view) {
	time_num_sview_draw(view);
}

void minuteL_sview_measure_size(struct sview *view) {
	time_num_sview_measure_size(view);
}

int minuteL_sview_sync(struct sview *view) {
	return time_num_sview_sync(view);
}

void minuteL_sview_free(struct sview *view) {
	return time_num_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_minuteL_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_time_num_sview(view, parent);
}
#endif
#endif

int init_minuteL_sview(struct minuteL_sview *minuteL, const char *name) {

	init_time_num_sview(&minuteL->timev, name);

	to_sview(minuteL)->is_alloc = 0;
	to_sview(minuteL)->type = SVIEW_MINUTE_L;

	time_num_sview_set_level(to_sview(minuteL), TIME_NUM_MIN_L);
	time_num_sview_set_pic_grp(to_sview(minuteL), "large_nums");

	return 0;
}

struct sview *alloc_minuteL_sview(const char *name) {
	struct minuteL_sview *minuteL;
	char *cpy_name;

	minuteL = malloc_with_name(sizeof(*minuteL), name);
	if (!minuteL) {
		pr_err("minuteL_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&minuteL[1];

	init_minuteL_sview(minuteL, cpy_name);

	to_sview(minuteL)->is_alloc = 1;

	return to_sview(minuteL);
}
