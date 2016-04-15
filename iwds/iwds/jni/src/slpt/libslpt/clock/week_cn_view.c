#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <view.h>
#include <week_en_cn_view.h>

/* week */
static struct viewdesc week_desc[] = {
	{"week", "week_nums", VIEW_NUM},
};

static struct viewdesc week_name_desc[] = {
	{"week_name", "clock/week_name", VIEW_PIC},
};

/* week */
static inline struct view *alloc_week(void) {
	return viewdesc_to_num_view(&week_desc[0]);
}

static inline struct view *alloc_week_name(void) {
	return viewdesc_to_pic_view(&week_name_desc[0]);
}

static void week_cn_view_set_week(struct week_cn_view *weekv, struct rtc_time *tm) {
	num_view_set_num(weekv->array[1], tm->tm_wday);
}

static void week_cn_view_time_callback(struct time_notify *no, struct rtc_time *tm) {
	struct week_cn_view *weekv = container_of(no, struct week_cn_view, no);
	week_cn_view_set_week(weekv, tm);
}

static void week_cn_view_free(struct view *view) {
	struct week_cn_view *weekv = to_week_cn_view(view);
	unsigned int is_alloc = view_is_alloc(view);
	unregister_time_notify(&weekv->no);

	view->is_alloc = 0;
	weekv->parent_freev(view);

	if (is_alloc)
		free(weekv);
}

int init_week_cn_view(struct week_cn_view *weekv, const char *name) {
	memset(weekv, 0, sizeof(*weekv));

	weekv->array[0] = alloc_week_name();
	if (!weekv->array[0]) {
		pr_err("weekv: failed to alloc week name\n");
		goto return_err;
	}

	weekv->array[1] = alloc_week();
	if (!weekv->array[1]) {
		pr_err("weekv: failed to alloc week\n");
		goto free_week_name;
	}

	init_text_view(&weekv->text, name, weekv->array, ARRAY_SIZE(weekv->array));

	view_of_week_cn_view(weekv)->is_alloc = 0;
	view_of_week_cn_view(weekv)->type = VIEW_WEEK_CN;

	weekv->parent_freev = view_of_week_cn_view(weekv)->freev;
	view_of_week_cn_view(weekv)->freev = week_cn_view_free;

	weekv->no.callback = week_cn_view_time_callback;
	register_time_notify(&weekv->no, TIME_TICK_DAY);

	return 0;

free_week_name:
	free_view(weekv->array[0]);
return_err:
	return -EINVAL;
}

struct view *alloc_week_cn_view(const char *name) {
	struct week_cn_view *weekv;
	char *cpy_name;

	weekv = malloc_with_name(sizeof(*weekv), name);
	if (!weekv) {
		pr_err("week_en_view: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&weekv[1];

	if (init_week_cn_view(weekv, cpy_name)) {
		pr_err("week_en_view: failed to init\n");
		goto free_view;
	}

	view_of_week_cn_view(weekv)->is_alloc = 1;

	return view_of_week_cn_view(weekv);
free_view:
	free(weekv);
	return NULL;
}
