/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  SunWenZhong(Fighter) <wenzhong.sun@ingenic.com, wanmyqawdr@126.com>
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

package com.ingenic.iwds.datatransactor.elf;

import android.os.Parcel;
import android.os.Parcelable;

import com.ingenic.iwds.utils.IwdsAssert;

/**
 * 天气信息阵列类.
 */
public class WeatherInfoArray implements Parcelable {

    /** 天气信息数据. */
    public WeatherInfo[] data;

    /**
     * 实例化天气信息阵列对象.
     *
     * @param weatherArray
     *            天气信息阵列
     */
    public WeatherInfoArray(WeatherInfo[] weatherArray) {
        IwdsAssert.dieIf(this,
                weatherArray == null || weatherArray.length <= 0,
                "Weather array is null or length <= 0.");

        data = weatherArray;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedArray(data, flags);
    }

    public static final Creator<WeatherInfoArray> CREATOR = new Creator<WeatherInfoArray>() {
        @Override
        public WeatherInfoArray createFromParcel(Parcel source) {
            WeatherInfo[] array = source.createTypedArray(WeatherInfo.CREATOR);

            WeatherInfoArray info = new WeatherInfoArray(array);

            return info;
        }

        @Override
        public WeatherInfoArray[] newArray(int size) {
            return new WeatherInfoArray[size];
        }
    };

    /**
     * 天气信息类.
     */
    public static class WeatherInfo implements Parcelable {

        /** 城市. */
        public String city;

        /** 天气描述（比如：多云）. */
        public String weather;

        /** 天气图标代码（引用自雅虎天气）. */
        public String weatherCode;

        /** 日期. */
        public String date;

        /** 星期. */
        public String dayOfWeek;

        /** 发布时间. */
        public String updateTime;

        /** 气温单位（'c'摄氏度，'f'华氏度）. */
        public String tempUnit;

        /** 当前实时气温. */
        public int currentTemp = -1;

        /** 最低气温. */
        public int minimumTemp = -1;

        /** 最高气温. */
        public int maximumTemp = -1;

        /** 日期索引. */
        public int dayIndex = -1;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(city);
            dest.writeString(weather);
            dest.writeString(weatherCode);
            dest.writeString(date);
            dest.writeString(dayOfWeek);
            dest.writeString(updateTime);
            dest.writeString(tempUnit);
            dest.writeInt(currentTemp);
            dest.writeInt(minimumTemp);
            dest.writeInt(maximumTemp);
            dest.writeInt(dayIndex);
        }

        public static final Creator<WeatherInfo> CREATOR = new Creator<WeatherInfo>() {
            @Override
            public WeatherInfo createFromParcel(Parcel source) {
                WeatherInfo info = new WeatherInfo();

                info.city = source.readString();
                info.weather = source.readString();
                info.weatherCode = source.readString();
                info.date = source.readString();
                info.dayOfWeek = source.readString();
                info.updateTime = source.readString();
                info.tempUnit = source.readString();
                info.currentTemp = source.readInt();
                info.minimumTemp = source.readInt();
                info.maximumTemp = source.readInt();
                info.dayIndex = source.readInt();

                return info;
            }

            @Override
            public WeatherInfo[] newArray(int size) {
                return new WeatherInfo[size];
            }
        };

        @Override
        public String toString() {
            return "WeatherInfo [city=" + city + ", weather=" + weather
                    + ", weatherCode=" + weatherCode + ", date=" + date
                    + ", dayOfWeek=" + dayOfWeek + ", updateTime=" + updateTime
                    + ", tempUnit=" + tempUnit + ", currentTemp=" + currentTemp
                    + ", minimumTemp=" + minimumTemp + ", maximumTemp="
                    + maximumTemp + ", dayIndex=" + dayIndex + "]";
        }
    }

    @Override
    public String toString() {
        String str = "WeatherInfoArray []:\n";

        int N = data.length;
        for (int i = 0; i < N; i++)
            str += "data[" + i + "]:" + data[i].toString() + "\n";

        return str;
    }
}
