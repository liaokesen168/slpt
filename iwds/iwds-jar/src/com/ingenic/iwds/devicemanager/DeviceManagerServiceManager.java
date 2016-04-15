/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  Huanglihong(Regen) <lihong.huang@ingenic.com, peterlihong@qq.com>
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

package com.ingenic.iwds.devicemanager;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;

import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.utils.IwdsLog;

/**
 * 设备管理服务
 */
public class DeviceManagerServiceManager extends ServiceManagerContext {
    /**
     * 广播: 设备被佩戴在右手
     */
    public static final String ACTION_WEAR_ON_RIGHT_HAND = "iwds.devicemanager.action.wear_on_right_hand";
    /**
     * 广播: 设备被佩戴在左手
     */
    public static final String ACTION_WEAR_ON_LEFT_HAND = "iwds.devicemanager.action.wear_on_left_hand";

    /**
     * 广播: 卸载应用
     */
    public static final String ACTION_DELETE_PACKAGE = "iwds.devicemanager.action.delete_package";

    /**
     * Extra: 卸载应用的包名
     */
    public static final String EXTRA_DELETE_PACKAGE_NAME = "deletePackageName";

    /**
     * Extra: 应用卸载的返回结果
     */
    public static final String EXTRA_DELETE_RETURN_CODE = "deleteReturnCode";

    private IDeviceManagerService m_service;

    public DeviceManagerServiceManager(Context context) {
        super(context);

        m_serviceClientProxy = new ServiceClientProxy() {
            @Override
            public void onServiceConnected(IBinder service) {
                m_service = IDeviceManagerService.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(boolean unexpected) {
            }

            @Override
            public IBinder getBinder() {
                return m_service.asBinder();
            }
        };
    }

    /**
     * 把设备设置为右手或左手佩戴模式
     * 
     * @param isOnRightHand
     *            表示设备是否佩戴在右手。真为右手，假为左手。
     */
    public void setWearOnRightHand(boolean isOnRightHand) {
        try {
            m_service.setWearOnRightHand(isOnRightHand);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in setWearOnRightHand: " + e.toString());
        }
    }

    /**
     * 判断设备是否佩戴在右手
     * 
     * @return 返回真表示佩戴在右手，否则表示佩戴在左手
     */
    public boolean isWearOnRightHand() {
        boolean isOnRightHand = false;
        try {
            isOnRightHand = m_service.isWearOnRightHand();
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in isWearOnRightHand: " + e.toString());
        }

        return isOnRightHand;
    }

    /**
     * 卸载APP
     *
     * @param packageName
     *            APP包名
     */
    public void deletePackage(String packageName) {
        try {
            m_service.deletePackage(packageName);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in deletePackage: " + e.toString());
        }
    }
}
