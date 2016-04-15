#include <common.h>
#include <sview/sview.h>

static unsigned short sview_id_counter = 0;

unsigned short sview_get_id_nums(void) {
	return sview_id_counter;
}

void sview_reset_id_counter(void) {
	sview_id_counter = 0;
}

void sview_init_base(struct sview *view, const char *name, unsigned int type) {
	memset(view, 0, sizeof(*view));
	view->name = name;
	view->type = type;
	view->id = sview_id_counter++;
	view->show = 1;
	view->background.color = INVALID_COLOR;
	view->align_parent_x = ALIGN_BY_PARENT;
	view->align_parent_y = ALIGN_BY_PARENT;
	INIT_LIST_HEAD(&view->grp);
	INIT_LIST_HEAD(&view->link);
}

int sview_sync_setting(struct sview *view) {
	int sync;

	if (view->align_x > ALIGN_CENTER)
		view->align_x = ALIGN_LEFT;

	if (view->align_y > ALIGN_CENTER)
		view->align_y = ALIGN_TOP;

	if (view->desc_w > RECT_SPECIFY)
		view->desc_w = RECT_FIT_BACKGROUND;

	if (view->desc_h > RECT_SPECIFY)
		view->desc_h = RECT_FIT_BACKGROUND;

	if (view->align_parent_x > ALIGN_BY_PARENT)
		view->align_parent_x = ALIGN_BY_PARENT;

	if (view->align_parent_y > ALIGN_BY_PARENT)
		view->align_parent_y = ALIGN_BY_PARENT;

	background_sync_setting(&view->background);

	sync = sview_method_sync_setting[view->type](view);
	pr_debug ("sview sync %s %d\n", view->name, sync);

	view->update = 1;
	view->ready = !sync;

	return sync;
}

void sview_measure_size(struct sview *view) {
	struct fb_region *region;

	if (!view->ready)
		return;

	sview_method_measure_size[view->type](view);

	region = background_region(&view->background);

	switch (view->desc_w) {
	case RECT_FIT_BACKGROUND:
		if (region != NULL)
			view->rect.w = region->xres;
		else
			view->rect.w = view->raw_rect.w;
		break;
	case RECT_WRAP_CONTENT:
		if (region != NULL)
			view->rect.w = max(view->raw_rect.w, region->xres);
		else
			view->rect.w = view->raw_rect.w;
		break;
	case RECT_SPECIFY:
		/* view->rect.w = view->rect.w; */
		break;
	default:
		break;
	}

	switch (view->desc_h) {
	case RECT_FIT_BACKGROUND:
		if (region != NULL)
			view->rect.h = region->yres;
		else
			view->rect.h = view->raw_rect.h;
		break;
	case RECT_WRAP_CONTENT:
		if (region != NULL)
			view->rect.h = max(view->raw_rect.h, region->yres);
		else
			view->rect.h = view->raw_rect.h;
		break;
	case RECT_SPECIFY:
		/* view->rect.h = view->rect.h; */
		break;
	default:
		break;
	}

	pr_debug ("measure %s rect: %d %d %p\n", view->name, view->rect.w, view->rect.h, region);
}

void sview_draw(struct sview *view) {
	struct fb_region *base = &view->base;
	struct position *pos = &view->position;

	int xres = (int)view->rect.w;
	int yres = (int)view->rect.h;

	if (!view->ready)
		return;

	pr_debug ("draw %s pos: (%d, %d) base: (%p, %d, %d)\n",
			view->name, pos->x, pos->y, base->base, base->xres, base->yres);

	pr_debug("alignx %u aligny %u\n", view->align_x, view->align_y);

	if (xres == 0 || yres == 0)
		return;
	if (pos->x > (int)base->xres || pos->y > (int)base->yres)
		return;

	if (pos->x <= 0) {
		xres += pos->x;
	} else {
		base->base = (unsigned int *)base->base + pos->x;
		base->xres -= pos->x;
		pos->x = 0;
	}

	if (pos->y <= 0) {
		yres += pos->y;
	} else {
		base->base = ((unsigned int *)base->base) + pos->y * base->pixels_per_line;
		base->yres -= pos->y;
		pos->y = 0;
	}

	if (xres <= 0 || yres <= 0)
		return;

	if ((int )base->xres > xres)
		base->xres = xres;

	if ((int )base->yres > yres)
		base->yres = yres;

	background_write_to_target(&view->background, base, pos);

	if (!view->layout_align_x) {
		switch (view->align_x) {
		case ALIGN_LEFT:
			break;
		case ALIGN_RIGHT:
			pos->x += (int) view->rect.w - (int) view->raw_rect.w;
			break;
		case ALIGN_CENTER:
			pos->x += ((int) view->rect.w - (int) view->raw_rect.w) / 2;
			break;
		default:
			break;
		}
	}

	if (!view->layout_align_y) {
		switch (view->align_y) {
		case ALIGN_TOP:
			break;
		case ALIGN_BOTTOM:
			pos->y += (int) view->rect.h - (int) view->raw_rect.h;
			break;
		case ALIGN_CENTER:
			pos->y += ((int) view->rect.h - (int) view->raw_rect.h) / 2;
			break;
		default:
			break;
		}
	}

	pr_debug ("draw %s pos: (%d, %d) base: (%p, %d, %d)\n",
			view->name, pos->x, pos->y, base->base, base->xres, base->yres);

	sview_method_draw[view->type](view);
}

