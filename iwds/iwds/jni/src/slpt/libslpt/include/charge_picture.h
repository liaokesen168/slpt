#ifndef _CHARGE_PICTURE_VIEW_H_
#define _CHARGE_PICTURE_VIEW_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <view.h>
#include <time_notify.h>
#include <file_ops.h>

#ifdef CONFIG_SLPT
#include <slpt.h>
#endif

#define CHARGE_FLAG_DIR "/sys/class/power_supply/usb/online"
#define CHARGEFULL_FLAG_DIR "/sys/class/power_supply/battery/capacity"
#define CHARGE_CLOCK_DIR "/sys/slpt/apps/slpt-app/res/clock"
#define CHARGE_FULL_CLOCK_DIR "/sys/slpt/apps/slpt-app/res/clock"

struct charge_pic {
	struct flash_pic_view fpicv;
};

extern struct charge_pic global_charge_pic;
extern int init_charge_picture(void);

static inline int get_the_charge_state(void)
{
	char charge_flag = '0';
	load_file(CHARGE_FLAG_DIR, &charge_flag, 1, NULL);
	return (charge_flag - '0');
}

static inline int get_the_chargefull_state(void)
{
	char chargefull_flag[4] = {'\0', '\0', '\0', '\0'};
	load_file(CHARGEFULL_FLAG_DIR, chargefull_flag, 3, NULL);

	return strtol(chargefull_flag, '\0', 0);
}

static inline int sync_charge_picture(void) {
	int ret = 0;
	char cur_main_dir[MAX_FILE_NAME];

	getcwd(cur_main_dir, MAX_FILE_NAME);

	if(chdir(CHARGE_CLOCK_DIR) != 0) {
		pr_err("Couldn`t change (%s) diretory!", CHARGE_CLOCK_DIR);
		return -1;
	}

	slpt_load_view(&(global_charge_pic.fpicv.view));

	chdir(cur_main_dir);

	ret = view_sync_setting(&(global_charge_pic.fpicv.view));

	view_sync_start(&(global_charge_pic.fpicv.view));

	return ret;
}

static inline void display_charge_picture(void) {
	unsigned int on;

	if (get_the_charge_state() && (get_the_chargefull_state() < 100))
		on = 1;
	else
		on = 0;

	flash_pic_view_set_display(&global_charge_pic.fpicv.view, on);

	view_display(&global_charge_pic.fpicv.view);

}

extern struct charge_pic global_chargefull_pic;
extern int init_chargefull_picture(void);

static inline int sync_chargefull_picture(void) {
	int ret = 0;
	char cur_main_dir[MAX_FILE_NAME];

	getcwd(cur_main_dir, MAX_FILE_NAME);

	if(chdir(CHARGE_FULL_CLOCK_DIR) != 0) {
		pr_err("Couldn`t change (%s) diretory!", CHARGE_FULL_CLOCK_DIR);
		return -1;
	}

	slpt_load_view(&(global_chargefull_pic.fpicv.view));

	chdir(cur_main_dir);

	ret = view_sync_setting(&(global_chargefull_pic.fpicv.view));

	view_sync_start(&(global_chargefull_pic.fpicv.view));

	return ret;
}

static inline void display_chargefull_picture(void) {
	unsigned int on;

	if (get_the_charge_state() && get_the_chargefull_state() == 100)
		on = 1;
	else
		on = 0;

	flash_pic_view_set_display(&global_chargefull_pic.fpicv.view, on);

	view_display(&global_chargefull_pic.fpicv.view);
}


#ifdef __cplusplus
}
#endif
#endif
