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

#include "./androidbtdatachannel.h"
#include "./androidbtclasses.h"

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


/*********************************************************************
 * For class BluetoothAdapter
 *
 ********************************************************************/

BluetoothAdapter::BluetoothAdapter() :
    m_classGlobalRef(),
    m_methodGetDefaultAdapter(0),
    m_methodListenUsingRfcommWithServiceRecord(0),
    m_methodGetRemoteDevice(0),
    m_methodEnable(0),
    m_methodDisable(0),
    m_methodIsEnabled(0),
    m_methodCancelDiscovery(0)
{
    Log::d(AndroidBtDataChannel::LOG_TAG,
                            "Attaching android...");

    m_classGlobalRef = findGlobalRefClass(
                    "android/bluetooth/BluetoothAdapter");
    if (!m_classGlobalRef) {
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find class: BluetoothAdapter");
        return;
    }

    /*
     * BluetoothAdapter.getDefaultAdapter
     */
    m_methodGetDefaultAdapter =
            getStaticMethodId(
                    toJclass(m_classGlobalRef),
                    "getDefaultAdapter",
                    "()Landroid/bluetooth/BluetoothAdapter;");
    if (!m_methodGetDefaultAdapter) {
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find method: BluetoothAdapter.getDefaultAdapter");
    }

    /*
     * BluetoothAdapter.listenUsingRfcommWithServiceRecord
     */
    m_methodListenUsingRfcommWithServiceRecord =
            getMethodId(
                    toJclass(m_classGlobalRef),
                    "listenUsingRfcommWithServiceRecord",
                    "(Ljava/lang/String;Ljava/util/UUID;)"
                    "Landroid/bluetooth/BluetoothServerSocket;");
    if (!m_methodListenUsingRfcommWithServiceRecord) {
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find method:"
                " BluetoothAdapter.listenUsingRfcommWithServiceRecord");
    }

    /*
     * BluetoothAdapter.getRemoteDevice
     */
    m_methodGetRemoteDevice =
            getMethodId(
                    toJclass(m_classGlobalRef),
                    "getRemoteDevice",
                    "(Ljava/lang/String;)Landroid/bluetooth/BluetoothDevice;");
    if (!m_methodGetRemoteDevice) {
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find method: BluetoothAdapter.getRemoteDevice");
    }

    /*
     * BluetoothAdapter.enable
     */
    m_methodEnable =
            getMethodId(
                    toJclass(m_classGlobalRef),
                    "enable",
                    "()Z");
    if (!m_methodEnable) {
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find method: BluetoothAdapter.enable");
    }

    /*
     * BluetoothAdapter.disable
     */
    m_methodDisable =
            getMethodId(
                    toJclass(m_classGlobalRef),
                    "disable",
                    "()Z");
    if (!m_methodDisable) {
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find method: BluetoothAdapter.disable");
    }

    /*
     * BluetoothAdapter.isEnabled
     */
    m_methodIsEnabled =
            getMethodId(
                    toJclass(m_classGlobalRef),
                    "isEnabled",
                    "()Z");
    if (!m_methodIsEnabled) {
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find method: BluetoothAdapter.isEnabled");
    }

    /*
     * BluetoothAdapter.cancelDiscovery
     */
    m_methodCancelDiscovery =
            getMethodId(
                    toJclass(m_classGlobalRef),
                    "cancelDiscovery",
                    "()Z");
    if (!m_methodCancelDiscovery) {
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find method: BluetoothAdapter.cancelDiscovery");
    }
}

bool BluetoothAdapter::enable(
        JavaApiContext *context, Iwds::GlobalJobject adapter)
{
    AutoJenv env;

    if (env->CallBooleanMethod(
                                adapter.get(),
                                m_methodEnable) == JNI_FALSE) {
        context->setErrorString("Remote exception on bluetooth stack.");

        return false;
    }

    return true;
}

