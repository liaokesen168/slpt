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

package com.ingenic.iwds.smartlocation.search.district;

import java.util.ArrayList;
import java.util.List;

import com.ingenic.iwds.smartlocation.search.core.RemoteLatLonPoint;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 行政区信息类
 */
public class RemoteDistrictItem implements Parcelable {
    private String adCode;
    private RemoteLatLonPoint center;
    private String cityCode;
    private String level;
    private String name;
    private List<RemoteDistrictItem> subDistrict = new ArrayList<RemoteDistrictItem>();

    /**
     * RemoteDistrictItem构造函数
     */
    public RemoteDistrictItem() {

    }

    /**
     * RemoteDistrictItem构造函数
     * 
     * @param adCode
     *            区域编码
     * 
     * @param center
     *            行政区域规划中心点的经纬度坐标
     * 
     * @param cityCode
     *            城市编码
     * 
     * @param level
     *            当前行政区划的级别
     * 
     * @param name
     *            行政区域的名称
     */
    public RemoteDistrictItem(String adCode, RemoteLatLonPoint center,
            String cityCode, String level, String name) {
        this.adCode = adCode;
        this.center = center;
        this.cityCode = cityCode;
        this.level = level;
        this.name = name;
    }

    /**
     * 返回城市编码，如果行政区为省或者国家，此字段无返回值
     * 
     * @return 城市编码
     */
    public String getCityCode() {
        return this.cityCode;
    }

    /**
     * 设置城市编码
     * 
     * @param cityCode
     *            城市编码
     */
    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    /**
     * 返回区域编码
     * 
     * @return 区域编码
     */
    public String getAdCode() {
        return this.adCode;
    }

    /**
     * 设置区域编码
     * 
     * @param adCode
     *            区域编码
     */
    public void setAdCode(String adCode) {
        this.adCode = adCode;
    }

    /**
     * 返回行政区域的名称
     * 
     * @return 行政区域的名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置行政区域的名称
     * 
     * @param name
     *            行政区域的名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 返回行政区域规划中心点的经纬度坐标
     * 
     * @return 行政区域规划中心点的经纬度坐标
     */
    public RemoteLatLonPoint getCenter() {
        return this.center;
    }

    /**
     * 设置行政区域规划中心点的经纬度坐标
     * 
     * @param center
     *            行政区域规划中心点的经纬度坐标
     */
    public void setCenter(RemoteLatLonPoint center) {
        this.center = center;
    }

    /**
     * 返回当前行政区划的级别。匹配级别共有五个级别，详见 RemoteDistrictSearch 中定义的常量
     * 
     * @return 当前行政区划的级别
     */
    public String getLevel() {
        return this.level;
    }

    /**
     * 设置当前行政区划的级别
     * 
     * @param level
     *            当前行政区划的级别
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * 返回下一级行政区划的结果，如果无下级行政区划，返回null
     * 
     * @return 下一级行政区划的结果，如果无下级行政区划，返回null
     */
    public List<RemoteDistrictItem> getSubDistrict() {
        return this.subDistrict;
    }

    /**
     * 设置下一级行政区划的结果
     * 
     * @param subDistrict
     *            下一级行政区划的结果
     */
    public void setSubDistrict(ArrayList<RemoteDistrictItem> subDistrict) {
        this.subDistrict = subDistrict;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (!(object instanceof RemoteDistrictItem))
            return false;

        RemoteDistrictItem other = (RemoteDistrictItem) object;

        if (this.adCode == null) {
            if (other.adCode != null)
                return false;
        } else if (!this.adCode.equals(other.adCode))
            return false;
        if (this.cityCode == null) {
            if (other.cityCode != null)
                return false;
        } else if (!this.cityCode.equals(other.cityCode))
            return false;
        if (this.center == null) {
            if (other.center != null)
                return false;
        } else if (!this.center.equals(other.center))
            return false;
        if (this.level == null) {
            if (other.level != null)
                return false;
        } else if (!this.level.equals(other.level))
            return false;
        if (this.subDistrict == null) {
            if (other.subDistrict != null)
                return false;
        } else if (!this.subDistrict.equals(other.subDistrict))
            return false;
        if (this.name == null) {
            if (other.name != null)
                return false;
        } else if (!this.name.equals(other.name))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = prime * result
                + (this.adCode == null ? 0 : this.adCode.hashCode());
        result = prime * result
                + (this.cityCode == null ? 0 : this.cityCode.hashCode());
        result = prime * result
                + (this.level == null ? 0 : this.level.hashCode());
        result = prime * result
                + (this.name == null ? 0 : this.name.hashCode());
        result = prime * result
                + (this.subDistrict == null ? 0 : this.subDistrict.hashCode());
        result = prime * result
                + (this.center == null ? 0 : this.center.hashCode());

        return result;
    }

    @Override
    public String toString() {
        return "RemoteDistictItem [" + "cityCode=" + this.cityCode
                + ", adCode=" + this.adCode + ", name=" + this.name
                + ", center=" + center + ", level=" + this.level
                + ", subDistricts=" + this.subDistrict + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.adCode);

        if (this.center != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.center, flags);
        } else {
            dest.writeInt(0);
        }

        dest.writeString(this.cityCode);
        dest.writeString(this.level);
        dest.writeString(this.name);

        if (this.subDistrict != null) {
            dest.writeInt(1);
            dest.writeList(this.subDistrict);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteDistrictItem> CREATOR = new Creator<RemoteDistrictItem>() {

        @Override
        public RemoteDistrictItem createFromParcel(Parcel source) {
            RemoteDistrictItem districtItem = new RemoteDistrictItem();

            districtItem.adCode = source.readString();
            if (source.readInt() != 0)
                districtItem.center = source
                        .readParcelable(RemoteLatLonPoint.class
                                .getClassLoader());

            districtItem.cityCode = source.readString();
            districtItem.level = source.readString();
            districtItem.name = source.readString();

            if (source.readInt() != 0)
                districtItem.subDistrict = source
                        .readArrayList(RemoteDistrictItem.class
                                .getClassLoader());

            return districtItem;
        }

        @Override
        public RemoteDistrictItem[] newArray(int size) {
            return new RemoteDistrictItem[size];
        }

    };
}
