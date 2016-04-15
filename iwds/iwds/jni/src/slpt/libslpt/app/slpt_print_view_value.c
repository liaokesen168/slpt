#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

#include <view.h>

#include <file_ops.h>
#include <digital_clock.h>
#include <analog_clock.h>
#include <analog_week_clock.h>
#include <analog_month_clock.h>
#include <analog_second_clock.h>
#include <analog_minute_clock.h>
#include <analog_hour_clock.h>
#include <slpt_file.h>

#define VIEW_TOP_DIR "/sys/slpt/apps/slpt-app/res/clock/"

#define SIZE 400
char separator[SIZE];

char *get_separator(unsigned int enter_level)
{
	char *separator_tmp = separator + strlen(separator) - enter_level - 4;
	return separator_tmp;
}

void slpt_print_common(struct view *view, unsigned int enter_level)
{
	char *separator_tmp = get_separator(enter_level);

	pr_info("%s|---- show          : %d\n", separator_tmp, view->show);
	pr_info("%s|---- start-x       : %d\n",separator_tmp, view->start.x);
	pr_info("%s|---- start-y       : %d\n",separator_tmp, view->start.y);
	pr_info("%s|---- follow-mode   : %d\n",separator_tmp, view->follow_mode);
	pr_info("%s|---- center-hor    : %d\n",separator_tmp, view->center_hor);
	pr_info("%s|---- center-ver    : %d\n",separator_tmp, view->center_ver);
	pr_info("%s|---- replace-mode  : %d\n",separator_tmp, view->replace_mode);
	pr_info("%s|---- replace-color : %d\n",separator_tmp, view->replace_color);
	pr_info("%s|---- level         : %d\n",separator_tmp, view->level);
	pr_info("%s|-alpha_mode : %d\n",separator_tmp, view->alpha_mode);

	printf("%s|---- show          : %d\n", separator_tmp, view->show);
	printf("%s|---- start-x       : %d\n",separator_tmp, view->start.x);
	printf("%s|---- start-y       : %d\n",separator_tmp, view->start.y);
	printf("%s|---- follow-mode   : %d\n",separator_tmp, view->follow_mode);
	printf("%s|---- center-hor    : %d\n",separator_tmp, view->center_hor);
	printf("%s|---- center-ver    : %d\n",separator_tmp, view->center_ver);
	printf("%s|---- replace-mode  : %d\n",separator_tmp, view->replace_mode);
	printf("%s|---- replace-color : %d\n",separator_tmp, view->replace_color);
	printf("%s|---- level         : %d\n",separator_tmp, view->level);
	printf("%s|-alpha_mode : %d\n",separator_tmp, view->alpha_mode);

}

void slpt_print_num_view(struct view *view, unsigned int enter_level)
{
	char *separator_tmp = get_separator(enter_level);

	struct num_view *nv = to_num_view(view);

	pr_info("%s%s ---> numview\n", separator_tmp + 1, view_name(view));
	pr_info("%s|---- num_view->num : %d \n",separator_tmp, nv->num);
	pr_info("%s|---- num_view->grp_name : %s \n", separator_tmp, nv->grp_name);

	printf("%s%s ---> numview\n", separator_tmp + 1, view_name(view));
	printf("%s|---- num_view->num : %d \n", separator_tmp, nv->num);
	printf("%s|---- num_view->grp_name : %s \n", separator_tmp, nv->grp_name);

	slpt_print_common(view, enter_level);
}

void slpt_print_pic_view(struct view *view, unsigned int enter_level)
{
	char *separator_tmp = get_separator(enter_level);

	struct pic_view *pv = to_pic_view(view);

	pr_info("%s%s ---> picview\n", separator_tmp + 1, view_name(view));
	printf("%s%s ---> picview\n", separator_tmp + 1, view_name(view));

	pr_info("%s|---- fpic_view->pic_name : %s \n", separator_tmp, pv->pic_name);
	printf("%s|---- fpic_view->pic_name : %s \n", separator_tmp, pv->pic_name);

	slpt_print_common(view, enter_level);
}

void slpt_print_flash_pic_view(struct view *view, unsigned int enter_level)
{
	char *separator_tmp = get_separator(enter_level);

	struct flash_pic_view *fpv = to_flash_pic_view(view);

	pr_info("%s%s ---> flash-pic-view\n", separator_tmp + 1, view_name(view));
	printf("%s%s ---> flash-pic-view\n", separator_tmp + 1, view_name(view));

	pr_info("%s|---- fpic_view->pic_name : %s \n", separator_tmp, fpv->pic_name);
	printf("%s|---- fpic_view->pic_name : %s \n", separator_tmp, fpv->pic_name);

	pr_info("%s|---- fpic_view->flash_mode : %d \n", separator_tmp, fpv->flash_mode);
	printf("%s|---- fpic_view->flash_mode : %d \n", separator_tmp, fpv->flash_mode);

	pr_info("%s|---- fpic_view->display : %d \n", separator_tmp, fpv->display);
	printf("%s|---- fpic_view->display : %d \n", separator_tmp, fpv->display);

	slpt_print_common(view, enter_level);
}

