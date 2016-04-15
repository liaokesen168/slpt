#include <common.h>
#include <sview/sview.h>
#include <sview/sview_utils.h>
#include <key_reader.h>

#define MSG_return_if(condition, msg)\
	do { \
		int _ret_val_i = (condition); \
		if (_ret_val_i) { \
			pr_err("%s [%s] %d key read error!\n", __func__, (msg), __LINE__); \
			return (_ret_val_i); \
		} \
	} while (0)

#define MSG_goto_if(condition, msg, lable) \
	do { \
		if (condition) { \
			pr_err("%s [%s] %d key read error!\n", __func__, (msg), __LINE__); \
			goto lable; \
		} \
	} while (0)

#define MSG_go_out_if(condition, msg) MSG_goto_if(condition, msg, out)
#define MSG_go_free_if(condition, msg) MSG_goto_if(condition, msg, free_sview)

static struct sview *create_sview(struct key_reader *reader);

static int init_layout_by_key_reader(struct list_head *grp, struct key_reader *reader) {
	struct sview *view;
	unsigned int i, N;

	MSG_return_if(kr_read_uint(reader, &N), "child_nums");
	for (i = 0; i < N; ++i) {
		MSG_go_out_if(!(view = create_sview(reader)), "create_sview");
		sview_grp_add_by_level(grp, view);
	}

	return 0;
out:
	return -1;
}

static int init_linear_layout_by_key_reader(struct sview *view, struct key_reader *reader) {
	struct linear_layout *data = to_linear_layout(view);

	MSG_return_if(init_layout_by_key_reader(&view->grp, reader), "layout");
	MSG_return_if(kr_read_uchar(reader, &data->orientation), "orientation");

	return 0;
}

static int init_frame_layout_by_key_reader(struct sview *view, struct key_reader *reader) {
	MSG_return_if(init_layout_by_key_reader(&view->grp, reader), "layout");

	return 0;
}

static int init_absolute_layout_by_key_reader(struct sview *view, struct key_reader *reader) {
	struct absolute_layout *data = to_absolute_layout(view);

	MSG_return_if(init_layout_by_key_reader(&view->grp, reader), "layout");
	MSG_return_if(kr_read_uchar(reader, &data->position_of_x_start), "position of x");
	MSG_return_if(kr_read_uchar(reader, &data->position_of_y_start), "position of y");

	return 0;
}

static int init_pic_sview_by_key_reader(struct sview *view, struct key_reader *reader) {
	struct pic_sview *data = to_pic_sview(view);

	MSG_return_if(kr_read_string(reader, data->pic_name, sizeof(data->pic_name)) < 0, "pic_name");

	return 0;
}

static int init_num_sview_by_key_reader(struct sview *view, struct key_reader *reader) {
	struct num_sview *data = to_num_sview(view);

	MSG_return_if(kr_read_uint(reader, &data->num), "num");
	MSG_return_if(kr_read_string(reader, data->grp_name, sizeof(data->grp_name)) < 0, "grp_name");

	return 0;
}

#define init_time_num_sview_by_key_reader init_num_sview_by_key_reader
#define init_secondL_sview_by_key_reader  init_time_num_sview_by_key_reader
#define init_secondH_sview_by_key_reader  init_time_num_sview_by_key_reader
#define init_minuteL_sview_by_key_reader  init_time_num_sview_by_key_reader
#define init_minuteH_sview_by_key_reader  init_time_num_sview_by_key_reader
#define init_hourL_sview_by_key_reader    init_time_num_sview_by_key_reader
#define init_hourH_sview_by_key_reader    init_time_num_sview_by_key_reader
#define init_dayL_sview_by_key_reader     init_time_num_sview_by_key_reader
#define init_dayH_sview_by_key_reader     init_time_num_sview_by_key_reader
#define init_week_sview_by_key_reader     init_time_num_sview_by_key_reader
#define init_monthL_sview_by_key_reader   init_time_num_sview_by_key_reader
#define init_monthH_sview_by_key_reader   init_time_num_sview_by_key_reader
#define init_year0_sview_by_key_reader    init_time_num_sview_by_key_reader
#define init_year1_sview_by_key_reader    init_time_num_sview_by_key_reader
#define init_year2_sview_by_key_reader    init_time_num_sview_by_key_reader
#define init_year3_sview_by_key_reader    init_time_num_sview_by_key_reader

#define init_rotate_pic_sview_by_key_reader    init_pic_sview_by_key_reader
#define init_analog_time_sview_by_key_reader   init_rotate_pic_sview_by_key_reader
#define init_analog_second_sview_by_key_reader init_analog_time_sview_by_key_reader
#define init_analog_minute_sview_by_key_reader init_analog_time_sview_by_key_reader
#define init_analog_hour_sview_by_key_reader   init_analog_time_sview_by_key_reader
#define init_analog_day_sview_by_key_reader    init_analog_time_sview_by_key_reader
#define init_analog_week_sview_by_key_reader   init_analog_time_sview_by_key_reader
#define init_analog_month_sview_by_key_reader  init_analog_time_sview_by_key_reader
#define init_analog_am_pm_sview_by_key_reader  init_analog_time_sview_by_key_reader
#define init_analog_hour_with_minute_sview_by_key_reader init_analog_time_sview_by_key_reader

