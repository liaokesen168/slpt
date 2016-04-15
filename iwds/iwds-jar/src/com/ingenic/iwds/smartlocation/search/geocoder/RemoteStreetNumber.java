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
 * 逆地理编码返回结果的门牌信息对象
 */
public class RemoteStreetNumber implements Parcelable {
    private String direction;
    private float distance;
    private RemoteLatLonPoint point;
    private String number;
    private String street;

    /**
     * RemoteStreetNumber构造函数
     */
    public RemoteStreetNumber() {

    }

    /**
     * 返回门牌信息中的街道名称
     * 
     * @return 结果的街道名称
     */
    public String getStreet() {
        return this.street;
    }

    /**
     * 设置门牌信息中的街道名称
     * 
     * @param street
     *            街道名称
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * 返回门牌信息中的门牌号码
     * 
     * @return 结果的门牌号码
     */
    public String getNumber() {
        return this.number;
    }

    /**
     * 设置门牌信息中的门牌号码
     * 
     * @param number
     *            门牌号码
     */
    public void setNumber(String number) {
        this.number = number;
    }

    /**
     * 返回门牌信息中的经纬度坐标
     * 
     * @return 结果的经纬度坐标
     */
    public RemoteLatLonPoint getLatLonPoint() {
        return this.point;
    }

    /**
     * 设置门牌信息中的经纬度坐标
     * 
     * @param point
     *            经纬度坐标
     */
    public void setLatLonPoint(RemoteLatLonPoint point) {
        this.point = point;
    }

    /**
     * 返回门牌信息中的方向 ，指结果点相对地理坐标点的方向。方向显示为英文名称，如South
     * 
     * @return 结果的方向
     */
    public String getDirection() {
        return this.direction;
    }

    /**
     * 设置门牌信息中的方向 ，指结果点相对地理坐标点的方向
     * 
     * @param direction
     *            门牌信息的方向
     */
    public void setDirection(String direction) {
        this.direction = direction;
    }

    /**
     * 返回门牌信息中地理坐标点与结果点的垂直距离。单位米
     * 
     * @return 结果的距离
     */
    public float getDistance() {
        return this.distance;
    }

    /**
     * 设置门牌信息中地理坐标点与结果点的垂直距离。单位米
     * 
     * @param distance
     *            距离
     */
    public void setDistance(float distance) {
        this.distance = distance;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.direction);
        dest.writeString(this.number);
        dest.writeString(this.street);
        dest.writeFloat(this.distance);

        if (this.point != null) {
            dest.writeInt(1);
            dest.writeParcelable(point, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteStreetNumber> CREATOR = new Creator<RemoteStreetNumber>() {
        @Override
        public RemoteStreetNumber createFromParcel(Parcel source) {
            RemoteStreetNumber streetNumber = new RemoteStreetNumber();

            streetNumber.direction = source.readString();
            streetNumber.number = source.readString();
            streetNumber.street = source.readString();
            streetNumber.distance = source.readFloat();

            if (source.readInt() != 0) {
                streetNumber.point = source
                        .readParcelable(RemoteLatLonPoint.class
                                .getClassLoader());
            }

            return streetNumber;
        }

        @Override
        public RemoteStreetNumber[] newArray(int size) {
            return new RemoteStreetNumber[size];
        }

    };

}
