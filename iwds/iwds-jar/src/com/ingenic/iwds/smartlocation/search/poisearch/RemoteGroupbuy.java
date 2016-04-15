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
import java.util.Date;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 定义一个POI（Point Of Interest，兴趣点）一条团购信息
 */
public class RemoteGroupbuy implements Parcelable {
    private int count;
    private String detail;
    private float dicount;
    // private Date endTime;
    private float groupBuyPrice;
    private float originalPrice;
    private List<RemotePhoto> photoList = new ArrayList<RemotePhoto>();
    private String provider;
    private int soldCount;
    // private Date startTime;
    private String ticketAddress;
    private String ticketTel;
    private String typeCode;
    private String typeDes;
    private String url;

    /**
     * RemoteGroupbuy构造函数
     */
    public RemoteGroupbuy() {

    }

    /**
     * 返回团购的分类代码。 分类代码有“01”（美食天下）、“02”（生活服务）、
     * “03”（休闲娱乐）、“04”（酒店旅游）、“05”（网购精品）、“06”（其他团购）
     * 
     * @return 团购的分类代码
     */
    public String getTypeCode() {
        return this.typeCode;
    }

    /**
     * 设置团购的分类代码
     * 
     * @param typeCode
     *            团购的分类代码
     */
    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    /**
     * 返回团购分类名称
     * 
     * @return 团购分类名称
     */
    public String getTypeDes() {
        return this.typeDes;
    }

    /**
     * 设置团购分类名称
     * 
     * @param typeDesc
     *            团购分类名称
     */
    public void setTypeDes(String typeDesc) {
        this.typeDes = typeDesc;
    }

    /**
     * 返回团购的详细信息
     * 
     * @return 团购的详细信息
     */
    public String getDetail() {
        return this.detail;
    }

    /**
     * 设置团购的详细信息
     * 
     * @param detail
     *            团购的详细信息
     */
    public void setDetail(String detail) {
        this.detail = detail;
    }

    // public Date getStartTime() {
    // if (this.startTime == null)
    // return null;
    //
    // return (Date) this.startTime.clone();
    // }
    //
    // public void setStartTime(Date startTime) {
    // if (startTime == null) {
    // this.startTime = null;
    // } else {
    // this.startTime = (Date) startTime.clone();
    // }
    // }
    //
    // public Date getEndTime() {
    // if (this.endTime == null)
    // return null;
    //
    // return (Date) this.endTime.clone();
    // }

    // public void setEndTime(Date endTime) {
    // if (endTime == null) {
    // this.endTime = null;
    // } else {
    // this.endTime = (Date) endTime.clone();
    // }
    // }

    /**
     * 返回团购的总数量
     * 
     * @return 团购的总数量
     */
    public int getCount() {
        return this.count;
    }

    /**
     * 设置团购的总数量
     * 
     * @param count
     *            团购的总数量
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * 返回团购的已销售数量
     * 
     * @return 团购的已销售数量
     */
    public int getSoldCount() {
        return this.soldCount;
    }

    /**
     * 设置团购的已销售数量
     * 
     * @param soldCount
     *            团购的已销售数量
     */
    public void setSoldCount(int soldCount) {
        this.soldCount = soldCount;
    }

    /**
     * 返回团购的商品原价
     * 
     * @return 团购的商品原价
     */
    public float getOriginalPrice() {
        return this.originalPrice;
    }

    /**
     * 设置团购的商品原价
     * 
     * @param originalPrice
     *            团购的商品原价
     */
    public void setOriginalPrice(float originalPrice) {
        this.originalPrice = originalPrice;
    }

    /**
     * 返回商品的团购价格
     * 
     * @return 商品的团购价格
     */
    public float getGroupbuyPrice() {
        return this.groupBuyPrice;
    }

    /**
     * 设置商品的团购价格
     * 
     * @param groupBuyPrice
     *            商品的团购价格
     */
    public void setGroupbuyPrice(float groupBuyPrice) {
        this.groupBuyPrice = groupBuyPrice;
    }

