#include <config.h>
#include <asm/errno.h>
#include <asm/io.h>
#include <common.h>
#include <linux/pr_info.h>
#include <fb_struct.h>

void write_color_icon_fb(struct fb_struct *fbs, struct color_icon *cicon) {
	if (!cicon->colors) {
		
	}
}
