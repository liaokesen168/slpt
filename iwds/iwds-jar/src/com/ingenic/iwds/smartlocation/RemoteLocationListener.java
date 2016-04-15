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
 * 定位位置监听器
 */
public interface RemoteLocationListener {
    /**
     * 返回当前定位位置信息
     * 
     * @param location
     *            当前定位位置，如果使用
     *            {@link RemoteLocationServiceManager#IWDS_NETWORK_PROVIDER}
     *            定位时，定位是否成功，都会回调该方法。 如果使用
     *            {@link RemoteLocationServiceManager#GPS_PROVIDER}定位，失败时不触发该回调
     */
    void onLocationChanged(RemoteLocation location);
}
