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

import com.ingenic.iwds.smartlocation.search.core.RemoteLatLonPoint;
import com.ingenic.iwds.smartlocation.search.core.RemotePoiItem;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 定义一个POI（Point Of Interest，兴趣点）的详细信息。 详细信息将逐渐扩充，目前有团购信息与优惠信息
 */
public class RemotePoiItemDetail extends RemotePoiItem implements Parcelable {
    private RemoteCinema cinema;
    private DeepType deepType;
    private RemoteDining dining;
    private List<RemoteDiscount> discountList = new ArrayList<RemoteDiscount>();
    private List<RemoteGroupbuy> groupBuyList = new ArrayList<RemoteGroupbuy>();
    private RemoteHotel hotel;
    private RemoteScenic scenic;

    /**
     * POI深度信息类型
     */
    public static enum DeepType {
        /**
         * 未知深度信息类型
         */
        UNKNOWN,

        /**
         * 餐饮深度信息类型
         */
        DINING,

        /**
         * 酒店深度信息类型
         */
        HOTEL,

        /**
         * 影院深度信息类型
         */
        CINEMA,

        /**
         * 景点深度信息类型
         */
        SCENIC;

        private DeepType() {

        }
    }

    /**
     * RemotePoiItemDetail构造函数
     */
    public RemotePoiItemDetail() {
        super();
    }

    /**
     * RemotePoiItemDetail构造函数
     * 
     * @param poiId
     *            POI的ID
     * 
     * @param point
     *            POI的经纬度坐标
     * 
     * @param title
     *            POI的名称
     * 
     * @param snippet
     *            POI的地址
     */
    public RemotePoiItemDetail(String poiId, RemoteLatLonPoint point,
            String title, String snippet) {
        super(poiId, point, title, snippet);
    }

    /**
     * 返回结果的团购信息列表
     * 
     * @return 结果的团购信息列表
     */
    public List<RemoteGroupbuy> getGroupbuys() {
        return this.groupBuyList;
    }

    /**
     * 初始化团购信息列表
     * 
     * @param groupBuyList
     */
    public void initGroupbuys(List<RemoteGroupbuy> groupBuyList) {
        if ((groupBuyList == null) || (groupBuyList.size() == 0))
            return;

        for (RemoteGroupbuy groupBuy : groupBuyList) {
            this.groupBuyList.add(groupBuy);
        }
    }

    /**
     * 添加团购信息
     * 
     * @param groupBuy
     *            团购信息
     */
    public void addGroupbuy(RemoteGroupbuy groupBuy) {
        this.groupBuyList.add(groupBuy);
    }

    /**
     * 返回结果的优惠信息列表
     * 
     * @return 结果的优惠信息列表
     */
    public List<RemoteDiscount> getDiscounts() {
        return this.discountList;
    }

    /**
     * 初始化优惠信息列表
     * 
     * @param discountList
     *            优惠信息列表
     */
    public void initDiscounts(List<RemoteDiscount> discountList) {
        if ((discountList == null) || (discountList.size() == 0))
            return;
        for (RemoteDiscount discount : discountList) {
            this.discountList.add(discount);
        }
    }

    /**
     * 添加优惠信息
     * 
     * @param discount
     *            优惠信息
     */
    public void addDiscount(RemoteDiscount discount) {
        this.discountList.add(discount);
    }

    /**
     * 返回POI深度信息类型。 目前深度信息有四种类型，如果没有或者不是此四种深度信息，此方法返回未知深度信息
     * 
     * @return POI深度信息的类型。目前有四种类型。 餐饮：Dining；酒店：Hotel；影院：Cinema；景点：Scenic
     */
    public DeepType getDeepType() {
        return this.deepType;
    }

    /**
     * 设置POI深度信息类型
     * 
     * @param deepType
     *            POI深度信息的类型。目前有四种类型。 餐饮：Dining；酒店：Hotel；影院：Cinema；景点：Scenic
     */
    public void setDeepType(DeepType deepType) {
        this.deepType = deepType;
    }

