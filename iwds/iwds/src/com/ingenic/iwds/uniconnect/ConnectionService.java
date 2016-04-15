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

package com.ingenic.iwds.uniconnect;

import java.nio.ByteBuffer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.uniconnect.link.RemoteDeviceDescriptorStorage;
import com.ingenic.iwds.utils.IwdsLog;

public class ConnectionService extends Service {
    private ConnectionServiceStub m_service = new ConnectionServiceStub();

    private class ConnectionServiceStub extends IConnectionService.Stub {
        @Override
        public IConnection createConnection(IConnectionCallBack callBack)
                throws RemoteException {
            return new ConnectionStub(callBack);
        }

        @Override
        public DeviceDescriptor[] getConnectedDeviceDescriptors()
                throws RemoteException {
            return RemoteDeviceDescriptorStorage.getInstance()
                    .getDeviceDescriptorsArray();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        IwdsLog.d(this, "onBind()");

        return m_service;
    }

    public static class ConnectionStub extends IConnection.Stub implements
            IBinder.DeathRecipient {
        private ByteBuffer m_readBuffer;
        private ByteBuffer m_writeBuffer;

        private String m_address;
        private long m_port = -1;
        private Object m_addressLock = new Object();

        private IConnectionCallBack m_callBack;

        public ConnectionStub(IConnectionCallBack callBack) {
            m_callBack = callBack;
        }

        @Override
        public void binderDied() {
            synchronized (m_addressLock) {
                if (m_port >= 0) {
                    nativeDestroyConnection(m_address, m_port);

                    m_address = null;
                    m_port = -1;
                }

                m_readBuffer = null;
                m_writeBuffer = null;

                m_addressLock = null;
            }
        }

        @Override
        public long open(String userName, int userPid, String address,
                String uuid) throws RemoteException {
            synchronized (m_addressLock) {
                m_address = address;

                m_port = nativeCreateConnectionByUuid(userName, userPid,
                        m_address, uuid.toLowerCase(), m_callBack);
                if (m_port >= 0) {
                    int payloadSize = nativeGetMaxPayloadSize(m_address, m_port);
                    if (payloadSize < 0)
                        return payloadSize;

                    allocReadBuffer(payloadSize);
                    m_readBuffer = ByteBuffer.allocateDirect(payloadSize);
                    m_writeBuffer = ByteBuffer.allocateDirect(PackageHeaderSize
                            + payloadSize);

                    m_callBack.asBinder().linkToDeath(this, 0);
                }

                return m_port;
            }
        }

        @Override
        public void close() throws RemoteException {
            synchronized (m_addressLock) {
                if (m_port >= 0) {
                    nativeDestroyConnection(m_address, m_port);
                    m_callBack.asBinder().unlinkToDeath(this, 0);

                    m_address = null;
                    m_port = -1;
                }
            }
        }

        @Override
        public int read(byte[] buffer, int offset, int maxSize)
                throws RemoteException {
            String address = null;
            long port = -1;

            synchronized (m_addressLock) {
                address = m_address;
                port = m_port;

                if (port == -1)
                    return UniconnectErrorCode.EPORTCLOSED;
            }

            synchronized (m_readBuffer) {
                int bytesRead = nativeRead(address, port, m_readBuffer, 0,
                        maxSize);
                if (bytesRead >= 0) {
                    m_readBuffer.get(buffer, offset, bytesRead);
                    m_readBuffer.clear();
                }

                return bytesRead;
            }
        }

        @Override
        public int write(byte[] buffer, int offset, int maxSize)
                throws RemoteException {
            String address = null;
            long port = -1;

            synchronized (m_addressLock) {
                address = m_address;
                port = m_port;

                if (port == -1)
                    return UniconnectErrorCode.EPORTCLOSED;
            }

            synchronized (m_writeBuffer) {
                m_writeBuffer.position(PackageHeaderSize);
                m_writeBuffer.put(buffer, offset, maxSize);

                return nativeWrite(address, port, m_writeBuffer,
                        PackageHeaderSize, maxSize);
            }
        }

        @Override
        public int available() throws RemoteException {
            String address = null;
            long port = -1;

            synchronized (m_addressLock) {
                address = m_address;
                port = m_port;

                if (port == -1)
                    return UniconnectErrorCode.EPORTCLOSED;
            }

            return nativeAvailable(address, port);
        }

        @Override
        public int handshake() throws RemoteException {
            String address = null;
            long port = -1;

            synchronized (m_addressLock) {
                address = m_address;
                port = m_port;

                if (port == -1)
                    return UniconnectErrorCode.EPORTCLOSED;
            }

            return nativeHandshake(address, port);
        }

        @Override
        public int getMaxPayloadSize() throws RemoteException {
            String address = null;
            long port = -1;

            synchronized (m_addressLock) {
                address = m_address;
                port = m_port;

                if (port == -1)
                    return UniconnectErrorCode.EPORTCLOSED;
            }

            return nativeGetMaxPayloadSize(address, port);
        }
    }

    /*
     * natives
     */
    private static native final long nativeCreateConnectionByUuid(
            String userName, int userPid, String address, String uuid,
            IConnectionCallBack callBack);

    private static native final void nativeDestroyConnection(String address,
            long port);

    private static native final int nativeRead(String address, long port,
            ByteBuffer buffer, int offset, int maxSize);

    private static native final int nativeWrite(String address, long port,
            ByteBuffer buffer, int offset, int maxSize);

    private static native final int nativeFlush(String address, long port);

    private static native final int nativeAvailable(String address, long port);

    private static native final int nativeHandshake(String address, long port);

    private static native final int nativeGetMaxPayloadSize(String address,
            long port);
}
