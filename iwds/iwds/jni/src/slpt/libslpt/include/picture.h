#ifndef _PICTURE_H_
#define _PICTURE_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <common.h>
#include <fb_struct.h>

#ifdef CONFIG_SLPT
#include <slpt.h>
#endif

#ifdef CONFIG_SLPT_LINUX
#include <time.h>
#endif

#define MAX_NAME_LEN 30
#define MAX_PIC_NAME_LEN (MAX_NAME_LEN * 2)

extern struct list_head picture_grp;

struct picture_desc {
	const char *name;
	unsigned int size;
};

struct picture {
	const char *name;
	char *buffer;
	unsigned int size;
	struct fb_region *region;
#ifdef CONFIG_SLPT_LINUX
	time_t time;
#endif
};

struct picture_grp {
	const char *name;
	struct picture **array;
	unsigned int size;
	unsigned int all_pictures_ok:1;
	struct list_head link;
#ifdef CONFIG_SLPT
	struct slpt_app_res *res;
#endif
};

extern int picture_sync(struct picture *picture);

static inline struct fb_region *picture_region(struct picture *picture) {
	return picture ? picture->region : NULL;
}

static inline struct fb_region *picture_grp_region(struct picture_grp *grp, unsigned int num) {
	assert(grp->size > num);

	return picture_region(grp->array[num]);
}

extern int picture_grp_sync(struct picture_grp *grp);
extern void free_picture_grp(struct picture_grp *grp);
extern int free_picture_grp_by_name(const char *grp_name);
extern void free_all_picture_grp(void);
extern struct picture_grp *alloc_picture_grp(const char *name, struct picture_desc *descs, unsigned int size);
extern struct picture_grp *get_picture_grp(const char *name);
extern void put_picture_grp(struct picture_grp *grp);

extern int picture_sync(struct picture *pic);
struct picture *get_picture_internal(const char *grp_name, const char *pic_name);
extern struct picture *get_picture(const char *name);
extern void put_picture(struct picture *pic);
extern struct picture *alloc_picture(const char *name, unsigned int size);
extern struct picture *alloc_picture2(const char *grp_name, const char *pic_name, unsigned int size);
extern struct picture *alloc_picture_to_grp(struct picture_grp *grp, const char *pic_name, unsigned int size);

extern void print_picture(struct picture *pic);
extern void print_picture_grp(struct picture_grp *grp);
extern void print_all_picture_grp(void);

#ifdef CONFIG_SLPT
extern struct slpt_app_res *slpt_register_picture_grp(struct picture_grp *grp);
extern void slpt_unregister_picture_grp(struct picture_grp *grp);
#endif

#define PICTURE_ERR_TAG  "PIC-err"
#define PICTURE_TAG      "PIC-tag"
#define PICTURE_TAG_SIZE 8

/* picture no bmp mode header, user do not need to know it */
struct picture_header {
	char tag[PICTURE_TAG_SIZE];
	unsigned int xres;
	unsigned int yres;
	unsigned int len;
	struct fb_region region;
	char mem[0];
};

extern int check_picture_header(struct picture_header *header);
extern int picture_sync_no_bmp(struct picture *pic);
extern struct picture_header *create_picture_header(char *buffer);
extern void free_picture_header(struct picture_header *header);
extern int write_picture_header(const char *filename, struct picture_header *header);
extern int write_bmp_by_picture_header(const char *path, char *buffer);

#ifdef __cplusplus
}
#endif
#endif /* _PICTURE_H_ */
