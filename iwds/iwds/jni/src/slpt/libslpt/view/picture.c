#include <common.h>
#include <list.h>
#include <picture.h>
#include <view.h>
#include <slpt_file.h>

extern void *malloc_with_name(unsigned int size, const char *name);

#undef pr_debug
#ifdef CONFIG_SLPT_DEBUG_PICTURE
int pr_debug_picture = 1;
#else
int pr_debug_picture = 0;
#endif

#define pr_debug(x...)							\
	do {										\
		if (pr_debug_picture)					\
			pr_err(x);							\
	} while (0)

LIST_HEAD(picture_grp);

#ifndef CONFIG_PICTURE_NO_BMP
int picture_no_bmp = 0;
#else
int picture_no_bmp = 1;
#endif

void picture_init(struct picture *pic) {
	return;
}

int picture_sync(struct picture *pic) {
	if (!pic || !pic->buffer)
		return -ENODEV;

	if (picture_no_bmp)
		picture_sync_no_bmp(pic);
	else
		bmp_file_to_fb_region(pic->buffer, &pic->region);
	pr_debug("picture: sync: pic: [%s] [%s]\n",
			 pic->name, pic->region ? "ok" : "No");

	return !pic->region;
}

int picture_grp_sync(struct picture_grp *grp) {
	unsigned int i;
	int errs = 0;

	pr_debug("picture: sync: grp: [%s]\n", grp->name);

	for (i = 0; i < grp->size; ++i) {
		if (picture_sync(grp->array[i])) {
			errs += 1;
		}
	}

	grp->all_pictures_ok = !!errs;
	return errs;
}

void put_picture_grp(struct picture_grp *grp) {
	return;
}

struct picture_grp *get_picture_grp(const char *name) {
	struct picture_grp *g;
	struct list_head *pos;
	int ret;

	assert(name != NULL);

	list_for_each(pos, &picture_grp) {
		g = list_entry(pos, struct picture_grp, link);
		ret = strcmp(name, g->name);
		if (ret == 0)
			return g;
	}

	pr_err("picture grp: no pic grp named: (%s)\n", name);

	return NULL;
}

void put_picture(struct picture *pic) {
	/**
	 * @Todo
	 *   may be we should use use cnt to free the allocated memory when no other handle the picture
	 */
	return;
}

struct picture *get_picture_internal(const char *grp_name, const char *pic_name) {
	struct picture_grp *grp;
	unsigned int i;

	grp = get_picture_grp(grp_name);
	if (!grp)
		return NULL;

	for (i = 0; i < grp->size; ++i) {
		if (grp->array[i]->name && !strcmp(grp->array[i]->name, pic_name))
			return grp->array[i];
	}

	pr_err("picture: no pic named: (%s) in pic grp (%s)\n", pic_name, grp_name);
	return NULL;
}

struct picture *get_picture(const char *name) {
	char buf[MAX_PIC_NAME_LEN];
	unsigned int len = strlen(name);
	char *p;

	if (len >= MAX_PIC_NAME_LEN) {
		pr_err("picture: (%s) is over the max name len %d\n", name, MAX_PIC_NAME_LEN);
		return NULL;
	}

	memcpy(buf, name, len + 1);
	p = strchr(buf, '/');

	if (p == NULL || p[1] == '\0') {
		pr_debug("picture: (%s) is invalid for an picture (style: xxxDIR/xxxpic)\n", name);
		return NULL;
	}

	p[0] = '\0'; p++;

	return get_picture_internal(buf, p);
}

#ifdef CONFIG_SLPT
struct slpt_app_res *slpt_register_picture_grp(struct picture_grp *grp) {
	unsigned int i;
	struct slpt_app_res *res;
	struct slpt_app_res grp_res = {.type = SLPT_RES_DIR};
	struct slpt_app_res pic_res = {.type = SLPT_RES_MEM};
	struct slpt_app_res *pic_dir = slpt_kernel_name_to_app_res("pictures", uboot_slpt_task);

	assert(pic_dir != NULL);

	grp_res.name = grp->name;
	slpt_set_res(grp_res, NULL, 0);

