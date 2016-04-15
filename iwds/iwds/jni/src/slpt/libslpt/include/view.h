#ifndef _VIEW_H_
#define _VIEW_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <common.h>
#include <string.h>
#include <picture.h>
#include <fb_struct.h>

#ifdef CONFIG_SLPT
#include <slpt.h>
#endif

#ifdef CONFIG_SLPT_LINUX
#include <time.h>
#endif

struct viewdesc {
	const char *s1;
	const char *s2;
	unsigned int type;
};

enum {
	VIEW_NUM,
	VIEW_FLASH_PIC,
	VIEW_PIC,
	VIEW_TEXT,
	VIEW_DIGITAL_CLOCK_EN,
	VIEW_DIGITAL_CLOCK_CN,
	VIEW_ANALOG_CLOCK,
	VIEW_ANALOG_WEEK_CLOCK,
	VIEW_ANALOG_MONTH_CLOCK,
	VIEW_ANALOG_SECOND_CLOCK,
	VIEW_ANALOG_MINUTE_CLOCK,
	VIEW_ANALOG_HOUR_CLOCK,
	VIEW_DATE_EN,
	VIEW_DATE_CN,
	VIEW_WEEK_EN,
	VIEW_WEEK_CN,
	VIEW_YEAR_EN,
	VIEW_TIME,

	/* keep last */
	VIEW_NUMS,
};

struct view {
	struct list_head link;
	struct list_head grp;
	struct view *parent;

	const char *name;
	unsigned int type;

	struct fb_region bg;
	struct position start;
	struct position end;
	unsigned int center_hor;
	unsigned int center_ver;
	unsigned int replace_mode;
	unsigned int replace_color;
	unsigned int alpha_mode; /* if alpha_mode=1, the alpha_color part of picture, will be changed to the background color*/
	unsigned int show;
	unsigned int follow_mode;
	unsigned int level;
	unsigned int updated;		/* private */
	unsigned int ready;
	unsigned int is_alloc;

	void (*display)(struct view *view);
	void (*pre_display)(struct view *view);
	void (*set_bg)(struct view *view, struct fb_region *bg);
	int (*sync)(struct view *view);
	void (*cal_size)(struct view *view, unsigned int *xmax, unsigned int *ymax);
	void (*freev)(struct view *view);

#ifdef CONFIG_SLPT
	struct slpt_app_res *(*register_slpt)(struct view *view, struct slpt_app_res *parent);
	struct slpt_app_res *res;
#endif

#ifdef CONFIG_SLPT_LINUX
	time_t time[10];
#endif
};

struct pic_view {
	struct view view;
	struct picture *pic;
	char pic_name[MAX_PIC_NAME_LEN];

#ifdef CONFIG_SLPT_LINUX
	time_t time[1];
#endif
};

struct num_view {
	struct view view;
	struct picture_grp *grp;
	unsigned int num;
	char grp_name[MAX_NAME_LEN];

#ifdef CONFIG_SLPT_LINUX
	time_t time[2];
#endif
};

struct flash_pic_view {
	struct view view;
	struct picture *pic;
	unsigned int display;
	unsigned int flash_mode;
	char pic_name[MAX_PIC_NAME_LEN];

#ifdef CONFIG_SLPT_LINUX
	time_t time[3];
#endif
};

struct text_view {
	struct view view;
};

/*
 * view
 */
static inline const char *view_name(struct view *v) {
	return v->name;
}

static inline unsigned int view_type(struct view *v) {
	return v->type;
}

extern void view_set_updated(struct view *v, int updated);
extern void free_view(struct view *v);
extern void view_display(struct view *v);
extern void view_pre_display(struct view *v);
extern void view_sync_start(struct view *v);

void view_init_status(struct view *v, const char *name, unsigned int type);

static inline int view_is_updated(struct view *v) {
	return !!v->updated;
}

static inline int view_want_to_show(struct view *v) {
	return !!v->show;
}

static inline void view_set_show(struct view *v, int show) {
	if (!!v->show ^ !!show) {
		view_set_updated(v, 1);
		v->show = !!show;
	}
}

static inline struct position *view_start(struct view *v) {
	return &v->start;
}

static inline void view_set_start(struct view *v, struct position *start) {
	if (!position_equal(&v->start, start)) {
		view_set_updated(v, 1);
		v->start = *start;
	}
}

static inline struct position *view_end(struct view *v) {
	return &v->end;
}

static inline void view_set_end(struct view *v, struct position *end) {
	v->end = *end;
}

static inline struct fb_region *view_bg(struct view *v) {
	return &v->bg;
}

static inline void view_set_bg(struct view *v, struct fb_region *bg) {
	if (!region_equal(&v->bg, bg)) {
		v->bg = *bg;
		view_set_updated(v, 1);
		v->set_bg(v, bg);
	}
}

