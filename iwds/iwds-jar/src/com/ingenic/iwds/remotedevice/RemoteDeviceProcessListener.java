/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  Huanglihong(Regen) <lihong.huang@ingenic.com>
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

package com.ingenic.iwds.remotedevice;

import java.util.List;

/**
 * 远程设备进程管理监听器
 */
public interface RemoteDeviceProcessListener {
    /**
     * 当获取到远程设备的系统内存信息时被调用。
     * 
     * @param availMemSize
     *            系统可用内存
     * @param totalMemSize
     *            系统总内存
     */
    void onResponseSystemMemoryInfo(long availMemSize, long totalMemSize);

    /**
     * 当获取到远程设备正在运行的应用进程信息时被调用。
     * 
     * @param processInfoList
     *            进程信息列表
     */
    void onResponseRunningAppProcessInfo(List<RemoteProcessInfo> processInfoList);

    /**
     * 当指定的远程设备应用包的所有后台进程被杀后调用。
     * 
     * @param packageName
     *            被杀的后台进程所在的应用包名
     */
    void onDoneKillProcess(String packageName);
}
