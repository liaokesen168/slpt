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
 */


#ifndef SENSOR_H
#define SENSOR_H


#include <tr1/memory>
#include <string>

#include <utils/mutex.h>
#include <utils/protectedlist.h>

#include <smartsense/hal/sensordevice.h>


class SensorManager;
class SensorEventCallback;

class Sensor
{
public:
    enum SensorType {
        TYPE_STEP_COUNTER = SENSOR_TYPE_STEP_COUNTER,
        TYPE_RELATIVE_HUMIDITY = SENSOR_TYPE_RELATIVE_HUMIDITY,
        TYPE_AMBIENT_TEMPERATURE = SENSOR_TYPE_AMBIENT_TEMPERATURE,
        TYPE_PRESSURE = SENSOR_TYPE_PRESSURE,
        TYPE_UV = SENSOR_TYPE_UV,
        TYPE_VOICE_TRIGGER = SENSOR_TYPE_VOICE_TRIGGER,
        TYPE_HEART_RATE = SENSOR_TYPE_HEART_RATE,  /* define in android 5.1 */
        TYPE_GESTURE = SENSOR_TYPE_GESTURE,
        TYPE_MOTION = SENSOR_TYPE_MOTION,
        TYPE_PROXIMITY = SENSOR_TYPE_PROXIMITY,

        TYPE_ALL = -1,
    };

    Sensor(enum SensorType sensorType);
    virtual ~Sensor();

    virtual bool initialize();

    virtual bool setActive(bool enable);
    virtual bool isActive() const;

    virtual bool setRightHand(bool isRightHand);

    bool installSensorEventCallback(
            std::tr1::shared_ptr<SensorEventCallback> callback);

    virtual bool handleEvent(sensors_event_t *hal_event);

    std::string getName() const;
    std::string getVendor() const;
    int getHandle() const;
    enum SensorType getType() const;
    float getMaxRange() const;
    float getResolution() const;
    int getMinDelay() const;
    int getVersion() const;

    std::string errorString() const;

protected:
    void setName(const std::string &name);
    void setVendor(const std::string &vendor);
    void setHandle(int handle);
    void setMaxRange(float maxRange);
    void setResolution(float resolution);
    void setMinDelay(int minDelay);
    void setVersion(int version);

private:
    void setErrorString(const std::string &errorString);

    std::string m_name;
    std::string m_vendor;
    int m_handle;
    enum SensorType m_type;
    float m_maxRange;
    float m_resolution;
    int m_minDelay;
    int m_version;

    bool m_isActived;
    std::tr1::shared_ptr<SensorEventCallback> m_eventCallback;
    mutable Iwds::Mutex m_eventCallbackLock;

    std::string m_errorString;
    mutable Iwds::Mutex m_errorStringLock;

    friend class SensorManager;
};

#endif
