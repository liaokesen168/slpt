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
 * 逆地理编码返回结果的道路对象
 */
public class RemoteRegeocodeRoad implements Parcelable {
    private String direction;
    private float distance;
    private String id;
    private RemoteLatLonPoint point;
    private String name;

    /**
     * RemoteRegeocodeRoad构造函数
     */
    public RemoteRegeocodeRoad() {

    }

    /**
     * 返回道路对象中的道路ID
     * 
     * @return 结果的道路ID
     */
    public String getId() {
        return this.id;
    }

    /**
     * 设置道路对象中的道路ID
     * 
     * @param id
     *            道路ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 返回道路对象中的道路名称
     * 
     * @return 结果的道路名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置道路对象中的道路名称
     * 
     * @param name
     *            道路名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 返回道路对象中道路中心点相对地理坐标点的方向。方向显示为英文名称，如South、 NorthEast
     * 
     * @return 道路中心点相对地理坐标点的方向
     */
    public String getDirection() {
        return this.direction;
    }

    /**
     * 设置道路对象中道路中心点相对地理坐标点的方向
     * 
     * @param direction
     *            道路对象中道路中心点相对地理坐标点的方向
     */
    public void setDirection(String direction) {
        this.direction = direction;
    }

    /**
     * 返回道路中心点的经纬度坐标
     * 
     * @return 道路中心点的经纬度坐标
     */
    public RemoteLatLonPoint getLatLonPoint() {
        return this.point;
    }

    /**
     * 设置道路中心点的经纬度坐标
     * 
     * @param point
     *            道路中心点的经纬度坐标
     */
    public void setLatLonPoint(RemoteLatLonPoint point) {
        this.point = point;
    }

    /**
     * 返回道路对象中地理坐标点与道路的垂直距离，单位米
     * 
     * @return 地理坐标点与道路的垂直距离
     */
    public float getDistance() {
        return this.distance;
    }

    /**
     * 设置道路对象中地理坐标点与道路的垂直距离，单位米
     * 
     * @param distance
     *            地理坐标点与道路的垂直距离
     */
    public void setDistance(float distance) {
        this.distance = distance;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.direction);
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeFloat(this.distance);
        if (this.point != null) {
            dest.writeInt(1);
            dest.writeParcelable(point, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteRegeocodeRoad> CREATOR = new Creator<RemoteRegeocodeRoad>() {
        @Override
        public RemoteRegeocodeRoad createFromParcel(Parcel source) {
            RemoteRegeocodeRoad regeocodeRoad = new RemoteRegeocodeRoad();

            regeocodeRoad.direction = source.readString();
            regeocodeRoad.id = source.readString();
            regeocodeRoad.name = source.readString();
            regeocodeRoad.distance = source.readFloat();
            if (source.readInt() != 0) {
                regeocodeRoad.point = source
                        .readParcelable(RemoteRegeocodeRoad.class
                                .getClassLoader());
            }

            return regeocodeRoad;
        }

        @Override
        public RemoteRegeocodeRoad[] newArray(int size) {
            return new RemoteRegeocodeRoad[size];
        }

    };

}
