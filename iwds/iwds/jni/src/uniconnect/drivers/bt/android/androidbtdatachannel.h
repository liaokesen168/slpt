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


#ifndef ANDROIDBTDATACHANNEL_H
#define ANDROIDBTDATACHANNEL_H

#include <string>

#include <memory>
#include <tr1/memory>

#include <iwds.h>

#include <utils/mutex.h>
#include <utils/log.h>
#include <utils/assert.h>

#include <uniconnect/drivers/datachannel.h>

class AndroidBtApi;

class AndroidBtDataChannel : public DataChannel
{
public:
    static const std::string LOG_TAG;

    typedef AndroidBtDataChannelTag ChannelCateory;

    ~AndroidBtDataChannel();

    bool enable(bool enable);

    bool reset();

    bool readyForOperation();

    bool readThreadReadyToRun();
    void readThreadAtExit();

    bool writeThreadReadyToRun();
    void writeThreadAtExit();

    bool listenerThreadReadyToRun();
    void listenerThreadAtExit();

    bool connectThreadReadyToRun();
    void connectThreadAtExit();

    std::tr1::shared_ptr<DataChannel> createChannel();

    bool probe(void *reserved);

    int read(char *buffer, int maxSize);
    bool write(const char *buffer, int size);
    bool flush();

    std::tr1::shared_ptr<DataChannelTag> trait() const;

    bool waitForConnect();
    bool connect();

    void clientSideDisconnect();
    void serverSideDisconnect();

private:
    AndroidBtDataChannel();

    std::unique_ptr<AndroidBtApi> m_btApi;

    friend class AndroidDataChannelRegister;
    friend class AndroidBtApi;

};

#endif
