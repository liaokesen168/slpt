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

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 定义一个POI（Point Of Interest，兴趣点）一条影院类信息
 */
public class RemoteCinema implements Parcelable {
    private String deepSrc;
    private String intro;
    private String openTime;
    private String openTimeGDF;
    private String parking;
    private String rating;
    private boolean isSeatOrdering;
    private List<RemotePhoto> photoList = new ArrayList<RemotePhoto>();

    /**
     * RemoteCinema构造函数
     */
    public RemoteCinema() {

    }

    /**
     * 返回影院是否可订座
     * 
     * @return 影院是否可订座
     */
    public boolean isSeatOrdering() {
        return this.isSeatOrdering;
    }

    /**
     * 设置影院是否可订座
     * 
     * @param seatOrdering
     *            影院是否可订座
     */
    public void setSeatOrdering(boolean seatOrdering) {
        this.isSeatOrdering = seatOrdering;
    }

    /**
     * 返回影院的简介
     * 
     * @return 影院的简介
     */
    public String getIntro() {
        return this.intro;
    }

    /**
     * 设置影院的简介
     * 
     * @param intro
     *            影院的简介
     */
    public void setIntro(String intro) {
        this.intro = intro;
    }

    /**
     * 返回影院的综合评分
     * 
     * @return 影院的综合评分
     */
    public String getRating() {
        return this.rating;
    }

    /**
     * 设置影院的综合评分
     * 
     * @param rating
     *            影院的综合评分
     */
    public void setRating(String rating) {
        this.rating = rating;
    }

    /**
     * 返回影院数据的信息来源
     * 
     * @return 影院数据的信息来源
     */
    public String getDeepsrc() {
        return this.deepSrc;
    }

    /**
     * 设置影院数据的信息来源
     * 
     * @param deepSrc
     *            影院数据的信息来源
     */
    public void setDeepsrc(String deepSrc) {
        this.deepSrc = deepSrc;
    }

    /**
     * 返回影院的停车场设施
     * 
     * @return 影院的停车场设施
     */
    public String getParking() {
        return this.parking;
    }

    /**
     * 设置影院的停车场设施
     * 
     * @param parking
     *            影院的停车场设施
     */
    public void setParking(String parking) {
        this.parking = parking;
    }

    /**
     * 返回影院的规范格式的营业时间
     * 
     * @return 影院的规范格式的营业时间
     */
    public String getOpentimeGDF() {
        return this.openTimeGDF;
    }

    /**
     * 设置影院的规范格式的营业时间
     * 
     * @param openTimeGDF
     *            影院的规范格式的营业时间
     */
    public void setOpentimeGDF(String openTimeGDF) {
        this.openTimeGDF = openTimeGDF;
    }

    /**
     * 返回影院的非规范格式的营业时间
     * 
     * @return 影院的非规范格式的营业时间
     */
    public String getOpenTime() {
        return this.openTime;
    }

    /**
     * 设置影院的非规范格式的营业时间
     * 
     * @param openTime
     *            影院的非规范格式的营业时间
     */
    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    /**
     * 返回影院的图片信息
     * 
     * @return 影院的图片信息
     */
    public List<RemotePhoto> getPhotos() {
        return this.photoList;
    }

    /**
     * 设置影院的图片信息
     * 
     * @param photoList
     *            影院的图片信息
     */
    public void setPhotots(List<RemotePhoto> photoList) {
        this.photoList = photoList;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (!(object instanceof RemoteCinema))
            return false;

        RemoteCinema other = (RemoteCinema) object;

        if (this.deepSrc == null) {
            if (other.deepSrc != null)
                return false;
        } else if (!this.deepSrc.equals(other.deepSrc))
            return false;
        if (this.intro == null) {
            if (other.intro != null)
                return false;
        } else if (!this.intro.equals(other.intro))
            return false;
        if (this.openTime == null) {
            if (other.openTime != null)
                return false;
        } else if (!this.openTime.equals(other.openTime))
            return false;
        if (this.openTimeGDF == null) {
            if (other.openTimeGDF != null)
                return false;
        } else if (!this.openTimeGDF.equals(other.openTimeGDF))
            return false;
        if (this.parking == null) {
            if (other.parking != null)
                return false;
        } else if (!this.parking.equals(other.parking))
            return false;
        if (this.rating == null) {
            if (other.rating != null)
                return false;
        } else if (!this.rating.equals(other.rating))
            return false;
        if (this.photoList == null) {
            if (other.photoList != null)
                return false;
        } else if (!this.photoList.equals(other.photoList))
            return false;
        if (this.isSeatOrdering != other.isSeatOrdering)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = prime * result
                + (this.deepSrc == null ? 0 : this.deepSrc.hashCode());
        result = prime * result
                + (this.intro == null ? 0 : this.intro.hashCode());
        result = prime * result
                + (this.openTime == null ? 0 : this.intro.hashCode());
        result = prime * result
                + (this.openTimeGDF == null ? 0 : this.openTimeGDF.hashCode());
        result = prime * result
                + (this.parking == null ? 0 : this.parking.hashCode());
        result = prime * result
                + (this.rating == null ? 0 : this.rating.hashCode());
        result = prime * result
                + (this.photoList == null ? 0 : this.photoList.hashCode());
        result = prime * result + (this.isSeatOrdering ? 1231 : 1237);

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deepSrc);
        dest.writeString(this.intro);
        dest.writeString(this.openTime);
        dest.writeString(this.openTimeGDF);
        dest.writeString(this.parking);
        dest.writeString(this.rating);
        dest.writeByte((byte) (this.isSeatOrdering ? 1 : 0));

        if (this.photoList != null) {
            dest.writeInt(1);
            dest.writeList(photoList);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteCinema> CREATOR = new Creator<RemoteCinema>() {
        @Override
        public RemoteCinema createFromParcel(Parcel source) {
            RemoteCinema cinema = new RemoteCinema();

            cinema.deepSrc = source.readString();
            cinema.intro = source.readString();
            cinema.openTime = source.readString();
            cinema.openTimeGDF = source.readString();
            cinema.parking = source.readString();
            cinema.rating = source.readString();
            if (source.readByte() == 1)
                cinema.isSeatOrdering = true;
            else
                cinema.isSeatOrdering = false;

            if (source.readInt() != 0) {
                cinema.photoList = source.readArrayList(RemotePhoto.class
                        .getClassLoader());
            }

            return cinema;
        }

        @Override
        public RemoteCinema[] newArray(int size) {
            return new RemoteCinema[size];
        }
    };
}
