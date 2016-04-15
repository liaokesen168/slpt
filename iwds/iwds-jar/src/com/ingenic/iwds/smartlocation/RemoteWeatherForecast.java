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

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 天气预报类。仅支持中国大陆、香港、澳门的数据返回。支持当前时间在内的3天的天气进行预报
 */
public class RemoteWeatherForecast implements Parcelable {
    private String reportTime;
    private List<RemoteDayWeatherForecast> weatherForecastList;
    private int errorCode;

    /**
     * 返回天气预报发布时间
     * 
     * @return 天气预报发布时间
     */
    public String getReportTime() {
        return this.reportTime;
    }

    /**
     * 设置天气预报发布时间
     * 
     * @param reportTime
     *            天气预报发布时间
     */
    public void setReportTime(String reportTime) {
        this.reportTime = reportTime;
    }

    /**
     * 返回天气预报的数据的数组，数组中的对象为 RemoteDayWeatherForecast
     * 数组中的第一个数据（即List.get(0)）表示今天的天气
     * ；第二个数据（即List.get(1)）表示明天的天气；第三个数据（即List.get(2)）表示后天的天气。
     * 
     * @return 天气预报的数据的数组
     */
    public List<RemoteDayWeatherForecast> getWeatherForecast() {
        return this.weatherForecastList;
    }

    /**
     * 设置天气预报的数据的数组
     * 
     * @param weatherForecastList
     *            天气预报的数据的数组
     */
    public void setListWeatherForecast(
            List<RemoteDayWeatherForecast> weatherForecastList) {
        this.weatherForecastList = weatherForecastList;
    }

    /**
     * 返回错误编码{@link com.ingenic.iwds.smartlocation.RemoteLocationErrorCode
     * RemoteLocationErrorCode}
     * 
     * @return 错误编码
     */
    public int getErrorCode() {
        return this.errorCode;
    }

    /**
     * 设置错误编码{@link com.ingenic.iwds.smartlocation.RemoteLocationErrorCode
     * RemoteLocationErrorCode}
     * 
     * @param errorCode
     *            错误编码
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        String data = null;

        for (RemoteDayWeatherForecast dayForecast : weatherForecastList) {
            data = dayForecast.toString();
        }

        return "RemoteWeatherForecast [errorCode=" + errorCode
                + " ,reportTime=" + reportTime + ", weatherForecastList="
                + data + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(reportTime);
        dest.writeInt(errorCode);

        if (weatherForecastList != null) {
            dest.writeInt(1);
            dest.writeList(weatherForecastList);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteWeatherForecast> CREATOR = new Creator<RemoteWeatherForecast>() {

        @Override
        public RemoteWeatherForecast createFromParcel(Parcel source) {
            RemoteWeatherForecast weatherForecast = new RemoteWeatherForecast();

            weatherForecast.reportTime = source.readString();
            weatherForecast.errorCode = source.readInt();

            if (source.readInt() != 0) {
                weatherForecast.weatherForecastList = source
                        .readArrayList(RemoteDayWeatherForecast.class
                                .getClassLoader());
            }
            return weatherForecast;
        }

        @Override
        public RemoteWeatherForecast[] newArray(int size) {
            return new RemoteWeatherForecast[size];
        }

    };

}
