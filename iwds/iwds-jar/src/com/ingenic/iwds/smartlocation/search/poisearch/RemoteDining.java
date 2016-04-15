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
 * 定义一个POI（Point Of Interest，兴趣点）一条餐饮类信息
 */
public class RemoteDining implements Parcelable {
    private String addition;
    private String atmosphere;
    private String cost;
    private String cpRating;
    private String cuisines;
    private String deepSrc;
    private String envRating;
    private String intro;
    private String openTime;
    private String openTimeGDF;
    private String orderinAppUrl;
    private String orderingWapUrl;
    private String orderingWebUrl;
    private List<RemotePhoto> photoList = new ArrayList<RemotePhoto>();
    private String rating;
    private String recommend;
    private String serviceRating;
    private String tag;
    private String tasteRating;
    private boolean isMealOrdering;

    /**
     * RemoteDining构造函数
     */
    public RemoteDining() {

    }

    /**
     * 返回是否可订餐
     * 
     * @return 是否可订餐
     */
    public boolean isMealOrdering() {
        return this.isMealOrdering;
    }

    /**
     * 设置是否可订餐
     * 
     * @param mealOrdering
     *            是否可订餐
     */
    public void setMealOrdering(boolean mealOrdering) {
        this.isMealOrdering = mealOrdering;
    }

    /**
     * 返回当前餐厅的菜系
     * 
     * @return 当前餐厅的菜系
     */
    public String getCuisines() {
        return this.cuisines;
    }

    /**
     * 设置当前餐厅的菜系
     * 
     * @param cuisines
     *            当前餐厅的菜系
     */
    public void setCuisines(String cuisines) {
        this.cuisines = cuisines;
    }

    /**
     * 返回当前餐厅的标记
     * 
     * @return 当前餐厅的标记
     */
    public String getTag() {
        return this.tag;
    }

    /**
     * 设置当前餐厅的标记
     * 
     * @param tag
     *            当前餐厅的标记
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * 返回当前餐厅的描述
     * 
     * @return 当前餐厅的描述
     */
    public String getIntro() {
        return this.intro;
    }

    /**
     * 设置当前餐厅的描述
     * 
     * @param intro
     *            当前餐厅的描述
     */
    public void setIntro(String intro) {
        this.intro = intro;
    }

    /**
     * 返回餐厅的综合评分
     * 
     * @return 餐厅的综合评分
     */
    public String getRating() {
        return this.rating;
    }

    /**
     * 设置餐厅的综合评分
     * 
     * @param rating
     *            返回餐厅的综合评分
     */
    public void setRating(String rating) {
        this.rating = rating;
    }

    /**
     * 返回某一信息来源的评分
     * 
     * @return 某一信息来源的评分
     */
    public String getCpRating() {
        return this.cpRating;
    }

    /**
     * 设置某一信息来源的评分
     * 
     * @param cpRating
     *            某一信息来源的评分
     */
    public void setCpRating(String cpRating) {
        this.cpRating = cpRating;
    }

    /**
     * 返回信息的来源
     * 
     * @return 信息的来源
     */
    public String getDeepsrc() {
        return this.deepSrc;
    }

    /**
     * 设置信息的来源
     * 
     * @param deepSrc
     *            信息的来源
     */
    public void setDeepsrc(String deepSrc) {
        this.deepSrc = deepSrc;
    }

    /**
     * 返回餐厅的口味评分
     * 
     * @return 餐厅的口味评分
     */
    public String getTasteRating() {
        return this.tasteRating;
    }

    /**
     * 设置餐厅的口味评分
     * 
     * @param tasteRating
     *            餐厅的口味评分
     */
    public void setTasteRating(String tasteRating) {
        this.tasteRating = tasteRating;
    }

    /**
     * 返回餐厅的环境评分
     * 
     * @return 餐厅的环境评分
     */
    public String getEnvironmentRating() {
        return this.envRating;
    }

    /**
     * 设置餐厅的环境评分
     * 
     * @param envRating
     *            餐厅的环境评分
     */
    public void setEnvironmentRating(String envRating) {
        this.envRating = envRating;
    }

    /**
     * 返回餐厅的服务评分
     * 
     * @return 餐厅的服务评分
     */
    public String getServiceRating() {
        return this.serviceRating;
    }

    /**
     * 设置餐厅的服务评分
     * 
     * @param serviceRating
     *            餐厅的服务评分
     */
    public void setServiceRating(String serviceRating) {
        this.serviceRating = serviceRating;
    }

    /**
     * 返回餐厅的人均消费
     * 
     * @return 餐厅的人均消费
     */
    public String getCost() {
        return this.cost;
    }

    /**
     * 设置餐厅的人均消费
     * 
     * @param cost
     *            餐厅的人均消费
     */
    public void setCost(String cost) {
        this.cost = cost;
    }

