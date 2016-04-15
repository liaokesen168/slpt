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

#include <utils/log.h>
#include <utils/thread.h>
#include <utils/mutex.h>
#include <utils/assert.h>

#include <smartsense/sensormanager.h>
#include <smartsense/hal/sensordevice.h>


using std::tr1::shared_ptr;
using std::string;

using Iwds::Thread;
using Iwds::Mutex;
using Iwds::Log;
using Iwds::Assert;
using Iwds::attachCurrentThread;


static const string LOG_TAG("SamrtSense: SensorDevice: ");

shared_ptr<SensorDevice> SensorDevice::getInstance()
{
    static shared_ptr<SensorDevice> the_sensorDevice(new SensorDevice());

    return the_sensorDevice;
}

SensorDevice::SensorDevice() :
    m_pollLock(),
    m_sensorDevice(0),
    m_sensorModule(0)
{

}

bool SensorDevice::initialize()
{
    int err = hw_get_module(
                        SENSORS_HARDWARE_MODULE_ID,
                            (hw_module_t const**)&m_sensorModule);

    if (err) {
        setErrorString("Can not load sensor module.");

        return false;
    }

    string moduleName(m_sensorModule->common.name);

    if (moduleName != SMART_SENSE_MODULE_NAME) {
        setErrorString(
                "not a valid sensor module, name: " + moduleName +
                "(must be: " SMART_SENSE_MODULE_NAME + ")");

        return false;
    }

    /*
     * seek to sensors_module_t[1]
     */
    m_sensorModule++;

    err = sensors_open_for_iwds(&m_sensorModule->common, &m_sensorDevice);
    if (err) {
        setErrorString("Can not open device for sensor module.");

        return false;
    }

    Assert::dieIf(
            !m_sensorDevice, "Object of sensor_poll_device_t is null.");

    return true;
}

bool SensorDevice::readyToRun()
{
    if (!attachCurrentThread()) {
        setErrorString("failed to attach current thread");

        return false;
    }

    return true;
}

bool SensorDevice::run()
{
    shared_ptr<SensorManager> manager = SensorManager::getInstance();

    sensors_event_t hal_events[32];

    for (;;) {
        int count;

        do {
            count = m_sensorDevice->poll(m_sensorDevice, hal_events, 32);
        } while (count == -EINTR);

        Assert::dieIf(
                count < 0, "Poll failed: " + string(strerror(count)));

        for (int i = 0; i < count; i++) {
            sensors_event_t *event = hal_events + i;

            Assert::dieIf(
                    !manager->handleEvent(event),
                    "Failed on handle hal event: " +
                    manager->errorString());
        }
    }

    return true;
}

int SensorDevice::getSensorList(sensor_t const **list)
{
    return m_sensorModule->get_sensors_list(m_sensorModule, list);
}

int SensorDevice::activate(int handle, int enabled)
{
    return m_sensorDevice->activate(m_sensorDevice, handle, enabled);
}

int SensorDevice::setDelay(int handle, int64_t ns)
{
    return m_sensorDevice->setDelay(m_sensorDevice, handle, ns);
}

int SensorDevice::setRightHand(int handle, int isRightHand)
{
    return m_sensorDevice->setRightHand(m_sensorDevice, handle, isRightHand);
}

