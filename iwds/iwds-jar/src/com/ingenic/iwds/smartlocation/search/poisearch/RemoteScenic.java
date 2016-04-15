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
 * 定义一个POI（Point Of Interest，兴趣点）一条景点信息
 */
public class RemoteScenic implements Parcelable {
    private String deepSrc;
    private String intro;
    private String level;
    private String openTime;
    private String openTimeGDF;
    private String orderWapUrl;
    private String orderWebUrl;
    private String price;
    private String rating;
    private String recommend;
    private String season;
    private String theme;
    private List<RemotePhoto> photoList = new ArrayList<RemotePhoto>();

    /**
     * RemoteScenic构造函数
     */
    public RemoteScenic() {

    }

    /**
     * 返回景点的简介
     * 
     * @return 景点的简介
     */
    public String getIntro() {
        return this.intro;
    }

    /**
     * 设置景点的简介
     * 
     * @param intro
     *            景点的简介
     */
    public void setIntro(String intro) {
        this.intro = intro;
    }

    /**
     * 返回景点的综合评价
     * 
     * @return 景点的综合评价
     */
    public String getRating() {
        return this.rating;
    }

    /**
     * 设置景点的综合评价
     * 
     * @param rating
     *            景点的综合评价
     */
    public void setRating(String rating) {
        this.rating = rating;
    }

    /**
     * 返回景点数据的信息来源
     * 
     * @return 景点数据的信息来源
     */
    public String getDeepsrc() {
        return this.deepSrc;
    }

    /**
     * 设置景点数据的信息来源
     * 
     * @param deepSrc
     *            景点数据的信息来源
     */
    public void setDeepsec(String deepSrc) {
        this.deepSrc = deepSrc;
    }

    /**
     * 返回景区的国标级别
     * 
     * @return 景区的国标级别
     */
    public String getLevel() {
        return this.level;
    }

    /**
     * 设置景区的国标级别
     * 
     * @param level
     *            景区的国标级别
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * 返回景区的门票价格
     * 
     * @return 景区的门票价格
     */
    public String getPrice() {
        return this.price;
    }

    /**
     * 设置景区的门票价格
     * 
     * @param price
     *            景区的门票价格
     */
    public void setPrice(String price) {
        this.price = price;
    }

    /**
     * 返回景区适合游玩的季节
     * 
     * @return 景区适合游玩的季节
     */
    public String getSeason() {
        return this.season;
    }

    /**
     * 设置景区适合游玩的季节
     * 
     * @param season
     *            景区适合游玩的季节
     */
    public void setSeason(String season) {
        this.season = season;
    }

    /**
     * 返回景区推荐的景点
     * 
     * @return 景区推荐的景点
     */
    public String getRecommend() {
        return this.recommend;
    }

    /**
     * 设置景区推荐的景点
     * 
     * @param recommend
     *            景区推荐的景点
     */
    public void setRecommend(String recommend) {
        this.recommend = recommend;
    }

    /**
     * 返回景区的主题
     * 
     * @return 景区的主题
     */
    public String getTheme() {
        return this.theme;
    }

    /**
     * 设置景区的主题
     * 
     * @param theme
     *            景区的主题
     */
    public void setTheme(String theme) {
        this.theme = theme;
    }

    /**
     * 返回景区wap的购票链接
     * 
     * @return 景区wap的购票链接
     */
    public String getOrderWapUrl() {
        return this.orderWapUrl;
    }

    /**
     * 设置景区wap的购票链接
     * 
     * @param orderWapUrl
     *            景区wap的购票链接
     */
    public void setOrderWapUrl(String orderWapUrl) {
        this.orderWapUrl = orderWapUrl;
    }

    /**
     * 返回景区web的购票链接
     * 
     * @return 返回景区web的购票链接
     */
    public String getOrderWebUrl() {
        return this.orderWebUrl;
    }

    /**
     * 设置景区web的购票链接
     * 
     * @param orderWebUrl
     *            景区web的购票链接
     */
    public void setOrderWebUrl(String orderWebUrl) {
        this.orderWebUrl = orderWebUrl;
    }

    /**
     * 返回景区规范格式的营业时间
     * 
     * @return 景区规范格式的营业时间
     */
    public String getOpentimeGDF() {
        return this.openTimeGDF;
    }

    /**
     * 设置景区规范格式的营业时间
     * 
     * @param openTimeGDF
     *            景区规范格式的营业时间
     */
    public void setOpentimeGDF(String openTimeGDF) {
        this.openTimeGDF = openTimeGDF;
    }

