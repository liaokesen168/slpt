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

package com.ingenic.iwds.smartlocation.search.road;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 逆地理编码返回的结果的交叉路口对象
 */
public class RemoteCrossroad extends RemoteRoad implements Parcelable {
    private float distance;
    private String direction;
    private String firstRoadId;
    private String firstRoadName;
    private String secondRoadId;
    private String secondRoadName;

    /**
     * RemoteCrossroad构造函数
     */
    public RemoteCrossroad() {
        super();
    }

    /**
     * 返回逆地理坐标点与交叉路口的垂直距离，单位米
     * 
     * @return 逆地理坐标点与交叉路口的垂直距离
     */
    public float getDistance() {
        return this.distance;
    }

    /**
     * 设置逆地理坐标点与交叉路口的垂直距离，单位米
     * 
     * @param distance
     *            逆地理坐标点与交叉路口的垂直距离
     */
    public void setDistance(float distance) {
        this.distance = distance;
    }

    /**
     * 返回交叉路口相对逆地理坐标点的方向。方向显示为中文名称，如南、 东北
     * 
     * @return 交叉路口相对逆地理坐标点的方向
     */
    public String getDirection() {
        return this.direction;
    }

    /**
     * 设置交叉路口相对逆地理坐标点的方向。方向显示为中文名称，如南、 东北
     * 
     * @param direction
     *            交叉路口相对逆地理坐标点的方向
     */
    public void setDirection(String direction) {
        this.direction = direction;
    }

    /**
     * 返回交叉路口的第一条道路ID
     * 
     * @return 交叉路口的第一条道路ID
     */
    public String getFirstRoadId() {
        return this.firstRoadId;
    }

    /**
     * 设置交叉路口的第一条道路ID
     * 
     * @param firstRoadId
     *            交叉路口的第一条道路ID
     */
    public void setFirstRoadId(String firstRoadId) {
        this.firstRoadId = firstRoadId;
    }

    /**
     * 返回交叉路口的第一条道路名称
     * 
     * @return 交叉路口的第一条道路名称
     */
    public String getFirstRoadName() {
        return this.firstRoadName;
    }

    /**
     * 设置交叉路口的第一条道路名称
     * 
     * @param firstRoadName
     *            交叉路口的第一条道路名称
     */
    public void setFirstRoadName(String firstRoadName) {
        this.firstRoadName = firstRoadName;
    }

    /**
     * 返回交叉路口的第二条道路ID
     * 
     * @return 交叉路口的第二条道路ID
     */
    public String getSecondRoadId() {
        return this.secondRoadId;
    }

    /**
     * 设置交叉路口的第二条道路ID
     * 
     * @param secondRoadId
     *            交叉路口的第二条道路ID
     */
    public void setSecondRoadId(String secondRoadId) {
        this.secondRoadId = secondRoadId;
    }

    /**
     * 返回交叉路口的第二条道路名称
     * 
     * @return 交叉路口的第二条道路名称
     */
    public String getSecondRoadName() {
        return this.secondRoadName;
    }

    /**
     * 设置交叉路口的第二条道路名称
     * 
     * @param secondRoadName
     *            交叉路口的第二条道路名称
     */
    public void setSecondRoadName(String secondRoadName) {
        this.secondRoadName = secondRoadName;
    }

    @Override
    public String toString() {
        return "RemoteCrossroad [" + "distance=" + this.distance
                + ", direction=" + this.direction + ", firstRoadId="
                + this.firstRoadId + ", firstRoadName=" + this.firstRoadName
                + ", secondRoadId=" + this.secondRoadId + ", secondRoadName="
                + this.secondRoadName + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeFloat(this.distance);
        dest.writeString(this.direction);
        dest.writeString(this.firstRoadId);
        dest.writeString(this.firstRoadName);
        dest.writeString(this.secondRoadId);
        dest.writeString(this.secondRoadName);
    }

    public static final Creator<RemoteCrossroad> CREATOR = new Creator<RemoteCrossroad>() {

        @Override
        public RemoteCrossroad createFromParcel(Parcel source) {
            RemoteRoad road = RemoteRoad.CREATOR.createFromParcel(source);

            RemoteCrossroad crossroad = new RemoteCrossroad();

            crossroad.distance = source.readFloat();
            crossroad.direction = source.readString();
            crossroad.firstRoadId = source.readString();
            crossroad.firstRoadName = source.readString();
            crossroad.secondRoadId = source.readString();
            crossroad.secondRoadName = source.readString();
            crossroad.setCenterPoint(road.getCenterPoint());
            crossroad.setCityCode(road.getCityCode());
            crossroad.setId(road.getId());
            crossroad.setName(road.getName());
            crossroad.setRoadWidth(road.getRoadWidth());
            crossroad.setType(road.getType());

            return crossroad;
        }

        @Override
        public RemoteCrossroad[] newArray(int size) {
            return new RemoteCrossroad[size];
        }

    };

}
