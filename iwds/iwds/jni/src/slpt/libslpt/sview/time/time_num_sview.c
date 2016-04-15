#include <common.h>
#include <time_notify.h>
#include <sview/sview.h>
#include <sview/time_num_sview.h>

void time_num_sview_set_level(struct sview *view, unsigned int level) {
	struct time_num_sview *timev = to_time_num_sview(view);
	unsigned int notify_level = 0;

	assert(level < TIME_NUM_NUMS);

	switch (level) {
	case TIME_NUM_SEC_L:
	case TIME_NUM_SEC_H:
		notify_level = TIME_TICK_SEC;
		break;
	case TIME_NUM_MIN_L:
	case TIME_NUM_MIN_H:
		notify_level = TIME_TICK_MIN;
		break;
	case TIME_NUM_HOUR_L:
	case TIME_NUM_HOUR_H:
		notify_level = TIME_TICK_HOUR;
		break;
	default:
		notify_level = TIME_TICK_DAY;
		break;
	}

	timev->level = level;
	unregister_time_notify(&timev->no);
	register_time_notify(&timev->no, notify_level);
}

void time_num_sview_set_tm(struct sview *view, struct rtc_time *tm) {
	struct time_num_sview *timev = to_time_num_sview(view);
	unsigned int num;

	switch (timev->level) {
	case TIME_NUM_SEC_L:  num = tm->tm_sec % 10; break;
	case TIME_NUM_SEC_H:  num = tm->tm_sec / 10; break;
	case TIME_NUM_MIN_L:  num = tm->tm_min % 10; break;
	case TIME_NUM_MIN_H:  num = tm->tm_min / 10; break;
	case TIME_NUM_HOUR_L: num = tm->tm_hour % 10; break;
	case TIME_NUM_HOUR_H: num = tm->tm_hour / 10; break;
	case TIME_NUM_DAY_L:  num = tm->tm_mday % 10; break;
	case TIME_NUM_DAY_H:  num = tm->tm_mday / 10; break;
	case TIME_NUM_WEEK:   num = tm->tm_wday; break;
	case TIME_NUM_MON_L:  num = tm->tm_mon % 10; break;
	case TIME_NUM_MON_H:  num = tm->tm_mon / 10; break;
	case TIME_NUM_YEAR_0: num = tm->tm_year % 10; break;
	case TIME_NUM_YEAR_1: num = (tm->tm_year / 10) % 10; break;
	case TIME_NUM_YEAR_2: num = (tm->tm_year / 100) % 10; break;
	case TIME_NUM_YEAR_3: num = (tm->tm_year / 1000) % 10; break;
	default: num = 0; break;
	}

	time_num_sview_set_num(to_sview(timev), num);
}

static void time_num_sview_time_callback(struct time_notify *no, struct rtc_time *tm) {
	struct time_num_sview *timev = container_of(no, struct time_num_sview, no);

	time_num_sview_set_tm(to_sview(timev), tm);
}

/* time_num_sview */

/*
 * see sview/time_num_sview.h,
 * those sview methods is inheritance from num_sview
 */
#if 0
void time_num_sview_draw(struct sview *view) {
	num_sview_draw(view);
}

void time_num_sview_measure_size(struct sview *view) {
	num_sview_measure_size(view);
}

int time_num_sview_sync(struct sview *view) {
	return num_sview_sync(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_time_num_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_num_sview(view, parent);
}
#endif
#endif

void time_num_sview_free(struct sview *view) {
	struct time_num_sview *timev = to_time_num_sview(view);
	unsigned int is_alloc = view->is_alloc;

	unregister_time_notify(&timev->no);

	view->is_alloc = 0;
	num_sview_free(view);

	if (is_alloc)
		free(timev);
}

int init_time_num_sview(struct time_num_sview *timev, const char *name) {

	init_num_sview(&timev->numv, name);

	to_sview(timev)->is_alloc = 0;
	to_sview(timev)->type = SVIEW_TIME_NUM;

	timev->level = TIME_NUM_SEC_L;
	timev->no.callback = time_num_sview_time_callback;
	register_time_notify(&timev->no, TIME_TICK_SEC);

	return 0;
}

struct sview *alloc_time_num_sview(const char *name) {
	struct time_num_sview *timev;
	char *cpy_name;

	timev = malloc_with_name(sizeof(*timev), name);
	if (!timev) {
		pr_err("time_num_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&timev[1];

	init_time_num_sview(timev, cpy_name);

	to_sview(timev)->is_alloc = 1;

	return to_sview(timev);
}
