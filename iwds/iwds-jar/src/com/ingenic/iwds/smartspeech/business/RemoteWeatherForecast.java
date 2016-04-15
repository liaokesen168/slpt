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
import android.os.Parcelable;

/**
 * 天气业务的forecast节点的抽象，表示某天天气的信息类
 */
public class RemoteWeatherForecast implements Parcelable {

    /**
     * datetime元素节点的date属性名，表示预报的日期（格式为YYYY-MM-DD）
     */
    public static String ATTRDATE = "date";

    /**
     * datetime元素节点名，表示时间信息
     */
    public static final String RAWDATE = "datetime";

    /**
     * condition元素节点名，表示天气状况。没有则为空节点
     */
    public static final String RAWCONDITION = "condition";

    /**
     * temp元素节点名，表示当前温度（℃）
     */
    public static final String RAWTEMP = "temp";

    /**
     * humidity元素节点名，当前湿度（%）
     */
    public static final String RAWHUMIDITY = "humidity";

    /**
     * wind元素节点名，表示当前风力风速描述
     */
    public static final String RAWWIND = "wind";

    /**
     * low元素节点名，全天最低温度（℃）
     */
    public static final String RAWLOW = "low";

    /**
     * high元素节点名，全天最高温度（℃）
     */
    public static final String RAWHIGH = "high";

    /**
     * datetime元素节点的date属性的值
     */
    public String mDate = null;

    /**
     * condition元素节点的值
     */
    public RemoteWeatherCondition mCondition = null;

    /**
     * temp元素节点的值
     */
    public String mTemp = null;

    /**
     * humidity元素节点的值
     */
    public String mHumidity = null;

    /**
     * wind元素节点的值
     */
    public String mWind = null;

    /**
     * low元素节点的值
     */
    public String mLow = null;

    /**
     * high元素节点的值
     */
    public String mHigh = null;

    @Override
    public String toString() {
        return "Weatherforecast [date=" + mDate + ", condition=" + mCondition
                + ", temp=" + mTemp + ", humidity=" + mHumidity + ", wind="
                + mWind + ", low=" + mLow + ", high=" + mHigh + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mDate);
        arg0.writeParcelable(mCondition, arg1);
        arg0.writeString(mTemp);
        arg0.writeString(mHumidity);
        arg0.writeString(mWind);
        arg0.writeString(mLow);
        arg0.writeString(mHigh);
    }

    public static final Creator<RemoteWeatherForecast> CREATOR = new Creator<RemoteWeatherForecast>() {
        @Override
        public RemoteWeatherForecast createFromParcel(Parcel source) {
            RemoteWeatherForecast info = new RemoteWeatherForecast();
            info.mDate = source.readString();
            info.mCondition = source
                    .readParcelable(RemoteWeatherCondition.class
                            .getClassLoader());
            info.mTemp = source.readString();
            info.mHumidity = source.readString();
            info.mWind = source.readString();
            info.mLow = source.readString();
            info.mHigh = source.readString();
            return info;
        }

        @Override
        public RemoteWeatherForecast[] newArray(int size) {
            return new RemoteWeatherForecast[size];
        }
    };

}
