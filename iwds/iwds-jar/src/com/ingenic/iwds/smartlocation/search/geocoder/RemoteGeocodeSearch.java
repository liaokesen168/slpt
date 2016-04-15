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

package com.ingenic.iwds.smartlocation.search.geocoder;

/**
 * 地理编码与逆地理编码类。 地理编码又称地址匹配，指的是从已知的地址描述到对应的经纬度坐标的转换， 即根据地址信息，获取地址所对应的点坐标等。
 * 逆地理编码即地址解析服务，具体是指从已知的经纬度坐标到对应的地址描述（如省市、街区、楼层、房间等）的转换。
 * 通过该类提供的方法可获取对应位置的地理描述，分为3种地物类型：交叉路口、兴趣点、道路
 */
public class RemoteGeocodeSearch {
    /**
     * GPS
     */
    public static final String GPS = "gps";

    /**
     * 高德服务
     */
    public static final String IWDS = "autonavi";

    private RemoteGeocodeQuery geocodeQuery;
    private RemoteRegeocodeQuery regeocodeQuery;
    private RemoteGeocodeSearchListener callback;

    /**
     * RemoteGeocodeSearch构造函数
     */
    public RemoteGeocodeSearch() {

    }

    /**
     * 返回地理编码查询的关键字和查询城市
     * 
     * @return 地理编码查询的关键字和查询城市
     */
    public RemoteGeocodeQuery getGeocodeQuery() {
        return this.geocodeQuery;
    }

    /**
     * 设置地理编码查询的关键字和查询城市
     * 
     * @param query
     *            地理编码查询的关键字和查询城市
     */
    public void setGeocodeQuery(RemoteGeocodeQuery query) {
        this.geocodeQuery = query;
    }

    /**
     * 设置逆地理编码查询的地理坐标点、查询范围、坐标类型
     * 
     * @param query
     *            逆地理编码查询的地理坐标点、查询范围、坐标类型
     */
    public void setRegeocodeQuery(RemoteRegeocodeQuery query) {
        this.regeocodeQuery = query;
    }

    /**
     * 返回逆地理编码查询的地理坐标点、查询范围、坐标类型
     * 
     * @return 逆地理编码查询的地理坐标点、查询范围、坐标类型
     */
    public RemoteRegeocodeQuery getRegeocodeQuery() {
        return this.regeocodeQuery;
    }

    /**
     * 设置监听接口
     * 
     * @param listener
     *            监听接口
     */
    public void setGeocodeSearchListener(RemoteGeocodeSearchListener listener) {
        this.callback = listener;
    }

    /**
     * 返回监听接口
     * 
     * @return 监听接口
     */
    public RemoteGeocodeSearchListener getGeocodeSearchListener() {
        return this.callback;
    }

    /**
     * 设置地理编码查询结果监听接口
     */
    public interface RemoteGeocodeSearchListener {
        /**
         * 根据给定的经纬度和最大结果数返回逆地理编码的结果列表。 逆地理编码兴趣点返回结果最大返回数目为10，道路和交叉路口返回最大数目为3
         * 
         * @param result
         *            逆地理编码返回的结果
         * 
         * @param errorCode
         *            返回结果成功或者失败的响应码。0为成功，其他为失败
         */
        void onRegeocodeSearched(RemoteRegeocodeResult result, int errorCode);

        /**
         * 根据给定的地理名称和查询城市，返回地理编码的结果列表。 地理编码返回结果集默认最大返回数目为10
         * 
         * @param result
         *            地理编码返回的结果
         * 
         * @param errorCode
         *            返回结果成功或者失败的响应码。0为成功，其他为失败
         */
        void onGeocodeSearched(RemoteGeocodeResult result, int errorCode);
    }

}
