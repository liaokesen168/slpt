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

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.smartlocation.search.busline.IRemoteBusLineSearchCallback;
import com.ingenic.iwds.smartlocation.search.busline.IRemoteBusStationSearchCallback;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusLineQuery;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusLineResult;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusLineSearch;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusLineSearch.RemoteBusLineSearchListener;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusStationQuery;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusStationResult;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusStationSearch;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusStationSearch.RemoteBusStationSearchListener;
import com.ingenic.iwds.smartlocation.search.district.IRemoteDistrictSearchCallback;
import com.ingenic.iwds.smartlocation.search.district.RemoteDistrictQuery;
import com.ingenic.iwds.smartlocation.search.district.RemoteDistrictResult;
import com.ingenic.iwds.smartlocation.search.district.RemoteDistrictSearch;
import com.ingenic.iwds.smartlocation.search.district.RemoteDistrictSearch.RemoteDistrictSearchListener;
import com.ingenic.iwds.smartlocation.search.geocoder.IRemoteGeocodeSearchCallback;
import com.ingenic.iwds.smartlocation.search.geocoder.IRemoteRegeocodeSearchCallback;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteGeocodeQuery;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteGeocodeResult;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteGeocodeSearch;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteGeocodeSearch.RemoteGeocodeSearchListener;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteRegeocodeQuery;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteRegeocodeResult;
import com.ingenic.iwds.smartlocation.search.help.IRemoteInputtipsCallback;
import com.ingenic.iwds.smartlocation.search.help.RemoteInputQuery;
import com.ingenic.iwds.smartlocation.search.help.RemoteInputtips;
import com.ingenic.iwds.smartlocation.search.help.RemoteInputtips.RemoteInputtipsListener;
import com.ingenic.iwds.smartlocation.search.help.RemoteTip;
import com.ingenic.iwds.smartlocation.search.poisearch.IRemotePoiDetailSearchCallback;
import com.ingenic.iwds.smartlocation.search.poisearch.IRemotePoiSearchCallback;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiItemDetail;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiQuery;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiResult;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiSearch;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiSearch.RemotePoiSearchListener;
import com.ingenic.iwds.smartlocation.search.route.IRemoteBusRouteSearchCallback;
import com.ingenic.iwds.smartlocation.search.route.IRemoteDriveRouteSearchCallback;
import com.ingenic.iwds.smartlocation.search.route.IRemoteWalkRouteSearchCallback;
import com.ingenic.iwds.smartlocation.search.route.RemoteBusRouteQuery;
import com.ingenic.iwds.smartlocation.search.route.RemoteBusRouteResult;
import com.ingenic.iwds.smartlocation.search.route.RemoteDriveRouteQuery;
import com.ingenic.iwds.smartlocation.search.route.RemoteDriveRouteResult;
import com.ingenic.iwds.smartlocation.search.route.RemoteRouteSearch;
import com.ingenic.iwds.smartlocation.search.route.RemoteRouteSearch.RemoteRouteSearchListener;
import com.ingenic.iwds.smartlocation.search.route.RemoteWalkRouteQuery;
import com.ingenic.iwds.smartlocation.search.route.RemoteWalkRouteResult;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

public class RemoteSearchServiceManager extends ServiceManagerContext {
    private IRemoteSearchService m_service;
    private boolean m_remoteAvailable;

    private HashMap<RemoteStatusListener, RemoteStatusCallback> m_remoteStatusListeners;
    private HashMap<RemoteGeocodeSearchListener, RemoteGeocodeSearchCallback> m_geocodeSearchListners;
    private HashMap<RemoteGeocodeSearchListener, RemoteRegeocodeSearchCallback> m_regeocodeSearchListners;
    private HashMap<RemoteDistrictSearchListener, RemoteDistrictSearchCallback> m_districtSearchListeners;
    private HashMap<RemotePoiSearchListener, RemotePoiSearchCallback> m_poiSearchListeners;
    private HashMap<RemotePoiSearchListener, RemotePoiDetailSearchCallback> m_poiDetailSearchListeners;
    private HashMap<RemoteInputtipsListener, RemoteInputtipsCallback> m_inputTipsListeners;
    private HashMap<RemoteBusLineSearchListener, RemoteBusLineSearchCallback> m_busLineListeners;
    private HashMap<RemoteBusStationSearchListener, RemoteBusStationSearchCallback> m_busStationListeners;
    private HashMap<RemoteRouteSearchListener, RemoteBusRouteSearchCallback> m_busRouteListeners;
    private HashMap<RemoteRouteSearchListener, RemoteDriveRouteSearchCallback> m_driveRouteListeners;
    private HashMap<RemoteRouteSearchListener, RemoteWalkRouteSearchCallback> m_walkRouteListeners;