    /**
     * 返回景区非规范格式的营业时间
     * 
     * @return 景区非规范格式的营业时间
     */
    public String getOpenTime() {
        return this.openTime;
    }

    /**
     * 设置景区非规范格式的营业时间
     * 
     * @param openTime
     *            景区非规范格式的营业时间
     */
    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    /**
     * 返回景区的图片信息
     * 
     * @return 景区的图片信息
     */
    public List<RemotePhoto> getPhotos() {
        return this.photoList;
    }

    /**
     * 设置景区的图片信息
     * 
     * @param photoList
     *            景区的图片信息
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
        if (!(object instanceof RemoteScenic))
            return false;

        RemoteScenic other = (RemoteScenic) object;

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
        if (this.level == null) {
            if (other.level != null)
                return false;
        } else if (!this.level.equals(other.level))
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
        if (this.orderWapUrl == null) {
            if (other.orderWapUrl != null)
                return false;
        } else if (!this.orderWapUrl.equals(other.orderWapUrl))
            return false;
        if (this.orderWebUrl == null) {
            if (other.orderWebUrl != null)
                return false;
        } else if (!this.orderWebUrl.equals(other.orderWebUrl))
            return false;
        if (this.photoList == null) {
            if (other.photoList != null)
                return false;
        } else if (!this.photoList.equals(other.photoList))
            return false;
        if (this.price == null) {
            if (other.price != null)
                return false;
        } else if (!this.price.equals(other.price))
            return false;
        if (this.rating == null) {
            if (other.rating != null)
                return false;
        } else if (!this.rating.equals(other.rating))
            return false;
        if (this.recommend == null) {
            if (other.recommend != null)
                return false;
        } else if (!this.recommend.equals(other.recommend))
            return false;
        if (this.season == null) {
            if (other.season != null)
                return false;
        } else if (!this.season.equals(other.season))
            return false;
        if (this.theme == null) {
            if (other.theme != null)
                return false;
        } else if (!this.theme.equals(other.theme))
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
                + (this.level == null ? 0 : this.level.hashCode());
        result = prime * result
                + (this.openTime == null ? 0 : this.openTime.hashCode());
        result = prime * result
                + (this.openTimeGDF == null ? 0 : this.openTimeGDF.hashCode());
        result = prime * result
                + (this.orderWapUrl == null ? 0 : this.orderWapUrl.hashCode());
        result = prime * result
                + (this.orderWebUrl == null ? 0 : this.orderWebUrl.hashCode());
        result = prime * result
                + (this.price == null ? 0 : this.price.hashCode());
        result = prime * result
                + (this.rating == null ? 0 : this.rating.hashCode());
        result = prime * result
                + (this.recommend == null ? 0 : this.recommend.hashCode());
        result = prime * result
                + (this.season == null ? 0 : this.season.hashCode());
        result = prime * result
                + (this.theme == null ? 0 : this.theme.hashCode());
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
        dest.writeString(this.deepSrc);
        dest.writeString(this.intro);
        dest.writeString(this.level);
        dest.writeString(this.openTime);
        dest.writeString(this.openTimeGDF);
        dest.writeString(this.orderWapUrl);
        dest.writeString(this.orderWebUrl);
        dest.writeString(this.price);
        dest.writeString(this.rating);
        dest.writeString(this.recommend);
        dest.writeString(this.season);
        dest.writeString(this.theme);
        if (this.photoList != null) {
            dest.writeInt(1);
            dest.writeList(this.photoList);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteScenic> CREATOR = new Creator<RemoteScenic>() {

        @Override
        public RemoteScenic createFromParcel(Parcel source) {
            RemoteScenic scenic = new RemoteScenic();

            scenic.deepSrc = source.readString();
            scenic.intro = source.readString();
            scenic.level = source.readString();
            scenic.openTime = source.readString();
            scenic.openTimeGDF = source.readString();
            scenic.orderWapUrl = source.readString();
            scenic.orderWebUrl = source.readString();
            scenic.price = source.readString();
            scenic.rating = source.readString();
            scenic.recommend = source.readString();
            scenic.season = source.readString();
            scenic.theme = source.readString();

            if (source.readInt() != 0) {
                scenic.photoList = source.readArrayList(RemotePhoto.class
                        .getClassLoader());
            }

            return scenic;
        }

        @Override
        public RemoteScenic[] newArray(int size) {
            return new RemoteScenic[size];
        }
    };
}
