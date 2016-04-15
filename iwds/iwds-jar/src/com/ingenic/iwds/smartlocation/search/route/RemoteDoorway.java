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

import com.ingenic.iwds.smartlocation.search.core.RemoteLatLonPoint;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 定义了公交换乘路径规划的一个换乘点的出入口信息。换乘点目前指换乘地铁
 */
public class RemoteDoorway implements Parcelable {
    private String name;
    private RemoteLatLonPoint latLonPoint;

    /**
     * RemoteDoorway构造函数
     */
    public RemoteDoorway() {

    }

    /**
     * 返回公交换乘中换乘点的出（入）口名称
     * 
     * @return 公交换乘中换乘点的出（入）口名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置公交换乘中换乘点的出（入）口名称
     * 
     * @param name
     *            公交换乘中换乘点的出（入）口名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 返回公交换乘中换乘点的出（入）口坐标
     * 
     * @return 公交换乘中换乘点的出（入）口坐标
     */
    public RemoteLatLonPoint getLatLonPoint() {
        return this.latLonPoint;
    }

    /**
     * 设置公交换乘中换乘点的出（入）口坐标
     * 
     * @param latLonPoint
     *            公交换乘中换乘点的出（入）口坐标
     */
    public void setLatLonPoint(RemoteLatLonPoint latLonPoint) {
        this.latLonPoint = latLonPoint;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        if (this.latLonPoint != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.latLonPoint, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteDoorway> CREATOR = new Creator<RemoteDoorway>() {

        @Override
        public RemoteDoorway createFromParcel(Parcel source) {
            RemoteDoorway doorway = new RemoteDoorway();

            doorway.name = source.readString();
            if (source.readInt() != 0) {
                doorway.latLonPoint = source
                        .readParcelable(RemoteLatLonPoint.class
                                .getClassLoader());
            }

            return doorway;
        }

        @Override
        public RemoteDoorway[] newArray(int size) {
            return new RemoteDoorway[size];
        }

    };

}
