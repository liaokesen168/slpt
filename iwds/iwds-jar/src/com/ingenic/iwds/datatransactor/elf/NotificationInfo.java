/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  SunWenZhong(Fighter) <wenzhong.sun@ingenic.com, wanmyqawdr@126.com>
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

package com.ingenic.iwds.datatransactor.elf;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 通知信息类.
 */
public class NotificationInfo implements Parcelable {

    /** 唯一标识该条消息 (无需传送). */
    public long id;

    /** 应用名. */
    public String appName;

    /** 应用的包名. */
    public String packageName;

    /** 标题. */
    public String title;

    /** 内容. */
    public String content;

    /** 发布时间. */
    public long updateTime;

    /** 是否已读（0已读，1未读，无需传送). */
    public int read;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appName);
        dest.writeString(packageName);
        dest.writeString(title);
        dest.writeString(content);

        dest.writeLong(updateTime);
    }

    public static final Creator<NotificationInfo> CREATOR = new Creator<NotificationInfo>() {
        @Override
        public NotificationInfo createFromParcel(Parcel source) {
            NotificationInfo info = new NotificationInfo();

            info.id = -1;

            info.appName = source.readString();
            info.packageName = source.readString();
            info.title = source.readString();
            info.content = source.readString();

            info.updateTime = source.readLong();

            info.read = -1;

            return info;
        }

        @Override
        public NotificationInfo[] newArray(int size) {
            return new NotificationInfo[size];
        }
    };

    @Override
    public String toString() {
        return "NotificationInfo [id=" + id + ", appName=" + appName
                + ", packageName=" + packageName + ", title=" + title
                + ", content=" + content + ", updateTime=" + updateTime
                + ", read=" + read + "]";
    }
}