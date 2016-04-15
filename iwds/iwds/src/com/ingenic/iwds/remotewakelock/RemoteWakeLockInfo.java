/*
 * Copyright (C) 2015 Ingenic Semiconductor
 * 
 * LiJingWen(Kevin) <kevin.jwli@ingenic.com>
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
package com.ingenic.iwds.remotewakelock;

import android.os.Parcel;
import android.os.Parcelable;

abstract class RemoteWakeLockInfo {
    protected int mCallerId;
    protected int mId;

    public int getCallerId() {
        return mCallerId;
    }

    public int getId() {
        return mId;
    }
}

class CreateInfo extends RemoteWakeLockInfo implements Parcelable {
    private int mLevelAndFlags;
    private String mTag;

    public CreateInfo(int callerId, int id, int levelAndFlags, String tag) {
        mCallerId = callerId;
        mId = id;
        mLevelAndFlags = levelAndFlags;
        mTag = tag;
    }

    protected CreateInfo(Parcel in) {
        readFromParcel(in);
    }

    public int getLevelAndFlags() {
        return mLevelAndFlags;
    }

    public String getTag() {
        return mTag;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mCallerId);
        dest.writeInt(mId);
        dest.writeInt(mLevelAndFlags);
        dest.writeString(mTag);
    }

    public void readFromParcel(Parcel in) {
        mCallerId = in.readInt();
        mId = in.readInt();
        mLevelAndFlags = in.readInt();
        mTag = in.readString();
    }

    public static final Creator<CreateInfo> CREATOR = new Creator<CreateInfo>() {

        @Override
        public CreateInfo createFromParcel(Parcel source) {
            return new CreateInfo(source);
        }

        @Override
        public CreateInfo[] newArray(int size) {
            return new CreateInfo[size];
        }
    };
}

class DeleteInfo extends RemoteWakeLockInfo implements Parcelable {

    public DeleteInfo(int callerId, int id) {
        mCallerId = callerId;
        mId = id;
    }

    protected DeleteInfo(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mCallerId);
        dest.writeInt(mId);
    }

    public void readFromParcel(Parcel in) {
        mCallerId = in.readInt();
        mId = in.readInt();
    }

    public static final Creator<DeleteInfo> CREATOR = new Creator<DeleteInfo>() {

        @Override
        public DeleteInfo createFromParcel(Parcel source) {
            return new DeleteInfo(source);
        }

        @Override
        public DeleteInfo[] newArray(int size) {
            return new DeleteInfo[size];
        }
    };
}

class CMDInfo extends RemoteWakeLockInfo implements Parcelable {
    public static final int CMD_RELEASE = 0;
    public static final int CMD_ACQUIRE = 1;

    private int mCmd = CMD_RELEASE;
    private long mTimeout = -1;

    public CMDInfo(int callerId, int id) {
        this(callerId, callerId, CMD_RELEASE);
    }

    public CMDInfo(int callerId, int id, int cmd) {
        this(callerId, id, cmd, -1);
    }

    public CMDInfo(int callerId, int id, int cmd, long timeout) {
        mCallerId = callerId;
        mId = id;
        mCmd = cmd;
        mTimeout = timeout;
    }

    protected CMDInfo(Parcel in) {
        readFromParcel(in);
    }

    public int getCmd() {
        return mCmd;
    }

    public long getTimeout() {
        return mTimeout;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mCallerId);
        dest.writeInt(mId);
        dest.writeInt(mCmd);
        dest.writeLong(mTimeout);
    }

    public void readFromParcel(Parcel in) {
        mCallerId = in.readInt();
        mId = in.readInt();
        mCmd = in.readInt();
        mTimeout = in.readLong();
    }

    public static final Creator<CMDInfo> CREATOR = new Creator<CMDInfo>() {

        @Override
        public CMDInfo createFromParcel(Parcel source) {
            return new CMDInfo(source);
        }

        @Override
        public CMDInfo[] newArray(int size) {
            return new CMDInfo[size];
        }
    };
}