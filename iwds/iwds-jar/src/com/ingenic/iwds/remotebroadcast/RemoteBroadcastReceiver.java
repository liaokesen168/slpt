/*
 * Copyright (C) 2015 Ingenic Semiconductor
 * 
 * LiJingWen(Kevin) <kevin.jwli@ingenic.com>
 * 
 * Elf/IDWS Project
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.ingenic.iwds.remotebroadcast;

import android.content.Context;
import android.content.Intent;

/**
 * 远程广播接收器。注册远程广播接收器，可以接收对端设备发送的普通广播。
 */
public abstract class RemoteBroadcastReceiver {
    /**
     * 远程广播接收回调。
     * 
     * @param intent 收到的广播意图。
     */
    public abstract void onReceive(Context context, Intent intent);
}