	res = slpt_kernel_register_app_dir_res(&grp_res, pic_dir);
	if (!res) {
		pr_err("picture grp: failed to register pic_grp to slpt: %s\n", grp->name);
		return NULL;
	}

	for (i = 0; i < grp->size; ++i) {
		pic_res.name = grp->array[i]->name;
		slpt_set_res(pic_res, grp->array[i]->buffer, grp->array[i]->size);
		if (!slpt_kernel_register_app_child_res(&pic_res, res)) {
			pr_err("picture grp: failed to register pic to slpt: %s ---> %s\n",
				   grp->array[i]->name, grp->name);
			slpt_kernel_unregister_app_res(res, uboot_slpt_task);
			return NULL;
		}
	}

	grp->res = res;

	return res;
}

void slpt_unregister_picture_grp(struct picture_grp *grp) {
	struct slpt_app_res *res;
	struct slpt_app_res *pic_dir = slpt_kernel_name_to_app_res("pictures", uboot_slpt_task);

	assert(pic_dir != NULL);

	res = slpt_kernel_name_to_app_child_res(grp->name, pic_dir);
	slpt_kernel_unregister_app_res(res, uboot_slpt_task);
}

static struct slpt_app_res *slpt_register_picture(struct picture_grp *grp, struct picture *pic) {
	struct slpt_app_res *res;
	struct slpt_app_res pic_res = {.type = SLPT_RES_MEM};

	assert(grp->res);

	pic_res.name = pic->name;
	slpt_set_res(pic_res, pic->buffer, pic->size);
	res = slpt_kernel_register_app_child_res(&pic_res, grp->res); 
	if (!res) {
		pr_err("picture grp: failed to register pic to slpt: %s ---> %s\n",
                pic->name, grp->name);
		return NULL;
	}

	return res;
}

static void slpt_unregister_picture(struct picture_grp *grp, struct picture *pic) {
	struct slpt_app_res *res;

	assert(grp->res);
	res = slpt_kernel_name_to_app_child_res(pic->name, grp->res);
	assert(res != NULL);
	slpt_kernel_unregister_app_res(res, uboot_slpt_task);
}

#endif

static void free_picture_region(struct picture *pic) {
	if (!picture_no_bmp) {
		if (pic->region) {
			if (pic->region->base)
				free(pic->region->base);
			free(pic->region);
		}
	}

	pic->region = NULL;
}

static void free_picture(struct picture *pic) {
	if (!pic)
		return;

	free_picture_region(pic);

	if (pic->buffer)
		free(pic->buffer);

	free(pic);
}

void free_picture_grp(struct picture_grp *grp) {
	unsigned int i;

	if (!grp)
		return;

#ifdef CONFIG_SLPT
	slpt_unregister_picture_grp(grp);
#endif

	list_del(&grp->link);
	for (i = 0; i < grp->size; ++i) {
		free_picture(grp->array[i]);
		grp->array[i] = NULL;
	}

	free(grp);
}

int free_picture_grp_by_name(const char *grp_name) {
	struct picture_grp *grp;

	grp = get_picture_grp(grp_name);
	if (!grp)
		return -ENODEV;

	free_picture_grp(grp);

	return 0;
}

void free_all_picture_grp(void) {
	struct list_head *pos, *n;

	list_for_each_safe(pos, n, &picture_grp) {
		struct picture_grp *grp =  list_entry(pos, struct picture_grp, link);

		free_picture_grp(grp);
	}
}

static int add_one_picture(struct picture_grp *grp, struct picture *pic) {
	struct picture **array;

#ifdef CONFIG_SLPT
	if (!slpt_register_picture(grp, pic)) {
		pr_err("picture: failed to register to slpt\n");
		return -ENOMEM;
	}
#endif

	array = realloc(grp->array, (grp->size + 1) * sizeof(*grp->array));
	if (!array) {
#ifdef CONFIG_SLPT
		slpt_unregister_picture(grp, pic);
#endif
		return -ENOMEM;
	}

	grp->array = array;
	array[grp->size] = pic;
	grp->size += 1;

	return 0;
}

