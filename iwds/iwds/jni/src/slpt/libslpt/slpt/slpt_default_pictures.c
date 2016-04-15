#include <common.h>
#include <string.h>
#include <malloc.h>

#include <picture.h>

#define PIC_SIZE_MEDIUM       (0)
#define PIC_SIZE_LARGE        (0)
#define PIC_SIZE_SMALL        (0)
#define PIC_SIZE_SMALLEST     (0)
#define PIC_SIZE_WEEK         (0)

#define PIC_SIZE_LOW_BATTERY_LOW  (0)
#define PIC_SIZE_CHARGEFULL       (0)
#define PIC_SIZE_CHARGE           (0)
#define PIC_SIZE_TIME_SEP         (0)
#define PIC_SIZE_DATE_SEP         (0)
#define PIC_SIZE_MON_NAME         (0)
#define PIC_SIZE_DAY_NAME         (0)
#define PIC_SIZE_WEEK_NAME        (0)
#define PIC_SIZE_SECOND_HANDER    (0)
#define PIC_SIZE_MINUTE_HANDER    (0)
#define PIC_SIZE_HOUR_HANDER      (0)
#define PIC_SIZE_WEEK_HANDER      (0)
#define PIC_SIZE_WEEK_BACKGROUND  (0)
#define PIC_SIZE_MONTH_HANDER      (0)
#define PIC_SIZE_MONTH_BACKGROUND  (0)
#define PIC_SIZE_SECOND_BACKGROUND  (0)
#define PIC_SIZE_MINUTE_BACKGROUND  (0)
#define PIC_SIZE_HOUR_BACKGROUND  (0)
#define PIC_SIZE_BACKGROUND       (0)

static struct picture_desc medium_pics[10] = {
	{"0", PIC_SIZE_MEDIUM},
	{"1", PIC_SIZE_MEDIUM},
	{"2", PIC_SIZE_MEDIUM},
	{"3", PIC_SIZE_MEDIUM},
	{"4", PIC_SIZE_MEDIUM},
	{"5", PIC_SIZE_MEDIUM},
	{"6", PIC_SIZE_MEDIUM},
	{"7", PIC_SIZE_MEDIUM},
	{"8", PIC_SIZE_MEDIUM},
	{"9", PIC_SIZE_MEDIUM},
};

static struct picture_desc large_pics[10] = {
	{"0", PIC_SIZE_LARGE},
	{"1", PIC_SIZE_LARGE},
	{"2", PIC_SIZE_LARGE},
	{"3", PIC_SIZE_LARGE},
	{"4", PIC_SIZE_LARGE},
	{"5", PIC_SIZE_LARGE},
	{"6", PIC_SIZE_LARGE},
	{"7", PIC_SIZE_LARGE},
	{"8", PIC_SIZE_LARGE},
	{"9", PIC_SIZE_LARGE},
};

static struct picture_desc small_pics[10] = {
	{"0", PIC_SIZE_SMALL},
	{"1", PIC_SIZE_SMALL},
	{"2", PIC_SIZE_SMALL},
	{"3", PIC_SIZE_SMALL},
	{"4", PIC_SIZE_SMALL},
	{"5", PIC_SIZE_SMALL},
	{"6", PIC_SIZE_SMALL},
	{"7", PIC_SIZE_SMALL},
	{"8", PIC_SIZE_SMALL},
	{"9", PIC_SIZE_SMALL},
};

static struct picture_desc smallest_pics[10] = {
	{"0", PIC_SIZE_SMALLEST},
	{"1", PIC_SIZE_SMALLEST},
	{"2", PIC_SIZE_SMALLEST},
	{"3", PIC_SIZE_SMALLEST},
	{"4", PIC_SIZE_SMALLEST},
	{"5", PIC_SIZE_SMALLEST},
	{"6", PIC_SIZE_SMALLEST},
	{"7", PIC_SIZE_SMALLEST},
	{"8", PIC_SIZE_SMALLEST},
	{"9", PIC_SIZE_SMALLEST},
};

static struct picture_desc week_pics[7] = {
	{"0", PIC_SIZE_WEEK},
	{"1", PIC_SIZE_WEEK},
	{"2", PIC_SIZE_WEEK},
	{"3", PIC_SIZE_WEEK},
	{"4", PIC_SIZE_WEEK},
	{"5", PIC_SIZE_WEEK},
	{"6", PIC_SIZE_WEEK},
};

static struct picture_desc clock_pics[] = {
	{"time_sep", PIC_SIZE_TIME_SEP},
	{"date_sep", PIC_SIZE_DATE_SEP},
	{"mon_name", PIC_SIZE_MON_NAME},
	{"day_name", PIC_SIZE_DAY_NAME},
	{"week_name", PIC_SIZE_WEEK_NAME},

	{"second_hander", PIC_SIZE_SECOND_HANDER},
	{"minute_hander", PIC_SIZE_MINUTE_HANDER},
	{"hour_hander", PIC_SIZE_HOUR_HANDER},

	{"analog_week_hander", PIC_SIZE_WEEK_HANDER},
	{"analog_week_background", PIC_SIZE_WEEK_BACKGROUND},

	{"analog_month_hander", PIC_SIZE_MONTH_HANDER},
	{"analog_month_background", PIC_SIZE_MONTH_BACKGROUND},

	{"analog_second_hander", PIC_SIZE_SECOND_HANDER},
	{"analog_second_background", PIC_SIZE_SECOND_BACKGROUND},

	{"analog_minute_hander", PIC_SIZE_MINUTE_HANDER},
	{"analog_minute_background", PIC_SIZE_MINUTE_BACKGROUND},

	{"analog_hour_hander", PIC_SIZE_HOUR_HANDER},
	{"analog_hour_background", PIC_SIZE_HOUR_BACKGROUND},

	{"background", PIC_SIZE_BACKGROUND},

	{"low-battery-low", PIC_SIZE_LOW_BATTERY_LOW},

	{"charge_pic", PIC_SIZE_CHARGE},
	{"chargefull_pic", PIC_SIZE_CHARGEFULL},
};

int default_pictures_init_onetime(void) {
	assert(alloc_picture_grp("large_nums", large_pics, ARRAY_SIZE(large_pics)));
	assert(alloc_picture_grp("medium_nums", medium_pics, ARRAY_SIZE(medium_pics)));
	assert(alloc_picture_grp("small_nums", small_pics, ARRAY_SIZE(small_pics)));
	assert(alloc_picture_grp("week_nums", week_pics, ARRAY_SIZE(week_pics)));
	assert(alloc_picture_grp("clock", clock_pics, ARRAY_SIZE(clock_pics)));
	assert(alloc_picture_grp("smallest_nums", smallest_pics, ARRAY_SIZE(smallest_pics)));

	return 0;
}
