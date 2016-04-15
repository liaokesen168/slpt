#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <view.h>
#include <charge_picture.h>

#ifdef CONFIG_SLPT
#include <slpt.h>
#endif

struct charge_pic global_charge_pic;
struct charge_pic global_chargefull_pic;

int init_charge_picture(void) {

	init_flash_pic_view(&global_charge_pic.fpicv, "charge_pic", "clock/charge_pic");

	return 0;
}

int init_chargefull_picture(void) {

	init_flash_pic_view(&global_chargefull_pic.fpicv, "chargefull_pic", "clock/chargefull_pic");

	return 0;
}
