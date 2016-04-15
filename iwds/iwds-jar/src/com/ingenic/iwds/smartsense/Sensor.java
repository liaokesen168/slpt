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
 */

package com.ingenic.iwds.smartsense;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 传感器类, 使用 {@link SensorServiceManager#getDefaultSensor} 获取指定类型的传感器.
 *
 * @see SensorServiceManager
 * @see SensorEventListener
 * @see SensorEvent
 *
 */
public class Sensor implements Parcelable {
    private static final int SMART_SENSE_TYPE_BASE = 100;

    /**
     * 描述心率传感器的常量
     * <p>更多详细参考 {@link com.ingenic.iwds.smartsense.SensorEvent#values SensorEvent.values}.
     */
    public static final int TYPE_HEART_RATE = 21;

    /**
     * 描述紫外线传感器的常量
     * <p>更多详细参考 {@link com.ingenic.iwds.smartsense.SensorEvent#values SensorEvent.values}.
     */
    public static final int TYPE_UV = (2 + SMART_SENSE_TYPE_BASE);

    /**
     * 描述特定手势传感器的常量
     * <p>更多详细参考 {@link com.ingenic.iwds.smartsense.SensorEvent#values SensorEvent.values}.
     */
    public static final int TYPE_GESTURE = (3 + SMART_SENSE_TYPE_BASE);

    /**
     * 描述特定动作传感器的常量
     * <p>更多详细参考 {@link com.ingenic.iwds.smartsense.SensorEvent#values SensorEvent.values}.
     */
    public static final int TYPE_MOTION = (4 + SMART_SENSE_TYPE_BASE);

    /**
     * 描述语音唤醒传感器的常量
     * <p>更多详细参考 {@link com.ingenic.iwds.smartsense.SensorEvent#values SensorEvent.values}.
     */
    public static final int TYPE_VOICE_TRIGGER = (5 + SMART_SENSE_TYPE_BASE);

    /**
     * 描述计步传感器的常量
     * <p>更多详细参考 {@link com.ingenic.iwds.smartsense.SensorEvent#values SensorEvent.values}.
     */
    public static final int TYPE_STEP_COUNTER = 19;

    /**
     * 描述环境湿度传感器的常量
     * <p>更多详细参考 {@link com.ingenic.iwds.smartsense.SensorEvent#values SensorEvent.values}.
     */
    public static final int TYPE_RELATIVE_HUMIDITY = 12;

    /**
     * 描述环境温度传感器的常量
     * <p>更多详细参考 {@link com.ingenic.iwds.smartsense.SensorEvent#values SensorEvent.values}.
     */
    public static final int TYPE_AMBIENT_TEMPERATURE = 13;

    /**
     * 描述大气压力传感器的常量
     * <p>更多详细参考 {@link com.ingenic.iwds.smartsense.SensorEvent#values SensorEvent.values}.
     */
    public static final int TYPE_PRESSURE = 6;

    /**
     * 描述距离传感器的常量
     * <p>更多详细参考 {@link com.ingenic.iwds.smartsense.SensorEvent#values SensorEvent.values}.
     */
    public static final int TYPE_PROXIMITY = 8;

    /**
     * 描述所有传感器的常量, 不要使用
     * <p>更多详细参考 {@link com.ingenic.iwds.smartsense.SensorEvent#values SensorEvent.values}.
     */
    public static final int TYPE_ALL = -1;

    private int m_type;
    private float m_maxRange;
    private float m_resolution;
    private int m_minDelay;
    private String m_name;
    private int m_version;
    private String m_vendor;

    Sensor() {

    }

    /**
     * 获取当前传感器名字
     * @return 当前传感器名字.
     */
    public String getName() {
        return m_name;
    }

    /**
     * 获取当前传感器版本
     * @return 当前传感器版本.
     */
    public int getVersion() {
        return m_version;
    }

    /**
     * 获取当前传感器厂商
     * @return 当前传感器厂商.
     */
    public String getVendor() {
        return m_vendor;
    }

    /**
     * 获取当前传感器类型
     * @return 当前传感器类型.
     */
    public int getType() {
        return m_type;
    }

    /**
     * 获取传感器最大采集数据范围
     * @return 传感器最大采集数据范围.
     */
    public float getMaximumRange() {
        return m_maxRange;
    }

    /**
     * 获取传感器数值数据采集分辨率
     * @return 传感器数值数据采集分辨率.
     */
    public float getResolution() {
        return m_resolution;
    }

    /**
     * 获取传感器两次上报事件之间的最小延时, 单位为微秒. 或者为零延迟, 此时当采集到数据有变化立刻上报事件
     * @return 传感器两次上报事件之间的最小延时, 单位为微秒. 或者为零延迟, 此时当采集到数据有变化立刻上报事件.
     */
    public int getMinDelay() {
        return m_minDelay;
    }

    /**
     * 调试使用
     */
    @Override
    public String toString() {
        return "{type=" + m_type + ", maxRange=" + m_maxRange + ", resolution="
                + m_resolution + ", minDelay=" + m_minDelay + ", name="
                + m_name + ", vendor=" + m_vendor + ", version=" + m_version + "}";
    }

    /**
     * Parcelable 接口定义, 不要使用
     */
    @Override
    public int describeContents() {

        return 0;
    }

    /**
     * Parcelable 接口定义, 不要使用
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(m_type);
        dest.writeFloat(m_maxRange);
        dest.writeFloat(m_resolution);
        dest.writeInt(m_minDelay);
        dest.writeString(m_name);
        dest.writeString(m_vendor);
        dest.writeInt(m_version);
    }

    /**
     * Parcelable 接口定义, 不要使用
     */
    public static final Creator<Sensor> CREATOR = new Creator<Sensor>() {
        @Override
        public Sensor createFromParcel(Parcel source) {
            Sensor sensor = new Sensor();

            sensor.m_type = source.readInt();
            sensor.m_maxRange = source.readFloat();
            sensor.m_resolution = source.readFloat();
            sensor.m_minDelay = source.readInt();
            sensor.m_name = source.readString();
            sensor.m_vendor = source.readString();
            sensor.m_version = source.readInt();

            return sensor;
        }

        @Override
        public Sensor[] newArray(int size) {
            return new Sensor[size];
        }
    };
}
