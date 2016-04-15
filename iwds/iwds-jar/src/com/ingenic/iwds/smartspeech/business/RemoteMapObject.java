/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  Zhouzhiqiang <zhiqiang.zhou@ingenic.com>
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

package com.ingenic.iwds.smartspeech.business;

import android.os.Parcel;

/**
 * map的object抽象,表示地图业务信息的类
 */
public class RemoteMapObject extends RemoteBusinessObject {

    /**
     * RemoteMapobject的object标识，区分于其他的object
     */
    public static String sFocus = "map";

    /**
     * point元素节点名，表示地址信息
     */
    public static final String RAWPOINT = "point";

    /**
     * url元素节点名，用来查看地图的网址
     */
    public static final String RAWURL = "url";

    /**
     * client元素节点名，用来区别多个point的point属性。
     * 当某个point的client属性值为true时，表示这个point节点的值是用户的定位地点。
     */
    public static String ATTRCLIENT = "client";

    /**
     * point元素节点的值，起始地址
     */
    public String mFirstPoint = null;
    /**
     * point元素节点的值，终点地址
     */
    public String mSecondPoint = null;

    /**
     * client元素节点的属性值 如果为“true”，表示该point是用户所在位置
     */
    public String mFirstPointClient = null;

    /**
     * client元素节点的属性值 如果为“true”，表示该point是用户所在位置
     */
    public String mSecondPointClient = null;

    /**
     * url元素节点的值
     */
    public String mUrl = null;

    @Override
    public String toString() {
        return "Mapobject [firstpoint=" + mFirstPoint + ", secondpoint="
                + mSecondPoint + ", firstpointclient=" + mFirstPointClient
                + ", secondpointclient=" + mSecondPointClient + ", url=" + mUrl
                + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {

        arg0.writeString(mFirstPoint);
        arg0.writeString(mSecondPoint);
        arg0.writeString(mFirstPointClient);
        arg0.writeString(mSecondPointClient);
        arg0.writeString(mUrl);
    }

    public static final Creator<RemoteMapObject> CREATOR = new Creator<RemoteMapObject>() {
        @Override
        public RemoteMapObject createFromParcel(Parcel source) {
            RemoteMapObject info = new RemoteMapObject();
            info.mFirstPoint = source.readString();
            info.mSecondPoint = source.readString();
            info.mFirstPointClient = source.readString();
            info.mSecondPointClient = source.readString();
            info.mUrl = source.readString();
            return info;
        }

        @Override
        public RemoteMapObject[] newArray(int size) {
            return new RemoteMapObject[size];
        }
    };
}
