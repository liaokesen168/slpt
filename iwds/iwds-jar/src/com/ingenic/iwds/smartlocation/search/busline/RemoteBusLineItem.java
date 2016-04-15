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
import java.util.Date;
import java.util.List;

import com.ingenic.iwds.smartlocation.search.core.RemoteLatLonPoint;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 公交线路信息类
 */
public class RemoteBusLineItem implements Parcelable {
    private float basicPrice;
    private List<RemoteLatLonPoint> bounds = new ArrayList<RemoteLatLonPoint>();
    private String busCompany;
    private String busLineId;
    private String busLineName;
    private String busLineType;
    private List<RemoteBusStationItem> busStations = new ArrayList<RemoteBusStationItem>();
    private String cityCode;
    private List<RemoteLatLonPoint> directionCoordinates = new ArrayList<RemoteLatLonPoint>();
    private float distance;
    // private Date firstBusTime;
    // private Date lastBusTime;
    private String originatingStation;
    private String terminalStation;
    private float totalPrice;

    /**
     * RemoteBusLineItem的构造方法
     */
    public RemoteBusLineItem() {

    }

    /**
     * RemoteBusLineItem的构造方法
     * 
     * @param item
     *            需要的源公交线路信息
     */
    public RemoteBusLineItem(RemoteBusLineItem item) {
        this.basicPrice = item.basicPrice;
        this.bounds = item.bounds;
        this.busCompany = item.busCompany;
        this.busLineId = item.busLineId;
        this.busLineName = item.busLineName;
        this.busLineType = item.busLineType;
        this.busStations = item.busStations;
        this.cityCode = item.cityCode;
        this.directionCoordinates = item.directionCoordinates;
        this.distance = item.distance;
        // this.firstBusTime = item.firstBusTime;
        // this.lastBusTime = item.lastBusTime;
        this.originatingStation = item.originatingStation;
        this.terminalStation = item.terminalStation;
        this.totalPrice = item.totalPrice;
    }

    /**
     * 返回公交线路全程里程，单位千米
     */
    public float getDistance() {
        return this.distance;
    }

    /**
     * 设置公交线路全程里程，单位千米
     * 
     * @param distance
     *            公交线路全程里程数
     */
    public void setDistance(float distance) {
        this.distance = distance;
    }

    /**
     * 返回公交线路的名称，包含线路编号和文字名称、类型、首发站、终点站
     * 
     * @return 公交线路的名称，包含线路编号和文字名称、类型、首发站、终点站
     */
    public String getBusLineName() {
        return this.busLineName;
    }

    /**
     * 设置公交线路的名称
     * 
     * @param busLineName
     *            公交线路的名称
     */
    public void setBusLineName(String busLineName) {
        this.busLineName = busLineName;
    }

    /**
     * 返回公交线路的类型，类型为中文名称。 公交线路的类型有普通公交、地铁、 轻轨、有轨电车、无轨电车、旅游专线、机场大巴、社区专车、磁悬浮列车、
     * 轮渡、索道交通、其他
     * 
     * @return 公交线路的类型
     */
    public String getBusLineType() {
        return this.busLineType;
    }

    /**
     * 设置公交线路的类型
     * 
     * @param busLineType
     *            公交线路的类型
     */
    public void setBusLineType(String busLineType) {
        this.busLineType = busLineType;
    }

    /**
     * 返回公交线路的城市编码
     * 
     * @return 公交线路的城市编码
     */
    public String getCityCode() {
        return this.cityCode;
    }

    /**
     * 设置公交线路的城市编码
     * 
     * @param cityCode
     *            公交线路的城市编码
     */
    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    /**
     * 返回公交线路外包矩形的左下与右上顶点坐标
     * 
     * @return 公交线路外包矩形的左下右上顶点坐标
     */
    public List<RemoteLatLonPoint> getBounds() {
        return this.bounds;
    }

    /**
     * 设置公交线路外包矩形的左下与右上顶点坐标
     * 
     * @param bounds
     *            公交线路外包矩形的左下右上顶点坐标
     */
    public void setBounds(List<RemoteLatLonPoint> bounds) {
        this.bounds = bounds;
    }

    /**
     * 设置公交线路的沿途坐标，包含首发站和终点站坐标
     * 
     * @param directionsCoordinates
     *            公交线路的沿途坐标
     */
    public void setDirectionsCoordinates(
            List<RemoteLatLonPoint> directionsCoordinates) {
        this.directionCoordinates = directionsCoordinates;
    }

    /**
     * 返回公交线路的沿途坐标，包含首发站和终点站坐标
     * 
     * @return 公交线路的沿途坐标
     */
    public List<RemoteLatLonPoint> getDirectionsCoordinates() {
        return this.directionCoordinates;
    }

    /**
     * 返回公交线路的唯一ID
     * 
     * @return 公交线路的唯一ID
     */
    public String getBusLineId() {
        return this.busLineId;
    }

    /**
     * 设置公交线路的唯一ID
     * 
     * @param busLineId
     *            公交线路的唯一ID
     */
    public void setBusLineId(String busLineId) {
        this.busLineId = busLineId;
    }

    /**
     * 返回公交线路的始发站名称
     * 
     * @return 公交线路的始发站名称
     */
    public String getOriginatingStation() {
        return this.originatingStation;
    }

    /**
     * 设置公交线路的始发站名称
     * 
     * @param originatingStation
     *            公交线路的始发站名称
     */
    public void setOriginatingStation(String originatingStation) {
        this.originatingStation = originatingStation;
    }

    /**
     * 返回公交线路的终点站名称
     * 
     * @return 公交线路的终点站名称
     */
    public String getTerminalStation() {
        return this.terminalStation;
    }

