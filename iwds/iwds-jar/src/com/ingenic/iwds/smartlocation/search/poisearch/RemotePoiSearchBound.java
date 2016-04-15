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

package com.ingenic.iwds.smartlocation.search.poisearch;

import java.util.List;

import com.ingenic.iwds.smartlocation.search.core.RemoteLatLonPoint;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 此类定义了查询圆形和查询矩形，查询返回的POI的位置在此圆形或矩形内
 */
public class RemotePoiSearchBound implements Parcelable, Cloneable {
    /**
     * 圆形
     */
    public static final String BOUND_SHAPE = "Bound";

    /**
     * 多边形
     */
    public static final String POLYGON_SHAPE = "Polygon";

    /**
     * 矩形
     */
    public static final String RECTANGLE_SHAPE = "Rectangle";

    /**
     * 椭圆
     */
    public static final String ELLIPSE_SHAPE = "Ellipse";

    private RemoteLatLonPoint centerPoint;
    private double latSpanInMeter;
    private double lonSpanInMeter;
    private RemoteLatLonPoint lowerLeft;
    private String shape;
    private RemoteLatLonPoint upperRight;
    private List<RemoteLatLonPoint> polyGonList;
    private int range;
    private boolean isDistanceSort = true;

    /**
     * 根据给定的参数来构造RemotePoiSearchBound 的新对象，默认由近到远排序。
     * 如果超出中国边界或者range<=0，则抛出IllegalArgumentException 异常
     * 
     * @param centerPoint
     *            该范围的中心点
     * 
     * @param range
     *            半径，单位：米
     */
    public RemotePoiSearchBound(RemoteLatLonPoint centerPoint, int range) {
        this.shape = "Bound";
        this.range = range;
        this.centerPoint = centerPoint;
    }

    /**
     * 根据给定的参数来构造RemotePoiSearchBound 的新对象，默认由近到远排序。
     * 如果超出中国边界或者range<=0，则抛出IllegalArgumentException 异常
     * 
     * @param centerPoint
     *            该范围的中心点
     * 
     * @param range
     *            半径，单位：米
     * 
     * @param distanceSort
     *            是否按照距离排序
     */
    public RemotePoiSearchBound(RemoteLatLonPoint centerPoint, int range,
            boolean distanceSort) {
        this.shape = "Bound";
        this.range = range;
        this.centerPoint = centerPoint;
        this.isDistanceSort = distanceSort;
    }

    /**
     * 根据给定的参数来构造RemotePoiSearchBound 的新对象。
     * 如果超出中国边界或者lowerLeft>=upperRight，则抛出IllegalArgumentException 异常
     * 
     * @param upperRight
     *            矩形的左下角
     * 
     * @param lowerLeft
     *            矩形的右下角
     */
    public RemotePoiSearchBound(RemoteLatLonPoint upperRight,
            RemoteLatLonPoint lowerLeft) {
        this.shape = "Rectangle";
        this.upperRight = upperRight;
        this.lowerLeft = lowerLeft;
    }

    /**
     * 根据给定的参数来构造RemotePoiSearchBound 的新对象。
     * 如果超出中国边界或者polyGonList不是首尾相接的几何点，并且不为多边形，则抛出IllegalArgumentException 异常
     * 
     * @param polyGonList
     *            首尾相接的几何点，可以组成多边形
     */
    public RemotePoiSearchBound(List<RemoteLatLonPoint> polyGonList) {
        this.shape = "Polygon";
        this.polyGonList = polyGonList;
    }

    private RemotePoiSearchBound(RemoteLatLonPoint lowerLeft,
            RemoteLatLonPoint upperRight, int range,
            RemoteLatLonPoint centerPoint, String shape,
            List<RemoteLatLonPoint> polyGonList, boolean isDistanceSort) {
        this.lowerLeft = lowerLeft;
        this.upperRight = upperRight;
        this.range = range;
        this.centerPoint = centerPoint;
        this.shape = shape;
        this.polyGonList = polyGonList;
        this.isDistanceSort = isDistanceSort;
    }

