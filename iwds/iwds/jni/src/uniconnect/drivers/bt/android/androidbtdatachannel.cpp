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

#include <utils/assert.h>
#include <utils/log.h>
#include <utils/thread.h>

#include <uniconnect/linkmanager.h>

#include "./androidbtdatachannel.h"

#include "./androidbtapi.h"
#include "./androidmtkbtapi.h"


using std::string;

using std::unique_ptr;
using std::tr1::shared_ptr;

using Iwds::Assert;
using Iwds::Log;
using Iwds::attachCurrentThread;
using Iwds::detachCurrentThread;
using Iwds::IwdsRuntime;
using Iwds::DeviceDescriptor;
using Iwds::Thread;

const string AndroidBtDataChannel::LOG_TAG("Uniconnect: AndroidBtDataChannel: ");

AndroidBtDataChannel::AndroidBtDataChannel() :
    m_btApi()
{

}

AndroidBtDataChannel::~AndroidBtDataChannel()
{

}

bool AndroidBtDataChannel::enable(bool enable)
{
    IWDS_TRACE;

    bool success = false;
    if (enable)
        success = m_btApi->enable();
    else
        success = m_btApi->disable();

    return success;
}

bool AndroidBtDataChannel::reset()
{
    IWDS_TRACE;

    bool ok = enable(false);
    if (!ok)
        return false;

    Thread::sleep(5);

    int timeout = 20;
    for (;;) {
        Thread::sleep(1);

        if (timeout-- == 0)
            return false;

        if (!readyForOperation())
            break;
    }

    Thread::sleep(2);

    ok = enable(true);
    if (!ok)
        return false;

    Thread::sleep(5);

    timeout = 20;
    for (;;) {
        Thread::sleep(1);

        if (timeout-- == 0)
            return false;

        if (readyForOperation())
            break;
    }

    return true;
}

bool AndroidBtDataChannel::readyForOperation()
{
    IWDS_TRACE;

    if (!m_btApi->isEnabled()) {
        setErrorString("android BT was disabled");

        return false;
    }

    return true;
}

bool AndroidBtDataChannel::readThreadReadyToRun()
{
    IWDS_TRACE;

    if (!attachCurrentThread()) {
        setErrorString("failed to attach current thread");

        return false;
    }

    return true;
}

bool AndroidBtDataChannel::writeThreadReadyToRun()
{
    IWDS_TRACE;

    if (!attachCurrentThread()) {
        setErrorString("failed to attach current thread");

        return false;
    }

    return true;
}

void AndroidBtDataChannel::readThreadAtExit()
{
    IWDS_TRACE;

    detachCurrentThread();
}

void AndroidBtDataChannel::writeThreadAtExit()
{
    IWDS_TRACE;

    detachCurrentThread();
}

bool AndroidBtDataChannel::listenerThreadReadyToRun()
{
    IWDS_TRACE;

    if (!attachCurrentThread()) {
        setErrorString("failed to attach current thread");

        return false;
    }

    return true;
}

void AndroidBtDataChannel::listenerThreadAtExit()
{
    IWDS_TRACE;

    detachCurrentThread();
}

bool AndroidBtDataChannel::connectThreadReadyToRun()
{
    IWDS_TRACE;

    if (!attachCurrentThread())
        return false;

    return true;
}

void AndroidBtDataChannel::connectThreadAtExit()
{
    IWDS_TRACE;

    detachCurrentThread();
}

shared_ptr<DataChannel> AndroidBtDataChannel::createChannel()
{
    return shared_ptr<DataChannel>(new AndroidBtDataChannel());
}

bool AndroidBtDataChannel::probe(void *reserved)
{
    IWDS_TRACE;

    AndroidBtApi *btApi;

    shared_ptr<IwdsRuntime> runtime = IwdsRuntime::getInstance();
    if (runtime->getDeviceClass() ==
            DeviceDescriptor::DEVICE_CLASS_MOBILE &&
            runtime->isMtkPlatform()) {

        btApi = new AndroidMtkBtApi(this);
    } else {

        btApi = new AndroidBtApi(this);
    }

    m_btApi.reset(btApi);
    if (!m_btApi->isAttached()) {
        setErrorString(m_btApi->errorString());

        return false;
    }

    return true;
}

int AndroidBtDataChannel::read(char *buffer, int maxSize)
{
    return m_btApi->read(buffer, maxSize);
}

bool AndroidBtDataChannel::write(const char *buffer, int size)
{
    return m_btApi->write(buffer, size);
}

bool AndroidBtDataChannel::flush()
{
    return m_btApi->flush();
}

shared_ptr<DataChannelTag> AndroidBtDataChannel::trait() const
{
    return shared_ptr<DataChannelTag>(new ChannelCateory());
}

bool AndroidBtDataChannel::waitForConnect()
{
    IWDS_TRACE;

    if (!m_btApi->waitForConnect()) {
        setErrorString(m_btApi->errorString());

        return false;
    }

    return true;
}

bool AndroidBtDataChannel::connect()
{
    IWDS_TRACE;

    string serverAddress = getRemoteAddress();
    if (serverAddress.empty()) {
        setErrorString("no valid address");
        return false;
    }

    if (!m_btApi->connect(serverAddress)) {
        setErrorString(m_btApi->errorString());

        return false;
    }

    return true;
}

void AndroidBtDataChannel::clientSideDisconnect()
{
    m_btApi->socketClose();
}

void AndroidBtDataChannel::serverSideDisconnect()
{
    m_btApi->socketClose();
}

static struct AndroidDataChannelRegister
{
    AndroidDataChannelRegister()
    {
        shared_ptr<LinkManager> lm = LinkManager::getInstance();
        lm->registerDataChannel(
                shared_ptr<DataChannel>(new AndroidBtDataChannel()));
    }
} the_register;
