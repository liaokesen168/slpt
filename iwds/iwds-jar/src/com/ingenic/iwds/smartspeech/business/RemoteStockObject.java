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
 * stock的object抽象，表示股票业务信息类
 */
public class RemoteStockObject extends RemoteBusinessObject {
    /**
     * RemoteStockobject的object标识
     */
    public static String sFocus = "stock";

    /**
     * name元素节点名，表示股票的名称
     */
    public static final String RAWNAME = "name";

    /**
     * code元素节点名，表示股票的代码
     */
    public static final String RAWCODE = "code";

    /**
     * category元素节点名，表示股票所属证交所。sz：深市；sh：沪市；hk：港股
     */
    public static final String RAWCATEGORY = "category";

    /**
     * type元素节点名，表示表示结果的类型。
     * 可能的取值：known表示股票是集内结果， 
     * unknown表示股票是集外结果 
     */
    public static final String RAWTYPE = "type";

    /**
     * url元素节点名，表示查询股票信息的的url
     */
    public static final String RAWURL = "url";

    /**
     * data元素节点名，表示股票信息数据
     */
    public static final String RAWDATA = "data";

    /**
     * name元素节点的值
     */
    public String mName = null;

    /**
     * code元素节点的值
     */
    public String mCode = null;

    /**
     * category元素节点的值
     */
    public String mCategory = null;

    /**
     * type元素节点的值
     */
    public String mType = null;

    /**
     * url元素节点的值
     */
    public String mUrl = null;

    /**
     * data元素节点的值
     */
    public RemoteStockDate mData = null;

    @Override
    public String toString() {
        return "Stockobject [name=" + mName + ", code=" + mCode + ", category="
                + mCategory + ", type=" + mType + ", url=" + mUrl + ", data="
                + mData + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mName);
        arg0.writeString(mCode);
        arg0.writeString(mCategory);
        arg0.writeString(mType);
        arg0.writeString(mUrl);
        arg0.writeParcelable(mData, arg1);
    }

    public static final Creator<RemoteStockObject> CREATOR = new Creator<RemoteStockObject>() {
        @Override
        public RemoteStockObject createFromParcel(Parcel source) {
            RemoteStockObject info = new RemoteStockObject();
            info.mName = source.readString();
            info.mCode = source.readString();
            info.mCategory = source.readString();
            info.mType = source.readString();
            info.mUrl = source.readString();
            info.mData = source.readParcelable(RemoteStockDate.class
                    .getClassLoader());
            return info;
        }

        @Override
        public RemoteStockObject[] newArray(int size) {
            return new RemoteStockObject[size];
        }
    };
}