bool BluetoothAdapter::disable(
        JavaApiContext *context, Iwds::GlobalJobject adapter)
{
    AutoJenv env;

    if (env->CallBooleanMethod(
                                adapter.get(),
                                m_methodDisable) == JNI_FALSE) {
        context->setErrorString("Remote exception on bluetooth stack.");

        return false;
    }

    return true;
}

bool BluetoothAdapter::isEnabled(
                JavaApiContext *context, GlobalJobject adapter) const
{
    AutoJenv env;

    if (env->CallBooleanMethod(
                                adapter.get(),
                                m_methodIsEnabled) == JNI_FALSE) {
        context->setErrorString("bluetooth was disabled.");

        return false;
    }

    return true;
}

bool BluetoothAdapter::cancelDiscovery(
                    JavaApiContext *context, GlobalJobject adapter)
{
    AutoJenv env;

    if (!env->CallBooleanMethod(
            adapter.get(),
            m_methodCancelDiscovery)) {
        context->setErrorString(
                "bluetooth was disabled"
                " or remote exception.");

        return false;
    }

    return true;
}

GlobalJobject BluetoothAdapter::getDefaultAdapter(
                                            JavaApiContext *context)
{
    AutoJenv env;

    LocalJobject obj(env->CallStaticObjectMethod(
                            toJclass(m_classGlobalRef),
                            m_methodGetDefaultAdapter));

    if (!obj) {
        context->setErrorString("system does not support bluetooth");

        return GlobalJobject();
    }

    return globalRefJobject(obj.data());
}

GlobalJobject BluetoothAdapter::listenUsingRfcommWithServiceRecord(
        JavaApiContext *context,
        GlobalJobject adapter, GlobalJobject name,
        GlobalJobject uuid)
{
    AutoJenv env;
    jobject obj = env->CallObjectMethod(
            adapter.get(),
            m_methodListenUsingRfcommWithServiceRecord,
            name.get(),
            uuid.get());
    if (checkExceptionAndDump(AndroidBtDataChannel::LOG_TAG)) {
        context->setErrorString(
                "IO exception on listenUsingRfcommWithServiceRecord");

        return GlobalJobject();
    }

    return globalRefJobject(obj);
}

GlobalJobject BluetoothAdapter::getRemoteDevice(
                        JavaApiContext *context,
                        GlobalJobject adapter, GlobalJobject address)
{
    AutoJenv env;
    LocalJobject obj(env->CallObjectMethod(
            adapter.get(), m_methodGetRemoteDevice, address.get()));

    return globalRefJobject(obj.data());
}

bool BluetoothAdapter::isAttached(JavaApiContext *context) const
{
    return m_methodGetDefaultAdapter &&
            m_methodListenUsingRfcommWithServiceRecord &&
            m_methodGetRemoteDevice &&
            m_methodEnable &&
            m_methodDisable &&
            m_methodIsEnabled &&
            m_methodCancelDiscovery;
}

/*********************************************************************
 * For class BluetoothDevice
 *
 ********************************************************************/
BluetoothDevice::BluetoothDevice() :
    m_classGlobalRef(),
    m_methodCreateRfcommSocketToServiceRecord(0),
    m_methodGetAddress()
{
    m_classGlobalRef = findGlobalRefClass(
                    "android/bluetooth/BluetoothDevice");
    if (!m_classGlobalRef) {
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find class: BluetoothDevice");
        return;
    }

    m_methodCreateRfcommSocketToServiceRecord =
            getMethodId(
                    toJclass(m_classGlobalRef),
                    "createRfcommSocketToServiceRecord",
                    "(Ljava/util/UUID;)Landroid/bluetooth/BluetoothSocket;");
    if (!m_methodCreateRfcommSocketToServiceRecord)
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find method:"
                " BluetoothDevice.createRfcommSocketToServiceRecord");

    m_methodGetAddress =
            getMethodId(
                    toJclass(m_classGlobalRef),
                    "getAddress",
                    "()Ljava/lang/String;");
    if (!m_methodGetAddress)
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find method:"
                " BluetoothDevice.getAddress");
}

