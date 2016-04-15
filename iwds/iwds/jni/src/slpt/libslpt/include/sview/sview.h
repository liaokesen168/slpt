#ifndef _SVIEW_H_
#define _SVIEW_H_

#ifdef __cplusplus
extern "C" {
#endif

#include <sview/sview_base.h>
#include <sview/sview_utils.h>

/*
 * all kind of sview's header file
 */
#include <sview/pic_sview.h>
#include <sview/num_sview.h>
#include <sview/linear_layout.h>
#include <sview/absolute_layout.h>
#include <sview/frame_layout.h>
#include <sview/time_num_sview.h>
#include <sview/secondL_sview.h>
#include <sview/secondH_sview.h>
#include <sview/minuteL_sview.h>
#include <sview/minuteH_sview.h>
#include <sview/hourL_sview.h>
#include <sview/hourH_sview.h>
#include <sview/dayL_sview.h>
#include <sview/dayH_sview.h>
#include <sview/week_sview.h>
#include <sview/monthL_sview.h>
#include <sview/monthH_sview.h>
#include <sview/year0_sview.h>
#include <sview/year1_sview.h>
#include <sview/year2_sview.h>
#include <sview/year3_sview.h>
#include <sview/rotate_pic_sview.h>
#include <sview/analog_time_sview.h>
#include <sview/analog_second_sview.h>
#include <sview/analog_minute_sview.h>
#include <sview/analog_hour_sview.h>
#include <sview/analog_day_sview.h>
#include <sview/analog_week_sview.h>
#include <sview/analog_month_sview.h>
#include <sview/analog_am_pm_sview.h>
#include <sview/analog_hour_with_minute_sview.h>

/*
 * sview methods
 */
extern int sview_sync_setting(struct sview *view);
extern void sview_measure_size(struct sview *view);
extern void sview_draw(struct sview *view);
extern void sview_free(struct sview *view);

#ifdef CONFIG_SLPT
extern struct slpt_app_res *slpt_register_sview
(struct sview *view,
 struct slpt_app_res *parent,
 struct slpt_app_res *array,
 unsigned int size);

extern void slpt_unregister_sview(struct sview *view);
#endif

/*
 * sview methods of root sview
 */
extern void root_sview_sync_setting(void);
extern void root_sview_measure_size(void);
extern void root_sview_draw(void);
extern void root_sview_free(void);
extern struct sview *get_root_sview(void);
extern void set_root_sview(struct sview *view);
/* NOTICE: only layout can add child */
extern void root_sview_add(struct sview *child);
extern void root_sview_add_array(struct sview **array, unsigned int size);
extern struct sview *root_sview_find(const char *name);

/* sview methods of id counter */
extern unsigned short sview_get_id_nums(void);
extern void sview_reset_id_counter(void);

#ifdef __cplusplus
}
#endif

#endif /* _SVIEW_H_ */
