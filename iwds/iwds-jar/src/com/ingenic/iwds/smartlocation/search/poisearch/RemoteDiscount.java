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
 * 定义一个POI（Point Of Interest，兴趣点）一条优惠信息
 */
public class RemoteDiscount implements Parcelable {
    private String detail;
    // private Date endTime;
    private List<RemotePhoto> photoList = new ArrayList<RemotePhoto>();
    private String provider;
    private int soldCount;
    // private Date startTime;
    private String title;
    private String url;

    /**
     * RemoteDiscount构造函数
     */
    public RemoteDiscount() {

    }

    /**
     * 返回优惠信息的标题
     * 
     * @return 优惠信息的标题
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * 设置优惠信息的标题
     * 
     * @param title
     *            优惠信息的标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 返回优惠的详细信息
     * 
     * @return 优惠的详细信息
     */
    public String getDetail() {
        return this.detail;
    }

    /**
     * 设置优惠的详细信息
     * 
     * @param detail
     *            优惠的详细信息
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

    // public void setStartTime(Date startTime) {
    // if (startTime == null) {
    // this.startTime = null;
    // } else {
    // this.startTime = (Date) startTime.clone();
    // }
    // }

    // public Date getEndTime() {
    // if (this.endTime == null)
    // return null;
    //
    // return (Date) this.endTime.clone();
    // }
    //
    // public void setEndTime(Date endTime) {
    // if (endTime == null) {
    // this.endTime = null;
    // } else {
    // this.endTime = (Date) endTime.clone();
    // }
    // }

    /**
     * 返回优惠的已销售数量
     * 
     * @return 优惠的已销售数量
     */
    public int getSoldCount() {
        return this.soldCount;
    }

    /**
     * 设置优惠的已销售数量
     * 
     * @param soldCount
     *            优惠的已销售数量
     */
    public void setSoldCount(int soldCount) {
        this.soldCount = soldCount;
    }

    /**
     * 返回优惠的图片信息
     * 
     * @return 优惠的图片信息
     */
    public List<RemotePhoto> getPhotos() {
        return this.photoList;
    }

    /**
     * 设置优惠的图片信息
     * 
     * @param photo
     *            优惠的图片信息
     */
    public void addPhotos(RemotePhoto photo) {
        this.photoList.add(photo);
    }

    /**
     * 初始化并清空图片信息列表
     * 
     * @param photoList
     *            图片信息列表
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
     * 返回优惠商品的来源url
     * 
     * @return 优惠商品的来源url
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * 设置优惠商品的来源url
     * 
     * @param url
     *            优惠商品的来源url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 返回优惠商品的提供商
     * 
     * @return 优惠商品的提供商
     */
    public String getProvider() {
        return this.provider;
    }

    /**
     * 设置优惠商品的提供商
     * 
     * @param provider
     *            优惠商品的提供商
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.detail);
        dest.writeString(this.provider);
        dest.writeString(this.title);
        dest.writeString(this.url);
        dest.writeInt(this.soldCount);

        if (this.photoList != null) {
            dest.writeInt(1);
            dest.writeList(this.photoList);
        } else {
            dest.writeInt(0);
        }

        // if (this.startTime != null) {
        // dest.writeInt(1);
        // dest.writeSerializable(this.startTime);
        // ;
        // } else {
        // dest.writeInt(0);
        // }
        //
        // if (this.endTime != null) {
        // dest.writeInt(1);
        // dest.writeSerializable(this.endTime);
        // } else {
        // dest.writeInt(0);
        // }
    }

    public static final Creator<RemoteDiscount> CREATOR = new Creator<RemoteDiscount>() {

        @Override
        public RemoteDiscount createFromParcel(Parcel source) {
            RemoteDiscount discount = new RemoteDiscount();

            discount.detail = source.readString();
            discount.provider = source.readString();
            discount.title = source.readString();
            discount.url = source.readString();
            discount.soldCount = source.readInt();

            if (source.readInt() != 0) {
                discount.photoList = source.readArrayList(RemotePhoto.class
                        .getClassLoader());
            }

            // if (source.readInt() != 0) {
            // discount.startTime = (Date) source.readSerializable();
            // }
            //
            // if (source.readInt() != 0) {
            // discount.endTime = (Date) source.readSerializable();
            // }

            return discount;
        }

        @Override
        public RemoteDiscount[] newArray(int size) {
            return new RemoteDiscount[size];
        }

    };

}
