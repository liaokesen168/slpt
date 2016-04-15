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
import java.util.Iterator;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.os.Build;

import com.amap.api.location.AMapLocalDayWeatherForecast;
import com.amap.api.location.AMapLocalWeatherForecast;
import com.amap.api.location.AMapLocalWeatherLive;
import com.amap.api.location.AMapLocation;

class ProxyUtils {

    private static double pi = 3.1415926535897932384626;
    private static double a = 6378245.0;
    private static double ee = 0.00669342162296594323;

    static RemoteGpsSatellite buildRemoteGpsSatellite(GpsSatellite satellite) {
        if (satellite == null)
            return null;

        RemoteGpsSatellite remoteSatellite = new RemoteGpsSatellite();

        remoteSatellite.setAlmanac(satellite.hasAlmanac());
        remoteSatellite.setAzimuth(satellite.getAzimuth());
        remoteSatellite.setElevation(satellite.getElevation());
        remoteSatellite.setEphemeris(satellite.hasEphemeris());
        remoteSatellite.setSnr(satellite.getSnr());
        remoteSatellite.setUsedInFix(satellite.usedInFix());

        return remoteSatellite;
    }

    static RemoteGpsStatus buildRemoteGpsStatus(GpsStatus status) {
        if (status == null)
            return null;

        RemoteGpsStatus remoteStatus = new RemoteGpsStatus();

        remoteStatus.setTimeToFirstFix(status.getTimeToFirstFix());

        Iterator<GpsSatellite> it = status.getSatellites().iterator();
        while (it.hasNext()) {
            remoteStatus.getSatellites()
                    .add(buildRemoteGpsSatellite(it.next()));
        }

        return remoteStatus;
    }

    static RemoteWeatherLive buildRemoteWeatherLive(
            AMapLocalWeatherLive aMapLocalweatherLive) {
        if (aMapLocalweatherLive == null)
            return null;

        RemoteWeatherLive weather = new RemoteWeatherLive();

        weather.setCity(aMapLocalweatherLive.getCity());
        weather.setCityCode(aMapLocalweatherLive.getCityCode());
        weather.setHumidity(aMapLocalweatherLive.getHumidity());
        weather.setProvince(aMapLocalweatherLive.getProvince());
        weather.setReportTime(aMapLocalweatherLive.getReportTime());
        weather.setTemperature(aMapLocalweatherLive.getTemperature());
        weather.setWeather(aMapLocalweatherLive.getWeather());
        weather.setWindDir(aMapLocalweatherLive.getWindDir());
        weather.setWindPower(aMapLocalweatherLive.getWindPower());
        weather.setErrorCode(aMapLocalweatherLive.getAMapException()
                .getErrorCode());

        return weather;
    }

    static RemoteDayWeatherForecast buildRemoteDayWeatherForecast(
            AMapLocalDayWeatherForecast aMapDayForecast) {
        if (aMapDayForecast == null)
            return null;

        RemoteDayWeatherForecast dayForecast = new RemoteDayWeatherForecast();

        dayForecast.setCity(aMapDayForecast.getCity());
        dayForecast.setCityCode(aMapDayForecast.getCityCode());
        dayForecast.setDate(aMapDayForecast.getDate());
        dayForecast.setDayTemp(aMapDayForecast.getDayTemp());
        dayForecast.setDayWeather(aMapDayForecast.getDayWeather());
        dayForecast.setDayWindDir(aMapDayForecast.getDayWindDir());
        dayForecast.setDayWindPower(aMapDayForecast.getDayWindPower());
        dayForecast.setNightTemp(aMapDayForecast.getNightTemp());
        dayForecast.setNightWeather(aMapDayForecast.getNightWeather());
        dayForecast.setNightWindDir(aMapDayForecast.getNightWindDir());
        dayForecast.setNightWindPower(aMapDayForecast.getNightWindPower());
        dayForecast.setProvince(aMapDayForecast.getProvince());
        dayForecast.setWeek(aMapDayForecast.getWeek());

        return dayForecast;
    }

