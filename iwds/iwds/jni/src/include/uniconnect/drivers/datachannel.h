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
 
 
#ifndef DATACHANNEL_H
#define DATACHANNEL_H

#include <string>
#include <tr1/memory>

#include <iwds.h>
#include <utils/mutex.h>

struct DataChannelTag {
    virtual ~DataChannelTag()
    {

    }

    virtual std::string tag() const = 0;

    virtual bool isValidAddress(const std::string &address) const = 0;
};

struct BtDataChannelTag : public DataChannelTag
{
    bool isValidAddress(const std::string &address) const
    {
        static const int ADDRESS_LENGTH = 17;

        if (address.empty() || address.length() != ADDRESS_LENGTH)
            return false;

        for (int i = 0; i < ADDRESS_LENGTH; i++) {
            char c = address.at(i);
            switch (i % 3) {
            case 0:
            case 1:
                if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')) {
                    // hex character, OK
                    break;
                }

                return false;

            case 2:
                if (c == ':')
                    break;  // OK

                return false;
            }
        }

        return true;
    }
};

struct BleDataChannelTag : public BtDataChannelTag
{
    std::string tag() const
    {
        return "BLE data channel";
    }
};

struct AndroidBtDataChannelTag : public BleDataChannelTag
{
    std::string tag() const
    {
        return "Android BT data channel";
    }
};

class DataChannel
{
public:
    DataChannel();

    virtual ~DataChannel();

    virtual bool enable(bool enable);

    virtual bool reset();

    virtual bool readyForOperation();

    virtual bool readThreadReadyToRun();
    virtual void readThreadAtExit();

    virtual bool writeThreadReadyToRun();
    virtual void writeThreadAtExit();

    virtual bool listenerThreadReadyToRun();
    virtual void listenerThreadAtExit();

    virtual bool connectThreadReadyToRun();
    virtual void connectThreadAtExit();

    virtual std::string getRemoteAddress() const;
    virtual bool setRemoteAddress(const std::string &address);

    /*
     * TODO:
     * ugly thing is oh yes this is a none-static factory.
     * why? several line codes saved, that's all.
     */
    virtual std::tr1::shared_ptr<DataChannel> createChannel() = 0;

    virtual bool probe(void *reserved) = 0;

    virtual int read(char *buffer, int maxSize) = 0;
    virtual bool write(const char *buffer, int size) = 0;
    virtual bool flush() = 0;

    virtual bool waitForConnect() = 0;
    virtual bool connect() = 0;
    virtual void clientSideDisconnect() = 0;
    virtual void serverSideDisconnect() = 0;

    virtual std::tr1::shared_ptr<DataChannelTag> trait() const = 0;

    virtual int getMaxPayloadSize() const
    {
        return 0x20000 >> 1;
    }

    std::string errorString() const;

protected:
    void setErrorString(const std::string &errorString);

private:
    mutable Iwds::Mutex m_errorStringLock;
    std::string m_errorString;

    mutable Iwds::Mutex m_remoteAddressLock;
    std::string m_remoteAddress;
};

#endif
