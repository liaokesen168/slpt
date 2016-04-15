/*
 * Copyright (C) 2015 Ingenic Semiconductor
 * 
 * LiJinWen(Kevin)<kevin.jwli@ingenic.com>
 * 
 * Elf/iwds-jar
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.ingenic.iwds.widget;

import android.os.Handler;

public abstract class OnClickHandler extends ILocalRemoteViewsClickHandler.Stub {

    private static final int HANDLE_ON_CLICK = 0;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case HANDLE_ON_CLICK:
                onClick((String) msg.obj, msg.arg1);
                break;
            }
        };
    };

    @Override
    public final void onClickHandler(String callingPkg, int viewId) {
        mHandler.obtainMessage(HANDLE_ON_CLICK, viewId, -1, callingPkg).sendToTarget();
    }

    protected abstract void onClick(String callingPkg, int viewId);
}
