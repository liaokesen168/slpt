#include <common.h>
#include <view.h>
#include <digital_clock.h>
#include <analog_clock.h>
#include <analog_week_clock.h>
#include <analog_month_clock.h>
#include <analog_second_clock.h>
#include <analog_minute_clock.h>
#include <analog_hour_clock.h>
#include <slpt_file.h>
#include <arg_parse.h>
#include <background.h>
#include <time_notify.h>
#include <charge_picture.h>
#include <find_pictures_test.h>
#include <sview/sview.h>

#define MAX_VIEWS 8
#define MAX_DISPLAY_LEN (MAX_VIEWS * (MAX_NAME_LEN + 1))

#define SLPT_RES_ROOT "/sys/slpt/apps/slpt-app/res/"

struct display_setting {
	char clocks[MAX_DISPLAY_LEN];
	time_t time;
	struct list_head handlers;
};

static struct display_setting display = {
	.handlers = LIST_HEAD_INIT(display.handlers),
};

static int load_dipslay_name() {
	time_t time = display.time;

	slpt_load_single_file("setting/display/clocks-to-show", display.clocks, &display.time, 1);
	
	if (display.time != time) {
		display.time = time;
		return 1;
	}

	return 0;
}

static int find_display_name(const char *name) {
	struct arg_parse ap;
	const char *arg;
	int ret = -1;

	arg_parse_init(&ap, display.clocks);
	while ((arg = arg_parse_next(&ap))) {
		pr_debug("display: str: [%s]\n", arg);
		if (!strcmp(arg, name)) {
			ret = 0;
			goto out;
		}
	}

out:
	arg_parse_destory(&ap);
	return ret;
}

static struct view *find_display_view(const char *name) {
	struct list_head *pos, *n;

	/* firstly we clean the dead fish */
	list_for_each_safe(pos, n, &display.handlers) {
		struct view *v = list_entry(pos, struct view, link);
		if (!strcmp(view_name(v), name))
			return v;
	}

	return NULL;
}

static struct view *gen_display_view(const char *name) {
	unsigned int i;
	struct view *v = NULL;
	char *vname;

	for (i = 0; i < VIEW_NUMS; ++i) {
		vname = strtail(view_types[i], name);

		if (vname) {
			v = alloc_view_by_type(name, i);
			break;
		}
	}

	pr_debug("display: gen: %s\t\t --> [%s] [%s] \n",
			name, i >= VIEW_NUMS ? "null" : view_types[i], vname ? vname : "null");

	return v;
}

static void add_display_view(struct view *v) {
	list_add_tail(&v->link, &display.handlers);
}

static void add_display_view_by_level(struct view *v) {
	struct list_head *pos;

	list_for_each(pos, &display.handlers) {
		struct view *view = list_entry(pos, struct view, link);
		pr_debug("scan: [%s]\n", view_name(view));
		if (view_level(v) < view_level(view))
			break;
	}
	list_add_tail(&v->link, pos);
}

static void sync_view_list(void) {
	struct list_head *pos, *n;
	struct arg_parse ap;
	const char *arg;
	struct view *v;

	pr_debug("display: clean dead fish\n");

	/* firstly we clean the dead fish */
	list_for_each_safe(pos, n, &display.handlers) {
		struct view *v = list_entry(pos, struct view, link);
		if (find_display_name(view_name(v)))
			free_view(v);
	}

	pr_debug("display: add new comer\n");
	/* add new comer */
	arg_parse_init(&ap, display.clocks);
	while ((arg = arg_parse_next(&ap))) {
		pr_debug("display: find: [%s] %s\n", arg, find_display_view(arg) ? "Y" : "N");

		if (!find_display_view(arg)) {
			v = gen_display_view(arg);
			if (v)
				add_display_view(v);
		}
	}

	arg_parse_destory(&ap);
}

static void sort_view_list(void) {
	struct list_head *pos, *n;
	struct list_head list = LIST_HEAD_INIT(list);

	list_cut_position(&list, &display.handlers, display.handlers.prev);
	list_for_each_safe(pos, n, &list) {
		struct view *v = list_entry(pos, struct view, link);
		list_del(pos);
		add_display_view_by_level(v);
	}
}