static inline int view_is_ready(struct view *v) {
	return v->ready;
}

static inline int view_sync_setting(struct view *v) {
	int ret = v->sync(v);
	v->ready = !ret;
	if (v->ready)
		v->updated = 1;
	return ret;
}

static inline void view_cal_size(struct view *v, unsigned int *xmax, unsigned int *ymax) {
	if (view_is_ready(v)) {
		v->cal_size(v, xmax, ymax);
	} else {
		*xmax = 0;
		*ymax = 0;
	}
}

extern void view_grp_set_updated(struct list_head *grp, int updated);

static inline void view_set_follow(struct view *view, int follow) {
	if (!!view->follow_mode != !!follow) {
		/**
		 * @Todo
		 *    may be we should notify all other views we are updated
		 */
		view_set_updated(view, 1);
		view_grp_set_updated(&view->grp, 1);
		view->follow_mode = !!follow;
	}
}

static inline int view_alpha_mode(struct view *view) {
	return view->alpha_mode;
}

static inline int view_is_follow(struct view *view) {
	return view->follow_mode;
}

static inline int view_is_replace(struct view *view) {
	return view->replace_mode;
}

static inline unsigned int view_replace_color(struct view *view) {
	return view->replace_color;
}

static inline unsigned int view_level(struct view *view) {
	return view->level;
}

static inline int view_is_alloc(struct view *view) {
	return view->is_alloc;
}

/* view change to clock view */
#define to_digital_clock_en(v) ((struct digital_clock_en *)(v))

#define to_digital_clock_cn(v) ((struct digital_clock_cn *)(v))

#define to_analog_clock(v) ((struct analog_clock *)(v))

#define to_analog_base_clock(v) ((struct analog_base_clock *)(v))

#define to_analog_week_clock(v) ((struct analog_week_clock *)(v))

#define to_analog_month_clock(v) ((struct analog_month_clock *)(v))

#define to_analog_second_clock(v) ((struct analog_second_clock *)(v))

#define to_analog_minute_clock(v) ((struct analog_minute_clock *)(v))

#define to_analog_hour_clock(v) ((struct analog_hour_clock *)(v))

#define to_text_view(v) ((struct text_view *)(v))

#define to_num_view(v) ((struct num_view *)(v))

#define to_date_en_view(v) ((struct date_en_view *)(to_text_view(v)))

#define to_date_cn_view(v) ((struct date_cn_view *)(to_text_view(v)))

#define to_week_en_view(v) ((struct week_en_view *)(to_num_view(v)))

#define to_week_cn_view(v) ((struct week_cn_view *)(to_text_view(v)))

#define to_year_en_view(v) ((struct year_en_view *)(to_text_view(v)))

#define to_time_view(v) ((struct time_view *)(to_text_view(v)))

/* clock view change to view */
#define view_of_digital_clock_en_view(v) (&(v->view))

#define view_of_digital_clock_cn_view(v) (&(v->view))

#define view_of_analog_view(v)  (&(v->view))

#define view_of_analog_base_clock_view(v)  (&(v->view))

#define view_of_analog_week_clock_view(v)  (view_of_analog_base_clock_view(&(v)->clock))

#define view_of_analog_month_clock_view(v)  (view_of_analog_base_clock_view(&(v)->clock))

#define view_of_analog_second_clock_view(v)  (view_of_analog_base_clock_view(&(v)->clock))

#define view_of_analog_minute_clock_view(v)  (view_of_analog_base_clock_view(&(v)->clock))

#define view_of_analog_hour_clock_view(v)  (view_of_analog_base_clock_view(&(v)->clock))

#define view_of_text_view(v) (&(v)->view)

#define view_of_num_view(v) (&(v)->view)

#define view_of_date_en_view(v) (view_of_text_view(&(v)->text))

#define view_of_date_cn_view(v) (view_of_text_view(&(v)->text))

#define view_of_week_en_view(v) (view_of_num_view(&(v)->numv))

#define view_of_week_cn_view(v) (view_of_text_view(&(v)->text))

#define view_of_year_en_view(v) (view_of_text_view(&(v)->text))

#define view_of_time_view(v)    (view_of_text_view(&(v)->text))

static inline void num_view_set_num(struct view *view, unsigned int num) {
	struct num_view *nv = to_num_view(view);

	if (nv->num != num) {
		view_set_updated(view, 1);
		nv->num = num;
	}
}

/*
 * flash_pic view
 */
#define to_flash_pic_view(v) ((struct flash_pic_view *)(v))

static inline int flash_pic_view_is_flash(struct view *view) {
	struct flash_pic_view *fpv = to_flash_pic_view(view);

	return !!fpv->flash_mode;
}

