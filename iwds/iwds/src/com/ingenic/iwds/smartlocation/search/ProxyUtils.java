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
import java.util.List;

import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusLineQuery;
import com.amap.api.services.busline.BusLineQuery.SearchType;
import com.amap.api.services.busline.BusLineResult;
import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.busline.BusStationQuery;
import com.amap.api.services.busline.BusStationResult;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearchQuery;
import com.amap.api.services.geocoder.BusinessArea;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.geocoder.RegeocodeRoad;
import com.amap.api.services.geocoder.StreetNumber;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.Cinema;
import com.amap.api.services.poisearch.Dining;
import com.amap.api.services.poisearch.Discount;
import com.amap.api.services.poisearch.Groupbuy;
import com.amap.api.services.poisearch.Hotel;
import com.amap.api.services.poisearch.Photo;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.Scenic;
import com.amap.api.services.road.Crossroad;
import com.amap.api.services.road.Road;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.District;
import com.amap.api.services.route.Doorway;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.Path;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.RouteBusWalkItem;
import com.amap.api.services.route.RouteResult;
import com.amap.api.services.route.RouteSearch.BusRouteQuery;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;
import com.amap.api.services.route.RouteSearch.FromAndTo;
import com.amap.api.services.route.RouteSearch.WalkRouteQuery;
import com.amap.api.services.route.RouteSearchCity;
import com.amap.api.services.route.SearchCity;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.route.WalkStep;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusLineItem;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusLineQuery;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusLineResult;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusStationItem;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusStationQuery;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusStationResult;
import com.ingenic.iwds.smartlocation.search.core.RemoteLatLonPoint;
import com.ingenic.iwds.smartlocation.search.core.RemotePoiItem;
import com.ingenic.iwds.smartlocation.search.core.RemoteSuggestionCity;
import com.ingenic.iwds.smartlocation.search.district.RemoteDistrictItem;
import com.ingenic.iwds.smartlocation.search.district.RemoteDistrictQuery;
import com.ingenic.iwds.smartlocation.search.district.RemoteDistrictResult;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteBusinessArea;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteGeocodeAddress;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteGeocodeQuery;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteGeocodeResult;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteRegeocodeAddress;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteRegeocodeQuery;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteRegeocodeResult;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteRegeocodeRoad;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteStreetNumber;
import com.ingenic.iwds.smartlocation.search.help.RemoteTip;
import com.ingenic.iwds.smartlocation.search.poisearch.RemoteCinema;
import com.ingenic.iwds.smartlocation.search.poisearch.RemoteDining;
import com.ingenic.iwds.smartlocation.search.poisearch.RemoteDiscount;
import com.ingenic.iwds.smartlocation.search.poisearch.RemoteGroupbuy;
import com.ingenic.iwds.smartlocation.search.poisearch.RemoteHotel;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePhoto;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiItemDetail;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiItemDetail.DeepType;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiQuery;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiResult;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiSearchBound;
import com.ingenic.iwds.smartlocation.search.poisearch.RemoteScenic;
import com.ingenic.iwds.smartlocation.search.road.RemoteCrossroad;
import com.ingenic.iwds.smartlocation.search.road.RemoteRoad;
import com.ingenic.iwds.smartlocation.search.route.RemoteBusPath;
import com.ingenic.iwds.smartlocation.search.route.RemoteBusRouteQuery;
import com.ingenic.iwds.smartlocation.search.route.RemoteBusRouteResult;
import com.ingenic.iwds.smartlocation.search.route.RemoteBusStep;
import com.ingenic.iwds.smartlocation.search.route.RemoteDistrict;
import com.ingenic.iwds.smartlocation.search.route.RemoteDoorway;
import com.ingenic.iwds.smartlocation.search.route.RemoteDrivePath;
import com.ingenic.iwds.smartlocation.search.route.RemoteDriveRouteQuery;
import com.ingenic.iwds.smartlocation.search.route.RemoteDriveRouteResult;
import com.ingenic.iwds.smartlocation.search.route.RemoteDriveStep;
import com.ingenic.iwds.smartlocation.search.route.RemoteFromAndTo;
import com.ingenic.iwds.smartlocation.search.route.RemotePath;
import com.ingenic.iwds.smartlocation.search.route.RemoteRouteBusLineItem;
import com.ingenic.iwds.smartlocation.search.route.RemoteRouteBusWalkItem;
import com.ingenic.iwds.smartlocation.search.route.RemoteRouteResult;
import com.ingenic.iwds.smartlocation.search.route.RemoteRouteSearchCity;
import com.ingenic.iwds.smartlocation.search.route.RemoteSearchCity;
import com.ingenic.iwds.smartlocation.search.route.RemoteWalkPath;
import com.ingenic.iwds.smartlocation.search.route.RemoteWalkRouteQuery;
import com.ingenic.iwds.smartlocation.search.route.RemoteWalkRouteResult;
import com.ingenic.iwds.smartlocation.search.route.RemoteWalkStep;

class ProxyUtils {

    static LatLonPoint buildLatLonPoint(RemoteLatLonPoint point) {
        LatLonPoint aMapPoint = new LatLonPoint(point.getLatitude(),
                point.getLongitude());

        return aMapPoint;
    }

    static GeocodeQuery buildGeocodeQuery(RemoteGeocodeQuery query) {
        GeocodeQuery aMapQuery = new GeocodeQuery(query.getLocationName(),
                query.getCity());

        return aMapQuery;
    }

    static RegeocodeQuery buildRegeocodeQuery(RemoteRegeocodeQuery query) {
        LatLonPoint aMapPoint = buildLatLonPoint(query.getPoint());

        RegeocodeQuery aMapQuery = new RegeocodeQuery(aMapPoint,
                query.getRadius(), query.getLatLonType());

        return aMapQuery;
    }

    static DistrictSearchQuery buildDistrictSearchQuery(
            RemoteDistrictQuery query) {
        if (query == null)
            return null;

        DistrictSearchQuery aMapQuery = new DistrictSearchQuery();

        aMapQuery.setKeywords(query.getKeywords());
        aMapQuery.setKeywordsLevel(query.getKeywordsLevel());
        aMapQuery.setPageNum(query.getPageNum());
        aMapQuery.setPageSize(query.getPageSize());
        aMapQuery.setShowChild(query.isShowChild());

        return aMapQuery;
    }

    static PoiSearch.Query buildPoiSearchQuery(RemotePoiQuery query) {

        PoiSearch.Query aMapQuery = new PoiSearch.Query(query.getQueryString(),
                query.getCategory(), query.getCity());

        aMapQuery.setLimitDiscount(query.hasDiscountLimit());
        aMapQuery.setLimitGroupbuy(query.hasGroupBuyLimit());
        aMapQuery.setPageNum(query.getPageNum());
        aMapQuery.setPageSize(query.getPageSize());

        return aMapQuery;
    }

    static PoiSearch.SearchBound buildPoiSearchBound(RemotePoiSearchBound bound) {
        if (bound == null)
            return null;

        PoiSearch.SearchBound aMapBound = null;

        if (bound.getUpperRight() != null && bound.getLowerLeft() != null) {
            aMapBound = new PoiSearch.SearchBound(
                    buildLatLonPoint(bound.getUpperRight()),
                    buildLatLonPoint(bound.getLowerLeft()));

        } else if (bound.getCenter() != null) {
            aMapBound = new PoiSearch.SearchBound(
                    buildLatLonPoint(bound.getCenter()), bound.getRange(),
                    bound.isDistanceSort());

        } else if (bound.getPolyGonList() != null) {
            List<RemoteLatLonPoint> points = bound.getPolyGonList();
            List<LatLonPoint> aMapPoints = new ArrayList<LatLonPoint>();
            for (RemoteLatLonPoint point : points) {
                LatLonPoint aMapPoint = buildLatLonPoint(point);
                aMapPoints.add(aMapPoint);
            }

            aMapBound = new PoiSearch.SearchBound(aMapPoints);
        }

        return aMapBound;
    }

