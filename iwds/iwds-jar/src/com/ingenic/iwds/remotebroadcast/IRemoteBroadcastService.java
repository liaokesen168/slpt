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
package com.ingenic.iwds.remotebroadcast;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

interface IRemoteBroadcastService extends IInterface {
    int CODE_REGISTER_REMOTE_BROADCAST_CALLBACK = 1;
    int CODE_UNREGISTER_REMOTE_BROADCAST_CALLBACK = 2;
    int CODE_REGISTER_REMOTE_RECEIVER = 3;
    int CODE_UNREGISTER_REMOTE_RECEIVER = 4;
    int CODE_SEND_REMOTE_BROADCAST = 5;

    String DESCRIPTOR = "com.ingenic.iwds.remotebroadcast.IRemoteBroadcastService";

    abstract class Stub extends Binder implements IRemoteBroadcastService {
        public static IRemoteBroadcastService asInterface(IBinder obj) {
            if (obj == null) return null;

            IRemoteBroadcastService irbs = (IRemoteBroadcastService) obj
                    .queryLocalInterface(DESCRIPTOR);
            if (irbs != null) return irbs;

            return new RemoteBroadcastServiceProxy(obj);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                throws RemoteException {
            switch (code) {
            case CODE_REGISTER_REMOTE_BROADCAST_CALLBACK: {
                data.enforceInterface(DESCRIPTOR);
                IBinder b = data.readStrongBinder();
                IRemoteBroadcastCallback callback = IRemoteBroadcastCallback.Stub.asInterface(b);

                int result = registerRemoteBroadcastCallback(callback);
                reply.writeNoException();
                reply.writeInt(result);
                return true;
            }

            case CODE_UNREGISTER_REMOTE_BROADCAST_CALLBACK: {
                data.enforceInterface(DESCRIPTOR);
                int callerId = data.readInt();
                unregisterRemoteBroadcastCallback(callerId);
                return true;
            }

            case CODE_REGISTER_REMOTE_RECEIVER: {
                data.enforceInterface(DESCRIPTOR);
                int callerId = data.readInt();
                int id = data.readInt();

                IntentFilter filter = null;
                if (data.readInt() != 0) {
                    filter = IntentFilter.CREATOR.createFromParcel(data);
                }

                String permission = data.readString();
                registerRemoteReceiver(callerId, id, filter, permission);
                return true;
            }

            case CODE_UNREGISTER_REMOTE_RECEIVER: {
                data.enforceInterface(DESCRIPTOR);
                int callerId = data.readInt();
                int id = data.readInt();

                unregisterRemoteReceiver(callerId, id);
                return true;
            }

            case CODE_SEND_REMOTE_BROADCAST: {
                data.enforceInterface(DESCRIPTOR);
                int id = data.readInt();

                Intent intent = null;
                if (data.readInt() != 0) {
                    intent = Intent.CREATOR.createFromParcel(data);
                }

                String perm = data.readString();
                sendRemoteBroadcast(id, intent, perm);
                return true;
            }

            default:
                break;
            }

            return super.onTransact(code, data, reply, flags);
        }
    }

    class RemoteBroadcastServiceProxy implements IRemoteBroadcastService {
        private IBinder mRemote;

        public RemoteBroadcastServiceProxy(IBinder remote) {
            mRemote = remote;
        }

        @Override
        public IBinder asBinder() {
            return mRemote;
        }

        @Override
        public int registerRemoteBroadcastCallback(IRemoteBroadcastCallback callback) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeStrongBinder(callback != null ? callback.asBinder() : null);

            try {
                mRemote.transact(CODE_REGISTER_REMOTE_BROADCAST_CALLBACK, data, reply, 0);
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
        public void unregisterRemoteBroadcastCallback(int callerId) {
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(callerId);

            try {
                mRemote.transact(CODE_UNREGISTER_REMOTE_BROADCAST_CALLBACK, data, null, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            data.recycle();
        }

        @Override
        public void registerRemoteReceiver(int callerId, int id, IntentFilter filter,
                String requestPermission) {
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(callerId);
            data.writeInt(id);

            if (filter != null) {
                data.writeInt(1);
                filter.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }

            data.writeString(requestPermission);

            try {
                mRemote.transact(CODE_REGISTER_REMOTE_RECEIVER, data, null, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            data.recycle();
        }

        @Override
        public void unregisterRemoteReceiver(int callerId, int id) {
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(callerId);
            data.writeInt(id);

            try {
                mRemote.transact(CODE_UNREGISTER_REMOTE_RECEIVER, data, null, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            data.recycle();
        }

        @Override
        public void sendRemoteBroadcast(int callerId, Intent intent, String perm) {
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(callerId);

            if (intent != null) {
                data.writeInt(1);
                intent.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }

            data.writeString(perm);
            try {
                mRemote.transact(CODE_SEND_REMOTE_BROADCAST, data, null, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            data.recycle();
        }
    }

    void registerRemoteReceiver(int callerId, int id, IntentFilter filter, String requestPermission);

    void unregisterRemoteReceiver(int callerId, int id);

    void sendRemoteBroadcast(int callerId, Intent intent, String perm);

    int registerRemoteBroadcastCallback(IRemoteBroadcastCallback callback);

    void unregisterRemoteBroadcastCallback(int callerId);
}