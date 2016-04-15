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
 * 定义了公交换乘路径规划的一个换乘段的步行信息
 */
public class RemoteRouteBusWalkItem extends RemoteWalkPath implements
        Parcelable {
    private RemoteLatLonPoint destination;
    private RemoteLatLonPoint origin;

    /**
     * RemoteRouteBusWalkItem构造函数
     */
    public RemoteRouteBusWalkItem() {
        super();
    }

    /**
     * RemoteRouteBusWalkItem构造函数
     * 
     * @param path
     *            用于序列化实现的一个换乘段的步行信息
     */
    public RemoteRouteBusWalkItem(RemoteWalkPath path) {
        super(path);
    }

    /**
     * 返回此路段步行导航信息的起点坐标
     * 
     * @return 此路段步行导航信息的起点坐标
     */
    public RemoteLatLonPoint getOrigin() {
        return this.origin;
    }

    /**
     * 设置此路段步行导航信息的起点坐标
     * 
     * @param origin
     *            此路段步行导航信息的起点坐标
     */
    public void setOrigin(RemoteLatLonPoint origin) {
        this.origin = origin;
    }

    /**
     * 返回此路段步行导航信息的终点坐标
     * 
     * @return 此路段步行导航信息的终点坐标
     */
    public RemoteLatLonPoint getDestination() {
        return this.destination;
    }

    /**
     * 设置此路段步行导航信息的终点坐标
     * 
     * @param destination
     *            此路段步行导航信息的终点坐标
     */
    public void setDestination(RemoteLatLonPoint destination) {
        this.destination = destination;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        if (this.origin != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.origin, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.destination != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.destination, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteRouteBusWalkItem> CREATOR = new Creator<RemoteRouteBusWalkItem>() {

        @Override
        public RemoteRouteBusWalkItem createFromParcel(Parcel source) {
            RemoteWalkPath walkPath = RemoteWalkPath.CREATOR
                    .createFromParcel(source);

            RemoteRouteBusWalkItem routeBusWalkItem = new RemoteRouteBusWalkItem(
                    walkPath);

            if (source.readInt() != 0) {
                routeBusWalkItem.origin = source
                        .readParcelable(RemoteLatLonPoint.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                routeBusWalkItem.destination = source
                        .readParcelable(RemoteLatLonPoint.class
                                .getClassLoader());
            }

            return routeBusWalkItem;

        }

        @Override
        public RemoteRouteBusWalkItem[] newArray(int size) {
            return new RemoteRouteBusWalkItem[size];
        }

    };

}
