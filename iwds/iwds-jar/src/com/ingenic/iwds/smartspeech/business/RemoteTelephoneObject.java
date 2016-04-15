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
 * telephone的object抽象,表示电话业务的信息类
 */
public class RemoteTelephoneObject extends RemoteBusinessObject {
    /**
     * RemoteTelephoneobject的object标识
     */
    public static String sFocus = "telephone";
    /**
     * name元素节点名，表示联系人名称
     */
    public static final String RAWNAME = "name";
    /**
     * category元素节点名，表示对联系人的描述
     */
    public static final String RAWCATEGORY = "category";

    /**
     * name元素节点的值
     */
    public String mName = null;

    /**
     * category元素节点的值
     */
    public String mCategory = null;

    @Override
    public String toString() {
        return "Telephoneobject [name=" + mName + ", category=" + mCategory
                + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mName);
        arg0.writeString(mCategory);
    }

    public static final Creator<RemoteTelephoneObject> CREATOR = new Creator<RemoteTelephoneObject>() {
        @Override
        public RemoteTelephoneObject createFromParcel(Parcel source) {
            RemoteTelephoneObject info = new RemoteTelephoneObject();
            info.mName = source.readString();
            info.mCategory = source.readString();
            return info;
        }

        @Override
        public RemoteTelephoneObject[] newArray(int size) {
            return new RemoteTelephoneObject[size];
        }
    };
}
