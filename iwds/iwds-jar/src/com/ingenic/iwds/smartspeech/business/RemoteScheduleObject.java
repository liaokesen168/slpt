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
 * schedule的object抽象，表示提醒业务信息的类
 */
public class RemoteScheduleObject extends RemoteBusinessObject {

    /**
     * RemoteScheduleobject的objct标识
     */
    public static String sFocus = "schedule";

    /**
     * name元素节点名，表示日程类型
     * 两种可能的值：
     * clock代表闹钟
     * reminder代表提醒
     */
    public static final String RAWNAME = "name";

    /**
     * datetime元素节点名，表示日程的时间
     */
    public static final String RAWDATETIME = "datetime";

    /**
     * repeat元素节点名，表示重复提醒的类型。
     * 取值有：once/everyday/w3/w1-w5/m10
     * 分别表示：“一次性”、“每天”、“每周几”、“周几到周几”“每月几号”
     */
    public static final String RAWREPEAT = "repeat";

    /**
     * name元素节点的值
     */
    public String mName = null;

    /**
     * datetime元素节点的值
     */
    public RemoteDateTime mDateTime = null;

    /**
     * repeat元素节点的值
     */
    public String mRepeat = null;

    @Override
    public String toString() {
        return "Scheduleobject [name=" + mName + ", datetime=" + mDateTime
                + ", repeat=" + mRepeat + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mName);
        arg0.writeParcelable(mDateTime, arg1);
        arg0.writeString(mRepeat);

    }

    public static final Creator<RemoteScheduleObject> CREATOR = new Creator<RemoteScheduleObject>() {
        @Override
        public RemoteScheduleObject createFromParcel(Parcel source) {
            RemoteScheduleObject info = new RemoteScheduleObject();
            info.mName = source.readString();
            info.mDateTime = source.readParcelable(RemoteDateTime.class
                    .getClassLoader());
            info.mRepeat = source.readString();
            return info;
        }

        @Override
        public RemoteScheduleObject[] newArray(int size) {
            return new RemoteScheduleObject[size];
        }
    };
}
