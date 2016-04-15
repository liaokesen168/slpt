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
 * 定义了驾车路径规划的结果集
 */
public class RemoteDriveRouteResult extends RemoteRouteResult implements
        Parcelable {
    private RemoteDriveRouteQuery driveRouteQuery;
    private List<RemoteDrivePath> drivePathList = new ArrayList<RemoteDrivePath>();
    private float taxiCost;

    /**
     * RemoteDriveRouteResult构造函数
     */
    public RemoteDriveRouteResult() {
        super();
    }

    /**
     * RemoteDriveRouteResult构造函数
     * 
     * @param result
     *            用于序列化实现的驾车路径规划的结果集
     */
    public RemoteDriveRouteResult(RemoteRouteResult result) {
        super(result);
    }

    /**
     * 返回搭乘的士的花费
     * 
     * @return 搭乘的士的花费
     */
    public float getTaxiCost() {
        return this.taxiCost;
    }

    /**
     * 设置搭乘的士的花费
     * 
     * @param taxiCost
     *            搭乘的士的花费
     */
    public void setTaxiCost(float taxiCost) {
        this.taxiCost = taxiCost;
    }

    /**
     * 返回驾车路径规划方案
     * 
     * @return 驾车路径规划方案
     */
    public List<RemoteDrivePath> getPaths() {
        return this.drivePathList;
    }

    /**
     * 设置驾车路径规划方案
     * 
     * @param drivePathList
     *            驾车路径规划方案
     */
    public void setPaths(List<RemoteDrivePath> drivePathList) {
        this.drivePathList = drivePathList;
    }

    /**
     * 返回该结果对应的查询参数
     * 
     * @return 该结果对应的查询参数
     */
    public RemoteDriveRouteQuery getDriveQuery() {
        return this.driveRouteQuery;
    }

    /**
     * 设置该结果对应的查询参数
     * 
     * @param driveRouteQuery
     *            该结果对应的查询参数
     */
    public void setDriveQuery(RemoteDriveRouteQuery driveRouteQuery) {
        this.driveRouteQuery = driveRouteQuery;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeFloat(this.taxiCost);

        if (this.drivePathList != null) {
            dest.writeInt(1);
            dest.writeList(this.drivePathList);
        } else {
            dest.writeInt(0);
        }

        if (this.driveRouteQuery != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.driveRouteQuery, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteDriveRouteResult> CREATOR = new Creator<RemoteDriveRouteResult>() {

        @Override
        public RemoteDriveRouteResult createFromParcel(Parcel source) {
            RemoteRouteResult routeResult = RemoteRouteResult.CREATOR
                    .createFromParcel(source);

            RemoteDriveRouteResult driveRouteResult = new RemoteDriveRouteResult(
                    routeResult);

            driveRouteResult.taxiCost = source.readFloat();

            if (source.readInt() != 0) {
                driveRouteResult.drivePathList = source
                        .readArrayList(RemoteDrivePath.class.getClassLoader());
            }

            if (source.readInt() != 0) {
                driveRouteResult.driveRouteQuery = source
                        .readParcelable(RemoteDriveRouteQuery.class
                                .getClassLoader());
            }

            return driveRouteResult;
        }

        @Override
        public RemoteDriveRouteResult[] newArray(int size) {
            return new RemoteDriveRouteResult[size];
        }

    };
}
