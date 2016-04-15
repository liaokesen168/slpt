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

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 地理编码的搜索结果
 */
public class RemoteGeocodeResult implements Parcelable {
    private RemoteGeocodeQuery geocodeQuery;
    private List<RemoteGeocodeAddress> geocodeAddressList = new ArrayList<RemoteGeocodeAddress>();

    /**
     * RemoteGeocodeResult构造函数
     */
    public RemoteGeocodeResult() {

    }

    /**
     * RemoteGeocodeResult构造函数
     * 
     * @param geocodeQuery
     *            查询参数
     * 
     * @param geocodeAddressList
     *            地理编码返回的地理结果
     */
    public RemoteGeocodeResult(RemoteGeocodeQuery geocodeQuery,
            List<RemoteGeocodeAddress> geocodeAddressList) {
        this.geocodeQuery = geocodeQuery;
        this.geocodeAddressList = geocodeAddressList;
    }

    /**
     * 返回该结果对应的查询参数
     * 
     * @return 该结果对应的查询参数
     */
    public RemoteGeocodeQuery getGeocodeQuery() {
        return this.geocodeQuery;
    }

    /**
     * 设置该结果对应的查询参数
     * 
     * @param geocodeQuery
     *            该结果对应的查询参数
     */
    public void setGeocodeQuery(RemoteGeocodeQuery geocodeQuery) {
        this.geocodeQuery = geocodeQuery;
    }

    /**
     * 返回地理编码搜索的地理结果
     * 
     * @return 地理编码搜索的地理结果
     */
    public List<RemoteGeocodeAddress> getGeocodeAddressList() {
        return this.geocodeAddressList;
    }

    /**
     * 设置地理编码搜索的地理结果
     * 
     * @param geocodeAddressList
     *            地理编码搜索的地理结果
     */
    public void setGeocodeAddressList(
            List<RemoteGeocodeAddress> geocodeAddressList) {
        this.geocodeAddressList = geocodeAddressList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        if (this.geocodeQuery != null) {
            dest.writeInt(1);
            dest.writeParcelable(geocodeQuery, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.geocodeAddressList != null) {
            dest.writeInt(1);
            dest.writeList(geocodeAddressList);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteGeocodeResult> CREATOR = new Creator<RemoteGeocodeResult>() {

        @Override
        public RemoteGeocodeResult createFromParcel(Parcel source) {

            RemoteGeocodeResult result = new RemoteGeocodeResult();

            if (source.readInt() != 0) {
                result.geocodeQuery = source
                        .readParcelable(RemoteGeocodeQuery.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                result.geocodeAddressList = source
                        .readArrayList(RemoteGeocodeAddress.class
                                .getClassLoader());
            }

            return result;
        }

        @Override
        public RemoteGeocodeResult[] newArray(int size) {
            return new RemoteGeocodeResult[size];
        }
    };

}
