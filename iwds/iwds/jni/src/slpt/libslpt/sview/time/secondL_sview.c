#include <common.h>
#include <sview/secondL_sview.h>

/* secondL_sview */

/*
 * see sview/secondL_sview.h,
 * those sview methods is inheritance from time_num_sview
 */
#if 0
void secondL_sview_draw(struct sview *view) {
	time_num_sview_draw(view);
}

void secondL_sview_measure_size(struct sview *view) {
	time_num_sview_measure_size(view);
}

int secondL_sview_sync(struct sview *view) {
	return time_num_sview_sync(view);
}

void secondL_sview_free(struct sview *view) {
	return time_num_sview_free(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_secondL_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_time_num_sview(view, parent);
}
#endif
#endif

int init_secondL_sview(struct secondL_sview *secondL, const char *name) {

	init_time_num_sview(&secondL->timev, name);

	to_sview(secondL)->is_alloc = 0;
	to_sview(secondL)->type = SVIEW_SECOND_L;

	time_num_sview_set_level(to_sview(secondL), TIME_NUM_SEC_L);
	time_num_sview_set_pic_grp(to_sview(secondL), "small_nums");

	return 0;
}

struct sview *alloc_secondL_sview(const char *name) {
	struct secondL_sview *secondL;
	char *cpy_name;

	secondL = malloc_with_name(sizeof(*secondL), name);
	if (!secondL) {
		pr_err("secondL_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&secondL[1];

	init_secondL_sview(secondL, cpy_name);

	to_sview(secondL)->is_alloc = 1;

	return to_sview(secondL);
}
