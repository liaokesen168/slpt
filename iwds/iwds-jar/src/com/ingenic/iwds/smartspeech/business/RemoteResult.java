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
import android.os.Parcelable;

/**
 * result节点的抽象,表示语义理解后，具体业务的结果
 */
public class RemoteResult implements Parcelable {

    /**
     * focus元素节点名，表示业务类型
     */
    public static final String RAWFOCUS = "focus";

    /**
     * action元素节点名，表示业务的行为信息
     */
    public static final String RAWACTION = "action";

    /**
     * object元素节点名，表示具体业务的信息
     */
    public static final String RAWOBJECT = "object";

    /**
     * content元素节点名，表示某些业务附带的内容，如发送短信的内容，查询短信的内容。
     */
    public static final String RAWCONTENT = "content";

    /**
     * content_type元素节点名，表示附带内容的类型， 比如查询祝福短信时，附带内容的类型为祝福
     */
    public static final String RAWCONTENTTYPE = "content_type";

    /**
     * focus元素节点的值
     */
    public String mFocus = null;

    /**
     * action元素节点的值
     */
    public RemoteAction mAction = null;

    /**
     * object元素节点的值
     */
    public RemoteBusinessObject mObject = null;

    /**
     * content元素节点的值
     */
    public String mContent = null;

    /**
     * content_type元素节点的值
     */
    public String mContentType = null;

    /**
     * object元素节点列表，有些业务的业务信息不止有一份，比如联系人可以有多个object
     */
    public List<RemoteBusinessObject> mObjects = null;

    @Override
    public String toString() {
        return "ResultModel [Focus=" + mFocus + ", Action=" + mAction
                + ", object=" + mObject + ", content=" + mContent
                + ", contenttype=" + mContentType + ", objects=" + mObjects
                + "]";
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mFocus);
        arg0.writeParcelable(mAction, arg1);
        arg0.writeParcelable(mObject, arg1);
        arg0.writeString(mContent);
        arg0.writeString(mContentType);
        arg0.writeList(mObjects);
    }

    public static final Creator<RemoteResult> CREATOR = new Creator<RemoteResult>() {
        @Override
        public RemoteResult createFromParcel(Parcel source) {
            RemoteResult info = new RemoteResult();
            info.mFocus = source.readString();
            info.mAction = source.readParcelable(RemoteAction.class
                    .getClassLoader());
            info.mObject = source.readParcelable(RemoteBusinessObject.class
                    .getClassLoader());
            info.mContent = source.readString();
            info.mContentType = source.readString();
            info.mObjects = new ArrayList<RemoteBusinessObject>();
            source.readList(info.mObjects,
                    RemoteBusinessObject.class.getClassLoader());
            return info;
        }

        @Override
        public RemoteResult[] newArray(int size) {
            return new RemoteResult[size];
        }
    };
}