static void sync_views(void) {
	struct list_head *pos, *n;
	char cur_dir[MAX_FILE_NAME] = SLPT_RES_ROOT;

	getcwd(cur_dir, MAX_FILE_NAME);

	if(chdir(SLPT_RES_ROOT) != 0) {
		pr_err("Couldn`t change (%s) diretory!", SLPT_RES_ROOT);
		return ;
	}
	chdir("clock");

	pr_debug("display: sync every views\n");

	list_for_each_safe(pos, n, &display.handlers) {
		struct view *v = list_entry(pos, struct view, link);
		slpt_load_view(v);
		view_sync_setting(v);
	}

	sort_view_list();

	chdir(cur_dir);
}

static void sync_start_of_views(void) {
	struct list_head *pos, *n;

	list_for_each_safe(pos, n, &display.handlers) {
		struct view *v = list_entry(pos, struct view, link);
		view_sync_start(v);
	}
}

static void pre_display_views(void) {
	struct list_head *pos, *n;

	list_for_each_safe(pos, n, &display.handlers) {
		struct view *v = list_entry(pos, struct view, link);
		view_pre_display(v);
	}
}

static void display_views(void) {
	struct list_head *pos, *n;

	list_for_each_safe(pos, n, &display.handlers) {
		struct view *v = list_entry(pos, struct view, link);
		view_display(v);
	}
}

static void slpt_sync_all_pictures(void) {
    do_find_pictures();
}

extern int default_pictures_init_onetime(void);
extern void slpt_sync_pictures(void);
extern void slpt_sync_settings(void);
extern void slpt_load_analog_clock(struct analog_clock *clock);
extern void slpt_display_sync(void);
extern int slpt_time_tick(void);
extern int slpt_files_need_sync(void);

void free_view_list(void) {
	struct list_head *pos, *n;

	pr_debug("display: free whole list\n");

	/* firstly we clean the dead fish */
	list_for_each_safe(pos, n, &display.handlers) {
		struct view *v = list_entry(pos, struct view, link);
		free_view(v);
	}
}

struct sview *sview1 = NULL;
struct sview *sview2 = NULL;
struct sview *sview3 = NULL;
struct sview *linear_layout = NULL;
struct sview *absolute_layout = NULL;
struct sview *secondl = NULL;
struct sview *secondh = NULL;
struct sview *rotate_view = NULL;
struct sview *analog_second = NULL;
struct sview *analog_minute = NULL;
struct sview *analog_hour = NULL;
struct sview *frame_layout = NULL;
unsigned int m_angle = 0;

void slpt_display_sync(void) {
	int ret;

	slpt_sync_pictures();
	slpt_sync_settings();
	sync_global_bg();
	sync_charge_picture();
	sync_chargefull_picture();
	slpt_sync_all_pictures();

	ret = load_dipslay_name();
	if (ret > 0)
		sync_view_list();

	sync_views();
	set_time_notify_level(TIME_TICK_YEAR);
#if 0
	if (sview1)
		sview_sync_setting(sview1);
	if (linear_layout)
		sview_sync_setting(linear_layout);
	if (absolute_layout)
		sview_sync_setting(absolute_layout);
#endif

	root_sview_sync_setting();
}

