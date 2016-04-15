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

import java.util.ArrayList;

import com.ingenic.iwds.datatransactor.DataTransactor;

import android.os.Parcel;
import android.os.Parcelable;

public class RemoteSensorResponse implements Parcelable {

    public final static int TYPE_SENSOR_LIST = 0;
    public final static int TYPE_SENSEOR_SERVICE_CONNECTED = 1;
    public final static int TYPE_SENSOR_CHANGED = 2;
    public final static int TYPE_SENSOR_ACCURACY_CHANGED = 3;

    public int type;
    public int result;
    public SensorEvent sensorEvent;
    public Sensor sensor;
    public int accuracy;
    public ArrayList<Sensor> sensorList;

    private DataTransactor sender;

    public RemoteSensorResponse() {
    }

    public static RemoteSensorResponse obtain() {
        return new RemoteSensorResponse();
    }

    public static RemoteSensorResponse obtain(DataTransactor sender) {
        RemoteSensorResponse response = obtain();

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
        dest.writeInt(result);
        dest.writeInt(accuracy);

        if (sensor != null) {
            dest.writeInt(1);
            dest.writeParcelable(sensor, flags);
        } else {
            dest.writeInt(0);
        }

        if (sensorEvent != null) {
            dest.writeInt(1);
            dest.writeParcelable(sensorEvent, flags);
        } else {
            dest.writeInt(0);
        }

        if (sensorList != null) {
            dest.writeInt(1);
            dest.writeList(sensorList);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteSensorResponse> CREATOR = new Creator<RemoteSensorResponse>() {

        @Override
        public RemoteSensorResponse createFromParcel(Parcel source) {
            RemoteSensorResponse response = new RemoteSensorResponse();

            response.type = source.readInt();
            response.result = source.readInt();
            response.accuracy = source.readInt();

            if (source.readInt() != 0) {
                response.sensor = source.readParcelable(Sensor.class
                        .getClassLoader());
            }

            if (source.readInt() != 0) {
                response.sensorEvent = source.readParcelable(SensorEvent.class
                        .getClassLoader());
            }

            if (source.readInt() != 0) {
                response.sensorList = source.readArrayList(Sensor.class
                        .getClassLoader());
            }

            return response;
        }

        @Override
        public RemoteSensorResponse[] newArray(int size) {
            return new RemoteSensorResponse[size];
        }
    };

}
