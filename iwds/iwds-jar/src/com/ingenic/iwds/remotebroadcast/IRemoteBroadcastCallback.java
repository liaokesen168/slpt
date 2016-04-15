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
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

interface IRemoteBroadcastCallback extends IInterface {

    String DESCRIPTOR = "com.ingenic.iwds.remotebroadcast.IRemoteBroadcastCallback";

    int CODE_SEND_RESULT = 1;
    int CODE_PERFORM_RECEIVE = 2;

    abstract class Stub extends Binder implements IRemoteBroadcastCallback {
        public static IRemoteBroadcastCallback asInterface(IBinder obj) {
            if (obj == null) return null;

            IRemoteBroadcastCallback irbc = (IRemoteBroadcastCallback) obj
                    .queryLocalInterface(DESCRIPTOR);
            if (irbc != null) return irbc;

            return new RemoteBroadcastCallbackProxy(obj);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                throws RemoteException {
            switch (code) {
            case CODE_SEND_RESULT: {
                data.enforceInterface(DESCRIPTOR);

                Intent intent = null;
                if (data.readInt() != 0) {
                    intent = Intent.CREATOR.createFromParcel(data);
                }

                String perm = data.readString();
                int resultCode = data.readInt();
                performSendResult(intent, perm, resultCode);
                return true;
            }

            case CODE_PERFORM_RECEIVE: {
                data.enforceInterface(DESCRIPTOR);
                int id = data.readInt();

                Intent intent = null;
                if (data.readInt() != 0) {
                    intent = Intent.CREATOR.createFromParcel(data);
                }

                performReceive(id, intent);
                return true;
            }

            default:
                break;
            }

            return super.onTransact(code, data, reply, flags);
        }
    }

    class RemoteBroadcastCallbackProxy implements IRemoteBroadcastCallback {
        private IBinder mRemote;

        public RemoteBroadcastCallbackProxy(IBinder remote) {
            mRemote = remote;
        }

        @Override
        public IBinder asBinder() {
            return mRemote;
        }

        @Override
        public void performSendResult(Intent intent, String perm, int resultCode) {
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(DESCRIPTOR);

            if (intent != null) {
                data.writeInt(1);
                intent.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }

            data.writeString(perm);
            data.writeInt(resultCode);
            try {
                mRemote.transact(CODE_SEND_RESULT, data, null, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            data.recycle();
        }

        @Override
        public void performReceive(int id, Intent intent) {
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(id);

            if (intent != null) {
                data.writeInt(1);
                intent.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }

            try {
                mRemote.transact(CODE_PERFORM_RECEIVE, data, null, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            data.recycle();
        }

    }

    void performSendResult(Intent intent, String perm, int resultCode);

    void performReceive(int id, Intent intent);
}