#include <view.h>
#include <digital_clock.h>
#include <analog_clock.h>
#include <analog_week_clock.h>
#include <analog_month_clock.h>
#include <analog_second_clock.h>
#include <analog_minute_clock.h>
#include <analog_hour_clock.h>
#include <slpt_file.h>

#define FB_REGION_DIR "/sys/slpt/apps/slpt-app/res/setting/region/"

#define DEBUG

#ifdef DEBUG
int debug_slpt_view_sync = 1;
#else
int debug_slpt_view_sync = 0;
#endif

#undef pr_debug
#define pr_debug(x...)                          \
	do {                                        \
		if (debug_slpt_view_sync)               \
			pr_info(x);                         \
	} while (0)

static int load_view_common(struct view *view_commond);
static int load_num_view(struct num_view *numv_rcv);
static int load_flash_pic_view(struct flash_pic_view *picv_rcv);
static int load_pic_view(struct pic_view *picv_rcv);
static int load_text_view(struct text_view *textv_rcv);
static int load_rotate_pic(struct rotate_pic *rotate_picv_rcv);
static int load_fb_region(struct fb_region *region_rcv);

void slpt_load_num_view(struct view *view)
{
	struct num_view *nv = to_num_view(view);

	load_num_view(nv);

	pr_debug("%s ---> numview\n", view_name(view));
}

void slpt_load_flash_pic_view(struct view *view)
{
	struct flash_pic_view *fpv = to_flash_pic_view(view);

	load_flash_pic_view(fpv);

	pr_debug("%s ---> flash-pic-view\n", view_name(view));
}

void slpt_load_pic_view(struct view *view)
{
	struct pic_view *pv = to_pic_view(view);

	load_pic_view(pv);

	pr_debug("%s ---> picview\n", view_name(view));
}

void slpt_load_text_view(struct view *view)
{
	struct text_view *text = to_text_view(view);

	load_text_view(text);

	pr_debug("%s ---> textview\n", view_name(view));

	slpt_load_view_grp(&view->grp);
}

void slpt_load_date_en_view(struct view *view)
{
	pr_debug("%s ---> date-en-view\n", view_name(view));

	slpt_load_view_grp(&view->grp);
}

void slpt_load_date_cn_view(struct view *view)
{
	pr_debug("%s ---> date-cn-view\n", view_name(view));

	slpt_load_view_grp(&view->grp);
}

void slpt_load_week_en_view(struct view *view)
{
	pr_debug("%s ---> week-en-view\n", view_name(view));

	slpt_load_view_grp(&view->grp);
}

void slpt_load_week_cn_view(struct view *view)
{
	pr_debug("%s ---> week-cn-view\n", view_name(view));

	slpt_load_view_grp(&view->grp);
}


void slpt_load_year_en_view(struct view *view)
{
	pr_debug("%s ---> year-en-view\n", view_name(view));

	slpt_load_view_grp(&view->grp);
}

void slpt_load_time_view(struct view *view)
{
	pr_debug("%s ---> time-view\n", view_name(view));

	slpt_load_view_grp(&view->grp);
}

void slpt_load_digital_clock_en_view(struct view *view)
{
	pr_debug("%s ---> digital-clock-en-view\n", view_name(view));

	slpt_load_view_grp(&view->grp);
}

void slpt_load_digital_clock_cn_view(struct view *view)
{
	pr_debug("%s ---> digital-clock-cn-view\n", view_name(view));

	slpt_load_view_grp(&view->grp);
}

void slpt_load_rotate_pic(struct rotate_pic *rpic)
{
	if(chdir(rpic->name) != 0) {
		pr_err("Couldn`t change (%s) diretory!", rpic->name);
		return ;
	}

	pr_debug("%s ---> rotate-pic\n", rpic->name);

	load_rotate_pic(rpic);

	chdir("..");
}

void slpt_load_analog_clock_view(struct view *view)
{
	struct analog_clock *clock = to_analog_clock(view);
	unsigned int i;

	pr_debug("%s ---> analog-clock-view\n", view_name(view));

	for (i = 0; i < ARRAY_SIZE(clock->handers); ++i) {
		slpt_load_rotate_pic(&clock->handers[i]);
	}
}

