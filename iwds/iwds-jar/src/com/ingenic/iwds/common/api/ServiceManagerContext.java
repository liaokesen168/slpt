/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  SunWenZhong(Fighter) <wzsun@ingenic.com, wanmyqawdr@126.com>
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

package com.ingenic.iwds.common.api;

import android.content.Context;
import android.os.IBinder;

import com.ingenic.iwds.utils.IwdsAssert;

public abstract class ServiceManagerContext {
    /**
     * 互联服务的名字
     */
    public static final String SERVICE_CONNECTION = "service_connection";
    /**
     * 远程语音服务的名字
     */
    public static final String SERVICE_REMOTE_SPEECH = "service_remote_speech";
    /**
     * 云服务的名字
     */
    public static final String SERVICE_CLOUD = "service_cloud";
    /**
     * 低功耗表盘服务的名字
     */
    public static final String SERVICE_SLPT_WATCH_FACE = "service_slpt_watchface";
    /**
     * 传感器服务的名字
     */
    public static final String SERVICE_SENSOR = "service_sensor";
    /**
     * 远程传感器服务的名字
     */
    public static final String SERVICE_REMOTE_SENSOR = "service_remote_sensor";
    /**
     * 远程定位服务的名字
     */
    public static final String SERVICE_REMOTE_LOCATION = "service_remote_location";
    /**
     * 振动器服务的名字
     */
    public static final String SERVICE_VIBRATE = "service_vibrate";
    /**
     * 通知代理服务的名字
     */
    public static final String SERVICE_NOTIFICATION_PROXY = "service_notification_proxy";
    /**
     * 设备管理服务的名字
     */
    public static final String SERVICE_DEVICE_MANAGER = "service_device_manager";
    /**
     * 远程设备管理服务的名字
     */
    public static final String SERVICE_REMOTE_DEVICE = "service_remote_device";
    /**
     * 远程搜索服务的名字
     */
    public static final String SERVICE_REMOTE_SEARCH = "service_remote_search";
    /**
     * 远程广播服务的名字
     */
    public static final String SERVICE_REMOTE_BROADCAST = "service_remote_broadcast";
    /**
     * 远程锁服务的名字
     */
    public static final String SERVICE_REMOTE_WAKELOCK = "service_remote_wakelock";
    /**
     * 本地控件服务的名字
     */
    public static final String SERVICE_LOCAL_WIDGET = "service_local_widget";
    /**
     * 通知点击的 action
     */
    public static final String ACTION_NOTIFICATION_CLICKED = "com.ingenic.iwds.notification.clicked";

    protected ServiceClientProxy m_serviceClientProxy;

    private Context mContext;

    /**
     * 构造 {@code ServiceManagerContext}
     * 
     * @param context
     *            应用上下文
     */
    public ServiceManagerContext(Context context) {
        IwdsAssert.dieIf(this, context == null, "Context can not be null.");

        mContext = context;
    }

    /**
     * 获取应用上下文
     */
    public Context getContext() {
        return mContext;
    }

    /* package */void onServiceConnected(IBinder binder) {
        m_serviceClientProxy.onServiceConnected(binder);
    }

    /* package */void onServiceDisconnected(boolean unexpected) {
        m_serviceClientProxy.onServiceDisconnected(unexpected);
    }

    /* package */IBinder getBinder() {
        return m_serviceClientProxy.getBinder();
    }

    protected static abstract class ServiceClientProxy {
        abstract public void onServiceConnected(IBinder binder);

        abstract public void onServiceDisconnected(boolean unexpected);

        abstract public IBinder getBinder();
    }
}
