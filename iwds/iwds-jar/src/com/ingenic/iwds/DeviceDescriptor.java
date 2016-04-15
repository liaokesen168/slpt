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

package com.ingenic.iwds;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

/**
 * 设备描述符类.
 */
public class DeviceDescriptor implements Parcelable {

    /** 穿戴式设备. */
    public static final int DEVICE_CLASS_WEARABLE = 0;

    /** 智能家居设备. */
    public static final int DEVICE_CLASS_SMARTHOME = 1;

    /** 移动设备. */
    public static final int DEVICE_CLASS_MOBILE = 2;

    /*
     * for wearable devices
     */
    /** 穿戴式设备子类: 手表. */
    public static final int WEARABLE_DEVICE_SUBCLASS_WATCH = 1;

    /** 穿戴式设备子类: 眼镜. */
    public static final int WEARABLE_DEVICE_SUBCLASS_GLASS = 2;

    /*
     * TODO add for smart home
     */

    /*
     * for mobile devices
     */
    /** 移动设备子类: 智能手机. */
    public static final int MOBILE_DEVICE_SUBCLASS_SMARTPHONE = 1;

    private static HashMap<Integer, ArrayList<Integer>> sm_deviceClasses;

    /** 设备地址. */
    public String devAddress;

    /** 连接标记. */
    public String linkTag;

    /** 设备类别. */
    public int devClass;

    /** 设备子类别. */
    public int devSubClass;

    /** 型号. */
    public String model;

    /** 厂家. */
    public String manufacture;

    /** 序列号. */
    public String serialNo;
    public String displayID;

    /** android api等级. */
    public int androidApiLevel;

    /** iwds版本. */
    public int iwdsVersion;

    /** LCD外型. */
    public String lcdExterior;

    /** LCD尺寸. */
    public String lcdSize;

    static {
        sm_deviceClasses = new HashMap<Integer, ArrayList<Integer>>();

        /*
         * for wearable
         */
        ArrayList<Integer> wearable = new ArrayList<Integer>();
        wearable.add(Integer.valueOf(WEARABLE_DEVICE_SUBCLASS_WATCH));
        wearable.add(Integer.valueOf(WEARABLE_DEVICE_SUBCLASS_GLASS));

        sm_deviceClasses.put(Integer.valueOf(DEVICE_CLASS_WEARABLE), wearable);

        /*
         * for smart home
         */
        ArrayList<Integer> smartHome = new ArrayList<Integer>();
        sm_deviceClasses
                .put(Integer.valueOf(DEVICE_CLASS_SMARTHOME), smartHome);

        /*
         * for mobile
         */
        ArrayList<Integer> mobile = new ArrayList<Integer>();
        mobile.add(Integer.valueOf(MOBILE_DEVICE_SUBCLASS_SMARTPHONE));

        sm_deviceClasses.put(Integer.valueOf(DEVICE_CLASS_MOBILE), mobile);
    }

    /**
     * 构造一个新的设备描述符.
     *
     * @param deviceAddress
     *            设备地址
     * @param devicelinkTag
     *            连接标记
     * @param deviceClass
     *            设备类别
     * @param deviceSubClass
     *            设备子类别
     */
    public DeviceDescriptor(String deviceAddress, String devicelinkTag,
            int deviceClass, int deviceSubClass) {
        IwdsAssert.dieIf(this,
                deviceAddress == null || deviceAddress.isEmpty(),
                "Device devAddress is null or empty.");

        IwdsAssert.dieIf(this, !sm_deviceClasses.containsKey(deviceClass),
                "Unknown device class, class code: " + deviceClass);

        IwdsAssert.dieIf(
                this,
                !sm_deviceClasses.get(Integer.valueOf(deviceClass)).contains(
                        Integer.valueOf(deviceSubClass)),
                "Unknown sub device class, subclass code: " + deviceSubClass);

        lcdExterior = HardwareList.getHardwareValue(HardwareList.KEY_LCD_EXTERIOR, BuildOptions.UNKNOWN);
        lcdSize = HardwareList.getHardwareValue(HardwareList.KEY_LCD_SIZE, BuildOptions.UNKNOWN);

        devAddress = deviceAddress;
        linkTag = devicelinkTag;

        devClass = deviceClass;
        devSubClass = deviceSubClass;

        model = BuildOptions.MODEL;
        manufacture = BuildOptions.MANUFACTURER;
        serialNo = BuildOptions.SERIAL;
        displayID = BuildOptions.DISPLAY;

        androidApiLevel = BuildOptions.VERSION.SDK_INT;
        iwdsVersion = BuildOptions.IWDS_VERSION_INT;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(devAddress);
        dest.writeString(linkTag);

        dest.writeInt(devClass);
        dest.writeInt(devSubClass);

        dest.writeString(model);
        dest.writeString(manufacture);
        dest.writeString(serialNo);
        dest.writeString(displayID);

        dest.writeInt(androidApiLevel);
        dest.writeInt(iwdsVersion);

        dest.writeString(lcdExterior);
        dest.writeString(lcdSize);
    }

