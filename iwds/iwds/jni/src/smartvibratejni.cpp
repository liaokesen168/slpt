/*
 *  Copyright (C) 2015 Ingenic Semiconductor
 *
 *  Kage Shen <kgat96@gmail.com>
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
#include <jni.h>

#include <string>
#include <tr1/memory>

#include <iwds.h>

#include <utils/assert.h>
#include <utils/log.h>
#include <utils/mutex.h>

#include <jni/javaclass.h>

#include <smartvibrate/vibrate.h>

using std::string;
using std::tr1::shared_ptr;

using Iwds::Assert;
using Iwds::Log;
using Iwds::LocalJobject;
using Iwds::toJclass;
using Iwds::Mutex;
using Iwds::GlobalJobject;
using Iwds::globalRefJobject;
using Iwds::findGlobalRefClass;
using Iwds::getMethodId;
using Iwds::toJclass;
using Iwds::AutoJenv;
using Iwds::LocalJobject;
using Iwds::IwdsRuntime;

static const string LOG_TAG("SmartVibrateJni: ");

static const char *classVibrateService =
        "com/ingenic/iwds/smartvibrate/VibrateService$VibrateServiceStub";

static struct vibrate_module_t *hm = NULL;

static int isAll0(jint *pattern, int len)
{
    for (int i = 0; i < len; i++) {
        if (pattern[i] != 0) {
            return false;
        }
    }
    return true;
}

JNIEXPORT jint JNICALL special_vibrate(JNIEnv *env, jclass clazz, jintArray arr)
{
    int len = env->GetArrayLength(arr);

    if (len == 0) {
        return 0;
    }

    jint *p = env->GetIntArrayElements(arr, 0);

    if (isAll0(p, len)) {
        return 0;
    }

    if (!hm->special_vibrate) {
        Log::e(LOG_TAG, "VibrateModule none vibrate method");
        return 0;
    }

    return jint(hm->special_vibrate(p, len));
}

static JNINativeMethod gVibrateServiceMethods[] = {
    { "nativeSpecialVibrate", "([I)I", (void *) special_vibrate },
};

jint registerSmartVibrateNatives(Iwds::Jenv *env)
{
    LocalJobject clazz(env->FindClass(classVibrateService));
    if (!clazz) {
        Log::e(LOG_TAG, "Can not find class: VibrateService");
        return JNI_ERR;
    }

    jint error = env->RegisterNatives(toJclass(clazz), gVibrateServiceMethods,
            sizeof(gVibrateServiceMethods) / sizeof(JNINativeMethod));

    if (error) {
        Log::e(LOG_TAG.c_str(), "Can not register natives for %s", classVibrateService);
        return error;
    }

    return JNI_OK;
}

bool initializeSmartVibrate()
{
    if (!IwdsRuntime::getInstance()->isXburstPlatform())
        return false;

    hm = getVibrateModule();
    if (hm == NULL) {
        Log::e(LOG_TAG, "VibrateModule load module failed");
    } else if (hm->init()) {
        Log::e(LOG_TAG, "VibrateModule initialize failed");
        return false;
    }

    Log::d(LOG_TAG, "Initialize done.");

    return true;
}

