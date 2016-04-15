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

package com.ingenic.iwds.uniconnect.link;

/**
 * 远端设备类.
 */
abstract public class RemoteDevice {

    /** 传统蓝牙. */
    public static int TYPE_BLUETOOTH_CLASSIC = 0;

    /** 双模蓝牙. */
    public static int TYPE_BLUETOOTH_DUAL = 1;

    /** 低功耗(BLE)蓝牙. */
    public static int TYPE_BLUETOOTH_LE = 2;

    /** 未定义类型. */
    public static int TYPE_BLUETOOTH_UNKNOWN = 3;

    /**
     * 获取设备类型.
     *
     * @return 设备类型
     */
    public abstract int getDeviceType();

    /**
     * 获取名称.
     *
     * @return 名称
     */
    public abstract String getName();

    /**
     * 获取地址.
     *
     * @return 地址
     */
    public abstract String getAddress();

    public abstract boolean equals(Object obj);

    public abstract int hashCode();

    public abstract String toString();
}
