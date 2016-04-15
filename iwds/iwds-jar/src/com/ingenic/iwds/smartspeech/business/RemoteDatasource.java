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
 * data_source节点的抽象，表示数据来源信息的类
 */
public class RemoteDatasource implements Parcelable {

    /**
     * id元素节点名，表示数据来源的唯一标识符
     */
    public static final String ATTRID = "id";

    /**
     * name元素节点名，数据来源的名称
     */
    public static final String ATTRNAME = "name";

    /**
     * id元素节点的值
     */
    public String mId = null;

    /**
     * name元素节点的值
     */
    public String mName = null;

    public RemoteDatasource(String Tid, String Tname) {
        mId = Tid;
        mName = Tname;
    }

    public RemoteDatasource() {
    }

    @Override
    public String toString() {
        return "Datasource [id=" + mId + ", name=" + mName + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mId);
        arg0.writeString(mName);
    }

    public static final Creator<RemoteDatasource> CREATOR = new Creator<RemoteDatasource>() {
        @Override
        public RemoteDatasource createFromParcel(Parcel source) {
            RemoteDatasource info = new RemoteDatasource();
            info.mId = source.readString();
            info.mName = source.readString();
            return info;
        }

        @Override
        public RemoteDatasource[] newArray(int size) {
            return new RemoteDatasource[size];
        }
    };
}
