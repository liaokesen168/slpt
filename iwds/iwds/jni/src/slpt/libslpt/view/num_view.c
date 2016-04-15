#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <view.h>
#include <background.h>

/* num view */
void num_view_display(struct view *view) {
	struct num_view *nv  = to_num_view(view);
	struct fb_region *region = picture_grp_region(nv->grp, nv->num);
	struct position start = *view_start(view);

	pr_debug("num view: [%s] (%d, %d)\n", view_name(view), start.x, start.y);

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

void num_view_cal_size(struct view *view, unsigned int *xmax, unsigned int *ymax) {
	struct num_view *nv  = to_num_view(view);
	struct fb_region *region = picture_grp_region(nv->grp, nv->num);

	*xmax = region->xres;
	*ymax = region->yres;
}

int num_view_sync(struct view *view) {
	struct num_view *nv  = to_num_view(view);
	struct picture_grp *grp;
	int sync;

	grp = get_picture_grp(nv->grp_name);
	if (!grp) {
		pr_err("num_view: can not get pic grp: %s\n", nv->grp_name);
		sync = -ENODEV;
	} else {
		sync = picture_grp_sync(grp);
	}

	put_picture_grp(nv->grp);
	nv->grp = grp;

	return sync;
}

void num_view_set_bg(struct view *view, struct fb_region *bg) {
	view->bg = *bg;
}

void num_view_free(struct view *view) {
	struct num_view *nv  = to_num_view(view);

	put_picture_grp(nv->grp);
	if (view_is_alloc(view))
		free(nv);
}

#ifdef CONFIG_SLPT
static struct slpt_app_res num_view_res[] = {
	SLPT_RES_EMPTY_DEF("num", SLPT_RES_INT),         /* num */
	SLPT_RES_EMPTY_DEF("pic_grp", SLPT_RES_MEM),     /* picture group */
};

struct slpt_app_res *slpt_register_num_view(struct view *view, struct slpt_app_res *parent) {
	struct num_view *nv = to_num_view(view);

	slpt_set_res(num_view_res[0], &nv->num, 4);
	slpt_set_res(num_view_res[1], nv->grp_name, sizeof(nv->grp_name));
	return slpt_register_view_base(view, parent, num_view_res, ARRAY_SIZE(num_view_res));
}
#endif

int init_num_view(struct num_view *nv, const char *name, const char *pic_grp_name) {
	struct picture_grp *grp;

	view_init_status(&nv->view, name, VIEW_NUM);

	grp = get_picture_grp(pic_grp_name);
	if (!grp) {
		pr_err("num_view: can not get pic grp: %s\n", pic_grp_name);
	}

	strcpy(nv->grp_name, pic_grp_name);
	nv->grp = grp;
	nv->num = 0;

	nv->view.display = num_view_display;
	nv->view.set_bg = num_view_set_bg;
	nv->view.sync = num_view_sync;
	nv->view.freev = num_view_free;
	nv->view.cal_size = num_view_cal_size;

#ifdef CONFIG_SLPT
	nv->view.register_slpt = slpt_register_num_view;
#endif

	return 0;
}

struct view *alloc_num_view(const char *name, const char *pic_grp_name) {
	struct num_view *nv;
	char *cpy_name;

	nv = malloc_with_name(sizeof(*nv), name);
	if (!nv) {
		pr_err("num_view: failed to alloc\n");
		return NULL;
	}
	cpy_name = (char *)&nv[1];

	if (init_num_view(nv, cpy_name, pic_grp_name))
		goto free_num_view;

	nv->view.is_alloc = 1;

	return &nv->view;
free_num_view:
	free(nv);
	return NULL;
}