    public RemoteSearchServiceManager(Context context) {
        super(context);

        m_remoteStatusListeners = new HashMap<RemoteStatusListener, RemoteStatusCallback>();
        m_geocodeSearchListners = new HashMap<RemoteGeocodeSearchListener, RemoteGeocodeSearchCallback>();
        m_regeocodeSearchListners = new HashMap<RemoteGeocodeSearchListener, RemoteRegeocodeSearchCallback>();
        m_districtSearchListeners = new HashMap<RemoteDistrictSearchListener, RemoteDistrictSearchCallback>();
        m_poiSearchListeners = new HashMap<RemotePoiSearchListener, RemotePoiSearchCallback>();
        m_poiDetailSearchListeners = new HashMap<RemotePoiSearchListener, RemotePoiDetailSearchCallback>();
        m_inputTipsListeners = new HashMap<RemoteInputtipsListener, RemoteInputtipsCallback>();
        m_busLineListeners = new HashMap<RemoteBusLineSearchListener, RemoteBusLineSearchCallback>();
        m_busStationListeners = new HashMap<RemoteBusStationSearchListener, RemoteBusStationSearchCallback>();
        m_busRouteListeners = new HashMap<RemoteRouteSearchListener, RemoteBusRouteSearchCallback>();
        m_driveRouteListeners = new HashMap<RemoteRouteSearchListener, RemoteDriveRouteSearchCallback>();
        m_walkRouteListeners = new HashMap<RemoteRouteSearchListener, RemoteWalkRouteSearchCallback>();

        m_serviceClientProxy = new ServiceClientProxy() {

            @Override
            public void onServiceConnected(IBinder binder) {
                m_service = IRemoteSearchService.Stub.asInterface(binder);
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

    /* ------------------ RemoteStatusCallback ---------------------- */
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
                    m_remoteAvailable = (Boolean) msg.obj;

                    if (!m_remoteAvailable)
                        unregisterAllListeners();

                    if (m_remoteAvailable != status)
                        m_listener.onAvailable(m_remoteAvailable);

                    status = m_remoteAvailable;

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
     * 用于注册远端设备搜索服务状态的监听器
     * {@link com.ingenic.iwds.smartlocation.search.RemoteStatusListener
     * RemoteStatusListener}
     *
     * @param listener
     *            监听器对象
     *            {@link com.ingenic.iwds.smartlocation.search.RemoteStatusListener
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
     * 注销远端设备搜索服务状态的监听器
     * {@link com.ingenic.iwds.smartlocation.search.RemoteStatusListener
     * RemoteStatusListener}
     * 
     * @param listener
     *            监听器对象
     *            {@link com.ingenic.iwds.smartlocation.search.RemoteStatusListener
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

    /* ------------------ RemoteStatusCallback end ---------------------- */

    /* -------------- Geocode/Regeocode search callback -------------- */
    private class RemoteGeocodeSearchCallback extends
            IRemoteGeocodeSearchCallback.Stub {
        private static final int MSG_ON_GEOCODE_SEARCHED = 0;

        private RemoteGeocodeSearchListener m_listener;
        String uuid;

        public RemoteGeocodeSearchCallback(RemoteGeocodeSearchListener listener) {
            m_listener = listener;
            uuid = UUID.randomUUID().toString();
        }

        private Handler m_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {

                case MSG_ON_GEOCODE_SEARCHED:
                    m_geocodeSearchListners.remove(m_listener);

                    m_listener.onGeocodeSearched((RemoteGeocodeResult) msg.obj,
                            msg.arg1);

                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");
                }
            }
        };

        @Override
        public void onGeocodeSearched(RemoteGeocodeResult result, int errorCode)
                throws RemoteException {
            Message.obtain(m_handler, MSG_ON_GEOCODE_SEARCHED, errorCode, 0,
                    result).sendToTarget();
        }
    }

    private class RemoteRegeocodeSearchCallback extends
            IRemoteRegeocodeSearchCallback.Stub {
        private static final int MSG_ON_REGEOCODE_SEARCHED = 0;

        private RemoteGeocodeSearchListener m_listener;
        String uuid;

        public RemoteRegeocodeSearchCallback(
                RemoteGeocodeSearchListener listener) {
            m_listener = listener;
            uuid = UUID.randomUUID().toString();
        }

        private Handler m_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_ON_REGEOCODE_SEARCHED:
                    m_regeocodeSearchListners.remove(m_listener);

                    m_listener.onRegeocodeSearched(
                            (RemoteRegeocodeResult) msg.obj, msg.arg1);

                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");
                }
            }
        };

        @Override
        public void onRegeocodeSearched(RemoteRegeocodeResult result,
                int errorCode) throws RemoteException {
            Message.obtain(m_handler, MSG_ON_REGEOCODE_SEARCHED, errorCode, 0,
                    result).sendToTarget();
        }
    }

