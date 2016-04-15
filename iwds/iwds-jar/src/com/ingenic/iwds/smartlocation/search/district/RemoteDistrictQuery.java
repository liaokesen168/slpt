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

package com.ingenic.iwds.smartlocation.search.district;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 此类定义了行政区划搜索的参数
 */
public class RemoteDistrictQuery implements Parcelable, Cloneable {
    /**
     * 行政区划级别-国家
     */
    public static final String KEYWORDS_COUNTRY = "country";

    /**
     * 行政区划级别-省
     */
    public static final String KEYWORDS_PROVINCE = "province";

    /**
     * 行政区划级别-市
     */
    public static final String KEYWORDS_CITY = "city";

    /**
     * 行政区划级别-区/县
     */
    public static final String KEYWORDS_DISTRICT = "district";

    /**
     * 行政区划级别-商圈
     */
    public static final String KEYWORDS_BUSINESS = "biz_area";

    private int pageNum;
    private int pageSize = 20;
    private String keywords;
    private String keywordsLevel;
    private boolean isShowChild = true;

    /**
     * RemoteDistrictQuery构造函数
     */
    public RemoteDistrictQuery() {

    }

    /**
     * 根据给定的参数来构造一个 RemoteDistrictSearchQuery 的新对象
     * 
     * @param keywords
     *            关键词，支持：行政区名称、城市编码、区域编码
     * 
     * @param keywordsLevel
     *            关键词级别
     * 
     * @param pageNum
     *            查询第几页的结果，从0开始，默认为0，最大为第99页
     */
    public RemoteDistrictQuery(String keywords, String keywordsLevel,
            int pageNum) {
        this.keywords = keywords;
        this.keywordsLevel = keywordsLevel;
        this.pageNum = pageNum;
    }

    /**
     * 根据给定的参数来构造一个 RemoteDistrictSearchQuery 的新对象
     * 
     * @param keywords
     *            关键词，支持：行政区名称、城市编码、区域编码
     * 
     * @param keywordsLevel
     *            关键词级别
     * 
     * @param pageNum
     *            查询第几页的结果，从0开始，默认为0，最大为第99页
     * 
     * @param isShowChild
     *            是否显示下一级别的行政区
     * 
     * @param pageSize
     *            每页显示多少个数据，默认值是20 条，取值范围在[1-50]
     */
    public RemoteDistrictQuery(String keywords, String keywordsLevel,
            int pageNum, boolean isShowChild, int pageSize) {
        this(keywords, keywordsLevel, pageNum);
        this.isShowChild = isShowChild;
        this.pageSize = pageSize;
    }

    /**
     * 获取设置查询的是第几页，从0开始
     * 
     * @return 查询的是第几页
     */
    public int getPageNum() {
        return this.pageNum;
    }

    /**
     * 设置查询第几页的结果数目
     * 
     * @param pageNum
     *            查询第几页的结果，从0开始
     */
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    /**
     * 返回查询页面的结果数目
     * 
     * @return 查询页面的结果数目
     */
    public int getPageSize() {
        return this.pageSize;
    }

    /**
     * 设置查询每页的结果数目， 默认值是20 条，取值范围在[1-50]
     * 
     * @param pageSize
     *            每页的最大结果数目
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 返回查询时所用字符串关键字
     * 
     * @return 查询字符串关键字
     */
    public String getKeywords() {
        return this.keywords;
    }

    /**
     * 设置查询字符串关键字。关键词支持：行政区名称、城市编码、区域编码
     * 
     * @param keywords
     *            查询字符串关键字
     */
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    /**
     * 返回查询关键字的级别
     * 
     * @return 查询关键字的级别
     */
    public String getKeywordsLevel() {
        return this.keywordsLevel;
    }

    /**
     * 设置查询关键字的级别
     * 
     * @param keywordsLevel
     *            查询关键字的级别
     */
    public void setKeywordsLevel(String keywordsLevel) {
        this.keywordsLevel = keywordsLevel;
    }

    /**
     * 是否返回下级区划
     * 
     * @return true表示返回下级区划，反之，不返回下级区划
     */
    public boolean isShowChild() {
        return this.isShowChild;
    }

    /**
     * 设置是否返回下级区划
     * 
     * @param showChild
     *            是否返回下级区划，默认为true，返回下级区划
     */
    public void setShowChild(boolean showChild) {
        this.isShowChild = showChild;
    }

    /**
     * 返回是否市级以及以上的行政区划级别
     * 
     * @return 是否市级以及以上的行政区划级别
     */
    public boolean checkLevels() {
        if (this.keywordsLevel == null)
            return false;

        if (this.keywordsLevel.trim().equals(KEYWORDS_COUNTRY)
                || this.keywordsLevel.trim().equals(KEYWORDS_PROVINCE)
                || this.keywordsLevel.trim().equals(KEYWORDS_CITY))
            return true;

        return false;
    }

    /**
     * 返回查询字符串关键字是否为空
     * 
     * @return 查询字符串关键字非空则返回true，否则返回false
     */
    public boolean checkKeywords() {
        if (this.keywords == null)
            return false;

        if (this.keywords.trim().equalsIgnoreCase(""))
            return false;

        return true;
    }

    @Override
    protected Object clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return new RemoteDistrictQuery(this.keywords, this.keywordsLevel,
                this.pageNum, this.isShowChild, this.pageSize);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (!(object instanceof RemoteDistrictQuery))
            return false;

        RemoteDistrictQuery other = (RemoteDistrictQuery) object;

        if (this.keywords == null) {
            if (other.keywords != null)
                return false;
        } else if (!this.keywords.equals(other.keywords))
            return false;
        if (this.keywordsLevel == null) {
            if (other.keywordsLevel != null)
                return false;
        } else if (!this.keywordsLevel.equals(other.keywordsLevel))
            return false;
        if (this.pageNum != other.pageNum)
            return false;
        if (this.pageSize != other.pageSize)
            return false;
        if (this.isShowChild != other.isShowChild)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = prime * result
                + (this.keywords == null ? 0 : this.keywords.hashCode());
        result = prime
                * result
                + (this.keywordsLevel == null ? 0 : this.keywordsLevel
                        .hashCode());
        result = prime * result + this.pageNum;
        result = prime * result + this.pageSize;
        result = prime * result + (this.isShowChild ? 1231 : 1237);

        return result;
    }

    @Override
    public String toString() {
        return "RemoteDistrictSearchQuery [" + "pageNum=" + this.pageNum
                + ", pageSize=" + this.pageSize + ", keywords=" + this.keywords
                + ", keyworkdsLevel=" + this.keywordsLevel + ", isShowChild="
                + this.isShowChild + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.pageNum);
        dest.writeInt(this.pageSize);
        dest.writeString(this.keywords);
        dest.writeString(this.keywordsLevel);
        dest.writeByte((byte) (this.isShowChild ? 1 : 0));
    }

    public static final Creator<RemoteDistrictQuery> CREATOR = new Creator<RemoteDistrictQuery>() {

        @Override
        public RemoteDistrictQuery createFromParcel(Parcel source) {
            RemoteDistrictQuery searchQuery = new RemoteDistrictQuery();

            searchQuery.pageNum = source.readInt();
            searchQuery.pageSize = source.readInt();
            searchQuery.keywords = source.readString();
            searchQuery.keywordsLevel = source.readString();

            if (source.readByte() == 1)
                searchQuery.isShowChild = true;
            else
                searchQuery.isShowChild = false;

            return searchQuery;

        }

        @Override
        public RemoteDistrictQuery[] newArray(int size) {
            return new RemoteDistrictQuery[size];
        }

    };

}
