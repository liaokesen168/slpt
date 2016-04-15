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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 此类定义了步行路径的起终点和计算路径的模式
 */
public class RemoteWalkRouteQuery implements Parcelable, Cloneable {
    private RemoteFromAndTo fromAndTo;
    private int mode;

    /**
     * RemoteWalkRouteQuery构造函数
     */
    public RemoteWalkRouteQuery() {

    }

    /**
     * RemoteWalkRouteQuery构造函数
     * 
     * @param fromAndTo
     *            步行路径的起终点
     * 
     * @param mode
     *            计算路径的模式
     */
    public RemoteWalkRouteQuery(RemoteFromAndTo fromAndTo, int mode) {
        this.fromAndTo = fromAndTo;
        this.mode = mode;
    }

    /**
     * 返回查询路径的起终点
     * 
     * @return 查询路径的起终点
     */
    public RemoteFromAndTo getFromAndTo() {
        return this.fromAndTo;
    }

    /**
     * 设置查询路径的起终点
     * 
     * @param fromAndTo
     *            查询路径的起终点
     */
    public void setFromAndTo(RemoteFromAndTo fromAndTo) {
        this.fromAndTo = fromAndTo;
    }

    /**
     * 返回计算路径的模式
     * 
     * @return 计算路径的模式
     */
    public int getMode() {
        return this.mode;
    }

    /**
     * 设置计算路径的模式
     * 
     * @param mode
     *            计算路径的模式
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    protected Object clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return new RemoteWalkRouteQuery(this.fromAndTo, this.mode);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (!(object instanceof RemoteWalkRouteQuery))
            return false;

        RemoteWalkRouteQuery other = (RemoteWalkRouteQuery) object;

        if (this.mode != other.mode)
            return false;
        if (this.fromAndTo == null) {
            if (other.fromAndTo != null)
                return false;
        } else if (!this.fromAndTo.equals(other.fromAndTo))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = prime * result
                + (this.fromAndTo == null ? 0 : this.fromAndTo.hashCode());
        result = prime * result + this.mode;

        return result;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mode);

        if (this.fromAndTo != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.fromAndTo, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteWalkRouteQuery> CREATOR = new Creator<RemoteWalkRouteQuery>() {

        @Override
        public RemoteWalkRouteQuery createFromParcel(Parcel source) {
            RemoteWalkRouteQuery walkRouteQuery = new RemoteWalkRouteQuery();

            walkRouteQuery.mode = source.readInt();

            if (source.readInt() != 0) {
                walkRouteQuery.fromAndTo = source
                        .readParcelable(RemoteFromAndTo.class.getClassLoader());
            }

            return walkRouteQuery;
        }

        @Override
        public RemoteWalkRouteQuery[] newArray(int size) {
            return new RemoteWalkRouteQuery[size];
        }
    };

}
