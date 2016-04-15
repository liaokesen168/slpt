/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  ZhangYanMing <yanming.zhang@ingenic.com, jamincheung@126.com>
 *
 *  Elf/IDWS Project
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation; either version 2 of the License, or (at your
 *  option) any later version.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

#include <string>
#include <sys/types.h>
#include <jni.h>
#include <jni/safeparceljni.h>
#include <safeparcel/safeparcel.h>
#include <utils/log.h>
#include <utils/errors.h>
#include <utils/exception.h>

using std::string;

using Iwds::Log;
using Iwds::signalExceptionForError;
using Iwds::SafeParcel;

using Iwds::NO_ERROR;
using Iwds::NO_MEMORY;

static const string LOG_TAG("SafeParcelJni: ");

static const char *classSafeParcel = "com/ingenic/iwds/os/SafeParcel";

JNIEXPORT jint JNICALL nativeDataSize(JNIEnv* env, jclass clazz,
        jlong nativePtr)
{
    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);

    return parcel ? parcel->dataSize() : 0;
}

JNIEXPORT jint JNICALL nativeDataAvail(JNIEnv* env, jclass clazz,
        jlong nativePtr)
{
    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);

    return parcel ? parcel->dataAvail() : 0;
}

JNIEXPORT jint JNICALL nativeDataPosition(JNIEnv* env, jclass clazz,
        jlong nativePtr)
{
    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);

    return parcel ? parcel->dataPosition() : 0;
}

JNIEXPORT jint JNICALL nativeDataCapacity(JNIEnv* env, jclass clazz,
        jlong nativePtr)
{
    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);

    return parcel ? parcel->dataCapacity() : 0;
}

JNIEXPORT void JNICALL nativeSetDataSize(JNIEnv* env, jclass clazz,
        jlong nativePtr, jint size)
{
    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);

    if (parcel == NULL)
        return;

    const status_t error = parcel->setDataSize(size);
    if (error != NO_ERROR)
        signalExceptionForError(env, clazz, error);
}

JNIEXPORT void JNICALL nativeSetDataPosition(JNIEnv* env, jclass clazz,
        jlong nativePtr, jint position)
{
    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);

    if (parcel == NULL)
        return;

    parcel->setDataPosition(position);
}

JNIEXPORT void JNICALL nativeSetDataCapacity(JNIEnv* env, jclass clazz,
        jlong nativePtr, jint capacity)
{
    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);

    if (parcel == NULL)
        return;

    const status_t error = parcel->setDataCapacity(capacity);
    if (error != NO_ERROR)
        signalExceptionForError(env, clazz, error);
}

JNIEXPORT void JNICALL nativeWriteByteArray(JNIEnv* env, jclass clazz,
        jlong nativePtr, jobject data, jint offset, jint length)
{
    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);

    if (parcel == NULL)
        return;

    const status_t error = parcel->writeInt32(length);
    if (error != NO_ERROR) {
        signalExceptionForError(env, clazz, error);
        return;
    }

    void* dest = parcel->writeInplace(length);
    if (dest == NULL) {
        signalExceptionForError(env, clazz, NO_MEMORY);
        return;
    }

    jbyte* ar = (jbyte *)env->GetPrimitiveArrayCritical((jarray)data, 0);
    if (ar) {
        memcpy(dest, ar + offset, length);
        env->ReleasePrimitiveArrayCritical((jarray)data, ar, 0);
    }
}

JNIEXPORT void JNICALL nativeWriteInt(JNIEnv* env, jclass clazz,
        jlong nativePtr, jint val)
{
    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);

    if (parcel == NULL)
        return;

    const status_t error = parcel->writeInt32(val);
    if (error != NO_ERROR)
        signalExceptionForError(env, clazz, error);
}

JNIEXPORT void JNICALL nativeWriteLong(JNIEnv* env, jclass clazz,
        jlong nativePtr, jlong val)
{
    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);

    if (parcel == NULL)
        return;

    const status_t error = parcel->writeInt64(val);
    if (error != NO_ERROR)
        signalExceptionForError(env, clazz, error);
}

JNIEXPORT void JNICALL nativeWriteFloat(JNIEnv* env, jclass clazz,
        jlong nativePtr, jfloat val)
{
    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);

    if (parcel == NULL)
        return;

    const status_t error = parcel->writeFloat(val);
    if (error != NO_ERROR)
        signalExceptionForError(env, clazz, error);
}

