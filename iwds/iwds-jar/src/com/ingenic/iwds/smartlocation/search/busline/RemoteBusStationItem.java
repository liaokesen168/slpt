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

package com.ingenic.iwds.smartlocation.search.busline;

import java.util.ArrayList;
import java.util.List;

import com.ingenic.iwds.smartlocation.search.core.RemoteLatLonPoint;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 公交站点信息类
 */
public class RemoteBusStationItem implements Parcelable {
    private String adCode;
    private List<RemoteBusLineItem> busLineItems = new ArrayList<RemoteBusLineItem>();
    private String busStationId;
    private String busStationName;
    private String cityCode;
    private RemoteLatLonPoint latLonPoint;

    /**
     * RemoteBusStationItem构造函数
     */
    public RemoteBusStationItem() {

    }

    /**
     * 返回车站ID
     * 
     * @return 车站ID
     */
    public String getBusStationId() {
        return this.busStationId;
    }

    /**
     * 设置车站ID
     * 
     * @param busStationId
     *            车站ID
     */
    public void setBusStationId(String busStationId) {
        this.busStationId = busStationId;
    }

    /**
     * 返回车站名称
     * 
     * @return 车站名称
     */
    public String getBusStationName() {
        return this.busStationName;
    }

    /**
     * 设置车站名称
     * 
     * @param busStationName
     *            车站名称
     */
    public void setBusStationName(String busStationName) {
        this.busStationName = busStationName;
    }

    /**
     * 返回车站经纬度坐标
     * 
     * @return 车站经纬度坐标
     */
    public RemoteLatLonPoint getLatLonPoint() {
        return this.latLonPoint;
    }

    /**
     * 设置车站经纬度坐标
     * 
     * @param latLonPoint
     *            车站经纬度坐标
     */
    public void setLatLonPoint(RemoteLatLonPoint latLonPoint) {
        this.latLonPoint = latLonPoint;
    }

    /**
     * 返回车站城市编码
     * 
     * @return 车站城市编码
     */
    public String getCityCode() {
        return this.cityCode;
    }

    /**
     * 设置车站城市编码
     * 
     * @param cityCode
     *            车站城市编码
     */
    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getAdCode() {
        return this.adCode;
    }

    /**
     * 返回车站区域编码
     * 
     * @param adCode
     *            车站区域编码
     */
    public void setAdCode(String adCode) {
        this.adCode = adCode;
    }

    /**
     * 返回途径此公交站的公交路线。列表内的公交路线信息为基本信息
     * 
     * @return 途径此公交站的公交路线
     */
    public List<RemoteBusLineItem> getBusLineItems() {
        return this.busLineItems;
    }

    /**
     * 设置途径此公交站的公交路线
     * 
     * @param busLineItems
     *            途径此公交站的公交路线
     */
    public void setBusLineItems(List<RemoteBusLineItem> busLineItems) {
        this.busLineItems = busLineItems;
    }

    @Override
    public String toString() {
        return "RemoteStationName [" + "BusStationName=" + this.busStationName
                + ", LatLonPoint=" + this.latLonPoint.toString() + "]";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (!(object instanceof RemoteBusStationItem))
            return false;

        RemoteBusStationItem other = (RemoteBusStationItem) object;

        if (this.busStationId == null) {
            if (other.busStationId != null)
                return false;
        } else if (!this.busStationId.equals(other.busStationId))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = prime
                * result
                + (this.busStationId == null ? 0 : this.busStationId.hashCode());

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.adCode);
        dest.writeString(this.busStationId);
        dest.writeString(this.busStationName);
        dest.writeString(this.cityCode);
        if (busLineItems != null) {
            dest.writeInt(1);
            dest.writeList(this.busLineItems);
        } else {
            dest.writeInt(0);
        }

        if (this.latLonPoint != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.latLonPoint, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteBusStationItem> CREATOR = new Creator<RemoteBusStationItem>() {

        @Override
        public RemoteBusStationItem createFromParcel(Parcel source) {
            RemoteBusStationItem busStationItem = new RemoteBusStationItem();

            busStationItem.adCode = source.readString();
            busStationItem.busStationId = source.readString();
            busStationItem.busStationName = source.readString();
            busStationItem.cityCode = source.readString();

            if (source.readInt() != 0) {
                busStationItem.busLineItems = source
                        .readArrayList(RemoteBusLineItem.class.getClassLoader());
            }

            if (source.readInt() != 0) {
                busStationItem.latLonPoint = source
                        .readParcelable(RemoteLatLonPoint.class
                                .getClassLoader());
            }

            return busStationItem;
        }

        @Override
        public RemoteBusStationItem[] newArray(int size) {
            return new RemoteBusStationItem[size];
        }

    };

}
