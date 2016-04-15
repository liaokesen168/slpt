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

import com.ingenic.iwds.smartlocation.search.core.RemotePoiItem;
import com.ingenic.iwds.smartlocation.search.road.RemoteCrossroad;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 逆地理编码返回的结果
 */
public class RemoteRegeocodeAddress implements Parcelable {
    private String adCode;
    private String building;
    private String city;
    private String cityCode;
    private String district;
    private String formatAddress;
    private String neighborhood;
    private String province;
    private String townShip;
    private RemoteStreetNumber streetNumber;
    private List<RemoteBusinessArea> businessAreaList = new ArrayList<RemoteBusinessArea>();
    private List<RemoteCrossroad> crossroadList = new ArrayList<RemoteCrossroad>();
    private List<RemotePoiItem> poiList = new ArrayList<RemotePoiItem>();
    private List<RemoteRegeocodeRoad> roadList = new ArrayList<RemoteRegeocodeRoad>();

    /**
     * RemoteRegeocodeAddress构造函数
     */
    public RemoteRegeocodeAddress() {

    }

    /**
     * 逆地理编码返回的格式化地址。如返回北京市朝阳区方恒国际中心
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
     * 逆地理编码返回的所在省名称、直辖市的名称
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
     * 逆地理编码返回的所在城市名称。直辖市的名称参见省名称，此项为空
     * 
     * @return 结果的所在城市名称
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
     * 逆地理编码返回的所在区（县）名称
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
     * 逆地理编码返回的乡镇名称
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
     * 逆地理编码返回的社区名称
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
     * 逆地理编码返回的建筑物名称
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
     * 返回逆地理编码结果所在区（县）的编码
     * 
     * @return 结果的所在区（县）的编码
     */
    public String getAdCode() {
        return this.adCode;
    }

    /**
     * 设置所在区（县）的编码
     * 
     * @param adCode
     *            所在区（县）的编码
     */
    public void setAdCode(String adCode) {
        this.adCode = adCode;
    }

    /**
     * 返回逆地理编码结果所在城市编码。直辖市的编码也会返回
     * 
     * @return 结果的所在城市编码
     */
    public String getCityCode() {
        return this.cityCode;
    }

    /**
     * 设置所在城市编码
     * 
     * @param cityCode
     *            所在城市编码
     */
    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    /**
     * 逆地理编码返回的门牌信息
     * 
     * @return 结果的门牌信息
     */
    public RemoteStreetNumber getStreetNumber() {
        return this.streetNumber;
    }

    /**
     * 设置门牌信息
     * 
     * @param streetNumber
     *            门牌信息
     */
    public void setStreetNumber(RemoteStreetNumber streetNumber) {
        this.streetNumber = streetNumber;
    }

    /**
     * 逆地理编码返回的道路列表
     * 
     * @return 结果的道路列表
     */
    public List<RemoteRegeocodeRoad> getRoads() {
        return this.roadList;
    }

    /**
     * 设置道路列表
     * 
     * @param roadList
     *            道路列表
     */
    public void setRoads(List<RemoteRegeocodeRoad> roadList) {
        this.roadList = roadList;
    }

    /**
     * 逆地理编码返回的POI(兴趣点)列表
     * 
     * @return 结果的POI(兴趣点)列表
     */
    public List<RemotePoiItem> getPois() {
        return this.poiList;
    }

    /**
     * 设置POI（兴趣点）列表
     * 
     * @param poiList
     *            POI（兴趣点）列表
     */
    public void setPois(List<RemotePoiItem> poiList) {
        this.poiList = poiList;
    }

    /**
     * 逆地理编码返回的交叉路口列表
     * 
     * @return 结果的交叉路口列表
     */
    public List<RemoteCrossroad> getCrossroads() {
        return this.crossroadList;
    }

    /**
     * 设置交叉路口列表
     * 
     * @param crossroadList
     *            交叉路口列表
     */
    public void setCrossroads(List<RemoteCrossroad> crossroadList) {
        this.crossroadList = crossroadList;
    }

    /**
     * 返回商圈对象列表，若服务没有相应数据，则返回列表长度为0
     * 
     * @return 商圈对象列表
     */
    public List<RemoteBusinessArea> getBusinessAreas() {
        return this.businessAreaList;
    }

    /**
     * 设置商圈对象列表
     * 
     * @param businessAreaList
     *            商圈对象列表
     */
    public void setBusinessAreas(List<RemoteBusinessArea> businessAreaList) {
        this.businessAreaList = businessAreaList;
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
        dest.writeString(this.cityCode);
        dest.writeString(this.district);
        dest.writeString(this.formatAddress);
        dest.writeString(this.neighborhood);
        dest.writeString(this.province);
        dest.writeString(this.townShip);

        if (this.streetNumber != null) {
            dest.writeInt(1);
            dest.writeParcelable(streetNumber, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.businessAreaList != null) {
            dest.writeInt(1);
            dest.writeList(businessAreaList);
        } else {
            dest.writeInt(0);
        }

        if (this.crossroadList != null) {
            dest.writeInt(1);
            dest.writeList(crossroadList);
        } else {
            dest.writeInt(0);
        }

        if (this.poiList != null) {
            dest.writeInt(1);
            dest.writeList(poiList);
        } else {
            dest.writeInt(0);
        }

        if (this.roadList != null) {
            dest.writeInt(1);
            dest.writeList(roadList);
        } else {
            dest.writeInt(0);
        }

    }

    public static final Creator<RemoteRegeocodeAddress> CREATOR = new Creator<RemoteRegeocodeAddress>() {

        @Override
        public RemoteRegeocodeAddress createFromParcel(Parcel source) {
            RemoteRegeocodeAddress regeocodeAddress = new RemoteRegeocodeAddress();

            regeocodeAddress.adCode = source.readString();
            regeocodeAddress.building = source.readString();
            regeocodeAddress.city = source.readString();
            regeocodeAddress.cityCode = source.readString();
            regeocodeAddress.district = source.readString();
            regeocodeAddress.formatAddress = source.readString();
            regeocodeAddress.neighborhood = source.readString();
            regeocodeAddress.province = source.readString();
            regeocodeAddress.townShip = source.readString();

            if (source.readInt() != 0) {
                regeocodeAddress.streetNumber = source
                        .readParcelable(RemoteStreetNumber.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                regeocodeAddress.businessAreaList = source
                        .readArrayList(RemoteBusinessArea.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                regeocodeAddress.crossroadList = source
                        .readArrayList(RemoteCrossroad.class.getClassLoader());
            }

            if (source.readInt() != 0) {
                regeocodeAddress.poiList = source
                        .readArrayList(RemotePoiItem.class.getClassLoader());
            }

            if (source.readInt() != 0) {
                regeocodeAddress.roadList = source
                        .readArrayList(RemoteRegeocodeRoad.class
                                .getClassLoader());
            }

            return regeocodeAddress;
        }

        @Override
        public RemoteRegeocodeAddress[] newArray(int size) {
            return new RemoteRegeocodeAddress[size];
        }
    };
}