JNIEXPORT void JNICALL nativeWriteDouble(JNIEnv* env, jclass clazz,
        jlong nativePtr, jdouble val)
{
    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);

    if (parcel == NULL)
        return;

    const status_t error = parcel->writeDouble(val);
    if (error != NO_ERROR)
        signalExceptionForError (env, clazz, error);
}

JNIEXPORT void JNICALL nativeWriteString(JNIEnv* env, jclass clazz,
        jlong nativePtr, jstring val)
{
    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);

    if (parcel == NULL)
        return;

    status_t error = NO_MEMORY;
    if (val) {
        const jchar* jstr = env->GetStringCritical(val, 0);

        if (jstr) {
            static_assert(sizeof(char16_t) == sizeof(jchar), "char16_t != jchar");

            const char16_t* str = reinterpret_cast<const char16_t*>(jstr);

            error = parcel->writeString16(str, env->GetStringLength(val));
            env->ReleaseStringCritical(val, jstr);
        }
    } else {
        error = parcel->writeString16(NULL, 0);
    }

    if (error != NO_ERROR)
        signalExceptionForError(env, clazz, error);
}

JNIEXPORT jbyteArray JNICALL nativeCreateByteArray(JNIEnv* env, jclass clazz,
        jlong nativePtr)
{
    jbyteArray ret = NULL;

    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);

    if (parcel == NULL)
        return NULL;

    int32_t len = parcel->readInt32();

    if (len >= 0 && len <= (int32_t) parcel->dataAvail()) {
        ret = env->NewByteArray(len);
        if (ret != NULL) {
            jbyte* a2 = (jbyte*) env->GetPrimitiveArrayCritical(ret, 0);
            if (a2) {
                const void* data = parcel->readInplace(len);
                memcpy(a2, data, len);
                env->ReleasePrimitiveArrayCritical(ret, a2, 0);
            }
        }
    }

    return ret;
}

JNIEXPORT jint JNICALL nativeReadInt(JNIEnv* env, jclass clazz, jlong nativePtr)
{
    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);

    if (parcel == NULL)
        return 0;

    return parcel->readInt32();
}

JNIEXPORT jlong JNICALL nativeReadLong(JNIEnv* env, jclass clazz,
        jlong nativePtr)
{
    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);

    if (parcel == NULL)
        return 0;

    return parcel->readInt64();
}

JNIEXPORT jfloat JNICALL nativeReadFloat(JNIEnv* env, jclass clazz,
        jlong nativePtr)
{
    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);

    if (parcel == NULL)
        return 0;

    return parcel->readFloat();
}

JNIEXPORT jdouble JNICALL nativeReadDouble(JNIEnv* env, jclass clazz,
        jlong nativePtr)
{
    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);

    if (parcel == NULL)
        return 0;

    return parcel->readDouble();
}

JNIEXPORT jstring JNICALL nativeReadString(JNIEnv* env, jclass clazz,
        jlong nativePtr)
{
    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);

    if (parcel == NULL)
        return NULL;

    size_t len;
    const char16_t* str = parcel->readString16Inplace(&len);

    if (str) {
        static_assert(sizeof(char16_t) == sizeof(jchar), "char16_t != jchar");

        const jchar* jstr = reinterpret_cast<const jchar*>(str);

        return env->NewString(jstr, len);
    }

    return NULL;
}

JNIEXPORT jint JNICALL nativeCreate(JNIEnv* env, jclass clazz)
{
    SafeParcel* parcel = new SafeParcel();

    return reinterpret_cast<jlong>(parcel);
}

JNIEXPORT void JNICALL nativeFreeBuffer(JNIEnv* env, jclass clazz,
        jlong nativePtr)
{
    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);
    if (parcel == NULL)
        return;

    parcel->freeData();
}

JNIEXPORT void JNICALL nativeDestroy(JNIEnv* env, jclass clazz, jlong nativePtr)
{
    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);

    if (parcel == NULL)
        return;

    delete parcel;
}

