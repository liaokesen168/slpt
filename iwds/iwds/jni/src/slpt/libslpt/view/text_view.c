#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <view.h>

/* text view */

static inline void text_view_add(struct view *view, struct view *child) {
	list_add_tail(&child->link, &view->grp);
	child->parent = view;
}

void text_view_add_array(struct view *view, struct view **array, unsigned int size) {
	unsigned int i;

	for (i = 0; i < size; ++i) {
		text_view_add(view, array[i]);
	}
}

void text_view_display(struct view *view) {
	struct position *start = view_start(view);

	if (view_is_follow(view))
		view_grp_display_continuously(&view->grp, &start);
	else
		view_grp_display(&view->grp, &start);
	view_set_end(view, start);
}

void text_view_cal_size(struct view *view, unsigned int *xmax, unsigned int *ymax) {
	if (view_is_follow(view))
		view_grp_cal_size_continuously(&view->grp, xmax, ymax);
	else
		view_grp_cal_size(&view->grp, xmax, ymax);
}

void text_view_set_bg(struct view *view, struct fb_region *bg) {
	view_grp_set_bg(&view->grp, bg);
}

int text_view_sync(struct view *view) {
	text_view_set_bg(view, view_bg(view));
	return view_grp_sync(&view->grp);
}

void text_view_free(struct view *view) {
	struct text_view *text = to_text_view(view);

	view_grp_free(&view->grp);
	if (view_is_alloc(view))
		free(text);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_text_view(struct view *view, struct slpt_app_res *parent) {
	struct slpt_app_res *res;

	res = slpt_register_view_base(view, parent, NULL, 0);
	if (!res)
		return NULL;

	if (!slpt_register_view_grp(&view->grp, res)) {
		slpt_unregister_view(view);
		res = NULL;
	}

	return res;
}
#endif

int init_text_view(struct text_view *text, const char *name, struct view **array, unsigned int size) {
	view_init_status(&text->view, name, VIEW_TEXT);

	text_view_add_array(&text->view, array, size);

	text->view.display = text_view_display;
	text->view.set_bg = text_view_set_bg;
	text->view.sync = text_view_sync;
	text->view.freev = text_view_free;
	text->view.cal_size = text_view_cal_size;

#ifdef CONFIG_SLPT
	text->view.register_slpt = slpt_register_text_view;
#endif
	return 0;
}

struct view *alloc_text_view(const char *name, struct view **array, unsigned int size) {
	struct text_view *text;
	char *cpy_name;

	text = malloc_with_name(sizeof(*text), name);
	if (!text) {
		pr_err("text_view: failed to alloc\n");
		return NULL;
	}
	cpy_name = (char *) &text[1];

	if (init_text_view(text, cpy_name, array, size))
		goto free_text_view;

	text->view.is_alloc = 1;

	return &text->view;
free_text_view:
	free(text);
	return NULL;
}
