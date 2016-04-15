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

package com.ingenic.iwds.smartlocation;

import java.util.ArrayList;

import com.ingenic.iwds.datatransactor.DataTransactor;

import android.os.Parcel;
import android.os.Parcelable;

public class RemoteLocationResponse implements Parcelable {

    public final static int TYPE_LOCATION_CHANGED = 0;
    public final static int TYPE_WEATHER_LIVE = 1;
    public final static int TYPE_WEATHER_FORECAST = 2;
    public final static int TYPE_GEOFENCE_ALERT = 3;
    public final static int TYPE_LAST_KNOWN_LOCATION = 4;
    public final static int TYPE_NETWORK_STATUS_CHANGED = 5;
    public final static int TYPE_GPS_STATUS_CHANGED = 6;
    public final static int TYPE_LOCATION_SERVICE_STATUS = 7;
    public final static int TYPE_GPS_STATUS = 8;
    public final static int TYPE_NETWORK_STATUS = 9;
    public final static int TYPE_PROVIDER_STATUS = 10;
    public final static int TYPE_PROXIMITY_ALERT = 11;
    public final static int TYPE_PROVIDERS = 12;

    public int type;
    public String uuid;

    /* ------- TYPE_WEATHER_LIVE ------- */
    public RemoteWeatherLive weatherLive;

    /* ------- TYPE_WEATHER_FORECAST ------- */
    public RemoteWeatherForecast weatherForecast;

    /* ------- TYPE_LOCATION_CHANGED ------- */
    /* ------- TYPE_LAST_KNOWN_LOCATION ------- */
    public RemoteLocation location;

    /* ------- TYPE_GEOFENCE_ALERT ------- */
    public int geofenceAlertState;

    /* ------- TYPE_PROXIMITY_ALERT ------- */
    public int proximityAlertState;

    /* ------- TYPE_NETWORK_STATUS_CHANGED ------- */
    /* ------- TYPE_NETWORK_CURRENT_STATUS ------- */
    public int networkState;

    /* ------- TYPE_GPS_STATUS_CHANGED ------- */
    /* ------- TYPE_GPS_STATUS ------- */
    public int gpsEvent;
    public RemoteGpsStatus gpsStatus;

    /* ------- TYPE_PROVIDER_STATUS ------- */
    public boolean enabled;
    public String provider;

    /* ------- TYPE_PROVIDERS ------- */
    public boolean enabledOnly;
    public ArrayList<String> providerList;

    /* ------- TYPE_LOCATION_SERVICE_CONNECTED ------- */
    public boolean serviceConnected;

    private DataTransactor sender;

    public RemoteLocationResponse() {

    }

    public static RemoteLocationResponse obtain() {
        return new RemoteLocationResponse();
    }

    public static RemoteLocationResponse obtain(DataTransactor sender) {
        RemoteLocationResponse response = obtain();

        response.sender = sender;

        return response;
    }

    public void sendToRemote() {
        sender.send(this);
    }

    public void setSender(DataTransactor sender) {
        this.sender = sender;
    }

    public DataTransactor getSender() {
        return sender;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(uuid);
        dest.writeInt(geofenceAlertState);
        dest.writeInt(proximityAlertState);
        dest.writeInt(networkState);
        dest.writeInt(gpsEvent);
        dest.writeInt(serviceConnected ? 1 : 0);
        dest.writeInt(enabled ? 1 : 0);
        dest.writeInt(enabledOnly ? 1 : 0);
        dest.writeString(provider);

        if (weatherLive != null) {
            dest.writeInt(1);
            dest.writeParcelable(weatherLive, flags);
        } else {
            dest.writeInt(0);
        }

        if (weatherForecast != null) {
            dest.writeInt(1);
            dest.writeParcelable(weatherForecast, flags);
        } else {
            dest.writeInt(0);
        }

        if (location != null) {
            dest.writeInt(1);
            dest.writeParcelable(location, flags);
        } else {
            dest.writeInt(0);
        }

        if (gpsStatus != null) {
            dest.writeInt(1);
            dest.writeParcelable(gpsStatus, flags);
        } else {
            dest.writeInt(0);
        }

        if (providerList != null) {
            dest.writeInt(1);
            dest.writeStringList(providerList);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteLocationResponse> CREATOR = new Creator<RemoteLocationResponse>() {

        @Override
        public RemoteLocationResponse createFromParcel(Parcel source) {
            RemoteLocationResponse response = new RemoteLocationResponse();

            response.type = source.readInt();
            response.uuid = source.readString();
            response.geofenceAlertState = source.readInt();
            response.proximityAlertState = source.readInt();
            response.networkState = source.readInt();
            response.gpsEvent = source.readInt();
            response.serviceConnected = source.readInt() != 0;
            response.enabled = source.readInt() != 0;
            response.enabledOnly = source.readInt() != 0;
            response.provider = source.readString();

            if (source.readInt() != 0) {
                response.weatherLive = source
                        .readParcelable(RemoteWeatherLive.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                response.weatherForecast = source
                        .readParcelable(RemoteWeatherForecast.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                response.location = source.readParcelable(RemoteLocation.class
                        .getClassLoader());
            }

            if (source.readInt() != 0) {
                response.gpsStatus = source
                        .readParcelable(RemoteGpsStatus.class.getClassLoader());
            }

            if (source.readInt() != 0) {
                response.providerList = new ArrayList<String>();
                source.readStringList(response.providerList);
            }

            return response;
        }

        @Override
        public RemoteLocationResponse[] newArray(int size) {
            return new RemoteLocationResponse[size];
        }

    };

}
