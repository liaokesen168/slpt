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
 * 此类定义了公交路径规划查询路径的起终点、计算路径的模式、城市和是否计算夜班车
 */
public class RemoteBusRouteQuery implements Parcelable, Cloneable {
    private RemoteFromAndTo fromAndTo;
    private int mode;
    private int nightFlag;
    private String city;

    /**
     * RemoteBusRouteQuery构造函数
     */
    public RemoteBusRouteQuery() {

    }

    /**
     * RemoteBusRouteQuery构造函数
     * 
     * @param fromAndTo
     *            路径的起终点
     * 
     * @param mode
     *            计算路径的模式。可选，默认为最快捷
     * 
     * @param city
     *            城市名称/城市区号/电话区号。此项不能为空
     * 
     * @param nightFlag
     *            是否计算夜班车，默认为不计算。0：不计算，1：计算。可选
     */
    public RemoteBusRouteQuery(RemoteFromAndTo fromAndTo, int mode,
            String city, int nightFlag) {
        this.fromAndTo = fromAndTo;
        this.mode = mode;
        this.city = city;
        this.nightFlag = nightFlag;
    }

    /**
     * 返回查询路径的起终点
     * 
     * @return 查询路径的起终点
     */
    public RemoteFromAndTo getFromAndTo() {
        return this.fromAndTo;
    }

    /**
     * 设置查询路径的起终点
     * 
     * @param fromAndTo
     *            查询路径的起终点
     */
    public void setFromAndTo(RemoteFromAndTo fromAndTo) {
        this.fromAndTo = fromAndTo;
    }

    /**
     * 返回计算路径的模式
     * 
     * @return 计算路径的模式
     */
    public int getMode() {
        return this.mode;
    }

    /**
     * 设置计算路径的模式
     * 
     * @param mode
     *            计算路径的模式
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * 返回查询的城市
     * 
     * @return 查询的城市
     */
    public String getCity() {
        return this.city;
    }

    /**
     * 设置查询的城市
     * 
     * @param city
     *            查询的城市
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * 返回是否计算夜班车
     * 
     * @return 是否计算夜班车
     */
    public int getNightFlag() {
        return this.nightFlag;
    }

    /**
     * 设置是否计算夜班车
     * 
     * @param nightFlag
     *            是否计算夜班车
     */
    public void setNightFlag(int nightFlag) {
        this.nightFlag = nightFlag;
    }

    @Override
    protected Object clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return new RemoteBusRouteQuery(this.fromAndTo, this.mode, this.city,
                this.nightFlag);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (!(object instanceof RemoteBusRouteQuery))
            return false;

        RemoteBusRouteQuery other = (RemoteBusRouteQuery) object;

        if (this.mode != other.mode)
            return false;
        if (this.nightFlag != other.nightFlag)
            return false;
        if (this.city == null) {
            if (other.city != null)
                return false;
        } else if (!this.city.equals(other.city))
            return false;
        if (this.fromAndTo == null) {
            if (other.fromAndTo != null)
                return false;
        } else if (!this.fromAndTo.equals(other.fromAndTo))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = prime * result
                + (this.city == null ? 0 : this.city.hashCode());
        result = prime * result
                + (this.fromAndTo == null ? 0 : this.fromAndTo.hashCode());
        result = prime * result + this.mode;
        result = prime * result + this.nightFlag;

        return result;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mode);
        dest.writeInt(this.nightFlag);
        dest.writeString(this.city);

        if (this.fromAndTo != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.fromAndTo, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteBusRouteQuery> CREATOR = new Creator<RemoteBusRouteQuery>() {

        @Override
        public RemoteBusRouteQuery createFromParcel(Parcel source) {
            RemoteBusRouteQuery busRouteQuery = new RemoteBusRouteQuery();

            busRouteQuery.mode = source.readInt();
            busRouteQuery.nightFlag = source.readInt();
            busRouteQuery.city = source.readString();

            if (source.readInt() != 0) {
                busRouteQuery.fromAndTo = source
                        .readParcelable(RemoteFromAndTo.class.getClassLoader());
            }

            return busRouteQuery;
        }

        @Override
        public RemoteBusRouteQuery[] newArray(int size) {
            return new RemoteBusRouteQuery[size];
        }
    };

}
