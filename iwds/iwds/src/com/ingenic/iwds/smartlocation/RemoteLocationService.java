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

package com.ingenic.iwds.smartlocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback;
import com.ingenic.iwds.datatransactor.ParcelTransactor;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

public class RemoteLocationService extends Service {

    private RemoteLocationServiceStub m_service = new RemoteLocationServiceStub();
    private ParcelTransactor<RemoteLocationResponse> m_transactor;
    private ServiceHandler m_handler;

    @Override
    public void onCreate() {
        IwdsLog.d(this, "onCreate");
        super.onCreate();

        m_transactor = new ParcelTransactor<RemoteLocationResponse>(this,
                RemoteLocationResponse.CREATOR, m_transportCallback,
                "c1dc19e2-17a4-0797-1111-68a0dd4bfb68");

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

    private class RemoteLocationServiceStub extends IRemoteLocationService.Stub {
        private ArrayList<RemoteLocationCallback> m_locationCallbacks;
        private ArrayList<RemoteWeatherUpdateCallback> m_weatherUpdateCallbacks;
        private ArrayList<RemoteLastKnownLocationCallback> m_lastKnownLocationCallbacks;
        private RemoteGeoFenceCallback m_geoFenceCallback;
        private RemoteProximityCallback m_proximityCallback;
        private RemoteGpsStatusCallback m_gpsStatusCallback;
        private RemoteNetworkStatusCallback m_networkStatusCallback;
        private RemoteStatusCallback m_remoteStatusCallback;
        private RemoteGpsCurrentStatusCallback m_gpsCurrentStatusCallback;
        private RemoteNetworkCurrentStatusCallback m_networkCurrentStatusCallback;
        private ArrayList<RemoteProviderStatusCallback> m_providerStatusCallbacks;
        private ArrayList<RemoteProviderListCallback> m_providerListCallbacks;

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

        /* ------------------ RemoteLocationCallback -------------------- */
        private class RemoteLocationCallback implements IBinder.DeathRecipient {
            private HashMap<String, IRemoteLocationCallback> m_listeners;
            String provider;

            RemoteLocationCallback(String p) {
                m_listeners = new HashMap<String, IRemoteLocationCallback>();
                provider = p;
            }

            public void registerLocationListener(String uuid, String provider,
                    long minTime, float minDistance,
                    IRemoteLocationCallback callback) {
                synchronized (m_listeners) {
                    try {
                        callback.asBinder().linkToDeath(this, 0);
                        m_listeners.put(uuid, callback);

                        if (m_listeners.size() == 1) {
                            m_handler.requestRegisterLocationListener(provider,
                                    minTime, minDistance);
                        }

                    } catch (RemoteException e) {
                        IwdsLog.e(
                                this,
                                "Exception in registerLocationListener: "
                                        + e.toString());
                    }
                }
            }

            public void unregisterLocationListener(String uuid) {
                synchronized (m_listeners) {
                    IRemoteLocationCallback callback = m_listeners.remove(uuid);
                    if (callback == null)
                        return;

                    callback.asBinder().unlinkToDeath(this, 0);

                    if (m_listeners.size() != 0)
                        return;

                    m_handler.requestUnregisterLocationListener(provider);
                }
            }

            public void onLocationChanged(RemoteLocation location) {
                synchronized (m_listeners) {
                    Collection<IRemoteLocationCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteLocationCallback cb : callbacks) {
                        try {

                            cb.onLocationChanged(location);

                        } catch (RemoteException e) {
                            IwdsLog.e(this, "Exception in onLocationChanged: "
                                    + e.toString());
                        }
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

                    if (m_listeners.size() == 0)
                        m_handler.requestUnregisterLocationListener(provider);
                }
            }

        }

        /* ------------------ RemoteLocationCallback end -------------------- */

        /* ------------------ RemoteLastKnownLocationCallback ---------------- */
        private class RemoteLastKnownLocationCallback implements
                IBinder.DeathRecipient {
            private HashMap<String, IRemoteLocationCallback> m_listeners;
            String provider;

            RemoteLastKnownLocationCallback(String p) {
                m_listeners = new HashMap<String, IRemoteLocationCallback>();
                provider = p;
            }

            public void requestLastKnownLocation(String uuid, String provider,
                    IRemoteLocationCallback callback) {

                synchronized (m_listeners) {
                    try {
                        callback.asBinder().linkToDeath(this, 0);

                        m_listeners.put(uuid, callback);

                        if (m_listeners.size() == 1)
                            m_handler.requestLastKnownLocation(provider);

                    } catch (RemoteException e) {
                        IwdsLog.e(
                                this,
                                "Exception in requestLastKnownLocation: "
                                        + e.toString());
                    }
                }
            }

            public void onLastKnownLocation(RemoteLocation location) {
                synchronized (m_listeners) {
                    Collection<IRemoteLocationCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteLocationCallback cb : callbacks) {
                        try {

                            cb.asBinder().unlinkToDeath(this, 0);

                            cb.onLastKnownLocation(location);

                        } catch (RemoteException e) {
                            IwdsLog.e(
                                    this,
                                    "Exception in onLastKnownLocation: "
                                            + e.toString());
                        }
                    }

                    m_listeners.clear();
                }
            }

            public void unregisterListeners() {
                synchronized (m_listeners) {
                    Collection<IRemoteLocationCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteLocationCallback cb : callbacks)
                        if (cb.asBinder().isBinderAlive())
                            cb.asBinder().unlinkToDeath(this, 0);

                    if (!m_listeners.isEmpty())
                        m_listeners.clear();
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

        /* --------------- RemoteLastKnownLocationCallback end --------------- */

        /* ------------------ RemoteWeatherUpdateCallback -------------------- */
        private class RemoteWeatherUpdateCallback implements
                IBinder.DeathRecipient {
            private HashMap<String, IRemoteWeatherCallback> m_listeners;
            int weatherType;

            RemoteWeatherUpdateCallback(int w) {
                m_listeners = new HashMap<String, IRemoteWeatherCallback>();
                weatherType = w;
            }

            public void requestWeatherUpdate(int weatherType, String uuid,
                    IRemoteWeatherCallback callback) {
                synchronized (m_listeners) {
                    try {
                        callback.asBinder().linkToDeath(this, 0);

                        m_listeners.put(uuid, callback);

                        if (m_listeners.size() == 1)
                            m_handler.requestWeatherUpdate(weatherType);

                    } catch (RemoteException e) {
                        IwdsLog.e(
                                this,
                                "Exception in requestWeatherLive: "
                                        + e.toString());
                    }
                }
            }

            public void onWeatherLiveSearched(RemoteWeatherLive weatherLive) {
                synchronized (m_listeners) {
                    Collection<IRemoteWeatherCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteWeatherCallback cb : callbacks) {
                        try {

                            cb.asBinder().unlinkToDeath(this, 0);

                            cb.onWeatherLiveSearched(weatherLive);

                        } catch (RemoteException e) {
                            IwdsLog.e(
                                    this,
                                    "Exception in onWeatherLiveSearched: "
                                            + e.toString());
                        }
                    }

                    m_listeners.clear();
                }
            }

            public void onWeatherForecastSearched(
                    RemoteWeatherForecast weatherForecast) {
                synchronized (m_listeners) {
                    Collection<IRemoteWeatherCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteWeatherCallback cb : callbacks) {
                        try {
                            cb.asBinder().unlinkToDeath(this, 0);

                            cb.onWeatherForecastSearched(weatherForecast);

                        } catch (RemoteException e) {
                            IwdsLog.e(this,
                                    "Exception in onWeatherForecastSearched: "
                                            + e.toString());
                        }
                    }

                    m_listeners.clear();
                }
            }

            public void unregisterListeners() {
                synchronized (m_listeners) {
                    Collection<IRemoteWeatherCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteWeatherCallback cb : callbacks)
                        if (cb.asBinder().isBinderAlive())
                            cb.asBinder().unlinkToDeath(this, 0);

                    if (!m_listeners.isEmpty())
                        m_listeners.clear();
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

        /* ------------------ RemoteWeatherCallback end -------------------- */

        /* ---------------- RemoteGeoFenceCallback --------------- */
        private class RemoteGeoFenceCallback implements IBinder.DeathRecipient {
            private HashMap<String, IRemoteGeoFenceCallback> m_listeners;

            RemoteGeoFenceCallback() {
                m_listeners = new HashMap<String, IRemoteGeoFenceCallback>();
            }

            public void registerGeoFenceListener(String uuid, double latitude,
                    double longitude, float radius, long expiration,
                    IRemoteGeoFenceCallback callback) {

                synchronized (m_listeners) {

                    try {
                        callback.asBinder().linkToDeath(this, 0);
                        m_listeners.put(uuid, callback);

                        m_handler.requestRegisterGeoFenceListener(uuid,
                                latitude, longitude, radius, expiration);

                    } catch (RemoteException e) {
                        IwdsLog.e(
                                this,
                                "Exception in registerGeoFenceListener: "
                                        + e.toString());
                    }

                }
            }

            public void unregisterGeoFenceListener(String uuid) {
                synchronized (m_listeners) {

                    IRemoteGeoFenceCallback callback = m_listeners.remove(uuid);

                    if (callback == null)
                        return;

                    callback.asBinder().unlinkToDeath(this, 0);

                    m_handler.requestUnregisterGeoFenceListener(uuid);
                }
            }

            public void onGeoFenceAlert(String uuid, int status) {
                synchronized (m_listeners) {
                    IRemoteGeoFenceCallback callback = m_listeners.get(uuid);

                    if (callback == null)
                        return;

                    try {
                        callback.onGeoFenceAlert(status);

                    } catch (RemoteException e) {
                        IwdsLog.e(this,
                                "Exception in onGeoFenceAlert: " + e.toString());
                    }
                }
            }

            @Override
            public void binderDied() {
                synchronized (m_listeners) {
                    Set<String> uuids = m_listeners.keySet();
                    for (Iterator<String> it = uuids.iterator(); it.hasNext();) {
                        String uuid = it.next();
                        if (!m_listeners.get(uuid).asBinder().isBinderAlive()) {
                            m_handler.requestUnregisterGeoFenceListener(uuid);
                            it.remove();
                        }
                    }
                }
            }
        }

        /* ---------------- RemoteGeoFenceCallback end --------------- */

        /* ---------------- RemoteProximityCallback --------------- */
        private class RemoteProximityCallback implements IBinder.DeathRecipient {
            private HashMap<String, IRemoteProximityCallback> m_listeners;

            RemoteProximityCallback() {
                m_listeners = new HashMap<String, IRemoteProximityCallback>();
            }

            public void registerProximityListener(String uuid, double latitude,
                    double longitude, float radius, long expiration,
                    IRemoteProximityCallback callback) {

                synchronized (m_listeners) {

                    try {
                        callback.asBinder().linkToDeath(this, 0);
                        m_listeners.put(uuid, callback);

                        m_handler.requestRegisterProximityListener(uuid,
                                latitude, longitude, radius, expiration);

                    } catch (RemoteException e) {
                        IwdsLog.e(
                                this,
                                "Exception in registerProximityListener: "
                                        + e.toString());
                    }

                }
            }

            public void unregisterProximityListener(String uuid) {
                synchronized (m_listeners) {

                    IRemoteProximityCallback callback = m_listeners
                            .remove(uuid);

                    if (callback == null)
                        return;

                    callback.asBinder().unlinkToDeath(this, 0);

                    m_handler.requestUnregisterProximityListener(uuid);
                }
            }

            public void onProximityAlert(String uuid, int status) {
                synchronized (m_listeners) {
                    IRemoteProximityCallback callback = m_listeners.get(uuid);

                    if (callback == null)
                        return;

                    try {
                        callback.onProximityAlert(status);

                    } catch (RemoteException e) {
                        IwdsLog.e(
                                this,
                                "Exception in onProximityAlert: "
                                        + e.toString());
                    }
                }
            }

            @Override
            public void binderDied() {
                synchronized (m_listeners) {
                    Set<String> uuids = m_listeners.keySet();
                    for (Iterator<String> it = uuids.iterator(); it.hasNext();) {
                        String uuid = it.next();
                        if (!m_listeners.get(uuid).asBinder().isBinderAlive()) {
                            m_handler.requestUnregisterProximityListener(uuid);
                            it.remove();
                        }
                    }
                }
            }
        }

        /* ---------------- RemoteProximityCallback end --------------- */

        /* --------------------- RemoteGpsStatusCallback ----------------- */
        private class RemoteGpsStatusCallback implements IBinder.DeathRecipient {
            private HashMap<String, IRemoteGpsStatusCallback> m_listeners;

            RemoteGpsStatusCallback() {
                m_listeners = new HashMap<String, IRemoteGpsStatusCallback>();
            }

            public void registerGpsStatusListener(String uuid,
                    IRemoteGpsStatusCallback callback) {
                synchronized (m_listeners) {
                    try {
                        callback.asBinder().linkToDeath(this, 0);
                        m_listeners.put(uuid, callback);

                        if (m_listeners.size() == 1)
                            m_handler.requestRegisterGpsListener();

                    } catch (RemoteException e) {
                        IwdsLog.e(
                                this,
                                "Exception in registerGpsStatusListener: "
                                        + e.toString());
                    }

                }
            }

            public void unregisterGpsStatusListener(String uuid) {
                synchronized (m_listeners) {

                    IRemoteGpsStatusCallback callback = m_listeners
                            .remove(uuid);

                    if (callback == null)
                        return;

                    callback.asBinder().unlinkToDeath(this, 0);

                    if (m_listeners.size() != 0)
                        return;

                    m_handler.requestUnregisterGpsStatusListener();
                }
            }

            public void onGpsStatusChanged(int status) {
                synchronized (m_listeners) {
                    Collection<IRemoteGpsStatusCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteGpsStatusCallback cb : callbacks) {
                        try {
                            cb.onGpsStatusChanged(status);

                        } catch (RemoteException e) {
                            IwdsLog.e(this, "Exception in onGpsStatusChanged: "
                                    + e.toString());
                        }
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

                    if (m_listeners.size() == 0)
                        m_handler.requestUnregisterGpsStatusListener();
                }
            }
        }

        /* ---------------- RemoteGpsStatusCallback end-------------------- */

        /* ---------------- RemoteGpsCurrentStatusCallback -------------------- */
        private class RemoteGpsCurrentStatusCallback implements
                IBinder.DeathRecipient {
            private HashMap<String, IRemoteGpsStatusCallback> m_listeners;

            RemoteGpsCurrentStatusCallback() {
                m_listeners = new HashMap<String, IRemoteGpsStatusCallback>();
            }

            public void requestGpsStatus(String uuid,
                    IRemoteGpsStatusCallback callback) {

                synchronized (m_listeners) {
                    try {
                        callback.asBinder().linkToDeath(this, 0);

                        m_listeners.put(uuid, callback);

                        if (m_listeners.size() == 1)
                            m_handler.requestGpsStatus();

                    } catch (RemoteException e) {
                        IwdsLog.e(
                                this,
                                "Exception in requestGpsStatus: "
                                        + e.toString());
                    }
                }
            }

            public void onGpsCurrentStatus(RemoteGpsStatus status) {
                synchronized (m_listeners) {
                    Collection<IRemoteGpsStatusCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteGpsStatusCallback cb : callbacks) {
                        try {
                            cb.onGpsCurrentStatus(status);

                        } catch (RemoteException e) {
                            IwdsLog.e(this, "Exception in onGpsCurrentStatus: "
                                    + e.toString());
                        }
                    }

                    m_listeners.clear();
                }
            }

            public void unregisterListeners() {
                synchronized (m_listeners) {
                    Collection<IRemoteGpsStatusCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteGpsStatusCallback cb : callbacks)
                        if (cb.asBinder().isBinderAlive())
                            cb.asBinder().unlinkToDeath(this, 0);

                    if (!m_listeners.isEmpty())
                        m_listeners.clear();
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

        /* ------------- RemoteGpsCurrentStatusCallback end---------------- */

        /* -------------- RemoteProviderStatusCallback ---------------- */
        private class RemoteProviderStatusCallback implements
                IBinder.DeathRecipient {
            private HashMap<String, IRemoteProviderCallback> m_listeners;
            private String provider;

            RemoteProviderStatusCallback(String p) {
                m_listeners = new HashMap<String, IRemoteProviderCallback>();
                provider = p;
            }

            public void requestProviderStatus(String uuid, String provider,
                    IRemoteProviderCallback callback) {

                synchronized (m_listeners) {
                    try {
                        callback.asBinder().linkToDeath(this, 0);

                        m_listeners.put(uuid, callback);

                        if (m_listeners.size() == 1)
                            m_handler.requestProviderStatus(provider);

                    } catch (RemoteException e) {
                        IwdsLog.e(this, "Exception in requestProviderStatus: "
                                + e.toString());
                    }
                }
            }

            public void onProviderStatus(String provider, boolean enabled) {
                synchronized (m_listeners) {
                    Collection<IRemoteProviderCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteProviderCallback cb : callbacks) {
                        try {

                            cb.asBinder().unlinkToDeath(this, 0);

                            cb.onProviderStatus(enabled, provider);

                        } catch (RemoteException e) {
                            IwdsLog.e(this, "Exception in onProviderStatus: "
                                    + e.toString());
                        }
                    }

                    m_listeners.clear();
                }
            }

            public void unregisterListeners() {
                synchronized (m_listeners) {
                    Collection<IRemoteProviderCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteProviderCallback cb : callbacks)
                        if (cb.asBinder().isBinderAlive())
                            cb.asBinder().unlinkToDeath(this, 0);

                    if (!m_listeners.isEmpty())
                        m_listeners.clear();
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

        /* -------------- RemoteProviderStatusCallback end ------------ */

        /* -------------- RemoteProvidersCallback ---------------- */
        private class RemoteProviderListCallback implements
                IBinder.DeathRecipient {
            private HashMap<String, IRemoteProviderCallback> m_listeners;
            private boolean enabledOnly;

            RemoteProviderListCallback(boolean e) {
                m_listeners = new HashMap<String, IRemoteProviderCallback>();
                enabledOnly = e;
            }

            public void requestProviderList(String uuid, boolean enabledOnly,
                    IRemoteProviderCallback callback) {

                synchronized (m_listeners) {
                    try {
                        callback.asBinder().linkToDeath(this, 0);

                        m_listeners.put(uuid, callback);

                        if (m_listeners.size() == 1)
                            m_handler.requestProviderList(enabledOnly);

                    } catch (RemoteException e) {
                        IwdsLog.e(this, "Exception in requestProviderList: "
                                + e.toString());
                    }
                }
            }

            public void onProviderList(boolean enabledOnly,
                    ArrayList<String> providerList) {
                synchronized (m_listeners) {
                    Collection<IRemoteProviderCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteProviderCallback cb : callbacks) {
                        try {

                            cb.asBinder().unlinkToDeath(this, 0);

                            cb.onProviderList(enabledOnly, providerList);

                        } catch (RemoteException e) {
                            IwdsLog.e(this,
                                    "Exception in onProviders: " + e.toString());
                        }
                    }

                    m_listeners.clear();
                }
            }

            public void unregisterListeners() {
                synchronized (m_listeners) {
                    Collection<IRemoteProviderCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteProviderCallback cb : callbacks)
                        if (cb.asBinder().isBinderAlive())
                            cb.asBinder().unlinkToDeath(this, 0);

                    if (!m_listeners.isEmpty())
                        m_listeners.clear();
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

        /* -------------- RemoteProviderListCallback end ------------ */

        /* ---------------- RemoteNetworkStatusCallback --------------- */
        private class RemoteNetworkStatusCallback implements
                IBinder.DeathRecipient {
            private HashMap<String, IRemoteNetworkStatusCallback> m_listeners;

            RemoteNetworkStatusCallback() {
                m_listeners = new HashMap<String, IRemoteNetworkStatusCallback>();
            }

            public void registerNetworkStatusListener(String uuid,
                    IRemoteNetworkStatusCallback callback) {
                synchronized (m_listeners) {

                    try {
                        callback.asBinder().linkToDeath(this, 0);
                        m_listeners.put(uuid, callback);

                        if (m_listeners.size() == 1)
                            m_handler.requestRegisterNetworkStatusListener();

                    } catch (RemoteException e) {
                        IwdsLog.e(this,
                                "Exception in registerNetworkStatusListener: "
                                        + e.toString());
                    }
                }
            }

            public void unregisterNetworkStatusListener(String uuid) {
                synchronized (m_listeners) {
                    IRemoteNetworkStatusCallback callback = m_listeners
                            .remove(uuid);

                    if (callback == null)
                        return;

                    callback.asBinder().unlinkToDeath(this, 0);

                    if (m_listeners.size() != 0)
                        return;

                    m_handler.requestUnregisterNetworkStatusListener();
                }
            }

            public void onNetworkStatusChanged(int status) {
                synchronized (m_listeners) {
                    Collection<IRemoteNetworkStatusCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteNetworkStatusCallback cb : callbacks) {
                        try {
                            cb.onNetworkStatusChanged(status);

                        } catch (RemoteException e) {
                            IwdsLog.e(
                                    this,
                                    "Exception in onNetworkStatusChanged: "
                                            + e.toString());
                        }
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

                    if (m_listeners.size() == 0)
                        m_handler.requestUnregisterNetworkStatusListener();
                }
            }
        }

        /* ---------------- RemoteNetworkStatusCallback end --------------- */

        /* ------------- RemoteNetworkCurrentStatusCallback end ------------ */
        private class RemoteNetworkCurrentStatusCallback implements
                IBinder.DeathRecipient {
            private HashMap<String, IRemoteNetworkStatusCallback> m_listeners;

            RemoteNetworkCurrentStatusCallback() {
                m_listeners = new HashMap<String, IRemoteNetworkStatusCallback>();
            }

            public void requestNetworkStatus(String uuid,
                    IRemoteNetworkStatusCallback callback) {

                synchronized (m_listeners) {
                    try {
                        callback.asBinder().linkToDeath(this, 0);

                        m_listeners.put(uuid, callback);

                        if (m_listeners.size() == 1)
                            m_handler.requestNetworkStatus();

                    } catch (RemoteException e) {
                        IwdsLog.e(this, "Exception in requestNetworkStatus: "
                                + e.toString());
                    }
                }
            }

            public void onNetworkCurrentStatus(int status) {
                synchronized (m_listeners) {
                    Collection<IRemoteNetworkStatusCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteNetworkStatusCallback cb : callbacks) {
                        try {
                            cb.onNetworkCurrentStatus(status);

                        } catch (RemoteException e) {
                            IwdsLog.e(
                                    this,
                                    "Exception in onNetworkCurrentStatus: "
                                            + e.toString());
                        }
                    }

                    m_listeners.clear();
                }
            }

            public void unregisterListeners() {
                synchronized (m_listeners) {
                    Collection<IRemoteNetworkStatusCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteNetworkStatusCallback cb : callbacks)
                        if (cb.asBinder().isBinderAlive())
                            cb.asBinder().unlinkToDeath(this, 0);

                    if (!m_listeners.isEmpty())
                        m_listeners.clear();
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

        /* ------------- RemoteNetworkCurrentStatusCallback --------------- */

        /* ------------- RemoteLocationService method -------------------- */
        public RemoteLocationServiceStub() {
            m_remoteStatusCallback = new RemoteStatusCallback();
            m_locationCallbacks = new ArrayList<RemoteLocationCallback>();
            m_weatherUpdateCallbacks = new ArrayList<RemoteWeatherUpdateCallback>();
            m_lastKnownLocationCallbacks = new ArrayList<RemoteLastKnownLocationCallback>();
            m_geoFenceCallback = new RemoteGeoFenceCallback();
            m_proximityCallback = new RemoteProximityCallback();
            m_gpsStatusCallback = new RemoteGpsStatusCallback();
            m_networkStatusCallback = new RemoteNetworkStatusCallback();
            m_gpsCurrentStatusCallback = new RemoteGpsCurrentStatusCallback();
            m_networkCurrentStatusCallback = new RemoteNetworkCurrentStatusCallback();
            m_providerStatusCallbacks = new ArrayList<RemoteProviderStatusCallback>();
            m_providerListCallbacks = new ArrayList<RemoteProviderListCallback>();

            m_weatherUpdateCallbacks.add(new RemoteWeatherUpdateCallback(
                    RemoteLocationServiceManager.WEATHER_TYPE_LIVE));
            m_weatherUpdateCallbacks.add(new RemoteWeatherUpdateCallback(
                    RemoteLocationServiceManager.WEATHER_TYPE_FORECAST));

            m_locationCallbacks.add(new RemoteLocationCallback(
                    RemoteLocationServiceManager.GPS_PROVIDER));
            m_locationCallbacks.add(new RemoteLocationCallback(
                    RemoteLocationServiceManager.IWDS_NETWORK_PROVIDER));

            m_lastKnownLocationCallbacks
                    .add(new RemoteLastKnownLocationCallback(
                            RemoteLocationServiceManager.GPS_PROVIDER));
            m_lastKnownLocationCallbacks
                    .add(new RemoteLastKnownLocationCallback(
                            RemoteLocationServiceManager.IWDS_NETWORK_PROVIDER));

            m_providerStatusCallbacks.add(new RemoteProviderStatusCallback(
                    RemoteLocationServiceManager.GPS_PROVIDER));
            m_providerStatusCallbacks.add(new RemoteProviderStatusCallback(
                    RemoteLocationServiceManager.IWDS_NETWORK_PROVIDER));

            m_providerListCallbacks.add(new RemoteProviderListCallback(true));
            m_providerListCallbacks.add(new RemoteProviderListCallback(false));
        }

        public void onRemoteStatusChanged(boolean available) {
            synchronized (m_remoteStatusCallback) {
                m_remoteStatusCallback.onRemoteStatusChanged(available);
            }
        }

        public void onLocationChanged(RemoteLocation location) {
            synchronized (m_locationCallbacks) {
                for (RemoteLocationCallback cb : m_locationCallbacks)
                    if (cb.provider.equals(location.getProvider()))
                        cb.onLocationChanged(location);
            }
        }

        public void onWeatherLiveSearched(RemoteWeatherLive weatherLive) {
            synchronized (m_weatherUpdateCallbacks) {
                for (RemoteWeatherUpdateCallback cb : m_weatherUpdateCallbacks)
                    if (cb.weatherType == RemoteLocationServiceManager.WEATHER_TYPE_LIVE)
                        cb.onWeatherLiveSearched(weatherLive);
            }
        }

        public void onWeatherForecastSearched(
                RemoteWeatherForecast weatherForecast) {
            synchronized (m_weatherUpdateCallbacks) {
                for (RemoteWeatherUpdateCallback cb : m_weatherUpdateCallbacks)
                    if (cb.weatherType == RemoteLocationServiceManager.WEATHER_TYPE_FORECAST)
                        cb.onWeatherForecastSearched(weatherForecast);
            }
        }

        public void onLastKnownLocation(RemoteLocation location) {
            synchronized (m_lastKnownLocationCallbacks) {
                for (RemoteLastKnownLocationCallback cb : m_lastKnownLocationCallbacks)
                    if (cb.provider.equals(location.getProvider()))
                        cb.onLastKnownLocation(location);
            }
        }

        public void onGeoFenceAlert(String uuid, int status) {
            synchronized (m_geoFenceCallback) {
                m_geoFenceCallback.onGeoFenceAlert(uuid, status);
            }
        }

        public void onProximityAlert(String uuid, int status) {
            synchronized (m_proximityCallback) {
                m_proximityCallback.onProximityAlert(uuid, status);
            }
        }

        public void onNetworkStatusChanged(int status) {
            synchronized (m_networkStatusCallback) {
                m_networkStatusCallback.onNetworkStatusChanged(status);
            }
        }

        public void onGpsStatusChanged(int status) {
            synchronized (m_gpsStatusCallback) {
                m_gpsStatusCallback.onGpsStatusChanged(status);
            }
        }

        public void onGpsCurrentStatus(RemoteGpsStatus status) {
            synchronized (m_gpsCurrentStatusCallback) {
                m_gpsCurrentStatusCallback.onGpsCurrentStatus(status);
            }
        }

        public void onNetworkCurrentStatus(int status) {
            synchronized (m_networkCurrentStatusCallback) {
                m_networkCurrentStatusCallback.onNetworkCurrentStatus(status);
            }
        }

        public void onProviderStatus(String provider, boolean enabled) {
            synchronized (m_providerStatusCallbacks) {
                for (RemoteProviderStatusCallback cb : m_providerStatusCallbacks)
                    if (cb.provider.equals(provider))
                        cb.onProviderStatus(provider, enabled);
            }
        }

        public void onProviderList(boolean enabledOnly,
                ArrayList<String> providerList) {
            synchronized (m_providerListCallbacks) {
                for (RemoteProviderListCallback cb : m_providerListCallbacks)
                    if (cb.enabledOnly == enabledOnly)
                        cb.onProviderList(enabledOnly, providerList);
            }
        }

        public void unregisterAllSingleListeners() {
            synchronized (m_weatherUpdateCallbacks) {
                for (RemoteWeatherUpdateCallback cb : m_weatherUpdateCallbacks)
                    cb.unregisterListeners();
            }

            synchronized (m_lastKnownLocationCallbacks) {
                for (RemoteLastKnownLocationCallback cb : m_lastKnownLocationCallbacks)
                    cb.unregisterListeners();
            }

            synchronized (m_gpsCurrentStatusCallback) {
                m_gpsCurrentStatusCallback.unregisterListeners();
            }

            synchronized (m_networkCurrentStatusCallback) {
                m_networkCurrentStatusCallback.unregisterListeners();
            }

            synchronized (m_providerStatusCallbacks) {
                for (RemoteProviderStatusCallback cb : m_providerStatusCallbacks)
                    cb.unregisterListeners();
            }

            synchronized (m_providerListCallbacks) {
                for (RemoteProviderListCallback cb : m_providerListCallbacks) {
                    cb.unregisterListeners();
                }
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
            m_handler.requestLocationServiceStatus();
        }

        @Override
        public void unregisterRemoteStatusListener(String uuid)
                throws RemoteException {
            synchronized (m_remoteStatusCallback) {
                m_remoteStatusCallback.unregisterRemoteStatusListener(uuid);
            }
        }

        @Override
        public void requestLastKnownLocation(String uuid, String provider,
                IRemoteLocationCallback callback) throws RemoteException {
            synchronized (m_lastKnownLocationCallbacks) {
                for (RemoteLastKnownLocationCallback cb : m_lastKnownLocationCallbacks)
                    if (cb.provider.equals(provider))
                        cb.requestLastKnownLocation(uuid, provider, callback);
            }
        }

        @Override
        public void requestWeatherUpdate(int weatherType, String uuid,
                IRemoteWeatherCallback callback) throws RemoteException {
            synchronized (m_weatherUpdateCallbacks) {
                for (RemoteWeatherUpdateCallback cb : m_weatherUpdateCallbacks)
                    if (cb.weatherType == weatherType)
                        cb.requestWeatherUpdate(weatherType, uuid, callback);
            }
        }

        @Override
        public void registerLocationListener(String uuid, String provider,
                IRemoteLocationCallback callback) throws RemoteException {
            synchronized (m_locationCallbacks) {

                for (RemoteLocationCallback cb : m_locationCallbacks) {
                    if (cb.provider.equals(provider)) {
                        // TODO: prefix minTime and minDistance ?
                        if (cb.provider
                                .equals(RemoteLocationServiceManager.GPS_PROVIDER))
                            cb.registerLocationListener(uuid, provider, 1000,
                                    1, callback);
                        else {
                            cb.registerLocationListener(uuid, provider, 2000,
                                    5, callback);
                        }
                    }
                }
            }
        }

        @Override
        public void unregisterLocationListener(String uuid)
                throws RemoteException {
            synchronized (m_locationCallbacks) {
                for (RemoteLocationCallback cb : m_locationCallbacks)
                    cb.unregisterLocationListener(uuid);

            }
        }

        @Override
        public void registerGeoFenceListener(String uuid, double latitude,
                double longitude, float radius, long expiration,
                IRemoteGeoFenceCallback callback) throws RemoteException {
            synchronized (m_geoFenceCallback) {
                m_geoFenceCallback.registerGeoFenceListener(uuid, latitude,
                        longitude, radius, expiration, callback);
            }
        }

        @Override
        public void unregisterGeoFenceListener(String uuid)
                throws RemoteException {
            synchronized (m_geoFenceCallback) {
                m_geoFenceCallback.unregisterGeoFenceListener(uuid);
            }
        }

        @Override
        public void registerProximityListener(String uuid, double latitude,
                double longitude, float radius, long expiration,
                IRemoteProximityCallback callback) throws RemoteException {
            synchronized (m_proximityCallback) {
                m_proximityCallback.registerProximityListener(uuid, latitude,
                        longitude, radius, expiration, callback);
            }
        }

        @Override
        public void unregisterProximityListener(String uuid)
                throws RemoteException {
            synchronized (m_proximityCallback) {
                m_proximityCallback.unregisterProximityListener(uuid);
            }
        }

        @Override
        public void registerGpsStatusListener(String uuid,
                IRemoteGpsStatusCallback callback) throws RemoteException {
            synchronized (m_gpsStatusCallback) {
                m_gpsStatusCallback.registerGpsStatusListener(uuid, callback);
            }
        }

        @Override
        public void unregisterGpsStatusListener(String uuid)
                throws RemoteException {
            synchronized (m_gpsStatusCallback) {
                m_gpsStatusCallback.unregisterGpsStatusListener(uuid);
            }
        }

        @Override
        public void registerNetworkStatusListener(String uuid,
                IRemoteNetworkStatusCallback callback) throws RemoteException {
            synchronized (m_networkStatusCallback) {
                m_networkStatusCallback.registerNetworkStatusListener(uuid,
                        callback);
            }
        }

        @Override
        public void unregisterNetworkStatusListener(String uuid)
                throws RemoteException {
            synchronized (m_networkStatusCallback) {
                m_networkStatusCallback.unregisterNetworkStatusListener(uuid);
            }
        }

        @Override
        public void requestGpsStatus(String uuid,
                IRemoteGpsStatusCallback callback) throws RemoteException {
            synchronized (m_gpsCurrentStatusCallback) {
                m_gpsCurrentStatusCallback.requestGpsStatus(uuid, callback);
            }

        }

        @Override
        public void requestGpsEnable(boolean enable) throws RemoteException {

        }

        @Override
        public void requestProviderStatus(String uuid, String provider,
                IRemoteProviderCallback callback) throws RemoteException {
            synchronized (m_providerStatusCallbacks) {
                for (RemoteProviderStatusCallback cb : m_providerStatusCallbacks)
                    if (cb.provider.equals(provider))
                        cb.requestProviderStatus(uuid, provider, callback);
            }
        }

        @Override
        public void requestProviderList(String uuid, boolean enabledOnly,
                IRemoteProviderCallback callback) throws RemoteException {
            synchronized (m_providerListCallbacks) {
                for (RemoteProviderListCallback cb : m_providerListCallbacks)
                    if (cb.enabledOnly == enabledOnly)
                        cb.requestProviderList(uuid, enabledOnly, callback);
            }
        }

        @Override
        public void requestNetworkStatus(String uuid,
                IRemoteNetworkStatusCallback callback) throws RemoteException {
            synchronized (m_networkCurrentStatusCallback) {
                m_networkCurrentStatusCallback.requestNetworkStatus(uuid,
                        callback);
            }
        }
        /* ------------- RemoteLocationService method -------------------- */
    }

    private class ServiceHandler extends Handler {
        private final static int MSG_CHANNEL_STATUS_CHANGED = 0;
        private final static int MSG_REQUEST_REGISTER_LOCATION_LISTENER = 1;
        private final static int MSG_REQUEST_UNREGISTER_LOCATION_LISTENER = 2;
        private final static int MSG_REQUEST_WEATHER_UPDATE = 3;
        private final static int MSG_REQUEST_LAST_KNOWN_LOCATION = 5;
        private final static int MSG_REQUEST_REGISTER_GEOFENCE_LISTENER = 6;
        private final static int MSG_REQUEST_UNREGISTER_GEOFENCE_LISTENER = 7;
        private final static int MSG_REQUEST_REGISTER_GPS_STATUS_LISTENER = 8;
        private final static int MSG_REQUEST_UNREGISTER_GPS_STATUS_LISTENER = 9;
        private final static int MSG_REQUEST_REGISTER_NETWORK_STATUS_LISTENER = 10;
        private final static int MSG_REQUEST_UNREGISTER_NETWORK_STATUS_LISTENER = 11;
        private final static int MSG_REQUEST_LOCATION_SERVICE_STATUS = 12;
        private final static int MSG_REQUEST_GPS_STATUS = 13;
        private final static int MSG_REQUEST_NETWORK_STATUS = 14;
        private final static int MSG_REQUEST_PROVIDER_LIST = 15;
        private final static int MSG_REQUEST_PROVIDER_STATUS = 16;
        private final static int MSG_REQUEST_GPS_ENABLE = 17;
        private final static int MSG_RESPONSE_WEATHER_LIVE = 18;
        private final static int MSG_RESPONSE_WEATHER_FORECAST = 19;
        private final static int MSG_RESPONSE_LOCATION_CHANGED = 20;
        private final static int MSG_RESPONSE_LAST_KNOWN_LOCATION = 21;
        private final static int MSG_RESPONSE_GEOFENCE_ALERT = 22;
        private final static int MSG_RESPONSE_NETWORK_STATUS_CHANGED = 23;
        private final static int MSG_RESPONSE_GPS_STATUS_CHANGED = 24;
        private final static int MSG_RESPONSE_LOCATION_SERVICE_CONNECTED = 25;
        private final static int MSG_RESPONSE_GPS_STATUS = 26;
        private final static int MSG_RESPONSE_NETWORK_STATUS = 27;
        private final static int MSG_RESPONSE_PROVIDER_LIST = 28;
        private final static int MSG_RESPONSE_PROVIDER_STATUS = 29;
        private final static int MSG_REQUEST_REGISTER_PROXIMITY_LISTENER = 30;
        private final static int MSG_REQUEST_UNREGISTER_PROXIMITY_LISTENER = 31;
        private final static int MSG_RESPONSE_PROXIMITY_ALERT = 32;
        private final static int MSG_SEND_RESULT = 40;

        private boolean m_serviceConnected;

        public void setChannelAvailable(boolean available) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_CHANNEL_STATUS_CHANGED;
            msg.arg1 = available ? 1 : 0;

            msg.sendToTarget();
        }

        public void broadcastRemoteServiceStatus(boolean available) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_RESPONSE_LOCATION_SERVICE_CONNECTED;

            msg.arg1 = available ? 1 : 0;

            msg.sendToTarget();
        }

        public void handleSendResult(boolean isOk) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_SEND_RESULT;
            msg.arg1 = isOk ? 1 : 0;

            msg.sendToTarget();

        }

        public void requestGpsStatus() {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_GPS_STATUS;

            msg.sendToTarget();
        }

        public void requestNetworkStatus() {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_NETWORK_STATUS;

            msg.sendToTarget();
        }

        public void requestProviderStatus(String provider) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_PROVIDER_STATUS;

            msg.obj = provider;

            msg.sendToTarget();
        }

        public void requestProviderList(boolean enabledOnly) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_PROVIDER_LIST;

            msg.arg1 = enabledOnly ? 1 : 0;

            msg.sendToTarget();
        }

        public void requestGpsEnable(boolean enable) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_GPS_ENABLE;

            msg.arg1 = enable ? 1 : 0;

            msg.sendToTarget();
        }

        public void requestLocationServiceStatus() {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_LOCATION_SERVICE_STATUS;

            msg.sendToTarget();
        }

        public void requestLastKnownLocation(String provider) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_LAST_KNOWN_LOCATION;
            msg.obj = provider;

            msg.sendToTarget();
        }

        public void requestRegisterLocationListener(String provider,
                long minTime, float minDistance) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            bundle.putString("provider", provider);
            bundle.putLong("minTime", minTime);
            bundle.putFloat("minDistance", minDistance);

            msg.what = MSG_REQUEST_REGISTER_LOCATION_LISTENER;
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void requestUnregisterLocationListener(String provider) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_UNREGISTER_LOCATION_LISTENER;
            msg.obj = provider;

            msg.sendToTarget();
        }

