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

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 此类定义路径规划返回的城市名称、编码和行政区的名称和编码
 */
public class RemoteRouteSearchCity extends RemoteSearchCity implements
        Parcelable {
    private List<RemoteDistrict> districtList = new ArrayList<RemoteDistrict>();

    /**
     * RemoteRouteSearchCity构造函数
     */
    public RemoteRouteSearchCity() {
        super();
    }

    /**
     * RemoteRouteSearchCity构造函数
     * 
     * @param city
     *            用于序列化实现的路径规划城市名称、编码和行政区的名称和编码
     */
    public RemoteRouteSearchCity(RemoteSearchCity city) {
        super(city);
    }

    /**
     * 设置城市的名称
     * 
     * @return 所有的行政区对象
     */
    public List<RemoteDistrict> getDistricts() {
        return this.districtList;
    }

    /**
     * 设置行政区对象
     * 
     * @param districtList
     *            行政区对象
     */
    public void setDistricts(List<RemoteDistrict> districtList) {
        this.districtList = districtList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        if (this.districtList != null) {
            dest.writeInt(1);
            dest.writeList(this.districtList);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteRouteSearchCity> CREATOR = new Creator<RemoteRouteSearchCity>() {

        @Override
        public RemoteRouteSearchCity createFromParcel(Parcel source) {
            RemoteSearchCity searchCity = RemoteSearchCity.CREATOR
                    .createFromParcel(source);

            RemoteRouteSearchCity routeSearchCity = new RemoteRouteSearchCity(
                    searchCity);

            if (source.readInt() != 0) {
                routeSearchCity.districtList = source
                        .readArrayList(RemoteDistrict.class.getClassLoader());
            }

            return routeSearchCity;
        }

        @Override
        public RemoteRouteSearchCity[] newArray(int size) {
            return new RemoteRouteSearchCity[size];
        }
    };
}
