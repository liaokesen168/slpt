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
 * 逆地理编码返回的商圈信息
 */
public class RemoteBusinessArea implements Parcelable {
    private RemoteLatLonPoint centerPoint;
    private String name;

    /**
     * RemoteBusinessArea构造函数
     */
    public RemoteBusinessArea() {

    }

    /**
     * 获取当前商圈的中心点坐标
     * 
     * @return 商圈中心点坐标
     */
    public RemoteLatLonPoint getCenterPoint() {
        return this.centerPoint;
    }

    /**
     * 设置当前商圈的中心点坐标
     * 
     * @param centerPoint
     *            商圈中心点坐标
     */
    public void setCenterPoint(RemoteLatLonPoint centerPoint) {
        this.centerPoint = centerPoint;
    }

    /**
     * 获取当前商圈的名字
     * 
     * @return 商圈的名字
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置当前商圈的名字
     * 
     * @param name
     *            商圈的名字
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "RemoteBusinessArea [" + "centerPoint=" + this.centerPoint
                + ", name=" + this.name + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        if (this.centerPoint != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.centerPoint, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteBusinessArea> CREATOR = new Creator<RemoteBusinessArea>() {
        @Override
        public RemoteBusinessArea createFromParcel(Parcel source) {
            RemoteBusinessArea businessArea = new RemoteBusinessArea();

            businessArea.name = source.readString();
            if (source.readInt() != 0) {
                businessArea.centerPoint = source
                        .readParcelable(RemoteBusinessArea.class
                                .getClassLoader());
            }

            return businessArea;
        }

        @Override
        public RemoteBusinessArea[] newArray(int size) {
            return new RemoteBusinessArea[size];
        }

    };

}
