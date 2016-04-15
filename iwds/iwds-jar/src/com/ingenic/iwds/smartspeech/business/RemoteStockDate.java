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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * stock的data节点的抽象，表示股票信息数据类
 */
public class RemoteStockDate implements Parcelable {

    /**
     * current_price元素节点名，当前价格（单位：元）
     */
    public static final String RAWCURRENTPRICE = "current_price";

    /**
     * closing_price元素节点名，表示昨日收盘价（单位：元）
     */
    public static final String RAWCLOSINGPRICE = "closing_price";

    /**
     * opening_price元素节点名，表示今日开盘价（单位：元）
     */
    public static final String RAWOPENINGPRICE = "opening_price";

    /**
     * high_price元素节点名，表示今日最高价（单位：元）
     */
    public static final String RAWHIGHPRICE = "high_price";

    /**
     * low_price元素节点名，表示今日最低价（单位：元）
     */
    public static final String RAWLOWPRICE = "low_price";

    /**
     * rise_value元素节点名，表示上涨值（例如：0.21。上涨时为正数，下跌时为负数）
     */
    public static final String RAWRISEVALUE = "rise_value";

    /**
     * rise_rate元素节点名，表示上涨幅度（例如：3.432%。上涨时为正数，下跌时为负数）
     */
    public static final String RAWRISERATE = "rise_rate";

    /**
     * update_datetime元素节点名，表示更新时的时间信息
     */
    public static final String RAWUPDATEDATETIME = "update_datetime";

    /**
     * mbm_chart_url元素节点名，表示分时图（minute-by-minute chart）url
     */
    public static final String RAWMBMCHART_URL = "mbm_chart_url";

    /**
     * current_price元素节点的值
     */
    public String mCurrentPrice = null;

    /**
     * closing_price元素节点的值
     */
    public String mClosingPrice = null;

    /**
     * opening_price元素节点的值
     */
    public String mOpeningPrice = null;

    /**
     * high_price元素节点的值
     */
    public String mHighPrice = null;

    /**
     * low_price元素节点的值
     */
    public String mLowPrice = null;

    /**
     * rise_value元素节点的值
     */
    public String mRiseValue = null;

    /**
     * rise_rate元素节点的值
     */
    public String mRiseRate = null;

    /**
     * update_datetime元素节点的值
     */
    public RemoteDateTime mUpdateDateTime = null;

    /**
     * mbm_chart_url元素节点的值
     */
    public String mMbmChartUrl = null;

    @Override
    public String toString() {
        return "StockDate [current_price=" + mCurrentPrice + ", closing_price="
                + mClosingPrice + ", opening_price=" + mOpeningPrice
                + ", high_price=" + mHighPrice + ", low_price=" + mLowPrice
                + ", rise_value=" + mRiseValue + ", rise_rate=" + mRiseRate
                + ", update_datetime=" + mUpdateDateTime + ", mbm_chart_url="
                + mMbmChartUrl + "]";
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {

        arg0.writeString(mCurrentPrice);
        arg0.writeString(mClosingPrice);
        arg0.writeString(mOpeningPrice);
        arg0.writeString(mHighPrice);
        arg0.writeString(mLowPrice);
        arg0.writeString(mRiseValue);
        arg0.writeString(mRiseRate);
        arg0.writeParcelable(mUpdateDateTime, arg1);
        arg0.writeString(mMbmChartUrl);
    }

    public static final Creator<RemoteStockDate> CREATOR = new Creator<RemoteStockDate>() {
        @Override
        public RemoteStockDate createFromParcel(Parcel source) {
            RemoteStockDate info = new RemoteStockDate();
            info.mCurrentPrice = source.readString();
            info.mClosingPrice = source.readString();
            info.mOpeningPrice = source.readString();
            info.mHighPrice = source.readString();
            info.mLowPrice = source.readString();
            info.mRiseRate = source.readString();
            info.mRiseValue = source.readString();
            info.mUpdateDateTime = source.readParcelable(RemoteDateTime.class
                    .getClassLoader());
            info.mMbmChartUrl = source.readString();
            return info;
        }

        @Override
        public RemoteStockDate[] newArray(int size) {
            return new RemoteStockDate[size];
        }
    };
}
