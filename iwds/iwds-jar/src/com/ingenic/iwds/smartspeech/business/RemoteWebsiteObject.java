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
 * website的object抽象,表示网站业务信息类
 */
public class RemoteWebsiteObject extends RemoteBusinessObject {
    /**
     * RemoteWebsiteobject的object标识
     */
    public static String sFocus = "website";

    /**
     * code元素节点名，表示网站的url
     */
    public static final String RAWCODE = "code";

    /**
     * name元素节点名，表示网站名
     */
    public static final String RAWNAME = "name";

    /**
     * type元素节点名，表示结果的类型。
     * 可能的取值：known表示网站是集内结果， unknown表示网站是集外结果 
     */
    public static final String RAWTYPE = "type";

    /**
     * name元素节点的值
     */
    public String mName = null;

    /**
     * code元素节点的值
     */
    public String mCode = null;

    /**
     * type元素节点的值
     */
    public String mType = null;

    @Override
    public String toString() {
        return "Websiteobject [name=" + mName + ", code=" + mCode + ", type="
                + mType + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mName);
        arg0.writeString(mCode);
        arg0.writeString(mType);
    }

    public static final Creator<RemoteWebsiteObject> CREATOR = new Creator<RemoteWebsiteObject>() {
        @Override
        public RemoteWebsiteObject createFromParcel(Parcel source) {
            RemoteWebsiteObject info = new RemoteWebsiteObject();
            info.mName = source.readString();
            info.mCode = source.readString();
            info.mType = source.readString();
            return info;
        }

        @Override
        public RemoteWebsiteObject[] newArray(int size) {
            return new RemoteWebsiteObject[size];
        }
    };
}
