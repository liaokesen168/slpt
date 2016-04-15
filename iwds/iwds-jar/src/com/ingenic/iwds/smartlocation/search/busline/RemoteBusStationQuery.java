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
 * 公交站点搜索的关键字和城市信息类
 */
public class RemoteBusStationQuery implements Parcelable, Cloneable {
    private String city;
    private int pageNum;
    private int pageSize = 10;
    private String queryString;

    /**
     * RemoteBusStationQuery构造函数
     */
    public RemoteBusStationQuery() {

    }

    /**
     * RemoteBusStationQuery构造函数
     * 
     * @param city
     *            可选值：cityname（中文或中文全拼）、citycode、adcode。 如传入null或空字符串则为“全国”
     * 
     * @param queryString
     *            查询关键字。关键字规则：多个关键字用“|”分割，“空格"表示与， "双引号" 表示不可分割
     */
    public RemoteBusStationQuery(String city, String queryString) {
        this.city = city;
        this.queryString = queryString;
        if (this.queryString == null || this.queryString.isEmpty())
            throw new IllegalArgumentException("Empty query string");
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
     * 设置查询城市参数
     * 
     * @param city
     *            查询城市参数。参数可以为城市编码/行政区划代码/城市名称
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
        if (pageSize > 20) {
            pageSize = 20;
        }

        if (pageSize <= 0) {
            pageSize = 10;
        }

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

        RemoteBusStationQuery busStationQuery = new RemoteBusStationQuery(
                this.city, this.queryString);
        busStationQuery.setPageNumber(this.pageNum);
        busStationQuery.setPageSize(this.pageSize);

        return busStationQuery;
    }

    /**
     * 比较两个查询条件是否相同
     * 
     * @param object
     *            查询条件
     * 
     * @return 查询条件是否相同
     */
    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (!(object instanceof RemoteBusStationQuery))
            return false;

        RemoteBusStationQuery other = (RemoteBusStationQuery) object;

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
    }

    public static final Creator<RemoteBusStationQuery> CREATOR = new Creator<RemoteBusStationQuery>() {

        @Override
        public RemoteBusStationQuery createFromParcel(Parcel source) {
            RemoteBusStationQuery busStationQuery = new RemoteBusStationQuery();

            busStationQuery.city = source.readString();
            busStationQuery.pageNum = source.readInt();
            busStationQuery.pageSize = source.readInt();
            busStationQuery.queryString = source.readString();

            return busStationQuery;
        }

        @Override
        public RemoteBusStationQuery[] newArray(int size) {
            return new RemoteBusStationQuery[size];
        }
    };
}
