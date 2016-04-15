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
 * 此类定义了搜索的关键字，类别及城市
 */
public class RemotePoiQuery implements Parcelable {
    private String category;
    private String city;
    private int pageNum;
    private int pageSize = 20;
    private String queryString;
    private String queryLanguage = "zh-CN";
    private boolean limitDiscount;
    private boolean limitGroupbuy;
    private String poiId;
    private RemotePoiSearchBound bound;

    /**
     * RemotePoiQuery构造函数
     */
    public RemotePoiQuery() {

    }

    /**
     * RemotePoiQuery构造函数
     * 
     * @param queryString
     *            查询字符串，多个关键字用“|”分割
     * 
     * @param category
     *            POI 类型的组合，比如定义如下组合：餐馆|电影院|景点
     */
    public RemotePoiQuery(String queryString, String category) {
        this(queryString, category, null);
    }

    /**
     * RemotePoiQuery构造函数
     * 
     * @param queryString
     *            查询字符串，多个关键字用“|”分割
     * 
     * @param category
     *            POI 类型的组合，比如定义如下组合：餐馆|电影院|景点
     * 
     * @param city
     *            待查询城市（地区）的城市编码 citycode、城市名称（中文或中文全拼）、行政区划代码adcode。必设参数
     */
    public RemotePoiQuery(String queryString, String category, String city) {
        this.queryString = queryString;
        this.category = category;
        this.city = city;
    }

    /**
     * 返回待查字符串
     * 
     * @return 待查字符串
     */
    public String getQueryString() {
        return this.queryString;
    }

    /**
     * 设置待查字符串
     * 
     * @param queryString
     *            待查字符串
     */
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    /**
     * 返回语言种类
     * 
     * @return 语言种类
     */
    public String getQueryLanguage() {
        return this.queryLanguage;
    }

    /**
     * 设置语言种类
     * 
     * @param queryLanguage
     *            语言种类
     */
    public void setQueryLanguage(String queryLanguage) {
        if (queryLanguage.equals("en")) {
            this.queryLanguage = "en";
        } else {
            this.queryLanguage = "zh-CN";
        }
    }

    /**
     * 是否查团购的信息，默认为false
     * 
     * @param limitGroupbuy
     *            布尔类型参数
     */
    public void setLimitGroupbuy(boolean limitGroupbuy) {
        this.limitGroupbuy = limitGroupbuy;
    }

    /**
     * 返回是否查团购的信息
     * 
     * @return 是否查团购的信息
     */
    public boolean hasGroupBuyLimit() {
        return this.limitGroupbuy;
    }

    /**
     * 是否查优惠的信息，默认为false
     * 
     * @param limitDiscount
     *            布尔类型参数
     */
    public void setLimitDiscount(boolean limitDiscount) {
        this.limitDiscount = limitDiscount;
    }

    /**
     * 返回是否查优惠的信息
     * 
     * @return 是否查优惠的信息
     */
    public boolean hasDiscountLimit() {
        return this.limitDiscount;
    }

    /**
     * 返回待查分类组合
     * 
     * @return 待查分类组合
     */
    public String getCategory() {
        return this.category;
    }

    /**
     * 设置待查分类组合
     * 
     * @param category
     *            待查分类组合
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * 返回待查城市（地区）的电话区号
     * 
     * @return 待查城市（地区）的电话区号
     */
    public String getCity() {
        return this.city;
    }

    /**
     * 设置城市（地区）的电话区号
     * 
     * @param city
     *            城市（地区）的电话区号
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * 获取设置查询的是第几页，从0开始
     * 
     * @return 设置查询的是第几页，从0开始
     */
    public int getPageNum() {
        return this.pageNum;
    }

    /**
     * 设置查询的是第几页，从0开始
     * 
     * @param pageNum
     *            设置查询的是第几页，从0开始
     */
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    /**
     * 设置的查询页面的结果数目
     * 
     * @param pageSize
     *            查询页面的结果数目
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 获取设置的查询页面的结果数目
     * 
     * @return 设置的查询页面的结果数目
     */
    public int getPageSize() {
        return this.pageSize;
    }