    /**
     * 根据给定的地理名称和查询城市请求获取地理编码的结果列表
     * 
     * @param search
     *            地理编码搜索对象
     * 
     * @return true 请求成功, false 请求失败
     */
    public boolean requestGeocodeSearch(RemoteGeocodeSearch search) {
        if (!m_remoteAvailable) {
            IwdsLog.e(this, "Search service on remote device unavailable");
            return false;
        }

        IwdsAssert.dieIf(this, search == null, "search is null");

        RemoteGeocodeQuery query = search.getGeocodeQuery();
        IwdsAssert.dieIf(this, query == null, "query is null");

        RemoteGeocodeSearchListener listener = search
                .getGeocodeSearchListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        RemoteGeocodeSearchCallback callback = m_geocodeSearchListners
                .get(search.getGeocodeSearchListener());

        if (callback == null) {
            callback = new RemoteGeocodeSearchCallback(listener);
            m_geocodeSearchListners.put(listener, callback);

        } else {
            return true;
        }

        try {
            m_service.requestGeocodeSearch(callback, callback.uuid, query);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in requestGeocode: " + e.toString());
            return false;
        }

        return true;
    }

    /**
     * 根据给定的经纬度和最大结果数请求获取逆地理编码的结果列表
     * 
     * @param search
     *            地理（逆）编码搜索对象
     * 
     * @return true 请求成功, false 请求失败
     */
    public boolean requestRegeocodeSearch(RemoteGeocodeSearch search) {
        if (!m_remoteAvailable) {
            IwdsLog.e(this, "Search service on remote device unavailable");
            return false;
        }

        IwdsAssert.dieIf(this, search == null, "search is null");

        RemoteRegeocodeQuery query = search.getRegeocodeQuery();
        IwdsAssert.dieIf(this, query == null, "query is null");

        RemoteGeocodeSearchListener listener = search
                .getGeocodeSearchListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        RemoteRegeocodeSearchCallback callback = m_regeocodeSearchListners
                .get(search.getGeocodeSearchListener());

        if (callback == null) {
            callback = new RemoteRegeocodeSearchCallback(listener);
            m_regeocodeSearchListners.put(listener, callback);

        } else {
            return true;
        }

        try {
            m_service.requestRegeocodeSearch(callback, callback.uuid, query);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in requestRegeocode: " + e.toString());
            return false;
        }

        return true;
    }

    /* -------------- Geocode/Regeocode search callback end -------------- */

