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

package com.ingenic.iwds.smartsense;

import com.ingenic.iwds.datatransactor.DataTransactor;
import android.os.Parcel;
import android.os.Parcelable;

public class RemoteSensorRequest implements Parcelable {

    public final static int TYPE_SENSOR_LIST = 0;
    public final static int TYPE_REGISTER_LISTENER = 1;
    public final static int TYPE_UNREGISTER_LISTENER = 2;

    public int type;
    public int sensorRate;
    public Sensor sensor;

    private DataTransactor sender;

    public RemoteSensorRequest() {
    }

    public static RemoteSensorRequest obtain() {
        return new RemoteSensorRequest();
    }

    public static RemoteSensorRequest obtain(DataTransactor sender) {
        RemoteSensorRequest request = obtain();

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
        dest.writeInt(sensorRate);

        if (sensor != null) {
            dest.writeInt(1);
            dest.writeParcelable(sensor, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteSensorRequest> CREATOR = new Creator<RemoteSensorRequest>() {

        @Override
        public RemoteSensorRequest createFromParcel(Parcel source) {
            RemoteSensorRequest request = new RemoteSensorRequest();
            request.type = source.readInt();
            request.sensorRate = source.readInt();

            if (source.readInt() != 0) {
                request.sensor = source.readParcelable(Sensor.class.getClassLoader());
            }

            return request;
        }

        @Override
        public RemoteSensorRequest[] newArray(int size) {
            return new RemoteSensorRequest[size];
        }

    };
}
