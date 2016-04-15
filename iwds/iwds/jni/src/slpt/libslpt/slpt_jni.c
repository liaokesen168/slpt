#include <jni.h>
#include "com_ingenic_iwds_slpt_view_SlptView.h"

#include <common.h>
#include <view.h>

extern int default_pictures_init_onetime(void);

JNIEXPORT jlong JNICALL Java_com_ingenic_iwds_slpt_view_SlptView_initSlptJni
(JNIEnv *env, jclass jc) {
	struct fb_region region;
	int ret;

	ret = slpt_load_fb_region(&region);
	assert(!ret);
	set_current_fb_region(&region);
	default_pictures_init_onetime();
}

JNIEXPORT jlong JNICALL Java_com_ingenic_iwds_slpt_view_SlptView_createJniView
(JNIEnv *env, jobject obj, jstring name, jstring type) {
	struct view *view;
	const char *view_type = (*env)->GetStringUTFChars(env, type, NULL);

	view = alloc_view_by_str(name, view_type);
	assert(view);

	pr_info("slpt-jni: view addr : %d\n", (unsigned int)view);

	return (jlong) (unsigned long)view;
}

JNIEXPORT jint JNICALL Java_com_ingenic_iwds_slpt_view_SlptView_syncViewMember__JLjava_lang_String_2I
(JNIEnv *env, jobject obj, jlong view_addr, jstring member, jint value) {

}

JNIEXPORT jint JNICALL Java_com_ingenic_iwds_slpt_view_SlptView_syncViewMember__JLjava_lang_String_2Ljava_lang_String_2
(JNIEnv *env, jobject obj, jlong view_addr, jstring member, jstring value) {

}
