#ifndef _DIGITAL_CLOCK_H_
#define _DIGITAL_CLOCK_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <view.h>
#include <current_time.h>
#include <time_notify.h>

#ifdef CONFIG_SLPT
#include <slpt.h>
#endif

struct timev {
	struct view *text;
	struct view *array[5];
	struct view *array2[3];
};

struct dateenv {
	struct view *text;
	struct view *array[5];
	struct view *array2[3];
};

struct weekenv {
	struct view *text;
};

struct datecnv {
	struct view *text;
	struct view *array[6];
	struct view *array2[4];
};

struct weekcnv {
	struct view *text;
	struct view *array[2];
};

struct digital_clock_en {
	struct view view;
	struct time_notify no;
	struct timev timev;
	struct dateenv datev;
	struct weekenv weekv;
};

struct digital_clock_cn {
	struct view view;
	struct time_notify no;
	struct timev timev;
	struct datecnv datev;
	struct weekcnv weekv;
};

extern int init_weekenv(struct weekenv *weekv);
extern void destory_weekenv(struct weekenv *weekv);
extern int init_dateenv(struct dateenv *datev);
extern void destory_dateenv(struct dateenv *datev);

extern int init_weekcnv(struct weekcnv *weekv);
extern void destory_weekcnv(struct weekcnv *weekv);
extern int init_datecnv(struct datecnv *datev);
extern void destory_datecnv(struct datecnv *datev);

extern int init_timev(struct timev *timev);
extern void destory_timev(struct timev *timev);

extern int init_digital_clock_en(struct digital_clock_en *clock, const char *name);
extern void destory_digital_clock_en(struct digital_clock_en *clock);
extern void display_digital_clock_en(struct digital_clock_en *clock);
extern int sync_digital_clock_en(struct digital_clock_en *clock);

extern int init_digital_clock_cn(struct digital_clock_cn *clock, const char *name);
extern void destory_digital_clock_cn(struct digital_clock_cn *clock);
extern void display_digital_clock_cn(struct digital_clock_cn *clock);
extern int sync_digital_clock_cn(struct digital_clock_cn *clock);

static inline void display_timev(struct timev *timev) {
	view_display(timev->text);
}

static inline void display_dateenv(struct dateenv *datev) {
	view_display(datev->text);
}

static inline void display_weekenv(struct weekenv *weekv) {
	view_display(weekv->text);
}

static inline void display_datecnv(struct datecnv *datev) {
	view_display(datev->text);
}

static inline void display_weekcnv(struct weekcnv *weekv) {
	view_display(weekv->text);
}

static inline int sync_timev(struct timev *timev) {
	return view_sync_setting(timev->text) &&
		view_want_to_show(timev->text);
}

static inline int sync_dateenv(struct dateenv *datev) {
	return view_sync_setting(datev->text) &&
		view_want_to_show(datev->text);
}

static inline int sync_weekenv(struct weekenv *weekv) {
	return view_sync_setting(weekv->text) &&
		view_want_to_show(weekv->text);
}

static inline int sync_datecnv(struct datecnv *datev) {
	return view_sync_setting(datev->text) &&
		view_want_to_show(datev->text);;
}

static inline int sync_weekcnv(struct weekcnv *weekv) {
	return view_sync_setting(weekv->text) &&
		view_want_to_show(weekv->text);;
}

static inline void timev_set_start(struct timev *timev, struct position *start) {
	view_set_start(timev->text, start);
}

static inline void dateenv_set_start(struct dateenv *datev, struct position *start) {
	view_set_start(datev->text, start);
}

static inline void weekenv_set_start(struct weekenv *weekv, struct position *start) {
	view_set_start(weekv->text, start);
}

static inline void datecnv_set_start(struct datecnv *datev, struct position *start) {
	view_set_start(datev->text, start);
}

static inline void weekcnv_set_start(struct weekcnv *weekv, struct position *start) {
	view_set_start(weekv->text, start);
}

