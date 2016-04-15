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

public class RemoteCloudRequest implements Parcelable {
    public final static int TYPE_OPEN_SESSION = 0;
    public final static int TYPE_CLOSE_SESSION = 1;
    public final static int TYPE_REGISTER_USER = 2;
    public final static int TYPE_REGISTER_USER_WITH_EMAIL = 3;
    public final static int TYPE_REGISTER_USER_WITH_PHONE = 4;
    public final static int TYPE_REQUEST_PHONE_VERIFY_CODE = 5;
    public final static int TYPE_RESET_PASSWORD_WITH_EMAIL = 6;
    public final static int TYPE_RESET_PASSWORD_WITH_PHONE = 7;
    public final static int TYPE_LOGIN_ANONYMOUS = 8;
    public final static int TYPE_LOGIN = 9;
    public final static int TYPE_LOGIN_WITH_THIRD_ACCOUNT = 10;
    public final static int TYPE_LOGOUT = 11;
    public final static int TYPE_CHANGE_USER_PASSWORD = 12;
    public final static int TYPE_QUERY_DATA = 13;
    public final static int TYPE_INSERT_DATA = 14;
    public final static int TYPE_UPDATE_DATA = 15;
    public final static int TYPE_DELETE_DATA = 16;

    public int type;

    public String sessionID;
    public String listenerID;

    public String string1;
    public String string2;
    public String string3;
    public String string4;

    public int int1;
    public int int2;

    public CloudQuery query;
    public List<CloudDataValues> datas;

    private DataTransactor sender;

    public RemoteCloudRequest() {

    }

    public static RemoteCloudRequest obtain() {
        return new RemoteCloudRequest();
    }

    public static RemoteCloudRequest obtain(DataTransactor sender) {
        RemoteCloudRequest request = obtain();

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
        dest.writeInt(type);

        if (sessionID != null) {
            dest.writeInt(1);
            dest.writeString(sessionID);
        } else {
            dest.writeInt(0);
        }

        if (listenerID != null) {
            dest.writeInt(1);
            dest.writeString(listenerID);
        } else {
            dest.writeInt(0);
        }

        if (string1 != null) {
            dest.writeInt(1);
            dest.writeString(string1);
        } else {
            dest.writeInt(0);
        }

        if (string2 != null) {
            dest.writeInt(1);
            dest.writeString(string2);
        } else {
            dest.writeInt(0);
        }
        if (string3 != null) {
            dest.writeInt(1);
            dest.writeString(string3);
        } else {
            dest.writeInt(0);
        }
        if (string4 != null) {
            dest.writeInt(1);
            dest.writeString(string4);
        } else {
            dest.writeInt(0);
        }

        dest.writeInt(int1);
        dest.writeInt(int2);

        if (query != null) {
            dest.writeInt(1);
            dest.writeParcelable(query, flags);
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

    public static final Creator<RemoteCloudRequest> CREATOR = new Creator<RemoteCloudRequest>() {

        @Override
        public RemoteCloudRequest createFromParcel(Parcel source) {
            RemoteCloudRequest request = new RemoteCloudRequest();

            request.type = source.readInt();

            if (source.readInt() != 0) {
                request.sessionID = source.readString();
            }

            if (source.readInt() != 0) {
                request.listenerID = source.readString();
            }

            if (source.readInt() != 0) {
                request.string1 = source.readString();
            }

            if (source.readInt() != 0) {
                request.string2 = source.readString();
            }

            if (source.readInt() != 0) {
                request.string3 = source.readString();
            }
            if (source.readInt() != 0) {
                request.string4 = source.readString();
            }

            request.int1 = source.readInt();
            request.int2 = source.readInt();

            if (source.readInt() != 0) {
                request.query = source.readParcelable(CloudQuery.class
                        .getClassLoader());
            }

            if (source.readInt() != 0) {
                request.datas = new ArrayList<CloudDataValues>();
                source.readTypedList(request.datas, CloudDataValues.CREATOR);
            }

            return request;
        }

        @Override
        public RemoteCloudRequest[] newArray(int size) {
            return new RemoteCloudRequest[size];
        }
    };

}
