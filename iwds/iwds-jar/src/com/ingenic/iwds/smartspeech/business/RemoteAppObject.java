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
 * app业务的object节点的抽象，表示应用业务信息的类
 */
public class RemoteAppObject extends RemoteBusinessObject {

    /**
     * RemoteAppobject的object标识，可以区分其他的objec
     */
    public static String sFocus = "app";

    /**
     * name元素节点名，表示应用程序名称
     */
    public static final String RAWNAME = "name";

    /**
     * post_data元素节点名，表示网络搜索url
     */
    public static final String RAWPOSTDATA = "post_data";

    /**
     * search_url元素节点名，表示需要post的数据，不需要则为空
     */
    public static final String RAWSEARCHURL = "search_url";

    /**
     * name元素节点的值
     */
    public String mName = null;

    /**
     * post_data元素节点的值
     */
    public String mPostdata = null;

    /**
     * search_url元素节点的值
     */
    public String mSearchurl = null;

    @Override
    public String toString() {
        return "Appobject [name=" + mName + ", postdata=" + mPostdata
                + ", searchurl=" + mSearchurl + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mName);
        arg0.writeString(mPostdata);
        arg0.writeString(mSearchurl);
    }

    public static final Creator<RemoteAppObject> CREATOR = new Creator<RemoteAppObject>() {
        @Override
        public RemoteAppObject createFromParcel(Parcel source) {
            RemoteAppObject info = new RemoteAppObject();
            info.mName = source.readString();
            info.mPostdata = source.readString();
            info.mSearchurl = source.readString();
            return info;
        }

        @Override
        public RemoteAppObject[] newArray(int size) {
            return new RemoteAppObject[size];
        }
    };

}
