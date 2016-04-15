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
 * 定义了步行路径规划的一个路段
 */
public class RemoteWalkStep implements Parcelable {
    private String action;
    private String assistantAction;
    private float distance;
    private float duration;
    private String instruction;
    private String orientation;
    private List<RemoteLatLonPoint> polyline = new ArrayList<RemoteLatLonPoint>();
    private String road;

    /**
     * RemoteWalkStep构造函数
     */
    public RemoteWalkStep() {

    }

    /**
     * 返回步行路段的行进指示
     * 
     * @return 步行路段的行进指示
     */
    public String getInstruction() {
        return this.instruction;
    }

    /**
     * 设置步行路段的行进指示
     * 
     * @param instruction
     *            步行路段的行进指示
     */
    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    /**
     * 返回步行路段的行进方向，方向为中文名称，如东、西南等
     * 
     * @return 步行路段的行进方向
     */
    public String getOrientation() {
        return this.orientation;
    }

    /**
     * 设置步行路段的行进方向，方向为中文名称，如东、西南等
     * 
     * @param orientation
     *            步行路段的行进方向
     */
    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    /**
     * 返回步行路段的道路名称
     * 
     * @return 步行路段的道路名称
     */
    public String getRoad() {
        return this.road;
    }

    /**
     * 设置步行路段的道路名称
     * 
     * @param road
     *            步行路段的道路名称
     */
    public void setRoad(String road) {
        this.road = road;
    }

    /**
     * 返回步行路段的距离，单位米
     * 
     * @return 步行路段的距离
     */
    public float getDistance() {
        return this.distance;
    }

    /**
     * 设置步行路段的距离，单位米
     * 
     * @param distance
     *            步行路段的距离
     */
    public void setDistance(float distance) {
        this.distance = distance;
    }

    /**
     * 返回步行路段的预计时间，单位为秒。时间根据当时路况估算
     * 
     * @return 步行路段的预计时间
     */
    public float getDuration() {
        return this.duration;
    }

    /**
     * 设置步行路段的预计时间，单位为秒
     * 
     * @param duration
     *            步行路段的预计时间
     */
    public void setDuration(float duration) {
        this.duration = duration;
    }

    /**
     * 返回步行路段的坐标点集合
     * 
     * @return 步行路段的坐标点集合
     */
    public List<RemoteLatLonPoint> getPolyline() {
        return this.polyline;
    }

    /**
     * 设置步行路段的坐标点集合
     * 
     * @param polyline
     *            步行路段的坐标点集合
     */
    public void setPolyline(List<RemoteLatLonPoint> polyline) {
        this.polyline = polyline;
    }

    /**
     * 返回步行路段的导航主要操作
     * 
     * @return 步行路段的导航主要操作
     */
    public String getAction() {
        return this.action;
    }

    /**
     * 设置步行路段的导航主要操作
     * 
     * @param action
     *            步行路段的导航主要操作
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * 返回步行路段的导航辅助操作
     * 
     * @return 步行路段的导航辅助操作
     */
    public String getAssistantAction() {
        return this.assistantAction;
    }

    /**
     * 设置步行路段的导航辅助操作
     * 
     * @param assistantAction
     *            步行路段的导航辅助操作
     */
    public void setAssistantAction(String assistantAction) {
        this.assistantAction = assistantAction;
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
        if (this.polyline != null) {
            dest.writeInt(1);
            dest.writeList(this.polyline);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteWalkStep> CREATOR = new Creator<RemoteWalkStep>() {

        @Override
        public RemoteWalkStep createFromParcel(Parcel source) {
            RemoteWalkStep walkStep = new RemoteWalkStep();

            walkStep.action = source.readString();
            walkStep.assistantAction = source.readString();
            walkStep.distance = source.readFloat();
            walkStep.duration = source.readFloat();
            walkStep.instruction = source.readString();
            walkStep.orientation = source.readString();
            walkStep.road = source.readString();

            if (source.readInt() != 0) {
                walkStep.polyline = source
                        .readArrayList(RemoteLatLonPoint.class.getClassLoader());
            }

            return walkStep;
        }

        @Override
        public RemoteWalkStep[] newArray(int size) {
            return new RemoteWalkStep[size];
        }

    };
}