    /**
     * 设置公交线路的终点站名称
     * 
     * @param terminalStation
     *            公交线路的终点站名称
     */
    public void setTerminalStation(String terminalStation) {
        this.terminalStation = terminalStation;
    }

    // public Date getFirstBusTime() {
    // if (this.firstBusTime == null)
    // return null;
    //
    // return (Date) this.firstBusTime.clone();
    // }

    // public void setFirstBusTime(Date firstBusTime) {
    // if (firstBusTime == null) {
    // this.firstBusTime = null;
    // } else {
    // this.firstBusTime = (Date) firstBusTime.clone();
    // }
    // }
    //
    // public Date getLastBusTime() {
    // if (this.lastBusTime == null)
    // return null;
    // return (Date) this.lastBusTime.clone();
    // }
    //
    // public void setLastBusTime(Date lastBusTime) {
    // if (lastBusTime == null)
    // this.lastBusTime = null;
    // else
    // this.lastBusTime = (Date) lastBusTime.clone();
    // }

    /**
     * 返回公交线路所属的公交公司
     * 
     * @return 公交线路所属的公交公司
     */
    public String getBusCompany() {
        return this.busCompany;
    }

    /**
     * 设置公交线路所属的公交公司
     * 
     * @param busCompany
     *            公交线路所属的公交公司
     */
    public void setBusCompany(String busCompany) {
        this.busCompany = busCompany;
    }

    /**
     * 返回公交线路的起步价，单位元
     * 
     * @return 公交线路的起步价
     */
    public float getBasicPrice() {
        return this.basicPrice;
    }

    /**
     * 设置公交线路的起步价，单位元
     * 
     * @param basicPrice
     *            公交线路的起步价
     */
    public void setBasicPrice(float basicPrice) {
        this.basicPrice = basicPrice;
    }

    /**
     * 返回公交线路的全程票价，单位元
     * 
     * @return 公交线路的全程票价
     */
    public float getTotalPrice() {
        return this.totalPrice;
    }

    /**
     * 设置公交线路的全程票价，单位元
     * 
     * @param totalPrice
     *            公交线路的全程票价
     */
    public void setTotalPrice(float totalPrice) {
        this.totalPrice = totalPrice;
    }

    /**
     * 返回公交线路的站点列表。此列表返回的公交站数据有ID、站名、经纬度、序号
     * 
     * @return 公交线路的站点列表
     */
    public List<RemoteBusStationItem> getBusStations() {
        return this.busStations;
    }

    /**
     * 设置公交线路的站点列表
     * 
     * @param busStations
     *            公交线路的站点列表
     */
    public void setBusStations(List<RemoteBusStationItem> busStations) {
        this.busStations = busStations;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (!(object instanceof RemoteBusLineItem))
            return false;

        RemoteBusLineItem other = (RemoteBusLineItem) object;

        if (this.busLineId == null) {
            if (other.busLineId != null)
                return false;
        } else if (!this.busLineId.equals(other.busLineId))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = prime * result
                + (this.busLineId == null ? 0 : this.busLineId.hashCode());

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.basicPrice);
        dest.writeFloat(this.totalPrice);
        dest.writeString(this.busCompany);
        dest.writeString(this.busLineId);
        dest.writeString(this.busLineName);
        dest.writeString(this.busLineType);
        dest.writeString(this.cityCode);
        dest.writeFloat(this.distance);
        dest.writeString(this.originatingStation);
        dest.writeString(this.terminalStation);

        if (this.bounds != null) {
            dest.writeInt(1);
            dest.writeList(this.bounds);
        } else {
            dest.writeInt(0);
        }

        if (this.busStations != null) {
            dest.writeInt(1);
            dest.writeList(this.busStations);
        } else {
            dest.writeInt(0);
        }

        if (this.directionCoordinates != null) {
            dest.writeInt(1);
            dest.writeList(this.directionCoordinates);
        } else {
            dest.writeInt(0);
        }

        // if (this.firstBusTime != null) {
        // dest.writeInt(1);
        // dest.writeSerializable(this.firstBusTime);
        // } else {
        // dest.writeInt(0);
        // }

        // if (this.lastBusTime != null) {
        // dest.writeInt(1);
        // dest.writeSerializable(this.lastBusTime);
        // } else {
        // dest.writeInt(0);
        // }
    }

    public static final Creator<RemoteBusLineItem> CREATOR = new Creator<RemoteBusLineItem>() {

        @Override
        public RemoteBusLineItem createFromParcel(Parcel source) {
            RemoteBusLineItem busLineItem = new RemoteBusLineItem();

            busLineItem.basicPrice = source.readFloat();
            busLineItem.totalPrice = source.readFloat();
            busLineItem.busCompany = source.readString();
            busLineItem.busLineId = source.readString();
            busLineItem.busLineName = source.readString();
            busLineItem.busLineType = source.readString();
            busLineItem.cityCode = source.readString();
            busLineItem.distance = source.readFloat();
            busLineItem.originatingStation = source.readString();
            busLineItem.terminalStation = source.readString();

            if (source.readInt() != 0) {
                busLineItem.bounds = source
                        .readArrayList(RemoteLatLonPoint.class.getClassLoader());
            }

            if (source.readInt() != 0) {
                busLineItem.busStations = source
                        .readArrayList(RemoteBusStationItem.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                busLineItem.directionCoordinates = source
                        .readArrayList(RemoteLatLonPoint.class.getClassLoader());
            }

            // if (source.readInt() != 0) {
            // busLineItem.firstBusTime = (Date) source.readSerializable();
            // }

            // if (source.readInt() != 0) {
            // busLineItem.lastBusTime = (Date) source.readSerializable();
            // }

            return busLineItem;
        }

        @Override
        public RemoteBusLineItem[] newArray(int size) {
            return new RemoteBusLineItem[size];
        }
    };

}
