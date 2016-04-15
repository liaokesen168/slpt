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
import android.os.Message;

public abstract class OnLongClickHandler extends ILocalRemoteViewsLongClickHandler.Stub {

    private static final int HANDLE_VIEW_LONG_CLICK = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case HANDLE_VIEW_LONG_CLICK:
                onLongClick((String) msg.obj, msg.arg1);
                break;
            }
        }
    };

    @Override
    public final void onLongClickHandler(String callingPkg, int viewId) {
        mHandler.obtainMessage(HANDLE_VIEW_LONG_CLICK, viewId, -1, callingPkg).sendToTarget();
    }

    protected abstract void onLongClick(String callingPkg, int viewId);
}