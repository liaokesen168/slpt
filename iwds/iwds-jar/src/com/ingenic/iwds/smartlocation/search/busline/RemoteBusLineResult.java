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

import java.util.ArrayList;
import java.util.List;

import com.ingenic.iwds.smartlocation.search.core.RemoteSuggestionCity;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 公交线路搜索结果是分页显示的，从第0页开始，每页默认显示20个BusLineItem。
 * RemoteBusLineResult封装了此分页结果，并且会缓存已经检索到的页的搜索结果
 */
public class RemoteBusLineResult implements Parcelable {
    private int pageCount;
    private List<RemoteBusLineItem> busLines = new ArrayList<RemoteBusLineItem>();
    private List<RemoteSuggestionCity> searchSuggestionCities = new ArrayList<RemoteSuggestionCity>();
    private List<String> searchSuggestionKeywords = new ArrayList<String>();
    private RemoteBusLineQuery query;

    /**
     * RemoteBusLineResult构造函数
     */
    public RemoteBusLineResult() {

    }

    /**
     * 返回该结果的总页数
     * 
     * @return 该结果的总页数
     */
    public int getPageCount() {
        return this.pageCount;
    }

    /**
     * 设置该结果的总页数
     * 
     * @param pageCount
     *            该结果的总页数
     */
    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    /**
     * 返回该结果对应的查询参数
     * 
     * @return 该结果对应的查询参数
     */
    public RemoteBusLineQuery getQuery() {
        return this.query;
    }

    /**
     * 设置该结果对应的查询参数
     * 
     * @param query
     *            该结果对应的查询参数
     */
    public void setQuery(RemoteBusLineQuery query) {
        this.query = query;
    }

    /**
     * 返回当前页对应的结果
     * 
     * @return 一个RemoteBusLineItem 的列表，其中每一项代表一个RemoteBusLine
     */
    public List<RemoteBusLineItem> getBusLines() {
        return this.busLines;
    }

    /**
     * 设置当前页对应的结果
     * 
     * @param busLines
     *            一个RemoteBusLineItem 的列表，其中每一项代表一个RemoteBusLine
     */
    public void setBusLines(List<RemoteBusLineItem> busLines) {
        this.busLines = busLines;
    }

    /**
     * 返回建议的城市。 如果搜索关键字无返回结果时，引擎将建议此关键字在其他城市的搜索情况。
     * 返回的详细内容见RemoteSuggestionCity类。如果有搜索结果则此方法必定返回空
     * 
     * @return 搜索建议的城市
     */
    public List<RemoteSuggestionCity> getSearchSuggestionCities() {
        return this.searchSuggestionCities;
    }

    /**
     * 设置建议的城市
     * 
     * @param searchSuggestionCities
     *            搜索建议的城市
     */
    public void setSearchSuggestionCities(
            List<RemoteSuggestionCity> searchSuggestionCities) {
        this.searchSuggestionCities = searchSuggestionCities;
    }

    /**
     * 返回搜索建议。 如果搜索关键字明显为误输入，则通过此方法得到搜索关键词建议。 如果有搜索结果则此方法必定返回空
     * 
     * @return 搜索建议
     */
    public List<String> getSearchSuggestionKeywords() {
        return this.searchSuggestionKeywords;
    }

    /**
     * 设置搜索关键词建议
     * 
     * @param searchSuggestionKeywords
     *            搜索关键词建议
     */
    public void setSearchSuggestionKeywords(
            List<String> searchSuggestionKeywords) {
        this.searchSuggestionKeywords = searchSuggestionKeywords;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.pageCount);

        if (this.busLines != null) {
            dest.writeInt(1);
            dest.writeList(this.busLines);
        } else {
            dest.writeInt(0);
        }

        if (this.searchSuggestionCities != null) {
            dest.writeInt(1);
            dest.writeList(this.searchSuggestionCities);
        } else {
            dest.writeInt(0);
        }

        if (this.searchSuggestionKeywords != null) {
            dest.writeInt(1);
            dest.writeStringList(this.searchSuggestionKeywords);
        } else {
            dest.writeInt(0);
        }

        if (this.query != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.query, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteBusLineResult> CREATOR = new Creator<RemoteBusLineResult>() {

        @Override
        public RemoteBusLineResult createFromParcel(Parcel source) {
            RemoteBusLineResult busLineResult = new RemoteBusLineResult();

            busLineResult.pageCount = source.readInt();

            if (source.readInt() != 0) {
                busLineResult.busLines = source
                        .readArrayList(RemoteBusLineItem.class.getClassLoader());
            }

            if (source.readInt() != 0) {
                busLineResult.searchSuggestionCities = source
                        .readArrayList(RemoteSuggestionCity.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                source.readStringList(busLineResult.searchSuggestionKeywords);
            }

            if (source.readInt() != 0) {
                busLineResult.query = source
                        .readParcelable(RemoteBusLineQuery.class
                                .getClassLoader());
            }

            return busLineResult;
        }

        @Override
        public RemoteBusLineResult[] newArray(int size) {
            return new RemoteBusLineResult[size];
        }

    };

}
