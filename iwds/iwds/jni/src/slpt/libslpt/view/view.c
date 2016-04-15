#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <view.h>

void free_view(struct view *v) {
	pr_debug("view: free: [%s]\n", view_name(v));
	list_del(&v->link);
	v->freev(v);
}

void view_set_updated(struct view *v, int updated) {
#ifdef CONFIG_SLPT_LINUX
	updated = 1;
#endif
	v->updated = updated;
	if (v->parent && !view_is_updated(v->parent)) {
		view_set_updated(v->parent, 1);
	}
}

void view_display(struct view *v) {
	pr_debug("view: [%s]\t --> [ready: %s] [show: %s]  [updated: %s]\n",
		   view_name(v),
		   view_is_ready(v) ? "y" : "n",
		   view_want_to_show(v) ? "y" : "n",
		   view_is_updated(v) ? "y" : "n"
		);

	if (!view_is_ready(v))
		return ;

	if (!view_is_updated(v))
		return;

	if (!view_want_to_show(v))
		return;

	v->display(v);
	view_set_updated(v, 0);
}

void view_pre_display(struct view *v) {
	if (!v->pre_display)
		return;

	if (!view_is_ready(v))
		return ;

	if (!view_is_updated(v))
		return;

	if (!view_want_to_show(v))
		return;

	v->pre_display(v);
}

void view_sync_start(struct view *v) {
	unsigned int xmax, ymax;
	struct position pos;

	view_cal_size(v, &xmax, &ymax);
	if (v->center_hor || v->center_ver) {
		pos = v->start;
		if (v->center_hor)
			pos.x = v->bg.xres < xmax ? 0 : (v->bg.xres - xmax) / 2;
		if (v->center_ver)
			pos.y = v->bg.yres < ymax ? 0 : (v->bg.yres - ymax) / 2;
		view_set_start(v, &pos);
	}
}


void view_cal_size_empty(struct view *v, unsigned int *xmax, unsigned int *ymax) {
	*xmax = 0;
	*ymax = 0;
}

void view_init_status(struct view *v, const char *name, unsigned int type) {
	assert(name != NULL);
	assert(!(type >= VIEW_NUMS));
	memset(v, 0, sizeof(*v));

	INIT_LIST_HEAD(&v->grp);
	v->name = name;
	v->type = type;
	v->show = 1;
	v->follow_mode = 1;
	v->alpha_mode = 0;
	v->bg = *get_current_fb_region();
	v->updated = 1;

	v->cal_size = view_cal_size_empty;
}

#ifdef CONFIG_SLPT
static struct slpt_app_res view_res[] = {
	SLPT_RES_EMPTY_DEF("show", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("start-x", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("start-y", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("follow-mode", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("center-hor", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("center-ver", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("replace-mode", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("replace-color", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("level", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("alpha_mode", SLPT_RES_INT),
};

struct slpt_app_res *slpt_register_view_base(struct view *view,
											 struct slpt_app_res *parent,
											 struct slpt_app_res *array,
											 unsigned int size) {
	struct slpt_app_res *res;
	struct slpt_app_res tmp = SLPT_RES_DIR_DEF(view->name, view_res);

	slpt_set_res(view_res[0], &view->show, 4);
	slpt_set_res(view_res[1], &view->start.x, 4);
	slpt_set_res(view_res[2], &view->start.y, 4);
	slpt_set_res(view_res[3], &view->follow_mode, 4);
	slpt_set_res(view_res[4], &view->center_hor, 4);
	slpt_set_res(view_res[5], &view->center_ver, 4);
	slpt_set_res(view_res[6], &view->replace_mode, 4);
	slpt_set_res(view_res[7], &view->replace_color, 4);
	slpt_set_res(view_res[8], &view->level, 4);
	slpt_set_res(view_res[9], &view->alpha_mode, 4);

	res = slpt_kernel_register_app_dir_res(&tmp, parent);
	if (!res) {
		pr_err("view: failed to regiseter view to slpt: %s\n", view->name);
		return NULL;
	}

	if (array && size) {
		if (!slpt_kernel_register_app_child_res_list(array, size, res)) {
			pr_err("view: failed to register additional res list\n");
			slpt_kernel_unregister_app_res(res, uboot_slpt_task);
			res = NULL;
		}
	}

	return res;
}

struct slpt_app_res *slpt_register_view(struct view *view,
										struct slpt_app_res *parent,
										struct slpt_app_res *array,
										unsigned int size) {
	struct slpt_app_res *res;

	res = view->register_slpt(view, parent);
	if (!res) {
		pr_err("view: failed to register view to slpt: %s\n", view->name);
		return NULL;
	}


	if (array && size) {
		if (!slpt_kernel_register_app_child_res_list(array, size, res)) {
			pr_err("view: failed to register additional res list\n");
			slpt_kernel_unregister_app_res(res, uboot_slpt_task);
			res = NULL;
		}
	}

	view->res = res;
	return res;
}

void slpt_unregister_view(struct view *view) {
	slpt_kernel_unregister_app_res(view->res, uboot_slpt_task);
}
#endif
