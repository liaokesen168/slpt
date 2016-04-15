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
 * 逆地理编码的搜索结果
 */
public class RemoteRegeocodeResult implements Parcelable {
    private RemoteRegeocodeQuery regeocodeQuery;
    private RemoteRegeocodeAddress regeocodeAddress;

    /**
     * RemoteRegeocodeResult构造函数
     */
    public RemoteRegeocodeResult() {

    }

    /**
     * RemoteRegeocodeResult构造函数
     * 
     * @param regeocodeQuery
     *            查询参数
     * 
     * @param regeocodeAddress
     *            地理结果
     */
    public RemoteRegeocodeResult(RemoteRegeocodeQuery regeocodeQuery,
            RemoteRegeocodeAddress regeocodeAddress) {
        this.regeocodeQuery = regeocodeQuery;
        this.regeocodeAddress = regeocodeAddress;
    }

    /**
     * 返回该结果对应的查询参数
     * 
     * @return 该结果对应的查询参数
     */
    public RemoteRegeocodeQuery getRegeocodeQuery() {
        return this.regeocodeQuery;
    }

    /**
     * 设置查询参数
     * 
     * @param regeocodeQuery
     *            查询参数
     */
    public void setRegeocodeQuery(RemoteRegeocodeQuery regeocodeQuery) {
        this.regeocodeQuery = regeocodeQuery;
    }

    /**
     * 返回逆地理编码搜索的地理结果
     * 
     * @return 逆地理编码搜索的地理结果
     */
    public RemoteRegeocodeAddress getRegeocodeAddress() {
        return this.regeocodeAddress;
    }

    /**
     * 设置地理结果
     * 
     * @param regeocodeAddress
     *            地理结果
     */
    public void setRegeocodeAddress(RemoteRegeocodeAddress regeocodeAddress) {
        this.regeocodeAddress = regeocodeAddress;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        if (this.regeocodeAddress != null) {
            dest.writeInt(1);
            dest.writeParcelable(regeocodeAddress, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.regeocodeQuery != null) {
            dest.writeInt(1);
            dest.writeParcelable(regeocodeQuery, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteRegeocodeResult> CREATOR = new Creator<RemoteRegeocodeResult>() {

        @Override
        public RemoteRegeocodeResult createFromParcel(Parcel source) {

            RemoteRegeocodeResult result = new RemoteRegeocodeResult();

            if (source.readInt() != 0) {
                result.regeocodeAddress = source
                        .readParcelable(RemoteRegeocodeAddress.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                result.regeocodeQuery = source
                        .readParcelable(RemoteRegeocodeQuery.class
                                .getClassLoader());
            }

            return result;
        }

        @Override
        public RemoteRegeocodeResult[] newArray(int size) {
            return new RemoteRegeocodeResult[size];
        }

    };

}