    static RemoteWeatherForecast buildRemoteWeatherForecast(
            AMapLocalWeatherForecast aMapWeatherForecast) {
        if (aMapWeatherForecast == null)
            return null;

        RemoteWeatherForecast weatherForecast = new RemoteWeatherForecast();

        weatherForecast.setReportTime(aMapWeatherForecast.getReportTime());

        ArrayList<AMapLocalDayWeatherForecast> aMapWeatherForecastList = (ArrayList<AMapLocalDayWeatherForecast>) aMapWeatherForecast
                .getWeatherForecast();

        ArrayList<RemoteDayWeatherForecast> localWeatherForecastList = new ArrayList<RemoteDayWeatherForecast>();

        if (aMapWeatherForecastList != null) {
            for (AMapLocalDayWeatherForecast aMapDayForecast : aMapWeatherForecastList) {
                localWeatherForecastList
                        .add(buildRemoteDayWeatherForecast(aMapDayForecast));
            }
        }

        weatherForecast.setListWeatherForecast(localWeatherForecastList);
        weatherForecast.setErrorCode(aMapWeatherForecast.getAMapException()
                .getErrorCode());

        return weatherForecast;
    }

    static RemoteLocation buildRemoteLocation(AMapLocation aMapLocation) {
        RemoteLocation location = new RemoteLocation();

        if (aMapLocation == null) {
            location.setProvider(RemoteLocationServiceManager.IWDS_NETWORK_PROVIDER);
            location.setErrorCode(RemoteLocationErrorCode.ERROR_CODE_NULL_PARAMETER);
            return location;
        }

        location.setProvider(aMapLocation.getProvider());
        location.setTime(aMapLocation.getTime());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            location.setElapsedRealtimeNanos(aMapLocation
                    .getElapsedRealtimeNanos());

        location.setLatitude(aMapLocation.getLatitude());
        location.setLongitude(aMapLocation.getLongitude());

        if (aMapLocation.hasAltitude())
            location.setAltitude(aMapLocation.getAltitude());

        if (aMapLocation.hasSpeed())
            location.setSpeed(aMapLocation.getSpeed());

        if (aMapLocation.hasBearing())
            location.setBearing(aMapLocation.getBearing());

        if (aMapLocation.hasAccuracy())
            location.setAccuracy(aMapLocation.getAccuracy());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            location.setIsFromMockProvider(aMapLocation.isFromMockProvider());

        location.setAdCode(aMapLocation.getAdCode());
        location.setAddress(aMapLocation.getAddress());
        location.setCity(aMapLocation.getCity());
        location.setCityCode(aMapLocation.getCityCode());
        location.setCountry(aMapLocation.getCountry());
        location.setDistrict(aMapLocation.getDistrict());
        location.setFloor(aMapLocation.getFloor());
        location.setPoiId(aMapLocation.getPoiId());
        location.setPoiName(aMapLocation.getPoiName());
        location.setProvince(aMapLocation.getProvince());
        location.setRoad(aMapLocation.getRoad());
        location.setStreet(aMapLocation.getStreet());
        location.setErrorCode(aMapLocation.getAMapException().getErrorCode());

        return location;
    }

    static RemoteLocation buildRemoteLocation(Location androidLocation) {
        RemoteLocation location = new RemoteLocation();

        if (androidLocation == null) {
            location.setProvider(RemoteLocationServiceManager.GPS_PROVIDER);
            location.setErrorCode(RemoteLocationErrorCode.ERROR_CODE_NULL_PARAMETER);
            return location;
        }

        location.setProvider(androidLocation.getProvider());
        location.setTime(androidLocation.getTime());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            location.setElapsedRealtimeNanos(androidLocation
                    .getElapsedRealtimeNanos());

        location.setLatitude(androidLocation.getLatitude());
        location.setLongitude(androidLocation.getLongitude());

        if (androidLocation.hasAltitude())
            location.setAltitude(androidLocation.getAltitude());

        if (androidLocation.hasSpeed())
            location.setSpeed(androidLocation.getSpeed());

        if (androidLocation.hasBearing())
            location.setBearing(androidLocation.getBearing());

        if (androidLocation.hasAccuracy())
            location.setAccuracy(androidLocation.getAccuracy());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            location.setIsFromMockProvider(androidLocation.isFromMockProvider());

        return location;
    }

    private static boolean outOfChina(double lat, double lon) {
        if (lon < 72.004 || lon > 137.8347)
            return true;

        if (lat < 0.8293 || lat > 55.8271)
            return true;

        return false;
    }

    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
                + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;

        return ret;
    }

    private static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
                * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0
                * pi)) * 2.0 / 3.0;

        return ret;
    }

    public static Gps transformWgs84ToGcj02(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return new Gps(lat, lon);
        }

        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;

        return new Gps(mgLat, mgLon);
    }
}
