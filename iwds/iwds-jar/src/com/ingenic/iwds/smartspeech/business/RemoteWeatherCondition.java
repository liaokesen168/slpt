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
import android.os.Parcelable;

/**
 * *天气业务的condition节点的抽象，表示天气状况。没有则为空节点
 */
public class RemoteWeatherCondition implements Parcelable {

    /**
     * description元素节点名，表示对天气状况的描述
     */
    public static final String RAWDESCRIPTION = "description";

    /**
     * image元素节点名，表示天气状态图
     */
    public static final String RAWIMAGE = "image";

    /**
     * bg_image元素节点名，表示天气背景图
     */
    public static final String RAWBGIMAGE = "bg_image";

    /**
     * description元素节点的值
     */
    public String mDescription = null;

    /**
     * image元素节点的值
     */
    public String mImage = null;

    /**
     * bg_image元素节点的值
     */
    public String mBgImage = null;

    @Override
    public String toString() {
        return "WeatherCondition [description=" + mDescription + ", image="
                + mImage + ", bg_image=" + mBgImage + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mDescription);
        arg0.writeString(mImage);
        arg0.writeString(mBgImage);
    }

    public static final Creator<RemoteWeatherCondition> CREATOR = new Creator<RemoteWeatherCondition>() {
        @Override
        public RemoteWeatherCondition createFromParcel(Parcel source) {
            RemoteWeatherCondition info = new RemoteWeatherCondition();
            info.mDescription = source.readString();
            info.mImage = source.readString();
            info.mBgImage = source.readString();
            return info;
        }

        @Override
        public RemoteWeatherCondition[] newArray(int size) {
            return new RemoteWeatherCondition[size];
        }
    };
}
