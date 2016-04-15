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

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ingenic.iwds.utils.IwdsLog;
import com.ingenic.iwds.widget.LocalRemoteViews;

public class WidgetHostView extends FrameLayout {

    private static final String ERROR_TEXT = "Cannot find widget!";

    private static final int DEFAULT_WIDGET_PADDING = 0;

    private static final int VIEW_MODE_NOINIT = 0;
    private static final int VIEW_MODE_CONTENT = 1;
    private static final int VIEW_MODE_ERROR = 2;
    private static final int VIEW_MODE_DEFAULT = 3;

    private Context mContext;
    private Context mRemoteContext;
    private int mViewMode = VIEW_MODE_NOINIT;
    private View mView;
    private WidgetProviderInfo mInfo;
    private int mWidgetId;
    private int mLayoutId;
    private WidgetManager mManager;

    WidgetHostView(Context context, WidgetManager manager) {
        super(context);
        mContext = context;
        mManager = manager;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        final Context context = mRemoteContext != null ? mRemoteContext : mContext;
        return new LayoutParams(context, attrs);
    }

    void setWidgetManager(WidgetManager manager) {
        mManager = manager;
    }

    void setAppWidget(int widgetId, WidgetProviderInfo info) {
        mWidgetId = widgetId;
        mInfo = info;

        if (info != null) {
            Rect padding = getDefaultPaddingForWidget(mContext, null);
            setPadding(padding.left, padding.top, padding.right, padding.bottom);
            setContentDescription(info.label);
        }
    }

    public int getWidgetId() {
        return mWidgetId;
    }

    private static Rect getDefaultPaddingForWidget(Context context, Rect padding) {
        if (padding == null) {
            padding = new Rect(0, 0, 0, 0);
        }

        final float density = context.getResources().getDisplayMetrics().density;
        int px = Math.round(density * DEFAULT_WIDGET_PADDING);

        padding.set(px, px, px, px);
        return padding;
    }

    void updateWidget(LocalRemoteViews views) {
        boolean recycled = false;
        View content = null;
        Exception exception = null;

        if (views == null) {
            if (mViewMode == VIEW_MODE_DEFAULT) {
                return;
            }

            content = getDefaultView();
            mLayoutId = -1;
            mViewMode = VIEW_MODE_DEFAULT;
        } else {
            mRemoteContext = getRemoteContext();
            int layoutId = views.getLayoutId();

            if (content == null && layoutId == mLayoutId) {
                try {
                    views.reapply(mContext, mView);
                    content = mView;
                    recycled = true;
                } catch (Exception e) {
                    exception = e;
                }
            }

            if (content == null) {
                try {
                    content = views.apply(mContext, this);
                } catch (Exception e) {
                    exception = e;
                }
            }

            mLayoutId = layoutId;
            mViewMode = VIEW_MODE_CONTENT;
        }

        if (content == null) {
            if (mViewMode == VIEW_MODE_ERROR) {
                return;
            }

            IwdsLog.w(this, "updateWidget couldn't find any view, using error view");
            if (exception != null) {
                exception.printStackTrace();
            }

            content = getErrorView();
            mViewMode = VIEW_MODE_ERROR;
        }

        if (!recycled) {
            prepareView(content);
            addView(content);
        }

        if (mView != content) {
            removeView(mView);
            mView = content;
        }
    }

    private View getDefaultView() {
        View defaultView = null;
        Exception exception = null;

        try {
            if (mInfo != null && mManager != null) {
                Context theirContext = getRemoteContext();
                mRemoteContext = theirContext;

                LayoutInflater inflater = (LayoutInflater) theirContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                inflater.cloneInContext(theirContext);

                int layoutId = mInfo.initialLayout;
                defaultView = inflater.inflate(layoutId, this, false);
            } else {
                IwdsLog.w(this, "can't inflate defaultView because mInfo is missing");
            }
        } catch (Exception e) {
            exception = e;
        }

        if (exception != null) {
            IwdsLog.w(this, "Error inflating Widget " + mInfo + ": " + exception.toString());
        }

        if (defaultView == null) {
            defaultView = getErrorView();
        }

        return defaultView;
    }

    private Context getRemoteContext() {
        String packageName = mInfo.providerInfo.packageName;

        try {
            return mContext.createPackageContext(packageName, Context.CONTEXT_RESTRICTED);
        } catch (NameNotFoundException e) {
            IwdsLog.e(this, "Package name " + packageName + " not found");
            return mContext;
        }
    }

    private View getErrorView() {
        TextView tv = new TextView(mContext);
        tv.setText(ERROR_TEXT);
        tv.setBackgroundColor(Color.argb(127, 0, 0, 0));
        return tv;
    }

    private void prepareView(View view) {
        LayoutParams requested = (LayoutParams) view.getLayoutParams();

        if (requested == null) {
            requested = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }

        requested.gravity = Gravity.CENTER;
        view.setLayoutParams(requested);
    }

    void resetWidget(WidgetProviderInfo info) {
        mInfo = info;
        mViewMode = VIEW_MODE_NOINIT;
        updateWidget(null);
    }
}