#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <current_time.h>
#include <analog_clock.h>

static inline void tm_to_angle(struct rtc_time *tm, unsigned int *angle) {
	angle[A_HOUR] = (ANGLE_HOUR * (tm->tm_hour % 12)) + (ANGLE_HOUR * tm->tm_min / 60);
	angle[A_HOUR] = angle[A_HOUR] - (angle[A_HOUR] % ANGLE_MINUTE);
	angle[A_MINUTE] = ANGLE_MINUTE * tm->tm_min;
	angle[A_SECOND] = ANGLE_SECOND * tm->tm_sec;
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_analog_clock_view(struct view *view,
                                                     struct slpt_app_res *parent) {
	struct analog_clock *clock = to_analog_clock(view);
	struct slpt_app_res *res;
	unsigned int i;

	res = slpt_register_view_base(view, parent, NULL, 0);
	if (!res)
		return NULL;

	for (i = 0; i < ARRAY_SIZE(clock->handers); ++i) {
		if (!slpt_register_rotate_pic(&clock->handers[i], res, NULL, 0)) {
			pr_err("analog clock: failed to register to hander [%d]\n", i);			
			goto unregister_dir;
		}
	}

	return res;
unregister_dir:
	slpt_kernel_unregister_app_res(res, uboot_slpt_task);
	return NULL;
}
#endif


void analog_clock_set_time(struct analog_clock *clock, struct rtc_time *tm) {
	tm_to_angle(tm, clock->angle);
	view_set_updated(&clock->view, 1);
}

static void do_restore_analog_clock(struct analog_clock *clock) {
#ifndef CONFIG_SLPT_LINUX
	rotate_pic_restore(&clock->handers[A_SECOND]);
	rotate_pic_restore(&clock->handers[A_MINUTE]);
	rotate_pic_restore(&clock->handers[A_HOUR]);
#endif
}

void restore_analog_clock(struct analog_clock *clock) {
	if (!view_is_ready(&clock->view))
		return ;
	if (!view_want_to_show(&clock->view))
		return;

	do_restore_analog_clock(clock);
}

static void do_save_and_draw_analog_clock(struct analog_clock *clock) {
	rotate_pic_save_and_draw(&clock->handers[A_HOUR], clock->angle[A_HOUR]);
	rotate_pic_save_and_draw(&clock->handers[A_MINUTE], clock->angle[A_MINUTE]);
	rotate_pic_save_and_draw(&clock->handers[A_SECOND], clock->angle[A_SECOND]);
}

void save_and_draw_analog_clock(struct analog_clock *clock) {
	if (!view_want_to_show(&clock->view))
		return;
	if (!view_is_ready(&clock->view))
		return;

	analog_clock_set_time(clock, get_currnet_tm());

	do_save_and_draw_analog_clock(clock);
}

int do_sync_analog_clock(struct analog_clock *clock) {
	int errs = 0;

	errs += rotate_pic_sync(&clock->handers[A_SECOND]) ? 1 : 0;
	errs += rotate_pic_sync(&clock->handers[A_MINUTE]) ? 1 : 0;
	errs += rotate_pic_sync(&clock->handers[A_HOUR]) ? 1 : 0;

	return errs;
}

int sync_analog_clock(struct analog_clock *clock) {
	return view_sync_setting(&clock->view);
}

void analog_clock_view_pre_display(struct view *view) {
	struct analog_clock *clock = to_analog_clock(view);

	do_restore_analog_clock(clock);
}

void analog_clock_view_display(struct view *view) {
	struct position *start = view_start(view);
	struct analog_clock *clock = to_analog_clock(view);

	do_save_and_draw_analog_clock(clock);
	view_set_end(view, start);
}

void analog_clock_view_set_bg(struct view *view, struct fb_region *bg) {
	/* currently do nothing */
}

int analog_clock_view_sync(struct view *view) {
	struct analog_clock *clock = to_analog_clock(view);

	return do_sync_analog_clock(clock);
}

void analog_clock_view_free(struct view *view) {
	struct analog_clock *clock = to_analog_clock(view);

	destory_analog_clock(clock);
	if (view_is_alloc(view))
		free(clock);
}

static void analog_clock_time_callback(struct time_notify *no, struct rtc_time *tm) {
	struct analog_clock *clock = container_of(no, struct analog_clock, no);

	analog_clock_set_time(clock, tm);
}

int init_analog_clock(struct analog_clock *clock, const char *name) {
	int ret;

	clock->view.is_alloc = 0;
	view_init_status(&clock->view, name, VIEW_ANALOG_CLOCK);

	ret = init_rotate_pic(&clock->handers[A_SECOND], "second", "clock/second_hander");
	if (ret) {
		pr_err("analog clock: failed to init sec\n");
		return ret;
	}

	ret = init_rotate_pic(&clock->handers[A_MINUTE], "minute", "clock/minute_hander");
	if (ret) {
		pr_err("analog clock: failed to init minute\n");
		goto destory_second;
	}

	ret = init_rotate_pic(&clock->handers[A_HOUR], "hour", "clock/hour_hander");
	if (ret) {
		pr_err("analog clock: failed to init hour\n");
		goto destory_minute;
	}

	clock->view.display = analog_clock_view_display;
	clock->view.set_bg = analog_clock_view_set_bg;
	clock->view.sync = analog_clock_view_sync;
	clock->view.freev = analog_clock_view_free;
	clock->view.pre_display = analog_clock_view_pre_display;

#ifdef CONFIG_SLPT
	clock->view.register_slpt = slpt_register_analog_clock_view;
#endif

	clock->no.callback = analog_clock_time_callback;
	register_time_notify(&clock->no, TIME_TICK_SEC);

	return 0;
destory_minute:
	destory_rotate_pic(&clock->handers[A_MINUTE]);
destory_second:
	destory_rotate_pic(&clock->handers[A_SECOND]);
	return ret;
}

struct view *alloc_analog_clock(const char *name) {
	struct analog_clock *clock;
	char *cpy_name;

	clock = malloc_with_name(sizeof(*clock), name);
	if (!clock) {
		pr_err("analog clock: failed to alloc\n");
		return NULL;
	}
	cpy_name = (char *)&clock[1];

	if (init_analog_clock(clock, cpy_name)) {
		pr_err("analog clock: failed to init\n");
		goto free_clock;
	}

	clock->view.is_alloc = 1;

	return &clock->view;
free_clock:
	free(clock);
	return NULL;
}

void destory_analog_clock(struct analog_clock *clock) {
	unregister_time_notify(&clock->no);
	destory_rotate_pic(&clock->handers[A_SECOND]);
	destory_rotate_pic(&clock->handers[A_MINUTE]);
	destory_rotate_pic(&clock->handers[A_HOUR]);
}
