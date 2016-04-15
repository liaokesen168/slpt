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
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.amap.api.services.busline.BusLineQuery;
import com.amap.api.services.busline.BusLineResult;
import com.amap.api.services.busline.BusLineSearch;
import com.amap.api.services.busline.BusLineSearch.OnBusLineSearchListener;
import com.amap.api.services.busline.BusStationQuery;
import com.amap.api.services.busline.BusStationResult;
import com.amap.api.services.busline.BusStationSearch;
import com.amap.api.services.busline.BusStationSearch.OnBusStationSearchListener;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearch.OnDistrictSearchListener;
import com.amap.api.services.district.DistrictSearchQuery;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Inputtips.InputtipsListener;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.BusRouteQuery;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.RouteSearch.WalkRouteQuery;
import com.amap.api.services.route.WalkRouteResult;
import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback;
import com.ingenic.iwds.datatransactor.ParcelTransactor;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusLineQuery;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusLineResult;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusStationQuery;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusStationResult;
import com.ingenic.iwds.smartlocation.search.district.RemoteDistrictQuery;
import com.ingenic.iwds.smartlocation.search.district.RemoteDistrictResult;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteGeocodeQuery;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteGeocodeResult;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteRegeocodeQuery;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteRegeocodeResult;
import com.ingenic.iwds.smartlocation.search.help.RemoteInputQuery;
import com.ingenic.iwds.smartlocation.search.help.RemoteTip;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiItemDetail;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiQuery;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiResult;
import com.ingenic.iwds.smartlocation.search.route.RemoteBusRouteQuery;
import com.ingenic.iwds.smartlocation.search.route.RemoteBusRouteResult;
import com.ingenic.iwds.smartlocation.search.route.RemoteDriveRouteQuery;
import com.ingenic.iwds.smartlocation.search.route.RemoteDriveRouteResult;
import com.ingenic.iwds.smartlocation.search.route.RemoteWalkRouteQuery;
import com.ingenic.iwds.smartlocation.search.route.RemoteWalkRouteResult;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

public class SearchServiceProxy {

    private static SearchServiceProxy sInstance;
    private Context m_context;
    private ParcelTransactor<RemoteSearchRequest> m_transactor;
    private ServiceProxyHandler m_handler;

    private HashMap<String, GeocodeSearchListener> m_geocodeSearchListeners;
    private HashMap<String, DistrictSearchListener> m_districtSearchListeners;
    private HashMap<String, PoiSearchListener> m_poiSearchListeners;
    private HashMap<String, InputTipsListener> m_inputTipsListeners;
    private HashMap<String, BusLineSearchListener> m_busLineSearchListeners;
    private HashMap<String, BusStationSearchListener> m_busStationSearchListeners;
    private HashMap<String, RouteSearchListener> m_routeSearchListeners;

    private SearchServiceProxy() {

    }

    public static SearchServiceProxy getInstance() {
        if (sInstance == null)
            sInstance = new SearchServiceProxy();

        return sInstance;
    }

    public void initialize(Context context) {
        IwdsLog.i(this, "Initialize");

        m_context = context;

        m_geocodeSearchListeners = new HashMap<String, GeocodeSearchListener>();
        m_districtSearchListeners = new HashMap<String, DistrictSearchListener>();
        m_poiSearchListeners = new HashMap<String, PoiSearchListener>();
        m_inputTipsListeners = new HashMap<String, InputTipsListener>();
        m_busLineSearchListeners = new HashMap<String, BusLineSearchListener>();
        m_busStationSearchListeners = new HashMap<String, BusStationSearchListener>();
        m_routeSearchListeners = new HashMap<String, RouteSearchListener>();

        m_transactor = new ParcelTransactor<RemoteSearchRequest>(m_context,
                RemoteSearchRequest.CREATOR, m_transactorCallback,
                "c1dc19e2-17a4-0797-2222-68a0dd4bfb68");

        m_handler = new ServiceProxyHandler();
    }

    public void start() {
        IwdsLog.i(this, "start");

        m_transactor.start();
    }

    private class GeocodeSearchListener implements OnGeocodeSearchListener {
        private String uuid;
        private GeocodeSearch search;
        private GeocodeQuery aMapGeocodeQuery;
        private RegeocodeQuery aMapRegeocodeQuery;

        private GeocodeSearchListener(String uuid) {
            this.uuid = uuid;
            this.search = new GeocodeSearch(m_context);
            this.search.setOnGeocodeSearchListener(this);
        }

        public GeocodeSearchListener(String uuid,
                RemoteGeocodeQuery geocodeQuery) {
            this(uuid);
            this.aMapGeocodeQuery = ProxyUtils.buildGeocodeQuery(geocodeQuery);
        }

