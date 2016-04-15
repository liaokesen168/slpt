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

#include <utils/log.h>
#include <utils/assert.h>

#include <jni/javaclass.h>
#include <jni/runtime.h>

using std::string;
using std::tr1::shared_ptr;

using Iwds::GlobalJobject;
using Iwds::findGlobalRefClass;
using Iwds::Log;
using Iwds::LocalJobject;
using Iwds::Assert;
using Iwds::AutoJenv;
using Iwds::toJclass;
using Iwds::IwdsRuntime;
using Iwds::DeviceDescriptor;

static const string LOG_TAG("Runtime: ");

class JDeviceDescriptor : public JavaClass
{
public:
    JDeviceDescriptor() :
        m_classGlobalRef(),
        m_deviceClass(0),
        m_deviceSubClass()
    {
        m_classGlobalRef =
                findGlobalRefClass("com/ingenic/iwds/DeviceDescriptor");
        if (!m_classGlobalRef) {
            Log::e(LOG_TAG, "Can not find class: DeviceDescriptor.");

            return;
        }

        AutoJenv env;

        m_deviceClass = env->GetFieldID(toJclass(
                                m_classGlobalRef), "devClass", "I");
        if (!m_deviceClass)
            Log::e(LOG_TAG + "JDeviceDescriptor",
                    "Can not find field: devClass.");

        m_deviceSubClass = env->GetFieldID(toJclass(
                            m_classGlobalRef), "devSubClass", "I");
        if (!m_deviceSubClass)
            Log::e(LOG_TAG + "JDeviceDescriptor",
                    "Can not find field: devSubClass.");
    }

    bool isAttached(JavaApiContext *context) const
    {
        return m_classGlobalRef &&
                m_deviceClass &&
                m_deviceSubClass;
    }

    int getDeviceClass(
            JavaApiContext *context, jobject jdeviceDescriptor) const
    {
        AutoJenv env;

        return env->GetIntField(jdeviceDescriptor, m_deviceClass);
    }

    int getDeviceSubClass(
            JavaApiContext *context, jobject jdeviceDescriptor) const
    {
        AutoJenv env;

        return env->GetIntField(jdeviceDescriptor, m_deviceSubClass);
    }

private:
    GlobalJobject m_classGlobalRef;

    jfieldID m_deviceClass;
    jfieldID m_deviceSubClass;
};

bool initializeIwdsRuntime(jobject deviceDescriptor)
{
    JDeviceDescriptor jclass;

    if (!jclass.isAttached(0)) {
        Log::e(LOG_TAG, "Failed to attach to android.");

        return false;
    }

    shared_ptr<DeviceDescriptor> descriptor(new DeviceDescriptor());

    descriptor->deviceClass =
            jclass.getDeviceClass(0, deviceDescriptor);
    descriptor->deviceSubClass =
            jclass.getDeviceSubClass(0, deviceDescriptor);

    if (Log::isDebugEnabled())
        descriptor->dump();

    shared_ptr<IwdsRuntime> runtime = IwdsRuntime::getInstance();
    if (!runtime->initialize(descriptor))
        return false;

    if (Log::isDebugEnabled())
        runtime->dump();

    return true;
}
