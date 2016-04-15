/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  Huanglihong(Regen) <lihong.huang@ingenic.com, peterlihong@qq.com>
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

package com.ingenic.iwds.devicemanager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;

import com.ingenic.iwds.smartsense.SensorService;
import com.ingenic.iwds.utils.IwdsLog;

public class DeviceManagerService extends Service {
    private static Context m_context;

    private static PackageManager mPM;
    private DeviceManagerServiceStub m_service = new DeviceManagerServiceStub();
    private final static String TAG = "DeviceManagerService";
    private final static String HAND_MODE = "isOnRightHand";

    @Override
    public void onCreate() {
        IwdsLog.d(this, "onCreate");
        super.onCreate();

        m_context = getBaseContext();
        boolean isOnRightHand = getBooleanPref(m_context, HAND_MODE, false);
        SensorService.setWearOnRightHand(isOnRightHand);
    }

    @Override
    public IBinder onBind(Intent intent) {
        IwdsLog.d(this, "onBind");

        return m_service;
    }

    private static class DeviceManagerServiceStub extends
            IDeviceManagerService.Stub {

        @Override
        public synchronized boolean isWearOnRightHand() throws RemoteException {
            boolean isOnRightHand = getBooleanPref(m_context, HAND_MODE, false);

            return isOnRightHand;
        }

        @Override
        public synchronized boolean setWearOnRightHand(boolean isOnRightHand)
                throws RemoteException {
            setBooleanPref(m_context, HAND_MODE, isOnRightHand);

            boolean isSuceeded = SensorService.setWearOnRightHand(isOnRightHand);

            if (isSuceeded) {
                String action = isOnRightHand ? DeviceManagerServiceManager.ACTION_WEAR_ON_RIGHT_HAND
                        : DeviceManagerServiceManager.ACTION_WEAR_ON_LEFT_HAND;
                Intent it = new Intent(action);
                m_context.sendBroadcast(it);
            }

            return isSuceeded;
        }

        @Override
        public void deletePackage(String packageName) throws RemoteException {
            IwdsLog.d(this, " on request to delete " + packageName);

            if(null == mPM){
                mPM = m_context.getPackageManager();
            }

            PackageDeleteObserver observer = new PackageDeleteObserver();

            int deleteFlags = PackageManager.DELETE_ALL_USERS;
            mPM.deletePackage(packageName, observer, deleteFlags);
        }

        private class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
            @Override
            public void packageDeleted(String packageName, int returnCode) {
                IwdsLog.d(this, " package: " + packageName
                        + " deletion return code = " + returnCode);

                Intent it = new Intent(DeviceManagerServiceManager.ACTION_DELETE_PACKAGE);
                it.putExtra(DeviceManagerServiceManager.EXTRA_DELETE_PACKAGE_NAME, packageName);
                it.putExtra(DeviceManagerServiceManager.EXTRA_DELETE_RETURN_CODE, returnCode);
                m_context.sendBroadcast(it);
            }
        }
    }


    private static boolean getBooleanPref(Context context, String name,
            boolean def) {
        SharedPreferences prefs = context.getSharedPreferences(TAG,
                Context.MODE_PRIVATE);
        boolean ret = prefs.getBoolean(name, def);

        return ret;
    }

    private static void setBooleanPref(Context context, String name,
            boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(TAG,
                Context.MODE_PRIVATE);
        Editor ed = prefs.edit();
        ed.putBoolean(name, value);
        ed.commit();
    }
}
