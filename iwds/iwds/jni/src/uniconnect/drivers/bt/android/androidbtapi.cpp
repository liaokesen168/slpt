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


#include <utils/log.h>
#include <utils/assert.h>

#include <uniconnect/package.h>

#include "./androidbtdatachannel.h"

#include "./androidbtapi.h"


using std::unique_ptr;
using std::shared_ptr;

using std::string;

using Iwds::Log;
using Iwds::Assert;
using Iwds::AutoJenv;
using Iwds::getJenv;
using Iwds::getJvm;
using Iwds::Jenv;
using Iwds::findGlobalRefClass;
using Iwds::getStaticMethodId;
using Iwds::getMethodId;
using Iwds::GlobalJobject;
using Iwds::toJclass;
using Iwds::globalRefJobject;
using Iwds::checkExceptionAndDump;
using Iwds::LocalJobject;
using Iwds::Mutex;


#if 1
    const string AndroidBtApi::sm_uuid("6927cfe7-3c38-da90-18bb-78cb60312b3e");
#else
    const string AndroidBtApi::sm_uuid("00001101-0000-1000-8000-00805F9B34FB");
#endif

Mutex AndroidBtApi::sm_classLock;

BluetoothAdapter *AndroidBtApi::sm_classBtAdapter = 0;
BluetoothDevice *AndroidBtApi::sm_classBtDevice = 0;
BluetoothSocket *AndroidBtApi::sm_classBtSocket = 0;
BluetoothServerSocket *AndroidBtApi::sm_classBtServerSocket = 0;
InputStream *AndroidBtApi::sm_classInputStream = 0;
OutputStream *AndroidBtApi::sm_classOutputStream = 0;
Uuid *AndroidBtApi::sm_classUuid = 0;

AndroidBtApi::AndroidBtApi(AndroidBtDataChannel *dataChannel) :
    m_dataChannel(dataChannel),
    m_adapter(),
    m_uuid(),
    m_btServerSocket(),
    m_socket(),
    m_device(),
    m_inputStream(),
    m_outputStream(),
    m_lock()
{
    sm_classLock.lock();
    if (!sm_classBtAdapter) {
        AndroidBtApi::sm_classBtAdapter = new BluetoothAdapter();
        AndroidBtApi::sm_classBtDevice = new BluetoothDevice();
        AndroidBtApi::sm_classBtSocket = new BluetoothSocket();
        AndroidBtApi::sm_classBtServerSocket = new BluetoothServerSocket();
        AndroidBtApi::sm_classInputStream =
                new InputStream(m_dataChannel->getMaxPayloadSize());
        AndroidBtApi::sm_classOutputStream =
                new OutputStream(m_dataChannel->getMaxPayloadSize() + sizeof(PackageHeader));
        AndroidBtApi::sm_classUuid = new Uuid();
    }
    sm_classLock.unlock();

    m_adapter = sm_classBtAdapter->getDefaultAdapter(this);
    m_uuid = sm_classUuid->fromString(this, sm_uuid);
}

bool AndroidBtApi::isAttached()
{
    return sm_classBtAdapter->isAttached(this) &&
            sm_classBtDevice->isAttached(this) &&
            sm_classBtSocket->isAttached(this) &&
            sm_classBtServerSocket->isAttached(this) &&
            sm_classInputStream->isAttached(this) &&
            sm_classOutputStream->isAttached(this) &&
            sm_classUuid->isAttached(this) &&
            m_adapter;
}

bool AndroidBtApi::enable()
{
    return sm_classBtAdapter->enable(this, m_adapter);
}

bool AndroidBtApi::disable()
{
    return sm_classBtAdapter->disable(this, m_adapter);
}

bool AndroidBtApi::isEnabled()
{
    return sm_classBtAdapter->isEnabled(this, m_adapter);
}