static int (*sview_method_init_by_key_reader[SVIEW_NUMS]) (struct sview *view, struct key_reader *reader) = {
	[SVIEW_PIC] = init_pic_sview_by_key_reader,
	[SVIEW_NUM] = init_num_sview_by_key_reader,
	[SVIEW_LINEAR_LAYOUT] = init_linear_layout_by_key_reader,
	[SVIEW_ABSOLUTE_LAYOUT] = init_absolute_layout_by_key_reader,
	[SVIEW_FRAME_LAYOUT] = init_frame_layout_by_key_reader,
	[SVIEW_TIME_NUM] = init_time_num_sview_by_key_reader,
	[SVIEW_SECOND_L] = init_secondL_sview_by_key_reader,
	[SVIEW_SECOND_H] = init_secondH_sview_by_key_reader,
	[SVIEW_MINUTE_L] = init_minuteL_sview_by_key_reader,
	[SVIEW_MINUTE_H] = init_minuteH_sview_by_key_reader,
	[SVIEW_HOUR_L] = init_hourL_sview_by_key_reader,
	[SVIEW_HOUR_H] = init_hourH_sview_by_key_reader,
	[SVIEW_DAY_L] = init_dayL_sview_by_key_reader,
	[SVIEW_DAY_H] = init_dayH_sview_by_key_reader,
	[SVIEW_WEEK] = init_week_sview_by_key_reader,
	[SVIEW_MONTH_L] = init_monthL_sview_by_key_reader,
	[SVIEW_MONTH_H] = init_monthH_sview_by_key_reader,
	[SVIEW_YEAR0] = init_year0_sview_by_key_reader,
	[SVIEW_YEAR1] = init_year1_sview_by_key_reader,
	[SVIEW_YEAR2] = init_year2_sview_by_key_reader,
	[SVIEW_YEAR3] = init_year3_sview_by_key_reader,
	[SVIEW_ROTATE_PIC] = init_rotate_pic_sview_by_key_reader,
	[SVIEW_ANALOG_TIME] = init_analog_time_sview_by_key_reader,
	[SVIEW_ANALOG_SECOND] = init_analog_second_sview_by_key_reader,
	[SVIEW_ANALOG_MINUTE] = init_analog_minute_sview_by_key_reader,
	[SVIEW_ANALOG_HOUR] = init_analog_hour_sview_by_key_reader,
	[SVIEW_ANALOG_DAY] = init_analog_day_sview_by_key_reader,
	[SVIEW_ANALOG_WEEK] = init_analog_week_sview_by_key_reader,
	[SVIEW_ANALOG_MONTH] = init_analog_month_sview_by_key_reader,
	[SVIEW_ANALOG_AM_PM] = init_analog_am_pm_sview_by_key_reader,
	[SVIEW_ANALOG_HOUR_WITH_MINUTE] = init_analog_hour_with_minute_sview_by_key_reader,
};

static struct sview *create_sview(struct key_reader *reader) {
	unsigned short type;
	struct sview *view;

	MSG_go_out_if(kr_read_ushort(reader, &type), "type");
	MSG_go_out_if(type >= SVIEW_NUMS, "invalid type");
	pr_debug("read : %s\n", sview_type_to_short_string(type));
	MSG_go_out_if(!(view = alloc_sview_by_type(sview_type_to_short_string(type), type)), "alloc sview");

	MSG_go_free_if(kr_read_int(reader, &view->raw_position.x), "x");
	MSG_go_free_if(kr_read_int(reader, &view->raw_position.y), "y");
	MSG_go_free_if(kr_read_ushort(reader, &view->padding.left), "left");
	MSG_go_free_if(kr_read_ushort(reader, &view->padding.right), "right");
	MSG_go_free_if(kr_read_ushort(reader, &view->padding.top), "top");
	MSG_go_free_if(kr_read_ushort(reader, &view->padding.bottom), "bottom");
	MSG_go_free_if(kr_read_uint(reader, &view->rect.w), "rect-w");
	MSG_go_free_if(kr_read_uint(reader, &view->rect.h), "rect-h");
	MSG_go_free_if(kr_read_uint(reader, &view->background.color), "color");
	pr_debug("read: 0x%x", view->background.color);
	MSG_go_free_if(kr_read_string(reader, view->background.pic_name, sizeof(view->background.pic_name)) < 0, "pic_name");
	MSG_go_free_if(kr_read_ushort(reader, &view->level), "level");
	MSG_go_free_if(kr_read_uchar(reader, &view->align_x), "align_x");
	MSG_go_free_if(kr_read_uchar(reader, &view->align_y), "align_y");
	MSG_go_free_if(kr_read_uchar(reader, &view->desc_w), "desc_w");
	MSG_go_free_if(kr_read_uchar(reader, &view->desc_h), "desc_h");
	MSG_go_free_if(kr_read_uchar(reader, &view->center_horizontal), "horizontal");
	MSG_go_free_if(kr_read_uchar(reader, &view->center_vertical), "vertical");
	MSG_go_free_if(kr_read_uchar(reader, &view->align_parent_x), "align_parent_x");
	MSG_go_free_if(kr_read_uchar(reader, &view->align_parent_y), "align_parent_y");
	MSG_go_free_if(kr_read_uchar(reader, &view->show), "show");

	MSG_go_free_if(sview_method_init_by_key_reader[type](view, reader), "init_sview");

	return view;
free_sview:
	sview_free(view);
out:
	return NULL;
}

struct sview *create_sview_from_key_reader(struct key_reader *reader) {
	return create_sview(reader);
}
