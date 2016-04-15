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
package com.ingenic.iwds.appwidget;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class WidgetService extends Service {
    private WidgetServiceImpl mService;

    @Override
    public void onCreate() {
        super.onCreate();

        mService = new WidgetServiceImpl(this);
        mService.onServiceCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mService.asBinder();
    }

    @Override
    public void onDestroy() {
        mService.onServiceDestroy();
        mService = null;
        super.onDestroy();
    }
}