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
 * 公交站点搜索结果是分页显示的，从第0页开始，每页默认显示20个RemoteBusStationItem。
 * RemoteBusStationResult封装了此分页结果，并且会缓存已经检索到的页的搜索结果
 */
public class RemoteBusStationResult implements Parcelable {
    private int pageCount;
    private List<RemoteBusStationItem> busStations = new ArrayList<RemoteBusStationItem>();
    private List<RemoteSuggestionCity> searchSuggestionCities = new ArrayList<RemoteSuggestionCity>();
    private List<String> searchSuggestionKeywords = new ArrayList<String>();
    private RemoteBusStationQuery query;

    /**
     * RemoteBusStationResult构造函数
     */
    public RemoteBusStationResult() {

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
    public RemoteBusStationQuery getQuery() {
        return this.query;
    }

    /**
     * 设置该结果对应的查询参数
     * 
     * @param query
     *            该结果对应的查询参数
     */
    public void setQuery(RemoteBusStationQuery query) {
        this.query = query;
    }

    /**
     * 返回公交站列表
     * 
     * @return 一个RemoteBusStationItem 的列表，其中每一项代表一个RemoteBusStation
     */
    public List<RemoteBusStationItem> getBusStations() {
        return this.busStations;
    }

    /**
     * 设置公交站列表
     * 
     * @param busStations
     *            一个RemoteBusStationItem 的列表
     */
    public void setBusStations(List<RemoteBusStationItem> busStations) {
        this.busStations = busStations;
    }

    /**
     * 返回建议的城市。 如果搜索关键字无返回结果时，引擎将建议此关键字在其他城市的搜索情况。
     * 返回的详细内容也见RemoteSuggestionCity类。如果有搜索结果则此方法必定返回空
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
     *            建议的城市
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
     * 设置搜索建议
     * 
     * @param searchSuggestionKeywords
     *            搜索建议
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

        if (this.busStations != null) {
            dest.writeInt(1);
            dest.writeList(this.busStations);
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
            dest.writeStringList(searchSuggestionKeywords);
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

    public static final Creator<RemoteBusStationResult> CREATOR = new Creator<RemoteBusStationResult>() {

        @Override
        public RemoteBusStationResult createFromParcel(Parcel source) {
            RemoteBusStationResult busStationResult = new RemoteBusStationResult();

            busStationResult.pageCount = source.readInt();

            if (source.readInt() != 0) {
                busStationResult.busStations = source
                        .readArrayList(RemoteBusStationItem.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                busStationResult.searchSuggestionCities = source
                        .readArrayList(RemoteSuggestionCity.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                source.readStringList(busStationResult.searchSuggestionKeywords);
            }

            if (source.readInt() != 0) {
                busStationResult.query = source
                        .readParcelable(RemoteBusStationQuery.class
                                .getClassLoader());
            }

            return busStationResult;
        }

        @Override
        public RemoteBusStationResult[] newArray(int size) {
            return new RemoteBusStationResult[size];
        }

    };
}
