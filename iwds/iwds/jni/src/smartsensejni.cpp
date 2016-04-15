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
#include <utils/mutex.h>

#include <smartsense/sensoreventcallback.h>
#include <smartsense/sensormanager.h>
#include <smartsense/sensorevent.h>
#include <smartsense/sensor.h>

#include <jni/javaclass.h>

#include <jni/smartsensejni.h>


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


static const string LOG_TAG("SmartsenseJni: ");

static const char *classSensorServiceStub =
        "com/ingenic/iwds/smartsense/SensorService$SensorServiceStub";

class JSensorEvent : public JavaClass
{
public:
    JSensorEvent() :
        m_classGlobalRef(),
        m_values(0),
        m_accuracy(0),
        m_sensorType(0),
        m_timestamp(0)
    {
        m_classGlobalRef = findGlobalRefClass(
                        "com/ingenic/iwds/smartsense/SensorEvent");
        if (!m_classGlobalRef) {
            Log::e(LOG_TAG, "Can not find class: SensorEvent");
            return;
        }

        AutoJenv env;

        /*
         * SensorEvent.<init>
         */
        m_methodConstructor =
                getMethodId(
                        toJclass(m_classGlobalRef),
                        "<init>",
                        "(I)V");

        if (!m_methodConstructor) {
            Log::e(LOG_TAG,
                    "Can not find method:"
                    " SensorEvent.<init>");
        }

        m_values = env->GetFieldID(
                toJclass(m_classGlobalRef), "values", "[F");

        if (!m_values)
            Log::e(LOG_TAG + "JSensorEvent",
                    "Can not find field: values.");

        m_accuracy = env->GetFieldID(
                toJclass(m_classGlobalRef), "accuracy", "I");

        if (!m_accuracy)
            Log::e(LOG_TAG + "JSensorEvent",
                    "Can not find field: accuracy.");

        m_sensorType = env->GetFieldID(
                toJclass(m_classGlobalRef), "sensorType", "I");

        if (!m_sensorType)
            Log::e(LOG_TAG + "JSensorEvent",
                    "Can not find field: sensorType.");

        m_timestamp  = env->GetFieldID(
                toJclass(m_classGlobalRef), "timestamp", "J");

        if (!m_timestamp)
            Log::e(LOG_TAG + "JSensorEvent",
                    "Can not find field: timestamp.");
    }

    jobject newObjectFromNative(
            JavaApiContext *context, sensors_event_t *hal_event)
    {
        AutoJenv env;

        jobject jevent = env->NewObject(
                                toJclass(m_classGlobalRef),
                                m_methodConstructor, 16);

        Assert::dieIf(
                !jevent, "Failed to alloc jobject of SensorEvent.");

        LocalJobject jvalues((jfloatArray)env->GetObjectField(jevent, m_values));

        /*
         * TODO not only data[0]
         */
        env->SetFloatArrayRegion(
                        jfloatArray(jvalues.data()), 0, 16, hal_event->data);

        /*
         * TODO accuracy ?
         */
        env->SetIntField(jevent, m_accuracy, 0);

        env->SetIntField(jevent, m_sensorType, hal_event->type);

        env->SetLongField(jevent, m_timestamp, hal_event->timestamp);

        return jevent;
    }

    bool isAttached(JavaApiContext *context) const
    {
        return m_methodConstructor &&
                m_values &&
                m_accuracy &&
                m_sensorType &&
                m_timestamp;
    }

private:
    GlobalJobject m_classGlobalRef;

    jmethodID m_methodConstructor;

    jfieldID m_values;
    jfieldID m_accuracy;
    jfieldID m_sensorType;
    jfieldID m_timestamp;

};


class JSensor : public JavaClass
{
public:
    JSensor() :
        m_classGlobalRef(),
        m_type(0),
        m_maxRange(0),
        m_resolution(0),
        m_minDelay(0),
        m_name(0),
        m_vendor(0),
        m_version(0)
    {
        m_classGlobalRef = findGlobalRefClass(
                        "com/ingenic/iwds/smartsense/Sensor");
        if (!m_classGlobalRef) {
            Log::e(LOG_TAG, "Can not find class: Sensor");
            return;
        }

        AutoJenv env;

        m_type = env->GetFieldID(
                toJclass(m_classGlobalRef), "m_type", "I");

        if (!m_type)
            Log::e(LOG_TAG + "JSensorEvent",
                    "Can not find field: m_type.");

        m_maxRange = env->GetFieldID(
                toJclass(m_classGlobalRef), "m_maxRange", "F");

        if (!m_maxRange)
            Log::e(LOG_TAG + "JSensorEvent",
                    "Can not find field: m_maxRange.");

        m_resolution = env->GetFieldID(
                toJclass(m_classGlobalRef), "m_resolution", "F");

        if (!m_resolution)
            Log::e(LOG_TAG + "JSensorEvent",
                    "Can not find field: m_resolution.");

        m_minDelay = env->GetFieldID(
                toJclass(m_classGlobalRef), "m_minDelay", "I");

        if (!m_minDelay)
            Log::e(LOG_TAG + "JSensorEvent",
                    "Can not find field m_minDelay.");

        m_name = env->GetFieldID(
                        toJclass(m_classGlobalRef), "m_name", "Ljava/lang/String;");

        if (!m_name)
            Log::e(LOG_TAG + "JSensorEvent",
                     "Can not find field m_name.");

        m_vendor = env->GetFieldID(
                        toJclass(m_classGlobalRef), "m_vendor", "Ljava/lang/String;");

        if (!m_vendor)
            Log::e(LOG_TAG + "JSensorEvent",
                     "Can not find field m_vendor.");

        m_version = env->GetFieldID(
                        toJclass(m_classGlobalRef), "m_version", "I");

        if (!m_version)
            Log::e(LOG_TAG + "JSensorEvent",
                     "Can not find field m_version.");
    }