    /**
     * RemotePoiSearchBound构造函数
     */
    public RemotePoiSearchBound() {

    }

    /**
     * 返回矩形左下角坐标
     * 
     * @return 矩形左下角坐标
     */
    public RemoteLatLonPoint getLowerLeft() {
        return this.lowerLeft;
    }

    /**
     * 设置矩形左下角坐标
     * 
     * @param lowerLeft
     *            矩形左下角坐标
     */
    public void setLowerLeft(RemoteLatLonPoint lowerLeft) {
        this.lowerLeft = lowerLeft;
    }

    /**
     * 返回矩形右上角坐标
     * 
     * @return 矩形右上角坐标
     */
    public RemoteLatLonPoint getUpperRight() {
        return this.upperRight;
    }

    /**
     * 设置矩形右上角坐标
     * 
     * @param upperRight
     *            矩形右上角坐标
     */
    public void setUpperRight(RemoteLatLonPoint upperRight) {
        this.upperRight = upperRight;
    }

    /**
     * 返回矩形中心点坐标
     * 
     * @return 矩形中心点坐标
     */
    public RemoteLatLonPoint getCenter() {
        return this.centerPoint;
    }

    /**
     * 设置矩形中心点坐标
     * 
     * @param centerPoint
     *            矩形中心点坐标
     */
    public void setCenter(RemoteLatLonPoint centerPoint) {
        this.centerPoint = centerPoint;
    }

    /**
     * 返回矩形水平方向的间距，单位为米
     * 
     * @return 矩形水平方向的间距，单位为米
     */
    public double getLonSpanInMeter() {
        return this.lonSpanInMeter;
    }

    /**
     * 设置矩形水平方向的间距，单位为米
     * 
     * @param lonSpanInMeter
     *            矩形水平方向的间距，单位为米
     */
    public void setLonSpanInMeter(double lonSpanInMeter) {
        this.lonSpanInMeter = lonSpanInMeter;
    }

    /**
     * 返回矩形竖直方向的间距，单位为米
     * 
     * @return 矩形竖直方向的间距，单位为米
     */
    public double getLatSpanInMeter() {
        return this.latSpanInMeter;
    }

    /**
     * 设置矩形竖直方向的间距，单位为米
     * 
     * @param latSpanInMeter
     *            矩形竖直方向的间距，单位为米
     */
    public void setLatSpanInMeter(double latSpanInMeter) {
        this.latSpanInMeter = latSpanInMeter;
    }

    /**
     * 返回矩形的范围
     * 
     * @return 矩形的范围
     */
    public int getRange() {
        return this.range;
    }

    /**
     * 设置矩形的范围
     * 
     * @param range
     *            矩形的范围
     */
    public void setRange(int range) {
        this.range = range;
    }

    /**
     * 返回查询范围形状。查询范围目前只有Bound(圆形)和Rectangle(矩形)两种
     * 
     * @return 查询范围形状
     */
    public String getShape() {
        return this.shape;
    }

    /**
     * 设置查询范围形状。查询范围目前只有Bound(圆形)和Rectangle(矩形)两种
     * 
     * @param shape
     *            查询范围形状
     */
    public void setShape(String shape) {
        this.shape = shape;
    }

    /**
     * 返回是否按照距离排序
     * 
     * @return 是否按照距离排序
     */
    public boolean isDistanceSort() {
        return this.isDistanceSort;
    }

    /**
     * 设置是否按照距离排序
     * 
     * @param isDistanceSort
     *            是否按照距离排序
     */
    public void setDistanceSort(boolean isDistanceSort) {
        this.isDistanceSort = isDistanceSort;
    }

    /**
     * 返回几何点对象图形
     * 
     * @return 几何点对象图形
     */
    public List<RemoteLatLonPoint> getPolyGonList() {
        return this.polyGonList;
    }

