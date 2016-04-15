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

import java.util.List;
import android.os.Handler;
import android.os.Message;
import com.ingenic.iwds.cloud.IDataInfoListener;
import com.ingenic.iwds.cloud.CloudDataValues;

/**
 * 监听数据查询的执行结果
 */
public class DataInfoListener {
    /**
     * 执行成功
     * @param list 返回查询结果
     */
    public void onSuccess(List<CloudDataValues> list) {

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
    IDataInfoListener callback = new IDataInfoListener.Stub() {
        @Override
        public void onSuccess(List<CloudDataValues> dataList) {
            Message.obtain(mHandler, SUCCESS, 0, 0, dataList).sendToTarget();
        }

        @Override
        public void onFailure(int errCode, String errMsg) {
            Message.obtain(mHandler, FAILURE, errCode, 0, errMsg).sendToTarget();
        }
    };

    // send Listener events to the client's main thread.
    private final Handler mHandler = new Handler() {
        @Override
        @SuppressWarnings("unchecked")
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SUCCESS:
                DataInfoListener.this.onSuccess((List<CloudDataValues>)msg.obj);
                break;

            case FAILURE:
                DataInfoListener.this.onFailure(msg.arg1, (String)msg.obj);
                break;
            }
        }
    };

}
