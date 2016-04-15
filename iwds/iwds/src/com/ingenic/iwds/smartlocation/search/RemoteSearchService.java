/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  ZhangYanMing <yanming.zhang@ingenic.com, jamincheung@126.com>
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

package com.ingenic.iwds.smartlocation.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback;
import com.ingenic.iwds.datatransactor.ParcelTransactor;
import com.ingenic.iwds.smartlocation.search.busline.IRemoteBusLineSearchCallback;
import com.ingenic.iwds.smartlocation.search.busline.IRemoteBusStationSearchCallback;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusLineQuery;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusLineResult;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusStationQuery;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusStationResult;
import com.ingenic.iwds.smartlocation.search.district.IRemoteDistrictSearchCallback;
import com.ingenic.iwds.smartlocation.search.district.RemoteDistrictQuery;
import com.ingenic.iwds.smartlocation.search.district.RemoteDistrictResult;
import com.ingenic.iwds.smartlocation.search.geocoder.IRemoteGeocodeSearchCallback;
import com.ingenic.iwds.smartlocation.search.geocoder.IRemoteRegeocodeSearchCallback;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteGeocodeQuery;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteGeocodeResult;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteRegeocodeQuery;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteRegeocodeResult;
import com.ingenic.iwds.smartlocation.search.help.IRemoteInputtipsCallback;
import com.ingenic.iwds.smartlocation.search.help.RemoteInputQuery;
import com.ingenic.iwds.smartlocation.search.help.RemoteTip;
import com.ingenic.iwds.smartlocation.search.poisearch.IRemotePoiDetailSearchCallback;
import com.ingenic.iwds.smartlocation.search.poisearch.IRemotePoiSearchCallback;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiItemDetail;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiQuery;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiResult;
import com.ingenic.iwds.smartlocation.search.route.IRemoteBusRouteSearchCallback;
import com.ingenic.iwds.smartlocation.search.route.IRemoteDriveRouteSearchCallback;
import com.ingenic.iwds.smartlocation.search.route.IRemoteWalkRouteSearchCallback;
import com.ingenic.iwds.smartlocation.search.route.RemoteBusRouteQuery;
import com.ingenic.iwds.smartlocation.search.route.RemoteBusRouteResult;
import com.ingenic.iwds.smartlocation.search.route.RemoteDriveRouteQuery;
import com.ingenic.iwds.smartlocation.search.route.RemoteDriveRouteResult;
import com.ingenic.iwds.smartlocation.search.route.RemoteWalkRouteQuery;
import com.ingenic.iwds.smartlocation.search.route.RemoteWalkRouteResult;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

public class RemoteSearchService extends Service {
    private RemoteSearchServiceStub m_service = new RemoteSearchServiceStub();
    private ParcelTransactor<RemoteSearchResponse> m_transactor;
    private ServiceHandler m_handler;

    @Override
    public void onCreate() {
        IwdsLog.d(this, "onCreate");
        super.onCreate();

        m_transactor = new ParcelTransactor<RemoteSearchResponse>(this,
                RemoteSearchResponse.CREATOR, m_transactorCallback,
                "c1dc19e2-17a4-0797-2222-68a0dd4bfb68");

        m_handler = new ServiceHandler();
    }

    @Override
    public void onDestroy() {
        IwdsLog.d(this, "onDestroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        IwdsLog.d(this, "onBind");

        m_transactor.start();

        return m_service;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        IwdsLog.d(this, "onUnbind");

        m_transactor.stop();

        return super.onUnbind(intent);
    }

    private class RemoteSearchServiceStub extends IRemoteSearchService.Stub {
        private RemoteStatusCallback m_remoteStatusCallback;
        private RemoteGeocodeSearchCallback m_geocodeSearchCallback;
        private RemoteRegeocodeSearchCallback m_regeocodeSearchCallback;
        private RemoteDistrictSearchCallback m_districtSearchCallback;
        private RemotePoiSearchCallback m_poiSearchCallback;
        private RemotePoiDetailSearchCallback m_poiDetailSearchCallback;
        private RemoteInputtipsCallback m_inputTipsCallback;
        private RemoteBusLineSearchCallback m_busLineSearchCallback;
        private RemoteBusStationSearchCallback m_busStationSearchCallback;
        private RemoteBusRouteSearchCallback m_busRouteSearchCallback;
        private RemoteDriveRouteSearchCallback m_driveRouteSearchCallback;
        private RemoteWalkRouteSearchCallback m_walkRouteSearchCallback;

        /* ------------------ RemoteStatusCallback ---------------------- */
        private class RemoteStatusCallback implements IBinder.DeathRecipient {
            private HashMap<String, IRemoteStatusCallback> m_listeners;

            RemoteStatusCallback() {
                m_listeners = new HashMap<String, IRemoteStatusCallback>();
            }

            public void registerRemoteStatusListener(String uuid,
                    IRemoteStatusCallback callback) {

                synchronized (m_listeners) {

                    try {
                        callback.asBinder().linkToDeath(this, 0);

                        m_listeners.put(uuid, callback);

                    } catch (RemoteException e) {
                        IwdsLog.e(this,
                                "Exception in registerRemoteStatusListener: "
                                        + e.toString());
                    }
                }
            }

            public void onRemoteStatusChanged(boolean available) {
                synchronized (m_listeners) {
                    Collection<IRemoteStatusCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteStatusCallback cb : callbacks) {
                        try {
                            cb.onAvailable(available);
                        } catch (RemoteException e) {
                            IwdsLog.e(
                                    this,
                                    "Exception in onRemoteStatusChanged: "
                                            + e.toString());
                        }
                    }
                }
            }

            public void unregisterRemoteStatusListener(String uuid) {
                synchronized (m_listeners) {
                    IRemoteStatusCallback callback = m_listeners.remove(uuid);

                    if (callback == null)
                        return;

                    callback.asBinder().unlinkToDeath(this, 0);
                }
            }

            @Override
            public void binderDied() {
                synchronized (m_listeners) {
                    Set<String> uuids = m_listeners.keySet();
                    for (Iterator<String> it = uuids.iterator(); it.hasNext();) {
                        String uuid = it.next();
                        if (!m_listeners.get(uuid).asBinder().isBinderAlive())
                            it.remove();
                    }
                }
            }
        }

        /* ------------------ RemoteStatusCallback end ---------------------- */

        /* --------------- RemoteGeocodeSearchCallback ------------------ */
        private class RemoteGeocodeSearchCallback implements
                IBinder.DeathRecipient {
            private HashMap<String, IRemoteGeocodeSearchCallback> m_listeners;

            RemoteGeocodeSearchCallback() {
                m_listeners = new HashMap<String, IRemoteGeocodeSearchCallback>();
            }

            public void requestGeocodeSearch(
                    IRemoteGeocodeSearchCallback callback, String uuid,
                    RemoteGeocodeQuery query) {
                synchronized (m_listeners) {
                    try {
                        callback.asBinder().linkToDeath(this, 0);
                        m_listeners.put(uuid, callback);

                        m_handler.requestGeocodeSearch(query, uuid);

                    } catch (RemoteException e) {
                        IwdsLog.e(this, "Exception in requestGeocodeSearch: "
                                + e.toString());
                    }
                }
            }

            public void onGeocodeSearched(RemoteGeocodeResult result,
                    String uuid, int errorCode) {
                synchronized (m_listeners) {

                    IRemoteGeocodeSearchCallback callback = m_listeners
                            .remove(uuid);

                    if (callback == null)
                        return;

                    try {
                        callback.asBinder().unlinkToDeath(this, 0);

                        callback.onGeocodeSearched(result, errorCode);

                    } catch (RemoteException e) {
                        IwdsLog.e(
                                this,
                                "Exception in onGeocodeSearched: "
                                        + e.toString());
                    }
                }

            }

            public void unregisterListeners() {
                synchronized (m_listeners) {
                    Collection<IRemoteGeocodeSearchCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteGeocodeSearchCallback cb : callbacks)
                        if (cb.asBinder().isBinderAlive())
                            cb.asBinder().unlinkToDeath(this, 0);

                    if (!m_listeners.isEmpty()) {
                        IwdsLog.i(this,
                                "unregister all geocode search listeners");
                        m_listeners.clear();
                    }
                }
            }

            @Override
            public void binderDied() {
                synchronized (m_listeners) {
                    Set<String> uuids = m_listeners.keySet();
                    for (Iterator<String> it = uuids.iterator(); it.hasNext();) {
                        String uuid = it.next();
                        if (!m_listeners.get(uuid).asBinder().isBinderAlive())
                            it.remove();
                    }
                }
            }
        }

