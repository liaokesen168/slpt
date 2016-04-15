#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <view.h>
#include <date_en_cn_view.h>

#ifdef CONFIG_SLPT
#include <slpt.h>
#endif

/* date */
static struct viewdesc mon_desc[] = {
	{"monh", "small_nums", VIEW_NUM},
	{"monl", "small_nums", VIEW_NUM},
};

static struct viewdesc day_desc[] = {
	{"dayh",  "small_nums", VIEW_NUM},
	{"dayl",  "small_nums", VIEW_NUM},
};

/* names: month, day */
static struct viewdesc mon_name_desc[] = {
	{"mon_name", "clock/mon_name", VIEW_PIC},
};

static struct viewdesc day_name_desc[] = {
	{"day_name", "clock/day_name", VIEW_PIC},
};

static inline struct view *alloc_mon(struct view *views[]) {
	return alloc_views_to_text("mon", views, mon_desc, ARRAY_SIZE(mon_desc));
}

static inline struct view *alloc_day(struct view *views[]) {
	return alloc_views_to_text("day", views, day_desc, ARRAY_SIZE(day_desc));
}

/* names: month, day */
static inline struct view *alloc_mon_name(void) {
	return viewdesc_to_pic_view(&mon_name_desc[0]);
}

static inline struct view *alloc_day_name(void) {
	return viewdesc_to_pic_view(&day_name_desc[0]);
}

static void date_cn_view_set_date(struct date_cn_view *datev, unsigned int mon, unsigned int day) {
	num_view_set_num(datev->array[0], mon / 10);
	num_view_set_num(datev->array[1], mon % 10);
	num_view_set_num(datev->array[3], day / 10);
	num_view_set_num(datev->array[4], day % 10);
}

static void date_cn_view_time_callback(struct time_notify *no, struct rtc_time *tm) {
	struct date_cn_view *datev = container_of(no, struct date_cn_view, no);
	date_cn_view_set_date(datev, tm->tm_mon, tm->tm_mday);
}

static void date_cn_view_free(struct view *view) {
	struct date_cn_view *datev = to_date_cn_view(view);
	unsigned int is_alloc = view_is_alloc(view);
	unregister_time_notify(&datev->no);

	view->is_alloc = 0;
	datev->parent_freev(view);

	if (is_alloc)
		free(datev);
}

int init_date_cn_view(struct date_cn_view *datev, const char *name) {

	datev->array2[0] = alloc_mon(&datev->array[0]);
	if (!datev->array2[0]) {
		pr_err("datev: failed to alloc mon\n");
		goto return_err;
	}

	datev->array2[1] = datev->array[2] = alloc_mon_name();
	if (!datev->array2[1]) {
		pr_err("datev: failed to alloc month name\n");
		goto free_mon;
	}

	datev->array2[2] = alloc_day(&datev->array[3]);
	if (!datev->array2[2]) {
		pr_err("datev: failed to alloc day\n");
		goto free_mon_name;
	}

	datev->array2[3] = datev->array[5] = alloc_day_name();
	if (!datev->array2[3]) {
		pr_err("datev: failed to alloc day name\n");
		goto free_day;
	}

	init_text_view(&datev->text, name, datev->array2, ARRAY_SIZE(datev->array2));

	view_of_date_cn_view(datev)->is_alloc = 0;
	view_of_date_cn_view(datev)->type = VIEW_DATE_CN;

	datev->parent_freev = view_of_date_cn_view(datev)->freev;
	view_of_date_cn_view(datev)->freev = date_cn_view_free;

	datev->no.callback = date_cn_view_time_callback;
	register_time_notify(&datev->no, TIME_TICK_DAY);

	return 0;

free_day:
	free_view(datev->array2[2]);
free_mon_name:
	free_view(datev->array2[1]);
free_mon:
	free_view(datev->array2[0]);
return_err:
	return -EINVAL;
}

struct view *alloc_date_cn_view(const char *name) {
	struct date_cn_view *datev;
	char *cpy_name;

	datev = malloc_with_name(sizeof(*datev), name);
	if (!datev) {
		pr_err("date_cn_view: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&datev[1];

	if (init_date_cn_view(datev, cpy_name)) {
		pr_err("date_cn_view: failed to init\n");
		goto free_view;
	}

	view_of_date_cn_view(datev)->is_alloc = 1;

	return view_of_date_cn_view(datev);
free_view:
	free(datev);
	return NULL;
}
