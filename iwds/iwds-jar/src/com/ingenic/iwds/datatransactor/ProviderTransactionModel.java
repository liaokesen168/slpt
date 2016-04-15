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

package com.ingenic.iwds.datatransactor;

import java.io.Serializable;

import android.content.Context;
import android.os.Parcelable;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback;
import com.ingenic.iwds.utils.IwdsAssert;

/**
 * 通用数据传输模型, 实现了 {@link com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback
 * DataTransactorCallback} 接口, 用于更新连接和传输状态
 *
 * @param <T>
 *        传输数据类型 {@link com.ingenic.iwds.utils.serializable.TransferAdapter TransferAdapter}
 * @see ParcelTransactor
 * @see DataTransactorCallback
 * @see com.ingenic.iwds.utils.serializable.TransferAdapter
 */
public class ProviderTransactionModel<T> implements DataTransactorCallback {

    /* ------------------ public API -------------------- */

    /**
     * 构造 {@code ProviderTransactionModel}
     * @param context
     *        应用的上下文
     * @param callback
     *        回调接口 {@link com.ingenic.iwds.datatransactor.ProviderTransactionModel.ProviderTransactionModelCallback
     *        ProviderTransactionModelCallback} 用于更新连接和传输状态
     * @param uuid
     *        UUID
     * @see   ProviderTransactionModelCallback
     */
    public ProviderTransactionModel(Context context, Parcelable.Creator<T> creator,
            ProviderTransactionModelCallback<T> callback, String uuid) {
        IwdsAssert.dieIf(this, callback == null, "Callback is null.");

        m_callback = callback;
        m_transactor = new ParcelTransactor(context, creator, this, uuid);
    }

    /**
     * 发送数据
     * @param object
     *        待发送的数据 {@link com.ingenic.iwds.utils.serializable.TransferAdapter TransferAdapter}
     */
    public void send(T object) {
        m_transactor.send(object);
    }

    /**
     * 发送传输请求
     */
    public void request() {
        m_transactor.send(new Request());
    }

    /**
     * 通知远端设备传输请求失败
     */
    public void notifyRequestFailed() {
        m_transactor.send(new RequestFailed());
    }

    /**
     * 连接 {@link com.ingenic.iwds.app.ConnectionHelper#start()} 互连
     * {@link com.ingenic.iwds.common.api.ServiceManagerContext#SERVICE_CONNECTION} 服务
     *
     * @see com.ingenic.iwds.app.ConnectionHelper
     * @see com.ingenic.iwds.common.api.ServiceClient
     * @see com.ingenic.iwds.common.api.ServiceManagerContext
     */
    public void start() {
        m_transactor.start();
    }

    /**
     * 检查传输服务是否启动.
     *
     * @return 如果已经启动，返回{@code true}.
     */
    public boolean isStarted()
    {
        return m_transactor.isStarted();
    }

    /**
     * 断开 {@link com.ingenic.iwds.app.ConnectionHelper#stop()} 互连
     * {@link com.ingenic.iwds.common.api.ServiceManagerContext#SERVICE_CONNECTION} 服务
     *
     * @see com.ingenic.iwds.app.ConnectionHelper
     * @see com.ingenic.iwds.common.api.ServiceClient
     * @see com.ingenic.iwds.common.api.ServiceManagerContext
     */
    public void stop() {
        m_transactor.stop();
    }

    /**
     * 数据传输回调接口, 用于更新设备连接和数据传输状态
     *
     * @param <T>
     *        数据类型
     */
    public interface ProviderTransactionModelCallback<T> {

        /**
         * 数据传输请求回调
         *
         * @see ProviderTransactionModel#request()
         */
        public void onRequest();

        /**
         * 远端设备通知传输失败回调
         *
         * @see ProviderTransactionModel#notifyRequestFailed()
         */
        public void onRequestFailed();

        /**
         * 数据接收完成回调
         * @param object
         *        接收到的数据
         */
        public void onObjectArrived(T object);

        /**
         * {@code link} 连接状态变化回调, 参考
         * {@link com.ingenic.iwds.app.ConnectionHelper#onConnected(ServiceClient)}
         */
        public void onLinkConnected(DeviceDescriptor descriptor,
                boolean isConnected);

        /**
         * {@code channel} 状态变化回调, 仅当通道可用时才能进行数据传输
         *
         * @param isAvailable
         *        true 通道可用, false 通道不可用
         */
        public void onChannelAvailable(boolean isAvailable);

        /**
         * 发送结果回调, 用于检查发送是否成功, 注意每次发送之前需要检查上次发送是否成功
         * @param result
         *        {@link com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult}
         */
        public void onSendResult(DataTransactResult result);
    }

    /* ------------------ public API end -------------------- */

    private ProviderTransactionModelCallback<T> m_callback;

    private DataTransactor m_transactor;

    /**
     * 数据传输请求类 {@link com.ingenic.iwds.datatransactor.ProviderTransactionModel#request()}
     */
    public static class Request implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = -8398132141094564822L;

    }

    /**
     * 请求失败类 {@link com.ingenic.iwds.datatransactor.ProviderTransactionModel#notifyRequestFailed()}
     */
    public static class RequestFailed implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = -130038790971468020L;

    }

    /**
     * {@link com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback
     * DataTransactorCallback} 接口定义, 不要使用
     */
    @Override
    public void onLinkConnected(DeviceDescriptor descriptor, boolean isConnected) {
        m_callback.onLinkConnected(descriptor, isConnected);
    }

    /**
     * {@link com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback
     * DataTransactorCallback} 接口定义, 不要使用
     */
    @Override
    public void onChannelAvailable(boolean isAvailable) {
        m_callback.onChannelAvailable(isAvailable);
    }

    /**
     * {@link com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback
     * DataTransactorCallback} 接口定义, 不要使用
     */
    @Override
    public void onSendResult(DataTransactResult result) {
        m_callback.onSendResult(result);
    }

    /**
     * {@link com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback
     * DataTransactorCallback} 接口定义, 不要使用
     */
    @Override
    public void onDataArrived(Object object) {
        if (object instanceof Request)
            m_callback.onRequest();
        else if (object instanceof RequestFailed)
            m_callback.onRequestFailed();
        else
            m_callback.onObjectArrived((T) object);
    }

    /**
     * 仅供 {@link com.ingenic.iwds.datatransactor.FileTransactionModel#FileTransactionModel
     * FileTransactionModel} 使用
     */
    @Override
    public void onSendFileProgress(int progress) {

    }

    /**
     * 仅供 {@link com.ingenic.iwds.datatransactor.FileTransactionModel#FileTransactionModel
     * FileTransactionModel} 使用
     */
    @Override
    public void onRecvFileProgress(int progress) {

    }

    @Override
    public void onSendFileInterrupted(int index) {

    }

    @Override
    public void onRecvFileInterrupted(int index) {

    }
}
