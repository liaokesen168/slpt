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

import com.ingenic.iwds.smartlocation.search.core.RemoteLatLonPoint;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 此类定义了驾车路径查询规划
 */
public class RemoteDriveRouteQuery implements Parcelable, Cloneable {
    private RemoteFromAndTo fromAndTo;
    private int mode;
    private String avoidRoad;
    private List<List<RemoteLatLonPoint>> avoidPolygons = new ArrayList<List<RemoteLatLonPoint>>();
    private List<RemoteLatLonPoint> passedByPoints = new ArrayList<RemoteLatLonPoint>();

    /**
     * RemoteDriveRouteQuery构造函数
     */
    public RemoteDriveRouteQuery() {

    }

    /**
     * RemoteDriveRouteQuery构造函数
     * 
     * @param fromAndTo
     *            路径的起点终点
     * 
     * @param mode
     *            计算路径的模式。可选，默认为速度优先
     * 
     * @param passedByPoints
     *            途经点，可选。最多支持16个途经点
     * 
     * @param avoidPolygons
     *            避让区域，可选。区域避让，支持32个避让区域，每个区域最多可有16个顶点。如果是四边形则有4个坐标点，
     *            如果是五边形则有5个坐标点
     * 
     * @param avoidRoad
     *            避让道路名称，可选。目前只支持一条避让道路
     * 
     */
    public RemoteDriveRouteQuery(RemoteFromAndTo fromAndTo, int mode,
            List<RemoteLatLonPoint> passedByPoints,
            List<List<RemoteLatLonPoint>> avoidPolygons, String avoidRoad) {
        this.fromAndTo = fromAndTo;
        this.mode = mode;
        this.avoidPolygons = avoidPolygons;
        this.passedByPoints = passedByPoints;
        this.avoidRoad = avoidRoad;
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
     * 返回设定查询的避让道路
     * 
     * @return 设定查询的避让道路
     */
    public String getAvoidRoad() {
        return this.avoidRoad;
    }

    /**
     * 设置设定查询的避让道路
     * 
     * @param avoidRoad
     *            设定查询的避让道路
     */
    public void setAvoidRoad(String avoidRoad) {
        this.avoidRoad = avoidRoad;
    }

    /**
     * 返回设定查询的途经点
     * 
     * @return 设定查询的途经点
     */
    public List<RemoteLatLonPoint> getPassedByPoints() {
        return this.passedByPoints;
    }

    /**
     * 设置途经点
     * 
     * @param passedByPoints
     *            途经点
     */
    public void setPassedByPoint(List<RemoteLatLonPoint> passedByPoints) {
        this.passedByPoints = passedByPoints;
    }

    /**
     * 返回设定查询的避让区域
     * 
     * @return 设定查询的避让区域
     */
    public List<List<RemoteLatLonPoint>> getAvoidpolygons() {
        return this.avoidPolygons;
    }

    /**
     * 设置避让区域
     * 
     * @param avoidPolygons
     *            避让区域
     */
    public void setAvoidpolygons(List<List<RemoteLatLonPoint>> avoidPolygons) {
        this.avoidPolygons = avoidPolygons;
    }

    /**
     * 返回String类型的途径点
     * 
     * @return 字符串类型的途径点
     */
    public String getPassedPointStr() {
        StringBuffer strBuf = new StringBuffer();
        if ((this.passedByPoints == null) || (this.passedByPoints.isEmpty()))
            return null;

        for (int i = 0; i < this.passedByPoints.size(); i++) {
            RemoteLatLonPoint point = this.passedByPoints.get(i);
            strBuf.append(point.getLongitude());
            strBuf.append(",");
            strBuf.append(point.getLatitude());
            if (i < this.passedByPoints.size() - 1)
                strBuf.append(";");
        }

        return strBuf.toString();
    }

    /**
     * 返回是否设置了途径点
     * 
     * @return 是否设置了途径点
     */
    public boolean hasPassPoint() {
        return this.getPassedPointStr() != null ? true : false;
    }

    /**
     * 返回String类型的设定查询的避让区域
     * 
     * @return String类型的设定查询的避让区域
     */
    public String getAvoidpolygonsStr() {
        StringBuffer strBuf = new StringBuffer();
        if ((this.avoidPolygons == null) || (this.avoidPolygons.isEmpty()))
            return null;

        for (int i = 0; i < this.avoidPolygons.size(); i++) {
            List<RemoteLatLonPoint> pointList = this.avoidPolygons.get(i);
            for (int j = 0; j < pointList.size(); j++) {
                RemoteLatLonPoint point = pointList.get(i);
                strBuf.append(point.getLongitude());
                strBuf.append(",");
                strBuf.append(point.getLatitude());
                if (j < pointList.size() - 1)
                    strBuf.append(";");
            }

            if (i < this.avoidPolygons.size() - 1)
                strBuf.append("|");
        }

        return strBuf.toString();
    }

    /**
     * 返回是否设置了避让区域
     * 
     * @return 是否设置了避让区域
     */
    public boolean hasAvoidpolygons() {
        return this.getAvoidpolygonsStr() != null ? true : false;
    }

    /**
     * 返回是否设置了避让道路
     * 
     * @return 是否设置了避让道路
     */
    public boolean hasAvoidRoad() {
        return this.getAvoidRoad() != null ? true : false;
    }

    @Override
    protected Object clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return new RemoteDriveRouteQuery(this.fromAndTo, this.mode,
                this.passedByPoints, this.avoidPolygons, this.avoidRoad);
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
        if (!(object instanceof RemoteDriveRouteQuery))
            return false;

        RemoteDriveRouteQuery other = (RemoteDriveRouteQuery) object;

        if (this.mode != other.mode)
            return false;
        if (this.avoidRoad == null) {
            if (other.avoidRoad != null)
                return false;
        } else if (!this.avoidRoad.equals(other.avoidRoad))
            return false;
        if (this.fromAndTo == null) {
            if (other.fromAndTo != null)
                return false;
        } else if (!this.fromAndTo.equals(other.fromAndTo))
            return false;
        if (this.passedByPoints == null) {
            if (other.passedByPoints != null)
                return false;
        } else if (!this.passedByPoints.equals(other.passedByPoints))
            return false;
        if (this.avoidPolygons == null) {
            if (other.avoidPolygons != null)
                return false;
        } else if (!this.avoidPolygons.equals(other.avoidPolygons))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = prime + result + this.mode;
        result = prime * result
                + (this.avoidRoad == null ? 0 : this.avoidRoad.hashCode());
        result = prime * result
                + (this.fromAndTo == null ? 0 : this.fromAndTo.hashCode());
        result = prime * result + this.mode;
        result = prime
                * result
                + (this.avoidPolygons == null ? 0 : this.avoidPolygons
                        .hashCode());
        result = prime
                * result
                + (this.passedByPoints == null ? 0 : this.passedByPoints
                        .hashCode());
        return result;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mode);
        dest.writeString(this.avoidRoad);

