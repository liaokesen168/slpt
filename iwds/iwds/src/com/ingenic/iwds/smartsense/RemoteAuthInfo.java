/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  ZhangYanMing <yanming.zhang@ingenic.com, jamincheung@126.com>
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

package com.ingenic.iwds.smartsense;

import com.ingenic.iwds.utils.IwdsAssert;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

public class RemoteAuthInfo implements Parcelable {
    public String model;
    public String manufacture;
    public String serialNo;
    public String packageName;

    public RemoteAuthInfo(String model, String manufacture, String serilNo,
            String packageName) {

        IwdsAssert.dieIf(this, model == null, "Device model is null");
        IwdsAssert.dieIf(this, manufacture == null, "Device manufacture is null");
        IwdsAssert.dieIf(this, serilNo == null, "Device seril number is null");
        IwdsAssert.dieIf(this, packageName == null, "Application package name is null");

        this.model = model;
        this.manufacture = manufacture;
        this.serialNo = serilNo;
        this.packageName = packageName;
    }

    public RemoteAuthInfo(String packageName) {
        IwdsAssert.dieIf(this, packageName == null, "Application package name is null");

        this.model = Build.MODEL;
        this.manufacture = Build.MANUFACTURER;
        this.serialNo = Build.SERIAL;
        this.packageName = packageName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(packageName);
        dest.writeString(model);
        dest.writeString(manufacture);
        dest.writeString(serialNo);
    }

    public static final Creator<RemoteAuthInfo> CREATOR = new Creator<RemoteAuthInfo>() {
        @Override
        public RemoteAuthInfo createFromParcel(Parcel source) {
            String packageName = source.readString();
            RemoteAuthInfo info = new RemoteAuthInfo(packageName);

            info.model = source.readString();
            info.manufacture = source.readString();
            info.serialNo = source.readString();

            return info;
        }

        @Override
        public RemoteAuthInfo[] newArray(int size) {
            return new RemoteAuthInfo[size];
        }
    };

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((manufacture == null) ? 0 : manufacture.hashCode());
        result = prime * result + ((model == null) ? 0 : model.hashCode());
        result = prime * result
                + ((serialNo == null) ? 0 : serialNo.hashCode());
        result = prime * result
                + ((packageName == null) ? 0 : packageName.hashCode());

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (!(o instanceof RemoteAuthInfo))
            return false;

        RemoteAuthInfo other = (RemoteAuthInfo) o;

        if (packageName == null) {
            if (other.packageName != null)
                return false;
        } else if (!packageName.equals(other.packageName))
            return false;
        if (model == null) {
            if (other.model != null)
                return false;
        } else if (!model.equals(other.model))
            return false;
        if (manufacture == null) {
            if (other.manufacture != null)
                return false;
        } else if (!manufacture.equals(other.manufacture))
            return false;
        if (serialNo == null) {
            if (other.serialNo != null)
                return false;
        } else if (!serialNo.equals(other.serialNo))
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "RemoteDeivceAuthInfo [model=" + model + ", manufacture=" +
                manufacture + ", serialNo=" + serialNo + ", packageName=" +
                packageName + "]";
    }
}