    /**
     * 返回POI餐饮的深度信息
     * 
     * @return POI餐饮的深度信息
     */
    public RemoteDining getDining() {
        return this.dining;
    }

    /**
     * 设置POI餐饮的深度信息
     * 
     * @param dining
     *            POI餐饮的深度信息
     */
    public void setDining(RemoteDining dining) {
        this.dining = dining;
    }

    /**
     * 返回POI酒店的深度信息
     * 
     * @return POI酒店的深度信息
     */
    public RemoteHotel getHotel() {
        return this.hotel;
    }

    /**
     * 设置POI酒店的深度信息
     * 
     * @param hotel
     *            POI酒店的深度信息
     */
    public void setHotel(RemoteHotel hotel) {
        this.hotel = hotel;
    }

    /**
     * 返回POI影院的深度信息
     * 
     * @return POI影院的深度信息
     */
    public RemoteCinema getCinema() {
        return this.cinema;
    }

    /**
     * 设置POI影院的深度信息
     * 
     * @param cinema
     *            POI影院的深度信息
     */
    public void setCinema(RemoteCinema cinema) {
        this.cinema = cinema;
    }

    /**
     * 返回POI景点的深度信息
     * 
     * @return POI景点的深度信息
     */
    public RemoteScenic getScenic() {
        return this.scenic;
    }

