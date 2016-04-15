/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  SunWenZhong(Fighter) <wzsun@ingenic.com, wanmyqawdr@126.com>
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


#include <utils/assert.h>
#include <utils/log.h>

#include <uniconnect/linkmanager.h>

#include "./bledatachannel.h"


using std::string;
using std::tr1::shared_ptr;

using Iwds::Assert;
using Iwds::Log;
using Iwds::Mutex;
using Iwds::ByteArray;

static const string LOG_TAG("Uniconnect: BleDataChannel: ");

BleDataChannel::~BleDataChannel()
{

}

BleDataChannel::BleDataChannel()
{

}

shared_ptr<DataChannel> BleDataChannel::createChannel()
{
    return shared_ptr<DataChannel>(new BleDataChannel());
}


bool BleDataChannel::probe(void *reserved)
{
    setErrorString("unsupported yet");

    return false;
}

int BleDataChannel::read(char *buffer, int maxSize)
{
    IWDS_TRACE;

    return -1;
}

bool BleDataChannel::write(const char *buffer, int size)
{
    IWDS_TRACE;

    return false;
}

bool BleDataChannel::flush()
{
    return false;
}

shared_ptr<DataChannelTag> BleDataChannel::trait() const
{
    return shared_ptr<DataChannelTag>(new ChannelCateory());
}

bool BleDataChannel::waitForConnect()
{
    return false;
}

bool BleDataChannel::connect()
{
    return false;
}

void BleDataChannel::clientSideDisconnect()
{

}

void BleDataChannel::serverSideDisconnect()
{

}

static struct BleDataChannelRegister
{
    BleDataChannelRegister()
    {
        shared_ptr<LinkManager> lm = LinkManager::getInstance();
        lm->registerDataChannel(
                shared_ptr<DataChannel>(new BleDataChannel()));
    }
} the_register;
