/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  Huanglihong(Regen) <lihong.huang@ingenic.com>
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
 * 远程设备状态监听器
 */
public interface RemoteDeviceStatusListener {

    /**
     * 当远程设备准备好时被调用。
     * 
     * @param isReady
     *            如果为真，表示远程设备已准备好；否则，未准备好。
     */
    public void onRemoteDeviceReady(boolean isReady);
}
