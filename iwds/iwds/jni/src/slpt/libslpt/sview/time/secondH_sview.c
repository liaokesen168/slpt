#include <common.h>
#include <sview/secondH_sview.h>

/* secondH_sview */

/*
 * see sview/secondH_sview.h,
 * those sview methods is inheritance from time_num_sview
 */
#if 0
void secondH_sview_draw(struct sview *view) {
	time_num_sview_draw(view);
}

void secondH_sview_measure_size(struct sview *view) {
	time_num_sview_measure_size(view);
}

int secondH_sview_sync(struct sview *view) {
	return time_num_sview_sync(view);
}

void secondH_sview_free(struct sview *view) {
	return time_num_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_secondH_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_time_num_sview(view, parent);
}
#endif
#endif

int init_secondH_sview(struct secondH_sview *secondH, const char *name) {

	init_time_num_sview(&secondH->timev, name);

	to_sview(secondH)->is_alloc = 0;
	to_sview(secondH)->type = SVIEW_SECOND_H;

	time_num_sview_set_level(to_sview(secondH), TIME_NUM_SEC_H);
	time_num_sview_set_pic_grp(to_sview(secondH), "small_nums");

	return 0;
}

struct sview *alloc_secondH_sview(const char *name) {
	struct secondH_sview *secondH;
	char *cpy_name;

	secondH = malloc_with_name(sizeof(*secondH), name);
	if (!secondH) {
		pr_err("secondH_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&secondH[1];

	init_secondH_sview(secondH, cpy_name);

	to_sview(secondH)->is_alloc = 1;

	return to_sview(secondH);
}
