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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 定义了公交路径规划的结果集
 */
public class RemoteBusRouteResult extends RemoteRouteResult implements
        Parcelable {
    private RemoteBusRouteQuery busRouteQuery;
    private List<RemoteBusPath> busPathList = new ArrayList<RemoteBusPath>();;
    private float taxiCost;

    /**
     * RemoteBusRouteResult构造函数
     */
    public RemoteBusRouteResult() {
        super();
    }

    /**
     * RemoteBusRouteResult
     * 
     * @param result
     *            用于序列化实现的公交路径规划的结果集
     */
    public RemoteBusRouteResult(RemoteRouteResult result) {
        super(result);
    }

    /**
     * 返回从起点到终点打车的费用，单位元。 费用是以驾车路线最短距离估计出租车的费用
     * 
     * @return 打车所需费用
     */
    public float getTaxiCost() {
        return this.taxiCost;
    }

    /**
     * 设置打车所需费用
     * 
     * @param taxiCost
     *            打车所需费用
     */
    public void setTaxiCost(float taxiCost) {
        this.taxiCost = taxiCost;
    }

    /**
     * 返回公交路径规划方案
     * 
     * @return 公交路径规划方案
     */
    public List<RemoteBusPath> getPaths() {
        return this.busPathList;
    }

    /**
     * 设置公交路径规划方案
     * 
     * @param busPathList
     *            公交路径规划方案
     */
    public void setPaths(List<RemoteBusPath> busPathList) {
        this.busPathList = busPathList;
    }

    /**
     * 返回该结果对应的查询参数
     * 
     * @return 该结果对应的查询参数
     */
    public RemoteBusRouteQuery getBusQuery() {
        return this.busRouteQuery;
    }

    /**
     * 设置查询参数
     * 
     * @param busRouteQuery
     *            查询参数
     */
    public void setBusRouteQuery(RemoteBusRouteQuery busRouteQuery) {
        this.busRouteQuery = busRouteQuery;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(this.taxiCost);

        if (this.busRouteQuery != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.busRouteQuery, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.busPathList != null) {
            dest.writeInt(1);
            dest.writeList(this.busPathList);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteBusRouteResult> CREATOR = new Creator<RemoteBusRouteResult>() {

        @Override
        public RemoteBusRouteResult createFromParcel(Parcel source) {
            RemoteRouteResult routeResult = RemoteRouteResult.CREATOR
                    .createFromParcel(source);

            RemoteBusRouteResult busRouteResult = new RemoteBusRouteResult(
                    routeResult);

            busRouteResult.taxiCost = source.readFloat();

            if (source.readInt() != 0) {
                busRouteResult.busRouteQuery = source
                        .readParcelable(RemoteBusRouteQuery.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                busRouteResult.busPathList = source
                        .readArrayList(RemoteBusPath.class.getClassLoader());
            }

            return busRouteResult;
        }

        @Override
        public RemoteBusRouteResult[] newArray(int size) {
            return new RemoteBusRouteResult[size];
        }

    };
}
