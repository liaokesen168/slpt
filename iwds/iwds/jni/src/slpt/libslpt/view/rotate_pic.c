#include <asm/errno.h>
#include <common.h>
#include <malloc.h>
#include <rotate_pic.h>

int rotate_pic_sync(struct rotate_pic *rpic) {
	struct picture *pic;
	int sync;

	pic = get_picture(rpic->pic_name);
	if (!pic) {
		pr_err("pic_view: can not get pic: %s\n", rpic->pic_name);
		sync = -ENODEV;
	} else {
		sync = picture_sync(pic);
	}

	put_picture(rpic->pic);
	rpic->pic = pic;

	if (!sync) {
		rotate_set_region(&rpic->rt, picture_region(rpic->pic));
		rotate_set_dst(&rpic->rt, &rpic->center);
		rotate_free_save(&rpic->rt);
		if (!rpic->show)
			rotate_free_maps(&rpic->rt);
		rpic->ready = 1;
	} else {
		rpic->ready = 0;
	}

	return sync;
}

int init_rotate_pic(struct rotate_pic *rpic, const char *name, const char *pic_name) {
	struct picture *pic;
	struct fb_region *bg = get_current_fb_region();

	assert(bg);

	memset(rpic, 0, sizeof(*rpic));
	rpic->name = name;
	init_rotate(&rpic->rt);

	pic = get_picture(pic_name);
	if (!pic) {
		pr_err("pic_view: can not get picture: %s\n", pic_name);
	}

	strcpy(rpic->pic_name, pic_name);
	rpic->pic = pic;
	rpic->show = 1;
	rotate_set_bg(&rpic->rt, bg);
	rotate_set_dst_to_center(&rpic->rt);
	rpic->center = *(rotate_dst_center(&rpic->rt));

	return 0;
}

void destory_rotate_pic(struct rotate_pic *rpic) {
	rotate_free_maps(&rpic->rt);
	put_picture(rpic->pic);
}

#ifdef CONFIG_SLPT
static struct slpt_app_res rpic_res[] = {
	SLPT_RES_EMPTY_DEF("show", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("center-x", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("center-y", SLPT_RES_INT),
	SLPT_RES_EMPTY_DEF("picture", SLPT_RES_MEM),
};

struct slpt_app_res *slpt_register_rotate_pic(struct rotate_pic *rpic,
											 struct slpt_app_res *parent,
											 struct slpt_app_res *array,
											 unsigned int size) {
	struct slpt_app_res *res;
	struct slpt_app_res tmp = SLPT_RES_DIR_DEF(rpic->name, rpic_res);

	slpt_set_res(rpic_res[0], &rpic->show, 4);
	slpt_set_res(rpic_res[1], &rpic->center.x, 4);
	slpt_set_res(rpic_res[2], &rpic->center.y, 4);
	slpt_set_res(rpic_res[3], rpic->pic_name, ARRAY_SIZE(rpic->pic_name));

	res = slpt_kernel_register_app_dir_res(&tmp, parent);
	if (!res) {
		pr_err("rotate pic: failed to regiseter pic to slpt: %s\n", rpic->name);
		return NULL;
	}

	if (array && size) {
		if (!slpt_kernel_register_app_child_res_list(array, size, res)) {
			pr_err("rotate pic: failed to register additional res list\n");
			slpt_kernel_unregister_app_res(res, uboot_slpt_task);
			res = NULL;
		}
	}

	rpic->res = res;

	return res;
}

void slpt_unregister_rotate_pic(struct rotate_pic *rpic) {
	slpt_kernel_unregister_app_res(rpic->res, uboot_slpt_task);
}
#endif
