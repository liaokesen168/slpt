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

package com.ingenic.iwds.smartlocation;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * GPS 状态类
 */
public class RemoteGpsStatus implements Parcelable {
    private static final int NUM_SATELLITES = 255;

    /**
     * 描述 GPS 启动事件的常量
     */
    public static final int GPS_EVENT_STARTED = 1;

    /**
     * 描述 GPS 停止事件的常量
     */
    public static final int GPS_EVENT_STOPPED = 2;

    /**
     * 描述 GPS 启动（重启）后首次定位成功事件的常量，通过{@link #getTimeToFirstFix()}获取定位用时
     */
    public static final int GPS_EVENT_FIRST_FIX = 3;

    /**
     * 描述 GPS 卫星状态变化的常量，通过{@link #getSatellites()}可以获取每一个卫星的状态
     */
    public static final int GPS_EVENT_SATELLITE_STATUS = 4;

    private int timeToFirstFix;
    private List<RemoteGpsSatellite> satelliteList = new ArrayList<RemoteGpsSatellite>();

    /**
     * 构造方法
     */
    RemoteGpsStatus() {

    }

    /**
     * GPS 状态监听器
     */
    public interface RemoteGpsStatusListener {
        /**
         * 返回 GPS 状态
         * <ul>
         * <li> {@link RemoteGpsStatus#GPS_EVENT_STARTED}
         * <li> {@link RemoteGpsStatus#GPS_EVENT_STOPPED}
         * <li> {@link RemoteGpsStatus#GPS_EVENT_FIRST_FIX}
         * <li> {@link RemoteGpsStatus#GPS_EVENT_SATELLITE_STATUS}
         * </ul>
         *
         * 当该方法被调用时，可以通过 requestGpsStatus 获取详细信息
         *
         * @param event
         *            GPS 状态
         */
        void onGpsStatusChanged(int event);

        /**
         * 返回 GPS 详细状态
         * 
         * @param status
         *            GPS 详细状态
         */
        void onGpsStatus(RemoteGpsStatus status);
    }

    /**
     * 设置最近一次 GPS 重启后定位成功用时，单位毫秒
     * 
     * @param timeToFirstFix
     *            最近一次 GPS 重启后定位成功用时
     */
    public void setTimeToFirstFix(int timeToFirstFix) {
        this.timeToFirstFix = timeToFirstFix;
    }

    /**
     * 获取最近一次 GPS 重启后定位成功用时，单位毫秒
     * 
     * @return 最近一次 GPS 重启后定位成功用时
     */
    public int getTimeToFirstFix() {
        return this.timeToFirstFix;
    }

    /**
     * 获取卫星列表
     * 
     * @return 卫星列表
     */
    public List<RemoteGpsSatellite> getSatellites() {
        return satelliteList;
    }

    /**
     * 获取最多卫星数
     * 
     * @return 最多卫星数
     */
    public int getMaxSatellites() {
        return NUM_SATELLITES;
    }

    @Override
    public String toString() {
        return "{timeToFirstFix=" + this.timeToFirstFix + ", satellite size="
                + this.satelliteList.size() + ", satellite="
                + this.satelliteList + "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.timeToFirstFix);
        dest.writeList(this.satelliteList);
    }

    public static final Creator<RemoteGpsStatus> CREATOR = new Creator<RemoteGpsStatus>() {

        @Override
        public RemoteGpsStatus createFromParcel(Parcel source) {
            RemoteGpsStatus status = new RemoteGpsStatus();

            status.timeToFirstFix = source.readInt();
            status.satelliteList = source
                    .readArrayList(RemoteGpsSatellite.class.getClassLoader());

            return status;
        }

        @Override
        public RemoteGpsStatus[] newArray(int size) {
            return new RemoteGpsStatus[size];
        }
    };
}