    /**
     * 返回团购的商品折扣
     * 
     * @return 团购的商品折扣
     */
    public float getDiscount() {
        return this.dicount;
    }

    /**
     * 设置团购的商品折扣
     * 
     * @param discount
     *            团购的商品折扣
     */
    public void setDiscount(float discount) {
        this.dicount = discount;
    }

    /**
     * 返回团购商品的取票地址
     * 
     * @return 团购商品的取票地址
     */
    public String getTicketAddress() {
        return this.ticketAddress;
    }

    /**
     * 设置团购商品的取票地址
     * 
     * @param ticketAddress
     *            团购商品的取票地址
     */
    public void setTicketAddress(String ticketAddress) {
        this.ticketAddress = ticketAddress;
    }

    /**
     * 返回团购商品的取票电话
     * 
     * @return 团购商品的取票电话
     */
    public String getTicketTel() {
        return this.ticketTel;
    }

    /**
     * 设置团购商品的取票电话
     * 
     * @param ticketTel
     *            团购商品的取票电话
     */
    public void setTicketTel(String ticketTel) {
        this.ticketTel = ticketTel;
    }

    /**
     * 返回团购商品的图片信息
     * 
     * @return 团购商品的图片信息
     */
    public List<RemotePhoto> getPhotos() {
        return this.photoList;
    }

    /**
     * 添加团购商品的图片信息
     * 
     * @param photo
     *            团购商品的图片信息
     */
    public void addPhotos(RemotePhoto photo) {
        this.photoList.add(photo);
    }

    /**
     * 初始化并清空团购商品的图片信息列表
     * 
     * @param photoList
     *            团购商品的图片信息列表
     */
    public void initPhotos(List<RemotePhoto> photoList) {
        if ((photoList == null) || (photoList.size() == 0))
            return;
        this.photoList.clear();
        for (RemotePhoto photo : photoList) {
            this.photoList.add(photo);
        }
    }

    /**
     * 返回团购商品的来源url
     * 
     * @return 团购商品的来源url
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * 设置团购商品的来源url
     * 
     * @param url
     *            团购商品的来源url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 返回团购商品的提供商
     * 
     * @return 团购商品的提供商
     */
    public String getProvider() {
        return this.provider;
    }

