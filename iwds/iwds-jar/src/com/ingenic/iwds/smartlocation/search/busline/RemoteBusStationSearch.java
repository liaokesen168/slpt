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

/**
 * 本类为公交站台搜索类。 在类RemoteBusStationSearch中，使用RemoteBusStationQuery类设定搜索参数
 */
public class RemoteBusStationSearch {
    private RemoteBusStationQuery query;
    private RemoteBusStationSearchListener callback;

    /**
     * RemoteBusStationSearch构造函数
     * 
     * @param query
     *            公交查询条件
     */
    public RemoteBusStationSearch(RemoteBusStationQuery query) {
        this.query = query;
    }

    /**
     * 设置查询条件
     * 
     * @param query
     *            新的查询条件
     */
    public void setQuery(RemoteBusStationQuery query) {
        this.query = query;
    }

    /**
     * 返回查询条件
     * 
     * @return 查询条件
     */
    public RemoteBusStationQuery getQuery() {
        return this.query;
    }

    /**
     * 设置公交站点搜索结果监听接口
     * 
     * @param listener
     *            公交站点搜索结果监听接口
     */
    public void setBusStationSearchListener(
            RemoteBusStationSearchListener listener) {
        this.callback = listener;
    }

    /**
     * 返回公交站点搜索结果监听接口
     * 
     * @return 公交站点搜索结果监听接口
     */
    public RemoteBusStationSearchListener getBusStationSearchListener() {
        return this.callback;
    }

    /**
     * 此接口定义了公交站点查询异步处理回调接口
     */
    public interface RemoteBusStationSearchListener {
        /**
         * 公交站点的查询异步处理
         * 
         * @param result
         *            公交站点的搜索结果
         * 
         * @param errorCode
         *            返回结果成功或者失败的响应码。0为成功，其他为失败
         */
        void onBusStationSearched(RemoteBusStationResult result, int errorCode);
    }
}
