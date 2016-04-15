#include <common.h>
#include <sview/sview.h>
#include <sview/sview_grp.h>
#include <sview/frame_layout.h>

void frame_layout_add(struct sview *view, struct sview *child) {
	if (!child)
		return;

	sview_grp_add_by_level(&view->grp, child);
	child->parent = view;
}

void frame_layout_add_array(struct sview *view, struct sview **array, unsigned int size) {
	unsigned int i;

	if (!(array && size))
		return;

	for (i = 0; i < size; ++i) {
		frame_layout_add(view, array[i]);
	}
}

void frame_layout_draw(struct sview *view) {
	struct position *start = &view->position;
	struct sview *v;
	struct list_head *p, *n;
	unsigned char align;

	list_for_each_safe(p, n, &view->grp) {
		v = list_entry(p, struct sview, link);
		if (!sview_can_be_show(v))
			continue;

		pr_debug("frame layout: %d %d --.  %d %d\n", view->rect.w, view->rect.h, v->rect.w, v->rect.h);
		pr_debug("frame layout: %d %d --\n\n", v->padding.right, v->padding.bottom);
		pr_debug("frame layout: %d %d --\n\n", start->x, start->y);

		if (v->center_horizontal) {
			v->position.x = ((int) view->rect.w - (int) v->rect.w) / 2;
		} else {
			align = sview_align(v->align_parent_x, view->align_x);
			switch (align) {
			case ALIGN_LEFT:
				v->position.x = v->padding.left;
				break;
			case ALIGN_RIGHT:
				v->position.x = (int) view->rect.w - (int) (v->rect.w + v->padding.right);
				break;
			case ALIGN_CENTER:
				v->position.x = ((int) view->rect.w - (int) v->rect.w) / 2;
				break;
			default:
				break;
			}
			pr_debug("align: %d\n", (int)align);
		}
		v->position.x += start->x;

		if (v->center_vertical) {
			v->position.y = ((int) view->rect.h - (int) v->rect.h) / 2;
		} else {
			align = sview_align(v->align_parent_y, view->align_y);
			switch (align) {
			case ALIGN_TOP:
				v->position.y = v->padding.top;
				break;
			case ALIGN_BOTTOM:
				v->position.y = (int) view->rect.h - (int) (v->rect.h + v->padding.bottom);
				break;
			case ALIGN_CENTER:
				v->position.y = ((int) view->rect.h - (int) v->rect.h) / 2;
				break;
			default:
				break;
			}
			pr_debug("align: %d\n", (int)align);
		}
		v->position.y += start->y;

		v->base = view->base;
		sview_draw(v);
	}

}

void frame_layout_measure_size(struct sview *view) {
	struct sview *v;
	struct list_head *pos, *n;

	unsigned int width = 0;
	unsigned int height = 0;
	unsigned int tmp;
	unsigned char align;

	list_for_each_safe(pos, n, &view->grp) {
		v = list_entry(pos, struct sview, link);
		if (!sview_can_be_show(v))
			continue;

		sview_measure_size(v);

		align = sview_align(v->align_parent_x, view->align_x);
		if (align == ALIGN_CENTER || v->center_horizontal) {
			if (width < v->rect.w)
				width = v->rect.w;
		} else {
			tmp = v->rect.w + v->padding.left + v->padding.right;
			if (width < tmp)
				width = tmp;
		}

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

	view->raw_rect.w = width;
	view->raw_rect.h = height;
}

int frame_layout_sync(struct sview *view) {
	return sview_grp_sync(&view->grp);
}

void frame_layout_free(struct sview *view) {
	struct frame_layout *layout = to_frame_layout(view);

	sview_grp_free(&view->grp);

	if (view->is_alloc)
		free(layout);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_frame_layout(struct sview *view, struct slpt_app_res *parent) {
	struct slpt_app_res *res;

	res = slpt_register_sview_base(view, parent, NULL, 0);
	if (!res)
		return NULL;

	if (!slpt_register_sview_grp(&view->grp, res)) {
		slpt_kernel_unregister_app_res(res, uboot_slpt_task);
		res = NULL;
	}

	return res;
}
#endif

void init_frame_layout(struct frame_layout *layout, const char *name) {
	sview_init_base(&layout->view, name, SVIEW_FRAME_LAYOUT);
	to_sview(layout)->layout_align_x = 1;
	to_sview(layout)->layout_align_y = 1;
}

struct sview *alloc_frame_layout(const char *name) {
	struct frame_layout *layout;
	char *cpy_name;

	layout = malloc_with_name(sizeof(*layout), name);
	if (!layout) {
		pr_err("frame_layout: failed to alloc\n");
		return NULL;
	}
	cpy_name = (char *) &layout[1];

	init_frame_layout(layout, cpy_name);

	layout->view.is_alloc = 1;

	return &layout->view;
}

