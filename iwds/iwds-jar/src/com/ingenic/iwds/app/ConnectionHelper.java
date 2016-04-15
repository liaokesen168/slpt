/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  SunWenZhong(Fighter) <wenzhong.sun@ingenic.com, wanmyqawdr@126.com>
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

package com.ingenic.iwds.app;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.uniconnect.ConnectionServiceManager;
import com.ingenic.iwds.utils.IwdsAssert;

/**
 * Connection帮助类.
 */
public abstract class ConnectionHelper implements
        ServiceClient.ConnectionCallbacks {
    private ConnectionServiceManager m_connectionService;
    private ServiceClient m_serviceClient;
    private ArrayList<DeviceDescriptor> m_devices;

    private Context m_context;

    private IntentFilter m_filter = new IntentFilter();
    private BroadcastReceiver m_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    ConnectionServiceManager.ACTION_CONNECTED_ADDRESS)) {

                handleConnectedDevice((DeviceDescriptor) intent
                        .getParcelableExtra("DeviceDescriptor"));

            } else if (intent.getAction().equals(
                    ConnectionServiceManager.ACTION_DISCONNECTED_ADDRESS)) {

                handleDisconnectedDevice((DeviceDescriptor) intent
                        .getParcelableExtra("DeviceDescriptor"));
            }
        }
    };

    /**
     * 构造
     *
     * @param context
     *            the 应用的上下文
     */
    public ConnectionHelper(Context context) {
        IwdsAssert.dieIf(this, context == null, "Context is null.");

        m_context = context.getApplicationContext();

        IwdsAssert
                .dieIf(this, m_context == null, "Application context is null");

        m_devices = new ArrayList<DeviceDescriptor>();

        m_serviceClient = new ServiceClient(m_context,
                ServiceManagerContext.SERVICE_CONNECTION, this);

        m_filter.addAction(ConnectionServiceManager.ACTION_CONNECTED_ADDRESS);
        m_filter.addAction(ConnectionServiceManager.ACTION_DISCONNECTED_ADDRESS);
    }

    private void handleConnectedDevice(DeviceDescriptor deviceDescriptor) {
        if (!m_devices.contains(deviceDescriptor)) {
            m_devices.add(deviceDescriptor);

            onConnectedDevice(deviceDescriptor);
        }
    }

    private void handleDisconnectedDevice(DeviceDescriptor deviceDescriptor) {
        if (m_devices.remove(deviceDescriptor))
            onDisconnectedDevice(deviceDescriptor);
    }

    /**
     * 启动. 连接到ConnectionService.
     */
    public void start() {
        m_serviceClient.connect();
    }

    /**
     * 停止. 断开和ConnectionService的连接.
     */
    public void stop() {
        m_serviceClient.disconnect();
    }

    /**
     * 检查是否启动.
     *
     * @return 如果已经启动，返回{@code true}.
     */
    public boolean isStarted() {
        return m_serviceClient.isConnected();
    }

    @Override
    public void onConnected(ServiceClient serviceClient) {
        if (m_connectionService != null)
            return;

        m_connectionService = (ConnectionServiceManager) m_serviceClient
                .getServiceManagerContext();

        onServiceConnected(m_connectionService);

        m_context.registerReceiver(m_receiver, m_filter);

        DeviceDescriptor[] devices = m_connectionService
                .getConnectedDeviceDescriptors();

        ArrayList<DeviceDescriptor> copyDevices = (ArrayList<DeviceDescriptor>) m_devices
                .clone();

        int N = devices == null ? 0 : devices.length;
        for (DeviceDescriptor deviceDescriptor : copyDevices) {
            boolean found = false;
            for (int i = 0; i < N; i++) {
                if (devices[i].equals(deviceDescriptor)) {
                    found = true;

                    break;
                }
            }

            if (!found)
                handleDisconnectedDevice(deviceDescriptor);
        }

        copyDevices = null;

        if (devices == null)
            return;

        for (DeviceDescriptor deviceDescriptor : devices)
            handleConnectedDevice(deviceDescriptor);
    }

    @Override
    public void onDisconnected(ServiceClient serviceClient, boolean unexpected) {
        if (m_connectionService != null) {
            m_context.unregisterReceiver(m_receiver);

            m_connectionService = null;

            for (DeviceDescriptor descriptor : m_devices)
                onDisconnectedDevice(descriptor);

            m_devices.clear();
        }

        onServiceDisconnected(unexpected);
    }

    @Override
    public void onConnectFailed(ServiceClient serviceClient, ConnectFailedReason reason) {
        IwdsAssert.dieIf(this, true, "Failed to connect to ConnectionService: "
                + reason.toString());
    }

    /**
     * 当连接服务连接成功时的回调.
     *
     * @param connectionServiceManager
     *            连接服务管理器
     */
    public abstract void onServiceConnected(
            ConnectionServiceManager connectionServiceManager);

    /**
     * 当连接服务断开时的回调.
     *
     * @param unexpected
     *            如果服务异常退出，值为{@code true}
     */
    public abstract void onServiceDisconnected(boolean unexpected);

    /**
     * 当设备连接成功时的回调.
     *
     * @param deviceDescriptor
     *            设备描述符
     */
    public abstract void onConnectedDevice(DeviceDescriptor deviceDescriptor);

    /**
     * 当设备断开连接时的回调.
     *
     * @param deviceDescriptor
     *            设备描述符
     */
    public abstract void onDisconnectedDevice(DeviceDescriptor deviceDescriptor);
}