        public void requestRegisterGeoFenceListener(String uuid,
                double latitude, double longitude, float radius, long expiration) {

            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            bundle.putString("uuid", uuid);
            bundle.putDouble("latitude", latitude);
            bundle.putDouble("longitude", longitude);
            bundle.putFloat("radius", radius);
            bundle.putLong("expiration", expiration);

            msg.what = MSG_REQUEST_REGISTER_GEOFENCE_LISTENER;
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void requestUnregisterGeoFenceListener(String uuid) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            bundle.putString("uuid", uuid);

            msg.what = MSG_REQUEST_UNREGISTER_GEOFENCE_LISTENER;
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void requestRegisterProximityListener(String uuid,
                double latitude, double longitude, float radius, long expiration) {

            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            bundle.putString("uuid", uuid);
            bundle.putDouble("latitude", latitude);
            bundle.putDouble("longitude", longitude);
            bundle.putFloat("radius", radius);
            bundle.putLong("expiration", expiration);

            msg.what = MSG_REQUEST_REGISTER_PROXIMITY_LISTENER;
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void requestRegisterGpsListener() {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_REGISTER_GPS_STATUS_LISTENER;

            msg.sendToTarget();
        }

        public void requestUnregisterProximityListener(String uuid) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            bundle.putString("uuid", uuid);

            msg.what = MSG_REQUEST_UNREGISTER_PROXIMITY_LISTENER;
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void requestUnregisterGpsStatusListener() {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_UNREGISTER_GPS_STATUS_LISTENER;

            msg.sendToTarget();
        }

        public void requestRegisterNetworkStatusListener() {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_REGISTER_NETWORK_STATUS_LISTENER;

            msg.sendToTarget();
        }

        public void requestUnregisterNetworkStatusListener() {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_UNREGISTER_NETWORK_STATUS_LISTENER;

            msg.sendToTarget();
        }

        public void requestWeatherUpdate(int weatherType) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_WEATHER_UPDATE;
            msg.arg1 = weatherType;

            msg.sendToTarget();
        }

        public void handleResponse(RemoteLocationResponse response) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            switch (response.type) {
            case RemoteLocationResponse.TYPE_LOCATION_SERVICE_STATUS:
                msg.what = MSG_RESPONSE_LOCATION_SERVICE_CONNECTED;

                msg.arg1 = response.serviceConnected ? 1 : 0;

                break;

            case RemoteLocationResponse.TYPE_LOCATION_CHANGED:
                msg.what = MSG_RESPONSE_LOCATION_CHANGED;

                msg.obj = response.location;

                break;

            case RemoteLocationResponse.TYPE_WEATHER_LIVE:
                msg.what = MSG_RESPONSE_WEATHER_LIVE;

                msg.obj = response.weatherLive;

                break;

            case RemoteLocationResponse.TYPE_WEATHER_FORECAST:
                msg.what = MSG_RESPONSE_WEATHER_FORECAST;

                msg.obj = response.weatherForecast;

                break;

            case RemoteLocationResponse.TYPE_GEOFENCE_ALERT:
                msg.what = MSG_RESPONSE_GEOFENCE_ALERT;

                bundle.putString("uuid", response.uuid);
                bundle.putInt("alert", response.geofenceAlertState);

                msg.setData(bundle);

                break;

            case RemoteLocationResponse.TYPE_PROXIMITY_ALERT:
                msg.what = MSG_RESPONSE_PROXIMITY_ALERT;

                bundle.putString("uuid", response.uuid);
                bundle.putInt("alert", response.proximityAlertState);

                msg.setData(bundle);

                break;

            case RemoteLocationResponse.TYPE_LAST_KNOWN_LOCATION:
                msg.what = MSG_RESPONSE_LAST_KNOWN_LOCATION;

                msg.obj = response.location;

                break;

            case RemoteLocationResponse.TYPE_NETWORK_STATUS_CHANGED:
                msg.what = MSG_RESPONSE_NETWORK_STATUS_CHANGED;

                msg.arg1 = response.networkState;

                break;

            case RemoteLocationResponse.TYPE_GPS_STATUS_CHANGED:
                msg.what = MSG_RESPONSE_GPS_STATUS_CHANGED;

                msg.arg1 = response.gpsEvent;

                break;

            case RemoteLocationResponse.TYPE_GPS_STATUS:
                msg.what = MSG_RESPONSE_GPS_STATUS;

                msg.obj = response.gpsStatus;

                break;

            case RemoteLocationResponse.TYPE_NETWORK_STATUS:
                msg.what = MSG_RESPONSE_NETWORK_STATUS;

                msg.arg1 = response.networkState;

                break;

            case RemoteLocationResponse.TYPE_PROVIDER_STATUS:
                msg.what = MSG_RESPONSE_PROVIDER_STATUS;

                msg.arg1 = response.enabled ? 1 : 0;
                msg.obj = response.provider;

                break;

            case RemoteLocationResponse.TYPE_PROVIDERS:
                msg.what = MSG_RESPONSE_PROVIDER_LIST;

                msg.arg1 = response.enabledOnly ? 1 : 0;
                msg.obj = response.providerList;

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

            RemoteLocationRequest request = RemoteLocationRequest
                    .obtain(m_transactor);
            Bundle bundle = msg.getData();

            String uuid = bundle.getString("uuid");

            switch (msg.what) {
            case MSG_RESPONSE_LOCATION_SERVICE_CONNECTED:
                m_serviceConnected = msg.arg1 == 1 ? true : false;

                IwdsLog.i(this, "Location service connected: "
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

            case MSG_REQUEST_LOCATION_SERVICE_STATUS:
                if (!m_serviceConnected) {
                    IwdsLog.i(this,
                            "Location service on remote device not connected yet");

                    broadcastRemoteServiceStatus(false);

                } else {
                    IwdsLog.d(this,
                            "Location service on remote device already connected");
                    broadcastRemoteServiceStatus(true);
                }

                break;

            case MSG_SEND_RESULT:
                boolean isOk = msg.arg1 == 1 ? true : false;

                if (!isOk)
                    m_service.unregisterAllSingleListeners();

                break;

            case MSG_REQUEST_REGISTER_LOCATION_LISTENER:
                request.type = RemoteLocationRequest.TYPE_REGISTER_LOCATION_LISTENER;
                request.provider = bundle.getString("provider");
                request.minTime = bundle.getLong("minTime");
                request.minDistance = bundle.getFloat("minDistance");

                IwdsLog.d(this,
                        "Try to request register location listener: provider="
                                + request.provider + " ,minTime="
                                + request.minTime + ", minDistance="
                                + request.minDistance);

                request.sendToRemote();

                break;

            case MSG_REQUEST_UNREGISTER_LOCATION_LISTENER:
                request.type = RemoteLocationRequest.TYPE_UNREGISTER_LOCATION_LISTENER;
                request.provider = (String) msg.obj;

                IwdsLog.d(this, "Try to request unregister location listener: "
                        + request.provider);

                request.sendToRemote();

                break;

            case MSG_REQUEST_WEATHER_UPDATE:
                if (!m_serviceConnected) {
                    handleSendResult(false);
                    return;
                }

                request.type = RemoteLocationRequest.TYPE_WEATHER_UPDATE;
                request.weatherType = msg.arg1;

                IwdsLog.d(this, "Try to request weather update: type="
                        + msg.arg1);

                request.sendToRemote();

                break;

            case MSG_REQUEST_LAST_KNOWN_LOCATION:
                if (!m_serviceConnected) {
                    handleSendResult(false);
                    return;
                }

                request.type = RemoteLocationRequest.TYPE_LAST_KNOWN_LOCATION;
                request.provider = (String) msg.obj;

                IwdsLog.d(this, "Try to request last known location: provider="
                        + request.provider);

                request.sendToRemote();

                break;

            case MSG_REQUEST_REGISTER_GEOFENCE_LISTENER:

                request.type = RemoteLocationRequest.TYPE_REGISTER_GEOFENCE_LISTENER;
                request.uuid = uuid;
                request.latitude = bundle.getDouble("latitude");
                request.longitude = bundle.getDouble("longitude");
                request.radius = bundle.getFloat("radius");
                request.expiration = bundle.getLong("expiration");

                IwdsLog.d(this,
                        "Try to request register geofence listener: latitude="
                                + request.latitude + " ,longtitude="
                                + request.longitude + ", radius="
                                + request.radius + ", expiration="
                                + request.expiration);

                request.sendToRemote();

                break;

            case MSG_REQUEST_UNREGISTER_GEOFENCE_LISTENER:

                request.type = RemoteLocationRequest.TYPE_UNREGISTER_GEOFENCE_LISTENER;
                request.uuid = uuid;

                IwdsLog.d(this,
                        "Try to request unregister geofence listener: uuid="
                                + uuid);

                request.sendToRemote();

                break;

            case MSG_REQUEST_REGISTER_GPS_STATUS_LISTENER:
                request.type = RemoteLocationRequest.TYPE_REGISTER_GPS_STATUS_LISTENER;

                IwdsLog.d(this, "Try to request register GPS status listener");

                request.sendToRemote();
                break;

            case MSG_REQUEST_REGISTER_PROXIMITY_LISTENER:
                request.type = RemoteLocationRequest.TYPE_REGISTER_PROXIMITY_LISTENER;
                request.uuid = uuid;
                request.latitude = bundle.getDouble("latitude");
                request.longitude = bundle.getDouble("longitude");
                request.radius = bundle.getFloat("radius");
                request.expiration = bundle.getLong("expiration");

                IwdsLog.d(this,
                        "Try to request register proximity listener: latitude="
                                + request.latitude + " ,longtitude="
                                + request.longitude + ", radius="
                                + request.radius + ", expiration="
                                + request.expiration);

                request.sendToRemote();
                break;

            case MSG_REQUEST_UNREGISTER_PROXIMITY_LISTENER:
                request.type = RemoteLocationRequest.TYPE_UNREGISTER_PROXIMITY_LISTENER;
                request.uuid = uuid;

                IwdsLog.d(this,
                        "Try to request unregister proximity listener: uuid="
                                + uuid);

                request.sendToRemote();
                break;

            case MSG_REQUEST_UNREGISTER_GPS_STATUS_LISTENER:

                request.type = RemoteLocationRequest.TYPE_UNREGISTER_GPS_STATUS_LISTENER;

                IwdsLog.d(this, "Try to request unregister GPS status listener");

                request.sendToRemote();
                break;

            case MSG_REQUEST_REGISTER_NETWORK_STATUS_LISTENER:
                request.type = RemoteLocationRequest.TYPE_REGISTER_NETWORK_STATUS_LISTENER;

                IwdsLog.d(this,
                        "Try to request register network status listener");

                request.sendToRemote();
                break;

            case MSG_REQUEST_UNREGISTER_NETWORK_STATUS_LISTENER:

                request.type = RemoteLocationRequest.TYPE_UNREGISTER_NETWORK_STATUS_LISTENER;

                IwdsLog.d(this,
                        "Try to request unregister network status listener");

                request.sendToRemote();
                break;

            case MSG_REQUEST_GPS_STATUS:
                if (!m_serviceConnected) {
                    handleSendResult(false);
                    return;
                }

                request.type = RemoteLocationRequest.TYPE_GPS_STATUS;

                IwdsLog.d(this, "Try to request GPS status");

                request.sendToRemote();
                break;

            case MSG_REQUEST_NETWORK_STATUS:
                if (!m_serviceConnected) {
                    handleSendResult(false);
                    return;
                }

                request.type = RemoteLocationRequest.TYPE_NETWORK_STATUS;

                IwdsLog.d(this, "Try to request network current status");

                request.sendToRemote();
                break;

            case MSG_REQUEST_PROVIDER_STATUS:
                if (!m_serviceConnected) {
                    handleSendResult(false);
                    return;
                }

                request.type = RemoteLocationRequest.TYPE_PROVIDER_STATUS;
                request.provider = (String) msg.obj;

                IwdsLog.d(this, "Try to request query provider enabled: "
                        + request.provider);

                request.sendToRemote();
                break;

            case MSG_REQUEST_PROVIDER_LIST:
                if (!m_serviceConnected) {
                    handleSendResult(false);
                    return;
                }

                request.type = RemoteLocationRequest.TYPE_PROVIDER_LIST;
                request.enabledOnly = msg.arg1 == 1 ? true : false;

                IwdsLog.d(this, "Try to request all provider enableOnly: "
                        + request.enabledOnly);

                request.sendToRemote();
                break;

            case MSG_REQUEST_GPS_ENABLE:
                if (!m_serviceConnected) {
                    handleSendResult(false);
                    return;
                }

                request.type = RemoteLocationRequest.TYPE_GPS_ENABLE;
                request.enabled = msg.arg1 == 1 ? true : false;

                IwdsLog.d(this, "Try to request set GPS enabled: "
                        + request.enabled);

                request.sendToRemote();
                break;

            case MSG_RESPONSE_LOCATION_CHANGED:

                m_service.onLocationChanged((RemoteLocation) msg.obj);

                break;

            case MSG_RESPONSE_WEATHER_LIVE:

                m_service.onWeatherLiveSearched((RemoteWeatherLive) msg.obj);

                break;

            case MSG_RESPONSE_WEATHER_FORECAST:

                m_service
                        .onWeatherForecastSearched((RemoteWeatherForecast) msg.obj);

                break;

            case MSG_RESPONSE_GEOFENCE_ALERT:

                m_service.onGeoFenceAlert(uuid, bundle.getInt("alert"));

                break;

            case MSG_RESPONSE_PROXIMITY_ALERT:
                m_service.onProximityAlert(uuid, bundle.getInt("alert"));
                break;

            case MSG_RESPONSE_LAST_KNOWN_LOCATION:

                m_service.onLastKnownLocation((RemoteLocation) msg.obj);
                break;

            case MSG_RESPONSE_NETWORK_STATUS_CHANGED:

                m_service.onNetworkStatusChanged(msg.arg1);
                break;

            case MSG_RESPONSE_GPS_STATUS_CHANGED:

                m_service.onGpsStatusChanged(msg.arg1);
                break;

            case MSG_RESPONSE_GPS_STATUS:

                m_service.onGpsCurrentStatus((RemoteGpsStatus) msg.obj);

                break;

            case MSG_RESPONSE_NETWORK_STATUS:

                m_service.onNetworkCurrentStatus(msg.arg1);

                break;

            case MSG_RESPONSE_PROVIDER_STATUS:

                m_service.onProviderStatus((String) msg.obj,
                        msg.arg1 == 1 ? true : false);
                break;

            case MSG_RESPONSE_PROVIDER_LIST:
                m_service.onProviderList(msg.arg1 == 1 ? true : false,
                        (ArrayList<String>) msg.obj);
                break;
            }
        }
    }

    /* ---------------------- DataTransactorCallback -------------------------- */
    private DataTransactorCallback m_transportCallback = new DataTransactorCallback() {

        @Override
        public void onLinkConnected(DeviceDescriptor descriptor,
                boolean isConnected) {
            // do not care
        }

        @Override
        public void onChannelAvailable(boolean isAvailable) {
            if (!isAvailable)
                m_handler.setChannelAvailable(isAvailable);
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
            if (object instanceof RemoteLocationResponse)
                m_handler.handleResponse((RemoteLocationResponse) object);
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
    /*------------------ DataTransactorCallback end ---------------------- */
}
