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

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IConnection extends IInterface {
    /** Local-side IPC implementation stub class. */
    public static abstract class Stub extends Binder implements IConnection {
        private static final String DESCRIPTOR = "com.ingenic.iwds.uniconnect.IConnection";

        private byte[] m_readBuffer = null;

        protected static final int PackageHeaderSize = 10;

        /** Construct the stub at attach it to the interface. */
        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public void allocReadBuffer(int bufferSize) {
            m_readBuffer = new byte[bufferSize];
        }

        /**
         * Cast an IBinder object into an IConnection interface, generating a
         * proxy if needed.
         */
        public static IConnection asInterface(IBinder obj) {
            if (obj == null)
                return null;

            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && (iin instanceof IConnection))
                return ((IConnection) iin);

            return new IConnection.Stub.Proxy(obj);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                throws RemoteException {
            switch (code) {
            case INTERFACE_TRANSACTION: {
                reply.writeString(DESCRIPTOR);

                return true;
            }

            case TRANSACTION_open: {
                data.enforceInterface(DESCRIPTOR);
                String _arg0;
                _arg0 = data.readString();
                int _arg1;
                _arg1 = data.readInt();
                String _arg2;
                _arg2 = data.readString();
                String _arg3;
                _arg3 = data.readString();

                long _result = open(_arg0, _arg1, _arg2, _arg3);

                reply.writeNoException();
                reply.writeLong(_result);

                return true;
            }

            case TRANSACTION_close: {
                data.enforceInterface(DESCRIPTOR);

                this.close();

                reply.writeNoException();

                return true;
            }

            case TRANSACTION_write: {
                data.enforceInterface(DESCRIPTOR);

                byte[] _arg0 = data.createByteArray();
                int _result = write(_arg0, 0, _arg0.length);

                reply.writeNoException();
                reply.writeInt(_result);

                return true;
            }

            case TRANSACTION_read: {
                data.enforceInterface(DESCRIPTOR);

                byte[] _arg0;
                int maxSize = data.readInt();
                if (maxSize < 0) {
                    _arg0 = null;
                } else {
                    _arg0 = m_readBuffer;
                }

                int _result = read(_arg0, 0, maxSize);

                reply.writeNoException();
                reply.writeInt(_result);

                if (_result >= 0)
                    reply.writeByteArray(_arg0);

                return true;
            }

            case TRANSACTION_available: {
                data.enforceInterface(DESCRIPTOR);

                int _result = available();

                reply.writeNoException();
                reply.writeInt(_result);

                return true;
            }

            case TRANSACTION_handshake: {
                data.enforceInterface(DESCRIPTOR);

                int _result = handshake();

                reply.writeNoException();
                reply.writeInt(_result);

                return true;
            }

            case TRANSACTION_getMaxPayloadSize: {
                data.enforceInterface(DESCRIPTOR);

                int _result = getMaxPayloadSize();

                reply.writeNoException();
                reply.writeInt(_result);

                return true;
            }

            } // end switch

            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements IConnection {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                mRemote = remote;
            }

            @Override
            public IBinder asBinder() {
                return mRemote;
            }

            public String getInterfaceDescriptor() {
                return DESCRIPTOR;
            }

            @Override
            public long open(String userName, int userPid, String address,
                    String uuid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                long _result;

                try {
                    _data.writeInterfaceToken(DESCRIPTOR);

                    _data.writeString(userName);
                    _data.writeInt(userPid);
                    _data.writeString(address);
                    _data.writeString(uuid);

                    mRemote.transact(Stub.TRANSACTION_open, _data, _reply, 0);

                    _reply.readException();
                    _result = _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }

                return _result;
            }

            @Override
            public void close() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);

                    mRemote.transact(Stub.TRANSACTION_close, _data, _reply, 0);

                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public int write(byte[] buffer, int offset, int maxSize)
                    throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                int _result;

                try {
                    _data.writeInterfaceToken(DESCRIPTOR);

                    _data.writeByteArray(buffer, offset, maxSize);

                    mRemote.transact(Stub.TRANSACTION_write, _data, _reply, 0);

                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }

                return _result;
            }

            @Override
            public int read(byte[] buffer, int offset, int maxSize)
                    throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                int _result;

                try {
                    _data.writeInterfaceToken(DESCRIPTOR);

                    if ((buffer == null)) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(maxSize);
                    }

                    mRemote.transact(Stub.TRANSACTION_read, _data, _reply, 0);

                    _reply.readException();
                    _result = _reply.readInt();

                    if (_result >= 0) {
                        byte[] bytesRead = _reply.createByteArray();
                        System.arraycopy(bytesRead, 0, buffer, offset, _result);
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }

                return _result;
            }

            @Override
            public int available() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                int _result;

                try {
                    _data.writeInterfaceToken(DESCRIPTOR);

                    mRemote.transact(Stub.TRANSACTION_available, _data, _reply,
                            0);

                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }

                return _result;
            }

            @Override
            public int handshake() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                int _result;

                try {
                    _data.writeInterfaceToken(DESCRIPTOR);

                    mRemote.transact(Stub.TRANSACTION_handshake, _data, _reply,
                            0);

                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }

                return _result;
            }

            @Override
            public int getMaxPayloadSize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                int _result;

                try {
                    _data.writeInterfaceToken(DESCRIPTOR);

                    mRemote.transact(Stub.TRANSACTION_getMaxPayloadSize, _data,
                            _reply, 0);

                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }

                return _result;
            }
        }

        static final int TRANSACTION_open = (IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_close = (IBinder.FIRST_CALL_TRANSACTION + 1);
        static final int TRANSACTION_write = (IBinder.FIRST_CALL_TRANSACTION + 2);
        static final int TRANSACTION_read = (IBinder.FIRST_CALL_TRANSACTION + 3);
        static final int TRANSACTION_available = (IBinder.FIRST_CALL_TRANSACTION + 4);
        static final int TRANSACTION_handshake = (IBinder.FIRST_CALL_TRANSACTION + 5);
        static final int TRANSACTION_getMaxPayloadSize = (IBinder.FIRST_CALL_TRANSACTION + 6);
    }

    public long open(String userName, int userPid, String address, String uuid)
            throws RemoteException;

    public void close() throws RemoteException;

    public int write(byte[] buffer, int offset, int maxSize)
            throws RemoteException;

    public int read(byte[] buffer, int offset, int maxSize)
            throws RemoteException;

    public int available() throws RemoteException;

    public int handshake() throws RemoteException;

    public int getMaxPayloadSize() throws RemoteException;
}