    /**
     * 返回餐厅的特色菜
     * 
     * @return 餐厅的特色菜
     */
    public String getRecommend() {
        return this.recommend;
    }

    /**
     * 设置餐厅的特色菜
     * 
     * @param recommend
     *            餐厅的特色菜
     */
    public void setRecommend(String recommend) {
        this.recommend = recommend;
    }

    /**
     * 返回餐厅的氛围
     * 
     * @return 餐厅的氛围
     */
    public String getAtmosphere() {
        return this.atmosphere;
    }

    /**
     * 设置餐厅的氛围
     * 
     * @param atmosphere
     *            餐厅的氛围
     */
    public void setAtmosphere(String atmosphere) {
        this.atmosphere = atmosphere;
    }

    /**
     * 返回订餐的wap链接
     * 
     * @return 订餐的wap链接
     */
    public String getOrderingWapUrl() {
        return this.orderingWapUrl;
    }

    /**
     * 设置订餐的wap链接
     * 
     * @param orderingWapUrl
     *            订餐的wap链接
     */
    public void setOrderingWapUrl(String orderingWapUrl) {
        this.orderingWapUrl = orderingWapUrl;
    }

    /**
     * 返回订餐的web链接
     * 
     * @return 订餐的web链接
     */
    public String getOrderingWebUrl() {
        return this.orderingWebUrl;
    }

    /**
     * 设置订餐的web链接
     * 
     * @param orderingWebUrl
     *            订餐的web链接
     */
    public void setOrderingWebUrl(String orderingWebUrl) {
        this.orderingWebUrl = orderingWebUrl;
    }

    /**
     * 返回订餐的APP URI
     * 
     * @return 订餐的APP URI
     */
    public String getOrderingAppUrl() {
        return this.orderinAppUrl;
    }

    /**
     * 设置订餐的APP URI
     * 
     * @param orderinAppUrl
     *            订餐的APP URI
     */
    public void setOrderingAppUrl(String orderinAppUrl) {
        this.orderinAppUrl = orderinAppUrl;
    }

    /**
     * 返回规范格式的营业时间
     * 
     * @return 规范格式的营业时间
     */
    public String getOpentimeGDF() {
        return this.openTimeGDF;
    }

    /**
     * 设置规范格式的营业时间
     * 
     * @param openTimeGDF
     *            规范格式的营业时间
     */
    public void setOpentimeGDF(String openTimeGDF) {
        this.openTimeGDF = openTimeGDF;
    }

    /**
     * 返回非规范格式的营业时间
     * 
     * @return 非规范格式的营业时间
     */
    public String getOpenTime() {
        return this.openTime;
    }

    /**
     * 设置非规范格式的营业时间
     * 
     * @param openTime
     *            非规范格式的营业时间
     */
    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    /**
     * 返回餐厅的特色
     * 
     * @return 餐厅的特色
     */
    public String getAddition() {
        return this.addition;
    }

    /**
     * 设置餐厅的特色
     * 
     * @param addition
     *            餐厅的特色
     */
    public void setAddition(String addition) {
        this.addition = addition;
    }

    /**
     * 返回餐厅的图片信息
     * 
     * @return 餐厅的图片信息
     */
    public List<RemotePhoto> getPhotos() {
        return this.photoList;
    }

