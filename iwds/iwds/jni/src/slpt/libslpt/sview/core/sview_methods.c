#include <common.h>
#include <sview/sview.h>

/**
 * this file is content the sview's methods.
 * if you have a new type of sview, implement all the methods, and put the function pointer here
 * NOTICE : NULL pointer is not allowed here!
 */

typedef void (*init_sview_type)(void *, const char *);

/**
 * init_view() : initialize exist view
 */
void (*sview_method_init_view[SVIEW_NUMS])(void *view, const char *name) = {
	[SVIEW_PIC] = (init_sview_type)init_pic_sview,
	[SVIEW_NUM] = (init_sview_type)init_num_sview,
	[SVIEW_LINEAR_LAYOUT] = (init_sview_type)init_linear_layout,
	[SVIEW_ABSOLUTE_LAYOUT] = (init_sview_type)init_absolute_layout,
	[SVIEW_FRAME_LAYOUT] = (init_sview_type)init_frame_layout,
	[SVIEW_TIME_NUM] = (init_sview_type)init_time_num_sview,
	[SVIEW_SECOND_L] = (init_sview_type)init_secondL_sview,
	[SVIEW_SECOND_H] = (init_sview_type)init_secondH_sview,
	[SVIEW_MINUTE_L] = (init_sview_type)init_minuteL_sview,
	[SVIEW_MINUTE_H] = (init_sview_type)init_minuteH_sview,
	[SVIEW_HOUR_L] = (init_sview_type)init_hourL_sview,
	[SVIEW_HOUR_H] = (init_sview_type)init_hourH_sview,
	[SVIEW_DAY_L] = (init_sview_type)init_dayL_sview,
	[SVIEW_DAY_H] = (init_sview_type)init_dayH_sview,
	[SVIEW_WEEK] = (init_sview_type)init_week_sview,
	[SVIEW_MONTH_L] = (init_sview_type)init_monthL_sview,
	[SVIEW_MONTH_H] = (init_sview_type)init_monthH_sview,
	[SVIEW_YEAR0] = (init_sview_type)init_year0_sview,
	[SVIEW_YEAR1] = (init_sview_type)init_year1_sview,
	[SVIEW_YEAR2] = (init_sview_type)init_year2_sview,
	[SVIEW_YEAR3] = (init_sview_type)init_year3_sview,
	[SVIEW_ROTATE_PIC] = (init_sview_type)init_rotate_pic_sview,
	[SVIEW_ANALOG_TIME] = (init_sview_type)init_analog_time_sview,
	[SVIEW_ANALOG_SECOND] = (init_sview_type)init_analog_second_sview,
	[SVIEW_ANALOG_MINUTE] = (init_sview_type)init_analog_minute_sview,
	[SVIEW_ANALOG_HOUR] = (init_sview_type)init_analog_hour_sview,
	[SVIEW_ANALOG_DAY] = (init_sview_type)init_analog_day_sview,
	[SVIEW_ANALOG_WEEK] = (init_sview_type)init_analog_week_sview,
	[SVIEW_ANALOG_MONTH] = (init_sview_type)init_analog_month_sview,
	[SVIEW_ANALOG_AM_PM] = (init_sview_type)init_analog_am_pm_sview,
	[SVIEW_ANALOG_HOUR_WITH_MINUTE] = (init_sview_type)init_analog_hour_with_minute_sview,
};

/**
 * alloc_view() : allocate a new view
 */
