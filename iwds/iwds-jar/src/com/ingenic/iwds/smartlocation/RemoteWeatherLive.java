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
 * <p>实况天气信息类。仅支持中国大陆、香港、澳门的数据返回</p>
 * <br>
 * 天气现象表：
 * <li> 晴
 * <li> 多云
 * <li> 阴
 * <li> 阵雨
 * <li> 雷阵雨
 * <li> 雷阵雨并伴有冰雹
 * <li> 雨夹雪
 * <li> 小雨
 * <li> 中雨
 * <li> 大雨
 * <li> 暴雨
 * <li> 大暴雨
 * <li> 特大暴雨
 * <li> 阵雪
 * <li> 小雪
 * <li> 中雪
 * <li> 大雪
 * <li> 暴雪
 * <li> 雾
 * <li> 冻雨
 * <li> 沙尘暴
 * <li> 小雨-中雨
 * <li> 中雨-大雨
 * <li> 大雨-暴雨
 * <li> 暴雨-大暴雨
 * <li> 沙尘暴
 * <li> 小雨-中雨
 * <li> 中雨-大雨
 * <li> 大雨-暴雨
 * <li> 暴雨-大暴雨
 * <li> 大暴雨-特大暴雨
 * <li> 小雪-中雪
 * <li> 中雪-大雪
 * <li> 大雪-暴雪
 * <li> 浮尘
 * <li> 扬沙
 * <li> 强沙尘暴
 * <li> 中雨-大雨
 * <li> 飑
 * <li> 龙卷风
 * <li> 弱高吹雪
 * <li> 轻霾
 * <li> 霾
 * <br>
 * <br>
 * 风力表:
 * <li> ≤3
 * <li> 4
 * <li> 5
 * <li> 6
 * <li> 7
 * <li> 8
 * <li> 9
 * <li> 10
 * <li> 11
 * <li> 12
 * <br>
 * <br>
 * 风向表:
 * <li> 无风向
 * <li> 东北
 * <li> 东
 * <li> 东南
 * <li> 南
 * <li> 西南
 * <li> 西
 * <li> 西北
 * <li> 北
 * <li> 旋转不定
 */
public class RemoteWeatherLive implements Parcelable {
    private String province;
    private String city;
    private String cityCode;
    private String weather;
    private String temperature;
    private String windDir;
    private String windPower;
    private String humidity;
    private String reportTime;
    private int errorCode;

    /**
     * 返回城市名称
     * 
     * @return 城市名称
     */
    public String getCity() {
        return this.city;
    }

    /**
     * 设置城市名称
     * 
     * @param city
     *            城市名称
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * 返回省份名称
     * 
     * @return 省份名称
     */
    public String getProvince() {
        return this.province;
    }

    /**
     * 设置省份名称
     * 
     * @param province
     *            省份名称
     */
    public void setProvince(String province) {
        this.province = province;
    }

    /**
     * 返回城市编码
     * 
     * @return 城市编码
     */
    public String getCityCode() {
        return this.cityCode;
    }

    /**
     * 设置城市编码
     * 
     * @param cityCode
     *            城市编码
     */
    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    /**
     * 返回天气现象描述，如“晴”、“多云”等
     * 
     * @return 天气现象描述
     */
    public String getWeather() {
        return this.weather;
    }

    /**
     * 设置天气现象描述
     * 
     * @param weather
     *            天气现象描述
     */
    public void setWeather(String weather) {
        this.weather = weather;
    }

    /**
     * 返回实时气温，单位：摄氏度
     * 
     * @return 实时气温，单位：摄氏度
     */
    public String getTemperature() {
        return this.temperature;
    }

    /**
     * 设置实时气温，单位：摄氏度
     * 
     * @param temperature
     *            实时气温，单位：摄氏度
     */
    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    /**
     * 返回风向
     * 
     * @return 风向
     */
    public String getWindDir() {
        return this.windDir;
    }

    /**
     * 设置风向
     * 
     * @param windDir
     *            风向
     */
    public void setWindDir(String windDir) {
        this.windDir = windDir;
    }

    /**
     * 返回风力，单位：级
     * 
     * @return 风力
     */
    public String getWindPower() {
        return this.windPower;
    }

    /**
     * 设置风力，单位：级
     * 
     * @param windPower
     *            风力
     */
    public void setWindPower(String windPower) {
        this.windPower = windPower;
    }

    /**
     * 返回空气湿度的百分比
     * 
     * @return 空气湿度的百分比
     */
    public String getHumidity() {
        return this.humidity;
    }

    /**
     * 设置空气湿度的百分比
     * 
     * @param humidity
     *            空气湿度的百分比
     */
    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    /**
     * 返回实时数据发布时间
     * 
     * @return 实时数据发布时间
     */
    public String getReportTime() {
        return this.reportTime;
    }

    /**
     * 设置实时数据发布时间
     * 
     * @param reportTime
     *            实时数据发布时间
     */
    public void setReportTime(String reportTime) {
        this.reportTime = reportTime;
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
        return "RemoteWeatherLive [errorCode=" + errorCode + " ,province="
                + province + " ,city=" + city + " ,cityCode=" + cityCode
                + " ,weather=" + weather + " ,temperature=" + temperature
                + " ,windDir=" + windDir + " ,windPower" + windPower
                + " ,humidity=" + humidity + " ,reportTime=" + reportTime + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(province);
        dest.writeString(city);
        dest.writeString(cityCode);
        dest.writeString(weather);
        dest.writeString(temperature);
        dest.writeString(windDir);
        dest.writeString(windPower);
        dest.writeString(humidity);
        dest.writeString(reportTime);
        dest.writeInt(errorCode);
    }

    public static final Creator<RemoteWeatherLive> CREATOR = new Creator<RemoteWeatherLive>() {

        @Override
        public RemoteWeatherLive createFromParcel(Parcel source) {
            RemoteWeatherLive weatherLive = new RemoteWeatherLive();

            weatherLive.province = source.readString();
            weatherLive.city = source.readString();
            weatherLive.cityCode = source.readString();
            weatherLive.weather = source.readString();
            weatherLive.temperature = source.readString();
            weatherLive.windDir = source.readString();
            weatherLive.windPower = source.readString();
            weatherLive.humidity = source.readString();
            weatherLive.reportTime = source.readString();
            weatherLive.errorCode = source.readInt();

            return weatherLive;
        }

        @Override
        public RemoteWeatherLive[] newArray(int size) {
            return new RemoteWeatherLive[size];
        }

    };

}
