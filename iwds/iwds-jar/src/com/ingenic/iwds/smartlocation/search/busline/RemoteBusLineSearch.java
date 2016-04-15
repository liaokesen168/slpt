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
 * 本类为公交线路搜索类。 在类RemoteBusLineSearch 中，使用RemoteBusLineQuery 类设定搜索参数
 */
public class RemoteBusLineSearch {
    private RemoteBusLineQuery query;
    private RemoteBusLineSearchListener callback;

    /**
     * RemoteBusLineSearch构造函数
     * 
     * @param query
     *            公交查询条件
     */
    public RemoteBusLineSearch(RemoteBusLineQuery query) {
        this.query = query;
    }

    /**
     * 返回查询条件
     * 
     * @return 查询条件
     */
    public RemoteBusLineQuery getQuery() {
        return this.query;
    }

    /**
     * 设置查询条件
     * 
     * @param query
     *            新的查询条件
     */
    public void setQuery(RemoteBusLineQuery query) {
        this.query = query;
    }

    /**
     * 公交路线搜索结果监听接口设置
     * 
     * @param listener
     *            公交路线搜索结果监听接口
     */
    public void setBusLineSearchListener(RemoteBusLineSearchListener listener) {
        this.callback = listener;
    }

    /**
     * 返回公交路线搜索结果监听接口
     * 
     * @return 公交路线搜索结果监听接口
     */
    public RemoteBusLineSearchListener getBusLineSearchListener() {
        return this.callback;
    }

    /**
     * 此接口定义了公交线路查询异步处理回调接口
     */
    public interface RemoteBusLineSearchListener {
        /**
         * 公交路线的查询异步处理
         * 
         * @param result
         *            公交线路搜索结果
         * 
         * @param errorCode
         *            返回结果成功或者失败的响应码。0为成功，其他为失败
         */
        void onBusLineSearched(RemoteBusLineResult result, int errorCode);
    }
}
