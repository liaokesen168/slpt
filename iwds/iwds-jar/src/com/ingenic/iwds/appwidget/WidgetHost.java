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

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;

import com.ingenic.iwds.widget.LocalRemoteViews;

public class WidgetHost {

    private static final int HANDLE_UPDATE_WIDGET = 0;
    private static final int HANDLE_PROVIDER_CHANGED = 1;
    private static final int HANDLE_PROVIDERS_CHANGED = 2;
    private static final int HANDLE_VIEW_DATA_CHANGED = 3;

    private Context mContext;
    private String mPackage;
    private Callbacks mCallbacks;
    private final WidgetManager mManager;
    private final DisplayMetrics mDisplayMetrics;

    private SparseArray<WidgetHostView> mViews = new SparseArray<WidgetHostView>();

    private static class Callbacks extends IWidgetHost.Stub {
        private final Handler mHandler;

        public Callbacks(Handler handler) {
            mHandler = handler;
        }

        @Override
        public void updateWidget(int widgetId, LocalRemoteViews views) {
            mHandler.obtainMessage(HANDLE_UPDATE_WIDGET, widgetId, -1, views).sendToTarget();
        }

        @Override
        public void providerChanged(int wid, WidgetProviderInfo info) {
            mHandler.obtainMessage(HANDLE_PROVIDER_CHANGED, wid, -1, info).sendToTarget();
        }

        @Override
        public void providersChanged() {
            mHandler.sendEmptyMessage(HANDLE_PROVIDERS_CHANGED);
        }
    }

    private class HostHandler extends Handler {
        public HostHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case HANDLE_UPDATE_WIDGET:
                updateWidgetView(msg.arg1, (LocalRemoteViews) msg.obj);
                break;

            case HANDLE_PROVIDER_CHANGED:
                onProviderChanged(msg.arg1, (WidgetProviderInfo) msg.obj);
                break;
            case HANDLE_PROVIDERS_CHANGED:
                onProvidersChanged();
                break;
            default:
                break;
            }
        }
    }

    public WidgetHost(WidgetManager manager) {
        mContext = manager.getContext();
        mDisplayMetrics = mContext.getResources().getDisplayMetrics();
        mManager = manager;
        HostHandler handler = new HostHandler(mContext.getMainLooper());
        mCallbacks = new Callbacks(handler);
    }

    /**
     * 启动监听
     */
    public void startListening() {
        ArrayList<LocalRemoteViews> updatedViews = new ArrayList<LocalRemoteViews>();

        if (mPackage == null) {
            mPackage = mContext.getPackageName();
        }
        int[] updatedIds = mManager.startListening(mCallbacks, mPackage, updatedViews);

        final int N = updatedIds.length;
        for (int i = 0; i < N; i++) {
            updateWidgetView(updatedIds[i], updatedViews.get(i));
        }
    }

    /**
     * 停止监听
     */
    public void stopListening() {
        if (mPackage == null) {
            mPackage = mContext.getPackageName();
        }
        mManager.stopListening(mPackage);

        clearViews();
    }

    private void clearViews() {
        synchronized (mViews) {
            mViews.clear();
        }
    }

    /**
     * 创建控件视图
     * 
     * @param context 上下文
     * @param info 控件提供者的信息
     * @return 控件视图
     */
    public WidgetHostView createView(Context context, WidgetProviderInfo info) {
        int wid = bindWidget(info);

        WidgetHostView v;
        synchronized (mViews) {
            v = mViews.get(wid);
        }

        if (v == null) {
            v = onCreateView(mContext, wid, info);
        }
        v.setAppWidget(wid, info);

        synchronized (mViews) {
            mViews.put(wid, v);
        }

        LocalRemoteViews views = mManager.getWidgetViews(wid);
        v.updateWidget(views);

        return v;
    }

    /**
     * 创建控件视图
     * 
     * @param context 上下文
     * @param widgetId 控件ID
     * @param info 控件提供者的信息
     * @return 控件视图
     */
    protected WidgetHostView onCreateView(Context context, int widgetId, WidgetProviderInfo info) {
        return new WidgetHostView(context, mManager);
    }

    private int bindWidget(WidgetProviderInfo info) {
        if (mPackage == null) {
            mPackage = mContext.getPackageName();
        }

        return mManager.bindWidget(mPackage, info.provider);
    }

    private void updateWidgetView(int wid, LocalRemoteViews views) {
        WidgetHostView v;

        synchronized (mViews) {
            v = mViews.get(wid);
        }

        if (v != null) {
            v.updateWidget(views);
        }
    }

    /**
     * 删除控件视图，同时删除控件
     * 
     * @param view 需删除的控件视图
     */
    public void deleteWidgetView(WidgetHostView view) {
        int widgetId = getKey(view);

        if (widgetId != -1) {
            deleteWidget(widgetId);
        }
    }

    private int getKey(WidgetHostView v) {
        synchronized (mViews) {
            int idx = mViews.indexOfValue(v);
            if (idx < 0) return -1;

            return mViews.keyAt(idx);
        }
    }

    private void deleteWidget(int wid) {
        synchronized (mViews) {
            mViews.delete(wid);
        }

        mManager.deleteWidget(wid);
    }

    /**
     * 控件提供者发生改变的回调接口
     * 
     * @param widgetId 发生改变的控件ID
     * @param info 发生改变后的控件提供者的信息
     */
    protected void onProviderChanged(int widgetId, WidgetProviderInfo info) {
        info.width = TypedValue.complexToDimensionPixelSize(info.width, mDisplayMetrics);
        info.height = TypedValue.complexToDimensionPixelSize(info.height, mDisplayMetrics);

        WidgetHostView v;
        synchronized (mViews) {
            v = mViews.get(widgetId);
        }

        if (v != null) {
            v.resetWidget(info);
        }
    }

    /**
     * 控件提供者数量发生改变的回调接口
     */
    protected void onProvidersChanged() {}
}