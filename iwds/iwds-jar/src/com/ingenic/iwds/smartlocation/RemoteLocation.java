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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 远程地理位置信息类
 *
 */
public class RemoteLocation implements Parcelable {
    private String provider;
    private long time = 0;
    private long elapsedRealtimeNanos = 0;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private boolean hasAltitude = false;
    private double altitude = 0.0f;
    private boolean hasSpeed = false;
    private float speed = 0.0f;
    private boolean hasBearing = false;
    private float bearing = 0.0f;
    private boolean hasAccuracy = false;
    private float accuracy = 0.0f;
    private boolean isFromMockProvider = false;

    private String poiName;
    private String country;
    private String road;
    private String poiId;
    private String floor;
    private String province;
    private String city;
    private String district;
    private String cityCode;
    private String adCode;
    private String address;
    private String street;
    private int errorCode;

    /**
     * 构造器
     */
    public RemoteLocation() {

    }

    /**
     * 构造方法
     *
     * @param provider
     *            定位提供者
     */
    public RemoteLocation(String provider) {
        this.provider = provider;
    }

    /**
     * 构造方法
     * 
     * @param location
     *            地理位置对象
     */
    public RemoteLocation(RemoteLocation location) {
        set(location);
    }

    /**
     * 使用给定的位置对象设置当前对象
     * 
     * @param location
     *            地理位置对象
     */
    public void set(RemoteLocation location) {
        this.provider = location.provider;
        this.time = location.time;
        this.elapsedRealtimeNanos = location.elapsedRealtimeNanos;
        this.latitude = location.latitude;
        this.longitude = location.longitude;
        this.hasAltitude = location.hasAltitude;
        this.altitude = location.altitude;
        this.hasSpeed = location.hasSpeed;
        this.speed = location.speed;
        this.hasBearing = location.hasBearing;
        this.bearing = location.bearing;
        this.hasAccuracy = location.hasAccuracy;
        this.accuracy = location.accuracy;
        this.isFromMockProvider = location.isFromMockProvider;
        this.poiName = location.poiName;
        this.country = location.country;
        this.road = location.road;
        this.poiId = location.poiId;
        this.floor = location.floor;
        this.province = location.province;
        this.city = location.city;
        this.district = location.district;
        this.cityCode = location.cityCode;
        this.adCode = location.adCode;
        this.address = location.address;
        this.street = location.street;
        this.errorCode = location.errorCode;
    }

    /**
     * 清除地理位置对象中的内容
     */
    public void reset() {
        this.provider = null;
        this.time = 0;
        this.elapsedRealtimeNanos = 0;
        this.latitude = 0;
        this.longitude = 0;
        this.hasAltitude = false;
        this.altitude = 0;
        this.hasSpeed = false;
        this.speed = 0;
        this.hasBearing = false;
        this.bearing = 0;
        this.hasAccuracy = false;
        this.accuracy = 0;
        this.isFromMockProvider = false;
        this.poiName = null;
        this.country = null;
        this.road = null;
        this.poiId = null;
        this.floor = null;
        this.province = null;
        this.city = null;
        this.district = null;
        this.cityCode = null;
        this.adCode = null;
        this.address = null;
        this.street = null;
        this.errorCode = 0;
    }

    /**
     * 返回定位信息提供者
     * 
     * @return 定位信息提供者
     */
    public String getProvider() {
        return this.provider;
    }

    /**
     * 设置定位信息提供者
     * 
     * @param provider
     *            　定位信息提供者
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * 返回定位时间 ，毫秒时间（距离1970年 1月 1日 00:00:00 GMT的时间）
     * 
     * @return 定位时间
     */
    public long getTime() {
        return this.time;
    }

    /**
     * 设置定位时间
     * 
     * @param time
     *            定位时间
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * 返回定位位置的纬度坐标
     * 
     * @return 定位位置的纬度坐标
     */
    public double getLatitude() {
        return this.latitude;
    }

    /**
     * 返回定位位置的经度坐标
     * 
     * @return 定位位置的经度坐标
     */
    public double getLongitude() {
        return this.longitude;
    }

    /**
     * 设置定位位置纬度坐标
     * 
     * @param latitude
     *            定位位置的纬度坐标
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * 设置定位位置经度坐标
     * 
     * @param longitude
     *            定位位置经度坐标
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * 返回海拔高度状态
     * 
     * @return 海拔高度状态
     */
    public boolean hasAltitude() {
        return this.hasAltitude;
    }

