#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <view.h>

void free_views(struct view *views[], unsigned int size) {
	unsigned int i;

	for (i = 0; i < size; ++i) {
		free_view(views[i]);
		views[i] = NULL;
	}
}

const char *view_types[VIEW_NUMS] = {
	[VIEW_NUM] = "numview",
	[VIEW_FLASH_PIC] = "flash-pic-view",
	[VIEW_PIC] = "picview",
	[VIEW_TEXT] = "textview",

	[VIEW_DIGITAL_CLOCK_EN] = "digital-clock-en-view",
	[VIEW_DIGITAL_CLOCK_CN] = "digital-clock-cn-view",
	[VIEW_ANALOG_CLOCK] = "analog-clock-view",
	[VIEW_ANALOG_WEEK_CLOCK] = "analog-week-clock-view",
	[VIEW_ANALOG_MONTH_CLOCK] = "analog-month-clock-view",
	[VIEW_ANALOG_SECOND_CLOCK] = "analog-second-clock-view",
	[VIEW_ANALOG_MINUTE_CLOCK] = "analog-minute-clock-view",
	[VIEW_ANALOG_HOUR_CLOCK] = "analog-hour-clock-view",

	[VIEW_DATE_EN] = "date-en-view",
	[VIEW_DATE_CN] = "date-cn-view",
	[VIEW_WEEK_EN] = "week-en-view",
	[VIEW_WEEK_CN] = "week-cn-view",
	[VIEW_YEAR_EN] = "year-en-view",
	[VIEW_TIME]    = "time-view",

};

unsigned int name_to_view_type(const char *str) {
	unsigned int i;

	for (i = 0; i < ARRAY_SIZE(view_types); ++i) {
		if (!strcmp(view_types[i], str)) {
			return i;
		}
	}

	return VIEW_NUMS;
}

void assert_viewdesc(struct viewdesc *desc) {
	assert(desc->s1);
	assert(desc->s2);
	assert(desc->type < VIEW_NUMS);
}

int alloc_views(struct view *views[], struct viewdesc *descs, unsigned int size) {
	unsigned int i;

	for (i = 0; i < size; ++i) {
		assert_viewdesc(&descs[i]);

		switch (descs[i].type) {
		case VIEW_NUM: views[i] = viewdesc_to_num_view(&descs[i]); break;
		case VIEW_FLASH_PIC: views[i] = viewdesc_to_flash_pic_view(&descs[i]); break;
		case VIEW_PIC: views[i] = viewdesc_to_pic_view(&descs[i]); break;
		default:views[i] = NULL; break;
		}

		if (!views[i]) {
			pr_err("view_utils: failed to alloc %s (%s), (%s)\n",
				   type_to_view_name(descs[i].type), descs[i].s1, descs[i].s2);
			free_views(views, i);
			return -EINVAL;
		}
	}
	return 0;
}

struct view *alloc_views_to_text(const char *name,
             struct view *views[], struct viewdesc *descs, unsigned int size) {
	if (alloc_views(views, descs, size)) {
		pr_err("view_utils: failed to alloc views for (%s)\n", name);
		return NULL;
	}

	return alloc_text_view(name, views, size);
}

struct view *alloc_view_by_type(const char *name, unsigned int type) {
	struct view *v = NULL;

	assert(type < VIEW_NUMS);

	switch (type) {
	case VIEW_NUM: v = alloc_num_view(name, "empty_nums"); break;
	case VIEW_FLASH_PIC: v = alloc_flash_pic_view(name, "clock/empty_picture"); break;
	case VIEW_PIC: v = alloc_pic_view(name, "clock/empty_picture"); break;
	case VIEW_TEXT: break; /* can't support it */
	case VIEW_DIGITAL_CLOCK_EN: v = alloc_digital_clock_en(name); break;
	case VIEW_DIGITAL_CLOCK_CN: v = alloc_digital_clock_cn(name); break;
	case VIEW_ANALOG_CLOCK: v = alloc_analog_clock(name); break;
	case VIEW_ANALOG_WEEK_CLOCK: v = alloc_analog_week_clock(name); break;
	case VIEW_ANALOG_MONTH_CLOCK: v = alloc_analog_month_clock(name); break;
	case VIEW_ANALOG_SECOND_CLOCK: v = alloc_analog_second_clock(name); break;
	case VIEW_ANALOG_MINUTE_CLOCK: v = alloc_analog_minute_clock(name); break;
	case VIEW_ANALOG_HOUR_CLOCK: v = alloc_analog_hour_clock(name); break;

	case VIEW_DATE_EN: v = alloc_date_en_view(name); break;
	case VIEW_DATE_CN: v = alloc_date_cn_view(name); break;
	case VIEW_WEEK_EN: v = alloc_week_en_view(name); break;
	case VIEW_WEEK_CN: v = alloc_week_cn_view(name); break;
	case VIEW_YEAR_EN: v = alloc_year_en_view(name); break;
	case VIEW_TIME:    v = alloc_time_view(name);    break;

	}

	return v;
}

struct view *alloc_view_by_str(const char *name, const char *str) {
	unsigned int type = name_to_view_type(str);
	return type < VIEW_NUMS	? alloc_view_by_type(name, type) : NULL;
}
