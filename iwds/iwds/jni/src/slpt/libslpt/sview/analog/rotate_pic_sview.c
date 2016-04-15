#include <common.h>
#include <sview/sview.h>
#include <sview/rotate_pic_sview.h>

/* pic sview */

/*
 * see sview/rotate_pic_sview.h,
 * those sview methods is inheritance from pic_sview
 */
#if 0
#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_rotate_pic_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_pic_sview(view, parent);
}
#endif
#endif

void rotate_pic_sview_draw(struct sview *view) {
	struct rotate_pic_sview *rpv  = to_rotate_pic_sview(view);
	
	rotate2_draw(&rpv->rotate, &view->base, &view->position);
}

void rotate_pic_sview_measure_size(struct sview *view) {
	struct rotate_pic_sview *rpv  = to_rotate_pic_sview(view);
	rotate2_measure_size(&rpv->rotate, &view->raw_rect);
}

int rotate_pic_sview_sync(struct sview *view) {
	struct rotate_pic_sview *rpv  = to_rotate_pic_sview(view);
	int sync;

	sync = pic_sview_sync(view);
	rotate2_set_region(&rpv->rotate, pic_sview_get_region(view));

	return sync;
}

void rotate_pic_sview_free(struct sview *view) {
	struct rotate_pic_sview *rpv  = to_rotate_pic_sview(view);
	unsigned int is_alloc = view->is_alloc;

	rotate2_free_save_colors(&rpv->rotate);

	view->is_alloc = 0;
	pic_sview_free(view);

	if (is_alloc)
		free(rpv);
}

void init_rotate_pic_sview(struct rotate_pic_sview *rpv, const char *name) {
	init_pic_sview(&rpv->picv, name);

	to_sview(rpv)->is_alloc = 0;
	to_sview(rpv)->type = SVIEW_ROTATE_PIC;

	init_rotate2(&rpv->rotate);
	rotate2_set_align_center(&rpv->rotate, 1);
}

struct sview *alloc_rotate_pic_sview(const char *name) {
	struct rotate_pic_sview *rpv;
	char *cpy_name;

	rpv = malloc_with_name(sizeof(*rpv), name);
	if (!rpv) {
		pr_err("pic_view: failed to alloc\n");
		return NULL;
	}
	cpy_name = (char *)&rpv[1];

	init_rotate_pic_sview(rpv, cpy_name);

	to_sview(rpv)->is_alloc = 1;

	return to_sview(rpv);
}
