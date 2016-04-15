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

package com.ingenic.iwds.smartlocation.search.core;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 定义一个POI（Point Of Interest，兴趣点）
 */
public class RemotePoiItem implements Parcelable {
    private String poiId;
    private RemoteLatLonPoint point;
    private String title;
    private String snippet;
    private String adName;
    private String cityName;
    private String provinceName;
    private String typeDesc = "";
    private String tel;
    private String adCode;
    private int distance = -1;
    private String cityCode;
    private RemoteLatLonPoint enterPoint;
    private RemoteLatLonPoint exitPoint;
    private String webSite;
    private String postCode;
    private String email;
    private boolean isGroupBuyInfo;
    private boolean isDicountInfo;
    private String direction;
    private boolean isIndoorMap;
    private String provinceCode;

    /**
     * 根据给定的参数构造一个RemotePoiItem 的新对象
     * 
     * @param poiId
     *            POI 的标识
     * 
     * @param point
     *            该POI的位置
     * 
     * @param title
     *            该POI的名称
     * 
     * @param snippet
     *            POI的地址
     */
    public RemotePoiItem(String poiId, RemoteLatLonPoint point, String title,
            String snippet) {
        this.poiId = poiId;
        this.point = point;
        this.title = title;
        this.snippet = snippet;
    }

    /**
     * RemotePoiItem构造函数
     */
    public RemotePoiItem() {

    }

    /**
     * 返回POI 的行政区划名称
     * 
     * @return POI 的行政区划名称
     */
    public String getAdName() {
        return this.adName;
    }

    /**
     * 设置POI 的行政区划名称
     * 
     * @param adName
     *            POI 的行政区划名称
     */
    public void setAdName(String adName) {
        this.adName = adName;
    }

    /**
     * 返回POI的城市名称
     * 
     * @return POI的城市名称
     */
    public String getCityName() {
        return this.cityName;
    }

    /**
     * 设置POI 的城市名称
     * 
     * @param cityName
     *            POI 的城市名称
     */
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    /**
     * 返回POI的省/自治区/直辖市/特别行政区名称
     * 
     * @return POI的省/自治区/直辖市/特别行政区名称
     */
    public String getProvinceName() {
        return this.provinceName;
    }

    /**
     * 设置POI 的省/自治区/直辖市/特别行政区名称
     * 
     * @param provinceName
     *            POI 的省/自治区/直辖市/特别行政区名称
     */
    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    /**
     * 返回POI 的类型描述
     * 
     * @return POI 的类型描述
     */
    public String getTypeDes() {
        return this.typeDesc;
    }

    /**
     * 设置POI 的类型描述
     * 
     * @param typeDesc
     *            POI 的类型描述
     */
    public void setTypeDes(String typeDesc) {
        this.typeDesc = typeDesc;
    }

    /**
     * 返回POI的电话号码
     * 
     * @return POI的电话号码
     */
    public String getTel() {
        return this.tel;
    }

    /**
     * 设置POI 的电话号码
     * 
     * @param tel
     *            POI 的电话号码
     */
    public void setTel(String tel) {
        this.tel = tel;
    }

    /**
     * 返回POI 的行政区划代码
     * 
     * @return POI 的行政区划代码
     */
    public String getAdCode() {
        return this.adCode;
    }

    /**
     * 设置POI 的行政区划代码
     * 
     * @param adCode
     *            POI 的行政区划代码
     */
    public void setAdCode(String adCode) {
        this.adCode = adCode;
    }

    /**
     * 获取 POI 距离中心点的距离
     * 
     * @return POI 到中心点的距离，单位：米。返回-1时，代表此字段无数据
     */
    public int getDistance() {
        return this.distance;
    }

    /**
     * 设置 POI 到中心点的距离
     * 
     * @param distance
     *            POI 到中心点的距离，单位：米
     */
    public void setDistance(int distance) {
        this.distance = distance;
    }

    /**
     * 返回POI的城市编码
     * 
     * @return POI的城市编码
     */
    public String getCityCode() {
        return this.cityCode;
    }

