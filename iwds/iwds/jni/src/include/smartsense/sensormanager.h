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


#ifndef SENSORMANAGER_H
#define SENSORMANAGER_H


#include <tr1/memory>
#include <string>

#include <iwds.h>

#include <utils/protectedlist.h>
#include <utils/mutex.h>

#include <smartsense/hal/sensordevice.h>

#include <smartsense/sensor.h>


class SensorEventCallback;

class SensorManager
{
public:
    static std::tr1::shared_ptr<SensorManager> getInstance();

    bool initialize();

    int getSensorCount() const;
    bool intallSensorEventCallback(
                enum Sensor::SensorType sensorType,
                std::tr1::shared_ptr<SensorEventCallback> callback);

    std::tr1::shared_ptr<Sensor> getSensorByIndex(int index);
    std::tr1::shared_ptr<Sensor> getSensorByType(
                                        enum Sensor::SensorType type);

    bool setSensorActive(enum Sensor::SensorType type, bool enable);
    bool isSensorActive(enum Sensor::SensorType type);

    std::string errorString() const;

    bool handleEvent(sensors_event_t *hal_event);

private:
    SensorManager();

    std::tr1::shared_ptr<Sensor> getSensorByTypeNolock(
                                        enum Sensor::SensorType type);

    void fillSensorByHal(Sensor *sensor, const sensor_t *hal) const;

    void setErrorString(const std::string &errorString);

    Iwds::ProtectedList<std::tr1::shared_ptr<Sensor> > m_sensors;

    mutable Iwds::Mutex m_errorStringLock;
    std::string m_errorString;

    std::tr1::shared_ptr<SensorDevice> m_sensorDevice;
};



#endif