int slpt_display(void) {
	int tmp = 0;
	if ((tmp = slpt_files_need_sync())) {
			slpt_display_sync();
	}

	if (!slpt_time_tick()) {
		if(!tmp) /* if the clock change, we need to display, can't wait it one second */
			return 0;
	}

	/* display_global_bg(); */
	display_charge_picture();
	display_chargefull_picture();
	sync_start_of_views();
	time_notify();
	pre_display_views();
	display_views();

	secondh->align_parent_y = ALIGN_BOTTOM;
	secondl->padding.top = 30;
	secondl->align_parent_y = ALIGN_BOTTOM;

#if 0
	analog_hour->rect.h = 100;
	analog_hour->desc_h = RECT_SPECIFY;
	analog_hour->rect.w = 100;
	analog_hour->desc_w = RECT_SPECIFY;
	analog_hour->background.color = 0xffff00;
	analog_hour->align_x = ALIGN_CENTER;
	analog_hour->align_y = ALIGN_CENTER;
	analog_hour->center_vertical = 1;
	analog_hour->center_horizontal = 1;

	analog_minute->rect.h = 80;
	analog_minute->desc_h = RECT_SPECIFY;
	analog_minute->rect.w = 60;
	analog_minute->desc_w = RECT_SPECIFY;
	analog_minute->background.color = 0xffff;
	analog_minute->align_x = ALIGN_CENTER;
	analog_minute->align_y = ALIGN_CENTER;
	analog_minute->center_vertical = 1;
	analog_minute->center_horizontal = 1;

	analog_second->rect.h = 50;
	analog_second->desc_h = RECT_SPECIFY;
	analog_second->rect.w = 50;
	analog_second->desc_w = RECT_SPECIFY;
	analog_second->background.color = 0xff;
	analog_second->align_x = ALIGN_CENTER;
	analog_second->align_y = ALIGN_CENTER;
	analog_second->center_vertical = 1;
	analog_second->center_horizontal = 1;
#endif

	frame_layout->align_x = ALIGN_CENTER;
	frame_layout->align_y = ALIGN_CENTER;
	frame_layout->center_vertical = 1;
	frame_layout->center_horizontal = 1;

	linear_layout->raw_position.x = 0;
	linear_layout->raw_position.y = 50;
	linear_layout->align_parent_y = ALIGN_CENTER;
	linear_layout->background.color = 0xff0000;
	linear_layout->rect.h = 100;
	linear_layout->desc_h = RECT_SPECIFY;
	/* linear_layout->center_vertical = 1; */
	/* linear_layout->center_horizontal = 1; */

	absolute_layout->raw_position.x = 0;
	absolute_layout->raw_position.y = 0;
	absolute_layout->background.color = 0x00ff00;
	absolute_layout->rect.w = 240;
	absolute_layout->rect.h = 240;
	absolute_layout->desc_w = RECT_SPECIFY;
	absolute_layout->desc_h = RECT_SPECIFY;
	absolute_layout->base = *get_current_fb_region();

	root_sview_measure_size();
	root_sview_draw();

	return 0;
}

void slpt_display_init(void) {
	default_pictures_init_onetime();
	init_global_bg_pic();
	init_charge_picture();
	init_chargefull_picture();
	sview1 = alloc_pic_sview("sview1");
	pic_sview_set_pic(sview1, "large_nums/5");

	sview2 = alloc_pic_sview("week-pic");
	pic_sview_set_pic(sview2, "week_nums/1");

	sview3 = alloc_num_sview("week-num");
	num_sview_set_pic_grp(sview3, "week_nums");

	secondl = alloc_secondL_sview("secondl");
	secondL_sview_set_pic_grp(secondl, "large_nums");

	secondh = alloc_secondH_sview("secondh");
	secondH_sview_set_pic_grp(secondh, "large_nums");

	rotate_view = alloc_rotate_pic_sview("rotate");
	rotate_pic_sview_set_pic(rotate_view, "clock/second_hander");

	analog_second = alloc_analog_second_sview("analog-second");
	analog_second_sview_set_pic(analog_second, "clock/second_hander");

	analog_minute = alloc_analog_minute_sview("analog-minute");
	analog_minute_sview_set_pic(analog_minute, "clock/minute_hander");

	analog_hour = alloc_analog_hour_with_minute_sview("analog-hour");
	analog_hour_with_minute_sview_set_pic(analog_hour, "clock/hour_hander");

	linear_layout = alloc_linear_layout("linear-layout");
#if 1
	linear_layout_add(linear_layout, sview1);
	linear_layout_add(linear_layout, sview2);
	linear_layout_add(linear_layout, sview3);
	linear_layout_add(linear_layout, secondh);
	linear_layout_add(linear_layout, secondl);
#endif

	frame_layout = alloc_frame_layout("frame-layout");
	frame_layout_add(frame_layout, analog_hour);
	frame_layout_add(frame_layout, analog_minute);
	frame_layout_add(frame_layout, analog_second);


	absolute_layout = alloc_absolute_layout("absolute_layout");
	/* absolute_layout_add(absolute_layout, linear_layout); */
	absolute_layout_add(absolute_layout, frame_layout);
	background_set_pic(&absolute_layout->background, "clock/background");

	set_root_sview(absolute_layout);

	slpt_display_sync();
}
