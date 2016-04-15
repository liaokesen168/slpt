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
 * 定义了公交换乘路径规划的一个方案
 */
public class RemoteBusPath extends RemotePath implements Parcelable {
    private float busDistance;
    private float cost;
    private List<RemoteBusStep> stepList = new ArrayList<RemoteBusStep>();
    private float walkDistance;
    private boolean isNightBus;

    /**
     * RemoteBusPath构造函数
     */
    public RemoteBusPath() {
        super();
    }

    /**
     * RemoteBusPath构造函数
     * 
     * @param path
     *            用于序列化实现的公交换乘路径规划方案
     */
    public RemoteBusPath(RemotePath path) {
        super(path);
    }

    /**
     * 返回公交换乘方案的花费，单位元
     * 
     * @return 公交换乘方案的花费
     */
    public float getCost() {
        return this.cost;
    }

    /**
     * 设置公交换乘方案的花费，单位元
     * 
     * @param cost
     *            公交换乘方案的花费
     */
    public void setCost(float cost) {
        this.cost = cost;
    }

    /**
     * 返回是否包含夜班车
     * 
     * @return 是否包含夜班车。true，包含夜班车；false，不包含
     */
    public boolean isNightBus() {
        return this.isNightBus;
    }

    /**
     * 设置是否包含夜班车
     * 
     * @param isNightBus
     *            是否包含夜班车。true，包含夜班车；false，不包含
     */
    public void setNightBus(boolean isNightBus) {
        this.isNightBus = isNightBus;
    }

    /**
     * 返回此方案整条路线的距离，包括公交行驶距离与步行距离，单位米
     */
    public float getDistance() {
        return this.busDistance + this.walkDistance;
    }

    /**
     * 返回此方案的总步行距离，单位米
     * 
     * @return 此方案的总步行距离
     */
    public float getWalkDistance() {
        return this.walkDistance;
    }

    /**
     * 设置此方案的总步行距离，单位米
     * 
     * @param walkDistance
     *            此方案的总步行距离
     */
    public void setWalkDistance(float walkDistance) {
        this.walkDistance = walkDistance;
    }

    /**
     * 返回此方案的公交行驶的总距离，单位米
     * 
     * @return 此方案的公交行驶的总距离
     */
    public float getBusDistance() {
        return this.busDistance;
    }

    /**
     * 设置此方案的公交行驶的总距离，单位米
     * 
     * @param busDistance
     */
    public void setBusDistance(float busDistance) {
        this.busDistance = busDistance;
    }

    /**
     * 返回公交路径规划方案的路段列表
     * 
     * @return 公交路径规划方案的路段列表
     */
    public List<RemoteBusStep> getSteps() {
        return this.stepList;
    }

    /**
     * 设置公交路径规划方案的路段列表
     * 
     * @param steps
     *            公交路径规划方案的路段列表
     */
    public void setSteps(List<RemoteBusStep> steps) {
        this.stepList = steps;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(this.busDistance);
        dest.writeFloat(this.cost);
        dest.writeFloat(this.walkDistance);
        dest.writeByte((byte) (this.isNightBus ? 1 : 0));

        if (this.stepList != null) {
            dest.writeInt(1);
            dest.writeList(this.stepList);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteBusPath> CREATOR = new Creator<RemoteBusPath>() {

        @Override
        public RemoteBusPath createFromParcel(Parcel source) {
            RemotePath path = RemotePath.CREATOR.createFromParcel(source);

            RemoteBusPath busPath = new RemoteBusPath(path);

            busPath.busDistance = source.readFloat();
            busPath.cost = source.readFloat();
            busPath.walkDistance = source.readFloat();

            if (source.readByte() == 1) {
                busPath.isNightBus = true;
            } else {
                busPath.isNightBus = false;
            }

            if (source.readInt() != 0)
                busPath.stepList = source.readArrayList(RemoteBusStep.class
                        .getClassLoader());

            return busPath;
        }

        @Override
        public RemoteBusPath[] newArray(int size) {
            return new RemoteBusPath[size];
        }
    };
}
