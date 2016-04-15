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

import android.content.Context;

/**
 * BLE适配器类.
 */
public class BleAdapter extends Adapter {

    /**
     * 实例化一个BLE适配器对象.
     *
     * @param context 应用的上下文
     * @param adapterManager 适配器的管理者
     */
    BleAdapter(Context context, AdapterManager adapterManager) {
        super(context, adapterManager, TAG_BLE_DATA_CHANNEL);
        // TODO
    }

    @Override
    public boolean isEnabled() {
        // TODO
        return false;
    }

    @Override
    public int startDiscovery(DeviceDiscoveryCallbacks callbacks) {
        // TODO
        return 0;
    }

    @Override
    public void cancelDiscovey() {
        // TODO

    }

    @Override
    public String getLocalAddress() {
        // TODO

        return null;
    }

    @Override
    public boolean enable() {
        // TODO
        return false;
    }

    @Override
    public void disable() {
        // TODO
    }

}