        public GeocodeSearchListener(String uuid,
                RemoteRegeocodeQuery regeocodeQuery) {
            this(uuid);
            this.aMapRegeocodeQuery = ProxyUtils
                    .buildRegeocodeQuery(regeocodeQuery);
        }

        public void startGeocodeSearch() {
            this.search.getFromLocationNameAsyn(aMapGeocodeQuery);
        }

        public void startRegeocodeSearch() {
            this.search.getFromLocationAsyn(aMapRegeocodeQuery);
        }

        @Override
        public void onRegeocodeSearched(RegeocodeResult aMapResult,
                int errorCode) {

            RemoteRegeocodeResult result = ProxyUtils
                    .buildRemoteRegeocodeResult(aMapResult);

            m_handler.notifyRegeoSearched(result, this.uuid, errorCode);
        }

        @Override
        public void onGeocodeSearched(GeocodeResult aMapResult, int errorCode) {

            RemoteGeocodeResult result = ProxyUtils
                    .buildRemoteGeocodeResult(aMapResult);

            m_handler.notifyGeocodeSearched(result, this.uuid, errorCode);
        }
    }

    private class DistrictSearchListener implements OnDistrictSearchListener {
        private String uuid;
        private DistrictSearch search;
        private DistrictSearchQuery query;

        public DistrictSearchListener(String uuid, RemoteDistrictQuery query) {
            this.uuid = uuid;
            this.search = new DistrictSearch(m_context);
            this.search.setOnDistrictSearchListener(this);
            this.query = ProxyUtils.buildDistrictSearchQuery(query);
        }

        public void stratDistrictSearch() {
            if (this.query != null)
                this.search.setQuery(this.query);

            this.search.searchDistrictAnsy();
        }

        @Override
        public void onDistrictSearched(DistrictResult aMapResult) {
            int errorCode = 0;

            if (aMapResult != null)
                errorCode = aMapResult.getAMapException().getErrorCode();

            RemoteDistrictResult result = ProxyUtils
                    .buildRemoteDistrictResult(aMapResult);

            m_handler.notifyDistrictSearched(result, this.uuid, errorCode);
        }
    }

    private class PoiSearchListener implements OnPoiSearchListener {
        private String uuid;
        private String poiId;
        private PoiSearch search;
        private PoiSearch.Query query;

        private PoiSearchListener(String uuid) {
            this.uuid = uuid;
        }

        public PoiSearchListener(String uuid, RemotePoiQuery query) {
            this(uuid);
            this.query = ProxyUtils.buildPoiSearchQuery(query);
            this.search = new PoiSearch(m_context, this.query);
            this.search.setLanguage(query.getQueryLanguage());
            if (query.getBound() != null)
                this.search.setBound(ProxyUtils.buildPoiSearchBound(query
                        .getBound()));
            this.search.setOnPoiSearchListener(this);
        }

        public PoiSearchListener(String uuid, String poiId) {
            this(uuid);
            this.poiId = poiId;
            this.search = new PoiSearch(m_context, null);
            this.search.setOnPoiSearchListener(this);
        }

        public void startPoiSearch() {
            this.search.searchPOIAsyn();
        }

        public void startPoiDetailSearch() {
            this.search.searchPOIDetailAsyn(this.poiId);
        }

        @Override
        public void onPoiSearched(PoiResult aMapResult, int errorCode) {

            RemotePoiResult result = ProxyUtils
                    .buildRemotePoiResult(aMapResult);

            if (result != null && result.getQuery() != null)
                result.getQuery().setQueryLanguage(this.search.getLanguage());

            m_handler.notifyPoiSearched(result, this.uuid, errorCode);
        }

        @Override
        public void onPoiItemDetailSearched(PoiItemDetail aMapDetail,
                int errorCode) {

            RemotePoiItemDetail detail = ProxyUtils
                    .buildRemotePoiItemDetail(aMapDetail);

            m_handler.notifyPoiItemDetailSearched(detail, this.uuid, errorCode);
        }
    }

    private class InputTipsListener implements InputtipsListener {
        private String uuid;
        private Inputtips inputTips;
        private String keyword;
        private String city;

        public InputTipsListener(String uuid, RemoteInputQuery query) {
            this.uuid = uuid;
            this.keyword = query.getKeyword();
            this.city = query.getCity();
            this.inputTips = new Inputtips(m_context, this);
        }