struct picture *alloc_picture_struct(const char *pic_name, unsigned int size) {
	struct picture *pic;

	if (!pic_name || strlen(pic_name) >= MAX_NAME_LEN)
		return NULL;

	pic = malloc_with_name(sizeof(*pic), pic_name);
	if (!pic)
		return NULL;
	pic->name = (char *)pic + sizeof(*pic);

	if (size) {
		pic->buffer = malloc(size);
		if (!pic->buffer) {
			free(pic);
			return NULL;
		}
	} else {
		pic->buffer = NULL;
	}

	pic->region = NULL;
	pic->size = size;
#ifdef CONFIG_SLPT_LINUX
	pic->time = 0;
#endif

	return pic;
}

struct picture *alloc_picture_to_grp(struct picture_grp *grp, const char *pic_name, unsigned int size) {
	struct picture *pic;
	unsigned int i;

	if (grp == NULL)
		return NULL;

	for (i = 0; i < grp->size; ++i) {
		if (grp->array[i]->name && !strcmp(grp->array[i]->name, pic_name)) {
			pr_err("picture: picture: [%s] already be registered\n", pic_name);
			return NULL;
		}
	}

	pic = alloc_picture_struct(pic_name, size);
	if (!pic)
		return NULL;

	if (add_one_picture(grp, pic)) {
		pr_err("picture: [%s] failed to add to grp [%s]\n", pic_name, grp->name);
		free_picture(pic);
		return NULL;
	}

	return pic;
}

static struct picture *alloc_picture_internal(const char *grp_name, const char *pic_name, unsigned int size) {
	struct picture_grp *grp;
	struct picture *pic;
	unsigned int i;

	grp = get_picture_grp(grp_name);
	if (!grp) {
		grp = alloc_picture_grp(grp_name, NULL, 0);
		if (!grp)
			return NULL;
	}

	for (i = 0; i < grp->size; ++i) {
		if (grp->array[i]->name && !strcmp(grp->array[i]->name, pic_name)) {
			pr_err("picture: picture: [%s] already be registered\n", pic_name);
			return NULL;
		}
	}

	pic = alloc_picture_struct(pic_name, size);
	if (!pic)
		return NULL;

	if (add_one_picture(grp, pic)) {
		pr_err("picture: [%s] failed to add to grp [%s]\n", pic_name, grp_name);
		free_picture(pic);
		return NULL;
	}

	return pic;
}

struct picture *alloc_picture2(const char *grp_name, const char *pic_name, unsigned int size) {
	return alloc_picture_internal(grp_name, pic_name, size);
}

struct picture *alloc_picture(const char *name, unsigned int size) {
	char buf[MAX_PIC_NAME_LEN];
	unsigned int len = strlen(name);
	char *p;

	if (len >= MAX_PIC_NAME_LEN) {
		pr_err("picture: (%s) is over the max name len %d\n", name, MAX_PIC_NAME_LEN);
		return NULL;
	}

	memcpy(buf, name, len + 1);
	p = strchr(buf, '/');

	if (p == NULL || p[1] == '\0') {
		pr_debug("picture: (%s) is invalid for an picture (style: xxxDIR/xxxpic)\n", name);
		return NULL;
	}

	p[0] = '\0'; p++;

	return alloc_picture_internal(buf, p, size);
}

struct picture_grp *alloc_picture_grp(const char *name, struct picture_desc *descs, unsigned int size) {
	struct picture_grp *grp;
	struct picture_grp *g;
	struct list_head *pos;
	unsigned int i;
	int ret;

	if (name == NULL || strlen(name) >= MAX_NAME_LEN)
		return NULL;

	grp = malloc_with_name(sizeof(*grp), name);
	if (!grp)
		return NULL;

	if (size) {
		grp->array = malloc(size * sizeof(*grp->array));
		if (!grp->array) {
			free(grp);
			return NULL;
		}
	} else {
		grp->array = NULL;
	}

	grp->name = (char *)grp + sizeof(*grp);
	grp->size = size;