        if (this.fromAndTo != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.fromAndTo, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.passedByPoints != null) {
            dest.writeInt(1);
            dest.writeList(this.passedByPoints);
        } else {
            dest.writeInt(0);
        }

        if (this.avoidPolygons != null) {
            dest.writeInt(this.avoidPolygons.size());
            for (List<RemoteLatLonPoint> list : this.avoidPolygons)
                dest.writeList(list);
        }
    }

    public static final Creator<RemoteDriveRouteQuery> CREATOR = new Creator<RemoteDriveRouteQuery>() {

        @Override
        public RemoteDriveRouteQuery createFromParcel(Parcel source) {
            RemoteDriveRouteQuery driveRouteQuery = new RemoteDriveRouteQuery();

            driveRouteQuery.mode = source.readInt();
            driveRouteQuery.avoidRoad = source.readString();

            if (source.readInt() != 0) {
                driveRouteQuery.fromAndTo = source
                        .readParcelable(RemoteFromAndTo.class.getClassLoader());
            }

            if (source.readInt() != 0) {
                driveRouteQuery.passedByPoints = source
                        .readArrayList(RemoteLatLonPoint.class.getClassLoader());
            }

            int size = source.readInt();
            if (size != 0) {
                for (int i = 0; i < size; i++)
                    driveRouteQuery.avoidPolygons.add(source
                            .readArrayList(RemoteLatLonPoint.class
                                    .getClassLoader()));
            }

            return driveRouteQuery;
        }

        @Override
        public RemoteDriveRouteQuery[] newArray(int size) {
            return new RemoteDriveRouteQuery[size];
        }
    };

}
