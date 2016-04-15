/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  WangLianCheng <liancheng.wang@ingenic.com>
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

package com.ingenic.iwds.cloud;

import android.os.Handler;
import android.os.Message;

import com.ingenic.iwds.cloud.IAccountListener;

/**
 * 监听账号处理相关操作的执行结果
 */
public class AccountListener {
    /**
     * 执行成功
     */
    public void onSuccess() {

    }

    /**
     * 执行失败
     * @param errCode 错误码
     * @param errMsg  错误信息
     */
    public void onFailure(int errCode, String errMsg) {

    }

    void failure(int errCode, String errMsg) {
        Message.obtain(mHandler, FAILURE, errCode, 0, errMsg).sendToTarget();
    }

    private static final int SUCCESS = 1;
    private static final int FAILURE = 2;
    IAccountListener callback = new IAccountListener.Stub() {
        @Override
        public void onSuccess() {
            Message.obtain(mHandler, SUCCESS).sendToTarget();
        }

        @Override
        public void onFailure(int errCode, String errMsg) {
            Message.obtain(mHandler, FAILURE, errCode, 0, errMsg).sendToTarget();
        }
    };

    // send Listener events to the client's main thread.
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SUCCESS:
                AccountListener.this.onSuccess();
                break;

            case FAILURE:
                AccountListener.this.onFailure(msg.arg1, (String)msg.obj);
                break;
            }
        }
    };
}
