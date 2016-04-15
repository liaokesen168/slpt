#include <sys/mman.h>			/* mmap/munmap */
#include <dlfcn.h>
#include <fcntl.h>
#include <errno.h>
#include <sys/ioctl.h>
#include <string.h>
#include <stdlib.h>
#include <linux/fb.h>

#include <fb_struct.h>
#include <file_ops.h>
#include "slpt.h"

#include <common.h>

extern int access_sysfs_pan_display(void);
extern int is_support_sysfs_pan_display(void);
extern int is_sysfs_pan_display(void);
extern int set_sysfs_pan_display(int frm);

#define DEBUG

#ifdef DEBUG
int debug_fb = 1;
#else
int debug_fb = 0;
#endif

#undef pr_debug
#define pr_debug(x...)                          \
	do {                                        \
		if (debug_fb)                           \
			pr_info(x);                         \
	} while (0)

struct fb_info {
	int fd;
	char name[MAX_FB_NAME_LEN];

	struct fb_var_screeninfo info;
	struct fb_fix_screeninfo fix;

	struct fb_struct fbs;
	struct fb_region region;

	void *mmap_base;
	unsigned int base_offset;
	unsigned int line_nums;

	struct {
		unsigned int inited:1;
	} s;
};

static struct fb_info fb_arr[MAX_FB_DEV_NUMS];

static struct fb_info *current_fb = NULL;
struct fb_region current_fb_region = {
	.base = NULL,
	.xres = 0,
	.yres = 0,
};

const char *fb_devs[] = {
	"/dev/graphics/fb%u",
	"/dev/fb%u",
	NULL,
};

struct fb_struct* get_default_fb(void) {
	return current_fb ? &current_fb->fbs : NULL;
}

void lcd_pan_display(unsigned int offset) {
	int ret = 0;

	if (current_fb) {
		current_fb->info.yoffset = current_fb->base_offset + offset;
		if (is_support_sysfs_pan_display())
			set_sysfs_pan_display(current_fb->info.yoffset / current_fb->info.yres);
		else
			ret = ioctl(current_fb->fd, FBIOPAN_DISPLAY, &current_fb->info);
		if (ret < 0)
			pr_err("FB: failed to pan display\n");
	}
}

static void fb_set_current(struct fb_info *fb) {
	current_fb = fb;
}

struct fb_var_screeninfo *get_default_fb_var(void) {
	return current_fb ? &current_fb->info : NULL;
}

static int open_fb(int id, char *name) {
	int i;
	int fd = -1;
	char buf[MAX_FB_NAME_LEN];

	if (name == NULL)
		name = buf;

	for (i = 0; fb_devs[i] != NULL; ++i) {
		sprintf(name, fb_devs[i], id);
		fd = open(name, O_RDWR);
		pr_debug("FB: open %s %s\n", name, fd > 0 ? "success" : "failed");
		if (fd > 0)
			break;
	}

	return fd;
}

int bpp_to_byte(unsigned int bpp) {
	switch (bpp) {
	case 15: case 16:
		return 2;
	case 18: case 24: case 32:
		return 4;
	default:
		return 2;
	}
}

void print_fb_info(struct fb_var_screeninfo *info) {
	pr_info("fbnfo: xres:          %d\n", info->xres);
	pr_info("fbnfo: yres:          %d\n", info->yres);
	pr_info("fbnfo: xres_virtual:  %d\n", info->xres_virtual);
	pr_info("fbnfo: yres_virtual:  %d\n", info->yres_virtual);
	pr_info("fbnfo: xoffset:       %d\n", info->xoffset);
	pr_info("fbnfo: yoffset:       %d\n", info->yoffset);
	pr_info("fbnfo: bits_per_pixel:%d\n", info->bits_per_pixel);
	pr_info("fbnfo: height:        %d\n", info->height);
	pr_info("fbnfo: width:         %d\n", info->width);
}

