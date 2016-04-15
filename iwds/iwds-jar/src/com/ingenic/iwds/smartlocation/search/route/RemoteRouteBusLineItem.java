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

import com.ingenic.iwds.smartlocation.search.busline.RemoteBusLineItem;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusStationItem;
import com.ingenic.iwds.smartlocation.search.core.RemoteLatLonPoint;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 定义了公交换乘路径规划的一个换乘段的公交信息
 */
public class RemoteRouteBusLineItem extends RemoteBusLineItem implements
        Parcelable {
    private RemoteBusStationItem arrivalBusStation;;
    private RemoteBusStationItem departureBusStation;
    private float duration;
    private int passStationNum;
    private List<RemoteBusStationItem> passStations = new ArrayList<RemoteBusStationItem>();
    private List<RemoteLatLonPoint> polyLine = new ArrayList<RemoteLatLonPoint>();

    /**
     * RemoteRouteBusLineItem构造函数
     */
    public RemoteRouteBusLineItem() {
        super();
    }

    /**
     * RemoteRouteBusLineItem构造函数
     * 
     * @param item
     *            用于序列化实现的一个换乘段的公交信息
     */
    public RemoteRouteBusLineItem(RemoteBusLineItem item) {
        super(item);
    }

    /**
     * 返回此公交换乘路段的出发站。此站有可能不是公交线路的始发车站
     * 
     * @return 此公交换乘路段的出发站
     */
    public RemoteBusStationItem getDepartureBusStation() {
        return this.departureBusStation;
    }

    /**
     * 设置此公交换乘路段的出发站
     * 
     * @param departureBusStation
     *            此公交换乘路段的出发站
     */
    public void setDepartureBusStation(RemoteBusStationItem departureBusStation) {
        this.departureBusStation = departureBusStation;
    }

    /**
     * 返回此公交换乘路段的到达站。此站有可能不是公交线路的终点站
     * 
     * @return 此公交换乘路段的到达站
     */
    public RemoteBusStationItem getArrivalBusStation() {
        return this.arrivalBusStation;
    }

    /**
     * 设置此公交换乘路段的到达站
     * 
     * @param arrivalBusStation
     *            此公交换乘路段的到达站
     */
    public void setArrivalBusStation(RemoteBusStationItem arrivalBusStation) {
        this.arrivalBusStation = arrivalBusStation;
    }

    /**
     * 返回此公交换乘路段（出发站-到达站）的坐标点集合
     * 
     * @return 此公交换乘路段（出发站-到达站）的坐标点集合
     */
    public List<RemoteLatLonPoint> getPolyline() {
        return this.polyLine;
    }

    /**
     * 设置此公交换乘路段（出发站-到达站）的坐标点集合
     * 
     * @param polyline
     *            此公交换乘路段（出发站-到达站）的坐标点集合
     */
    public void setPolyline(List<RemoteLatLonPoint> polyline) {
        this.polyLine = polyline;
    }

    /**
     * 返回此公交换乘路段经过的站点数目（除出发站、到达站）
     * 
     * @return 此公交换乘路段经过的站点数目（除出发站、到达站）
     */
    public int getPassStationNum() {
        return this.passStationNum;
    }

    /**
     * 设置此公交换乘路段经过的站点数目（除出发站、到达站）
     * 
     * @param passStationNum
     *            此公交换乘路段经过的站点数目（除出发站、到达站）
     */
    public void setPassStationNum(int passStationNum) {
        this.passStationNum = passStationNum;
    }

    /**
     * 返回此公交换乘路段经过的站点名称
     * 
     * @return 此公交换乘路段经过的站点名称
     */
    public List<RemoteBusStationItem> getPassStations() {
        return this.passStations;
    }

    /**
     * 设置此公交换乘路段经过的站点名称
     * 
     * @param passStations
     *            此公交换乘路段经过的站点名称
     */
    public void setPassStations(List<RemoteBusStationItem> passStations) {
        this.passStations = passStations;
    }

    /**
     * 返回此公交换乘路段公交预计行驶时间
     * 
     * @return 此公交换乘路段公交预计行驶时间
     */
    public float getDuration() {
        return this.duration;
    }

    /**
     * 设置此公交换乘路段公交预计行驶时间
     * 
     * @param duration
     *            此公交换乘路段公交预计行驶时间
     */
    public void setDuration(float duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (!super.equals(object))
            return false;
        if (!(object instanceof RemoteRouteBusLineItem))
            return false;

        RemoteRouteBusLineItem other = (RemoteRouteBusLineItem) object;

        if (this.arrivalBusStation == null) {
            if (other.arrivalBusStation != null)
                return false;
        } else if (!this.arrivalBusStation.equals(other.arrivalBusStation))
            return false;
        if (this.departureBusStation == null) {
            if (other.departureBusStation != null)
                return false;
        } else if (!this.departureBusStation.equals(other.departureBusStation))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();

        result = prime
                * result
                + (this.arrivalBusStation == null ? 0 : this.arrivalBusStation
                        .hashCode());
        result = prime
                * result
                + (this.departureBusStation == null ? 0
                        : this.departureBusStation.hashCode());

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(this.duration);
        dest.writeInt(this.passStationNum);

        if (this.arrivalBusStation != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.arrivalBusStation, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.departureBusStation != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.departureBusStation, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.passStations != null) {
            dest.writeInt(1);
            dest.writeList(this.passStations);
        } else {
            dest.writeInt(0);
        }

        if (this.polyLine != null) {
            dest.writeInt(1);
            dest.writeList(this.polyLine);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteRouteBusLineItem> CREATOR = new Creator<RemoteRouteBusLineItem>() {

        @Override
        public RemoteRouteBusLineItem createFromParcel(Parcel source) {
            RemoteBusLineItem busLineItem = RemoteBusLineItem.CREATOR
                    .createFromParcel(source);

            RemoteRouteBusLineItem routeBusLineItem = new RemoteRouteBusLineItem(
                    busLineItem);

            routeBusLineItem.duration = source.readFloat();
            routeBusLineItem.passStationNum = source.readInt();

            if (source.readInt() != 0) {
                routeBusLineItem.arrivalBusStation = source
                        .readParcelable(RemoteBusStationItem.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                routeBusLineItem.departureBusStation = source
                        .readParcelable(RemoteBusStationItem.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                routeBusLineItem.passStations = source
                        .readArrayList(RemoteBusStationItem.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                routeBusLineItem.polyLine = source
                        .readArrayList(RemoteLatLonPoint.class.getClassLoader());
            }

            return routeBusLineItem;
        }

        @Override
        public RemoteRouteBusLineItem[] newArray(int size) {
            return new RemoteRouteBusLineItem[size];
        }
    };
}