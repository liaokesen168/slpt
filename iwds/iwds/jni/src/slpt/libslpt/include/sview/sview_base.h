#ifndef _SVIEW_BASE_H_
#define _SVIEW_BASE_H_

#ifdef __cplusplus
extern "C" {
#endif

#include <common.h>
#include <string.h>
#include <picture.h>
#include <fb_struct.h>
#include <sview/sview_background.h>
#include <sview/sview_grp.h>

#ifdef CONFIG_SLPT
#include <slpt.h>
#endif

struct padding {
	unsigned short left;
	unsigned short right;
	unsigned short top;
	unsigned short bottom;
};

struct rect {
	unsigned int w;	/* width of rect */
	unsigned int h;	/* height of rect */
};

/* align */
#define ALIGN_LEFT   0
#define ALIGN_RIGHT  1
#define ALIGN_TOP    0
#define ALIGN_BOTTOM 1
#define ALIGN_CENTER 2
#define ALIGN_BY_PARENT 3

/* rect descript */
#define RECT_FIT_BACKGROUND 0
#define RECT_WRAP_CONTENT   1
#define RECT_SPECIFY        2

/**
 * struct sview - base struct of view in slpt
 * @type: type of view
 * @id: unique id of view
 * @name: name of view
 * @position: the position will use to draw on @base: x, y
 * @raw_position: the configure position of view
 * @padding: view's padding: left, right, top, bottom
 * @rect: to sepcify view's rect area: w, h
 * @raw_rect: the real rect area of view's content
 * @background: color or picture to descript view's background
 * @level: the order of view to be drawn, the lower is first.
 * @align_x: x align: ALIGN_LEFT, ALIGN_CENTER, ALIGN_RIGHT
 * @align_y: y align: ALIGN_TOP, ALIGN_CENTER, ALIGN_BOTTOM
 * @desc_w: descript how the view's rect will be: RECT_FIT_BACKGROUND, RECT_WRAP_CONTENT, RECT_SPECIFY
 * @desc_h: descript how the view's rect will be: RECT_FIT_BACKGROUND, RECT_WRAP_CONTENT, RECT_SPECIFY
 * @center_horizontal: when view is in absolute_layout, it's position.x can be center horizontal
 * @center_vertical: when view is in absolute_layout, it's position.y can be center vertical
 * @show: view will be draw or not
 * @ready: view is ready for draw or not
 * @update: view's config
 * @is_alloc: view is come from malloc or not
 * @base: the base fb_region, view will draw to.
 * @link: link to parent view's grp
 * @grp: list_head to add child view
 * @parent: parent view
 */
struct sview {
	unsigned short type;
	unsigned short id;
	const char *name;
	struct position position;
	struct position raw_position;
	struct padding padding;
	struct rect rect;
	struct rect raw_rect;
	struct background background;
	unsigned short level;
	unsigned char align_x;
	unsigned char align_y;
	unsigned char desc_w;
	unsigned char desc_h;
	unsigned char center_horizontal;
	unsigned char center_vertical;
	unsigned char align_parent_x;
	unsigned char align_parent_y;
	unsigned char show;
	unsigned int ready:1;
	unsigned int update:1;
	unsigned int is_alloc:1;
	unsigned int layout_align_x:1;
	unsigned int layout_align_y:1;

	struct fb_region base;
	struct list_head link;
	struct list_head grp;
	struct sview *parent;

#ifdef CONFIG_SLPT
	struct slpt_app_res *res;
#endif
};

/**
 * sview type
 */
enum {
	SVIEW_PIC,
	SVIEW_NUM,
	SVIEW_LINEAR_LAYOUT,
	SVIEW_ABSOLUTE_LAYOUT,
	SVIEW_FRAME_LAYOUT,
	SVIEW_TIME_NUM,
	SVIEW_SECOND_L,
	SVIEW_SECOND_H,
	SVIEW_MINUTE_L,
	SVIEW_MINUTE_H,
	SVIEW_HOUR_L,
	SVIEW_HOUR_H,
	SVIEW_DAY_L,
	SVIEW_DAY_H,
	SVIEW_WEEK,
	SVIEW_MONTH_L,
	SVIEW_MONTH_H,
	SVIEW_YEAR0,
	SVIEW_YEAR1,
	SVIEW_YEAR2,
	SVIEW_YEAR3,
	SVIEW_ROTATE_PIC,
	SVIEW_ANALOG_TIME,
	SVIEW_ANALOG_SECOND,
	SVIEW_ANALOG_MINUTE,
	SVIEW_ANALOG_HOUR,
	SVIEW_ANALOG_DAY,
	SVIEW_ANALOG_WEEK,
	SVIEW_ANALOG_MONTH,
	SVIEW_ANALOG_AM_PM,
	SVIEW_ANALOG_HOUR_WITH_MINUTE,

	/* keep last */
	SVIEW_NUMS,
};

/* 
 * sview methods function pointer array
 */
extern void (*sview_method_init_view[SVIEW_NUMS])(void *view, const char *name);
extern struct sview *(*sview_method_alloc_view[SVIEW_NUMS])(const char *name);
extern void (*sview_method_measure_size[SVIEW_NUMS])(struct sview *view);
extern void (*sview_method_draw[SVIEW_NUMS])(struct sview *view);
extern int (*sview_method_sync_setting[SVIEW_NUMS])(struct sview *view);
extern void (*sview_method_free[SVIEW_NUMS])(struct sview *view);
#ifdef CONFIG_SLPT
extern struct slpt_app_res *(*sview_method_register_slpt[SVIEW_NUMS])(struct sview *view, struct slpt_app_res *parent);
#endif

/* 
 * inline functions for call sview methods function
 * a new kind of sview which base on a old type may need those functions.
 */
static inline void init_sview_by_type(void *view, const char *name, unsigned int type) {
	sview_method_init_view[type](view, name);
}

static inline struct sview *alloc_sview_by_type(const char *name, unsigned int type) {
	return sview_method_alloc_view[type](name);
}

static inline void sview_measure_size_by_type(struct sview *view, unsigned int type) {
	sview_method_measure_size[type](view);
}

static inline void sview_draw_by_type(struct sview *view, unsigned int type) {
	sview_method_draw[type](view);
}

static inline int sview_sync_setting_by_type(struct sview *view, unsigned int type) {
	return sview_method_sync_setting[type](view);
}

static inline void sview_free_by_type(struct sview *view, unsigned int type) {
	sview_method_free[type](view);
}

#ifdef CONFIG_SLPT
static inline struct slpt_app_res *slpt_register_sview_by_type(struct sview *view, struct slpt_app_res *parent, unsigned int type) {
	return sview_method_register_slpt[type](view, parent);
}
#endif

/*
 * all kind of sview, must let it's parent sview be the first member of it's struct
 */
#define to_sview(view) ((struct sview *) (view))

/*
 * sview methods
 */
extern void sview_init_base(struct sview *view, const char *name, unsigned int type);
extern int sview_sync_setting(struct sview *view);
extern void sview_measure_size(struct sview *view);
extern void sview_draw(struct sview *view);
extern void sview_free(struct sview *view);

#ifdef CONFIG_SLPT
extern struct slpt_app_res *slpt_register_sview_base
(struct sview *view,
 struct slpt_app_res *parent,
 struct slpt_app_res *array,
 unsigned int size);

extern struct slpt_app_res *slpt_register_sview
(struct sview *view,
 struct slpt_app_res *parent,
 struct slpt_app_res *array,
 unsigned int size);

extern void slpt_unregister_sview(struct sview *view);
#endif

static inline unsigned char sview_align(unsigned char align_parent, unsigned char layout_align) {
	return align_parent == ALIGN_BY_PARENT ? layout_align : align_parent;
}

static inline int sview_can_be_show(struct sview *view) {
	return view->show && view->ready;
}

#ifdef __cplusplus
}
#endif
#endif /* _SVIEW_BASE_H_ */
