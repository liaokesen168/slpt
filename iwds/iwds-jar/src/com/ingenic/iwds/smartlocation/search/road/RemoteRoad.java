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

import com.ingenic.iwds.smartlocation.search.core.RemoteLatLonPoint;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 定义道路Road的类
 */
public class RemoteRoad implements Parcelable {
    private RemoteLatLonPoint centerPoint;
    private String cityCode;
    private String id;
    private String name;
    private float roadWidth;
    private String type;

    /**
     * RemoteRoad构造函数
     */
    public RemoteRoad() {

    }

    /**
     * RemoteRoad构造函数
     * 
     * @param id
     *            道路的唯一ID
     * 
     * @param name
     *            道路的名称
     */
    public RemoteRoad(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * 设置道路ID
     * 
     * @param id
     *            道路ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 设置道路名称
     * 
     * @param name
     *            道路名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 返回结果的城市编码
     * 
     * @return 结果的城市编码
     */
    public String getCityCode() {
        return this.cityCode;
    }

    /**
     * 设置城市编码
     * 
     * @param cityCode
     *            城市编码
     */
    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    /**
     * 返回结果的道路宽度，单位米
     * 
     * @return 结果的道路宽度
     */
    public float getRoadWidth() {
        return this.roadWidth;
    }

    /**
     * 设置道路宽度，单位米
     * 
     * @param roadWidth
     *            道路宽度
     */
    public void setRoadWidth(float roadWidth) {
        this.roadWidth = roadWidth;
    }

    /**
     * 返回结果的道路类型。 道路的类型有41000（高速公路）、42000（国道）、43000（城市环路/城市快速路）、
     * 51000（省道）、44000（主要道路（城市主干道））、45000（次要道路（城市次干道））、
     * 52000（县道）、53000（乡村道路）、54000（区县内部道路）、47000（一般道路）、49（非导航道路）
     * 
     * @return 结果的道路类型
     */
    public String getType() {
        return this.type;
    }

    /**
     * 设置道路类型。 道路的类型有41000（高速公路）、42000（国道）、43000（城市环路/城市快速路）、
     * 51000（省道）、44000（主要道路（城市主干道））、45000（次要道路（城市次干道））、
     * 52000（县道）、53000（乡村道路）、54000（区县内部道路）、47000（一般道路）、49（非导航道路）
     * 
     * @param type
     *            道路类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 返回结果的道路中心点坐标
     * 
     * @return 结果的道路中心点坐标
     */
    public RemoteLatLonPoint getCenterPoint() {
        return this.centerPoint;
    }

    /**
     * 设置道路中心点坐标
     * 
     * @param centerPoint
     *            道路中心点坐标
     */
    public void setCenterPoint(RemoteLatLonPoint centerPoint) {
        this.centerPoint = centerPoint;
    }

    /**
     * 返回结果的道路ID
     * 
     * @return 道路ID
     */
    public String getId() {
        return this.id;
    }

    /**
     * 返回结果的道路名称
     * 
     * @return 道路名称
     */
    public String getName() {
        return this.name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeFloat(this.roadWidth);
        dest.writeString(this.cityCode);
        dest.writeString(this.type);
        if (this.centerPoint != null) {
            dest.writeInt(1);
            dest.writeParcelable(centerPoint, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteRoad> CREATOR = new Creator<RemoteRoad>() {
        @Override
        public RemoteRoad createFromParcel(Parcel source) {
            String id = source.readString();
            String name = source.readString();

            RemoteRoad road = new RemoteRoad(id, name);

            road.roadWidth = source.readFloat();
            road.cityCode = source.readString();
            road.type = source.readString();
            if (source.readInt() != 0) {
                road.centerPoint = source
                        .readParcelable(RemoteLatLonPoint.class
                                .getClassLoader());
            }

            return road;
        }

        @Override
        public RemoteRoad[] newArray(int size) {
            return new RemoteRoad[size];
        }
    };
}
