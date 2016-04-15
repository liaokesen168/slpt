/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  ZhangYanMing <yamming.zhang@ingenic.com, jamincheung@126.com>
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
 */

package com.ingenic.iwds.smartlocation;

import com.ingenic.iwds.smartlocation.IRemoteStatusCallback;
import com.ingenic.iwds.smartlocation.IRemoteLocationCallback;
import com.ingenic.iwds.smartlocation.IRemoteWeatherCallback;
import com.ingenic.iwds.smartlocation.IRemoteGeoFenceCallback;
import com.ingenic.iwds.smartlocation.IRemoteProximityCallback;
import com.ingenic.iwds.smartlocation.IRemoteGpsStatusCallback;
import com.ingenic.iwds.smartlocation.IRemoteNetworkStatusCallback;
import com.ingenic.iwds.smartlocation.IRemoteProviderCallback;

interface IRemoteLocationService {

    void registerRemoteStatusListener(String uuid, IRemoteStatusCallback callback);
    void unregisterRemoteStatusListener(String uuid);

    void registerLocationListener(String uuid, String provider, IRemoteLocationCallback callback);
    void unregisterLocationListener(String uuid);

    void registerGeoFenceListener(String uuid, double latitude, double longitude, float radius, long expiration, IRemoteGeoFenceCallback callback);
    void unregisterGeoFenceListener(String uuid);

    void registerProximityListener(String uuid, double latitude, double longitude, float radius, long expiration, IRemoteProximityCallback callback);
    void unregisterProximityListener(String uuid);

    void registerGpsStatusListener(String uuid, IRemoteGpsStatusCallback callback);
    void unregisterGpsStatusListener(String uuid);

    void requestGpsStatus(String uuid, IRemoteGpsStatusCallback callback);

    void requestGpsEnable(boolean enable);

    void requestNetworkStatus(String uuid, IRemoteNetworkStatusCallback callback);

    void requestProviderStatus(String uuid, String provider, IRemoteProviderCallback callback);

    void requestProviderList(String uuid, boolean enabledOnly, IRemoteProviderCallback callback);

    void registerNetworkStatusListener(String uuid, IRemoteNetworkStatusCallback callback);
    void unregisterNetworkStatusListener(String uuid);

    void requestWeatherUpdate(int weatherType, String uuid, IRemoteWeatherCallback callback);

    void requestLastKnownLocation(String uuid, String provider, IRemoteLocationCallback callback);

}
