/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  Huanglihong(Regen) <lihong.huang@ingenic.com, peterlihong@qq.com>
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

package com.ingenic.iwds.remotedevice;

/**
 * 远程设备设置管理监听器
 */
public interface RemoteDeviceSettingListener {
    /**
     * 当对远程设备的设置完成时被调用。
     * 
     * @param type
     *            设置类型，可能的值：
     *            {@link RemoteDeviceManagerInfo#TYPE_SETTING_WEAR_ON_WHICH_HAND}
     * 
     * @param returnCode
     *            返回值，与设置类型有关。当设置类型为
     *            {@link RemoteDeviceManagerInfo#TYPE_SETTING_WEAR_ON_WHICH_HAND}
     *            时， 可能的值为：
     *            {@link RemoteDeviceManagerInfo#VALUE_WEAR_ON_LEFT_HAND}、
     *            {@link RemoteDeviceManagerInfo#VALUE_WEAR_ON_RIGHT_HAND}、
     *            {@link RemoteDeviceManagerInfo#REQUEST_FAILED_SERVICE_DISCONNECTED}
     */
    void onDoneSetting(int type, int returnCode);

    /**
     * 当获取到远程设备的设置时被调用。
     * 
     * @param type
     *            设置类型，可能的值：
     *            {@link RemoteDeviceManagerInfo#TYPE_SETTING_WEAR_ON_WHICH_HAND}
     * 
     * @param returnCode
     *            返回值，与设置类型有关。当设置类型为
     *            {@link RemoteDeviceManagerInfo#TYPE_SETTING_WEAR_ON_WHICH_HAND}
     *            时， 可能的值为：
     *            {@link RemoteDeviceManagerInfo#VALUE_WEAR_ON_LEFT_HAND}、
     *            {@link RemoteDeviceManagerInfo#VALUE_WEAR_ON_RIGHT_HAND}、
     *            {@link RemoteDeviceManagerInfo#REQUEST_FAILED_SERVICE_DISCONNECTED}
     */
    void onGetSetting(int type, int returnCode);
}
