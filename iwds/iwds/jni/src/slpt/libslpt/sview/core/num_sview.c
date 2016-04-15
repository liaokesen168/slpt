#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <sview/num_sview.h>

void num_sview_set_num(struct sview *view, unsigned int num) {
	struct num_sview *nv  = to_num_sview(view);

	nv->num = num;
}

int num_sview_set_pic_grp(struct sview *view, const char *grp_name) {
	struct num_sview *nv  = to_num_sview(view);

	if (grp_name == NULL || strlen(grp_name) >= sizeof(nv->grp_name))
		return -EINVAL;

	strcpy(nv->grp_name, grp_name);

	return 0;
}

/* num sview */
void num_sview_draw(struct sview *view) {
	struct num_sview *nv  = to_num_sview(view);
	struct fb_region *region = picture_grp_region(nv->grp, nv->num);

	if (region)
		fb_region_write_alpha(&view->base, region, &view->position, ALPHA32BIT);
	else
		pr_err("num sview: failed to get picture grp region : %d\n", nv->num);
}

void num_sview_measure_size(struct sview *view) {
	struct num_sview *nv  = to_num_sview(view);
	struct fb_region *region = picture_grp_region(nv->grp, nv->num);

	view->raw_rect.w = region->xres;
	view->raw_rect.h = region->yres;
}

int num_sview_sync(struct sview *view) {
	struct num_sview *nv  = to_num_sview(view);
	struct picture_grp *grp;
	int sync;

	grp = get_picture_grp(nv->grp_name);
	if (!grp) {
		pr_err("num_sview: can not get pic grp: %s\n", nv->grp_name);
		sync = -ENODEV;
	} else {
		sync = picture_grp_sync(grp);
	}

	put_picture_grp(nv->grp);
	nv->grp = grp;

	return sync;
}

void num_sview_free(struct sview *view) {
	struct num_sview *nv  = to_num_sview(view);

	put_picture_grp(nv->grp);
	if (view->is_alloc)
		free(nv);
}

#ifdef CONFIG_SLPT
static struct slpt_app_res num_sview_res[] = {
	SLPT_RES_EMPTY_DEF("num", SLPT_RES_INT),         /* num */
	SLPT_RES_EMPTY_DEF("pic_grp", SLPT_RES_MEM),     /* picture group */
};

struct slpt_app_res *slpt_register_num_sview(struct sview *view, struct slpt_app_res *parent) {
	struct num_sview *nv = to_num_sview(view);

	slpt_set_res(num_sview_res[0], &nv->num, 4);
	slpt_set_res(num_sview_res[1], nv->grp_name, sizeof(nv->grp_name));
	return slpt_register_sview_base(view, parent, num_sview_res, ARRAY_SIZE(num_sview_res));
}
#endif

void init_num_sview(struct num_sview *nv, const char *name) {
	sview_init_base(&nv->view, name, SVIEW_NUM);

	strcpy(nv->grp_name, "");
	nv->grp = NULL;
	nv->num = 0;
}

struct sview *alloc_num_sview(const char *name) {
	struct num_sview *nv;
	char *cpy_name;

	nv = malloc_with_name(sizeof(*nv), name);
	if (!nv) {
		pr_err("num_sview: failed to alloc\n");
		return NULL;
	}
	cpy_name = (char *)&nv[1];

	init_num_sview(nv, cpy_name);

	nv->view.is_alloc = 1;

	return &nv->view;
}
