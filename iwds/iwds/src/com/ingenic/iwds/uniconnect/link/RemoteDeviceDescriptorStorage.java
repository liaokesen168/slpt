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

import java.util.ArrayList;

import com.ingenic.iwds.DeviceDescriptor;

/**
 * 远端设备描述符存储类.
 */
public class RemoteDeviceDescriptorStorage {
    private static RemoteDeviceDescriptorStorage sm_thisStorage;

    private static ArrayList<DeviceDescriptor> m_deviceDescriptors;

    /**
     * 获取单例的远端设备描述符存储对象.
     *
     * @return 单例的远端设备描述符存储对象
     */
    public static RemoteDeviceDescriptorStorage getInstance() {
        if (sm_thisStorage == null)
            sm_thisStorage = new RemoteDeviceDescriptorStorage();

        return sm_thisStorage;
    }

    /* package */private RemoteDeviceDescriptorStorage() {
        m_deviceDescriptors = new ArrayList<DeviceDescriptor>();
    }

    /**
     * 添加远端设备描述符.
     *
     * @param deviceDescriptor
     *            远端设备描述符
     */
    public void addDeviceDescriptors(DeviceDescriptor deviceDescriptor) {
        synchronized (m_deviceDescriptors) {
            m_deviceDescriptors.add(deviceDescriptor);
        }
    }

    /**
     * 删除远端设备描述符.
     *
     * @param deviceDescriptor
     *            远端设备描述符
     */
    public void removeDeviceDescriptor(DeviceDescriptor deviceDescriptor) {
        synchronized (m_deviceDescriptors) {
            m_deviceDescriptors.remove(deviceDescriptor);
        }
    }

    /**
     * 获取远端设备描述符阵列.
     *
     * @return 远端设备描述符阵列
     */
    public DeviceDescriptor[] getDeviceDescriptorsArray() {
        synchronized (m_deviceDescriptors) {
            Object[] descriptors = m_deviceDescriptors.toArray();

            DeviceDescriptor[] deviceDescriptors = new DeviceDescriptor[descriptors.length];
            for (int i = 0; i < deviceDescriptors.length; i++)
                deviceDescriptors[i] = (DeviceDescriptor) descriptors[i];

            return deviceDescriptors;
        }
    }
}
