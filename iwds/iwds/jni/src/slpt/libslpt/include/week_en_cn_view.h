#ifndef WEEK_EN_CN_VIEW_H_
#define WEEK_EN_CN_VIEW_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <view.h>
#include <current_time.h>
#include <time_notify.h>

#ifdef CONFIG_SLPT
#include <slpt.h>
#endif

struct week_en_view {
	struct num_view numv;
	struct time_notify no;

/*if the view-member of the function we need to re-define, we should store the parent's ,too. others we can persist it*/
	void (*parent_freev)(struct view *view);
};

struct week_cn_view {
	struct text_view text;
	struct time_notify no;
	struct view *array[2];

/*if the view-member of the function we need to re-define, we should store the parent's ,too. others we can persist it*/
	void (*parent_freev)(struct view *view);
};

#ifdef CONFIG_SLPT
extern struct slpt_app_res *slpt_register_num_view(struct view *view, struct slpt_app_res *parent);
extern struct slpt_app_res *slpt_register_text_view(struct view *view, struct slpt_app_res *parent);

static inline struct slpt_app_res *slpt_register_week_en(struct week_en_view *weekv,
                                                                  struct slpt_app_res *parent) {
	return slpt_register_view(&weekv->numv.view, parent, NULL, 0);
}

static inline struct slpt_app_res *slpt_register_week_cn(struct week_cn_view *weekv,
                                                                  struct slpt_app_res *parent) {
	return slpt_register_view(&weekv->text.view, parent, NULL, 0);
}
#endif

#ifdef __cplusplus
}
#endif
#endif /* WEEK_EN_CN_VIEW_H_ */
