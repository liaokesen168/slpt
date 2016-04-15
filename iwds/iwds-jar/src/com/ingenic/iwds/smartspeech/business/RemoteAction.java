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
 * action节点的抽象,表示一个业务的行为信息
 */
public class RemoteAction implements Parcelable {

    /**
     * channel 通道的值
     */
    public String mChannel = null;

    /**
     * operation 操作的值
     */
    public String mOperation = null;

    /**
     * chananl 通道名
     * 在telephone和message业务中，值用来区分服务平台，值有移动、联通、电信
     * 在other业务中，值用来区分搜索引擎，值有百度、谷歌、必应
     */
    public static final String RAWCHANNAL = "chanel";

    /**
     * opreation 操作名，值用来表示行为action的操作类行，如打开、查询、搜索等
     */
    public static final String RAWOPERATION = "operation";

    public RemoteAction(String Topration, String Tchanal) {
        mChannel = Tchanal;
        mOperation = Topration;
    }

    public RemoteAction() {
    }

    @Override
    public String toString() {
        return "ActionModel [chanal=" + mChannel + ", opration=" + mOperation
                + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mChannel);
        arg0.writeString(mOperation);
    }

    public static final Creator<RemoteAction> CREATOR = new Creator<RemoteAction>() {
        @Override
        public RemoteAction createFromParcel(Parcel source) {
            RemoteAction info = new RemoteAction();
            info.mChannel = source.readString();
            info.mOperation = source.readString();
            return info;
        }

        @Override
        public RemoteAction[] newArray(int size) {
            return new RemoteAction[size];
        }
    };
}
