#include <view.h>
#include <digital_clock.h>
#include <analog_clock.h>
#include <analog_week_clock.h>
#include <analog_month_clock.h>
#include <analog_second_clock.h>
#include <analog_minute_clock.h>
#include <analog_hour_clock.h>
#include <slpt_file.h>

extern int debug_slpt_view_sync;

#undef pr_debug
#define pr_debug(x...)                          \
	do {                                        \
		if (debug_slpt_view_sync)               \
			pr_info(x);                         \
	} while (0)

static int write_view_common(struct view *view_commond);
static int write_num_view(struct num_view *numv_rcv);
static int write_flash_pic_view(struct flash_pic_view *picv_rcv);
static int write_pic_view(struct pic_view *picv_rcv);
static int write_text_view(struct text_view *textv_rcv);
static int write_rotate_pic(struct rotate_pic *rotate_picv_rcv);

void slpt_write_num_view(struct view *view)
{
	struct num_view *nv = to_num_view(view);

	write_num_view(nv);

	pr_debug("%s ---> numview\n", view_name(view));
}

void slpt_write_flash_pic_view(struct view *view)
{
	struct flash_pic_view *fpv = to_flash_pic_view(view);

	write_flash_pic_view(fpv);

	pr_debug("%s ---> flash-pic-view\n", view_name(view));
}

void slpt_write_pic_view(struct view *view)
{
	struct pic_view *pv = to_pic_view(view);

	write_pic_view(pv);

	pr_debug("%s ---> picview\n", view_name(view));
}

void slpt_write_text_view(struct view *view)
{
	struct text_view *text = to_text_view(view);

	write_text_view(text);

	pr_debug("%s ---> textview\n", view_name(view));

	slpt_write_view_grp(&view->grp);
}

void slpt_write_digital_clock_en_view(struct view *view)
{
	pr_debug("%s ---> digital-clock-en-view\n", view_name(view));

	slpt_write_view_grp(&view->grp);
}

void slpt_write_digital_clock_cn_view(struct view *view)
{
	pr_debug("%s ---> digital-clock-cn-view\n", view_name(view));

	slpt_write_view_grp(&view->grp);
}

void slpt_write_date_cn_view(struct view *view)
{
	pr_debug("%s ---> date-cn-view\n", view_name(view));

	slpt_write_view_grp(&view->grp);
}

void slpt_write_date_en_view(struct view *view)
{
	pr_debug("%s ---> date-en-view\n", view_name(view));

	slpt_write_view_grp(&view->grp);
}

void slpt_write_week_en_view(struct view *view)
{
	pr_debug("%s ---> week-en-view\n", view_name(view));

	slpt_write_view_grp(&view->grp);
}

void slpt_write_week_cn_view(struct view *view)
{
	pr_debug("%s ---> week-cn-view\n", view_name(view));

	slpt_write_view_grp(&view->grp);
}

void slpt_write_year_en_view(struct view *view)
{
	pr_debug("%s ---> year-en-view\n", view_name(view));

	slpt_write_view_grp(&view->grp);
}

void slpt_write_time_view(struct view *view)
{
	pr_debug("%s ---> time-view\n", view_name(view));

	slpt_write_view_grp(&view->grp);
}

void slpt_write_rotate_pic(struct rotate_pic *rpic)
{
	if(chdir(rpic->name) != 0) {
		pr_err("Couldn`t change (%s) diretory!", rpic->name);
		return ;
	}

	pr_debug("%s ---> rotate-pic\n", rpic->name);

	write_rotate_pic(rpic);

	chdir("..");
}

void slpt_write_analog_clock_view(struct view *view)
{
	struct analog_clock *clock = to_analog_clock(view);
	unsigned int i;

	pr_debug("%s ---> analog-clock-view\n", view_name(view));

	for (i = 0; i < ARRAY_SIZE(clock->handers); ++i) {
		slpt_write_rotate_pic(&clock->handers[i]);
	}
}

void slpt_write_analog_base_clock_view(struct analog_base_clock *clock)
{
	unsigned int i;

	slpt_write_view(&clock->bkg.view); /* write the background */

	for (i = 0; i < ARRAY_SIZE(clock->handers); ++i) {
		slpt_write_rotate_pic(&clock->handers[i]);
	}
}

void slpt_write_analog_week_clock_view(struct view *view)
{
	struct analog_base_clock *clock = to_analog_base_clock(view);

	struct analog_week_clock *week_clock = to_analog_week_clock(clock);
	unsigned int i;

	pr_debug("%s ---> analog-week-clock-view\n", view_name(view));

	slpt_write_view(&week_clock->clock.bkg.view); /* write the background */

	for (i = 0; i < ARRAY_SIZE(week_clock->clock.handers); ++i) {
		slpt_write_rotate_pic(&week_clock->clock.handers[i]);
	}
}

