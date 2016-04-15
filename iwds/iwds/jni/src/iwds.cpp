/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  SunWenZhong(Fighter) <wenzhong.sun@ingenic.com, wanmyqawdr@126.com>
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


#include <iwds.h>

#include <utils/assert.h>

namespace Iwds
{
    static Jvm *the_jvm;

    void saveJvm(Jvm *jvm)
    {
        the_jvm = jvm;
    }

    Jvm *getJvm()
    {
        Assert::dieIf(!the_jvm, "Oops! Java VM pointer is null.");

        return the_jvm;
    }

    Jenv *getJenv()
    {
        Jvm *vm = getJvm();

        Jenv *env;

        Assert::dieIf(
                    vm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK,
                    __FUNCTION__);

        return env;
    }

    Jenv *attachCurrentThread()
    {
        Jvm *vm = getJvm();

        Jenv *env;

        Assert::dieIf(
                    vm->AttachCurrentThread(&env, NULL) != JNI_OK,
                    __FUNCTION__);

        return env;
    }

    void detachCurrentThread()
    {
        Jvm *vm = getJvm();

        vm->DetachCurrentThread();
    }

    GlobalJobject globalRefJobject(jobject refTo) {
        Assert::dieIf(!refTo, "jobject is null.");

        AutoJenv env;

        GlobalJobject globalObj =
                std::tr1::shared_ptr<_jobject>(
                            env->NewGlobalRef(refTo),
                            [](_jobject *obj) {
                                if (obj) {
                                    AutoJenv env;
                                    env->DeleteGlobalRef(obj);
                                }
                            });

        Assert::dieIf(!globalObj, "Global reference to jobject is null");

        return globalObj;
    }

    GlobalJobject findGlobalRefClass(const std::string &className)
    {
        AutoJenv env;

        GlobalJobject globalObj;

        LocalJobject clazz(env->FindClass(className.c_str()));
        if (!clazz) {
            Log::e("iwds", std::string("can not find class: ") + className);

            return globalObj;
        }

        globalObj = globalRefJobject(clazz.data());
        if (!globalObj)
            Log::e("iwds", std::string(
                    "can not create global reference for: ") + className);

        return globalObj;
    }

    void deleteGlobalRef(jobject globalRef)
    {
        AutoJenv env;

        env->DeleteGlobalRef(globalRef);
    }

    jmethodID getMethodId(jclass clazz, const std::string &name,
                                                const std::string &sig)
    {
        Assert::dieIf(!clazz, "Clazz is null.");

        AutoJenv env;

        return env->GetMethodID(clazz, name.c_str(), sig.c_str());
    }

    jmethodID getStaticMethodId(jclass clazz, const std::string &name,
                                                const std::string &sig)
    {
        Assert::dieIf(!clazz, "Clazz is null.");

        AutoJenv env;

        return env->GetStaticMethodID(clazz, name.c_str(), sig.c_str());
    }

    bool checkExceptionAndDump(const std::string &logTag)
    {
        AutoJenv env;

        LocalJobject exception(env->ExceptionOccurred());
        if (exception) {
            Log::e(logTag, "Exception dump:");
            env->ExceptionDescribe();
            env->ExceptionClear();

            return true;
        }

        return false;
    }
}
