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
package com.ingenic.iwds.widget;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterViewAnimator;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ingenic.iwds.appwidget.IWidgetService;
import com.ingenic.iwds.appwidget.WidgetHostView;
import com.ingenic.iwds.utils.IwdsLog;

/**
 * 一个描述了可以在其他进程中显示的view层，它inflate于layout资源文件，并且这个类对提供一些基本操作修改view内容。
 */
public class LocalRemoteViews implements Parcelable {
    private static final int TAG_KEY_FILLININTENT = 0;
    static final int TAG_KEY_ROWTYPEDID = 1;

    static final String EXTRA_REMOTEADAPTER_WIDGET_ID = "remoteAdapterWidgetId";

    private String mPackage;
    private int mLayoutId;

    private ArrayList<Action> mActions;

    private MemoryUsageCounter mCounter;

    private boolean mIsRoot = true;

    private BitmapCache mBitmapCache;

    private boolean mIsWidgetCollectionChild;

    /**
     * 错误发生并发送异常时执行的动作
     */
    public static class ActionException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public ActionException(Exception e) {
            super(e);
        }

        public ActionException(String message) {
            super(message);
        }
    }

    private abstract static class Action implements Parcelable {
        static final int MERGE_REPLACE = 0;
        static final int MERGE_APPEND = 1;
        static final int MERGE_IGNORE = 2;

        protected int mViewId;

        public abstract void apply(View root, ViewGroup rootParent) throws ActionException;

        public void updateMemoryUsageEstimate(MemoryUsageCounter counter) {}

        public void setBitmapCache(BitmapCache cache) {}

        public int mergeBehavior() {
            return MERGE_REPLACE;
        }

        public abstract String getActionName();

        public String getUniqueKey() {
            return getActionName() + mViewId;
        }

        @Override
        public int describeContents() {
            return 0;
        }
    }

    private class MemoryUsageCounter {
        private int mMemoryUsage;

        public void clear() {
            mMemoryUsage = 0;
        }

        public void increment(int numBytes) {
            mMemoryUsage += numBytes;
        }

        public int getMemoryUsage() {
            return mMemoryUsage;
        }

        public void addBitmapMemory(Bitmap b) {
            final Bitmap.Config c = b.getConfig();

            int bpp = 4;
            if (c != null) {
                switch (c) {
                case ALPHA_8:
                    bpp = 1;
                    break;

                case RGB_565:
                case ARGB_4444:
                    bpp = 2;
                    break;

                case ARGB_8888:
                    bpp = 4;
                    break;
                }
            }

            increment(b.getWidth() * b.getHeight() * bpp);
        }
    }

    private static class BitmapCache {
        private ArrayList<Bitmap> mBitmaps;

        public BitmapCache() {
            mBitmaps = new ArrayList<Bitmap>();
        }

        public BitmapCache(Parcel in) {
            mBitmaps = new ArrayList<Bitmap>();

            int count = in.readInt();
            for (int i = 0; i < count; i++) {
                Bitmap b = Bitmap.CREATOR.createFromParcel(in);
                mBitmaps.add(b);
            }
        }

        public int getBitmapId(Bitmap b) {
            if (b == null) return -1;

            if (mBitmaps.contains(b)) {
                return mBitmaps.indexOf(b);
            } else {
                mBitmaps.add(b);
                return mBitmaps.size() - 1;
            }
        }

        public Bitmap getBitmapForId(int id) {
            if (id < 0 || id >= mBitmaps.size()) {
                return null;
            } else {
                return mBitmaps.get(id);
            }
        }

        public void assimilate(BitmapCache cache) {
            ArrayList<Bitmap> bitmapsToBeAdded = cache.mBitmaps;

            for (Bitmap b : bitmapsToBeAdded) {
                if (!mBitmaps.contains(b)) {
                    mBitmaps.add(b);
                }
            }
        }

        public void addBitmapMemory(MemoryUsageCounter counter) {
            for (Bitmap b : mBitmaps) {
                counter.addBitmapMemory(b);
            }
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mBitmaps.size());

            for (Bitmap b : mBitmaps) {
                b.writeToParcel(dest, flags);
            }
        }
    }

    private static boolean handleClick(View view, PendingIntent pendingIntent, Intent fillInIntent) {
        if (view == null) return false;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return handleClickSDK(view, pendingIntent, fillInIntent);
        }

        return handleClickSDK16(view, pendingIntent, fillInIntent);
    }

    private static boolean handleClickSDK(View view, PendingIntent pendingIntent,
            Intent fillInIntent) {
        Context context = view.getContext();

        try {
            context.startIntentSender(pendingIntent.getIntentSender(), fillInIntent,
                    Intent.FLAG_ACTIVITY_NEW_TASK, Intent.FLAG_ACTIVITY_NEW_TASK, 0);
        } catch (SendIntentException e) {
            IwdsLog.e("LocalRemoteViews", "Cannot send pendding intent: ");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static boolean handleClickSDK16(View view, PendingIntent pendingIntent,
            Intent fillInIntent) {
        Context context = view.getContext();

        ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(view, 0, 0,
                view.getMeasuredWidth(), view.getMeasuredHeight());

        try {
            context.startIntentSender(pendingIntent.getIntentSender(), fillInIntent,
                    Intent.FLAG_ACTIVITY_NEW_TASK, Intent.FLAG_ACTIVITY_NEW_TASK, 0,
                    opts.toBundle());
        } catch (SendIntentException e) {
            IwdsLog.e("LocalRemoteViews", "Cannot send pendding intent: ");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void handleItemClick(View view, PendingIntent pendingIntent) {
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;

            Intent fillInIntent = null;
            int childCount = vg.getChildCount();
            for (int i = 0; i < childCount; i++) {
                Object tag = vg.getChildAt(i).getTag(TAG_KEY_FILLININTENT);

                if (tag instanceof Intent) {
                    fillInIntent = (Intent) tag;
                    break;
                }
            }

            if (fillInIntent == null) return;

            final Rect rect = getSourceBounds(view);
            fillInIntent.setSourceBounds(rect);
            handleClick(view, pendingIntent, fillInIntent);
        }
    }

    private class SetOnClickPendingIntent extends Action {
        static final int TAG = 1;
        private PendingIntent mPendingIntent;

        public SetOnClickPendingIntent(int viewId, PendingIntent pendingIntent) {
            mViewId = viewId;
            mPendingIntent = pendingIntent;
        }

        public SetOnClickPendingIntent(Parcel in) {
            mViewId = in.readInt();

            if (in.readInt() != 0) {
                mPendingIntent = PendingIntent.CREATOR.createFromParcel(in);
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);

            if (mPendingIntent != null) {
                dest.writeInt(1);
                mPendingIntent.writeToParcel(dest, 0);
            } else {
                dest.writeInt(0);
            }
        }

        @Override
        public void apply(View root, ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null) return;

            if (mIsWidgetCollectionChild) {
                IwdsLog.w(this, "Cannot setOnClickPendingIntent for collection item (id: "
                        + mViewId + ")");

                ApplicationInfo app = root.getContext().getApplicationInfo();
                if (app != null && app.targetSdkVersion >= Build.VERSION_CODES.JELLY_BEAN) {
                    return;
                }
            }

            View.OnClickListener l = null;
            if (mPendingIntent != null) {
                l = new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        final Rect rect = getSourceBounds(v);

                        final Intent it = new Intent();
                        it.setSourceBounds(rect);
                        // handler.onClickHandler(v, pendingIntent, it);
                        handleClick(v, mPendingIntent, it);
                    }
                };
            }

            target.setOnClickListener(l);
        }

        @Override
        public String getActionName() {
            return "SetOnClickPendingIntent";
        }
    }

    private class SetOnClickHandler extends Action {
        static final int TAG = 2;
        private ILocalRemoteViewsClickHandler mHandler;

        public SetOnClickHandler(int viewId, OnClickHandler handler) {
            mViewId = viewId;
            mHandler = handler;
        }

        public SetOnClickHandler(Parcel in) {
            mViewId = in.readInt();
            IBinder b = in.readStrongBinder();
            mHandler = b != null ? ILocalRemoteViewsClickHandler.Stub.asInterface(b) : null;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);
            dest.writeStrongBinder(mHandler != null ? mHandler.asBinder() : null);
        }

        @Override
        public void apply(View root, final ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null) return;

            if (mIsWidgetCollectionChild) {
                IwdsLog.w(this, "Cannot setOnClickHandler for collection item (id: " + mViewId
                        + ")");

                ApplicationInfo app = root.getContext().getApplicationInfo();
                if (app != null && app.targetSdkVersion >= Build.VERSION_CODES.JELLY_BEAN) {
                    return;
                }
            }

            View.OnClickListener l = null;
            if (mHandler != null) {
                l = new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        try {
                            Context context = rootParent.getContext();

                            String pkg = context.getPackageName();
                            mHandler.onClickHandler(pkg, v.getId());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                };
            }
            target.setOnClickListener(l);
        }

        @Override
        public String getActionName() {
            return "SetOnClickHandler";
        }
    }

    private class SetOnLongClickHandler extends Action {
        static final int TAG = 3;
        private ILocalRemoteViewsLongClickHandler mHandler;

        public SetOnLongClickHandler(int viewId, ILocalRemoteViewsLongClickHandler handler) {
            mViewId = viewId;
            mHandler = handler;
        }

        public SetOnLongClickHandler(Parcel in) {
            mViewId = in.readInt();
            IBinder b = in.readStrongBinder();
            mHandler = b != null ? ILocalRemoteViewsLongClickHandler.Stub.asInterface(b) : null;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);
            dest.writeStrongBinder(mHandler != null ? mHandler.asBinder() : null);
        }

        @Override
        public void apply(View root, final ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null) return;

            if (mIsWidgetCollectionChild) {
                IwdsLog.w(this, "Cannot setOnLongClickHandler for collection item (id: " + mViewId
                        + ")");

                ApplicationInfo app = root.getContext().getApplicationInfo();
                if (app != null && app.targetSdkVersion >= Build.VERSION_CODES.JELLY_BEAN) {
                    return;
                }
            }

            View.OnLongClickListener l = null;
            if (mHandler != null) {
                l = new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        Context context = rootParent.getContext();

                        String pkg = context.getPackageName();
                        try {
                            mHandler.onLongClickHandler(pkg, v.getId());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            return false;
                        }

                        return true;
                    }
                };
            }
            target.setOnLongClickListener(l);
        }

        @Override
        public String getActionName() {
            return "SetOnLongClickHandler";
        }

    }

    private class SetDrawableParameters extends Action {
        static final int TAG = 4;
        private boolean m2Backageground;
        private int mAlpha;
        private int mColorFilter;
        private PorterDuff.Mode mMode;
        private int mLevel;
        public SetDrawableParameters(int viewId, boolean toBackground, int alpha, int colorFilter,
                PorterDuff.Mode mode, int level) {
            mViewId = viewId;
            m2Backageground = toBackground;
            mAlpha = alpha;
            mColorFilter = colorFilter;
            mMode = mode;
            mLevel = level;
        }

        public SetDrawableParameters(Parcel in) {
            mViewId = in.readInt();
            m2Backageground = in.readInt() != 0;
            mAlpha = in.readInt();
            mColorFilter = in.readInt();
            mMode = readPorterDuffMode(in);
            mLevel = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);
            dest.writeInt(m2Backageground ? 1 : 0);
            dest.writeInt(mAlpha);
            dest.writeInt(mColorFilter);

            if (mMode != null) {
                dest.writeInt(mMode.ordinal());
            } else {
                dest.writeInt(-1);
            }

            dest.writeInt(mLevel);
        }

        @Override
        public void apply(View root, ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null) return;

            Drawable drawable = null;
            if (m2Backageground) {
                drawable = target.getBackground();
            } else if (target instanceof ImageView) {
                drawable = ((ImageView) target).getDrawable();
            }

            if (drawable != null) {
                if (mAlpha != -1) {
                    drawable.mutate().setAlpha(mAlpha);
                }

                if (mMode != null) {
                    drawable.mutate().setColorFilter(mColorFilter, mMode);
                }

                if (mLevel != -1) {
                    drawable.mutate().setLevel(mLevel);
                }
            }
        }

        @Override
        public String getActionName() {
            return "SetDrawableParameters" + (m2Backageground ? "Background" : "Image");
        }
    }

    private static PorterDuff.Mode readPorterDuffMode(Parcel in) {
        int mode = in.readInt();

        PorterDuff.Mode[] modes = PorterDuff.Mode.values();
        if (mode >= 0 && mode < modes.length) {
            return modes[mode];
        } else {
            return PorterDuff.Mode.CLEAR;
        }
    }

    private class ViewGroupAction extends Action {
        static final int TAG = 5;
        private LocalRemoteViews mViews;

        public ViewGroupAction(int viewId, LocalRemoteViews views) {
            mViewId = viewId;
            mViews = views;

            if (views != null) {
                configureRemoteViewsAsChild(views);
            }
        }

        public ViewGroupAction(Parcel in, BitmapCache cache) {
            mViewId = in.readInt();

            if (in.readInt() != 0) {
                mViews = new LocalRemoteViews(in, cache);
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);

            if (mViews != null) {
                dest.writeInt(1);
                mViews.writeToParcel(dest, flags);
            } else {
                dest.writeInt(0);
            }
        }

        @Override
        public void apply(View root, ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null || !(target instanceof ViewGroup)) return;

            final Context context = root.getContext();
            ViewGroup vg = (ViewGroup) target;
            if (mViews != null) {
                vg.addView(mViews.apply(context, vg));
            } else {
                vg.removeAllViews();
            }
        }

        @Override
        public void updateMemoryUsageEstimate(MemoryUsageCounter counter) {
            if (mViews != null) {
                counter.increment(mViews.estimateMemoryUsage());
            }
        }

        @Override
        public void setBitmapCache(BitmapCache cache) {
            if (mViews != null) {
                mViews.setBitmapCache(cache);
            }
        }

        @Override
        public int mergeBehavior() {
            return MERGE_APPEND;
        }

        @Override
        public String getActionName() {
            return "ViewGroupAction" + (mViews != null ? "Add" : "Remove");
        }
    }

    private void configureRemoteViewsAsChild(LocalRemoteViews lrv) {
        mBitmapCache.assimilate(lrv.mBitmapCache);

        lrv.setBitmapCache(mBitmapCache);
        lrv.setNotRoot();
    }

    private class SetEmptyView extends Action {
        static final int TAG = 6;
        private int mEmptyId;

        public SetEmptyView(int viewId, int emptyId) {
            mViewId = viewId;
            mEmptyId = emptyId;
        }

        public SetEmptyView(Parcel in) {
            mViewId = in.readInt();
            mEmptyId = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);
            dest.writeInt(mEmptyId);
        }

        @Override
        public void apply(View root, ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null) return;

            final View emptyView = root.findViewById(mEmptyId);
            if (target instanceof AdapterView<?>) {
                AdapterView<?> av = (AdapterView<?>) target;
                av.setEmptyView(emptyView);
            } else if (target instanceof android.widget.AdapterView<?>) {
                android.widget.AdapterView<?> av = (android.widget.AdapterView<?>) target;
                av.setEmptyView(emptyView);
            }
        }

        @Override
        public String getActionName() {
            return "SetEmptyView";
        }
    }

    private class SetPendingIntentTemplate extends Action {
        static final int TAG = 7;
        private PendingIntent mPendingIntent;
        public SetPendingIntentTemplate(int viewId, PendingIntent pendingIntent) {
            mViewId = viewId;
            mPendingIntent = pendingIntent;
        }

        public SetPendingIntentTemplate(Parcel in) {
            mViewId = in.readInt();

            if (in.readInt() != 0) {
                mPendingIntent = PendingIntent.readPendingIntentOrNullFromParcel(in);
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);

            if (mPendingIntent != null) {
                dest.writeInt(1);
                mPendingIntent.writeToParcel(dest, flags);
            } else {
                dest.writeInt(0);
            }
        }

        @Override
        public void apply(View root, ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null) return;

            if (target instanceof AdapterView<?>) {
                AdapterView<?> av = (AdapterView<?>) target;

                AdapterView.OnItemClickListener l = new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        handleItemClick(view, mPendingIntent);
                    }
                };

                av.setOnItemClickListener(l);
                av.setTag(mPendingIntent);
            } else if (target instanceof android.widget.AdapterView<?>) {
                android.widget.AdapterView<?> av = (android.widget.AdapterView<?>) target;

                android.widget.AdapterView.OnItemClickListener l = new android.widget.AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(android.widget.AdapterView<?> parent, View view,
                            int position, long id) {
                        handleItemClick(view, mPendingIntent);
                    }
                };

                av.setOnItemClickListener(l);
                av.setTag(mPendingIntent);
            } else {
                IwdsLog.e(this, "Cannot setPendingIntentTemplate on a view which is not"
                        + "an AdapterView (id: " + mViewId + ")");
            }
        }

        @Override
        public String getActionName() {
            return "SetPendingIntentTemplate";
        }
    }

    private class SetOnClickFillInIntent extends Action {
        static final int TAG = 8;
        private Intent mFillInIntent;
        public SetOnClickFillInIntent(int viewId, Intent fillInIntent) {
            mViewId = viewId;
            mFillInIntent = fillInIntent;
        }

        public SetOnClickFillInIntent(Parcel in) {
            mViewId = in.readInt();

            if (in.readInt() != 0) {
                mFillInIntent = Intent.CREATOR.createFromParcel(in);
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);

            if (mFillInIntent != null) {
                dest.writeInt(1);
                mFillInIntent.writeToParcel(dest, 0);
            } else {
                dest.writeInt(0);
            }
        }

        @Override
        public void apply(View root, ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null) return;

            if (!mIsWidgetCollectionChild) {
                IwdsLog.e(this, "The method setOnClickFillInIntent is available "
                        + "only from RemoteViewsFactory (ie. on collection items).");
                return;
            }

            if (target == root) {
                target.setTag(TAG_KEY_FILLININTENT, mFillInIntent);
            } else if (mFillInIntent != null) {
                View.OnClickListener l = new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        View parent = (View) v.getParent();

                        while (parent != null && !(parent instanceof AdapterView<?>)
                                && !(parent instanceof android.widget.AdapterView<?>)
                                && !(parent instanceof WidgetHostView)) {
                            parent = (View) parent.getParent();
                        }

                        if (parent instanceof WidgetHostView || parent == null) {
                            IwdsLog.e(this, "Collection item doesn't have AdapterView parent");
                            return;
                        }

                        if (!(parent.getTag() instanceof PendingIntent)) {
                            IwdsLog.e(this, "Attempting setOnClickFillInIntent without"
                                    + " calling setPendingIntentTemplate on parent.");
                            return;
                        }

                        PendingIntent pendingIntent = (PendingIntent) parent.getTag();
                        final Rect rect = getSourceBounds(v);
                        mFillInIntent.setSourceBounds(rect);
                        handleClick(v, pendingIntent, mFillInIntent);
                    }
                };
                target.setOnClickListener(l);
            }
        }

        @Override
        public String getActionName() {
            return "SetOnClickFillInIntent";
        }
    }

    private class SetOnItemClickHandler extends Action {
        static final int TAG = 9;

        public SetOnItemClickHandler(Parcel in) {}

        @Override
        public void writeToParcel(Parcel dest, int flags) {}

        @Override
        public void apply(View root, ViewGroup rootParent) throws ActionException {}

        @Override
        public String getActionName() {
            return "SetOnItemClickHandler";
        }
    }

    private class SetOnItemLongClickHandler extends Action {
        static final int TAG = 10;

        public SetOnItemLongClickHandler(Parcel in) {}

        @Override
        public void writeToParcel(Parcel dest, int flags) {}

        @Override
        public void apply(View root, ViewGroup rootParent) throws ActionException {}

        @Override
        public String getActionName() {
            return "SetOnItemLongClickHandler";
        }
    }

    private class SetRemoteViewsAdapterIntent extends Action {
        static final int TAG = 11;
        private IWidgetService mService;
        private Intent mIntent;

        public SetRemoteViewsAdapterIntent(int viewId, IBinder service, Intent intent) {
            mViewId = viewId;
            mService = IWidgetService.Stub.asInterface(service);
            mIntent = intent;
        }

        public SetRemoteViewsAdapterIntent(Parcel in) {
            mViewId = in.readInt();

            IBinder b = in.readStrongBinder();
            mService = b != null ? IWidgetService.Stub.asInterface(b) : null;

            if (in.readInt() != 0) {
                mIntent = Intent.CREATOR.createFromParcel(in);
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);
            dest.writeStrongBinder(mService != null ? mService.asBinder() : null);

            if (mIntent != null) {
                dest.writeInt(1);
                mIntent.writeToParcel(dest, flags);
            } else {
                dest.writeInt(0);
            }
        }

        @Override
        public void apply(View root, ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null || mIntent == null) return;

            if (rootParent instanceof WidgetHostView) {
                IwdsLog.e(this, "SetRemoteViewsAdapterIntent action can only be used for "
                        + "AppWidgets (root id: " + mViewId + ")");
                return;
            }

            if (!(target instanceof AbsListView)) {
                IwdsLog.e(this, "Cannot setRemoteViewsAdapter on a view which is not "
                        + "an AbsListView or AdapterViewAnimator (id: " + mViewId + ")");
                return;
            }

            if (!(rootParent instanceof WidgetHostView)) return;

            WidgetHostView host = (WidgetHostView) rootParent;
            mIntent.putExtra(EXTRA_REMOTEADAPTER_WIDGET_ID, host.getWidgetId());

            if (target instanceof AbsListView) {
                AbsListView v = (AbsListView) target;
                v.setRemoteViewsAdapter(mService, mIntent);
            }
        }

        @Override
        public String getActionName() {
            return "SetRemoteViewsAdapterIntent";
        }
    }

    private class SetRemoteViewsAdapterList extends Action {
        static final int TAG = 12;

        private ArrayList<LocalRemoteViews> mList;
        private int mTypeCount;

        public SetRemoteViewsAdapterList(int viewId, ArrayList<LocalRemoteViews> list, int typeCount) {
            mViewId = viewId;
            mList = list;
            mTypeCount = typeCount;
        }

        public SetRemoteViewsAdapterList(Parcel in) {
            mViewId = in.readInt();
            mTypeCount = in.readInt();

            int count = in.readInt();
            if (count > 0) {
                mList = new ArrayList<LocalRemoteViews>(count);

                for (int i = 0; i < count; i++) {
                    mList.add(CREATOR.createFromParcel(in));
                }
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);
            dest.writeInt(mTypeCount);

            if (mList != null) {
                int count = mList.size();

                dest.writeInt(count);
                for (int i = 0; i < count; i++) {
                    LocalRemoteViews lrv = mList.get(i);

                    lrv.writeToParcel(dest, flags);
                }
            } else {
                dest.writeInt(0);
            }
        }

        @Override
        public void apply(View root, ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null) return;

            if (!(rootParent instanceof WidgetHostView)) {
                IwdsLog.e(this, "SetRemoteViewsAdapterList action can only be used for "
                        + "Widgets (root id: " + mViewId + ")");
                return;
            }

            if (!(target instanceof AbsListView)) {
                IwdsLog.e(this, "Cannot setRemoteViewsAdapter on a view which is not "
                        + "an AbsListView (id: " + mViewId + ")");
                return;
            }

            AbsListView v = (AbsListView) target;
            Adapter a = v.getAdapter();
            if (a instanceof LocalRemoteViewsListAdapter && mTypeCount <= a.getViewTypeCount()) {
                ((LocalRemoteViewsListAdapter) a).setViewList(mList);
            } else {
                v.setAdapter(new LocalRemoteViewsListAdapter(v.getContext(), mList, mTypeCount));
            }
        }

        @Override
        public String getActionName() {
            return "SetRemoteViewsAdapterList";
        }

    }

    private class SetTextViewText extends Action {
        static final int TAG = 13;

        private static final int TEXT_ID = 1;
        private static final int CHARSEQUENCE = 2;

        private Object mData;
        private int mType;

        public SetTextViewText(int viewId, int type, Object data) {
            mViewId = viewId;
            mType = type;
            mData = data;
        }

        public SetTextViewText(Parcel in) {
            mViewId = in.readInt();
            mType = in.readInt();

            switch (mType) {
            case TEXT_ID:
                mData = in.readInt();
                break;

            case CHARSEQUENCE:
                if (in.readInt() != 0) {
                    mData = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
                }
                break;
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);
            dest.writeInt(mType);

            switch (mType) {
            case TEXT_ID:
                dest.writeInt((Integer) mData);
                break;

            case CHARSEQUENCE:
                if (mData != null) {
                    dest.writeInt(1);
                    TextUtils.writeToParcel((CharSequence) mData, dest, flags);
                } else {
                    dest.writeInt(0);
                }
                break;
            }
        }

        @Override
        public void apply(View root, ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null) return;

            if (target instanceof TextView) {
                TextView tv = (TextView) target;

                switch (mType) {
                case TEXT_ID:
                    tv.setText((Integer) mData);
                    break;

                case CHARSEQUENCE:
                    tv.setText((CharSequence) mData);
                    break;
                }
            }
        }

        @Override
        public String getActionName() {
            return "SetTextViewText";
        }
    }

    private class TextViewDrawableAction extends Action {
        static final int TAG = 14;

        private boolean mIsRelative = false;
        private int mDL, mDT, mDR, mDB;

        public TextViewDrawableAction(int viewId, boolean isRelative, int dl, int dt, int dr, int db) {
            mViewId = viewId;
            mIsRelative = isRelative;

            mDL = dl;
            mDT = dt;
            mDR = dr;
            mDB = db;
        }

        public TextViewDrawableAction(Parcel in) {
            mViewId = in.readInt();
            mIsRelative = in.readInt() != 0;

            mDL = in.readInt();
            mDT = in.readInt();
            mDR = in.readInt();
            mDB = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);

            dest.writeInt(mViewId);
            dest.writeInt(mIsRelative ? 1 : 0);
            dest.writeInt(mDL);
            dest.writeInt(mDT);
            dest.writeInt(mDR);
            dest.writeInt(mDB);
        }

        @Override
        public void apply(View root, ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null) return;

            if (target instanceof TextView) {
                TextView tv = (TextView) target;

                if (mIsRelative) {
                    tv.setCompoundDrawablesRelativeWithIntrinsicBounds(mDL, mDT, mDR, mDB);
                } else {
                    tv.setCompoundDrawablesWithIntrinsicBounds(mDL, mDT, mDR, mDB);
                }
            }
        }

        @Override
        public String getActionName() {
            return "TextViewDrawableAction";
        }
    }

    private class TextViewDrawableColorFilterAction extends Action {
        static final int TAG = 15;

        private boolean mIsRelative;
        private int mIndex;
        private int mColor;
        private PorterDuff.Mode mMode;

        public TextViewDrawableColorFilterAction(int viewId, boolean isRelative, int index,
                int color, PorterDuff.Mode mode) {
            mViewId = viewId;
            mIsRelative = isRelative;
            mIndex = index;
            mColor = color;
            mMode = mode;
        }

        public TextViewDrawableColorFilterAction(Parcel in) {
            mViewId = in.readInt();
            mIsRelative = in.readInt() != 0;
            mIndex = in.readInt();
            mColor = in.readInt();
            mMode = readPorterDuffMode(in);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mIsRelative ? 1 : 0);
            dest.writeInt(mIndex);
            dest.writeInt(mColor);

            if (mMode != null) {
                dest.writeInt(mMode.ordinal());
            } else {
                dest.writeInt(-1);
            }
        }

        @Override
        public void apply(View root, ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null) return;

            if (target instanceof TextView) {
                TextView tv = (TextView) target;

                if (mIndex < 0 || mIndex >= 4) {
                    throw new IllegalStateException("index must be in range [0, 3].");
                }

                Drawable[] drawables = mIsRelative ? tv.getCompoundDrawablesRelative() : tv
                        .getCompoundDrawables();
                Drawable d = drawables[mIndex];
                if (d != null) {
                    d.mutate().setColorFilter(mColor, mMode);

                }
            }
        }

        @Override
        public String getActionName() {
            return "TextViewDrawableColorFilterAction";
        }
    }

    private class TextViewSizeAction extends Action {
        static final int TAG = 16;

        private int mUnit;
        private float mSize;

        public TextViewSizeAction(int viewId, int unit, float size) {
            mViewId = viewId;
            mUnit = unit;
            mSize = size;
        }

        public TextViewSizeAction(Parcel in) {
            mViewId = in.readInt();
            mUnit = in.readInt();
            mSize = in.readFloat();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);
            dest.writeInt(mUnit);
            dest.writeFloat(TAG);
        }

        @Override
        public void apply(View root, ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null) return;

            if (target instanceof TextView) {
                TextView tv = (TextView) target;
                tv.setTextSize(mUnit, mSize);
            }
        }

        @Override
        public String getActionName() {
            return "TextViewSizeAction";
        }
    }

    private class SetTextColor extends Action {
        static final int TAG = 17;

        static final int COLOR = 1;
        static final int COLOR_STATE_LIST = 2;

        private int mType;
        private Object mValue;

        public SetTextColor(int viewId, int type, Object value) {
            mViewId = viewId;
            mType = type;
            mValue = value;
        }

        public SetTextColor(Parcel in) {
            mViewId = in.readInt();
            mType = in.readInt();

            switch (mType) {
            case COLOR:
                mValue = in.readInt();
                break;

            case COLOR_STATE_LIST:
                if (in.readInt() != 0) {
                    mValue = ColorStateList.CREATOR.createFromParcel(in);
                }
                break;

            default:
                break;
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);
            dest.writeInt(mType);

            switch (mType) {
            case COLOR:
                dest.writeInt((Integer) mValue);
                break;

            case COLOR_STATE_LIST:
                if (mValue != null) {
                    dest.writeInt(1);
                    ((ColorStateList) mValue).writeToParcel(dest, flags);
                } else {
                    dest.writeInt(0);
                }
                break;

            default:
                break;
            }
        }

        @Override
        public void apply(View root, ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null) return;

            if (target instanceof TextView) {
                TextView tv = (TextView) target;

                switch (mType) {
                case COLOR:
                    tv.setTextColor((Integer) mValue);
                    break;

                case COLOR_STATE_LIST:
                    tv.setTextColor((ColorStateList) mValue);
                    break;

                default:
                    break;
                }
            }
        }

        @Override
        public String getActionName() {
            return "SetTextColor";
        }
    }

    private class ViewPaddingAction extends Action {
        static final int TAG = 18;
        private int mLeft, mTop, mRight, mBottom;

        public ViewPaddingAction(int viewId, int left, int top, int right, int bottom) {
            mViewId = viewId;
            mLeft = left;
            mTop = top;
            mRight = right;
            mBottom = bottom;
        }

        public ViewPaddingAction(Parcel in) {
            mViewId = in.readInt();
            mLeft = in.readInt();
            mTop = in.readInt();
            mRight = in.readInt();
            mBottom = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);

            dest.writeInt(mLeft);
            dest.writeInt(mTop);
            dest.writeInt(mRight);
            dest.writeInt(mBottom);
        }

        @Override
        public void apply(View root, ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null) return;

            target.setPadding(mLeft, mTop, mRight, mBottom);
        }

        @Override
        public String getActionName() {
            return "ViewPaddingAction";
        }
    }

    private class SetViewParameters extends Action {
        static final int TAG = 19;

        static final int VISIBILITY = 1;
        static final int CONTENT_DESCRIPTION = 2;
        static final int ACCESSIBILITY_TRAVERSAL_BEFORE = 3;
        static final int ACCESSIBILITY_TRAVERSAL_AFTER = 4;
        static final int LABEL_FOR = 5;

        private int mType;
        private Object mValue;

        public SetViewParameters(int viewId, int type, Object value) {
            mViewId = viewId;
            mType = type;
            mValue = value;
        }

        public SetViewParameters(Parcel in) {
            mViewId = in.readInt();
            mType = in.readInt();

            switch (mType) {
            case VISIBILITY:
                mValue = in.readInt();
                break;

            case CONTENT_DESCRIPTION:
                mValue = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
                break;

            case ACCESSIBILITY_TRAVERSAL_BEFORE:
                mValue = in.readInt();
                break;

            case ACCESSIBILITY_TRAVERSAL_AFTER:
                mValue = in.readInt();
                break;

            case LABEL_FOR:
                mValue = in.readInt();
                break;

            default:
                break;
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);
            dest.writeInt(mType);

            switch (mType) {
            case VISIBILITY:
                dest.writeInt((Integer) mValue);
                break;

            case CONTENT_DESCRIPTION:
                TextUtils.writeToParcel((CharSequence) mValue, dest, flags);
                break;

            case ACCESSIBILITY_TRAVERSAL_BEFORE:
                dest.writeInt((Integer) mValue);
                break;

            case ACCESSIBILITY_TRAVERSAL_AFTER:
                dest.writeInt((Integer) mValue);
                break;

            case LABEL_FOR:
                dest.writeInt((Integer) mValue);
                break;

            default:
                break;
            }
        }

        @Override
        public void apply(View root, ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null) return;

            switch (mType) {
            case VISIBILITY:
                target.setVisibility((Integer) mValue);
                break;

            case CONTENT_DESCRIPTION:
                target.setContentDescription((CharSequence) mValue);
                break;

            case ACCESSIBILITY_TRAVERSAL_BEFORE:
                // TODO:API add in 5.1.1(22)
                break;

            case ACCESSIBILITY_TRAVERSAL_AFTER:
                // TODO:API add in 5.1.1(22)
                break;

            case LABEL_FOR:
                target.setLabelFor((Integer) mValue);
                break;

            default:
                break;
            }
        }

        @Override
        public String getActionName() {
            return "SetViewParameters" + mType;
        }
    }

    private class SetImageViewResource extends Action {
        static final int TAG = 20;

        static final int RESOURCE = 1;
        static final int URI = 2;

        private int mType;
        private Object mValue;

        public SetImageViewResource(int viewId, int type, Object value) {
            mViewId = viewId;
            mType = type;
            mValue = value;
        }

        public SetImageViewResource(Parcel in) {
            mViewId = in.readInt();
            mType = in.readInt();

            switch (mType) {
            case RESOURCE:
                mValue = in.readInt();
                break;

            case URI:
                if (in.readInt() != 0) {
                    mValue = Uri.CREATOR.createFromParcel(in);
                }
                break;
            default:
                break;
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);
            dest.writeInt(mType);

            switch (mType) {
            case RESOURCE:
                dest.writeInt((Integer) mValue);
                break;

            case URI:
                if (mValue != null) {
                    dest.writeInt(1);
                    ((Uri) mValue).writeToParcel(dest, flags);
                } else {
                    dest.writeInt(0);
                }
                break;

            default:
                break;
            }
        }

        @Override
        public void apply(View root, ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null) return;

            if (target instanceof ImageView) {
                ImageView iv = (ImageView) target;

                switch (mType) {
                case RESOURCE:
                    iv.setImageResource((Integer) mValue);
                    break;

                case URI:
                    iv.setImageURI((Uri) mValue);
                    break;

                default:
                    break;
                }
            }
        }

        @Override
        public String getActionName() {
            return "SetImageViewResource";
        }
    }

    private class ReflectionAction extends Action {
        static final int TAG = 21;

        static final int BOOLEAN = 1;
        static final int BYTE = 2;
        static final int SHORT = 3;
        static final int INT = 4;
        static final int LONG = 5;
        static final int FLOAT = 6;
        static final int DOUBLE = 7;
        static final int CHAR = 8;
        static final int STRING = 9;
        static final int CHAR_SEQUENCE = 10;
        static final int URI = 11;
        // 使用BitmapReflectionAction
        static final int BITMAP = 12;
        static final int BUNDLE = 13;
        static final int INTENT = 14;
        static final int COLOR_STATE_LIST = 15;

        private String mMethod;
        private int mType;
        private Object mValue;

        public ReflectionAction(int viewId, String method, int type, Object value) {
            mViewId = viewId;
            mMethod = method;
            mType = type;
            mValue = value;
        }

        public ReflectionAction(Parcel in) {
            mViewId = in.readInt();
            mMethod = in.readString();

            mType = in.readInt();
            switch (mType) {
            case BOOLEAN:
                mValue = in.readInt() != 0;
                break;

            case BYTE:
                mValue = in.readByte();
                break;

            case SHORT:
                mValue = (short) in.readInt();
                break;

            case INT:
                mValue = in.readInt();
                break;

            case LONG:
                mValue = in.readLong();
                break;

            case FLOAT:
                mValue = in.readFloat();
                break;

            case DOUBLE:
                mValue = in.readDouble();
                break;

            case CHAR:
                mValue = (char) in.readInt();
                break;

            case STRING:
                mValue = in.readString();
                break;

            case CHAR_SEQUENCE:
                mValue = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
                break;

            case URI:
                if (in.readInt() != 0) {
                    mValue = Uri.CREATOR.createFromParcel(in);
                }
                break;

            case BITMAP:
                if (in.readInt() != 0) {
                    mValue = Bitmap.CREATOR.createFromParcel(in);
                }
                break;

            case BUNDLE:
                mValue = in.readBundle();
                break;

            case INTENT:
                if (in.readInt() != 0) {
                    mValue = Intent.CREATOR.createFromParcel(in);
                }
                break;

            case COLOR_STATE_LIST:
                if (in.readInt() != 0) {
                    mValue = ColorStateList.CREATOR.createFromParcel(in);
                }
                break;

            default:
                break;
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);
            dest.writeString(mMethod);
            dest.writeInt(mType);

            switch (mType) {
            case BOOLEAN:
                dest.writeInt((Boolean) mValue ? 1 : 0);
                break;

            case BYTE:
                dest.writeByte((Byte) mValue);
                break;

            case SHORT:
                dest.writeInt((Short) mValue);
                break;

            case INT:
                dest.writeInt((Integer) mValue);
                break;

            case LONG:
                dest.writeLong((Long) mValue);
                break;

            case FLOAT:
                dest.writeFloat((Float) mValue);
                break;

            case DOUBLE:
                dest.writeDouble((Double) mValue);
                break;

            case CHAR:
                dest.writeInt((int) ((Character) mValue).charValue());
                break;

            case STRING:
                dest.writeString((String) mValue);
                break;

            case CHAR_SEQUENCE:
                TextUtils.writeToParcel((CharSequence) mValue, dest, flags);
                break;

            case URI:
                dest.writeInt(mValue != null ? 1 : 0);
                if (mValue != null) {
                    ((Uri) mValue).writeToParcel(dest, flags);
                }
                break;

            case BITMAP:
                dest.writeInt(mValue != null ? 1 : 0);
                if (mValue != null) {
                    ((Bitmap) mValue).writeToParcel(dest, flags);
                }
                break;

            case BUNDLE:
                dest.writeBundle((Bundle) mValue);
                break;

            case INTENT:
                dest.writeInt(mValue != null ? 1 : 0);
                if (mValue != null) {
                    ((Intent) mValue).writeToParcel(dest, flags);
                }
                break;

            case COLOR_STATE_LIST:
                dest.writeInt(mValue != null ? 1 : 0);
                if (mValue != null) {
                    ((ColorStateList) mValue).writeToParcel(dest, flags);
                }
                break;

            default:
                break;
            }
        }

        @Override
        public void apply(View root, ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null) return;

            Class<?> param = getParameterType();
            if (param == null) {
                throw new ActionException("bad type: " + mType);
            }

            Class<? extends View> cls = target.getClass();
            Method method;
            try {
                method = cls.getMethod(mMethod, param);
            } catch (NoSuchMethodException e) {
                throw new ActionException("view: " + cls.getName() + " doesn't have method: "
                        + mMethod + "(" + param.getName() + ")");
            }

            method.setAccessible(true);
            try {
                method.invoke(target, mValue);
            } catch (Exception e) {
                throw new ActionException(e);
            }
        }

        private Class<?> getParameterType() {
            switch (mType) {
            case BOOLEAN:
                return boolean.class;

            case BYTE:
                return byte.class;

            case SHORT:
                return short.class;

            case INT:
                return int.class;

            case LONG:
                return long.class;

            case FLOAT:
                return float.class;

            case DOUBLE:
                return double.class;

            case CHAR:
                return char.class;

            case STRING:
                return String.class;

            case CHAR_SEQUENCE:
                return CharSequence.class;

            case URI:
                return Uri.class;

            case BITMAP:
                return Bitmap.class;

            case BUNDLE:
                return Bundle.class;

            case INTENT:
                return Intent.class;

            case COLOR_STATE_LIST:
                return ColorStateList.class;

            default:
                return null;
            }
        }

        @Override
        public int mergeBehavior() {
            if ("smoothScrollBy".equals(mMethod)) {
                return MERGE_APPEND;
            } else {
                return MERGE_REPLACE;
            }
        }

        @Override
        public String getActionName() {
            return "ReflectionAction" + mMethod + mType;
        }
    }

    private class ReflectionActionWithoutParams extends Action {
        static final int TAG = 22;

        private String mMethod;

        public ReflectionActionWithoutParams(int viewId, String method) {
            mViewId = viewId;
            mMethod = method;
        }

        public ReflectionActionWithoutParams(Parcel in) {
            mViewId = in.readInt();
            mMethod = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);
            dest.writeString(mMethod);
        }

        @Override
        public void apply(View root, ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null) return;

            Class<? extends View> cls = target.getClass();
            Method method;
            try {
                method = cls.getMethod(mMethod);
            } catch (NoSuchMethodException e) {
                throw new ActionException("view: " + cls.getName() + " doesn't have method: "
                        + mMethod + "()");
            }

            method.setAccessible(true);
            try {
                method.invoke(target);
            } catch (Exception e) {
                throw new ActionException(e);
            }
        }

        public int mergeBehavior() {
            if ("showNext".equals(mMethod) || "showPrevious".equals(mMethod)) {
                return MERGE_IGNORE;
            } else {
                return MERGE_REPLACE;
            }
        }

        @Override
        public String getActionName() {
            return "ReflectionActionWithoutParams" + mMethod;
        }
    }

    private class BitmapReflectionAction extends Action {
        static final int TAG = 23;

        private int mBitmapId;
        private Bitmap mBitmap;
        private String mMethod;

        BitmapReflectionAction(int viewId, String methodName, Bitmap bitmap) {
            mBitmap = bitmap;
            mViewId = viewId;
            mMethod = methodName;
            mBitmapId = mBitmapCache.getBitmapId(bitmap);
        }

        BitmapReflectionAction(Parcel in) {
            mViewId = in.readInt();
            mMethod = in.readString();
            mBitmapId = in.readInt();
            mBitmap = mBitmapCache.getBitmapForId(mBitmapId);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);
            dest.writeString(mMethod);
            dest.writeInt(mBitmapId);
        }

        @Override
        public void apply(View root, ViewGroup rootParent) throws ActionException {
            ReflectionAction ra = new ReflectionAction(mViewId, mMethod, ReflectionAction.BITMAP,
                    mBitmap);
            ra.apply(root, rootParent);
        }

        @Override
        public void setBitmapCache(BitmapCache bitmapCache) {
            mBitmapId = bitmapCache.getBitmapId(mBitmap);
        }

        @Override
        public String getActionName() {
            return "BitmapReflectionAction";
        }
    }

    private class SetChronometer extends Action {
        static final int TAG = 24;

        private long mBase;
        private String mFormat;
        private boolean mStarted;

        public SetChronometer(int viewId, long base, String format, boolean started) {
            mViewId = viewId;
            mBase = base;
            mFormat = format;
            mStarted = started;
        }

        public SetChronometer(Parcel in) {
            mViewId = in.readInt();
            mBase = in.readLong();
            mFormat = in.readString();
            mStarted = in.readInt() != 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);
            dest.writeLong(mBase);
            dest.writeString(mFormat);
            dest.writeInt(mStarted ? 1 : 0);
        }

        @Override
        public void apply(View root, ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null) return;

            if (target instanceof Chronometer) {
                Chronometer c = (Chronometer) target;

                c.setBase(mBase);
                c.setFormat(mFormat);

                if (mStarted) {
                    c.start();
                } else {
                    c.stop();
                }
            }
        }

        @Override
        public String getActionName() {
            return "SetChronometer";
        }
    }

    private class SetProgressBar extends Action {
        static final int TAG = 25;

        private boolean mIndeterminate;
        private int mMax;
        private int mProgress;

        public SetProgressBar(int viewId, int max, int progress, boolean indeterminate) {
            mViewId = viewId;
            mMax = max;
            mProgress = progress;
            mIndeterminate = indeterminate;
        }

        public SetProgressBar(Parcel in) {
            mViewId = in.readInt();
            mMax = in.readInt();
            mProgress = in.readInt();
            mIndeterminate = in.readInt() != 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);
            dest.writeInt(mMax);
            dest.writeInt(mProgress);
            dest.writeInt(mIndeterminate ? 1 : 0);
        }

        @Override
        public void apply(View root, ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null) return;

            if (target instanceof ProgressBar) {
                ProgressBar pb = (ProgressBar) target;

                pb.setIndeterminate(mIndeterminate);
                if (!mIndeterminate) {
                    pb.setMax(mMax);
                    pb.setProgress(mProgress);
                }
            }
        }

        @Override
        public String getActionName() {
            return "SetProgressBar";
        }
    }

    private class SetScrollPosition extends Action {
        static final int TAG = 26;

        private boolean mByOffset;
        private int mPosition;

        public SetScrollPosition(int viewId, int position, boolean byOffset) {
            mViewId = viewId;
            mPosition = position;
            mByOffset = byOffset;
        }

        public SetScrollPosition(Parcel in) {
            mViewId = in.readInt();
            mPosition = in.readInt();
            mByOffset = in.readInt() != 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);
            dest.writeInt(mPosition);
            dest.writeInt(mByOffset ? 1 : 0);
        }

        @Override
        public void apply(View root, ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null) return;

            if (target instanceof AbsListView) {
                AbsListView v = (AbsListView) target;

                if (mByOffset) {
                    v.smoothScrollByOffset(mPosition);
                } else {
                    v.smoothScrollToPosition(mPosition);
                }
            }
        }

        @Override
        public String getActionName() {
            return "SetScrollPosition";
        }
    }

    private class SetOnSeekBarChangedHandler extends Action {
        static final int TAG = 27;

        private IOnSeekBarChangedHandler mHandler;

        public SetOnSeekBarChangedHandler(int viewId, IOnSeekBarChangedHandler handler) {
            mViewId = viewId;
            mHandler = handler;
        }

        public SetOnSeekBarChangedHandler(Parcel in) {
            mViewId = in.readInt();
            IBinder b = in.readStrongBinder();
            mHandler = b != null ? IOnSeekBarChangedHandler.Stub.asInterface(b) : null;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(TAG);
            dest.writeInt(mViewId);
            dest.writeStrongBinder(mHandler != null ? mHandler.asBinder() : null);
        }

        @Override
        public void apply(View root, ViewGroup rootParent) {
            final View target = root.findViewById(mViewId);
            if (target == null) return;

            if (target instanceof SeekBar) {
                SeekBar sb = (SeekBar) target;

                SeekBar.OnSeekBarChangeListener l = null;
                if (mHandler != null) {
                    final String callingPkg = rootParent.getContext().getPackageName();

                    l = new SeekBar.OnSeekBarChangeListener() {

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            try {
                                mHandler.handleStopTrackingTouch(callingPkg, seekBar.getId());
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                            try {
                                mHandler.handleStartTrackingTouch(callingPkg, seekBar.getId());
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress,
                                boolean fromUser) {
                            try {
                                mHandler.handleProgressChanged(callingPkg, seekBar.getId(),
                                        progress, fromUser);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                }

                sb.setOnSeekBarChangeListener(l);
            }
        }

        @Override
        public String getActionName() {
            return "SetOnSeekBarChangedHandler";
        }
    }

    private static Rect getSourceBounds(View v) {
        final int[] pos = new int[2];
        v.getLocationOnScreen(pos);

        return new Rect(pos[0], pos[1], pos[0] + v.getWidth(), pos[1] + v.getHeight());
    }

    public LocalRemoteViews(String packageName, int layoutId) {
        mPackage = packageName;
        mLayoutId = layoutId;

        mBitmapCache = new BitmapCache();
        mCounter = new MemoryUsageCounter();
        recalculateMemoryUsage();
    }

    public LocalRemoteViews(Parcel in) {
        this(in, null);
    }

    protected LocalRemoteViews(Parcel in, BitmapCache bitmapCache) {
        if (bitmapCache == null) {
            mBitmapCache = new BitmapCache(in);
        } else {
            setBitmapCache(bitmapCache);
            setNotRoot();
        }

        mPackage = in.readString();
        mLayoutId = in.readInt();
        mIsWidgetCollectionChild = in.readInt() != 0;

        int count = in.readInt();
        if (count > 0) {
            mActions = new ArrayList<LocalRemoteViews.Action>(count);

            for (int i = 0; i < count; i++) {
                int tag = in.readInt();

                switch (tag) {
                case SetOnClickPendingIntent.TAG:
                    mActions.add(new SetOnClickPendingIntent(in));
                    break;

                case SetOnClickHandler.TAG:
                    mActions.add(new SetOnClickHandler(in));
                    break;

                case SetOnLongClickHandler.TAG:
                    mActions.add(new SetOnLongClickHandler(in));
                    break;

                case SetDrawableParameters.TAG:
                    mActions.add(new SetDrawableParameters(in));
                    break;

                case ViewGroupAction.TAG:
                    mActions.add(new ViewGroupAction(in, mBitmapCache));
                    break;

                case SetEmptyView.TAG:
                    mActions.add(new SetEmptyView(in));
                    break;

                case SetPendingIntentTemplate.TAG:
                    mActions.add(new SetPendingIntentTemplate(in));
                    break;

                case SetOnClickFillInIntent.TAG:
                    mActions.add(new SetOnClickFillInIntent(in));
                    break;

                case SetOnItemClickHandler.TAG:
                    mActions.add(new SetOnItemClickHandler(in));
                    break;

                case SetOnItemLongClickHandler.TAG:
                    mActions.add(new SetOnItemLongClickHandler(in));
                    break;

                case SetRemoteViewsAdapterIntent.TAG:
                    mActions.add(new SetRemoteViewsAdapterIntent(in));
                    break;

                case SetRemoteViewsAdapterList.TAG:
                    mActions.add(new SetRemoteViewsAdapterList(in));
                    break;

                case SetTextViewText.TAG:
                    mActions.add(new SetTextViewText(in));
                    break;

                case TextViewDrawableAction.TAG:
                    mActions.add(new TextViewDrawableAction(in));
                    break;

                case TextViewDrawableColorFilterAction.TAG:
                    mActions.add(new TextViewDrawableColorFilterAction(in));
                    break;

                case TextViewSizeAction.TAG:
                    mActions.add(new TextViewSizeAction(in));
                    break;

                case SetTextColor.TAG:
                    mActions.add(new SetTextColor(in));
                    break;

                case ViewPaddingAction.TAG:
                    mActions.add(new ViewPaddingAction(in));
                    break;

                case SetViewParameters.TAG:
                    mActions.add(new SetViewParameters(in));
                    break;

                case SetImageViewResource.TAG:
                    mActions.add(new SetImageViewResource(in));
                    break;

                case ReflectionAction.TAG:
                    mActions.add(new ReflectionAction(in));
                    break;

                case ReflectionActionWithoutParams.TAG:
                    mActions.add(new ReflectionActionWithoutParams(in));
                    break;

                case BitmapReflectionAction.TAG:
                    mActions.add(new BitmapReflectionAction(in));
                    break;

                case SetChronometer.TAG:
                    mActions.add(new SetChronometer(in));
                    break;

                case SetProgressBar.TAG:
                    mActions.add(new SetProgressBar(in));
                    break;

                case SetScrollPosition.TAG:
                    mActions.add(new SetScrollPosition(in));
                    break;

                case SetOnSeekBarChangedHandler.TAG:
                    mActions.add(new SetOnSeekBarChangedHandler(in));
                    break;

                default:
                    throw new ActionException("Tag " + tag + " not found");
                }
            }
        }

        mCounter = new MemoryUsageCounter();
        recalculateMemoryUsage();
    }

    /**
     * 取得当前进程的包名
     * 
     * @return 当前进程的包名
     */
    public String getPackage() {
        return mPackage;
    }

    /**
     * 取得显示到另一进程的视图布局ID
     * 
     * @return 显示到另一进程的布局ID
     */
    public int getLayoutId() {
        return mLayoutId;
    }

    private void setBitmapCache(BitmapCache bitmapCache) {
        mBitmapCache = bitmapCache;

        if (mActions != null) {
            for (Action a : mActions) {
                a.setBitmapCache(bitmapCache);
            }
        }
    }

    private void setNotRoot() {
        mIsRoot = false;
    }

    private void recalculateMemoryUsage() {
        mCounter.clear();

        if (mActions != null) {
            for (Action a : mActions) {
                a.updateMemoryUsageEstimate(mCounter);
            }
        }

        if (mIsRoot) {
            mBitmapCache.addBitmapMemory(mCounter);
        }
    }

    /**
     * 合并远程视图操作
     * 
     * @param views 被和并的远程视图操作类
     */
    public void mergeRemoteViews(LocalRemoteViews views) {
        if (views == null) return;

        LocalRemoteViews copy = views.clone();
        ArrayList<Action> newActions = copy.mActions;
        if (newActions == null) return;

        if (mActions == null) {
            mActions = new ArrayList<Action>();
        }

        HashMap<String, Action> actions = new HashMap<String, Action>();
        for (Action a : mActions) {
            actions.put(a.getUniqueKey(), a);
        }

        for (Action a : newActions) {
            String key = a.getUniqueKey();
            int mergeBehavior = a.mergeBehavior();

            if (actions.containsKey(key) && mergeBehavior == Action.MERGE_REPLACE) {
                mActions.remove(actions.get(key));
                actions.remove(key);
            }

            if (mergeBehavior == Action.MERGE_REPLACE || mergeBehavior == Action.MERGE_APPEND) {
                mActions.add(a);
            }
        }

        mBitmapCache = new BitmapCache();
        setBitmapCache(mBitmapCache);
    }

    int estimateMemoryUsage() {
        return mCounter.getMemoryUsage();
    }

    void setIsWidgetCollectionChild(boolean isWidgetCollectionChild) {
        mIsWidgetCollectionChild = isWidgetCollectionChild;
    }

    /**
     * Inflates视图对象并且应用到所有的动作中。
     * <p>
     * 调用注意：这个方法可能有异常抛出。
     * 
     * @param context 默认使用的context
     * @param parent 生成视图层将要填充的parent。此方法不会附加到层次结构。调用者应该在适当的时候处理。
     * @return 被inflated的视图层
     */
    public View apply(Context context, ViewGroup parent) {
        final Context contextForResources = getContextForResources(context);

        LayoutInflater inflater = (LayoutInflater) contextForResources
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.cloneInContext(contextForResources);

        LocalRemoteViews lrvToApply = getLocalRemoteViewsToApply(context);
        View result = inflater.inflate(lrvToApply.getLayoutId(), parent, false);
        lrvToApply.performApply(result, parent);
        return result;
    }

    /**
     * 为视图对象应用所有的动作
     * 
     * @param context 默认使用的context
     * @param v 要应用动作的视图。这个应该是{@link #apply(Context, ViewGroup)}调用的结果
     */
    public void reapply(Context context, View v) {
        LocalRemoteViews lrvToApply = getLocalRemoteViewsToApply(context);
        lrvToApply.performApply(v, (ViewGroup) v.getParent());
    }

    private LocalRemoteViews getLocalRemoteViewsToApply(Context context) {
        return this;
    }

    private Context getContextForResources(Context context) {
        try {
            return context.createPackageContext(mPackage, Context.CONTEXT_RESTRICTED);
        } catch (NameNotFoundException e) {
            IwdsLog.w(this, "Package name " + mPackage + " not found");
        }

        return context;
    }

    private void performApply(View v, ViewGroup parent) {
        if (mActions != null) {
            for (Action a : mActions) {
                a.apply(v, parent);
            }
        }
    }

    private void addAction(Action a) {
        if (mActions == null) {
            mActions = new ArrayList<Action>();
        }

        mActions.add(a);

        a.updateMemoryUsageEstimate(mCounter);
    }

    /**
     * 相当于当inflate指定的{@link LocalRemoteViews}后调用{@link ViewGroup#addView(View)}
     * 方法。它允许用户构建”nested(内部)“{@link LocalRemoteViews}。在某种情况下{@link LocalRemoteViews}可以回收布局，调用
     * {@link #removeAllViews(int)}方法清除所有存在的子视图。
     * 
     * @param viewId 要加入子视图的父{@link ViewGroup}的id
     * @param views 描述子视图的{@link LocalRemoteViews}
     */
    public void addView(int viewId, LocalRemoteViews views) {
        addAction(new ViewGroupAction(viewId, views));
    }

    /**
     * 相当于调用{@link ViewGroup#removeAllViews()}方法
     * 
     * @param viewId 要移除所有子视图的父{@link ViewGroup}的id
     */
    public void removeAllViews(int viewId) {
        addAction(new ViewGroupAction(viewId, null));
    }

    /**
     * 相当于调用{@link AdapterViewAnimator#showNext()}方法
     * 
     * @param viewId 调用{@link AdapterViewAnimator#showNext()}方法的视图ID
     */
    public void showNext(int viewId) {
        addAction(new ReflectionActionWithoutParams(viewId, "showNext"));
    }

    /**
     * 相当于调用{@link AdapterViewAnimator#showPrevious()}方法
     * 
     * @param viewId 调用{@link AdapterViewAnimator#showPrevious()}方法的视图ID
     */
    public void showPrevious(int viewId) {
        addAction(new ReflectionActionWithoutParams(viewId, "showPrevious"));
    }

    /**
     * 相当于调用{@link AdapterViewAnimator#setDisplayedChild(int)}方法
     * 
     * @param viewId 调用{@link AdapterViewAnimator#setDisplayedChild(int)}方法的视图id
     * @param childIndex 调用{@link AdapterViewAnimator#setDisplayedChild(int)}方法的参数，切换到该页显示
     */
    public void setDisplayedChild(int viewId, int childIndex) {
        setInt(viewId, "setDisplayedChild", childIndex);
    }

    /**
     * 相当于调用{@link View#setVisibility(int)}方法
     * 
     * @param viewId 修改可见性的视图ID
     * @param visibility 视图新的可见性
     */
    public void setViewVisibility(int viewId, int visibility) {
        addAction(new SetViewParameters(viewId, SetViewParameters.VISIBILITY, visibility));
    }

    /**
     * 相当于调用{@link TextView#setText(int, TextView.BufferType)}方法
     * 
     * @param viewId 修改文本显示的视图id
     * @param resId 新的文本资源id
     */
    public void setTextViewText(int viewId, int resId) {
        addAction(new SetTextViewText(viewId, SetTextViewText.TEXT_ID, resId));
    }

    /**
     * 相当于调用{@link TextView#setText(CharSequence, TextView.BufferType)}方法
     * 
     * @param viewId 修改文本显示的视图id
     * @param text 新的显示文本
     */
    public void setTextViewText(int viewId, CharSequence text) {
        addAction(new SetTextViewText(viewId, SetTextViewText.CHARSEQUENCE, text));
    }

    /**
     * 相当于调用{@link TextView#setTextSize(float)}方法
     * 
     * @param viewId 修改文本字体大小的视图id
     * @param size 新的字体大小（单位为px）
     */
    public void setTextViewTextSize(int viewId, float size) {
        setTextViewTextSize(viewId, TypedValue.COMPLEX_UNIT_PX, size);
    }

    /**
     * 相当于调用{@link TextView#setTextSize(int, float)}方法
     * 
     * @param viewId 修改文本字体大小的视图id
     * @param unit 新的字体大小单位
     * @param size 新的字体大小数值
     */
    public void setTextViewTextSize(int viewId, int unit, float size) {
        addAction(new TextViewSizeAction(viewId, unit, size));
    }

    /**
     * 相当于调用{@link TextView#setCompoundDrawablesWithIntrinsicBounds(int, int, int, int)}方法
     * 
     * @param viewId 修改显示的视图id
     * @param l 显示在左边的可绘制对象id
     * @param t 显示在上方的可绘制对象id
     * @param r 显示在右边的可绘制对象id
     * @param b 显示在下方的可绘制对象id
     */
    public void setTextViewCompoundDrawables(int viewId, int l, int t, int r, int b) {
        addAction(new TextViewDrawableAction(viewId, false, l, t, r, b));
    }

    /**
     * 相当于调用{@link TextView#setCompoundDrawablesRelativeWithIntrinsicBounds(int, int, int, int))}方法
     * 
     * @param viewId 修改显示的视图id
     * @param l 显示在左边的可绘制对象id
     * @param t 显示在上方的可绘制对象id
     * @param r 显示在右边的可绘制对象id
     * @param b 显示在下方的可绘制对象id
     */
    public void setTextViewCompoundDrawablesRelative(int viewId, int l, int t, int r, int b) {
        addAction(new TextViewDrawableAction(viewId, true, l, t, r, b));
    }

    void setTextViewCompoundDrawablesColorFilter(int viewId, int index, int color,
            PorterDuff.Mode mode) {
        if (index < 0 || index >= 4) {
            throw new IllegalArgumentException("index must be in range [0, 3].");
        }

        addAction(new TextViewDrawableColorFilterAction(viewId, false, index, color, mode));
    }

    void setTextViewCompoundDrawablesRelativeColorFilter(int viewId, int index, int color,
            PorterDuff.Mode mode) {
        if (index < 0 || index >= 4) {
            throw new IllegalArgumentException("index must be in range [0, 3].");
        }

        addAction(new TextViewDrawableColorFilterAction(viewId, true, index, color, mode));
    }

    /**
     * 相当于调用{@link ImageView#setImageResource(int)}方法
     * 
     * @param viewId 视图id
     * @param resId 用于绘制新的资源id
     */
    public void setImageViewResource(int viewId, int resId) {
        addAction(new SetImageViewResource(viewId, SetImageViewResource.RESOURCE, resId));
    }

    /**
     * 相当于调用{@link ImageView#setImageURI(Uri)}方法
     * 
     * @param viewId 视图id
     * @param uri 用于绘制新的资源{@link Uri}
     */
    public void setImageViewUri(int viewId, Uri uri) {
        addAction(new SetImageViewResource(viewId, SetImageViewResource.URI, uri));
    }

    /**
     * 相当于调用{@link ImageView#setImageBitmap(Bitmap)}方法
     * 
     * @param viewId 视图id
     * @param bitmap 用于绘制新的{@link Bitmap}
     */
    public void setImageViewBitmap(int viewId, Bitmap bitmap) {
        setBitmap(viewId, "setImageBitmap", bitmap);
    }

    /**
     * 相当于调用{@link AdapterView#setEmptyView(View)}方法
     * 
     * @param viewId 要设置空视图的视图id
     * @param emptyId 空视图的id
     */
    public void setEmptyView(int viewId, int emptyId) {
        addAction(new SetEmptyView(viewId, emptyId));
    }

    /**
     * 相当于调用{@link Chronometer#setBase(long)}、{@link Chronometer#setFormat(String)}以及
     * {@link Chronometer#start()}或{@link Chronometer#stop()}方法。
     * 
     * @param viewId 视图id
     * @param base 计时器应该从0:00开始读取的时间，这个时间应该基于SystemClock.elapsedRealtime()
     * @param format 计时器的格式字符串，或者是简单显示时间的<code>null</code>值
     * @param started <code>true</code>表示开始，<code>false</code>表示停止
     */
    public void setChronometer(int viewId, long base, String format, boolean started) {
        addAction(new SetChronometer(viewId, base, format, started));
    }

    /**
     * 如果indeterminate为<code>true</code>的话，相当于调用{@link ProgressBar#setMax(int)}方法、
     * {@link ProgressBar#setProgress(int))方法和{@link ProgressBar#setIndeterminate(boolean)}
     * 方法；否则最大值和当前进度值被忽略。
     * 
     * @param viewId 视图的id
     * @param max 进度条的最大值
     * @param progress 进度条的当前进度值
     * @param indeterminate 如果进度条是不确定的为{@code true}，反之为{@code false}
     */
    public void setProgressBar(int viewId, int max, int progress, boolean indeterminate) {
        addAction(new SetProgressBar(viewId, max, progress, indeterminate));
    }

    /**
     * 相当于调用{@link View#setOnClickListener(View.OnClickListener)}方法启动准备好的{@link PendingIntent}。
     * 
     * 当在集合中设置条目的点击动作时（比如说：AmazingListView, {@link android.widget.StackView}等），这个方法不起作用。改为
     * {@link #setPendingIntentTemplate(int, PendingIntent)}方法和
     * {@link #setOnClickFillInIntent(int, Intent)}方法一起使用才有效。
     * 
     * @param viewId 当点击时将要触发{@link PendingIntent}的视图id
     * @param pendingIntent 当点击时传过去的{@link PendingIntent}
     */
    public void setOnClickPendingIntent(int viewId, PendingIntent pendingIntent) {
        addAction(new SetOnClickPendingIntent(viewId, pendingIntent));
    }

    /**
     * 设置视图的点击事件处理器，点击事件处理器由用户实现。
     * 
     * @param viewId 需要设置点击事件处理器的视图ID
     * @param handler 点击事件处理器，为null视为清空之前设置的处理器。
     */
    public void setOnClickHandler(int viewId, OnClickHandler handler) {
        addAction(new SetOnClickHandler(viewId, handler));
    }

    /**
     * 设置视图的长按事件处理器，长按事件处理器由用户实现。
     * 
     * @param viewId 需要设置长按事件处理器的视图ID
     * @param handler 长按事件处理器，为null视为清空之前设置的处理器。
     */
    public void setOnLongClickHandler(int viewId, OnLongClickHandler handler) {
        addAction(new SetOnLongClickHandler(viewId, handler));
    }

    /**
     * 当在控件（Widgets）中使用集合（比如说：AmazingListView, {@link android.widget.StackView}等等）时，在单独的一个条目中设置
     * {@link PendingIntent}是非常浪费的，并且是不被允许的。这个方法将在集合中设置一个单独的{@link PendingIntent}
     * 模板,然后每一个单独的条目都可以通过调用{@link #setOnClickFillInIntent(int, Intent)}方法来区别他们的点击事件。
     * 
     * @param viewId 当点击时，其子视图需要使用{@link PendingIntent}模板的集合id
     * @param pendingIntent 和viewId的一个子类绑定在一起的{@link PendingIntent}，在点击时会被执行
     */
    public void setPendingIntentTemplate(int viewId, PendingIntent pendingIntent) {
        addAction(new SetPendingIntentTemplate(viewId, pendingIntent));
    }

    /**
     * 当在控件（Widgets）中使用集合（比如说：AmazingListView, {@link android.widget.StackView}等等）时，在单独的一个条目中设置
     * {@link PendingIntent}是非常浪费的，并且是不被允许的。然而一个单独的{@link PendingIntent}模板可以设置在集合里，参见
     * {@link #setPendingIntentTemplate(int, PendingIntent)}
     * 方法，并且一个给定条目的单独的点击动作可以通过在条目上设置一个fillInIntent来区别。然后这个fillInIntent就和{@link PendingIntent}
     * 的实例结合在一起决定最终的被点击执行的{@link Intent}。规定如下：在{@link PendingIntent}
     * 实例里，左边为空白的任何区域由fillInIntent提供的，该空白区域将被重写，同时{@link PendingIntent}的结果将被使用。
     * 
     * 然后{@link PendingIntent}将被在fillInIntent中设置的有联系的字段填充。参见{@link Intent#fillIn(Intent, int)}方法。
     * 
     * @param viewId 要设置fillInIntent视图id
     * @param fillInIntent 要和父类的{@link PendingIntent}组合在一起的意图，用来决定被viewid指定的视图的点击动作
     */
    public void setOnClickFillInIntent(int viewId, Intent fillInIntent) {
        addAction(new SetOnClickFillInIntent(viewId, fillInIntent));
    }

    /**
     * 相当于在指定的view调用了{@link Drawable#setAlpha(int)}方法、
     * {@link Drawable#setColorFilter(int, PorterDuff.Mode)}方法以及{@link Drawable#setLevel(int)}方法。
     * <p>
     * 当toBackground为<code>true</code>时，这些操作将在{@link View#getBackground()}方法的返回值上执行；否则，程序将假定目标为
     * {@link ImageView}，这些操作也将在{@link ImageView#getDrawable()}方法的返回值上执行。
     * <p>
     * 可以传入<code>null</code>或者-1来略过某个调用。
     * 
     * @param viewId 调用这些方法组合的视图id
     * @param toBackground 如果为{@code true}，这些操作将在{@link View#getBackground()}方法的返回值上执行；否则，程序假定目标为
     *        {@link ImageView}，并且这些操作将在{@link ImageView#getDrawable()}方法的返回值上执行。
     * @param alpha 图片新的透明度。执行{@link Drawable#setAlpha(int)}方法的参数，传入-1可以略过这个调用。
     * @param colorFilter 图片新的颜色过滤。执行{@link Drawable#setColorFilter(int, PorterDuff.Mode)}
     *        方法的参数，传入-1可以略过这个调用。
     * @param mode 图片新的过滤模式。执行{@link Drawable#setColorFilter(int, PorterDuff.Mode)}方法的参数，传入
     *        <code>null</code>可以略过这个调用。
     * @param level 图片的级数。执行{@link Drawable#setLevel(int)}方法的参数，传入-1可以略过这个调用。
     */
    public void setDrawableParameters(int viewId, boolean toBackground, int alpha, int colorFilter,
            PorterDuff.Mode mode, int level) {
        addAction(new SetDrawableParameters(viewId, toBackground, alpha, colorFilter, mode, level));
    }

    // API add in 5.0.1(21)
    void setProgressTintList(int viewId, ColorStateList tint) {
        // TODO:write an Action instead
        addAction(new ReflectionAction(viewId, "setProgressTintList",
                ReflectionAction.COLOR_STATE_LIST, tint));
    }

    // API add in 5.0.1(21)
    void setProgressBackgroundTintList(int viewId, ColorStateList tint) {
        // TODO:write an Action instead
        addAction(new ReflectionAction(viewId, "setProgressBackgroundTintList",
                ReflectionAction.COLOR_STATE_LIST, tint));
    }

    // API add in 5.0.1(21)
    void setProgressIndeterminateTintList(int viewId, ColorStateList tint) {
        // TODO:write an Action instead
        addAction(new ReflectionAction(viewId, "setIndeterminateTintList",
                ReflectionAction.COLOR_STATE_LIST, tint));
    }

    /**
     * 相当于调用{@link TextView#setTextColor(int)}方法
     * 
     * @param viewId 视图id
     * @param color 设置所有的状态(normal, selected, focused)下的text颜色为指定颜色
     */
    public void setTextColor(int viewId, int color) {
        addAction(new SetTextColor(viewId, SetTextColor.COLOR, color));
    }

    /**
     * 相当于调用{@link TextView#setTextColor(ColorStateList)}方法
     * 
     * @param viewId 视图id
     * @param colorList 设置text的颜色为colorList指定的颜色
     */
    public void setTextColor(int viewId, ColorStateList colorList) {
        addAction(new SetTextColor(viewId, SetTextColor.COLOR_STATE_LIST, colorList));
    }

    /**
     * 相当于调用{@link AbsListView#setRemoteViewsAdapter(IWidgetService, Intent)}方法。
     * 
     * @param viewId 视图id
     * @param service 控件服务的{@link IBinder}
     * @param intent 为{@link LocalRemoteViewsAdapter}提供数据的服务的intent
     */
    public void setRemoteAdapter(int viewId, IBinder service, Intent intent) {
        addAction(new SetRemoteViewsAdapterIntent(viewId, service, intent));
    }

    void setRemoteAdapter(int viewId, ArrayList<LocalRemoteViews> list, int typeCount) {
        addAction(new SetRemoteViewsAdapterList(viewId, list, typeCount));
    }

    /**
     * 相当于调用{@link AbsListView#smoothScrollToPosition(int)}方法。
     * 
     * @param viewId 视图id
     * @param position 滚动到适配器指定位置
     */
    public void setScrollPosition(int viewId, int position) {
        addAction(new SetScrollPosition(viewId, position, false));
    }

    /**
     * 相当于调用{@link AbsListView#smoothScrollByOffset(int)}方法。
     * 
     * @param viewId 视图id
     * @param position 滚动视图的偏移量
     */
    public void setRelativeScrollPosition(int viewId, int position) {
        addAction(new SetScrollPosition(viewId, position, true));
    }

    /**
     * 相当于调用{@link View#setPadding(int, int, int, int)}方法。
     * 
     * @param viewId 视图id
     * @param l 视图左边填充间距
     * @param t 视图上边填充间距
     * @param r 视图右边填充间距
     * @param b 视图下边填充间距
     */
    public void setViewPadding(int viewId, int l, int t, int r, int b) {
        addAction(new ViewPaddingAction(viewId, l, t, r, b));
    }

    /**
     * 为{@link LocalRemoteViews}在视图布局上调用一个带有<code>boolean</code>值的方法。
     * 
     * @param viewId 视图id
     * @param methodName 要调用的方法名
     * @param value 要传的值
     */
    public void setBoolean(int viewId, String methodName, boolean value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.BOOLEAN, value));
    }

    /**
     * 为{@link LocalRemoteViews}在视图而已上调用一个带有<code>byte</code>值方法。
     * 
     * @param viewId 视图id
     * @param methodName 要调用的方法名
     * @param value 要传的值
     */
    public void setByte(int viewId, String methodName, byte value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.BYTE, value));
    }

    /**
     * 为{@link LocalRemoteViews}在视图而已上调用一个带有<code>short</code>值方法。
     * 
     * @param viewId 视图id
     * @param methodName 要调用的方法名
     * @param value 要传的值
     */
    public void setShort(int viewId, String methodName, short value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.SHORT, value));
    }

    /**
     * 为{@link LocalRemoteViews}在视图而已上调用一个带有<code>int</code>值方法。
     * 
     * @param viewId 视图id
     * @param methodName 要调用的方法名
     * @param value 要传的值
     */
    public void setInt(int viewId, String methodName, int value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.INT, value));
    }

    /**
     * 为{@link LocalRemoteViews}在视图而已上调用一个带有<code>long</code>值方法。
     * 
     * @param viewId 视图id
     * @param methodName 要调用的方法名
     * @param value 要传的值
     */
    public void setLong(int viewId, String methodName, long value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.LONG, value));
    }

    /**
     * 为{@link LocalRemoteViews}在视图而已上调用一个带有<code>float</code>值方法。
     * 
     * @param viewId 视图id
     * @param methodName 要调用的方法名
     * @param value 要传的值
     */
    public void setFloat(int viewId, String methodName, float value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.FLOAT, value));
    }

    /**
     * 为{@link LocalRemoteViews}在视图而已上调用一个带有<code>double</code>值方法。
     * 
     * @param viewId 视图id
     * @param methodName 要调用的方法名
     * @param value 要传的值
     */
    public void setDouble(int viewId, String methodName, double value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.DOUBLE, value));
    }

    /**
     * 为{@link LocalRemoteViews}在视图而已上调用一个带有<code>char</code>值方法。
     * 
     * @param viewId 视图id
     * @param methodName 要调用的方法名
     * @param value 要传的值
     */
    public void setChar(int viewId, String methodName, char value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.CHAR, value));
    }

    /**
     * 为{@link LocalRemoteViews}在视图而已上调用一个带有{@link String}值方法。
     * 
     * @param viewId 视图id
     * @param methodName 要调用的方法名
     * @param value 要传的值
     */
    public void setString(int viewId, String methodName, String value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.STRING, value));
    }

    /**
     * 为{@link LocalRemoteViews}在视图而已上调用一个带有{@link CharSequence}值方法。
     * 
     * @param viewId 视图id
     * @param methodName 要调用的方法名
     * @param value 要传的值
     */
    public void setCharSequence(int viewId, String methodName, CharSequence value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.CHAR_SEQUENCE, value));
    }

    /**
     * 为{@link LocalRemoteViews}在视图而已上调用一个带有{@link Uri}值方法。
     * 
     * @param viewId 视图id
     * @param methodName 要调用的方法名
     * @param value 要传的值
     */
    public void setUri(int viewId, String methodName, Uri value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.URI, value));
    }

    /**
     * 为{@link LocalRemoteViews}在视图布局上调用一个带有{@link Bitmap}值的方法。
     * 
     * @more <p class="note">
     *       在整个传输过程中，位图将被序列化，因此可能会使用大量的内存，并且可能非常慢。
     *       </p>
     * 
     * @param viewId 视图id
     * @param methodName 要调用的方法名
     * @param value 要传的值
     */
    public void setBitmap(int viewId, String methodName, Bitmap value) {
        addAction(new BitmapReflectionAction(viewId, methodName, value));
    }

    /**
     * 为{@link LocalRemoteViews}在视图而已上调用一个带有{@link Bundle}值方法。
     * 
     * @param viewId 视图id
     * @param methodName 要调用的方法名
     * @param value 要传的值
     */
    public void setBundle(int viewId, String methodName, Bundle value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.BUNDLE, value));
    }

    /**
     * 为{@link LocalRemoteViews}在视图而已上调用一个带有{@link Intent}值方法。
     * 
     * @param viewId 视图id
     * @param methodName 要调用的方法名
     * @param value 要传的值
     */
    public void setIntent(int viewId, String methodName, Intent value) {
        addAction(new ReflectionAction(viewId, methodName, ReflectionAction.INTENT, value));
    }

    /**
     * 相当于调用{@link View#setContentDescription(CharSequence)}方法。
     * 
     * @param viewId 视图id
     * @param description 视图新的内容描述
     */
    public void setContentDescription(int viewId, CharSequence description) {
        addAction(new SetViewParameters(viewId, SetViewParameters.CONTENT_DESCRIPTION, description));
    }

    // API add in 5.1.1(22)
    void setAccessibilityTraversalBefore(int viewId, int nextId) {
        // TODO:write in SetViewParams action instead
        setInt(viewId, "setAccessibilityTraversalBefore", nextId);
    }

    // API add in 5.1.1(22)
    void setAccessibilityTraversalAfter(int viewId, int nextId) {
        // TODO:write in SetViewParams action instead
        setInt(viewId, "setAccessibilityTraversalAfter", nextId);
    }

    /**
     * 相当于调用{@link View#setLabelFor(int)}方法。
     * 
     * @param viewId 视图id
     * @param labelId 视图新的标题资源id
     */
    public void setLabelFor(int viewId, int labelId) {
        addAction(new SetViewParameters(viewId, SetViewParameters.LABEL_FOR, labelId));
    }

    public void setOnSeekBarChangedHandler(int viewId, OnSeekBarChangedHandler handler) {
        addAction(new SetOnSeekBarChangedHandler(viewId, handler));
    }

    /**
     * 创建并返回这个对象的复本。默认的实现返回一个浅拷贝：它重新创建这个类的实例并从原来的实例中复制字段值（包括类相关的）到这个新的实例。相比之下，深拷贝，还会递归复制嵌套类。
     * 
     * @return 对象的复本。
     */
    public LocalRemoteViews clone() {
        Parcel p = Parcel.obtain();
        writeToParcel(p, 0);
        p.setDataPosition(0);

        LocalRemoteViews lrv = new LocalRemoteViews(p);
        p.recycle();
        return lrv;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (mIsRoot) {
            mBitmapCache.writeToParcel(dest, flags);
        }

        dest.writeString(mPackage);
        dest.writeInt(mLayoutId);
        dest.writeInt(mIsWidgetCollectionChild ? 1 : 0);

        int count = mActions == null ? 0 : mActions.size();
        dest.writeInt(count);
        if (count > 0) {
            for (Action a : mActions) {
                a.writeToParcel(dest, 0);
            }
        }
    }

    /**
     * 构造器{@link Parcelable.Creator}，用来实例化{@link LocalRemoteViews}类
     */
    public static final Creator<LocalRemoteViews> CREATOR = new Creator<LocalRemoteViews>() {

        @Override
        public LocalRemoteViews createFromParcel(Parcel source) {
            return new LocalRemoteViews(source);
        }

        @Override
        public LocalRemoteViews[] newArray(int size) {
            return new LocalRemoteViews[size];
        }
    };
}