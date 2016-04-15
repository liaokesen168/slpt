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

/**
 * 本类为POI（Point Of Interest，兴趣点）搜索的“入口”类
 */
public class RemotePoiSearch {
    /**
     * 英文
     */
    public static final String ENGLISH = "en";

    /**
     * 中文
     */
    public static final String CHINESE = "zh-CN";

    private RemotePoiQuery query;
    private RemotePoiSearchListener callback;

    /**
     * RemotePoiSearch构造函数
     * 
     * @param query
     *            查询条件
     */
    public RemotePoiSearch(RemotePoiQuery query) {
        this.query = query;
    }

    /**
     * 设置查询条件
     * 
     * @param query
     *            新的查询条件
     */
    public void setQuery(RemotePoiQuery query) {
        this.query = query;
    }

    /**
     * 返回查询条件
     * 
     * @return 查询条件
     */
    public RemotePoiQuery getQuery() {
        return this.query;
    }

    /**
     * 设置查询监听接口
     * 
     * @param listener
     *            查询监听接口
     */
    public void setPoiSearchListener(RemotePoiSearchListener listener) {
        this.callback = listener;
    }

    /**
     * 返回查询监听接口
     * 
     * @return 设置查询监听接口
     */
    public RemotePoiSearchListener getPoiSearchListener() {
        return this.callback;
    }

    /**
     * POI（Point Of Interest，兴趣点）搜索结果的异步处理回调接口
     */
    public interface RemotePoiSearchListener {
        /**
         * 
         * @param result
         * @param errorCode
         */
        void onPoiSearched(RemotePoiResult result, int errorCode);

        void onPoiItemDetailSearched(RemotePoiItemDetail poiItemDetail,
                int errorCode);
    }
}
