#include <common.h>
#include <sview/sview_grp.h>
#include <sview/sview.h>

unsigned int sview_grp_size(struct list_head *grp) {
	struct list_head *pos;
	unsigned int size = 0;

	list_for_each(pos, grp) {
		size++;
	}

	return size;
}

void sview_grp_add(struct list_head *grp, struct sview *view) {
	list_add_tail(&view->link, grp);
}

void sview_grp_add_array(struct list_head *grp, struct sview **array, unsigned int size) {
	unsigned int i;

	if (!array || !size)
		return;

	for (i = 0; i < size; ++i) {
		sview_grp_add(grp, array[i]);
	}
}

void sview_grp_add_by_level(struct list_head *grp, struct sview *view) {
	struct list_head *pos;

	list_for_each(pos, grp) {
		struct sview *v = list_entry(pos, struct sview, link);
		pr_debug("scan: [%s]\n", view->name);
		if (view->level < v->level)
			break;
	}
	list_add_tail(&view->link, pos);
}

void sview_grp_add_array_by_level(struct list_head *grp, struct sview **array, unsigned int size) {
	unsigned int i;

	if (!array || !size)
		return;

	for (i = 0; i < size; ++i) {
		sview_grp_add_by_level(grp, array[i]);
	}
}

void sview_grp_sort(struct list_head *grp) {
	struct list_head *pos, *n;
	struct list_head list = LIST_HEAD_INIT(list);

	list_cut_position(&list, grp, grp->prev);
	list_for_each_safe(pos, n, &list) {
		struct sview *v = list_entry(pos, struct sview, link);
		list_del(pos);
		sview_grp_add_by_level(grp, v);
	}
}

int sview_grp_sync(struct list_head *grp) {
	struct sview *v;
	struct list_head *pos, *n;

	list_for_each_safe(pos, n, grp) {
		v = list_entry(pos, struct sview, link);
		sview_sync_setting(v);
	}

	return 0;
}

int sview_grp_sync_strictly(struct list_head *grp) {
	struct sview *v;
	struct list_head *pos, *n;
	int sync = 0;

	list_for_each_safe(pos, n, grp) {
		v = list_entry(pos, struct sview, link);
		if (sview_sync_setting(v) && v->show)
			sync += 1;
	}

	return sync;
}

void sview_grp_free(struct list_head *grp) {
	struct sview *v;
	struct list_head *pos, *n;

	list_for_each_safe(pos, n, grp) {
		v = list_entry(pos, struct sview, link);
		sview_free(v);
	}
}

struct sview *sview_grp_find(struct list_head *grp, const char *child_name) {
	struct sview *v;
	struct list_head *pos;

	list_for_each(pos, grp) {
		v = list_entry(pos, struct sview, link);
		if (!strcmp(child_name, v->name))
			return v;
	}

	return NULL;
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_sview_grp(struct list_head *grp, struct slpt_app_res *parent) {
	struct list_head *pos, *tmp;
	struct sview *v;

	list_for_each(pos, grp) {
		v = list_entry(pos, struct sview, link);
		if (!slpt_register_sview(v, parent, NULL, 0)) {
			tmp = pos;
			pr_err("view grp: failed to register sview to slpt: %s\n", v->name);
			goto unregister_others;
		}
	}

	return parent;
unregister_others:
	list_for_each(pos, grp) {
		if (pos == tmp)
			break;
		v = list_entry(pos, struct sview, link);
		slpt_unregister_sview(v);
	}
	return NULL;
}
#endif
