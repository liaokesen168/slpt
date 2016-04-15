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

package com.ingenic.iwds.smartsense;

import java.util.ArrayList;

/**
 * 该接口用于监听远端设备上传感器服务状态
 */
public interface RemoteSensorListener {
    /**
     * 远端设备传感器服务状态变化时被回调
     *
     * @param sensorList
     *            远端传感器列表 <li>null - 远端设备传感器不可用<li> <li>非null - 远端设备传感器可用,
     *            并给出可用的传感器列表<li>
     */
    public void onSensorAvailable(ArrayList<Sensor> sensorList);
}
