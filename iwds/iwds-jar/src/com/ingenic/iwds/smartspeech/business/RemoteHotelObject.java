/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  Zhouzhiqiang <zhiqiang.zhou@ingenic.com>
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

package com.ingenic.iwds.smartspeech.business;

import android.os.Parcel;

/**
 * hotel的object抽象，表示酒店业务信息的类
 */
public class RemoteHotelObject extends RemoteBusinessObject {

    /**
     * RemoteHotelobject的object标识，区分于其他的object
     */
    public static String sFocus = "hotel";

    /**
     * url元素节点名，值表示用来查询的网址
     */
    public static final String RAWURL = "url";

    /**
     * data_source元素节点名，表示结果的来源信息
     */
    public static final String RAWDATASOURCE = "data_source";

    /**
     * data_source元素节点的值
     */
    public RemoteDatasource mDataSource = null;

    /**
     * url元素节点的值
     */
    public String mUrl = null;

    @Override
    public String toString() {
        return "Hotelobject [data_source=" + mDataSource + ", url=" + mUrl + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mUrl);
        arg0.writeParcelable(mDataSource, arg1);
    }

    public static final Creator<RemoteHotelObject> CREATOR = new Creator<RemoteHotelObject>() {
        @Override
        public RemoteHotelObject createFromParcel(Parcel source) {
            RemoteHotelObject info = new RemoteHotelObject();
            info.mUrl = source.readString();
            info.mDataSource = source.readParcelable(RemoteDatasource.class
                    .getClassLoader());
            return info;
        }

        @Override
        public RemoteHotelObject[] newArray(int size) {
            return new RemoteHotelObject[size];
        }
    };

}