    /* -------------- District search callback -------------- */
    private class RemoteDistrictSearchCallback extends
            IRemoteDistrictSearchCallback.Stub {
        private static final int MSG_ON_DISTRICT_SEARCHED = 0;
        private RemoteDistrictSearchListener m_listener;
        String uuid;

        private RemoteDistrictSearchCallback(
                RemoteDistrictSearchListener listener) {
            m_listener = listener;
            uuid = UUID.randomUUID().toString();
        }

        private Handler m_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_ON_DISTRICT_SEARCHED:
                    m_districtSearchListeners.remove(m_listener);

                    m_listener.onDistrictSearched(
                            (RemoteDistrictResult) msg.obj, msg.arg1);
                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");
                    break;
                }
            }

        };

        @Override
        public void onDistrictSearched(RemoteDistrictResult result,
                int errorCode) throws RemoteException {
            Message.obtain(m_handler, MSG_ON_DISTRICT_SEARCHED, errorCode, 0,
                    result).sendToTarget();
            ;
        }

    }

    /**
     * 请求查询行政区
     * 
     * @param search
     *            行政区搜索对象
     * 
     * @return true 请求成功, false 请求失败
     */
    public boolean requestDistrictSearch(RemoteDistrictSearch search) {
        if (!m_remoteAvailable) {
            IwdsLog.e(this, "Search service on remote device unavailable");
            return false;
        }
        IwdsAssert.dieIf(this, search == null, "search is null");

        RemoteDistrictQuery query = search.getQuery();

        RemoteDistrictSearchListener listener = search.getDistrictListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        RemoteDistrictSearchCallback callback = m_districtSearchListeners
                .get(listener);
        if (callback == null) {
            callback = new RemoteDistrictSearchCallback(listener);
            m_districtSearchListeners.put(listener, callback);

        } else {
            return true;
        }

        try {
            m_service.requestDistrictSearch(callback, callback.uuid, query);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in requestDistrictSearch: " + e.toString());
            return false;
        }

        return true;
    }

    /* -------------- District search callback end -------------- */

    /* -------------- Poi/PoiDetail search callback -------------- */
    private class RemotePoiSearchCallback extends IRemotePoiSearchCallback.Stub {
        private final int MSG_ON_POI_SEARCHED = 0;

        private RemotePoiSearchListener m_listener;
        String uuid;

        RemotePoiSearchCallback(RemotePoiSearchListener listener) {
            m_listener = listener;
            uuid = UUID.randomUUID().toString();
        }

        private Handler m_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_ON_POI_SEARCHED:
                    m_poiSearchListeners.remove(m_listener);

                    m_listener.onPoiSearched((RemotePoiResult) msg.obj,
                            msg.arg1);
                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");
                    break;
                }
            }

        };

        @Override
        public void onPoiSearched(RemotePoiResult result, int errorCode)
                throws RemoteException {
            Message.obtain(m_handler, MSG_ON_POI_SEARCHED, errorCode, 0, result)
                    .sendToTarget();
        }
    }

    /**
     * 请求查询POI，
     * 如果此时矩形查询（RemotePoiSearchBound）已定义，则搜索范围为该矩形内的符合条件的POI，否则如果此时查询条件（Query）
     * 中的行政区划代码已定义
     * ，则搜索该城市（地区）内的所有符合条件的POI,如上述两个条件均未定义，则搜索范围为全国。建议定义一个范围：矩形或行政区划代码均可
     * 。整个查询的语义为查找符合查询关键字，符合查询类型编码组合及在指定范围内的指定POI。查询POI的结果，结果是分页的，每页最多30个
     * 
     * @param search
     *            POI搜索对象
     * @return true 请求成功, false 请求失败
     */
    public boolean requestPoiSearch(RemotePoiSearch search) {
        if (!m_remoteAvailable) {
            IwdsLog.e(this, "Search service on remote device unavailable");
            return false;
        }
        IwdsAssert.dieIf(this, search == null, "search is null");

        RemotePoiSearchListener listener = search.getPoiSearchListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        RemotePoiQuery query = search.getQuery();
        IwdsAssert.dieIf(this, query == null, "query is null");

        RemotePoiSearchCallback callback = m_poiSearchListeners.get(listener);

        if (callback == null) {
            callback = new RemotePoiSearchCallback(listener);
            m_poiSearchListeners.put(listener, callback);

        } else {
            return true;
        }

        try {
            m_service.requestPoiSearch(callback, callback.uuid, query);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in requestPoiSearch: " + e.toString());
            return false;
        }

        return true;
    }

    private class RemotePoiDetailSearchCallback extends
            IRemotePoiDetailSearchCallback.Stub {
        private static final int MSG_ON_POI_DETAIL_SEARCHED = 0;
        private RemotePoiSearchListener m_listener;
        String uuid;

        RemotePoiDetailSearchCallback(RemotePoiSearchListener listener) {
            m_listener = listener;
            uuid = UUID.randomUUID().toString();
        }

        private Handler m_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_ON_POI_DETAIL_SEARCHED:
                    m_poiDetailSearchListeners.remove(m_listener);

                    m_listener.onPoiItemDetailSearched(
                            (RemotePoiItemDetail) msg.obj, msg.arg1);
                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");
                    break;
                }
            }

        };

        @Override
        public void onPoiItemDetailSearched(RemotePoiItemDetail poiItemDetail,
                int errorCode) throws RemoteException {
            Message.obtain(m_handler, MSG_ON_POI_DETAIL_SEARCHED, errorCode, 0,
                    poiItemDetail).sendToTarget();
        }
    }

    /**
     * 按照POI的ID查找POI详细信息，ID需要存放在RemotePoiQuery中。
     * 
     * @param search
     *            POI查询对象
     * @return true 请求成功, false 请求失败
     */
    public boolean requestPoiDetailSearch(RemotePoiSearch search) {
        if (!m_remoteAvailable) {
            IwdsLog.e(this, "Search service on remote device unavailable");
            return false;
        }

        IwdsAssert.dieIf(this, search == null, "search is null");

        RemotePoiSearchListener listener = search.getPoiSearchListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        RemotePoiQuery query = search.getQuery();
        IwdsAssert.dieIf(this, query == null, "query is null");

        String poiId = query.getPoiId();
        IwdsAssert.dieIf(this, poiId == null || poiId.isEmpty(),
                "poiId is null or empty");

        RemotePoiDetailSearchCallback callback = m_poiDetailSearchListeners
                .get(listener);

        if (callback == null) {
            callback = new RemotePoiDetailSearchCallback(listener);
            m_poiDetailSearchListeners.put(listener, callback);

        } else {
            return true;
        }

        try {
            m_service.requestPoiDetailSearch(callback, callback.uuid, poiId);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in requestPoiDetailSearch: " + e.toString());
            return false;
        }

        return true;
    }

    /* -------------- Poi/PoiDetail search callback end -------------- */

    /* -------------- Input Tips search callback -------------- */
    private class RemoteInputtipsCallback extends IRemoteInputtipsCallback.Stub {
        private static final int MSG_ON_GET_INPUT_TIPS = 0;

        private RemoteInputtipsListener m_listener;
        private String uuid;

        RemoteInputtipsCallback(RemoteInputtipsListener listener) {
            m_listener = listener;
            uuid = UUID.randomUUID().toString();
        }

        private Handler m_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_ON_GET_INPUT_TIPS:
                    m_inputTipsListeners.remove(m_listener);

                    m_listener.onGetInputtips((List<RemoteTip>) msg.obj,
                            msg.arg1);
                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");
                    break;
                }
            }

        };

        @Override
        public void onGetInputtips(List<RemoteTip> tipList, int errorCode)
                throws RemoteException {
            Message.obtain(m_handler, MSG_ON_GET_INPUT_TIPS, errorCode, 0,
                    tipList).sendToTarget();
        }
    }

    /**
     * 请求获取发送输入提示请求
     * 
     * @param tips
     *            输入请求对象
     * 
     * @return true 请求成功, false 请求失败
     */
    public boolean requestInputtips(RemoteInputtips tips) {
        if (!m_remoteAvailable) {
            IwdsLog.e(this, "Search service on remote device unavailable");
            return false;
        }

        IwdsAssert.dieIf(this, tips == null, "tips is null");

        RemoteInputtipsListener listener = tips.getRemoteInputtipsListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        RemoteInputQuery query = tips.getQuery();
        IwdsAssert.dieIf(this, query == null, "query is null");

        IwdsAssert.dieIf(this, query.getKeyword() == null
                || query.getKeyword().isEmpty(), "keyword is null or empty");

        RemoteInputtipsCallback callback = m_inputTipsListeners.get(listener);

        if (callback == null) {
            callback = new RemoteInputtipsCallback(listener);
            m_inputTipsListeners.put(listener, callback);
        } else {
            return true;
        }

        try {
            m_service.requestInputtips(callback, callback.uuid, query);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in requestInputtips: " + e.toString());
            return false;
        }

        return true;
    }

    /* -------------- Input Tips search callback end -------------- */

    /* -------------- Bus line search callback -------------- */
    private class RemoteBusLineSearchCallback extends
            IRemoteBusLineSearchCallback.Stub {
        private static final int MSG_ON_BUS_LINE_SEARCHED = 0;

        private RemoteBusLineSearchListener m_listener;
        String uuid;

        RemoteBusLineSearchCallback(RemoteBusLineSearchListener listener) {
            m_listener = listener;
            uuid = UUID.randomUUID().toString();
        }

        private Handler m_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_ON_BUS_LINE_SEARCHED:
                    m_busLineListeners.remove(m_listener);

                    m_listener.onBusLineSearched((RemoteBusLineResult) msg.obj,
                            msg.arg1);
                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");
                    break;
                }
            }

        };

        @Override
        public void onBusLineSearched(RemoteBusLineResult result, int errorCode)
                throws RemoteException {
            Message.obtain(m_handler, MSG_ON_BUS_LINE_SEARCHED, errorCode, 0,
                    result).sendToTarget();
        }

    }

    /**
     * 请求搜索公交线路，
     * 如果此时查询条件（RemoteBusLineQuery）中的行政区划代码已定义，则查询为该城市（地区）内的所有符合条件的POI,否则范围为全国
     * 。根据指定查询类型和关键字搜索公交线路结果。
     * 
     * @param search
     *            公交线路搜索对象
     * @return true 请求成功, false 请求失败
     */
    public boolean requestBusLineSearch(RemoteBusLineSearch search) {
        if (!m_remoteAvailable) {
            IwdsLog.e(this, "Search service on remote device unavailable");
            return false;
        }

        IwdsAssert.dieIf(this, search == null, "search is null");

        RemoteBusLineSearchListener listener = search
                .getBusLineSearchListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        RemoteBusLineQuery query = search.getQuery();
        IwdsAssert.dieIf(this, query == null, "query is null");

        RemoteBusLineSearchCallback callback = m_busLineListeners.get(listener);

        if (callback == null) {
            callback = new RemoteBusLineSearchCallback(listener);
        } else {
            return true;
        }

        try {
            m_service.requestBusLineSearch(callback, callback.uuid, query);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in requestBusLineSearch: " + e.toString());
            return false;
        }

        return true;
    }

    /* -------------- Bus line search callback end -------------- */

    /* -------------- Bus station search callback -------------- */
    private class RemoteBusStationSearchCallback extends
            IRemoteBusStationSearchCallback.Stub {
        private static final int MSG_ON_BUS_STATION_SEARCHED = 0;
        private RemoteBusStationSearchListener m_listener;
        String uuid;

        RemoteBusStationSearchCallback(RemoteBusStationSearchListener listener) {
            m_listener = listener;
            uuid = UUID.randomUUID().toString();
        }

        private Handler m_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_ON_BUS_STATION_SEARCHED:
                    m_busStationListeners.remove(m_listener);

                    m_listener.onBusStationSearched(
                            (RemoteBusStationResult) msg.obj, msg.arg1);
                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");
                    break;
                }
            }
        };

        @Override
        public void onBusStationSearched(RemoteBusStationResult result,
                int errorCode) throws RemoteException {
            Message.obtain(m_handler, MSG_ON_BUS_STATION_SEARCHED, errorCode,
                    0, result).sendToTarget();
        }
    }

    /**
     * 请求搜索公交站点，
     * 如果此时查询条件（RemoteBusStationQuery）中的行政区划代码已定义，则查询为该城市（地区）内的所有符合条件的POI
     * ,否则范围为全国 。根据指定查询类型和关键字搜索公交站点结果
     * 
     * @param search
     *            公交站点搜索对象
     * @return true 请求成功, false 请求失败
     */
    public boolean requestBusStationSearch(RemoteBusStationSearch search) {
        if (!m_remoteAvailable) {
            IwdsLog.e(this, "Search service on remote device unavailable");
            return false;
        }

        IwdsAssert.dieIf(this, search == null, "search is null");

        RemoteBusStationSearchListener listener = search
                .getBusStationSearchListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        RemoteBusStationQuery query = search.getQuery();
        IwdsAssert.dieIf(this, query == null, "query is null");

        RemoteBusStationSearchCallback callback = m_busStationListeners
                .get(listener);

        if (callback == null) {
            callback = new RemoteBusStationSearchCallback(listener);
        } else {
            return true;
        }

        try {
            m_service.requestBusStationSearch(callback, callback.uuid, query);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in requestBusStationSearch: " + e.toString());
            return false;
        }

        return true;
    }

    /* -------------- Bus station search callback end -------------- */

    /* -------------- Bus route search callback -------------- */
    private class RemoteBusRouteSearchCallback extends
            IRemoteBusRouteSearchCallback.Stub {
        private final int MSG_ON_BUS_ROUTE_SEARCHED = 0;

        private RemoteRouteSearchListener m_listener;
        String uuid;

        RemoteBusRouteSearchCallback(RemoteRouteSearchListener listener) {
            m_listener = listener;
            uuid = UUID.randomUUID().toString();
        }

        private Handler m_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_ON_BUS_ROUTE_SEARCHED:
                    m_busRouteListeners.remove(m_listener);

                    m_listener.onBusRouteSearched(
                            (RemoteBusRouteResult) msg.obj, msg.arg1);
                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");
                    break;
                }
            }

        };

        @Override
        public void onBusRouteSearched(RemoteBusRouteResult result,
                int errorCode) throws RemoteException {
            Message.obtain(m_handler, MSG_ON_BUS_ROUTE_SEARCHED, errorCode, 0,
                    result).sendToTarget();
        }
    }

    /**
     * 根据指定的参数来请求获取公交路径。只支持市内公交换乘
     * 
     * @param search
     *            公交路径搜索对象
     * @return true 请求成功, false 请求失败
     */
    public boolean requestBusRouteSearch(RemoteRouteSearch search) {
        if (!m_remoteAvailable) {
            IwdsLog.e(this, "Search service on remote device unavailable");
            return false;
        }

        IwdsAssert.dieIf(this, search == null, "search is null");

        RemoteRouteSearchListener listener = search.getRouteSearchListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        RemoteBusRouteQuery query = search.getBusRouteQuery();
        IwdsAssert.dieIf(this, query == null, "query is null");

        RemoteBusRouteSearchCallback callback = m_busRouteListeners
                .get(listener);

        if (callback == null) {
            callback = new RemoteBusRouteSearchCallback(listener);
            m_busRouteListeners.put(listener, callback);
        } else {
            return true;
        }

        try {
            m_service.requestBusRouteSearch(callback, callback.uuid, query);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in requestBusRouteSearch: " + e.toString());
            return false;
        }

        return true;
    }

    /* -------------- Bus route search callback end -------------- */

    /* -------------- Drive route search callback -------------- */
    private class RemoteDriveRouteSearchCallback extends
            IRemoteDriveRouteSearchCallback.Stub {
        private final static int MSG_ON_DRIVE_ROUTE_SEARCHED = 0;

        private RemoteRouteSearchListener m_listener;
        String uuid;

        RemoteDriveRouteSearchCallback(RemoteRouteSearchListener listener) {
            m_listener = listener;
            uuid = UUID.randomUUID().toString();
        }

        private Handler m_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_ON_DRIVE_ROUTE_SEARCHED:
                    m_driveRouteListeners.remove(m_listener);

                    m_listener.onDriveRouteSearched(
                            (RemoteDriveRouteResult) msg.obj, msg.arg1);
                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");
                    break;
                }
            }

        };

        @Override
        public void onDriveRouteSearched(RemoteDriveRouteResult result,
                int errorCode) throws RemoteException {
            Message.obtain(m_handler, MSG_ON_DRIVE_ROUTE_SEARCHED, errorCode,
                    0, result).sendToTarget();
        }
    }

    /**
     * 根据指定的参数来请求获取驾车路径
     * 
     * @param search
     *            驾车路径搜索对象
     * @return true 请求成功, false 请求失败
     */
    public boolean requestDriveRouteSearch(RemoteRouteSearch search) {
        if (!m_remoteAvailable) {
            IwdsLog.e(this, "Search service on remote device unavailable");
            return false;
        }

        IwdsAssert.dieIf(this, search == null, "search is null");

        RemoteRouteSearchListener listener = search.getRouteSearchListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        RemoteDriveRouteQuery query = search.getDriveRouteQuery();
        IwdsAssert.dieIf(this, query == null, "query is null");

        RemoteDriveRouteSearchCallback callback = m_driveRouteListeners
                .get(listener);

        if (callback == null) {
            callback = new RemoteDriveRouteSearchCallback(listener);
            m_driveRouteListeners.put(listener, callback);
        } else {
            return true;
        }

        try {
            m_service.requestDriveRouteSearch(callback, callback.uuid, query);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in requestDriveRouteSearch: " + e.toString());
            return false;
        }

        return true;
    }

    /* -------------- Drive route search callback end -------------- */

    /* -------------- Walk route search callback -------------- */
    private class RemoteWalkRouteSearchCallback extends
            IRemoteWalkRouteSearchCallback.Stub {
        private final static int MSG_ON_WALK_ROUTE_SEARCHED = 0;

        private RemoteRouteSearchListener m_listener;
        String uuid;

        RemoteWalkRouteSearchCallback(RemoteRouteSearchListener listener) {
            m_listener = listener;
            uuid = UUID.randomUUID().toString();
        }

        private Handler m_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_ON_WALK_ROUTE_SEARCHED:
                    m_walkRouteListeners.remove(m_listener);

                    m_listener.onWalkRouteSearched(
                            (RemoteWalkRouteResult) msg.obj, msg.arg1);
                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");
                    break;
                }
            }

        };

        @Override
        public void onWalkRouteSearched(RemoteWalkRouteResult result,
                int errorCode) throws RemoteException {
            Message.obtain(m_handler, MSG_ON_WALK_ROUTE_SEARCHED, errorCode, 0,
                    result).sendToTarget();
        }
    }

    /**
     * 根据指定的参数来请求获取步行路径
     * 
     * @param search
     *            步行路径搜索对象
     * @return true 请求成功, false 请求失败
     */
    public boolean requestWalkRouteSearch(RemoteRouteSearch search) {
        if (!m_remoteAvailable) {
            IwdsLog.e(this, "Search service on remote device unavailable");
            return false;
        }

        IwdsAssert.dieIf(this, search == null, "search is null");

        RemoteRouteSearchListener listener = search.getRouteSearchListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        RemoteWalkRouteQuery query = search.getWalkRouteQuery();
        IwdsAssert.dieIf(this, query == null, "query is null");

        RemoteWalkRouteSearchCallback callback = m_walkRouteListeners
                .get(listener);

        if (callback == null) {
            callback = new RemoteWalkRouteSearchCallback(listener);
            m_walkRouteListeners.put(listener, callback);
        } else {
            return true;
        }

        try {
            m_service.requestWalkRouteSearch(callback, callback.uuid, query);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in requestWalkRouteSearch: " + e.toString());
            return false;
        }

        return true;
    }

    /* -------------- Walk route search callback end -------------- */

    private void unregisterAllListeners() {
        if (!m_geocodeSearchListners.isEmpty())
            m_geocodeSearchListners.clear();

        if (!m_regeocodeSearchListners.isEmpty())
            m_regeocodeSearchListners.clear();

        if (!m_districtSearchListeners.isEmpty())
            m_districtSearchListeners.clear();

        if (!m_poiSearchListeners.isEmpty())
            m_poiSearchListeners.clear();

        if (!m_poiDetailSearchListeners.isEmpty())
            m_poiDetailSearchListeners.clear();

        if (!m_inputTipsListeners.isEmpty())
            m_inputTipsListeners.clear();

        if (!m_busLineListeners.isEmpty())
            m_busLineListeners.clear();

        if (!m_busStationListeners.isEmpty())
            m_busStationListeners.clear();

        if (!m_walkRouteListeners.isEmpty())
            m_walkRouteListeners.clear();

        if (!m_busRouteListeners.isEmpty())
            m_busRouteListeners.clear();

        if (!m_driveRouteListeners.isEmpty())
            m_driveRouteListeners.clear();
    }
}
