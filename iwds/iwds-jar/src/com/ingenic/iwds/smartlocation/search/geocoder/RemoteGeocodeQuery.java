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

package com.ingenic.iwds.smartlocation.search.geocoder;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 此类定义了地理编码查询的关键字和查询城市
 */
public class RemoteGeocodeQuery implements Parcelable {
    private String city;
    private String locationName;

    /**
     * RemoteGeocodeQuery构造函数
     */
    public RemoteGeocodeQuery() {

    }

    /**
     * RemoteGeocodeQuery构造函数
     * 
     * @param city
     *            可选值：cityname（中文或中文全拼）、citycode、adcode。如传入null或空字符串则为“全国”
     * 
     * @param locationName
     *            查询关键字。关键字规则：多个关键字用“|”分割，“空格"表示与，"双引号" 表示不可分割
     * 
     */
    public RemoteGeocodeQuery(String city, String locationName) {
        this.city = city;
        this.locationName = locationName;
    }

    /**
     * 返回查询地理名称
     * 
     * @return 该结果的查询地理名称
     */
    public String getLocationName() {
        return this.locationName;
    }

    /**
     * 设置查询的地理名称
     * 
     * @param locationName
     *            查询的地理名称
     */
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    /**
     * 返回查询城市编码/城市名称/行政区划代码
     * 
     * @return 该结果的查询城市
     */
    public String getCity() {
        return this.city;
    }

    /**
     * 设置查询城市名称、城市编码或行政区划代码
     * 
     * @param city
     *            查询的cityname（中文或中文全拼）、citycode、adcode
     */
    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (!(object instanceof RemoteGeocodeQuery))
            return false;

        RemoteGeocodeQuery other = (RemoteGeocodeQuery) object;

        if (this.city == null) {
            if (other.city != null)
                return false;
        } else if (!this.city.equals(other.city))
            return false;
        if (this.locationName == null) {
            if (other.locationName != null)
                return false;
        } else if (!this.locationName.equals(other.locationName))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = prime * result
                + (this.city == null ? 0 : this.city.hashCode());
        result = prime
                * result
                + (this.locationName == null ? 0 : this.locationName.hashCode());

        return result;
    }

    @Override
    public String toString() {
        return "RemoteGeocodeQuery[" + "city=" + this.city + ", locationName="
                + this.locationName + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.city);
        dest.writeString(this.locationName);
    }

    public static final Creator<RemoteGeocodeQuery> CREATOR = new Creator<RemoteGeocodeQuery>() {

        @Override
        public RemoteGeocodeQuery createFromParcel(Parcel source) {
            RemoteGeocodeQuery query = new RemoteGeocodeQuery();

            query.city = source.readString();
            query.locationName = source.readString();

            return query;
        }

        @Override
        public RemoteGeocodeQuery[] newArray(int size) {
            return new RemoteGeocodeQuery[size];
        }
    };
}
