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
 * item节点的抽象,表示单个新闻内容
 */
public class RemoteNewsItem implements Parcelable {

    /**
     * title元素节点名。节点意义：新闻标题
     */
    public static final String RAWTITLE = "title";

    /**
     * marked_title元素节点名。节点意义：标记过新闻标题
     */
    public static final String RAWMARKEDTITLE = "marked_title";

    /**
     * content元素节点名。节点意义：新闻内容
     */
    public static final String RAWCONTENT = "content";

    /**
     * marked_content元素节点名。节点意义：标记过新闻内容
     */
    public static final String RAWMARKEDCONTENT = "marked_content";

    /**
     * source元素节点名。节点意义：新闻来源
     */
    public static final String RAWSOURCE = "source";

    /**
     * url元素节点名。节点意义：可播放的url
     */
    public static final String RAWURL = "url";

    /**
     * title元素节点的值
     */
    public String mTitle = null;

    /**
     * marked_title元素节点的值
     */
    public String mMarkedTitle = null;

    /**
     * content元素节点的值
     */
    public String mContent = null;

    /**
     * marked_content元素节点的值
     */
    public String mMarkedcontent = null;

    /**
     * source元素节点的值
     */
    public String mSource = null;

    /**
     * url元素节点的值
     */
    public String mUrl = null;

    @Override
    public String toString() {
        return "NewsItem [title=" + mTitle + ", marked_title=" + mMarkedTitle
                + ", content=" + mContent + ", marked_content="
                + mMarkedcontent + ", source=" + mSource + ", url=" + mUrl
                + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mTitle);
        arg0.writeString(mMarkedTitle);
        arg0.writeString(mContent);
        arg0.writeString(mMarkedcontent);
        arg0.writeString(mSource);
        arg0.writeString(mUrl);
    }

    public static final Creator<RemoteNewsItem> CREATOR = new Creator<RemoteNewsItem>() {
        @Override
        public RemoteNewsItem createFromParcel(Parcel source) {
            RemoteNewsItem info = new RemoteNewsItem();
            info.mTitle = source.readString();
            info.mMarkedTitle = source.readString();
            info.mContent = source.readString();
            info.mMarkedcontent = source.readString();
            info.mSource = source.readString();
            info.mUrl = source.readString();
            return info;
        }

        @Override
        public RemoteNewsItem[] newArray(int size) {
            return new RemoteNewsItem[size];
        }
    };

}
