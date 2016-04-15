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


#ifndef ANDROIDBTAPI_H
#define ANDROIDBTAPI_H


#include <string>
#include <memory>
#include <tr1/memory>

#include <iwds.h>

#include <utils/thread.h>
#include <utils/mutex.h>

#include "./androidbtclasses.h"


class AndroidBtDataChannel;


class AndroidBtApi : public JavaApiContext
{
public:
    AndroidBtApi(AndroidBtDataChannel *dataChannel);
    virtual ~AndroidBtApi()
    {

    }

    bool isAttached();

    virtual bool enable();
    virtual bool disable();
    virtual bool isEnabled();
    virtual bool waitForConnect();
    virtual bool connect(const std::string &serverAddress);
    virtual void socketClose();
    virtual std::string getRemoteAddress();

    virtual int read(char *buffer, int maxSize);
    virtual bool write(const char *buffer, int size);
    virtual bool flush();

protected:
    static BluetoothAdapter *sm_classBtAdapter;
    static BluetoothDevice *sm_classBtDevice;
    static BluetoothSocket *sm_classBtSocket;
    static BluetoothServerSocket *sm_classBtServerSocket;
    static InputStream *sm_classInputStream;
    static OutputStream *sm_classOutputStream;
    static Uuid *sm_classUuid;
    static const std::string sm_uuid;
    static Iwds::Mutex sm_classLock;

    AndroidBtDataChannel *m_dataChannel;

    Iwds::GlobalJobject m_adapter;
    Iwds::GlobalJobject m_uuid;

    Iwds::GlobalJobject m_btServerSocket;
    Iwds::GlobalJobject m_socket;
    Iwds::GlobalJobject m_device;
    Iwds::GlobalJobject m_inputStream;
    Iwds::GlobalJobject m_outputStream;

    Iwds::Mutex m_lock;
};

#endif
