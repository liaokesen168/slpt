#ifndef _ROTATE_PIC_H_
#define _ROTATE_PIC_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <picture.h>
#include <rotate.h>
#include <asm/errno.h>
#ifdef CONFIG_SLPT
#include <slpt.h>
#endif

struct rotate_pic {
	struct rotate rt;
	const char *name;
	char pic_name[MAX_PIC_NAME_LEN];
	struct position center;
	unsigned int show;
	unsigned int ready;
	struct picture *pic;
#ifdef CONFIG_SLPT
	struct slpt_app_res *res;
#endif

#ifdef CONFIG_SLPT_LINUX
	time_t time[4];
#endif
};

extern int rotate_pic_sync(struct rotate_pic *rpic);
extern int init_rotate_pic(struct rotate_pic *rpic, const char *name, const char *pic_name);
extern void destory_rotate_pic(struct rotate_pic *rpic);

static inline void rotate_pic_restore(struct rotate_pic *rpic) {
	pr_debug("rpic: restore %s\n", rpic->name);
	if (rpic->show && rpic->ready)
		rotate_restore(&rpic->rt);
}

static inline int rotate_pic_save_and_draw(struct rotate_pic *rpic, unsigned int angle) {
	pr_debug("rpic: save draw %s\n", rpic->name);
	pr_debug("draw: rpic: [%d]  (%d, %d) [%s] \n",
			rpic->show,
			rpic->center.x,
			rpic->center.y,
			rpic->pic_name);
	if (rpic->show && rpic->ready)
		return rotate_save_and_draw(&rpic->rt, angle);
	return -EINVAL;
}

#ifdef CONFIG_SLPT
extern struct slpt_app_res *slpt_register_rotate_pic(struct rotate_pic *rpic,
                                              struct slpt_app_res *parent,
                                              struct slpt_app_res *array,
                                              unsigned int size);

extern void slpt_unregister_rotate_pic(struct rotate_pic *rpic);
#endif

#ifdef __cplusplus
}
#endif
#endif /* _ROTATE_PIC_H_ */