void slpt_load_analog_base_clock_view(struct analog_base_clock *clock)
{
	unsigned int i;

	slpt_load_view(&clock->bkg.view); /* load the background */

	for (i = 0; i < ARRAY_SIZE(clock->handers); ++i) {
		slpt_load_rotate_pic(&clock->handers[i]);
	}
}

void slpt_load_analog_week_clock_view(struct view *view)
{
	struct analog_base_clock *clock = to_analog_base_clock(view);
	struct analog_week_clock *week_clock = to_analog_week_clock(clock);

	pr_debug("%s ---> analog-week-clock-view\n", view_name(view));

	slpt_load_analog_base_clock_view(&week_clock->clock);
}

void slpt_load_analog_month_clock_view(struct view *view)
{
	struct analog_base_clock *clock = to_analog_base_clock(view);
	struct analog_month_clock *month_clock = to_analog_month_clock(clock);

	pr_debug("%s ---> analog-month-clock-view\n", view_name(view));

	slpt_load_analog_base_clock_view(&month_clock->clock);
}

void slpt_load_analog_second_clock_view(struct view *view)
{
	struct analog_base_clock *clock = to_analog_base_clock(view);
	struct analog_second_clock *second_clock = to_analog_second_clock(clock);

	pr_debug("%s ---> analog-second-clock-view\n", view_name(view));

	slpt_load_analog_base_clock_view(&second_clock->clock);
}

void slpt_load_analog_minute_clock_view(struct view *view)
{
	struct analog_base_clock *clock = to_analog_base_clock(view);
	struct analog_minute_clock *minute_clock = to_analog_minute_clock(clock);

	pr_debug("%s ---> analog-minute-clock-view\n", view_name(view));

	slpt_load_analog_base_clock_view(&minute_clock->clock);
}

void slpt_load_analog_hour_clock_view(struct view *view)
{
	struct analog_base_clock *clock = to_analog_base_clock(view);
	struct analog_hour_clock *hour_clock = to_analog_hour_clock(clock);

	pr_debug("%s ---> analog-hour-clock-view\n", view_name(view));

	slpt_load_analog_base_clock_view(&hour_clock->clock);
}

int slpt_load_fb_region(struct fb_region *region)
{
	int ret;
	char cur_dir[MAX_FILE_NAME] = SLPT_RES_ROOT;

	getcwd(cur_dir, MAX_FILE_NAME);

	if(chdir(FB_REGION_DIR) != 0) {
		pr_err("Couldn`t change (%s) diretory!", FB_REGION_DIR);
		return -1;
	}

	ret = load_fb_region(region);

	chdir(cur_dir);

	return ret;
}

void slpt_load_view(struct view *view)
{
	if(chdir(view_name(view)) != 0) {
		pr_err("Couldn`t change (%s) diretory!", view_name(view));
		return ;
	}
	load_view_common(view);

	switch (view_type(view)) {
		case VIEW_NUM: slpt_load_num_view(view); break;
		case VIEW_FLASH_PIC: slpt_load_flash_pic_view(view); break;
		case VIEW_PIC: slpt_load_pic_view(view); break;
		case VIEW_TEXT: slpt_load_text_view(view); break;

		case VIEW_DIGITAL_CLOCK_EN: slpt_load_digital_clock_en_view(view); break;
		case VIEW_DIGITAL_CLOCK_CN: slpt_load_digital_clock_cn_view(view); break;
		case VIEW_ANALOG_CLOCK: slpt_load_analog_clock_view(view); break;
		case VIEW_ANALOG_WEEK_CLOCK: slpt_load_analog_week_clock_view(view); break;
		case VIEW_ANALOG_MONTH_CLOCK: slpt_load_analog_month_clock_view(view); break;
		case VIEW_ANALOG_SECOND_CLOCK: slpt_load_analog_second_clock_view(view); break;
		case VIEW_ANALOG_MINUTE_CLOCK: slpt_load_analog_minute_clock_view(view); break;
		case VIEW_ANALOG_HOUR_CLOCK: slpt_load_analog_hour_clock_view(view); break;

		case VIEW_DATE_EN: slpt_load_date_en_view(view);break;
		case VIEW_DATE_CN: slpt_load_date_cn_view(view);break;
		case VIEW_WEEK_EN: slpt_load_week_en_view(view);break;
		case VIEW_WEEK_CN: slpt_load_week_cn_view(view);break;
		case VIEW_YEAR_EN: slpt_load_year_en_view(view); break;
		case VIEW_TIME: slpt_load_time_view(view);break;

		default:
			pr_debug("Your type number is (%d), maybe wrong, match nothing\n", view_type(view));
	}

	chdir("..");
}

