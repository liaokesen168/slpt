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


#include <smartsense/sensor.h>
#include <smartsense/sensoreventcallback.h>
#include <smartsense/sensormanager.h>


using std::tr1::shared_ptr;
using std::string;

using Iwds::ProtectedList;
using Iwds::Log;
using Iwds::Assert;
using Iwds::Mutex;


static const string LOG_TAG("SmartSense: SensorManager: ");

SensorManager::SensorManager() :
    m_sensors(),
    m_errorStringLock(),
    m_errorString(""),
    m_sensorDevice(SensorDevice::getInstance())
{

}

std::tr1::shared_ptr<SensorManager> SensorManager::getInstance()
{
    static shared_ptr<SensorManager> the_manager(new SensorManager());

    return the_manager;
}

bool SensorManager::initialize()
{
    if (!m_sensorDevice->initialize()) {
        setErrorString(
                "failed to initialize sensor HAL: " +
                    m_sensorDevice->errorString());

        return false;
    }

    sensor_t const *list;
    int N = m_sensorDevice->getSensorList(&list);
    if (N <= 0) {
        setErrorString(
                "failed to initialize sensor HAL: no sensors or error");

        return false;
    }

    for (int i = 0; i < N; i++) {
        enum Sensor::SensorType type;

        const sensor_t *hal_sensor = list + i;

        switch (hal_sensor->type) {
        case SENSOR_TYPE_RELATIVE_HUMIDITY:
            type = Sensor::TYPE_RELATIVE_HUMIDITY;

            break;

        case SENSOR_TYPE_AMBIENT_TEMPERATURE:
            type = Sensor::TYPE_AMBIENT_TEMPERATURE;

            break;

        case SENSOR_TYPE_STEP_COUNTER:
            type = Sensor::TYPE_STEP_COUNTER;

            break;

        case SENSOR_TYPE_HEART_RATE:
            type = Sensor::TYPE_HEART_RATE;

            break;

        case SENSOR_TYPE_GESTURE:
            type = Sensor::TYPE_GESTURE;

            break;

        case SENSOR_TYPE_MOTION:
            type = Sensor::TYPE_MOTION;

            break;

        case SENSOR_TYPE_PRESSURE:
            type = Sensor::TYPE_PRESSURE;

            break;

        case SENSOR_TYPE_UV:
            type = Sensor::TYPE_UV;

            break;

        case SENSOR_TYPE_VOICE_TRIGGER:
            type = Sensor::TYPE_VOICE_TRIGGER;

            break;

        case SENSOR_TYPE_PROXIMITY:
            type = Sensor::TYPE_PROXIMITY;

            break;

        default:
            Log::e(LOG_TAG,
                    "Unsupported sensor: " + string(hal_sensor->name));
            continue;

            break;
        }

        Assert::dieIf(
                getSensorByType(type),
                "Duplicated sensor type: " + string(hal_sensor->name));

        shared_ptr<Sensor> sensor(new Sensor(type));
        fillSensorByHal(sensor.get(), hal_sensor);

        Log::i(LOG_TAG, "New sensor: " + sensor->getName());

        if (!sensor->initialize()) {
            setErrorString(sensor->errorString());

            return false;
        }

        m_sensors.append(shared_ptr<Sensor>(sensor));
    } // end for

    if (!m_sensorDevice->start()) {
        setErrorString(m_sensorDevice->errorString());

        return false;
    }

    return true;
}

void SensorManager::fillSensorByHal(
                            Sensor *sensor, const sensor_t *hal) const
{
    sensor->setHandle(hal->handle);
    sensor->setMaxRange(hal->maxRange);
    sensor->setMinDelay(hal->minDelay);
    sensor->setName(hal->name);
    sensor->setResolution(hal->resolution);
    sensor->setVendor(hal->vendor);
    sensor->setVersion(hal->version);
}

int SensorManager::getSensorCount() const
{
    return (int)m_sensors.size();
}

bool SensorManager::intallSensorEventCallback(
                    enum Sensor::SensorType sensorType,
                    std::tr1::shared_ptr<SensorEventCallback> callback)
{
    shared_ptr<Sensor> sensor = getSensorByType(sensorType);

    Assert::dieIf(!sensor, "No such sensor type");

    if (!sensor->installSensorEventCallback(callback)) {
        setErrorString(sensor->errorString());

        return false;
    }

    return true;
}

bool SensorManager::handleEvent(sensors_event_t *hal_event)
{
    m_sensors.lock();
    for (shared_ptr<Sensor> sensor : *m_sensors.data()) {
        m_sensors.unlock();

        if (sensor->getType() == hal_event->type) {
            if (!sensor->handleEvent(hal_event)) {
                setErrorString(sensor->errorString());

                return false;
            }

            return true;
        }

        m_sensors.lock();
    }
    m_sensors.unlock();

    setErrorString("Invalid sensor type");

    return false;
}

shared_ptr<Sensor> SensorManager::getSensorByIndex(int index)
{
    Mutex::Autolock l(m_sensors.locker());

    Assert::dieIf(index < 0 ||
            index > int(m_sensors.sizeNolock() - 1), "Invalid index.");

    ProtectedList<shared_ptr<Sensor> >::iterator it =
                                            m_sensors.data()->begin();
    std::advance(it, index);

    return *it;
}

shared_ptr<Sensor> SensorManager::getSensorByType(
                                        enum Sensor::SensorType type)
{
    Mutex::Autolock l(m_sensors.locker());

    return getSensorByTypeNolock(type);
}

std::tr1::shared_ptr<Sensor> SensorManager::getSensorByTypeNolock(
                                    enum Sensor::SensorType type)
{
    ProtectedList<shared_ptr<Sensor> >::iterator it =
                                            m_sensors.data()->begin();
    for (; it != m_sensors.data()->end(); it++)
        if ((*it)->getType() == type)
            return *it;

    return shared_ptr<Sensor>();
}

bool SensorManager::setSensorActive(
                            enum Sensor::SensorType type, bool enable)
{
    shared_ptr<Sensor> sensor = getSensorByType(type);

    Assert::dieIf(!sensor, "No such sensor type");

    return sensor->setActive(enable);
}

bool SensorManager::isSensorActive(enum Sensor::SensorType type)
{
    shared_ptr<Sensor> sensor = getSensorByType(type);

    Assert::dieIf(!sensor, "No such sensor type");

    return sensor->isActive();
}

void SensorManager::setErrorString(const std::string &errorString)
{
    Mutex::Autolock l(&m_errorStringLock);

    m_errorString = errorString;
}

string SensorManager::errorString() const
{
    Mutex::Autolock l(&m_errorStringLock);

    return m_errorString;
}