void slpt_print_text_view(struct view *view, unsigned int enter_level)
{
	char *separator_tmp = get_separator(enter_level);

	pr_info("%s %s ---> textview\n", separator_tmp + 1, view_name(view));
	printf("%s %s ---> textview\n", separator_tmp + 1, view_name(view));

	slpt_print_common(view, enter_level);

	slpt_print_view_grp(&view->grp, enter_level + 1);
}

void slpt_print_digital_clock_en_view(struct view *view, unsigned int enter_level)
{
	char *separator_tmp = get_separator(enter_level);

	pr_info("%s %s ---> digital-clock-en-view\n", separator_tmp + 1, view_name(view));
	printf("%s %s ---> digital-clock-en-view\n", separator_tmp + 1, view_name(view));

	slpt_print_common(view, enter_level);

	slpt_print_view_grp(&view->grp, enter_level + 1);
}

void slpt_print_date_en_view(struct view *view, unsigned int enter_level)
{
	char *separator_tmp = get_separator(enter_level);

	pr_info("%s %s ---> date-en-view\n", separator_tmp + 1, view_name(view));
	printf("%s %s ---> date-en-view\n", separator_tmp + 1, view_name(view));

	slpt_print_common(view, enter_level);

	slpt_print_view_grp(&view->grp, enter_level + 1);
}

void slpt_print_date_cn_view(struct view *view, unsigned int enter_level)
{
	char *separator_tmp = get_separator(enter_level);

	pr_info("%s %s ---> date-cn-view\n", separator_tmp + 1, view_name(view));
	printf("%s %s ---> date-cn-view\n", separator_tmp + 1, view_name(view));

	slpt_print_common(view, enter_level);

	slpt_print_view_grp(&view->grp, enter_level + 1);
}

void slpt_print_week_en_view(struct view *view, unsigned int enter_level)
{
	char *separator_tmp = get_separator(enter_level);

	pr_info("%s %s ---> week-en-view\n", separator_tmp + 1, view_name(view));
	printf("%s %s ---> week-en-view\n", separator_tmp + 1, view_name(view));

	slpt_print_common(view, enter_level);

	slpt_print_view_grp(&view->grp, enter_level + 1);
}

void slpt_print_week_cn_view(struct view *view, unsigned int enter_level)
{
	char *separator_tmp = get_separator(enter_level);

	pr_info("%s %s ---> week-cn-view\n", separator_tmp + 1, view_name(view));
	printf("%s %s ---> week-cn-view\n", separator_tmp + 1, view_name(view));

	slpt_print_common(view, enter_level);

	slpt_print_view_grp(&view->grp, enter_level + 1);
}

void slpt_print_year_en_view(struct view *view, unsigned int enter_level)
{
	char *separator_tmp = get_separator(enter_level);

	pr_info("%s %s ---> year-en-view\n", separator_tmp + 1, view_name(view));
	printf("%s %s ---> year-en-view\n", separator_tmp + 1, view_name(view));

	slpt_print_common(view, enter_level);

	slpt_print_view_grp(&view->grp, enter_level + 1);
}

void slpt_print_time_view(struct view *view, unsigned int enter_level)
{
	char *separator_tmp = get_separator(enter_level);

	pr_info("%s %s ---> time-view\n", separator_tmp + 1, view_name(view));
	printf("%s %s ---> time-view\n", separator_tmp + 1, view_name(view));

	slpt_print_common(view, enter_level);

	slpt_print_view_grp(&view->grp, enter_level + 1);
}

void slpt_print_digital_clock_cn_view(struct view *view, unsigned int enter_level)
{
	char *separator_tmp = get_separator(enter_level);

	pr_info("%s %s ---> digital-clock-cn-view\n", separator_tmp + 1,view_name(view));
	printf("%s %s ---> digital-clock-cn-view\n", separator_tmp + 1, view_name(view));

	slpt_print_common(view, enter_level);

	slpt_print_view_grp(&view->grp, enter_level + 1);
}

