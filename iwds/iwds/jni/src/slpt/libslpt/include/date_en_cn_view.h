
#ifndef DATE_EN_VIEW_H_
#define DATE_EN_VIEW_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <view.h>
#include <current_time.h>
#include <time_notify.h>

#ifdef CONFIG_SLPT
#include <slpt.h>
#endif

struct date_en_view {
	struct text_view text;
	struct time_notify no;
	struct view *array[10];
	struct view *array2[5];

/*if the view-member of the function we need to re-define, we should store the parent's ,too. others we can persist it*/
	void (*parent_freev)(struct view *view);
};

struct date_cn_view {
	struct text_view text;
	struct time_notify no;
	struct view *array[6];
	struct view *array2[4];

/*if the view-member of the function we need to re-define, we should store the parent's ,too. others we can persist it*/
	void (*parent_freev)(struct view *view);
};

#ifdef CONFIG_SLPT
extern struct slpt_app_res *slpt_register_text_view(struct view *view, struct slpt_app_res *parent);

static inline struct slpt_app_res *slpt_register_date_en(struct date_en_view *datev,
                                                                  struct slpt_app_res *parent) {
	return slpt_register_view(&datev->text.view, parent, NULL, 0);
}

static inline struct slpt_app_res *slpt_register_date_cn(struct date_cn_view *datev,
                                                                  struct slpt_app_res *parent) {
	return slpt_register_view(&datev->text.view, parent, NULL, 0);
}
#endif

#ifdef __cplusplus
}
#endif
#endif /* _DIGITAL_CLOCK_H_ */
