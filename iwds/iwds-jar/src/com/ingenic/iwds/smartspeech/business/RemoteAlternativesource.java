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
 * alternative_source节点的抽象,附带的其他可选数据源链接（例如移动12580链接）
 */
public class RemoteAlternativesource implements Parcelable {

    /**
     * url元素节点的值
     */
    public String mUrl = null;

    /**
     * name元素节点的值
     */
    public String mName = null;

    /**
     * url元素节点名，表示链接的网址
     */
    public static final String RAWURL = "url";

    /**
     * name元素节点名，表示链接的名字，比如移动12580
     */
    public static final String RAWNAME = "name";

    public RemoteAlternativesource(String Turl, String Tname) {
        mUrl = Turl;
        mName = Tname;
    }

    public RemoteAlternativesource() {
    }

    @Override
    public String toString() {
        return "Alternativesource [url=" + mUrl + ", name=" + mName + "]";
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {

        arg0.writeString(mUrl);
        arg0.writeString(mName);
    }

    public static final Creator<RemoteAlternativesource> CREATOR = new Creator<RemoteAlternativesource>() {
        @Override
        public RemoteAlternativesource createFromParcel(Parcel source) {
            RemoteAlternativesource info = new RemoteAlternativesource();
            info.mUrl = source.readString();
            info.mName = source.readString();
            return info;
        }

        @Override
        public RemoteAlternativesource[] newArray(int size) {
            return new RemoteAlternativesource[size];
        }
    };

}
