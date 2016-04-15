#include <jni.h>
#include <common.h>
#include <sview/sview.h>
#include <sview/sview_utils.h>
#include <key_reader.h>
#include <key_writer.h>
#include <slpt_ioctl.h>
#include <slpt_display_dispatcher.h>

#include "com_ingenic_iwds_slpt_SlptClock.h"

int slpt_init_ok = 0;

int init_slpt() {
	int ret;

	if(slpt_init_ok)
		return 0;

	ret = slpt_ioctl_load_default_firmware();
	if (ret) {
		pr_err("init slpt: failed to load firmware \n");
		return ret;
	}

	ret = slpt_ioctl_enable_default_task();
	if (ret) {
		pr_err("init slpt: failed to enable default task \n");
		return ret;
	}

	usleep(100 * 1000); // wait for the file node from slpt

	ret = slpt_display_dispatcher_init(0, NULL);
	if (ret) {
		pr_err("init slpt: failed to init display dispatcher \n");
		goto disable_task;
	}

	slpt_init_ok = 1;
	pr_err("slpt init ok!");

	return 0;
disable_task:
	slpt_ioctl_disable_default_task();
	return ret;
}

int enable_slpt() {
	int ret;

	if(!slpt_init_ok)
		return -ENODEV;

	slpt_display_dispatcher_enable_fb();

	ret = slpt_ioctl_enable_default_task();
	if (ret) {
		pr_err("init slpt: failed to enable default task \n");
	}

	return ret;
}

int disable_slpt() {
	int ret;
	if (!slpt_init_ok)
		return -ENODEV;

	slpt_display_dispatcher_disable_fb();

	ret = slpt_ioctl_disable_default_task();
	if (ret) {
		pr_err("init slpt: failed to disable default task \n");
	}

	return ret;
}

int set_brightness_of_slpt(int brightness) {
	if (!slpt_init_ok)
		return -ENODEV;
	slpt_display_dispatcher_set_brightness(brightness);

	return 0;
}

void request_slpt_display_pause() {
	if (slpt_init_ok)
		slpt_display_dispatcher_pause();
}

void request_slpt_display_resume() {
	if (slpt_init_ok)
		slpt_display_dispatcher_resume();
}

void init_sview(int *sview_byte_arr, int arr_length)
{
	void *buffer = NULL;
	int length = 0;
	struct key_reader *reader;
	struct sview *root_view;

	if (!slpt_init_ok) {
		pr_err("init sview: slpt not inited\n");
		return;
	}

	buffer = sview_byte_arr;
	if(buffer == NULL){
		pr_err("Get the ByteArray Failed!\n");
		return;
	}
	length = arr_length;
	
	reader = alloc_key_reader(buffer, length);
	
	root_sview_free();
	sview_reset_id_counter();

	root_view = create_sview_from_key_reader(reader);
	if(root_view)
		set_root_sview(root_view);

	slpt_ioctl_init_sview(buffer, length);

	free_key_reader(reader);
}

/*
 * Class:     com_ingenic_iwds_slpt_SlptClock
 * Method:    initSlpt
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_ingenic_iwds_slpt_SlptClock_initSlpt
(JNIEnv *env, jclass clazz) {
	int ret;

	ret = init_slpt();

	return ret;
}

/*
 * Class:     com_ingenic_iwds_slpt_SlptClock
 * Method:    enableSlpt
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_ingenic_iwds_slpt_SlptClock_enableSlpt
(JNIEnv *env, jclass clazz) {
	int ret;

	ret = enable_slpt();

	return ret;
}

/*
 * Class:     com_ingenic_iwds_slpt_SlptClock
 * Method:    disableSlpt
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_ingenic_iwds_slpt_SlptClock_disableSlpt
(JNIEnv *env, jclass clazz) {
	int ret;

	ret = disable_slpt();

	return ret;
}

/*
 * Class:     com_ingenic_iwds_slpt_SlptClock
 * Method:    setBrightnessOfSlpt
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_ingenic_iwds_slpt_SlptClock_setBrightnessOfSlpt
(JNIEnv *env, jclass clazz, jint brightness) {
	int ret;

	ret = set_brightness_of_slpt(brightness);

	return ret;
}

/*
 * Class:     com_ingenic_iwds_slpt_SlptClock
 * Method:    requestSlptDisplayPause
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_SlptClock_requestSlptDisplayPause
(JNIEnv *env, jclass clazz) {
	request_slpt_display_pause();
}

/*
 * Class:     com_ingenic_iwds_slpt_SlptClock
 * Method:    requestSlptDisplayResume
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_SlptClock_requestSlptDisplayResume
(JNIEnv *env, jclass clazz) {
	request_slpt_display_resume();
}

/*
 * Class:     com_ingenic_iwds_slpt_SlptClock
 * Method:    initSview
 * Signature: (J)V
 */
JNIEXPORT void JNICALL  Java_com_ingenic_iwds_slpt_SlptClock_initSview__J
(JNIEnv *env, jclass clazz, jlong writer_address) {
	struct key_writer *writer;
	struct key_reader *reader;
	struct sview *root_view;

	if (!slpt_init_ok) {
		pr_err("init sview: slpt not inited\n");
		return;
	}
	pr_debug("key reader: writer address %llx\n", writer_address);

	if (writer_address == 0) {
		pr_info("key reader: writer address is null!");
		return;
	}

	writer = jlong_to_address(writer_address);
	reader = alloc_key_reader(kw_get_buffer(writer), kw_get_size(writer));

	root_sview_free();
	sview_reset_id_counter();

//	print_all_picture_grp();

	root_view = create_sview_from_key_reader(reader);
	if (root_view)
		set_root_sview(root_view);

	slpt_ioctl_init_sview(kw_get_buffer(writer), kw_get_size(writer));

	free_key_reader(reader);
}

/*
 * Class:     com_ingenic_iwds_slpt_SlptClock
 * Method:    initSview
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_SlptClock_initSview___3B
(JNIEnv *env, jclass clazz, jbyteArray byte_arr) {
	void *buffer = NULL;
	int length = 0;
	struct key_reader *reader;
	struct sview *root_view;

	if (!slpt_init_ok) {
		pr_err("init sview: slpt not inited\n");
		return;
	}

	buffer = (*env)->GetByteArrayElements(env, byte_arr, NULL);
	if (buffer == NULL) {
		pr_err("Get the ByteArray Failed!\n");
		return;
	}
	length = (*env)->GetArrayLength(env, byte_arr);

	reader = alloc_key_reader(buffer, length);
	root_sview_free();
	sview_reset_id_counter();

	root_view = create_sview_from_key_reader(reader);
	if (root_view)
		set_root_sview(root_view);

	slpt_ioctl_init_sview(buffer, length);

	(*env)->ReleaseByteArrayElements(env, byte_arr, (jbyte *) buffer, 0);

	free_key_reader(reader);
}