void slpt_write_analog_month_clock_view(struct view *view)
{
	struct analog_base_clock *clock = to_analog_base_clock(view);

	struct analog_month_clock *month_clock = to_analog_month_clock(clock);
	unsigned int i;

	pr_debug("%s ---> analog-month-clock-view\n", view_name(view));

	slpt_write_view(&month_clock->clock.bkg.view); /* write the background */

	for (i = 0; i < ARRAY_SIZE(month_clock->clock.handers); ++i) {
		slpt_write_rotate_pic(&month_clock->clock.handers[i]);
	}
}

void slpt_write_analog_second_clock_view(struct view *view)
{
	struct analog_base_clock *clock = to_analog_base_clock(view);

	struct analog_second_clock *second_clock = to_analog_second_clock(clock);
	unsigned int i;

	pr_debug("%s ---> analog-second-clock-view\n", view_name(view));

	slpt_write_view(&second_clock->clock.bkg.view); /* write the background */

	for (i = 0; i < ARRAY_SIZE(second_clock->clock.handers); ++i) {
		slpt_write_rotate_pic(&second_clock->clock.handers[i]);
	}
}

void slpt_write_analog_minute_clock_view(struct view *view)
{
	struct analog_base_clock *clock = to_analog_base_clock(view);

	struct analog_minute_clock *minute_clock = to_analog_minute_clock(clock);
	unsigned int i;

	pr_debug("%s ---> analog-minute-clock-view\n", view_name(view));

	slpt_write_view(&minute_clock->clock.bkg.view); /* write the background */

	for (i = 0; i < ARRAY_SIZE(minute_clock->clock.handers); ++i) {
		slpt_write_rotate_pic(&minute_clock->clock.handers[i]);
	}
}

void slpt_write_analog_hour_clock_view(struct view *view)
{
	struct analog_base_clock *clock = to_analog_base_clock(view);

	struct analog_hour_clock *hour_clock = to_analog_hour_clock(clock);
	unsigned int i;

	pr_debug("%s ---> analog-hour-clock-view\n", view_name(view));

	slpt_write_view(&hour_clock->clock.bkg.view); /* write the background */

	for (i = 0; i < ARRAY_SIZE(hour_clock->clock.handers); ++i) {
		slpt_write_rotate_pic(&hour_clock->clock.handers[i]);
	}
}

void slpt_write_view(struct view *view)
{

	if(chdir(view_name(view)) != 0) {
		pr_err("Couldn`t change (%s) diretory!", view_name(view));
		return ;
	}

	write_view_common(view);

	switch (view_type(view)) {
		case VIEW_NUM: slpt_write_num_view(view); break;
		case VIEW_FLASH_PIC: slpt_write_flash_pic_view(view); break;
		case VIEW_PIC: slpt_write_pic_view(view); break;
		case VIEW_TEXT: slpt_write_text_view(view); break;

		case VIEW_DIGITAL_CLOCK_EN: slpt_write_digital_clock_en_view(view); break;
		case VIEW_DIGITAL_CLOCK_CN: slpt_write_digital_clock_cn_view(view); break;
		case VIEW_ANALOG_CLOCK: slpt_write_analog_clock_view(view); break;
		case VIEW_ANALOG_WEEK_CLOCK: slpt_write_analog_week_clock_view(view); break;
		case VIEW_ANALOG_MONTH_CLOCK: slpt_write_analog_month_clock_view(view); break;
		case VIEW_ANALOG_SECOND_CLOCK: slpt_write_analog_second_clock_view(view); break;
		case VIEW_ANALOG_MINUTE_CLOCK: slpt_write_analog_minute_clock_view(view); break;
		case VIEW_ANALOG_HOUR_CLOCK: slpt_write_analog_hour_clock_view(view); break;

		case VIEW_DATE_EN: slpt_write_date_en_view(view); break;
		case VIEW_DATE_CN: slpt_write_date_cn_view(view); break;
		case VIEW_WEEK_EN: slpt_write_week_en_view(view); break;
		case VIEW_WEEK_CN: slpt_write_week_cn_view(view); break;
		case VIEW_YEAR_EN: slpt_write_year_en_view(view); break;
		case VIEW_TIME:    slpt_write_time_view(view);    break;

		default:
			pr_debug("Your type number is (%d), maybe wrong, match nothing\n", view_type(view));

	}

	chdir("..");
}