        /* --------------- RemoteGeocodeSearchCallback end ----------------- */

        /* --------------- RemoteRegepcodeSearchCallback ------------------ */
        private class RemoteRegeocodeSearchCallback implements
                IBinder.DeathRecipient {
            private HashMap<String, IRemoteRegeocodeSearchCallback> m_listeners;

            RemoteRegeocodeSearchCallback() {
                m_listeners = new HashMap<String, IRemoteRegeocodeSearchCallback>();
            }

            public void requestRegeocodeSearch(
                    IRemoteRegeocodeSearchCallback callback, String uuid,
                    RemoteRegeocodeQuery query) {
                synchronized (m_listeners) {
                    try {
                        callback.asBinder().linkToDeath(this, 0);
                        m_listeners.put(uuid, callback);

                        m_handler.requestRegeocodeSearch(query, uuid);

                    } catch (RemoteException e) {
                        IwdsLog.e(this, "Exception in requestRegeocodeSearch: "
                                + e.toString());
                    }
                }
            }

            public void onRegeocodeSearched(RemoteRegeocodeResult result,
                    String uuid, int errorCode) {
                synchronized (m_listeners) {
                    IRemoteRegeocodeSearchCallback callback = m_listeners
                            .remove(uuid);

                    if (callback == null)
                        return;

                    try {
                        callback.asBinder().unlinkToDeath(this, 0);

                        callback.onRegeocodeSearched(result, errorCode);

                    } catch (RemoteException e) {
                        IwdsLog.e(this, "Exception in onRegeocodeSearched: "
                                + e.toString());
                    }
                }
            }

            public void unregisterListeners() {
                synchronized (m_listeners) {
                    Collection<IRemoteRegeocodeSearchCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteRegeocodeSearchCallback cb : callbacks)
                        if (cb.asBinder().isBinderAlive())
                            cb.asBinder().unlinkToDeath(this, 0);

                    if (!m_listeners.isEmpty()) {
                        IwdsLog.i(this,
                                "unregister all regeocode search listeners");
                        m_listeners.clear();
                    }
                }
            }