    /**
     * 返回海拔高度，如果返回0.0，说明没有返回海拔高度，只有在GPS定位时才返回值。
     * 
     * @return 返回海拔高度（单位米）
     */
    public double getAltitude() {
        return this.altitude;
    }

    /**
     * 设置定位位置海拔
     * 
     * @param altitude
     *            海拔高度（单位米）
     */
    public void setAltitude(double altitude) {
        this.altitude = altitude;
        this.hasAltitude = true;
    }

    /**
     * 移除定位位置海拔高度信息
     */
    public void removeAltitude() {
        this.altitude = 0.0f;
        this.hasAltitude = false;
    }

    /**
     * 返回定位速度状态
     * 
     * @return 定位速度状态
     */
    public boolean hasSpeed() {
        return this.hasSpeed;
    }

    /**
     * 返回定位速度，单位：米/秒，如果此位置不具有速度，则返回0.0
     * 
     * @return 定位速度
     */
    public float getSpeed() {
        return this.speed;
    }

    /**
     * 设置定位速度，单位：米/秒
     * 
     * @param speed
     *            定位速度
     */
    public void setSpeed(float speed) {
        this.speed = speed;
        this.hasSpeed = true;
    }

    /**
     * 移除定位位置中速度信息
     */
    public void removeSpeed() {
        this.speed = 0.0f;
        this.hasSpeed = false;
    }

    /**
     * 返回定位方向（方位）状态
     * 
     * @return 定位方向（方位）状态
     */
    public boolean hasBearing() {
        return this.hasBearing;
    }

    /**
     * 返回定位方位（方向），以度为单位，与正北方向顺时针的角度
     * 
     * @return 定位方位（方向）
     */
    public float getBearing() {
        return this.bearing;
    }

    /**
     * 设置定位方位（方向）
     * 
     * @param bearing
     *            定位方位（方向）
     */
    public void setBearing(float bearing) {
        while (bearing < 0.0f) {
            this.bearing += 360.0f;
        }
        while (bearing >= 360.0f) {
            this.bearing -= 360.0f;
        }
        this.bearing = bearing;
        this.hasBearing = true;
    }

    /**
     * 移除定位位置中方向信息
     */
    public void removeBearing() {
        this.bearing = 0.0f;
        this.hasBearing = false;
    }

    /**
     * 返回定位精度状态
     * 
     * @return 定位精度状态
     */
    public boolean hasAccuracy() {
        return this.hasAccuracy;
    }

    /**
     * 返回定位精度半径
     * 
     * @return 定位精度半径
     */
    public float getAccuracy() {
        return this.accuracy;
    }

