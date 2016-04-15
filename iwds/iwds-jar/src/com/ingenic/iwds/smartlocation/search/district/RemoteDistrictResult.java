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

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 行政区域查询结果类
 */
public class RemoteDistrictResult implements Parcelable {
    private int pageCount;
    private RemoteDistrictQuery searchQuery;
    private ArrayList<RemoteDistrictItem> districtList = new ArrayList<RemoteDistrictItem>();

    /**
     * RemoteDistrictResult构造函数
     */
    public RemoteDistrictResult() {

    }

    /**
     * RemoteDistrictResult构造函数
     * 
     * @param searchQuery
     *            驾车路径查询规划
     * 
     * @param disctrictList
     *            行政区信息
     */
    public RemoteDistrictResult(RemoteDistrictQuery searchQuery,
            ArrayList<RemoteDistrictItem> disctrictList) {
        this.searchQuery = searchQuery;
        this.districtList = disctrictList;
    }

    /**
     * 返回查询行政区的结果
     * 
     * @return 查询行政区的结果
     */
    public ArrayList<RemoteDistrictItem> getDistrict() {
        return this.districtList;
    }

    /**
     * 设置查询行政区的结构
     * 
     * @param districtList
     *            查询行政区的结构
     */
    public void setDistrict(ArrayList<RemoteDistrictItem> districtList) {
        this.districtList = districtList;
    }

    /**
     * 返回查询结果对应的查询参数
     * 
     * @return 查询结果对应的查询参数
     */
    public RemoteDistrictQuery getQuery() {
        return this.searchQuery;
    }

    /**
     * 设置查询结果对应的查询参数
     * 
     * @param searchQuery
     *            查询结果对应的查询参数
     */
    public void setQuery(RemoteDistrictQuery searchQuery) {
        this.searchQuery = searchQuery;
    }

    /**
     * 返回查询结果的页数
     * 
     * @return 查询结果的页数
     */
    public int getPageCount() {
        return this.pageCount;
    }

    /**
     * 设置查询结果的页数
     * 
     * @param pageCount
     *            查询结果的页数
     */
    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (!(object instanceof RemoteDistrictResult))
            return false;

        RemoteDistrictResult other = (RemoteDistrictResult) object;

        if (this.searchQuery == null) {
            if (other.searchQuery != null)
                return false;
        } else if (!this.searchQuery.equals(other.searchQuery))
            return false;
        if (this.districtList == null) {
            if (other.districtList != null)
                return false;
        } else if (!this.districtList.equals(other.districtList))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = prime * result
                + (this.searchQuery == null ? 0 : this.searchQuery.hashCode());
        result = prime
                * result
                + (this.districtList == null ? 0 : this.districtList.hashCode());

        return result;
    }

    @Override
    public String toString() {
        return "RemoteDistrictResult [" + "districtSearchQuery=" + searchQuery
                + ", districtList=" + districtList + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(pageCount);

        if (this.districtList != null) {
            dest.writeInt(1);
            dest.writeList(districtList);
        } else {
            dest.writeInt(0);
        }

        if (this.searchQuery != null) {
            dest.writeInt(1);
            dest.writeParcelable(searchQuery, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteDistrictResult> CREATOR = new Creator<RemoteDistrictResult>() {

        @Override
        public RemoteDistrictResult createFromParcel(Parcel source) {
            RemoteDistrictResult districtResult = new RemoteDistrictResult();

            districtResult.pageCount = source.readInt();

            if (source.readInt() != 0) {
                districtResult.districtList = source
                        .readArrayList(RemoteDistrictItem.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                districtResult.searchQuery = source
                        .readParcelable(RemoteDistrictQuery.class
                                .getClassLoader());
            }

            return districtResult;
        }

        @Override
        public RemoteDistrictResult[] newArray(int size) {
            return new RemoteDistrictResult[size];
        }
    };
}
