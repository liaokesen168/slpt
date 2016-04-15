/*
 * Copyright (C) 2015 Ingenic Semiconductor
 * 
 * LiJingWen(Kevin) <kevin.jwli@ingenic.com>
 * 
 * Elf/IDWS Project
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.ingenic.iwds.remotewakelock;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

interface IRemoteWakeLockCallback extends IInterface {

    String DESCRIPTOR = "com.ingenic.iwds.remotewakelock.IRemoteWakeLockCallback";

    int CODE_PERFORM_ACQUIRE_RESULT = 1;
    int CODE_PERFORM_AVAILABLE_CHANGED = 2;

    public abstract class Stub extends Binder implements IRemoteWakeLockCallback {

        public static IRemoteWakeLockCallback asInterface(IBinder obj) {
            if (obj == null) return null;

            IRemoteWakeLockCallback irwc = (IRemoteWakeLockCallback) obj
                    .queryLocalInterface(DESCRIPTOR);
            if (irwc != null) {
                return irwc;
            }

            return new RemoteWakeLockCallbackPorxy(obj);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                throws RemoteException {
            switch (code) {
            case CODE_PERFORM_ACQUIRE_RESULT: {
                data.enforceInterface(DESCRIPTOR);
                int id = data.readInt();
                int resultCode = data.readInt();
                long timeout = data.readLong();

                performAcquireResult(id, resultCode, timeout);
                return true;
            }

            case CODE_PERFORM_AVAILABLE_CHANGED: {
                data.enforceInterface(DESCRIPTOR);
                boolean isAvailable = data.readInt() != 0;

                performAvailableChanged(isAvailable);
                return true;
            }

            default:
                break;
            }
            return super.onTransact(code, data, reply, flags);
        }
    }

    public class RemoteWakeLockCallbackPorxy implements IRemoteWakeLockCallback {
        private IBinder mRemote;

        public RemoteWakeLockCallbackPorxy(IBinder remote) {
            mRemote = remote;
        }

        @Override
        public IBinder asBinder() {
            return mRemote;
        }

        @Override
        public void performAcquireResult(int id, int resultCode, long timeout) {
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(DESCRIPTOR);

            data.writeInt(id);
            data.writeInt(resultCode);
            data.writeLong(timeout);

            try {
                mRemote.transact(CODE_PERFORM_ACQUIRE_RESULT, data, null, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            data.recycle();
        }

        @Override
        public void performAvailableChanged(boolean isAvailable) {
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(isAvailable ? 1 : 0);

            try {
                mRemote.transact(CODE_PERFORM_AVAILABLE_CHANGED, data, null, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            data.recycle();
        }
    }

    void performAcquireResult(int id, int resultCode, long timeout);

    void performAvailableChanged(boolean isAvailable);
}