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

package com.ingenic.iwds.uniconnect;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

public class ConnectionServiceManager extends ServiceManagerContext {
    /**
     * 广播: 与远端设备建立了{@code link}
     */
    public static final String ACTION_CONNECTED_ADDRESS = "iwds.uniconnect.action.connected_address";

    /**
     * 广播: 与远端设备解除了{@code link}
     */
    public static final String ACTION_DISCONNECTED_ADDRESS = "iwds.uniconnect.action.disconnected_address";

    private IConnectionService m_service;

    /**
     * 构造{@code ConnectionServiceManager}
     *
     * @param context
     *            应用的上下文
     */
    public ConnectionServiceManager(Context context) {
        super(context);

        m_serviceClientProxy = new ServiceClientProxy() {
            @Override
            public void onServiceConnected(IBinder service) {
                m_service = IConnectionService.Stub.asInterface(service);
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
     * 创建{@code connection}
     *
     * @return {@code connection}
     */
    public Connection createConnection() {
        IConnectionCallBack callBack = new IConnectionCallBack.Stub() {

            @Override
            public void stateChanged(int state, int arg0)
                    throws RemoteException {
                // TODO

            }
        };

        return createConnection(callBack);
    }

    /**
     * 获取已建立{@code link}的远端设备描述符数组
     *
     * @return 已建立{@code link}的远端设备描述符数组
     */
    public DeviceDescriptor[] getConnectedDeviceDescriptors() {
        try {
            return m_service.getConnectedDeviceDescriptors();
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in getConnectedDeviceDescriptors: " + e);
        }

        return null;
    }

    private Connection createConnection(IConnectionCallBack callback) {
        IwdsAssert
                .dieIf(this, callback == null, "Connection callback is null.");

        try {
            IConnection connection = m_service.createConnection(callback);

            return new Connection(getContext(), connection);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in createConnection: " + e);
        }

        return null;
    }
}