    static BusLineQuery buildBusLineQuery(RemoteBusLineQuery query) {
        String queryString = query.getQueryString();
        String city = query.getCity();
        SearchType type = null;
        BusLineQuery aMapQuery = null;

        switch (query.getCategory()) {
        case BY_LINE_ID:
            type = SearchType.BY_LINE_ID;
            break;
        case BY_LINE_NAME:
            type = SearchType.BY_LINE_NAME;
            break;
        default:
            type = SearchType.BY_LINE_ID;
            break;
        }

        aMapQuery = new BusLineQuery(queryString, type, city);

        aMapQuery.setPageNumber(query.getPageNumber());
        aMapQuery.setPageSize(query.getPageSize());

        return aMapQuery;
    }

    static BusStationQuery buildBusStationQuery(RemoteBusStationQuery query) {
        BusStationQuery aMapQuery = null;

        aMapQuery = new BusStationQuery(query.getQueryString(), query.getCity());

        aMapQuery.setPageNumber(query.getPageNumber());
        aMapQuery.setPageSize(query.getPageSize());

        return aMapQuery;
    }

    static FromAndTo buildFromAndTo(RemoteFromAndTo fromAndTo) {
        String startPoiId = fromAndTo.getDestinationPoiId();
        String destPoiId = fromAndTo.getStartPoiId();

        FromAndTo aMapFromAndTo = new FromAndTo(
                buildLatLonPoint(fromAndTo.getFrom()),
                buildLatLonPoint(fromAndTo.getTo()));

        aMapFromAndTo.setDestinationPoiID(destPoiId);
        aMapFromAndTo.setStartPoiID(startPoiId);

        return aMapFromAndTo;
    }

    static BusRouteQuery buildBusRouteQuery(RemoteBusRouteQuery query) {
        return new BusRouteQuery(buildFromAndTo(query.getFromAndTo()),
                query.getMode(), query.getCity(), query.getNightFlag());
    }

    static DriveRouteQuery buildDriveRouteQuery(RemoteDriveRouteQuery query) {
        FromAndTo aMapFromAndTo = buildFromAndTo(query.getFromAndTo());
        int mode = query.getMode();

        List<LatLonPoint> aMapPointList = new ArrayList<LatLonPoint>();
        if (query.getPassedByPoints() != null) {
            for (RemoteLatLonPoint point : query.getPassedByPoints()) {
                aMapPointList.add(buildLatLonPoint(point));
            }
        }

        List<List<LatLonPoint>> aMapPolygonList = new ArrayList<List<LatLonPoint>>();
        if (query.getAvoidpolygons() != null) {
            List<LatLonPoint> aMapPolygon = new ArrayList<LatLonPoint>();
            for (List<RemoteLatLonPoint> pointList : query.getAvoidpolygons()) {
                if (pointList != null) {
                    for (RemoteLatLonPoint point : pointList) {
                        aMapPolygon.add(buildLatLonPoint(point));
                    }
                }
                aMapPolygonList.add(aMapPolygon);
            }
        }

        String avoidRoad = query.getAvoidRoad();

        DriveRouteQuery aMapQuery = new DriveRouteQuery(aMapFromAndTo, mode,
                aMapPointList, aMapPolygonList, avoidRoad);

        return aMapQuery;
    }

    static WalkRouteQuery buildWalkRouteQuery(RemoteWalkRouteQuery query) {
        FromAndTo aMapFromAndTo = buildFromAndTo(query.getFromAndTo());
        int mode = query.getMode();

        return new WalkRouteQuery(aMapFromAndTo, mode);
    }

    /*----------------------------------------------------------------*/
    static RemoteLatLonPoint buildRemoteLatLonPoint(LatLonPoint aMapPoint) {
        if (aMapPoint == null)
            return null;

        RemoteLatLonPoint point = new RemoteLatLonPoint(
                aMapPoint.getLatitude(), aMapPoint.getLongitude());

        return point;
    }

    static RemoteBusinessArea buildRemoteBusinessArea(BusinessArea aMapArea) {
        if (aMapArea == null)
            return null;

        RemoteBusinessArea area = new RemoteBusinessArea();

        area.setCenterPoint(buildRemoteLatLonPoint(aMapArea.getCenterPoint()));

        area.setName(aMapArea.getName());

        return area;
    }

    static RemoteRegeocodeRoad buildRemoteRegeocodeRoad(RegeocodeRoad aMapRoad) {
        if (aMapRoad == null)
            return null;

        RemoteRegeocodeRoad road = new RemoteRegeocodeRoad();

        road.setDirection(aMapRoad.getDirection());
        road.setDistance(aMapRoad.getDistance());
        road.setId(aMapRoad.getId());
        road.setLatLonPoint(buildRemoteLatLonPoint(aMapRoad.getLatLngPoint()));
        road.setName(aMapRoad.getName());

        return road;
    }

    static RemoteRoad buildRemoteRoad(Road aMapRoad) {
        if (aMapRoad == null)
            return null;

        RemoteRoad road = new RemoteRoad();

        road.setCenterPoint(buildRemoteLatLonPoint(aMapRoad.getCenterPoint()));
        road.setCityCode(aMapRoad.getCityCode());
        road.setId(aMapRoad.getId());
        road.setName(aMapRoad.getName());
        road.setRoadWidth(aMapRoad.getRoadWidth());
        road.setType(aMapRoad.getType());

        return road;
    }

    static RemoteCrossroad buildRemoteCrossroad(Crossroad aMapRoad) {
        if (aMapRoad == null)
            return null;

        RemoteCrossroad road = new RemoteCrossroad();

        road.setDirection(aMapRoad.getDirection());
        road.setDistance(aMapRoad.getDistance());
        road.setFirstRoadId(aMapRoad.getFirstRoadId());
        road.setFirstRoadName(aMapRoad.getFirstRoadName());
        road.setSecondRoadId(aMapRoad.getSecondRoadId());
        road.setSecondRoadName(aMapRoad.getSecondRoadName());
        road.setCenterPoint(buildRemoteLatLonPoint(aMapRoad.getCenterPoint()));
        road.setCityCode(aMapRoad.getCityCode());
        road.setId(aMapRoad.getId());
        road.setName(aMapRoad.getName());
        road.setRoadWidth(aMapRoad.getRoadWidth());
        road.setType(aMapRoad.getType());

        return road;
    }

