#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <view.h>
#include <time_view.h>

#ifdef CONFIG_SLPT
#include <slpt.h>
#endif

/* time */
static struct viewdesc hour_desc[] = {
	{"hourh", "large_nums", VIEW_NUM},
	{"hourl", "large_nums", VIEW_NUM},
};

static struct viewdesc min_desc[] = {
	{"minh",  "large_nums", VIEW_NUM},
	{"minl",  "large_nums", VIEW_NUM},
};

/* time sep */
static struct viewdesc time_sep_desc[] = {
	{"time_sep", "clock/time_sep", VIEW_FLASH_PIC},
};

static struct view *alloc_hour(struct view *views[]) {
	return alloc_views_to_text("hour", views, hour_desc, ARRAY_SIZE(hour_desc));
}

static struct view *alloc_min(struct view *views[]) {
	return alloc_views_to_text("min", views, min_desc, ARRAY_SIZE(min_desc));
}

/* timesep */
static struct view *alloc_timesep(void) {
	return viewdesc_to_flash_pic_view(&time_sep_desc[0]);
}

static void time_view_set_time(struct time_view *timev, unsigned int hour, unsigned int min, unsigned int sec) {
	num_view_set_num(timev->array[0], hour / 10);
	num_view_set_num(timev->array[1], hour % 10);
	if (flash_pic_view_is_flash(timev->array[2]))
		flash_pic_view_set_display(timev->array[2], sec % 2);
	num_view_set_num(timev->array[3], min / 10);
	num_view_set_num(timev->array[4], min % 10);
}

static void time_view_time_callback(struct time_notify *no, struct rtc_time *tm) {
	struct time_view *timev = container_of(no, struct time_view, no);
	time_view_set_time(timev, tm->tm_hour, tm->tm_min, tm->tm_sec);
}

static void time_view_free(struct view *view) {
	struct time_view *timev = to_time_view(view);
	unsigned int is_alloc = view_is_alloc(view);
	unregister_time_notify(&timev->no);

	view->is_alloc = 0;
	timev->parent_freev(view);

	if (is_alloc)
		free(timev);
}

int init_time_view(struct time_view *timev, const char *name) {
	memset(timev, 0, sizeof(*timev));

	timev->array2[0] = alloc_hour(&timev->array[0]);
	if (!timev->array2[0]) {
		pr_err("timev: failed to alloc hor\n");
		goto return_err;
	}

	timev->array2[1] = timev->array[2] = alloc_timesep();
	if (!timev->array2[1]) {
		pr_err("timev: failed to alloc timesep\n");
		goto free_hour;
	}

	timev->array2[2] = alloc_min(&timev->array[3]);
	if (!timev->array2[2]) {
		pr_err("timev: failed to alloc min\n");
		goto free_timesep;
	}

	init_text_view(&timev->text, name, timev->array2, ARRAY_SIZE(timev->array2));

	view_of_time_view(timev)->is_alloc = 0;
	view_of_time_view(timev)->type = VIEW_TIME;

	timev->parent_freev = view_of_time_view(timev)->freev;
	view_of_time_view(timev)->freev = time_view_free;

	timev->no.callback = time_view_time_callback;
	register_time_notify(&timev->no, TIME_TICK_SEC);

	return 0;

free_timesep:
	free_view(timev->array2[1]);
free_hour:
	free_view(timev->array2[0]);
return_err:
	return -EINVAL;
}

struct view *alloc_time_view(const char *name) {
	struct time_view *timev;
	char *cpy_name;

	timev = malloc_with_name(sizeof(*timev), name);
	if (!timev) {
		pr_err("time_view: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&timev[1];

	if (init_time_view(timev, cpy_name)) {
		pr_err("date_cn_view: failed to init\n");
		goto free_view;
	}

	view_of_time_view(timev)->is_alloc = 1;

	return view_of_time_view(timev);
free_view:
	free(timev);
	return NULL;
}
