#include <jni.h>
#include <common.h>
#include <key_reader.h>
#include <key_writer.h>

#include "com_ingenic_iwds_slpt_view_utils_KeyReader.h"

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyReader
 * Method:    initialize_native
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyReader_initialize_1native
(JNIEnv *env, jobject obj, jlong writer_address) {
	struct key_reader *reader;
	struct key_writer *writer;

	pr_info("key reader: writer address %llx\n", writer_address);

	if (writer_address == 0) {
		pr_info("key reader: writer address is null!");
		return 0;
	}

	writer = jlong_to_address(writer_address);
	reader = alloc_key_reader(kw_get_buffer(writer), kw_get_size(writer));

	return address_to_jlong(reader);
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyReader
 * Method:    recycle
 * Signature: (J)J
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyReader_recycle
(JNIEnv *env, jobject obj, jlong address) {
	struct key_reader *reader = jlong_to_address(address);

	free_key_reader(reader);
}

static void setResultCode(JNIEnv *env, jobject obj, int result) {
	jfieldID filed;
	jclass clazz = (*env)->FindClass(env, "com/ingenic/iwds/slpt/view/utils/KeyReader");

	if (clazz == NULL) {
		pr_info("key reader: can not find the class! ");
		return;
	}

	filed = (*env)->GetFieldID(env, clazz, "result_code", "I");
	if (filed == NULL) {
		pr_info("key reader: can not find the filed ! ");
		return;
	}

	(*env)->SetIntField(env, obj, filed, result);
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyReader
 * Method:    readByte
 * Signature: (J)B
 */
JNIEXPORT jbyte JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyReader_readByte
  (JNIEnv *env, jobject obj, jlong address) {
	struct key_reader *reader = jlong_to_address(address);
	int ret;
	char val = 0;

	ret = kr_read_char(reader, &val);
	if (ret < 0)
		setResultCode(env, obj, ret);

	return (jbyte) val;
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyReader
 * Method:    readBoolean
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyReader_readBoolean
  (JNIEnv *env, jobject obj, jlong address) {
	struct key_reader *reader = jlong_to_address(address);
	int ret;
	char val = 0;

	ret = kr_read_bool(reader, &val);
	if (ret < 0)
		setResultCode(env, obj, ret);

	return (jboolean) val;
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyReader
 * Method:    readShort
 * Signature: (J)S
 */
JNIEXPORT jshort JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyReader_readShort
  (JNIEnv *env, jobject obj, jlong address) {
	struct key_reader *reader = jlong_to_address(address);
	int ret;
	short val = 0;

	ret = kr_read_short(reader, &val);
	if (ret < 0)
		setResultCode(env, obj, ret);

	return val;
}


/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyReader
 * Method:    readInt
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyReader_readInt
  (JNIEnv *env, jobject obj, jlong address) {
	struct key_reader *reader = jlong_to_address(address);
	int ret;
	int val = 0;

	ret = kr_read_int(reader, &val);
	if (ret < 0)
		setResultCode(env, obj, ret);

	return val;
}


/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyReader
 * Method:    readLong
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyReader_readLong
  (JNIEnv *env, jobject obj, jlong address) {
	struct key_reader *reader = jlong_to_address(address);
	int ret;
	long long val = 0;

	ret = kr_read_longlong(reader, &val);
	if (ret < 0)
		setResultCode(env, obj, ret);

	return val;
}


/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyReader
 * Method:    readFloat
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyReader_readFloat
  (JNIEnv *env, jobject obj, jlong address) {
	struct key_reader *reader = jlong_to_address(address);
	int ret;
	float val = 0;

	ret = kr_read_float(reader, &val);
	if (ret < 0)
		setResultCode(env, obj, ret);

	return val;
}


/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyReader
 * Method:    readDouble
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyReader_readDouble
  (JNIEnv *env, jobject obj, jlong address) {
	struct key_reader *reader = jlong_to_address(address);
	int ret;
	double val = 0;

	ret = kr_read_double(reader, &val);
	if (ret < 0)
		setResultCode(env, obj, ret);

	return val;
}


/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyReader
 * Method:    readString
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyReader_readString
  (JNIEnv *env, jobject obj, jlong address) {
	struct key_reader *reader = jlong_to_address(address);
	int ret;
	char *val = 0;
	jstring str;

	ret = kr_create_string(reader, &val);
	if (ret < 0) {
		setResultCode(env, obj, ret);
		return NULL;
	}

	str = (*env)->NewStringUTF(env, val);
	free(val);

	return str;
}

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyReader
 * Method:    readByteArray
 * Signature: (J)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyReader_readByteArray
  (JNIEnv *env, jobject obj, jlong address) {
	struct key_reader *reader = jlong_to_address(address);
	int ret;
	char *val = 0;
	jbyteArray array;

	ret = kr_create_char_array(reader, &val);
	if (ret < 0) {
		setResultCode(env, obj, ret);
		return NULL;
	}

	array = (*env)->NewByteArray(env, ret);
	(*env)->SetByteArrayRegion(env, array, 0, ret, (jbyte *)val);
	free(val);

	return array;
}


/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyReader
 * Method:    readBooleanArray
 * Signature: (J)[Z
 */
JNIEXPORT jbooleanArray JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyReader_readBooleanArray
  (JNIEnv *env, jobject obj, jlong address) {
	struct key_reader *reader = jlong_to_address(address);
	int ret;
	char *val = 0;
	jbooleanArray array;

	ret = kr_create_bool_array(reader, &val);
	if (ret < 0) {
		setResultCode(env, obj, ret);
		return NULL;
	}

	array = (*env)->NewBooleanArray(env, ret);
	(*env)->SetBooleanArrayRegion(env, array, 0, ret, (jboolean *)val);
	free(val);

	return array;
}


/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyReader
 * Method:    readShortArray
 * Signature: (J)[S
 */
JNIEXPORT jshortArray JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyReader_readShortArray
  (JNIEnv *env, jobject obj, jlong address) {
	struct key_reader *reader = jlong_to_address(address);
	int ret;
	short *val = 0;
	jshortArray array;

	ret = kr_create_short_array(reader, &val);
	if (ret < 0) {
		setResultCode(env, obj, ret);
		return NULL;
	}

	array = (*env)->NewShortArray(env, ret);
	(*env)->SetShortArrayRegion(env, array, 0, ret, val);
	free(val);

	return array;
}


/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyReader
 * Method:    readIntArray
 * Signature: (J)[I
 */
JNIEXPORT jintArray JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyReader_readIntArray
  (JNIEnv *env, jobject obj, jlong address) {
	struct key_reader *reader = jlong_to_address(address);
	int ret;
	int *val = 0;
	jintArray array;

	ret = kr_create_int_array(reader, &val);
	if (ret < 0) {
		setResultCode(env, obj, ret);
		return NULL;
	}

	array = (*env)->NewIntArray(env, ret);
	(*env)->SetIntArrayRegion(env, array, 0, ret, val);
	free(val);

	return array;
}


/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyReader
 * Method:    readLongArray
 * Signature: (J)[J
 */
JNIEXPORT jlongArray JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyReader_readLongArray
  (JNIEnv *env, jobject obj, jlong address) {
	struct key_reader *reader = jlong_to_address(address);
	int ret;
	long long *val = 0;
	jlongArray array;

	ret = kr_create_longlong_array(reader, &val);
	if (ret < 0) {
		setResultCode(env, obj, ret);
		return NULL;
	}

	array = (*env)->NewLongArray(env, ret);
	(*env)->SetLongArrayRegion(env, array, 0, ret, val);
	free(val);

	return array;
}


/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyReader
 * Method:    readFloatArray
 * Signature: (J)[F
 */
JNIEXPORT jfloatArray JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyReader_readFloatArray
  (JNIEnv *env, jobject obj, jlong address) {
	struct key_reader *reader = jlong_to_address(address);
	int ret;
	float *val = 0;
	jfloatArray array;

	ret = kr_create_float_array(reader, &val);
	if (ret < 0) {
		setResultCode(env, obj, ret);
		return NULL;
	}

	array = (*env)->NewFloatArray(env, ret);
	(*env)->SetFloatArrayRegion(env, array, 0, ret, val);
	free(val);

	return array;
}


/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyReader
 * Method:    readDoubleArray
 * Signature: (J)[D
 */
JNIEXPORT jdoubleArray JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyReader_readDoubleArray
  (JNIEnv *env, jobject obj, jlong address) {
	struct key_reader *reader = jlong_to_address(address);
	int ret;
	double *val = 0;
	jdoubleArray array;

	ret = kr_create_double_array(reader, &val);
	if (ret < 0) {
		setResultCode(env, obj, ret);
		return NULL;
	}

	array = (*env)->NewDoubleArray(env, ret);
	(*env)->SetDoubleArrayRegion(env, array, 0, ret, val);
	free(val);

	return array;
}

