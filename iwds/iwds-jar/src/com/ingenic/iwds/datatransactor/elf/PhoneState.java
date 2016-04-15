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

import com.ingenic.iwds.os.SafeParcelable;

/**
 * 电话状态信息，用于设备之间的电话状态同步。
 */
public class PhoneState implements Parcelable {
    /**
     * 表明电话目前处于空闲状态。
     */
    public static final int STATE_IDLE = 0;
    /**
     * 表明电话目前处于来电状态。
     */
    public static final int STATE_INCOMING = 1;
    /**
     * 表明电话目前处于摘机（拨出或接通）状态。
     */
    public static final int STATE_OFFHOOK = 2;
    /**
     * 表明目前正在同步用于快捷回复的短信内容。
     */
    public static final int STATE_SEND_SMS_LIST = -1;
    /**
     * 表明用户禁用了设备之间的电话状态同步。
     */
    public static final int STATE_SYNC_PHONE_DISABLED = -2;
    /**
     * 表明用户拒接电话并回复短信。
     */
    public static final int STATE_SEND_SMS = 21;
    /**
     * 电话状态
     */
    public int state;
    /**
     * 电话号码。当state为{@link #STATE_SEND_SMS_LIST}时作为快捷回复短信存储的键；当state为{
     * {@link #STATE_SYNC_PHONE_DISABLED}时无效。
     */
    public String number;
    /**
     * 对端名字。当state为{@link #STATE_SEND_SMS_LIST}或{@link #STATE_SEND_SMS}时作为短信的内容；当state为
     * {@link #STATE_SYNC_PHONE_DISABLED}时无效。
     */
    public String name;

    /**
     * 由{@link Parcelable#describeContents()}定义
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * 由{@link Parcelable#writeToParcel(Parcel, int)}定义
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(state);
        dest.writeString(number);
        dest.writeString(name);
    }

    /**
     * 电话状态信息的构造器，用于从{@link Parcel}中构造电话状态信息。
     */
    public static final Creator<PhoneState> CREATOR = new Creator<PhoneState>() {

        @Override
        public PhoneState createFromParcel(Parcel source) {
            PhoneState state = new PhoneState();
            state.state = source.readInt();
            state.number = source.readString();
            state.name = source.readString();
            return state;
        }

        @Override
        public PhoneState[] newArray(int size) {
            return new PhoneState[size];
        }
    };

    /**
     * 继承自{@link Object#toString()}
     */
    @Override
    public String toString() {
        return "PhoneStete:state:" + state + ",number:" + number + ",name:" + name;
    }
}
