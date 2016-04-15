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

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * 远程广播后台服务
 */
public class RemoteBroadcastService extends Service {

    private RemoteBroadcastServiceStub mService;
    private RemoteBroadcastProxy mProxy;

    @Override
    public void onCreate() {
        super.onCreate();

        mProxy = RemoteBroadcastProxy.getInstance(this);
        mService = new RemoteBroadcastServiceStub();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mService;
    }

    private class RemoteBroadcastServiceStub extends IRemoteBroadcastService.Stub {
        @Override
        public void registerRemoteReceiver(int callerId, int id, IntentFilter filter,
                String requestPermission) {
            mProxy.registerRemoteReceiver(callerId, id, filter, requestPermission);
        }

        @Override
        public void unregisterRemoteReceiver(int callerId, int receiverId) {
            mProxy.unregisterRemoteReceiver(callerId, receiverId);
        }

        @Override
        public void sendRemoteBroadcast(int callerId, Intent intent, String perm) {
            mProxy.sendRemoteBroadcast(callerId, intent, perm);
        }

        @Override
        public int registerRemoteBroadcastCallback(IRemoteBroadcastCallback callback) {
            return mProxy.registerRemoteBroadcastCallback(callback);
        }

        public void unregisterRemoteBroadcastCallback(int callerId) {
            mProxy.unregisterRemoteBroadcastCallback(callerId);
        }
    }
}