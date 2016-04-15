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

package com.ingenic.iwds.smartlocation.search.route;

import java.util.ArrayList;
import java.util.List;

import com.ingenic.iwds.smartlocation.search.core.RemoteLatLonPoint;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 定义了驾车路径规划的一个路段
 */
public class RemoteDriveStep implements Parcelable {
    private String action;
    private String assistantAction;
    private float distance;
    private float duration;
    private String instruction;
    private String orientation;
    private List<RemoteLatLonPoint> polyline = new ArrayList<RemoteLatLonPoint>();
    private String road;
    private List<RemoteRouteSearchCity> routeSearchCityList = new ArrayList<RemoteRouteSearchCity>();
    private float tollDistance;
    private String tollRoad;
    private float tolls;

    /**
     * RemoteDriveStep构造函数
     */
    public RemoteDriveStep() {

    }

    /**
     * 返回驾车路段的行驶指示
     * 
     * @return 驾车路段的行驶指示
     */
    public String getInstruction() {
        return this.instruction;
    }

    /**
     * 设置驾车路段的行驶指示
     * 
     * @param instruction
     *            驾车路段的行驶指示
     */
    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    /**
     * 返回驾车路段的行驶方向。方向为中文名称，如东、西南等
     * 
     * @return 驾车路段的行驶方向
     */
    public String getOrientation() {
        return this.orientation;
    }

    /**
     * 设置驾车路段的行驶方向。方向为中文名称，如东、西南等
     * 
     * @param orientation
     *            驾车路段的行驶方向
     */
    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    /**
     * 返回驾车路段的道路名称
     * 
     * @return 驾车路段的道路名称
     */
    public String getRoad() {
        return this.road;
    }

    /**
     * 设置驾车路段的道路名称
     * 
     * @param road
     *            驾车路段的道路名称
     */
    public void setRoad(String road) {
        this.road = road;
    }

    /**
     * 返回驾车路段的距离，单位米
     * 
     * @return 驾车路段的距离
     */
    public float getDistance() {
        return this.distance;
    }

    /**
     * 设置驾车路段的距离，单位米
     * 
     * @param distance
     *            驾车路段的距离
     */
    public void setDistance(float distance) {
        this.distance = distance;
    }

    /**
     * 返回驾车路段的收费价格，单位元
     * 
     * @return 驾车路段的收费价格
     */
    public float getTolls() {
        return this.tolls;
    }

    /**
     * 设置驾车路段的收费价格，单位元
     * 
     * @param tolls
     *            驾车路段的收费价格
     */
    public void setTolls(float tolls) {
        this.tolls = tolls;
    }

    /**
     * 返回驾车收费路段的距离，单位米
     * 
     * @return 驾车收费路段的距离
     */
    public float getTollDistance() {
        return this.tollDistance;
    }

    /**
     * 设置驾车收费路段的距离，单位米
     * 
     * @param tollDistance
     *            驾车收费路段的距离
     */
    public void setTollDistance(float tollDistance) {
        this.tollDistance = tollDistance;
    }

    /**
     * 返回驾车路段的主要收费道路
     * 
     * @return 驾车路段的主要收费道路
     */
    public String getTollRoad() {
        return this.tollRoad;
    }

    /**
     * 设置驾车路段的主要收费道路
     * 
     * @param tollRoad
     *            驾车路段的主要收费道路
     */
    public void setTollRoad(String tollRoad) {
        this.tollRoad = tollRoad;
    }

    /**
     * 返回驾车路段的预计时间，单位秒。时间根据当时路况估算
     * 
     * @return 驾车路段的预计时间
     */
    public float getDuration() {
        return this.duration;
    }

    /**
     * 设置驾车路段的预计时间，单位秒。时间根据当时路况估算
     * 
     * @param duration
     *            驾车路段的预计时间
     */
    public void setDuration(float duration) {
        this.duration = duration;
    }

    /**
     * 返回驾车路段的坐标点集合
     * 
     * @return 驾车路段的坐标点集合
     */
    public List<RemoteLatLonPoint> getPolyline() {
        return this.polyline;
    }

    /**
     * 设置驾车路段的坐标点集合
     * 
     * @param polyline
     *            驾车路段的坐标点集合
     */
    public void setPolyline(List<RemoteLatLonPoint> polyline) {
        this.polyline = polyline;
    }

    /**
     * 返回驾车路段的导航主要操作
     * 
     * @return 驾车路段的导航主要操作
     */
    public String getAction() {
        return this.action;
    }

    /**
     * 设置驾车路段的导航主要操作
     * 
     * @param action
     *            驾车路段的导航主要操作
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * 返回驾车路段的导航辅助操作
     * 
     * @return 驾车路段的导航辅助操作
     */
    public String getAssistantAction() {
        return this.assistantAction;
    }

    /**
     * 设置驾车路段的导航辅助操作
     * 
     * @param assitantAction
     *            驾车路段的导航辅助操作
     */
    public void setAssitantAction(String assitantAction) {
        this.assistantAction = assitantAction;
    }

    /**
     * 得到搜索返回的路径规划途径城市和行政区
     * 
     * @return 搜索返回的路径规划途径城市和行政区
     */
    public List<RemoteRouteSearchCity> getRouteSearchCityList() {
        return this.routeSearchCityList;
    }

    /**
     * 设置搜索返回的路径规划途径城市和行政区
     * 
     * @param routeSearchCityList
     *            搜索返回的路径规划途径城市和行政区
     */
    public void setRouteSearchCityList(
            List<RemoteRouteSearchCity> routeSearchCityList) {
        this.routeSearchCityList = routeSearchCityList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.action);
        dest.writeString(this.assistantAction);
        dest.writeFloat(this.distance);
        dest.writeFloat(this.duration);
        dest.writeString(this.instruction);
        dest.writeString(this.orientation);
        dest.writeString(this.road);
        dest.writeFloat(this.tollDistance);
        dest.writeString(this.tollRoad);
        dest.writeFloat(this.tolls);

        if (this.polyline != null) {
            dest.writeInt(1);
            dest.writeList(this.polyline);
        } else {
            dest.writeInt(0);
        }

        if (this.routeSearchCityList != null) {
            dest.writeInt(1);
            dest.writeList(this.routeSearchCityList);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteDriveStep> CREATOR = new Creator<RemoteDriveStep>() {

        @Override
        public RemoteDriveStep createFromParcel(Parcel source) {
            RemoteDriveStep driveStep = new RemoteDriveStep();

            driveStep.action = source.readString();
            driveStep.assistantAction = source.readString();
            driveStep.distance = source.readFloat();
            driveStep.duration = source.readFloat();
            driveStep.instruction = source.readString();
            driveStep.orientation = source.readString();
            driveStep.road = source.readString();
            driveStep.tollDistance = source.readFloat();
            driveStep.tollRoad = source.readString();
            driveStep.tolls = source.readFloat();

            if (source.readInt() != 0) {
                driveStep.polyline = source
                        .readArrayList(RemoteLatLonPoint.class.getClassLoader());
            }

            if (source.readInt() != 0) {
                driveStep.routeSearchCityList = source
                        .readArrayList(RemoteRouteSearchCity.class
                                .getClassLoader());
            }

            return driveStep;
        }

        @Override
        public RemoteDriveStep[] newArray(int size) {
            return new RemoteDriveStep[size];
        }

    };
}
