/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_ingenic_iwds_slpt_view_utils_KeyWriter */

#ifndef _Included_com_ingenic_iwds_slpt_view_utils_KeyWriter
#define _Included_com_ingenic_iwds_slpt_view_utils_KeyWriter
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    initialize_native
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_initialize_1native
  (JNIEnv *, jobject);

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    recycle
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_recycle
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    getBytes
 * Signature: (J)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_getBytes
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    getSize
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_getSize
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeByte
 * Signature: (JB)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeByte
  (JNIEnv *, jobject, jlong, jbyte);

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeBoolean
 * Signature: (JB)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeBoolean
  (JNIEnv *, jobject, jlong, jbyte);

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeShort
 * Signature: (JS)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeShort
  (JNIEnv *, jobject, jlong, jshort);

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeInt
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeInt
  (JNIEnv *, jobject, jlong, jint);

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeLong
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeLong
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeFloat
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeFloat
  (JNIEnv *, jobject, jlong, jfloat);

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeDouble
 * Signature: (JD)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeDouble
  (JNIEnv *, jobject, jlong, jdouble);

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeString
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeString
  (JNIEnv *, jobject, jlong, jstring);

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeByteArray
 * Signature: (J[BII)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeByteArray
  (JNIEnv *, jobject, jlong, jbyteArray, jint, jint);

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeBooleanArray
 * Signature: (J[ZII)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeBooleanArray
  (JNIEnv *, jobject, jlong, jbooleanArray, jint, jint);

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeShortArray
 * Signature: (J[SII)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeShortArray
  (JNIEnv *, jobject, jlong, jshortArray, jint, jint);

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeIntArray
 * Signature: (J[III)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeIntArray
  (JNIEnv *, jobject, jlong, jintArray, jint, jint);

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeLongArray
 * Signature: (J[JII)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeLongArray
  (JNIEnv *, jobject, jlong, jlongArray, jint, jint);

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeFloatArray
 * Signature: (J[FII)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeFloatArray
  (JNIEnv *, jobject, jlong, jfloatArray, jint, jint);

/*
 * Class:     com_ingenic_iwds_slpt_view_utils_KeyWriter
 * Method:    writeDoubleArray
 * Signature: (J[DII)V
 */
JNIEXPORT void JNICALL Java_com_ingenic_iwds_slpt_view_utils_KeyWriter_writeDoubleArray
  (JNIEnv *, jobject, jlong, jdoubleArray, jint, jint);

#ifdef __cplusplus
}
#endif
#endif