#include <common.h>
#include <item_parser.h>
#include <sview/sview.h> 

static struct sview *root_sview = NULL;

void root_sview_sync_setting(void) {
	if (!root_sview)
		return;

	sview_sync_setting(root_sview);
}

void root_sview_measure_size(void) {
	struct fb_region *region;

	if (!root_sview)
		return;

	/* to fit the frame buffer size */
	region = get_current_fb_region();
	root_sview->rect.w = region->xres;
	root_sview->rect.h = region->yres;
	root_sview->desc_w = RECT_SPECIFY;
	root_sview->desc_h = RECT_SPECIFY;

	sview_measure_size(root_sview);
}

void root_sview_draw(void) {
	struct picture *save_pic;
	unsigned int save_color;

	if (!root_sview)
		return;

	save_pic = background_picture(&root_sview->background);
	save_color = background_color(&root_sview->background);

	/* position set to (0, 0) */
	root_sview->raw_position.x = 0;
	root_sview->raw_position.y = 0;
	root_sview->position = root_sview->raw_position;

	/* base fb_region is frame buffer */
	root_sview->base = *get_current_fb_region();

	/* write background with no alpha method, it will a little faster */
	background_write_to_target_no_alpha(&root_sview->background, &root_sview->base, &root_sview->position);
	background_set_color(&root_sview->background, INVALID_COLOR);
	background_set_picture(&root_sview->background, NULL);

	sview_draw(root_sview);

	background_set_picture(&root_sview->background, save_pic);
	background_set_color(&root_sview->background, save_color);
}

void root_sview_free(void) {
	if (!root_sview)
		return;

	sview_free(root_sview);
	root_sview = NULL;
}

struct sview *get_root_sview(void) {
	return root_sview;
}

void set_root_sview(struct sview *view) {
	assert(view);

	if (root_sview)
		sview_free(root_sview);

	root_sview = view;
}

/* NOTICE: only layout can add child */
void root_sview_add(struct sview *child) {
	if (!root_sview)
		return;

	switch (root_sview->type) {
	case SVIEW_LINEAR_LAYOUT:
		linear_layout_add(root_sview, child);
		break;
	case SVIEW_ABSOLUTE_LAYOUT:
		absolute_layout_add(root_sview, child);
		break;
	default:
		break;
	}
}

/* NOTICE: only layout can add child */
void root_sview_add_array(struct sview **array, unsigned int size) {
	unsigned int i;

	if (!(array && size))
		return;

	for (i = 0; i < size; ++i) {
		root_sview_add(array[i]);
	}
}

static struct sview *root_sview_find_inner(struct sview *view, const char *child_name) {
	switch (view->type) {
	case SVIEW_LINEAR_LAYOUT:
	case SVIEW_ABSOLUTE_LAYOUT:
		break;
	default:
		return NULL;
	}

	return sview_grp_find(&view->grp, child_name);
}

struct sview *root_sview_find(const char *name) {
	struct item_parser parser;
	const char *child_name;
	struct sview *parent = root_sview;
	struct sview *view = NULL;

	if (!root_sview)
		return NULL;
	
	if (!name)
		return NULL;

	init_item_parser(&parser, name, '/');

	while ((child_name = item_parser_next(&parser)) != NULL) {
		view = root_sview_find_inner(parent, child_name);
		if (!view)
			break;
		parent = view;
	}

	destory_item_parser(&parser);

	return view;
}
