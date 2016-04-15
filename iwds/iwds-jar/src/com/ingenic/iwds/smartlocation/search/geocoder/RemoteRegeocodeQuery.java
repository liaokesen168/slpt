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

package com.ingenic.iwds.smartlocation.search.geocoder;

import com.ingenic.iwds.smartlocation.search.core.RemoteLatLonPoint;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 此类定义了逆地理编码查询的地理坐标点、查询范围、坐标类型
 */
public class RemoteRegeocodeQuery implements Parcelable {
    private RemoteLatLonPoint point;
    private String pointType = "autonavi";
    private float radius;

    /**
     * RemoteRegeocodeQuery构造函数
     */
    public RemoteRegeocodeQuery() {

    }

    /**
     * RemoteRegeocodeQuery构造函数
     * 
     * @param point
     *            要进行逆地理编码的地理坐标点
     * 
     * @param radius
     *            查找范围。默认值为1000，取值范围1-3000，单位米
     * 
     * @param pointType
     *            输入参数坐标类型。包含GPS坐标和高德坐标
     */
    public RemoteRegeocodeQuery(RemoteLatLonPoint point, float radius,
            String pointType) {
        this.point = point;
        this.radius = radius;

        setLatLonType(pointType);
    }

    /**
     * 返回逆地理编码的地理坐标点
     * 
     * @return 该结果的逆地理编码的地理坐标点
     */
    public RemoteLatLonPoint getPoint() {
        return this.point;
    }

    /**
     * 设置逆地理编码的地理坐标点
     * 
     * @param point
     *            逆地理编码的地理坐标点
     */
    public void setPoint(RemoteLatLonPoint point) {
        this.point = point;
    }

    /**
     * 返回查找范围
     * 
     * @return 查找的范围
     */
    public float getRadius() {
        return this.radius;
    }

    /**
     * 设置查找范围
     * 
     * @param radius
     *            查找锁范围
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }

    /**
     * 返回参数坐标类型
     * 
     * @return 参数坐标类型。包含GPS坐标和高德坐标
     */
    public String getLatLonType() {
        return this.pointType;
    }

    /**
     * 设置参数坐标类型
     * 
     * @param pointType
     *            参数坐标类型。包含GPS坐标和高德坐标
     */
    public void setLatLonType(String pointType) {
        if ((pointType != null)
                && ((pointType.equals("autonavi")) || (pointType.equals("gps"))))
            this.pointType = pointType;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (!(object instanceof RemoteRegeocodeQuery))
            return false;

        RemoteRegeocodeQuery other = (RemoteRegeocodeQuery) object;

        if (this.pointType == null) {
            if (other.pointType != null)
                return false;
        } else if (!this.pointType.equals(other.pointType))
            return false;

        if (this.point == null) {
            if (other.point != null)
                return false;
        } else if (!this.point.equals(other.point))
            return false;

        if (Float.floatToIntBits(this.radius) != Float
                .floatToIntBits(other.radius))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = prime * result
                + (this.pointType == null ? 0 : this.pointType.hashCode());
        result = prime * result
                + (this.point == null ? 0 : this.point.hashCode());
        result = prime * result + Float.floatToIntBits(this.radius);

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.pointType);
        dest.writeFloat(this.radius);

        if (this.point != null) {
            dest.writeInt(1);
            dest.writeParcelable(point, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteRegeocodeQuery> CREATOR = new Creator<RemoteRegeocodeQuery>() {

        @Override
        public RemoteRegeocodeQuery createFromParcel(Parcel source) {
            RemoteRegeocodeQuery query = new RemoteRegeocodeQuery();

            query.pointType = source.readString();
            query.radius = source.readFloat();

            if (source.readInt() != 0) {
                query.point = source.readParcelable(RemoteLatLonPoint.class
                        .getClassLoader());
            }

            return query;
        }

        @Override
        public RemoteRegeocodeQuery[] newArray(int size) {
            return new RemoteRegeocodeQuery[size];
        }

    };

}
