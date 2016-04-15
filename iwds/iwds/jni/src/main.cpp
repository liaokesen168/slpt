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


#include <string>
#include <tr1/memory>

#include <iwds.h>

#include <utils/assert.h>
#include <utils/log.h>

#include <jni/runtime.h>
#include <jni/uniconnectjni.h>
#include <jni/smartsensejni.h>
#include <jni/smartvibratejni.h>


using std::string;
using std::tr1::shared_ptr;

using Iwds::Assert;
using Iwds::Log;
using Iwds::GlobalJobject;
using Iwds::IwdsRuntime;


static const string LOG_TAG("main");


JNIEXPORT void JNICALL nativeInit(
        JNIEnv *env, jclass clazz,
        jobject deviceDescriptor, jboolean enableDebug)
{
    /*
     * Disable debug
     */
    Log::setDebugEnabled(true);

    Assert::dieIf(!initializeIwdsRuntime(deviceDescriptor),
            "Oops! Initialize IWDS runtime failed.");

    Assert::dieIf(!initializeUniconnect(),
            "Oops! Initialize uniconnect failed.");

    initializeSmartSense();

    initializeSmartVibrate();
}

static JNINativeMethod gMethod[] =
{
    {
            "nativeInit",
            "(Lcom/ingenic/iwds/DeviceDescriptor;Z)V",
            (void *)nativeInit
    },
};

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    /*
     * save jvm
     */
    Iwds::saveJvm(vm);

    Iwds::Jenv *env = Iwds::getJenv();

    /*
     * register for main
     */
    const char *className = "com/ingenic/iwds/IwdsInitializer";
    jclass clazz = env->FindClass(className);
    if (!clazz) {
        Log::e(LOG_TAG, string("Can not find class: ") + className);

        return JNI_ERR;
    }

    jint error = env->RegisterNatives(clazz, gMethod,
                        sizeof(gMethod) / sizeof(JNINativeMethod));
    if (error) {
        Log::e(LOG_TAG, "Can not register natives for main.");

        return JNI_ERR;
    }

    /*
     * register for uniconnect
     */
    error = registerUniconnectNatives(env);
    if (error)
        return JNI_ERR;


    /*
     * register for smartsense
     */
    error = registerSmartSenseNatives(env);
    if (error)
        return JNI_ERR;


    /*
     * register for vibrate device
     */
    error = registerSmartVibrateNatives(env);
    if (error)
        return JNI_ERR;

    return JNI_VERSION_1_6;
}