    /**
     * 设置团购商品的提供商
     * 
     * @param provider
     *            团购商品的提供商
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (!(object instanceof RemoteGroupbuy))
            return false;

        RemoteGroupbuy other = (RemoteGroupbuy) object;

        if (this.count != other.count)
            return false;
        if (this.detail == null) {
            if (other.detail != null)
                return false;
        } else if (!this.detail.equals(other.detail))
            return false;
        if (Float.floatToIntBits(this.dicount) != Float
                .floatToIntBits(other.dicount))
            return false;
        if (Float.floatToIntBits(this.groupBuyPrice) != Float
                .floatToIntBits(other.groupBuyPrice))
            return false;
        if (Float.floatToIntBits(this.originalPrice) != Float
                .floatToIntBits(other.originalPrice))
            return false;
        if (this.provider == null) {
            if (other.provider != null)
                return false;
        } else if (!this.provider.equals(other.provider))
            return false;
        if (this.soldCount != other.soldCount)
            return false;
        if (this.ticketAddress == null) {
            if (other.ticketAddress != null)
                return false;
        } else if (!this.ticketAddress.equals(other.ticketAddress))
            return false;
        if (this.ticketTel == null) {
            if (other.ticketTel != null)
                return false;
        } else if (!this.ticketTel.equals(other.ticketTel))
            return false;
        if (this.typeCode == null) {
            if (other.typeCode != null)
                return false;
        } else if (!this.typeCode.equals(other.typeCode))
            return false;
        if (this.typeDes == null) {
            if (other.typeDes != null)
                return false;
        } else if (!this.typeDes.equals(other.typeDes))
            return false;
        if (this.url == null) {
            if (other.url != null)
                return false;
        } else if (!this.url.equals(other.url))
            return false;
        if (this.photoList == null) {
            if (other.photoList != null)
                return false;
        } else if (!this.photoList.equals(other.photoList))
            return false;
        // if (this.startTime == null) {
        // if (other.startTime != null)
        // return false;
        // } else if (!this.startTime.equals(other.startTime))
        // return false;
        // if (this.endTime == null) {
        // if (other.endTime != null)
        // return false;
        // } else if (!this.endTime.equals(other.endTime))
        // return false;

        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = prime * result + this.count;
        result = prime * result
                + (this.detail == null ? 0 : this.detail.hashCode());
        result = prime * result + Float.floatToIntBits(this.dicount);
        result = prime * result + Float.floatToIntBits(this.groupBuyPrice);
        result = prime * result + Float.floatToIntBits(this.originalPrice);
        result = prime * result
                + (this.provider == null ? 0 : this.provider.hashCode());
        result = prime * result + this.soldCount;
        result = prime
                * result
                + (this.ticketAddress == null ? 0 : this.ticketAddress
                        .hashCode());
        result = prime * result
                + (this.ticketTel == null ? 0 : this.ticketTel.hashCode());
        result = prime * result
                + (this.typeCode == null ? 0 : this.typeCode.hashCode());
        result = prime * result
                + (this.typeDes == null ? 0 : this.typeDes.hashCode());
        result = prime * result
                + (this.photoList == null ? 0 : this.photoList.hashCode());
        // result = prime * result
        // + (this.startTime == null ? 0 : this.startTime.hashCode());
        // result = prime * result
        // + (this.endTime == null ? 0 : this.endTime.hashCode());

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.count);
        dest.writeString(this.detail);
        dest.writeFloat(this.dicount);
        dest.writeFloat(this.groupBuyPrice);
        dest.writeFloat(this.originalPrice);
        dest.writeString(this.provider);
        dest.writeInt(this.soldCount);
        dest.writeString(this.ticketAddress);
        dest.writeString(this.ticketTel);
        dest.writeString(this.typeCode);
        dest.writeString(this.typeDes);
        dest.writeString(this.url);

        if (this.photoList != null) {
            dest.writeInt(1);
            dest.writeList(this.photoList);
        } else {
            dest.writeInt(0);
        }

        // if (this.startTime != null) {
        // dest.writeInt(1);
        // dest.writeSerializable(this.startTime);
        // } else {
        // dest.writeInt(0);
        // }

        // if (this.endTime != null) {
        // dest.writeInt(1);
        // dest.writeSerializable(this.endTime);
        // } else {
        // dest.writeInt(0);
        // }
    }

    public static final Creator<RemoteGroupbuy> CREATOR = new Creator<RemoteGroupbuy>() {

        @Override
        public RemoteGroupbuy createFromParcel(Parcel source) {
            RemoteGroupbuy groupBuy = new RemoteGroupbuy();

            groupBuy.count = source.readInt();
            groupBuy.detail = source.readString();
            groupBuy.dicount = source.readFloat();
            groupBuy.groupBuyPrice = source.readFloat();
            groupBuy.originalPrice = source.readFloat();
            groupBuy.provider = source.readString();
            groupBuy.soldCount = source.readInt();
            groupBuy.ticketAddress = source.readString();
            groupBuy.ticketTel = source.readString();
            groupBuy.typeCode = source.readString();
            groupBuy.typeDes = source.readString();
            groupBuy.url = source.readString();

            if (source.readInt() != 0) {
                groupBuy.photoList = source.readArrayList(RemotePhoto.class
                        .getClassLoader());
            }

            // if (source.readInt() != 0) {
            // groupBuy.startTime = (Date) source.readSerializable();
            // }
            //
            // if (source.readInt() != 0)
            // groupBuy.endTime = (Date) source.readSerializable();
            //
            return groupBuy;
        }

        @Override
        public RemoteGroupbuy[] newArray(int size) {
            return new RemoteGroupbuy[size];
        }
    };
}