        public void startInputTipsSearch() {
            try {
                inputTips.requestInputtips(this.keyword, this.city);

            } catch (AMapException e) {
                IwdsLog.e(this,
                        "Exception in startInputTipsSearch: " + e.toString());
                return;
            }
        }

        @Override
        public void onGetInputtips(List<Tip> aMapTipList, int errorCode) {
            ArrayList<RemoteTip> tipList = null;

            if (aMapTipList != null) {
                tipList = new ArrayList<RemoteTip>();

                for (Tip aMapTip : aMapTipList) {
                    tipList.add(ProxyUtils.buildRemoteTip(aMapTip));
                }
            }

            m_handler.notifyInputTipsSearched(tipList, this.uuid, errorCode);
        }
    }

    private class BusLineSearchListener implements OnBusLineSearchListener {
        private String uuid;
        private BusLineSearch search;
        private BusLineQuery query;

        public BusLineSearchListener(String uuid, RemoteBusLineQuery query) {
            this.uuid = uuid;
            this.query = ProxyUtils.buildBusLineQuery(query);
            this.search = new BusLineSearch(m_context, this.query);
            this.search.setOnBusLineSearchListener(this);
        }

        public void startBusLineSearch() {
            this.search.searchBusLineAsyn();
        }

        @Override
        public void onBusLineSearched(BusLineResult aMapResult, int errorCode) {

            RemoteBusLineResult result = ProxyUtils
                    .buildRemoteBusLineResult(aMapResult);

            m_handler.notifyBusLineSearched(result, this.uuid, errorCode);
        }

    }

    private class BusStationSearchListener implements
            OnBusStationSearchListener {
        private String uuid;
        private BusStationSearch search;
        private BusStationQuery query;

        public BusStationSearchListener(String uuid, RemoteBusStationQuery query) {
            this.uuid = uuid;
            this.query = ProxyUtils.buildBusStationQuery(query);
            this.search = new BusStationSearch(m_context, this.query);
            this.search.setOnBusStationSearchListener(this);
        }

        public void startBusStationSearch() {
            this.search.searchBusStationAsyn();
        }

        @Override
        public void onBusStationSearched(BusStationResult aMapResult,
                int errorCode) {
            RemoteBusStationResult result = ProxyUtils
                    .buildRemoteBusStationResult(aMapResult);

            m_handler.notifyBusStationSearched(result, this.uuid, errorCode);
        }

    }

    private class RouteSearchListener implements OnRouteSearchListener {
        private String uuid;
        private RouteSearch search;
        private BusRouteQuery busQuery;
        private DriveRouteQuery driveQuery;
        private WalkRouteQuery walkQuery;

        private RouteSearchListener(String uuid) {
            this.uuid = uuid;
            this.search = new RouteSearch(m_context);
            this.search.setRouteSearchListener(this);
        }

        public RouteSearchListener(String uuid, RemoteBusRouteQuery query) {
            this(uuid);
            this.busQuery = ProxyUtils.buildBusRouteQuery(query);
        }

        public RouteSearchListener(String uuid, RemoteDriveRouteQuery query) {
            this(uuid);
            this.driveQuery = ProxyUtils.buildDriveRouteQuery(query);
        }

        public RouteSearchListener(String uuid, RemoteWalkRouteQuery query) {
            this(uuid);
            this.walkQuery = ProxyUtils.buildWalkRouteQuery(query);
        }

        public void startBusRouteSearch() {
            this.search.calculateBusRouteAsyn(this.busQuery);
        }

        public void startDriveRouteSearch() {
            this.search.calculateDriveRouteAsyn(this.driveQuery);
        }

        public void startWalkRouteSearch() {
            this.search.calculateWalkRouteAsyn(this.walkQuery);
        }

        @Override
        public void onBusRouteSearched(BusRouteResult aMapResult, int errorCode) {
            RemoteBusRouteResult result = ProxyUtils
                    .buildRemoteBusRouteResult(aMapResult);

            m_handler.notifyBusRouteSearched(result, this.uuid, errorCode);
        }

        @Override
        public void onDriveRouteSearched(DriveRouteResult aMapResult,
                int errorCode) {
            RemoteDriveRouteResult result = ProxyUtils
                    .buildRemoteDriveRouteResult(aMapResult);

            m_handler.notifyDriveRouteSearched(result, this.uuid, errorCode);
        }

        @Override
        public void onWalkRouteSearched(WalkRouteResult aMapResult,
                int errorCode) {
            RemoteWalkRouteResult result = ProxyUtils
                    .buildRemoteWalkRouteResult(aMapResult);

            m_handler.notifyWalkRouteSearched(result, this.uuid, errorCode);
        }

    }

