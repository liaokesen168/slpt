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

#ifndef EXCEPTION_H
#define EXCEPTION_H

#include <unistd.h>
#include <iwds.h>

namespace Iwds
{
    #ifdef __cplusplus
    extern "C"
    {
    #endif

    int jniThrowException(C_JNIEnv* env, const char* className,
            const char* msg);

    int jniThrowExceptionFmt(C_JNIEnv* env, const char* className,
            const char* fmt, va_list args);

    int jniThrowNullPointerException(C_JNIEnv* env, const char* msg);

    int jniThrowIOException(C_JNIEnv* env, int errnum);

    int jniThrowRuntimeException(C_JNIEnv* env, const char* msg);

    void signalExceptionForError(JNIEnv* env, jobject obj, int error);

    #ifdef __cplusplus
    }
    #endif

    #ifdef __cplusplus

    inline int jniThrowException(JNIEnv* env, const char* className,
            const char* msg)
    {
        return jniThrowException(&env->functions, className, msg);
    }

    inline int jniThrowExceptionFmt(JNIEnv* env, const char* className,
            const char* fmt, ...) {
        va_list args;
        va_start(args, fmt);
        return jniThrowExceptionFmt(&env->functions, className, fmt, args);
        va_end(args);
    }

    inline int jniThrowNullPointerException(JNIEnv* env, const char* msg) {
        return jniThrowNullPointerException(&env->functions, msg);
    }

    inline int jniThrowRuntimeException(JNIEnv* env, const char* msg)
    {
        return jniThrowRuntimeException(&env->functions, msg);
    }

    inline int jniThrowIOException(JNIEnv* env, int errnum) {
        return jniThrowIOException(&env->functions, errnum);
    }

    #endif

}

#endif //EXCEPTION_H

