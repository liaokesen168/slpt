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

#include <utils/log.h>
#include <utils/errors.h>
#include <utils/exception.h>

using Iwds::Log;

static const char* LOG_TAG = "Exception: ";

namespace Iwds
{
    template<typename T>
    class ScopedLocalRef
    {
    public:
        ScopedLocalRef(C_JNIEnv* env, T localRef = NULL) :
                m_env(env), m_localRef()
        {
        }

        ~ScopedLocalRef()
        {
            reset();
        }

        void reset(T localRef = NULL)
        {
            if (m_localRef != NULL) {
                (*m_env)->DeleteLocalRef(reinterpret_cast<JNIEnv*>(m_env),
                        m_localRef);
                m_localRef = localRef;
            }
        }

        T get() const
        {
            return m_localRef;
        }

    private:
        C_JNIEnv* m_env;
        T m_localRef;

        ScopedLocalRef(const ScopedLocalRef&);
        void operator=(const ScopedLocalRef&);
    };

    static jclass findClass(C_JNIEnv* env, const char* className)
    {
        JNIEnv* e = reinterpret_cast<JNIEnv*>(env);
        return (*env)->FindClass(e, className);
    }

    static bool getExceptionSummary(C_JNIEnv* env, jthrowable exception,
            std::string& result)
    {
        JNIEnv* e = reinterpret_cast<JNIEnv*>(env);

        ScopedLocalRef<jclass> exceptionClass(env,
                (*env)->GetObjectClass(e, exception));

        ScopedLocalRef<jclass> classClass(env,
                (*env)->GetObjectClass(e, exceptionClass.get()));

        jmethodID classGetNameMethod = (*env)->GetMethodID(e, classClass.get(),
                "getName", "()Ljava/lang/String;");

        ScopedLocalRef<jstring> classNameStr(env,
                (jstring) (*env)->CallObjectMethod(e, exceptionClass.get(),
                        classGetNameMethod));

        if (classNameStr.get() == NULL) {
            (*env)->ExceptionClear(e);
            result = "<error getting class name>";
            return false;
        }

        const char* classNameChars = (*env)->GetStringUTFChars(e,
                classNameStr.get(), NULL);
        if (classNameChars == NULL) {
            (*env)->ExceptionClear(e);
            result = "<error getting class name UTF-8>";
            return false;
        }

        result += classNameChars;
        (*env)->ReleaseStringUTFChars(e, classNameStr.get(), classNameChars);

        jmethodID getMessage = (*env)->GetMethodID(e, exceptionClass.get(),
                "getMessage", "(Ljava/lang/String;)");

        ScopedLocalRef<jstring> messageStr(env,
                (jstring) (*env)->CallObjectMethod(e, exception, getMessage));
        if (messageStr.get() == NULL)
            return true;

        result += ": ";

        const char* messageChars = (*env)->GetStringUTFChars(e,
                messageStr.get(), NULL);
        if (messageChars != NULL) {
            result += messageChars;
            (*env)->ReleaseStringUTFChars(e, messageStr.get(), messageChars);

        } else {
            result += "<error getting message>";
            (*env)->ExceptionClear(e);
        }

        return true;
    }

    const char* jniStrError(int errnum, char* buf, size_t buflen) {
        #if __GLIBC__
            return strerror_r(errnum, buf, buflen);
        #else
            int rc = strerror_r(errnum, buf, buflen);
            if (rc != 0) {
                snprintf(buf, buflen, "errno %d", errnum);
            }
            return buf;
        #endif
    }

    int jniThrowExceptionFmt(C_JNIEnv* env, const char* className,
            const char* fmt, va_list args) {
        char msgBuf[512];
        vsnprintf(msgBuf, sizeof(msgBuf), fmt, args);

        return jniThrowException(env, className, msgBuf);
    }

    int jniThrowNullPointerException(C_JNIEnv* env, const char* msg) {
        return jniThrowException(env, "java/lang/NullPointerException", msg);
    }

    int jniThrowRuntimeException(C_JNIEnv* env, const char* msg)
    {
        return jniThrowException(env, "java/lang/RuntimeException", msg);
    }

    int jniThrowIOException(C_JNIEnv* env, int errnum) {
        char buffer[80];
        const char* message = jniStrError(errnum, buffer, sizeof(buffer));
        return jniThrowException(env, "java/io/IOException", message);
    }

    extern "C" int jniThrowException(C_JNIEnv* env, const char* className, const char* msg)
    {
        JNIEnv* e = reinterpret_cast<JNIEnv*>(env);
        if ((*env)->ExceptionCheck(e)) {
            ScopedLocalRef<jthrowable> exception(env, (*env)->ExceptionOccurred(e));
            (*env)->ExceptionClear(e);

            if (exception.get() != NULL) {
                std::string text;
                getExceptionSummary(env, exception.get(), text);
                Log::w(LOG_TAG, "Discarding pending exception (%s) to throw %s",
                        text.c_str(), className);
            }
        }

        ScopedLocalRef<jclass> exceptionClass(env, findClass(env, className));
        if (exceptionClass.get() == NULL) {
            Log::e(LOG_TAG, "Unable to find exception class %s", className);
            return -1;
        }

        if ((*env)->ThrowNew(e, exceptionClass.get(), msg) != JNI_OK) {
            Log::e(LOG_TAG, "Failed throwing '%s' '%s'", className, msg);
            return -1;
        }

        return 0;
    }

    void signalExceptionForError(JNIEnv* env, jobject obj, int error)
    {
        switch (error) {
        case UNKNOWN_ERROR:
            jniThrowException(env, "java/lang/RuntimeException", "Unknown error");
            break;

        case NO_MEMORY:
            jniThrowException(env, "java/lang/OutOfMemoryError", NULL);
            break;

        case INVALID_OPERATION:
            jniThrowException(env, "java/lang/UnsupportedOperationException", NULL);
            break;

        case BAD_VALUE:
            jniThrowException(env, "java/lang/IllegalArgumentException", NULL);
            break;

        case BAD_INDEX:
            jniThrowException(env, "java/lang/IndexOutOfBoundsException", NULL);
            break;

        case BAD_TYPE:
            jniThrowException(env, "java/lang/IllegalArgumentException", NULL);
            break;

        case NAME_NOT_FOUND:
            jniThrowException(env, "java/util/NoSuchElementException", NULL);
            break;

        case PERMISSION_DENIED:
            jniThrowException(env, "java/lang/SecurityException", NULL);
            break;

        case NOT_ENOUGH_DATA:
            jniThrowException(env, "com/ingenic/iwds/os/SafeParcelFormatException",
                    "Not enough data");
            break;

        case NO_INIT:
            jniThrowException(env, "java/lang/RuntimeException", "Not initialized");
            break;

        case ALREADY_EXISTS:
            jniThrowException(env, "java/lang/RuntimeException",
                    "Item already exists");
            break;

        case UNKNOWN_TRANSACTION:
            jniThrowException(env, "java/lang/RuntimeException", "Unknown transaction code");
            break;

        default:
            Log::e(LOG_TAG, "Unknown error code 0x%x", error);
            char* msg = NULL;
            asprintf(&msg, "Unknown error code 0x%x", error);
            jniThrowException(env, "java/lang/RuntimeException", msg);
            free(msg);
            break;
        }
    }
}
