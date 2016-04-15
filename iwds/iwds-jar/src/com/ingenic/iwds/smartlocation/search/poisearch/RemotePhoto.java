/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  ZhangYanMing <yanming.zhang@ingenic.com, jamincheung@126.com>
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

package com.ingenic.iwds.smartlocation.search.poisearch;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 定义一个图片信息
 */
public class RemotePhoto implements Parcelable {
    private String title;
    private String url;

    /**
     * RemotePhoto构造函数
     */
    public RemotePhoto() {

    }

    /**
     * RemotePhoto构造函数
     * 
     * @param title
     *            团购（或优惠）商品的图片标题
     * 
     * @param url
     *            团购（或优惠）商品的图片地址
     */
    public RemotePhoto(String title, String url) {
        this.title = title;
        this.url = url;
    }

    /**
     * 返回团购（或优惠）商品的图片标题
     * 
     * @return 团购（或优惠）商品的图片标题
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * 设置团购（或优惠）商品的图片标题
     * 
     * @param title
     *            团购（或优惠）商品的图片标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 返回团购（或优惠）商品的图片地址
     * 
     * @return 团购（或优惠）商品的图片地址
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * 设置团购（或优惠）商品的图片地址
     * 
     * @param url
     *            团购（或优惠）商品的图片地址
     */
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.url);
    }

    public static final Creator<RemotePhoto> CREATOR = new Creator<RemotePhoto>() {

        @Override
        public RemotePhoto createFromParcel(Parcel source) {
            RemotePhoto photo = new RemotePhoto();

            photo.title = source.readString();
            photo.url = source.readString();

            return photo;
        }

        @Override
        public RemotePhoto[] newArray(int size) {
            return new RemotePhoto[size];
        }

    };
}
