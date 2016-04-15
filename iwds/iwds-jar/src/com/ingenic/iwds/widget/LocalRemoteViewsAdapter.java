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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ingenic.iwds.appwidget.IWidgetService;
import com.ingenic.iwds.utils.IwdsLog;

public class LocalRemoteViewsAdapter extends BaseAdapter implements Handler.Callback {

    private static final int DEFAULT_LOADING_VIEW_HEIGHT = 50;

    private static final int UNBIND_SERVICE_MESSAGE_TYPE = 1;

    private static final int UNBIND_SERVICE_DELAY = 5000;

    private static final int DEFAULT_CACHE_SIZE = 40;

    private static final int REMOTE_VIEWS_CACHE_DURATION = 5000;

    private static final HashMap<LocalRemoteViewsCacheKey, FixedSizeLocalRemoteViewsCache> CACHED_REMOTEVIEWS_CACHES =
            new HashMap<LocalRemoteViewsCacheKey, FixedSizeLocalRemoteViewsCache>();
    private static final HashMap<LocalRemoteViewsCacheKey, Runnable> REMOTEVIEWS_CACHE_REMOVE_RUNNABLES =
            new HashMap<LocalRemoteViewsCacheKey, Runnable>();

    private static HandlerThread sCacheRemovalThread;
    private static Handler sCacheRemovalQueue;

    private IWidgetService mWidgetService;
    private Intent mIntent;
    private int mWidgetId;
    private LocalRemoteViewsFrameLayoutRefSet mRequestedViews;

    private HandlerThread mWorkerThread;
    private Handler mWorkerQueue;
    private Handler mMainQueue;

    private WeakReference<LocalRemoteAdapterConnectionCallback> mWeakCallback;
    private boolean mNotifyDataSetChangedAfterOnServiceConnected = false;
    private LocalRemoteViewsAdapterConnection mConnection;
    private FixedSizeLocalRemoteViewsCache mCache;

    private int mVisibleWindowLowerBound;
    private int mVisibleWindowUpperBound;

    private boolean mDataReady = false;

    public interface LocalRemoteAdapterConnectionCallback {

        void onLocalRemoteAdapterConnected();

        void onLocalRemoteAdapterDisconnected();

        void deferNotifyDataSetChanged();
    }

    private class LocalRemoteViewsFrameLayoutRefSet {
        private SparseArray<LinkedList<LocalRemoteViewsFrameLayout>> mReferences;
        private HashMap<LocalRemoteViewsFrameLayout, LinkedList<LocalRemoteViewsFrameLayout>> mView2LinkedList;

        public LocalRemoteViewsFrameLayoutRefSet() {
            mReferences = new SparseArray<LinkedList<LocalRemoteViewsFrameLayout>>();
            mView2LinkedList =
                    new HashMap<LocalRemoteViewsFrameLayout, LinkedList<LocalRemoteViewsFrameLayout>>();
        }

        public void add(int position, LocalRemoteViewsFrameLayout layout) {
            LinkedList<LocalRemoteViewsFrameLayout> refs = mReferences.get(position);

            if (refs == null) {
                refs = new LinkedList<LocalRemoteViewsFrameLayout>();
                mReferences.put(position, refs);
            }

            mView2LinkedList.put(layout, refs);
            refs.add(layout);
        }

        public void notifyOnRemoteViewsLoaded(int position, LocalRemoteViews view) {
            if (view == null) return;

            final LinkedList<LocalRemoteViewsFrameLayout> refs = mReferences.get(position);
            if (refs == null) return;

            for (LocalRemoteViewsFrameLayout layout : refs) {
                layout.onRemoteViewsLoaded(view);

                if (mView2LinkedList.containsKey(layout)) {
                    mView2LinkedList.remove(layout);
                }
            }

            refs.clear();
            mReferences.delete(position);
        }

        public void removeView(LocalRemoteViewsFrameLayout lrvfl) {
            if (mView2LinkedList.containsKey(lrvfl)) {
                mView2LinkedList.remove(lrvfl).remove(lrvfl);
            }
        }

        public void clear() {
            mReferences.clear();
            mView2LinkedList.clear();
        }
    }

    private static class LocalRemoteViewsFrameLayout extends FrameLayout {

        public LocalRemoteViewsFrameLayout(Context context) {
            super(context);
        }

        public void onRemoteViewsLoaded(LocalRemoteViews view) {
            try {
                removeAllViews();
                addView(view.apply(getContext(), this));
            } catch (Exception e) {
                IwdsLog.e(this, "Failed to apply LocalRemoteViews.");
            }
        }
    }