    static RemotePoiItem buildRemotePoiItem(PoiItem aMapItem) {
        if (aMapItem == null)
            return null;

        RemotePoiItem item = new RemotePoiItem();

        item.setAdCode(aMapItem.getAdCode());
        item.setAdName(aMapItem.getAdName());
        item.setCityCode(aMapItem.getCityCode());
        item.setCityName(aMapItem.getCityName());
        item.setDirection(aMapItem.getDirection());
        item.setDiscountInfo(aMapItem.isDiscountInfo());
        item.setDistance(aMapItem.getDistance());
        item.setEmail(aMapItem.getEmail());
        item.setEnter(buildRemoteLatLonPoint(aMapItem.getEnter()));
        item.setExit(buildRemoteLatLonPoint(aMapItem.getExit()));
        item.setGroupBuyInfo(aMapItem.isGroupbuyInfo());
        item.setIndoormap(aMapItem.isIndoorMap());
        item.setPostCode(aMapItem.getPostcode());
        item.setProvinceCode(aMapItem.getProvinceCode());
        item.setProvinceName(aMapItem.getProvinceName());
        item.setTel(aMapItem.getTel());
        item.setTypeDes(aMapItem.getTypeDes());
        item.setWebSite(aMapItem.getWebsite());
        item.setPoiId(aMapItem.getPoiId());
        item.setTitle(aMapItem.getTitle());
        item.setSnippet(aMapItem.getSnippet());
        item.setLatLonPoint(buildRemoteLatLonPoint(aMapItem.getLatLonPoint()));

        return item;
    }

    static RemoteGeocodeAddress buildRemoteGeocodeAddress(
            GeocodeAddress aMapAddress) {
        if (aMapAddress == null)
            return null;

        RemoteGeocodeAddress address = new RemoteGeocodeAddress();

        address.setAdCode(aMapAddress.getAdcode());
        address.setBuilding(aMapAddress.getBuilding());
        address.setCity(aMapAddress.getCity());
        address.setDistrict(aMapAddress.getDistrict());
        address.setFormatAddress(aMapAddress.getFormatAddress());
        address.setLatLonPoint(buildRemoteLatLonPoint(aMapAddress
                .getLatLonPoint()));
        address.setLevel(aMapAddress.getLevel());
        address.setNeighborhood(aMapAddress.getNeighborhood());
        address.setProvince(aMapAddress.getProvince());
        address.setTownShip(aMapAddress.getTownship());

        return address;
    }

    static RemoteStreetNumber buildRemoteStreetNumber(StreetNumber aMapNumber) {
        if (aMapNumber == null)
            return null;

        RemoteStreetNumber number = new RemoteStreetNumber();

        number.setDirection(aMapNumber.getDirection());
        number.setDistance(aMapNumber.getDistance());
        number.setLatLonPoint(buildRemoteLatLonPoint(aMapNumber
                .getLatLonPoint()));
        number.setNumber(aMapNumber.getNumber());
        number.setStreet(aMapNumber.getStreet());

        return number;
    }

    static RemoteRegeocodeAddress buildRemoteRegeocodeAddreee(
            RegeocodeAddress aMapAddress) {
        if (aMapAddress == null)
            return null;

        RemoteRegeocodeAddress address = new RemoteRegeocodeAddress();

        address.setAdCode(aMapAddress.getAdCode());
        address.setBuilding(aMapAddress.getBuilding());

        List<BusinessArea> aMapAreaList = aMapAddress.getBusinessAreas();
        if (aMapAreaList != null) {
            for (BusinessArea aMapArea : aMapAreaList) {
                RemoteBusinessArea area = buildRemoteBusinessArea(aMapArea);
                address.getBusinessAreas().add(area);
            }
        }

        List<PoiItem> aMapItemList = aMapAddress.getPois();
        if (aMapItemList != null) {
            for (PoiItem aMapItem : aMapItemList) {
                RemotePoiItem item = buildRemotePoiItem(aMapItem);
                address.getPois().add(item);
            }
        }

        List<Crossroad> aMapCrossRoadList = aMapAddress.getCrossroads();
        if (aMapCrossRoadList != null) {
            for (Crossroad aMapRoad : aMapCrossRoadList) {
                RemoteCrossroad road = buildRemoteCrossroad(aMapRoad);
                address.getCrossroads().add(road);
            }
        }

        List<RegeocodeRoad> aMapRegeocodeRoadList = aMapAddress.getRoads();
        if (aMapRegeocodeRoadList != null) {
            for (RegeocodeRoad aMapRoad : aMapRegeocodeRoadList) {
                RemoteRegeocodeRoad road = buildRemoteRegeocodeRoad(aMapRoad);
                address.getRoads().add(road);
            }
        }

        address.setCity(aMapAddress.getCity());
        address.setCityCode(aMapAddress.getCityCode());
        address.setDistrict(aMapAddress.getDistrict());
        address.setFormatAddress(aMapAddress.getFormatAddress());
        address.setNeighborhood(aMapAddress.getNeighborhood());
        address.setProvince(aMapAddress.getProvince());
        address.setStreetNumber(buildRemoteStreetNumber(aMapAddress
                .getStreetNumber()));
        address.setTownShip(aMapAddress.getTownship());

        return address;

    }

    static RemoteGeocodeQuery buildRemoteGeocodeQuery(GeocodeQuery aMapQuery) {
        if (aMapQuery == null)
            return null;

        RemoteGeocodeQuery query = new RemoteGeocodeQuery();

        query.setCity(aMapQuery.getCity());
        query.setLocationName(aMapQuery.getLocationName());

        return query;
    }

    static RemoteRegeocodeQuery buildRemoteRegeocodeQuery(
            RegeocodeQuery aMapQuery) {
        if (aMapQuery == null)
            return null;

        RemoteRegeocodeQuery query = new RemoteRegeocodeQuery();

        query.setLatLonType(aMapQuery.getLatLonType());
        query.setRadius(aMapQuery.getRadius());
        query.setPoint(buildRemoteLatLonPoint(aMapQuery.getPoint()));

        return query;
    }

    static RemoteGeocodeResult buildRemoteGeocodeResult(GeocodeResult aMapResult) {
        if (aMapResult == null)
            return null;

        RemoteGeocodeResult result = new RemoteGeocodeResult();

        result.setGeocodeQuery(buildRemoteGeocodeQuery(aMapResult
                .getGeocodeQuery()));

        List<GeocodeAddress> aMapAddressList = aMapResult
                .getGeocodeAddressList();
        if (aMapAddressList != null) {
            for (GeocodeAddress aMapAddress : aMapAddressList) {
                RemoteGeocodeAddress address = buildRemoteGeocodeAddress(aMapAddress);
                result.getGeocodeAddressList().add(address);
            }
        }

        return result;
    }

    static RemoteRegeocodeResult buildRemoteRegeocodeResult(
            RegeocodeResult aMapResult) {
        if (aMapResult == null)
            return null;

        RemoteRegeocodeResult result = new RemoteRegeocodeResult();

        result.setRegeocodeQuery(buildRemoteRegeocodeQuery(aMapResult
                .getRegeocodeQuery()));
        result.setRegeocodeAddress(buildRemoteRegeocodeAddreee(aMapResult
                .getRegeocodeAddress()));

        return result;
    }

    static RemoteDistrictItem buildRemoteDistrictItem(DistrictItem aMapItem) {
        if (aMapItem == null)
            return null;

        RemoteDistrictItem item = new RemoteDistrictItem();

        item.setAdCode(aMapItem.getAdcode());
        item.setCenter(buildRemoteLatLonPoint(aMapItem.getCenter()));
        item.setCityCode(aMapItem.getCitycode());
        item.setLevel(aMapItem.getLevel());
        item.setName(aMapItem.getName());

        List<DistrictItem> aMapItemList = aMapItem.getSubDistrict();
        if (aMapItemList != null) {
            for (DistrictItem aMapSubItem : aMapItemList) {
                RemoteDistrictItem subItem = buildRemoteDistrictItem(aMapSubItem);
                item.getSubDistrict().add(subItem);
            }
        }

        return item;
    }