struct sview * (*sview_method_alloc_view[SVIEW_NUMS])(const char *name) = {
	[SVIEW_PIC] = alloc_pic_sview,
	[SVIEW_NUM] = alloc_num_sview,
	[SVIEW_LINEAR_LAYOUT] = alloc_linear_layout,
	[SVIEW_ABSOLUTE_LAYOUT] = alloc_absolute_layout,
	[SVIEW_FRAME_LAYOUT] = alloc_frame_layout,
	[SVIEW_TIME_NUM] = alloc_time_num_sview,
	[SVIEW_SECOND_L] = alloc_secondL_sview,
	[SVIEW_SECOND_H] = alloc_secondH_sview,
	[SVIEW_MINUTE_L] = alloc_minuteL_sview,
	[SVIEW_MINUTE_H] = alloc_minuteH_sview,
	[SVIEW_HOUR_L] = alloc_hourL_sview,
	[SVIEW_HOUR_H] = alloc_hourH_sview,
	[SVIEW_DAY_L] = alloc_dayL_sview,
	[SVIEW_DAY_H] = alloc_dayH_sview,
	[SVIEW_WEEK] = alloc_week_sview,
	[SVIEW_MONTH_L] = alloc_monthL_sview,
	[SVIEW_MONTH_H] = alloc_monthH_sview,
	[SVIEW_YEAR0] = alloc_year0_sview,
	[SVIEW_YEAR1] = alloc_year1_sview,
	[SVIEW_YEAR2] = alloc_year2_sview,
	[SVIEW_YEAR3] = alloc_year3_sview,
	[SVIEW_ROTATE_PIC] = alloc_rotate_pic_sview,
	[SVIEW_ANALOG_TIME] = alloc_analog_time_sview,
	[SVIEW_ANALOG_SECOND] = alloc_analog_second_sview,
	[SVIEW_ANALOG_MINUTE] = alloc_analog_minute_sview,
	[SVIEW_ANALOG_HOUR] = alloc_analog_hour_sview,
	[SVIEW_ANALOG_DAY] = alloc_analog_day_sview,
	[SVIEW_ANALOG_WEEK] = alloc_analog_week_sview,
	[SVIEW_ANALOG_MONTH] = alloc_analog_month_sview,
	[SVIEW_ANALOG_AM_PM] = alloc_analog_am_pm_sview,
	[SVIEW_ANALOG_HOUR_WITH_MINUTE] = alloc_analog_hour_with_minute_sview,
};

/**
 * measure_size() : measure the raw rect of view
 */
void (*sview_method_measure_size[SVIEW_NUMS])(struct sview *view) = {
	[SVIEW_PIC] = pic_sview_measure_size,
	[SVIEW_NUM] = num_sview_measure_size,
	[SVIEW_LINEAR_LAYOUT] = linear_layout_measure_size,
	[SVIEW_ABSOLUTE_LAYOUT] = absolute_layout_measure_size,
	[SVIEW_FRAME_LAYOUT] = frame_layout_measure_size,
	[SVIEW_TIME_NUM] = time_num_sview_measure_size,
	[SVIEW_SECOND_L] = secondL_sview_measure_size,
	[SVIEW_SECOND_H] = secondH_sview_measure_size,
	[SVIEW_MINUTE_L] = minuteL_sview_measure_size,
	[SVIEW_MINUTE_H] = minuteH_sview_measure_size,
	[SVIEW_HOUR_L] = hourL_sview_measure_size,
	[SVIEW_HOUR_H] = hourH_sview_measure_size,
	[SVIEW_DAY_L] = dayL_sview_measure_size,
	[SVIEW_DAY_H] = dayH_sview_measure_size,
	[SVIEW_WEEK] = week_sview_measure_size,
	[SVIEW_MONTH_L] = monthL_sview_measure_size,
	[SVIEW_MONTH_H] = monthH_sview_measure_size,
	[SVIEW_YEAR0] = year0_sview_measure_size,
	[SVIEW_YEAR1] = year1_sview_measure_size,
	[SVIEW_YEAR2] = year2_sview_measure_size,
	[SVIEW_YEAR3] = year3_sview_measure_size,
	[SVIEW_ROTATE_PIC] = rotate_pic_sview_measure_size,
	[SVIEW_ANALOG_TIME] = analog_time_sview_measure_size,
	[SVIEW_ANALOG_SECOND] = analog_second_sview_measure_size,
	[SVIEW_ANALOG_MINUTE] = analog_minute_sview_measure_size,
	[SVIEW_ANALOG_HOUR] = analog_hour_sview_measure_size,
	[SVIEW_ANALOG_DAY] = analog_day_sview_measure_size,
	[SVIEW_ANALOG_WEEK] = analog_week_sview_measure_size,
	[SVIEW_ANALOG_MONTH] = analog_month_sview_measure_size,
	[SVIEW_ANALOG_AM_PM] = analog_am_pm_sview_measure_size,
	[SVIEW_ANALOG_HOUR_WITH_MINUTE] = analog_hour_with_minute_sview_measure_size,
};

/**
 * draw() : draw the view
 */
