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

package com.ingenic.iwds.smartlocation.search.route;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 此类定义搜索返回城市的名称和编码
 */
public class RemoteSearchCity implements Parcelable {
    private String searchCityAdCode;
    private String searchCityCode;
    private String searchCityName;

    /**
     * RemoteSearchCity构造函数
     */
    public RemoteSearchCity() {

    }

    /**
     * RemoteSearchCity构造函数
     * 
     * @param city
     *            用于序列化实现的城市的名称和编码
     */
    public RemoteSearchCity(RemoteSearchCity city) {
        this.searchCityAdCode = city.searchCityAdCode;
        this.searchCityCode = city.searchCityCode;
        this.searchCityName = city.searchCityName;
    }

    /**
     * 得到城市的名称
     * 
     * @return 城市的名称
     */
    public String getSearchCityName() {
        return this.searchCityName;
    }

    /**
     * 设置城市的名称
     * 
     * @param searchCityName
     *            城市的名称
     */
    public void setSearchCityName(String searchCityName) {
        this.searchCityName = searchCityName;
    }

    /**
     * 得到城市的编码
     * 
     * @return 城市的编码
     */
    public String getSearchCityCode() {
        return this.searchCityCode;
    }

    /**
     * 设置城市的编码
     * 
     * @param searchCityCode
     *            城市的编码
     */
    public void setSearchCityCode(String searchCityCode) {
        this.searchCityCode = searchCityCode;
    }

    /**
     * 得到城市的行政编码
     * 
     * @return 城市的行政编码
     */
    public String getSearchCityAdCode() {
        return this.searchCityAdCode;
    }

    /**
     * 设置城市的行政编码
     * 
     * @param searchCityAdCode
     *            城市的行政编码
     */
    public void setSearchCityAdCode(String searchCityAdCode) {
        this.searchCityAdCode = searchCityAdCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.searchCityAdCode);
        dest.writeString(this.searchCityCode);
        dest.writeString(this.searchCityName);
    }

    public static final Creator<RemoteSearchCity> CREATOR = new Creator<RemoteSearchCity>() {

        @Override
        public RemoteSearchCity createFromParcel(Parcel source) {
            RemoteSearchCity searchCity = new RemoteSearchCity();

            searchCity.searchCityAdCode = source.readString();
            searchCity.searchCityCode = source.readString();
            searchCity.searchCityName = source.readString();

            return searchCity;
        }

        @Override
        public RemoteSearchCity[] newArray(int size) {
            return new RemoteSearchCity[size];
        }

    };

}
