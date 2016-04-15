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

package com.ingenic.iwds.smartlocation.search.core;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 定义了当POI搜索无结果时，引擎对于搜索的城市建议内容
 */
public class RemoteSuggestionCity implements Parcelable {
    private String cityName;
    private String cityCode;
    private String adCode;
    private int suggestionNum;

    /**
     * 定义了当POI搜索无结果时，引擎对于搜索的城市建议内容
     * 
     * @param cityName
     *            城市名称
     * 
     * @param cityCode
     *            城市编码
     * 
     * @param adCode
     *            区域代码
     * 
     * @param suggestionNum
     *            此区域的建议结果数目
     */
    public RemoteSuggestionCity(String cityName, String cityCode,
            String adCode, int suggestionNum) {
        this.cityName = cityName;
        this.cityCode = cityCode;
        this.adCode = adCode;
        this.suggestionNum = suggestionNum;
    }

    /**
     * 返回城市名称
     * 
     * @return 城市名称
     */
    public String getCityName() {
        return this.cityName;
    }

    /**
     * 设置城市名称
     * 
     * @param cityName
     *            城市名称
     */
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    /**
     * 返回城市编码
     * 
     * @return 城市编码
     */
    public String getCityCode() {
        return this.cityCode;
    }

    /**
     * 设置城市编码
     * 
     * @param cityCode
     *            城市编码
     */
    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    /**
     * 返回区域代码
     * 
     * @return 区域代码
     */
    public String getAdCode() {
        return this.adCode;
    }

    /**
     * 设置区域代码
     * 
     * @param adCode
     *            区域代码
     */
    public void setAdCode(String adCode) {
        this.adCode = adCode;
    }

    /**
     * 返回区域的建议结果数目
     * 
     * @return 区域的建议结果数目
     */
    public int getSuggestionNum() {
        return this.suggestionNum;
    }

    /**
     * 设置区域的建议结果数目
     * 
     * @param suggestionNum
     *            区域的建议结果数目
     */
    public void setSuggestionNum(int suggestionNum) {
        this.suggestionNum = suggestionNum;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cityName);
        dest.writeString(cityCode);
        dest.writeString(adCode);
        dest.writeInt(suggestionNum);
    }

    public static final Creator<RemoteSuggestionCity> CREATOR = new Creator<RemoteSuggestionCity>() {
        @Override
        public RemoteSuggestionCity createFromParcel(Parcel source) {
            String cityName = source.readString();
            String cityCode = source.readString();
            String adCode = source.readString();
            int suggestionNum = source.readInt();

            RemoteSuggestionCity suggestionCity = new RemoteSuggestionCity(
                    cityName, cityCode, adCode, suggestionNum);

            return suggestionCity;
        }

        @Override
        public RemoteSuggestionCity[] newArray(int size) {
            return new RemoteSuggestionCity[size];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }
}
