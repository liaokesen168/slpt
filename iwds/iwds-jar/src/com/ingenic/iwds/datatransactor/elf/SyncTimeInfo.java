/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  jiabao.nong@ingenic.com
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

package com.ingenic.iwds.datatransactor.elf;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 时间同步类.
 */
public class SyncTimeInfo implements Parcelable {

    /** 当前时间 */
    public long currenttime;
    /** 当前时区 */
    public String timezoneid;

    public SyncTimeInfo(){
        currenttime = System.currentTimeMillis();
        timezoneid  = java.util.TimeZone.getDefault().getID();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(currenttime);
        dest.writeString(timezoneid);
    }

    public static final Creator<SyncTimeInfo> CREATOR = new Creator<SyncTimeInfo>() {
        @Override
        public SyncTimeInfo createFromParcel(Parcel source) {
            SyncTimeInfo info = new SyncTimeInfo();

            info.currenttime = source.readLong();
            info.timezoneid = source.readString();
            return info;
        }

        @Override
        public SyncTimeInfo[] newArray(int size) {
            return new SyncTimeInfo[size];
        }
    };

    private String TimetoString(long currtime) {
        if (currtime < 0)
            return null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
        Date curDate = new Date(currtime);
        String timestr = formatter.format(curDate);
        return timestr;
    }

    @Override
    public String toString() {
        return "currenttime = " + TimetoString(currenttime) + ", timezoneid =" + timezoneid + "]";
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }
}