void slpt_print_rotate_pic(struct rotate_pic *rpic, unsigned int enter_level)
{
	char *separator_tmp = get_separator(enter_level);

	if(chdir(rpic->name) != 0) {
		pr_err("Couldn`t change (%s) diretory!", rpic->name);
		return ;
	}

	pr_info("%s %s ---> rotate-pic\n", separator_tmp + 1, rpic->name);
	pr_info("%s|---- view->show : %d \n", separator_tmp, rpic->show);
	pr_info("%s|---- view->center.x : %d \n", separator_tmp, rpic->center.x);
	pr_info("%s|---- view->center.y : %d \n", separator_tmp, rpic->center.y);
	pr_info("%s|---- view->pic_name : %s \n", separator_tmp, rpic->pic_name);

	printf("%s %s ---> rotate-pic\n", separator_tmp + 1, rpic->name);
	printf("%s|---- view->show : %d \n", separator_tmp, rpic->show);
	printf("%s|---- view->center.x : %d \n", separator_tmp, rpic->center.x);
	printf("%s|---- view->center.y : %d \n", separator_tmp, rpic->center.y);
	printf("%s|---- view->pic_name : %s \n", separator_tmp, rpic->pic_name);

	chdir("..");
}

void slpt_print_analog_clock_view(struct view *view, unsigned int enter_level)
{
	unsigned int i;

	struct analog_clock *clock = to_analog_clock(view);

	char *separator_tmp = get_separator(enter_level);

	pr_info("%s %s ---> analog-clock-view\n", separator_tmp + 1, view_name(view));
	printf("%s %s ---> analog-clock-view\n", separator_tmp + 1, view_name(view));

	for (i = 0; i < ARRAY_SIZE(clock->handers); ++i) {
		slpt_print_rotate_pic(&clock->handers[i], enter_level);
	}

	slpt_print_common(view, enter_level);

}

void slpt_print_analog_base_clock_view(struct analog_base_clock *clock, unsigned int enter_level)
{
	unsigned int i;

	slpt_print_view(&clock->bkg.view, enter_level); /* print the background */

	for (i = 0; i < ARRAY_SIZE(clock->handers); ++i) {
		slpt_print_rotate_pic(&clock->handers[i], enter_level);
	}

	slpt_print_common(&clock->view, enter_level);

}

void slpt_print_analog_week_clock_view(struct view *view, unsigned int enter_level)
{
	struct analog_base_clock *clock = to_analog_base_clock(view);

	struct analog_week_clock *week_clock = to_analog_week_clock(clock);

	char *separator_tmp = get_separator(enter_level);

	pr_info("%s %s ---> analog-week-clock-view\n", separator_tmp + 1, view_name(view));
	printf("%s %s ---> analog-week-clock-view\n", separator_tmp + 1, view_name(view));

	slpt_print_analog_base_clock_view(&week_clock->clock, enter_level);

}

void slpt_print_analog_month_clock_view(struct view *view, unsigned int enter_level)
{
	struct analog_base_clock *clock = to_analog_base_clock(view);

	struct analog_month_clock *month_clock = to_analog_month_clock(clock);

	char *separator_tmp = get_separator(enter_level);

	pr_info("%s %s ---> analog-month-clock-view\n", separator_tmp + 1, view_name(view));
	printf("%s %s ---> analog-month-clock-view\n", separator_tmp + 1, view_name(view));

	slpt_print_analog_base_clock_view(&month_clock->clock, enter_level);

}

void slpt_print_analog_second_clock_view(struct view *view, unsigned int enter_level)
{
	struct analog_base_clock *clock = to_analog_base_clock(view);

	struct analog_second_clock *second_clock = to_analog_second_clock(clock);

	char *separator_tmp = get_separator(enter_level);

	pr_info("%s %s ---> analog-second-clock-view\n", separator_tmp + 1, view_name(view));
	printf("%s %s ---> analog-second-clock-view\n", separator_tmp + 1, view_name(view));

	slpt_print_analog_base_clock_view(&second_clock->clock, enter_level);

}

void slpt_print_analog_minute_clock_view(struct view *view, unsigned int enter_level)
{
	struct analog_base_clock *clock = to_analog_base_clock(view);

	struct analog_minute_clock *minute_clock = to_analog_minute_clock(clock);

	char *separator_tmp = get_separator(enter_level);

	pr_info("%s %s ---> analog-minute-clock-view\n", separator_tmp + 1, view_name(view));
	printf("%s %s ---> analog-minute-clock-view\n", separator_tmp + 1, view_name(view));

	slpt_print_analog_base_clock_view(&minute_clock->clock, enter_level);

}

void slpt_print_analog_hour_clock_view(struct view *view, unsigned int enter_level)
{
	struct analog_base_clock *clock = to_analog_base_clock(view);

	struct analog_hour_clock *hour_clock = to_analog_hour_clock(clock);

	char *separator_tmp = get_separator(enter_level);

	pr_info("%s %s ---> analog-hour-clock-view\n", separator_tmp + 1, view_name(view));
	printf("%s %s ---> analog-hour-clock-view\n", separator_tmp + 1, view_name(view));

	slpt_print_analog_base_clock_view(&hour_clock->clock, enter_level);

}