    bool isAttached(JavaApiContext *context) const
    {
        return m_type &&
                m_maxRange &&
                m_resolution &&
                m_minDelay &&
                m_name &&
                m_vendor &&
                m_version;
    }

    void fillWithNative(
            JavaApiContext *context, jobject jsensor,
            shared_ptr<Sensor> sensor)
    {
        AutoJenv env;

        env->SetIntField(jsensor, m_type, (jint)sensor->getType());
        env->SetFloatField(jsensor, m_maxRange, sensor->getMaxRange());
        env->SetFloatField(jsensor, m_resolution, sensor->getResolution());
        env->SetIntField(jsensor, m_minDelay, sensor->getMinDelay());
        env->SetIntField(jsensor, m_version, sensor->getVersion());

        LocalJobject name(env->NewStringUTF(sensor->getName().c_str()));
        LocalJobject vendor(env->NewStringUTF(sensor->getVendor().c_str()));

        env->SetObjectField(jsensor, m_name, name.data());
        env->SetObjectField(jsensor, m_vendor, vendor.data());
    }

private:
    GlobalJobject m_classGlobalRef;

    jfieldID m_type;
    jfieldID m_maxRange;
    jfieldID m_resolution;
    jfieldID m_minDelay;
    jfieldID m_name;
    jfieldID m_vendor;
    jfieldID m_version;
};


class JSensorEventCallback : public JavaClass
{
public:
    JSensorEventCallback() :
        m_classGlobalRef(),
        m_methodOnSensorEvent(0)
    {
        m_classGlobalRef = findGlobalRefClass(
                        "com/ingenic/iwds/smartsense/SensorService"
                        "$SensorServiceStub$SensorEventCallback");
        if (!m_classGlobalRef) {
            Log::e(LOG_TAG, "Can not find class: SensorEventCallback");
            return;
        }

        /*
         * SensorEventCallback.onSensorEvent
         */
        m_methodOnSensorEvent =
                getMethodId(
                        toJclass(m_classGlobalRef),
                        "onSensorEvent",
                        "(Lcom/ingenic/iwds/smartsense/SensorEvent;)V");
        if (!m_methodOnSensorEvent) {
            Log::e(LOG_TAG,
                    "Can not find method:"
                    " SensorEventCallback.onSensorEvent");
        }
    }

    bool isAttached(JavaApiContext *context) const
    {
        return !!m_methodOnSensorEvent;
    }

    void onSensorEvent(
            JavaApiContext *context,
            GlobalJobject callback, jobject event)
    {
        AutoJenv env;

        env->CallVoidMethod(
                toJclass(callback), m_methodOnSensorEvent, event);
    }

private:
    GlobalJobject m_classGlobalRef;

    jmethodID m_methodOnSensorEvent;
};


class NativeSensorEventCallback : public SensorEventCallback,
                                    public JavaApiContext
{
public:
    NativeSensorEventCallback(jobject jCallback) :
        m_callback(globalRefJobject(jCallback))
    {
        if (!sm_classSensorEventCallback) {
            sm_classSensorEventCallback = new JSensorEventCallback();
            sm_classSensorEvent = new JSensorEvent();

            bool isAttached =
                    sm_classSensorEventCallback->isAttached(this) &&
                    sm_classSensorEvent->isAttached(this);

            Assert::dieIf(!isAttached,
                            "Fail attaching to smart sense service.");
        }
    }

    void onSensorEvent(sensors_event_t *hal_event)
    {
        AutoJenv env;

        LocalJobject jevent
                (sm_classSensorEvent->newObjectFromNative(this, hal_event));

        Assert::dieIf(
                !jevent, "Failed to alloc jobject of SensorEvent.");

        sm_classSensorEventCallback->onSensorEvent(
                                            this, m_callback, jevent.data());
    }

private:
    static JSensorEventCallback *sm_classSensorEventCallback;
    static JSensorEvent *sm_classSensorEvent;

    GlobalJobject m_callback;
};

