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

/**
 * 行政区域查询类。请用 DistrictSearchQuery 类设定搜索参数，如果不设定，默认查询全国
 */
public class RemoteDistrictSearch {
    private RemoteDistrictQuery query;
    private RemoteDistrictSearchListener callback;

    /**
     * RemoteDistrictSearch构造函数
     */
    public RemoteDistrictSearch() {

    }

    /**
     * 设置查询条件
     * 
     * @param query
     *            新的查询条件
     */
    public void setQuery(RemoteDistrictQuery query) {
        this.query = query;
    }

    /**
     * 返回查询条件
     * 
     * @return 查询条件
     */
    public RemoteDistrictQuery getQuery() {
        return this.query;
    }

    /**
     * 设置行政区划查询监听
     * 
     * @param listener
     *            行政区划查询监听对象
     */
    public void setDistrictSearchListener(RemoteDistrictSearchListener listener) {
        this.callback = listener;
    }

    /**
     * 获取行政区划查询监听
     * 
     * @return 行政区划查询监听对象
     */
    public RemoteDistrictSearchListener getDistrictListener() {
        return this.callback;
    }

    /**
     * 本类为 RemoteDistrict（行政区域）搜索结果的异步处理回调接口
     */
    public interface RemoteDistrictSearchListener {
        /**
         * 返回RemoteDistrict（行政区划）异步处理的结果
         * 
         * @param result
         *            行政区域的搜索结果
         * 
         * @param errorCode
         *            错误码
         */
        void onDistrictSearched(RemoteDistrictResult result, int errorCode);
    }

}
