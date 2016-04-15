#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <view.h>
#include <background.h>

/* flash_pic view */
void flash_pic_view_display(struct view *view) {
	struct flash_pic_view *fpv  = to_flash_pic_view(view);
	struct fb_region *region = picture_region(fpv->pic);
	struct position start = *view_start(view);
	struct fb_region empty;

	pr_debug("pic view: [%s] (%d, %d) (%d)\n", view_name(view), start.x, start.y, fpv->display);

	if (!fpv->display) {
		region_cat_region(&empty, global_bg_region(), region, &start);
		fb_region_write(view_bg(view), &empty, &start);
	} else {
		if (view_is_replace(view)) {
				fb_region_write_alpha_replace(view_bg(view),
					region, global_bg_region(), &start, alpha32, view_replace_color(view));
		} else if(view_alpha_mode(view)) {
				fb_region_write_alpha_replace_src(view_bg(view),
						region, global_bg_region(), &start, alpha32);
		} else {
			fb_region_write(view_bg(view), region, &start);
		}
	}

	start.x += region->xres;
	view_set_end(view, &start);
}

void flash_pic_view_cal_size(struct view *view, unsigned int *xmax, unsigned int *ymax) {
	struct flash_pic_view *fpv  = to_flash_pic_view(view);
	struct fb_region *region = picture_region(fpv->pic);

	*xmax = region->xres;
	*ymax = region->yres;
}

int flash_pic_view_sync(struct view *view) {
	struct flash_pic_view *fpv  = to_flash_pic_view(view);
	struct picture *pic;
	int sync;

	pic = get_picture(fpv->pic_name);
	if (!pic) {
		pr_err("flash_pic_view: can not get pic: %s\n", fpv->pic_name);
		sync = -ENODEV;
	} else {
		sync = picture_sync(pic);
	}

	put_picture(fpv->pic);
	fpv->pic = pic;

	return sync;
}

void flash_pic_view_set_bg(struct view *view, struct fb_region *bg) {
	view->bg = *bg;
}

void flash_pic_view_free(struct view *view) {
	struct flash_pic_view *fpv  = to_flash_pic_view(view);

	put_picture(fpv->pic);
	if (view_is_alloc(view))
		free(fpv);
}

#ifdef CONFIG_SLPT
static struct slpt_app_res flash_pic_view_res[] = {
	SLPT_RES_EMPTY_DEF("picture", SLPT_RES_MEM),     /* picture */
	SLPT_RES_EMPTY_DEF("flash_mode", SLPT_RES_INT),     /* flash_mode : on/off */
	SLPT_RES_EMPTY_DEF("display", SLPT_RES_INT),     /* display: show/hide */
};

struct slpt_app_res *slpt_register_flash_pic_view(struct view *view, struct slpt_app_res *parent) {
	struct flash_pic_view *fpv = to_flash_pic_view(view);

	slpt_set_res(flash_pic_view_res[0], fpv->pic_name, sizeof(fpv->pic_name));
	slpt_set_res(flash_pic_view_res[1], &fpv->flash_mode, 4);
	slpt_set_res(flash_pic_view_res[2], &fpv->display, 4);

	return slpt_register_view_base(view, parent, flash_pic_view_res, ARRAY_SIZE(flash_pic_view_res));
}
#endif

int init_flash_pic_view(struct flash_pic_view *fpv, const char *name, const char *pic_name) {
	struct picture *pic;

	view_init_status(&fpv->view, name, VIEW_FLASH_PIC);

	pic = get_picture(pic_name);
	if (!pic) {
		pr_err("flash_pic_view: can not get picture: %s\n", pic_name);
	}

	strcpy(fpv->pic_name, pic_name);
	fpv->pic = pic;
	fpv->display = 1;
	fpv->flash_mode = 1;

	fpv->view.display = flash_pic_view_display;
	fpv->view.set_bg = flash_pic_view_set_bg;
	fpv->view.sync = flash_pic_view_sync;
	fpv->view.freev = flash_pic_view_free;
	fpv->view.cal_size = flash_pic_view_cal_size;

#ifdef CONFIG_SLPT
	fpv->view.register_slpt = slpt_register_flash_pic_view;
#endif

	return 0;
}

struct view *alloc_flash_pic_view(const char *name, const char *pic_name) {
	struct flash_pic_view *fpv;
	char *cpy_name;

	fpv = malloc_with_name(sizeof(*fpv), name);
	if (!fpv) {
		pr_err("flash_pic_view: failed to alloc\n");
		return NULL;
	}
	cpy_name = (char *)&fpv[1];

	if (init_flash_pic_view(fpv, cpy_name, pic_name))
		goto free_flash_pic_view;

	fpv->view.is_alloc = 1;

	return &fpv->view;
free_flash_pic_view:
	free(fpv);
	return NULL;
}
