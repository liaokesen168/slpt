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


#ifndef BLE_DATA_CHANNEL_H
#define BLE_DATA_CHANNEL_H


#include <string>
#include <tr1/memory>

#include <uniconnect/drivers/datachannel.h>


class BleDataChannel : public DataChannel
{
public:
    typedef BleDataChannelTag ChannelCateory;

    ~BleDataChannel();

    std::tr1::shared_ptr<DataChannel> createChannel();

    bool probe(void *reserved);

    int read(char *buffer, int size);
    bool write(const char *buffer, int maxSize);
    bool flush();

    std::tr1::shared_ptr<DataChannelTag> trait() const;

    bool waitForConnect();
    bool connect();

    void clientSideDisconnect();
    void serverSideDisconnect();

private:
    BleDataChannel();

    friend class BleDataChannelRegister;
};


#endif