    /**
     * 设置POI的城市编码
     * 
     * @param cityCode
     *            POI的城市编码
     */
    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    /**
     * 返回POI 的id，即其唯一标识
     * 
     * @return POI的id
     */
    public String getPoiId() {
        return this.poiId;
    }

    /**
     * 设置POI 的id，即其唯一标识
     * 
     * @param poiId
     *            POI的id
     */
    public void setPoiId(String poiId) {
        this.poiId = poiId;
    }

    /**
     * 返回POI的名称
     * 
     * @return POI的名称
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * 设置POI的名称
     * 
     * @param title
     *            POI的名称
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 返回POI的地址
     * 
     * @return POI的地址
     */
    public String getSnippet() {
        return this.snippet;
    }

    /**
     * 设置POI的地址
     * 
     * @param snippet
     *            POI的地址
     */
    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    /**
     * 返回POI的经纬度坐标
     * 
     * @return POI的经纬度坐标
     */
    public RemoteLatLonPoint getLatLonPoint() {
        return this.point;
    }

    /**
     * 设置POI的经纬度坐标
     * 
     * @param point
     *            POI的经纬度坐标
     */
    public void setLatLonPoint(RemoteLatLonPoint point) {
        this.point = point;
    }

    /**
     * 返回POI入口经纬度
     * 
     * @return POI入口经纬度
     */
    public RemoteLatLonPoint getEnter() {
        return this.enterPoint;
    }

    /**
     * 设置POI入口经纬度。 POI入口一般指商场、停车场等的POI兴趣点入口
     * 
     * @param enterPoint
     *            、 POI入口经纬度
     */
    public void setEnter(RemoteLatLonPoint enterPoint) {
        this.enterPoint = enterPoint;
    }

    /**
     * 返回POI出口经纬度
     * 
     * @return POI出口经纬度
     */
    public RemoteLatLonPoint getExit() {
        return this.exitPoint;
    }

    /**
     * 设置POI出口经纬度。 POI出口一般指商场、停车场等POI兴趣点的出口
     * 
     * @param exitPoint
     *            POI出口经纬度
     */
    public void setExit(RemoteLatLonPoint exitPoint) {
        this.exitPoint = exitPoint;
    }

    /**
     * 返回POI的网址
     * 
     * @return POI的网址
     */
    public String getWebSize() {
        return this.webSite;
    }

    /**
     * 设置POI的网址
     * 
     * @param webSite
     *            POI的网址
     */
    public void setWebSite(String webSite) {
        this.webSite = webSite;
    }

    /**
     * 返回POI的邮编
     * 
     * @return POI的邮编
     */
    public String getPostCode() {
        return this.postCode;
    }

    /**
     * 设置POI的邮编
     * 
     * @param postCode
     *            POI的邮编
     */
    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    /**
     * 返回POI的电子邮件地址
     * 
     * @return POI的电子邮件地址
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * 设置POI的电子邮件地址
     * 
     * @param email
     *            POI的电子邮件地址
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 此POI是否有团购信息
     * 
     * @return POI是否有团购信息。true，存在团购信息；false，不存在
     */
    public boolean isGroupBuyInfo() {
        return this.isGroupBuyInfo;
    }

    /**
     * 设置POI是否有团购信息
     * 
     * @param groupBuyInfo
     *            是否有团购信息
     */
    public void setGroupBuyInfo(boolean groupBuyInfo) {
        this.isGroupBuyInfo = groupBuyInfo;
    }

    /**
     * 此POI是否有优惠信息
     * 
     * @return POI是否有优惠信息。true，存在优惠信息；false，不存在
     */
    public boolean isDiscountInfo() {
        return this.isDicountInfo;
    }

    /**
     * 设置POI是否有优惠信息
     * 
     * @param discountInfo
     *            是否有优惠信息
     */
    public void setDiscountInfo(boolean discountInfo) {
        this.isDicountInfo = discountInfo;
    }

    /**
     * 返回逆地理编码查询时POI坐标点相对于地理坐标点的方向。POI搜索时，此处为空。 方向为中文名称，如东、西南等
     * 
     * @return POI坐标点相对于地理坐标点的方向
     */
    public String getDirection() {
        return this.direction;
    }

