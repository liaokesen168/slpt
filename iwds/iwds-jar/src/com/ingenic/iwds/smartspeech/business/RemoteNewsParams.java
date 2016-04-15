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
 * params节点的抽象,此节点包括所有用于后续请求业务参数
 */
public class RemoteNewsParams implements Parcelable {

    /**
     * media元素节点名。节点意义：新闻媒体名称
     */
    public static final String RAWMEDIA = "media";

    /**
     * category元素节点名。节点意义：新闻分类
     */
    public static final String RAWCATEGORY = "category";

    /**
     * loc元素节点名。节点意义：新闻发生的地域
     */
    public static final String RAWLOC = "loc";

    /**
     * keyword元素节点名。节点意义：关键词
     */
    public static final String RAWKEYWORD = "keyword";

    /**
     * datetime元素节点名。节点意义：日期
     */
    public static final String RAWDATETIME = "datetime";

    /**
     * newsid元素节点名。节点意义：根据data_source而不同的特定参数，表示新闻id
     */
    public static final String RAWNEWSID = "newsid";

    /**
     * remains元素节点名。节点意义：根据data_source而不同的特定参数，表示剩余新闻数量
     */
    public static final String RAWREMAINS = "remains";

    /**
     * categoryids元素节点名。节点意义：根据data_source而不同的特定参数，表示分类id
     */
    public static final String RAWCATEGORYIDS = "categoryids";

    /**
     * datetime元素节点的date属性节点名。节点意义：日期
     */
    public static final String ATTRDATE = "date";

    /**
     * media元素节点的值
     */
    public String mMedia = null;

    /**
     * category元素节点的值
     */
    public String mCategory = null;

    /**
     * loc元素节点的值
     */
    public String mLoc = null;

    /**
     * keyword元素节点的值
     */
    public String mKeyword = null;

    /**
     * datetime元素节点的date属性节点的值
     */
    public String mDateTime = null;

    /**
     * newsid元素节点的值
     */
    public String mNewsid = null;

    /**
     * remains元素节点的值
     */
    public String mRemains = null;

    /**
     * categoryids元素节点的值
     */
    public String mcategoryids = null;

    @Override
    public String toString() {
        return "Newsparams [media=" + mMedia + ", category=" + mCategory
                + ", loc=" + mLoc + ", keyword=" + mKeyword + ", datetime="
                + mDateTime + ", newsid=" + mNewsid + ", remains=" + mRemains
                + ", categoryids=" + mcategoryids + "]";
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mMedia);
        arg0.writeString(mCategory);
        arg0.writeString(mLoc);
        arg0.writeString(mKeyword);
        arg0.writeString(mDateTime);
        arg0.writeString(mNewsid);
        arg0.writeString(mRemains);
        arg0.writeString(mcategoryids);
    }

    public static final Creator<RemoteNewsParams> CREATOR = new Creator<RemoteNewsParams>() {
        @Override
        public RemoteNewsParams createFromParcel(Parcel source) {
            RemoteNewsParams info = new RemoteNewsParams();
            info.mMedia = source.readString();
            info.mCategory = source.readString();
            info.mLoc = source.readString();
            info.mKeyword = source.readString();
            info.mDateTime = source.readString();
            info.mNewsid = source.readString();
            info.mRemains = source.readString();
            info.mcategoryids = source.readString();
            return info;
        }

        @Override
        public RemoteNewsParams[] newArray(int size) {
            return new RemoteNewsParams[size];
        }
    };
}
