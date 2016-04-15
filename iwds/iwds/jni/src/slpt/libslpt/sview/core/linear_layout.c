#include <common.h>
#include <sview/sview.h>
#include <sview/sview_grp.h>
#include <sview/linear_layout.h>

int linear_layout_get_orientation(struct sview *view) {
	struct linear_layout *layout = to_linear_layout(view);

	return layout->orientation;
}

void linear_layout_set_orientation(struct sview *view, int orientation) {
	struct linear_layout *layout = to_linear_layout(view);

	layout->orientation = orientation == HORIZONTAL ? HORIZONTAL : VERTICAL;
}

void linear_layout_add(struct sview *view, struct sview *child) {
	if (!child)
		return;

	sview_grp_add_by_level(&view->grp, child);
	child->parent = view;
}

void linear_layout_add_array(struct sview *view, struct sview **array, unsigned int size) {
	unsigned int i;

	if (!(array && size))
		return;

	for (i = 0; i < size; ++i) {
		linear_layout_add(view, array[i]);
	}
}

void linear_layout_draw(struct sview *view) {
	struct linear_layout *layout = to_linear_layout(view);
	struct fb_region *base = &view->base;
	struct position *start = &view->position;
	struct position position = *start;
	unsigned char align;
	struct sview *v;
	struct list_head *p, *n;

	if (layout->orientation == HORIZONTAL) {
		list_for_each_safe(p, n, &view->grp) {
			v = list_entry(p, struct sview, link);
			if (!sview_can_be_show(v))
				continue;

			if (v->center_vertical) {
				position.y = ((int) view->rect.h - (int) v->rect.h) / 2;
			} else {
				align = sview_align(v->align_parent_y, view->align_y);
				switch (align) {
				case ALIGN_TOP:
					position.y = v->padding.top;
					break;
				case ALIGN_BOTTOM:
					position.y = (int) view->rect.h - (int) (v->rect.h + v->padding.bottom);
					break;
				case ALIGN_CENTER:
					position.y = ((int) view->rect.h - (int) v->rect.h) / 2;
					break;
				default:
					break;
				}
			}
			position.y += start->y;
			position.x += v->padding.left;

			v->base = *base;
			v->position = position;

			sview_draw(v);

			position.x += v->rect.w;
		}
	} else { /* VERTICAL */
		list_for_each_safe(p, n, &view->grp) {
			v = list_entry(p, struct sview, link);
			if (!sview_can_be_show(v))
				continue;

			if (v->center_horizontal) {
				position.x = ((int) view->rect.w - (int) v->rect.w) / 2;
			} else {
				align = sview_align(v->align_parent_x, view->align_x);
				switch (align) {
				case ALIGN_LEFT:
					position.x = v->padding.left;
					break;
				case ALIGN_RIGHT:
					position.x = (int) view->rect.w - (int) (v->rect.w + v->padding.right);
					break;
				case ALIGN_CENTER:
					position.x = ((int) view->rect.w - (int) v->rect.w) / 2;
					break;
				default:
					break;
				}
			}
			position.x += start->x;
			position.y += v->padding.top;

			v->base = *base;
			v->position = position;

			sview_draw(v);

			position.y += v->rect.h;
		}
	}
}

void linear_layout_measure_size(struct sview *view) {
	struct linear_layout *layout = to_linear_layout(view);
	struct sview *v;
	struct list_head *pos, *n;

	unsigned int width = 0;
	unsigned int height = 0;
	unsigned int tmp;
	unsigned char align;

	if (layout->orientation == HORIZONTAL) {
		list_for_each_safe(pos, n, &view->grp) {
			v = list_entry(pos, struct sview, link);
			if (!sview_can_be_show(v))
				continue;

			sview_measure_size(v);
			width += v->rect.w + v->padding.left + v->padding.right;
			align = sview_align(v->align_parent_y, view->align_y);
			if (align == ALIGN_CENTER || v->center_vertical) {
				if (height < v->rect.h)
					height = v->rect.h;
			} else {
				tmp = v->rect.h + v->padding.top + v->padding.bottom;
				if (height < tmp)
					height = tmp;
			}
		}
	} else { /* VERTICAL */
		list_for_each_safe(pos, n, &view->grp) {
			v = list_entry(pos, struct sview, link);
			if (!sview_can_be_show(v))
				continue;

			sview_measure_size(v);
			height += v->rect.h + v->padding.top + v->padding.bottom;
			align = sview_align(v->align_parent_x, view->align_x);
			if (align == ALIGN_CENTER || v->center_horizontal) {
				if (width < v->rect.w)
					width = v->rect.w;
			} else {
				tmp = v->rect.w + v->padding.left + v->padding.right;
				if (width < tmp)
					width = tmp;
			}
		}
	}

	view->raw_rect.w = width;
	view->raw_rect.h = height;
}

int linear_layout_sync(struct sview *view) {
	struct linear_layout *layout = to_linear_layout(view);

	if (layout->orientation > VERTICAL)
		layout->orientation = HORIZONTAL;

	if (layout->orientation == HORIZONTAL) {
		to_sview(layout)->layout_align_x = 0;
		to_sview(layout)->layout_align_y = 1;
	} else { /* VERTICAL */
		to_sview(layout)->layout_align_x = 1;
		to_sview(layout)->layout_align_y = 0;
	}

	return sview_grp_sync(&view->grp);
}

void linear_layout_free(struct sview *view) {
	struct linear_layout *layout = to_linear_layout(view);

	sview_grp_free(&view->grp);

	if (view->is_alloc)
		free(layout);
}

#ifdef CONFIG_SLPT
static struct slpt_app_res linear_layout_res[] = {
	SLPT_RES_EMPTY_DEF("orientation", SLPT_RES_INT),     /* picture */
};

struct slpt_app_res *slpt_register_linear_layout(struct sview *view, struct slpt_app_res *parent) {
	struct linear_layout *layout = to_linear_layout(view);
	struct slpt_app_res *res;

	slpt_set_res(linear_layout_res[0], &layout->orientation, 1);
	res = slpt_register_sview_base(view, parent, linear_layout_res, ARRAY_SIZE(linear_layout_res));
	if (!res)
		return NULL;

	if (!slpt_register_sview_grp(&view->grp, res)) {
		slpt_kernel_unregister_app_res(res, uboot_slpt_task);
		res = NULL;
	}

	return res;
}
#endif

void init_linear_layout(struct linear_layout *layout, const char *name) {
	sview_init_base(&layout->view, name, SVIEW_LINEAR_LAYOUT);

	layout->orientation = HORIZONTAL;
}

struct sview *alloc_linear_layout(const char *name) {
	struct linear_layout *layout;
	char *cpy_name;

	layout = malloc_with_name(sizeof(*layout), name);
	if (!layout) {
		pr_err("linear_layout: failed to alloc\n");
		return NULL;
	}
	cpy_name = (char *) &layout[1];

	init_linear_layout(layout, cpy_name);

	layout->view.is_alloc = 1;

	return &layout->view;
}
