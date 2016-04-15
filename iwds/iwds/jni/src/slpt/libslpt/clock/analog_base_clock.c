/*
 * analog_clock_common.c
 *
 *  Created on: May 18, 2015
 *      Author: xblin
 */

#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <current_time.h>
#include <analog_base_clock.h>

#define ANGLE_DEFAULT (360 / 7)

static void tm_to_angle_default(struct rtc_time *tm, unsigned int *angle) {
	*angle = ANGLE_DEFAULT * tm->tm_wday;
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_analog_base_clock_view(struct view *view,
                                                     struct slpt_app_res *parent) {
	struct analog_base_clock *clock = to_analog_base_clock(view);
	struct slpt_app_res *res;
	unsigned int i;

	res = slpt_register_view_base(view, parent, NULL, 0);
	if (!res)
		return NULL;

	for (i = 0; i < ARRAY_SIZE(clock->handers); ++i) {
		if (!slpt_register_rotate_pic(&clock->handers[i], res, NULL, 0)) {
			pr_err("analog small clock: failed to register to hander [%d]\n", i);
			goto unregister_dir;
		}
	}

	if (!slpt_register_view(&clock->bkg.view, res, NULL, 0)) {
		pr_err("analog small clock: failed to register the background view [%d]\n", i);
		goto unregister_rotate_pic;
	}

	return res;

unregister_rotate_pic:
	for (i = ARRAY_SIZE(clock->handers) -1; i < 0; --i) {
		slpt_unregister_rotate_pic(&clock->handers[i]);
	}
unregister_dir:
	slpt_kernel_unregister_app_res(res, uboot_slpt_task);

return NULL;
}
#endif

static void analog_base_clock_set_time(struct analog_base_clock *clock, struct rtc_time *tm) {
	clock->tm_to_angle(tm, &clock->angle);
	view_set_updated(&clock->view, 1);
}

static void do_restore_analog_base_clock(struct analog_base_clock *clock) {
#ifndef CONFIG_SLPT_LINUX
	rotate_pic_restore(&clock->handers[0]);
#endif
}

static void do_save_and_draw_analog_base_clock(struct analog_base_clock *clock) {
	rotate_pic_save_and_draw(&clock->handers[0], clock->angle);
}

static int sync_analog_base_clock_background(struct view *view) {
	int ret = 0;

	ret = view_sync_setting(view);

	view_sync_start(view);

	return ret;
}


static int do_sync_analog_base_clock(struct analog_base_clock *clock) {
	int errs = 0;

	errs += sync_analog_base_clock_background(&(clock->bkg.view));

	errs += rotate_pic_sync(&clock->handers[0]) ? 1 : 0;

	return errs;
}

static void analog_base_clock_view_pre_display(struct view *view) {
	struct analog_base_clock *clock = to_analog_base_clock(view);

	do_restore_analog_base_clock(clock);
}

static void analog_base_clock_view_display(struct view *view) {
	struct position start = *view_start(view); /* this "start" express the center-x & center-y */
	struct analog_base_clock *clock = to_analog_base_clock(view);

	if (view_is_follow(view)) {
		if (view_want_to_show(view)) {
			if (!position_equal(&start, &clock->handers[0].center)) {
				view_set_updated(view, 1);
				clock->handers[0].center = start;
				analog_base_clock_view_pre_display(view);
				rotate_set_dst(&clock->handers[0].rt, &clock->handers[0].center);
			}

			/* clock->bkg.view.start.x&y is the start position,so we should change the  center-x for it */
			start.x = start.x - pic_view_region(&clock->bkg.view)->xres/2;
			start.y = start.y - pic_view_region(&clock->bkg.view)->yres/2;

			view_set_start(&clock->bkg.view, &start);

			view_display(&clock->bkg.view);
		}
	} else {
		view_display(&clock->bkg.view);
	}

	do_save_and_draw_analog_base_clock(clock);
}

static void analog_base_clock_view_set_bg(struct view *view, struct fb_region *bg) {
	/* currently do nothing */
}

static int analog_base_clock_view_sync(struct view *view) {
	struct analog_base_clock *clock = to_analog_base_clock(view);
	return do_sync_analog_base_clock(clock);
}

static void destory_analog_base_clock(struct view *view) {
	struct analog_base_clock *clock = to_analog_base_clock(view);

	unregister_time_notify(&clock->no);
	destory_rotate_pic(&clock->handers[0]);
}

static void analog_base_clock_view_free(struct view *view) {
	struct analog_base_clock *clock = to_analog_base_clock(view);

	destory_analog_base_clock(view);

	if (view_is_alloc(view))
		free(clock);

}

static void analog_base_clock_time_callback(struct time_notify *no, struct rtc_time *tm) {
	struct analog_base_clock *clock = container_of(no, struct analog_base_clock, no);

	analog_base_clock_set_time(clock, tm);
}

void init_analog_base_clock_status(struct analog_base_clock *clock, const char *bkg_pic,
									const char *hander_pic, unsigned int time_level) {

	init_pic_view(&clock->bkg, "background", bkg_pic/*"clock/analog_week_background"*/);

	init_rotate_pic(&clock->handers[0], "hander", hander_pic/*"clock/analog_week_hander"*/);

	clock->view.is_alloc = 0;
	view_set_follow(&clock->view, 1); /* set view follow-mode default is 1 */

	clock->tm_to_angle = tm_to_angle_default;
	clock->view.display = analog_base_clock_view_display;
	clock->view.set_bg = analog_base_clock_view_set_bg;
	clock->view.sync = analog_base_clock_view_sync;
	clock->view.freev = analog_base_clock_view_free;
	clock->view.pre_display = analog_base_clock_view_pre_display;

#ifdef CONFIG_SLPT
	clock->view.register_slpt = slpt_register_analog_base_clock_view;
#endif

	clock->no.callback = analog_base_clock_time_callback;

	register_time_notify(&clock->no, time_level);

}