string BluetoothDevice::getAddress(
                JavaApiContext *context, GlobalJobject device) const
{
    AutoJenv env;

    LocalJobject address(env->CallObjectMethod(
                                device.get(), m_methodGetAddress));

    const char *arg = env->GetStringUTFChars(
                            static_cast<jstring>(address.data()), JNI_FALSE);
    string addr(arg);
    env->ReleaseStringUTFChars(static_cast<jstring>(address.data()), arg);

    return addr;
}

GlobalJobject BluetoothDevice::createRfcommSocketToServiceRecord(
        JavaApiContext *context,
        GlobalJobject device, GlobalJobject uuid)
{
    AutoJenv env;

    jobject socket = env->CallObjectMethod(
            device.get(),
            m_methodCreateRfcommSocketToServiceRecord,
            uuid.get());
    if (checkExceptionAndDump(AndroidBtDataChannel::LOG_TAG)) {
        context->setErrorString(
                "IO exception on createRfcommSocketToServiceRecord");

        return GlobalJobject();
    }

    return globalRefJobject(socket);
}

bool BluetoothDevice::isAttached(JavaApiContext *context) const
{
    return m_methodCreateRfcommSocketToServiceRecord &&
            m_methodGetAddress;
}

/*********************************************************************
 * For class BluetoothSocket
 *
 ********************************************************************/
BluetoothSocket::BluetoothSocket() :
    m_classGlobalRef(),
    m_methodConnect(0),
    m_methodClose(0),
    m_methodGetOutputStream(0),
    m_methodGetInputStream(0),
    m_methodGetRemoteDevice(0)
{
    m_classGlobalRef = findGlobalRefClass(
                    "android/bluetooth/BluetoothSocket");
    if (!m_classGlobalRef) {
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find class: BluetoothSocket");
        return;
    }

    m_methodConnect = getMethodId(
                            toJclass(m_classGlobalRef),
                            "connect",
                            "()V");
    if (!m_methodConnect)
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find method: BluetoothSocket.connect");

    m_methodClose = getMethodId(
            toJclass(m_classGlobalRef),
            "close",
            "()V");
    if (!m_methodClose)
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find method: BluetoothSocket.close");

    m_methodGetOutputStream = getMethodId(
                                    toJclass(m_classGlobalRef),
                                    "getOutputStream",
                                    "()Ljava/io/OutputStream;");
    if (!m_methodGetOutputStream)
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find method: BluetoothSocket.getOutputStream");

    m_methodGetInputStream = getMethodId(
                                toJclass(m_classGlobalRef),
                                "getInputStream",
                                "()Ljava/io/InputStream;");
    if (!m_methodGetInputStream)
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find method: BluetoothSocket.getInputStream");

    m_methodGetRemoteDevice = getMethodId(
                                toJclass(m_classGlobalRef),
                                "getRemoteDevice",
                                "()Landroid/bluetooth/BluetoothDevice;");
    if (!m_methodGetRemoteDevice)
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find method: BluetoothSocket.getRemoteDevice");
}

bool BluetoothSocket::connect(
                        JavaApiContext *context, GlobalJobject socket)
{
    AutoJenv env;

    env->CallVoidMethod(socket.get(), m_methodConnect);
    if (checkExceptionAndDump(AndroidBtDataChannel::LOG_TAG)) {
        context->setErrorString("IO exception on connect");

        return false;
    }

    return true;
}

bool BluetoothSocket::close(
                        JavaApiContext *context, GlobalJobject socket)
{
    AutoJenv env;

    env->CallVoidMethod(socket.get(), m_methodClose);

    if (checkExceptionAndDump(AndroidBtDataChannel::LOG_TAG)) {
        context->setErrorString("IO exception on close");

        return false;
    }

    return true;
}

Iwds::GlobalJobject BluetoothSocket::getRemoteDevice(
                        JavaApiContext *context, GlobalJobject socket)
{
    AutoJenv env;

    return globalRefJobject(env->CallObjectMethod(
                            socket.get(), m_methodGetRemoteDevice));
}