JNIEXPORT jbyteArray JNICALL nativeMarshall(JNIEnv* env, jclass clazz,
        jlong nativePtr)
{
    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);

    if (parcel == NULL)
        return NULL;

    jbyteArray ret = env->NewByteArray(parcel->dataSize());
    if (ret != NULL) {
        jbyte* array = (jbyte*) env->GetPrimitiveArrayCritical(ret, 0);
        if (array != NULL) {
            memcpy(array, parcel->data(), parcel->dataSize());
            env->ReleasePrimitiveArrayCritical(ret, array, 0);
        }
    }

    return ret;
}

JNIEXPORT void JNICALL nativeUnmarshall(JNIEnv* env, jclass clazz,
        jlong nativePtr, jbyteArray data, jint offset, jint length)
{
    SafeParcel* parcel = reinterpret_cast<SafeParcel*>(nativePtr);

    if (parcel == NULL || length < 0)
       return;

    jbyte* array = (jbyte*)env->GetPrimitiveArrayCritical(data, 0);
    if (array)
    {
        parcel->setDataSize(length);
        parcel->setDataPosition(0);

        void* raw = parcel->writeInplace(length);
        memcpy(raw, (array + offset), length);

        env->ReleasePrimitiveArrayCritical(data, array, 0);
    }
}

static const JNINativeMethod gSafeParcelMethods[] = {
        {
                "nativeDataSize",
                "(J)I",
                (void *) nativeDataSize
        },

        {
                "nativeDataAvail",
                "(J)I",
                (void *) nativeDataAvail
        },


        {
                "nativeDataPosition",
                "(J)I",
                (void *) nativeDataPosition
        },

        {
                "nativeDataCapacity",
                "(J)I",
                (void *) nativeDataCapacity
        },

        {
                "nativeSetDataSize",
                "(JI)V",
                (void *) nativeSetDataSize
        },

        {
                "nativeSetDataPosition",
                "(JI)V",
                (void *) nativeSetDataPosition
        },

        {
                "nativeSetDataCapacity",
                "(JI)V",
                (void *) nativeSetDataCapacity
        },

        {
                "nativeWriteByteArray",
                "(J[BII)V",
                (void *) nativeWriteByteArray
        },

        {
                "nativeWriteInt",
                "(JI)V",
                (void *) nativeWriteInt
        },

        {
                "nativeWriteLong",
                "(JJ)V",
                (void *) nativeWriteLong
        },

        {
                "nativeWriteFloat",
                "(JF)V",
                (void *) nativeWriteFloat
        },

        {
                "nativeWriteDouble",
                "(JD)V",
                (void *) nativeWriteDouble
        },

        {
                "nativeWriteString",
                "(JLjava/lang/String;)V",
                (void *) nativeWriteString
        },

        {
                "nativeCreateByteArray",
                "(J)[B",
                (void *) nativeCreateByteArray
        },

        {
                "nativeReadInt",
                "(J)I",
                (void *) nativeReadInt
        },

        {
                "nativeReadLong",
                "(J)J",
                (void *) nativeReadLong
        },

        {
                "nativeReadFloat",
                "(J)F",
                (void *) nativeReadFloat
        },

        {
                "nativeReadDouble",
                "(J)D",
                (void *) nativeReadDouble
        },

        {
                "nativeReadString",
                "(J)Ljava/lang/String;",
                (void *) nativeReadString
        },

        {
                "nativeCreate",
                "()J",
                (void *) nativeCreate
        },

        {
                "nativeFreeBuffer",
                "(J)V",
                (void *) nativeFreeBuffer
        },

        {
                "nativeDestroy",
                "(J)V",
                (void *) nativeDestroy
        },

        {
                "nativeMarshall",
                "(J)[B",
                (void *) nativeMarshall
        },

        {
                "nativeUnmarshall",
                "(J[BII)V",
                (void *) nativeUnmarshall
        },
};

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv* env;
    jclass clazz;

    if(vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        Log::e(LOG_TAG, "Can not get java env.");

        return JNI_ERR;
    }

    clazz = env->FindClass(classSafeParcel);
    if (!clazz) {
        Log::e(LOG_TAG, "Can not find class: SafeParcel");

        return JNI_ERR;
    }

    jint error = env->RegisterNatives(clazz, gSafeParcelMethods,
            sizeof(gSafeParcelMethods) / sizeof(JNINativeMethod));
    if (error) {
        Log::e(LOG_TAG, "Can not register natives for SafeParcel.");

        return error;
    }

    Log::i(LOG_TAG, "Native register done.");

    return JNI_VERSION_1_6;
}
