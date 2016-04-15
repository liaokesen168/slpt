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

package com.ingenic.iwds.smartlocation.search.route;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 定义了步行路径规划的一个方案
 */
public class RemoteWalkPath extends RemotePath implements Parcelable {

    private List<RemoteWalkStep> walkStepList = new ArrayList<RemoteWalkStep>();

    /**
     * RemoteWalkPath构造函数
     */
    public RemoteWalkPath() {
        super();
    }

    /**
     * RemoteWalkPath构造函数
     * 
     * @param path
     *            用于序列化实现的一个步行路径规划方案
     */
    public RemoteWalkPath(RemotePath path) {
        super(path);
    }

    /**
     * 返回步行方案的路段列表
     * 
     * @return 步行方案的路段列表
     */
    public List<RemoteWalkStep> getSteps() {
        return this.walkStepList;
    }

    /**
     * 设置步行方案的路段列表
     * 
     * @param walkStepList
     *            步行方案的路段列表
     */
    public void setSteps(List<RemoteWalkStep> walkStepList) {
        this.walkStepList = walkStepList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        if (this.walkStepList != null) {
            dest.writeInt(1);
            dest.writeList(this.walkStepList);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Creator<RemoteWalkPath> CREATOR = new Creator<RemoteWalkPath>() {

        @Override
        public RemoteWalkPath createFromParcel(Parcel source) {
            RemotePath path = RemotePath.CREATOR.createFromParcel(source);

            RemoteWalkPath walkPath = new RemoteWalkPath(path);

            if (source.readInt() != 0) {
                walkPath.walkStepList = source
                        .readArrayList(RemoteWalkStep.class.getClassLoader());
            }

            return walkPath;
        }

        @Override
        public RemoteWalkPath[] newArray(int size) {
            return new RemoteWalkPath[size];
        }

    };

}
