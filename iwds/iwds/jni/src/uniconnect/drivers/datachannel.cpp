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


#include <uniconnect/drivers/datachannel.h>

using std::string;

using Iwds::Mutex;

DataChannel::DataChannel() :
    m_errorStringLock(),
    m_errorString(""),
    m_remoteAddressLock(),
    m_remoteAddress("")
{

}

DataChannel::~DataChannel()
{

}

bool DataChannel::enable(bool enable)
{
    return true;
}

bool DataChannel::reset()
{
    return true;
}

bool DataChannel::readyForOperation()
{
    return true;
}

bool DataChannel::readThreadReadyToRun()
{
    return true;
}

bool DataChannel::writeThreadReadyToRun()
{
    return true;
}

void DataChannel::readThreadAtExit()
{

}

void DataChannel::writeThreadAtExit()
{

}

bool DataChannel::listenerThreadReadyToRun()
{
    return true;
}

void DataChannel::listenerThreadAtExit()
{

}

bool DataChannel::connectThreadReadyToRun()
{
    return true;
}

void DataChannel::connectThreadAtExit()
{

}

string DataChannel::getRemoteAddress() const
{
    Mutex::Autolock l(&m_remoteAddressLock);

    return m_remoteAddress;
}


bool DataChannel::setRemoteAddress(const std::string &address)
{
    Mutex::Autolock l(&m_remoteAddressLock);

    /*
     * clear operation
     */
    if (address.empty()) {
        m_remoteAddress.clear();

        return true;
    }

    /*
     * only accept a valid address
     */
    if (!trait()->isValidAddress(address)) {
        setErrorString("invalid server address");

        return false;
    }

    m_remoteAddress = address;

    return true;
}

void DataChannel::setErrorString(const string &errorString)
{
    Mutex::Autolock l(&m_errorStringLock);

    m_errorString = errorString;
}

string DataChannel::errorString() const
{
    Mutex::Autolock l(&m_errorStringLock);

    return m_errorString;
}
