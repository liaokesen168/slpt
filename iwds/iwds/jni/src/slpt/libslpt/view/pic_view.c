#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <view.h>
#include <background.h>

/* pic view */
void pic_view_display(struct view *view) {
	struct pic_view *pv  = to_pic_view(view);
	struct fb_region *region = picture_region(pv->pic);
	struct position start = *view_start(view);

	pr_debug("pic view: [%s] (%d, %d) (%d, %x)\n",
			 view_name(view), start.x, start.y, view_is_replace(view), view_replace_color(view));

	if (view_is_replace(view)) {
			fb_region_write_alpha_replace(view_bg(view),
				region, global_bg_region(), &start, alpha32, view_replace_color(view));
	} else if(view_alpha_mode(view)) {
			fb_region_write_alpha_replace_src(view_bg(view),
					region, global_bg_region(), &start, alpha32);
	} else {
		fb_region_write(view_bg(view), region, &start);
	}

	start.x += region->xres;
	view_set_end(view, &start);
}

void pic_view_cal_size(struct view *view, unsigned int *xmax, unsigned int *ymax) {
	struct pic_view *pv  = to_pic_view(view);
	struct fb_region *region = picture_region(pv->pic);

	*xmax = region->xres;
	*ymax = region->yres;
}

int pic_view_sync(struct view *view) {
	struct pic_view *pv  = to_pic_view(view);
	struct picture *pic;
	int sync;

	pic = get_picture(pv->pic_name);
	if (!pic) {
		pr_err("pic_view: can not get pic: %s\n", pv->pic_name);
		sync = -ENODEV;
	} else {
		sync = picture_sync(pic);
	}

	put_picture(pv->pic);
	pv->pic = pic;

	return sync;
}

void pic_view_set_bg(struct view *view, struct fb_region *bg) {
	view->bg = *bg;
}

void pic_view_free(struct view *view) {
	struct pic_view *pv  = to_pic_view(view);

	put_picture(pv->pic);
	if (view_is_alloc(view))
		free(pv);
}

#ifdef CONFIG_SLPT
static struct slpt_app_res pic_view_res[] = {
	SLPT_RES_EMPTY_DEF("picture", SLPT_RES_MEM),     /* picture */
};

struct slpt_app_res *slpt_register_pic_view(struct view *view, struct slpt_app_res *parent) {
	struct pic_view *pv = to_pic_view(view);

	slpt_set_res(pic_view_res[0], pv->pic_name, sizeof(pv->pic_name));
	return slpt_register_view_base(view, parent, pic_view_res, ARRAY_SIZE(pic_view_res));
}
#endif

int init_pic_view(struct pic_view *pv, const char *name, const char *pic_name) {
	struct picture *pic;

	view_init_status(&pv->view, name, VIEW_PIC);

	pic = get_picture(pic_name);
	if (!pic) {
		pr_err("pic_view: can not get picture: %s\n", pic_name);
	}

	strcpy(pv->pic_name, pic_name);
	pv->pic = pic;

	pv->view.display = pic_view_display;
	pv->view.set_bg = pic_view_set_bg;
	pv->view.sync = pic_view_sync;
	pv->view.freev = pic_view_free;
	pv->view.cal_size = pic_view_cal_size;

#ifdef CONFIG_SLPT
	pv->view.register_slpt = slpt_register_pic_view;
#endif

	return 0;
}

struct view *alloc_pic_view(const char *name, const char *pic_name) {
	struct pic_view *pv;
	char *cpy_name;

	pv = malloc_with_name(sizeof(*pv), name);
	if (!pv) {
		pr_err("pic_view: failed to alloc\n");
		return NULL;
	}
	cpy_name = (char *)&pv[1];

	if (init_pic_view(pv, cpy_name, pic_name))
		goto free_pic_view;

	pv->view.is_alloc = 1;

	return &pv->view;
free_pic_view:
	free(pv);
	return NULL;
}
