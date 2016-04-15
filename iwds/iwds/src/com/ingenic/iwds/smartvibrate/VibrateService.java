/*
 *  Copyright (C) 2014 Ingenic Semiconductor
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
 */

package com.ingenic.iwds.smartvibrate;

import com.ingenic.iwds.smartvibrate.VibrateServiceManager.VibrateModes;
import com.ingenic.iwds.utils.IwdsLog;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class VibrateService extends Service {
    private VibrateServiceStub m_service = new VibrateServiceStub();

    @Override
    public IBinder onBind(Intent intent) {
        IwdsLog.d(this, "onBind()");
        return m_service;
    }

    private static class VibrateServiceStub extends IVibrateService.Stub {

        @Override
        public int Drv2605Vibrate(int[] effect) throws RemoteException {
            return nativeSpecialVibrate(effect);
        }

        private static native final int nativeSpecialVibrate(int[] effect);
    }
}