bool AndroidBtApi::waitForConnect()
{
    AutoJenv env;

    /*****************************************************************
     * STEP 1:
     * BluetoothServerSocket serverSocket =
     *          adapter.listenUsingRfcommWithServiceRecord(
     *                              "UiconnectAndroidBtServer", <UUID>)
     *
     ****************************************************************/
    LocalJobject temp(env->NewStringUTF("UiconnectAndroidBtServer"));
    GlobalJobject name = globalRefJobject(temp.data());

    GlobalJobject serverSocket =
            sm_classBtAdapter->listenUsingRfcommWithServiceRecord(
                                        this, m_adapter, name, m_uuid);
    if (!serverSocket)
        return false;


    /*****************************************************************
     * STEP 2:
     * BluetoothSocket client = serverSocket.accept();
     *
     ****************************************************************/
    m_lock.lock();
    m_btServerSocket = serverSocket;
    m_lock.unlock();


    GlobalJobject socket = sm_classBtServerSocket->accept(this, serverSocket);
    if (!socket)
        return false;

    {
        Mutex::Autolock l(&m_lock);

        m_socket = socket;

        m_device = sm_classBtSocket->getRemoteDevice(this, m_socket);
        if(!m_device) {
            setErrorString("Failed to get remote bluetooth device.");

            return false;
        }

        m_inputStream =
                sm_classBtSocket->getInputStream(this, m_socket);
        if (!m_inputStream)
            return false;

        m_outputStream =
                sm_classBtSocket->getOutputStream(this, m_socket);
        if (!m_outputStream)
            return false;
    }


    /*****************************************************************
     * STEP 3:
     * serverSocket.close();
     *
     *****************************************************************/
    sm_classBtServerSocket->close(this, m_btServerSocket);


    /*****************************************************************
     * STEP 4:
     * Setup remote address of server side link
     *
     ****************************************************************/
    Assert::dieIf(
            !m_dataChannel->setRemoteAddress(getRemoteAddress()),
            m_dataChannel->errorString());

    return true;
}

bool AndroidBtApi::connect(const string &serverAddress)
{
    AutoJenv env;

    /*****************************************************************
     * STEP 1:
     * BluetoothDevice device = adapter.getRemoteDevice(address);
     *
     ****************************************************************/
    LocalJobject temp(env->NewStringUTF(serverAddress.c_str()));
    GlobalJobject address = globalRefJobject(temp.data());

    Assert::dieIf(!address, "Failed to allocate String.");

    GlobalJobject device = sm_classBtAdapter->getRemoteDevice(
                                            this, m_adapter, address);

    Assert::dieIf(!device, "Failed to allocate BluetoothDevice.");


    /*****************************************************************
     * STEP 2:
     * BluetoothSocket socket =
     *              device.createRfcommSocketToServiceRecord(<UUID>);
     *
     ****************************************************************/
    GlobalJobject socket =
            sm_classBtDevice->createRfcommSocketToServiceRecord(
                                                this, device, m_uuid);
    if (!socket)
        return false;

    /*****************************************************************
     * STEP 3:
     * BluetoothAdapter.cancelDiscovery();
     ****************************************************************/
    sm_classBtAdapter->cancelDiscovery(this, m_adapter);


    /*****************************************************************
     * STEP 4:
     * socket.connect();
     *
     ****************************************************************/
    {
        Mutex::Autolock l(&m_lock);

        m_device = device;

        m_socket = socket;

        m_inputStream =
                sm_classBtSocket->getInputStream(this, m_socket);
        m_outputStream =
                sm_classBtSocket->getOutputStream(this, m_socket);
    }

    if (!sm_classBtSocket->connect(this, m_socket))
        return false;

    return true;
}

void AndroidBtApi::socketClose()
{
    Mutex::Autolock l(&m_lock);

    if (m_btServerSocket) {
        sm_classBtServerSocket->close(this, m_btServerSocket);

        m_btServerSocket.reset();
    }

    if (m_socket) {
        sm_classBtSocket->close(this, m_socket);

        m_socket.reset();
        m_device.reset();
        m_inputStream.reset();
        m_outputStream.reset();
    }
}

string AndroidBtApi::getRemoteAddress()
{
    GlobalJobject device;

    {
        Mutex::Autolock l(&m_lock);

        device = m_device;
    }

    if (!device)
        return string("");

    return sm_classBtDevice->getAddress(this, device);
}

int AndroidBtApi::read(char *buffer, int maxSize)
{
    GlobalJobject is;

    {
        Mutex::Autolock l(&m_lock);

        is = m_inputStream;
    }

    if (!is)
        return -1;

    return sm_classInputStream->read(
                                this, is, buffer, maxSize);
}

bool AndroidBtApi::write(const char *buffer, int size)
{
    GlobalJobject os;

    {
        Mutex::Autolock l(&m_lock);

        os = m_outputStream;
    }

    if (!os)
        return false;

    return sm_classOutputStream->write(
                            this, os, buffer, size);
}

bool AndroidBtApi::flush()
{
    GlobalJobject os;

    {
        Mutex::Autolock l(&m_lock);

        os = m_outputStream;
    }

    if (!os)
        return -1;

    return sm_classOutputStream->flush(this, os);
}

