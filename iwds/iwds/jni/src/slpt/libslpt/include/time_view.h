
#ifndef TIME_EN_CN_VIEW_H_
#define TIME_EN_CN_VIEW_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <view.h>
#include <current_time.h>
#include <time_notify.h>

#ifdef CONFIG_SLPT
#include <slpt.h>
#endif

struct time_view {
	struct text_view text;
	struct time_notify no;
	struct view *array[5];
	struct view *array2[3];

/*if the view-member of the function we need to re-define, we should store the parent's ,too. others we can persist it*/
	void (*parent_freev)(struct view *view);
};

#ifdef CONFIG_SLPT
extern struct slpt_app_res *slpt_register_text_view(struct view *view, struct slpt_app_res *parent);

static inline struct slpt_app_res *slpt_register_time(struct time_view *timev,
                                                                  struct slpt_app_res *parent) {
	return slpt_register_view(&timev->text.view, parent, NULL, 0);
}
#endif

#ifdef __cplusplus
}
#endif
#endif /* TIME_EN_CN_VIEW_H_ */
