#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <view.h>
#include <background.h>

struct bg_pic global_bg;

int init_global_bg_pic(void) {
	struct fb_region *bg;
	struct view *pv;

	bg = get_current_fb_region();
	assert(bg);

	pv = alloc_pic_view("background", "clock/background");
	if (!pv) {
		pr_err("global bg: failed to alloc pic view\n");
		return -ENODEV;
	}

	global_bg.pv = pv;
	view_set_bg(global_bg.pv, bg);

	return 0;
}
