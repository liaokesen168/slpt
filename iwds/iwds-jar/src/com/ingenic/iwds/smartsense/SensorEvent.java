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
 * 传感器事件类, 该类表示一个{@link com.ingenic.iwds.smartsense.Sensor Sensor}事件, 并且保存传感器类型,
 * 精度以及传感器的数据 {@link SensorEvent#values values} 等信息.
 *
 * @see SensorServiceManager
 * @see Sensor
 *
 */
public class SensorEvent implements Parcelable {

    /**
     * <p>
     * 传感器数据 {@link #values values} 内容, 具体依赖的传感器
     * {@link com.ingenic.iwds.smartsense.Sensor Sensor} 类型
     * </p>
     * <br/>
     *
     * <h4>{@link com.ingenic.iwds.smartsense.Sensor#TYPE_HEART_RATE
     * Sensor.TYPE_HEART_RATE}</h4>
     *
     * <ul>
     * <li>values[0]: 每分钟心跳次数
     * </ul>
     * <br/>
     *
     * <h4>{@link com.ingenic.iwds.smartsense.Sensor#TYPE_UV Sensor.TYPE_UV}</h4>
     *
     * <ul>
     * <li>values[0]: 紫外线强度(0-1500)
     * </ul>
     * <br/>
     *
     * <h4>{@link com.ingenic.iwds.smartsense.Sensor#TYPE_GESTURE
     * Sensor.TYPE_GESTURE}</h4>
     *
     * <ul>
     * <li>values[0]: 手势编码
     * <li>GESTURE_SHAKE_HAND: 摇晃手腕
     * <li>GESTURE_RAISE_HAND_AND_LOOK: 抬起手腕
     * <li>GESTURE_LET_HAND_DOWN_AFTER_LOOK: 放下手腕
     * <li>GESTURE_TURN_WRIST: 翻转手腕
     * </ul>
     * <br/>
     *
     * <h4>{@link com.ingenic.iwds.smartsense.Sensor#TYPE_MOTION
     * Sensor.TYPE_MOTION}</h4>
     *
     * <ul>
     * <li>values[0]: 运动姿态编码
     * <li>MOTION_RESET: 静止
     * <li>MOTION_STOP: 运动停止
     * <li>MOTION_WALK: 正在走路
     * <li>MOTION_RUN: 正在跑步
     * <li>MOTION_SLEEP：睡眠， values[1]: 睡眠期间翻身次数
     * <li>MOTION_DEEP_SLEEP：深睡， values[1]: 睡眠期间翻身次数
     * </ul>
     * <br/>
     *
     * <h4>{@link com.ingenic.iwds.smartsense.Sensor#TYPE_VOICE_TRIGGER
     * Sensor.TYPE_VOICE_TRIGGER}</h4>
     *
     * <ul>
     * <li>values[0]: 语音唤醒事件
     * </ul>
     * <br/>
     *
     * <h4>{@link com.ingenic.iwds.smartsense.Sensor#TYPE_STEP_COUNTER
     * Sensor.TYPE_STEP_COUNTER}</h4>
     *
     * <ul>
     * <li>values[0]: 计步数
     * </ul>
     * <br/>
     *
     * <h4>{@link com.ingenic.iwds.smartsense.Sensor#TYPE_RELATIVE_HUMIDITY
     * Sensor.TYPE_RELATIVE_HUMIDITY}</h4>
     *
     * <ul>
     * <li>values[0]: 相对空气湿度, 单位百分比
     * </ul>
     * <br/>
     *
     * <h4>{@link com.ingenic.iwds.smartsense.Sensor#TYPE_AMBIENT_TEMPERATURE
     * Sensor.TYPE_AMBIENT_TEMPERATURE}</h4>
     *
     * <ul>
     * <li>values[0]: 环境温度, 单位摄氏度
     * </ul>
     * <br/>
     *
     * <h4>{@link com.ingenic.iwds.smartsense.Sensor#TYPE_PRESSURE
     * Sensor.TYPE_PRESSURE}</h4>
     *
     * <ul>
     * <li>values[0]: 大气压, 单位帕斯卡
     * </ul>
     * <br/>
     * 
     * <h4>{@link com.ingenic.iwds.smartsense.Sensor#TYPE_PROXIMITY
     * Sensor.TYPE_PROXIMITY}</h4>
     *
     * <ul>
     * <li>values[0]: 距离(0-255)
     * <li>values[1]: 1 - 靠近； 0 - 远离
     * </ul>
     * <br/>
     */

    /**
     * 描述手势传感器上报的手势：摇晃手腕
     */
    public static final int GESTURE_SHAKE_HAND = 1;

    /**
     * 描述手势传感器上报的手势：抬起手腕
     */
    public static final int GESTURE_RAISE_HAND_AND_LOOK = 2;

    /**
     * 描述手势传感器上报的手势：放下手腕
     */
    public static final int GESTURE_LET_HAND_DOWN_AFTER_LOOK = 3;

    /**
     * 描述手势传感器上报的手势：翻转手腕
     */
    public static final int GESTURE_TURN_WRIST = 4;

    /**
     * 描述运动状态： 静止
     */
    public static final int MOTION_RESET = 0;

    /**
     * 描述运动状态： 运动停止
     */
    public static final int MOTION_STOP = 1;

    /**
     * 描述运动状态： 正在走路
     */
    public static final int MOTION_WALK = 2;

    /**
     * 描述运动状态： 正在跑步
     */
    public static final int MOTION_RUN = 3;

    /**
     * 描述运动状态： 睡眠
     */
    public static final int MOTION_SLEEP = 4;

    /**
     * 描述运动状态： 深睡
     */
    public static final int MOTION_DEEP_SLEEP = 5;

    /**
     * 该变量只应用于原相心率，仅在获取当前传感器厂商返回值为"PixArt"时有效。
     * <p>更多详细参考 {@link com.ingenic.iwds.smartsense.Sensor#getVendor()}
     *
     * 用户只需在{@link com.ingenic.iwds.smartsense.SensorEventListener
     *          #onAccuracyChanged(Sensor sensor, int accuracy)}
     * 回调时判断accuracy是否与该值匹配！
     *
     * 目前心率传感器上报的精度值有两种，
     * 分别为ACCURACY_HEART_RATE_UNAVALIABLE和ACCURACY_HEART_RATE_AVALIABLE。
     *
     * 当上报精度为ACCURACY_HEART_RATE_UNAVALIABLE时，表示心率传感器工作状态不正常，
     * 有可能是心率没有正常接触人体皮肤，或者手表佩戴不适导致测量心率超时。
     */
    public static final int ACCURACY_HEART_RATE_UNAVALIABLE = 1;

    /**
     * 该变量只应用于原相心率，仅在获取当前传感器厂商返回值为"PixArt"时有效。
     * <p>更多详细参考 {@link com.ingenic.iwds.smartsense.Sensor#getVendor()}
     *
     * 用户只需在{@link com.ingenic.iwds.smartsense.SensorEventListener
     *          #onAccuracyChanged(Sensor sensor, int accuracy)}
     * 回调时判断accuracy是否与该值匹配！
     *
     * 目前心率传感器上报的精度值有两种，
     * 分别为ACCURACY_HEART_RATE_UNAVALIABLE和ACCURACY_HEART_RATE_AVALIABLE。
     *
     * 当上报精度为ACCURACY_HEART_RATE_AVALIABLE时，表示心率传感器工作状态正常，
     * 手表佩戴良好，心率测量数据即将上报（通常会在1～5s内上报心率数据）。
     */
    public static final int ACCURACY_HEART_RATE_AVALIABLE = 2;

    /**
     * 描述语音唤醒系统事件的常量，当且仅当系统唤醒时才产生该事件
     */
    public static final int EVENT_WAKE_UP = 1;

    /**
     * 传感器数据
     *
     * 当监听原相心率事件，且在获取当前传感器厂商返回值为"PixArt"时以下有效：
     * <p>更多详细参考 {@link com.ingenic.iwds.smartsense.Sensor#getVendor()}
     * values[0]为心率值，values[1]为传感器精度值
     * （有效值为ACCURACY_HEART_RATE_UNAVALIABLE或者ACCURACY_HEART_RATE_AVALIABLE），
     * values[2]为 float型传感器裸数据（目前版本未归一化该数据），values[3]为计数值（0～255），
     * values[4]为touch标志位，1表示touch on，0表示touch off，values[5]为裸数据每秒上报速率。
     */
    public final float[] values;

    /**
     * 传感器数据精度
     */
    public int accuracy;

    /**
     * 传感器类型
     */
    public int sensorType;

    /**
     * 事件时间戳
     */
    public long timestamp;

    private final int valuesSize;

    SensorEvent(int valSize) {
        valuesSize = valSize;
        values = new float[valuesSize];
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
        dest.writeInt(valuesSize);

        for (int i = 0; i < valuesSize; i++)
            dest.writeFloat(values[i]);

        dest.writeInt(accuracy);
        dest.writeInt(sensorType);
        dest.writeLong(timestamp);
    }

    /**
     * Parcelable 接口定义, 不要使用
     */
    public static final Creator<SensorEvent> CREATOR = new Creator<SensorEvent>() {
        @Override
        public SensorEvent createFromParcel(Parcel source) {
            int valSize = source.readInt();
            SensorEvent event = new SensorEvent(valSize);

            for (int i = 0; i < valSize; i++)
                event.values[i] = source.readFloat();

            event.accuracy = source.readInt();
            event.sensorType = source.readInt();
            event.timestamp = source.readLong();

            return event;
        }

        @Override
        public SensorEvent[] newArray(int size) {
            return new SensorEvent[size];
        }
    };
}
