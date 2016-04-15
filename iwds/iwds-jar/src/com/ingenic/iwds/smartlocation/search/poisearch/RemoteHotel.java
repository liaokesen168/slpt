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
 * 定义一个POI（Point Of Interest，兴趣点）一条酒店类信息
 */
public class RemoteHotel implements Parcelable {
    private String addition;
    private String deepSrc;
    private String envRating;
    private String faciRating;
    private String healthRating;
    private String intro;
    private String lowestPrice;
    private List<RemotePhoto> photoList = new ArrayList<RemotePhoto>();
    private String rating;
    private String serviceRating;
    private String star;
    private String traffic;

    /**
     * RemoteHotel构造函数
     */
    public RemoteHotel() {

    }

    /**
     * 返回酒店的综合评分
     * 
     * @return 酒店的综合评分
     */
    public String getRating() {
        return this.rating;
    }

    /**
     * 设置酒店的综合评分
     * 
     * @param rating
     *            酒店的综合评分
     */
    public void setRating(String rating) {
        this.rating = rating;
    }

    /**
     * 返回酒店的星级
     * 
     * @return 酒店的星级
     */
    public String getStar() {
        return this.star;
    }

    /**
     * 设置酒店的星级
     * 
     * @param star
     *            酒店的星级
     */
    public void setStar(String star) {
        this.star = star;
    }

    /**
     * 返回酒店的简介
     * 
     * @return 酒店的简介
     */
    public String getIntro() {
        return this.intro;
    }

    /**
     * 设置酒店的简介
     * 
     * @param intro
     *            酒店的简介
     */
    public void setIntro(String intro) {
        this.intro = intro;
    }

    /**
     * 返回酒店的最低房价
     * 
     * @return 酒店的最低房价
     */
    public String getLowestPrice() {
        return this.lowestPrice;
    }

    /**
     * 设置酒店的最低房价
     * 
     * @param lowestPrice
     *            酒店的最低房价
     */
    public void setLowestPrice(String lowestPrice) {
        this.lowestPrice = lowestPrice;
    }

    /**
     * 返回酒店的设施评分
     * 
     * @return 酒店的设施评分
     */
    public String getFaciRating() {
        return this.faciRating;
    }

    /**
     * 设置酒店的设施评分
     * 
     * @param faciRating
     *            酒店的设施评分
     */
    public void setFaciRating(String faciRating) {
        this.faciRating = faciRating;
    }

    /**
     * 返回酒店的卫生评分
     * 
     * @return 酒店的卫生评分
     */
    public String getHealthRating() {
        return this.healthRating;
    }

    /**
     * 设置酒店的卫生评分
     * 
     * @param healthRating
     *            酒店的卫生评分
     */
    public void setHealthRating(String healthRating) {
        this.healthRating = healthRating;
    }

    /**
     * 返回酒店的环境评分
     * 
     * @return 酒店的环境评分
     */
    public String getEnvironmentRating() {
        return this.envRating;
    }

    /**
     * 设置酒店的环境评分
     * 
     * @param envRating
     *            酒店的环境评分
     */
    public void setEnvironmentRating(String envRating) {
        this.envRating = envRating;
    }

    /**
     * 返回酒店的服务评分
     * 
     * @return 酒店的服务评分
     */
    public String getServiceRating() {
        return this.serviceRating;
    }

    /**
     * 设置酒店的服务评分
     * 
     * @param serviceRating
     *            酒店的服务评分
     */
    public void setServiceRating(String serviceRating) {
        this.serviceRating = serviceRating;
    }

    /**
     * 返回酒店的交通提示
     * 
     * @return 酒店的交通提示
     */
    public String getTraffic() {
        return this.traffic;
    }

    /**
     * 设置酒店的交通提示
     * 
     * @param traffic
     *            酒店的交通提示
     */
    public void setTraffic(String traffic) {
        this.traffic = traffic;
    }

    /**
     * 返回酒店的特色服务
     * 
     * @return 酒店的特色服务
     */
    public String getAddition() {
        return this.addition;
    }

    /**
     * 设置酒店的特色服务
     * 
     * @param addition
     *            酒店的特色服务
     */
    public void setAddition(String addition) {
        this.addition = addition;
    }

    /**
     * 返回酒店数据的信息来源
     * 
     * @return 酒店数据的信息来源
     */
    public String getDeepsrc() {
        return this.deepSrc;
    }

    /**
     * 设置酒店数据的信息来源
     * 
     * @param deepSrc
     *            酒店数据的信息来源
     */
    public void setDeepsrc(String deepSrc) {
        this.deepSrc = deepSrc;
    }

