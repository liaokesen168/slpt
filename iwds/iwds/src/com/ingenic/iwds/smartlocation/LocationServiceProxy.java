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
import java.util.List;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.amap.api.location.AMapLocalWeatherForecast;
import com.amap.api.location.AMapLocalWeatherListener;
import com.amap.api.location.AMapLocalWeatherLive;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback;
import com.ingenic.iwds.datatransactor.ParcelTransactor;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

public class LocationServiceProxy {

    private final static String ACTION_GEOFENCE_ALERT = "com.ingenic.iwds.smartlocation.locationserviceproxy.geofence";
    private final static String ACTION_PROXIMITY_ALERT = "com.ingenic.iwds.smartlocation.locationserviceproxy.proximity";

    private Context m_context;
    private ParcelTransactor<RemoteLocationRequest> m_transactor;
    private ServiceProxyHandler m_handler;
    private static LocationServiceProxy sInstance;
    private LocationManagerProxy m_locationManagerProxy;
    private LocationManager m_locationManager;
    private ConnectivityManager m_connectivityManager;
    private IntentFilter m_networkFilter;

    private HashMap<String, GeoFenceAlertObserver> m_geoFenceAlertObservers;
    private HashMap<String, ProximityAlertObserver> m_proximityAlertObservers;

    private LocationServiceProxy() {

    }

    public static LocationServiceProxy getInstance() {
        if (sInstance == null)
            sInstance = new LocationServiceProxy();

        return sInstance;
    }

    public void initialize(Context context) {
        IwdsLog.i(this, "Initialize");

        m_context = context;

        m_transactor = new ParcelTransactor<RemoteLocationRequest>(m_context,
                RemoteLocationRequest.CREATOR, m_transportCallback,
                "c1dc19e2-17a4-0797-1111-68a0dd4bfb68");

        m_locationManager = (LocationManager) m_context
                .getSystemService(Context.LOCATION_SERVICE);

        m_geoFenceAlertObservers = new HashMap<String, GeoFenceAlertObserver>();
        m_proximityAlertObservers = new HashMap<String, ProximityAlertObserver>();

        m_connectivityManager = (ConnectivityManager) m_context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        m_networkFilter = new IntentFilter();
        m_networkFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        m_networkFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);

