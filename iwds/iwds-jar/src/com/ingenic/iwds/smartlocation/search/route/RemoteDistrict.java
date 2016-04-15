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
 * 此类定义搜索返回行政区的名称和编码
 */
public class RemoteDistrict implements Parcelable {
    private String districtAdCode;
    private String districtName;

    /**
     * RemoteDistrict构造函数
     */
    public RemoteDistrict() {

    }

    /**
     * 返回行政区的编码
     * 
     * @return 行政区的编码
     */
    public String getDistrictAdCode() {
        return this.districtAdCode;
    }

    /**
     * 设置行政区的编码
     * 
     * @param districtCode
     *            行政区的编码
     */
    public void setDistrictAdCode(String districtCode) {
        this.districtAdCode = districtCode;
    }

    /**
     * 返回行政区的名称
     * 
     * @return 行政区的名称
     */
    public String getDistrictName() {
        return this.districtName;
    }

    /**
     * 设置行政区的名称
     * 
     * @param districtName
     *            行政区的名称
     */
    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.districtAdCode);
        dest.writeString(this.districtName);
    }

    public static final Creator<RemoteDistrict> CREATOR = new Creator<RemoteDistrict>() {

        @Override
        public RemoteDistrict createFromParcel(Parcel source) {
            RemoteDistrict district = new RemoteDistrict();

            district.districtAdCode = source.readString();
            district.districtName = source.readString();

            return district;
        }

        @Override
        public RemoteDistrict[] newArray(int size) {
            return new RemoteDistrict[size];
        }

    };

}
