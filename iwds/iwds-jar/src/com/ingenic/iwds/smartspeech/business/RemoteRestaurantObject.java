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

/**
 * restaurant的object抽象，表示旅店业务信息的类
 */
public class RemoteRestaurantObject extends RemoteBusinessObject {
    
    /**
     * RemoteRestaurantobject的object标识，用于区分其他object
     */
    public static String sFocus = "restaurant";
    
    /**
     * data_type元素节点名，表示数据类型，有两种取值：
     * 1. url，表示本object的内容只包含一个url，客户端直接使用浏览器打开；
     * 2. data，表示本object包含的是用于界面显示的实际数据。
     * 客户端根据这个字段确定后续是直接打开URL还是进行其他数据展现
     */
    public static final String RAWDATATYPE = "data_type";
    
    /**
     * loc_judgement元素节点名，地点判断方式。
     * 1. 用户明确要求：demand；
     * 2. 服务端推断得出：infer
     */
    public static final String RAWLOCJUDGEMENT = "loc_judgement";
    
    /**
     * url元素节点名，客户端直接使用浏览器打开的url
     */
    public static final String RAWURL = "url";
    
    /**
     * data_source元素节点名，表示数据来源，同时用于后续请求
     */
    public static final String RAWDATA_SOURCE = "data_source";
    
    /**
     * city元素节点名，表示城市信息，同时用于后续请求
     */
    public static final String RAWCITY = "city";
    
    /**
     * category元素节点名，表示餐饮分类信息，同时用于后续请求
     */
    public static final String RAWCATEGORY = "category";
    
    /**
     * keyword元素节点名，表示查询使用的关键词，同时用于后续请求
     */
    public static final String RAWKEYWORD = "keyword";
    
    /**
     * server_url元素节点名，表示用于后续直接请求数据的url
     */
    public static final String RAWSERVERURL = "server_url";
    
    /**
     * shop_list元素节点名，表示商户列表，内含0个或多个shop元素
     */
    public static final String RAWSHOPLIST = "shop_list";
    
    /**
     * shop元素节点名，表示一个商戶的所有信息
     */
    public static final String RAWSHOP = "shop";
    
    /**
     * page_index元素节点名，表示当前结果页索引（最小为1）
     */
    public static final String RAWPAGEINDEX = "page_index";
    
    /**
     * page_total元素节点名，表示结果页总数
     */
    public static final String RAWPAGETOTAL = "page_total";
    
    /**
     * record_count元素节点名，表示结果总数
     */
    public static final String RAWRECORDCOUNT = "record_count";
    
    /**
     * alternative_source元素节点名，表示附带的其他可选数据源链接（例如移动12580链接）
     */
    public static final String RAWALTERNATIVESOURCE = "alternative_source";
    
    /**
     * data_type元素节点的值
     */
    public String mDataType = null;
    
    /**
     * loc_judgement元素节点的值
     */
    public String mLocJudgement = null;
    
    /**
     * url元素节点的值
     */
    public String mUrl = null;
    
    /**
     * data_source元素节点的值
     */
    public RemoteDatasource mDataSource = null;
    
    /**
     * city元素节点的值
     */
    public RemoteDatasource mCity = null;
    
    /**
     * category元素节点的值
     */
    public RemoteDatasource mCategory = null;
    
    /**
     * keyword元素节点的值
     */
    public String mKeyword = null;
    
    /**
     * server_url元素节点的值
     */
    public String mServerUrl = null;
    
    /**
     * shop_list元素节点的值
     */
    public List<RemoteShopDatasource> mShopList = null;
    
    /**
     * page_index元素节点的值
     */
    public String mPageIndex = null;
    
    /**
     * page_total元素节点的值
     */
    public String mPageTotal = null;
    
    /**
     * record_count元素节点的值
     */
    public String mRecorCount = null;
    
    /**
     * alternative_source元素节点的值
     */
    public RemoteAlternativesource mAlternativeSource = null;

    @Override
    public String toString() {
        return "Restaurantobject [data_type=" + mDataType + ", loc_judgement="
                + mLocJudgement + ", url=" + mUrl + ", data_source="
                + mDataSource + ", city=" + mCity + ", category=" + mCategory
                + ", keyword=" + mKeyword + ", server_url=" + mServerUrl
                + ", shop_list=" + mShopList + ", page_index=" + mPageIndex
                + ", page_total=" + mPageTotal + ", record_count="
                + mRecorCount + ", alternative_source=" + mAlternativeSource
                + "]";
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mDataType);
        arg0.writeString(mLocJudgement);
        arg0.writeString(mUrl);
        arg0.writeParcelable(mDataSource, arg1);
        arg0.writeParcelable(mCity, arg1);
        arg0.writeParcelable(mCategory, arg1);
        arg0.writeString(mKeyword);
        arg0.writeString(mServerUrl);
        arg0.writeList(mShopList);
        arg0.writeString(mPageIndex);
        arg0.writeString(mPageTotal);
        arg0.writeString(mRecorCount);
        arg0.writeParcelable(mAlternativeSource, arg1);
    }

    public static final Creator<RemoteRestaurantObject> CREATOR = new Creator<RemoteRestaurantObject>() {
        @Override
        public RemoteRestaurantObject createFromParcel(Parcel source) {
            RemoteRestaurantObject info = new RemoteRestaurantObject();
            info.mDataType = source.readString();
            info.mLocJudgement = source.readString();
            info.mUrl = source.readString();
            info.mDataSource = source.readParcelable(RemoteDatasource.class
                    .getClassLoader());
            info.mCity = source.readParcelable(RemoteDatasource.class
                    .getClassLoader());
            info.mCategory = source.readParcelable(RemoteDatasource.class
                    .getClassLoader());
            info.mKeyword = source.readString();
            info.mServerUrl = source.readString();
            info.mShopList = new ArrayList<RemoteShopDatasource>();
            source.readList(info.mShopList,
                    RemoteShopDatasource.class.getClassLoader());
            info.mPageIndex = source.readString();
            info.mPageTotal = source.readString();
            info.mRecorCount = source.readString();
            info.mAlternativeSource = source
                    .readParcelable(RemoteAlternativesource.class
                            .getClassLoader());
            return info;
        }

        @Override
        public RemoteRestaurantObject[] newArray(int size) {
            return new RemoteRestaurantObject[size];
        }
    };

}
