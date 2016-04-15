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

import com.ingenic.iwds.smartlocation.search.core.RemoteLatLonPoint;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 构造路径规划的起点和终点坐标
 */
public class RemoteFromAndTo implements Parcelable, Cloneable {
    private String destinationPoiId;
    private RemoteLatLonPoint fromPos;
    private RemoteLatLonPoint toPos;
    private String startPoiId;

    /**
     * RemoteFromAndTo构造函数
     */
    public RemoteFromAndTo() {

    }

    /**
     * RemoteFromAndTo构造函数
     * 
     * @param fromPos
     *            路径规划的起点坐标
     * 
     * @param toPos
     *            路径规划的终点坐标
     */
    public RemoteFromAndTo(RemoteLatLonPoint fromPos, RemoteLatLonPoint toPos) {
        this.fromPos = fromPos;
        this.toPos = toPos;
    }

    /**
     * 返回路径规划目的地POI的ID
     * 
     * @return 路径规划目的地POI的ID
     */
    public String getDestinationPoiId() {
        return this.destinationPoiId;
    }

    /**
     * 设置路径规划目的地POI的ID
     * 
     * @param destinationPoiId
     *            路径规划目的地POI的ID
     */
    public void setDestinationPoiId(String destinationPoiId) {
        this.destinationPoiId = destinationPoiId;
    }

    /**
     * 返回路径规划起点POI的ID
     * 
     * @return 路径规划起点POI的ID
     */
    public String getStartPoiId() {
        return this.startPoiId;
    }

    /**
     * 设置路径规划起点POI的ID
     * 
     * @param startPoiId
     *            路径规划起点POI的ID
     */
    public void setStartPoiId(String startPoiId) {
        this.startPoiId = startPoiId;
    }

    /**
     * 返回路径规划的起点坐标
     * 
     * @return 路径规划的起点坐标
     */
    public RemoteLatLonPoint getFrom() {
        return this.fromPos;
    }

    /**
     * 设置路径规划的起点坐标
     * 
     * @param fromPos
     *            路径规划的起点坐标
     */
    public void setFrom(RemoteLatLonPoint fromPos) {
        this.fromPos = fromPos;
    }

    /**
     * 返回路径规划的终点坐标
     * 
     * @return 路径规划的终点坐标
     */
    public RemoteLatLonPoint getTo() {
        return this.toPos;
    }

    /**
     * 设置路径规划的终点坐标
     * 
     * @param toPos
     *            路径规划的终点坐标
     */
    public void setTo(RemoteLatLonPoint toPos) {
        this.toPos = toPos;
    }

    @Override
    protected Object clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        RemoteFromAndTo fromAndTo = new RemoteFromAndTo(this.fromPos,
                this.toPos);
        fromAndTo.setStartPoiId(this.startPoiId);
        fromAndTo.setDestinationPoiId(this.destinationPoiId);

        return fromAndTo;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (!(object instanceof RemoteFromAndTo))
            return false;

        RemoteFromAndTo other = (RemoteFromAndTo) object;

        if (this.destinationPoiId == null) {
            if (other.destinationPoiId != null)
                return false;
        } else if (!this.destinationPoiId.equals(other.destinationPoiId))
            return false;
        if (this.startPoiId == null) {
            if (other.startPoiId != null)
                return false;
        } else if (!this.startPoiId.equals(other.startPoiId))
            return false;
        if (this.fromPos == null) {
            if (other.fromPos != null)
                return false;
        } else if (!this.fromPos.equals(other.fromPos))
            return false;
        if (this.toPos == null) {
            if (other.toPos != null)
                return false;
        } else if (!this.toPos.equals(other.toPos))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = prime
                * result
                + (this.destinationPoiId == null ? 0 : this.destinationPoiId
                        .hashCode());
        result = prime * result
                + (this.startPoiId == null ? 0 : this.startPoiId.hashCode());
        result = prime * result
                + (this.fromPos == null ? 0 : this.fromPos.hashCode());
        result = prime * result
                + (this.toPos == null ? 0 : this.toPos.hashCode());

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.destinationPoiId);
        dest.writeString(this.startPoiId);

        if (this.fromPos != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.fromPos, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.toPos != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.toPos, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteFromAndTo> CREATOR = new Creator<RemoteFromAndTo>() {

        @Override
        public RemoteFromAndTo createFromParcel(Parcel source) {
            RemoteFromAndTo fromAndTo = new RemoteFromAndTo();

            fromAndTo.destinationPoiId = source.readString();
            fromAndTo.startPoiId = source.readString();

            if (source.readInt() != 0)
                fromAndTo.fromPos = source
                        .readParcelable(RemoteLatLonPoint.class
                                .getClassLoader());

            if (source.readInt() != 0)
                fromAndTo.toPos = source.readParcelable(RemoteLatLonPoint.class
                        .getClassLoader());

            return fromAndTo;
        }

        @Override
        public RemoteFromAndTo[] newArray(int size) {
            return new RemoteFromAndTo[size];
        }

    };
}
