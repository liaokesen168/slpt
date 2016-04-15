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
 * 某一天的天气预报类
 */
public class RemoteDayWeatherForecast implements Parcelable {
    private String province;
    private String city;
    private String cityCode;
    private String date;
    private String week;
    private String dayWeather;
    private String nightWeather;
    private String dayTemp;
    private String nightTemp;
    private String dayWindDir;
    private String nightWindDir;
    private String dayWindPower;
    private String nightWindPower;

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
     * 返回预报天气的年月日
     * 
     * @return 预报天气的年月日
     */
    public String getDate() {
        return this.date;
    }

    /**
     * 设置预报天气的年月日
     * 
     * @param date
     *            预报天气的年月日
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * 返回预报天气的星期, 返回阿拉伯数字1-7
     * 
     * @return 预报天气的星期
     */
    public String getWeek() {
        return this.week;
    }

    /**
     * 设置预报天气的星期，阿拉伯数字1-7
     * 
     * @param week
     *            预报天气的星期
     */
    public void setWeek(String week) {
        this.week = week;
    }

    /**
     * 返回白天天气现象，如“晴”、“多云”，参考
     * {@link com.ingenic.iwds.smartlocation.RemoteWeatherLive
     * RemoteWeatherLive}中天气现象表
     * 
     * @return 白天天气现象
     */
    public String getDayWeather() {
        return this.dayWeather;
    }

    /**
     * 设置白天天气现象，如“晴”、“多云”
     * 
     * @param dayWeather
     *            白天天气现象
     */
    public void setDayWeather(String dayWeather) {
        this.dayWeather = dayWeather;
    }

    /**
     * 返回夜间天气现象，如“晴”、“多云”，参考
     * {@link com.ingenic.iwds.smartlocation.RemoteWeatherLive
     * RemoteWeatherLive}中天气现象表
     * 
     * @return 夜间天气现象
     */
    public String getNightWeather() {
        return this.nightWeather;
    }

    /**
     * 设置夜间天气现象，如“晴”、“多云”
     * 
     * @param nightWeather
     *            夜间天气现象
     */
    public void setNightWeather(String nightWeather) {
        this.nightWeather = nightWeather;
    }

    /**
     * 返回白天天气温度, 单位: 摄氏度
     * 
     * @return 白天天气温度
     */
    public String getDayTemp() {
        return this.dayTemp;
    }

    /**
     * 设置白天天气温度, 单位: 摄氏度
     * 
     * @param dayTemp
     *            白天天气温度
     */
    public void setDayTemp(String dayTemp) {
        this.dayTemp = dayTemp;
    }

    /**
     * 返回夜间天气温度, 单位: 摄氏度
     * 
     * @return 夜间天气温度
     */
    public String getNightTemp() {
        return this.nightTemp;
    }

    /**
     * 设置夜间天气温度, 单位: 摄氏度
     * 
     * @param nightTemp
     *            夜间天气温度
     */
    public void setNightTemp(String nightTemp) {
        this.nightTemp = nightTemp;
    }

    /**
     * 返回白天风向，参考 {@link com.ingenic.iwds.smartlocation.RemoteWeatherLive
     * RemoteWeatherLive}中风向表
     * 
     * @return 白天风向
     */
    public String getDayWindDir() {
        return this.dayWindDir;
    }

    /**
     * 设置白天风向
     * 
     * @param dayWindDir
     *            白天风向
     */
    public void setDayWindDir(String dayWindDir) {
        this.dayWindDir = dayWindDir;
    }

    /**
     * 返回夜间风向，参考 {@link com.ingenic.iwds.smartlocation.RemoteWeatherLive
     * RemoteWeatherLive}中风向表
     * 
     * @return 夜间风向
     */
    public String getNightWindDir() {
        return this.nightWindDir;
    }

    /**
     * 设置夜间风向
     * 
     * @param nightWindDir
     *            夜间风向
     */
    public void setNightWindDir(String nightWindDir) {
        this.nightWindDir = nightWindDir;
    }

    /**
     * 返回白天风力，单位：级，参考 {@link com.ingenic.iwds.smartlocation.RemoteWeatherLive
     * RemoteWeatherLive}中风力表
     * 
     * @return 白天风力
     */
    public String getDayWindPower() {
        return this.dayWindPower;
    }

    /**
     * 设置白天风力，单位：级
     * 
     * @param dayWindPower
     *            白天风力
     */
    public void setDayWindPower(String dayWindPower) {
        this.dayWindPower = dayWindPower;
    }

    /**
     * 返回夜间风力，单位：级，参考 {@link com.ingenic.iwds.smartlocation.RemoteWeatherLive
     * RemoteWeatherLive}中风力表
     * 
     * @return 夜间风力
     */
    public String getNightWindPower() {
        return this.nightWindPower;
    }

    /**
     * 设置夜间风力，单位：级
     * 
     * @param nightWindPower
     *            夜间风力
     */
    public void setNightWindPower(String nightWindPower) {
        this.nightWindPower = nightWindPower;
    }

    @Override
    public String toString() {
        return "RemoteDayWeather[province=" + province + " ,city=" + city
                + " ,cityCode=" + cityCode + " ,date=" + date + " ,week="
                + week + " ,dayWeather=" + dayWeather + " ,nightWeather="
                + nightWeather + " ,dayTemp=" + dayTemp + " ,nightTemp="
                + " ,dayWindDir=" + dayWindDir + " ,nightWindDir="
                + nightWindDir + " ,dayWindPower=" + dayWindPower
                + " ,nightWindPower=" + nightWindPower + "]";
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
        dest.writeString(date);
        dest.writeString(week);
        dest.writeString(dayWeather);
        dest.writeString(nightWeather);
        dest.writeString(dayTemp);
        dest.writeString(nightTemp);
        dest.writeString(dayWindDir);
        dest.writeString(nightWindDir);
        dest.writeString(dayWindPower);
        dest.writeString(nightWindPower);
    }

    public static final Creator<RemoteDayWeatherForecast> CREATOR = new Creator<RemoteDayWeatherForecast>() {

        @Override
        public RemoteDayWeatherForecast createFromParcel(Parcel source) {
            RemoteDayWeatherForecast dayWeatherForecast = new RemoteDayWeatherForecast();

            dayWeatherForecast.province = source.readString();
            dayWeatherForecast.city = source.readString();
            dayWeatherForecast.cityCode = source.readString();
            dayWeatherForecast.date = source.readString();
            dayWeatherForecast.week = source.readString();
            dayWeatherForecast.dayWeather = source.readString();
            dayWeatherForecast.nightWeather = source.readString();
            dayWeatherForecast.dayTemp = source.readString();
            dayWeatherForecast.nightTemp = source.readString();
            dayWeatherForecast.dayWindDir = source.readString();
            dayWeatherForecast.nightWindDir = source.readString();
            dayWeatherForecast.dayWindPower = source.readString();
            dayWeatherForecast.nightWindPower = source.readString();

            return dayWeatherForecast;
        }

        @Override
        public RemoteDayWeatherForecast[] newArray(int size) {
            return new RemoteDayWeatherForecast[size];
        }

    };

}