    /**
     * 设置POI 的ID号
     * 
     * @param poiId
     *            POI 的ID号
     */
    public void setPoiId(String poiId) {
        this.poiId = poiId;
    }

    /**
     * 获取POI 的ID号
     * 
     * @return POI 的ID号
     */
    public String getPoiId() {
        return this.poiId;
    }

    /**
     * 获取POI的位置所在圆形或矩形
     * 
     * @return POI的位置所在圆形或矩形
     */
    public RemotePoiSearchBound getBound() {
        return this.bound;
    }

    /**
     * 设置POI的位置所在圆形或矩形
     * 
     * @param bound
     *            POI的位置所在圆形或矩形
     */
    public void setBound(RemotePoiSearchBound bound) {
        this.bound = bound;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (!(object instanceof RemotePoiQuery))
            return false;

        RemotePoiQuery other = (RemotePoiQuery) object;

        if (this.category == null) {
            if (other.category != null)
                return false;
        } else if (!this.category.equals(other.category))
            return false;
        if (this.city == null) {
            if (other.city != null)
                return false;
        } else if (!this.city.equals(other.city))
            return false;
        if (this.pageNum != other.pageNum)
            return false;
        if (this.pageSize != other.pageSize)
            return false;
        if (this.queryString == null) {
            if (other.queryString != null)
                return false;
        } else if (!this.queryString.equals(other.queryString))
            return false;
        if (this.queryLanguage == null) {
            if (other.queryLanguage != null)
                return false;
        } else if (!this.queryLanguage.equals(other.queryLanguage))
            return false;
        if (this.limitDiscount != other.limitDiscount)
            return false;
        if (this.limitGroupbuy != other.limitGroupbuy)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = prime * result
                + (this.category == null ? 0 : this.category.hashCode());
        result = prime * result
                + (this.city == null ? 0 : this.city.hashCode());
        result = prime * result + this.pageNum;
        result = prime * result + this.pageSize;
        result = prime * result
                + (this.queryString == null ? 0 : this.queryString.hashCode());
        result = prime
                * result
                + (this.queryLanguage == null ? 0 : this.queryLanguage
                        .hashCode());
        result = prime * result + (this.limitDiscount ? 1231 : 1237);
        result = prime * result + (this.limitGroupbuy ? 1231 : 1237);

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.category);
        dest.writeString(this.city);
        dest.writeInt(this.pageNum);
        dest.writeInt(this.pageSize);
        dest.writeString(this.queryLanguage);
        dest.writeString(this.queryString);
        dest.writeByte((byte) (this.limitDiscount ? 1 : 0));
        dest.writeByte((byte) (this.limitGroupbuy ? 1 : 0));
        dest.writeString(this.poiId);
        if (this.bound != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.bound, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemotePoiQuery> CREATOR = new Creator<RemotePoiQuery>() {

        @Override
        public RemotePoiQuery createFromParcel(Parcel source) {
            RemotePoiQuery query = new RemotePoiQuery();

            query.category = source.readString();
            query.city = source.readString();
            query.pageNum = source.readInt();
            query.pageSize = source.readInt();
            query.queryLanguage = source.readString();
            query.queryString = source.readString();

            if (source.readByte() == 1)
                query.limitDiscount = true;
            else
                query.limitDiscount = false;

            if (source.readByte() == 1)
                query.limitGroupbuy = true;
            else
                query.limitGroupbuy = false;

            query.poiId = source.readString();

            if (source.readInt() != 0)
                query.bound = source.readParcelable(RemotePoiSearchBound.class
                        .getClassLoader());

            return query;
        }

        @Override
        public RemotePoiQuery[] newArray(int size) {
            return new RemotePoiQuery[size];
        }

    };

}