static void slpt_file_init(struct slpt_file *file, const char *fn, void *buf, \
							unsigned int size) {
	memset(file, 0, sizeof(*file));

	file->fn = fn;
	file->buf = buf;
	file->size = size;
}

int slpt_write_file_to_mem(const char *fn, void *buf)
{
	int ret;
	struct slpt_file file;
	unsigned int size = sizeof(unsigned int);

	slpt_file_init(&file, fn, buf, size);

	ret = slpt_write_file(&file);
	if (ret) {
		pr_err("slpt: failed to write view: %s\n", file.fn);
		return ret;
	}

	return 0;
}

int slpt_write_file_to_mem_char(const char *fn, void *buf)
{
	int ret;
	struct slpt_file file;
	unsigned int size = strlen(buf) + 1;

	slpt_file_init(&file, fn, buf, size);

	ret = slpt_write_file(&file);
	if (ret) {
		pr_err("slpt: failed to write view: %s\n", file.fn);
		return ret;
	}

	return 0;
}

/* write the common of all the view : "show" "start-x" "start-y" ... */
static int write_view_common(struct view *view_set)
{
	int ret = 0, err = 0;

	ret = slpt_write_file_to_mem("show", &view_set->show);
	err += ret ? 1 : 0;

	ret = slpt_write_file_to_mem("start-x", &view_set->start.x);
	err += ret ? 1 : 0;

	ret = slpt_write_file_to_mem("start-y", &view_set->start.y);
	err += ret ? 1 : 0;

	ret = slpt_write_file_to_mem("follow-mode", &view_set->follow_mode);
	err += ret ? 1 : 0;

	ret = slpt_write_file_to_mem("center-hor", &view_set->center_hor);
	err += ret ? 1 : 0;

	ret = slpt_write_file_to_mem("center-ver", &view_set->center_ver);
	err += ret ? 1 : 0;

	ret = slpt_write_file_to_mem("replace-mode", &view_set->replace_mode);
	err += ret ? 1 : 0;

	ret = slpt_write_file_to_mem("replace-color", &view_set->replace_color);
	err += ret ? 1 : 0;

	ret = slpt_write_file_to_mem("alpha_mode", &view_set->alpha_mode);
	err += ret ? 1 : 0;

	pr_info("pic view: [%s]  (%d, %x)\n",
			 view_name(view_set), view_is_replace(view_set), view_replace_color(view_set));

	return err;
}

/* write "num" "pic_grp" and the common */
static int write_num_view(struct num_view *numv_rcv)
{
	int ret = 0, err = 0;

	ret = slpt_write_file_to_mem("num", &numv_rcv->num);
	err += ret ? 1 : 0;

	ret = slpt_write_file_to_mem_char("pic_grp", numv_rcv->grp_name);
	err += ret ? 1 : 0;

	return err;
}

/* write "picture" and the common */
static int write_flash_pic_view(struct flash_pic_view *fpicv_rcv)
{
	int ret = 0, err = 0;

	ret = slpt_write_file_to_mem_char("picture", fpicv_rcv->pic_name);
	err += ret ? 1 : 0;

	ret = slpt_write_file_to_mem("flash_mode", &fpicv_rcv->flash_mode);
	err += ret ? 1 : 0;

	ret = slpt_write_file_to_mem("display", &fpicv_rcv->display);
	err += ret ? 1 : 0;

	return err;
}

/* write "picture" and the common */
static int write_pic_view(struct pic_view *picv_rcv)
{
	int ret = 0, err = 0;

	ret = slpt_write_file_to_mem_char("picture", picv_rcv->pic_name);
	err += ret ? 1 : 0;

	return err;
}

/* not support now */
static int write_text_view(struct text_view *textv_rcv)
{
	return 0;
}

/* write "show" "center-x" "center-y" "picture" and the common */
static int write_rotate_pic(struct rotate_pic *rotate_picv_rcv)
{
	int ret = 0, err = 0;

	pr_info("write: rpic: [%d]  (%d, %d) [%s] \n",
			rotate_picv_rcv->show,
			rotate_picv_rcv->center.x,
			rotate_picv_rcv->center.y,
			rotate_picv_rcv->pic_name);

	ret = slpt_write_file_to_mem("show", &rotate_picv_rcv->show);
	err += ret ? 1 : 0;

	ret = slpt_write_file_to_mem("center-x", &rotate_picv_rcv->center.x);
	err += ret ? 1 : 0;

	ret = slpt_write_file_to_mem("center-y", &rotate_picv_rcv->center.y);
	err += ret ? 1 : 0;

	ret = slpt_write_file_to_mem_char("picture", rotate_picv_rcv->pic_name);
	err += ret ? 1 : 0;

	return err;
}
