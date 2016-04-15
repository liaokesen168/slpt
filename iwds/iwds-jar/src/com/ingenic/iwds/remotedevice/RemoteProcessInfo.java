/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  Huanglihong(Regen) <lihong.huang@ingenic.com>
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

package com.ingenic.iwds.remotedevice;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

class RemoteProcessInfoList implements Parcelable {
    List<RemoteProcessInfo> processInfoList = new ArrayList<RemoteProcessInfo>();

    public RemoteProcessInfoList() {
    }

    public void add(RemoteProcessInfo processInfo) {
        processInfoList.add(processInfo);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(processInfoList);
    }

    public static final Creator<RemoteProcessInfoList> CREATOR = new Creator<RemoteProcessInfoList>() {

        @Override
        public RemoteProcessInfoList createFromParcel(Parcel source) {
            return new RemoteProcessInfoList(source);
        }

        @Override
        public RemoteProcessInfoList[] newArray(int size) {
            return new RemoteProcessInfoList[size];
        }
    };

    private RemoteProcessInfoList(Parcel source) {
        source.readList(processInfoList,
                RemoteProcessInfo.class.getClassLoader());
    }
}

/**
 * 远程设备的进程信息。
 */
public class RemoteProcessInfo implements Parcelable {
    /**
     * 进程名
     */
    public String processName;

    /**
     * 进程号
     */
    public int pid;

    /**
     * 进程用户号
     */
    public int uid;

    /**
     * 进程占用的内存大小。是进程的总PSS大小，为dalvik PSS、native PSS及other PSS之和，单位kB。
     */
    public int memSize;

    public RemoteProcessInfo(String processName, int pid, int uid, int memSize) {
        this.processName = processName;
        this.pid = pid;
        this.uid = uid;
        this.memSize = memSize;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(processName);
        dest.writeInt(pid);
        dest.writeInt(uid);
        dest.writeInt(memSize);
    }

    void readFromParcel(Parcel source) {
        processName = source.readString();
        pid = source.readInt();
        uid = source.readInt();
        memSize = source.readInt();
    }

    public static final Creator<RemoteProcessInfo> CREATOR = new Creator<RemoteProcessInfo>() {

        @Override
        public RemoteProcessInfo createFromParcel(Parcel source) {
            return new RemoteProcessInfo(source);
        }

        @Override
        public RemoteProcessInfo[] newArray(int size) {
            return new RemoteProcessInfo[size];
        }
    };

    private RemoteProcessInfo(Parcel source) {
        readFromParcel(source);
    }
}