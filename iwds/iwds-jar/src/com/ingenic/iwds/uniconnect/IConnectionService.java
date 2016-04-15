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

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Parcelable;

import com.ingenic.iwds.DeviceDescriptor;

public interface IConnectionService extends IInterface {
    /** Local-side IPC implementation stub class. */
    public static abstract class Stub extends android.os.Binder implements
            IConnectionService {
        private static final java.lang.String DESCRIPTOR = "com.ingenic.iwds.uniconnect.IConnectionService";

        /** Construct the stub at attach it to the interface. */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an IConnectionService interface,
         * generating a proxy if needed.
         */
        public static IConnectionService asInterface(IBinder obj) {
            if (obj == null)
                return null;

            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && (iin instanceof IConnectionService))
                return (IConnectionService) iin;

            return new IConnectionService.Stub.Proxy(obj);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                throws android.os.RemoteException {
            switch (code) {
            case INTERFACE_TRANSACTION: {
                reply.writeString(DESCRIPTOR);

                return true;
            }

            case TRANSACTION_createConnection: {
                data.enforceInterface(DESCRIPTOR);

                IConnectionCallBack _arg0;
                _arg0 = IConnectionCallBack.Stub.asInterface(data
                        .readStrongBinder());

                IConnection _result = createConnection(_arg0);

                reply.writeNoException();
                reply.writeStrongBinder(_result != null ? _result.asBinder()
                        : null);

                return true;
            }

            case TRANSACTION_getConnectedDeviceDescriptors: {
                data.enforceInterface(DESCRIPTOR);

                DeviceDescriptor[] _result = getConnectedDeviceDescriptors();

                reply.writeNoException();
                reply.writeTypedArray(_result,
                        Parcelable.PARCELABLE_WRITE_RETURN_VALUE);

                return true;
            }

            } // end swtich

            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements IConnectionService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                mRemote = remote;
            }

            @Override
            public IBinder asBinder() {
                return mRemote;
            }

            public java.lang.String getInterfaceDescriptor() {
                return DESCRIPTOR;
            }

            @Override
            public IConnection createConnection(IConnectionCallBack callback)
                    throws android.os.RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                IConnection _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);

                    _data.writeStrongBinder(callback != null ? callback
                            .asBinder() : null);

                    mRemote.transact(Stub.TRANSACTION_createConnection, _data,
                            _reply, 0);

                    _reply.readException();
                    _result = IConnection.Stub.asInterface(_reply
                            .readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public DeviceDescriptor[] getConnectedDeviceDescriptors()
                    throws android.os.RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                DeviceDescriptor[] _result;

                try {
                    _data.writeInterfaceToken(DESCRIPTOR);

                    mRemote.transact(
                            Stub.TRANSACTION_getConnectedDeviceDescriptors,
                            _data, _reply, 0);

                    _reply.readException();
                    _result = _reply.createTypedArray(DeviceDescriptor.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }
        }

        static final int TRANSACTION_createConnection = (IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_getConnectedDeviceDescriptors = (IBinder.FIRST_CALL_TRANSACTION + 1);
    }

    public IConnection createConnection(IConnectionCallBack callback)
            throws android.os.RemoteException;

    public DeviceDescriptor[] getConnectedDeviceDescriptors()
            throws android.os.RemoteException;
}