    private static class LocalRemoteViewsAdapterConnection extends
            ILocalRemoteViewsAdapterConnection.Stub {

        private WeakReference<LocalRemoteViewsAdapter> mWeakAdapter;
        private boolean mIsConnected;
        private boolean mIsConnecting;
        private ILocalRemoteViewsFactory mFactory;

        public LocalRemoteViewsAdapterConnection(LocalRemoteViewsAdapter adapter) {
            mWeakAdapter = new WeakReference<LocalRemoteViewsAdapter>(adapter);
        }

        public synchronized void bind(IWidgetService service, int widgetId, Intent intent) {
            if (!mIsConnecting) {
                try {
                    final LocalRemoteViewsAdapter adapter = mWeakAdapter.get();

                    if (adapter != null) {
                        service.bindRemoteViewsService(widgetId, intent, asBinder());
                    } else {
                        IwdsLog.w(this, "bind: adapter is null");
                    }

                    mIsConnecting = true;
                } catch (Exception e) {
                    IwdsLog.e(this, "bind(): " + e.getMessage());
                    mIsConnecting = false;
                    mIsConnected = false;
                }
            }
        }

        public synchronized void unbind(IWidgetService service, int widgetId, Intent inetnt) {
            try {
                final LocalRemoteViewsAdapter adapter = mWeakAdapter.get();

                if (adapter != null) {
                    service.unbindRemoteViewsService(widgetId, inetnt);
                } else {
                    IwdsLog.w(this, "unbind: adapter is null");
                }

                mIsConnecting = false;
            } catch (Exception e) {
                IwdsLog.e(this, "unbind(): " + e.getMessage());
                mIsConnecting = false;
                mIsConnected = false;
            }
        }

        @Override
        public synchronized void onServiceConnected(IBinder service) {
            mFactory = ILocalRemoteViewsFactory.Stub.asInterface(service);

            final LocalRemoteViewsAdapter adapter = mWeakAdapter.get();
            if (adapter == null) return;

            adapter.mWorkerQueue.post(new Runnable() {

                @Override
                public void run() {
                    if (adapter.mNotifyDataSetChangedAfterOnServiceConnected) {
                        adapter.onNotifyDataSetChanged();
                    } else {
                        ILocalRemoteViewsFactory factory = adapter.mConnection.getFactory();

                        try {
                            if (!factory.isCreated()) {
                                factory.onDataSetChanged();
                            }
                        } catch (RemoteException e) {
                            IwdsLog.e(this, "Error notifying factory of data set changed in "
                                    + "onServiceConnected(): " + e.getMessage());
                            return;
                        } catch (RuntimeException e) {
                            IwdsLog.e(this, "Error notifying factory of data set changed in "
                                    + "onServiceConnected(): " + e.getMessage());
                        }

                        adapter.updateTemporaryMetaData();
                        adapter.mMainQueue.post(new Runnable() {

                            @Override
                            public void run() {
                                synchronized (adapter.mCache) {
                                    final LocalRemoteAdapterConnectionCallback callback =
                                            adapter.mWeakCallback.get();

                                    if (callback != null) {
                                        callback.onLocalRemoteAdapterConnected();
                                    }
                                }
                            }
                        });

                        adapter.enqueueDeferredUnbindServiceMessage();
                        mIsConnected = true;
                        mIsConnecting = false;
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected() {
            mIsConnected = false;
            mIsConnecting = false;
            mFactory = null;

            final LocalRemoteViewsAdapter adapter = mWeakAdapter.get();
            if (adapter == null) return;

            adapter.mMainQueue.post(new Runnable() {

                @Override
                public void run() {
                    adapter.mMainQueue.removeMessages(UNBIND_SERVICE_MESSAGE_TYPE);

                    final LocalRemoteAdapterConnectionCallback callback =
                            adapter.mWeakCallback.get();
                    if (callback != null) {
                        callback.onLocalRemoteAdapterDisconnected();
                    }
                }
            });
        }

        public synchronized ILocalRemoteViewsFactory getFactory() {
            return mFactory;
        }

        public synchronized boolean isConnected() {
            return mIsConnected;
        }
    }

    private static class FixedSizeLocalRemoteViewsCache {

        private static final float MAX_COUNT_SLACK_PERCENT = 0.75f;

        private static final int MAX_MEMORY_LIMIT_IN_BTYES = 2 * 1024 * 1024;

        private int mMaxCount;
        private int mMaxCountSlack;
        private int mPreloadLowerBound;
        private int mPreloadUpperBound;
        private int mLastRequestedIndex;

        private final LocalRemoteViewsMetaData mMetaData;
        private final LocalRemoteViewsMetaData mTemporaryMetaData;

        private SparseArray<LocalRemoteViewsIndexMetaData> mIndexMetaDatas;
        private SparseArray<LocalRemoteViews> mIndexViews;
        private HashSet<Integer> mRequestedIndices;
        private HashSet<Integer> mLoadIndices;

        public FixedSizeLocalRemoteViewsCache(int maxCacheSize) {
            mMaxCount = maxCacheSize;
            mMaxCountSlack = Math.round(MAX_COUNT_SLACK_PERCENT * (mMaxCount / 2));
            mPreloadLowerBound = 0;
            mPreloadUpperBound = -1;

            mMetaData = new LocalRemoteViewsMetaData();
            mTemporaryMetaData = new LocalRemoteViewsMetaData();

            mIndexMetaDatas = new SparseArray<LocalRemoteViewsIndexMetaData>();
            mIndexViews = new SparseArray<LocalRemoteViews>();
            mRequestedIndices = new HashSet<Integer>();

            mLastRequestedIndex = -1;
            mLoadIndices = new HashSet<Integer>();
        }

        public void insert(int position, LocalRemoteViews v, long itemId,
                ArrayList<Integer> visibleWindow) {
            if (mIndexViews.size() >= mMaxCount) {
                mIndexViews.delete(getFarthestPositionFrom(position, visibleWindow));
            }

            int pruneFromPosition = (mLastRequestedIndex > -1) ? mLastRequestedIndex : position;
            while (getRemoteViewsBitmapMemoryUsage() >= MAX_MEMORY_LIMIT_IN_BTYES) {
                int trimIndex = getFarthestPositionFrom(pruneFromPosition, visibleWindow);
                if (trimIndex < 0) break;

                mIndexViews.delete(trimIndex);
            }

            LocalRemoteViewsIndexMetaData data = mIndexMetaDatas.get(position);
            if (data == null) {
                mIndexMetaDatas.put(position, new LocalRemoteViewsIndexMetaData(v, itemId));
            } else {
                data.set(v, itemId);
            }

            mIndexViews.put(position, v);
        }

        private int getFarthestPositionFrom(int pos, ArrayList<Integer> visibleWindow) {
            int maxDist = 0;
            int maxDistIndex = -1;
            int maxDistNotVisible = 0;
            int maxDistIndexNotVisible = -1;

            final int N = mIndexViews.size();
            for (int i = 0; i < N; i++) {
                int key = mIndexViews.keyAt(i);
                int dist = Math.abs(key - pos);

                if (dist > maxDistNotVisible && !visibleWindow.contains(key)) {
                    maxDistIndexNotVisible = key;
                    maxDistNotVisible = dist;
                }

                if (dist >= maxDist) {
                    maxDistIndex = key;
                    maxDist = dist;
                }
            }

            if (maxDistIndexNotVisible > -1) {
                return maxDistIndexNotVisible;
            }

            return maxDistIndex;
        }

        private int getRemoteViewsBitmapMemoryUsage() {
            int mem = 0;

            final int N = mIndexViews.size();
            for (int i = 0; i < N; i++) {
                LocalRemoteViews v = mIndexViews.valueAt(i);

                if (v != null) {
                    mem += v.estimateMemoryUsage();
                }
            }

            return mem;
        }

        public LocalRemoteViewsMetaData getMetaData() {
            return mMetaData;
        }

        public LocalRemoteViewsMetaData getTemporaryMetaData() {
            return mTemporaryMetaData;
        }

        public LocalRemoteViews getRemoteViewsAt(int position) {
            return mIndexViews.get(position);
        }

        public LocalRemoteViewsIndexMetaData getIndexMetaDataAt(int position) {
            return mIndexMetaDatas.get(position);
        }

        public void commitTemporaryMetaData() {
            synchronized (mTemporaryMetaData) {
                synchronized (mMetaData) {
                    mMetaData.set(mTemporaryMetaData);
                }
            }
        }

        public void queueRequestedPositionToLoad(int position) {
            mLastRequestedIndex = position;
            synchronized (mLoadIndices) {
                mRequestedIndices.add(position);
                mLoadIndices.add(position);
            }
        }

        public boolean queuePositionsToBePreloadedFromRequestedPosition(int position) {
            if (mPreloadLowerBound <= position && position <= mPreloadUpperBound) {
                int center = (mPreloadUpperBound + mPreloadLowerBound) / 2;
                if (Math.abs(position - center) < mMaxCountSlack) {
                    return false;
                }
            }

            int count = 0;
            synchronized (mMetaData) {
                count = mMetaData.count;
            }

            synchronized (mLoadIndices) {
                mLoadIndices.clear();
                mLoadIndices.addAll(mRequestedIndices);

                int halfMaxCount = mMaxCount / 2;
                mPreloadLowerBound = position - halfMaxCount;
                mPreloadUpperBound = position + halfMaxCount;

                int effectiveLowerBound = Math.max(0, mPreloadLowerBound);
                int effectiveUpperBound = Math.min(mPreloadUpperBound, count - 1);
                for (int i = effectiveLowerBound; i <= effectiveUpperBound; ++i) {
                    mLoadIndices.add(i);
                }

                ArrayList<Integer> keys = new ArrayList<Integer>();
                final int N = mIndexViews.size();
                for (int i = 0; i < N; i++) {
                    keys.add(mIndexViews.keyAt(i));
                }

                mLoadIndices.removeAll(keys);
            }

            return true;
        }

        public int[] getNextIndexToLoad() {
            synchronized (mLoadIndices) {
                if (!mRequestedIndices.isEmpty()) {
                    Integer i = mRequestedIndices.iterator().next();

                    mRequestedIndices.remove(i);
                    mLoadIndices.remove(i);
                    return new int[] { i.intValue(), 1 };
                }

                if (!mLoadIndices.isEmpty()) {
                    Integer i = mLoadIndices.iterator().next();

                    mLoadIndices.remove(i);
                    return new int[] { i.intValue(), 0 };
                }

                return new int[] { -1, 0 };
            }
        }

        public boolean containsRemoteViewAt(int position) {
            return mIndexViews.get(position) != null;
        }

        public boolean containsMetaDataAt(int position) {
            return mIndexMetaDatas.get(position) != null;
        }

        public void reset() {
            mPreloadLowerBound = 0;
            mPreloadUpperBound = -1;
            mLastRequestedIndex = -1;

            mIndexViews.clear();
            mIndexMetaDatas.clear();

            synchronized (mLoadIndices) {
                mRequestedIndices.clear();
                mLoadIndices.clear();
            }
        }
    }

    private static class LocalRemoteViewsMetaData {
        int count;
        int viewTypeCount;
        boolean hasStableIds;

        LocalRemoteViews loadingView;
        LocalRemoteViews firstView;
        int firstViewHeight;

        private final SparseIntArray mTypeIdIndexs = new SparseIntArray();

        public LocalRemoteViewsMetaData() {
            reset();
        }

        public void set(LocalRemoteViewsMetaData d) {
            if (d == null) {
                reset();
                return;
            }

            synchronized (d) {
                count = d.count;
                viewTypeCount = d.viewTypeCount;
                hasStableIds = d.hasStableIds;

                setLoadingViewTemplates(d.loadingView, d.firstView);
            }
        }

        private void setLoadingViewTemplates(LocalRemoteViews loading, LocalRemoteViews first) {
            loadingView = loading;

            if (first != null) {
                firstView = first;
                firstViewHeight = -1;
            }
        }

        public void reset() {
            count = 0;

            viewTypeCount = 1;
            hasStableIds = true;
            loadingView = null;
            firstView = null;
            firstViewHeight = 0;
            mTypeIdIndexs.clear();
        }

        public int getViewType(int typeId) {
            int incrementalTypeId = mTypeIdIndexs.get(typeId);

            if (incrementalTypeId == 0) {
                incrementalTypeId = mTypeIdIndexs.size() + 1;

                mTypeIdIndexs.put(typeId, incrementalTypeId);
            }

            return incrementalTypeId;
        }

        public boolean isViewTypeInRange(int typeId) {
            return getViewType(typeId) < viewTypeCount;
        }

        private LocalRemoteViewsFrameLayout createLoadingView(int position, View convertView,
                ViewGroup parent, Object lock) {
            final Context context = parent.getContext();

            LocalRemoteViewsFrameLayout layout = new LocalRemoteViewsFrameLayout(context);
            synchronized (lock) {
                boolean customLoadingViewAvailable = false;

                if (loadingView != null) {
                    try {
                        View loading = loadingView.apply(context, parent);

                        loading.setTag(LocalRemoteViews.TAG_KEY_ROWTYPEDID, 0);
                        layout.addView(loading);
                        customLoadingViewAvailable = true;
                    } catch (Exception e) {
                        IwdsLog.e(this, "Error inflating custom loading view, using default"
                                + " loading view instead");
                        e.printStackTrace();
                    }
                }

                if (!customLoadingViewAvailable) {
                    if (firstViewHeight < 0) {
                        try {
                            View first = firstView.apply(context, parent);

                            first.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                            firstViewHeight = first.getMeasuredHeight();
                            firstView = null;
                        } catch (Exception e) {
                            float density = context.getResources().getDisplayMetrics().density;

                            firstViewHeight = Math.round(DEFAULT_LOADING_VIEW_HEIGHT * density);
                            firstView = null;
                            IwdsLog.e(this, "Error inflating first LocalRemoteViews" + e);
                        }
                    }

                    TextView loadingTv = new TextView(context);
                    // TODO setup loading TextView
                    loadingTv.setHeight(firstViewHeight);
                    loadingTv.setTag(0);

                    layout.addView(loadingTv);
                }
            }

            return layout;
        }
    }

    private static class LocalRemoteViewsIndexMetaData {

        int typeId;
        long itemId;

        public LocalRemoteViewsIndexMetaData(LocalRemoteViews v, long itemId) {
            set(v, itemId);
        }

        public void set(LocalRemoteViews v, long id) {
            itemId = id;
            typeId = v != null ? v.getLayoutId() : 0;
        }
    }

    private static class LocalRemoteViewsCacheKey {
        final Intent.FilterComparison filter;
        final int widgetId;

        public LocalRemoteViewsCacheKey(Intent.FilterComparison filter, int widgetId) {
            this.filter = filter;
            this.widgetId = widgetId;
        }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof LocalRemoteViewsCacheKey)) {
                return false;
            }

            LocalRemoteViewsCacheKey other = (LocalRemoteViewsCacheKey) o;
            return other.filter.equals(filter) && other.widgetId == widgetId;
        }

        @Override
        public int hashCode() {
            return (filter != null ? filter.hashCode() : 0) ^ (widgetId << 2);
        }
    }

    public LocalRemoteViewsAdapter(IWidgetService service, Intent intent,
            LocalRemoteAdapterConnectionCallback callback) {
        mWidgetService = service;
        mIntent = intent;

        if (intent == null) {
            throw new IllegalArgumentException("on-null Intent must be specified.");
        }

        int widgetId =
                mWidgetId = intent.getIntExtra(LocalRemoteViews.EXTRA_REMOTEADAPTER_WIDGET_ID, -1);
        mRequestedViews = new LocalRemoteViewsFrameLayoutRefSet();

        if (intent.hasExtra(LocalRemoteViews.EXTRA_REMOTEADAPTER_WIDGET_ID)) {
            intent.removeExtra(LocalRemoteViews.EXTRA_REMOTEADAPTER_WIDGET_ID);
        }

        mWorkerThread = new HandlerThread("LocalRemoteViewsCache-loader");
        mWorkerThread.start();
        mWorkerQueue = new Handler(mWorkerThread.getLooper());
        mMainQueue = new Handler(Looper.myLooper(), this);

        if (sCacheRemovalThread == null) {
            sCacheRemovalThread = new HandlerThread("LocalRemoteViewsAdapter-cachePruner");
            sCacheRemovalThread.start();
            sCacheRemovalQueue = new Handler(sCacheRemovalThread.getLooper());
        }

        mWeakCallback = new WeakReference<LocalRemoteAdapterConnectionCallback>(callback);
        mConnection = new LocalRemoteViewsAdapterConnection(this);

        LocalRemoteViewsCacheKey key =
                new LocalRemoteViewsCacheKey(new Intent.FilterComparison(intent), widgetId);
        synchronized (CACHED_REMOTEVIEWS_CACHES) {
            boolean ready = mDataReady;

            if (CACHED_REMOTEVIEWS_CACHES.containsKey(key)) {
                FixedSizeLocalRemoteViewsCache cache = mCache = CACHED_REMOTEVIEWS_CACHES.get(key);

                LocalRemoteViewsMetaData data = cache.mMetaData;
                synchronized (data) {
                    if (data.count > 0) {
                        ready = mDataReady = true;
                    }
                }
            } else {
                mCache = new FixedSizeLocalRemoteViewsCache(DEFAULT_CACHE_SIZE);
            }

            if (!ready) {
                requestBindService();
            }
        }
    }

    private boolean requestBindService() {
        if (!mConnection.isConnected()) {
            mConnection.bind(mWidgetService, mWidgetId, mIntent);
        }

        mMainQueue.removeMessages(UNBIND_SERVICE_MESSAGE_TYPE);
        return mConnection.isConnected();
    }

    protected void finalize() throws Throwable {
        try {
            if (mWorkerThread != null) {
                mWorkerThread.quit();
            }
        } finally {
            super.finalize();
        }
    }

    public boolean isDataReady() {
        return mDataReady;
    }

    public void saveRemoteViewsCache() {
        final LocalRemoteViewsCacheKey key =
                new LocalRemoteViewsCacheKey(new Intent.FilterComparison(mIntent), mWidgetId);

        synchronized (CACHED_REMOTEVIEWS_CACHES) {
            if (REMOTEVIEWS_CACHE_REMOVE_RUNNABLES.containsKey(key)) {
                Runnable r = REMOTEVIEWS_CACHE_REMOVE_RUNNABLES.remove(key);
                sCacheRemovalQueue.removeCallbacks(r);
            }

            int count = 0;
            int numCached = 0;
            FixedSizeLocalRemoteViewsCache cache = mCache;
            LocalRemoteViewsMetaData data = cache.mMetaData;
            synchronized (data) {
                count = data.count;
            }

            synchronized (cache) {
                numCached = cache.mIndexViews.size();
            }

            if (count > 0 && numCached > 0) {
                CACHED_REMOTEVIEWS_CACHES.put(key, cache);
            }

            Runnable r = new Runnable() {

                @Override
                public void run() {
                    synchronized (CACHED_REMOTEVIEWS_CACHES) {
                        if (CACHED_REMOTEVIEWS_CACHES.containsKey(key)) {
                            CACHED_REMOTEVIEWS_CACHES.remove(key);
                        }

                        if (REMOTEVIEWS_CACHE_REMOVE_RUNNABLES.containsKey(key)) {
                            REMOTEVIEWS_CACHE_REMOVE_RUNNABLES.remove(key);
                        }
                    }
                }
            };

            REMOTEVIEWS_CACHE_REMOVE_RUNNABLES.put(key, r);
            sCacheRemovalQueue.postDelayed(r, REMOTE_VIEWS_CACHE_DURATION);
        }
    }

    public Intent getServiceIntent() {
        return mIntent;
    }

    @Override
    public int getCount() {
        final LocalRemoteViewsMetaData data = mCache.getMetaData();
        synchronized (data) {
            return data.count;
        }
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        FixedSizeLocalRemoteViewsCache cache = mCache;

        synchronized (cache) {
            if (cache.containsMetaDataAt(position)) {
                return cache.getIndexMetaDataAt(position).itemId;
            }
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        int typeId = 0;
        FixedSizeLocalRemoteViewsCache cache = mCache;

        synchronized (cache) {
            if (cache.containsMetaDataAt(position)) {
                typeId = cache.getIndexMetaDataAt(position).typeId;
            } else {
                return 0;
            }
        }

        final LocalRemoteViewsMetaData data = cache.getMetaData();
        synchronized (data) {
            return data.getViewType(typeId);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FixedSizeLocalRemoteViewsCache cache = mCache;

        synchronized (cache) {
            boolean inCache = cache.containsRemoteViewAt(position);
            boolean isConnected = mConnection.isConnected();
            boolean hasNewItems = false;

            if (convertView != null && convertView instanceof LocalRemoteViewsFrameLayout) {
                mRequestedViews.removeView((LocalRemoteViewsFrameLayout) convertView);
            }

            if (inCache && isConnected) {
                requestBindService();
            } else {
                hasNewItems = cache.queuePositionsToBePreloadedFromRequestedPosition(position);
            }

            if (inCache) {
                View convertChild = null;
                int convertTypeId = 0;
                LocalRemoteViewsFrameLayout layout = null;

                if (convertView != null && convertView instanceof LocalRemoteViewsFrameLayout) {
                    layout = (LocalRemoteViewsFrameLayout) convertView;

                    convertChild = layout.getChildAt(0);
                    convertTypeId = getConvertViewTypeId(convertChild);
                }

                Context context = parent.getContext();
                LocalRemoteViews lrv = cache.getRemoteViewsAt(position);
                LocalRemoteViewsIndexMetaData indexData = cache.getIndexMetaDataAt(position);
                int typeId = indexData.typeId;

                try {
                    if (layout != null) {
                        if (convertTypeId == typeId) {
                            lrv.reapply(context, convertChild);

                            return layout;
                        }
                        layout.removeAllViews();
                    } else {
                        layout = new LocalRemoteViewsFrameLayout(context);
                    }

                    View newView = lrv.apply(context, parent);
                    newView.setTag(LocalRemoteViews.TAG_KEY_ROWTYPEDID, typeId);
                    layout.addView(convertChild);
                    return layout;
                } catch (Exception e) {
                    IwdsLog.w(this, "Error inflating RemoteViews at position: " + position
                            + ", using loading view instead" + e);

                    LocalRemoteViewsFrameLayout loading = null;
                    final LocalRemoteViewsMetaData data = cache.getMetaData();
                    synchronized (data) {
                        loading = data.createLoadingView(position, convertView, parent, cache);
                    }

                    return loading;
                } finally {
                    if (hasNewItems) {
                        loadNextIndexInBackground();
                    }
                }
            } else {
                LocalRemoteViewsFrameLayout loading = null;

                final LocalRemoteViewsMetaData data = cache.getMetaData();
                synchronized (data) {
                    loading = data.createLoadingView(position, convertView, parent, cache);
                }

                mRequestedViews.add(position, loading);
                cache.queueRequestedPositionToLoad(position);
                loadNextIndexInBackground();

                return loading;
            }
        }
    }

    private int getConvertViewTypeId(View convertView) {
        int typeId = -1;

        if (convertView != null) {
            Object tag = convertView.getTag(LocalRemoteViews.TAG_KEY_ROWTYPEDID);

            if (tag != null) {
                typeId = (Integer) tag;
            }
        }
        return typeId;
    }

    private void loadNextIndexInBackground() {
        mWorkerQueue.post(new Runnable() {

            @Override
            public void run() {
                if (mConnection.isConnected()) {
                    int position = -1;

                    FixedSizeLocalRemoteViewsCache cache = mCache;
                    synchronized (cache) {
                        int[] res = cache.getNextIndexToLoad();
                        position = res[0];
                    }

                    if (position > -1) {
                        updateRemoteViews(position, true);

                        loadNextIndexInBackground();
                    } else {
                        enqueueDeferredUnbindServiceMessage();
                    }
                }
            }
        });
    }

    @Override
    public int getViewTypeCount() {
        final LocalRemoteViewsMetaData data = mCache.getMetaData();

        synchronized (data) {
            return data.viewTypeCount;
        }
    }

    @Override
    public boolean hasStableIds() {
        final LocalRemoteViewsMetaData data = mCache.getMetaData();

        synchronized (data) {
            return data.hasStableIds;
        }
    }

    @Override
    public boolean isEmpty() {
        return getCount() <= 0;
    }

    @Override
    public void notifyDataSetChanged() {
        mMainQueue.removeMessages(UNBIND_SERVICE_MESSAGE_TYPE);

        if (!mConnection.isConnected()) {
            mNotifyDataSetChangedAfterOnServiceConnected = true;

            requestBindService();
            return;
        }

        mWorkerQueue.post(new Runnable() {

            @Override
            public void run() {
                onNotifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean result = false;

        switch (msg.what) {
        case UNBIND_SERVICE_MESSAGE_TYPE:
            if (mConnection.isConnected()) {
                mConnection.unbind(mWidgetService, mWidgetId, mIntent);
            }

            result = true;
            break;
        }
        return result;
    }

    public void setVisibleRangeHint(int lower, int upper) {
        mVisibleWindowLowerBound = lower;
        mVisibleWindowUpperBound = upper;
    }

    private void onNotifyDataSetChanged() {
        ILocalRemoteViewsFactory factory = mConnection.getFactory();

        try {
            factory.onDataSetChanged();
        } catch (RemoteException e) {
            IwdsLog.e(this, "Error in updateNotifyDataSetChanged(): " + e.getMessage());
            return;
        } catch (RuntimeException e) {
            IwdsLog.e(this, "Error in updateNotifyDataSetChanged(): " + e.getMessage());
            return;
        }

        synchronized (mCache) {
            mCache.reset();
        }
        updateTemporaryMetaData();

        int newCount;
        ArrayList<Integer> visibleWindow;
        LocalRemoteViewsMetaData tmpData = mCache.getTemporaryMetaData();
        synchronized (tmpData) {
            newCount = tmpData.count;
            visibleWindow =
                    getVisibleWindow(mVisibleWindowLowerBound, mVisibleWindowUpperBound, newCount);
        }

        for (int i : visibleWindow) {
            if (i < newCount) {
                updateRemoteViews(i, false);
            }
        }

        mMainQueue.post(new Runnable() {

            @Override
            public void run() {
                synchronized (mCache) {
                    mCache.commitTemporaryMetaData();
                }

                superNotifyDataSetChanged();
                enqueueDeferredUnbindServiceMessage();
            }
        });

        mNotifyDataSetChangedAfterOnServiceConnected = false;
    }

    private void updateTemporaryMetaData() {
        ILocalRemoteViewsFactory factory = mConnection.getFactory();

        try {
            boolean hasStableIds = factory.hasStableIds();

            int viewTypeCount = factory.getViewTypeCount();
            int count = factory.getCount();

            LocalRemoteViews loadingView = factory.getLoadingView();
            LocalRemoteViews firstView = null;
            if ((count > 0) && (loadingView == null)) {
                firstView = factory.getViewAt(0);
            }

            final LocalRemoteViewsMetaData tmpData = mCache.getTemporaryMetaData();
            synchronized (tmpData) {
                tmpData.hasStableIds = hasStableIds;

                tmpData.viewTypeCount = viewTypeCount + 1;
                tmpData.count = count;
                tmpData.setLoadingViewTemplates(loadingView, firstView);
            }
        } catch (RemoteException e) {
            processException("updateMetaData", e);
        } catch (RuntimeException e) {
            processException("updateMetaData", e);
        }
    }

    private void processException(String method, Exception e) {
        IwdsLog.e(this, "Error in " + method + ": " + e.getMessage());

        final LocalRemoteViewsMetaData data = mCache.getMetaData();
        synchronized (data) {
            data.reset();
        }

        synchronized (mCache) {
            mCache.reset();
        }

        mMainQueue.post(new Runnable() {

            @Override
            public void run() {
                superNotifyDataSetChanged();
            }
        });
    }

    private void superNotifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    private ArrayList<Integer> getVisibleWindow(int lower, int upper, int count) {
        ArrayList<Integer> window = new ArrayList<Integer>();

        if ((lower == 0 && upper == 0) || lower < 0 || upper < 0) {
            return window;
        }

        if (lower <= upper) {
            for (int i = lower; i <= upper; i++) {
                window.add(i);
            }
        } else {
            for (int i = lower; i < count; i++) {
                window.add(i);
            }

            for (int i = 0; i <= upper; i++) {
                window.add(i);
            }
        }

        return window;
    }

    private void updateRemoteViews(final int position, boolean notifyWhenLoaded) {
        ILocalRemoteViewsFactory factory = mConnection.getFactory();

        LocalRemoteViews views = null;
        long itemId = 0;
        try {
            views = factory.getViewAt(position);
            itemId = factory.getItemId(position);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Error in updateRemoteViews(" + position + "): " + e.getMessage());
            return;
        } catch (RuntimeException e) {
            IwdsLog.e(this, "Error in updateRemoteViews(" + position + "): " + e.getMessage());
            return;
        }

        if (views == null) {
            IwdsLog.e(this, "Error in updateRemoteViews(" + position + "): " + " null RemoteViews "
                    + "returned from RemoteViewsFactory.");
            return;
        }

        int layoutId = views.getLayoutId();
        FixedSizeLocalRemoteViewsCache cache = mCache;
        LocalRemoteViewsMetaData data = cache.getMetaData();
        boolean viewTypeInRange;
        int cacheCount;
        synchronized (data) {
            viewTypeInRange = data.isViewTypeInRange(layoutId);
            cacheCount = data.count;
        }

        synchronized (cache) {
            if (viewTypeInRange) {
                ArrayList<Integer> visibleWindow =
                        getVisibleWindow(mVisibleWindowLowerBound, mVisibleWindowUpperBound,
                                cacheCount);

                cache.insert(position, views, itemId, visibleWindow);
                final LocalRemoteViews lrv = views;
                if (notifyWhenLoaded) {
                    mMainQueue.post(new Runnable() {

                        @Override
                        public void run() {
                            mRequestedViews.notifyOnRemoteViewsLoaded(position, lrv);
                        }
                    });
                }
            } else {
                IwdsLog.e(this, "Error: widget's LocalRemoteViewsFactory returns more view types"
                        + " than indicated by getViewTypeCount() ");
            }
        }
    }

    private void enqueueDeferredUnbindServiceMessage() {
        mMainQueue.removeMessages(UNBIND_SERVICE_MESSAGE_TYPE);
        mMainQueue.sendEmptyMessageDelayed(UNBIND_SERVICE_MESSAGE_TYPE, UNBIND_SERVICE_DELAY);
    }
}