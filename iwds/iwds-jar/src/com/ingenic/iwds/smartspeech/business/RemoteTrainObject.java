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
 * train的object抽象，表示火车业务信息类
 */
public class RemoteTrainObject extends RemoteBusinessObject {
    /**
     * RemoteTrainobject的object标识
     */
    public static String sFocus = "train";

    /**
     * datetime元素节点名，表示时间信息
     */
    public static final String RAWDATETIME = "datetime";

    /**
     * point元素节点名，表示地址信息
     */
    public static final String RAWPOINT = "point";

    /**
     * url元素节点名，表示查询机票需要打开的url
     */
    public static final String RAWURL = "url";

    /**
     * datetime元素节点的值，表示开车时间
     */
    public RemoteDateTime mStartDateTime = null;

    /**
     * datetime元素节点的值，表示到达时间
     */
    public RemoteDateTime mEndDateTime = null;

    /**
     * point元素节点的值组，存儲了火车地址信息，第一个是起始地点，第二个是终点
     */
    public List<String> mPoints = null;

    /**
     * url元素节点的值
     */
    public String mUrl = null;

    @Override
    public String toString() {
        return "Trainobject [startdatetime=" + mStartDateTime
                + ", enddatetime=" + mEndDateTime + ", ponint=" + mPoints
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

    public static final Creator<RemoteTrainObject> CREATOR = new Creator<RemoteTrainObject>() {
        @Override
        public RemoteTrainObject createFromParcel(Parcel source) {
            RemoteTrainObject info = new RemoteTrainObject();

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
        public RemoteTrainObject[] newArray(int size) {
            return new RemoteTrainObject[size];
        }
    };

}
