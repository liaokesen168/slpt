#include <common.h>
#include <sview/sview.h>
#include <sview/sview_utils.h>
#include <key_writer.h>

static void write_sview(struct sview *view, struct key_writer *writer);

static void write_layout_to_key_writer(struct list_head *grp, struct key_writer *writer) {
	struct list_head *pos;

	kw_write_uint(writer, sview_grp_size(grp));
	list_for_each(pos, grp) {
		struct sview *v = list_entry(pos, struct sview, link);
		write_sview(v, writer);
	}
}

static void write_linear_layout_to_key_writer(struct sview *view, struct key_writer *writer) {
	struct linear_layout *data = to_linear_layout(view);

	write_layout_to_key_writer(&view->grp, writer);
	kw_write_uchar(writer, data->orientation);
}

static void write_frame_layout_to_key_writer(struct sview *view, struct key_writer *writer) {
	write_layout_to_key_writer(&view->grp, writer);
}

static void write_absolute_layout_to_key_writer(struct sview *view, struct key_writer *writer) {
	struct absolute_layout *data = to_absolute_layout(view);

	write_layout_to_key_writer(&view->grp, writer);
	kw_write_uchar(writer, data->position_of_x_start);
	kw_write_uchar(writer, data->position_of_y_start);
}

static void write_pic_sview_to_key_writer(struct sview *view, struct key_writer *writer) {
	struct pic_sview *data = to_pic_sview(view);

	kw_write_string(writer, data->pic_name);
}

static void write_num_sview_to_key_writer(struct sview *view, struct key_writer *writer) {
	struct num_sview *data = to_num_sview(view);

	kw_write_uint(writer, data->num);
	kw_write_string(writer, data->grp_name);
}

#define write_time_num_sview_to_key_writer write_num_sview_to_key_writer
#define write_secondL_sview_to_key_writer  write_time_num_sview_to_key_writer
#define write_secondH_sview_to_key_writer  write_time_num_sview_to_key_writer
#define write_minuteL_sview_to_key_writer  write_time_num_sview_to_key_writer
#define write_minuteH_sview_to_key_writer  write_time_num_sview_to_key_writer
#define write_hourL_sview_to_key_writer    write_time_num_sview_to_key_writer
#define write_hourH_sview_to_key_writer    write_time_num_sview_to_key_writer
#define write_dayL_sview_to_key_writer     write_time_num_sview_to_key_writer
#define write_dayH_sview_to_key_writer     write_time_num_sview_to_key_writer
#define write_week_sview_to_key_writer     write_time_num_sview_to_key_writer
#define write_monthL_sview_to_key_writer   write_time_num_sview_to_key_writer
#define write_monthH_sview_to_key_writer   write_time_num_sview_to_key_writer
#define write_year0_sview_to_key_writer    write_time_num_sview_to_key_writer
#define write_year1_sview_to_key_writer    write_time_num_sview_to_key_writer
#define write_year2_sview_to_key_writer    write_time_num_sview_to_key_writer
#define write_year3_sview_to_key_writer    write_time_num_sview_to_key_writer

#define write_rotate_pic_sview_to_key_writer    write_pic_sview_to_key_writer
#define write_analog_time_sview_to_key_writer   write_rotate_pic_sview_to_key_writer
#define write_analog_second_sview_to_key_writer write_analog_time_sview_to_key_writer
#define write_analog_minute_sview_to_key_writer write_analog_time_sview_to_key_writer
#define write_analog_hour_sview_to_key_writer   write_analog_time_sview_to_key_writer
#define write_analog_day_sview_to_key_writer    write_analog_time_sview_to_key_writer
#define write_analog_week_sview_to_key_writer   write_analog_time_sview_to_key_writer
#define write_analog_month_sview_to_key_writer  write_analog_time_sview_to_key_writer
#define write_analog_am_pm_sview_to_key_writer  write_analog_time_sview_to_key_writer
#define write_analog_hour_with_minute_sview_to_key_writer write_analog_time_sview_to_key_writer

