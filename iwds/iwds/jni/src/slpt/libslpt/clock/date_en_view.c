#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <view.h>
#include <date_en_cn_view.h>

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

/* date */
static struct viewdesc mon_desc[] = {
	{"monh", "small_nums", VIEW_NUM},
	{"monl", "small_nums", VIEW_NUM},
};

static struct viewdesc day_desc[] = {
	{"dayh",  "small_nums", VIEW_NUM},
	{"dayl",  "small_nums", VIEW_NUM},
};

/* date sep (en mode) */
static struct viewdesc date_sep_desc1[] = {
	{"date_sep1", "clock/date_sep", VIEW_PIC},
};

/* date sep (en mode) */
static struct viewdesc date_sep_desc2[] = {
	{"date_sep2", "clock/date_sep", VIEW_PIC},
};

static struct view *alloc_year(struct view *views[]) {
	return alloc_views_to_text("year", views, year_desc, ARRAY_SIZE(year_desc));
}

static struct view *alloc_mon(struct view *views[]) {
	return alloc_views_to_text("mon", views, mon_desc, ARRAY_SIZE(mon_desc));
}

static struct view *alloc_day(struct view *views[]) {
	return alloc_views_to_text("day", views, day_desc, ARRAY_SIZE(day_desc));
}

/* datesep (en mode) */
static struct view *alloc_datesep1(void) {
	return viewdesc_to_pic_view(&date_sep_desc1[0]);
}

/* datesep (en mode) */
static struct view *alloc_datesep2(void) {
	return viewdesc_to_pic_view(&date_sep_desc2[0]);
}

static void date_en_view_set_date(struct date_en_view *datev, unsigned int mon, unsigned int day, unsigned int year) {

	num_view_set_num(datev->array[0], year / 1000);
	num_view_set_num(datev->array[1], (year % 1000) / 100);
	num_view_set_num(datev->array[2], (year % 100) / 10);
	num_view_set_num(datev->array[3], year % 10);

	num_view_set_num(datev->array[5], mon / 10);
	num_view_set_num(datev->array[6], mon % 10);
	num_view_set_num(datev->array[8], day / 10);
	num_view_set_num(datev->array[9], day % 10);
}

static void date_en_view_time_callback(struct time_notify *no, struct rtc_time *tm) {
	struct date_en_view *datev = container_of(no, struct date_en_view, no);
	date_en_view_set_date(datev, tm->tm_mon, tm->tm_mday, tm->tm_year);
}

static void date_en_view_free(struct view *view) {
	struct date_en_view *datev = to_date_en_view(view);
	unsigned int is_alloc = view_is_alloc(view);
	unregister_time_notify(&datev->no);

	view->is_alloc = 0;
	datev->parent_freev(view);

	if (is_alloc)
		free(datev);
}

int init_date_en_view(struct date_en_view *datev, const char *name) {

	datev->array2[0] = alloc_year(&datev->array[0]);
	if (!datev->array2[0]) {
		pr_err("datev: failed to alloc mon\n");
		goto return_err;
	}

	datev->array2[1] = datev->array2[2] = alloc_datesep1();
	if (!datev->array2[1]) {
		pr_err("datev: failed to alloc datesep\n");
		goto free_year;
	}

	datev->array2[2] = alloc_mon(&datev->array[5]);
	if (!datev->array2[2]) {
		pr_err("datev: failed to alloc mon\n");
		goto free_datesep1;
	}

	datev->array2[3] = datev->array2[4] = alloc_datesep2();
	if (!datev->array2[3]) {
		pr_err("datev: failed to alloc datesep\n");
		goto free_mon;
	}

	datev->array2[4] = alloc_day(&datev->array[8]);
	if (!datev->array2[4]) {
		pr_err("datev: failed to alloc day\n");
		goto free_datesep2;
	}

	init_text_view(&datev->text, name, datev->array2, ARRAY_SIZE(datev->array2));

	view_set_show(datev->array2[0], 0); /* default "year" not show */
	view_set_show(datev->array2[1], 0); /* default "date-sep1" not show */

	view_of_date_en_view(datev)->is_alloc = 0;
	view_of_date_en_view(datev)->type = VIEW_DATE_EN;

	datev->parent_freev = view_of_date_en_view(datev)->freev;
	view_of_date_en_view(datev)->freev = date_en_view_free;

	datev->no.callback = date_en_view_time_callback;
	register_time_notify(&datev->no, TIME_TICK_DAY);

	return 0;

free_datesep2:
	free_view(datev->array2[3]);
free_mon:
	free_view(datev->array2[2]);
free_datesep1:
	free_view(datev->array2[1]);
free_year:
	free_view(datev->array2[0]);
return_err:
	return -EINVAL;
}

struct view *alloc_date_en_view(const char *name) {
	struct date_en_view *datev;
	char *cpy_name;

	datev = malloc_with_name(sizeof(*datev), name);
	if (!datev) {
		pr_err("date_en_view: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&datev[1];

	if (init_date_en_view(datev, cpy_name)) {
		pr_err("date_en_view: failed to init\n");
		goto free_view;
	}

	view_of_date_en_view(datev)->is_alloc = 1;

	return view_of_date_en_view(datev);
free_view:
	free(datev);
	return NULL;
}
