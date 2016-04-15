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

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;

/**
 * flight的object的抽象，表示飞机业务信息的类
 */
public class RemoteFlightObject extends RemoteBusinessObject {

    /**
     * RemoteFlightobject的object标识，可以区分其他的objec
     */
    public static String sFocus = "flight";

    /**
     * datetime元素节点名，表示航班的时间信息
     */
    public static final String RAWDATETIME = "datetime";

    /**
     * point元素节点名，表示航班的地址信息
     */
    public static final String RAWPOINT = "point";

    /**
     * url元素节点名，表示可用于查询的链接
     */
    public static final String RAWURL = "url";

    /**
     * datetime元素节点的值，表示起飞时间
     */
    public RemoteDateTime mStartDateTime = null;

    /**
     * datetime元素节点的值，表示降落时间
     */
    public RemoteDateTime mEndDateTime = null;

    /**
     * point元素节点的数组，存储了航班的起点和终点，第一个为起点，第二个为终点
     */
    public List<String> mPoints = null;

    /**
     * url元素节点的值
     */
    public String mUrl = null;

    @Override
    public String toString() {
        return "Flightobject [startdatetime=" + mStartDateTime
                + ", enddatetime=" + mEndDateTime + ", point=" + mPoints
                + ", url=" + mUrl + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mUrl);
        arg0.writeParcelable(mStartDateTime, arg1);
        arg0.writeParcelable(mEndDateTime, arg1);
        arg0.writeList(mPoints);
    }

    public static final Creator<RemoteFlightObject> CREATOR = new Creator<RemoteFlightObject>() {
        @Override
        public RemoteFlightObject createFromParcel(Parcel source) {
            RemoteFlightObject info = new RemoteFlightObject();
            info.mUrl = source.readString();
            info.mStartDateTime = source.readParcelable(RemoteDateTime.class
                    .getClassLoader());
            info.mEndDateTime = source.readParcelable(RemoteDateTime.class
                    .getClassLoader());
            info.mPoints = new ArrayList<String>();
            source.readList(info.mPoints, String.class.getClassLoader());
            return info;
        }

        @Override
        public RemoteFlightObject[] newArray(int size) {
            return new RemoteFlightObject[size];
        }
    };
}
