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

import com.ingenic.iwds.smartlocation.search.core.RemoteLatLonPoint;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 地理编码返回的结果
 */
public class RemoteGeocodeAddress implements Parcelable {
    private String adCode;
    private String building;
    private String city;
    private String district;
    private String formatAddress;
    private RemoteLatLonPoint point;
    private String level;
    private String neighborhood;
    private String province;
    private String townShip;

    /**
     * RemoteGeocodeAddress构造函数
     */
    public RemoteGeocodeAddress() {

    }

    /**
     * 地理编码返回的格式化地址。如返回北京市朝阳区方恒国际中心
     * 
     * @return 结果的格式化地址
     */
    public String getFormatAddress() {
        return this.formatAddress;
    }

    /**
     * 设置格式化地址
     * 
     * @param formatAddress
     *            格式化地址
     */
    public void setFormatAddress(String formatAddress) {
        this.formatAddress = formatAddress;
    }

    /**
     * 地理编码返回的所在省名称、直辖市的名称
     * 
     * @return 结果的所在省名称、直辖市的名称
     */
    public String getProvince() {
        return this.province;
    }

    /**
     * 设置所在省名称、直辖市的名称
     * 
     * @param province
     *            所在省名称、直辖市的名称
     */
    public void setProvince(String province) {
        this.province = province;
    }

    /**
     * 地理编码返回的所在城市名称。直辖市的名称参见省名称，此项为空
     * 
     * @return 城市名称
     */
    public String getCity() {
        return this.city;
    }

    /**
     * 设置所在城市名称
     * 
     * @param city
     *            城市名称
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * 地理编码返回的所在区（县）名称
     * 
     * @return 结果的所在区（县）名称
     */
    public String getDistrict() {
        return this.district;
    }

    /**
     * 设置所在区（县）名称
     * 
     * @param district
     *            所在区（县）名称
     */
    public void setDistrict(String district) {
        this.district = district;
    }

    /**
     * 地理编码返回的乡镇名称
     * 
     * @return 结果的乡镇名称
     */
    public String getTownShip() {
        return this.townShip;
    }

    /**
     * 设置乡镇名称
     * 
     * @param townShip
     *            乡镇名称
     */
    public void setTownShip(String townShip) {
        this.townShip = townShip;
    }

    /**
     * 地理编码返回的社区名称
     * 
     * @return 结果的社区名称
     */
    public String getNeighborhood() {
        return this.neighborhood;
    }

    /**
     * 设置社区名称
     * 
     * @param neighborhood
     *            社区名称
     */
    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    /**
     * 地理编码返回的建筑物名称
     * 
     * @return 结果的建筑物名称
     */
    public String getBuilding() {
        return this.building;
    }

    /**
     * 设置建筑物名称
     * 
     * @param building
     *            建筑物名称
     */
    public void setBuilding(String building) {
        this.building = building;
    }

    /**
     * 地理编码返回的区域编码
     * 
     * @return 结果的区域编码
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
     * 地理编码返回的经纬度坐标
     * 
     * @return 结果的经纬度坐标
     */
    public RemoteLatLonPoint getLatLonPoint() {
        return this.point;
    }

    /**
     * 设置经纬度坐标
     * 
     * @param point
     *            经纬度坐标
     */
    public void setLatLonPoint(RemoteLatLonPoint point) {
        this.point = point;
    }

    /**
     * 地理编码返回的匹配级别。匹配级别有“兴趣点”、“交叉路口”、“道路”
     * 
     * @return 结果的匹配级别
     */
    public String getLevel() {
        return this.level;
    }

    /**
     * 设置地理编码的匹配级别。匹配级别有“兴趣点”、“交叉路口”、“道路”
     * 
     * @param level
     *            匹配级别
     */
    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.adCode);
        dest.writeString(this.building);
        dest.writeString(this.city);
        dest.writeString(this.district);
        dest.writeString(this.formatAddress);
        dest.writeString(this.level);
        dest.writeString(this.neighborhood);
        dest.writeString(this.province);
        dest.writeString(this.townShip);

        if (this.point != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.point, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteGeocodeAddress> CREATOR = new Creator<RemoteGeocodeAddress>() {
        @Override
        public RemoteGeocodeAddress createFromParcel(Parcel source) {
            RemoteGeocodeAddress geocodeAddress = new RemoteGeocodeAddress();

            geocodeAddress.adCode = source.readString();
            geocodeAddress.building = source.readString();
            geocodeAddress.city = source.readString();
            geocodeAddress.district = source.readString();
            geocodeAddress.formatAddress = source.readString();
            geocodeAddress.level = source.readString();
            geocodeAddress.neighborhood = source.readString();
            geocodeAddress.province = source.readString();
            geocodeAddress.townShip = source.readString();

            if (source.readInt() != 0) {
                geocodeAddress.point = source
                        .readParcelable(RemoteLatLonPoint.class
                                .getClassLoader());
            }

            return geocodeAddress;
        }

        @Override
        public RemoteGeocodeAddress[] newArray(int size) {
            return new RemoteGeocodeAddress[size];
        }

    };

}