void slpt_print_fb_region(struct fb_region *region_rcv, unsigned int enter_level)
{
	char *separator_tmp = get_separator(enter_level);

	pr_info("%s region_rcv->base : %p \n", separator_tmp, region_rcv->base);
	pr_info("%s region_rcv->bpp : %d \n", separator_tmp, region_rcv->bpp);
	pr_info("%s region_rcv->pixels_per_line : %d \n", separator_tmp, region_rcv->pixels_per_line);
	pr_info("%s region_rcv->xres : %d \n", separator_tmp, region_rcv->xres);
	pr_info("%s region_rcv->yres : %d \n", separator_tmp, region_rcv->yres);

	printf("%s region_rcv->base : %p \n", separator_tmp, region_rcv->base);
	printf("%s region_rcv->bpp : %d \n", separator_tmp, region_rcv->bpp);
	printf("%s region_rcv->pixels_per_line : %d \n", separator_tmp, region_rcv->pixels_per_line);
	printf("%s region_rcv->xres : %d \n", separator_tmp, region_rcv->xres);
	printf("%s region_rcv->yres : %d \n", separator_tmp, region_rcv->yres);
}

void slpt_print_view(struct view *view, unsigned int enter_level)
{
	if(chdir(view_name(view)) != 0) {
		pr_err("Couldn`t change (%s) diretory!", view_name(view));
		return ;
	}

	switch (view_type(view)) {
		case VIEW_NUM:		 slpt_print_num_view(view, enter_level + 1); 	  break;
		case VIEW_FLASH_PIC: slpt_print_flash_pic_view(view, enter_level + 1);break;
		case VIEW_PIC:		 slpt_print_pic_view(view, enter_level + 1); 	  break;
		case VIEW_TEXT:		 slpt_print_text_view(view, enter_level + 1);	  break;

		case VIEW_DIGITAL_CLOCK_EN: slpt_print_digital_clock_en_view(view, enter_level + 1); break;
		case VIEW_DIGITAL_CLOCK_CN: slpt_print_digital_clock_cn_view(view, enter_level + 1); break;
		case VIEW_ANALOG_CLOCK: 	slpt_print_analog_clock_view(view, enter_level + 1); 	 break;
		case VIEW_ANALOG_WEEK_CLOCK: 	slpt_print_analog_week_clock_view(view, enter_level + 1); 	 break;
		case VIEW_ANALOG_MONTH_CLOCK: 	slpt_print_analog_month_clock_view(view, enter_level + 1); 	 break;
		case VIEW_ANALOG_SECOND_CLOCK: 	slpt_print_analog_second_clock_view(view, enter_level + 1); 	 break;
		case VIEW_ANALOG_MINUTE_CLOCK: 	slpt_print_analog_minute_clock_view(view, enter_level + 1); 	 break;
		case VIEW_ANALOG_HOUR_CLOCK: 	slpt_print_analog_hour_clock_view(view, enter_level + 1); 	 break;

		case VIEW_DATE_EN:          slpt_print_date_en_view(view, enter_level + 1);          break;
		case VIEW_DATE_CN:          slpt_print_date_cn_view(view, enter_level + 1);          break;
		case VIEW_WEEK_EN:          slpt_print_week_en_view(view, enter_level + 1);          break;
		case VIEW_WEEK_CN:          slpt_print_week_cn_view(view, enter_level + 1);          break;
		case VIEW_YEAR_EN:          slpt_print_year_en_view(view, enter_level + 1);          break;
		case VIEW_TIME:             slpt_print_time_view(view, enter_level + 1);             break;

		default:
			pr_debug("Your type number is (%d), maybe wrong, match nothing\n", view_type(view));
	}

	chdir("..");
}

extern int default_pictures_init_onetime(void);

int slpt_print_the_view(int argc, char **argv)
{
	char cur_main_dir[MAX_FILE_NAME] = VIEW_TOP_DIR;
	unsigned int enter_level = 0;
	struct view *view_to_printf;

	memset(separator, ' ', SIZE - 1);
	separator[SIZE - 1] = '\0';

	if(argc < 3) {
		pr_info(" your param too few\n\n");
		return 0;
	}

	getcwd(cur_main_dir, MAX_FILE_NAME);

	if(chdir(VIEW_TOP_DIR) != 0) {
		pr_err("Couldn`t change (%s) diretory!", VIEW_TOP_DIR);
		return 0;
	}

	default_pictures_init_onetime();

	/* the first param is the type, the second param is the view_name */
	view_to_printf = alloc_view_by_str(argv[2], argv[1]);
	if(!view_to_printf) {
		pr_info("alloc_view_by_str is wrong\n");
		return 0;
	}

	slpt_load_view(view_to_printf);

	slpt_print_view(view_to_printf, enter_level + 1);

	chdir(cur_main_dir);

	return 0;
}
