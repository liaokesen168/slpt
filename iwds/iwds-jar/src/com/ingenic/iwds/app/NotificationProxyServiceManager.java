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

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;

import com.ingenic.iwds.app.NotificationServiceBackend;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

/**
 * 这个类用于通知管理
 * @see com.ingenic.iwds.app.Note
 * @see com.ingenic.iwds.common.api.ServiceClient#getServiceManagerContext
 */
public class NotificationProxyServiceManager extends ServiceManagerContext {
    private INotificationProxyService m_service;

    /**
     * 构造 {@code NotificationProxyServiceManager}
     * @param  context 应用上下文
     */
    public NotificationProxyServiceManager(Context context) {
        super(context);

        m_serviceClientProxy = new ServiceClientProxy() {
            @Override
            public void onServiceConnected(IBinder service) {
                m_service = INotificationProxyService.Stub.asInterface(service);
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
     * 显示一个通知到状态栏上。
     *    如果有一个与新通知id相同的通知已经被显示，那就会替换并更新这个已经存在的通知。
     * @param  id   指定通知id，这个id应该保持在整个app内唯一
     * @param  note {@link Note}对象，指定通知的内容。不能为null
     * @return      返回通知显示是否成功。true 成功，false 失败。
     */
    public boolean notify(int id, Note note) {
        IwdsAssert.dieIf(this, note == null, "note is null");

        try {
            return m_service.notify(getContext().getPackageName(), id, note);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in notify: " + e.toString());
        }

        return false;
    }

    /**
     * 取消一个显示的通知
     * @param id 指定要取消的通知的id
     */
    public void cancel(int id) {
        try {
            m_service.cancel(getContext().getPackageName(), id);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in cancel: " + e.toString());
        }
    }

    /**
     * 取消所有显示的通知。只取消本应用的通知，不影响其他应用
     */
    public void cancelAll() {
        try {
            m_service.cancelAll(getContext().getPackageName());

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in cancelAll: " + e.toString());
        }
    }

    /**
     * 注册一个后端
     * @param  backend 指定要注册的后端对象
     * @param  uuid    该后端对应的uuid
     * @return         true 注册成功，false 注册失败
     */
    public boolean registerBackend(NotificationServiceBackend backend,
            String uuid) {
        IwdsAssert.dieIf(this, backend == null, "backend is null.");
        IwdsAssert.dieIf(this, uuid == null || uuid.isEmpty(),
                "uuid is null or empty.");

        try {
            return m_service.registerBackend(backend.callback, getContext()
                    .getPackageName(), uuid);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in registerBackend: " + e.toString());
        }

        return false;
    }

    /**
     * 取消后端注册
     * @param  uuid 指定后端对应的uuid
     * @return      true 取消注册成功，false 取消注册失败
     */
    public boolean unregisterBackend(String uuid) {
        IwdsAssert.dieIf(this, uuid == null || uuid.isEmpty(),
                "uuid is null or empty.");

        try {
            return m_service.unregisterBackend(getContext().getPackageName(),
                    uuid);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in registerBackend: " + e.toString());
        }

        return true;
    }
}
