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

/**
 * RemoteOtherSearchobject类 other的object的抽象
 */
public class RemoteOtherObject extends RemoteBusinessObject {

    /**
     * RemoteOtherSearchobject的object标识
     */
    public static String sFocus = "other";

    /**
     * name元素节点名
     */
    public static final String RAWNAME = "name";

    /**
     * url元素节点名
     */
    public static final String RAWURL = "url";

    /**
     * name元素节点的值
     */
    public String mName = null;

    /**
     * url元素节点的值
     */
    public String mUrl = null;

    @Override
    public String toString() {
        return "Othersearchobject [name=" + mName + ", url=" + mUrl + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mName);
        arg0.writeString(mUrl);
    }

    public static final Creator<RemoteOtherObject> CREATOR = new Creator<RemoteOtherObject>() {
        @Override
        public RemoteOtherObject createFromParcel(Parcel source) {
            RemoteOtherObject info = new RemoteOtherObject();
            info.mName = source.readString();
            info.mUrl = source.readString();
            return info;
        }

        @Override
        public RemoteOtherObject[] newArray(int size) {
            return new RemoteOtherObject[size];
        }
    };

}
