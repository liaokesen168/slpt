/*
 * Copyright (C) 2015 Ingenic Semiconductor
 * 
 * LiJinWen(Kevin)<kevin.jwli@ingenic.com>
 * 
 * Elf/IDWS Project
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.ingenic.iwds.datatransactor.elf;

import android.os.Parcel;
import android.os.Parcelable;

public class CalllogInfo implements Parcelable {
    private int _id;
    private String number;
    private long date;
    private int duration;
    private int type;
    private int news;
    private String name;
    private int is_read;
    public static final int OPT_ADD = 1;
    public static final int OPT_DEL = 2;
    public static final int OPT_START = 3;
    public static final int OPT_END = 4;
    public int operation; /* STATE_ADD, STATE_DEL*/
    @Override
    public int describeContents() {
        return 0;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getNewflag() {
        return news;
    }

    public void setNewflag(int news) {
        this.news = news;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIs_read() {
        return is_read;
    }

    public void setIs_read(int is_read) {
        this.is_read = is_read;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(operation);
        dest.writeInt(_id);
        dest.writeString(number);
        dest.writeLong(date);
        dest.writeInt(duration);
        dest.writeInt(type);
        dest.writeInt(news);
        dest.writeString(name);
        dest.writeInt(is_read);
    }

    public static final Creator<CalllogInfo> CREATOR = new Creator<CalllogInfo>() {

        @Override
        public CalllogInfo createFromParcel(Parcel source) {
            CalllogInfo info = new CalllogInfo();
            info.operation = source.readInt();
            info._id = source.readInt();
            info.number = source.readString();
            info.date = source.readLong();
            info.duration = source.readInt();
            info.type = source.readInt();
            info.news = source.readInt();
            info.name = source.readString();
            info.is_read = source.readInt();
            return info;
        }

        @Override
        public CalllogInfo[] newArray(int size) {
            return new CalllogInfo[size];
        }
    };

    public String toString() {
        return "Calllog info operation:" + operation + " _id:" + _id + ",number:" + number + ",date:" + date + ",duration:"
                + duration + ",type:" + type + ",new:" + news + ",name:" + name + ",is_read:"
                + is_read;
    };
}
