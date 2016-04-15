#include <common.h>
#include <sview/sview.h>
#include <sview/sview_grp.h>
#include <sview/absolute_layout.h>

void absolute_layout_add(struct sview *view, struct sview *child) {
	if (!child)
		return;

	sview_grp_add_by_level(&view->grp, child);
	child->parent = view;
}

void absolute_layout_add_array(struct sview *view, struct sview **array, unsigned int size) {
	unsigned int i;

	if (!(array && size))
		return;

	for (i = 0; i < size; ++i) {
		absolute_layout_add(view, array[i]);
	}
}

void absolute_layout_draw(struct sview *view) {
	struct absolute_layout *layout = to_absolute_layout(view);
	struct position *start = &view->position;
	struct sview *v;
	struct list_head *p, *n;
	int tmp = 0;

	printf ("position x y: %d %d\n", start->x, start->y);

	list_for_each_safe(p, n, &view->grp) {
		v = list_entry(p, struct sview, link);
		if (!sview_can_be_show(v))
			continue;

		if (v->center_horizontal) {
			v->position.x = ((int) view->rect.w - (int) v->rect.w) / 2;
		} else {
			switch (layout->position_of_x_start) {
			case POSITION_LEFT:
				tmp = 0;
				break;
			case POSITION_RIGHT:
				tmp = (int) v->rect.w;
				break;
			case POSITION_CENTER:
				tmp = (int) (v->rect.w / 2);
				break;
			default:
				break;
			}
			v->position.x = v->raw_position.x - tmp;
		}

		if (v->center_vertical) {
			v->position.y = ((int) view->rect.h - (int) v->rect.h) / 2;
		} else {
			switch (layout->position_of_y_start) {
			case POSITION_TOP:
				tmp = 0;
				break;
			case POSITION_BOTTOM:
				tmp = (int) v->rect.h;
				break;
			case POSITION_CENTER:
				tmp = (int) (v->rect.h / 2);
				break;
			default:
				break;
			}
			v->position.y = v->raw_position.y - tmp;
		}

		v->position.x += start->x;
		v->position.y += start->y;
		v->base = view->base;

		sview_draw(v);
	}
}

void absolute_layout_measure_size(struct sview *view) {
	struct absolute_layout *layout = to_absolute_layout(view);
	struct sview *v;
	struct list_head *pos, *n;

	int x_end = 0;
	int y_end = 0;
	int width = 0;
	int height = 0;

	list_for_each_safe(pos, n, &view->grp) {
		v = list_entry(pos, struct sview, link);
		if (!sview_can_be_show(v))
			continue;

		sview_measure_size(v);

		if (v->center_horizontal) {
			x_end = v->rect.w;
		} else {
			switch (layout->position_of_x_start) {
			case POSITION_LEFT:
				x_end = (int) v->rect.w;
				break;
			case POSITION_RIGHT:
				x_end = 0;
				break;
			case POSITION_CENTER:
				x_end = (int) (v->rect.w / 2);
				break;
			default:
				break;
			}
			x_end += v->raw_position.x;
		}

		if (v->center_vertical) {
			y_end = v->rect.h;
		} else {
			switch (layout->position_of_y_start) {
			case POSITION_TOP:
				y_end = (int) v->rect.h;
				break;
			case POSITION_BOTTOM:
				y_end = 0;
				break;
			case POSITION_CENTER:
				y_end = (int) (v->rect.h / 2);
				break;
			default:
				break;
			}
			y_end += v->raw_position.y;
		}

		if (width < x_end)
			width = x_end;
		if (height < y_end)
			height = y_end;
	}

	view->raw_rect.w = width;
	view->raw_rect.h = height;
}

int absolute_layout_sync(struct sview *view) {
	struct absolute_layout *layout = to_absolute_layout(view);

	if (layout->position_of_x_start > POSITION_CENTER)
		layout->position_of_x_start = POSITION_LEFT;

	if (layout->position_of_y_start > POSITION_CENTER)
		layout->position_of_y_start = POSITION_TOP;

	return sview_grp_sync(&view->grp);
}

void absolute_layout_free(struct sview *view) {
	struct absolute_layout *layout = to_absolute_layout(view);

	sview_grp_free(&view->grp);

	if (view->is_alloc)
		free(layout);
}

#ifdef CONFIG_SLPT
static struct slpt_app_res absolute_layout_res[] = {
	SLPT_RES_EMPTY_DEF("position_of_x_start", SLPT_RES_MEM),     /* picture */
	SLPT_RES_EMPTY_DEF("position_of_y_start", SLPT_RES_MEM),     /* picture */
};

struct slpt_app_res *slpt_register_absolute_layout(struct sview *view, struct slpt_app_res *parent) {
	struct absolute_layout *layout = to_absolute_layout(view);
	struct slpt_app_res *res;

	slpt_set_res(absolute_layout_res[0], &layout->position_of_x_start, 1);
	slpt_set_res(absolute_layout_res[1], &layout->position_of_y_start, 1);
	res = slpt_register_sview_base(view, parent, absolute_layout_res, ARRAY_SIZE(absolute_layout_res));
	if (!res)
		return NULL;

	if (!slpt_register_sview_grp(&view->grp, res)) {
		slpt_kernel_unregister_app_res(res, uboot_slpt_task);
		res = NULL;
	}

	return res;
}
#endif

void init_absolute_layout(struct absolute_layout *layout, const char *name) {
	sview_init_base(&layout->view, name, SVIEW_ABSOLUTE_LAYOUT);
	to_sview(layout)->layout_align_x = 0;
	to_sview(layout)->layout_align_y = 0;
	layout->position_of_x_start = POSITION_LEFT;
	layout->position_of_y_start = POSITION_TOP;
}

struct sview *alloc_absolute_layout(const char *name) {
	struct absolute_layout *layout;
	char *cpy_name;

	layout = malloc_with_name(sizeof(*layout), name);
	if (!layout) {
		pr_err("absolute_layout: failed to alloc\n");
		return NULL;
	}
	cpy_name = (char *) &layout[1];

	init_absolute_layout(layout, cpy_name);

	layout->view.is_alloc = 1;

	return &layout->view;
}
