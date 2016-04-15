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

import android.content.pm.PackageStats;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * implementation of PackageStats associated with a application package.
 */
class RemotePackageStats implements Parcelable {
    /** Name of the package to which this stats applies. */
    public String packageName;

    /** Size of the code (e.g., APK) */
    public long codeSize;

    /**
     * Size of the internal data size for the application. (e.g.,
     * /data/data/<app>)
     */
    public long dataSize;

    /** Size of cache used by the application. (e.g., /data/data/<app>/cache) */
    public long cacheSize;

    /**
     * Size of the secure container on external storage holding the
     * application's code.
     */
    public long externalCodeSize;

    /**
     * Size of the external data used by the application (e.g.,
     * <sdcard>/Android/data/<app>)
     */
    public long externalDataSize;

    /**
     * Size of the external cache used by the application (i.e., on the SD
     * card). If this is a subdirectory of the data directory, this size will be
     * subtracted out of the external data size.
     */
    public long externalCacheSize;

    /** Size of the external media size used by the application. */
    public long externalMediaSize;

    /** Size of the package's OBBs placed on external media. */
    public long externalObbSize;

    private PackageStats packageStats;

    public static final Parcelable.Creator<RemotePackageStats> CREATOR = new Parcelable.Creator<RemotePackageStats>() {
        public RemotePackageStats createFromParcel(Parcel in) {
            return new RemotePackageStats(in);
        }

        public RemotePackageStats[] newArray(int size) {
            return new RemotePackageStats[size];
        }
    };

    public String toString() {
        final StringBuilder sb = new StringBuilder("PackageStats{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" ");
        sb.append(packageName);
        if (codeSize != 0) {
            sb.append(" code=");
            sb.append(codeSize);
        }
        if (dataSize != 0) {
            sb.append(" data=");
            sb.append(dataSize);
        }
        if (cacheSize != 0) {
            sb.append(" cache=");
            sb.append(cacheSize);
        }
        if (externalCodeSize != 0) {
            sb.append(" extCode=");
            sb.append(externalCodeSize);
        }
        if (externalDataSize != 0) {
            sb.append(" extData=");
            sb.append(externalDataSize);
        }
        if (externalCacheSize != 0) {
            sb.append(" extCache=");
            sb.append(externalCacheSize);
        }
        if (externalMediaSize != 0) {
            sb.append(" media=");
            sb.append(externalMediaSize);
        }
        if (externalObbSize != 0) {
            sb.append(" obb=");
            sb.append(externalObbSize);
        }
        sb.append("}");
        return sb.toString();
    }

    public RemotePackageStats(String pkgName) {
        packageName = pkgName;
    }

    public RemotePackageStats(Parcel source) {
        packageName = source.readString();
        packageStats = new PackageStats(packageName);
        packageStats.codeSize = codeSize = source.readLong();
        packageStats.dataSize = dataSize = source.readLong();
        packageStats.cacheSize = cacheSize = source.readLong();
        packageStats.externalCodeSize = externalCodeSize = source.readLong();
        packageStats.externalDataSize = externalDataSize = source.readLong();
        packageStats.externalCacheSize = externalCacheSize = source.readLong();
        packageStats.externalMediaSize = externalMediaSize = source.readLong();
        packageStats.externalObbSize = externalObbSize = source.readLong();
    }

    public RemotePackageStats(PackageStats pStats) {
        packageName = pStats.packageName;
        codeSize = pStats.codeSize;
        dataSize = pStats.dataSize;
        cacheSize = pStats.cacheSize;
        externalCodeSize = pStats.externalCodeSize;
        externalDataSize = pStats.externalDataSize;
        externalCacheSize = pStats.externalCacheSize;
        externalMediaSize = pStats.externalMediaSize;
        externalObbSize = pStats.externalObbSize;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeString(packageName);
        dest.writeLong(codeSize);
        dest.writeLong(dataSize);
        dest.writeLong(cacheSize);
        dest.writeLong(externalCodeSize);
        dest.writeLong(externalDataSize);
        dest.writeLong(externalCacheSize);
        dest.writeLong(externalMediaSize);
        dest.writeLong(externalObbSize);
    }

    public PackageStats getLocalPackageStats() {
        return packageStats;
    }
}