void slpt_load_digital_clock_en(struct digital_clock_en *clock)
{
	pr_debug("%s ---> digital-clock-en\n", view_name(&clock->view));

	slpt_load_view(&clock->view);
}

void slpt_load_digital_clock_cn(struct digital_clock_cn *clock)
{
	pr_debug("%s ---> digital-clock-cn\n", view_name(&clock->view));

	slpt_load_view(&clock->view);
}

void slpt_load_analog_clock(struct analog_clock *clock)
{
	pr_debug("%s ---> analog-clock\n", clock->name);

	slpt_load_view(&clock->view);
}

int slpt_load_file_to_mem(const char *fn, void *buf, time_t *mtime)
{
	int ret;
	struct slpt_file file ;

	slpt_file_init_status(&file, fn, *mtime);

	ret = slpt_load_file(&file);
	if (ret) {
		pr_err("slpt: failed to load view: %s\n", file.fn);
		return ret;
	}

	if (*mtime != file.mtime) {
		memcpy(buf, file.buf, file.size);
		*mtime = file.mtime;
		free(file.buf);
	}

	return 0;
}

int slpt_load_file_to_mem_char(const char *fn, void *buf, time_t *mtime)
{
	int ret;
	struct slpt_file file ;

	slpt_file_init_status(&file, fn, *mtime);

	ret = slpt_load_file(&file);
	if (ret) {
		pr_err("slpt: failed to load view: %s\n", file.fn);
		return ret;
	}

	if (*mtime != file.mtime) {
		memcpy(buf, file.buf, file.size);
		strctoc(buf, '\n', '\0'); /* instread the first '\n' to '\0' */
		*mtime = file.mtime;
		free(file.buf);
	}

	return 0;
}

/* load the common of all the view : "show" "start-x" "start-y" */
static int load_view_common(struct view *view_commond)
{
	int ret = 0, err = 0;

	/* time[0] : show motime */
	ret = slpt_load_file_to_mem("show", &view_commond->show, &view_commond->time[0]);
	err += ret ? 1 : 0;

	/* time[1] :start-x motime */
	ret = slpt_load_file_to_mem("start-x", &view_commond->start.x, &view_commond->time[1]);
	err += ret ? 1 : 0;

	/* time[2] :start-y motime */
	ret = slpt_load_file_to_mem("start-y", &view_commond->start.y, &view_commond->time[2]);
	err += ret ? 1 : 0;

	/* time[3] : follow_mode motime */
	ret = slpt_load_file_to_mem("follow-mode", &view_commond->follow_mode, &view_commond->time[3]);
	err += ret ? 1 : 0;

	/* time[4] : center-hor motime */
	ret = slpt_load_file_to_mem("center-hor", &view_commond->center_hor, &view_commond->time[4]);
	err += ret ? 1 : 0;

	/* time[5] : center-ver motime */
	ret = slpt_load_file_to_mem("center-ver", &view_commond->center_ver, &view_commond->time[5]);
	err += ret ? 1 : 0;

	/* time[6] : replace_mode motime */
	ret = slpt_load_file_to_mem("replace-mode", &view_commond->replace_mode, &view_commond->time[6]);
	err += ret ? 1 : 0;

	/* time[7] : replace_color motime */
	ret = slpt_load_file_to_mem("replace-color", &view_commond->replace_color, &view_commond->time[7]);
	err += ret ? 1 : 0;

	/* time[8] : level motime */
	ret = slpt_load_file_to_mem("level", &view_commond->level, &view_commond->time[8]);
	err += ret ? 1 : 0;

	/* time[9] : alpha_mode motime */
	ret = slpt_load_file_to_mem("alpha_mode", &view_commond->alpha_mode, &view_commond->time[9]);
	err += ret ? 1 : 0;

	pr_debug(" view_name: [%s]  (%d, %x)\n",
			 view_name(view_commond), view_is_replace(view_commond), view_replace_color(view_commond));

	return err;
}

