/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  SunWenZhong(Fighter) <wzsun@ingenic.com, wanmyqawdr@126.com>
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

import android.bluetooth.BluetoothDevice;

/**
 * Android蓝牙设备类.
 */
public class AndroidBtDevice extends RemoteDevice {
    private BluetoothDevice mDevice;

    /**
     * 实例化一个Android蓝牙设备对象.
     *
     * @param device 蓝牙设备
     */
    /* package */AndroidBtDevice(BluetoothDevice device) {
        mDevice = device;
    }

    @Override
    public int getDeviceType() {
        // TODO: implement me

        return TYPE_BLUETOOTH_CLASSIC;
    }

    @Override
    public String getName() {
        return mDevice.getName();
    }

    @Override
    public String getAddress() {
        return mDevice.getAddress();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mDevice == null) ? 0 : mDevice.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "AndroidBtDevice [Name=" + getName() + ", MAC=" + mDevice + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        AndroidBtDevice other = (AndroidBtDevice) obj;
        if (mDevice == null) {
            if (other.mDevice != null)
                return false;

        } else if (!mDevice.equals(other.mDevice))
            return false;

        return true;
    }

}
