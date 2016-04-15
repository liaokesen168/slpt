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

package com.ingenic.iwds.smartlocation.search.help;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 定义Tip的类
 */
public class RemoteTip implements Parcelable {
    private String adCode;
    private String district;
    private String name;

    /**
     * RemoteTip构造函数
     */
    public RemoteTip() {

    }

    /**
     * 返回提示名称
     * 
     * @return 提示名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置提示名称
     * 
     * @param name
     *            提示名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 返回提示区域
     * 
     * @return 提示区域
     */
    public String getDistrict() {
        return this.district;
    }

    /**
     * 设置提示区域
     * 
     * @param district
     *            提示区域
     */
    public void setDistrict(String district) {
        this.district = district;
    }

    /**
     * 返回提示区域编码
     * 
     * @return 提示区域编码
     */
    public String getAdCode() {
        return this.adCode;
    }

    /**
     * 设置提示区域编码
     * 
     * @param adCode
     *            提示区域编码
     */
    public void setAdCode(String adCode) {
        this.adCode = adCode;
    }

    @Override
    public String toString() {
        return "RemoteTip [" + "name=" + this.name + ", district="
                + this.district + ", adCode=" + this.adCode + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.adCode);
        dest.writeString(this.district);
        dest.writeString(this.name);
    }

    public static final Creator<RemoteTip> CREATOR = new Creator<RemoteTip>() {

        @Override
        public RemoteTip createFromParcel(Parcel source) {
            RemoteTip tip = new RemoteTip();

            tip.adCode = source.readString();
            tip.district = source.readString();
            tip.name = source.readString();

            return tip;
        }

        @Override
        public RemoteTip[] newArray(int size) {
            return new RemoteTip[size];
        }

    };
}
