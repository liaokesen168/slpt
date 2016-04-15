/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  Zhouzhiqiang <zhiqiang.zhou@ingenic.com>
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

package com.ingenic.iwds.smartspeech.business;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * restaurant中shop节点的抽象，表示商户的信息
 */
public class RemoteShopDatasource implements Parcelable {

    /**
     * id元素节点名，表示商户的唯一标识符 
     */
    public static final String ATTRID = "id";

    /**
     * name元素节点名，表示商户名称
     */
    public static final String ATTRNAME = "name";

    /**
     * branch_name元素节点名，表示商户分店名称
     */
    public static final String RAWBRANCHNAME = "branch_name";

    /**
     * phone_numbers元素节点名，表示商户所有电话号码（可能有0到多个number元素）
     */
    public static final String RAWPHONENUMBERS = "phone_numbers";

    /**
     * number元素节点名，表示商户的电话号码
     */
    public static final String RAWPHONENUMBER = "number";

    /**
     * address元素节点名，表示商户地址
     */
    public static final String RAWADDRESS = "address";

    /**
     * avg_price元素节点名，表示人均消费（单位：元）
     */
    public static final String RAWAVGPRICE = "avg_price";

    /**
     * category元素节点名，表示商户分类（火锅、川菜等）
     */
    public static final String RAWCATEGORY = "category";

    /**
     * latitude元素节点名，表示该商户地理位置纬度
     */
    public static final String RAWLATITUDE = "latitude";

    /**
     * longitude元素节点名，表示该商户地理位置经度
     */
    public static final String RAWLONGITUDE = "longitude";

    /**
     * url元素节点名，表示使用网页搜索该商户地点的url
     */
    public static final String RAWURL = "url";

    /**
     * distance元素节点名，表示距离用户所在地点的距离（单位：米）
     */
    public static final String RAWDISTANCE = "distance";

    /**
     * score元素节点名，表示商户评分，取值范围0～100
     */
    public static final String RAWSCORE = "score";

    /**
     * score_text元素节点名，表示分数描述
     */
    public static final String RAWSCORETEXT = "score_text";

    /**
     * pic元素节点名，表示商户图片url
     */
    public static final String RAWRPIC = "pic";

    /**
     * dish_tags元素节点名，表示网友推荐菜（多个标签以“|”分隔）
     */
    public static final String RAWDISHTAGS = "dish_tags";

    /**
     * shop_tags元素节点名，表示商户标签（多个标签以“|”分隔）
     */
    public static final String RAWSHOPTAGS = "shop_tags";

    /**
     * id元素节点的值
     */
    public String mId = null;

    /**
     * name元素节点的值
     */
    public String mName = null;

    /**
     * branch_name元素节点的值
     */
    public String mBranchName = null;

    /**
     * number元素节点链表
     */
    public List<String> mPhoneNumbers = null;

    /**
     * category元素节点的值
     */
    public String mCategory = null;

    /**
     * avg_price元素节点的值
     */
    public String mAvgPrice = null;

    /**
     * address元素节点的值
     */
    public String mAddress = null;

    /**
     * latitude元素节点的值
     */
    public String mLatitude = null;

    /**
     * longitude元素节点的值
     */
    public String mLongitude = null;

    /**
     * url元素节点的值
     */
    public String mUrl = null;

    /**
     * distance元素节点的值
     */
    public String mDistance = null;

    /**
     * score元素节点的值
     */
    public String mScore = null;

    /**
     * score_text元素节点的值
     */
    public String mScoreText = null;

    /**
     * pic元素节点的值
     */
    public String mPic = null;

    /**
     * dish_tags元素节点的值
     */
    public String mDishTags = null;

    /**
     * shop_tags元素节点的值
     */
    public String mShopTags = null;

    @Override
    public String toString() {
        return "ShopDatasource [branch_name=" + mBranchName
                + ", phone_numbers=" + mPhoneNumbers + ", category="
                + mCategory + ", avg_price=" + mAvgPrice + ", address="
                + mAddress + ", latitude=" + mLatitude + ", longitude="
                + mLongitude + ", url=" + mUrl + ", distance=" + mDistance
                + ", score=" + mScore + ", score_text=" + mScoreText + ", pic="
                + mPic + ", dish_tags=" + mDishTags + ", shop_tags="
                + mShopTags + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mId);
        arg0.writeString(mName);
        arg0.writeString(mBranchName);
        arg0.writeList(mPhoneNumbers);
        arg0.writeString(mCategory);
        arg0.writeString(mAvgPrice);
        arg0.writeString(mAddress);
        arg0.writeString(mLatitude);
        arg0.writeString(mLongitude);
        arg0.writeString(mUrl);
        arg0.writeString(mDistance);
        arg0.writeString(mScore);
        arg0.writeString(mScoreText);
        arg0.writeString(mPic);
        arg0.writeString(mDishTags);
        arg0.writeString(mShopTags);
    }

    public static final Creator<RemoteShopDatasource> CREATOR = new Creator<RemoteShopDatasource>() {
        @Override
        public RemoteShopDatasource createFromParcel(Parcel source) {
            RemoteShopDatasource info = new RemoteShopDatasource();
            info.mId = source.readString();
            info.mName = source.readString();
            info.mBranchName = source.readString();
            info.mPhoneNumbers = new ArrayList<String>();
            source.readList(info.mPhoneNumbers, String.class.getClassLoader());
            info.mCategory = source.readString();
            info.mAvgPrice = source.readString();
            info.mAddress = source.readString();
            info.mLatitude = source.readString();
            info.mLongitude = source.readString();
            info.mUrl = source.readString();
            info.mDistance = source.readString();
            info.mScore = source.readString();
            info.mScoreText = source.readString();
            info.mPic = source.readString();
            info.mDishTags = source.readString();
            info.mShopTags = source.readString();
            return info;
        }

        @Override
        public RemoteShopDatasource[] newArray(int size) {
            return new RemoteShopDatasource[size];
        }
    };

}
