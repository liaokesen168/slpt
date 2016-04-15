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


#ifndef SENSORDEVICE_H
#define SENSORDEVICE_H


#include <tr1/memory>

#include <string>

#include <utils/thread.h>
#include <utils/mutex.h>

#include <smartsense/hal/sensors.h>


class SensorDevice : public Iwds::Thread
{
public:
    static std::tr1::shared_ptr<SensorDevice> getInstance();

    bool initialize();

    int getSensorList(sensor_t const **list);
    int activate(int handle, int enabled);
    int setDelay(int handle, int64_t ns);
    int setRightHand(int handle, int isRightHand);

protected:
    bool readyToRun();

    bool run();

private:
    SensorDevice();

    int poll(sensors_event_t* buffer, size_t count);

    Iwds::Mutex m_pollLock;

    struct sensors_poll_device_t* m_sensorDevice;
    struct sensors_module_t* m_sensorModule;
};


#endif
