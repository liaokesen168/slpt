#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <view.h>
#include <year_en_cn_view.h>

#ifdef CONFIG_SLPT
#include <slpt.h>
#endif

/* year */
static struct viewdesc year_desc[] = {
	{"yearhh", "small_nums", VIEW_NUM},
	{"yearhl", "small_nums", VIEW_NUM},
	{"yearlh", "small_nums", VIEW_NUM},
	{"yearll", "small_nums", VIEW_NUM},
};

static struct view *alloc_year(struct view *views[]) {
	return alloc_views_to_text("year", views, year_desc, ARRAY_SIZE(year_desc));
}

static void year_en_view_set_year(struct year_en_view *yearv, unsigned int year) {
	num_view_set_num(yearv->array[3], year % 10);
	num_view_set_num(yearv->array[2], (year % 100) / 10);
	num_view_set_num(yearv->array[1], (year % 1000) / 100);
	num_view_set_num(yearv->array[0], year / 1000);
}

static void year_en_view_time_callback(struct time_notify *no, struct rtc_time *tm) {
	struct year_en_view *yearv = container_of(no, struct year_en_view, no);
	year_en_view_set_year(yearv, tm->tm_year);
}

static void year_en_view_free(struct view *view) {
	struct year_en_view *yearv = to_year_en_view(view);
	unsigned int is_alloc = view_is_alloc(view);
	unregister_time_notify(&yearv->no);

	view->is_alloc = 0;
	yearv->parent_freev(view);

	if (is_alloc)
		free(yearv);
}

int init_year_en_view(struct year_en_view *yearv, const char *name) {

	yearv->array2[0] = alloc_year(yearv->array);
	if (!yearv->array2[0]) {
		pr_err("yearv: failed to alloc year\n");
		return -EINVAL;
	}

	init_text_view(&yearv->text, name, yearv->array2, ARRAY_SIZE(yearv->array2));

	view_of_year_en_view(yearv)->is_alloc = 0;
	view_of_year_en_view(yearv)->type = VIEW_YEAR_EN;

	yearv->parent_freev = view_of_year_en_view(yearv)->freev;
	view_of_year_en_view(yearv)->freev = year_en_view_free;

	yearv->no.callback = year_en_view_time_callback;
	register_time_notify(&yearv->no, TIME_TICK_MON);

	return 0;
}

struct view *alloc_year_en_view(const char *name) {
	struct year_en_view *yearv;
	char *cpy_name;

	yearv = malloc_with_name(sizeof(*yearv), name);
	if (!yearv) {
		pr_err("year_en_view: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&yearv[1];

	if (init_year_en_view(yearv, cpy_name)) {
		pr_err("year_en_view: failed to init\n");
		goto free_view;
	}

	view_of_year_en_view(yearv)->is_alloc = 1;

	return view_of_year_en_view(yearv);
free_view:
	free(yearv);
	return NULL;
}