    @Override
    protected Object clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        RemotePoiSearchBound bound = new RemotePoiSearchBound(this.lowerLeft,
                this.upperRight, this.range, this.centerPoint, this.shape,
                this.polyGonList, this.isDistanceSort);
        bound.setLatSpanInMeter(this.latSpanInMeter);
        bound.setLonSpanInMeter(this.lonSpanInMeter);

        return bound;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (!(object instanceof RemotePoiSearchBound))
            return false;

        RemotePoiSearchBound other = (RemotePoiSearchBound) object;

        if (this.centerPoint == null) {
            if (other.centerPoint != null)
                return false;
        } else if (!this.centerPoint.equals(other.centerPoint))
            return false;
        if (this.latSpanInMeter != other.latSpanInMeter)
            return false;
        if (this.lonSpanInMeter != other.lonSpanInMeter)
            return false;
        if (this.lowerLeft == null) {
            if (other.lowerLeft != null)
                return false;
        } else if (!this.equals(other.lowerLeft))
            return false;
        if (this.upperRight == null) {
            if (other.upperRight != null)
                return false;
        } else if (!this.upperRight.equals(other.upperRight))
            return false;
        if (this.shape == null) {
            if (other.shape != null)
                return false;
        } else if (!this.shape.equals(other.shape))
            return false;
        if (this.range != other.range)
            return false;
        if (this.isDistanceSort != other.isDistanceSort)
            return false;
        if (this.polyGonList == null) {
            if (other.polyGonList != null)
                return false;
        } else if (!this.polyGonList.equals(other.polyGonList))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = prime * result
                + (this.centerPoint != null ? 0 : this.centerPoint.hashCode());
        result = prime * result
                + (this.lowerLeft != null ? 0 : this.lowerLeft.hashCode());
        result = prime * result
                + (this.shape != null ? 0 : this.shape.hashCode());
        result = prime * result
                + (this.upperRight != null ? 0 : this.upperRight.hashCode());
        result = prime * result
                + (this.polyGonList != null ? 0 : this.polyGonList.hashCode());
        result = prime * result + this.range;
        result = prime * result + (this.isDistanceSort ? 1231 : 1237);

        long code = Double.doubleToLongBits(this.latSpanInMeter);
        result = prime * result + (int) (code ^ code >>> 32);

        code = Double.doubleToLongBits(this.lonSpanInMeter);
        result = prime * result + (int) (code ^ code >>> 32);

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.latSpanInMeter);
        dest.writeDouble(this.lonSpanInMeter);
        dest.writeString(this.shape);
        dest.writeInt(this.range);
        dest.writeByte((byte) (this.isDistanceSort ? 1 : 0));

        if (this.centerPoint != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.centerPoint, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.lowerLeft != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.lowerLeft, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.upperRight != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.upperRight, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.polyGonList != null) {
            dest.writeInt(1);
            dest.writeList(this.polyGonList);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemotePoiSearchBound> CREATOR = new Creator<RemotePoiSearchBound>() {

        @Override
        public RemotePoiSearchBound createFromParcel(Parcel source) {
            RemotePoiSearchBound searchBound = new RemotePoiSearchBound();

            searchBound.latSpanInMeter = source.readDouble();
            searchBound.lonSpanInMeter = source.readDouble();
            searchBound.shape = source.readString();
            searchBound.range = source.readInt();

            if (source.readByte() == 1)
                searchBound.isDistanceSort = true;
            else
                searchBound.isDistanceSort = false;

            if (source.readInt() != 0) {
                searchBound.centerPoint = source
                        .readParcelable(RemoteLatLonPoint.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                searchBound.lowerLeft = source
                        .readParcelable(RemoteLatLonPoint.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                searchBound.upperRight = source
                        .readParcelable(RemoteLatLonPoint.class
                                .getClassLoader());
            }

            if (source.readInt() != 0) {
                searchBound.polyGonList = source
                        .readArrayList(RemoteLatLonPoint.class.getClassLoader());
            }

            return searchBound;
        }

        @Override
        public RemotePoiSearchBound[] newArray(int size) {
            return new RemotePoiSearchBound[size];
        }

    };

}