    /**
     * 设置POI景点的深度信息
     * 
     * @param scenic
     *            POI景点的深度信息
     */
    public void setScenic(RemoteScenic scenic) {
        this.scenic = scenic;
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
        if (!super.equals(object))
            return false;
        if (!(object instanceof RemotePoiItemDetail))
            return false;

        RemotePoiItemDetail other = (RemotePoiItemDetail) object;

        if (this.cinema == null) {
            if (other.cinema != null)
                return false;
        } else if (!this.cinema.equals(other.cinema))
            return false;
        if (this.deepType == null) {
            if (other.deepType != null)
                return false;
        } else if (!this.deepType.equals(other.deepType))
            return false;
        if (this.dining == null) {
            if (other.dining != null)
                return false;
        } else if (!this.dining.equals(other.dining))
            return false;
        if (this.discountList == null) {
            if (other.discountList != null)
                return false;
        } else if (!this.discountList.equals(other.discountList))
            return false;
        if (this.groupBuyList == null) {
            if (other.groupBuyList != null)
                return false;
        } else if (!this.groupBuyList.equals(other.groupBuyList))
            return false;
        if (this.hotel == null) {
            if (other.hotel != null)
                return false;
        } else if (!this.hotel.equals(other.hotel))
            return false;
        if (this.scenic == null) {
            if (other.scenic != null)
                return false;
        } else if (!this.scenic.equals(other.scenic))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = prime * result
                + (this.cinema == null ? 0 : this.cinema.hashCode());
        result = prime * result
                + (this.deepType == null ? 0 : this.deepType.hashCode());
        result = prime * result
                + (this.dining == null ? 0 : this.dining.hashCode());
        result = prime
                * result
                + (this.discountList == null ? 0 : this.discountList.hashCode());
        result = prime
                * result
                + (this.groupBuyList == null ? 0 : this.groupBuyList.hashCode());
        result = prime * result
                + (this.hotel == null ? 0 : this.hotel.hashCode());
        result = prime * result
                + (this.scenic == null ? 0 : this.scenic.hashCode());

        return result;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        if (this.cinema != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.cinema, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.dining != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.dining, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.hotel != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.hotel, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.scenic != null) {
            dest.writeInt(1);
            dest.writeParcelable(this.scenic, flags);
        } else {
            dest.writeInt(0);
        }

        if (this.deepType != null) {
            dest.writeInt(1);
            switch (this.deepType) {
            case DINING:
                dest.writeInt(0);
                break;
            case HOTEL:
                dest.writeInt(1);
                break;
            case CINEMA:
                dest.writeInt(2);
                break;
            case SCENIC:
                dest.writeInt(3);
                break;
            case UNKNOWN:
            default:
                dest.writeInt(4);
                break;
            }
        } else {
            dest.writeInt(0);
        }

        if (this.discountList != null) {
            dest.writeInt(1);
            dest.writeList(this.discountList);
        } else {
            dest.writeInt(0);
        }

        if (this.groupBuyList != null) {
            dest.writeInt(1);
            dest.writeList(this.groupBuyList);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemotePoiItemDetail> CREATOR = new Creator<RemotePoiItemDetail>() {

        @Override
        public RemotePoiItemDetail createFromParcel(Parcel source) {
            RemotePoiItem item = RemotePoiItem.CREATOR.createFromParcel(source);

            RemotePoiItemDetail poiItemDetail = new RemotePoiItemDetail();

            poiItemDetail.setAdCode(item.getAdCode());
            poiItemDetail.setAdName(item.getAdName());
            poiItemDetail.setCityCode(item.getCityCode());
            poiItemDetail.setCityName(item.getCityName());
            poiItemDetail.setDirection(item.getDirection());
            poiItemDetail.setDiscountInfo(item.isDiscountInfo());
            poiItemDetail.setDistance(item.getDistance());
            poiItemDetail.setEmail(item.getEmail());
            poiItemDetail.setEnter(item.getEnter());
            poiItemDetail.setExit(item.getExit());
            poiItemDetail.setGroupBuyInfo(item.isGroupBuyInfo());
            poiItemDetail.setIndoormap(item.isIndoorMap());
            poiItemDetail.setLatLonPoint(item.getLatLonPoint());
            poiItemDetail.setPoiId(item.getPoiId());
            poiItemDetail.setPostCode(item.getPostCode());
            poiItemDetail.setProvinceCode(item.getProvinceCode());
            poiItemDetail.setProvinceName(item.getProvinceName());
            poiItemDetail.setSnippet(item.getSnippet());
            poiItemDetail.setTel(item.getTel());
            poiItemDetail.setTypeDes(item.getTypeDes());
            poiItemDetail.setWebSite(item.getWebSize());
            poiItemDetail.setTitle(item.getTitle());

            if (source.readInt() != 0)
                poiItemDetail.cinema = source.readParcelable(RemoteCinema.class
                        .getClassLoader());

            if (source.readInt() != 0)
                poiItemDetail.dining = source.readParcelable(RemoteDining.class
                        .getClassLoader());

            if (source.readInt() != 0)
                poiItemDetail.hotel = source.readParcelable(RemoteHotel.class
                        .getClassLoader());

            if (source.readInt() != 0)
                poiItemDetail.scenic = source.readParcelable(RemoteScenic.class
                        .getClassLoader());

            if (source.readInt() != 0) {
                switch (source.readInt()) {
                case 0:
                    poiItemDetail.deepType = DeepType.DINING;
                    break;
                case 1:
                    poiItemDetail.deepType = DeepType.HOTEL;
                    break;
                case 2:
                    poiItemDetail.deepType = DeepType.CINEMA;
                    break;
                case 3:
                    poiItemDetail.deepType = DeepType.SCENIC;
                    break;
                case 4:
                    poiItemDetail.deepType = DeepType.UNKNOWN;
                default:
                    break;
                }
            }

            if (source.readInt() != 0) {
                poiItemDetail.discountList = source
                        .readArrayList(RemoteDiscount.class.getClassLoader());
            }

            if (source.readInt() != 0) {
                poiItemDetail.groupBuyList = source
                        .readArrayList(RemoteGroupbuy.class.getClassLoader());
            }

            return poiItemDetail;
        }

        @Override
        public RemotePoiItemDetail[] newArray(int size) {
            return new RemotePoiItemDetail[size];
        }
    };
}