    static RemoteDistrictQuery buildRemoteDistrictQuery(
            DistrictSearchQuery aMapQuery) {
        if (aMapQuery == null)
            return null;

        RemoteDistrictQuery query = new RemoteDistrictQuery();

        query.setKeywords(aMapQuery.getKeywords());
        query.setKeywordsLevel(aMapQuery.getKeywordsLevel());
        query.setPageNum(aMapQuery.getPageNum());
        query.setPageSize(aMapQuery.getPageSize());
        query.setShowChild(aMapQuery.isShowChild());

        return query;
    }

    static RemoteDistrictResult buildRemoteDistrictResult(
            DistrictResult aMapResult) {
        if (aMapResult == null)
            return null;

        RemoteDistrictResult result = new RemoteDistrictResult();

        result.setPageCount(aMapResult.getPageCount());
        result.setQuery(buildRemoteDistrictQuery(aMapResult.getQuery()));

        List<DistrictItem> aMapItemList = aMapResult.getDistrict();
        if (aMapItemList != null) {
            for (DistrictItem aMapItem : aMapItemList) {
                RemoteDistrictItem item = buildRemoteDistrictItem(aMapItem);
                result.getDistrict().add(item);
            }
        }

        return result;
    }

    static RemotePoiSearchBound buildRemotePoiSearchBound(
            PoiSearch.SearchBound aMapBound) {
        if (aMapBound == null)
            return null;

        RemotePoiSearchBound bound = new RemotePoiSearchBound();

        bound.setCenter(buildRemoteLatLonPoint(aMapBound.getCenter()));
        bound.setDistanceSort(aMapBound.isDistanceSort());
        bound.setLatSpanInMeter(aMapBound.getLatSpanInMeter());
        bound.setLonSpanInMeter(aMapBound.getLonSpanInMeter());
        bound.setLowerLeft(buildRemoteLatLonPoint(aMapBound.getLowerLeft()));
        bound.setRange(aMapBound.getRange());
        bound.setShape(aMapBound.getShape());
        bound.setUpperRight(buildRemoteLatLonPoint(aMapBound.getUpperRight()));

        if (aMapBound.getPolyGonList() != null) {
            for (LatLonPoint aMapPoint : aMapBound.getPolyGonList()) {
                bound.getPolyGonList().add(buildRemoteLatLonPoint(aMapPoint));
            }
        }

        return bound;
    }

    static RemotePoiQuery buildRemotePoiQuery(PoiSearch.Query aMapQuery) {
        if (aMapQuery == null)
            return null;

        RemotePoiQuery query = new RemotePoiQuery();

        query.setCity(aMapQuery.getCity());
        query.setLimitDiscount(aMapQuery.hasDiscountLimit());
        query.setLimitGroupbuy(aMapQuery.hasGroupBuyLimit());
        query.setPageNum(aMapQuery.getPageNum());
        query.setPageSize(aMapQuery.getPageSize());
        query.setQueryString(aMapQuery.getQueryString());
        query.setCategory(aMapQuery.getCategory());

        return query;
    }

    static RemoteSuggestionCity buildRemoteSuggestionCity(
            SuggestionCity aMapCity) {
        if (aMapCity == null)
            return null;

        RemoteSuggestionCity city = new RemoteSuggestionCity(
                aMapCity.getCityName(), aMapCity.getCityCode(),
                aMapCity.getAdCode(), aMapCity.getSuggestionNum());

        return city;
    }

    static RemotePoiResult buildRemotePoiResult(PoiResult aMapResult) {
        if (aMapResult == null)
            return null;

        RemotePoiResult result = new RemotePoiResult();

        result.setBound(buildRemotePoiSearchBound(aMapResult.getBound()));
        result.setPageCount(aMapResult.getPageCount());

        List<PoiItem> poiItemList = aMapResult.getPois();
        if (poiItemList != null) {
            for (PoiItem aMapItem : poiItemList) {
                RemotePoiItem item = buildRemotePoiItem(aMapItem);
                result.getPois().add(item);
            }
        }

        result.setQuery(buildRemotePoiQuery(aMapResult.getQuery()));

        List<SuggestionCity> cityList = aMapResult.getSearchSuggestionCitys();
        if (cityList != null) {
            for (SuggestionCity aMapCity : cityList) {
                RemoteSuggestionCity city = buildRemoteSuggestionCity(aMapCity);
                result.getSearchSuggestionCitys().add(city);
            }
        }

        result.setSearchSuggestionKeywords(aMapResult
                .getSearchSuggestionKeywords());

        return result;
    }

    static RemotePhoto buildRemotePhoto(Photo aMapPhoto) {
        if (aMapPhoto == null)
            return null;

        RemotePhoto photo = new RemotePhoto();

        photo.setTitle(aMapPhoto.getTitle());
        photo.setUrl(aMapPhoto.getUrl());

        return photo;
    }

    static RemoteCinema buildRemoteCinema(Cinema aMapCinema) {
        if (aMapCinema == null)
            return null;

        RemoteCinema cinema = new RemoteCinema();

        cinema.setDeepsrc(aMapCinema.getDeepsrc());
        cinema.setIntro(aMapCinema.getIntro());
        cinema.setOpenTime(aMapCinema.getOpentime());
        cinema.setOpentimeGDF(aMapCinema.getOpentimeGDF());
        cinema.setParking(aMapCinema.getParking());

        List<Photo> photoList = aMapCinema.getPhotos();
        if (photoList != null) {
            for (Photo aMapPhoto : photoList) {
                RemotePhoto photo = buildRemotePhoto(aMapPhoto);
                cinema.getPhotos().add(photo);
            }
        }

        cinema.setRating(aMapCinema.getRating());
        cinema.setSeatOrdering(aMapCinema.isSeatOrdering());

        return cinema;
    }

    static RemoteDining buildRemoteDining(Dining aMapDining) {
        if (aMapDining == null)
            return null;

        RemoteDining dining = new RemoteDining();

        dining.setAddition(aMapDining.getAddition());
        dining.setAtmosphere(aMapDining.getAtmosphere());
        dining.setCost(aMapDining.getCost());
        dining.setCpRating(aMapDining.getCpRating());
        dining.setCuisines(aMapDining.getCuisines());
        dining.setDeepsrc(aMapDining.getDeepsrc());
        dining.setEnvironmentRating(aMapDining.getEnvironmentRating());
        dining.setIntro(aMapDining.getIntro());
        dining.setMealOrdering(aMapDining.isMealOrdering());
        dining.setOpenTime(aMapDining.getOpentime());
        dining.setOpentimeGDF(aMapDining.getOpentimeGDF());
        dining.setOrderingAppUrl(aMapDining.getOrderinAppUrl());
        dining.setOrderingWapUrl(aMapDining.getOrderingWapUrl());
        dining.setOrderingWebUrl(aMapDining.getOrderingWebUrl());

        List<Photo> photoList = aMapDining.getPhotos();
        if (photoList != null) {
            for (Photo aMapPhoto : photoList) {
                RemotePhoto photo = buildRemotePhoto(aMapPhoto);
                dining.getPhotos().add(photo);
            }
        }

        dining.setRating(dining.getRating());
        dining.setRecommend(dining.getRecommend());
        dining.setServiceRating(dining.getServiceRating());
        dining.setTag(dining.getTag());
        dining.setTasteRating(dining.getTasteRating());

        return dining;
    }

