#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <view.h>
#include <week_en_cn_view.h>

#ifdef CONFIG_SLPT
#include <slpt.h>
#endif

static void week_en_view_set_week(struct week_en_view *weekv, struct rtc_time *tm) {
	num_view_set_num(&weekv->numv.view, tm->tm_wday);
}

static void week_en_view_time_callback(struct time_notify *no, struct rtc_time *tm) {
	struct week_en_view *weekv = container_of(no, struct week_en_view, no);
	week_en_view_set_week(weekv, tm);
}

static void week_en_view_free(struct view *view) {
	struct week_en_view *weekv = to_week_en_view(view);
	unsigned int is_alloc = view_is_alloc(view);
	unregister_time_notify(&weekv->no);

	view->is_alloc = 0;
	weekv->parent_freev(view);

	if (is_alloc)
		free(weekv);
}

int init_week_en_view(struct week_en_view *weekv, const char *name) {

	init_num_view(&weekv->numv, name, "week_nums");

	view_of_week_en_view(weekv)->is_alloc = 0;
	view_of_week_en_view(weekv)->type = VIEW_WEEK_EN;

	weekv->parent_freev = view_of_week_en_view(weekv)->freev;
	view_of_week_en_view(weekv)->freev = week_en_view_free;

	weekv->no.callback = week_en_view_time_callback;
	register_time_notify(&weekv->no, TIME_TICK_DAY);

	return 0;
}

struct view *alloc_week_en_view(const char *name) {
	struct week_en_view *weekv;
	char *cpy_name;

	weekv = malloc_with_name(sizeof(*weekv), name);
	if (!weekv) {
		pr_err("week_en_view: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&weekv[1];

	if (init_week_en_view(weekv, cpy_name)) {
		pr_err("week_en_view: failed to init\n");
		goto free_view;
	}

	view_of_week_en_view(weekv)->is_alloc = 1;

	return view_of_week_en_view(weekv);
free_view:
	free(weekv);
	return NULL;
}
