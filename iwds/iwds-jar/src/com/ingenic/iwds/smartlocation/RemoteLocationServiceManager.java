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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.smartlocation.RemoteGpsStatus.RemoteGpsStatusListener;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

public class RemoteLocationServiceManager extends ServiceManagerContext {
    /**
     * 描述 GPS 定位方式的常量
     */
    public static final String GPS_PROVIDER = "gps";

    /**
     * 描述网络定位方式的常量
     */
    public static final String IWDS_NETWORK_PROVIDER = "lbs";

    /**
     * 描述实况天气的常量
     */
    public static final int WEATHER_TYPE_LIVE = 1;

    /**
     * 描述天气预报的常量
     */
    public static final int WEATHER_TYPE_FORECAST = 2;

    /**
     * 描述网络状态为不可用的常量
     */
    public static final int NETWORK_STATUS_UNAVAILABLE = 0;

    /**
     * 描述网络状态为可用的常量
     */
    public static final int NETWORK_STATUS_AVAILABLE = 1;

    /**
     * 描述离开地理围栏的常量
     */
    public static final int GEOFENCE_STATUS_NON_ALERT = 0;

    /**
     * 描述进入地理围栏的常量
     */
    public static final int GEOFENCE_STATUS_ALERT = 1;

    /**
     * 描述进入警戒区域的常量
     */
    public static final int PROXIMITY_STATUS_ALERT = 1;

    private IRemoteLocationService m_service;
    private boolean m_remoteAvaliable;

    private HashMap<RemoteStatusListener, RemoteStatusCallback> m_remoteStatusListeners;
    private HashMap<RemoteLocationListener, ArrayList<RemoteLocationCallback>> m_locationListeners;
    private HashMap<RemoteWeatherListener, ArrayList<RemoteWeatherUpdateCallback>> m_weatherUpdateListeners;
    private HashMap<RemoteLocationListener, ArrayList<RemoteLocationCallback>> m_lastKnownLocationListeners;
    private HashMap<RemoteLocationListener, ArrayList<RemoteLocationCallback>> m_currentLocationListeners;
    private HashMap<RemoteGeoFenceListener, RemoteGeoFenceCallback> m_geofenceListeners;
    private HashMap<RemoteProximityListener, RemoteProximityCallback> m_proximityListeners;
    private HashMap<RemoteGpsStatusListener, RemoteGpsStatusCallback> m_gpsStatusListeners;
    private HashMap<RemoteNetworkStatusListener, RemoteNetworkStatusCallback> m_networkStatusListeners;
    private HashMap<RemoteGpsStatusListener, RemoteGpsStatusCallback> m_gpsCurrentStatusListeners;
    private HashMap<RemoteNetworkStatusListener, RemoteNetworkStatusCallback> m_networkCurrentStatusListeners;
    private HashMap<RemoteProviderListener, ArrayList<RemoteProviderCallback>> m_providerStatusListeners;
    private HashMap<RemoteProviderListener, ArrayList<RemoteProviderCallback>> m_providerListListeners;

    /**
     * 不要直接构造, 构造 {@link com.ingenic.iwds.common.api.ServiceClient
     * ServiceClient} 时会自动构造
     */
    public RemoteLocationServiceManager(Context context) {
        super(context);

        m_locationListeners = new HashMap<RemoteLocationListener, ArrayList<RemoteLocationCallback>>();
        m_weatherUpdateListeners = new HashMap<RemoteWeatherListener, ArrayList<RemoteWeatherUpdateCallback>>();
        m_lastKnownLocationListeners = new HashMap<RemoteLocationListener, ArrayList<RemoteLocationCallback>>();
        m_currentLocationListeners = new HashMap<RemoteLocationListener, ArrayList<RemoteLocationCallback>>();
        m_geofenceListeners = new HashMap<RemoteGeoFenceListener, RemoteGeoFenceCallback>();
        m_proximityListeners = new HashMap<RemoteProximityListener, RemoteProximityCallback>();
        m_gpsStatusListeners = new HashMap<RemoteGpsStatusListener, RemoteGpsStatusCallback>();
        m_networkStatusListeners = new HashMap<RemoteNetworkStatusListener, RemoteNetworkStatusCallback>();
        m_remoteStatusListeners = new HashMap<RemoteStatusListener, RemoteStatusCallback>();
        m_gpsCurrentStatusListeners = new HashMap<RemoteGpsStatusListener, RemoteGpsStatusCallback>();
        m_networkCurrentStatusListeners = new HashMap<RemoteNetworkStatusListener, RemoteNetworkStatusCallback>();
        m_providerStatusListeners = new HashMap<RemoteProviderListener, ArrayList<RemoteProviderCallback>>();
        m_providerListListeners = new HashMap<RemoteProviderListener, ArrayList<RemoteProviderCallback>>();

        m_serviceClientProxy = new ServiceClientProxy() {

            @Override
            public void onServiceConnected(IBinder binder) {
                m_service = IRemoteLocationService.Stub.asInterface(binder);
            }

            @Override
            public void onServiceDisconnected(boolean unexpected) {
                unregisterAllListeners();
            }

            @Override
            public IBinder getBinder() {
                return m_service.asBinder();
            }
        };
    }

    /* --------------------- RemoteStatusCallback -------------------------- */
    private class RemoteStatusCallback extends IRemoteStatusCallback.Stub {
        private final static int MSG_REMOTE_STATUS_CHANGED = 0;

        private RemoteStatusListener m_listener;
        private boolean status;
        String uuid;

        RemoteStatusCallback(RemoteStatusListener listener) {
            m_listener = listener;
            uuid = UUID.randomUUID().toString();
        }

        private Handler m_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_REMOTE_STATUS_CHANGED:
                    m_remoteAvaliable = (Boolean) msg.obj;

                    if (!m_remoteAvaliable)
                        unregisterAllListeners();

                    if (m_remoteAvaliable != status)
                        m_listener.onAvailable(m_remoteAvaliable);