void (*sview_method_draw[SVIEW_NUMS])(struct sview *view) = {
	[SVIEW_PIC] = pic_sview_draw,
	[SVIEW_NUM] = num_sview_draw,
	[SVIEW_LINEAR_LAYOUT] = linear_layout_draw,
	[SVIEW_ABSOLUTE_LAYOUT] = absolute_layout_draw,
	[SVIEW_FRAME_LAYOUT] = frame_layout_draw,
	[SVIEW_SECOND_L] = secondL_sview_draw,
	[SVIEW_SECOND_H] = secondH_sview_draw,
	[SVIEW_MINUTE_L] = minuteL_sview_draw,
	[SVIEW_MINUTE_H] = minuteH_sview_draw,
	[SVIEW_HOUR_L] = hourL_sview_draw,
	[SVIEW_HOUR_H] = hourH_sview_draw,
	[SVIEW_DAY_L] = dayL_sview_draw,
	[SVIEW_DAY_H] = dayH_sview_draw,
	[SVIEW_WEEK] = week_sview_draw,
	[SVIEW_MONTH_L] = monthL_sview_draw,
	[SVIEW_MONTH_H] = monthH_sview_draw,
	[SVIEW_YEAR0] = year0_sview_draw,
	[SVIEW_YEAR1] = year1_sview_draw,
	[SVIEW_YEAR2] = year2_sview_draw,
	[SVIEW_YEAR3] = year3_sview_draw,
	[SVIEW_ROTATE_PIC] = rotate_pic_sview_draw,
	[SVIEW_ANALOG_TIME] = analog_time_sview_draw,
	[SVIEW_ANALOG_SECOND] = analog_second_sview_draw,
	[SVIEW_ANALOG_MINUTE] = analog_minute_sview_draw,
	[SVIEW_ANALOG_HOUR] = analog_hour_sview_draw,
	[SVIEW_ANALOG_DAY] = analog_day_sview_draw,
	[SVIEW_ANALOG_WEEK] = analog_week_sview_draw,
	[SVIEW_ANALOG_MONTH] = analog_month_sview_draw,
	[SVIEW_ANALOG_AM_PM] = analog_am_pm_sview_draw,
	[SVIEW_ANALOG_HOUR_WITH_MINUTE] = analog_hour_with_minute_sview_draw,
};

/**
 * sync_setting() : sync the configure and resource of view
 */
int (*sview_method_sync_setting[SVIEW_NUMS])(struct sview *view) = {
	[SVIEW_PIC] = pic_sview_sync,
	[SVIEW_NUM] = num_sview_sync,
	[SVIEW_LINEAR_LAYOUT] = linear_layout_sync,
	[SVIEW_ABSOLUTE_LAYOUT] = absolute_layout_sync,
	[SVIEW_FRAME_LAYOUT] = frame_layout_sync,
	[SVIEW_TIME_NUM] = time_num_sview_sync,
	[SVIEW_SECOND_L] = secondL_sview_sync,
	[SVIEW_SECOND_H] = secondH_sview_sync,
	[SVIEW_MINUTE_L] = minuteL_sview_sync,
	[SVIEW_MINUTE_H] = minuteH_sview_sync,
	[SVIEW_HOUR_L] = hourL_sview_sync,
	[SVIEW_HOUR_H] = hourH_sview_sync,
	[SVIEW_DAY_L] = dayL_sview_sync,
	[SVIEW_DAY_H] = dayH_sview_sync,
	[SVIEW_WEEK] = week_sview_sync,
	[SVIEW_MONTH_L] = monthL_sview_sync,
	[SVIEW_MONTH_H] = monthH_sview_sync,
	[SVIEW_YEAR0] = year0_sview_sync,
	[SVIEW_YEAR1] = year1_sview_sync,
	[SVIEW_YEAR2] = year2_sview_sync,
	[SVIEW_YEAR3] = year3_sview_sync,
	[SVIEW_ROTATE_PIC] = rotate_pic_sview_sync,
	[SVIEW_ANALOG_TIME] = analog_time_sview_sync,
	[SVIEW_ANALOG_SECOND] = analog_second_sview_sync,
	[SVIEW_ANALOG_MINUTE] = analog_minute_sview_sync,
	[SVIEW_ANALOG_HOUR] = analog_hour_sview_sync,
	[SVIEW_ANALOG_DAY] = analog_day_sview_sync,
	[SVIEW_ANALOG_WEEK] = analog_week_sview_sync,
	[SVIEW_ANALOG_MONTH] = analog_month_sview_sync,
	[SVIEW_ANALOG_AM_PM] = analog_am_pm_sview_sync,
	[SVIEW_ANALOG_HOUR_WITH_MINUTE] = analog_hour_with_minute_sview_sync,
};

/**
 * freev() : free the view, and it's resource
 */
