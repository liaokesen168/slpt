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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;

import android.content.Context;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.common.exception.FileStatusException;
import com.ingenic.iwds.common.exception.FileTransferException;
import com.ingenic.iwds.common.exception.SDCardFullException;
import com.ingenic.iwds.common.exception.SDCardNotMountedException;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.serializable.UtilsConstants;

/**
 * 文件传输模型, 实现了
 * {@link com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback
 * DataTransactorCallback} 接口, 用于更新连接和传输状态
 *
 * @see DataTransactor
 * @see DataTransactorCallback
 * @see FileInfo
 */
public class FileTransactionModel implements DataTransactorCallback {
    private FileTransactionModelCallback m_callback;

    protected DataTransactor m_transactor;
    private File m_file;
    private FileInfo m_fileInfo = new FileInfo();

    /**
     * 构造 {@code FileTransactionModel}
     * 
     * @param context
     *            应用的上下文
     * @param callback
     *            回调接口
     *            {@link com.ingenic.iwds.datatransactor.FileTransactionModel.FileTransactionModelCallback
     *            FileTransactionModelCallback}用于更新连接和传输状态
     * @param uuid
     *            UUID
     * @see FileTransactionModelCallback
     */
    public FileTransactionModel(Context context,
            FileTransactionModelCallback callback, String uuid) {
        IwdsAssert.dieIf(this, callback == null, "Callback is null.");

        m_callback = callback;
        m_transactor = new DataTransactor(context, this, uuid);
    }

    /**
     * 发送文件传输请求, 远端设备将收到传输请求
     * {@link FileTransactionModelCallback#onRequestSendFile(FileInfo)}
     * 
     * @param filePath
     *            文件路径
     * @throws FileNotFoundException
     *             文件未找到类型异常
     * @see FileTransactionModelCallback
     * @see FileInfo
     */
    public void requestSendFile(String filePath) throws FileNotFoundException {
        this.requestSendFile(filePath, 0);
    }

    /**
     * 发送文件传输请求, 远端设备将收到传输请求
     * {@link FileTransactionModelCallback#onRequestSendFile(FileInfo)}
     * 
     * @param filePath
     *            文件路径
     * @param index
     *            发送断点
     * @throws FileNotFoundException
     *             文件未找到类型异常
     * @see FileTransactionModelCallback
     * @see FileInfo
     */
    public void requestSendFile(String filePath, int index)
            throws FileNotFoundException {
        IwdsAssert.dieIf(this, filePath == null || filePath.isEmpty(),
                "File path is null or empty");

        File file = new File(filePath);
        if (file.isDirectory())
            throw new FileNotFoundException("File can not be a directory.");

        IwdsAssert.dieIf(this, !file.canRead(), "file can not be read");

        long length = file.length();
        if (length <= 0)
            throw new FileNotFoundException("File does not exist or is empty.");

        int chunkSize = UtilsConstants.SizeOf.FileChunk;
        int chunkCount = 0;
        if (length % chunkSize == 0)
            chunkCount = (int) (length / chunkSize);
        else
            chunkCount = (int) (length / chunkSize + 1);

        IwdsAssert
                .dieIf(this, index > chunkCount || index < 0, "Invalid index");

        m_fileInfo.name = file.getName();
        m_fileInfo.length = length;
        m_fileInfo.chunkIndex = index;
        m_fileInfo.chunkSize = chunkSize;

        m_file = file;

        m_transactor.send(new RequestSendFile(m_fileInfo));
    }

    /**
     * 通知远端设备: 确定接收文件
     * {@link FileTransactionModelCallback#onConfirmForReceiveFile}
     *
     * @see FileTransactionModelCallback
     */
    public void notifyConfirmForReceiveFile() {
        m_transactor.send(new ConfirmForReceiveFile());
    }

    /**
     * 通知远端设备: 取消接收文件
     * {@link FileTransactionModelCallback#onCancelForReceiveFile}
     *
     * @see FileTransactionModelCallback
     */
    public void notifyCancelForReceiveFile() {
        m_transactor.send(new CancelForReceiveFile());
    }

    /**
     * 连接 {@link com.ingenic.iwds.app.ConnectionHelper#start()} 互连
     * {@link com.ingenic.iwds.common.api.ServiceManagerContext#SERVICE_CONNECTION}
     * 服务
     *
     * @see com.ingenic.iwds.app.ConnectionHelper
     * @see com.ingenic.iwds.common.api.ServiceClient
     * @see com.ingenic.iwds.common.api.ServiceManagerContext
     */
    public void start() {
        m_transactor.start();
    }

    /**
     * 检查文件传输服务是否启动.
     *
     * @return 如果已经启动，返回{@code true}.
     *
     * @see com.ingenic.iwds.app.ConnectionHelper
     * @see com.ingenic.iwds.common.api.ServiceClient
     * @see com.ingenic.iwds.common.api.ServiceManagerContext
     */
    public boolean isStarted() {
        return m_transactor.isStarted();
    }

