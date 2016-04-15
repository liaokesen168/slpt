#include <common.h>
#include <view.h>
#include <list.h>

/* view grp */
void view_grp_display(struct list_head *grp, struct position **start) {
	struct list_head *pos;
	struct view *v;

	list_for_each(pos, grp) {
		v = list_entry(pos, struct view, link);
		view_display(v);
	}
}

void view_grp_display_continuously(struct list_head *grp, struct position **start) {
	struct list_head *pos;
	struct view *v;

	list_for_each(pos, grp) {
		v = list_entry(pos, struct view, link);
		if (view_is_follow(v)) {
			if (view_want_to_show(v)) {
				view_set_start(v, *start);
				view_display(v);
				*start = view_end(v);
			}
		} else {
			view_display(v);
		}
	}
}

int view_grp_sync(struct list_head *grp) {
	struct list_head *pos;
	struct view *v;
	int errs = 0;

	list_for_each(pos, grp) {
		v = list_entry(pos, struct view, link);
		if (view_sync_setting(v) && view_want_to_show(v)) {
			pr_err("%s: sync err\n", v->name);
			errs += 1;
		}
	}
	return errs;
}

int view_grp_sync_strictly(struct list_head *grp) {
	struct list_head *pos;
	struct view *v;
	int errs = 0;

	list_for_each(pos, grp) {
		v = list_entry(pos, struct view, link);
		if (view_sync_setting(v)) {
			pr_err("%s: sync err\n", v->name);
			errs += 1;
		}
	}
	return errs;
}

void view_grp_set_bg(struct list_head *grp, struct fb_region *bg) {
	struct list_head *pos;
	struct view *v;

	list_for_each(pos, grp) {
		v = list_entry(pos, struct view, link);
		view_set_bg(v, bg);
	}
}

void view_grp_set_updated(struct list_head *grp, int updated) {
	struct list_head *pos;
	struct view *v;

	list_for_each(pos, grp) {
		v = list_entry(pos, struct view, link);
		view_set_updated(v, updated);
		view_grp_set_updated(&v->grp, updated);
	}
}

void view_grp_cal_size(struct list_head *grp, unsigned int *xmax, unsigned int *ymax) {
	struct list_head *pos, *n;
	struct view *v;

	list_for_each_safe(pos, n, grp) {
		v = list_entry(pos, struct view, link);
		view_sync_start(v);
	}

	*xmax = 0;
	*ymax = 0;
}

void view_grp_cal_size_continuously(struct list_head *grp, unsigned int *xmax, unsigned int *ymax) {
	struct list_head *pos, *n;
	unsigned int x, y;
	struct view *v;

	*xmax = 0;
	*ymax = 0;

	list_for_each_safe(pos, n, grp) {
		v = list_entry(pos, struct view, link);
		if (view_is_follow(v)) {
			if (view_want_to_show(v)) {
				view_cal_size(v, &x, &y);
					*xmax += x;
				if (y > *ymax)
					*ymax = y;
			}
		} else {
			view_sync_start(v);
		}
	}
}

void view_grp_free(struct list_head *grp) {
	struct list_head *pos, *n;
	struct view *v;

	list_for_each_safe(pos, n, grp) {
		v = list_entry(pos, struct view, link);
		free_view(v);
	}
}

#ifdef CONFIG_SLPT_LINUX
extern void slpt_load_view(struct view *view);
extern void slpt_write_view(struct view *view);
extern void slpt_print_view(struct view *view, unsigned int enter_level);

void slpt_load_view_grp(struct list_head *grp) {
	struct list_head *pos;
	struct view *v;

	list_for_each(pos, grp) {
		v = list_entry(pos, struct view, link);
		slpt_load_view(v);
	}
}

void slpt_write_view_grp(struct list_head *grp) {
	struct list_head *pos;
	struct view *v;

	list_for_each(pos, grp) {
		v = list_entry(pos, struct view, link);
		slpt_write_view(v);
	}
}

void slpt_print_view_grp(struct list_head *grp, unsigned int enter_level) {
	struct list_head *pos;
	struct view *v;

	list_for_each(pos, grp) {
		v = list_entry(pos, struct view, link);
		slpt_print_view(v, enter_level + 1);
	}
}

#endif

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_view_grp(struct list_head *grp, struct slpt_app_res *parent) {
	struct list_head *pos, *tmp;
	struct view *v;

	list_for_each(pos, grp) {
		v = list_entry(pos, struct view, link);
		if (!slpt_register_view(v, parent, NULL, 0)) {
			tmp = pos;
			pr_err("view grp: failed to register view to slpt: %s\n", v->name);
			goto unregister_others;
		}
	}

	return parent;
unregister_others:
	list_for_each(pos, grp) {
		if (pos == tmp)
			break;
		v = list_entry(pos, struct view, link);
		slpt_unregister_view(v);
	}
	return NULL;
}
#endif
