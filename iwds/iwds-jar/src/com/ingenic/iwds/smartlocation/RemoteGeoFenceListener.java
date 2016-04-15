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
 * 地理围栏监听器
 */
public interface RemoteGeoFenceListener {
    /**
     * 返回是否进入警戒区域
     * 
     * @param status
     *            警戒状态,
     *            {@link RemoteLocationServiceManager#GEOFENCE_STATUS_ALERT},
     *            {@link RemoteLocationServiceManager#GEOFENCE_STATUS_NON_ALERT}
     */
    void onGeoFenceAlert(int status);
}