        m_handler = new ServiceProxyHandler();
    }

    public void start() {
        IwdsLog.i(this, "start");

        m_transactor.start();
    }

    private class GeoFenceAlertObserver extends BroadcastReceiver {
        private double latitude;
        private double longitude;
        private float radius;
        private long expiration;
        private PendingIntent intent;
        private String uuid;
        private IntentFilter filter;
        private String action;

        public GeoFenceAlertObserver(double latitude, double longitude,
                float radius, long expiration, String uuid) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.radius = radius;
            this.expiration = expiration;
            this.uuid = uuid;
            this.action = ACTION_GEOFENCE_ALERT + "." + this.uuid;
            this.intent = PendingIntent.getBroadcast(m_context, 0, new Intent(
                    this.action), 0);
            this.filter = new IntentFilter(this.action);
            this.filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
        }

        public void startObserver() {
            m_context.registerReceiver(this, this.filter);
            m_locationManagerProxy.addGeoFenceAlert(this.latitude,
                    this.longitude, this.radius, this.expiration, this.intent);
        }

        public void stopObserver() {
            m_context.unregisterReceiver(this);
            m_locationManagerProxy.removeGeoFenceAlert(this.intent);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(this.action)) {

                Bundle bundle = intent.getExtras();
                int status = bundle
                        .getInt(LocationManagerProxy.KEY_STATUS_CHANGED);

                m_handler.notifyGeoFenceAlert(status == 0 ? false : true,
                        this.uuid);
            }
        }
    }

    private class ProximityAlertObserver extends BroadcastReceiver {
        private double latitude;
        private double longitude;
        private float radius;
        private long expiration;
        private PendingIntent intent;
        private String uuid;
        private IntentFilter filter;
        private String action;

        public ProximityAlertObserver(double latitude, double longitude,
                float radius, long expiration, String uuid) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.radius = radius;
            this.expiration = expiration;
            this.uuid = uuid;
            this.action = ACTION_PROXIMITY_ALERT + "." + this.uuid;
            this.intent = PendingIntent.getBroadcast(m_context, 0, new Intent(
                    this.action), 0);
            this.filter = new IntentFilter(this.action);
            this.filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
        }

        public void startObserver() {
            m_context.registerReceiver(this, this.filter);
            m_locationManagerProxy.addProximityAlert(this.latitude,
                    this.longitude, this.radius, this.expiration, this.intent);
        }

        public void stopObserver() {
            m_context.unregisterReceiver(this);
            m_locationManagerProxy.removeProximityAlert(this.intent);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(this.action)) {

                Bundle bundle = intent.getExtras();
                int status = bundle
                        .getInt(LocationManagerProxy.KEY_STATUS_CHANGED);

                m_handler.notifyProximityAlert(status == 1 ? false : true,
                        this.uuid);
            }
        }
    }

    /* -------------------- Monitor network state ----------------- */
    private final BroadcastReceiver m_networkReceiver = new BroadcastReceiver() {
        private boolean statusAvailable = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    ConnectivityManager.CONNECTIVITY_ACTION)) {

                NetworkInfo networkInfo = m_connectivityManager
                        .getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isAvailable()) {

                    if (!statusAvailable)
                        m_handler.notifyNetworkState(true);

                    statusAvailable = true;

                } else {
                    if (statusAvailable)
                        m_handler.notifyNetworkState(false);

                    statusAvailable = false;
                }
            }
        }
    };
    /* -------------------- Monitor network state end ----------------- */

    /* ---------------------Monitor GPS state ------------------------ */
    private final GpsStatus.Listener m_gpsStatusListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            m_handler.notifyGpsState(event);
        }
    };
    /* ---------------------Monitor GPS state end ------------------------ */

    /* ---------------------- AMapLocalWeatherListener------------------------ */
    private AMapLocalWeatherListener m_localWeatherListener = new AMapLocalWeatherListener() {

        @Override
        public void onWeatherForecaseSearched(
                AMapLocalWeatherForecast aMapWeatherForecast) {

            RemoteWeatherForecast weatherForecast = ProxyUtils
                    .buildRemoteWeatherForecast(aMapWeatherForecast);

            m_handler.notifyWeatherForecast(weatherForecast);

        }

        @Override
        public void onWeatherLiveSearched(AMapLocalWeatherLive aMapWeatherLive) {

            RemoteWeatherLive weatherLive = ProxyUtils
                    .buildRemoteWeatherLive(aMapWeatherLive);

            m_handler.notifyWeatherLive(weatherLive);
        }
    };
    /*---------------------- AMapLocalWeatherListener end --------------------------*/

    /* ---------------------- AMapLocationListener -------------------------- */
    private AMapLocationListener m_aMapLocationListener = new AMapLocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            // do not care
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // do not care
        }

        @Override
        public void onProviderEnabled(String provider) {
            // do not care
        }

        @Override
        public void onProviderDisabled(String provider) {
            // do not care
        }

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {

            RemoteLocation remoteLocation = ProxyUtils
                    .buildRemoteLocation(aMapLocation);

            m_handler.notifyLocationChanged(remoteLocation);

        }
    };

    /* --------------------- AMapLocationListener end ------------------------ */

    /* --------------------- Android LocationListener ------------------------ */
    private final LocationListener m_androidLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {

            RemoteLocation remoteLocation = ProxyUtils
                    .buildRemoteLocation(location);

            // TODO: if network is connected, query coordinates online
            if (true) {
                if (location != null) {
                    // World Geodetic System ==> Mars Geodetic System
                    Gps marsGps = ProxyUtils.transformWgs84ToGcj02(
                            location.getLatitude(), location.getLongitude());

                    remoteLocation.setLatitude(marsGps.getWgLat());
                    remoteLocation.setLongitude(marsGps.getWgLon());
                }
            }

            m_handler.notifyLocationChanged(remoteLocation);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

    };

    /* ---------------- Android LocationListener end --------------------- */

    private class ServiceProxyHandler extends Handler {
        private final static int MSG_CHANNEL_STATUS_CHANGED = 0;
        private final static int MSG_LOCATION_SERVICE_CONNECTED = 1;
        private final static int MSG_REQUEST_WEATHER_UPDATE = 2;
        private final static int MSG_REQUEST_REGISTER_LOCATION_LISTENER = 3;
        private final static int MSG_REQUEST_UNREGISTER_LOCATION_LISTENER = 4;
        private final static int MSG_REQUEST_REGISTER_GEOFENCE_LISTENER = 5;
        private final static int MSG_REQUEST_UNREGISTER_GEOFENCE_LISTENER = 6;
        private final static int MSG_REQUEST_REGISTER_GPS_STATUS_LISTENER = 7;
        private final static int MSG_REQUEST_UNREGISTER_GPS_STATUS_LISTENER = 8;
        private final static int MSG_REQUEST_REGISTER_NETWORK_STATUS_LISTENER = 9;
        private final static int MSG_REQUEST_UNREGISTER_NETWORK_STATUS_LISTENER = 10;
        private final static int MSG_REQUEST_REGISTER_PROXIMITY_LISTENER = 11;
        private final static int MSG_REQUEST_UNREGISTER_PROXIMITY_LISTENER = 12;
        private final static int MSG_REQUEST_LAST_KNOWN_LOCATION = 13;
        private final static int MSG_REQUEST_GPS_STATUS = 14;
        private final static int MSG_REQUEST_NETWORK_STATUS = 15;
        private final static int MSG_REQUEST_PROVIDER_STATUS = 16;
        private final static int MSG_REQUEST_PROVIDER_LIST = 17;
        private final static int MSG_REQUEST_GPS_ENABLE = 18;
        private final static int MSG_PROXIMITY_ALERT = 19;
        private final static int MSG_UPDATE_WEATHER_LIVE = 20;
        private final static int MSG_UPDATE_WEATHER_FORECAST = 21;
        private final static int MSG_LOCATION_CHANGED = 22;
        private final static int MSG_GEOFENCE_ALERT = 23;
        private final static int MSG_NETWORK_STATUS_CHANGED = 24;
        private final static int MSG_GPS_STATUS_CHANGED = 25;

        private boolean m_channelAvailable;

        private boolean m_aMapLocationListenerRegistered;
        private boolean m_androidLocationListenerRegistered;
        private boolean m_gpsStatusListenerRegistered;
        private boolean m_networkListenerRegistered;

        private boolean m_weatherLiveRequesting;
        private boolean m_weatherLivePending;
        private boolean m_weatherForecastRequesting;
        private boolean m_weatherForecastPending;

        public void setChannelState(boolean available) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_CHANNEL_STATUS_CHANGED;
            msg.arg1 = available ? 1 : 0;

            msg.sendToTarget();
        }

        public void notifyLocationServiceConnected(boolean connected) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_LOCATION_SERVICE_CONNECTED;
            msg.arg1 = connected ? 1 : 0;

            msg.sendToTarget();
        }

        public void notifyNetworkState(boolean available) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_NETWORK_STATUS_CHANGED;
            msg.arg1 = available ? 1 : 0;

            msg.sendToTarget();
        }

        public void notifyGpsState(int status) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_GPS_STATUS_CHANGED;
            msg.arg1 = status;

            msg.sendToTarget();
        }

        public void notifyLocationChanged(RemoteLocation location) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_LOCATION_CHANGED;
            msg.obj = location;

            msg.sendToTarget();
        }

        public void notifyWeatherLive(RemoteWeatherLive weatherLive) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_UPDATE_WEATHER_LIVE;
            msg.obj = weatherLive;

            msg.sendToTarget();
        }

        public void notifyWeatherForecast(RemoteWeatherForecast weatherForecast) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_UPDATE_WEATHER_FORECAST;
            msg.obj = weatherForecast;

            msg.sendToTarget();
        }

        public void notifyGeoFenceAlert(boolean alert, String uuid) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            bundle.putString("uuid", uuid);
            bundle.putBoolean("alert", alert);

            msg.what = MSG_GEOFENCE_ALERT;
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void notifyProximityAlert(boolean alert, String uuid) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            bundle.putString("uuid", uuid);
            bundle.putBoolean("alert", alert);

            msg.what = MSG_PROXIMITY_ALERT;
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void handleRequest(RemoteLocationRequest request) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            switch (request.type) {
            case RemoteLocationRequest.TYPE_REGISTER_LOCATION_LISTENER:

                bundle.putString("provider", request.provider);
                bundle.putLong("minTime", request.minTime);
                bundle.putFloat("minDistance", request.minDistance);

                msg.what = MSG_REQUEST_REGISTER_LOCATION_LISTENER;
                msg.setData(bundle);

                break;

            case RemoteLocationRequest.TYPE_UNREGISTER_LOCATION_LISTENER:
                msg.what = MSG_REQUEST_UNREGISTER_LOCATION_LISTENER;
                msg.obj = request.provider;

                break;

            case RemoteLocationRequest.TYPE_WEATHER_UPDATE:
                msg.what = MSG_REQUEST_WEATHER_UPDATE;
                msg.arg1 = request.weatherType;

                break;

            case RemoteLocationRequest.TYPE_LAST_KNOWN_LOCATION:
                msg.what = MSG_REQUEST_LAST_KNOWN_LOCATION;
                msg.obj = request.provider;
                break;

            case RemoteLocationRequest.TYPE_REGISTER_GEOFENCE_LISTENER:
                msg.what = MSG_REQUEST_REGISTER_GEOFENCE_LISTENER;

                bundle.putDouble("latitude", request.latitude);
                bundle.putDouble("longitude", request.longitude);
                bundle.putFloat("radius", request.radius);
                bundle.putLong("expiration", request.expiration);
                bundle.putString("uuid", request.uuid);

                msg.setData(bundle);

                break;

            case RemoteLocationRequest.TYPE_UNREGISTER_GEOFENCE_LISTENER:
                msg.what = MSG_REQUEST_UNREGISTER_GEOFENCE_LISTENER;

                bundle.putString("uuid", request.uuid);
                msg.setData(bundle);
                break;

            case RemoteLocationRequest.TYPE_REGISTER_PROXIMITY_LISTENER:
                msg.what = MSG_REQUEST_REGISTER_PROXIMITY_LISTENER;

                bundle.putDouble("latitude", request.latitude);
                bundle.putDouble("longitude", request.longitude);
                bundle.putFloat("radius", request.radius);
                bundle.putLong("expiration", request.expiration);
                bundle.putString("uuid", request.uuid);

                msg.setData(bundle);
                break;

            case RemoteLocationRequest.TYPE_UNREGISTER_PROXIMITY_LISTENER:
                msg.what = MSG_REQUEST_UNREGISTER_PROXIMITY_LISTENER;

                bundle.putString("uuid", request.uuid);
                msg.setData(bundle);
                break;

            case RemoteLocationRequest.TYPE_REGISTER_GPS_STATUS_LISTENER:
                msg.what = MSG_REQUEST_REGISTER_GPS_STATUS_LISTENER;
                break;

            case RemoteLocationRequest.TYPE_UNREGISTER_GPS_STATUS_LISTENER:
                msg.what = MSG_REQUEST_UNREGISTER_GPS_STATUS_LISTENER;
                break;

            case RemoteLocationRequest.TYPE_REGISTER_NETWORK_STATUS_LISTENER:
                msg.what = MSG_REQUEST_REGISTER_NETWORK_STATUS_LISTENER;
                break;

            case RemoteLocationRequest.TYPE_UNREGISTER_NETWORK_STATUS_LISTENER:
                msg.what = MSG_REQUEST_UNREGISTER_NETWORK_STATUS_LISTENER;
                break;

            case RemoteLocationRequest.TYPE_GPS_STATUS:
                msg.what = MSG_REQUEST_GPS_STATUS;
                break;

            case RemoteLocationRequest.TYPE_NETWORK_STATUS:
                msg.what = MSG_REQUEST_NETWORK_STATUS;
                break;

            case RemoteLocationRequest.TYPE_PROVIDER_STATUS:
                msg.what = MSG_REQUEST_PROVIDER_STATUS;
                msg.obj = request.provider;
                break;

            case RemoteLocationRequest.TYPE_PROVIDER_LIST:
                msg.what = MSG_REQUEST_PROVIDER_LIST;
                msg.arg1 = request.enabledOnly ? 1 : 0;
                break;

            case RemoteLocationRequest.TYPE_GPS_ENABLE:
                msg.what = MSG_REQUEST_GPS_ENABLE;
                msg.arg1 = request.enabled ? 1 : 0;
                break;

            default:
                IwdsAssert.dieIf(this, true, "Unsupported request type: "
                        + request.type);
                return;
            }

            msg.sendToTarget();
        }

        @Override
        public void handleMessage(Message msg) {

            RemoteLocationResponse response = RemoteLocationResponse
                    .obtain(m_transactor);

            Bundle bundle = msg.getData();

            String uuid = bundle.getString("uuid");
            double latitude = bundle.getDouble("latitude");
            double longitude = bundle.getDouble("longitude");
            float radius = bundle.getFloat("radius");
            long expiration = bundle.getLong("expiration");

            GeoFenceAlertObserver genFenceAlertObserver = null;
            ProximityAlertObserver proximityAlertObserver = null;

            switch (msg.what) {
            case MSG_CHANNEL_STATUS_CHANGED:
                m_channelAvailable = msg.arg1 == 1 ? true : false;

                if (!m_channelAvailable) {

                    IwdsLog.i(this, "Channgel is unavailable");

                    if (m_aMapLocationListenerRegistered) {
                        m_locationManagerProxy
                                .removeUpdates(m_aMapLocationListener);
                        m_aMapLocationListenerRegistered = false;
                    }

                    if (m_androidLocationListenerRegistered) {
                        m_locationManager
                                .removeUpdates(m_androidLocationListener);
                        m_androidLocationListenerRegistered = false;
                    }

                    if (m_gpsStatusListenerRegistered) {
                        m_locationManagerProxy
                                .removeGpsStatusListener(m_gpsStatusListener);
                        m_gpsStatusListenerRegistered = false;
                    }

                    if (m_networkListenerRegistered) {
                        m_context.unregisterReceiver(m_networkReceiver);
                        m_networkListenerRegistered = false;
                    }

                    Collection<GeoFenceAlertObserver> geoFenceObservers = m_geoFenceAlertObservers
                            .values();
                    for (GeoFenceAlertObserver observer : geoFenceObservers)
                        observer.stopObserver();

                    m_geoFenceAlertObservers.clear();

                    Collection<ProximityAlertObserver> proximityObservers = m_proximityAlertObservers
                            .values();
                    for (ProximityAlertObserver observer : proximityObservers)
                        observer.stopObserver();

                    m_proximityAlertObservers.clear();

                    m_weatherLiveRequesting = false;
                    m_weatherLivePending = false;
                    m_weatherForecastRequesting = false;
                    m_weatherForecastPending = false;

                    m_locationManagerProxy.destroy();

                } else {
                    IwdsLog.i(this, "Channel is available");

                    m_locationManagerProxy = LocationManagerProxy
                            .getInstance(m_context);

                    IwdsLog.i(this, "AMap location SDK version: "
                            + LocationManagerProxy.getVersion());

                    notifyLocationServiceConnected(true);
                }

                break;

            case MSG_REQUEST_WEATHER_UPDATE:
                int weatherType = msg.arg1;

                if (!m_weatherLiveRequesting && !m_weatherForecastRequesting) {
                    IwdsLog.d(this, "request weather update: type="
                            + weatherType);

                    if (weatherType == RemoteLocationServiceManager.WEATHER_TYPE_LIVE)
                        m_weatherLiveRequesting = true;
                    else if (weatherType == RemoteLocationServiceManager.WEATHER_TYPE_FORECAST)
                        m_weatherForecastRequesting = true;

                    m_locationManagerProxy.requestWeatherUpdates(weatherType,
                            m_localWeatherListener);

                } else if (weatherType == RemoteLocationServiceManager.WEATHER_TYPE_LIVE) {
                    IwdsLog.d(this, "Pending weather live request");
                    m_weatherLivePending = true;

                } else if (weatherType == RemoteLocationServiceManager.WEATHER_TYPE_FORECAST) {
                    IwdsLog.d(this, "Pending weather forecast request");
                    m_weatherForecastPending = true;
                }

                break;

            case MSG_REQUEST_REGISTER_LOCATION_LISTENER:
                String provider = bundle.getString("provider");
                long minTime = bundle.getLong("minTime");
                float minDistance = bundle.getFloat("minDistance");

                if (provider.equals(RemoteLocationServiceManager.GPS_PROVIDER)) {
                    m_locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, minTime, minDistance,
                            m_androidLocationListener);

                    m_androidLocationListenerRegistered = true;

                } else if (provider
                        .equals(RemoteLocationServiceManager.IWDS_NETWORK_PROVIDER)) {

                    m_locationManagerProxy.setGpsEnable(false);
                    m_locationManagerProxy.requestLocationData(
                            LocationProviderProxy.AMapNetwork, minTime,
                            minDistance, m_aMapLocationListener);

                    m_aMapLocationListenerRegistered = true;
                }

                IwdsLog.d(this, "register location listener: provider="
                        + provider + " ,minTime=" + minTime + " ,minDistance="
                        + minDistance);

                break;

            case MSG_REQUEST_UNREGISTER_LOCATION_LISTENER:

                IwdsLog.d(this, "unregister location listener: "
                        + (String) msg.obj);

                if (((String) msg.obj)
                        .equals(RemoteLocationServiceManager.GPS_PROVIDER)) {

                    m_locationManager.removeUpdates(m_androidLocationListener);
                    m_androidLocationListenerRegistered = false;

                } else if (((String) msg.obj)
                        .equals(RemoteLocationServiceManager.IWDS_NETWORK_PROVIDER)) {

                    m_locationManagerProxy
                            .removeUpdates(m_aMapLocationListener);

                    m_aMapLocationListenerRegistered = false;
                }

                break;

            case MSG_REQUEST_REGISTER_GEOFENCE_LISTENER:
                genFenceAlertObserver = m_geoFenceAlertObservers.get(uuid);
                if (genFenceAlertObserver == null) {
                    genFenceAlertObserver = new GeoFenceAlertObserver(latitude,
                            longitude, radius, expiration, uuid);

                    genFenceAlertObserver.startObserver();
                    m_geoFenceAlertObservers.put(uuid, genFenceAlertObserver);

                    IwdsLog.d(this, "register geofence alert listener: uuid="
                            + uuid + ", latitude=" + latitude + " ,longtitude="
                            + longitude + ", radius=" + radius
                            + ", expiration=" + expiration);
                }

                break;

            case MSG_REQUEST_UNREGISTER_GEOFENCE_LISTENER:
                IwdsLog.d(this, "unregister gepfence alert listener: uuid="
                        + uuid);

                genFenceAlertObserver = m_geoFenceAlertObservers.remove(uuid);
                if (genFenceAlertObserver != null)
                    genFenceAlertObserver.stopObserver();

                break;

            case MSG_REQUEST_REGISTER_PROXIMITY_LISTENER:
                proximityAlertObserver = m_proximityAlertObservers.get(uuid);
                if (proximityAlertObserver == null) {
                    proximityAlertObserver = new ProximityAlertObserver(
                            latitude, longitude, radius, expiration, uuid);

                    proximityAlertObserver.startObserver();
                    m_proximityAlertObservers.put(uuid, proximityAlertObserver);

                    IwdsLog.d(this, "register proximity alert listener: uuid="
                            + uuid + ", latitude=" + latitude + " ,longtitude="
                            + longitude + ", radius=" + radius
                            + ", expiration=" + expiration);
                }
                break;

            case MSG_REQUEST_UNREGISTER_PROXIMITY_LISTENER:
                IwdsLog.d(this, "unregister gepfence alert listener: uuid="
                        + uuid);

                proximityAlertObserver = m_proximityAlertObservers.remove(uuid);
                if (proximityAlertObserver != null)
                    proximityAlertObserver.stopObserver();
                break;

            case MSG_REQUEST_REGISTER_GPS_STATUS_LISTENER:

                IwdsLog.d(this, "register GPS status listener");

                m_locationManagerProxy
                        .addGpsStatusListener(m_gpsStatusListener);

                m_gpsStatusListenerRegistered = true;
                break;

            case MSG_REQUEST_UNREGISTER_GPS_STATUS_LISTENER:

                IwdsLog.d(this, "unregister GPS status listener");

                m_locationManagerProxy
                        .removeGpsStatusListener(m_gpsStatusListener);

                m_gpsStatusListenerRegistered = false;

                break;

            case MSG_REQUEST_REGISTER_NETWORK_STATUS_LISTENER:

                IwdsLog.d(this, "register network status listener");

                m_context.registerReceiver(m_networkReceiver, m_networkFilter);

                m_networkListenerRegistered = true;

                break;

            case MSG_REQUEST_UNREGISTER_NETWORK_STATUS_LISTENER:

                IwdsLog.d(this, "unregister network status listener");

                m_context.unregisterReceiver(m_networkReceiver);

                m_networkListenerRegistered = false;

                break;

            case MSG_REQUEST_GPS_STATUS:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                RemoteGpsStatus gpsStatus = ProxyUtils
                        .buildRemoteGpsStatus(m_locationManager
                                .getGpsStatus(null));

                response.type = RemoteLocationResponse.TYPE_GPS_STATUS;
                response.gpsStatus = gpsStatus;

                IwdsLog.d(this, "Notify GPS current status: " + gpsStatus);

                response.sendToRemote();
                break;

            case MSG_REQUEST_NETWORK_STATUS:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                NetworkInfo networkInfo = m_connectivityManager
                        .getActiveNetworkInfo();

                int status = 0;

                if (networkInfo != null && networkInfo.isAvailable())
                    status = 1;

                response.type = RemoteLocationResponse.TYPE_NETWORK_STATUS;
                response.networkState = status;

                IwdsLog.d(this, "Notify network current status: " + status);

                response.sendToRemote();
                break;

            case MSG_REQUEST_PROVIDER_STATUS:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                boolean enabled = m_locationManagerProxy
                        .isProviderEnabled((String) msg.obj);

                response.type = RemoteLocationResponse.TYPE_PROVIDER_STATUS;
                response.enabled = enabled;
                response.provider = (String) msg.obj;

                IwdsLog.d(this, "Notify provider is enabled, provider="
                        + (String) msg.obj + ", enabled=" + enabled);

                response.sendToRemote();
                break;

            case MSG_REQUEST_PROVIDER_LIST:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                List<String> providerList = new ArrayList<String>();

                if (msg.arg1 == 1)
                    providerList = m_locationManagerProxy.getProviders(true);
                else
                    providerList = m_locationManagerProxy.getProviders(false);

                response.type = RemoteLocationResponse.TYPE_PROVIDERS;
                response.enabledOnly = msg.arg1 == 1 ? true : false;
                response.providerList = (ArrayList<String>) providerList;

                IwdsLog.d(this, "Notify provider all providers: enabledOnly="
                        + response.enabledOnly + ", providerList"
                        + response.providerList);

                response.sendToRemote();
                break;

            case MSG_REQUEST_GPS_ENABLE:

                m_locationManagerProxy.setGpsEnable(msg.arg1 == 1 ? true
                        : false);

                IwdsLog.d(this, "set GPS enabled: " + msg.arg1);

                break;

            case MSG_LOCATION_SERVICE_CONNECTED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteLocationResponse.TYPE_LOCATION_SERVICE_STATUS;
                response.serviceConnected = msg.arg1 == 1 ? true : false;

                IwdsLog.d(this, "Notify location service connected: "
                        + (msg.arg1 == 1 ? true : false));

                response.sendToRemote();
                break;

            case MSG_UPDATE_WEATHER_LIVE:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                // TODO: AMap may callback twice
                if (!m_weatherLiveRequesting)
                    return;

                response.type = RemoteLocationResponse.TYPE_WEATHER_LIVE;
                response.weatherLive = (RemoteWeatherLive) msg.obj;

                IwdsLog.d(this, "Notify update weather live: weatherLive="
                        + response.weatherLive);

                response.sendToRemote();

                m_weatherLiveRequesting = false;

                if (m_weatherForecastPending) {
                    IwdsLog.d(this,
                            "request weather forecast pending, try request again.");

                    m_weatherForecastPending = false;
                    Message.obtain(this, MSG_REQUEST_WEATHER_UPDATE,
                            RemoteLocationServiceManager.WEATHER_TYPE_FORECAST,
                            0).sendToTarget();
                }

                break;

            case MSG_UPDATE_WEATHER_FORECAST:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                // TODO: AMap may callback twice
                if (!m_weatherForecastRequesting)
                    return;

                response.type = RemoteLocationResponse.TYPE_WEATHER_FORECAST;
                response.weatherForecast = (RemoteWeatherForecast) msg.obj;

                IwdsLog.d(this,
                        "Notify update weather forecast: weatherForecast="
                                + response.weatherForecast);

                response.sendToRemote();

                m_weatherForecastRequesting = false;

                if (m_weatherLivePending) {
                    IwdsLog.d(this,
                            "request weather live pending, try request again.");

                    m_weatherLivePending = false;
                    Message.obtain(this, MSG_REQUEST_WEATHER_UPDATE,
                            RemoteLocationServiceManager.WEATHER_TYPE_LIVE, 0)
                            .sendToTarget();
                }

                break;

            case MSG_REQUEST_LAST_KNOWN_LOCATION:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                RemoteLocation remoteLocation = null;

                if (((String) msg.obj)
                        .equals(RemoteLocationServiceManager.GPS_PROVIDER)) {
                    Location androidLocation = m_locationManager
                            .getLastKnownLocation((String) msg.obj);

                    remoteLocation = ProxyUtils
                            .buildRemoteLocation(androidLocation);

                    if (androidLocation != null) {
                        // World Geodetic System ==> Mars Geodetic System
                        Gps marsGps = ProxyUtils.transformWgs84ToGcj02(
                                androidLocation.getLatitude(),
                                androidLocation.getLongitude());

                        remoteLocation.setLatitude(marsGps.getWgLat());
                        remoteLocation.setLongitude(marsGps.getWgLon());
                    }

                } else if (((String) msg.obj)
                        .equals(RemoteLocationServiceManager.IWDS_NETWORK_PROVIDER)) {
                    AMapLocation aMapLocation = m_locationManagerProxy
                            .getLastKnownLocation((String) msg.obj);

                    remoteLocation = ProxyUtils
                            .buildRemoteLocation(aMapLocation);
                }

                response.type = RemoteLocationResponse.TYPE_LAST_KNOWN_LOCATION;

                response.location = remoteLocation;

                IwdsLog.d(this, "Notify las known location: lastKnownLocation="
                        + response.location);

                response.sendToRemote();
                break;

            case MSG_GEOFENCE_ALERT:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteLocationResponse.TYPE_GEOFENCE_ALERT;
                response.uuid = uuid;
                response.geofenceAlertState = bundle.getBoolean("alert") ? 1
                        : 0;

                IwdsLog.d(this, "Notify GenFence alert: uuid=" + uuid
                        + "status=" + response.geofenceAlertState);

                response.sendToRemote();

                break;

            case MSG_PROXIMITY_ALERT:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }
                response.type = RemoteLocationResponse.TYPE_PROXIMITY_ALERT;
                response.uuid = uuid;
                response.proximityAlertState = bundle.getBoolean("alert") ? 1
                        : 0;

                IwdsLog.d(this, "Notify proximity alert: uuid=" + uuid
                        + "status=" + response.proximityAlertState);

                response.sendToRemote();
                break;

            case MSG_LOCATION_CHANGED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteLocationResponse.TYPE_LOCATION_CHANGED;
                response.location = (RemoteLocation) msg.obj;

                IwdsLog.d(this, "Notify location changed: locaton="
                        + response.location);

                response.sendToRemote();

                break;

            case MSG_NETWORK_STATUS_CHANGED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteLocationResponse.TYPE_NETWORK_STATUS_CHANGED;
                response.networkState = msg.arg1;

                IwdsLog.d(this, "Notify network status changed: status="
                        + response.networkState);

                response.sendToRemote();
                break;

            case MSG_GPS_STATUS_CHANGED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteLocationResponse.TYPE_GPS_STATUS_CHANGED;
                response.gpsEvent = msg.arg1;

                IwdsLog.d(this, "Notify GPS status changed: event="
                        + response.gpsEvent);

                response.sendToRemote();
                break;

            default:
                IwdsAssert.dieIf(this, true, "Unknown message");
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
            m_handler.setChannelState(isAvailable);
        }

        @Override
        public void onSendResult(DataTransactResult result) {

        }

        @Override
        public void onDataArrived(Object object) {
            if (object instanceof RemoteLocationRequest)
                m_handler.handleRequest((RemoteLocationRequest) object);
        }

        @Override
        public void onSendFileProgress(int progress) {

        }

        @Override
        public void onRecvFileProgress(int progress) {

        }

        @Override
        public void onSendFileInterrupted(int index) {

        }

        @Override
        public void onRecvFileInterrupted(int index) {

        }

    };
    /* -------------------- DataTransactorCallback end ----------------------- */

}