	list_for_each(pos, &picture_grp) {
		g = list_entry(pos, struct picture_grp, link);
		ret = strcmp(name, g->name);
		if (ret == 0) {
			pr_err("picture grp: %s already exist\n", name);
			goto free_grp;
		}
//		else if (ret < 0) /* sort the grp as its name, if don't do it, the new grp is always added at tail */
//			break;
	}

	for (i = 0; i < size; ++i) {
		grp->array[i] = alloc_picture_struct(descs[i].name, descs[i].size);
		if (!grp->array[i]) {
			pr_err("picture_grp: failed to alloc picture struct: [%s] [%d]\n",
                   descs[i].name, descs[i].size);
			goto free_pictures;
		}
		picture_init(grp->array[i]);
	}

#ifdef CONFIG_SLPT
	if (!slpt_register_picture_grp(grp)) {
		pr_err("picture grp: failed to register pic grp to slpt: %s\n", grp->name);
		goto free_pictures;
	}
#endif

	list_add_tail(&grp->link, pos);

	return grp;
free_pictures:
	for (; --i > 0;)
		free_picture(grp->array[i]);
free_grp:
	free(grp);
	return NULL;
}

void print_picture(struct picture *pic) {
	pr_err ("pic: [%s] %u bytes\n", pic->name, pic->size);
}

void print_picture_grp(struct picture_grp *grp) {
	unsigned int i;

	pr_err ("grp: [%s] %u pictures\n", grp->name, grp->size);
	for (i = 0; i < grp->size; ++i) {
		print_picture(grp->array[i]);
	}
}

void print_all_picture_grp(void) {
	struct list_head *pos;

	list_for_each(pos, &picture_grp) {
		struct picture_grp *grp =  list_entry(pos, struct picture_grp, link);
		print_picture_grp(grp);
	}
}

#ifdef CONFIG_SLPT_LINUX

#define SLPT_RES_ROOT "/sys/slpt/apps/slpt-app/res/"

static int set_picture_buffer(struct picture *pic, char *buffer, unsigned int size) {

	free_picture_region(pic);

	if (pic->buffer)
		free(pic->buffer);

	pic->buffer = buffer;
	pic->size = size;

	return 0;
}

void slpt_sync_pic(struct picture *pic) {
	struct slpt_file file;

	slpt_file_init_status(&file, pic->name, pic->time);

	if (slpt_load_file(&file)) {
		pr_err("picture grp: failed to load file (%s)\n", pic->name);
		return;
	}

	if (file.mtime != pic->time) {
		set_picture_buffer(pic, file.buf, file.size);
		pic->time = file.mtime;
		if (!picture_no_bmp) {
			pic->buffer[0] = 'B';	/* we need to re-tag 'BM' */
			pic->buffer[1] = 'M';
		}
	}

	pr_debug("%s --> picture [%d.%dKB] %s\n",
			 pic->name, pic->size / 1024, (pic->size % 1024) * 1000 / 1024,
			file.update ? "Y" : "N");
}

void slpt_sync_pic_grp(struct picture_grp *grp) {
	unsigned int i;

	if(chdir(grp->name) != 0) {
		pr_err("Couldn`t change (%s) diretory!", grp->name);
		return ;
	}

	pr_debug("%s --> pic_grp\n", grp->name);

	for (i = 0; i < grp->size; ++i) {
		if (grp->array[i]->name)
			slpt_sync_pic(grp->array[i]);
	}

	chdir("..");
}

void slpt_sync_pictures(void) {
	struct picture_grp *g;
	struct list_head *pos;

	char cur_dir[MAX_FILE_NAME] = SLPT_RES_ROOT;

	getcwd(cur_dir, MAX_FILE_NAME);

	if(chdir(SLPT_RES_ROOT) != 0) {
		pr_err("Couldn`t change (%s) diretory!", SLPT_RES_ROOT);
		return ;
	}
	chdir("pictures");

	list_for_each(pos, &picture_grp) {
		g = list_entry(pos, struct picture_grp, link);
		slpt_sync_pic_grp(g);
	}

	chdir(cur_dir);
}
#endif

