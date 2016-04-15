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
 
package com.ingenic.iwds.smartlocation.search;

import com.ingenic.iwds.smartlocation.search.geocoder.RemoteGeocodeQuery;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteRegeocodeQuery;
import com.ingenic.iwds.smartlocation.search.district.RemoteDistrictQuery;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiQuery;
import com.ingenic.iwds.smartlocation.search.help.RemoteInputQuery;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusLineQuery;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusStationQuery;
import com.ingenic.iwds.smartlocation.search.route.RemoteBusRouteQuery;
import com.ingenic.iwds.smartlocation.search.route.RemoteDriveRouteQuery;
import com.ingenic.iwds.smartlocation.search.route.RemoteWalkRouteQuery;
import com.ingenic.iwds.smartlocation.search.IRemoteStatusCallback;
import com.ingenic.iwds.smartlocation.search.geocoder.IRemoteGeocodeSearchCallback;
import com.ingenic.iwds.smartlocation.search.geocoder.IRemoteRegeocodeSearchCallback;
import com.ingenic.iwds.smartlocation.search.district.IRemoteDistrictSearchCallback;
import com.ingenic.iwds.smartlocation.search.poisearch.IRemotePoiSearchCallback;
import com.ingenic.iwds.smartlocation.search.poisearch.IRemotePoiDetailSearchCallback;
import com.ingenic.iwds.smartlocation.search.help.IRemoteInputtipsCallback;
import com.ingenic.iwds.smartlocation.search.busline.IRemoteBusLineSearchCallback;
import com.ingenic.iwds.smartlocation.search.busline.IRemoteBusStationSearchCallback;
import com.ingenic.iwds.smartlocation.search.route.IRemoteWalkRouteSearchCallback;
import com.ingenic.iwds.smartlocation.search.route.IRemoteBusRouteSearchCallback;
import com.ingenic.iwds.smartlocation.search.route.IRemoteDriveRouteSearchCallback;


interface IRemoteSearchService {
    void registerRemoteStatusListener(String uuid, IRemoteStatusCallback callback);
    void unregisterRemoteStatusListener(String uuid);

    void requestGeocodeSearch(IRemoteGeocodeSearchCallback callback, String uuid, in RemoteGeocodeQuery query);
    void requestRegeocodeSearch(IRemoteRegeocodeSearchCallback callback, String uuid, in RemoteRegeocodeQuery query);

    void requestDistrictSearch(IRemoteDistrictSearchCallback callback, String uuid, in RemoteDistrictQuery query);

    void requestPoiSearch(IRemotePoiSearchCallback callback, String uuid, in RemotePoiQuery query);
    void requestPoiDetailSearch(IRemotePoiDetailSearchCallback callback, String uuid, String poiId);

    void requestInputtips(IRemoteInputtipsCallback callback, String uuid, in RemoteInputQuery query);

    void requestBusLineSearch(IRemoteBusLineSearchCallback callback, String uuid, in RemoteBusLineQuery query);

    void requestBusStationSearch(IRemoteBusStationSearchCallback callback, String uuid, in RemoteBusStationQuery query);

    void requestBusRouteSearch(IRemoteBusRouteSearchCallback callback, String uuid, in RemoteBusRouteQuery query);
    void requestDriveRouteSearch(IRemoteDriveRouteSearchCallback callback, String uuid, in RemoteDriveRouteQuery query);
    void requestWalkRouteSearch(IRemoteWalkRouteSearchCallback callback, String uuid, in RemoteWalkRouteQuery query);
}