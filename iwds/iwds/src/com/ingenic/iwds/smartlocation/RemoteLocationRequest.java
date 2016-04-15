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

import com.ingenic.iwds.datatransactor.DataTransactor;

import android.os.Parcel;
import android.os.Parcelable;

public class RemoteLocationRequest implements Parcelable {

    public final static int TYPE_REGISTER_LOCATION_LISTENER = 0;
    public final static int TYPE_UNREGISTER_LOCATION_LISTENER = 1;
    public final static int TYPE_WEATHER_UPDATE = 2;
    public final static int TYPE_REGISTER_GEOFENCE_LISTENER = 4;
    public final static int TYPE_UNREGISTER_GEOFENCE_LISTENER = 5;
    public final static int TYPE_LAST_KNOWN_LOCATION = 6;
    public final static int TYPE_REGISTER_GPS_STATUS_LISTENER = 7;
    public final static int TYPE_UNREGISTER_GPS_STATUS_LISTENER = 8;
    public final static int TYPE_REGISTER_NETWORK_STATUS_LISTENER = 9;
    public final static int TYPE_UNREGISTER_NETWORK_STATUS_LISTENER = 10;
    public final static int TYPE_GPS_STATUS = 11;
    public final static int TYPE_NETWORK_STATUS = 12;
    public final static int TYPE_PROVIDER_LIST = 13;
    public final static int TYPE_PROVIDER_STATUS = 14;
    public final static int TYPE_GPS_ENABLE = 15;
    public final static int TYPE_REGISTER_PROXIMITY_LISTENER = 16;
    public final static int TYPE_UNREGISTER_PROXIMITY_LISTENER = 17;

    public int type;
    public String uuid;

    /* ------- TYPE_PROVIDER_STATUS ------- */
    /* ------- TYPE_REGISTER_LOCATION_LISTENER ------- */
    /* ------- TYPE_UNREGISTER_LOCATION_LISTENER ------- */
    public String provider;
    public long minTime;
    public float minDistance;

    /* ------- TYPE_REGISTER_GEOFENCE_LISTENER ------- */
    /* ------- TYPE_UNREGISTER_GEOFENCE_LISTENER ------- */
    /* ------- TYPE_REGISTER_PROXIMITY_LISTENER ------- */
    /* ------- TYPE_UNREGISTER_PROXIMITY_LISTENER ------- */
    public double latitude;
    public double longitude;
    public float radius;
    public long expiration;

    /* ------- TYPE_GPS_ENABLE ------- */
    public boolean enabled;

    /* ------- TYPE_PROVIDERS ------- */
    public boolean enabledOnly;

    public int weatherType;

    private DataTransactor sender;

    public RemoteLocationRequest() {

    }

    public static RemoteLocationRequest obtain() {
        return new RemoteLocationRequest();
    }

    public static RemoteLocationRequest obtain(DataTransactor sender) {
        RemoteLocationRequest request = obtain();

        request.sender = sender;

        return request;
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
        dest.writeString(provider);
        dest.writeLong(minTime);
        dest.writeFloat(minDistance);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeFloat(radius);
        dest.writeLong(expiration);
        dest.writeInt(enabled ? 1 : 0);
        dest.writeInt(enabledOnly ? 1 : 0);
        dest.writeInt(weatherType);
    }

    public static final Creator<RemoteLocationRequest> CREATOR = new Creator<RemoteLocationRequest>() {

        @Override
        public RemoteLocationRequest createFromParcel(Parcel source) {
            RemoteLocationRequest request = new RemoteLocationRequest();

            request.type = source.readInt();
            request.uuid = source.readString();
            request.provider = source.readString();
            request.minTime = source.readLong();
            request.minDistance = source.readFloat();
            request.latitude = source.readDouble();
            request.longitude = source.readDouble();
            request.radius = source.readFloat();
            request.expiration = source.readLong();
            request.enabled = source.readInt() != 0;
            request.enabledOnly = source.readInt() != 0;
            request.weatherType = source.readInt();

            return request;
        }

        @Override
        public RemoteLocationRequest[] newArray(int size) {
            return new RemoteLocationRequest[size];
        }

    };
}
