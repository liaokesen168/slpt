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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 定义了路径规划的一个方案
 */
public class RemotePath implements Parcelable {
    private float distance;
    private long duration;

    /**
     * RemotePath构造函数
     */
    public RemotePath() {

    }

    /**
     * RemotePath构造函数
     * 
     * @param path
     *            用于序列化实现的一个路径规划方案
     */
    public RemotePath(RemotePath path) {
        this.distance = path.distance;
        this.duration = path.duration;
    }

    /**
     * 返回此规划方案的距离，单位米。 公交换乘路径规划时，此距离为方案的步行距离与公交距离之和
     * 
     * @return 此规划方案的距离
     */
    public float getDistance() {
        return this.distance;
    }

    /**
     * 设置路径规划方案的大约距离，单位米
     * 
     * @param distance
     *            路径规划方案的大约距离
     */
    public void setDistance(float distance) {
        this.distance = distance;
    }

    /**
     * 返回方案的预计消耗时间，单位秒
     * 
     * @return 方案的预计消耗时间
     */
    public long getDuration() {
        return this.duration;
    }

    /**
     * 设置方案的预计消耗时间，单位秒
     * 
     * @param duration
     *            方案的预计消耗时间
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.distance);
        dest.writeLong(this.duration);
    }

    public static final Creator<RemotePath> CREATOR = new Creator<RemotePath>() {

        @Override
        public RemotePath createFromParcel(Parcel source) {
            RemotePath path = new RemotePath();

            path.distance = source.readFloat();
            path.duration = source.readLong();

            return path;
        }

        @Override
        public RemotePath[] newArray(int size) {
            return new RemotePath[size];
        }

    };

}
