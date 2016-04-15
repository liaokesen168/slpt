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
 * news节点的抽象 ,表示新闻内容的对象，此对象包含0至多个表示具体单条新闻的item元素
 * @see RemoteNewsItem
 */
public class RemoteNews implements Parcelable {

    /**
     * type元素节点名。节点意义：表示新闻内容的类型，text或者audio
     */
    public static final String ATTRTYPE = "type";

    /**
     * item元素节点名。节点意义：表示具体的单条新闻
     */
    public static final String RAWITEM = "item";

    /**
     * type元素节点的值
     */
    public String mType = null;

    /**
     * item元素节点的值
     */
    public List<RemoteNewsItem> mNewsiTems = null;

    @Override
    public String toString() {
        return "News [type=" + mType + ", newsitems=" + mNewsiTems + "]";
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {

        arg0.writeString(mType);
        arg0.writeList(mNewsiTems);
    }

    public static final Creator<RemoteNews> CREATOR = new Creator<RemoteNews>() {
        @Override
        public RemoteNews createFromParcel(Parcel source) {
            RemoteNews info = new RemoteNews();

            info.mType = source.readString();
            info.mNewsiTems = new ArrayList<RemoteNewsItem>();
            source.readList(info.mNewsiTems,
                    RemoteNewsItem.class.getClassLoader());
            return info;
        }

        @Override
        public RemoteNews[] newArray(int size) {
            return new RemoteNews[size];
        }
    };

}
