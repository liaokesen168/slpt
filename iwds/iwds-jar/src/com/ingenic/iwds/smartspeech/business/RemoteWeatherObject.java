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
 * weather的object抽象，表示天气业务信息类
 */
public class RemoteWeatherObject extends RemoteBusinessObject {

    /**
     * RemoteWeatherobject的object标识
     */
    public static String sFocus = "weather";

    /**
     * data_source元素节点名，表示数据来源信息
     */
    public static final String RAWDATESOURCE = "data_source";

    /**
     * city元素节点名，表示城市信息
     */
    public static final String RAWCITY = "city";

    /**
     * last_update元素节点名，表示最后一次更新的时间信息
     */
    public static final String RAWLASTUPDATE = "last_update";

    /**
     * interest_datetime元素节点名，用户希望查询的日期时间，数量不确定（0~n个）
     */
    public static final String RAWINTERESTDATETIME = "interest_datetime";

    /**
     * forecast元素节点名，表示0~n天的天气预报。有一个或者多个 
     */
    public static final String RAWFORECAST = "forecast";
    /**
     * datetime元素节点名，表示时间信息
     */
    public static final String RAWDATETIME = "datetime";

    /**
     * data_source元素节点的值
     */
    public RemoteDatasource mDataSource = null;

    /**
     * city元素节点的值
     */
    public String mCity = null;

    /**
     * last_update元素节点的值
     */
    public RemoteDateTime mLastUpdate = null;

    /**
     * interest_datetime元素节点的值
     */
    public RemoteDateTime mInterestDatetime = null;

    /**
     * forecast元素节点的值组
     */
    public List<RemoteWeatherForecast> mForecasts = null;

    @Override
    public String toString() {
        return "Weatherobject [datesource=" + mDataSource + ", city=" + mCity
                + ", last_update=" + mLastUpdate + ", interest_datetime="
                + mInterestDatetime + ", forecast=" + mForecasts + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeParcelable(mDataSource, arg1);
        arg0.writeString(mCity);
        arg0.writeParcelable(mLastUpdate, arg1);
        arg0.writeParcelable(mInterestDatetime, arg1);
        arg0.writeList(mForecasts);
    }

    public static final Creator<RemoteWeatherObject> CREATOR = new Creator<RemoteWeatherObject>() {
        @Override
        public RemoteWeatherObject createFromParcel(Parcel source) {
            RemoteWeatherObject info = new RemoteWeatherObject();
            info.mDataSource = source.readParcelable(RemoteDatasource.class
                    .getClassLoader());
            info.mCity = source.readString();
            info.mLastUpdate = source.readParcelable(RemoteDateTime.class
                    .getClassLoader());
            info.mInterestDatetime = source.readParcelable(RemoteDateTime.class
                    .getClassLoader());
            info.mForecasts = new ArrayList<RemoteWeatherForecast>();
            source.readList(info.mForecasts,
                    RemoteWeatherForecast.class.getClassLoader());
            return info;
        }

        @Override
        public RemoteWeatherObject[] newArray(int size) {
            return new RemoteWeatherObject[size];
        }
    };
}