    private class ServiceProxyHandler extends Handler {
        private final static int MSG_CHANNEL_STATUS_CHANGED = 0;
        private final static int MSG_SEARCH_SERVICE_CONNECTED = 1;
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
        private final static int MSG_GEOCODE_SEARCHED = 20;
        private final static int MSG_REGEOCODE_SEARCHED = 21;
        private final static int MSG_DISTRICT_SEARCHED = 22;
        private final static int MSG_POI_SEARCHED = 23;
        private final static int MSG_POI_DETAIL_SEARCHED = 24;
        private final static int MSG_INPUT_TIPS_SEARCHED = 25;
        private final static int MSG_BUS_LINE_SEARCHED = 26;
        private final static int MSG_BUS_STATION_SEARCHED = 27;
        private final static int MSG_BUS_ROUTE_SEARCHED = 28;
        private final static int MSG_DRIVE_ROUTE_SEARCHED = 29;
        private final static int MSG_WALK_ROUTE_SEARCHED = 30;

        private boolean m_channelAvailable;

        public void setChannelState(boolean available) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_CHANNEL_STATUS_CHANGED;
            msg.arg1 = available ? 1 : 0;

            msg.sendToTarget();
        }

        public void notifySearchServiceConnected(boolean connected) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_SEARCH_SERVICE_CONNECTED;
            msg.arg1 = connected ? 1 : 0;

