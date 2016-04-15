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
 * news的object节点的抽象,表示新闻业务信息的类
 *
 * @see RemoteNewsParams
 * @see RemoteNews
 */
public class RemoteNewsObject extends RemoteBusinessObject {

    /**
     * RemoteNewsobject的object标识，可以区分其他的objec
     */
    public static String sFocus = "news";

    /**
     * data_source元素节点名。节点意义：数据源，用于后续请求
     */
    public static final String RawDataSource = "data_source";

    /**
     * server_url元素节点名。节点意义：用于后续直接请求数据的url
     */
    public static final String RawServerUrl = "server_url";

    /**
     * param元素节点名。节点意义：业务参数，用于后续请求
     */
    public static final String RawParam = "param";

    /**
     * news元素节点名。节点意义：新闻内容
     */
    public static final String RawNews = "news";

    /**
     * data_source元素节点的值
     */
    public RemoteDatasource mDataSource = null;

    /**
     * server_url元素节点的值
     */
    public String mServerUrl = null;

    /**
     * param元素节点的值
     */
    public RemoteNewsParams mParam = null;
    /**
     * news元素节点的值
     */
    public RemoteNews mNews = null;

    @Override
    public String toString() {
        return "Newsobject [data_source=" + mDataSource + ", server_url="
                + mServerUrl + ", param=" + mParam + ", news=" + mNews + "]";
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {

        arg0.writeString(mServerUrl);
        arg0.writeParcelable(mDataSource, arg1);
        arg0.writeParcelable(mParam, arg1);
        arg0.writeParcelable(mNews, arg1);
    }

    public static final Creator<RemoteNewsObject> CREATOR = new Creator<RemoteNewsObject>() {
        @Override
        public RemoteNewsObject createFromParcel(Parcel source) {
            RemoteNewsObject info = new RemoteNewsObject();

            info.mServerUrl = source.readString();
            info.mDataSource = source.readParcelable(RemoteDateTime.class
                    .getClassLoader());
            info.mParam = source.readParcelable(RemoteNewsParams.class
                    .getClassLoader());
            info.mNews = source.readParcelable(RemoteNews.class
                    .getClassLoader());
            return info;
        }

        @Override
        public RemoteNewsObject[] newArray(int size) {
            return new RemoteNewsObject[size];
        }
    };
}
