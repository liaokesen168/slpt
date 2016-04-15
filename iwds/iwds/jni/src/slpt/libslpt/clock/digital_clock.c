#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <view.h>
#include <digital_clock.h>

/* time */
static struct viewdesc hour_desc[] = {
	{"hourh", "large_nums", VIEW_NUM},
	{"hourl", "large_nums", VIEW_NUM},
};

static struct viewdesc min_desc[] = {
	{"minh",  "large_nums", VIEW_NUM},
	{"minl",  "large_nums", VIEW_NUM},
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

/* week */
static struct viewdesc week_desc[] = {
	{"week", "week_nums", VIEW_NUM},
};

/* time sep */
static struct viewdesc time_sep_desc[] = {
	{"time_sep", "clock/time_sep", VIEW_FLASH_PIC},
};

/* date sep (en mode) */
static struct viewdesc date_sep_desc[] = {
	{"date_sep", "clock/date_sep", VIEW_PIC},
};

/* names: month, day, week (cn mode) */
static struct viewdesc mon_name_desc[] = {
	{"mon_name", "clock/mon_name", VIEW_PIC},
};

static struct viewdesc day_name_desc[] = {
	{"day_name", "clock/day_name", VIEW_PIC},
};

static struct viewdesc week_name_desc[] = {
	{"week_name", "clock/week_name", VIEW_PIC},
};

/* mon */
static inline struct view *alloc_monh(void) {
	return viewdesc_to_num_view(&mon_desc[0]);
}

static inline struct view *alloc_monl(void) {
	return viewdesc_to_num_view(&mon_desc[1]);
}

static inline struct view *alloc_mon(struct view *views[]) {
	return alloc_views_to_text("mon", views, mon_desc, ARRAY_SIZE(mon_desc));
}

/* day */
static inline struct view *alloc_dayh(void) {
	return viewdesc_to_num_view(&day_desc[0]);
}

static inline struct view *alloc_dayl(void) {
	return viewdesc_to_num_view(&day_desc[1]);
}

static inline struct view *alloc_day(struct view *views[]) {
	return alloc_views_to_text("day", views, day_desc, ARRAY_SIZE(day_desc));
}

/* week */
static inline struct view *alloc_week(void) {
	return viewdesc_to_num_view(&week_desc[0]);
}

/* hour */
static inline struct view *alloc_hourh(void) {
	return viewdesc_to_num_view(&hour_desc[0]);
}

static inline struct view *alloc_hourl(void) {
	return viewdesc_to_num_view(&hour_desc[1]);
}

static inline struct view *alloc_hour(struct view *views[]) {
	return alloc_views_to_text("hour", views, hour_desc, ARRAY_SIZE(hour_desc));
}

/* min */
static inline struct view *alloc_minh(void) {
	return viewdesc_to_num_view(&min_desc[0]);
}

static inline struct view *alloc_minl(void) {
	return viewdesc_to_num_view(&min_desc[1]);
}

static inline struct view *alloc_min(struct view *views[]) {
	return alloc_views_to_text("min", views, min_desc, ARRAY_SIZE(min_desc));
}

/* timesep */
static inline struct view *alloc_timesep(void) {
	return viewdesc_to_flash_pic_view(&time_sep_desc[0]);
}

/* datesep (en mode) */
static inline struct view *alloc_datesep(void) {
	return viewdesc_to_pic_view(&date_sep_desc[0]);
}

/* names: month, day, week (cn mode) */
static inline struct view *alloc_mon_name(void) {
	return viewdesc_to_pic_view(&mon_name_desc[0]);
}

static inline struct view *alloc_day_name(void) {
	return viewdesc_to_pic_view(&day_name_desc[0]);
}

static inline struct view *alloc_week_name(void) {
	return viewdesc_to_pic_view(&week_name_desc[0]);
}

int init_timev(struct timev *timev) {
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

	timev->text = alloc_text_view("time", timev->array2, ARRAY_SIZE(timev->array2));
	if (!timev->text) {
		pr_err("timev: failed to alloc text view\n");
		goto free_min;
	}

	return 0;
free_min:
	free_view(timev->array2[2]);
free_timesep:
	free_view(timev->array2[1]);
free_hour:
	free_view(timev->array2[0]);
return_err:
	return -EINVAL;
}

void destory_timev(struct timev *timev) {
	free_view(timev->text);
}

int init_dateenv(struct dateenv *datev) {
	memset(datev, 0, sizeof(*datev));

	datev->array2[0] = alloc_mon(&datev->array[0]);
	if (!datev->array2[0]) {
		pr_err("datev: failed to alloc mon\n");
		goto return_err;
	}

	datev->array2[1] = datev->array[2] = alloc_datesep();
	if (!datev->array2[1]) {
		pr_err("datev: failed to alloc datesep\n");
		goto free_mon;
	}

	datev->array2[2] = alloc_day(&datev->array[3]);
	if (!datev->array2[2]) {
		pr_err("datev: failed to alloc day\n");
		goto free_datesep;
	}

	datev->text = alloc_text_view("date", datev->array2, ARRAY_SIZE(datev->array2));
	if (!datev->text) {
		pr_err("datev: failed to alloc text view\n");
		goto free_day;
	}

	return 0;
free_day:
	free_view(datev->array2[2]);
free_datesep:
	free_view(datev->array2[1]);
free_mon:
	free_view(datev->array2[0]);
return_err:
	return -EINVAL;
}

void destory_dateenv(struct dateenv *datev) {
	free_view(datev->text);
}

int init_weekenv(struct weekenv *weekv) {
	memset(weekv, 0, sizeof(*weekv));

	weekv->text = alloc_week();
	if (!weekv->text) {
		pr_err("weekv: failed to alloc week en\n");
		return -EINVAL;
	}
	return 0;
}

void destory_weekenv(struct weekenv *weekv) {
	free_view(weekv->text);
}

int init_datecnv(struct datecnv *datev) {
	memset(datev, 0, sizeof(*datev));

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

	datev->text = alloc_text_view("date", datev->array2, ARRAY_SIZE(datev->array2));
	if (!datev->text) {
		pr_err("datev: failed to alloc text view\n");
		goto free_day_name;
	}

	return 0;
free_day_name:
	free_view(datev->array2[3]);
free_day:
	free_view(datev->array2[2]);
free_mon_name:
	free_view(datev->array2[1]);
free_mon:
	free_view(datev->array2[0]);
return_err:
	return -EINVAL;
}

void destory_datecnv(struct datecnv *datev) {
	free_view(datev->text);
}

int init_weekcnv(struct weekcnv *weekv) {
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

	weekv->text = alloc_text_view("week", weekv->array, ARRAY_SIZE(weekv->array));
	if (!weekv->text) {
		pr_err("weekv: failed to alloc week cn\n");
		goto free_week;
	}

	return 0;
free_week:
	free_view(weekv->array[1]);
free_week_name:
	free_view(weekv->array[0]);
return_err:
	return -EINVAL;
}

void destory_weekcnv(struct weekcnv *weekv) {
	free_view(weekv->text);
}

static void do_display_digital_clock_en(struct digital_clock_en *clock) {
	display_timev(&clock->timev);
	display_dateenv(&clock->datev);
	display_weekenv(&clock->weekv);
}

static int do_sync_digital_clock_en(struct digital_clock_en *clock) {
	int errs = 0;

	errs += sync_timev(&clock->timev) ? 1 : 0;
	errs += sync_dateenv(&clock->datev) ? 1 : 0;
	errs += sync_weekenv(&clock->weekv) ? 1 : 0;

	return errs;
}

void display_digital_clock_en(struct digital_clock_en *clock) {
	if (!view_is_ready(&clock->view))
		return ;
	if (!view_want_to_show(&clock->view))
		return;

	digital_clock_en_set_time(clock, get_currnet_tm());
	do_display_digital_clock_en(clock);
}

int sync_digital_clock_en(struct digital_clock_en *clock) {
	return view_sync_setting(&clock->view);
}

void digital_clock_en_view_display(struct view *view) {
	struct position *start = view_start(view);
	struct digital_clock_en *clock = to_digital_clock_en(view);

	do_display_digital_clock_en(clock);
	view_set_end(view, start);
}

void digital_clock_en_cal_size(struct view *view, unsigned int *xmax, unsigned int *ymax) {
	view_grp_cal_size(&view->grp, xmax, ymax);
}

void digital_clock_en_view_set_bg(struct view *view, struct fb_region *bg) {
	view_grp_set_bg(&view->grp, bg);
}

int digital_clock_en_view_sync(struct view *view) {
	struct digital_clock_en *clock = to_digital_clock_en(view);

	digital_clock_en_view_set_bg(view, view_bg(view));
	return do_sync_digital_clock_en(clock);
}

void digital_clock_en_view_free(struct view *view) {
	struct digital_clock_en *clock = to_digital_clock_en(view);

	destory_digital_clock_en(clock);
	if (view_is_alloc(view))
		free(clock);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_digital_clock_en_view(struct view *view, struct slpt_app_res *parent) {
	struct slpt_app_res *res;
	struct digital_clock_en *clock = to_digital_clock_en(view);

	res = slpt_register_view_base(view, parent, NULL, 0);
	if (!res)
		return NULL;

	if (!slpt_register_timev(&clock->timev, res)) {
		pr_err("digital clock: failed to register time to slpt\n");
		goto unregister_dir;
	}
	if (!slpt_register_dateenv(&clock->datev, res)) {
		pr_err("digital clock: failed to register date to slpt\n");
		goto unregister_dir;
	}
	if (!slpt_register_weekenv(&clock->weekv, res)) {
		pr_err("digital clock: failed to register week to slpt\n");
		goto unregister_dir;
	}

	return res;
unregister_dir:
	slpt_kernel_unregister_app_res(res, uboot_slpt_task);
	return NULL;
}
#endif

static void digital_clock_en_time_callback(struct time_notify *no, struct rtc_time *tm) {
	struct digital_clock_en *clock = container_of(no, struct digital_clock_en, no);

	digital_clock_en_set_time(clock, tm);
}

int init_digital_clock_en(struct digital_clock_en *clock, const char *name) {
	int ret;

	clock->view.is_alloc = 0;
	view_init_status(&clock->view, name, VIEW_DIGITAL_CLOCK_EN);

	ret = init_timev(&clock->timev);
	if (ret) {
		pr_err("digital clock: failed to init timev\n");
		return ret;
	}

	ret = init_dateenv(&clock->datev);
	if (ret) {
		pr_err("digital clock: failed to init timev\n");
		goto destory_timev;
	}

	ret = init_weekenv(&clock->weekv);
	if (ret) {
		pr_err("digital clock: failed to init timev\n");
		goto destory_datev;
	}

	view_add_child(&clock->view, clock->timev.text);
	view_add_child(&clock->view, clock->datev.text);
	view_add_child(&clock->view, clock->weekv.text);

	clock->view.display = digital_clock_en_view_display;
	clock->view.set_bg = digital_clock_en_view_set_bg;
	clock->view.sync = digital_clock_en_view_sync;
	clock->view.freev = digital_clock_en_view_free;
	clock->view.cal_size = digital_clock_en_cal_size;

#ifdef CONFIG_SLPT
	clock->view.register_slpt = slpt_register_digital_clock_en_view;
#endif

	clock->no.callback = digital_clock_en_time_callback;
	register_time_notify(&clock->no, TIME_TICK_SEC);

	return 0;
destory_datev:
	destory_dateenv(&clock->datev);
destory_timev:
	destory_timev(&clock->timev);
	return ret;
}

struct view *alloc_digital_clock_en(const char *name) {
	struct digital_clock_en *clock;
	char *cpy_name;

	clock = malloc_with_name(sizeof(*clock), name);
	if (!clock) {
		pr_err("digital clock: failed to alloc\n");
		return NULL;
	}
	cpy_name = (char *)&clock[1];

	if (init_digital_clock_en(clock, cpy_name)) {
		pr_err("digital clock: failed to init\n");
		goto free_clock;
	}

	clock->view.is_alloc = 1;

	return &clock->view;
free_clock:
	free(clock);
	return NULL;
}

void destory_digital_clock_en(struct digital_clock_en *clock) {
	unregister_time_notify(&clock->no);
	destory_weekenv(&clock->weekv);
	destory_dateenv(&clock->datev);
	destory_timev(&clock->timev);
}

void do_display_digital_clock_cn(struct digital_clock_cn *clock) {
	display_timev(&clock->timev);
	display_datecnv(&clock->datev);
	display_weekcnv(&clock->weekv);
}

int do_sync_digital_clock_cn(struct digital_clock_cn *clock) {
	int errs = 0;

	errs += sync_timev(&clock->timev) ? 1 : 0;
	errs += sync_datecnv(&clock->datev) ? 1 : 0;
	errs += sync_weekcnv(&clock->weekv) ? 1 : 0;

	return errs;
}

void display_digital_clock_cn(struct digital_clock_cn *clock) {
	if (!view_is_ready(&clock->view))
		return ;
	if (!view_want_to_show(&clock->view))
		return;

	digital_clock_cn_set_time(clock, get_currnet_tm());
	do_display_digital_clock_cn(clock);
}

int sync_digital_clock_cn(struct digital_clock_cn *clock) {
	return view_sync_setting(&clock->view);
}

void digital_clock_cn_view_display(struct view *view) {
	struct position *start = view_start(view);
	struct digital_clock_cn *clock = to_digital_clock_cn(view);

	do_display_digital_clock_cn(clock);
	view_set_end(view, start);
}

void digital_clock_cn_cal_size(struct view *view, unsigned int *xmax, unsigned int *ymax) {
	view_grp_cal_size(&view->grp, xmax, ymax);
}

void digital_clock_cn_view_set_bg(struct view *view, struct fb_region *bg) {
	view_grp_set_bg(&view->grp, bg);
}

int digital_clock_cn_view_sync(struct view *view) {
	struct digital_clock_cn *clock = to_digital_clock_cn(view);

	digital_clock_cn_view_set_bg(view, view_bg(view));
	return do_sync_digital_clock_cn(clock);
}

void digital_clock_cn_view_free(struct view *view) {
	struct digital_clock_cn *clock = to_digital_clock_cn(view);

	destory_digital_clock_cn(clock);
	if (view_is_alloc(view))
		free(clock);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_digital_clock_cn_view(struct view *view, struct slpt_app_res *parent) {
	struct slpt_app_res *res;
	struct digital_clock_cn*clock = to_digital_clock_cn(view);

	res = slpt_register_view_base(view, parent, NULL, 0);
	if (!res)
		return NULL;

	if (!slpt_register_timev(&clock->timev, res)) {
		pr_err("digital clock: failed to register time to slpt\n");
		goto unregister_dir;
	}
	if (!slpt_register_datecnv(&clock->datev, res)) {
		pr_err("digital clock: failed to register date to slpt\n");
		goto unregister_dir;
	}
	if (!slpt_register_weekcnv(&clock->weekv, res)) {
		pr_err("digital clock: failed to register week to slpt\n");
		goto unregister_dir;
	}

	return res;
unregister_dir:
	slpt_kernel_unregister_app_res(res, uboot_slpt_task);
	return NULL;
}
#endif

static void digital_clock_cn_time_callback(struct time_notify *no, struct rtc_time *tm) {
	struct digital_clock_cn *clock = container_of(no, struct digital_clock_cn, no);

	digital_clock_cn_set_time(clock, tm);
}

int init_digital_clock_cn(struct digital_clock_cn *clock, const char *name) {
	int ret;

	clock->view.is_alloc = 0;
	view_init_status(&clock->view, name, VIEW_DIGITAL_CLOCK_CN);

	ret = init_timev(&clock->timev);
	if (ret) {
		pr_err("digital clock: failed to init timev\n");
		return ret;
	}

	ret = init_datecnv(&clock->datev);
	if (ret) {
		pr_err("digital clock: failed to init timev\n");
		goto destory_timev;
	}

	ret = init_weekcnv(&clock->weekv);
	if (ret) {
		pr_err("digital clock: failed to init timev\n");
		goto destory_datev;
	}

	view_add_child(&clock->view, clock->timev.text);
	view_add_child(&clock->view, clock->datev.text);
	view_add_child(&clock->view, clock->weekv.text);

	clock->view.display = digital_clock_cn_view_display;
	clock->view.set_bg = digital_clock_cn_view_set_bg;
	clock->view.sync = digital_clock_cn_view_sync;
	clock->view.freev = digital_clock_cn_view_free;
	clock->view.cal_size = digital_clock_cn_cal_size;

#ifdef CONFIG_SLPT
	clock->view.register_slpt = slpt_register_digital_clock_cn_view;
#endif

	clock->no.callback = digital_clock_cn_time_callback;
	register_time_notify(&clock->no, TIME_TICK_SEC);

	return 0;
destory_datev:
	destory_datecnv(&clock->datev);
destory_timev:
	destory_timev(&clock->timev);
	return ret;
}

struct view *alloc_digital_clock_cn(const char *name) {
	struct digital_clock_cn *clock;
	char *cpy_name;

	clock = malloc_with_name(sizeof(*clock), name);
	if (!clock) {
		pr_err("digital clock: failed to alloc\n");
		return NULL;
	}
	cpy_name = (char *)&clock[1];

	if (init_digital_clock_cn(clock, cpy_name)) {
		pr_err("digital clock: failed to init\n");
		goto free_clock;
	}

	clock->view.is_alloc = 1;

	return &clock->view;
free_clock:
	free(clock);
	return NULL;
}

void destory_digital_clock_cn(struct digital_clock_cn *clock) {
	unregister_time_notify(&clock->no);

	destory_weekcnv(&clock->weekv);
	destory_datecnv(&clock->datev);
	destory_timev(&clock->timev);
}