JSensorEventCallback
            *NativeSensorEventCallback::sm_classSensorEventCallback = 0;
JSensorEvent *NativeSensorEventCallback::sm_classSensorEvent = 0;

class SensorApi : public JavaApiContext
{
public:
    SensorApi() :
        m_classSensor(new JSensor())
    {
        Assert::dieIf(!m_classSensor->isAttached(this),
                            "Fail attaching to smart sense service.");
    }

    void fillSensorObjectByNative(
                        jobject jsensor, shared_ptr<Sensor> sensor)
    {
        m_classSensor->fillWithNative(this, jsensor, sensor);
    }

private:
    JSensor *m_classSensor;
};


static shared_ptr<SensorManager> sm = SensorManager::getInstance();
static SensorApi *sensorApi = 0;

JNIEXPORT jint JNICALL getSensorCount(JNIEnv *env, jclass clazz)
{
    return jint(sm->getSensorCount());
}

JNIEXPORT void JNICALL forEachSensor(
                JNIEnv *env, jclass clazz, jobject jsensor, jint index)
{
    shared_ptr<Sensor> sensor = sm->getSensorByIndex(index);

    sensorApi->fillSensorObjectByNative(jsensor, sensor);
}


JNIEXPORT void JNICALL installSensorEventCallback(
        JNIEnv *env, jclass clazz, jint sensorType, jobject jcallback)
{
    shared_ptr<Sensor> sensor =
            sm->getSensorByType(Sensor::SensorType(sensorType));

    Assert::dieIf(!sensor, "No such type sensor.");

    shared_ptr<SensorEventCallback> callback(
                        new NativeSensorEventCallback(jcallback));

    sensor->installSensorEventCallback(callback);
}

JNIEXPORT jboolean JNICALL setActive(
        JNIEnv *env, jclass clazz, jint sensorType, jboolean enable)
{
    shared_ptr<Sensor> sensor =
            sm->getSensorByType(Sensor::SensorType(sensorType));

    Assert::dieIf(!sensor, "No such type sensor.");

    return sensor->setActive(!!enable) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL isActive(
                            JNIEnv *env, jclass clazz, jint sensorType)
{
    shared_ptr<Sensor> sensor =
            sm->getSensorByType(Sensor::SensorType(sensorType));

    Assert::dieIf(!sensor, "No such type sensor.");

    return sensor->isActive() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL setWearOnRightHand(
        JNIEnv *env, jclass clazz, jboolean isRightHand)
{
    shared_ptr<Sensor> sensor =
            sm->getSensorByType(Sensor::SensorType(Sensor::TYPE_GESTURE));

    if (!sensor) {
        Log::e(LOG_TAG, "No gesture sensor, setting to wear on which hand "
                "only can be done when gesture sensor exists.");
        return JNI_FALSE;
    }

    return sensor->setRightHand(!!isRightHand) ? JNI_TRUE : JNI_FALSE;
}

static JNINativeMethod gSensorServiceMethods[] = {
        {
                "getSensorCount",
                "()I",
                (void *)getSensorCount
        },

        {
                "forEachSensor",
                "(Lcom/ingenic/iwds/smartsense/Sensor;I)V",
                (void *)forEachSensor
        },

        {
                "installSensorEventCallback",
                "(ILcom/ingenic/iwds/smartsense/"
                "SensorService$SensorServiceStub$SensorEventCallback;)V",
                (void *)installSensorEventCallback
        },

        {
                "setActive",
                "(IZ)Z",
                (void *)setActive
        },

        {
                "isActive",
                "(I)Z",
                (void *)isActive
        },

        {
                "setWearOnRightHand",
                "(Z)Z",
                (void *)setWearOnRightHand
        },
};

jint registerSmartSenseNatives(Iwds::Jenv *env)
{
    LocalJobject clazz(env->FindClass(classSensorServiceStub));
    if (!clazz) {
        Log::e(LOG_TAG, "Can not find class: SensorServiceStub");

        return JNI_ERR;
    }

    jint error = env->RegisterNatives(
            toJclass(clazz),
            gSensorServiceMethods,
            sizeof(gSensorServiceMethods) / sizeof(JNINativeMethod));
    if (error) {
        Log::e(LOG_TAG, "Can not register natives for SensorServiceStub.");

        return error;
    }

    return JNI_OK;
}

bool initializeSmartSense()
{
    if (!IwdsRuntime::getInstance()->isXburstPlatform())
        return false;

    sensorApi = new SensorApi();

    if (!sm->initialize()) {
        Log::e(LOG_TAG, string("SmartSense initialize failed: ") +
                                            sm->errorString());
        return false;
    }

    Log::d(LOG_TAG, "Initialize done.");

    return true;
}