void (*sview_method_free[SVIEW_NUMS])(struct sview *view) = {
	[SVIEW_PIC] = pic_sview_free,
	[SVIEW_NUM] = num_sview_free,
	[SVIEW_LINEAR_LAYOUT] = linear_layout_free,
	[SVIEW_ABSOLUTE_LAYOUT] = absolute_layout_free,
	[SVIEW_FRAME_LAYOUT] = frame_layout_free,
	[SVIEW_TIME_NUM] = time_num_sview_free,
	[SVIEW_SECOND_L] = secondL_sview_free,
	[SVIEW_SECOND_H] = secondH_sview_free,
	[SVIEW_MINUTE_L] = minuteL_sview_free,
	[SVIEW_MINUTE_H] = minuteH_sview_free,
	[SVIEW_HOUR_L] = hourL_sview_free,
	[SVIEW_HOUR_H] = hourH_sview_free,
	[SVIEW_DAY_L] = dayL_sview_free,
	[SVIEW_DAY_H] = dayH_sview_free,
	[SVIEW_WEEK] = week_sview_free,
	[SVIEW_MONTH_L] = monthL_sview_free,
	[SVIEW_MONTH_H] = monthH_sview_free,
	[SVIEW_YEAR0] = year0_sview_free,
	[SVIEW_YEAR1] = year1_sview_free,
	[SVIEW_YEAR2] = year2_sview_free,
	[SVIEW_YEAR3] = year3_sview_free,
	[SVIEW_ROTATE_PIC] = rotate_pic_sview_free,
	[SVIEW_ANALOG_TIME] = analog_time_sview_free,
	[SVIEW_ANALOG_SECOND] = analog_second_sview_free,
	[SVIEW_ANALOG_MINUTE] = analog_minute_sview_free,
	[SVIEW_ANALOG_HOUR] = analog_hour_sview_free,
	[SVIEW_ANALOG_DAY] = analog_day_sview_free,
	[SVIEW_ANALOG_WEEK] = analog_week_sview_free,
	[SVIEW_ANALOG_MONTH] = analog_month_sview_free,
	[SVIEW_ANALOG_AM_PM] = analog_am_pm_sview_free,
	[SVIEW_ANALOG_HOUR_WITH_MINUTE] = analog_hour_with_minute_sview_free,
};

/**
 * register_slpt() : register view to slpt res
 */
#ifdef CONFIG_SLPT
struct slpt_app_res *(*sview_method_register_slpt[SVIEW_NUMS])(struct sview *view, struct slpt_app_res *parent) = {
	[SVIEW_PIC] = slpt_register_pic_sview,
	[SVIEW_NUM] = slpt_register_num_sview,
	[SVIEW_LINEAR_LAYOUT] = slpt_register_linear_layout,
	[SVIEW_ABSOLUTE_LAYOUT] = slpt_register_absolute_layout,
	[SVIEW_FRAME_LAYOUT] = slpt_register_frame_layout,
	[SVIEW_TIME_NUM] = slpt_register_time_num_sview,
	[SVIEW_SECOND_L] = slpt_register_secondL_sview,
	[SVIEW_SECOND_H] = slpt_register_secondH_sview,
	[SVIEW_MINUTE_L] = slpt_register_minuteL_sview,
	[SVIEW_MINUTE_H] = slpt_register_minuteH_sview,
	[SVIEW_HOUR_L] = slpt_register_hourL_sview,
	[SVIEW_HOUR_H] = slpt_register_hourH_sview,
	[SVIEW_DAY_L] = slpt_register_dayL_sview,
	[SVIEW_DAY_H] = slpt_register_dayH_sview,
	[SVIEW_WEEK] = slpt_register_week_sview,
	[SVIEW_MONTH_L] = slpt_register_monthL_sview,
	[SVIEW_MONTH_H] = slpt_register_monthH_sview,
	[SVIEW_YEAR0] = slpt_register_year0_sview,
	[SVIEW_YEAR1] = slpt_register_year1_sview,
	[SVIEW_YEAR2] = slpt_register_year2_sview,
	[SVIEW_YEAR3] = slpt_register_year3_sview,
	[SVIEW_ROTATE_PIC] = slpt_register_rotate_pic_sview,
	[SVIEW_ANALOG_TIME] = slpt_register_analog_time_sview,
	[SVIEW_ANALOG_SECOND] = slpt_register_analog_second_sview,
	[SVIEW_ANALOG_MINUTE] = slpt_register_analog_minute_sview,
	[SVIEW_ANALOG_HOUR] = slpt_register_analog_hour_sview,
	[SVIEW_ANALOG_DAY] = slpt_register_analog_day_sview,
	[SVIEW_ANALOG_WEEK] = slpt_register_analog_week_sview,
	[SVIEW_ANALOG_MONTH] = slpt_register_analog_month_sview,
	[SVIEW_ANALOG_AM_PM] = slpt_register_analog_am_pm_sview,
	[SVIEW_ANALOG_HOUR_WITH_MINUTE] = slpt_register_analog_hour_with_minute_sview,
};
#endif
