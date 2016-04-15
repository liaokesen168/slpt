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
 * 定义了驾车路径规划的一个方案
 */
public class RemoteDrivePath extends RemotePath implements Parcelable {
    private List<RemoteDriveStep> stepList = new ArrayList<RemoteDriveStep>();
    private String strategy;
    private float tollDistance;
    private float tolls;

    /**
     * RemoteDrivePath构造函数
     */
    public RemoteDrivePath() {
        super();
    }

    /**
     * RemoteDrivePath构造函数
     * 
     * @param path
     *            用于序列化实现的一个驾车路径规划方案
     */
    public RemoteDrivePath(RemotePath path) {
        super(path);
    }

    /**
     * 返回导航策略，显示为中文，如返回“速度最快”
     * 
     * @return 导航策略
     */
    public String getStrategy() {
        return this.strategy;
    }

    /**
     * 设置导航策略
     * 
     * @param strategy
     *            导航策略
     */
    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    /**
     * 返回此方案中的收费道路的总费用，单位元
     * 
     * @return 此方案中的收费道路的总费用
     */
    public float getTolls() {
        return this.tolls;
    }

    /**
     * 设置此方案中的收费道路的总费用，单位元
     * 
     * @param tolls
     *            此方案中的收费道路的总费用
     */
    public void setTolls(float tolls) {
        this.tolls = tolls;
    }

    /**
     * 返回此方案中的收费道路的总长度，单位米
     * 
     * @return 此方案中的收费道路的总长度
     */
    public float getTollDistance() {
        return this.tollDistance;
    }

    /**
     * 设置此方案中的收费道路的总长度，单位米
     * 
     * @param tollDistance
     *            此方案中的收费道路的总长度
     */
    public void setTollDistance(float tollDistance) {
        this.tollDistance = tollDistance;
    }

    /**
     * 返回驾车规划方案的路段列表
     * 
     * @return 驾车规划方案的路段列表
     */
    public List<RemoteDriveStep> getSteps() {
        return this.stepList;
    }

    /**
     * 设置驾车规划方案的路段列表
     * 
     * @param stepList
     *            驾车规划方案的路段列表
     */
    public void setSteps(List<RemoteDriveStep> stepList) {
        this.stepList = stepList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeString(this.strategy);
        dest.writeFloat(this.tollDistance);
        dest.writeFloat(this.tolls);

        if (this.stepList != null) {
            dest.writeInt(1);
            dest.writeList(this.stepList);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteDrivePath> CREATOR = new Creator<RemoteDrivePath>() {

        @Override
        public RemoteDrivePath createFromParcel(Parcel source) {
            RemotePath path = RemotePath.CREATOR.createFromParcel(source);

            RemoteDrivePath drivePath = new RemoteDrivePath(path);

            drivePath.strategy = source.readString();
            drivePath.tollDistance = source.readFloat();
            drivePath.tolls = source.readFloat();

            if (source.readInt() != 0) {
                drivePath.stepList = source.readArrayList(RemoteDriveStep.class
                        .getClassLoader());
            }

            return drivePath;
        }

        @Override
        public RemoteDrivePath[] newArray(int size) {
            return new RemoteDrivePath[size];
        }
    };
}