static inline void flash_pic_view_set_flash_mode(struct view *view, unsigned int on) {
	struct flash_pic_view *fpv = to_flash_pic_view(view);

	if (fpv->flash_mode != on) {
		view_set_updated(view, 1);
		fpv->flash_mode = on;
	}
}

static inline int flash_pic_view_is_display(struct view *view) {
	struct flash_pic_view *fpv = to_flash_pic_view(view);

	return !!fpv->display;
}

static inline void flash_pic_view_set_display(struct view *view, unsigned int show) {
	struct flash_pic_view *fpv = to_flash_pic_view(view);

	if (fpv->display != show) {
		view_set_updated(view, 1);
		fpv->display = show;
	}
}

/*
 * pic view
 */
#define to_pic_view(v) ((struct pic_view *)(v))

static inline struct fb_region *pic_view_region(struct view *view) {
	struct pic_view *pv = to_pic_view(view);

	return picture_region(pv->pic);
}

/*
 * view grp
 */
static inline void view_add_child(struct view *view, struct view *child) {
	list_add_tail(&child->link, &view->grp);
	child->parent = view;
}

extern int view_grp_sync_strictly(struct list_head *grp);
extern void view_grp_set_bg(struct list_head *grp, struct fb_region *bg);
extern int view_grp_sync(struct list_head *grp);
extern void view_grp_display_continuously(struct list_head *grp, struct position **start);
extern void view_grp_display(struct list_head *grp, struct position **start);
extern void view_grp_set_updated(struct list_head *grp, int updated);
extern void view_grp_free(struct list_head *grp);
extern void view_grp_cal_size(struct list_head *grp, unsigned int *xmax, unsigned int *ymax);
extern void view_grp_cal_size_continuously(struct list_head *grp,
                                           unsigned int *xmax, unsigned int *ymax);

/* views: have a */
extern struct view *alloc_text_view(const char *name, struct view **array, unsigned int size);
extern struct view *alloc_num_view(const char *name, const char *pic_grp_name);
extern struct view *alloc_flash_pic_view(const char *name, const char *pic_name);
extern struct view *alloc_pic_view(const char *name, const char *pic_name);
extern struct view *alloc_digital_clock_en(const char *name);
extern struct view *alloc_digital_clock_cn(const char *name);
extern struct view *alloc_analog_clock(const char *name);
extern struct view *alloc_analog_week_clock(const char *name);
extern struct view *alloc_analog_month_clock(const char *name);
extern struct view *alloc_analog_second_clock(const char *name);
extern struct view *alloc_analog_minute_clock(const char *name);
extern struct view *alloc_analog_hour_clock(const char *name);
extern struct view *alloc_date_en_view(const char *name);
extern struct view *alloc_date_cn_view(const char *name);
extern struct view *alloc_week_en_view(const char *name);
extern struct view *alloc_week_cn_view(const char *name);
extern struct view *alloc_year_en_view(const char *name);
extern struct view *alloc_time_view(const char *name);

struct digital_clock_en;
struct digital_clock_cn;
struct analog_clock;
struct analog_week_clock;
struct analog_month_clock;
struct analog_second_clock;
struct analog_minute_clock;
struct analog_hour_clock;
struct date_en_view;
struct date_cn_view;
struct week_en_view;
struct week_cn_view;
struct year_en_view;
struct time_view;

/* views: is a */
extern int init_text_view(struct text_view *text, const char *name, struct view **array, unsigned int size);
extern int init_num_view(struct num_view *nv, const char *name, const char *pic_grp_name);
extern int init_flash_pic_view(struct flash_pic_view *fpv, const char *name, const char *pic_name);
extern int init_pic_view(struct pic_view *pv, const char *name, const char *pic_name);
extern int init_digital_clock_en(struct digital_clock_en *clock, const char *name);
extern int init_digital_clock_cn(struct digital_clock_cn *clock, const char *name);
extern int init_analog_clock(struct analog_clock *clock, const char *name);
extern int init_analog_week_clock(struct analog_week_clock *clock, const char *name);
extern int init_analog_month_clock(struct analog_month_clock *clock, const char *name);
extern int init_analog_second_clock(struct analog_second_clock *clock, const char *name);
extern int init_analog_minute_clock(struct analog_minute_clock *clock, const char *name);
extern int init_analog_hour_clock(struct analog_hour_clock *clock, const char *name);
extern int init_date_en_view(struct date_en_view *datev, const char *name);
extern int init_date_cn_view(struct date_cn_view *datev, const char *name);
extern int init_week_en_view(struct week_en_view *datev, const char *name);
extern int init_week_cn_view(struct week_cn_view *datev, const char *name);
extern int init_year_en_view(struct year_en_view *yearv, const char *name);
extern int init_time_view(struct time_view *timev, const char *name);

