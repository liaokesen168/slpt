#include <jni.h>
#include <common.h>
#include <picture.h>
#include <slpt_ioctl.h>
#include "com_ingenic_iwds_slpt_view_core_Picture_PictureContainer.h"
//#include <sys/types.h>

extern int slpt_init_ok;

static struct picture_grp *cur_grp = NULL;

void clear_picture_group() {
	if (!slpt_init_ok)
		return;

	free_all_picture_grp();
	cur_grp = NULL;

	slpt_ioctl_clear_picture_grp();
}

void add_picture_group(char *grp_name) {
	char *str;

	if (!slpt_init_ok)
		return;

	str = grp_name;
	cur_grp = alloc_picture_grp(str, NULL, 0);
	slpt_ioctl_add_picture_grp(str);
}

void add_picture(char *pic_name, int width, int height, unsigned int *array, int array_size, int background_color) {
	const char *str;
	unsigned int *mem, *dst, *src;
	int i, j;
	unsigned int size;
	struct picture_header *header;
	struct picture *pic;

	if (!slpt_init_ok)
		return;

	if (!cur_grp)
		return;

	size = sizeof(*header) + (width * height * 4);

	str = pic_name;
	pic = alloc_picture_to_grp(cur_grp, str, size);
	if (!pic)
		return;

	header = (struct picture_header *)pic->buffer;
	strcpy(header->tag, PICTURE_TAG);
	header->xres = width;
	header->yres = height;
	header->len = size;
	header->region.xres = width;
	header->region.yres = height;
	header->region.pixels_per_line = width;
	header->region.base = header->mem;
	header->region.bpp = 32;

	mem = (width == 0 || height == 0) ? NULL : array;
	src = mem;
	dst = (unsigned int *)header->mem;

	for (i = 0; i < height; ++i) {
		for (j = 0; j < width; ++j) {
			if (*src == (unsigned int)background_color)
				*dst = 0x00ffffff;
			else if ((*src & 0xff000000) == 0x00)
				*dst = 0x00ffffff;
			else if ((*src & 0x00ffffff) == 0x00ffffff)
				*dst = 0x00fffffe;
			else
				*dst = *src & 0x00ffffff;
			++src;
			++dst;
		}
	}

	slpt_ioctl_add_picture(str, header, size);
}

/*
 * Class:     com_ingenic_iwds_slpt_view_core_Picture_PictureContainer
 * Method:    clearPictureGroup
 * Signature: ()V
 */

JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_core_Picture_00024PictureContainer_clearPictureGroup
(JNIEnv *env, jclass clazz) {
	clear_picture_group();
}

/*
 * Class:     com_ingenic_iwds_slpt_view_core_Picture_PictureContainer
 * Method:    addPictureGroup
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_core_Picture_00024PictureContainer_addPictureGroup
(JNIEnv *env, jclass clazz, jstring grp_name) {
	char *str;

	if (!slpt_init_ok)
		return;
	//pr_err("%s %d  t_id=%d \n", __FUNCTION__, __LINE__, gettid());

	str = (char *)(*env)->GetStringUTFChars(env, grp_name, NULL);
	add_picture_group(str);
	//cur_grp = alloc_picture_grp(str, NULL, 0);
	//slpt_ioctl_add_picture_grp(str);
	(*env)->ReleaseStringUTFChars(env, grp_name, str);
}

/*
 * Class:     com_ingenic_iwds_slpt_view_core_Picture_PictureContainer
 * Method:    addPicture
 * Signature: (Ljava/lang/String;II[II)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_core_Picture_00024PictureContainer_addPicture
(JNIEnv *env, jclass clazz, jstring pic_name, jint width, jint height, jintArray array, jint background_color) {
	const char *str;
	unsigned int *mem, *dst, *src;
	int i, j;
	unsigned int size;
	struct picture_header *header;
	struct picture *pic;

	if (!slpt_init_ok)
		return;

	if (!cur_grp)
		return;

	//pr_err("%s %d  t_id=%d \n", __FUNCTION__, __LINE__, gettid());

	size = sizeof(*header) + (width * height * 4);

	str = (*env)->GetStringUTFChars(env, pic_name, NULL);
	pic = alloc_picture_to_grp(cur_grp, str, size);
	if (!pic)
		return;

	header = (struct picture_header *)pic->buffer;
	strcpy(header->tag, PICTURE_TAG);
	header->xres = width;
	header->yres = height;
	header->len = size;
	header->region.xres = width;
	header->region.yres = height;
	header->region.pixels_per_line = width;
	header->region.base = header->mem;
	header->region.bpp = 32;

	mem = (width == 0 || height == 0) ? NULL : (unsigned int *)(*env)->GetIntArrayElements(env, array, NULL);
	src = mem;
	dst = (unsigned int *)header->mem;

	for (i = 0; i < height; ++i) {
		for (j = 0; j < width; ++j) {
			if (*src == (unsigned int)background_color)
				*dst = 0x00ffffff;
			else if ((*src & 0xff000000) == 0x00)
				*dst = 0x00ffffff;
			else if ((*src & 0x00ffffff) == 0x00ffffff)
				*dst = 0x00fffffe;
			else
				*dst = *src & 0x00ffffff;
			++src;
			++dst;
		}
	}

	slpt_ioctl_add_picture(str, header, size);

	if (mem)
		(*env)->ReleaseIntArrayElements(env, array, (jint *)mem, 0);
}
