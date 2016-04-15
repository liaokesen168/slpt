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
 * biz_result节点的抽象,表示返回的结果，里面包含业务信息
 */
public class RemoteBusiness implements Parcelable {

    /**
     * status元素节点的值
     */
    public String mState = null;
    
    /**
     * error_code元素节点的值
     */
    public String mErrorCode = null;
    
    /**
     * time_stamp元素节点的值
     */
    public RemoteDateTime mTimeStamp = null;
    
    /**
     * desc元素节点的值
     */
    public String mDesc = null;
    
    /**
     * rawtext元素节点的值
     */
    public String mRawText = null;
    
    /**
     * result元素节点的值
     */
    public RemoteResult mResult = null;
    
    /**
     * status元素节点名，表示理解的结果状态，值有成功和失败
     */
    public static final String RAWSTATUS = "status";
    
    /**
     * error_code元素节点名，表示理解失败时返回的错误代码
     */
    public static final String RAWERRORCODE = "error_code";
    
    /**
     * time_stamp元素节点名，表示返回结果的时间
     */
    public static final String RAWTIMESTAMP = "time_stamp";
    
    /**
     * desc元素节点名，对返回结果的描述，包括对失败信息的描述
     */
    public static final String RAWDESC = "desc";
    
    /**
     * rawtext元素节点名，对您说的话进行语音识别的结果
     */
    public static final String RAWRAWTEXT = "rawtext";
    
    /**
     * result元素节点名，语义理解的结果
     */
    public static final String RAWRESULT = "result";

    @Override
    public String toString() {
        return "BusinessInfo [State=" + mState + ", error_code=" + mErrorCode
                + ", time_stamp=" + mTimeStamp + ", desc=" + mDesc
                + ", rawtext=" + mRawText + ", result=" + mResult + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mState);
        arg0.writeString(mErrorCode);
        arg0.writeParcelable(mTimeStamp, arg1);
        arg0.writeString(mDesc);
        arg0.writeString(mRawText);
        arg0.writeParcelable(mResult, arg1);
    }

    public static final Creator<RemoteBusiness> CREATOR = new Creator<RemoteBusiness>() {
        @Override
        public RemoteBusiness createFromParcel(Parcel source) {
            RemoteBusiness info = new RemoteBusiness();
            info.mState = source.readString();
            info.mErrorCode = source.readString();
            info.mTimeStamp = source.readParcelable(RemoteDateTime.class
                    .getClassLoader());
            info.mDesc = source.readString();
            info.mRawText = source.readString();
            info.mResult = source.readParcelable(RemoteResult.class
                    .getClassLoader());
            return info;
        }

        @Override
        public RemoteBusiness[] newArray(int size) {
            return new RemoteBusiness[size];
        }
    };

}
