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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 定义了公交路径规划的一个路段。 路段最多包含一段步行信息和公交导航信息。可能出现路段中没有步行信息的情况
 */
public class RemoteBusStep implements Parcelable {
    private List<RemoteRouteBusLineItem> busLineList = new ArrayList<RemoteRouteBusLineItem>();
    private RemoteDoorway entrance;
    private RemoteDoorway exit;
    private RemoteRouteBusWalkItem walk;

    /**
     * RemoteBusStep构造函数
     */
    public RemoteBusStep() {

    }

    /**
     * 返回此路段的步行信息
     * 
     * @return 此路段的步行信息
     */
    public RemoteRouteBusWalkItem getWalk() {
        return this.walk;
    }

    /**
     * 设置此路段的步行信息
     * 
     * @param walk
     *            此路段的步行信息
     */
    public void setWalk(RemoteRouteBusWalkItem walk) {
        this.walk = walk;
    }

    /**
     * 返回此路段的公交导航信息。目前只返回一条公交线路信息
     * 
     * @return 此路段的公交导航信息
     */
    public RemoteRouteBusLineItem getBusLine() {
        if ((this.busLineList == null) || this.busLineList.size() == 0)
            return null;
        return this.busLineList.get(0);
    }

    /**
     * 设置此路段的公交导航信息
     * 
     * @param busLine
     *            此路段的公交导航信息
     */
    public void setBusLine(RemoteRouteBusLineItem busLine) {
        if (this.busLineList == null)
            return;
        if (this.busLineList.size() == 0)
            this.busLineList.add(busLine);
        this.busLineList.set(0, busLine);
    }

    /**
     * 设置此路段的公交导航信息列表
     * 
     * @param busLineList
     *            此路段的公交导航信息列表
     */
    public void setBusLines(List<RemoteRouteBusLineItem> busLineList) {
        this.busLineList = busLineList;
    }

    /**
     * 返回此路段的入口信息。 入口信息指换乘地铁时的进站口
     * 
     * @return 此路段的入口信息
     */
    public RemoteDoorway getEntrance() {
        return this.entrance;
    }

    /**
     * 设置此路段的入口信息。 入口信息指换乘地铁时的进站口
     * 
     * @param entrance
     *            此路段的入口信息
     */
    public void setEntrance(RemoteDoorway entrance) {
        this.entrance = entrance;
    }

    /**
     * 返回此路段的出口信息。 出口信息指乘地铁之后的出站口信息
     * 
     * @return 此路段的出口信息
     */
    public RemoteDoorway getExit() {
        return this.exit;
    }

    /**
     * 设置此路段的出口信息。 出口信息指乘地铁之后的出站口信息
     * 
     * @param exit
     *            此路段的出口信息
     */
    public void setExit(RemoteDoorway exit) {
        this.exit = exit;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (this.busLineList != null) {
            dest.writeInt(1);
            dest.writeList(this.busLineList);
        } else {
            dest.writeInt(0);
        }

        if (this.entrance != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.entrance, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.exit != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.exit, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.walk != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.walk, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteBusStep> CREATOR = new Creator<RemoteBusStep>() {

        @Override
        public RemoteBusStep createFromParcel(Parcel source) {
            RemoteBusStep busStep = new RemoteBusStep();

            if (source.readInt() != 0) {
                busStep.busLineList = source
                        .readArrayList(RemoteRouteBusLineItem.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                busStep.entrance = source.readParcelable(RemoteDoorway.class
                        .getClassLoader());
            }

            if (source.readInt() != 0) {
                busStep.exit = source.readParcelable(RemoteDoorway.class
                        .getClassLoader());
            }

            if (source.readInt() != 0) {
                busStep.walk = source
                        .readParcelable(RemoteRouteBusWalkItem.class
                                .getClassLoader());
            }

            return busStep;
        }

        @Override
        public RemoteBusStep[] newArray(int size) {
            return new RemoteBusStep[size];
        }

    };
}