    /**
     * 设置餐厅的图片信息
     * 
     * @param photoList
     *            餐厅的图片信息
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
        if (!(object instanceof RemoteDining))
            return false;

        RemoteDining other = (RemoteDining) object;

        if (other.isMealOrdering != other.isMealOrdering)
            return false;

        if (this.addition == null) {
            if (other.addition != null)
                return false;
        } else if (!this.addition.equals(other.addition))
            return false;
        if (this.atmosphere == null) {
            if (other.atmosphere != null)
                return false;
        } else if (!this.atmosphere.equals(other.atmosphere))
            return false;
        if (this.cost == null) {
            if (other.cost != null)
                return false;
        } else if (!this.cost.equals(other.cost))
            return false;
        if (this.cpRating == null) {
            if (other.cpRating != null)
                return false;
        } else if (!this.cpRating.equals(other.cpRating))
            return false;
        if (this.cuisines == null) {
            if (other.cuisines != null)
                return false;
        } else if (!this.cuisines.equals(other.cuisines))
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
        if (this.orderinAppUrl == null) {
            if (other.orderinAppUrl != null)
                return false;
        } else if (!this.orderinAppUrl.equals(other.orderinAppUrl))
            return false;
        if (this.orderingWapUrl == null) {
            if (other.orderingWapUrl != null)
                return false;
        } else if (!this.orderingWapUrl.equals(other.orderingWapUrl))
            return false;
        if (this.orderingWebUrl == null) {
            if (other.orderingWebUrl != null)
                return false;
        } else if (!this.orderingWebUrl.equals(other.orderingWebUrl))
            return false;
        if (this.photoList == null) {
            if (other.photoList != null)
                return false;
        } else if (!this.photoList.equals(other.photoList))
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
        if (this.serviceRating == null) {
            if (other.serviceRating != null)
                return false;
        } else if (!this.serviceRating.equals(other.serviceRating))
            return false;
        if (this.tag == null) {
            if (other.tag != null)
                return false;
        } else if (!this.tag.equals(other.tag))
            return false;
        if (this.tasteRating == null) {
            if (other.tasteRating != null)
                return false;
        } else if (!this.tasteRating.equals(other.tasteRating))
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
                + (this.atmosphere == null ? 0 : this.atmosphere.hashCode());
        result = prime * result
                + (this.cost == null ? 0 : this.cost.hashCode());
        result = prime * result
                + (this.cpRating == null ? 0 : this.cpRating.hashCode());
        result = prime * result
                + (this.cuisines == null ? 0 : this.cuisines.hashCode());
        result = prime * result
                + (this.deepSrc == null ? 0 : this.deepSrc.hashCode());
        result = prime * result
                + (this.envRating == null ? 0 : this.envRating.hashCode());
        result = prime * result
                + (this.intro == null ? 0 : this.intro.hashCode());
        result = prime * result
                + (this.openTime == null ? 0 : this.openTime.hashCode());
        result = prime * result
                + (this.openTimeGDF == null ? 0 : this.openTimeGDF.hashCode());
        result = prime
                * result
                + (this.orderinAppUrl == null ? 0 : this.orderinAppUrl
                        .hashCode());
        result = prime
                * result
                + (this.orderingWapUrl == null ? 0 : this.orderingWapUrl
                        .hashCode());
        result = prime
                * result
                + (this.orderingWebUrl == null ? 0 : this.orderingWebUrl
                        .hashCode());
        result = prime * result
                + (this.rating == null ? 0 : this.rating.hashCode());
        result = prime * result
                + (this.recommend == null ? 0 : this.recommend.hashCode());
        result = prime
                * result
                + (this.serviceRating == null ? 0 : this.serviceRating
                        .hashCode());
        result = prime * result + (this.tag == null ? 0 : this.tag.hashCode());
        result = prime * result
                + (this.tasteRating == null ? 0 : this.tasteRating.hashCode());
        result = prime * result + (this.isMealOrdering ? 1231 : 1237);
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
        dest.writeByte((byte) (this.isMealOrdering ? 1 : 0));
        dest.writeString(this.addition);
        dest.writeString(this.atmosphere);
        dest.writeString(this.cost);
        dest.writeString(this.cpRating);
        dest.writeString(this.cuisines);
        dest.writeString(this.deepSrc);
        dest.writeString(this.envRating);
        dest.writeString(this.intro);
        dest.writeString(this.openTime);
        dest.writeString(this.openTimeGDF);
        dest.writeString(this.orderinAppUrl);
        dest.writeString(this.orderingWapUrl);
        dest.writeString(this.orderingWebUrl);
        dest.writeString(this.rating);
        dest.writeString(this.recommend);
        dest.writeString(this.serviceRating);
        dest.writeString(this.tag);
        dest.writeString(this.tasteRating);

        if (this.photoList != null) {
            dest.writeInt(1);
            dest.writeList(photoList);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteDining> CREATOR = new Creator<RemoteDining>() {

        @Override
        public RemoteDining createFromParcel(Parcel source) {
            RemoteDining dining = new RemoteDining();

            if (source.readByte() == 1)
                dining.isMealOrdering = true;
            else
                dining.isMealOrdering = false;

            dining.addition = source.readString();
            dining.atmosphere = source.readString();
            dining.cost = source.readString();
            dining.cpRating = source.readString();
            dining.cuisines = source.readString();
            dining.deepSrc = source.readString();
            dining.envRating = source.readString();
            dining.intro = source.readString();
            dining.openTime = source.readString();
            dining.openTimeGDF = source.readString();
            dining.orderinAppUrl = source.readString();
            dining.orderingWapUrl = source.readString();
            dining.orderingWebUrl = source.readString();
            dining.rating = source.readString();
            dining.recommend = source.readString();
            dining.serviceRating = source.readString();
            dining.tag = source.readString();
            dining.tasteRating = source.readString();

            if (source.readInt() != 0) {
                dining.photoList = source.readArrayList(RemotePhoto.class
                        .getClassLoader());
            }

            return dining;
        }

        @Override
        public RemoteDining[] newArray(int size) {
            return new RemoteDining[size];
        }
    };
}