#ifdef CONFIG_SLPT
extern struct slpt_app_res *slpt_register_view_grp(struct list_head *grp,
												   struct slpt_app_res *parent);

extern struct slpt_app_res *slpt_register_view_base(struct view *view,
													struct slpt_app_res *parent,
													struct slpt_app_res *array,
													unsigned int size);
extern struct slpt_app_res *slpt_register_view(struct view *view,
											   struct slpt_app_res *parent,
											   struct slpt_app_res *array,
											   unsigned int size);
extern void slpt_unregister_view(struct view *view);
#endif

#ifdef CONFIG_SLPT_LINUX
extern void slpt_load_view_grp(struct list_head *grp);
extern void slpt_load_num_view(struct view *view);
extern void slpt_load_pic_view(struct view *view);
extern void slpt_load_text_view(struct view *view);
extern void slpt_load_digital_clock_en_view(struct view *view);
extern void slpt_load_digital_clock_cn_view(struct view *view);
extern void slpt_load_date_en_view(struct view *view);
extern void slpt_load_date_cn_view(struct view *view);
extern void slpt_load_week_en_view(struct view *view);
extern void slpt_load_week_cn_view(struct view *view);
extern void slpt_load_time_view(struct view *view);

extern void slpt_load_view(struct view *view);

extern void slpt_write_view_grp(struct list_head *grp);
extern void slpt_write_num_view(struct view *view);
extern void slpt_write_pic_view(struct view *view);
extern void slpt_write_text_view(struct view *view);
extern void slpt_write_digital_clock_en_view(struct view *view);
extern void slpt_write_digital_clock_cn_view(struct view *view);
extern void slpt_write_date_en_view(struct view *view);
extern void slpt_write_date_cn_view(struct view *view);
extern void slpt_write_week_en_view(struct view *view);
extern void slpt_write_week_cn_view(struct view *view);
extern void slpt_write_time_view(struct view *view);
extern void slpt_write_analog_clock_view(struct view *view);
extern void slpt_write_analog_week_clock_view(struct view *view);
extern void slpt_write_analog_month_clock_view(struct view *view);
extern void slpt_write_analog_second_clock_view(struct view *view);
extern void slpt_write_analog_minute_clock_view(struct view *view);
extern void slpt_write_analog_hour_clock_view(struct view *view);

extern void slpt_write_view(struct view *view);

extern void slpt_print_view_grp(struct list_head *grp, unsigned int enter_level);
extern void slpt_print_num_view(struct view *view, unsigned int enter_level);
extern void slpt_print_pic_view(struct view *view, unsigned int enter_level);
extern void slpt_print_text_view(struct view *view, unsigned int enter_level);
extern void slpt_print_digital_clock_en_view(struct view *view, unsigned int enter_level);
extern void slpt_print_digital_clock_cn_view(struct view *view, unsigned int enter_level);
extern void slpt_print_date_en_view(struct view *view, unsigned int enter_level);
extern void slpt_print_date_cn_view(struct view *view, unsigned int enter_level);
extern void slpt_print_week_en_view(struct view *view, unsigned int enter_level);
extern void slpt_print_week_cn_view(struct view *view, unsigned int enter_level);
extern void slpt_print_time_view(struct view *view, unsigned int enter_level);

extern void slpt_print_view(struct view *view, unsigned int enter_level);

#endif

/*
 * utils
 */
extern void assert_viewdesc(struct viewdesc *desc);
extern void free_views(struct view *views[], unsigned int size);
extern int alloc_views(struct view *views[], struct viewdesc *descs, unsigned int size);
extern struct view *alloc_views_to_text(const char *name,
                    struct view *views[], struct viewdesc *descs, unsigned int size);
extern const char *view_types[VIEW_NUMS];

extern unsigned int name_to_view_type(const char *str);
extern struct view *alloc_view_by_type(const char *name, unsigned int type);
extern struct view *alloc_view_by_str(const char *name, const char *str);

static inline const char *type_to_view_name(unsigned int type) {
	return view_types[type];
}

static inline struct view *viewdesc_to_num_view(struct viewdesc *desc) {
	return alloc_num_view(desc->s1, desc->s2);
}

static inline struct view *viewdesc_to_flash_pic_view(struct viewdesc *desc) {
	return alloc_flash_pic_view(desc->s1, desc->s2);
}

static inline struct view *viewdesc_to_pic_view(struct viewdesc *desc) {
	return alloc_pic_view(desc->s1, desc->s2);
}

extern void *malloc_with_name(unsigned int size, const char *name);

#ifdef __cplusplus
}
#endif
#endif /* _VIEW_H_ */