    /**
     * 断开 {@link com.ingenic.iwds.app.ConnectionHelper#stop()} 互连
     * {@link com.ingenic.iwds.common.api.ServiceManagerContext#SERVICE_CONNECTION}
     * 服务
     *
     * @see com.ingenic.iwds.app.ConnectionHelper
     * @see com.ingenic.iwds.common.api.ServiceClient
     * @see com.ingenic.iwds.common.api.ServiceManagerContext
     */
    public void stop() {
        m_transactor.stop();
    }

    /**
     * 文件传输回调接口, 用于更新设备连接和文件传输状态
     */
    public interface FileTransactionModelCallback {

        /**
         * 文件传输请求回调
         * 
         * @param info
         *            文件信息
         * @see FileTransactionModel#requestSendFile(String)
         */
        public void onRequestSendFile(FileInfo info);

        /**
         * {@code link} 连接状态变化回调, 参考
         * {@link com.ingenic.iwds.app.ConnectionHelper#onConnected(ServiceClient)}
         */
        public void onLinkConnected(DeviceDescriptor descriptor,
                boolean isConnected);

        /**
         * {@code channel} 状态变化回调, 仅当通道可用时才能进行文件传输
         *
         * @param isAvailable
         *            true 通道可用, false 通道不可用
         */
        public void onChannelAvailable(boolean isAvailable);

        /**
         * 发送结果回调, 用于检查发送是否成功, 注意每次发送之前需要检查上次发送是否成功
         * 
         * @param result
         *            {@link com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult}
         */
        public void onSendResult(DataTransactResult result);

        /**
         * 文件接收完成回调, 接收目录为
         * {@code Environment.getExternalStorageDirectory()/iwds}
         * 
         * @param file
         *            接收到的文件对象
         */
        public void onFileArrived(File file);

        /**
         * 发送进度回调
         * 
         * @param progress
         *            发送进度
         */
        public void onSendFileProgress(int progress);

        /**
         * 接收进度回调
         * 
         * @param progress
         *            接收进度
         */
        public void onRecvFileProgress(int progress);

        /**
         * 远端设备确认接收回调
         */
        public void onConfirmForReceiveFile();

        /**
         * 远端设备取消接收回调
         */
        public void onCancelForReceiveFile();

        /**
         * 文件接收错误回调
         */
        public void onFileTransferError(int errorCode);

        /**
         * 文件发送中断回调
         * 
         * @param index
         *            发送断点
         */
        public void onSendFileInterrupted(int index);

        /**
         * 文件接收中断回调
         * 
         * @param index
         *            接收断点
         */
        public void onRecvFileInterrupted(int index);
    }

    private static class RequestSendFile implements Serializable {
        private static final long serialVersionUID = 5098980108404157152L;

        private FileInfo info;

        public RequestSendFile(FileInfo info) {
            this.info = info;
        }
    }

    private static class ConfirmForReceiveFile implements Serializable {
        private static final long serialVersionUID = -7030085924484744118L;
    }

    private static class CancelForReceiveFile implements Serializable {
        private static final long serialVersionUID = 3611791257589864674L;
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
        if (object instanceof RequestSendFile) {
            m_callback.onRequestSendFile(((RequestSendFile) object).info);

        } else if (object instanceof ConfirmForReceiveFile) {
            m_transactor.send(m_file, m_fileInfo.chunkIndex);
            m_callback.onConfirmForReceiveFile();

        } else if (object instanceof CancelForReceiveFile) {
            m_callback.onCancelForReceiveFile();

        } else if (object instanceof File) {
            m_callback.onFileArrived((File) object);

        } else if (object instanceof FileTransferException) {
            Throwable cause = ((FileTransferException) object).getCause();

            if (cause instanceof FileStatusException) {
                m_callback
                        .onFileTransferError(FileTransferErrorCode.EFILESTATUS);

            } else if (cause instanceof SDCardNotMountedException) {
                m_callback.onFileTransferError(FileTransferErrorCode.ENOSDCARD);

            } else if (cause instanceof SDCardFullException) {
                m_callback
                        .onFileTransferError(FileTransferErrorCode.ESDCARDFULL);

            } else {
                IwdsAssert.dieIf(this, true, "Implement me.");
            }

        }
    }

    /**
     * {@link com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback
     * DataTransactorCallback} 接口定义, 不要使用
     */
    @Override
    public void onSendFileProgress(int progress) {
        m_callback.onSendFileProgress(progress);
    }

    /**
     * {@link com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback
     * DataTransactorCallback} 接口定义, 不要使用
     */
    @Override
    public void onRecvFileProgress(int progress) {
        m_callback.onRecvFileProgress(progress);
    }

    /**
     * {@link com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback
     * DataTransactorCallback} 接口定义, 不要使用
     */
    public void onSendFileInterrupted(int index) {
        m_callback.onSendFileInterrupted(index);
    }

    /**
     * {@link com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback
     * DataTransactorCallback} 接口定义, 不要使用
     */
    public void onRecvFileInterrupted(int index) {
        m_callback.onRecvFileInterrupted(index);
    }
}
