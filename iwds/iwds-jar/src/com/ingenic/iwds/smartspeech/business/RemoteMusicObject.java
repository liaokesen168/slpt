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
 * music的object的抽象,表示音乐信息的类
 */
public class RemoteMusicObject extends RemoteBusinessObject {
    /**
     * RemoteMusicobject的object标识，用于区分其他的object
     */
    public static String sFocus = "music";
    /**
     * singer元素节点名，表示歌手
     */
    public static final String RAWSINGER = "singer";
    /**
     * song元素节点名，表示歌名
     */
    public static final String RAWSONG = "song";
    /**
     * category元素节点名，表示歌曲类型，如流行、经典、怀旧等
     */
    public static final String RAWCATEGORY = "category";

    /**
     * ms_response元素节点名，调用音乐搜索接口获得的结果（已进行base64编码）
     */
    public static final String RAWMSRESPONSE = "ms_response";
    /**
     * server_url元素节点名，用于后续音乐接口请求的url
     */
    public static final String RAWSERVERURL = "server_url";

    /**
     * singer元素节点的值
     */
    public String mSinger = null;

    /**
     * song元素节点的值
     */
    public String mSong = null;

    /**
     * category元素节点的值
     */
    public String mCategory = null;

    /**
     * ms_response元素节点的值
     */
    public String mMsResponse = null;

    /**
     * server_url元素节点的值
     */
    public String mServerUrl = null;

    @Override
    public String toString() {
        return "Musicobject [singer=" + mSinger + ", song=" + mSong
                + ", category=" + mCategory + ", ms_response=" + mMsResponse
                + ", server_url=" + mServerUrl + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mSinger);
        arg0.writeString(mSong);
        arg0.writeString(mCategory);
        arg0.writeString(mMsResponse);
        arg0.writeString(mServerUrl);
    }

    public static final Creator<RemoteMusicObject> CREATOR = new Creator<RemoteMusicObject>() {
        @Override
        public RemoteMusicObject createFromParcel(Parcel source) {
            RemoteMusicObject info = new RemoteMusicObject();
            info.mSinger = source.readString();
            info.mSong = source.readString();
            info.mCategory = source.readString();
            info.mMsResponse = source.readString();
            info.mServerUrl = source.readString();
            return info;
        }

        @Override
        public RemoteMusicObject[] newArray(int size) {
            return new RemoteMusicObject[size];
        }
    };
}
