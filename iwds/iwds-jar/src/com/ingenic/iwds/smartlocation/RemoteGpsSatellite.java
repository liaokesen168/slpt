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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * GPS 卫星信息类
 */
public class RemoteGpsSatellite implements Parcelable {
    boolean hasEphemeris;
    boolean hasAlmanac;
    boolean usedInFix;
    float snr;
    float elevation;
    float azimuth;

    /**
     * 构造方法
     */
    RemoteGpsSatellite() {

    }

    /**
     * 返回卫星信噪比
     * 
     * @return 信噪比
     */
    public float getSnr() {
        return this.snr;
    }

    /**
     * 设置卫星信噪比
     * 
     * @param snr
     *            信噪比
     */
    public void setSnr(float snr) {
        this.snr = snr;
    }

    /**
     * 返回卫星的仰角，范围0-90
     * 
     * @return 卫星的仰角
     */
    public float getElevation() {
        return this.elevation;
    }

    /**
     * 设置卫星的仰角
     * 
     * @param elevation
     *            卫星的仰角
     */
    public void setElevation(float elevation) {
        this.elevation = elevation;
    }

    /**
     * 返回卫星的方位角，范围0-360
     * 
     * @return 卫星的方位角
     */
    public float getAzimuth() {
        return this.azimuth;
    }

    /**
     * 设置卫星的方位角
     * 
     * @param azimuth
     *            卫星的方位角
     */
    public void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
    }

    /**
     * 返回卫星是否有星历表
     * 
     * @return true 有，false 无
     */
    public boolean hasEphemeris() {
        return this.hasEphemeris;
    }

    /**
     * 设置卫星是否有星历表
     * 
     * @param hasEphemeris
     *            是否有星历表
     */
    public void setEphemeris(boolean hasEphemeris) {
        this.hasEphemeris = hasEphemeris;
    }

    /**
     * 返回卫星是否有年鉴表
     * 
     * @return true 有，false 无
     */
    public boolean hasAlmanac() {
        return this.hasAlmanac;
    }

    /**
     * 设置卫星是否有年鉴表
     * 
     * @param hasAlmanac
     *            是否有年鉴表
     */
    public void setAlmanac(boolean hasAlmanac) {
        this.hasAlmanac = hasAlmanac;
    }

    /**
     * 返回卫星是否被用于近期的GPS修正计算
     * 
     * @return true 是，false 否
     */
    public boolean usedInFix() {
        return this.usedInFix;
    }

    /**
     * 设置卫星是否被用于近期的GPS修正计算
     * 
     * @param usedInFix
     *            卫星是否被用于近期的GPS修正计算
     */
    public void setUsedInFix(boolean usedInFix) {
        this.usedInFix = usedInFix;
    }

    @Override
    public String toString() {
        return "{snr=" + this.snr + ", elevation=" + this.elevation
                + ", azimuth=" + this.azimuth + ", hasEphemeris="
                + this.hasEphemeris + ", hasAlmanac=" + this.hasAlmanac
                + ", usedInFix=" + this.usedInFix + "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.hasEphemeris ? 1 : 0);
        dest.writeInt(this.hasAlmanac ? 1 : 0);
        dest.writeInt(this.usedInFix ? 1 : 0);
        dest.writeFloat(this.snr);
        dest.writeFloat(this.elevation);
        dest.writeFloat(this.azimuth);
    }

    public static final Creator<RemoteGpsSatellite> CREATOR = new Creator<RemoteGpsSatellite>() {

        @Override
        public RemoteGpsSatellite createFromParcel(Parcel source) {
            RemoteGpsSatellite satellite = new RemoteGpsSatellite();

            satellite.hasEphemeris = source.readInt() != 0;
            satellite.hasAlmanac = source.readInt() != 0;
            satellite.usedInFix = source.readInt() != 0;
            satellite.snr = source.readInt();
            satellite.elevation = source.readFloat();
            satellite.azimuth = source.readFloat();

            return satellite;
        }

        @Override
        public RemoteGpsSatellite[] newArray(int size) {
            return new RemoteGpsSatellite[size];
        }

    };
}