Iwds::GlobalJobject BluetoothSocket::getOutputStream(
                        JavaApiContext *context, GlobalJobject socket)
{
    AutoJenv env;

    jobject obj = env->CallObjectMethod(
            socket.get(), m_methodGetOutputStream);
    if (checkExceptionAndDump(AndroidBtDataChannel::LOG_TAG)) {
        context->setErrorString("IO exception on getOutputStream");

        return GlobalJobject();
    }

    return globalRefJobject(obj);
}

Iwds::GlobalJobject BluetoothSocket::getInputStream(
                        JavaApiContext *context, GlobalJobject socket)
{
    AutoJenv env;

    jobject obj = env->CallObjectMethod(
            socket.get(), m_methodGetInputStream);
    if (checkExceptionAndDump(AndroidBtDataChannel::LOG_TAG)) {
        context->setErrorString("IO exception on getInputStream");

        return GlobalJobject();
    }

    return globalRefJobject(obj);
}

bool BluetoothSocket::isAttached(JavaApiContext *context) const
{
    return m_methodConnect &&
            m_methodClose &&
            m_methodGetOutputStream &&
            m_methodGetInputStream &&
            m_methodGetRemoteDevice;
}

/*********************************************************************
 * For class BluetoothServerSocket
 *
 ********************************************************************/
BluetoothServerSocket::BluetoothServerSocket() :
    m_classGlobalRef(),
    m_methodAccept(0),
    m_methodClose(0)
{
    m_classGlobalRef = findGlobalRefClass(
            "android/bluetooth/BluetoothServerSocket");
    if (!m_classGlobalRef) {
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find class: BluetoothServerSocket");
        return;
    }

    m_methodAccept = getMethodId(
                            toJclass(m_classGlobalRef),
                            "accept",
                            "()Landroid/bluetooth/BluetoothSocket;");
    if (!m_methodAccept)
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find method: BluetoothServerSocket.accept");

    m_methodClose = getMethodId(
                            toJclass(m_classGlobalRef),
                            "close",
                            "()V");
    if (!m_methodClose)
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find method: BluetoothServerSocket.close");
}

GlobalJobject BluetoothServerSocket::accept(
        JavaApiContext *context, GlobalJobject serverSocket)
{
    AutoJenv env;

    jobject obj = env->CallObjectMethod(
            serverSocket.get(), m_methodAccept);

    if (checkExceptionAndDump(AndroidBtDataChannel::LOG_TAG)) {
        context->setErrorString("IO exception on accept");

        return GlobalJobject();
    }

    return globalRefJobject(obj);
}

bool BluetoothServerSocket::close(
        JavaApiContext *context, GlobalJobject serverSocket)
{
    AutoJenv env;

    env->CallVoidMethod(serverSocket.get(), m_methodClose);
    if (checkExceptionAndDump(AndroidBtDataChannel::LOG_TAG)) {
        context->setErrorString("IO exception on close");

        return false;
    }

    return true;
}

bool BluetoothServerSocket::isAttached(JavaApiContext *context) const
{
    return m_classGlobalRef &&
            m_methodAccept &&
            m_methodClose;
}

/*********************************************************************
 * For class InputStream
 *
 ********************************************************************/
InputStream::InputStream(int maxPayLoadSize) :
    m_classGlobalRef(),
    m_methodRead(0),
    m_readBuffer()
{
    AutoJenv env;

    m_readBuffer = globalRefJobject(env->NewByteArray(maxPayLoadSize));

    m_classGlobalRef = findGlobalRefClass("java/io/InputStream");
    if (!m_classGlobalRef) {
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find class: InputStream");
        return;
    }

    m_methodRead = getMethodId(
                            toJclass(m_classGlobalRef),
                            "read",
                            "([BII)I");
    if (!m_methodRead)
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find method: InputStream.read");
}

