/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  ZhangYanMing <yanming.zhang@ingenic.com, jamincheung@126.com>
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

package com.ingenic.iwds.smartlocation.search;

import com.ingenic.iwds.datatransactor.DataTransactor;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusLineQuery;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusStationQuery;
import com.ingenic.iwds.smartlocation.search.district.RemoteDistrictQuery;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteGeocodeQuery;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteRegeocodeQuery;
import com.ingenic.iwds.smartlocation.search.help.RemoteInputQuery;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiQuery;
import com.ingenic.iwds.smartlocation.search.route.RemoteBusRouteQuery;
import com.ingenic.iwds.smartlocation.search.route.RemoteDriveRouteQuery;
import com.ingenic.iwds.smartlocation.search.route.RemoteWalkRouteQuery;

import android.os.Parcel;
import android.os.Parcelable;

public class RemoteSearchRequest implements Parcelable {
    public final static int TYPE_GEOCODE_SEARCH = 0;
    public final static int TYPE_REGEOCODE_SEARCH = 1;
    public final static int TYPE_DISTRICT_SEARCH = 2;
    public final static int TYPE_POI_SEARCH = 3;
    public final static int TYPE_POI_DETAIL_SEARCH = 4;
    public final static int TYPE_INPUT_TIPS_SEARCH = 5;
    public final static int TYPE_BUS_LINE_SEARCH = 6;
    public final static int TYPE_BUS_STATION_SEARCH = 7;
    public final static int TYPE_BUS_ROUTE_SEARCH = 9;
    public final static int TYPE_DRIVE_ROUTE_SEARCH = 10;
    public final static int TYPE_WALK_ROUTE_SEARCH = 11;

    public int type;
    public String uuid;

    public RemoteGeocodeQuery geocodeQuery;
    public RemoteRegeocodeQuery regeocodeQuery;
    public RemoteDistrictQuery districtQuery;
    public RemotePoiQuery poiQuery;
    public String poiId;
    public RemoteInputQuery inputQuery;
    public RemoteBusLineQuery busLineQuery;
    public RemoteBusStationQuery busStationQuery;
    public RemoteBusRouteQuery busRouteQuery;
    public RemoteDriveRouteQuery driveRouteQuery;
    public RemoteWalkRouteQuery walkRouteQuery;

    private DataTransactor sender;

    public RemoteSearchRequest() {

    }

    public static RemoteSearchRequest obtain() {
        return new RemoteSearchRequest();
    }

    public static RemoteSearchRequest obtain(DataTransactor sender) {
        RemoteSearchRequest request = obtain();

        request.sender = sender;

        return request;
    }

    public void sendToRemote() {
        this.sender.send(this);
    }

    public void setSender(DataTransactor sender) {
        this.sender = sender;
    }

    public DataTransactor getSender() {
        return this.sender;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeString(this.uuid);

        if (this.geocodeQuery != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.geocodeQuery, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.regeocodeQuery != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.regeocodeQuery, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.districtQuery != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.districtQuery, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.poiQuery != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.poiQuery, flags);
        } else {
            dest.writeInt(0);
        }

        dest.writeString(this.poiId);

        if (this.inputQuery != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.inputQuery, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.busLineQuery != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.busLineQuery, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.busStationQuery != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.busStationQuery, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.busRouteQuery != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.busRouteQuery, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.driveRouteQuery != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.driveRouteQuery, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.walkRouteQuery != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.walkRouteQuery, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteSearchRequest> CREATOR = new Creator<RemoteSearchRequest>() {

        @Override
        public RemoteSearchRequest createFromParcel(Parcel source) {
            RemoteSearchRequest request = new RemoteSearchRequest();

            request.type = source.readInt();
            request.uuid = source.readString();

            if (source.readInt() != 0)
                request.geocodeQuery = source
                        .readParcelable(RemoteGeocodeQuery.class
                                .getClassLoader());

            if (source.readInt() != 0)
                request.regeocodeQuery = source
                        .readParcelable(RemoteRegeocodeQuery.class
                                .getClassLoader());

            if (source.readInt() != 0)
                request.districtQuery = source
                        .readParcelable(RemoteDistrictQuery.class
                                .getClassLoader());

            if (source.readInt() != 0)
                request.poiQuery = source.readParcelable(RemotePoiQuery.class
                        .getClassLoader());

            request.poiId = source.readString();

            if (source.readInt() != 0)
                request.inputQuery = source
                        .readParcelable(RemoteInputQuery.class.getClassLoader());

            if (source.readInt() != 0)
                request.busLineQuery = source
                        .readParcelable(RemoteBusLineQuery.class
                                .getClassLoader());

            if (source.readInt() != 0)
                request.busStationQuery = source
                        .readParcelable(RemoteBusStationQuery.class
                                .getClassLoader());

            if (source.readInt() != 0)
                request.busRouteQuery = source
                        .readParcelable(RemoteBusRouteQuery.class
                                .getClassLoader());

            if (source.readInt() != 0)
                request.driveRouteQuery = source
                        .readParcelable(RemoteDriveRouteQuery.class
                                .getClassLoader());

            if (source.readInt() != 0)
                request.walkRouteQuery = source
                        .readParcelable(RemoteWalkRouteQuery.class
                                .getClassLoader());

            return request;
        }

        @Override
        public RemoteSearchRequest[] newArray(int size) {
            return new RemoteSearchRequest[size];
        }
    };

}
