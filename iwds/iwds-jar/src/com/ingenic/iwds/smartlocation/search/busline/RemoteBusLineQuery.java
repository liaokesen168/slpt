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

package com.ingenic.iwds.smartlocation.search.busline;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 公交线路搜索的关键字、类别及城市信息类
 */
public class RemoteBusLineQuery implements Parcelable, Cloneable {
    private String city;
    private int pageNum;
    private int pageSize = 10;
    private String queryString;
    private SearchType searchType;

    /**
     * 搜索类型
     */
    public static enum SearchType {
        /**
         * 根据线路ID搜索
         */
        BY_LINE_ID,

        /**
         * 根据线路名称搜索
         */
        BY_LINE_NAME;
    }

    /**
     * RemoteBusLineQuery构造函数
     */
    public RemoteBusLineQuery() {

    }

    /**
     * RemoteBusLineQuery构造函数
     * 
     * @param city
     *            可选值：cityname（中文或中文全拼）、citycode、adcode。 如传入null或空字符串则为“全国”
     * 
     * @param queryString
     *            查询关键字。关键字规则：多个关键字用“|”分割，“空格"表示与， "双引号" 表示不可分割
     * 
     * @param searchType
     *            查询类型
     */
    public RemoteBusLineQuery(String city, String queryString,
            SearchType searchType) {
        this.city = city;
        this.queryString = queryString;
        this.searchType = searchType;
        if (this.queryString == null || this.queryString.isEmpty()) {
            throw new IllegalArgumentException("Empty query string");
        }
    }

    /**
     * 返回查询类型
     * 
     * @return 该结果的查询类型
     */
    public SearchType getCategory() {
        return this.searchType;
    }

    /**
     * 设置查询类型
     * 
     * @param searchType
     *            查询类型
     */
    public void setCategory(SearchType searchType) {
        this.searchType = searchType;
    }

    /**
     * 返回查询关键字
     * 
     * @return 该结果的查询关键字
     */
    public String getQueryString() {
        return this.queryString;
    }

    /**
     * 设置查询关键字
     * 
     * @param queryString
     *            查询关键字
     */
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    /**
     * 返回查询城市编码/行政区划代码/城市名称
     * 
     * @return 查询城市参数
     */
    public String getCity() {
        return this.city;
    }

    /**
     * 设置查询城市参数，参数可以为城市编码/行政区划代码/城市名称
     * 
     * @param city
     *            查询城市参数
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * 获得查询每页的结果数目
     * 
     * @return 查询每页的结果数目
     */
    public int getPageSize() {
        return this.pageSize;
    }

    /**
     * 设置查询每页的结果数目
     * 
     * @param pageSize
     *            新的查询条件。默认每页显示20条结果，数目大于100按默认值
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 获得查询第几页的数据
     * 
     * @return 查询第几页，从0开始
     */
    public int getPageNumber() {
        return this.pageNum;
    }

    /**
     * 设置查询第几页
     * 
     * @param pageNum
     *            查询第几页的数据，从0开始
     */
    public void setPageNumber(int pageNum) {
        this.pageNum = pageNum;
    }

    @Override
    protected Object clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        RemoteBusLineQuery busLineQuery = new RemoteBusLineQuery(this.city,
                this.queryString, this.searchType);
        busLineQuery.setPageNumber(this.pageNum);
        busLineQuery.setPageSize(this.pageSize);

        return busLineQuery;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (!(object instanceof RemoteBusLineQuery))
            return false;

        RemoteBusLineQuery other = (RemoteBusLineQuery) object;

        if (this.city == null) {
            if (other.city != null)
                return false;
        } else if (!this.city.equals(other.city))
            return false;
        if (this.queryString == null) {
            if (other.queryString != null)
                return false;
        } else if (!this.queryString.equals(other.queryString))
            return false;
        if (this.searchType != other.searchType)
            return false;
        if (this.pageNum != other.pageNum)
            return false;
        if (this.pageSize != other.pageSize)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = prime * result
                + (this.searchType == null ? 0 : this.searchType.hashCode());
        result = prime * result
                + (this.city == null ? 0 : this.city.hashCode());
        result = prime * result
                + (this.queryString == null ? 0 : this.queryString.hashCode());
        result = prime * result + this.pageNum;
        result = prime * result + this.pageSize;

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.city);
        dest.writeInt(this.pageNum);
        dest.writeInt(this.pageSize);
        dest.writeString(this.queryString);
        if (this.searchType == SearchType.BY_LINE_ID)
            dest.writeInt(1);
        else
            dest.writeInt(0);
    }

    public static final Creator<RemoteBusLineQuery> CREATOR = new Creator<RemoteBusLineQuery>() {

        @Override
        public RemoteBusLineQuery createFromParcel(Parcel source) {
            RemoteBusLineQuery busLineQuery = new RemoteBusLineQuery();

            busLineQuery.city = source.readString();
            busLineQuery.pageNum = source.readInt();
            busLineQuery.pageSize = source.readInt();
            busLineQuery.queryString = source.readString();
            if (source.readInt() == 1) {
                busLineQuery.searchType = SearchType.BY_LINE_ID;
            } else {
                busLineQuery.searchType = SearchType.BY_LINE_NAME;
            }

            return busLineQuery;
        }

        @Override
        public RemoteBusLineQuery[] newArray(int size) {
            return new RemoteBusLineQuery[size];
        }
    };

}