            msg.sendToTarget();
        }

        public void notifyGeocodeSearched(RemoteGeocodeResult result,
                String uuid, int errorCode) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            msg.what = MSG_GEOCODE_SEARCHED;
            bundle.putString("uuid", uuid);
            bundle.putParcelable("result", result);
            bundle.putInt("errorCode", errorCode);
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void notifyRegeoSearched(RemoteRegeocodeResult result,
                String uuid, int errorCode) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            msg.what = MSG_REGEOCODE_SEARCHED;
            bundle.putString("uuid", uuid);
            bundle.putParcelable("result", result);
            bundle.putInt("errorCode", errorCode);
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void notifyDistrictSearched(RemoteDistrictResult result,
                String uuid, int errorCode) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            msg.what = MSG_DISTRICT_SEARCHED;
            bundle.putString("uuid", uuid);
            bundle.putParcelable("result", result);
            bundle.putInt("errorCode", errorCode);
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void notifyPoiSearched(RemotePoiResult result, String uuid,
                int errorCode) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            msg.what = MSG_POI_SEARCHED;
            bundle.putString("uuid", uuid);
            bundle.putParcelable("result", result);
            bundle.putInt("errorCode", errorCode);
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void notifyPoiItemDetailSearched(RemotePoiItemDetail detail,
                String uuid, int errorCode) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            msg.what = MSG_POI_DETAIL_SEARCHED;
            bundle.putString("uuid", uuid);
            bundle.putParcelable("result", detail);
            bundle.putInt("errorCode", errorCode);
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void notifyInputTipsSearched(ArrayList<RemoteTip> tipList,
                String uuid, int errorCode) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            msg.what = MSG_INPUT_TIPS_SEARCHED;
            bundle.putString("uuid", uuid);
            bundle.putParcelableArrayList("tipList", tipList);
            bundle.putInt("errorCode", errorCode);
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void notifyBusLineSearched(RemoteBusLineResult result,
                String uuid, int errorCode) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            msg.what = MSG_BUS_LINE_SEARCHED;
            bundle.putString("uuid", uuid);
            bundle.putParcelable("result", result);
            bundle.putInt("errorCode", errorCode);
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void notifyBusStationSearched(RemoteBusStationResult result,
                String uuid, int errorCode) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            msg.what = MSG_BUS_STATION_SEARCHED;
            bundle.putString("uuid", uuid);
            bundle.putParcelable("result", result);
            bundle.putInt("errorCode", errorCode);
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void notifyBusRouteSearched(RemoteBusRouteResult result,
                String uuid, int errorCode) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            msg.what = MSG_BUS_ROUTE_SEARCHED;
            bundle.putString("uuid", uuid);
            bundle.putParcelable("result", result);
            bundle.putInt("errorCode", errorCode);
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void notifyDriveRouteSearched(RemoteDriveRouteResult result,
                String uuid, int errorCode) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            msg.what = MSG_DRIVE_ROUTE_SEARCHED;
            bundle.putString("uuid", uuid);
            bundle.putParcelable("result", result);
            bundle.putInt("errorCode", errorCode);
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void notifyWalkRouteSearched(RemoteWalkRouteResult result,
                String uuid, int errorCode) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            msg.what = MSG_WALK_ROUTE_SEARCHED;
            bundle.putString("uuid", uuid);
            bundle.putParcelable("result", result);
            bundle.putInt("errorCode", errorCode);
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void handlerRequest(RemoteSearchRequest request) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            switch (request.type) {
            case RemoteSearchRequest.TYPE_GEOCODE_SEARCH:
                msg.what = MSG_REQUEST_GEOCODE_SEARCH;

                bundle.putString("uuid", request.uuid);
                bundle.putParcelable("geocodeQuery", request.geocodeQuery);

                msg.setData(bundle);
                break;

            case RemoteSearchRequest.TYPE_REGEOCODE_SEARCH:
                msg.what = MSG_REQUEST_REGEOCODE_SEARCH;

                bundle.putString("uuid", request.uuid);
                bundle.putParcelable("regeocodeQuery", request.regeocodeQuery);

                msg.setData(bundle);
                break;

            case RemoteSearchRequest.TYPE_DISTRICT_SEARCH:
                msg.what = MSG_REQUEST_DISTRICT_SEARCH;

                bundle.putString("uuid", request.uuid);
                bundle.putParcelable("districtQuery", request.districtQuery);

                msg.setData(bundle);
                break;

            case RemoteSearchRequest.TYPE_POI_SEARCH:
                msg.what = MSG_REQUEST_POI_SEARCH;

                bundle.putString("uuid", request.uuid);
                bundle.putParcelable("poiQuery", request.poiQuery);

                msg.setData(bundle);
                break;

            case RemoteSearchRequest.TYPE_POI_DETAIL_SEARCH:
                msg.what = MSG_REQUEST_POI_DETAIL_SEARCH;

                bundle.putString("uuid", request.uuid);
                bundle.putString("poiId", request.poiId);

                msg.setData(bundle);
                break;

            case RemoteSearchRequest.TYPE_INPUT_TIPS_SEARCH:
                msg.what = MSG_REQUEST_INPUT_TIPS_SEARCH;

                bundle.putString("uuid", request.uuid);
                bundle.putParcelable("inputQuery", request.inputQuery);

                msg.setData(bundle);
                break;

            case RemoteSearchRequest.TYPE_BUS_LINE_SEARCH:
                msg.what = MSG_REQUEST_BUS_LINE_SEARCH;

                bundle.putString("uuid", request.uuid);
                bundle.putParcelable("busLineQuery", request.busLineQuery);

                msg.setData(bundle);
                break;

            case RemoteSearchRequest.TYPE_BUS_STATION_SEARCH:
                msg.what = MSG_REQUEST_BUS_STATION_SEARCH;

                bundle.putString("uuid", request.uuid);
                bundle.putParcelable("busStationQuery", request.busStationQuery);

                msg.setData(bundle);
                break;

            case RemoteSearchRequest.TYPE_BUS_ROUTE_SEARCH:
                msg.what = MSG_REQUEST_BUS_ROUTE_SEARCH;

                bundle.putString("uuid", request.uuid);
                bundle.putParcelable("busRouteQuery", request.busRouteQuery);

                msg.setData(bundle);
                break;

            case RemoteSearchRequest.TYPE_DRIVE_ROUTE_SEARCH:
                msg.what = MSG_REQUEST_DRIVE_ROUTE_SEARCH;

                bundle.putString("uuid", request.uuid);
                bundle.putParcelable("driveRouteQuery", request.driveRouteQuery);

                msg.setData(bundle);
                break;

            case RemoteSearchRequest.TYPE_WALK_ROUTE_SEARCH:
                msg.what = MSG_REQUEST_WALK_ROUTE_SEARCH;

                bundle.putString("uuid", request.uuid);
                bundle.putParcelable("walkRouteQuery", request.walkRouteQuery);

                msg.setData(bundle);
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
            RemoteSearchResponse response = RemoteSearchResponse
                    .obtain(m_transactor);

            Bundle bundle = msg.getData();

            String uuid = bundle.getString("uuid");
            int errorCode = bundle.getInt("errorCode");

            switch (msg.what) {
            case MSG_CHANNEL_STATUS_CHANGED:
                m_channelAvailable = msg.arg1 == 1 ? true : false;
                if (m_channelAvailable) {
                    IwdsLog.i(this, "Channel available");
                    notifySearchServiceConnected(true);

                } else {
                    IwdsLog.i(this, "Channel unavailable");

                    if (!m_geocodeSearchListeners.isEmpty())
                        m_geocodeSearchListeners.clear();

                    if (!m_districtSearchListeners.isEmpty())
                        m_districtSearchListeners.clear();

                    if (!m_poiSearchListeners.isEmpty())
                        m_poiSearchListeners.clear();

                    if (!m_inputTipsListeners.isEmpty())
                        m_inputTipsListeners.clear();

                    if (!m_busLineSearchListeners.isEmpty())
                        m_busLineSearchListeners.clear();

                    if (!m_busStationSearchListeners.isEmpty())
                        m_busStationSearchListeners.clear();

                    if (!m_routeSearchListeners.isEmpty())
                        m_routeSearchListeners.clear();
                }

                break;

            case MSG_SEARCH_SERVICE_CONNECTED:
                response.type = RemoteSearchResponse.TYPE_SEARCH_SERVICE_STATUS;
                response.serviceConnected = msg.arg1;

                IwdsLog.i(this, "Notify search service connected: "
                        + (msg.arg1 == 1 ? true : false));

                response.sendToRemote();
                break;

            case MSG_REQUEST_GEOCODE_SEARCH:

                RemoteGeocodeQuery geocodeQuery = (RemoteGeocodeQuery) bundle
                        .getParcelable("geocodeQuery");

                GeocodeSearchListener geocodeListener = m_geocodeSearchListeners
                        .get(uuid);

                if (geocodeListener == null) {
                    geocodeListener = new GeocodeSearchListener(uuid,
                            geocodeQuery);

                    m_geocodeSearchListeners.put(uuid, geocodeListener);
                    geocodeListener.startGeocodeSearch();
                    IwdsLog.d(this, "Request Geocode search: uuid=" + uuid);
                }

                break;

            case MSG_REQUEST_REGEOCODE_SEARCH:

                RemoteRegeocodeQuery regeocodeQuery = (RemoteRegeocodeQuery) bundle
                        .getParcelable("regeocodeQuery");

                GeocodeSearchListener regeocodeListener = m_geocodeSearchListeners
                        .get(uuid);
                if (regeocodeListener == null) {
                    regeocodeListener = new GeocodeSearchListener(uuid,
                            regeocodeQuery);

                    m_geocodeSearchListeners.put(uuid, regeocodeListener);
                    regeocodeListener.startRegeocodeSearch();
                    IwdsLog.d(this, "Request Regeocode search: uuid=" + uuid);
                }
                break;

            case MSG_REQUEST_DISTRICT_SEARCH:
                RemoteDistrictQuery districtQuery = (RemoteDistrictQuery) bundle
                        .getParcelable("districtQuery");

                DistrictSearchListener districtListener = m_districtSearchListeners
                        .get(uuid);
                if (districtListener == null) {
                    districtListener = new DistrictSearchListener(uuid,
                            districtQuery);

                    m_districtSearchListeners.put(uuid, districtListener);
                    districtListener.stratDistrictSearch();
                    IwdsLog.d(this, "Request District search: uuid=" + uuid);
                }
                break;

            case MSG_REQUEST_POI_SEARCH:
                RemotePoiQuery poiQuery = (RemotePoiQuery) bundle
                        .getParcelable("poiQuery");

                PoiSearchListener poiListener = m_poiSearchListeners.get(uuid);
                if (poiListener == null) {
                    poiListener = new PoiSearchListener(uuid, poiQuery);

                    m_poiSearchListeners.put(uuid, poiListener);
                    poiListener.startPoiSearch();
                    IwdsLog.d(this, "Request poi search: uuid=" + uuid);
                }
                break;

            case MSG_REQUEST_POI_DETAIL_SEARCH:
                String poiId = bundle.getString("poiId");
                PoiSearchListener poiDetailListener = m_poiSearchListeners
                        .get(uuid);
                if (poiDetailListener == null) {
                    poiDetailListener = new PoiSearchListener(uuid, poiId);

                    m_poiSearchListeners.put(uuid, poiDetailListener);
                    poiDetailListener.startPoiDetailSearch();
                    IwdsLog.d(this, "Request poi detail search: uuid=" + uuid);
                }
                break;

            case MSG_REQUEST_INPUT_TIPS_SEARCH:
                RemoteInputQuery inputQuery = (RemoteInputQuery) bundle
                        .getParcelable("inputQuery");
                InputTipsListener inputListener = m_inputTipsListeners
                        .get(uuid);
                if (inputListener == null) {
                    inputListener = new InputTipsListener(uuid, inputQuery);

                    m_inputTipsListeners.put(uuid, inputListener);
                    inputListener.startInputTipsSearch();
                    IwdsLog.d(this, "Request input tips search: uuid=" + uuid);
                }
                break;

            case MSG_REQUEST_BUS_LINE_SEARCH:
                RemoteBusLineQuery busLineQuery = (RemoteBusLineQuery) bundle
                        .getParcelable("busLineQuery");
                BusLineSearchListener busLineSearchListener = m_busLineSearchListeners
                        .get(uuid);
                if (busLineSearchListener == null) {
                    busLineSearchListener = new BusLineSearchListener(uuid,
                            busLineQuery);

                    m_busLineSearchListeners.put(uuid, busLineSearchListener);
                    busLineSearchListener.startBusLineSearch();
                    IwdsLog.d(this, "Request bus line search: uuid=" + uuid);
                }
                break;

            case MSG_REQUEST_BUS_STATION_SEARCH:
                RemoteBusStationQuery busStationQuery = (RemoteBusStationQuery) bundle
                        .getParcelable("busStationQuery");
                BusStationSearchListener busStationSearchListener = m_busStationSearchListeners
                        .get(uuid);
                if (busStationSearchListener == null) {
                    busStationSearchListener = new BusStationSearchListener(
                            uuid, busStationQuery);

                    m_busStationSearchListeners.put(uuid,
                            busStationSearchListener);
                    busStationSearchListener.startBusStationSearch();
                    IwdsLog.d(this, "Request bus station search: uuid=" + uuid);
                }

                break;

            case MSG_REQUEST_BUS_ROUTE_SEARCH:
                RemoteBusRouteQuery busRouteQuery = (RemoteBusRouteQuery) bundle
                        .getParcelable("busRouteQuery");
                RouteSearchListener busRouteSearchListener = m_routeSearchListeners
                        .get(uuid);
                if (busRouteSearchListener == null) {
                    busRouteSearchListener = new RouteSearchListener(uuid,
                            busRouteQuery);

                    m_routeSearchListeners.put(uuid, busRouteSearchListener);
                    busRouteSearchListener.startBusRouteSearch();
                    IwdsLog.d(this, "Request bus route search: uuid=" + uuid);
                }
                break;

            case MSG_REQUEST_DRIVE_ROUTE_SEARCH:
                RemoteDriveRouteQuery driveRouteQuery = (RemoteDriveRouteQuery) bundle
                        .getParcelable("driveRouteQuery");
                RouteSearchListener driveRouteSearchListener = m_routeSearchListeners
                        .get(uuid);
                if (driveRouteSearchListener == null) {
                    driveRouteSearchListener = new RouteSearchListener(uuid,
                            driveRouteQuery);

                    m_routeSearchListeners.put(uuid, driveRouteSearchListener);
                    driveRouteSearchListener.startDriveRouteSearch();
                    IwdsLog.d(this, "Request drive route search: uuid=" + uuid);
                }
                break;

            case MSG_REQUEST_WALK_ROUTE_SEARCH:
                RemoteWalkRouteQuery walkRouteQuery = (RemoteWalkRouteQuery) bundle
                        .getParcelable("walkRouteQuery");
                RouteSearchListener walkRouteSearchListener = m_routeSearchListeners
                        .get(uuid);
                if (walkRouteSearchListener == null) {
                    walkRouteSearchListener = new RouteSearchListener(uuid,
                            walkRouteQuery);

                    m_routeSearchListeners.put(uuid, walkRouteSearchListener);
                    walkRouteSearchListener.startWalkRouteSearch();
                    IwdsLog.d(this, "Request walk route search: uuid=" + uuid);
                }
                break;

            case MSG_GEOCODE_SEARCHED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                RemoteGeocodeResult geocodeResult = bundle
                        .getParcelable("result");

                IwdsLog.d(this, "Geocode Searched: uuid=" + uuid);

                response.type = RemoteSearchResponse.TYPE_GEOCODE_SEARCHED;
                response.uuid = uuid;
                response.geocodeResult = geocodeResult;
                response.errorCode = errorCode;

                response.sendToRemote();

                m_geocodeSearchListeners.remove(uuid);

                break;

            case MSG_REGEOCODE_SEARCHED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                RemoteRegeocodeResult regeocodeResult = bundle
                        .getParcelable("result");

                IwdsLog.d(this, "Regeocode Searched: uuid=" + uuid);

                response.type = RemoteSearchResponse.TYPE_REGEOCODE_SEARCHED;
                response.uuid = uuid;
                response.errorCode = errorCode;
                response.regeocodeResult = regeocodeResult;

                response.sendToRemote();

                m_geocodeSearchListeners.remove(uuid);

                break;

            case MSG_DISTRICT_SEARCHED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                RemoteDistrictResult districtResult = bundle
                        .getParcelable("result");

                IwdsLog.d(this, "District Searched: uuid=" + uuid);

                response.type = RemoteSearchResponse.TYPE_DISTRICT_SEARCHED;
                response.uuid = uuid;
                response.errorCode = errorCode;
                response.districtResult = districtResult;

                response.sendToRemote();

                m_districtSearchListeners.remove(uuid);

                break;

            case MSG_POI_SEARCHED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                IwdsLog.d(this, "Poi Searched: uuid=" + uuid);

                RemotePoiResult poiResult = bundle.getParcelable("result");
                response.type = RemoteSearchResponse.TYPE_POI_SEARCHED;
                response.uuid = uuid;
                response.errorCode = errorCode;
                response.poiResult = poiResult;

                response.sendToRemote();

                m_poiSearchListeners.remove(uuid);
                break;

            case MSG_POI_DETAIL_SEARCHED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                IwdsLog.d(this, "Poi detail Searched: uuid=" + uuid);

                RemotePoiItemDetail detail = bundle.getParcelable("result");
                response.type = RemoteSearchResponse.TYPE_POI_DETAIL_SEARCHED;
                response.uuid = uuid;
                response.errorCode = errorCode;
                response.poiItemDetail = detail;

                response.sendToRemote();

                m_poiSearchListeners.remove(uuid);
                break;

            case MSG_INPUT_TIPS_SEARCHED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                IwdsLog.d(this, "Input tips Searched: uuid=" + uuid);

                ArrayList<RemoteTip> tipList = bundle
                        .getParcelableArrayList("tipList");
                response.type = RemoteSearchResponse.TYPE_INPUT_TIPS_SEARCHED;
                response.uuid = uuid;
                response.errorCode = errorCode;
                response.tipList = tipList;

                response.sendToRemote();

                m_inputTipsListeners.remove(uuid);
                break;

            case MSG_BUS_LINE_SEARCHED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                IwdsLog.d(this, "Bus line searched: uuid=" + uuid);

                RemoteBusLineResult busLineResult = bundle
                        .getParcelable("result");
                response.type = RemoteSearchResponse.TYPE_BUS_LINE_SEARCHED;
                response.uuid = uuid;
                response.errorCode = errorCode;
                response.busLineResult = busLineResult;

                response.sendToRemote();

                m_busLineSearchListeners.remove(uuid);
                break;

            case MSG_BUS_STATION_SEARCHED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                IwdsLog.d(this, "Bus station searched: uuid=" + uuid);

                RemoteBusStationResult busStationResult = bundle
                        .getParcelable("result");
                response.type = RemoteSearchResponse.TYPE_BUS_STATION_SEARCHED;
                response.uuid = uuid;
                response.errorCode = errorCode;
                response.busStationResult = busStationResult;

                response.sendToRemote();

                m_busStationSearchListeners.remove(uuid);
                break;

            case MSG_BUS_ROUTE_SEARCHED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                IwdsLog.d(this, "Bus route searched: uuid=" + uuid);

                RemoteBusRouteResult busRouteResult = bundle
                        .getParcelable("result");
                response.type = RemoteSearchResponse.TYPE_BUS_ROUTE_SEARCHED;
                response.uuid = uuid;
                response.errorCode = errorCode;
                response.busRouteResult = busRouteResult;

                response.sendToRemote();

                m_routeSearchListeners.remove(uuid);
                break;

            case MSG_DRIVE_ROUTE_SEARCHED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                IwdsLog.d(this, "Drive route searched: uuid=" + uuid);

                RemoteDriveRouteResult driveRouteResult = bundle
                        .getParcelable("result");
                response.type = RemoteSearchResponse.TYPE_DRIVE_ROUTE_SEARCHED;
                response.uuid = uuid;
                response.errorCode = errorCode;
                response.driveRouteResult = driveRouteResult;

                response.sendToRemote();

                m_routeSearchListeners.remove(uuid);
                break;

            case MSG_WALK_ROUTE_SEARCHED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                IwdsLog.d(this, "Walk route searched: uuid=" + uuid);

                RemoteWalkRouteResult walkRouteResult = bundle
                        .getParcelable("result");
                response.type = RemoteSearchResponse.TYPE_WALK_ROUTE_SEARCHED;
                response.uuid = uuid;
                response.errorCode = errorCode;
                response.walkRouteResult = walkRouteResult;

                response.sendToRemote();

                m_routeSearchListeners.remove(uuid);
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
            m_handler.setChannelState(isAvailable);
        }

        @Override
        public void onSendResult(DataTransactResult result) {

        }

        @Override
        public void onDataArrived(Object object) {
            if (object instanceof RemoteSearchRequest)
                m_handler.handlerRequest((RemoteSearchRequest) object);
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
