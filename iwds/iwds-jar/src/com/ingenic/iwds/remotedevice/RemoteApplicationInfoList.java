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

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;

class RemoteApplicationInfoList implements Parcelable {
    private List<RemoteApplicationInfo> remoteAppInfoList;

    public RemoteApplicationInfoList() {

    }

    public RemoteApplicationInfoList(PackageManager pm,
            List<PackageInfo> pkgList) {

        remoteAppInfoList = new ArrayList<RemoteApplicationInfo>();

        for (PackageInfo appInfo : pkgList) {
            remoteAppInfoList.add(new RemoteApplicationInfo(pm, appInfo));
        }
    }

    private RemoteApplicationInfoList(Parcel in) {
        remoteAppInfoList = new ArrayList<RemoteApplicationInfo>();

        in.readList(remoteAppInfoList,
                RemoteApplicationInfo.class.getClassLoader());
    }

    public List<RemoteApplicationInfo> getList() {
        return remoteAppInfoList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(remoteAppInfoList);
    }

    public static final Creator<RemoteApplicationInfoList> CREATOR = new Creator<RemoteApplicationInfoList>() {
        @Override
        public RemoteApplicationInfoList createFromParcel(Parcel source) {
            return new RemoteApplicationInfoList(source);
        }

        @Override
        public RemoteApplicationInfoList[] newArray(int size) {
            return new RemoteApplicationInfoList[size];
        }
    };
}
