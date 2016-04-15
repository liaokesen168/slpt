/*
 * Copyright (C) 2015 Ingenic Semiconductor
 * 
 * LiJinWen(Kevin)<kevin.jwli@ingenic.com>
 * 
 * Elf/iwds-jar
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.ingenic.iwds.appwidget;

import java.util.Collections;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;

import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.widget.LocalRemoteViews;

public class WidgetManager extends ServiceManagerContext {

    /**
     * 表明无效的控件ID
     */
    public static final int INVALID_WIDGET_ID = 0;

    /**
     * meta-data标签中，声明为控件提供者使用的名称
     */
    public static final String META_DATA_WIDGET_PROVIDER = "widget.provider.local";

    /**
     * 绑定控件的Action
     */
    public static final String ACTION_BIND_WIDGET = "com.ingenic.iwds.ACTION_BIND_WIDGET";

    private IWidgetService mService;

    public WidgetManager(Context context) {
        super(context);

        m_serviceClientProxy = new ServiceClientProxy() {

            @Override
            public void onServiceDisconnected(boolean unexpected) {}

            @Override
            public void onServiceConnected(IBinder binder) {
                mService = IWidgetService.Stub.asInterface(binder);
            }

            @Override
            public IBinder getBinder() {
                return mService.asBinder();
            }
        };
    }

    int[] startListening(IWidgetHost callbacks, String hpkg, List<LocalRemoteViews> updatedViews) {
        try {
            return mService.startListening(callbacks, hpkg, updatedViews);
        } catch (RemoteException e) {
            throw new RuntimeException("Widget server dead?", e);
        }
    }

    void stopListening(String hpkg) {
        try {
            mService.stopListening(hpkg);
        } catch (RemoteException e) {
            throw new RuntimeException("Widget server dead?", e);
        }
    }

    int bindWidget(String hpkg, ComponentName provider) {
        try {
            return mService.bindWidget(hpkg, provider);
        } catch (RemoteException e) {
            throw new RuntimeException("Widget server dead?", e);
        }
    }

    LocalRemoteViews getWidgetViews(int widgetId) {
        try {
            return mService.getWidgetViews(widgetId);
        } catch (RemoteException e) {
            throw new RuntimeException("Widget server dead?", e);
        }
    }

    void getWidgetSize(int widgetId, int[] size) {
        try {
            mService.getWidgetSize(widgetId, size);
        } catch (RemoteException e) {
            throw new RuntimeException("Widget server dead?", e);
        }
    }

    void deleteWidget(int widgetId) {
        try {
            mService.deleteWidget(widgetId);
        } catch (RemoteException e) {
            throw new RuntimeException("Widget server dead?", e);
        }
    }

    void updateWidgetSize(int widgetId, int width, int height) {
        try {
            mService.updateWidgetSize(widgetId, width, height);
        } catch (RemoteException e) {
            throw new RuntimeException("Widget server dead?", e);
        }
    }

    /**
     * 获取已安装的控件
     * 
     * @return 已安装的控件列表
     */
    public List<WidgetProviderInfo> getInstalledProviders() {
        if (mService == null) {
            return Collections.emptyList();
        }

        try {
            return mService.getInstalledProviders();
        } catch (RemoteException e) {
            throw new RuntimeException("Widget server dead?", e);
        }
    }
}
