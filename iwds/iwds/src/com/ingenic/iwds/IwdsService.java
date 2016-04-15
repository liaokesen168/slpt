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

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.utils.IwdsLog;

public class IwdsService extends Service {
    @Override
    public void onCreate() {
        IwdsLog.d(this, "onCreate()");

        CharSequence label = getApplication().getApplicationInfo().loadLabel(
                getPackageManager());

        Notification notification = new Notification(getApplication()
                .getApplicationInfo().icon, label, System.currentTimeMillis());

        Intent it = new Intent(
                ServiceManagerContext.ACTION_NOTIFICATION_CLICKED);
        it.setFlags(it.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, it, 0);
        notification.setLatestEventInfo(this, label, "", pendingIntent);
        notification.flags = notification.flags
                | Notification.FLAG_ONGOING_EVENT;

        startForeground(9999, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

}
