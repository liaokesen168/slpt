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

package com.ingenic.iwds.cloud;

import java.util.ArrayList;
import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;

import com.ingenic.iwds.datatransactor.DataTransactor;

public class RemoteCloudResponse implements Parcelable {
    public final static int TYPE_LOGIN_SUCCESS = 0;
    public final static int TYPE_LOGIN_FAILURE = 1;
    public final static int TYPE_ACCOUNT_SUCCESS = 2;
    public final static int TYPE_ACCOUNT_FAILURE = 3;
    public final static int TYPE_QUERY_SUCCESS = 4;
    public final static int TYPE_QUERY_FAILURE = 5;
    public final static int TYPE_INSERT_SUCCESS = 6;
    public final static int TYPE_INSERT_FAILURE = 7;
    public final static int TYPE_OPERATION_SUCCESS = 8;
    public final static int TYPE_OPERATION_FAILURE = 9;

    public int type;

    public String listenerID;
    public int errorCode;
    public String errorMsg;
    public List<CloudDataValues> datas;

    private DataTransactor sender;

    public RemoteCloudResponse() {

    }

    public static RemoteCloudResponse obtain() {
        return new RemoteCloudResponse();
    }

    public static RemoteCloudResponse obtain(DataTransactor sender) {
        RemoteCloudResponse response = obtain();

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
        dest.writeInt(type);

        if (listenerID != null) {
            dest.writeInt(1);
            dest.writeString(listenerID);
        } else {
            dest.writeInt(0);
        }

        dest.writeInt(errorCode);

        if (errorMsg != null) {
            dest.writeInt(1);
            dest.writeString(errorMsg);
        } else {
            dest.writeInt(0);
        }

        if (datas != null) {
            dest.writeInt(1);
            dest.writeTypedList(datas);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteCloudResponse> CREATOR = new Creator<RemoteCloudResponse>() {
        @Override
        public RemoteCloudResponse createFromParcel(Parcel source) {
            RemoteCloudResponse response = new RemoteCloudResponse();

            response.type = source.readInt();
            if (source.readInt() != 0) {
                response.listenerID = source.readString();
            }
            response.errorCode = source.readInt();
            if (source.readInt() != 0) {
                response.errorMsg = source.readString();
            }

            if (source.readInt() != 0) {
                response.datas = new ArrayList<CloudDataValues>();
                source.readTypedList(response.datas, CloudDataValues.CREATOR);
            }

            return response;
        }

        @Override
        public RemoteCloudResponse[] newArray(int size) {
            return new RemoteCloudResponse[size];
        }
    };

}