    /**
     * 设置定位精度半径
     * 
     * @param accuracy
     *            定位精度半径
     */
    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
        this.hasAccuracy = true;
    }

    /**
     * 移除定位位置中精度信息
     */
    public void removeAccuracy() {
        this.accuracy = 0.0f;
        this.hasAccuracy = false;
    }

    /**
     * 返回定位信息中POI名称，仅限中国大陆、香港、澳门
     * 
     * @return 定位信息中POI名称
     */
    public String getPoiName() {
        return this.poiName;
    }

    /**
     * 返回系统启动至此次成功定位的时间，单位：纳秒，只有在GPS定位时才有返回值
     * 
     * @return 系统启动至此次成功定位的时间
     */
    public long getElapsedRealtimeNanos() {
        return this.elapsedRealtimeNanos;
    }

    /**
     * 设置系统启动至此次成功定位的时间，单位：纳秒
     * 
     * @param time
     *            系统启动至此次成功定位的时间
     */
    public void setElapsedRealtimeNanos(long time) {
        this.elapsedRealtimeNanos = time;
    }

    /**
     * 返回定位信息是否来自模拟provider
     * 
     * @return 定位信息是否来自模拟provider
     */
    public boolean isFromMockProvider() {
        return this.isFromMockProvider;
    }

    /**
     * 设置定位信息是否来自模拟provider
     * 
     * @param isFromMockProvider
     *            定位信息是否来自模拟provider
     */
    public void setIsFromMockProvider(boolean isFromMockProvider) {
        this.isFromMockProvider = isFromMockProvider;
    }

    /**
     * 设置定位信息中POI名称
     * 
     * @param poiName
     *            POI名称
     */
    public void setPoiName(String poiName) {
        this.poiName = poiName;
    }

    /**
     * 返回定位信息中所属国家名称，如“中国”
     * 
     * @return 定位信息中所属国家名称
     */
    public String getCountry() {
        return this.country;
    }

    /**
     * 设置定位信息中所属国家名称
     * 
     * @param country
     *            国家名称
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * 返回定位信息中道路名称,如“阜荣街”
     * 
     * @return 定位信息中道路名称
     */
    public String getRoad() {
        return this.road;
    }

    /**
     * 设置定位信息中道路名称
     * 
     * @param road
     *            道路名称
     */
    public void setRoad(String road) {
        this.road = road;
    }

    /**
     * 返回定位到的室内地图POI的id，如果不在室内或者无此数据，默认为null
     * 
     * @return 室内地图POI的id
     */
    public String getPoiId() {
        return this.poiId;
    }

    /**
     * 设置定位到的室内地图POI的id，如果不在室内或者无此数据，默认为null
     * 
     * @param poiId
     *            室内地图POI的id
     */
    public void setPoiId(String poiId) {
        this.poiId = poiId;
    }

    /**
     * 返回定位到的室内地图的楼层，如果不在室内或者无数据，则返回默认值null
     * 
     * @return 定位到的室内地图的楼层，如果不在室内或者无数据，则返回默认值null
     */
    public String getFloor() {
        return this.floor;
    }

    /**
     * 设置定位到的室内地图的楼层，如果不在室内或者无数据，则默认为null
     * 
     * @param floor
     *            定位到的室内地图的楼层
     */
    public void setFloor(String floor) {
        this.floor = floor;
    }

    /**
     * 返回定位信息中所属省名称，如“河北省”，只有在网络定位时才返回值
     * 
     * @return 定位信息所属省名称
     */
    public String getProvince() {
        return this.province;
    }

    /**
     * 返回设置定位信息中所属省名称
     * 
     * @param province
     *            - 定位信息中所属省名称
     */
    public void setProvince(String province) {
        this.province = province;
    }

    /**
     * 返回定位信息中所属城市名称，如“北京市”，只有在网络定位时才返回值
     * 
     * @return 定位信息中所属城市名称
     */
    public String getCity() {
        return this.city;
    }

    /**
     * 设置定位信息中所属城市名称
     * 
     * @param city
     *            定位信息中所属城市名称
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * 返回定位信息中所属区（县）名称，如“朝阳区”，只有在网络定位时才返回值(仅限中国大陆、香港、澳门)
     * 
     * @return 定位信息中所属区（县）名称
     */
    public String getDistrict() {
        return this.district;
    }

    /**
     * 设置定位信息中所属区（县）名称
     * 
     * @param district
     *            定位信息中所属区（县）名称
     */
    public void setDistrict(String district) {
        this.district = district;
    }

    /**
     * 返回定位信息中的城市编码，如北京市为“010”，只有在网络定位时才返回值(仅限中国大陆、香港、澳门)
     * 
     * @return 定位信息中的城市编码
     */
    public String getCityCode() {
        return this.cityCode;
    }

    /**
     * 设置定位信息中的城市编码
     * 
     * @param cityCode
     *            定位信息中的城市编码
     */
    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    /**
     * 返回定位信息中的区域编码，只有在网络定位时才返回值(仅支持中国大陆、香港、澳门)
     * 
     * @return 定位信息中的区域编码
     */
    public String getAdCode() {
        return this.adCode;
    }

    /**
     * 设置定位信息中的区域编码
     * 
     * @param adCode
     *            定位信息中的区域编码
     */
    public void setAdCode(String adCode) {
        this.adCode = adCode;
    }

    /**
     * 返回地址的详细描述，包括省、市、区和街道
     * 
     * @return 地址的详细描述，包括省、市、区和街道
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * 设置地址的详细描述，包括省、市、区和街道
     * 
     * @param address
     *            地址的详细描述，包括省、市、区和街道
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * 返回街道和门牌号，只有在网络定位才返回值(仅限中国大陆、香港、澳门)
     * 
     * @return 街道和门牌号
     */
    public String getStreet() {
        return this.street;
    }

    /**
     * 设置街道和门牌号
     * 
     * @param street
     *            街道和门牌号
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * 返回错误编码{@link com.ingenic.iwds.smartlocation.RemoteLocationErrorCode
     * RemoteLocationErrorCode}
     * 
     * @return 错误编码
     */
    public int getErrorCode() {
        return this.errorCode;
    }

    /**
     * 设置错误编码{@link com.ingenic.iwds.smartlocation.RemoteLocationErrorCode
     * RemoteLocationErrorCode}
     * 
     * @param errorCode
     *            错误编码
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "RemoteLocation [errorCode=" + errorCode + ", provider="
                + this.provider + ", time=" + this.time + ", elapsed time="
                + this.elapsedRealtimeNanos + ", latitude=" + this.latitude
                + ", longtitude=" + this.longitude + ", hasAltitude="
                + this.hasAltitude + ", altitude=" + this.altitude
                + ", hasSpeed=" + this.hasSpeed + ", speed=" + speed
                + ", hasBearing=" + this.hasBearing + ", bearing="
                + this.bearing + ", hasAccuracy=" + this.hasAccuracy
                + ", accuracy=" + this.accuracy + ", isFromMockProvider="
                + this.isFromMockProvider + ", address=" + address
                + ", country=" + country + ", province=" + province + ", city="
                + city + ", cityCode=" + cityCode + ", district=" + district
                + ", street=" + street + ", road=" + road + ", floor=" + floor
                + ", adCode=" + adCode + ", poiId=" + poiId + ", poiIName="
                + poiName + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.provider);
        dest.writeLong(this.time);
        dest.writeLong(this.elapsedRealtimeNanos);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeByte((byte) (this.hasAltitude ? 1 : 0));
        dest.writeDouble(this.altitude);
        dest.writeByte((byte) (this.hasSpeed ? 1 : 0));
        dest.writeFloat(this.speed);
        dest.writeByte((byte) (this.hasBearing ? 1 : 0));
        dest.writeFloat(this.bearing);
        dest.writeByte((byte) (this.hasAccuracy ? 1 : 0));
        dest.writeFloat(this.accuracy);
        dest.writeInt(this.isFromMockProvider ? 1 : 0);
        dest.writeString(this.poiName);
        dest.writeString(this.country);
        dest.writeString(this.road);
        dest.writeString(this.poiId);
        dest.writeString(this.floor);
        dest.writeString(this.province);
        dest.writeString(this.city);
        dest.writeString(this.district);
        dest.writeString(this.cityCode);
        dest.writeString(this.adCode);
        dest.writeString(this.address);
        dest.writeString(this.street);
        dest.writeInt(this.errorCode);
    }

    public static final Creator<RemoteLocation> CREATOR = new Creator<RemoteLocation>() {

        @Override
        public RemoteLocation createFromParcel(Parcel source) {
            RemoteLocation location = new RemoteLocation();

            location.provider = source.readString();
            location.time = source.readLong();
            location.elapsedRealtimeNanos = source.readLong();
            location.latitude = source.readDouble();
            location.longitude = source.readDouble();

            if (source.readByte() == 1)
                location.hasAltitude = true;
            else
                location.hasAltitude = false;

            location.altitude = source.readDouble();

            if (source.readByte() == 1) {
                location.hasSpeed = true;
            } else {
                location.hasSpeed = false;
            }

            location.speed = source.readFloat();

            if (source.readByte() == 1) {
                location.hasBearing = true;
            } else {
                location.hasBearing = false;
            }

            location.bearing = source.readFloat();

            if (source.readByte() == 1) {
                location.hasAccuracy = true;
            } else {
                location.hasAccuracy = false;
            }

            location.accuracy = source.readFloat();
            location.isFromMockProvider = source.readInt() != 0;
            location.poiName = source.readString();
            location.country = source.readString();
            location.road = source.readString();
            location.poiId = source.readString();
            location.floor = source.readString();
            location.province = source.readString();
            location.city = source.readString();
            location.district = source.readString();
            location.cityCode = source.readString();
            location.adCode = source.readString();
            location.address = source.readString();
            location.street = source.readString();
            location.errorCode = source.readInt();

            return location;
        }

        @Override
        public RemoteLocation[] newArray(int size) {
            return new RemoteLocation[size];
        }
    };
}
