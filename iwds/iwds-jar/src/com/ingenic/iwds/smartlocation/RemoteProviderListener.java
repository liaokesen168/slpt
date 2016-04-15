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

import java.util.ArrayList;

/**
 * provider 状态监听器
 */
public interface RemoteProviderListener {
    /**
     * 返回 provider 的状态
     * 
     * @param enabled
     *            true 可用，false 不可用
     * @param provider
     *            provider的名称
     */
    void onProviderStatus(boolean enabled, String provider);

    /**
     * 返回 provider 列表，目前只支持
     * {@link com.ingenic.iwds.smartlocation.RemoteLocationServiceManager#GPS_PROVIDER}
     * 和
     * {@link com.ingenic.iwds.smartlocation.RemoteLocationServiceManager#IWDS_NETWORK_PROVIDER}
     * 
     * @param enabledOnly
     *            true 已使能的provider，false 所有provider
     * @param providerList
     *            provider 列表
     */
    void onProviderList(boolean enabledOnly, ArrayList<String> providerList);
}
