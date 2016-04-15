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

import java.util.ArrayList;

import com.ingenic.iwds.datatransactor.DataTransactor;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusLineResult;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusStationResult;
import com.ingenic.iwds.smartlocation.search.district.RemoteDistrictResult;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteGeocodeResult;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteRegeocodeResult;
import com.ingenic.iwds.smartlocation.search.help.RemoteTip;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiItemDetail;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiResult;
import com.ingenic.iwds.smartlocation.search.route.RemoteBusRouteResult;
import com.ingenic.iwds.smartlocation.search.route.RemoteDriveRouteResult;
import com.ingenic.iwds.smartlocation.search.route.RemoteWalkRouteResult;

import android.os.Parcel;
import android.os.Parcelable;

public class RemoteSearchResponse implements Parcelable {
    public final static int TYPE_SEARCH_SERVICE_STATUS = 0;
    public final static int TYPE_GEOCODE_SEARCHED = 1;
    public final static int TYPE_REGEOCODE_SEARCHED = 2;
    public final static int TYPE_DISTRICT_SEARCHED = 3;
    public final static int TYPE_POI_SEARCHED = 4;
    public final static int TYPE_POI_DETAIL_SEARCHED = 5;
    public final static int TYPE_INPUT_TIPS_SEARCHED = 6;
    public final static int TYPE_BUS_LINE_SEARCHED = 7;
    public final static int TYPE_BUS_STATION_SEARCHED = 8;
    public final static int TYPE_BUS_ROUTE_SEARCHED = 9;
    public final static int TYPE_DRIVE_ROUTE_SEARCHED = 10;
    public final static int TYPE_WALK_ROUTE_SEARCHED = 11;

    public int type;
    public int serviceConnected;
    public String uuid;
    public int errorCode;

    public RemoteGeocodeResult geocodeResult;
    public RemoteRegeocodeResult regeocodeResult;
    public RemoteDistrictResult districtResult;
    public RemotePoiResult poiResult;
    public RemotePoiItemDetail poiItemDetail;
    public ArrayList<RemoteTip> tipList;
    public RemoteBusLineResult busLineResult;
    public RemoteBusStationResult busStationResult;
    public RemoteBusRouteResult busRouteResult;
    public RemoteDriveRouteResult driveRouteResult;
    public RemoteWalkRouteResult walkRouteResult;

    private DataTransactor sender;

    public RemoteSearchResponse() {

    }

    public static RemoteSearchResponse obtain() {
        return new RemoteSearchResponse();
    }

    public static RemoteSearchResponse obtain(DataTransactor sender) {
        RemoteSearchResponse response = obtain();

        response.sender = sender;

        return response;
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
        dest.writeInt(this.serviceConnected);
        dest.writeString(this.uuid);
        dest.writeInt(this.errorCode);

        if (this.geocodeResult != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.geocodeResult, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.regeocodeResult != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.regeocodeResult, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.districtResult != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.districtResult, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.poiResult != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.poiResult, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.poiItemDetail != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.poiItemDetail, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.tipList != null) {
            dest.writeInt(1);
            dest.writeList(tipList);
        } else {
            dest.writeInt(0);
        }

        if (this.busLineResult != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.busLineResult, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.busStationResult != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.busStationResult, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.busRouteResult != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.busRouteResult, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.driveRouteResult != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.driveRouteResult, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.walkRouteResult != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.walkRouteResult, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteSearchResponse> CREATOR = new Creator<RemoteSearchResponse>() {

        @Override
        public RemoteSearchResponse createFromParcel(Parcel source) {
            RemoteSearchResponse response = new RemoteSearchResponse();

            response.type = source.readInt();
            response.serviceConnected = source.readInt();
            response.uuid = source.readString();
            response.errorCode = source.readInt();

            if (source.readInt() != 0)
                response.geocodeResult = source
                        .readParcelable(RemoteGeocodeResult.class
                                .getClassLoader());

            if (source.readInt() != 0)
                response.regeocodeResult = source
                        .readParcelable(RemoteRegeocodeResult.class
                                .getClassLoader());

            if (source.readInt() != 0)
                response.districtResult = source
                        .readParcelable(RemoteDistrictResult.class
                                .getClassLoader());

            if (source.readInt() != 0)
                response.poiResult = source
                        .readParcelable(RemotePoiResult.class.getClassLoader());

            if (source.readInt() != 0)
                response.poiItemDetail = source
                        .readParcelable(RemotePoiItemDetail.class
                                .getClassLoader());

            if (source.readInt() != 0)
                response.tipList = source.readArrayList(RemoteTip.class
                        .getClassLoader());

            if (source.readInt() != 0)
                response.busLineResult = source
                        .readParcelable(RemoteBusLineResult.class
                                .getClassLoader());

            if (source.readInt() != 0)
                response.busStationResult = source
                        .readParcelable(RemoteBusStationResult.class
                                .getClassLoader());

            if (source.readInt() != 0)
                response.busRouteResult = source
                        .readParcelable(RemoteBusRouteResult.class
                                .getClassLoader());

            if (source.readInt() != 0)
                response.driveRouteResult = source
                        .readParcelable(RemoteDriveRouteResult.class
                                .getClassLoader());

            if (source.readInt() != 0)
                response.walkRouteResult = source
                        .readParcelable(RemoteWalkRouteResult.class
                                .getClassLoader());

            return response;
        }

        @Override
        public RemoteSearchResponse[] newArray(int size) {
            return new RemoteSearchResponse[size];
        }
    };

}
