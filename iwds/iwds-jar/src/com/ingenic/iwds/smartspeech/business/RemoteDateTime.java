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
 * datetime节点的抽象,表示时间信息的类
 */
public class RemoteDateTime implements Parcelable {
    
    /**
     * date元素的节点名，表示年月
     */
    public static final String ROWDATE = "date";
    
    /**
     * time元素的节点名，表示时刻
     */
    public static final String ROWTIME = "time";
    
    /**
     * date元素的节点的值
     */
    public String mDate = null;
    
    /**
     * time元素的节点的值
     */
    public String mTime = null;

    public RemoteDateTime(String Tdate, String Ttime) {
        mDate = Tdate;
        mTime = Ttime;
    }

    public RemoteDateTime() {
    }

    @Override
    public String toString() {
        return "DateTime [date=" + mDate + ", time=" + mTime + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mDate);
        arg0.writeString(mTime);
    }

    public static final Creator<RemoteDateTime> CREATOR = new Creator<RemoteDateTime>() {
        @Override
        public RemoteDateTime createFromParcel(Parcel source) {
            RemoteDateTime info = new RemoteDateTime();
            info.mDate = source.readString();
            info.mTime = source.readString();
            return info;
        }

        @Override
        public RemoteDateTime[] newArray(int size) {
            return new RemoteDateTime[size];
        }
    };
}