/* load "num" "pic_grp" and the common */
static int load_num_view(struct num_view *numv_rcv)
{
	int ret = 0, err = 0;

	/* time[0] : num view motime */
	ret = slpt_load_file_to_mem("num", &numv_rcv->num, &numv_rcv->time[0]);
	err += ret ? 1 : 0;

	/* time[1] :pic view motime */
	ret = slpt_load_file_to_mem_char("pic_grp", numv_rcv->grp_name, &numv_rcv->time[1]);
	err += ret ? 1 : 0;

	return err;
}

/* load "picture" and the common */
static int load_flash_pic_view(struct flash_pic_view *fpicv_rcv)
{
	int ret = 0, err = 0;

	/* time[0] : pic view motime */
	ret = slpt_load_file_to_mem_char("picture", fpicv_rcv->pic_name, &fpicv_rcv->time[0]);
	err += ret ? 1 : 0;

	ret = slpt_load_file_to_mem("flash_mode", &fpicv_rcv->flash_mode, &fpicv_rcv->time[1]);
	err += ret ? 1 : 0;

	ret = slpt_load_file_to_mem("display", &fpicv_rcv->display, &fpicv_rcv->time[2]);
	err += ret ? 1 : 0;

	return err;
}

/* load "picture" and the common */
static int load_pic_view(struct pic_view *picv_rcv)
{
	int ret = 0, err = 0;

	/* time[0] : pic view motime */
	ret = slpt_load_file_to_mem_char("picture", picv_rcv->pic_name, &picv_rcv->time[0]);
	err += ret ? 1 : 0;

	return err;
}

/* load "follow_mode" and the common */
static int load_text_view(struct text_view *textv_rcv)
{
	return 0;
}

/* load "show" "center-x" "center-y" "picture" and the common */
static int load_rotate_pic(struct rotate_pic *rotate_picv_rcv)
{
	int ret = 0, err = 0;

	/* time[0] : show view motime */
	ret = slpt_load_file_to_mem("show", &rotate_picv_rcv->show, &rotate_picv_rcv->time[0]);
	err += ret ? 1 : 0;

	/* time[1] : center-x view motime */
	ret = slpt_load_file_to_mem("center-x", &rotate_picv_rcv->center.x, &rotate_picv_rcv->time[1]);
	err += ret ? 1 : 0;

	/* time[2] : center-y view motime */
	ret = slpt_load_file_to_mem("center-y", &rotate_picv_rcv->center.y, &rotate_picv_rcv->time[2]);
	err += ret ? 1 : 0;

	/* time[3] : picture view motime */
	ret = slpt_load_file_to_mem_char("picture", rotate_picv_rcv->pic_name, &rotate_picv_rcv->time[3]);
	err += ret ? 1 : 0;

	pr_debug("load: rpic: [%d]  (%d, %d) [%s] \n",
			rotate_picv_rcv->show,
			rotate_picv_rcv->center.x,
			rotate_picv_rcv->center.y,
			rotate_picv_rcv->pic_name);

	return err;
}

static int load_fb_region(struct fb_region *region_rcv)
{
	int ret = 0, err = 0;

	time_t time = 0;

	ret = slpt_load_file_to_mem("base", &region_rcv->base, &time);
	err += ret ? 1 : 0;

	time = 0;
	ret = slpt_load_file_to_mem("bpp", &region_rcv->bpp, &time);
	err += ret ? 1 : 0;

	time = 0;
	ret = slpt_load_file_to_mem("pixels_per_line", &region_rcv->pixels_per_line, &time);
	err += ret ? 1 : 0;

	time = 0;
	ret = slpt_load_file_to_mem("xres", &region_rcv->xres, &time);
	err += ret ? 1 : 0;

	time = 0;
	ret = slpt_load_file_to_mem("yres", &region_rcv->yres, &time);
	err += ret ? 1 : 0;

	pr_debug("load: rpic: [%p] [%d] [%d]  (%d, %d)\n",
			region_rcv->base,
			region_rcv->bpp,
			region_rcv->pixels_per_line,
			region_rcv->xres,
			region_rcv->yres);

	return err;
}
