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

import java.util.Arrays;

import com.ingenic.iwds.utils.IwdsAssert;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 健康信息类.
 */
public class HealthInfo implements Parcelable {

    /** 描述运动状态： 静放 */
    public static final int MOTION_RESET = 0;

    /** 描述运动状态： 运动停止 */
    public static final int MOTION_STOP = 1;

    /** 描述运动状态： 正在走路 */
    public static final int MOTION_WALK = 2;

    /** 描述运动状态： 正在跑步 */
    public static final int MOTION_RUN = 3;

    /** 描述运动状态： 正在车上（单车，公交，开车） */
    public static final int MOTION_VEHICLE = 4;

    /** 描述运动状态： 无含义，保留 */
    public static final int MOTION_MAX = 5;

    /** 描述睡眠状态： 睡眠 */
    public static final int MOTION_SLEEP = 6;

    /** 描述睡眠状态： 深睡 */
    public static final int MOTION_DEEP_SLEEP = 7;

    /** 描述运动状态： 跌倒 */
    public static final int MOTION_FALL = 64;

    /** 睡眠监测日期 */
    public long recordDate;

    /** 总睡眠时间/小时 */
    public long sleepTime;

    /** 深睡时间/小时 */
    public long deepSleepTime;

    /** 浅睡时间/小时 */
    public long lightSleepTime;

    /** 睡眠质量/五星等级（1～５） */
    public int sleepQuality;

    /** 睡眠监测记录数 */
    public int sleepRecordCount = 0;

    /** 睡眠记录详情 */
    public Record record[];

    /**
     * 表示2-24点（每隔2点的总步数）. 12个数据，但是并不一定每一个都有意义，只有小于或者等于当前小时数的数据才有意义.
     */
    public String[] days;

    /** 表示按天记录的计步. 7个数据（暂时只右一周，后面再做修改）. */
    public String[] weeks;

    /** 表示心率[A[rate,time]A[rate,time]] */
    public String rates;

    /** 手表当前时间的long型(可精确到秒，具体看手表端的需求，从该值可以得到days字符串的长度) */
    public long nowDate;

    /** 温度值 */
    public int temp;

    /** 湿度值 */
    public int humidity;

    /** 气压值 */
    public int pressure;

    /** 　紫外线强度 */
    public int uitravioletIntensity;

    /** 海拔高度 */
    public String altitude;

    /** 实例化健康信息类 */
    public HealthInfo() {
        days = new String[13];
        weeks = new String[7];
    }

    /**
     * 实例化健康信息类
     * @param count
     *            睡眠监测记录数
     */
    public HealthInfo(int count) {
        IwdsAssert.dieIf(this, count < 0, "monitor size < 0.");

        this.sleepRecordCount = count;

        if (count != 0) {
            record = new Record[count];
        }

        days = new String[13];
        weeks = new String[7];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (sleepRecordCount > 0) {
            dest.writeInt(sleepRecordCount);
        } else {
            dest.writeInt(0);
        }

        dest.writeStringArray(days);
        dest.writeStringArray(weeks);
        dest.writeString(rates);

        dest.writeLong(nowDate);

        dest.writeInt(temp);
        dest.writeInt(humidity);
        dest.writeInt(pressure);

        dest.writeLong(recordDate);
        dest.writeLong(sleepTime);
        dest.writeLong(deepSleepTime);
        dest.writeLong(lightSleepTime);
        dest.writeInt(sleepQuality);

        if (record != null) {
            dest.writeTypedArray(record, flags);
        }

        dest.writeInt(uitravioletIntensity);
        dest.writeString(altitude);
    }

    public static final Creator<HealthInfo> CREATOR = new Creator<HealthInfo>() {
        @Override
        public HealthInfo createFromParcel(Parcel source) {
            int count = source.readInt();
            HealthInfo info = new HealthInfo(count);

            source.readStringArray(info.days);
            source.readStringArray(info.weeks);
            info.rates = source.readString();

            info.nowDate = source.readLong();

            info.temp = source.readInt();
            info.humidity = source.readInt();
            info.pressure = source.readInt();

            info.recordDate = source.readLong();
            info.sleepTime = source.readLong();
            info.deepSleepTime = source.readLong();
            info.lightSleepTime = source.readLong();
            info.sleepQuality = source.readInt();
            info.sleepRecordCount = count;

            if (info.record != null) {
                source.readTypedArray(info.record, Record.CREATOR);
            }

            info.uitravioletIntensity = source.readInt();
            info.altitude = source.readString();

            return info;
        }

        @Override
        public HealthInfo[] newArray(int size) {
            return new HealthInfo[size];
        }
    };

    /**
     * 记录类.
     */
    public static class Record implements Parcelable {

        /** 开始时间 */
        public long startTime;

        /** 结束时间 */
        public long endTime;

        /** 睡眠类型（深睡/浅睡） */
        public int type;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(startTime);
            dest.writeLong(endTime);

            dest.writeInt(type);
        }

        public static final Creator<Record> CREATOR = new Creator<Record>() {
            @Override
            public Record createFromParcel(Parcel source) {
                Record info = new Record();

                info.startTime = source.readLong();
                info.endTime = source.readLong();

                info.type = source.readInt();

                return info;
            }

            @Override
            public Record[] newArray(int size) {
                return new Record[size];
            }
        };

        @Override
        public String toString() {
            return "Record [startTime=" + startTime + ", endTime=" + endTime
                    + ", type=" + type + "]";
        }
    }

    @Override
    public String toString() {
        return "HealthInfo [days=" + Arrays.toString(days) + ", weeks="
                + Arrays.toString(weeks) + ", rates=[" + rates + "], nowDate ="
                + nowDate + ", temp=" + temp + ", humidity=" + humidity
                + ", pressure=" + pressure + ", recordDate=" + recordDate
                + ", sleepTime=" + sleepTime + ", deepSleepTime="
                + deepSleepTime + ", lightSleepTime =" + lightSleepTime
                + ", sleepQuality=" + sleepQuality + ", sleepRecordCount="
                + sleepRecordCount + ", record=" + Arrays.toString(record)
                + "]";
    }
}
