#include <common.h>
#include <sview/sview.h>
#include <sview/pic_sview.h>

int pic_sview_set_pic(struct sview *view, const char *pic_name) {
	struct pic_sview *pv  = to_pic_sview(view);

	if (pic_name == NULL || strlen(pic_name) >= sizeof(pv->pic_name))
		return -EINVAL;

	strcpy(pv->pic_name, pic_name);

	return 0;
}

/* pic sview */
void pic_sview_draw(struct sview *view) {
	struct pic_sview *pv  = to_pic_sview(view);
	struct fb_region *region = picture_region(pv->pic);

	fb_region_write_alpha(&view->base, region, &view->position, ALPHA32BIT);
}

void pic_sview_measure_size(struct sview *view) {
	struct pic_sview *pv  = to_pic_sview(view);
	struct fb_region *region = picture_region(pv->pic);

	view->raw_rect.w = region->xres;
	view->raw_rect.h = region->yres;
}

int pic_sview_sync(struct sview *view) {
	struct pic_sview *pv  = to_pic_sview(view);
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

void pic_sview_free(struct sview *view) {
	struct pic_sview *pv  = to_pic_sview(view);

	put_picture(pv->pic);
	if (view->is_alloc)
		free(pv);
}

#ifdef CONFIG_SLPT
static struct slpt_app_res pic_sview_res[] = {
	SLPT_RES_EMPTY_DEF("picture", SLPT_RES_MEM),     /* picture */
};

struct slpt_app_res *slpt_register_pic_sview(struct sview *view, struct slpt_app_res *parent) {
	struct pic_sview *pv = to_pic_sview(view);

	slpt_set_res(pic_sview_res[0], pv->pic_name, sizeof(pv->pic_name));
	return slpt_register_sview_base(view, parent, pic_sview_res, ARRAY_SIZE(pic_sview_res));
}
#endif

void init_pic_sview(struct pic_sview *pv, const char *name) {
	sview_init_base(&pv->view, name, SVIEW_PIC);

	strcpy(pv->pic_name, "");
	pv->pic = NULL;
}

struct sview *alloc_pic_sview(const char *name) {
	struct pic_sview *pv;
	char *cpy_name;

	pv = malloc_with_name(sizeof(*pv), name);
	if (!pv) {
		pr_err("pic_view: failed to alloc\n");
		return NULL;
	}
	cpy_name = (char *)&pv[1];

	init_pic_sview(pv, cpy_name);

	pv->view.is_alloc = 1;

	return &pv->view;
}
