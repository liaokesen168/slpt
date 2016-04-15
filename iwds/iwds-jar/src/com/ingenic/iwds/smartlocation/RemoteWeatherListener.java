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

package com.ingenic.iwds.smartlocation;

/**
 * 天气监听器
 */
public interface RemoteWeatherListener {
    /**
     * 返回天气实况，在发送天气实况请求后触发回调
     * 
     * @param weatherLive
     *            实况天气的信息。当失败时，此对象返回null
     */
    public void onWeatherLiveSearched(RemoteWeatherLive weatherLive);

    /**
     * 返回天气预报，在发送天气预报请求后触发回调
     * 
     * @param weatherForecast
     *            天气预报的信息。当失败时，此对象返回null
     */
    public void onWeatherForecastSearched(RemoteWeatherForecast weatherForecast);
}