    static RemoteDiscount buildRemoteDiscount(Discount aMapDiscount) {
        if (aMapDiscount == null)
            return null;

        RemoteDiscount discount = new RemoteDiscount();

        discount.setDetail(aMapDiscount.getDetail());
        // discount.setEndTime(aMapDiscount.getEndTime());
        discount.setProvider(aMapDiscount.getProvider());
        discount.setSoldCount(aMapDiscount.getSoldCount());
        // discount.setStartTime(aMapDiscount.getStartTime());
        discount.setTitle(aMapDiscount.getTitle());
        discount.setUrl(aMapDiscount.getUrl());

        List<Photo> photoList = aMapDiscount.getPhotos();
        if (photoList != null) {
            for (Photo aMapPhoto : photoList) {
                RemotePhoto photo = buildRemotePhoto(aMapPhoto);
                discount.getPhotos().add(photo);
            }
        }

        return discount;
    }

    static RemoteGroupbuy buildRemoteGroupbuy(Groupbuy aMapBuy) {
        if (aMapBuy == null)
            return null;

        RemoteGroupbuy buy = new RemoteGroupbuy();

        buy.setCount(aMapBuy.getCount());
        buy.setDetail(aMapBuy.getDetail());
        buy.setDiscount(aMapBuy.getDiscount());
        // buy.setEndTime(aMapBuy.getEndTime());
        buy.setGroupbuyPrice(aMapBuy.getGroupbuyPrice());
        buy.setOriginalPrice(aMapBuy.getOriginalPrice());
        buy.setProvider(aMapBuy.getProvider());
        buy.setSoldCount(aMapBuy.getSoldCount());
        // buy.setStartTime(aMapBuy.getStartTime());
        buy.setTicketAddress(aMapBuy.getTicketAddress());
        buy.setTicketTel(aMapBuy.getTicketTel());
        buy.setTypeCode(aMapBuy.getTypeCode());
        buy.setTypeDes(aMapBuy.getTypeDes());
        buy.setUrl(aMapBuy.getUrl());

        List<Photo> photoList = aMapBuy.getPhotos();
        if (photoList != null) {
            for (Photo aMapPhoto : photoList) {
                RemotePhoto photo = buildRemotePhoto(aMapPhoto);
                buy.getPhotos().add(photo);
            }
        }

        return buy;
    }

    static RemoteHotel buildRemoteHotel(Hotel aMapHotel) {
        if (aMapHotel == null)
            return null;

        RemoteHotel hotel = new RemoteHotel();

        hotel.setAddition(aMapHotel.getAddition());
        hotel.setDeepsrc(aMapHotel.getDeepsrc());
        hotel.setEnvironmentRating(aMapHotel.getEnvironmentRating());
        hotel.setFaciRating(aMapHotel.getFaciRating());
        hotel.setHealthRating(aMapHotel.getHealthRating());
        hotel.setIntro(aMapHotel.getIntro());
        hotel.setLowestPrice(aMapHotel.getLowestPrice());

        List<Photo> photoList = aMapHotel.getPhotos();
        if (photoList != null) {
            for (Photo aMapPhoto : photoList) {
                RemotePhoto photo = buildRemotePhoto(aMapPhoto);
                hotel.getPhotos().add(photo);
            }
        }

        hotel.setRating(aMapHotel.getRating());
        hotel.setServiceRating(aMapHotel.getServiceRating());
        hotel.setStar(aMapHotel.getStar());
        hotel.setTraffic(aMapHotel.getTraffic());

        return hotel;
    }

    static RemoteScenic buildRemoteScenic(Scenic aMapScenic) {
        if (aMapScenic == null)
            return null;

        RemoteScenic scenic = new RemoteScenic();

        scenic.setDeepsec(aMapScenic.getDeepsrc());
        scenic.setIntro(aMapScenic.getIntro());
        scenic.setLevel(aMapScenic.getLevel());
        scenic.setOpenTime(aMapScenic.getOpentime());
        scenic.setOpentimeGDF(aMapScenic.getOpentimeGDF());
        scenic.setOrderWapUrl(aMapScenic.getOrderWapUrl());
        scenic.setOrderWebUrl(aMapScenic.getOrderWebUrl());

        List<Photo> photoList = aMapScenic.getPhotos();
        if (photoList != null) {
            for (Photo aMapPhoto : photoList) {
                RemotePhoto photo = buildRemotePhoto(aMapPhoto);
                scenic.getPhotos().add(photo);
            }
        }

        scenic.setPrice(aMapScenic.getPrice());
        scenic.setRating(aMapScenic.getRating());
        scenic.setRecommend(aMapScenic.getRecommend());
        scenic.setSeason(aMapScenic.getSeason());
        scenic.setTheme(aMapScenic.getTheme());

        return scenic;
    }

    static RemotePoiItemDetail buildRemotePoiItemDetail(PoiItemDetail aMapDetail) {
        if (aMapDetail == null)
            return null;

        RemotePoiItemDetail detail = new RemotePoiItemDetail();

        detail.setCinema(buildRemoteCinema(aMapDetail.getCinema()));
        detail.setDining(buildRemoteDining(aMapDetail.getDining()));
        detail.setHotel(buildRemoteHotel(aMapDetail.getHotel()));
        detail.setScenic(buildRemoteScenic(aMapDetail.getScenic()));

        if (aMapDetail.getDeepType() != null) {
            switch (aMapDetail.getDeepType()) {

            case DINING:
                detail.setDeepType(DeepType.DINING);
                break;
            case HOTEL:
                detail.setDeepType(DeepType.HOTEL);
                break;
            case CINEMA:
                detail.setDeepType(DeepType.CINEMA);
                break;
            case SCENIC:
                detail.setDeepType(DeepType.SCENIC);
                break;
            case UNKNOWN:
            default:
                detail.setDeepType(DeepType.UNKNOWN);
                break;
            }
        }

        if (aMapDetail.getDiscounts() != null) {
            for (Discount aMapDiscount : aMapDetail.getDiscounts()) {
                detail.getDiscounts().add(buildRemoteDiscount(aMapDiscount));
            }
        }

        if (aMapDetail.getGroupbuys() != null) {
            for (Groupbuy aMapBuy : aMapDetail.getGroupbuys()) {
                detail.getGroupbuys().add(buildRemoteGroupbuy(aMapBuy));
            }
        }

        detail.setAdCode(aMapDetail.getAdCode());
        detail.setAdName(aMapDetail.getAdName());
        detail.setCityCode(aMapDetail.getCityCode());
        detail.setCityName(aMapDetail.getCityName());
        detail.setDirection(aMapDetail.getDirection());
        detail.setDiscountInfo(aMapDetail.isDiscountInfo());
        detail.setDistance(aMapDetail.getDistance());
        detail.setEmail(aMapDetail.getEmail());
        detail.setEnter(buildRemoteLatLonPoint(aMapDetail.getEnter()));
        detail.setExit(buildRemoteLatLonPoint(aMapDetail.getExit()));
        detail.setGroupBuyInfo(aMapDetail.isGroupbuyInfo());
        detail.setIndoormap(aMapDetail.isIndoorMap());
        detail.setPostCode(aMapDetail.getPostcode());
        detail.setProvinceCode(aMapDetail.getProvinceCode());
        detail.setProvinceName(aMapDetail.getProvinceName());
        detail.setTel(aMapDetail.getTel());
        detail.setTypeDes(aMapDetail.getTypeDes());
        detail.setWebSite(aMapDetail.getWebsite());
        detail.setSnippet(aMapDetail.getSnippet());
        detail.setPoiId(aMapDetail.getPoiId());
        detail.setLatLonPoint(buildRemoteLatLonPoint(aMapDetail
                .getLatLonPoint()));
        detail.setTitle(aMapDetail.getTitle());

        return detail;
    }

