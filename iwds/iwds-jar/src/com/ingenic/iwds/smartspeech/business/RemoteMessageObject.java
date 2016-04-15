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
 * message的object抽象，表示短信业务信息的类
 */
public class RemoteMessageObject extends RemoteBusinessObject {

    /**
     * RemoteMessageobject的object标识，用于区分其他的object
     */
    public static String sFocus = "message";

    /**
     * name元素节点名，表示联系人名称
     */
    public static final String RAWNAME = "name";

    /**
     * category元素节点名，对联系人的描述
     */
    public static final String RAWCATEGORY = "category";

    /**
     * name_type元素节点名，值为all时，表示所有联系人
     */
    public static final String RAWNAMETYPE = "name_type";

    /**
     * name元素节点的值
     */
    public List<String> mNames = null;

    /**
     * category元素节点的值
     */
    public String mCategory = null;

    /**
     * name_type元素节点的值
     */
    public String mNameType = null;

    @Override
    public String toString() {
        return "Messageobject [name=" + mNames + ", category=" + mCategory
                + ", nametype=" + mNameType + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mCategory);
        arg0.writeString(mNameType);
        arg0.writeList(mNames);
    }

    public static final Creator<RemoteMessageObject> CREATOR = new Creator<RemoteMessageObject>() {
        @Override
        public RemoteMessageObject createFromParcel(Parcel source) {
            RemoteMessageObject info = new RemoteMessageObject();
            info.mCategory = source.readString();
            info.mNameType = source.readString();
            info.mNames = new ArrayList<String>();
            source.readList(info.mNames, String.class.getClassLoader());
            return info;
        }

        @Override
        public RemoteMessageObject[] newArray(int size) {
            return new RemoteMessageObject[size];
        }
    };
}
