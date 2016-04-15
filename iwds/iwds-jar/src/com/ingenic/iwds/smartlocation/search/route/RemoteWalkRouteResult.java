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
 * 定义了步行路径规划的结果集
 */
public class RemoteWalkRouteResult extends RemoteRouteResult implements
        Parcelable {
    private RemoteWalkRouteQuery walkRouteQuery;
    private List<RemoteWalkPath> walkPathList = new ArrayList<RemoteWalkPath>();

    /**
     * RemoteWalkRouteResult构造函数
     */
    public RemoteWalkRouteResult() {
        super();
    }

    /**
     * RemoteWalkRouteResult构造函数
     * 
     * @param result
     *            用于序列化实现的一个步行路径规划的结果集
     */
    public RemoteWalkRouteResult(RemoteRouteResult result) {
        super(result);
    }

    /**
     * 返回步行路径规划方案
     * 
     * @return 步行路径规划方案
     */
    public List<RemoteWalkPath> getPaths() {
        return this.walkPathList;
    }

    /**
     * 设置步行路径规划方案
     * 
     * @param walkPathList
     *            步行路径规划方案
     */
    public void setPaths(List<RemoteWalkPath> walkPathList) {
        this.walkPathList = walkPathList;
    }

    /**
     * 返回该结果对应的查询参数
     * 
     * @return 该结果对应的查询参数
     */
    public RemoteWalkRouteQuery getWalkQuery() {
        return this.walkRouteQuery;
    }

    /**
     * 设置该结果对应的查询参数
     * 
     * @param walkRouteQuery
     *            该结果对应的查询参数
     */
    public void setWalkQuery(RemoteWalkRouteQuery walkRouteQuery) {
        this.walkRouteQuery = walkRouteQuery;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        if (this.walkPathList != null) {
            dest.writeInt(1);
            dest.writeList(this.walkPathList);
        } else {
            dest.writeInt(0);
        }

        if (this.walkRouteQuery != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.walkRouteQuery, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteWalkRouteResult> CREATOR = new Creator<RemoteWalkRouteResult>() {

        @Override
        public RemoteWalkRouteResult createFromParcel(Parcel source) {
            RemoteRouteResult routeResult = RemoteRouteResult.CREATOR
                    .createFromParcel(source);

            RemoteWalkRouteResult walkRouteResult = new RemoteWalkRouteResult(
                    routeResult);

            if (source.readInt() != 0) {
                walkRouteResult.walkPathList = source
                        .readArrayList(RemoteWalkPath.class.getClassLoader());
            }

            if (source.readInt() != 0) {
                walkRouteResult.walkRouteQuery = source
                        .readParcelable(RemoteWalkRouteQuery.class
                                .getClassLoader());
            }

            return walkRouteResult;
        }

        @Override
        public RemoteWalkRouteResult[] newArray(int size) {
            return new RemoteWalkRouteResult[size];
        }

    };
}
