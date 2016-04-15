#include <common.h>
#include <sview/sview.h>
#include <sview/sview_background.h>

int background_set_pic(struct background *background, const char *pic_name) {
	if (pic_name == NULL || strlen(pic_name) >= sizeof(background->pic_name))
		return -EINVAL;

	strcpy(background->pic_name, pic_name);

	return 0;
}

int background_sync_setting(struct background *background) {
	struct picture *pic;
	int sync;

	pic = get_picture(background->pic_name);
	if (!pic) {
		/* pr_info("pic_view: can not get pic: %s\n", background->pic_name); */
		sync = -ENODEV;
	} else {
		sync = picture_sync(pic);
	}

	put_picture(background->pic);
	background->pic = pic;

	return sync;
}

void background_write_to_target(struct background *background, struct fb_region *base, struct position *pos) {
	if (background->pic != NULL) {
		fb_region_write_alpha(base, picture_region(background->pic), pos, ALPHA32BIT);
		return;
	}

	if (background->color != INVALID_COLOR) {
		fb_region_clear(base, background->color);
	}
}

void background_write_to_target_no_alpha(struct background *background, struct fb_region *base, struct position *pos) {
	if (background->pic != NULL) {
		fb_region_write(base, picture_region(background->pic), pos);
		return;
	}

	if (background->color != INVALID_COLOR) {
		fb_region_clear(base, background->color);
	}
}
