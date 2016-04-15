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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 远程设备的存储信息类。
 */
public class RemoteStorageInfo implements Parcelable {
    /**
     * 远程设备内部存储空间的可用大小。单位为byte。
     */
    public long availInternalSize;

    /**
     * 远程设备内部存储空间的总大小。单位为byte。
     */
    public long totalInternalSize;

    /**
     * 表示远程设备是否有外部存储。
     */
    public boolean hasExternalStorage;

    /**
     * 远程设备外部存储空间的可用大小。单位为byte。
     */
    public long availExternalSize;

    /**
     * 远程设备外部存储空间的总大小。单位为byte。
     */
    public long totalExternalSize;

    RemoteStorageInfo(long availInternalSize, long totalInternalSize,
            boolean hasExternalStorage, long availExternalSize,
            long totalExternalSize) {
        this.availInternalSize = availInternalSize;
        this.totalInternalSize = totalInternalSize;
        this.hasExternalStorage = hasExternalStorage;
        this.availExternalSize = availExternalSize;
        this.totalExternalSize = totalExternalSize;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(availInternalSize);
        dest.writeLong(totalInternalSize);
        dest.writeInt(hasExternalStorage ? 1 : 0);
        dest.writeLong(availExternalSize);
        dest.writeLong(totalExternalSize);
    }

    public static final Creator<RemoteStorageInfo> CREATOR = new Creator<RemoteStorageInfo>() {

        @Override
        public RemoteStorageInfo createFromParcel(Parcel source) {
            return new RemoteStorageInfo(source);
        }

        @Override
        public RemoteStorageInfo[] newArray(int size) {
            return new RemoteStorageInfo[size];
        }
    };

    private RemoteStorageInfo(Parcel source) {
        availInternalSize = source.readLong();
        totalInternalSize = source.readLong();
        hasExternalStorage = (source.readInt() == 1) ? true : false;
        availExternalSize = source.readLong();
        totalExternalSize = source.readLong();
    }
}
