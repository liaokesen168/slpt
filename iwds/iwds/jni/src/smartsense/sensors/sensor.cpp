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


#include <smartsense/sensoreventcallback.h>
#include <smartsense/sensor.h>


using std::tr1::shared_ptr;
using std::string;

using Iwds::Mutex;

Sensor::Sensor(enum SensorType sensorType) :
    m_name(""),
    m_vendor(""),
    m_handle(-1),
    m_type(sensorType),
    m_maxRange(-1.0f),
    m_resolution(-1.0f),
    m_minDelay(-1),
    m_version(-1),
    m_isActived(false),
    m_eventCallback(),
    m_eventCallbackLock(),
    m_errorString(""),
    m_errorStringLock()
{

}

Sensor::~Sensor()
{

}

bool Sensor::initialize()
{
    if (!setActive(false)) {
        setErrorString("Failed to deactive sensor: " + getName());

        return false;
    }

    return true;
}

bool Sensor::setActive(bool enable)
{
    Mutex::Autolock l(&m_eventCallbackLock);

    if (m_isActived == enable)
        return true;

    m_isActived = enable;

    shared_ptr<SensorDevice> dev = SensorDevice::getInstance();

    return !dev->activate(getHandle(), enable ? 1 : 0);
}

bool Sensor::isActive() const
{
    Mutex::Autolock l(&m_eventCallbackLock);

    return m_isActived;
}

bool Sensor::setRightHand(bool isRightHand)
{
    shared_ptr<SensorDevice> dev = SensorDevice::getInstance();
    return !dev->setRightHand(getHandle(), isRightHand ? 1 : 0);
}

bool Sensor::installSensorEventCallback(
                std::tr1::shared_ptr<SensorEventCallback> callback)
{
    Mutex::Autolock l(&m_eventCallbackLock);

    m_eventCallback = callback;

    return true;
}

bool Sensor::handleEvent(sensors_event_t *hal_event)
{
    Mutex::Autolock l(&m_eventCallbackLock);

    if (!m_isActived) {
        setErrorString("already deactived");

        return true;
    }

    if (m_eventCallback)
        m_eventCallback->onSensorEvent(hal_event);

    return true;
}

std::string Sensor::getName() const
{
    return m_name;
}

std::string Sensor::getVendor() const
{
    return m_vendor;
}

int Sensor::getHandle() const
{
    return m_handle;
}

enum Sensor::SensorType Sensor::getType() const
{
    return m_type;
}

float Sensor::getMaxRange() const
{
    return m_maxRange;
}

float Sensor::getResolution() const
{
    return m_resolution;
}

int Sensor::getMinDelay() const
{
    return m_minDelay;
}

int Sensor::getVersion() const
{
    return m_version;
}
void Sensor::setName(const std::string &name)
{
    m_name = name;
}

void Sensor::setVendor(const std::string &vendor)
{
    m_vendor = vendor;
}

void Sensor::setHandle(int handle)
{
    m_handle = handle;
}

void Sensor::setMaxRange(float maxRange)
{
    m_maxRange = maxRange;
}

void Sensor::setResolution(float resolution)
{
    m_resolution = resolution;
}

void Sensor::setMinDelay(int minDelay)
{
    m_minDelay = minDelay;
}

void Sensor::setVersion(int version)
{
    m_version = version;
}

void Sensor::setErrorString(const std::string &errorString)
{
    Mutex::Autolock l(&m_errorStringLock);

    m_errorString = errorString;
}

string Sensor::errorString() const
{
    Mutex::Autolock l(&m_errorStringLock);

    return m_errorString;
}
