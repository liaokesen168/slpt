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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class RemoteWakeLockService extends Service {
    private RemoteWakeLockProxy mProxy;
    private RemoteWakeLockStub mService;

    @Override
    public void onCreate() {
        super.onCreate();
        mProxy = RemoteWakeLockProxy.getInstance(this);
        mService = new RemoteWakeLockStub();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mService;
    }

    private class RemoteWakeLockStub extends IRemoteWakeLockService.Stub {

        @Override
        public int registerRemoteWakeLockCallback(IRemoteWakeLockCallback callback) {
            return mProxy.registerRemoteWakeLockCallback(callback);
        }

        @Override
        public void unregisterRemoteWakeLockCallback(int callerId) {
            mProxy.unregisterRemoteWakeLockCallback(callerId);
        }

        @Override
        public void newRemoteWakeLock(int callerId, int id, int levelAndFlags, String tag) {
            mProxy.newRemoteWakeLock(callerId, id, levelAndFlags, tag);
        }

        @Override
        public void destroyRemoteWakeLock(int callerId, int id) {
            mProxy.destroyRemoteWakeLock(callerId, id);
        }

        @Override
        public void acquireWakeLock(int callerId, int id, long timeout) {
            mProxy.acquireWakeLock(callerId, id, timeout);
        }

        @Override
        public void releaseWakeLock(int callerId, int id) {
            mProxy.releaseWakeLock(callerId, id);
        }
    }
}