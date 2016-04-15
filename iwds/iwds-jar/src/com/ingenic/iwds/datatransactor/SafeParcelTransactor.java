/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  ZhangYanMing <yanming.zhang@ingenic.com, jamincheung@126.com>
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

package com.ingenic.iwds.datatransactor;

import com.ingenic.iwds.os.SafeParcelable;
import com.ingenic.iwds.os.SafeParcelable.Creator;

import android.content.Context;

/**
 * 继承 {@link com.ingenic.iwds.datatransactor.DataTransactor DataTransactor} 针对
 * SafeParcelable 对象的传输进行了优化, 传输效率优于
 * {@link com.ingenic.iwds.datatransactor.DataTransactor DataTransactor}, 同时兼容
 * {@link com.ingenic.iwds.datatransactor.DataTransactor DataTransactor}
 * 支持的所有类型数据的传输. 注意此类只支持单类型的 SafeParcelable 对象传输, 如果需要传输多种类型 的对象, 请构造多个
 * SafeParcelTransactor.
 *
 * @param <T>
 *            实现 SafeParcelable 接口的类
 */
public class SafeParcelTransactor<T extends SafeParcelable> extends
        DataTransactor {

    /**
     * 构造 {@code ParcelTransactor}
     *
     * @param context
     *            应用的上下文
     * @param creator
     *            实现 SafeParcelable 接口的类的静态接口 CREATOR
     * @param callback
     *            回调接口
     *            {@link com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback
     *            DataTransactorCallback}
     *            用于更新连接和数据通道状态、告知数据发送结果、通知有数据接收以及通知文件发送和接收的进度
     * @param uuid
     *            UUID
     * @see DataTransactorCallback
     */
    public SafeParcelTransactor(Context context, Creator<T> creator,
            DataTransactorCallback callback, String uuid) {
        super(context, callback, uuid);
        m_safeParcelableCreator = (Creator<SafeParcelable>) creator;
    }

    /**
     * 启动传输服务.
     */
    @Override
    public void start() {
        super.start();
    }

    /**
     * 停止传输服务.
     */
    @Override
    public void stop() {
        super.stop();
    }

    /**
     * 检查传输服务是否启动.
     *
     * @return 如果已经启动，返回{@code true}.
     */
    @Override
    public boolean isStarted() {
        return super.isStarted();
    }

    /**
     * 发送数据
     *
     * @param object
     *            待发送的数据
     *            {@link com.ingenic.iwds.utils.serializable.TransferAdapter
     *            TransferAdapter}
     */
    @Override
    public void send(Object object) {
        super.send(object);
    }
}
