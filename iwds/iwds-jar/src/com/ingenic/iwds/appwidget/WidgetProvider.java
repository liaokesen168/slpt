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

import java.lang.ref.WeakReference;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.SparseArray;

import com.ingenic.iwds.widget.LocalRemoteViews;

public abstract class WidgetProvider extends Service {

    private static final int HANDLE_PROVIDER_ENABLED = 1;
    private static final int HANDLE_PROVIDER_ADDED = 2;
    private static final int HANDLE_PROVIDER_UPDATE = 3;
    private static final int HANDLE_OPTIONS_CHANGED = 4;
    private static final int HANDLE_PROVIDER_DISABLED = 5;
    private static final int HANDLE_PROVIDER_DELETED = 6;
    private static final int HANDLE_PROVIDER_RESTORED = 7;

    private Handler mHandler;
    private Callbacks mCallbacks;
    private IWidgetService mService;
    private LocalRemoteViews mRemoteViews;
    private final SparseArray<String> mHostPkgs = new SparseArray<String>();

    private static class Callbacks extends IWidgetProvider.Stub {
        private final WeakReference<Handler> mWeakHandler;
        public Callbacks(Handler handler) {
            mWeakHandler = new WeakReference<Handler>(handler);
        }

        @Override
        public void onEnabled(IWidgetService service) {
            mWeakHandler.get().obtainMessage(HANDLE_PROVIDER_ENABLED, service).sendToTarget();
        }

        @Override
        public void onAdded(int widgetId, String hostPkg) {
            mWeakHandler.get().obtainMessage(HANDLE_PROVIDER_ADDED, widgetId, -1, hostPkg)
                    .sendToTarget();
        }

        @Override
        public void onUpdate(int[] widgetIds) {
            mWeakHandler.get().obtainMessage(HANDLE_PROVIDER_UPDATE, widgetIds).sendToTarget();
        }

        @Override
        public void onWidgetOptionsChanged(int widgetId, Bundle options) {
            mWeakHandler.get().obtainMessage(HANDLE_OPTIONS_CHANGED, widgetId, -1, options)
                    .sendToTarget();
        }

        @Override
        public void onDisabled() {
            mWeakHandler.get().sendEmptyMessage(HANDLE_PROVIDER_DISABLED);
        }

        @Override
        public void onDeleted(int widgetId) {
            mWeakHandler.get().obtainMessage(HANDLE_PROVIDER_DELETED, widgetId, -1).sendToTarget();
        }

        @Override
        public void onRestored(int[] oldWidgetIds, int[] newWidgetIds) {
            Message msg = mWeakHandler.get().obtainMessage(HANDLE_PROVIDER_RESTORED);
            Bundle data = new Bundle();
            data.putIntArray("oldIds", oldWidgetIds);
            data.putIntArray("newIds", newWidgetIds);
            msg.setData(data);
            msg.sendToTarget();
        }
    }

    private class UIHandler extends Handler {
        public UIHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case HANDLE_PROVIDER_ENABLED:
                enabled((IWidgetService) msg.obj);
                break;

            case HANDLE_PROVIDER_ADDED:
                onAdded(msg.arg1, (String) msg.obj);
                break;

            case HANDLE_PROVIDER_UPDATE:
                update((int[]) msg.obj);
                break;

            case HANDLE_OPTIONS_CHANGED:
                onWidgetOptionsChanged(msg.arg1, (Bundle) msg.obj);
                break;

            case HANDLE_PROVIDER_DISABLED:
                onDisabled();
                break;

            case HANDLE_PROVIDER_DELETED:
                onDeleted(msg.arg1);
                break;

            case HANDLE_PROVIDER_RESTORED:
                Bundle data = msg.getData();
                int[] oldWidgetIds = data.getIntArray("oldIds");
                int[] newWidgetIds = data.getIntArray("newIds");
                onRestored(oldWidgetIds, newWidgetIds);
                break;
            }
        }
    }

    private void update(int[] widgetIds) {
        for (int id : widgetIds) {
            mRemoteViews = onUpdate(mRemoteViews);

            try {
                mService.updateWidget(id, mRemoteViews);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void onWidgetOptionsChanged(int widgetId, Bundle options) {}

    private void onDeleted(int widgetId) {
        synchronized (mHostPkgs) {
            String hostPkg = mHostPkgs.get(widgetId);

            if (hostPkg != null) {
                onDeletedFromHost(hostPkg);
            }

            mHostPkgs.delete(widgetId);
        }
    }

    /**
     * 从显示端中被删除的回调接口
     * 
     * @param hostPkg 显示端的包名
     */
    protected void onDeletedFromHost(String hostPkg) {}

    private void enabled(IWidgetService service) {
        mService = service;
        onEnabled();
    }

    /**
     * 控件被启动的回调接口
     */
    protected void onEnabled() {}

    private void onAdded(int widgetId, String hostPkg) {
        synchronized (mHostPkgs) {
            mHostPkgs.put(widgetId, hostPkg);
        }

        onAddedToHost(hostPkg);

        if (mRemoteViews == null) {
            mRemoteViews = onCreateRemoteViews();
        }

        try {
            mService.updateWidget(widgetId, mRemoteViews);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 控件被添加到显示端的回调接口
     * 
     * @param hostPkg 显示端的包名
     */
    protected void onAddedToHost(String hostPkg) {}

    /**
     * 创建RemoteViews的回调接口
     * 
     * @return 控件更新使用的RemoteViews
     */
    protected abstract LocalRemoteViews onCreateRemoteViews();

    /**
     * 更新控件的回调接口
     * 
     * @param views 更新前的RemoteViews
     * @return 更新使用的RemoteViews
     */
    protected LocalRemoteViews onUpdate(LocalRemoteViews views) {
        return views;
    }

    /**
     * 控件被禁用的回调接口
     */
    protected void onDisabled() {}

    /**
     * 控件被重载的回调接口
     * 
     * @param oldWidgetIds 重载前的控件ID
     * @param newWidgetIds 重载后的控件ID
     */
    public void onRestored(int[] oldWidgetIds, int[] newWidgetIds) {}

    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new UIHandler(getMainLooper());
        mCallbacks = new Callbacks(mHandler);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mCallbacks.asBinder();
    }

    /**
     * 更新所有由本身提供的控件
     * 
     * @param views 更新使用的RemoteViews
     */
    public void updateAllWidgets(LocalRemoteViews views) {
        int[] widgetIds = null;

        synchronized (mHostPkgs) {
            final int N = mHostPkgs.size();

            widgetIds = new int[N];
            for (int i = 0; i < N; i++) {
                widgetIds[i] = mHostPkgs.keyAt(i);
            }
        }

        try {
            mService.updateWidgets(widgetIds, views);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新由本身提供，由提供的包名指向的应用显示的控件
     * 
     * @param hostPkg 指定的显示控件的应用包名
     * @param views 更新使用的RemoteViews
     */
    public void updateWidgetForHost(String hostPkg, LocalRemoteViews views) {
        int widgetId = getKey(hostPkg);

        try {
            mService.updateWidget(widgetId, views);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private int getKey(String pkg) {
        synchronized (mHostPkgs) {
            final int N = mHostPkgs.size();

            for (int i = 0; i < N; i++) {
                String hpkg = mHostPkgs.valueAt(i);

                if (hpkg.equals(pkg)) {
                    return mHostPkgs.keyAt(i);
                }
            }
        }
        return -1;
    }
}
