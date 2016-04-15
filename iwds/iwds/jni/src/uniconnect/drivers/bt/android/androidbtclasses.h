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


#ifndef ANDROIDBTCLASSES_H
#define ANDROIDBTCLASSES_H


#include <string>

#include <iwds.h>

#include <jni/javaclass.h>


class BluetoothAdapter : public JavaClass
{
public:
    BluetoothAdapter();
    ~BluetoothAdapter()
    {

    }

    bool enable(
            JavaApiContext *context, Iwds::GlobalJobject adapter);

    bool disable(
            JavaApiContext *context, Iwds::GlobalJobject adapter);

    bool isEnabled(
            JavaApiContext *context, Iwds::GlobalJobject adapter) const;

    bool cancelDiscovery(
            JavaApiContext *context, Iwds::GlobalJobject adapter);

    Iwds::GlobalJobject getDefaultAdapter(JavaApiContext *context);

    Iwds::GlobalJobject listenUsingRfcommWithServiceRecord(
                JavaApiContext *context,
                Iwds::GlobalJobject adapter, Iwds::GlobalJobject name,
                Iwds::GlobalJobject uuid);

    Iwds::GlobalJobject  getRemoteDevice(
            JavaApiContext *context,
            Iwds::GlobalJobject adapter, Iwds::GlobalJobject address);

    bool isAttached(JavaApiContext *context) const;

private:
    Iwds::GlobalJobject m_classGlobalRef;

    jmethodID m_methodGetDefaultAdapter;
    jmethodID m_methodListenUsingRfcommWithServiceRecord;
    jmethodID m_methodGetRemoteDevice;
    jmethodID m_methodEnable;
    jmethodID m_methodDisable;
    jmethodID m_methodIsEnabled;
    jmethodID m_methodCancelDiscovery;
};

class BluetoothDevice : public JavaClass
{
public:
    BluetoothDevice();
    ~BluetoothDevice()
    {

    }

    std::string getAddress(
            JavaApiContext *context, Iwds::GlobalJobject device) const;

    Iwds::GlobalJobject createRfcommSocketToServiceRecord(
            JavaApiContext *context,
            Iwds::GlobalJobject device, Iwds::GlobalJobject uuid);

    bool isAttached(JavaApiContext *context) const;

private:
    Iwds::GlobalJobject m_classGlobalRef;

    jmethodID m_methodCreateRfcommSocketToServiceRecord;
    jmethodID m_methodGetAddress;
};

class BluetoothSocket : public JavaClass
{
public:
    BluetoothSocket();
    ~BluetoothSocket()
    {

    }

    bool connect(JavaApiContext *context, Iwds::GlobalJobject socket);

    bool close(JavaApiContext *context, Iwds::GlobalJobject socket);

    Iwds::GlobalJobject getRemoteDevice(
            JavaApiContext *context, Iwds::GlobalJobject socket);

    Iwds::GlobalJobject getOutputStream(
                JavaApiContext *context, Iwds::GlobalJobject socket);

    Iwds::GlobalJobject getInputStream(
                JavaApiContext *context, Iwds::GlobalJobject socket);

    bool isAttached(JavaApiContext *context) const;

private:

    Iwds::GlobalJobject m_classGlobalRef;

    jmethodID m_methodConnect;
    jmethodID m_methodClose;
    jmethodID m_methodGetOutputStream;
    jmethodID m_methodGetInputStream;
    jmethodID m_methodGetRemoteDevice;
};

class BluetoothServerSocket : public JavaClass
{
public:
    BluetoothServerSocket();
    ~BluetoothServerSocket()
    {

    }

    Iwds::GlobalJobject accept(
            JavaApiContext *context, Iwds::GlobalJobject serverSocket);

    bool close(
            JavaApiContext *context, Iwds::GlobalJobject serverSocket);

    bool isAttached(JavaApiContext *context) const;

private:
    Iwds::GlobalJobject m_classGlobalRef;

    jmethodID m_methodAccept;
    jmethodID m_methodClose;
};

class InputStream : public JavaClass
{
public:
    InputStream(int maxPayLoadSize);
    ~InputStream()
    {

    }

    int read(JavaApiContext *context,
                Iwds::GlobalJobject inputStream,
                char *buffer, unsigned int maxSize);

    bool isAttached(JavaApiContext *context) const;

private:
    Iwds::GlobalJobject m_classGlobalRef;

    jmethodID m_methodRead;

    Iwds::GlobalJobject m_readBuffer;
};

class OutputStream : public JavaClass
{
public:
    OutputStream(int maxPayLoadSize);
    ~OutputStream()
    {

    }

    bool write(
            JavaApiContext *context,
            Iwds::GlobalJobject outputStream,
            const char *buffer, unsigned int size);

    bool flush(
            JavaApiContext *context, Iwds::GlobalJobject outputStream);

    bool isAttached(JavaApiContext *context) const;

private:
    Iwds::GlobalJobject m_classGlobalRef;

    jmethodID m_methodWrite;
    jmethodID m_methodFlush;

    Iwds::GlobalJobject m_writeBuffer;
};

class Uuid
{
public:
    Uuid();
    ~Uuid()
    {

    }

    Iwds::GlobalJobject fromString(
                        JavaApiContext *context, std::string uuid);

    bool isAttached(JavaApiContext *context) const;

private:
    Iwds::GlobalJobject m_classGlobalRef;

    jmethodID m_methodFromString;
};

#endif
