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
 * dialog的object的抽象,表示对话业务信息的类
 */
public class RemoteDialogObject extends RemoteBusinessObject {

    /**
     * RemoteDialogobject的object标识，可以区分其他的objec
     */
    public static String sFocus = "dialog";

    /**
     * topic元素节点名，对话的主题
     */
    public static final String RAWTOPIC = "topic";

    /**
     * question元素节点名，你说话的内容
     */
    public static final String RAWQUESTION = "question";

    /**
     * answer元素节点名，语义理解后作的回答内容
     */
    public static final String RAWANSWER = "answer";

    /**
     * audio_url元素节点名，音频的链接地址
     */
    public static final String RAWAUDIOURL = "audio_url";

    /**
     * pic_url元素节点名，图片的链接地址
     */
    public static final String RAWPICURL = "pic_url";

    /**
     * page_url元素节点名，网页的链接地址
     */
    public static final String RAWPAGEURL = "page_url";

    /**
     * topic元素节点的值
     */
    public String mTopic = null;

    /**
     * question元素节点的值
     */
    public String mQuestion = null;

    /**
     * answer元素节点的值
     */
    public String mAnswer = null;

    /**
     * audio_url元素节点的值
     */
    public String mAudioUrl = null;

    /**
     * pic_url元素节点的值
     */
    public String mPicUrl = null;

    /**
     * page_url元素节点的值
     */
    public String mPageUrl = null;

    @Override
    public String toString() {
        return "Dialogobject [topic=" + mTopic + ", question=" + mQuestion
                + ", answer=" + mAnswer + ", audio_url=" + mAudioUrl
                + ", pic_url=" + mPicUrl + ", page_url=" + mPageUrl + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mTopic);
        arg0.writeString(mQuestion);
        arg0.writeString(mAnswer);
        arg0.writeString(mAudioUrl);
        arg0.writeString(mPicUrl);
        arg0.writeString(mPageUrl);
    }

    public static final Creator<RemoteDialogObject> CREATOR = new Creator<RemoteDialogObject>() {
        @Override
        public RemoteDialogObject createFromParcel(Parcel source) {
            RemoteDialogObject info = new RemoteDialogObject();
            info.mTopic = source.readString();
            info.mQuestion = source.readString();
            info.mAnswer = source.readString();
            info.mAudioUrl = source.readString();
            info.mPicUrl = source.readString();
            info.mPageUrl = source.readString();
            return info;
        }

        @Override
        public RemoteDialogObject[] newArray(int size) {
            return new RemoteDialogObject[size];
        }
    };

}
