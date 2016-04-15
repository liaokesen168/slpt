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

import java.util.ArrayList;
import java.util.List;

import com.ingenic.iwds.smartlocation.search.core.RemotePoiItem;
import com.ingenic.iwds.smartlocation.search.core.RemoteSuggestionCity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * POI（Point Of Interest，兴趣点）搜索结果是分页显示的，从第0页开始，每页最多30个POI。 PoiResult
 * 封装了此分页结果，并且会缓存已经检索到的页的搜索结果。
 */
public class RemotePoiResult implements Parcelable {
    private RemotePoiSearchBound searchBound;
    private int pageCount;
    private List<RemotePoiItem> poiList = new ArrayList<RemotePoiItem>();
    private RemotePoiQuery query;
    private List<RemoteSuggestionCity> searchSuggestionCitys = new ArrayList<RemoteSuggestionCity>();
    private List<String> searchSuggestionKeywords = new ArrayList<String>();;

    /**
     * RemotePoiResult构造函数
     */
    public RemotePoiResult() {

    }

    /**
     * 返回该结果对应的范围参数
     * 
     * @return 该结果对应的范围参数
     */
    public RemotePoiSearchBound getBound() {
        return this.searchBound;
    }

    /**
     * 设置范围参数
     * 
     * @param searchBound
     *            范围参数
     */
    public void setBound(RemotePoiSearchBound searchBound) {
        this.searchBound = searchBound;
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
     * 返回当前页所有POI结果
     * 
     * @return 当前页所有POI结果
     */
    public List<RemotePoiItem> getPois() {
        return this.poiList;
    }

    /**
     * 设置当前页所有POI结果
     * 
     * @param poiList
     *            当前页所有POI结果
     */
    public void setPois(List<RemotePoiItem> poiList) {
        this.poiList = poiList;
    }

    /**
     * 返回该结果对应的查询参数
     * 
     * @return 该结果对应的查询参数
     */
    public RemotePoiQuery getQuery() {
        return this.query;
    }

    /**
     * 设置该结果对应的查询参数
     * 
     * @param query
     *            该结果对应的查询参数
     */
    public void setQuery(RemotePoiQuery query) {
        this.query = query;
    }

    /**
     * 返回建议的城市。 如果搜索关键字无返回结果时，引擎将建议此关键字在其他城市的搜索情况。
     * 返回的详细内容也见SuggestionCity类。如果有搜索结果则此方法必定返回空
     * 
     * @return 搜索建议的城市
     */
    public List<RemoteSuggestionCity> getSearchSuggestionCitys() {
        return this.searchSuggestionCitys;
    }

    /**
     * 设置搜索建议的城市
     * 
     * @param searchSuggestionCitys
     *            搜索建议的城市
     */
    public void setSearchSuggestionCitys(
            List<RemoteSuggestionCity> searchSuggestionCitys) {
        this.searchSuggestionCitys = searchSuggestionCitys;
    }

    /**
     * 返回搜索建议。 如果搜索关键字明显为误输入，则通过此方法得到搜索关键词建议。如搜索“早君庙”，搜索引擎给出建议为“皂君庙”。
     * 建议按照与查询关键词发音近似的原则给出。如果有搜索结果则此方法必定返回空
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

        if (this.searchBound != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.searchBound, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.poiList != null) {
            dest.writeInt(1);
            dest.writeList(this.poiList);
        } else {
            dest.writeInt(0);
        }

        if (this.query != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.query, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.searchSuggestionCitys != null) {
            dest.writeInt(1);
            dest.writeList(this.searchSuggestionCitys);
        } else {
            dest.writeInt(0);
        }

        if (this.searchSuggestionKeywords != null) {
            dest.writeInt(1);
            dest.writeStringList(this.searchSuggestionKeywords);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemotePoiResult> CREATOR = new Creator<RemotePoiResult>() {

        @Override
        public RemotePoiResult createFromParcel(Parcel source) {
            RemotePoiResult poiResult = new RemotePoiResult();

            poiResult.pageCount = source.readInt();

            if (source.readInt() != 0) {
                poiResult.searchBound = source
                        .readParcelable(RemotePoiSearchBound.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                poiResult.poiList = source.readArrayList(RemotePoiItem.class
                        .getClassLoader());
            }

            if (source.readInt() != 0) {
                poiResult.query = source.readParcelable(RemotePoiQuery.class
                        .getClassLoader());
            }

            if (source.readInt() != 0) {
                poiResult.searchSuggestionCitys = source
                        .readArrayList(RemoteSuggestionCity.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                source.readStringList(poiResult.searchSuggestionKeywords);
            }

            return poiResult;
        }

        @Override
        public RemotePoiResult[] newArray(int size) {
            return new RemotePoiResult[size];
        }

    };

}