static void (*sview_method_write_to_key_writer[SVIEW_NUMS]) (struct sview *view, struct key_writer *writer) = {
	[SVIEW_PIC] = write_pic_sview_to_key_writer,
	[SVIEW_NUM] = write_num_sview_to_key_writer,
	[SVIEW_LINEAR_LAYOUT] = write_linear_layout_to_key_writer,
	[SVIEW_ABSOLUTE_LAYOUT] = write_absolute_layout_to_key_writer,
	[SVIEW_FRAME_LAYOUT] = write_frame_layout_to_key_writer,
	[SVIEW_TIME_NUM] = write_time_num_sview_to_key_writer,
	[SVIEW_SECOND_L] = write_secondL_sview_to_key_writer,
	[SVIEW_SECOND_H] = write_secondH_sview_to_key_writer,
	[SVIEW_MINUTE_L] = write_minuteL_sview_to_key_writer,
	[SVIEW_MINUTE_H] = write_minuteH_sview_to_key_writer,
	[SVIEW_HOUR_L] = write_hourL_sview_to_key_writer,
	[SVIEW_HOUR_H] = write_hourH_sview_to_key_writer,
	[SVIEW_DAY_L] = write_dayL_sview_to_key_writer,
	[SVIEW_DAY_H] = write_dayH_sview_to_key_writer,
	[SVIEW_WEEK] = write_week_sview_to_key_writer,
	[SVIEW_MONTH_L] = write_monthL_sview_to_key_writer,
	[SVIEW_MONTH_H] = write_monthH_sview_to_key_writer,
	[SVIEW_YEAR0] = write_year0_sview_to_key_writer,
	[SVIEW_YEAR1] = write_year1_sview_to_key_writer,
	[SVIEW_YEAR2] = write_year2_sview_to_key_writer,
	[SVIEW_YEAR3] = write_year3_sview_to_key_writer,
	[SVIEW_ROTATE_PIC] = write_rotate_pic_sview_to_key_writer,
	[SVIEW_ANALOG_TIME] = write_analog_time_sview_to_key_writer,
	[SVIEW_ANALOG_SECOND] = write_analog_second_sview_to_key_writer,
	[SVIEW_ANALOG_MINUTE] = write_analog_minute_sview_to_key_writer,
	[SVIEW_ANALOG_HOUR] = write_analog_hour_sview_to_key_writer,
	[SVIEW_ANALOG_DAY] = write_analog_day_sview_to_key_writer,
	[SVIEW_ANALOG_WEEK] = write_analog_week_sview_to_key_writer,
	[SVIEW_ANALOG_MONTH] = write_analog_month_sview_to_key_writer,
	[SVIEW_ANALOG_AM_PM] = write_analog_am_pm_sview_to_key_writer,
	[SVIEW_ANALOG_HOUR_WITH_MINUTE] = write_analog_hour_with_minute_sview_to_key_writer,
};

static void write_sview(struct sview *view, struct key_writer *writer) {
	assert(view->type < SVIEW_NUMS);

	kw_write_ushort(writer, view->type);
	kw_write_int(writer, view->raw_position.x);
	kw_write_int(writer, view->raw_position.y);
	kw_write_ushort(writer, view->padding.left);
	kw_write_ushort(writer, view->padding.right);
	kw_write_ushort(writer, view->padding.top);
	kw_write_ushort(writer, view->padding.bottom);
	kw_write_uint(writer, view->rect.w);
	kw_write_uint(writer, view->rect.h);
	kw_write_uint(writer, view->background.color);
	kw_write_string(writer, view->background.pic_name);
	kw_write_ushort(writer, view->level);
	kw_write_uchar(writer, view->align_x);
	kw_write_uchar(writer, view->align_y);
	kw_write_uchar(writer, view->desc_w);
	kw_write_uchar(writer, view->desc_h);
	kw_write_uchar(writer, view->center_horizontal);
	kw_write_uchar(writer, view->center_vertical);
	kw_write_uchar(writer, view->align_parent_x);
	kw_write_uchar(writer, view->align_parent_y);
	kw_write_uchar(writer, view->show);

	sview_method_write_to_key_writer[view->type](view, writer);
}

struct key_writer *write_sview_to_key_writer(struct sview *view) {
	struct key_writer *writer;

	writer = alloc_key_writer(1024);

	if (view != NULL)
		write_sview(view, writer);

	return writer;
}
