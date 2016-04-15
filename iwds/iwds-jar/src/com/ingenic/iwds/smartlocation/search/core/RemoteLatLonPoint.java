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

package com.ingenic.iwds.smartlocation.search.core;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 几何点对象类。 该类为不可变类，表示一对经、纬度值，以双精度形式存储
 */
public class RemoteLatLonPoint implements Parcelable {
    private double latitude;
    private double longitude;

    /**
     * RemoteLatLonPoint构造函数
     * 
     * @param latitude
     *            该点的纬度。为保持Mercator 投影精确度，其取值范围是[-80,80]
     * 
     * @param longitude
     *            该点的经度，可被规范化到(-180,180]
     */
    public RemoteLatLonPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * 获取该点经度
     * 
     * @return 该点的经度
     */
    public double getLongitude() {
        return this.longitude;
    }

    /**
     * 设置该点经度
     * 
     * @param longitude
     *            经度值
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * 获取该点纬度
     * 
     * @return 该点的纬度
     */
    public double getLatitude() {
        return this.latitude;
    }

    /**
     * 设置该点纬度
     * 
     * @param latitude
     *            纬度值
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * 复制一个经纬度点对象
     * 
     * @return 返回一个和原经纬度相同的经纬度坐标对象
     */
    public RemoteLatLonPoint copy() {
        return new RemoteLatLonPoint(this.latitude, this.longitude);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (!(object instanceof RemoteLatLonPoint))
            return false;

        RemoteLatLonPoint other = (RemoteLatLonPoint) object;
        if (Double.doubleToLongBits(this.latitude) != Double
                .doubleToLongBits(other.latitude))
            return false;
        if (Double.doubleToLongBits(this.longitude) != Double
                .doubleToLongBits(other.longitude))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        long code = Double.doubleToLongBits(this.latitude);
        result = prime * result + (int) (code ^ code >>> 32);
        code = Double.doubleToLongBits(this.longitude);
        result = prime * result + (int) (code ^ code >>> 32);

        return result;
    }

    @Override
    public String toString() {
        return "LatLonPoint [" + "latitude=" + this.latitude + ", longitude="
                + this.longitude + "]";
    }

    public static final Creator<RemoteLatLonPoint> CREATOR = new Creator<RemoteLatLonPoint>() {
        @Override
        public RemoteLatLonPoint createFromParcel(Parcel source) {
            double latitude = source.readDouble();
            double longitude = source.readDouble();

            RemoteLatLonPoint point = new RemoteLatLonPoint(latitude, longitude);

            return point;
        }

        @Override
        public RemoteLatLonPoint[] newArray(int size) {
            return new RemoteLatLonPoint[size];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}