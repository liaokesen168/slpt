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
 * 定义了路径规划的结果集
 */
public class RemoteRouteResult implements Parcelable {
    private RemoteLatLonPoint startPos;
    private RemoteLatLonPoint targetPos;

    /**
     * RemoteRouteResult构造函数
     */
    public RemoteRouteResult() {

    }

    /**
     * RemoteRouteResult构造函数
     * 
     * @param result
     *            用于序列化实现的一个路径规划的结果集
     */
    public RemoteRouteResult(RemoteRouteResult result) {
        this.startPos = result.startPos;
        this.targetPos = result.targetPos;
    }

    /**
     * 返回路径规划起点的位置
     */
    public RemoteLatLonPoint getStartPos() {
        return this.startPos;
    }

    /**
     * 设置路径规划起点的位置
     * 
     * @param startPos
     *            路径规划起点的位置
     */
    public void setStartPos(RemoteLatLonPoint startPos) {
        this.startPos = startPos;
    }

    /**
     * 返回路径规划起点的位置
     * 
     * @return 路径规划起点的位置
     */
    public RemoteLatLonPoint getTargetPos() {
        return this.targetPos;
    }

    /**
     * 设置路径规划起点的位置
     * 
     * @param targetPos
     *            路径规划起点的位置
     */
    public void setTartgetPos(RemoteLatLonPoint targetPos) {
        this.targetPos = targetPos;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (this.startPos != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.startPos, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.targetPos != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.targetPos, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteRouteResult> CREATOR = new Creator<RemoteRouteResult>() {

        @Override
        public RemoteRouteResult createFromParcel(Parcel source) {
            RemoteRouteResult routeResult = new RemoteRouteResult();

            if (source.readInt() != 0) {
                routeResult.startPos = source
                        .readParcelable(RemoteLatLonPoint.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                routeResult.targetPos = source
                        .readParcelable(RemoteLatLonPoint.class
                                .getClassLoader());
            }

            return routeResult;
        }

        @Override
        public RemoteRouteResult[] newArray(int size) {
            return new RemoteRouteResult[size];
        }

    };

}