void sview_free(struct sview *view) {
	list_del(&view->link);
	sview_method_free[view->type](view);
}

#ifdef CONFIG_SLPT
static struct slpt_app_res view_res[] = {
	SLPT_RES_EMPTY_DEF("type", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("position-x", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("position-y", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("padding-left", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("padding-right", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("padding-top", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("padding-bottom", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("rect-w", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("rect-h", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("backgroud-color", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("backgroud-picture", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("level", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("align-x", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("align-y", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("desc-w", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("desc-h", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("center-horizontal", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("center-vertical", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("align-parent-x", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("align-parent-y", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("show", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("id", SLPT_RES_INT),
};

struct slpt_app_res *slpt_register_sview_base(struct sview *view,
											  struct slpt_app_res *parent,
											  struct slpt_app_res *array,
											  unsigned int size) {
	struct slpt_app_res *res;
	struct slpt_app_res tmp = SLPT_RES_DIR_DEF(view->name, view_res);

	slpt_set_res(view_res[0], &view->type, 2);
	slpt_set_res(view_res[1], &view->raw_position.x, 4);
	slpt_set_res(view_res[2], &view->raw_position.y, 4);
	slpt_set_res(view_res[3], &view->padding.left, 2);
	slpt_set_res(view_res[4], &view->padding.right, 2);
	slpt_set_res(view_res[5], &view->padding.top, 2);
	slpt_set_res(view_res[6], &view->padding.bottom, 2);
	slpt_set_res(view_res[7], &view->rect.w, 4);
	slpt_set_res(view_res[8], &view->rect.h, 4);
	slpt_set_res(view_res[9], &view->background.color, 4);
	slpt_set_res(view_res[10], view->background.pic_name, sizeof(view->background.pic_name));
	slpt_set_res(view_res[11], &view->level, 2);
	slpt_set_res(view_res[12], &view->align_x, 1);
	slpt_set_res(view_res[13], &view->align_y, 1);
	slpt_set_res(view_res[14], &view->desc_w, 1);
	slpt_set_res(view_res[15], &view->desc_h, 1);
	slpt_set_res(view_res[16], &view->center_horizontal, 1);
	slpt_set_res(view_res[17], &view->center_vertical, 1);
	slpt_set_res(view_res[18], &view->align_parent_x, 1);
	slpt_set_res(view_res[19], &view->align_parent_y, 1);
	slpt_set_res(view_res[20], &view->show, 1);
	slpt_set_res(view_res[21], &view->id, 2);

	res = slpt_kernel_register_app_dir_res(&tmp, parent);
	if (!res) {
		pr_err("sview: failed to regiseter view to slpt: %s\n", view->name);
		return NULL;
	}

	if (array && size) {
		if (!slpt_kernel_register_app_child_res_list(array, size, res)) {
			pr_err("sview: failed to register additional res list\n");
			slpt_kernel_unregister_app_res(res, uboot_slpt_task);
			res = NULL;
		}
	}

	return res;
}

struct slpt_app_res *slpt_register_sview(struct sview *view,
										 struct slpt_app_res *parent,
										 struct slpt_app_res *array,
										 unsigned int size) {
	struct slpt_app_res *res;

	res = sview_method_register_slpt[view->type](view, parent);
	if (!res) {
		pr_err("sview: failed to register view to slpt: %s\n", view->name);
		return NULL;
	}

	if (array && size) {
		if (!slpt_kernel_register_app_child_res_list(array, size, res)) {
			pr_err("sview: failed to register additional res list\n");
			slpt_kernel_unregister_app_res(res, uboot_slpt_task);
			res = NULL;
		}
	}

	view->res = res;
	return res;
}

void slpt_unregister_sview(struct sview *view) {
	slpt_kernel_unregister_app_res(view->res, uboot_slpt_task);
}
#endif