                    status = m_remoteAvaliable;

                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");
                }
            }

        };

        @Override
        public void onAvailable(boolean available) throws RemoteException {
            Message.obtain(m_handler, MSG_REMOTE_STATUS_CHANGED, available)
                    .sendToTarget();
        }
    }

    /**
     * 用于注册远端设备定位服务状态的监听器
     * {@link com.ingenic.iwds.smartlocation.RemoteStatusListener
     * RemoteStatusListener}
     *
     * @param listener
     *            监听器对象
     *            {@link com.ingenic.iwds.smartlocation.RemoteStatusListener
     *            RemoteStatusListener}
     *
     * @return true 注册成功, false 注册失败
     */
    public boolean registerRemoteStatusListener(RemoteStatusListener listener) {
        IwdsAssert.dieIf(this, listener == null, "Listener is null");

        RemoteStatusCallback callback = m_remoteStatusListeners.get(listener);
        if (callback != null) {
            IwdsAssert.dieIf(this, true, "Unable to register listener: "
                    + "Did you forget to call unregisterRemoteStatusListener?");
            return false;

        } else {
            callback = new RemoteStatusCallback(listener);
            m_remoteStatusListeners.put(listener, callback);
        }

        try {
            m_service.registerRemoteStatusListener(callback.uuid, callback);

        } catch (RemoteException e) {
            IwdsLog.e(
                    this,
                    "Exception in registerRemoteStatusListener: "
                            + e.toString());
            return false;
        }

        return true;
    }

    /**
     * 注销远端设备定位服务状态的监听器
     * {@link com.ingenic.iwds.smartlocation.RemoteStatusListener
     * RemoteStatusListener}
     * 
     * @param listener
     *            监听器对象
     *            {@link com.ingenic.iwds.smartlocation.RemoteStatusListener
     *            RemoteStatusListener}
     */
    public void unregisterRemoteStatusListener(RemoteStatusListener listener) {
        IwdsAssert.dieIf(this, listener == null, "Listener is null");

        RemoteStatusCallback callback = m_remoteStatusListeners.get(listener);
        if (callback == null)
            return;

        try {
            m_service.unregisterRemoteStatusListener(callback.uuid);

        } catch (RemoteException e) {
            IwdsLog.e(
                    this,
                    "Exception in unregisterRemoteStatusListener: "
                            + e.toString());
        }

        m_remoteStatusListeners.remove(listener);
    }

    /* -------------------- RemoteStatusCallback end ----------------------- */

    /* --------------------- GeoFence alert callback -------------------- */
    private class RemoteGeoFenceCallback extends IRemoteGeoFenceCallback.Stub {
        private static final int MSG_GEOFENCE_ALERT = 0;

        private RemoteGeoFenceListener m_listener;
        String uuid;

        RemoteGeoFenceCallback(RemoteGeoFenceListener listener) {
            m_listener = listener;
            uuid = UUID.randomUUID().toString();
        }

        private Handler m_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_GEOFENCE_ALERT:
                    m_listener.onGeoFenceAlert(msg.arg1);
                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");
                }
            }
        };

        @Override
        public void onGeoFenceAlert(int status) throws RemoteException {
            Message.obtain(m_handler, MSG_GEOFENCE_ALERT, status, 0)
                    .sendToTarget();
        }
    }

    /**
     * 注册地理围栏监听器{@link com.ingenic.iwds.smartlocation.RemoteGeoFenceListener
     * RemoteGeoFenceListener}, 根据给定的经纬度和半径，当设备进入或从区域中离开时注册的监听器将被回调一次,
     * 此方法需要与定位请求方法同时使用.
     * 
     * @param latitude
     *            警戒区域中心点的纬度
     * @param longitude
     *            警戒区域中心点的经度
     * @param radius
     *            警戒区域的半径，单位为米
     * @param expiration
     *            警戒时间，单位为毫秒，-1 表示没有限制
     * @param listener
     *            监听器对象
     *            {@link com.ingenic.iwds.smartlocation.RemoteGeoFenceListener
     *            RemoteGeoFenceListener}
     * 
     * @return true 注册成功, false 注册失败
     */
    public boolean registerGeoFenceListener(double latitude, double longitude,
            float radius, long expiration, RemoteGeoFenceListener listener) {
        if (!m_remoteAvaliable) {
            IwdsLog.e(this, "Location service on remote device unavailable");
            return false;
        }

        IwdsAssert.dieIf(this, latitude < 0, "latitude < 0");
        IwdsAssert.dieIf(this, longitude < 0, "longitude < 0");
        IwdsAssert.dieIf(this, radius <= 0, "radius <= 0");
        IwdsAssert.dieIf(this, (expiration < 0 && expiration != -1),
                "(expiration < 0 && expiration != -1)");

        IwdsAssert.dieIf(this, listener == null, "Listener is null");

        RemoteGeoFenceCallback callback = m_geofenceListeners.get(listener);
        if (callback != null) {
            return false;

        } else {
            callback = new RemoteGeoFenceCallback(listener);
            m_geofenceListeners.put(listener, callback);
        }

        try {
            m_service.registerGeoFenceListener(callback.uuid, latitude,
                    longitude, radius, expiration, callback);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in registerGeoFenceListener: " + e.toString());
            return false;
        }

        return true;
    }

    /**
     * 注销地理围栏监听器{@link com.ingenic.iwds.smartlocation.RemoteGeoFenceListener
     * RemoteGeoFenceListener}
     * 
     * @param listener
     *            监听器对象
     *            {@link com.ingenic.iwds.smartlocation.RemoteGeoFenceListener
     *            RemoteGeoFenceListener}
     */
    public void unregisterGeoFenceListener(RemoteGeoFenceListener listener) {
        IwdsAssert.dieIf(this, listener == null, "Listener is null");

        RemoteGeoFenceCallback callback = m_geofenceListeners.get(listener);

        if (callback == null)
            return;

        try {
            m_service.unregisterGeoFenceListener(callback.uuid);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in unregisterGeoFenceListener: " + e.toString());
        }

        m_geofenceListeners.remove(listener);
    }

    /* -------------- GeoFence alert callback end --------------- */

    /* --------------------- Proximity alert callback -------------------- */
    private class RemoteProximityCallback extends IRemoteProximityCallback.Stub {
        private static final int MSG_PROXIMITY_ALERT = 0;

        private RemoteProximityListener m_listener;
        String uuid;

        RemoteProximityCallback(RemoteProximityListener listener) {
            m_listener = listener;
            uuid = UUID.randomUUID().toString();
        }

        private Handler m_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_PROXIMITY_ALERT:
                    m_listener.onProximityAlert(msg.arg1);
                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");
                }
            }
        };

        @Override
        public void onProximityAlert(int status) throws RemoteException {
            Message.obtain(m_handler, MSG_PROXIMITY_ALERT, status, 0)
                    .sendToTarget();
        }
    }

    /**
     * 注册区域警戒监听器{@link com.ingenic.iwds.smartlocation.RemoteProximityListener
     * RemoteProximityListener}, 根据给定的经纬度和半径，当设备进入区域注册的监听器将被连续回调,
     * 此方法需要与定位请求方法同时使用.
     * 
     * @param latitude
     *            警戒区域中心点的纬度
     * @param longitude
     *            警戒区域中心点的经度
     * @param radius
     *            警戒区域的半径，单位为米
     * @param expiration
     *            警戒时间，单位为毫秒
     * @param listener
     *            监听器对象
     *            {@link com.ingenic.iwds.smartlocation.RemoteProximityListener
     *            RemoteProximityListener}
     * 
     * @return true 注册成功, false 注册失败
     */
    public boolean registerProximityListener(double latitude, double longitude,
            float radius, long expiration, RemoteProximityListener listener) {
        if (!m_remoteAvaliable) {
            IwdsLog.e(this, "Location service on remote device unavailable");
            return false;
        }

        IwdsAssert.dieIf(this, latitude < 0, "latitude < 0");
        IwdsAssert.dieIf(this, longitude < 0, "longitude < 0");
        IwdsAssert.dieIf(this, radius <= 0, "radius <= 0");
        IwdsAssert.dieIf(this, (expiration < 0), "expiration < 0");

        IwdsAssert.dieIf(this, listener == null, "Listener is null");

        RemoteProximityCallback callback = m_proximityListeners.get(listener);
        if (callback != null) {
            return false;

        } else {
            callback = new RemoteProximityCallback(listener);
            m_proximityListeners.put(listener, callback);
        }

        try {
            m_service.registerProximityListener(callback.uuid, latitude,
                    longitude, radius, expiration, callback);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in registerProximityListener: " + e.toString());
            return false;
        }

        return true;
    }

    /**
     * 注销区域警戒监听器{@link com.ingenic.iwds.smartlocation.RemoteProximityListener
     * RemoteProximityListener}
     * 
     * @param listener
     *            监听器对象
     *            {@link com.ingenic.iwds.smartlocation.RemoteProximityListener
     *            RemoteProximityListener}
     */
    public void unregisterProximityListener(RemoteProximityListener listener) {
        IwdsAssert.dieIf(this, listener == null, "Listener is null");

        RemoteProximityCallback callback = m_proximityListeners.get(listener);

        if (callback == null)
            return;

        try {
            m_service.unregisterProximityListener(callback.uuid);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in unregisterProximityListener: " + e.toString());
        }

        m_proximityListeners.remove(listener);
    }

    /* -------------- Proximity alert callback end --------------- */

    /* ------------- Location update callback --------------- */
    private class RemoteLocationCallback extends IRemoteLocationCallback.Stub {
        private static final int MSG_ON_LOCAION_CHANGED = 0;
        private static final int MSG_ON_LAST_KNOWN_LOCATION = 1;

        private RemoteLocationListener m_listener;
        String uuid;
        String provider;
        boolean single;

        public RemoteLocationCallback(RemoteLocationListener listener,
                String p, boolean s) {
            m_listener = listener;
            provider = p;
            single = s;
            uuid = UUID.randomUUID().toString();
        }

        private Handler m_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_ON_LOCAION_CHANGED:
                    if (single)
                        unregisterCurrentLocationListener(provider, m_listener);

                    m_listener.onLocationChanged((RemoteLocation) msg.obj);
                    break;

                case MSG_ON_LAST_KNOWN_LOCATION:
                    unregisterLastKnownLocationListener(provider, m_listener);
                    m_listener.onLocationChanged((RemoteLocation) msg.obj);
                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");
                }
            }
        };

        @Override
        public void onLocationChanged(RemoteLocation location)
                throws RemoteException {
            Message.obtain(m_handler, MSG_ON_LOCAION_CHANGED, location)
                    .sendToTarget();
        }

        @Override
        public void onLastKnownLocation(RemoteLocation location)
                throws RemoteException {
            Message.obtain(m_handler, MSG_ON_LAST_KNOWN_LOCATION, location)
                    .sendToTarget();
        }
    }

    /**
     * 注册定位监听器{@link com.ingenic.iwds.smartlocation.RemoteLocationListener
     * RemoteLocationListener}. 如果参数 provider 为 IWDS_NETWORK_PROVIDER,
     * 则由高德定位服务处理. 使用该方法，不管定位是否成功，都会回调 onLocationChanged(RemoteLocation
     * location)方法. 如果参数 provider 为 GPS_PROVIDER, 则由 android 定位服务处理.
     * 
     * @param provider
     *            注册监听的 provider 名称, {@link #GPS_PROVIDER},
     *            {@link #IWDS_NETWORK_PROVIDER}
     * @param listener
     *            监听器对象
     *            {@link com.ingenic.iwds.smartlocation.RemoteLocationListener
     *            RemoteLocationListener}
     * 
     * @return true 注册成功, false 注册失败
     */
    public boolean registerLocationListener(String provider,
            RemoteLocationListener listener) {
        if (!m_remoteAvaliable) {
            IwdsLog.e(this, "Location service on remote device unavailable");
            return false;
        }

        IwdsAssert.dieIf(this, listener == null, "listener == null");
        IwdsAssert.dieIf(this, provider == null || provider.isEmpty(),
                "provider is null or empty");

        if ((!provider.equals(GPS_PROVIDER))
                && (!provider.equals(IWDS_NETWORK_PROVIDER)))
            IwdsAssert.dieIf(this, true, "Unsupported provider type");

        ArrayList<RemoteLocationCallback> callbacks = m_locationListeners
                .get(listener);
        if (callbacks != null) {
            for (RemoteLocationCallback cb : callbacks)
                if (cb.provider.equals(provider))
                    return false;
        } else {
            callbacks = new ArrayList<RemoteLocationCallback>();
            m_locationListeners.put(listener, callbacks);
        }

        RemoteLocationCallback callback = new RemoteLocationCallback(listener,
                provider, false);

        callbacks.add(callback);

        try {
            m_service.registerLocationListener(callback.uuid, provider,
                    callback);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in registerLocationListener: " + e.toString());
            return false;
        }

        return true;
    }

    /**
     * 注销定位监听器{@link com.ingenic.iwds.smartlocation.RemoteLocationListener
     * RemoteLocationListener}.
     * 
     * @param listener
     *            监听器对象
     *            {@link com.ingenic.iwds.smartlocation.RemoteLocationListener
     *            RemoteLocationListener}
     * @param provider
     *            注销监听的 provider 名称, {@link #GPS_PROVIDER},
     *            {@link #IWDS_NETWORK_PROVIDER}
     */
    public void unregisterLocationListener(RemoteLocationListener listener,
            String provider) {
        IwdsAssert.dieIf(this, listener == null, "listener == null");
        IwdsAssert.dieIf(this, provider == null || provider.isEmpty(),
                "provider is null or empty");

        ArrayList<RemoteLocationCallback> callbacks = m_locationListeners
                .get(listener);

        if (callbacks == null)
            return;

        try {
            for (Iterator<RemoteLocationCallback> it = callbacks.iterator(); it
                    .hasNext();) {
                RemoteLocationCallback cb = it.next();
                if (cb.provider.equals(provider)) {
                    m_service.unregisterLocationListener(cb.uuid);
                    it.remove();
                    break;
                }
            }

            if (callbacks.isEmpty())
                m_locationListeners.remove(listener);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in unregisterLocationListener: " + e.toString());
        }
    }

    /**
     * 根据给定的 provider 请求一次定位
     * 
     * @param provider
     *            注销监听的 provider 名称, {@link #GPS_PROVIDER},
     *            {@link #IWDS_NETWORK_PROVIDER}
     * @param listener
     *            监听器对象
     *            {@link com.ingenic.iwds.smartlocation.RemoteLocationListener
     *            RemoteLocationListener}
     * 
     * @return true 请求成功, false 请求失败
     */
    public boolean requestLocationUpdate(String provider,
            RemoteLocationListener listener) {
        if (!m_remoteAvaliable) {
            IwdsLog.e(this, "Location service on remote device unavailable");
            return false;
        }

        IwdsAssert.dieIf(this, listener == null, "listener == null");
        IwdsAssert.dieIf(this, provider == null || provider.isEmpty(),
                "provider is null or empty");

        if ((!provider.equals(GPS_PROVIDER))
                && (!provider.equals(IWDS_NETWORK_PROVIDER))) {
            IwdsAssert.dieIf(this, true, "Unsupported provider type");
        }

        ArrayList<RemoteLocationCallback> callbacks = m_currentLocationListeners
                .get(listener);
        if (callbacks != null) {
            for (RemoteLocationCallback cb : callbacks)
                if (cb.provider.equals(provider))
                    return false;
        } else {
            callbacks = new ArrayList<RemoteLocationCallback>();
            m_currentLocationListeners.put(listener, callbacks);
        }

        RemoteLocationCallback callback = new RemoteLocationCallback(listener,
                provider, true);

        callbacks.add(callback);

        try {
            m_service.registerLocationListener(callback.uuid, provider,
                    callback);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in requestLocation: " + e.toString());
            return false;
        }

        return true;
    }

    /**
     * 根据给定的 provider 请求得到最近一次定位的位置
     * 
     * @param provider
     *            注销监听的 provider 名称, {@link #GPS_PROVIDER},
     *            {@link #IWDS_NETWORK_PROVIDER}
     * @param listener
     *            监听器对象
     *            {@link com.ingenic.iwds.smartlocation.RemoteLocationListener
     *            RemoteLocationListener}
     * 
     * @return true 请求成功, false 请求失败
     */
    public boolean requestLastKnownLocation(String provider,
            RemoteLocationListener listener) {
        if (!m_remoteAvaliable) {
            IwdsLog.e(this, "Location service on remote device unavailable");
            return false;
        }

        IwdsAssert.dieIf(this, listener == null, "listener == null");
        IwdsAssert.dieIf(this, provider == null || provider.isEmpty(),
                "provider is null or empty");

        if ((!provider.equals(GPS_PROVIDER))
                && (!provider.equals(IWDS_NETWORK_PROVIDER))) {
            IwdsAssert.dieIf(this, true, "Unsupported provider type");
        }

        ArrayList<RemoteLocationCallback> callbacks = m_lastKnownLocationListeners
                .get(listener);
        if (callbacks != null) {
            for (RemoteLocationCallback cb : callbacks)
                if (cb.provider.equals(provider))
                    return false;
        } else {
            callbacks = new ArrayList<RemoteLocationCallback>();
            m_lastKnownLocationListeners.put(listener, callbacks);
        }

        RemoteLocationCallback callback = new RemoteLocationCallback(listener,
                provider, false);

        callbacks.add(callback);

        try {
            m_service.requestLastKnownLocation(callback.uuid, provider,
                    callback);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in requestLastKnownLocation: " + e.toString());
            return false;
        }

        return true;
    }

    private void unregisterLastKnownLocationListener(String provider,
            RemoteLocationListener listener) {

        ArrayList<RemoteLocationCallback> callbacks = m_lastKnownLocationListeners
                .get(listener);

        if (callbacks == null)
            return;

        for (Iterator<RemoteLocationCallback> it = callbacks.iterator(); it
                .hasNext();) {
            RemoteLocationCallback cb = it.next();
            if (cb.provider.equals(provider)) {
                it.remove();
                break;
            }
        }

        if (callbacks.isEmpty())
            m_lastKnownLocationListeners.remove(listener);

    }

    private void unregisterCurrentLocationListener(String provider,
            RemoteLocationListener listener) {
        ArrayList<RemoteLocationCallback> callbacks = m_currentLocationListeners
                .get(listener);

        if (callbacks == null)
            return;

        try {
            for (Iterator<RemoteLocationCallback> it = callbacks.iterator(); it
                    .hasNext();) {
                RemoteLocationCallback cb = it.next();
                if (cb.provider.equals(provider)) {
                    m_service.unregisterLocationListener(cb.uuid);
                    it.remove();
                    break;
                }
            }

            if (callbacks.isEmpty())
                m_currentLocationListeners.remove(listener);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in unregisterCurrentLocationListener: "
                    + e.toString());
        }
    }

    /* ------------ Location update callback end --------------- */

    /* -------------- Weather update callback -------------- */
    private class RemoteWeatherUpdateCallback extends
            IRemoteWeatherCallback.Stub {
        private static final int MSG_WEATHER_LIVE = 0;
        private static final int MSG_WEATHER_FORECAST = 1;

        private RemoteWeatherListener m_listener;
        String uuid;
        int weatherType;

        public RemoteWeatherUpdateCallback(int w, RemoteWeatherListener listener) {
            m_listener = listener;
            weatherType = w;
            uuid = UUID.randomUUID().toString();
        }

        private Handler m_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_WEATHER_LIVE:
                    unregisterWeatherListener(weatherType, m_listener);
                    m_listener
                            .onWeatherLiveSearched((RemoteWeatherLive) msg.obj);
                    break;

                case MSG_WEATHER_FORECAST:
                    unregisterWeatherListener(weatherType, m_listener);
                    m_listener
                            .onWeatherForecastSearched((RemoteWeatherForecast) msg.obj);
                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");
                }
            }

        };

        @Override
        public void onWeatherLiveSearched(RemoteWeatherLive weatherLive)
                throws RemoteException {
            Message.obtain(m_handler, MSG_WEATHER_LIVE, weatherLive)
                    .sendToTarget();
        }

        @Override
        public void onWeatherForecastSearched(
                RemoteWeatherForecast weatherForecast) throws RemoteException {
            Message.obtain(m_handler, MSG_WEATHER_FORECAST, weatherForecast)
                    .sendToTarget();

        }
    }

    /**
     * 请求天气监听, 天气接口仅支持中国大陆、香港、澳门数据返回.
     *
     * @param weatherType
     *            请求天气的类型 {@link #WEATHER_TYPE_LIVE},
     *            {@link #WEATHER_TYPE_FORECAST}
     * @param listener
     *            监听器对象
     *            {@link com.ingenic.iwds.smartlocation.RemoteWeatherListener
     *            RemoteWeatherListener}
     * @return true 请求成功, false 请求失败
     */
    public boolean requestWeatherUpdate(int weatherType,
            RemoteWeatherListener listener) {
        if (!m_remoteAvaliable) {
            IwdsLog.e(this, "Location service on remote device unavailable");
            return false;
        }

        IwdsAssert.dieIf(this, listener == null, "Listener is null.");
        IwdsAssert.dieIf(this, (weatherType != WEATHER_TYPE_LIVE)
                && (weatherType != WEATHER_TYPE_FORECAST),
                "Unsuppored weather type");

        ArrayList<RemoteWeatherUpdateCallback> callbacks = m_weatherUpdateListeners
                .get(listener);

        if (callbacks != null) {
            for (RemoteWeatherUpdateCallback cb : callbacks)
                if (cb.weatherType == weatherType)
                    return false;

        } else {
            callbacks = new ArrayList<RemoteWeatherUpdateCallback>();
            m_weatherUpdateListeners.put(listener, callbacks);
        }

        RemoteWeatherUpdateCallback callback = new RemoteWeatherUpdateCallback(
                weatherType, listener);

        callbacks.add(callback);

        try {
            m_service
                    .requestWeatherUpdate(weatherType, callback.uuid, callback);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in requestWeatherUpdate: " + e.toString());
            return false;
        }

        return true;
    }

    private void unregisterWeatherListener(int weatherType,
            RemoteWeatherListener listener) {
        ArrayList<RemoteWeatherUpdateCallback> callbacks = m_weatherUpdateListeners
                .get(listener);

        if (callbacks == null)
            return;

        for (Iterator<RemoteWeatherUpdateCallback> it = callbacks.iterator(); it
                .hasNext();) {
            RemoteWeatherUpdateCallback cb = it.next();
            if (cb.weatherType == weatherType) {
                it.remove();

                break;
            }
        }

        if (callbacks.isEmpty())
            m_weatherUpdateListeners.remove(listener);
    }

    /* -------------- Weather update callback end -------------- */

    /* --------------- GPS status callback ------------------- */
    private class RemoteGpsStatusCallback extends IRemoteGpsStatusCallback.Stub {
        private final static int MSG_ON_GPS_STATUS_CHANGED = 0;
        private final static int MSG_ON_GPS_CURRENT_STATUS = 1;

        private RemoteGpsStatusListener m_listener;
        String uuid;

        RemoteGpsStatusCallback(RemoteGpsStatusListener listener) {
            m_listener = listener;
            uuid = UUID.randomUUID().toString();
        }

        private Handler m_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_ON_GPS_STATUS_CHANGED:
                    m_listener.onGpsStatusChanged(msg.arg1);
                    break;

                case MSG_ON_GPS_CURRENT_STATUS:
                    m_gpsCurrentStatusListeners.remove(m_listener);
                    m_listener.onGpsStatus((RemoteGpsStatus) msg.obj);
                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");

                }
            }
        };

        @Override
        public void onGpsStatusChanged(int event) throws RemoteException {
            Message.obtain(m_handler, MSG_ON_GPS_STATUS_CHANGED, event, 0)
                    .sendToTarget();
        }

        @Override
        public void onGpsCurrentStatus(RemoteGpsStatus status)
                throws RemoteException {
            Message.obtain(m_handler, MSG_ON_GPS_CURRENT_STATUS, status)
                    .sendToTarget();
        }

    }

    /**
     * 注册 GPS 状态监听器
     * {@link com.ingenic.iwds.smartlocation.RemoteGpsStatus.RemoteGpsStatusListener
     * RemoteGpsStatusListener}.
     * 
     * @param listener
     *            监听器对象
     *            {@link com.ingenic.iwds.smartlocation.RemoteGpsStatus.RemoteGpsStatusListener
     *            RemoteGpsStatusListener}.
     * @return true 注册成功, false 注册失败
     */
    public boolean registerGpsStatusListener(RemoteGpsStatusListener listener) {
        if (!m_remoteAvaliable) {
            IwdsLog.e(this, "Location service on remote device unavailable");
            return false;
        }

        IwdsAssert.dieIf(this, listener == null, "Listener is null");

        RemoteGpsStatusCallback callback = m_gpsStatusListeners.get(listener);
        if (callback != null) {
            return false;

        } else {
            callback = new RemoteGpsStatusCallback(listener);
            m_gpsStatusListeners.put(listener, callback);
        }

        try {
            m_service.registerGpsStatusListener(callback.uuid, callback);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in registerGpsStatusListener: " + e.toString());
            return false;
        }

        return true;
    }

    /**
     * 注销 GPS 状态监听器
     * {@link com.ingenic.iwds.smartlocation.RemoteGpsStatus.RemoteGpsStatusListener
     * RemoteGpsStatusListener}.
     * 
     * @param listener
     *            监听器对象
     *            {@link com.ingenic.iwds.smartlocation.RemoteGpsStatus.RemoteGpsStatusListener
     *            RemoteGpsStatusListener}.
     */
    public void unregisterGpsStatusListener(RemoteGpsStatusListener listener) {
        IwdsAssert.dieIf(this, listener == null, "Listener is null");

        RemoteGpsStatusCallback callback = m_gpsStatusListeners.get(listener);

        if (callback == null)
            return;

        try {
            m_service.unregisterGpsStatusListener(callback.uuid);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in unregisterGpsStatusListener: " + e.toString());
        }

        m_gpsStatusListeners.remove(listener);
    }

    /**
     * 请求获取 GPS 状态
     * 
     * @param listener
     *            监听器对象
     *            {@link com.ingenic.iwds.smartlocation.RemoteGpsStatus.RemoteGpsStatusListener
     *            RemoteGpsStatusListener}.
     * 
     * @return true 请求成功, false 请求失败
     */
    public boolean requestGpsStatus(RemoteGpsStatusListener listener) {
        if (!m_remoteAvaliable) {
            IwdsLog.e(this, "Location service on remote device unavailable");
            return false;
        }

        IwdsAssert.dieIf(this, listener == null, "Listener is null");

        RemoteGpsStatusCallback callback = m_gpsCurrentStatusListeners
                .get(listener);

        if (callback != null) {
            return false;

        } else {
            callback = new RemoteGpsStatusCallback(listener);
            m_gpsCurrentStatusListeners.put(listener, callback);
        }

        try {
            m_service.requestGpsStatus(callback.uuid, callback);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in requestGpsStatus: " + e.toString());
            return false;
        }

        return true;
    }

    /* --------------- GPS status callback end ------------------- */

    private boolean requestGpsEnable(boolean enable) {
        if (!m_remoteAvaliable) {
            IwdsLog.e(this, "Location service on remote device unavailable");
            return false;
        }

        try {
            m_service.requestGpsEnable(enable);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in requestGpsEnable: " + e.toString());
            return false;
        }

        return true;
    }

    /* --------------- Providers callback ------------------- */
    private class RemoteProviderCallback extends IRemoteProviderCallback.Stub {
        private static final int MSG_ON_PROVIDER_LIST = 0;
        private static final int MSG_ON_PROVIDER_STATUS = 1;

        private RemoteProviderListener m_listener;
        String provider;
        String uuid;
        boolean enabledOnly;

        RemoteProviderCallback(RemoteProviderListener listener, String p) {
            m_listener = listener;
            provider = p;
            uuid = UUID.randomUUID().toString();
        }

        RemoteProviderCallback(RemoteProviderListener listener, boolean e) {
            m_listener = listener;
            enabledOnly = e;
            uuid = UUID.randomUUID().toString();
        }

        private Handler m_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_ON_PROVIDER_STATUS:
                    unregisterProviderStatusListener(m_listener, provider);
                    m_listener.onProviderStatus(msg.arg1 == 1 ? true : false,
                            (String) msg.obj);
                    break;

                case MSG_ON_PROVIDER_LIST:
                    unregisterProvidersListener(m_listener, enabledOnly);
                    m_listener.onProviderList(msg.arg1 == 1 ? true : false,
                            (ArrayList<String>) msg.obj);
                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");
                }
            }
        };

        @Override
        public void onProviderStatus(boolean enabled, String provider)
                throws RemoteException {
            Message.obtain(m_handler, MSG_ON_PROVIDER_STATUS, enabled ? 1 : 0,
                    0, provider).sendToTarget();
        }

        @Override
        public void onProviderList(boolean enabledOnly,
                List<String> providerList) throws RemoteException {
            Message.obtain(m_handler, MSG_ON_PROVIDER_LIST,
                    enabledOnly ? 1 : 0, 0, providerList).sendToTarget();
        }
    }

    /**
     * 请求检测给定的 provider 是否是可用
     * 
     * @param provider
     *            provider 的名称
     * @param listener
     *            {@link com.ingenic.iwds.smartlocation.RemoteProviderListener
     *            RemoteProviderListener}.
     * @return true 请求成功, false 请求失败
     */
    public boolean requestProviderStatus(String provider,
            RemoteProviderListener listener) {
        if (!m_remoteAvaliable) {
            IwdsLog.e(this, "Location service on remote device unavailable");
            return false;
        }

        IwdsAssert.dieIf(this, listener == null, "Listener is null");

        if ((!provider.equals(GPS_PROVIDER))
                && (!provider.equals(IWDS_NETWORK_PROVIDER))) {
            IwdsAssert.dieIf(this, true, "Unsupported provider type");
        }

        ArrayList<RemoteProviderCallback> callbacks = m_providerStatusListeners
                .get(listener);

        if (callbacks != null) {
            for (RemoteProviderCallback cb : callbacks)
                if (cb.provider.equals(provider))
                    return false;

        } else {
            callbacks = new ArrayList<RemoteProviderCallback>();

            m_providerStatusListeners.put(listener, callbacks);
        }

        RemoteProviderCallback callback = new RemoteProviderCallback(listener,
                provider);

        callbacks.add(callback);

        try {
            m_service.requestProviderStatus(callback.uuid, provider, callback);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in requestProviderStatus: " + e.toString());
            return false;
        }

        return true;
    }

    /**
     * 根据给定条件请求获取 provider 列表
     * 
     * @param enabledOnly
     *            如果为true 只返回当前使能的provider，否则返回所有支持的provider。目前只支持
     *            {@link #GPS_PROVIDER} 和 {@link #IWDS_NETWORK_PROVIDER}
     * @param listener
     *            {@link com.ingenic.iwds.smartlocation.RemoteProviderListener
     *            RemoteProviderListener}.
     * @return true 请求成功, false 请求失败
     */
    public boolean requestProviderList(boolean enabledOnly,
            RemoteProviderListener listener) {
        if (!m_remoteAvaliable) {
            IwdsLog.e(this, "Location service on remote device unavailable");
            return false;
        }

        IwdsAssert.dieIf(this, listener == null, "Listener is null");

        ArrayList<RemoteProviderCallback> callbacks = m_providerListListeners
                .get(listener);
        if (callbacks != null) {
            for (RemoteProviderCallback cb : callbacks)
                if (cb.enabledOnly == enabledOnly)
                    return false;

        } else {
            callbacks = new ArrayList<RemoteProviderCallback>();
            m_providerListListeners.put(listener, callbacks);
        }

        RemoteProviderCallback callback = new RemoteProviderCallback(listener,
                enabledOnly);

        callbacks.add(callback);

        try {
            m_service.requestProviderList(callback.uuid, enabledOnly, callback);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in requestProviderList: " + e.toString());
            return false;
        }

        return true;
    }

    private void unregisterProviderStatusListener(
            RemoteProviderListener listener, String provider) {
        ArrayList<RemoteProviderCallback> callbacks = m_providerStatusListeners
                .get(listener);

        if (callbacks == null)
            return;

        for (Iterator<RemoteProviderCallback> it = callbacks.iterator(); it
                .hasNext();) {
            RemoteProviderCallback cb = it.next();
            if (cb.provider.equals(provider)) {
                it.remove();
                break;
            }
        }

        if (callbacks.isEmpty())
            m_providerStatusListeners.remove(listener);
    }

    private void unregisterProvidersListener(RemoteProviderListener listener,
            boolean enabledOnly) {
        ArrayList<RemoteProviderCallback> callbacks = m_providerListListeners
                .get(listener);
        if (callbacks == null)
            return;

        for (Iterator<RemoteProviderCallback> it = callbacks.iterator(); it
                .hasNext();) {
            RemoteProviderCallback cb = it.next();
            if (cb.enabledOnly == enabledOnly) {
                it.remove();
                break;
            }
        }

        if (callbacks.isEmpty())
            m_providerListListeners.remove(listener);
    }

    /* --------------- Provider callback end ------------------- */

    /* --------------- Network status callback ------------------- */
    private class RemoteNetworkStatusCallback extends
            IRemoteNetworkStatusCallback.Stub {
        private final static int MSG_ON_NETWORK_STATUS_CHANGED = 0;
        private final static int MSG_ON_NETWORK_CURRENT_STATUS = 1;

        private RemoteNetworkStatusListener m_listener;
        String uuid;

        RemoteNetworkStatusCallback(RemoteNetworkStatusListener listener) {
            m_listener = listener;
            uuid = UUID.randomUUID().toString();
        }

        private Handler m_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_ON_NETWORK_STATUS_CHANGED:
                    m_listener.onNetworkStatusChanged(msg.arg1);
                    break;

                case MSG_ON_NETWORK_CURRENT_STATUS:
                    m_networkCurrentStatusListeners.remove(m_listener);
                    m_listener.onNetworkStatusChanged(msg.arg1);
                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");
                }
            }
        };

        @Override
        public void onNetworkStatusChanged(int status) throws RemoteException {
            Message.obtain(m_handler, MSG_ON_NETWORK_STATUS_CHANGED, status, 0)
                    .sendToTarget();
        }

        @Override
        public void onNetworkCurrentStatus(int status) throws RemoteException {
            Message.obtain(m_handler, MSG_ON_NETWORK_CURRENT_STATUS, status, 0)
                    .sendToTarget();
        }
    }

    /**
     * 注销网络状态监听器
     * {@link com.ingenic.iwds.smartlocation.RemoteNetworkStatusListener
     * RemoteNetworkStatusListener}.
     * 
     * @param listener
     *            监听器对象
     *            {@link com.ingenic.iwds.smartlocation.RemoteNetworkStatusListener
     *            RemoteNetworkStatusListener}.
     */
    public boolean registerNetworkStatusListener(
            RemoteNetworkStatusListener listener) {
        if (!m_remoteAvaliable) {
            IwdsLog.e(this, "Location service on remote device unavailable");
            return false;
        }

        IwdsAssert.dieIf(this, listener == null, "Listener is null");

        RemoteNetworkStatusCallback callback = m_networkStatusListeners
                .get(listener);
        if (callback != null) {
            return false;

        } else {
            callback = new RemoteNetworkStatusCallback(listener);
            m_networkStatusListeners.put(listener, callback);
        }

        try {
            m_service.registerNetworkStatusListener(callback.uuid, callback);

        } catch (RemoteException e) {
            IwdsLog.e(
                    this,
                    "Exception in registerNetworkStatusListener: "
                            + e.toString());
            return false;
        }

        return true;
    }

    /**
     * 注销网络状态监听器
     * {@link com.ingenic.iwds.smartlocation.RemoteNetworkStatusListener
     * RemoteNetworkStatusListener}.
     * 
     * @param listener
     *            监听器对象
     *            {@link com.ingenic.iwds.smartlocation.RemoteNetworkStatusListener
     *            RemoteGpsStatusListener}.
     */
    public void unregisterNetworkStatusListener(
            RemoteNetworkStatusListener listener) {
        IwdsAssert.dieIf(this, listener == null, "Listener is null");

        RemoteNetworkStatusCallback callback = m_networkStatusListeners
                .get(listener);

        if (callback == null)
            return;

        try {
            m_service.unregisterNetworkStatusListener(callback.uuid);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in unregisterNetworkStatusListener: "
                    + e.toString());
        }

        m_networkStatusListeners.remove(listener);
    }

    /**
     * 请求获取网络状态
     * 
     * @param listener
     *            监听器对象
     *            {@link com.ingenic.iwds.smartlocation.RemoteNetworkStatusListener
     *            RemoteNetworkStatusListener}.
     * 
     * @return true 请求成功, false 请求失败
     */
    public boolean requestNetworkStatus(RemoteNetworkStatusListener listener) {
        if (!m_remoteAvaliable) {
            IwdsLog.e(this, "Location service on remote device unavailable");
            return false;
        }

        IwdsAssert.dieIf(this, listener == null, "Listener is null");

        RemoteNetworkStatusCallback callback = m_networkCurrentStatusListeners
                .get(listener);

        if (callback != null) {
            return false;

        } else {
            callback = new RemoteNetworkStatusCallback(listener);
            m_networkCurrentStatusListeners.put(listener, callback);
        }

        try {
            m_service.requestNetworkStatus(callback.uuid, callback);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in requestNetworkStatus: " + e.toString());
            return false;
        }

        return true;
    }

    /* --------------- Network status callback end ------------------- */

    /* --------------- unregister all listeners -------------------- */
    private void unregisterAllListeners() {
        unregisterAllWeatherListener();
        unregisterAllLastKnownLocationListener();
        unregisterAllLocationListener();
        unregisterAllCurrentLocationListener();
        unregisterAllGpsStatusListener();
        unregisterAllNetworkStatusListener();
        unregisterAllGeoFenceListener();
        unregisterAllProximityListener();
        unregisterAllGpsCurrentStatusListener();
        unregisterAllNetworkCurrentStatusListener();
        unregisterAllProviderStatusListener();
        unregisterAllProviderListListener();
    }

    private void unregisterAllWeatherListener() {
        if (!m_weatherUpdateListeners.isEmpty())
            m_weatherUpdateListeners.clear();
    }

    private void unregisterAllLastKnownLocationListener() {
        if (!m_lastKnownLocationListeners.isEmpty())
            m_lastKnownLocationListeners.clear();
    }

    private void unregisterAllGpsCurrentStatusListener() {
        if (!m_gpsCurrentStatusListeners.isEmpty())
            m_gpsCurrentStatusListeners.clear();
    }

    private void unregisterAllNetworkCurrentStatusListener() {
        if (!m_networkCurrentStatusListeners.isEmpty())
            m_networkCurrentStatusListeners.clear();
    }

    private void unregisterAllProviderStatusListener() {
        if (!m_providerStatusListeners.isEmpty())
            m_providerStatusListeners.clear();
    }

    private void unregisterAllProviderListListener() {
        if (!m_providerListListeners.isEmpty())
            m_providerListListeners.clear();
    }

    private void unregisterAllCurrentLocationListener() {
        Set<RemoteLocationListener> listeners = m_currentLocationListeners
                .keySet();

        Iterator<RemoteLocationListener> it = listeners.iterator();
        while (it.hasNext()) {
            ArrayList<RemoteLocationCallback> callbacks = m_currentLocationListeners
                    .get(it.next());

            try {
                for (RemoteLocationCallback cb : callbacks)
                    m_service.unregisterLocationListener(cb.uuid);

            } catch (RemoteException e) {
                IwdsLog.e(
                        this,
                        "Exception in unregisterLocationListener: "
                                + e.toString());
            }
        }

        m_currentLocationListeners.clear();
    }

    private void unregisterAllLocationListener() {
        Set<RemoteLocationListener> listeners = m_locationListeners.keySet();

        Iterator<RemoteLocationListener> it = listeners.iterator();
        while (it.hasNext()) {
            ArrayList<RemoteLocationCallback> callbacks = m_locationListeners
                    .get(it.next());

            try {
                for (RemoteLocationCallback cb : callbacks)
                    m_service.unregisterLocationListener(cb.uuid);

            } catch (RemoteException e) {
                IwdsLog.e(
                        this,
                        "Exception in unregisterLocationListener: "
                                + e.toString());
            }
        }

        m_locationListeners.clear();
    }

    private void unregisterAllGpsStatusListener() {
        Set<RemoteGpsStatusListener> listeners = m_gpsStatusListeners.keySet();

        Iterator<RemoteGpsStatusListener> it = listeners.iterator();

        while (it.hasNext()) {
            RemoteGpsStatusCallback callback = m_gpsStatusListeners.get(it
                    .next());

            try {
                m_service.unregisterGpsStatusListener(callback.uuid);

            } catch (RemoteException e) {
                IwdsLog.e(this,
                        "Exception in unregisterGpsListener: " + e.toString());
            }
        }

        m_gpsStatusListeners.clear();
    }

    private void unregisterAllNetworkStatusListener() {
        Set<RemoteNetworkStatusListener> listeners = m_networkStatusListeners
                .keySet();

        Iterator<RemoteNetworkStatusListener> it = listeners.iterator();

        while (it.hasNext()) {
            RemoteNetworkStatusCallback callback = m_networkStatusListeners
                    .get(it.next());

            try {
                m_service.unregisterNetworkStatusListener(callback.uuid);

            } catch (RemoteException e) {
                IwdsLog.e(
                        this,
                        "Exception in unregisterNetworkListener: "
                                + e.toString());
            }
        }

        m_networkStatusListeners.clear();
    }

    private void unregisterAllGeoFenceListener() {
        Set<RemoteGeoFenceListener> listeners = m_geofenceListeners.keySet();

        Iterator<RemoteGeoFenceListener> it = listeners.iterator();

        while (it.hasNext()) {
            RemoteGeoFenceCallback callback = m_geofenceListeners
                    .get(it.next());

            try {
                m_service.unregisterGeoFenceListener(callback.uuid);

            } catch (RemoteException e) {
                IwdsLog.e(
                        this,
                        "Exception in unregisterGeoFenceListener: "
                                + e.toString());
            }
        }

        m_geofenceListeners.clear();
    }

    private void unregisterAllProximityListener() {
        Set<RemoteProximityListener> listeners = m_proximityListeners.keySet();

        Iterator<RemoteProximityListener> it = listeners.iterator();

        while (it.hasNext()) {
            RemoteProximityCallback callback = m_proximityListeners.get(it
                    .next());

            try {
                m_service.unregisterProximityListener(callback.uuid);

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in unregisterProximityListener: "
                        + e.toString());
            }
        }

        m_proximityListeners.clear();
    }

    /* --------------- unregister all listeners end -------------------- */
}