static inline void timev_set_time(struct timev *timev, unsigned int hour, unsigned int min, unsigned int sec) {
	num_view_set_num(timev->array[0], hour / 10);
	num_view_set_num(timev->array[1], hour % 10);
	if (flash_pic_view_is_flash(timev->array[2]))
		flash_pic_view_set_display(timev->array[2], sec % 2);
	num_view_set_num(timev->array[3], min / 10);
	num_view_set_num(timev->array[4], min % 10);
}

static inline void dateenv_set_date(struct dateenv *datev, unsigned int mon, unsigned int day) {
	num_view_set_num(datev->array[0], mon / 10);
	num_view_set_num(datev->array[1], mon % 10);
	num_view_set_num(datev->array[3], day / 10);
	num_view_set_num(datev->array[4], day % 10);
}

static inline void weekenv_set_week(struct weekenv *weekv, unsigned int week) {
	num_view_set_num(weekv->text, week);
}

static inline void datecnv_set_date(struct datecnv *datev, unsigned int mon, unsigned int day) {
	num_view_set_num(datev->array[0], mon / 10);
	num_view_set_num(datev->array[1], mon % 10);
	num_view_set_num(datev->array[3], day / 10);
	num_view_set_num(datev->array[4], day % 10);
}

static inline void weekcnv_set_week(struct weekcnv *weekv, unsigned int week) {
	num_view_set_num(weekv->array[1], week);
}

static inline void digital_clock_en_set_show(struct digital_clock_en *clock, unsigned int show) {
	view_set_show(&clock->view, show);
}

static inline void digital_clock_cn_set_show(struct digital_clock_cn *clock, unsigned int show) {
	view_set_show(&clock->view, show);
}

static inline void digital_clock_cn_set_time(struct digital_clock_cn *clock, struct rtc_time *tm) {
	timev_set_time(&clock->timev, tm->tm_hour, tm->tm_min, tm->tm_sec);
	datecnv_set_date(&clock->datev, tm->tm_mon, tm->tm_mday);
	weekcnv_set_week(&clock->weekv, tm->tm_wday);
}

static inline void digital_clock_en_set_time(struct digital_clock_en *clock, struct rtc_time *tm) {
	timev_set_time(&clock->timev, tm->tm_hour, tm->tm_min, tm->tm_sec);
	dateenv_set_date(&clock->datev, tm->tm_mon, tm->tm_mday);
	weekenv_set_week(&clock->weekv, tm->tm_wday);
}

#ifdef CONFIG_SLPT
static inline struct slpt_app_res *slpt_register_timev(struct timev *timev, struct slpt_app_res *parent) {
	return slpt_register_view(timev->text, parent, NULL, 0);
}

static inline struct slpt_app_res *slpt_register_dateenv(struct dateenv *datev, struct slpt_app_res *parent) {
	return slpt_register_view(datev->text, parent, NULL, 0);	
}

static inline struct slpt_app_res *slpt_register_weekenv(struct weekenv *weekv, struct slpt_app_res *parent) {
	return slpt_register_view(weekv->text, parent, NULL, 0);
}

static inline struct slpt_app_res *slpt_register_datecnv(struct datecnv *datev, struct slpt_app_res *parent) {
	return slpt_register_view(datev->text, parent, NULL, 0);	
}

static inline struct slpt_app_res *slpt_register_weekcnv(struct weekcnv *weekv, struct slpt_app_res *parent) {
	return slpt_register_view(weekv->text, parent, NULL, 0);
}

static inline struct slpt_app_res *slpt_register_digital_clock_en(struct digital_clock_en *clock,
                                                                  struct slpt_app_res *parent) {
	return slpt_register_view(&clock->view, parent, NULL, 0);
}

static inline struct slpt_app_res *slpt_register_digital_clock_cn(struct digital_clock_cn *clock,
                                                                  struct slpt_app_res *parent) {
	return slpt_register_view(&clock->view, parent, NULL, 0);
}

#endif

#ifdef CONFIG_SLPT_LINUX
extern void slpt_load_digital_clock_en(struct digital_clock_en *clock);
extern void slpt_load_digital_clock_cn(struct digital_clock_cn *clock);
#endif

#ifdef __cplusplus
}
#endif
#endif /* _DIGITAL_CLOCK_H_ */