int InputStream::read(
        JavaApiContext *context, GlobalJobject inputStream,
        char *buffer, unsigned int maxSize)
{
    AutoJenv env;

    int bytesRead = env->CallIntMethod(
                        inputStream.get(), m_methodRead,
                            m_readBuffer.get(), 0, maxSize);
    if (checkExceptionAndDump(AndroidBtDataChannel::LOG_TAG)) {
        context->setErrorString("IO exception on read");

        return -1;
    }

    env->GetByteArrayRegion(
            static_cast<jbyteArray>(m_readBuffer.get()),
            0, bytesRead, reinterpret_cast<jbyte *>(buffer));

    return bytesRead;
}

bool InputStream::isAttached(JavaApiContext *context) const
{
    return m_classGlobalRef &&
            m_methodRead;
}

/*********************************************************************
 * For class OutputStream
 *
 ********************************************************************/
OutputStream::OutputStream(int maxPayLoadSize) :
    m_classGlobalRef(),
    m_methodWrite(0),
    m_methodFlush(0),
    m_writeBuffer()
{
    AutoJenv env;

    m_writeBuffer = globalRefJobject(env->NewByteArray(maxPayLoadSize));

    m_classGlobalRef = findGlobalRefClass("java/io/OutputStream");
    if (!m_classGlobalRef) {
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find class: OutputStream");
        return;
    }

    m_methodWrite = getMethodId(
                                toJclass(m_classGlobalRef),
                                "write",
                                "([BII)V");
    if (!m_methodWrite)
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find method: OutputStream.write");

    m_methodFlush = getMethodId(
                                toJclass(m_classGlobalRef),
                                "flush",
                                "()V");
    if (!m_methodFlush)
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find method: OutputStream.flush");
}

bool OutputStream::write(
        JavaApiContext *context, GlobalJobject outputStream,
        const char *buffer, unsigned int size)
{
    AutoJenv env;

    env->SetByteArrayRegion(
            static_cast<jbyteArray>(m_writeBuffer.get()),
            0, size, reinterpret_cast<jbyte *>(const_cast<char *>(buffer)));

    env->CallVoidMethod(
            outputStream.get(), m_methodWrite,
            m_writeBuffer.get(), 0, size);
    if (checkExceptionAndDump(AndroidBtDataChannel::LOG_TAG)) {
        context->setErrorString("IO exception on write");

        return false;
    }

    return true;
}

bool OutputStream::flush(
            JavaApiContext *context, Iwds::GlobalJobject outputStream)
{
    AutoJenv env;

    env->CallVoidMethod(outputStream.get(), m_methodFlush);
    if (checkExceptionAndDump(AndroidBtDataChannel::LOG_TAG)) {
        context->setErrorString("IO exception on flush");

        return false;
    }

    return true;
}

bool OutputStream::isAttached(JavaApiContext *context) const
{
    return m_classGlobalRef &&
            m_methodWrite &&
            m_methodFlush;
}

/*********************************************************************
 * For class UUID
 *
 ********************************************************************/
Uuid::Uuid() :
    m_classGlobalRef(),
    m_methodFromString(0)
{
    m_classGlobalRef = findGlobalRefClass(
                    "java/util/UUID");
    if (!m_classGlobalRef) {
        Log::e(AndroidBtDataChannel::LOG_TAG, "Can not find class: UUID");

        return;
    }

    m_methodFromString = getStaticMethodId(
                                toJclass(m_classGlobalRef),
                                "fromString",
                                "(Ljava/lang/String;)Ljava/util/UUID;");
    if (!m_methodFromString)
        Log::e(AndroidBtDataChannel::LOG_TAG,
                "Can not find method: UUID.fromString");
}

bool Uuid::isAttached(JavaApiContext *context) const
{
    return m_classGlobalRef &&
            m_methodFromString;
}

GlobalJobject Uuid::fromString(JavaApiContext *context, string uuid)
{
    AutoJenv env;

    LocalJobject id(env->NewStringUTF(uuid.c_str()));

    return  globalRefJobject(env->CallStaticObjectMethod(
                                    toJclass(m_classGlobalRef),
                                    m_methodFromString,
                                    id.data()));
}

