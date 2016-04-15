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

package com.ingenic.iwds.datatransactor.elf;

import java.io.Serializable;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.datatransactor.DataTransactor;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback;
import com.ingenic.iwds.utils.IwdsAssert;

/**
 * 摄像头传输模型类.
 */
public class CameraTransactionModel implements DataTransactorCallback {

    /* ------------------ public API -------------------- */

    /**
     * 实例化摄像头传输模型对象.
     *
     * @param context
     *            应用的上下文
     * @param callback
     *            回调
     * @param uuid
     *            UUID
     */
    public CameraTransactionModel(Context context,
            CameraTransactionModelCallback callback, String uuid) {
        IwdsAssert.dieIf(this, callback == null, "Callback is null.");

        m_callback = callback;
        m_transactor = new DataTransactor(context, this, uuid);
    }

    /**
     * 发送一帧图像.
     *
     * @param frame
     *            帧信息
     */
    public void send(CameraFrameInfo frame) {
        m_transactor.send(frame);
    }

    /**
     * 请求开始预览.
     *
     * @param sizeInfo
     *            预览大小信息
     */
    public void requestStartPreview(CameraPreviewSizeInfo sizeInfo) {
        IwdsAssert.dieIf(this, sizeInfo == null, "Size info is null.");

        m_transactor.send(new RequestStartPreview(sizeInfo));
    }

    /**
     * 请求停止预览.
     */
    public void requestStopPreview() {
        m_transactor.send(new RequestStopPreview());
    }

    /**
     * 请求拍照.
     */
    public void requestTakePicture() {
        m_transactor.send(new RequestTakePicture());
    }

    /**
     * 通知请求开始预览失败.
     */
    public void notifyRequestStartPreviewFailed() {
        m_transactor.send(new RequestStartPreviewFailed());
    }

    /**
     * 通知请求停止预览失败.
     */
    public void notifyRequestStopPreviewFailed() {
        m_transactor.send(new RequestStopPreviewFailed());
    }

    /**
     * 通知请求拍照失败.
     */
    public void notifyRequestTakePictureFailed() {
        m_transactor.send(new RequestTakePictureFailed());
    }

    /**
     * 通知拍照成功.
     *
     * @param frame
     *            帧信息
     */
    public void notifyTakePictureDone(CameraFrameInfo frame) {
        m_transactor.send(new TakePictureDone(frame));
    }

    /**
     * 启动传输服务
     */
    public void start() {
        m_transactor.start();
    }

    /**
     * 停止传输服务
     */
    public void stop() {
        m_transactor.stop();
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
     * 摄像头传输模型的回调接口.
     */
    public interface CameraTransactionModelCallback {

        /**
         * 当接收到开始预览请求的回调.
         *
         * @param sizeInfo
         *            预览大小信息
         */
        public void onRequestStartPreview(CameraPreviewSizeInfo sizeInfo);

        /**
         * 当接收到停止预览请求的回调.
         */
        public void onRequestStopPreview();

        /**
         * 当接收到拍照请求的回调.
         */
        public void onRequestTakePicture();

        /**
         * 当请求开始预览失败时的回调.
         */
        public void onRequestStartPreviewFailed();

        /**
         * 当请求停止预览失败时的回调.
         */
        public void onRequestStopPreviewFailed();

        /**
         * 当请求拍照失败时的回调.
         */
        public void onRequestTakePictureFailed();

        /**
         * 当拍照成功时的回调.
         *
         * @param frame
         *            帧信息
         */
        public void onTakePictureDone(CameraFrameInfo frame);

        /**
         * 当接收到CameraFrameInfo对象的回调.
         *
         * @param frame
         *            帧信息
         */
        public void onObjectArrived(CameraFrameInfo frame);

        /**
         * 当{@code link}状态变化时的回调.
         *
         * @param descriptor
         *            设备描述符
         * @param isConnected
         *            是否连接上，如果连接成功，值为{@code true}
         */
        public void onLinkConnected(DeviceDescriptor descriptor,
                boolean isConnected);

        /**
         * 通道状态变化的回调.
         *
         * @param isAvailable
         *            通道是否可用, 如果可用, 值为{@code true}
         */
        public void onChannelAvailable(boolean isAvailable);

        /**
         * 当收到发送结果时的回调.
         *
         * @param result
         *            发送结果
         */
        public void onSendResult(DataTransactResult result);
    }

    /* ------------------ public API end -------------------- */

    private CameraTransactionModelCallback m_callback;

    private DataTransactor m_transactor;

    public static class RequestStartPreview implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = -5609409498789811806L;

        public CameraPreviewSizeInfo sizeInfo;

        public RequestStartPreview(CameraPreviewSizeInfo info) {
            sizeInfo = info;
        }
    }

    public static class RequestStopPreview implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 2465019248251532073L;

    }

    public static class RequestTakePicture implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 4843107640702082657L;

    }

    public static class TakePictureDone implements Parcelable {
        public CameraFrameInfo frame;

        public TakePictureDone() {

        }

        public TakePictureDone(CameraFrameInfo Frame) {
            frame = Frame;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            frame.writeToParcel(dest, flags);
        }

        public static final Creator<TakePictureDone> CREATOR = new Creator<TakePictureDone>() {
            @Override
            public TakePictureDone createFromParcel(Parcel source) {
                TakePictureDone done = new TakePictureDone();

                done.frame = CameraFrameInfo.CREATOR.createFromParcel(source);

                return done;
            }

            @Override
            public TakePictureDone[] newArray(int size) {
                return new TakePictureDone[size];
            }
        };
    }

    public static class RequestStartPreviewFailed implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 8110140648716686091L;

    }

    public static class RequestStopPreviewFailed implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = -4042505134553328080L;

    }

    public static class RequestTakePictureFailed implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = -1074205716986715196L;

    }

    @Override
    public void onLinkConnected(DeviceDescriptor descriptor, boolean isConnected) {
        m_callback.onLinkConnected(descriptor, isConnected);
    }

    @Override
    public void onChannelAvailable(boolean isAvailable) {
        m_callback.onChannelAvailable(isAvailable);
    }

    @Override
    public void onSendResult(DataTransactResult result) {
        m_callback.onSendResult(result);
    }

    @Override
    public void onDataArrived(Object object) {
        if (object instanceof RequestStartPreview)
            m_callback
                    .onRequestStartPreview(((RequestStartPreview) object).sizeInfo);

        else if (object instanceof RequestStopPreview)
            m_callback.onRequestStopPreview();

        else if (object instanceof RequestTakePicture)
            m_callback.onRequestTakePicture();

        else if (object instanceof RequestTakePictureFailed)
            m_callback.onRequestTakePictureFailed();

        else if (object instanceof RequestStartPreviewFailed)
            m_callback.onRequestStartPreviewFailed();

        else if (object instanceof RequestStopPreviewFailed)
            m_callback.onRequestStopPreviewFailed();

        else if (object instanceof TakePictureDone)
            m_callback
                    .onTakePictureDone((CameraFrameInfo) (((TakePictureDone) object).frame));

        else
            m_callback.onObjectArrived((CameraFrameInfo) object);
    }

    @Override
    public void onSendFileProgress(int progress) {

    }

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
