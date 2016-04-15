#ifndef _SLPT_IOCTL_H_
#define _SLPT_IOCTL_H_
#ifdef __cplusplus
extern "C" {
#endif

extern int slpt_ioctl(void *hdr, unsigned int hdr_len, void *mem, unsigned int mem_len, unsigned int cmd);
extern void slpt_ioctl_close(void);
extern int slpt_ioctl_load_default_firmware(void);
extern int slpt_ioctl_enable_default_task(void);
extern int slpt_ioctl_disable_default_task(void);

extern int slpt_ioctl_init_sview(void *mem, unsigned int size);
extern int slpt_ioctl_clear_picture_grp(void);
extern int slpt_ioctl_add_picture_grp(const char *grp_name);
extern int slpt_ioctl_add_picture(const char *pic_name, void *mem, unsigned int size);

#ifdef __cplusplus
}
#endif
#endif /* _SLPT_IOCTL_H_ */