    static RemoteTip buildRemoteTip(Tip aMapTip) {
        if (aMapTip == null)
            return null;

        RemoteTip tip = new RemoteTip();

        tip.setAdCode(aMapTip.getAdcode());
        tip.setDistrict(aMapTip.getDistrict());
        tip.setName(aMapTip.getName());

        return tip;
    }

    static RemoteBusStationItem buildRemoteBusStationItem(
            BusStationItem aMapItem) {
        if (aMapItem == null)
            return null;

        RemoteBusStationItem item = new RemoteBusStationItem();

        item.setAdCode(aMapItem.getAdCode());

        List<BusLineItem> aMapBusLineItemList = aMapItem.getBusLineItems();
        if (aMapBusLineItemList != null) {
            for (BusLineItem aMapBusLineItem : aMapBusLineItemList) {
                RemoteBusLineItem busLineItem = buildRemoteBusLineItem(aMapBusLineItem);
                item.getBusLineItems().add(busLineItem);
            }
        }

        item.setBusStationId(aMapItem.getBusStationId());
        item.setBusStationName(aMapItem.getBusStationName());
        item.setCityCode(aMapItem.getCityCode());
        item.setLatLonPoint(buildRemoteLatLonPoint(aMapItem.getLatLonPoint()));

        return item;
    }

    static RemoteBusLineItem buildRemoteBusLineItem(BusLineItem aMapItem) {
        if (aMapItem == null)
            return null;

        RemoteBusLineItem item = new RemoteBusLineItem();

        item.setBasicPrice(aMapItem.getBasicPrice());

        List<LatLonPoint> aMapBounds = aMapItem.getBounds();
        if (aMapBounds != null) {
            for (LatLonPoint aMapPoint : aMapBounds) {
                RemoteLatLonPoint point = buildRemoteLatLonPoint(aMapPoint);
                item.getBounds().add(point);
            }
        }

        List<LatLonPoint> aMapCoordinates = aMapItem.getDirectionsCoordinates();
        if (aMapCoordinates != null) {
            for (LatLonPoint aMapPoint : aMapCoordinates) {
                RemoteLatLonPoint point = buildRemoteLatLonPoint(aMapPoint);
                item.getDirectionsCoordinates().add(point);
            }
        }

        item.setBusCompany(aMapItem.getBusCompany());
        item.setBusLineId(aMapItem.getBusLineId());
        item.setBusLineName(aMapItem.getBusLineName());
        item.setBusLineType(aMapItem.getBusLineType());

        List<BusStationItem> aMapBusStationItemList = aMapItem.getBusStations();
        if (aMapBusStationItemList != null) {
            for (BusStationItem aMapBusStationItem : aMapBusStationItemList) {
                RemoteBusStationItem busStationItem = buildRemoteBusStationItem(aMapBusStationItem);
                item.getBusStations().add(busStationItem);
            }
        }

        item.setCityCode(aMapItem.getCityCode());
        item.setDistance(aMapItem.getDistance());
        // item.setFirstBusTime(aMapItem.getFirstBusTime());
        // item.setLastBusTime(aMapItem.getLastBusTime());
        item.setOriginatingStation(aMapItem.getOriginatingStation());
        item.setTerminalStation(aMapItem.getTerminalStation());
        item.setTotalPrice(aMapItem.getTotalPrice());

        return item;
    }

    static RemoteBusLineQuery buildRemoteBusLineQuery(BusLineQuery aMapQuery) {
        if (aMapQuery == null)
            return null;

        RemoteBusLineQuery query = new RemoteBusLineQuery();

        query.setCity(aMapQuery.getCity());
        query.setPageNumber(aMapQuery.getPageNumber());
        query.setPageSize(aMapQuery.getPageSize());
        query.setQueryString(aMapQuery.getQueryString());

        if (aMapQuery.getCategory() != null) {
            switch (aMapQuery.getCategory()) {
            case BY_LINE_ID:
                query.setCategory(RemoteBusLineQuery.SearchType.BY_LINE_ID);
                break;
            case BY_LINE_NAME:
                query.setCategory(RemoteBusLineQuery.SearchType.BY_LINE_NAME);
                break;
            default:
                query.setCategory(RemoteBusLineQuery.SearchType.BY_LINE_ID);
                break;
            }
        }

        return query;
    }

    static RemoteBusStationQuery buildRemoteBusStationQuery(
            BusStationQuery aMapQuery) {
        if (aMapQuery == null)
            return null;

        RemoteBusStationQuery query = new RemoteBusStationQuery();

        query.setCity(aMapQuery.getCity());
        query.setPageNumber(aMapQuery.getPageNumber());
        query.setPageSize(aMapQuery.getPageSize());
        query.setQueryString(aMapQuery.getQueryString());

        return query;
    }

    static RemoteBusLineResult buildRemoteBusLineResult(BusLineResult aMapResult) {
        if (aMapResult == null)
            return null;

        RemoteBusLineResult result = new RemoteBusLineResult();

        List<BusLineItem> aMapBusLineItemList = aMapResult.getBusLines();
        if (aMapBusLineItemList != null) {
            for (BusLineItem aMapBusLineItem : aMapBusLineItemList) {
                RemoteBusLineItem busLineItem = buildRemoteBusLineItem(aMapBusLineItem);
                result.getBusLines().add(busLineItem);
            }
        }

        result.setPageCount(aMapResult.getPageCount());
        result.setQuery(buildRemoteBusLineQuery(aMapResult.getQuery()));

        List<SuggestionCity> cityList = aMapResult.getSearchSuggestionCities();
        if (cityList != null) {
            for (SuggestionCity aMapCity : cityList) {
                RemoteSuggestionCity city = buildRemoteSuggestionCity(aMapCity);
                result.getSearchSuggestionCities().add(city);
            }
        }

        result.setSearchSuggestionKeywords(aMapResult
                .getSearchSuggestionKeywords());

        return result;
    }

    static RemoteBusStationResult buildRemoteBusStationResult(
            BusStationResult aMapResult) {
        if (aMapResult == null)
            return null;

        RemoteBusStationResult result = new RemoteBusStationResult();

        List<BusStationItem> aMapBusStationItemList = aMapResult
                .getBusStations();
        if (aMapBusStationItemList != null) {
            for (BusStationItem aMapBusStationItem : aMapBusStationItemList) {
                RemoteBusStationItem busStationItem = buildRemoteBusStationItem(aMapBusStationItem);
                result.getBusStations().add(busStationItem);
            }
        }

        result.setPageCount(aMapResult.getPageCount());
        result.setQuery(buildRemoteBusStationQuery(aMapResult.getQuery()));

        List<SuggestionCity> cityList = aMapResult.getSearchSuggestionCities();
        if (cityList != null) {
            for (SuggestionCity aMapCity : cityList) {
                RemoteSuggestionCity city = buildRemoteSuggestionCity(aMapCity);
                result.getSearchSuggestionCities().add(city);
            }
        }

        result.setSearchSuggestionKeywords(aMapResult
                .getSearchSuggestionKeywords());

        return result;
    }

    static RemoteFromAndTo buildRemoteFromAndTo(FromAndTo aMapFromAndTo) {
        if (aMapFromAndTo == null)
            return null;

        RemoteFromAndTo fromAndTo = new RemoteFromAndTo();

        fromAndTo.setDestinationPoiId(aMapFromAndTo.getDestinationPoiID());
        fromAndTo.setFrom(buildRemoteLatLonPoint(aMapFromAndTo.getFrom()));
        fromAndTo.setStartPoiId(aMapFromAndTo.getStartPoiID());
        fromAndTo.setTo(buildRemoteLatLonPoint(aMapFromAndTo.getTo()));

        return fromAndTo;
    }

