#include <jni.h>
#include <common.h>
#include <key_reader.h>
#include <key_writer.h>

#include "com_ingenic_iwds_slpt_view_utils_KeyWriter.h"

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    initialize_native
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_initialize_1native
(JNIEnv *env, jobject obj) {
	struct key_writer *writer;

	writer = alloc_key_writer(512);

	return address_to_jlong(writer);
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    recycle
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_recycle
(JNIEnv *env, jobject obj, jlong address) {
	struct key_writer *writer = jlong_to_address(address);

	free_key_writer(writer);
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    getBytes
 * Signature: (J)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_getBytes
(JNIEnv *env, jobject obj, jlong address) {
	struct key_writer *writer = jlong_to_address(address);

	jbyteArray array = (*env)->NewByteArray(env, kw_get_size(writer));
	(*env)->SetByteArrayRegion(env, array, 0, kw_get_size(writer), (jbyte *)kw_get_buffer(writer));

	return array;
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    getSize
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_getSize
(JNIEnv *env, jobject obj, jlong address) {
	struct key_writer *writer = jlong_to_address(address);

	return kw_get_size(writer);
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeByte
 * Signature: (JC)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeByte
(JNIEnv *env, jobject obj, jlong address, jbyte val) {
	struct key_writer *writer = jlong_to_address(address);

	kw_write_char(writer, (char) val);
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeBoolean
 * Signature: (JC)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeBoolean
  (JNIEnv *env, jobject obj, jlong address, jbyte val) {
	struct key_writer *writer = jlong_to_address(address);

	kw_write_bool(writer, (char) val);
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeShort
 * Signature: (JS)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeShort
  (JNIEnv *env, jobject obj, jlong address, jshort val) {
	struct key_writer *writer = jlong_to_address(address);

	kw_write_short(writer, val);
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeInt
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeInt
  (JNIEnv *env, jobject obj, jlong address, jint val) {
	struct key_writer *writer = jlong_to_address(address);

	kw_write_int(writer, val);
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeLong
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeLong
  (JNIEnv *env, jobject obj, jlong address, jlong val) {
	struct key_writer *writer = jlong_to_address(address);

	kw_write_longlong(writer, val);
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeFloat
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeFloat
  (JNIEnv *env, jobject obj, jlong address, jfloat val) {
	struct key_writer *writer = jlong_to_address(address);

	kw_write_float(writer, val);
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeDouble
 * Signature: (JD)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeDouble
  (JNIEnv *env, jobject obj, jlong address, jdouble val) {
	struct key_writer *writer = jlong_to_address(address);

	kw_write_double(writer, val);
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeString
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeString
  (JNIEnv *env, jobject obj, jlong address, jstring str) {
	struct key_writer *writer = jlong_to_address(address);
	const char *buffer;

	buffer = (*env)->GetStringUTFChars(env, str, NULL);

	kw_write_string(writer, buffer);
	(*env)->ReleaseStringUTFChars(env, str, buffer);
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeByteArray
 * Signature: (J[BII)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeByteArray
  (JNIEnv *env, jobject obj, jlong address, jbyteArray array, jint position, jint length) {
	struct key_writer *writer = jlong_to_address(address);
	char *buffer;

	buffer = (char *) (*env)->GetByteArrayElements(env, array, NULL);
	kw_write_char_array(writer, buffer + position, length);
	(*env)->ReleaseByteArrayElements(env, array, (jbyte *) buffer, 0);
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeBooleanArray
 * Signature: (J[ZII)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeBooleanArray
  (JNIEnv *env, jobject obj, jlong address, jbooleanArray array, jint position, jint length) {
	struct key_writer *writer = jlong_to_address(address);
	char *buffer;

	buffer = (char *) (*env)->GetBooleanArrayElements(env, array, NULL);
	kw_write_bool_array(writer, buffer + position, length);
	(*env)->ReleaseBooleanArrayElements(env, array, (jboolean *)buffer, 0);
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeShortArray
 * Signature: (J[SII)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeShortArray
  (JNIEnv *env, jobject obj, jlong address, jshortArray array, jint position, jint length) {
	struct key_writer *writer = jlong_to_address(address);
	short *buffer;

	buffer = (*env)->GetShortArrayElements(env, array, NULL);
	kw_write_short_array(writer, buffer + position, length);
	(*env)->ReleaseShortArrayElements(env, array, buffer, 0);
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeIntArray
 * Signature: (J[III)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeIntArray
  (JNIEnv *env, jobject obj, jlong address, jintArray array, jint position, jint length) {
	struct key_writer *writer = jlong_to_address(address);
	int *buffer;

	buffer = (*env)->GetIntArrayElements(env, array, NULL);
	kw_write_int_array(writer, buffer + position, length);
	(*env)->ReleaseIntArrayElements(env, array, buffer, 0);
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeLongArray
 * Signature: (J[JII)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeLongArray
  (JNIEnv *env, jobject obj, jlong address, jlongArray array, jint position, jint length) {
	struct key_writer *writer = jlong_to_address(address);
	long long *buffer;

	buffer = (*env)->GetLongArrayElements(env, array, NULL);
	kw_write_longlong_array(writer, buffer + position, length);
	(*env)->ReleaseLongArrayElements(env, array, buffer, 0);
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeFloatArray
 * Signature: (J[FII)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeFloatArray
  (JNIEnv *env, jobject obj, jlong address, jfloatArray array, jint position, jint length) {
	struct key_writer *writer = jlong_to_address(address);
	float *buffer;

	buffer = (*env)->GetFloatArrayElements(env, array, NULL);
	kw_write_float_array(writer, buffer + position, length);
	(*env)->ReleaseFloatArrayElements(env, array, buffer, 0);
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeDoubleArray
 * Signature: (J[DII)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeDoubleArray
  (JNIEnv *env, jobject obj, jlong address, jdoubleArray array, jint position, jint length) {
	struct key_writer *writer = jlong_to_address(address);
	double *buffer;

	buffer = (*env)->GetDoubleArrayElements(env, array, NULL);
	kw_write_double_array(writer, buffer + position, length);
	(*env)->ReleaseDoubleArrayElements(env, array, buffer, 0);
}