    /** 实现Parcelable的接口 */
    public static final Creator<DeviceDescriptor> CREATOR = new Creator<DeviceDescriptor>() {
        @Override
        public DeviceDescriptor createFromParcel(Parcel source) {
            String devAddress = source.readString();
            String linkTag = source.readString();

            int devClass = source.readInt();
            int devSubClass = source.readInt();

            DeviceDescriptor devDesc = new DeviceDescriptor(devAddress,
                    linkTag, devClass, devSubClass);

            devDesc.model = source.readString();
            devDesc.manufacture = source.readString();
            devDesc.serialNo = source.readString();
            devDesc.displayID = source.readString();

            devDesc.androidApiLevel = source.readInt();
            devDesc.iwdsVersion = source.readInt();

            devDesc.lcdExterior = source.readString();
            devDesc.lcdSize = source.readString();

            return devDesc;
        }

        @Override
        public DeviceDescriptor[] newArray(int size) {
            return new DeviceDescriptor[size];
        }
    };

    @Override
    public String toString() {
        return "DeviceDescriptor [devAddress=" + devAddress + ", linkTag="
                + linkTag + ", devClass=" + devClass + ", devSubClass="
                + devSubClass + ", model=" + model + ", manufacture="
                + manufacture + ", serialNo=" + serialNo + ", androidApiLevel="
                + androidApiLevel + ", iwdsVersion=" + iwdsVersion
                + ", displayid=" + displayID + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + androidApiLevel;
        result = prime * result
                + ((devAddress == null) ? 0 : devAddress.hashCode());
        result = prime * result + devClass;
        result = prime * result + devSubClass;
        result = prime * result + iwdsVersion;
        result = prime * result + ((linkTag == null) ? 0 : linkTag.hashCode());
        result = prime * result
                + ((manufacture == null) ? 0 : manufacture.hashCode());
        result = prime * result + ((model == null) ? 0 : model.hashCode());
        result = prime * result
                + ((serialNo == null) ? 0 : serialNo.hashCode());
        result = prime * result
                + ((displayID == null) ? 0 : displayID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof DeviceDescriptor))
            return false;
        DeviceDescriptor other = (DeviceDescriptor) obj;
        if (androidApiLevel != other.androidApiLevel)
            return false;
        if (devAddress == null) {
            if (other.devAddress != null)
                return false;
        } else if (!devAddress.equals(other.devAddress))
            return false;
        if (devClass != other.devClass)
            return false;
        if (devSubClass != other.devSubClass)
            return false;
        if (iwdsVersion != other.iwdsVersion)
            return false;
        if (linkTag == null) {
            if (other.linkTag != null)
                return false;
        } else if (!linkTag.equals(other.linkTag))
            return false;
        if (manufacture == null) {
            if (other.manufacture != null)
                return false;
        } else if (!manufacture.equals(other.manufacture))
            return false;
        if (model == null) {
            if (other.model != null)
                return false;
        } else if (!model.equals(other.model))
            return false;
        if (serialNo == null) {
            if (other.serialNo != null)
                return false;
        } else if (!serialNo.equals(other.serialNo))
            return false;
        if (displayID == null) {
            if (other.displayID != null)
                return false;
        } else if (!displayID.equals(other.displayID))
            return false;
        return true;
    }
}
