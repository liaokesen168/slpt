#ifndef _SLPT_DISPLAY_DISPATCHER_H_
#define _SLPT_DISPLAY_DISPATCHER_H_
#ifdef __cplusplus
extern "C" {
#endif

extern void slpt_display_dispatcher_enable_fb(void);
extern void slpt_display_dispatcher_disable_fb(void);

extern void slpt_display_dispatcher_power_on_lcd(void);
extern void slpt_display_dispatcher_power_off_lcd(void);

extern void slpt_display_dispatcher_set_brightness(unsigned int brightness);

extern int slpt_display_dispatcher_init(int argc, char **argv);
extern void slpt_display_dispatcher_exit(void);
extern void slpt_display_dispatcher_pause(void);
extern void slpt_display_dispatcher_resume(void);

#ifdef __cplusplus
}
#endif
#endif /* _SLPT_DISPLAY_DISPATCHER_H_ */
