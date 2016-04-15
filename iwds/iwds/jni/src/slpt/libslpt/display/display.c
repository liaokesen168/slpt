#include <common.h>
#include <sview/sview.h>

extern int slpt_time_tick(void);

int init_before_display(void) {
	return 0;
}

void destory_after_display(void) {
	return;
}

void slpt_sync_sview(void) {
	root_sview_sync_setting();
	set_time_notify_level(TIME_TICK_YEAR);
}

int slpt_display_sview(void) {
	if (!slpt_time_tick())
		return -1;

	pr_debug("slpt_display_sview : time tick\n");
	time_notify();
	root_sview_measure_size();
	root_sview_draw();

	return 0;
}
