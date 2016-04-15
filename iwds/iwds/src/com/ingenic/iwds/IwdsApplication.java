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

package com.ingenic.iwds;

import android.app.Application;
import android.content.Intent;

import com.ingenic.iwds.cloud.CloudServiceProxy;
import com.ingenic.iwds.remotebroadcast.RemoteBroadcastProxy;
import com.ingenic.iwds.remotedevice.DeviceManagerProxy;
import com.ingenic.iwds.remotewakelock.RemoteWakeLockProxy;
import com.ingenic.iwds.smartlocation.LocationServiceProxy;
import com.ingenic.iwds.smartlocation.search.SearchServiceProxy;
import com.ingenic.iwds.smartsense.SensorServiceProxy;
import com.ingenic.iwds.smartspeech.SpeechServiceProxy;

public class IwdsApplication extends Application {
    private DeviceDescriptor m_localDevDesc = null;

    final protected boolean initialize(int deviceClass, int deviceSubClass) {
        m_localDevDesc = new DeviceDescriptor("any", "any", deviceClass,
                deviceSubClass);

        IwdsInitializer.getInstance().initialize(m_localDevDesc);

        Intent it = new Intent("com.ingenic.iwds.IwdsService");
        it.setClass(this, IwdsService.class);

        startService(it);

        if (m_localDevDesc.devClass == DeviceDescriptor.DEVICE_CLASS_MOBILE) {
            LocationServiceProxy locationProxy = LocationServiceProxy
                    .getInstance();
            locationProxy.initialize(getApplicationContext());
            locationProxy.start();

            SearchServiceProxy searchProxy = SearchServiceProxy.getInstance();
            searchProxy.initialize(getApplicationContext());
            searchProxy.start();

            CloudServiceProxy cloudProxy = CloudServiceProxy.getInstance();
            cloudProxy.initialize(getApplicationContext());
            cloudProxy.start();

            SpeechServiceProxy speechProxy = SpeechServiceProxy.getInstance();
            speechProxy.initialize(getApplicationContext());
            speechProxy.start();

        } else {
            SensorServiceProxy sensorProxy = SensorServiceProxy.getInstance();
            sensorProxy.initialize(getApplicationContext());
            sensorProxy.start();

            DeviceManagerProxy deviceProxy = DeviceManagerProxy.getInstance();
            deviceProxy.initialize(getApplicationContext());
            deviceProxy.start();
        }

        RemoteBroadcastProxy broadcastProxy = RemoteBroadcastProxy.getInstance(this);
        broadcastProxy.startTransaction();

        RemoteWakeLockProxy wakeLockProxy = RemoteWakeLockProxy.getInstance(this);
        wakeLockProxy.startTransaction();

        return true;
    }

    public DeviceDescriptor getLocalDeviceDescriptor() {
        return m_localDevDesc;
    }
}
