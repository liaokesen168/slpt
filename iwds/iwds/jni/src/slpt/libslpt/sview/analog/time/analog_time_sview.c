#include <common.h>
#include <rtc_time.h>
#include <sview/analog_time_sview.h>

void analog_time_sview_set_level(struct sview *view, unsigned int level) {
	struct analog_time_sview *timev = to_analog_time_sview(view);
	unsigned int notify_level = 0;

	assert(level < ANALOG_TIME_NUMS);

	switch (level) {
	case ANALOG_TIME_SEC:
		notify_level = TIME_TICK_SEC;
		break;
	case ANALOG_TIME_MIN:
		notify_level = TIME_TICK_MIN;
		break;
	case ANALOG_TIME_HOUR_WITH_MIN:
		notify_level = TIME_TICK_MIN;
		break;
	case ANALOG_TIME_HOUR:
		notify_level = TIME_TICK_HOUR;
		break;
	case ANALOG_TIME_AM_PM:
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

void analog_time_sview_set_tm(struct sview *view, struct rtc_time *tm) {
	struct analog_time_sview *timev = to_analog_time_sview(view);
	unsigned int angle;

	switch (timev->level) {
	case ANALOG_TIME_SEC:    angle = tm->tm_sec * 6; break;
	case ANALOG_TIME_MIN:    angle = tm->tm_min * 6; break;
	case ANALOG_TIME_HOUR:   angle = tm->tm_hour * 30; break;
	case ANALOG_TIME_DAY:    angle = (tm->tm_mday * 360) / 31; break;
	case ANALOG_TIME_WEEK:   angle = (tm->tm_wday * 360) / 7; break;
	case ANALOG_TIME_MON:    angle = tm->tm_mon * 30; break;

	case ANALOG_TIME_HOUR_WITH_MIN:
	                         angle = tm->tm_hour * 30 + ((tm->tm_min * 30) / 60); 
		                     angle = angle - angle % 6; break;
	case ANALOG_TIME_AM_PM:  angle = tm->tm_hour < 12 ? 0 : 180; break;
	default: angle = 0; break;
	}

	analog_time_sview_set_angle(to_sview(timev), angle);
}

static void analog_time_sview_time_callback(struct time_notify *no, struct rtc_time *tm) {
	struct analog_time_sview *timev = container_of(no, struct analog_time_sview, no);

	analog_time_sview_set_tm(to_sview(timev), tm);
}

/* analog_time_sview */

/*
 * see sview/analog_time_sview.h,
 * those sview methods is inheritance from rotate_pic_sview
 */
#if 0
void analog_time_sview_draw(struct sview *view) {
	rotate_pic_sview_draw(view);
}

void analog_time_sview_measure_size(struct sview *view) {
	rotate_pic_sview_measure_size(view);
}

int analog_time_sview_sync(struct sview *view) {
	return rotate_pic_sview_sync(view);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_analog_time_sview(struct sview *view, struct slpt_app_res *parent) {
	return slpt_register_rotate_pic_sview(view, parent);
}
#endif
#endif

void analog_time_sview_free(struct sview *view) {
	struct analog_time_sview *timev = to_analog_time_sview(view);
	unsigned int is_alloc = view->is_alloc;

	unregister_time_notify(&timev->no);

	view->is_alloc = 0;
	rotate_pic_sview_free(view);

	if (is_alloc)
		free(timev);
}

int init_analog_time_sview(struct analog_time_sview *timev, const char *name) {

	init_rotate_pic_sview(&timev->rpv, name);

	to_sview(timev)->is_alloc = 0;
	to_sview(timev)->type = SVIEW_ANALOG_TIME;

	timev->level = ANALOG_TIME_SEC;
	timev->no.callback = analog_time_sview_time_callback;
	register_time_notify(&timev->no, TIME_TICK_SEC);

	return 0;
}

struct sview *alloc_analog_time_sview(const char *name) {
	struct analog_time_sview *timev;
	char *cpy_name;

	timev = malloc_with_name(sizeof(*timev), name);
	if (!timev) {
		pr_err("analog_time_sview: failed to alloc\n");
		return NULL;
	}

	cpy_name = (char *)&timev[1];

	init_analog_time_sview(timev, cpy_name);

	to_sview(timev)->is_alloc = 1;

	return to_sview(timev);
}

