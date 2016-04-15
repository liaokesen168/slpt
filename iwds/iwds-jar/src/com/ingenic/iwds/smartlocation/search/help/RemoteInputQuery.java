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

package com.ingenic.iwds.smartlocation.search.help;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 帮助信息输入查询类
 */
public class RemoteInputQuery implements Parcelable, Cloneable {
    private String keyword;
    private String city;

    /**
     * RemoteInputQuery构造函数
     * 
     * @param keyword
     *            输入的关键字
     * 
     * @param city
     *            查询的城市编码citycode、城市名称（中文或中文全拼）、行政区划代码adcode。 设置null或“”为全国
     */
    public RemoteInputQuery(String keyword, String city) {
        this.keyword = keyword;
        this.city = city;
    }

    /**
     * 设置关键字
     * 
     * @param keyword
     *            关键字
     */
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    /**
     * 返回关键字
     * 
     * @return 关键字
     */
    public String getKeyword() {
        return this.keyword;
    }

    /**
     * 设置城市
     * 
     * @param city
     *            城市编码citycode、城市名称（中文或中文全拼）、行政区划代码adcode
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * 返回城市
     * 
     * @return 城市编码citycode、城市名称（中文或中文全拼）、行政区划代码adcode
     */
    public String getCity() {
        return this.city;
    }

    @Override
    protected Object clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return new RemoteInputQuery(this.keyword, this.city);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.keyword);
        dest.writeString(this.city);
    }

    public static final Creator<RemoteInputQuery> CREATOR = new Creator<RemoteInputQuery>() {

        @Override
        public RemoteInputQuery createFromParcel(Parcel source) {
            String keyword = source.readString();
            String city = source.readString();

            RemoteInputQuery query = new RemoteInputQuery(keyword, city);

            return query;
        }

        @Override
        public RemoteInputQuery[] newArray(int size) {
            return new RemoteInputQuery[size];
        }

    };

}
