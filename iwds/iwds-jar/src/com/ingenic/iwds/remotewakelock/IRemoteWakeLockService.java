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

interface IRemoteWakeLockService extends IInterface {

    String DESCRIPTOR = "com.ingenic.iwds.remotewakelock.IRemoteWakeLockService";

    int CODE_REGISTER_REMOTE_WAKELOCK_CALLBACK = 1;
    int CODE_UNREGISTER_REMOTE_WAKELOCK_CALLBACK = 2;
    int CODE_NEW_REMOTE_WAKELOCK = 3;
    int CODE_DESTROY_REMOTE_WAKELOCK = 4;
    int CODE_ACQUIRE_WAKELOCK = 5;
    int CODE_RELEASE_WAKELOCK = 6;

    public abstract class Stub extends Binder implements IRemoteWakeLockService {

        public static IRemoteWakeLockService asInterface(IBinder obj) {
            if (obj == null) return null;

            IRemoteWakeLockService irws = (IRemoteWakeLockService) obj
                    .queryLocalInterface(DESCRIPTOR);
            if (irws != null) {
                return irws;
            }

            return new RemoteWakeLockProxy(obj);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                throws RemoteException {
            switch (code) {
            case CODE_REGISTER_REMOTE_WAKELOCK_CALLBACK: {
                data.enforceInterface(DESCRIPTOR);
                IBinder b = data.readStrongBinder();
                IRemoteWakeLockCallback callback = IRemoteWakeLockCallback.Stub.asInterface(b);

                int result = registerRemoteWakeLockCallback(callback);
                reply.writeNoException();
                reply.writeInt(result);

                return true;
            }

            case CODE_UNREGISTER_REMOTE_WAKELOCK_CALLBACK: {
                data.enforceInterface(DESCRIPTOR);
                int callerId = data.readInt();

                unregisterRemoteWakeLockCallback(callerId);
                return true;
            }

            case CODE_NEW_REMOTE_WAKELOCK: {
                data.enforceInterface(DESCRIPTOR);
                int callerId = data.readInt();
                int id = data.readInt();
                int levelAndFlags = data.readInt();
                String tag = data.readString();

                newRemoteWakeLock(callerId, id, levelAndFlags, tag);
                return true;
            }

            case CODE_DESTROY_REMOTE_WAKELOCK: {
                data.enforceInterface(DESCRIPTOR);
                int callerId = data.readInt();
                int id = data.readInt();

                destroyRemoteWakeLock(callerId, id);
                return true;
            }

            case CODE_ACQUIRE_WAKELOCK: {
                data.enforceInterface(DESCRIPTOR);
                int callerId = data.readInt();
                int id = data.readInt();
                long timeout = data.readLong();

                acquireWakeLock(callerId, id, timeout);
                return true;
            }

            case CODE_RELEASE_WAKELOCK: {
                data.enforceInterface(DESCRIPTOR);
                int callerId = data.readInt();
                int id = data.readInt();

                releaseWakeLock(callerId, id);
                return true;
            }

            default:
                break;
            }
            return super.onTransact(code, data, reply, flags);
        }
    }

    public class RemoteWakeLockProxy implements IRemoteWakeLockService {
        private IBinder mRemote;

        public RemoteWakeLockProxy(IBinder remote) {
            mRemote = remote;
        }

        @Override
        public IBinder asBinder() {
            return mRemote;
        }

        @Override
        public int registerRemoteWakeLockCallback(IRemoteWakeLockCallback callback) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();

            data.writeInterfaceToken(DESCRIPTOR);
            data.writeStrongBinder(callback != null ? callback.asBinder() : null);

            try {
                mRemote.transact(CODE_REGISTER_REMOTE_WAKELOCK_CALLBACK, data, reply, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            reply.readException();
            int result = reply.readInt();
            data.recycle();
            reply.recycle();

            return result;
        }

        @Override
        public void unregisterRemoteWakeLockCallback(int callerId) {
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(callerId);

            try {
                mRemote.transact(CODE_UNREGISTER_REMOTE_WAKELOCK_CALLBACK, data, null, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            data.recycle();
        }

        @Override
        public void newRemoteWakeLock(int callerId, int id, int levelAndFlags, String tag) {
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(DESCRIPTOR);

            data.writeInt(callerId);
            data.writeInt(id);
            data.writeInt(levelAndFlags);
            data.writeString(tag);

            try {
                mRemote.transact(CODE_NEW_REMOTE_WAKELOCK, data, null, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            data.recycle();
        }

        @Override
        public void destroyRemoteWakeLock(int callerId, int id) {
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(callerId);
            data.writeInt(id);

            try {
                mRemote.transact(CODE_DESTROY_REMOTE_WAKELOCK, data, null, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            data.recycle();
        }

        @Override
        public void acquireWakeLock(int callerId, int id, long timeout) {
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(DESCRIPTOR);

            data.writeInt(callerId);
            data.writeInt(id);
            data.writeLong(timeout);

            try {
                mRemote.transact(CODE_ACQUIRE_WAKELOCK, data, null, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            data.recycle();
        }

        @Override
        public void releaseWakeLock(int callerId, int id) {
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(DESCRIPTOR);

            data.writeInt(callerId);
            data.writeInt(id);

            try {
                mRemote.transact(CODE_RELEASE_WAKELOCK, data, null, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            data.recycle();
        }
    }

    int registerRemoteWakeLockCallback(IRemoteWakeLockCallback callback);

    void unregisterRemoteWakeLockCallback(int callerId);

    void newRemoteWakeLock(int callerId, int id, int levelAndFlags, String tag);

    void destroyRemoteWakeLock(int callerId, int id);

    void acquireWakeLock(int callerId, int id, long timeout);

    void releaseWakeLock(int callerId, int id);
}