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

import android.content.pm.PackageStats;

/**
 * 远程设备应用管理监听器
 *
 */
public interface RemoteDeviceAppListener {

    /**
     * 当获取到远程设备的应用安装列表时被调用。
     * 
     * @param appList
     *            远程设备的应用安装列表
     */
    public void onRemoteAppInfoListAvailable(List<RemoteApplicationInfo> appList);

    /**
     * 当获取到远程设备的存储信息时被调用。
     * 
     * @param storageInfo
     *            远程设备的存储信息
     */
    public void onRemoteStorageInfoAvailable(RemoteStorageInfo storageInfo);

    /**
     * 安装应用时把安装文件发送到远程设备的进度信息更新时被调用。
     * 
     * @param packageName
     *            被安装的应用包名
     * @param progress
     *            安装文件的发送进度
     */
    public void onSendFileProgressForInstall(String packageName, int progress);

    /**
     * 当应用包在远程设备上的安装操作结束时被调用。
     * 
     * @param packageName
     *            被安装的包名
     * @param returnCode
     *            返回码，可能的值为:<br />
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#INSTALL_SUCCEEDED}
     *            ，
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#REQUEST_FAILED_PREVIOUS_DOING}
     *            ，
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#INSTALL_FAILED_ALREADY_EXISTS}
     *            ，
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#INSTALL_FAILED_INVALID_URI}
     *            ，
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#INSTALL_FAILED_INSUFFICIENT_STORAGE}
     *            ，
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#INSTALL_FAILED_INVALID_APK}
     *            ，
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES}
     *            ，
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#INSTALL_FAILED_OLDER_SDK}
     *            ，
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#INSTALL_FAILED_CPU_ABI_INCOMPATIBLE}
     *            ，
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#INSTALL_FAILED_VERSION_DOWNGRADE}
     *            ，
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#INSTALL_FAILED_SEND_APK_FILE_ERROR}
     *            ， 以上是需要处理的值，其他返回码可以作为未知错误处理。<br />
     *            完整的返回码请参考Android原生API：
     *            android.content.pm.PackageManager中所有前缀为INSTALL_的值。
     */
    public void onDoneInstallApp(String packageName, int returnCode);

    /**
     * 当应用包在远程设备上的卸载操作结束时被调用。
     * 
     * @param packageName
     *            被卸载应用的包名
     * @param returnCode
     *            返回码，可能的值为:<br />
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#DELETE_SUCCEEDED}
     *            ，
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#REQUEST_FAILED_PREVIOUS_DOING}
     *            ，
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#DELETE_FAILED_INTERNAL_ERROR}
     *            ，
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#DELETE_FAILED_DEVICE_POLICY_MANAGER}
     *            ，
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#DELETE_FAILED_USER_RESTRICTED}
     */
    public void onDoneDeleteApp(String packageName, int returnCode);

    /**
     * 当收到远程设备返回的应用包数据统计信息时被调用。
     * 
     * @param stats
     *            包数据统计信息
     * @param returnCode
     *            返回码，可能的值为：<br />
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#REQUEST_SUCCEEDED}
     *            ,
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#REQUEST_REMOTE_FAILED}
     *            ,
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#REQUEST_FAILED_PREVIOUS_DOING}
     */
    public void onResponsePkgSizeInfo(PackageStats stats, int returnCode);

    /**
     * 当远程设备清除应用数据或缓存的操作结束时被调用。
     * 
     * @param packageName
     *            被清除应用数据或缓存的包名
     * @param requestType
     *            请求类型，可能的值：<br />
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#TYPE_CLEAR_APP_USER_DATA}
     *            ,
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#TYPE_CLEAR_APP_CACHE}
     * @param returnCode
     *            返回码，可能的值为：<br />
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#REQUEST_SUCCEEDED}
     *            ,
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#REQUEST_REMOTE_FAILED}
     *            ,
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#REQUEST_FAILED_PREVIOUS_DOING}
     */
    public void onResponseClearAppDataOrCache(String packageName,
            int requestType, int returnCode);

    /**
     * 当远程设备清除所有应用的数据和缓存的操作结束时被调用。
     * 
     * @param returnCode
     *            返回码，可能的值为：<br />
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#REQUEST_SUCCEEDED}
     *            ,
     *            {@link com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo#REQUEST_FAILED_PREVIOUS_DOING}
     */
    public void onResponseClearAllAppDataAndCache(int totalClearCount,
            int index, String packageName, int typeOfIndex, int returnCode);
}