    /**
     * 设置逆地理编码查询时POI坐标点相对于地理坐标点的方向。 方向为中文名称，如东、西南等
     * 
     * @param direction
     *            POI坐标点相对于地理坐标点的方向
     */
    public void setDirection(String direction) {
        this.direction = direction;
    }

    /**
     * 返回是否支持室内地图
     * 
     * @return 支持室内地图返回 true，否则返回 false
     */
    public boolean isIndoorMap() {
        return this.isIndoorMap;
    }

    /**
     * 设置是否支持室内地图
     * 
     * @param indoorMap
     *            是否支持室内地图
     */
    public void setIndoormap(boolean indoorMap) {
        this.isIndoorMap = indoorMap;
    }

    /**
     * 返回 POI 的省/自治区/直辖市/特别行政区编码
     * 
     * @return POI 的省/自治区/直辖市/特别行政区编码
     */
    public String getProvinceCode() {
        return this.provinceCode;
    }

    /**
     * 设置 POI 的省/自治区/直辖市/特别行政区编码
     * 
     * @param provinceCode
     *            POI 的省/自治区/直辖市/特别行政区编码
     */
    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (!(object instanceof RemotePoiItem))
            return false;

        RemotePoiItem other = (RemotePoiItem) object;

        if (this.poiId == null) {
            if (other.poiId != null)
                return false;
        } else if (!this.poiId.equals(other.poiId))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = prime * result
                + (this.poiId == null ? 0 : this.poiId.hashCode());

        return result;
    }

    @Override
    public String toString() {
        return this.title;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.poiId);

        if (this.point != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.point, flags);
        } else {
            dest.writeInt(0);
        }

        dest.writeString(this.title);
        dest.writeString(this.snippet);
        dest.writeString(this.adName);
        dest.writeString(this.cityName);
        dest.writeString(this.provinceName);
        dest.writeString(this.typeDesc);
        dest.writeString(this.tel);
        dest.writeString(this.adCode);
        dest.writeInt(this.distance);
        dest.writeString(this.cityCode);

        if (this.enterPoint != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.enterPoint, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.exitPoint != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.exitPoint, flags);
        } else {
            dest.writeInt(0);
        }
        dest.writeString(this.webSite);
        dest.writeString(this.postCode);
        dest.writeString(this.email);
        dest.writeBooleanArray(new boolean[] { this.isGroupBuyInfo,
                this.isDicountInfo, this.isIndoorMap });
        dest.writeString(this.direction);
        dest.writeString(this.provinceCode);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RemotePoiItem> CREATOR = new Creator<RemotePoiItem>() {

        @Override
        public RemotePoiItem createFromParcel(Parcel source) {
            String poiId = source.readString();

            RemoteLatLonPoint newPoint = null;
            if (source.readInt() != 0) {
                newPoint = source.readParcelable(RemoteLatLonPoint.class
                        .getClassLoader());
            }

            String title = source.readString();
            String snippet = source.readString();

            RemotePoiItem poiItem = new RemotePoiItem(poiId, newPoint, title,
                    snippet);

            poiItem.adName = source.readString();
            poiItem.cityName = source.readString();
            poiItem.provinceName = source.readString();
            poiItem.typeDesc = source.readString();
            poiItem.tel = source.readString();
            poiItem.adCode = source.readString();
            poiItem.distance = source.readInt();
            poiItem.cityCode = source.readString();

            if (source.readInt() != 0) {
                poiItem.enterPoint = source
                        .readParcelable(RemoteLatLonPoint.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                poiItem.exitPoint = source
                        .readParcelable(RemoteLatLonPoint.class
                                .getClassLoader());
            }

            poiItem.webSite = source.readString();
            poiItem.postCode = source.readString();
            poiItem.email = source.readString();
            boolean[] booleanArray = new boolean[3];
            source.readBooleanArray(booleanArray);
            poiItem.isGroupBuyInfo = booleanArray[0];
            poiItem.isDicountInfo = booleanArray[1];
            poiItem.isIndoorMap = booleanArray[2];
            poiItem.direction = source.readString();
            poiItem.provinceCode = source.readString();

            return poiItem;
        }

        @Override
        public RemotePoiItem[] newArray(int size) {
            return new RemotePoiItem[size];
        }

    };

}