    static RemoteBusRouteQuery buildRemoteBusRouteQuery(BusRouteQuery aMapQuery) {
        if (aMapQuery == null)
            return null;

        RemoteBusRouteQuery query = new RemoteBusRouteQuery();

        query.setCity(aMapQuery.getCity());
        query.setFromAndTo(buildRemoteFromAndTo(aMapQuery.getFromAndTo()));
        query.setMode(aMapQuery.getMode());
        query.setNightFlag(aMapQuery.getNightFlag());

        return query;
    }

    static RemoteDriveRouteQuery buildRemoteDriveRouteQuery(
            DriveRouteQuery aMapQuery) {
        if (aMapQuery == null)
            return null;

        RemoteDriveRouteQuery query = new RemoteDriveRouteQuery();

        if (query.getAvoidpolygons() != null) {
            List<RemoteLatLonPoint> polygon = new ArrayList<RemoteLatLonPoint>();
            for (List<LatLonPoint> pointList : aMapQuery.getAvoidpolygons()) {
                if (pointList != null) {
                    for (LatLonPoint aMapPoint : pointList) {
                        polygon.add(buildRemoteLatLonPoint(aMapPoint));
                    }
                }
                query.getAvoidpolygons().add(polygon);
            }
        }

        if (query.getPassedByPoints() != null) {
            for (LatLonPoint aMapPoint : aMapQuery.getPassedByPoints()) {
                query.getPassedByPoints()
                        .add(buildRemoteLatLonPoint(aMapPoint));
            }
        }

        query.setAvoidRoad(aMapQuery.getAvoidRoad());
        query.setFromAndTo(buildRemoteFromAndTo(aMapQuery.getFromAndTo()));
        query.setMode(aMapQuery.getMode());

        return query;
    }

    static RemoteWalkRouteQuery buildRemoteWalkRouteQuery(
            WalkRouteQuery aMapQuery) {
        if (aMapQuery == null)
            return null;

        RemoteWalkRouteQuery query = new RemoteWalkRouteQuery();

        query.setFromAndTo(buildRemoteFromAndTo(aMapQuery.getFromAndTo()));
        query.setMode(aMapQuery.getMode());

        return query;
    }

    static RemotePath buildRemotePath(Path aMapPath) {
        RemotePath path = new RemotePath();

        path.setDistance(aMapPath.getDistance());
        path.setDuration(aMapPath.getDuration());

        return path;
    }

    static RemoteWalkStep buildRemoteWalkStep(WalkStep aMapStep) {
        if (aMapStep == null)
            return null;

        RemoteWalkStep step = new RemoteWalkStep();

        step.setAction(aMapStep.getAction());
        step.setAssistantAction(aMapStep.getAssistantAction());
        step.setDistance(aMapStep.getDistance());
        step.setDuration(aMapStep.getDuration());
        step.setInstruction(aMapStep.getInstruction());
        step.setOrientation(aMapStep.getOrientation());
        step.setRoad(aMapStep.getRoad());

        if (aMapStep.getPolyline() != null) {
            for (LatLonPoint aMapPoint : aMapStep.getPolyline()) {
                step.getPolyline().add(buildRemoteLatLonPoint(aMapPoint));
            }
        }

        return step;
    }

    static RemoteWalkPath buildRemoteWalkPath(WalkPath aMapPath) {
        if (aMapPath == null)
            return null;

        RemoteWalkPath path = new RemoteWalkPath();

        path.setDistance(aMapPath.getDistance());
        path.setDuration(aMapPath.getDuration());

        if (aMapPath.getSteps() != null) {
            for (WalkStep aMapStep : aMapPath.getSteps()) {
                path.getSteps().add(buildRemoteWalkStep(aMapStep));
            }
        }

        return path;
    }

    static RemoteRouteBusWalkItem buildRemoteRouteBusWalkItem(
            RouteBusWalkItem aMapItem) {
        if (aMapItem == null)
            return null;

        RemoteRouteBusWalkItem item = new RemoteRouteBusWalkItem();

        item.setDestination(buildRemoteLatLonPoint(aMapItem.getDestination()));
        item.setDistance(aMapItem.getDistance());
        item.setDuration(aMapItem.getDuration());
        item.setOrigin(buildRemoteLatLonPoint(aMapItem.getOrigin()));

        if (aMapItem.getSteps() != null) {
            for (WalkStep aMapStep : aMapItem.getSteps()) {
                item.getSteps().add(buildRemoteWalkStep(aMapStep));
            }
        }

        return item;

    }

    static RemoteRouteBusLineItem buildRemoteRouteBusLineItem(
            RouteBusLineItem aMapItem) {
        if (aMapItem == null)
            return null;

        RemoteRouteBusLineItem item = new RemoteRouteBusLineItem();

        item.setBasicPrice(aMapItem.getBasicPrice());
        item.setArrivalBusStation(buildRemoteBusStationItem(aMapItem
                .getArrivalBusStation()));

        if (aMapItem.getBounds() != null) {
            for (LatLonPoint aMapPoint : aMapItem.getBounds()) {
                item.getBounds().add(buildRemoteLatLonPoint(aMapPoint));
            }
        }

        item.setBusCompany(aMapItem.getBusCompany());
        item.setBusLineId(aMapItem.getBusLineId());
        item.setBusLineName(aMapItem.getBusLineName());
        item.setBusLineType(aMapItem.getBusLineType());

        if (aMapItem.getBusStations() != null) {
            for (BusStationItem aMapBusStationItem : aMapItem.getBusStations()) {
                item.getBusStations().add(
                        buildRemoteBusStationItem(aMapBusStationItem));
            }
        }

        item.setCityCode(aMapItem.getCityCode());
        item.setDepartureBusStation(buildRemoteBusStationItem(aMapItem
                .getDepartureBusStation()));
        item.setDistance(aMapItem.getDistance());
        item.setDuration(aMapItem.getDuration());
        item.setOriginatingStation(aMapItem.getOriginatingStation());
        item.setPassStationNum(aMapItem.getPassStationNum());

        if (aMapItem.getPassStations() != null) {
            for (BusStationItem aMapBusStationItem : aMapItem.getPassStations()) {
                item.getPassStations().add(
                        buildRemoteBusStationItem(aMapBusStationItem));
            }
        }

        if (aMapItem.getPolyline() != null) {
            for (LatLonPoint aMapPoint : aMapItem.getPolyline()) {
                item.getPolyline().add(buildRemoteLatLonPoint(aMapPoint));
            }
        }

        if (aMapItem.getDirectionsCoordinates() != null) {
            for (LatLonPoint aMapPoint : aMapItem.getDirectionsCoordinates()) {
                item.getDirectionsCoordinates().add(
                        buildRemoteLatLonPoint(aMapPoint));
            }
        }

        item.setTerminalStation(aMapItem.getTerminalStation());
        item.setTotalPrice(aMapItem.getTotalPrice());

        // item.setFirstBusTime(aMapItem.getFirstBusTime());
        // item.setLastBusTime(aMapItem.getLastBusTime());

        return item;

    }

    static RemoteDoorway buildRemoteDoorway(Doorway aMapWay) {
        if (aMapWay == null)
            return null;

        RemoteDoorway way = new RemoteDoorway();

        way.setLatLonPoint(buildRemoteLatLonPoint(aMapWay.getLatLonPoint()));
        way.setName(aMapWay.getName());

        return way;
    }