static int do_fb_init(int id, struct fb_info *fb) {
	int fd = -1;
	int ret;
	struct fb_var_screeninfo info;
	struct fb_fix_screeninfo fix;
	unsigned int *fb_base;
	unsigned int length, line_nums, line_length, base_offset;

	fd = open_fb(id, fb->name);
	if (fd < 0) {
		pr_debug("FB: failed to get fb%d device node\n", id);
		return -ENODEV;
	}

	ret = ioctl(fd, FBIOGET_VSCREENINFO, &info);
	if (ret < 0) {
		pr_debug("FB: failed to screenifo \n");
		goto close_file;
	}

	if (debug_fb)
		print_fb_info(&info);

	ret = ioctl(fd, FBIOGET_FSCREENINFO, &fix);
	if (ret < 0) {
		pr_debug("FB: failed to fix screenifo \n");
		goto close_file;
	}

	line_length = fix.line_length ;

	if (is_support_sysfs_pan_display()) {
		line_nums = info.yres_virtual + info.yres;
		length = line_length * line_nums;
		fb_base = mmap(0, length, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
		base_offset = info.yres_virtual;
	} else {
		line_nums = info.yres_virtual;
		length = line_length * line_nums;
		fb_base = mmap(0, length, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);		
		base_offset = 0;
	}

	if (IS_ERR(fb_base)) {
		pr_err("FB: failed to map fb mem %u lines\n", line_nums);
		ret = PTR_ERR(fb_base);
		goto close_file;
	}

	fb->fd = fd;
	fb->info = info;
	fb->fix = fix;
	fb->s.inited = 1;
	fb->mmap_base = fb_base;
	fb->base_offset = base_offset;

	fb->fbs.base = (char *)fb_base + base_offset * fix.line_length;
	fb->fbs.xres = info.xres;
	fb->fbs.yres = info.yres;
	fb->fbs.bits_per_pixel = info.bits_per_pixel;
	fb->fbs.pixels_per_line = line_length / bpp_to_byte(info.bits_per_pixel);
	fb->fbs.size = line_length * info.yres;
	fb->fbs.nums = length / fb->fbs.size;
 	fb->fbs.base_phys = 0;

	fb->region.base = fb->fbs.base;
	fb->region.xres = info.xres;
	fb->region.yres = info.yres;
	fb->region.pixels_per_line = line_length / bpp_to_byte(info.bits_per_pixel);
	fb->region.bpp = info.bits_per_pixel;

	return 0;
close_file:
	close(fd);
	return ret;
}

int fb_init(void) {
	int first_meet = 0;
	unsigned int i;
	int ret;

	if (current_fb)
		return 0;

	access_sysfs_pan_display();

	for (i = 0; i < ARRAY_SIZE(fb_arr); ++i) {
		memset(&fb_arr[i], 0, sizeof(fb_arr[i]));
	    ret = do_fb_init(i, &fb_arr[i]);
		if (ret == 0 && !first_meet) {
			first_meet = 1;
			fb_set_current(&fb_arr[i]);
			set_current_fb_region(&fb_arr[i].region);
			pr_info("FB: (%s) as default display device\n", fb_arr[i].name);
		}
	}

	if (!first_meet) {
		pr_err("FB: failed to get fb device\n");
		return -ENODEV;
	}

	return 0;
}

void fb_exit(void) {
	unsigned int i;
	struct fb_region clear = {0, 0, 0, 0, 0};

	fb_set_current(NULL);
	set_current_fb_region(&clear);

	for (i = 0; i < ARRAY_SIZE(fb_arr); ++i) {
		if (fb_arr[i].fd >= 0 && fb_arr[i].s.inited) {
			munmap(fb_arr[i].mmap_base, fb_arr[i].fix.line_length * fb_arr[i].info.yres_virtual);
			close(fb_arr[i].fd);
		}
		memset(&fb_arr[i], 0, sizeof(fb_arr[i]));
	}
}
