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

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;

/**
 * contacts的object抽象，表示联系人业务信息的类
 */
public class RemoteContactsObject extends RemoteBusinessObject {
    /**
     * RemoteContactsobject的object标识，可以区分其他的objec
     */
    public static String sFocus = "contacts";

    /**
     * name元素节点名，联系人名称
     */
    public static final String RAWNAME = "name";

    /**
     * category元素节点名，对联系人的描述
     */
    public static final String RAWCATEGORY = "category";

    /**
     * code元素节点名，联系人的号码（用于新建联系人）
     */
    public static final String RAWCODE = "code";

    /**
     * name元素节点的值
     */
    public List<String> mNames = null;

    /**
     * category元素节点的值
     */
    public String mCategory = null;

    /**
     * code元素节点的值
     */
    public String mCode = null;

    @Override
    public String toString() {
        return "Contactsobject [name=" + mNames + ", category=" + mCategory
                + ", code=" + mCode + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mCategory);
        arg0.writeString(mCode);
        arg0.writeList(mNames);
    }

    public static final Creator<RemoteContactsObject> CREATOR = new Creator<RemoteContactsObject>() {
        @Override
        public RemoteContactsObject createFromParcel(Parcel source) {
            RemoteContactsObject info = new RemoteContactsObject();
            info.mCategory = source.readString();
            info.mCode = source.readString();
            info.mNames = new ArrayList<String>();
            source.readList(info.mNames, String.class.getClassLoader());
            return info;
        }

        @Override
        public RemoteContactsObject[] newArray(int size) {
            return new RemoteContactsObject[size];
        }
    };
}