    static RemoteBusPath buildRemoteBusPath(BusPath aMapPath) {
        if (aMapPath == null)
            return null;

        RemoteBusPath path = new RemoteBusPath();

        path.setBusDistance(aMapPath.getBusDistance());
        path.setCost(aMapPath.getCost());
        path.setDistance(aMapPath.getDistance());
        path.setDuration(aMapPath.getDuration());
        path.setNightBus(aMapPath.isNightBus());

        if (aMapPath.getSteps() != null) {
            for (BusStep aMapStep : aMapPath.getSteps()) {
                path.getSteps().add(buildRemoteBusStep(aMapStep));
            }
        }

        path.setWalkDistance(aMapPath.getWalkDistance());

        return path;
    }

    static RemoteBusStep buildRemoteBusStep(BusStep aMapStep) {
        if (aMapStep == null)
            return null;

        RemoteBusStep step = new RemoteBusStep();

        step.setBusLine(buildRemoteRouteBusLineItem(aMapStep.getBusLine()));

        // TODO: AMap have not implements getBusLines
        // step.setBusLines(aMapStep.getBusLines);

        step.setEntrance(buildRemoteDoorway(aMapStep.getEntrance()));
        step.setExit(buildRemoteDoorway(aMapStep.getExit()));
        step.setWalk(buildRemoteRouteBusWalkItem(aMapStep.getWalk()));

        return step;
    }

    static RemoteRouteResult buildRemoteRouteResult(RouteResult aMapResult) {
        if (aMapResult == null)
            return null;

        RemoteRouteResult result = new RemoteRouteResult();

        result.setStartPos(buildRemoteLatLonPoint(aMapResult.getStartPos()));
        result.setTartgetPos(buildRemoteLatLonPoint(aMapResult.getTargetPos()));

        return result;
    }

    static RemoteBusRouteResult buildRemoteBusRouteResult(
            BusRouteResult aMapResult) {
        if (aMapResult == null)
            return null;

        RemoteBusRouteResult result = new RemoteBusRouteResult();

        result.setBusRouteQuery(buildRemoteBusRouteQuery(aMapResult
                .getBusQuery()));

        if (aMapResult.getPaths() != null) {
            for (BusPath aMapPath : aMapResult.getPaths()) {
                result.getPaths().add(buildRemoteBusPath(aMapPath));
            }
        }

        result.setStartPos(buildRemoteLatLonPoint(aMapResult.getStartPos()));
        result.setTartgetPos(buildRemoteLatLonPoint(aMapResult.getTargetPos()));
        result.setTaxiCost(aMapResult.getTaxiCost());

        return result;
    }

    static RemoteDistrict buildRemoteDistrict(District aMapDistrict) {
        if (aMapDistrict == null)
            return null;

        RemoteDistrict district = new RemoteDistrict();

        district.setDistrictAdCode(aMapDistrict.getDistrictAdcode());
        district.setDistrictName(aMapDistrict.getDistrictName());

        return district;
    }

    static RemoteSearchCity buildRemoteSearchCity(SearchCity aMapCity) {
        if (aMapCity == null)
            return null;

        RemoteSearchCity city = new RemoteSearchCity();

        city.setSearchCityAdCode(aMapCity.getSearchCityAdCode());
        city.setSearchCityCode(aMapCity.getSearchCitycode());
        city.setSearchCityName(aMapCity.getSearchCityName());

        return city;
    }

    static RemoteRouteSearchCity buildRemoteRouteSearchCity(
            RouteSearchCity aMapCity) {
        if (aMapCity == null)
            return null;

        RemoteRouteSearchCity city = new RemoteRouteSearchCity();

        if (aMapCity.getDistricts() != null) {
            for (District aMapDistrict : aMapCity.getDistricts()) {
                city.getDistricts().add(buildRemoteDistrict(aMapDistrict));
            }
        }

        city.setSearchCityAdCode(aMapCity.getSearchCityAdCode());
        city.setSearchCityCode(aMapCity.getSearchCitycode());
        city.setSearchCityName(aMapCity.getSearchCityName());

        return city;

    }

    static RemoteDriveStep buildRemoteDriveStep(DriveStep aMapStep) {
        if (aMapStep == null)
            return null;

        RemoteDriveStep step = new RemoteDriveStep();

        step.setAction(aMapStep.getAction());
        step.setAssitantAction(aMapStep.getAssistantAction());
        step.setDistance(aMapStep.getDistance());
        step.setDuration(aMapStep.getDuration());
        step.setInstruction(aMapStep.getInstruction());
        step.setOrientation(aMapStep.getOrientation());

        if (aMapStep.getPolyline() != null) {
            for (LatLonPoint aMapPoint : aMapStep.getPolyline()) {
                step.getPolyline().add(buildRemoteLatLonPoint(aMapPoint));
            }
        }

        step.setRoad(aMapStep.getRoad());

        if (aMapStep.getRouteSearchCityList() != null) {
            for (RouteSearchCity aMapCity : aMapStep.getRouteSearchCityList()) {
                step.getRouteSearchCityList().add(
                        buildRemoteRouteSearchCity(aMapCity));
            }
        }

        step.setTollDistance(aMapStep.getTollDistance());
        step.setTollRoad(aMapStep.getTollRoad());
        step.setTolls(aMapStep.getTolls());

        return step;
    }

    static RemoteDrivePath buildRemoteDrivePath(DrivePath aMapPath) {
        if (aMapPath == null)
            return null;

        RemoteDrivePath path = new RemoteDrivePath();

        path.setDistance(aMapPath.getDistance());
        path.setDuration(aMapPath.getDuration());

        if (aMapPath.getSteps() != null) {
            for (DriveStep aMapStep : aMapPath.getSteps()) {
                path.getSteps().add(buildRemoteDriveStep(aMapStep));
            }
        }

        path.setStrategy(aMapPath.getStrategy());
        path.setTollDistance(aMapPath.getTollDistance());
        path.setTolls(aMapPath.getTolls());

        return path;
    }

    static RemoteDriveRouteResult buildRemoteDriveRouteResult(
            DriveRouteResult aMapResult) {
        if (aMapResult == null)
            return null;

        RemoteDriveRouteResult result = new RemoteDriveRouteResult();

        result.setTaxiCost(aMapResult.getTaxiCost());
        result.setDriveQuery(buildRemoteDriveRouteQuery(aMapResult
                .getDriveQuery()));

        if (aMapResult.getPaths() != null) {
            for (DrivePath aMapPath : aMapResult.getPaths()) {
                result.getPaths().add(buildRemoteDrivePath(aMapPath));
            }
        }

        result.setStartPos(buildRemoteLatLonPoint(aMapResult.getStartPos()));
        result.setTartgetPos(buildRemoteLatLonPoint(aMapResult.getTargetPos()));

        return result;
    }

    static RemoteWalkRouteResult buildRemoteWalkRouteResult(
            WalkRouteResult aMapResult) {
        if (aMapResult == null)
            return null;

        RemoteWalkRouteResult result = new RemoteWalkRouteResult();

        result.setWalkQuery(buildRemoteWalkRouteQuery(aMapResult
                .getWalkQuery()));

        if (aMapResult.getPaths() != null) {
            for (WalkPath aMapPath : aMapResult.getPaths()) {
                result.getPaths().add(buildRemoteWalkPath(aMapPath));
            }
        }

        result.setStartPos(buildRemoteLatLonPoint(aMapResult.getStartPos()));
        result.setTartgetPos(buildRemoteLatLonPoint(aMapResult.getTargetPos()));

        return result;
    }
}
