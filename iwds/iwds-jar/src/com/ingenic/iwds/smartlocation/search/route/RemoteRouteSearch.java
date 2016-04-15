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

package com.ingenic.iwds.smartlocation.search.route;

/**
 * 该类定义了一条路径。此类不可以直接构造，只能通过类的静态（static）方法得到其实例。一条路径，可以是公交路径、步行路径或者自驾路径。
 * 根据构造时的参数不同，可以得到不同的路径
 */
public class RemoteRouteSearch {
    /**
     * 最经济模式
     */
    public static final int BUS_DEFAULT = 0;

    /**
     * 最快捷模式
     */
    public static final int BUS_SAVE_MONEY = 1;

    /**
     * 最少换乘
     */
    public static final int BUS_LEASE_CHANGE = 2;

    /**
     * 最少步行
     */
    public static final int BUS_LEASE_WALK = 3;

    /**
     * 最舒适
     */
    public static final int BUS_COMFORTABLE = 4;

    /**
     * 不乘地铁
     */
    public static final int BUS_NO_SUBWAY = 5;

    /**
     * 速度优先
     */
    public static final int DRIVING_DEFAULT = 0;

    /**
     * 费用优先（不走收费路的最快道路）
     */
    public static final int DRIVING_SAVE_MONEY = 1;

    /**
     * 距离优先
     */
    public static final int DRIVING_SHORT_DISTANCE = 2;

    /**
     * 不走快速路
     */
    public static final int DRIVING_NO_EXPRESSWAYS = 3;

    /**
     * 避免拥堵
     */
    public static final int DRIVING_AVOID_CONGESTION = 4;

    /**
     * 同时使用速度优先、费用优先、距离优先三个策略计算路径
     */
    public static final int DRIVING_MULTI_STRATEGY = 5;

    /**
     * 不走高速
     */
    public static final int DRIVING_NO_HIGH_WAY = 6;

    /**
     * 不走高速且避免收费
     */
    public static final int DRIVING_NO_HIGH_WAY_SAVE_MONEY = 7;

    /**
     * 避免收费与拥堵
     */
    public static final int DRIVING_NO_HIGH_WAY_AVOID_CONGESTION = 8;

    /**
     * 不走高速且躲避收费和拥堵
     */
    public static final int DRIVING_NO_HIGH_AVOID_CONGESTION_SAVE_MONEY = 9;

    /**
     * 路径为步行模式。只提供一条步行方案
     */
    public static final int WALK_DEFAULT = 0;

    /**
     * 提供备选步行方案（有可能无备选方案）
     */
    public static final int WALK_MULTI_PATH = 1;

    private RemoteBusRouteQuery busRouteQuery;
    private RemoteDriveRouteQuery driveRouteQuery;
    private RemoteWalkRouteQuery walkRouteQuery;
    private RemoteRouteSearchListener callback;

    /**
     * RemoteRouteSearch构造函数
     */
    public RemoteRouteSearch() {

    }

    /**
     * 设置公交路线查询参数
     * 
     * @param query
     *            公交路线查询参数
     */
    public void setBusRouteQuery(RemoteBusRouteQuery query) {
        this.busRouteQuery = query;
    }

    /**
     * 返回公交路线查询参数
     * 
     * @return 公交路线查询参数
     */
    public RemoteBusRouteQuery getBusRouteQuery() {
        return this.busRouteQuery;
    }

    /**
     * 设置驾车路线查询参数
     * 
     * @param query
     *            驾车路线查询参数
     */
    public void setDriveRouteQuery(RemoteDriveRouteQuery query) {
        this.driveRouteQuery = query;
    }

    /**
     * 返回驾车路线查询参数
     * 
     * @return 驾车路线查询参数
     */
    public RemoteDriveRouteQuery getDriveRouteQuery() {
        return this.driveRouteQuery;
    }

    /**
     * 设置步行路线查询参数
     * 
     * @param query
     *            步行路线查询参数
     */
    public void setWalkRouteQuery(RemoteWalkRouteQuery query) {
        this.walkRouteQuery = query;
    }

    /**
     * 返回步行路线查询参数
     * 
     * @return 步行路线查询参数
     */
    public RemoteWalkRouteQuery getWalkRouteQuery() {
        return this.walkRouteQuery;
    }

    /**
     * 设置步行路线查询参数
     * 
     * @param listener
     *            步行路线查询参数
     */
    public void setRouteSearchListener(RemoteRouteSearchListener listener) {
        this.callback = listener;
    }

    /**
     * 返回路径搜索结果监听接口
     * 
     * @return 路径搜索结果监听接口
     */
    public RemoteRouteSearchListener getRouteSearchListener() {
        return this.callback;
    }

    /**
     * 路径搜索结果监听接口
     */
    public interface RemoteRouteSearchListener {
        /**
         * 公交换乘路径规划结果的回调方法
         * 
         * @param result
         *            公交路径规划的结果集
         * @param errorCode
         *            返回结果成功或者失败的响应码。0为成功，其他为失败
         */
        void onBusRouteSearched(RemoteBusRouteResult result, int errorCode);

        /**
         * 驾车路径规划结果的回调方法
         * 
         * @param result
         *            驾车路径规划的结果集
         * 
         * @param errorCode
         *            返回结果成功或者失败的响应码。0为成功，其他为失败
         */
        void onDriveRouteSearched(RemoteDriveRouteResult result, int errorCode);

        /**
         * 步行路径规划结果的回调方法
         * 
         * @param result
         *            步行路径规划的结果集
         * 
         * @param errorCode
         *            返回结果成功或者失败的响应码。0为成功，其他为失败
         */
        void onWalkRouteSearched(RemoteWalkRouteResult result, int errorCode);
    }
}
