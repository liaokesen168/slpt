/*
 * Copyright (C) 2015 Ingenic Semiconductor
 * 
 * LiJingWen(Kevin) <kevin.jwli@ingenic.com>
 * 
 * Elf/IDWS Project
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.FilterComparison;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Pair;

import com.ingenic.iwds.utils.IwdsLog;
import com.ingenic.iwds.widget.ILocalRemoteViewsAdapterConnection;
import com.ingenic.iwds.widget.LocalRemoteViews;

public class WidgetServiceImpl extends IWidgetService.Stub {

    private static final String TAG_WIDGET_PROVIDER = "widget-provider";

    private static final String ATTR_WIDTH = "width";
    private static final String ATTR_HEIGHT = "height";
    private static final String ATTR_UPDATE_MILLS = "update";
    private static final String ATTR_INIT_LAYOUT = "layout";
    private static final String ATTR_CONFIGURE = "configure";
    private static final String ATTR_PREVIEW = "preview";
    private static final String ATTR_ADVANCE = "advance";

    private final ArrayList<Widget> mWidgets = new ArrayList<Widget>();
    private final ArrayList<Host> mHosts = new ArrayList<Host>();
    private final ArrayList<Provider> mProviders = new ArrayList<Provider>();
    private final HashMap<ComponentName, ServiceConnection> mProviderServices =
            new HashMap<ComponentName, ServiceConnection>();
    private final HashMap<Pair<Integer, FilterComparison>, ServiceConnectionProxy> mBoundRemoteViewsServices =
            new HashMap<Pair<Integer, FilterComparison>, ServiceConnectionProxy>();
    private final HashMap<Pair<ComponentName, FilterComparison>, HashSet<Integer>> mRemoteViewsServicesWidgets =
            new HashMap<Pair<ComponentName, FilterComparison>, HashSet<Integer>>();

    private final Context mContext;
    private final PackageManager mPM;
    private final WidgetDao mDao;
    private final Object mLock = new Object();

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onPackageBroadcastReceived(intent);
        }
    };

    private int mNextWidgetId = WidgetManager.INVALID_WIDGET_ID + 1;

    WidgetServiceImpl(Context context) {
        mContext = context;
        mPM = context.getPackageManager();
        mDao = new WidgetDao(context);
    }

    void onServiceCreate() {
        synchronized (mLock) {
            loadWidgetsLocked();
            loadStateLocked();
        }
        registerReceiver();
    }

    void onServiceDestroy() {
        unregisterReceiver();

        synchronized (mLock) {
            mHosts.clear();

            // TODO:unbind all provider services
            for (Provider p : mProviders) {
                ServiceConnection conn = mProviderServices.get(p.info.provider);

                p.status = Provider.STATUS_IDLE;
                if (conn != null) {
                    mContext.unbindService(conn);
                }
            }

            mProviderServices.clear();
            mProviders.clear();
            clearWidgetsLocked();
        }
    }

    private void registerReceiver() {
        IntentFilter pkgFilter = new IntentFilter();

        pkgFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        pkgFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        pkgFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);

        pkgFilter.addDataScheme("package");
        mContext.registerReceiver(mReceiver, pkgFilter);

        IntentFilter sdFilter = new IntentFilter();
        sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        mContext.registerReceiver(mReceiver, sdFilter);
    }

    private void unregisterReceiver() {
        mContext.unregisterReceiver(mReceiver);
    }

    @Override
    public List<WidgetProviderInfo> getInstalledProviders() {
        ArrayList<WidgetProviderInfo> result = new ArrayList<WidgetProviderInfo>();

        synchronized (mLock) {
            for (Provider p : mProviders) {
                WidgetProviderInfo info = p.info;

                if (info != null) {
                    result.add(cloneIfLocalBinder(info));
                }
            }
        }

        return result;
    }

    @Override
    public void getWidgetSize(int wid, int[] size) {
        synchronized (mLock) {
            Widget w = lookupWidgetLocked(wid);
            if (w != null) {
                size[0] = w.width;
                size[1] = w.height;
            }
        }
    }

    @Override
    public void updateWidgetSize(int wid, int width, int height) {
        synchronized (mLock) {
            Widget w = lookupWidgetLocked(wid);
            if (w != null) {
                w.width = width;
                w.height = height;
            }
        }
    }

    @Override
    public LocalRemoteViews getWidgetViews(int wid) {
        synchronized (mLock) {
            Widget w = lookupWidgetLocked(wid);
            if (w != null) {
                return cloneIfLocalBinder(w.views);
            }
        }
        return null;
    }

    @Override
    public int[] startListening(IWidgetHost hcb, String hpkg, List<LocalRemoteViews> updatedViews) {
        synchronized (mLock) {
            Host h = lookupOrAddHostLocked(hpkg);

            h.cb = hcb;
            updatedViews.clear();

            ArrayList<Widget> instances = h.widgets;
            int N = instances.size();
            int[] updatedIds = new int[N];

            for (int i = 0; i < N; i++) {
                Widget w = instances.get(i);
                updatedIds[i] = w.id;
                updatedViews.add(cloneIfLocalBinder(w.views));
            }

            return updatedIds;
        }
    }

    @Override
    public void stopListening(String hpkg) {
        synchronized (mLock) {
            Host h = lookupHostLocked(hpkg);

            if (h != null) {
                h.cb = null;
                pruneHostLocked(h);
            }
        }
    }

    private int allocateWidgetIdLocked() {
        return mNextWidgetId;
    }

    @Override
    public int bindWidget(String hpkg, ComponentName provider) throws RemoteException {
        int id = 0;
        Provider p = null;
        Host h = null;

        synchronized (mLock) {
            Widget w = findWidgetLocked(hpkg, provider);

            if (w != null) {
                p = w.provider;
                h = w.host;
                id = w.id;
            } else {
                w = new Widget();

                p = w.provider = lookupProviderLocked(provider);
                h = w.host = lookupHostLocked(hpkg);
                id = w.id = allocateWidgetIdLocked();

                if (p == null || h == null || id == 0) return 0;

                p.widgets.add(w);
                h.widgets.add(w);
                addWidgetLocked(w);
                mDao.insert(w.toWidgetInfo());
            }

            switch (p.status) {
            case Provider.STATUS_IDLE:
                bindProviderServiceLocked(p);
                break;

            case Provider.STATUS_BOND:
                IWidgetProvider pcb = p.cb;

                if (pcb != null) {
                    pcb.onAdded(id, h.pkg);
                }
                break;
            }
        }

        return id;
    }

    private static void updateProvider(Provider p) {
        if (p == null || p.status != Provider.STATUS_BOND) return;

        IWidgetProvider pcb = p.cb;

        if (pcb != null) {
            ArrayList<Widget> widgets = p.widgets;
            if (widgets == null) return;

            final int N = widgets.size();
            int[] ids = new int[N];
            for (int i = 0; i < N; i++) {
                Widget w = widgets.get(i);
                ids[i] = w.id;
            }

            try {
                pcb.onUpdate(ids);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void bindProviderServiceLocked(Provider p) {
        if (p.status != Provider.STATUS_IDLE) {
            IwdsLog.w(this, "You should not bind a pending or bond service.");
            return;
        }

        Intent it = new Intent(WidgetManager.ACTION_BIND_WIDGET);
        ComponentName provider = p.info.provider;
        it.setComponent(provider);

        p.status = Provider.STATUS_PENDING;
        ServiceConnection conn = new ProviderServiceConnection(this, p);
        mContext.bindService(it, conn, Context.BIND_AUTO_CREATE);
        mProviderServices.put(provider, conn);
    }

    private Widget findWidgetLocked(String hpkg, ComponentName provider) {
        for (Widget w : mWidgets) {
            Provider p = w.provider;
            if (p == null) continue;

            WidgetProviderInfo info = p.info;
            if (info == null) continue;

            Host h = w.host;
            if (h == null) continue;

            if (h.pkg.equals(hpkg) && info.provider.equals(provider)) {
                return w;
            }
        }

        return null;
    }

    @Override
    public void deleteWidget(int wid) {
        synchronized (mLock) {
            Widget w = lookupWidgetLocked(wid);
            if (w == null) return;

            deleteWidgetLocked(w);
        }
    }

    @Override
    public void updateWidget(int wid, LocalRemoteViews views) {
        synchronized (mLock) {
            Widget w = lookupWidgetLocked(wid);

            if (w != null) {
                Provider p = w.provider;

                if (p != null && p.status == Provider.STATUS_BOND) {
                    LocalRemoteViews lrv = w.views;

                    if (lrv != null) {
                        lrv.mergeRemoteViews(views);
                    } else {
                        w.views = views;
                    }

                    scheduleNotifyUpdateWidgetLocked(w, views);
                }
            }
        }
    }

    @Override
    public void updateWidgets(int[] wids, LocalRemoteViews views) {
        for (int id : wids) {
            updateWidget(id, views);
        }
    }

    private void scheduleNotifyUpdateWidgetLocked(Widget w, LocalRemoteViews views) {
        if (w == null) return;

        Provider p = w.provider;
        if (p == null || p.status != Provider.STATUS_BOND) {
            IwdsLog.w(this, "Provider not bond in this widget.");
            return;
        }

        Host h = w.host;
        if (h == null) return;

        IWidgetHost hcb = h.cb;
        if (hcb == null) {
            IwdsLog.w(this, "callbacks is null");
            return;
        }

        try {
            hcb.updateWidget(w.id, views);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void bindRemoteViewsService(int wid, Intent it, IBinder cb) {
        synchronized (mLock) {
            Widget w = lookupWidgetLocked(wid);

            if (w == null) {
                throw new IllegalArgumentException("Bad widget id " + wid);
            }

            Provider p = w.provider;
            if (p == null) {
                throw new IllegalArgumentException("No provider for widget " + wid);
            }

            ComponentName provider = p.info.provider;
            if (provider == null) {
                throw new IllegalArgumentException("No provider for widget " + wid);
            }

            ComponentName comp = it.getComponent();
            String ppkg = provider.getPackageName();
            String spkg = comp.getPackageName();
            if (!spkg.equals(ppkg)) {
                throw new SecurityException("The taget service not in the same package"
                        + " as the widget provider");
            }

            ServiceConnectionProxy scp = null;
            FilterComparison fc = new FilterComparison(it);
            Pair<Integer, FilterComparison> key = Pair.create(wid, fc);
            if (mBoundRemoteViewsServices.containsKey(key)) {
                scp = mBoundRemoteViewsServices.remove(key);

                if (scp != null) {
                    scp.disconnect();
                    mContext.unbindService(scp);
                }
            }

            scp = new ServiceConnectionProxy(cb);
            mContext.bindService(it, scp, Context.BIND_AUTO_CREATE);
            mBoundRemoteViewsServices.put(key, scp);

            Pair<ComponentName, FilterComparison> skey = Pair.create(provider, fc);
            incrementWidgetServiceRefCount(skey, wid);
        }
    }

    private void incrementWidgetServiceRefCount(Pair<ComponentName, FilterComparison> key, int wid) {
        HashSet<Integer> wids = null;

        if (mRemoteViewsServicesWidgets.containsKey(key)) {
            wids = mRemoteViewsServicesWidgets.get(key);
        } else {
            wids = new HashSet<Integer>();
            mRemoteViewsServicesWidgets.put(key, wids);
        }

        wids.add(wid);
    }

    @Override
    public void unbindRemoteViewsService(int wid, Intent it) {
        synchronized (mLock) {
            FilterComparison fc = new FilterComparison(it);

            Pair<Integer, FilterComparison> key = Pair.create(wid, fc);
            if (mBoundRemoteViewsServices.containsKey(key)) {
                Widget w = lookupWidgetLocked(wid);

                if (w == null) {
                    throw new IllegalArgumentException("Bad widget id " + wid);
                }

                ServiceConnectionProxy proxy = mBoundRemoteViewsServices.remove(key);
                if (proxy != null) {
                    proxy.disconnect();
                    mContext.unbindService(proxy);
                }
            }
        }
    }

    private void loadWidgetsLocked() {
        Intent intent = new Intent(WidgetManager.ACTION_BIND_WIDGET);
        List<ResolveInfo> resolveInfos =
                mPM.queryIntentServices(intent, PackageManager.GET_META_DATA);

        if (resolveInfos != null) {
            for (ResolveInfo resolveInfo : resolveInfos) {
                addProviderLocked(resolveInfo.serviceInfo);
            }
        }
    }

    private boolean addProviderLocked(ServiceInfo si) {
        if (si == null || !si.isEnabled()) {
            return false;
        }

        ApplicationInfo appInfo = si.applicationInfo;
        if ((appInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
            return false;
        }

        ComponentName provider = new ComponentName(si.packageName, si.name);
        Provider p = parseProviderInfoXmlLocked(provider, si);

        if (p != null) {
            Provider existing = lookupProviderLocked(provider);

            if (existing != null) {
                existing.info = p.info;
            } else {
                mProviders.add(p);
            }

            return true;
        }

        return false;
    }

    private Provider parseProviderInfoXmlLocked(ComponentName provider, ServiceInfo si) {
        XmlResourceParser parser = si.loadXmlMetaData(mPM, WidgetManager.META_DATA_WIDGET_PROVIDER);

        if (parser == null) {
            IwdsLog.w(this, "No " + WidgetManager.META_DATA_WIDGET_PROVIDER
                    + " meta-data for Widget provider '" + provider + '\'');
            return null;
        }

        final String pkg = si.packageName;

        int type;
        Provider p = null;
        try {
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && type != XmlPullParser.START_TAG) {}

            String node = parser.getName();
            if (!TAG_WIDGET_PROVIDER.equals(node)) {
                IwdsLog.w(this, "Meta-data does not start width " + TAG_WIDGET_PROVIDER
                        + " tag for Widget provider " + provider);
                return null;
            }

            p = new Provider();
            WidgetProviderInfo info = p.info = new WidgetProviderInfo();
            info.provider = provider;
            info.providerInfo = si;

            final Resources res;

            try {
                res = mPM.getResourcesForApplication(pkg);
            } catch (NameNotFoundException e) {
                IwdsLog.w(this, "Can't get resources for application: " + pkg);
                e.printStackTrace();
                return null;
            }

            int resId = parser.getAttributeResourceValue(null, ATTR_WIDTH, 0);
            if (resId == 0) {
                String wString = parser.getAttributeValue(null, ATTR_WIDTH);
                info.width = text2px(wString, res);
            } else {
                info.width = res.getDimensionPixelOffset(resId);
            }

            resId = parser.getAttributeResourceValue(null, ATTR_HEIGHT, 0);
            if (resId == 0) {
                String hString = parser.getAttributeValue(null, ATTR_HEIGHT);
                info.height = text2px(hString, res);
            } else {
                info.height = res.getDimensionPixelOffset(resId);
            }

            info.updatePeriodMillis = parser.getAttributeIntValue(null, ATTR_UPDATE_MILLS, 0);
            info.initialLayout = parser.getAttributeResourceValue(null, ATTR_INIT_LAYOUT, 0);

            String className = parser.getAttributeValue(null, ATTR_CONFIGURE);
            if (className != null) {
                info.configure = new ComponentName(pkg, className);
            }

            info.label = si.loadLabel(mPM).toString();
            info.icon = si.getIconResource();
            info.previewImage = parser.getAttributeResourceValue(null, ATTR_PREVIEW, 0);
            info.autoAdvanceViewId = parser.getAttributeResourceValue(null, ATTR_ADVANCE, 0);
        } catch (Exception e) {
            IwdsLog.w(this, "XML parsing failed for Widget provider " + provider);
            e.printStackTrace();
            return null;
        } finally {
            if (parser != null) {
                parser.close();
            }
        }

        return p;
    }

    private static int text2px(String text, Resources res) {
        if (text == null) {
            return 0;
        }

        String s = "[0-9]*[px|sp|dp|dip]";
        if (text.matches(s)) {
            Pattern pUnit = Pattern.compile("[a-z]*");
            Matcher mUnit = pUnit.matcher(text);

            String unit = null;
            if (mUnit.find()) {
                unit = mUnit.group();
            } else {
                return 0;
            }

            Pattern pNum = Pattern.compile("[0-9]*");
            Matcher mNum = pNum.matcher(text);

            if (mNum.find()) {
                return text2px(mNum.group(), unit, res);
            }
        }

        return 0;
    }

    private static int text2px(String num, String unit, Resources res) {
        int px = Integer.parseInt(num);

        if (unit.equals("dp") || unit.equals("dip")) {
            float density = res.getDisplayMetrics().density;
            return Math.round(density * px);
        } else if (unit.equals("sp")) {
            float scaledDensity = res.getDisplayMetrics().scaledDensity;
            return Math.round(scaledDensity * px);
        } else {
            return px;
        }
    }

    private Widget lookupWidgetLocked(int wid) {
        for (Widget w : mWidgets) {
            if (w.id == wid) {
                return w;
            }
        }

        return null;
    }

    private Provider lookupProviderLocked(ComponentName provider) {
        for (Provider p : mProviders) {
            WidgetProviderInfo info = p.info;

            if (info != null && info.provider.equals(provider)) {
                return p;
            }
        }

        return null;
    }

    private Host lookupHostLocked(String hpkg) {
        for (Host h : mHosts) {
            if (h.pkg.endsWith(hpkg)) {
                return h;
            }
        }

        return null;
    }

    private void pruneHostLocked(Host h) {
        if (h.widgets.size() == 0 && h.cb == null) {
            mHosts.remove(h);
        }
    }

    private Host lookupOrAddHostLocked(String hpkg) {
        Host h = lookupHostLocked(hpkg);

        if (h != null) return h;

        h = new Host(hpkg);
        mHosts.add(h);
        return h;
    }

    private void deleteWidgetLocked(Widget w) {
        Host h = w.host;
        h.widgets.remove(w);
        pruneHostLocked(h);

        removeWidgetLocked(w);
        mDao.delete(w.toWidgetInfo());

        Provider p = w.provider;
        if (p != null) {
            ArrayList<Widget> widgets = p.widgets;
            widgets.remove(w);

            if (p.status != Provider.STATUS_IDLE) {
                onProviderDeleted(p, w.id);

                if (widgets.isEmpty()) {
                    unbindProviderServiceLocked(p);
                }
            }
        }
    }

    private void onProviderDeleted(Provider p, int wid) {
        IWidgetProvider pcb = p.cb;

        if (pcb != null) {
            try {
                pcb.onDeleted(wid);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void unbindProviderServiceLocked(Provider p) {
        disableProvider(p);

        ComponentName provider = p.info.provider;
        if (!mProviderServices.containsKey(provider)) return;

        ServiceConnection conn = mProviderServices.get(provider);
        if (conn != null) {
            mContext.unbindService(conn);
            mProviderServices.remove(provider);
        }
    }

    private static void disableProvider(Provider p) {
        if (p.status != Provider.STATUS_BOND) return;
        p.status = Provider.STATUS_IDLE;

        IWidgetProvider pcb = p.cb;
        if (pcb != null) {
            try {
                pcb.onDisabled();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadStateLocked() {
        ArrayList<WidgetInfo> infos = mDao.queryAll();
        if (infos == null) return;

        for (WidgetInfo info : infos) {
            Widget w = widgetFromInfoLocked(info);

            if (buildWidgetLocked(w)) {
                addWidgetLocked(w);
            } else {
                mDao.delete(info);
            }
        }
    }

    private boolean buildWidgetLocked(Widget w) {
        Provider p = w.provider;
        if (p == null) return false;

        p.widgets.add(w);

        Host h = w.host;
        if (h == null) return false;

        h.widgets.add(w);
        return true;
    }

    private void addWidgetLocked(Widget w) {
        int id = w.id;

        if (id >= mNextWidgetId) {
            mNextWidgetId = id + 1;
        }
        mWidgets.add(w);
    }

    private void removeWidgetLocked(Widget w) {
        mWidgets.remove(w);

        if (mWidgets.isEmpty()) {
            mNextWidgetId = WidgetManager.INVALID_WIDGET_ID + 1;
        }
    }

    private void clearWidgetsLocked() {
        mWidgets.clear();
        mNextWidgetId = WidgetManager.INVALID_WIDGET_ID + 1;
    }

    private static WidgetProviderInfo cloneIfLocalBinder(WidgetProviderInfo info) {
        if (isLocalBinder() && info != null) {
            return info.clone();
        }

        return info;
    }

    private static LocalRemoteViews cloneIfLocalBinder(LocalRemoteViews views) {
        if (isLocalBinder() && views != null) {
            return views.clone();
        }

        return views;
    }

    private static Bundle cloneIfLocalBinder(Bundle bundle) {
        if (isLocalBinder() && bundle != null) {
            return (Bundle) bundle.clone();
        }

        return bundle;
    }

    private static boolean isLocalBinder() {
        return Process.myPid() == Binder.getCallingPid();
    }

    private Widget widgetFromInfoLocked(WidgetInfo info) {
        if (info == null) return null;

        Widget w = new Widget();
        w.id = info.getId();
        w.restoredId = info.rid;

        String ppkg = info.ppkg;
        String pcls = info.pcls;
        if (ppkg != null && pcls != null) {
            ComponentName provider = new ComponentName(ppkg, pcls);
            w.provider = lookupProviderLocked(provider);
        }

        w.host = lookupOrAddHostLocked(info.hpkg);
        w.width = info.width;
        w.height = info.height;

        return w;
    }

    private void onPackageBroadcastReceived(Intent it) {
        final String action = it.getAction();

        boolean added = false;
        boolean changed = false;
        boolean componentsModified = false;

        String[] pkgs = null;
        if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
            pkgs = it.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            added = true;
        } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
            pkgs = it.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            added = false;
        } else {
            Uri uri = it.getData();
            if (uri == null) return;

            String pkg = uri.getSchemeSpecificPart();
            if (pkg == null) return;

            pkgs = new String[] { pkg };
            added = Intent.ACTION_PACKAGE_ADDED.equals(action);
            changed = Intent.ACTION_PACKAGE_CHANGED.equals(action);
        }

        if (pkgs == null || pkgs.length == 0) return;

        synchronized (mLock) {
            Bundle extras = it.getExtras();
            if (added || changed) {
                final boolean newPkgAdded =
                        (extras == null || !extras.getBoolean(Intent.EXTRA_REPLACING, false))
                                && added;

                for (String pkg : pkgs) {
                    componentsModified |= updateProvidersForPackageLocked(pkg, null);
                }

                if (!newPkgAdded) {

                }
            } else {
                final boolean pkgRemoved =
                        extras == null || !extras.getBoolean(Intent.EXTRA_REPLACING, false);

                if (pkgRemoved) {
                    for (String pkg : pkgs) {
                        componentsModified |= removeHostsAndProvidersForPackageLocked(pkg);
                    }
                }
            }

            if (componentsModified) {
                scheduleNotifyProvidersChangedLocked();
            }
        }
    }

    private boolean
            updateProvidersForPackageLocked(String pkg, Set<ComponentName> removedProviders) {
        boolean updated = false;

        Intent it = new Intent(WidgetManager.ACTION_BIND_WIDGET);
        it.setPackage(pkg);

        HashSet<ComponentName> keep = new HashSet<ComponentName>();
        List<ResolveInfo> infos = queryIntentServices(it);
        for (ResolveInfo info : infos) {
            ServiceInfo si = info.serviceInfo;

            if ((si.applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                continue;
            }

            if (pkg.equals(si.packageName)) {
                ComponentName provider = new ComponentName(pkg, si.name);

                Provider p = lookupProviderLocked(provider);
                if (p == null) {
                    if (addProviderLocked(si)) {
                        keep.add(provider);
                        updated = true;
                    }
                } else {
                    Provider parsed = parseProviderInfoXmlLocked(provider, si);

                    if (parsed != null) {
                        keep.add(provider);
                        p.info = parsed.info;

                        if (p.status != Provider.STATUS_IDLE) {
                            ServiceConnection conn = mProviderServices.get(provider);
                            if (conn == null) {
                                conn = new ProviderServiceConnection(this, p);

                                synchronized (mProviderServices) {
                                    mProviderServices.put(provider, conn);
                                }
                            }

                            Intent service = new Intent(WidgetManager.ACTION_BIND_WIDGET);
                            service.setComponent(provider);
                            p.status = Provider.STATUS_PENDING;
                            mContext.bindService(service, conn, Context.BIND_AUTO_CREATE);
                        }

                        ArrayList<Widget> ws = p.widgets;
                        for (Widget w : ws) {
                            scheduleNotifyProviderChangedLocked(w);
                        }
                    }

                    updated = true;
                }
            }
        }

        final int N = mProviders.size();
        for (int i = N - 1; i >= 0; i--) {
            Provider p = mProviders.get(i);

            ComponentName provider = p.info.provider;
            if (pkg.equals(provider.getPackageName()) && !keep.contains(provider)) {
                if (removedProviders != null) {
                    removedProviders.add(provider);
                }

                deleteProviderLocked(p);
                updated = true;
            }
        }

        return updated;
    }

    private List<ResolveInfo> queryIntentServices(Intent it) {
        int flags = PackageManager.GET_META_DATA | PackageManager.GET_SHARED_LIBRARY_FILES;
        return mPM.queryIntentServices(it, flags);
    }

    private void scheduleNotifyProviderChangedLocked(Widget w) {
        if (w == null) return;

        Provider p = w.provider;
        if (p == null || p.status != Provider.STATUS_BOND) return;

        Host h = w.host;
        if (h == null) return;

        IWidgetHost hcb = h.cb;
        if (hcb != null) {
            try {
                hcb.providerChanged(w.id, p.info);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteProviderLocked(Provider p) {
        ArrayList<Widget> ws = p.widgets;

        final int N = ws.size();
        for (int i = N - 1; i >= 0; i--) {
            Widget w = ws.remove(i);

            Host h = w.host;
            h.widgets.remove(w);
            removeWidgetLocked(w);
            w.provider = null;
            pruneHostLocked(h);
            w.host = null;
        }

        mProviders.remove(p);
        p.cb = null;
    }

    private boolean removeHostsAndProvidersForPackageLocked(String pkg) {
        boolean removed = removeProvidersForPackageLocked(pkg);
        return removed |= removeHostsForPackageLocked(pkg);
    }

    private boolean removeProvidersForPackageLocked(String pkg) {
        boolean removed = false;

        final int N = mProviders.size();
        for (int i = N - 1; i >= 0; i--) {
            Provider p = mProviders.get(i);

            if (pkg.equals(p.info.provider.getPackageName())) {
                deleteProviderLocked(p);
                removed = true;
            }
        }

        return removed;
    }

    private boolean removeHostsForPackageLocked(String pkg) {
        boolean removed = false;

        final int N = mHosts.size();
        for (int i = N - 1; i >= 0; i--) {
            Host h = mHosts.get(i);

            if (pkg.equals(h.pkg)) {
                deleteHostLocked(h);
                removed = true;
            }
        }

        return removed;
    }

    private void deleteHostLocked(Host h) {
        ArrayList<Widget> ws = h.widgets;

        final int N = ws.size();
        for (int i = N - 1; i >= 0; i--) {
            Widget w = ws.remove(i);
            deleteWidgetLocked(w);
        }

        mHosts.remove(h);
        h.cb = null;
    }

    private void scheduleNotifyProvidersChangedLocked() {
        for (Host h : mHosts) {
            IWidgetHost cb = h.cb;

            if (cb != null) {
                try {
                    cb.providersChanged();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class Provider {
        static final int STATUS_IDLE = 0;
        static final int STATUS_PENDING = 1;
        static final int STATUS_BOND = 2;

        WidgetProviderInfo info;
        ArrayList<Widget> widgets = new ArrayList<Widget>();
        IWidgetProvider cb;
        int status = STATUS_IDLE;

        public Provider() {}

        @Override
        public String toString() {
            return "Provider{" + (info == null ? "" : info.provider) + '}';
        }
    }

    private static class Host {
        final String pkg;
        ArrayList<Widget> widgets = new ArrayList<Widget>();
        IWidgetHost cb;

        public Host(String pkg) {
            this.pkg = pkg;
        }

        @Override
        public String toString() {
            return "Host{" + pkg + '}';
        }
    }

    private static class Widget {
        int id;
        int restoredId;
        Provider provider;
        Host host;
        LocalRemoteViews views;
        int width;
        int height;

        @Override
        public String toString() {
            return "Widget{" + id + ':' + host + ':' + provider + '}';
        }

        public WidgetInfo toWidgetInfo() {
            WidgetInfo info = new WidgetInfo(id);
            info.rid = restoredId;

            if (provider != null) {
                ComponentName component = provider.info.provider;

                if (component != null) {
                    info.ppkg = component.getPackageName();
                    info.pcls = component.getClassName();
                }
            }

            info.hpkg = host != null ? host.pkg : null;
            info.width = width;
            info.height = height;

            return info;
        }
    }

    private static class ProviderServiceConnection implements ServiceConnection {
        private final IWidgetService mService;
        private final Provider mProvider;

        public ProviderServiceConnection(IWidgetService service, Provider provider) {
            mService = service;
            mProvider = provider;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mProvider.status = Provider.STATUS_BOND;

            IWidgetProvider pcb = mProvider.cb = IWidgetProvider.Stub.asInterface(service);
            if (pcb != null) {
                try {
                    pcb.onEnabled(mService);

                    ArrayList<Widget> ws = mProvider.widgets;
                    for (Widget w : ws) {
                        Host h = w.host;

                        pcb.onAdded(w.id, h.pkg);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mProvider.cb = null;
        }
    }

    private static class ServiceConnectionProxy implements ServiceConnection {

        private ILocalRemoteViewsAdapterConnection mConnection;

        public ServiceConnectionProxy(IBinder connection) {
            mConnection = ILocalRemoteViewsAdapterConnection.Stub.asInterface(connection);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                mConnection.onServiceConnected(service);
            } catch (RemoteException e) {
                IwdsLog.e(this, "Error parsing service interface");
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            disconnect();
        }

        private void disconnect() {
            try {
                mConnection.onServiceDisconnected();
            } catch (RemoteException e) {
                IwdsLog.e(this, "Error clearing service interface");
                e.printStackTrace();
            }
        }
    }
}