    /**
     * 返回酒店的图片信息
     * 
     * @return 酒店的图片信息
     */
    public List<RemotePhoto> getPhotos() {
        return this.photoList;
    }

    /**
     * 设置酒店的图片信息
     * 
     * @param photoList
     *            酒店的图片信息
     */
    public void setPhotos(List<RemotePhoto> photoList) {
        this.photoList = photoList;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (!(object instanceof RemoteHotel))
            return false;

        RemoteHotel other = (RemoteHotel) object;

        if (this.addition == null) {
            if (other.addition != null)
                return false;
        } else if (!this.addition.equals(other.addition))
            return false;
        if (this.deepSrc == null) {
            if (other.deepSrc != null)
                return false;
        } else if (!this.deepSrc.equals(other.deepSrc))
            return false;
        if (this.envRating == null) {
            if (other.envRating != null)
                return false;
        } else if (!this.envRating.equals(other.envRating))
            return false;
        if (this.faciRating == null) {
            if (other.faciRating != null)
                return false;
        } else if (!this.faciRating.equals(other.faciRating))
            return false;
        if (this.healthRating == null) {
            if (other.healthRating != null)
                return false;
        } else if (!this.healthRating.equals(other.healthRating))
            return false;
        if (this.intro == null) {
            if (other.intro != null)
                return false;
        } else if (!this.intro.equals(other.intro))
            return false;
        if (this.lowestPrice == null) {
            if (other.lowestPrice != null)
                return false;
        } else if (!this.lowestPrice.equals(other.lowestPrice))
            return false;
        if (this.photoList == null) {
            if (other.photoList != null)
                return false;
        } else if (!this.photoList.equals(object))
            return false;
        if (this.rating == null) {
            if (other.rating != null)
                return false;
        } else if (!this.rating.equals(other.rating))
            return false;
        if (this.serviceRating == null) {
            if (other.serviceRating != null)
                return false;
        } else if (!this.serviceRating.equals(other.serviceRating))
            return false;
        if (this.star == null) {
            if (other.star != null)
                return false;
        } else if (!this.star.equals(other.star))
            return false;
        if (this.traffic == null) {
            if (other.traffic != null)
                return false;
        } else if (!this.traffic.equals(other.traffic))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = prime * result
                + (this.addition == null ? 0 : this.addition.hashCode());
        result = prime * result
                + (this.deepSrc == null ? 0 : this.deepSrc.hashCode());
        result = prime * result
                + (this.envRating == null ? 0 : this.envRating.hashCode());
        result = prime * result
                + (this.faciRating == null ? 0 : this.faciRating.hashCode());
        result = prime
                * result
                + (this.healthRating == null ? 0 : this.healthRating.hashCode());
        result = prime * result
                + (this.intro == null ? 0 : this.intro.hashCode());
        result = prime * result
                + (this.lowestPrice == null ? 0 : this.lowestPrice.hashCode());
        result = prime * result
                + (this.rating == null ? 0 : this.rating.hashCode());
        result = prime
                * result
                + (this.serviceRating == null ? 0 : this.serviceRating
                        .hashCode());
        result = prime * result
                + (this.star == null ? 0 : this.star.hashCode());
        result = prime * result
                + (this.traffic == null ? 0 : this.traffic.hashCode());
        result = prime * result
                + (this.photoList == null ? 0 : this.photoList.hashCode());

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.addition);
        dest.writeString(this.deepSrc);
        dest.writeString(this.envRating);
        dest.writeString(this.faciRating);
        dest.writeString(this.healthRating);
        dest.writeString(this.intro);
        dest.writeString(this.lowestPrice);
        dest.writeString(this.rating);
        dest.writeString(this.serviceRating);
        dest.writeString(this.star);
        dest.writeString(this.traffic);

        if (this.photoList != null) {
            dest.writeInt(1);
            dest.writeList(this.photoList);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteHotel> CREATOR = new Creator<RemoteHotel>() {
        @Override
        public RemoteHotel createFromParcel(Parcel source) {
            RemoteHotel hotel = new RemoteHotel();

            hotel.addition = source.readString();
            hotel.deepSrc = source.readString();
            hotel.envRating = source.readString();
            hotel.faciRating = source.readString();
            hotel.healthRating = source.readString();
            hotel.intro = source.readString();
            hotel.lowestPrice = source.readString();
            hotel.rating = source.readString();
            hotel.serviceRating = source.readString();
            hotel.star = source.readString();
            hotel.traffic = source.readString();

            if (source.readInt() != 0) {
                hotel.photoList = source.readArrayList(RemotePhoto.class
                        .getClassLoader());
            }

            return hotel;
        }

        @Override
        public RemoteHotel[] newArray(int size) {
            return new RemoteHotel[size];
        }
    };
}