            @Override
            public void binderDied() {
                synchronized (m_listeners) {
                    Set<String> uuids = m_listeners.keySet();
                    for (Iterator<String> it = uuids.iterator(); it.hasNext();) {
                        String uuid = it.next();
                        if (!m_listeners.get(uuid).asBinder().isBinderAlive())
                            it.remove();
                    }
                }
            }

        }

        /* --------------- RemoteRegepcodeSearchCallback end ----------------- */

        /* --------------- RemoteDistrictSearchCallback ----------------- */
        private class RemoteDistrictSearchCallback implements
                IBinder.DeathRecipient {
            private HashMap<String, IRemoteDistrictSearchCallback> m_listeners;

            RemoteDistrictSearchCallback() {
                m_listeners = new HashMap<String, IRemoteDistrictSearchCallback>();
            }

            public void requestDistrictSearch(
                    IRemoteDistrictSearchCallback callback, String uuid,
                    RemoteDistrictQuery query) {

                synchronized (m_listeners) {
                    try {
                        callback.asBinder().linkToDeath(this, 0);
                        m_listeners.put(uuid, callback);

                        m_handler.requestDistrictSearch(query, uuid);

                    } catch (RemoteException e) {
                        IwdsLog.e(this, "Exception in requestDistrictSearch: "
                                + e.toString());
                    }
                }
            }

            public void onDistrictSearched(RemoteDistrictResult result,
                    String uuid, int errorCode) {
                synchronized (m_listeners) {
                    IRemoteDistrictSearchCallback callback = m_listeners
                            .remove(uuid);

                    if (callback == null) {
                        return;
                    }

                    try {
                        callback.asBinder().unlinkToDeath(this, 0);

                        callback.onDistrictSearched(result, errorCode);

                    } catch (RemoteException e) {
                        IwdsLog.e(this, "Exception in onRegeocodeSearched: "
                                + e.toString());
                    }
                }
            }

            public void unregisterListeners() {
                synchronized (m_listeners) {
                    Collection<IRemoteDistrictSearchCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteDistrictSearchCallback cb : callbacks)
                        if (cb.asBinder().isBinderAlive())
                            cb.asBinder().unlinkToDeath(this, 0);

                    if (!m_listeners.isEmpty()) {
                        IwdsLog.i(this, "unregister district search listeners");
                        m_listeners.clear();
                    }
                }
            }

            @Override
            public void binderDied() {
                synchronized (m_listeners) {
                    Set<String> uuids = m_listeners.keySet();
                    for (Iterator<String> it = uuids.iterator(); it.hasNext();) {
                        String uuid = it.next();
                        if (!m_listeners.get(uuid).asBinder().isBinderAlive())
                            it.remove();
                    }
                }
            }
        }

        /* --------------- RemoteDistrictSearchCallback end ----------------- */

        /* --------------- RemotePoiSearchCallback ----------------- */
        private class RemotePoiSearchCallback implements IBinder.DeathRecipient {
            private HashMap<String, IRemotePoiSearchCallback> m_listeners;

            RemotePoiSearchCallback() {
                m_listeners = new HashMap<String, IRemotePoiSearchCallback>();
            }

            public void requestPoiSearch(IRemotePoiSearchCallback callback,
                    String uuid, RemotePoiQuery query) {
                synchronized (m_listeners) {

                    try {
                        callback.asBinder().linkToDeath(this, 0);
                        m_listeners.put(uuid, callback);

                        m_handler.requestPoiSearch(query, uuid);

                    } catch (RemoteException e) {
                        IwdsLog.e(
                                this,
                                "Exception in requestPoiSearch: "
                                        + e.toString());
                    }
                }
            }

            public void onPoiSearched(RemotePoiResult result, int errorCode,
                    String uuid) {

                synchronized (m_listeners) {
                    IRemotePoiSearchCallback callback = m_listeners
                            .remove(uuid);
                    if (callback == null)
                        return;

                    try {
                        callback.asBinder().unlinkToDeath(this, 0);

                        callback.onPoiSearched(result, errorCode);

                    } catch (RemoteException e) {
                        IwdsLog.e(this,
                                "Exception in onPoiSearched: " + e.toString());
                    }
                }
            }

            public void unregisterListeners() {
                synchronized (m_listeners) {
                    Collection<IRemotePoiSearchCallback> callbacks = m_listeners
                            .values();

                    for (IRemotePoiSearchCallback cb : callbacks)
                        if (cb.asBinder().isBinderAlive())
                            cb.asBinder().unlinkToDeath(this, 0);

                    if (!m_listeners.isEmpty()) {
                        IwdsLog.i(this, "unregister poi search listeners");
                        m_listeners.clear();
                    }
                }
            }

            @Override
            public void binderDied() {
                synchronized (m_listeners) {
                    Set<String> uuids = m_listeners.keySet();
                    for (Iterator<String> it = uuids.iterator(); it.hasNext();) {
                        String uuid = it.next();
                        if (!m_listeners.get(uuid).asBinder().isBinderAlive())
                            it.remove();
                    }
                }
            }
        }

        /* --------------- RemotePoiSearchCallback end ----------------- */

        /* --------------- RemotePoiDetailSearchCallback ----------------- */
        private class RemotePoiDetailSearchCallback implements
                IBinder.DeathRecipient {
            private HashMap<String, IRemotePoiDetailSearchCallback> m_listeners;

            RemotePoiDetailSearchCallback() {
                m_listeners = new HashMap<String, IRemotePoiDetailSearchCallback>();
            }

            public void requestPoiDetailSearch(
                    IRemotePoiDetailSearchCallback callback, String uuid,
                    String poiId) {
                synchronized (m_listeners) {

                    try {
                        callback.asBinder().linkToDeath(this, 0);
                        m_listeners.put(uuid, callback);

                        m_handler.requestPoiDetailSearch(poiId, uuid);

                    } catch (RemoteException e) {
                        IwdsLog.e(this, "Exception in requestPoiDetailSearch: "
                                + e.toString());
                    }
                }
            }

            public void onPoiItemDetailSearched(
                    RemotePoiItemDetail poiItemDetail, int errorCode,
                    String uuid) {

                synchronized (m_listeners) {
                    IRemotePoiDetailSearchCallback callback = m_listeners
                            .remove(uuid);
                    if (callback == null)
                        return;

                    try {
                        callback.asBinder().unlinkToDeath(this, 0);

                        callback.onPoiItemDetailSearched(poiItemDetail,
                                errorCode);
                    } catch (RemoteException e) {
                        IwdsLog.e(
                                this,
                                "Exception in onPoiItemDetailSearched: "
                                        + e.toString());
                    }
                }
            }

            public void unregisterListeners() {
                synchronized (m_listeners) {
                    Collection<IRemotePoiDetailSearchCallback> callbacks = m_listeners
                            .values();

                    for (IRemotePoiDetailSearchCallback cb : callbacks)
                        if (cb.asBinder().isBinderAlive())
                            cb.asBinder().unlinkToDeath(this, 0);

                    if (!m_listeners.isEmpty()) {
                        IwdsLog.i(this, "unregister poi search listeners");
                        m_listeners.clear();
                    }
                }
            }

            @Override
            public void binderDied() {
                synchronized (m_listeners) {
                    Set<String> uuids = m_listeners.keySet();
                    for (Iterator<String> it = uuids.iterator(); it.hasNext();) {
                        String uuid = it.next();
                        if (!m_listeners.get(uuid).asBinder().isBinderAlive())
                            it.remove();
                    }
                }
            }
        }

        /* --------------- RemotePoiDetailSearchCallback end ----------------- */

        /* --------------- RemoteInputtipsCallback ----------------- */
        private class RemoteInputtipsCallback implements IBinder.DeathRecipient {
            private HashMap<String, IRemoteInputtipsCallback> m_listeners;

            RemoteInputtipsCallback() {
                m_listeners = new HashMap<String, IRemoteInputtipsCallback>();
            }

            public void requestInputtips(IRemoteInputtipsCallback callback,
                    String uuid, RemoteInputQuery query) {
                synchronized (m_listeners) {
                    try {
                        callback.asBinder().linkToDeath(this, 0);
                        m_listeners.put(uuid, callback);

                        m_handler.requestInputtips(query, uuid);

                    } catch (RemoteException e) {
                        IwdsLog.e(
                                this,
                                "Exception in requestInputtips: "
                                        + e.toString());
                    }
                }
            }

            public void onGetInputtips(ArrayList<RemoteTip> tipList,
                    String uuid, int errorCode) {
                synchronized (m_listeners) {
                    IRemoteInputtipsCallback callback = m_listeners
                            .remove(uuid);

                    if (callback == null)
                        return;

                    try {
                        callback.asBinder().unlinkToDeath(this, 0);
                        callback.onGetInputtips(tipList, errorCode);

                    } catch (RemoteException e) {
                        IwdsLog.e(this,
                                "Exception in onGetInputtips: " + e.toString());
                    }
                }
            }

            public void unregisterListeners() {
                synchronized (m_listeners) {
                    Collection<IRemoteInputtipsCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteInputtipsCallback cb : callbacks)
                        if (cb.asBinder().isBinderAlive())
                            cb.asBinder().unlinkToDeath(this, 0);

                    if (!m_listeners.isEmpty()) {
                        IwdsLog.i(this,
                                "unregister input tips search listeners");
                        m_listeners.clear();
                    }
                }
            }

            @Override
            public void binderDied() {
                synchronized (m_listeners) {
                    Set<String> uuids = m_listeners.keySet();
                    for (Iterator<String> it = uuids.iterator(); it.hasNext();) {
                        String uuid = it.next();
                        if (!m_listeners.get(uuid).asBinder().isBinderAlive())
                            it.remove();
                    }
                }
            }
        }

        /* --------------- RemoteInputtipsCallback end ----------------- */

        /* --------------- RemoteBusLineSearchCallback ----------------- */
        private class RemoteBusLineSearchCallback implements
                IBinder.DeathRecipient {
            private HashMap<String, IRemoteBusLineSearchCallback> m_listeners;

            RemoteBusLineSearchCallback() {
                m_listeners = new HashMap<String, IRemoteBusLineSearchCallback>();
            }

            public void requestBusLineSearch(
                    IRemoteBusLineSearchCallback callback, String uuid,
                    RemoteBusLineQuery query) {
                synchronized (m_listeners) {
                    try {
                        callback.asBinder().linkToDeath(this, 0);
                        m_listeners.put(uuid, callback);

                        m_handler.requestBusLineSearch(query, uuid);

                    } catch (RemoteException e) {
                        IwdsLog.e(this, "Exception in requestBusLineSearch: "
                                + e.toString());
                    }
                }

            }

            public void onBusLineSearched(RemoteBusLineResult result,
                    String uuid, int errorCode) {
                synchronized (m_listeners) {
                    IRemoteBusLineSearchCallback callback = m_listeners
                            .remove(uuid);

                    if (callback == null)
                        return;

                    try {
                        callback.asBinder().unlinkToDeath(this, 0);

                        callback.onBusLineSearched(result, errorCode);
                    } catch (RemoteException e) {
                        IwdsLog.e(
                                this,
                                "Exception in onBusLineSearched: "
                                        + e.toString());
                    }
                }
            }

            public void unregisterListeners() {
                synchronized (m_listeners) {
                    Collection<IRemoteBusLineSearchCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteBusLineSearchCallback cb : callbacks)
                        if (cb.asBinder().isBinderAlive())
                            cb.asBinder().unlinkToDeath(this, 0);

                    if (!m_listeners.isEmpty()) {
                        IwdsLog.i(this, "unregister bus line search listeners");
                        m_listeners.clear();
                    }
                }
            }

            @Override
            public void binderDied() {
                synchronized (m_listeners) {
                    Set<String> uuids = m_listeners.keySet();
                    for (Iterator<String> it = uuids.iterator(); it.hasNext();) {
                        String uuid = it.next();
                        if (!m_listeners.get(uuid).asBinder().isBinderAlive())
                            it.remove();
                    }
                }
            }
        }

        /* --------------- RemoteBusLineSearchCallback end ----------------- */

        /* --------------- RemoteBusStationSearchCallback ----------------- */
        private class RemoteBusStationSearchCallback implements
                IBinder.DeathRecipient {
            private HashMap<String, IRemoteBusStationSearchCallback> m_listeners;

            RemoteBusStationSearchCallback() {
                m_listeners = new HashMap<String, IRemoteBusStationSearchCallback>();
            }

            public void requestBusStationSearch(
                    IRemoteBusStationSearchCallback callback, String uuid,
                    RemoteBusStationQuery query) {
                synchronized (m_listeners) {
                    try {
                        callback.asBinder().linkToDeath(this, 0);
                        m_listeners.put(uuid, callback);

                        m_handler.requestBusStationSearch(query, uuid);

                    } catch (RemoteException e) {
                        IwdsLog.e(
                                this,
                                "Exception in requestBusStationSearch: "
                                        + e.toString());
                    }
                }
            }

            public void onBusStationSearched(RemoteBusStationResult result,
                    String uuid, int errorCode) {
                synchronized (m_listeners) {
                    IRemoteBusStationSearchCallback callback = m_listeners
                            .remove(uuid);

                    if (callback == null)
                        return;

                    try {
                        callback.asBinder().unlinkToDeath(this, 0);

                        callback.onBusStationSearched(result, errorCode);
                    } catch (RemoteException e) {
                        IwdsLog.e(this, "Exception in onBusStationSearched: "
                                + e.toString());
                    }
                }
            }

            public void unregisterListeners() {
                synchronized (m_listeners) {
                    Collection<IRemoteBusStationSearchCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteBusStationSearchCallback cb : callbacks)
                        if (cb.asBinder().isBinderAlive())
                            cb.asBinder().unlinkToDeath(this, 0);

                    if (!m_listeners.isEmpty()) {
                        IwdsLog.i(this, "unregister bus line search listeners");
                        m_listeners.clear();
                    }
                }
            }

            @Override
            public void binderDied() {
                synchronized (m_listeners) {
                    Set<String> uuids = m_listeners.keySet();
                    for (Iterator<String> it = uuids.iterator(); it.hasNext();) {
                        String uuid = it.next();
                        if (!m_listeners.get(uuid).asBinder().isBinderAlive())
                            it.remove();
                    }
                }
            }
        }

        /* --------------- RemoteBusStationSearchCallback end ----------------- */

        /* --------------- RemoteBusRouteSearchCallback ----------------- */
        private class RemoteBusRouteSearchCallback implements
                IBinder.DeathRecipient {
            private HashMap<String, IRemoteBusRouteSearchCallback> m_listeners;

            RemoteBusRouteSearchCallback() {
                m_listeners = new HashMap<String, IRemoteBusRouteSearchCallback>();
            }

            public void requestBusRouteSearch(
                    IRemoteBusRouteSearchCallback callback, String uuid,
                    RemoteBusRouteQuery query) {
                synchronized (m_listeners) {
                    try {
                        callback.asBinder().linkToDeath(this, 0);
                        m_listeners.put(uuid, callback);

                        m_handler.requestBusRouteSearch(query, uuid);

                    } catch (RemoteException e) {
                        IwdsLog.e(this, "Exception in requestBusRouteSearch: "
                                + e.toString());
                    }
                }
            }

            public void onBusRouteSearched(RemoteBusRouteResult result,
                    String uuid, int errorCode) {
                synchronized (m_listeners) {
                    IRemoteBusRouteSearchCallback callback = m_listeners
                            .remove(uuid);

                    if (callback == null)
                        return;

                    try {
                        callback.asBinder().unlinkToDeath(this, 0);
                        callback.onBusRouteSearched(result, errorCode);

                    } catch (RemoteException e) {
                        IwdsLog.e(
                                this,
                                "Exception in onBusRouteSearched: "
                                        + e.toString());
                    }
                }
            }

            public void unregisterListeners() {
                synchronized (m_listeners) {
                    Collection<IRemoteBusRouteSearchCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteBusRouteSearchCallback cb : callbacks)
                        if (cb.asBinder().isBinderAlive())
                            cb.asBinder().unlinkToDeath(this, 0);

                    if (!m_listeners.isEmpty()) {
                        IwdsLog.i(this, "unregister bus route search listeners");
                        m_listeners.clear();
                    }
                }
            }

            @Override
            public void binderDied() {
                synchronized (m_listeners) {
                    Set<String> uuids = m_listeners.keySet();
                    for (Iterator<String> it = uuids.iterator(); it.hasNext();) {
                        String uuid = it.next();
                        if (!m_listeners.get(uuid).asBinder().isBinderAlive())
                            it.remove();
                    }
                }
            }
        }

        /* --------------- RemoteBusRouteSearchCallback end ----------------- */

        /* --------------- RemoteDriveRouteSearchCallback ----------------- */
        private class RemoteDriveRouteSearchCallback implements
                IBinder.DeathRecipient {
            private HashMap<String, IRemoteDriveRouteSearchCallback> m_listeners;

            RemoteDriveRouteSearchCallback() {
                m_listeners = new HashMap<String, IRemoteDriveRouteSearchCallback>();
            }

            public void requestDriveRouteSearch(
                    IRemoteDriveRouteSearchCallback callback, String uuid,
                    RemoteDriveRouteQuery query) {
                synchronized (m_listeners) {
                    try {
                        callback.asBinder().linkToDeath(this, 0);
                        m_listeners.put(uuid, callback);

                        m_handler.requestDriveRouteSearch(query, uuid);

                    } catch (RemoteException e) {
                        IwdsLog.e(
                                this,
                                "Exception in requestDriveRouteSearch: "
                                        + e.toString());
                    }
                }
            }

            public void onDriveRouteSearched(RemoteDriveRouteResult result,
                    String uuid, int errorCode) {
                synchronized (m_listeners) {
                    IRemoteDriveRouteSearchCallback callback = m_listeners
                            .remove(uuid);

                    if (callback == null)
                        return;

                    try {
                        callback.asBinder().unlinkToDeath(this, 0);
                        callback.onDriveRouteSearched(result, errorCode);

                    } catch (RemoteException e) {
                        IwdsLog.e(this, "Exception in onDriveRouteSearched: "
                                + e.toString());
                    }
                }
            }

            public void unregisterListeners() {
                synchronized (m_listeners) {
                    Collection<IRemoteDriveRouteSearchCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteDriveRouteSearchCallback cb : callbacks)
                        if (cb.asBinder().isBinderAlive())
                            cb.asBinder().unlinkToDeath(this, 0);

                    if (!m_listeners.isEmpty()) {
                        IwdsLog.i(this,
                                "unregister drive route search listeners");
                        m_listeners.clear();
                    }
                }
            }

            @Override
            public void binderDied() {
                synchronized (m_listeners) {
                    Set<String> uuids = m_listeners.keySet();
                    for (Iterator<String> it = uuids.iterator(); it.hasNext();) {
                        String uuid = it.next();
                        if (!m_listeners.get(uuid).asBinder().isBinderAlive())
                            it.remove();
                    }
                }
            }

        }

        /* --------------- RemoteDriveRouteSearchCallback end ----------------- */

        /* --------------- RemoteWalkRouteSearchCallback ----------------- */
        private class RemoteWalkRouteSearchCallback implements
                IBinder.DeathRecipient {
            private HashMap<String, IRemoteWalkRouteSearchCallback> m_listeners;

            RemoteWalkRouteSearchCallback() {
                m_listeners = new HashMap<String, IRemoteWalkRouteSearchCallback>();
            }

            public void requestWalkRouteSearch(
                    IRemoteWalkRouteSearchCallback callback, String uuid,
                    RemoteWalkRouteQuery query) {
                synchronized (m_listeners) {
                    try {
                        callback.asBinder().linkToDeath(this, 0);
                        m_listeners.put(uuid, callback);

                        m_handler.requestWalkRouteSearch(query, uuid);

                    } catch (RemoteException e) {
                        IwdsLog.e(this, "Exception in requestWalkRouteSearch: "
                                + e.toString());
                    }
                }
            }

            public void onWalkRouteSearched(RemoteWalkRouteResult result,
                    String uuid, int errorCode) {
                synchronized (m_listeners) {
                    IRemoteWalkRouteSearchCallback callback = m_listeners
                            .remove(uuid);

                    if (callback == null)
                        return;

                    try {
                        callback.asBinder().unlinkToDeath(this, 0);
                        callback.onWalkRouteSearched(result, errorCode);

                    } catch (RemoteException e) {
                        IwdsLog.e(this, "Exception in onWalkRouteSearched: "
                                + e.toString());
                    }
                }
            }

            public void unregisterListeners() {
                synchronized (m_listeners) {
                    Collection<IRemoteWalkRouteSearchCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteWalkRouteSearchCallback cb : callbacks)
                        if (cb.asBinder().isBinderAlive())
                            cb.asBinder().unlinkToDeath(this, 0);

                    if (!m_listeners.isEmpty()) {
                        IwdsLog.i(this,
                                "unregister walk route search listeners");
                        m_listeners.clear();
                    }
                }
            }

            @Override
            public void binderDied() {
                synchronized (m_listeners) {
                    Set<String> uuids = m_listeners.keySet();
                    for (Iterator<String> it = uuids.iterator(); it.hasNext();) {
                        String uuid = it.next();
                        if (!m_listeners.get(uuid).asBinder().isBinderAlive())
                            it.remove();
                    }
                }
            }

        }

        /* --------------- RemoteWalkRouteSearchCallback end ----------------- */

        /* ------------- RemoteSearchService method -------------------- */
        public RemoteSearchServiceStub() {
            m_remoteStatusCallback = new RemoteStatusCallback();
            m_geocodeSearchCallback = new RemoteGeocodeSearchCallback();
            m_regeocodeSearchCallback = new RemoteRegeocodeSearchCallback();
            m_districtSearchCallback = new RemoteDistrictSearchCallback();
            m_poiSearchCallback = new RemotePoiSearchCallback();
            m_poiDetailSearchCallback = new RemotePoiDetailSearchCallback();
            m_inputTipsCallback = new RemoteInputtipsCallback();
            m_busLineSearchCallback = new RemoteBusLineSearchCallback();
            m_busStationSearchCallback = new RemoteBusStationSearchCallback();
            m_busRouteSearchCallback = new RemoteBusRouteSearchCallback();
            m_driveRouteSearchCallback = new RemoteDriveRouteSearchCallback();
            m_walkRouteSearchCallback = new RemoteWalkRouteSearchCallback();
        }

        public void unregisterAllSingleListeners() {
            synchronized (m_geocodeSearchCallback) {
                m_geocodeSearchCallback.unregisterListeners();
            }

            synchronized (m_regeocodeSearchCallback) {
                m_regeocodeSearchCallback.unregisterListeners();
            }

            synchronized (m_districtSearchCallback) {
                m_districtSearchCallback.unregisterListeners();
            }

            synchronized (m_poiSearchCallback) {
                m_poiSearchCallback.unregisterListeners();
            }

            synchronized (m_poiDetailSearchCallback) {
                m_poiDetailSearchCallback.unregisterListeners();
            }

            synchronized (m_inputTipsCallback) {
                m_inputTipsCallback.unregisterListeners();
            }

            synchronized (m_busLineSearchCallback) {
                m_busLineSearchCallback.unregisterListeners();
            }

            synchronized (m_busStationSearchCallback) {
                m_busStationSearchCallback.unregisterListeners();
            }

            synchronized (m_busRouteSearchCallback) {
                m_busRouteSearchCallback.unregisterListeners();
            }

            synchronized (m_driveRouteSearchCallback) {
                m_driveRouteSearchCallback.unregisterListeners();
            }

            synchronized (m_walkRouteSearchCallback) {
                m_walkRouteSearchCallback.unregisterListeners();
            }
        }

        public void onRemoteStatusChanged(boolean available) {
            synchronized (m_remoteStatusCallback) {
                m_remoteStatusCallback.onRemoteStatusChanged(available);
            }
        }

        public void onGeocodeSearched(RemoteGeocodeResult result, String uuid,
                int errorCode) {
            synchronized (m_geocodeSearchCallback) {
                m_geocodeSearchCallback.onGeocodeSearched(result, uuid,
                        errorCode);
            }
        }

        public void onRegeocodeSearched(RemoteRegeocodeResult result,
                String uuid, int errorCode) {
            synchronized (m_regeocodeSearchCallback) {
                m_regeocodeSearchCallback.onRegeocodeSearched(result, uuid,
                        errorCode);
            }
        }

        public void onDistrictSearched(RemoteDistrictResult result,
                String uuid, int errorCode) {
            synchronized (m_districtSearchCallback) {
                m_districtSearchCallback.onDistrictSearched(result, uuid,
                        errorCode);
            }
        }

        public void onPoiSearched(RemotePoiResult result, String uuid,
                int errorCode) {
            synchronized (m_poiSearchCallback) {
                m_poiSearchCallback.onPoiSearched(result, errorCode, uuid);
            }
        }

        public void onPoiItemDetailSearched(RemotePoiItemDetail poiItemDetail,
                String uuid, int errorCode) {
            synchronized (m_poiDetailSearchCallback) {
                m_poiDetailSearchCallback.onPoiItemDetailSearched(
                        poiItemDetail, errorCode, uuid);
            }
        }

        public void onGetInputtips(ArrayList<RemoteTip> tipList, String uuid,
                int errorCode) {
            synchronized (m_inputTipsCallback) {
                m_inputTipsCallback.onGetInputtips(tipList, uuid, errorCode);
            }
        }

        public void onBusLineSearched(RemoteBusLineResult result, String uuid,
                int errorCode) {
            synchronized (m_busLineSearchCallback) {
                m_busLineSearchCallback.onBusLineSearched(result, uuid,
                        errorCode);
            }
        }

        public void onBusStationSearched(RemoteBusStationResult result,
                String uuid, int errorCode) {
            synchronized (m_busStationSearchCallback) {
                m_busStationSearchCallback.onBusStationSearched(result, uuid,
                        errorCode);
            }
        }

        public void onBusRouteSearched(RemoteBusRouteResult result,
                String uuid, int errorCode) {
            synchronized (m_busRouteSearchCallback) {
                m_busRouteSearchCallback.onBusRouteSearched(result, uuid,
                        errorCode);
            }
        }

        public void onDriveRouteSearched(RemoteDriveRouteResult result,
                String uuid, int errorCode) {
            synchronized (m_driveRouteSearchCallback) {
                m_driveRouteSearchCallback.onDriveRouteSearched(result, uuid,
                        errorCode);
            }
        }

        public void onWalkRouteSearched(RemoteWalkRouteResult result,
                String uuid, int errorCode) {
            synchronized (m_walkRouteSearchCallback) {
                m_walkRouteSearchCallback.onWalkRouteSearched(result, uuid,
                        errorCode);
            }
        }

        @Override
        public void registerRemoteStatusListener(String uuid,
                IRemoteStatusCallback callback) throws RemoteException {
            synchronized (m_remoteStatusCallback) {
                m_remoteStatusCallback.registerRemoteStatusListener(uuid,
                        callback);
            }

            // do cold boot
            m_handler.requestSearchServiceStatus();
        }

        @Override
        public void unregisterRemoteStatusListener(String uuid)
                throws RemoteException {
            synchronized (m_remoteStatusCallback) {
                m_remoteStatusCallback.unregisterRemoteStatusListener(uuid);
            }
        }

        @Override
        public void requestGeocodeSearch(IRemoteGeocodeSearchCallback callback,
                String uuid, RemoteGeocodeQuery query) throws RemoteException {
            synchronized (m_geocodeSearchCallback) {
                m_geocodeSearchCallback.requestGeocodeSearch(callback, uuid,
                        query);
            }
        }

        @Override
        public void requestRegeocodeSearch(
                IRemoteRegeocodeSearchCallback callback, String uuid,
                RemoteRegeocodeQuery query) throws RemoteException {
            synchronized (m_regeocodeSearchCallback) {
                m_regeocodeSearchCallback.requestRegeocodeSearch(callback,
                        uuid, query);
            }
        }

        @Override
        public void requestDistrictSearch(
                IRemoteDistrictSearchCallback callback, String uuid,
                RemoteDistrictQuery query) throws RemoteException {
            synchronized (m_districtSearchCallback) {
                m_districtSearchCallback.requestDistrictSearch(callback, uuid,
                        query);
            }
        }

        @Override
        public void requestPoiDetailSearch(
                IRemotePoiDetailSearchCallback callback, String uuid,
                String poiId) throws RemoteException {
            synchronized (m_poiDetailSearchCallback) {
                m_poiDetailSearchCallback.requestPoiDetailSearch(callback,
                        uuid, poiId);
            }
        }

        @Override
        public void requestPoiSearch(IRemotePoiSearchCallback callback,
                String uuid, RemotePoiQuery query) throws RemoteException {
            synchronized (m_poiSearchCallback) {
                m_poiSearchCallback.requestPoiSearch(callback, uuid, query);
            }
        }

        @Override
        public void requestInputtips(IRemoteInputtipsCallback callback,
                String uuid, RemoteInputQuery query) throws RemoteException {
            synchronized (m_inputTipsCallback) {
                m_inputTipsCallback.requestInputtips(callback, uuid, query);
            }
        }

        @Override
        public void requestBusLineSearch(IRemoteBusLineSearchCallback callback,
                String uuid, RemoteBusLineQuery query) throws RemoteException {
            synchronized (m_busLineSearchCallback) {
                m_busLineSearchCallback.requestBusLineSearch(callback, uuid,
                        query);
            }
        }

        @Override
        public void requestBusStationSearch(
                IRemoteBusStationSearchCallback callback, String uuid,
                RemoteBusStationQuery query) throws RemoteException {
            synchronized (m_busStationSearchCallback) {
                m_busStationSearchCallback.requestBusStationSearch(callback,
                        uuid, query);
            }

        }

        @Override
        public void requestBusRouteSearch(
                IRemoteBusRouteSearchCallback callback, String uuid,
                RemoteBusRouteQuery query) throws RemoteException {
            synchronized (m_busRouteSearchCallback) {
                m_busRouteSearchCallback.requestBusRouteSearch(callback, uuid,
                        query);
            }
        }

        @Override
        public void requestDriveRouteSearch(
                IRemoteDriveRouteSearchCallback callback, String uuid,
                RemoteDriveRouteQuery query) throws RemoteException {
            synchronized (m_driveRouteSearchCallback) {
                m_driveRouteSearchCallback.requestDriveRouteSearch(callback,
                        uuid, query);
            }

        }

        @Override
        public void requestWalkRouteSearch(
                IRemoteWalkRouteSearchCallback callback, String uuid,
                RemoteWalkRouteQuery query) throws RemoteException {
            synchronized (m_walkRouteSearchCallback) {
                m_walkRouteSearchCallback.requestWalkRouteSearch(callback,
                        uuid, query);
            }

        }
        /* ------------- RemoteSearchService method end -------------------- */
    }

    private class ServiceHandler extends Handler {
        private final static int MSG_CHANNEL_STATUS_CHANGED = 0;
        private final static int MSG_REQUEST_SEARCH_SERVICE_STATUS = 1;
        private final static int MSG_REQUEST_GEOCODE_SEARCH = 2;
        private final static int MSG_REQUEST_REGEOCODE_SEARCH = 3;
        private final static int MSG_REQUEST_DISTRICT_SEARCH = 4;
        private final static int MSG_REQUEST_POI_SEARCH = 5;
        private final static int MSG_REQUEST_POI_DETAIL_SEARCH = 6;
        private final static int MSG_REQUEST_INPUT_TIPS_SEARCH = 7;
        private final static int MSG_REQUEST_BUS_LINE_SEARCH = 8;
        private final static int MSG_REQUEST_BUS_STATION_SEARCH = 9;
        private final static int MSG_REQUEST_BUS_ROUTE_SEARCH = 10;
        private final static int MSG_REQUEST_DRIVE_ROUTE_SEARCH = 11;
        private final static int MSG_REQUEST_WALK_ROUTE_SEARCH = 12;
        private final static int MSG_RESPONSE_SEARCH_SERVICE_CONNECTED = 20;
        private final static int MSG_RESPONSE_GEOCODE_SEARCHED = 21;
        private final static int MSG_RESPONSE_REGEOCODE_SEARCHED = 22;
        private final static int MSG_RESPONSE_DISTRICT_SEARCHED = 23;
        private final static int MSG_RESPONSE_POI_SEARCHED = 24;
        private final static int MSG_RESPONSE_POI_DETAIL_SEARCHED = 25;
        private final static int MSG_RESPONSE_INPUT_TIPS_SEARCHED = 26;
        private final static int MSG_RESPONSE_BUS_LINE_SEARCHED = 27;
        private final static int MSG_RESPONSE_BUS_STATION_SEARCHED = 28;
        private final static int MSG_RESPONSE_BUS_ROUTE_SEARCHED = 29;
        private final static int MSG_RESPONSE_DRIVE_ROUTE_SEARCHED = 30;
        private final static int MSG_RESPONSE_WALK_ROUTE_SEARCHED = 31;
        private final static int MSG_SEND_RESULT = 40;

        private boolean m_serviceConnected;

        public void requestSearchServiceStatus() {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_SEARCH_SERVICE_STATUS;

            msg.sendToTarget();
        }

        public void setChannelAvailable(boolean available) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_CHANNEL_STATUS_CHANGED;
            msg.arg1 = available ? 1 : 0;

            msg.sendToTarget();
        }

        public void broadcastRemoteServiceStatus(boolean available) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_RESPONSE_SEARCH_SERVICE_CONNECTED;
            msg.arg1 = available ? 1 : 0;

            msg.sendToTarget();
        }

        public void handleSendResult(boolean isOk) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_SEND_RESULT;
            msg.arg1 = isOk ? 1 : 0;

            msg.sendToTarget();

        }

        public void requestGeocodeSearch(RemoteGeocodeQuery query, String uuid) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            bundle.putString("uuid", uuid);
            bundle.putParcelable("query", query);

            msg.what = MSG_REQUEST_GEOCODE_SEARCH;
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void requestRegeocodeSearch(RemoteRegeocodeQuery query,
                String uuid) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            bundle.putString("uuid", uuid);
            bundle.putParcelable("query", query);

            msg.what = MSG_REQUEST_REGEOCODE_SEARCH;
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void requestDistrictSearch(RemoteDistrictQuery query, String uuid) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            bundle.putString("uuid", uuid);
            bundle.putParcelable("query", query);

            msg.what = MSG_REQUEST_DISTRICT_SEARCH;
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void requestPoiSearch(RemotePoiQuery query, String uuid) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            bundle.putString("uuid", uuid);
            bundle.putParcelable("query", query);

            msg.what = MSG_REQUEST_POI_SEARCH;
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void requestPoiDetailSearch(String poiId, String uuid) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            bundle.putString("uuid", uuid);
            bundle.putString("poiId", poiId);

            msg.what = MSG_REQUEST_POI_DETAIL_SEARCH;
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void requestInputtips(RemoteInputQuery query, String uuid) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            bundle.putString("uuid", uuid);
            bundle.putParcelable("query", query);

            msg.what = MSG_REQUEST_INPUT_TIPS_SEARCH;
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void requestBusLineSearch(RemoteBusLineQuery query, String uuid) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            bundle.putString("uuid", uuid);
            bundle.putParcelable("query", query);

            msg.what = MSG_REQUEST_BUS_LINE_SEARCH;
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void requestBusStationSearch(RemoteBusStationQuery query,
                String uuid) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            bundle.putString("uuid", uuid);
            bundle.putParcelable("query", query);

            msg.what = MSG_REQUEST_BUS_STATION_SEARCH;
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void requestBusRouteSearch(RemoteBusRouteQuery query, String uuid) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            bundle.putString("uuid", uuid);
            bundle.putParcelable("query", query);

            msg.what = MSG_REQUEST_BUS_ROUTE_SEARCH;
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void requestDriveRouteSearch(RemoteDriveRouteQuery query,
                String uuid) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            bundle.putString("uuid", uuid);
            bundle.putParcelable("query", query);

            msg.what = MSG_REQUEST_DRIVE_ROUTE_SEARCH;
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void requestWalkRouteSearch(RemoteWalkRouteQuery query,
                String uuid) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            bundle.putString("uuid", uuid);
            bundle.putParcelable("query", query);

            msg.what = MSG_REQUEST_WALK_ROUTE_SEARCH;
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void handleResponse(RemoteSearchResponse response) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            switch (response.type) {
            case RemoteSearchResponse.TYPE_SEARCH_SERVICE_STATUS:
                msg.what = MSG_RESPONSE_SEARCH_SERVICE_CONNECTED;
                msg.arg1 = response.serviceConnected;

                break;

            case RemoteSearchResponse.TYPE_GEOCODE_SEARCHED:
                msg.what = MSG_RESPONSE_GEOCODE_SEARCHED;

                bundle.putParcelable("result", response.geocodeResult);
                bundle.putString("uuid", response.uuid);
                bundle.putInt("errorCode", response.errorCode);

                msg.setData(bundle);

                break;

            case RemoteSearchResponse.TYPE_REGEOCODE_SEARCHED:
                msg.what = MSG_RESPONSE_REGEOCODE_SEARCHED;

                bundle.putParcelable("result", response.regeocodeResult);
                bundle.putString("uuid", response.uuid);
                bundle.putInt("errorCode", response.errorCode);

                msg.setData(bundle);

                break;

            case RemoteSearchResponse.TYPE_DISTRICT_SEARCHED:
                msg.what = MSG_RESPONSE_DISTRICT_SEARCHED;

                bundle.putParcelable("result", response.districtResult);
                bundle.putString("uuid", response.uuid);
                bundle.putInt("errorCode", response.errorCode);

                msg.setData(bundle);

                break;

            case RemoteSearchResponse.TYPE_POI_SEARCHED:
                msg.what = MSG_RESPONSE_POI_SEARCHED;

                bundle.putParcelable("result", response.poiResult);
                bundle.putString("uuid", response.uuid);
                bundle.putInt("errorCode", response.errorCode);

                msg.setData(bundle);

                break;

            case RemoteSearchResponse.TYPE_POI_DETAIL_SEARCHED:
                msg.what = MSG_RESPONSE_POI_DETAIL_SEARCHED;

                bundle.putParcelable("result", response.poiItemDetail);
                bundle.putString("uuid", response.uuid);
                bundle.putInt("errorCode", response.errorCode);

                msg.setData(bundle);

                break;

            case RemoteSearchResponse.TYPE_INPUT_TIPS_SEARCHED:
                msg.what = MSG_RESPONSE_INPUT_TIPS_SEARCHED;

                bundle.putParcelableArrayList("tipList", response.tipList);
                bundle.putString("uuid", response.uuid);
                bundle.putInt("errorCode", response.errorCode);

                msg.setData(bundle);

                break;

            case RemoteSearchResponse.TYPE_BUS_LINE_SEARCHED:
                msg.what = MSG_RESPONSE_BUS_LINE_SEARCHED;

                bundle.putParcelable("result", response.busLineResult);
                bundle.putString("uuid", response.uuid);
                bundle.putInt("errorCode", response.errorCode);

                msg.setData(bundle);
                break;

            case RemoteSearchResponse.TYPE_BUS_STATION_SEARCHED:
                msg.what = MSG_RESPONSE_BUS_STATION_SEARCHED;

                bundle.putParcelable("result", response.busStationResult);
                bundle.putString("uuid", response.uuid);
                bundle.putInt("errorCode", response.errorCode);

                msg.setData(bundle);
                break;

            case RemoteSearchResponse.TYPE_BUS_ROUTE_SEARCHED:
                msg.what = MSG_RESPONSE_BUS_ROUTE_SEARCHED;

                bundle.putParcelable("result", response.busRouteResult);
                bundle.putString("uuid", response.uuid);
                bundle.putInt("errorCode", response.errorCode);

                msg.setData(bundle);
                break;

            case RemoteSearchResponse.TYPE_DRIVE_ROUTE_SEARCHED:
                msg.what = MSG_RESPONSE_DRIVE_ROUTE_SEARCHED;

                bundle.putParcelable("result", response.driveRouteResult);
                bundle.putString("uuid", response.uuid);
                bundle.putInt("errorCode", response.errorCode);

                msg.setData(bundle);
                break;

            case RemoteSearchResponse.TYPE_WALK_ROUTE_SEARCHED:
                msg.what = MSG_RESPONSE_WALK_ROUTE_SEARCHED;

                bundle.putParcelable("result", response.walkRouteResult);
                bundle.putString("uuid", response.uuid);
                bundle.putInt("errorCode", response.errorCode);

                msg.setData(bundle);
                break;

            default:
                IwdsAssert.dieIf(this, true, "Unsupported response type: "
                        + response.type);
                return;
            }

            msg.sendToTarget();
        }

        @Override
        public void handleMessage(Message msg) {
            RemoteSearchRequest request = RemoteSearchRequest
                    .obtain(m_transactor);

            Bundle bundle = msg.getData();

            String uuid = bundle.getString("uuid");
            int errorCode = bundle.getInt("errorCode");

            switch (msg.what) {
            case MSG_RESPONSE_SEARCH_SERVICE_CONNECTED:
                m_serviceConnected = msg.arg1 == 1 ? true : false;

                IwdsLog.i(this, "Search service connected: "
                        + m_serviceConnected);

                m_service.onRemoteStatusChanged(m_serviceConnected);
                break;

            case MSG_CHANNEL_STATUS_CHANGED:
                boolean available = msg.arg1 == 1 ? true : false;
                if (!available) {
                    broadcastRemoteServiceStatus(false);
                    m_service.unregisterAllSingleListeners();
                }

                break;

            case MSG_SEND_RESULT:
                boolean isOk = msg.arg1 == 1 ? true : false;

                if (!isOk)
                    m_service.unregisterAllSingleListeners();

                break;

            case MSG_REQUEST_SEARCH_SERVICE_STATUS:
                if (!m_serviceConnected) {
                    IwdsLog.i(this,
                            "Search service on remote device not connected yet");
                    broadcastRemoteServiceStatus(false);

                } else {
                    IwdsLog.d(this,
                            "Search service on remote device already connected");
                    broadcastRemoteServiceStatus(true);
                }

                break;

            case MSG_REQUEST_GEOCODE_SEARCH:
                if (!m_serviceConnected) {
                    handleSendResult(false);
                    return;
                }

                request.type = RemoteSearchRequest.TYPE_GEOCODE_SEARCH;
                request.geocodeQuery = bundle.getParcelable("query");
                request.uuid = uuid;

                IwdsLog.i(this, "Try to request geocode search");

                request.sendToRemote();
                break;

            case MSG_REQUEST_REGEOCODE_SEARCH:
                if (!m_serviceConnected) {
                    handleSendResult(false);
                    return;
                }

                request.type = RemoteSearchRequest.TYPE_REGEOCODE_SEARCH;
                request.regeocodeQuery = bundle.getParcelable("query");
                request.uuid = uuid;

                IwdsLog.i(this, "Try to request regeocode search");

                request.sendToRemote();
                break;

            case MSG_REQUEST_DISTRICT_SEARCH:
                if (!m_serviceConnected) {
                    handleSendResult(false);
                    return;
                }

                request.type = RemoteSearchRequest.TYPE_DISTRICT_SEARCH;
                request.districtQuery = bundle.getParcelable("query");
                request.uuid = uuid;

                IwdsLog.i(this, "Try to request district search");

                request.sendToRemote();
                break;

            case MSG_REQUEST_POI_SEARCH:
                if (!m_serviceConnected) {
                    handleSendResult(false);
                    return;
                }

                request.type = RemoteSearchRequest.TYPE_POI_SEARCH;
                request.poiQuery = bundle.getParcelable("query");
                request.uuid = uuid;

                IwdsLog.i(this, "Try to request poi search");

                request.sendToRemote();

                break;

            case MSG_REQUEST_POI_DETAIL_SEARCH:
                if (!m_serviceConnected) {
                    handleSendResult(false);
                    return;
                }

                request.type = RemoteSearchRequest.TYPE_POI_DETAIL_SEARCH;
                request.poiId = bundle.getString("poiId");
                request.uuid = uuid;

                IwdsLog.i(this, "Try to request poi detail search");

                request.sendToRemote();
                break;

            case MSG_REQUEST_INPUT_TIPS_SEARCH:
                if (!m_serviceConnected) {
                    handleSendResult(false);
                    return;
                }

                request.type = RemoteSearchRequest.TYPE_INPUT_TIPS_SEARCH;

                request.inputQuery = bundle.getParcelable("query");
                request.uuid = uuid;

                IwdsLog.i(this, "Try to request input tips search");

                request.sendToRemote();

                break;

            case MSG_REQUEST_BUS_LINE_SEARCH:
                if (!m_serviceConnected) {
                    handleSendResult(false);
                    return;
                }

                request.type = RemoteSearchRequest.TYPE_BUS_LINE_SEARCH;

                request.busLineQuery = bundle.getParcelable("query");
                request.uuid = uuid;

                IwdsLog.i(this, "Try to request bus line search");

                request.sendToRemote();

                break;

            case MSG_REQUEST_BUS_STATION_SEARCH:
                if (!m_serviceConnected) {
                    handleSendResult(false);
                    return;
                }

                request.type = RemoteSearchRequest.TYPE_BUS_STATION_SEARCH;

                request.busStationQuery = bundle.getParcelable("query");
                request.uuid = uuid;

                IwdsLog.d(this, "Try to request bus station search");

                request.sendToRemote();

                break;

            case MSG_REQUEST_BUS_ROUTE_SEARCH:
                if (!m_serviceConnected) {
                    handleSendResult(false);
                    return;
                }

                request.type = RemoteSearchRequest.TYPE_BUS_ROUTE_SEARCH;

                request.busRouteQuery = bundle.getParcelable("query");
                request.uuid = uuid;

                IwdsLog.d(this, "Try to request bus route search");

                request.sendToRemote();
                break;

            case MSG_REQUEST_DRIVE_ROUTE_SEARCH:
                if (!m_serviceConnected) {
                    handleSendResult(false);
                    return;
                }

                request.type = RemoteSearchRequest.TYPE_DRIVE_ROUTE_SEARCH;

                request.driveRouteQuery = bundle.getParcelable("query");
                request.uuid = uuid;

                IwdsLog.d(this, "Try to request drive route search");

                request.sendToRemote();
                break;

            case MSG_REQUEST_WALK_ROUTE_SEARCH:
                if (!m_serviceConnected) {
                    handleSendResult(false);
                    return;
                }

                request.type = RemoteSearchRequest.TYPE_WALK_ROUTE_SEARCH;

                request.walkRouteQuery = bundle.getParcelable("query");
                request.uuid = uuid;

                IwdsLog.d(this, "Try to request walk route search");

                request.sendToRemote();
                break;

            case MSG_RESPONSE_GEOCODE_SEARCHED:

                m_service.onGeocodeSearched(
                        (RemoteGeocodeResult) bundle.getParcelable("result"),
                        uuid, errorCode);

                break;

            case MSG_RESPONSE_REGEOCODE_SEARCHED:

                m_service.onRegeocodeSearched(
                        (RemoteRegeocodeResult) bundle.getParcelable("result"),
                        uuid, errorCode);
                break;

            case MSG_RESPONSE_DISTRICT_SEARCHED:

                m_service.onDistrictSearched(
                        (RemoteDistrictResult) bundle.getParcelable("result"),
                        uuid, errorCode);
                break;

            case MSG_RESPONSE_POI_SEARCHED:

                m_service.onPoiSearched(
                        (RemotePoiResult) bundle.getParcelable("result"), uuid,
                        errorCode);
                break;

            case MSG_RESPONSE_POI_DETAIL_SEARCHED:

                m_service.onPoiItemDetailSearched(
                        (RemotePoiItemDetail) bundle.getParcelable("result"),
                        uuid, errorCode);
                break;

            case MSG_RESPONSE_INPUT_TIPS_SEARCHED:

                ArrayList<RemoteTip> tipList = bundle
                        .getParcelableArrayList("tipList");

                m_service.onGetInputtips(tipList, uuid, errorCode);
                break;

            case MSG_RESPONSE_BUS_LINE_SEARCHED:
                m_service.onBusLineSearched(
                        (RemoteBusLineResult) bundle.getParcelable("result"),
                        uuid, errorCode);
                break;

            case MSG_RESPONSE_BUS_STATION_SEARCHED:
                m_service
                        .onBusStationSearched((RemoteBusStationResult) bundle
                                .getParcelable("result"), uuid, errorCode);
                break;

            case MSG_RESPONSE_BUS_ROUTE_SEARCHED:
                m_service.onBusRouteSearched(
                        (RemoteBusRouteResult) bundle.getParcelable("result"),
                        uuid, errorCode);
                break;

            case MSG_RESPONSE_DRIVE_ROUTE_SEARCHED:
                m_service
                        .onDriveRouteSearched((RemoteDriveRouteResult) bundle
                                .getParcelable("result"), uuid, errorCode);
                break;

            case MSG_RESPONSE_WALK_ROUTE_SEARCHED:
                m_service.onWalkRouteSearched(
                        (RemoteWalkRouteResult) bundle.getParcelable("result"),
                        uuid, errorCode);
                break;

            default:
                IwdsAssert.dieIf(this, true, "Unknown message");
            }
        }
    }

    private DataTransactorCallback m_transactorCallback = new DataTransactorCallback() {

        @Override
        public void onLinkConnected(DeviceDescriptor descriptor,
                boolean isConnected) {
            // do not care
        }

        @Override
        public void onChannelAvailable(boolean isAvailable) {
            if (!isAvailable) {
                m_handler.setChannelAvailable(false);
            }
        }

        @Override
        public void onSendResult(DataTransactResult result) {
            // TODO: send unsuccessful equal channel unavailable?
            switch (result.getResultCode()) {
            case DataTransactResult.RESULT_FAILED_CHANNEL_UNAVAILABLE:
            case DataTransactResult.RESULT_FAILED_LINK_DISCONNECTED:
            case DataTransactResult.RESULT_FAILED_IWDS_CRASH:
                m_handler.handleSendResult(false);
                break;
            default:
                break;
            }
        }

        @Override
        public void onDataArrived(Object object) {
            if (object instanceof RemoteSearchResponse)
                m_handler.handleResponse((RemoteSearchResponse) object);
        }

        @Override
        public void onSendFileProgress(int progress) {
            // do not care
        }

        @Override
        public void onRecvFileProgress(int progress) {
            // do not care
        }

        @Override
        public void onSendFileInterrupted(int index) {

        }

        @Override
        public void onRecvFileInterrupted(int index) {

        }
    };
}
