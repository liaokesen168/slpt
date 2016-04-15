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
import java.io.IOException;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.app.ConnectionHelper;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.exception.FileTransferException;
import com.ingenic.iwds.common.exception.LinkDisconnectedException;
import com.ingenic.iwds.common.exception.LinkUnbondedException;
import com.ingenic.iwds.common.exception.PortClosedException;
import com.ingenic.iwds.common.exception.PortDisconnectedException;
import com.ingenic.iwds.common.exception.SerializeException;
import com.ingenic.iwds.os.SafeParcelable;
import com.ingenic.iwds.uniconnect.Connection;
import com.ingenic.iwds.uniconnect.ConnectionServiceManager;
import com.ingenic.iwds.uniconnect.UniconnectErrorCode;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;
import com.ingenic.iwds.utils.serializable.TransferAdapter;
import com.ingenic.iwds.utils.serializable.TransferAdapter.TransferAdapterCallback;

/**
 * 数据传输管理类, 实现通用数据类型的发送和接收。 其中回调接口
 * {@link com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback
 * DataTransactorCallback} 用于更新连接和数据通道状态、告知数据发送结果、通知有数据接收以及通知文件发送和接收的进度
 *
 * @see DataTransactorCallback
 * @see ConnectionHelper
 * @see TransferAdapter
 */
public class DataTransactor extends
        TransactorParcelableCreator<Parcelable, SafeParcelable> {
    private String m_uuid;
    private Helper m_helper;
    private CallbackHandler m_callbackHandler;

    private Reader m_reader;
    private Sender m_sender;
    private Controller m_controller;
    private TransferAdapterObserver m_tranfserAdapterObserver;
    private ObjectExchangeCallback m_obexCallback;

    /* ---------------------- public API -------------------------- */

    /**
     * 构造 {@code DataTransactor}
     * 
     * @param context
     *            应用的上下文
     * @param callback
     *            回调接口
     *            {@link com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback
     *            DataTransactorCallback}
     *            用于更新连接和数据通道状态、告知数据发送结果、通知有数据接收以及通知文件发送和接收的进度
     * @param uuid
     *            UUID
     * @see DataTransactorCallback
     */
    public DataTransactor(Context context, DataTransactorCallback callback,
            String uuid) {
        IwdsAssert.dieIf(this, callback == null, "Callback is null.");

        IwdsAssert.dieIf(this, uuid == null || uuid.isEmpty(),
                "Uuid is null or empty.");

        m_uuid = uuid;
        m_helper = new Helper(context);
        m_callbackHandler = new CallbackHandler(callback);

        m_reader = new Reader();
        m_sender = new Sender();
        m_controller = new Controller();

        m_tranfserAdapterObserver = new TransferAdapterObserver();
    }

    /**
     * 构造 {@code DataTransactor}
     * 
     * @param context
     *            应用的上下文
     * @param tranactorCallback
     *            回调接口
     *            {@link com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback
     *            DataTransactorCallback}
     *            用于更新连接和数据通道状态、告知数据发送结果、通知有数据接收以及通知文件发送和接收的进度
     * @param obexCallback
     *            回调接口
     *            {@link com.ingenic.iwds.datatransactor.DataTransactor.ObjectExchangeCallback
     *            ObjectExchangeCallback} 用于对象的发送和接收
     * @param uuid
     *            UUID
     * @see DataTransactorCallback
     * @see ObjectExchangeCallback
     */
    public DataTransactor(Context context,
            DataTransactorCallback tranactorCallback,
            ObjectExchangeCallback obexCallback, String uuid) {

        IwdsAssert.dieIf(this, tranactorCallback == null,
                "DataTransactorCallback is null.");
        IwdsAssert.dieIf(this, obexCallback == null,
                "ObjectExchangeCallback is null.");

        IwdsAssert.dieIf(this, uuid == null || uuid.isEmpty(),
                "Uuid is null or empty.");

        m_uuid = uuid;
        m_helper = new Helper(context);
        m_callbackHandler = new CallbackHandler(tranactorCallback);

        m_reader = new Reader();
        m_sender = new Sender();
        m_controller = new Controller();

        m_obexCallback = obexCallback;
    }

    /**
     * 启动传输服务.
     */
    public void start() {
        m_helper.start();
    }

    /**
     * 停止传输服务.
     */
    public void stop() {
        m_helper.stop();
    }

    /**
     * 检查传输服务是否启动.
     *
     * @return 如果已经启动，返回{@code true}.
     */
    public boolean isStarted() {
        return m_helper.isStarted();
    }

    /**
     * 取消所有未处理的传输，该操作会阻塞调用线程
     */
    public void cancelAll() {
        m_controller.cancelAll();
    }

    /**
     * 发送数据
     * 
     * @param object
     *            待发送的数据
     *            {@link com.ingenic.iwds.utils.serializable.TransferAdapter
     *            TransferAdapter}
     */
    public void send(Object object) {
        IwdsAssert.dieIf(this, object == null, "Object is null.");
        IwdsAssert.dieIf(this, object instanceof File,
                "Object can not be file.");

        m_controller.send(object);
    }

    void send(File file, int index) {
        m_controller.send(file, index);
    }

    void send(File file) {
        this.send(file, 0);
    }

    /**
     * 对象序列化传输接口
     */
    public interface ObjectExchangeCallback {
        /**
         * 发送通用对象 {@code object} 到远端设备
         * 
         * @param connection
         *            已经建立连接的{@code connection}
         * @param object
         *            待发送的对象
         * @throws SerializeException
         *             序列化异常
         * @throws IOException
         *             {@code connection} IO异常
         * @see Connection
         */
        public void send(Connection connection, Object object)
                throws SerializeException, IOException;

        /**
         * 从远端设备接收对象 {@code object}
         * 
         * @param connection
         *            已经建立连接的{@code connection}
         * @return 接收到的对象 {@code Object}
         * @throws SerializeException
         *             序列化异常
         * @throws IOException
         *             {@code connection} IO异常
         * @see Connection
         */
        public Object recv(Connection connection) throws SerializeException,
                IOException;
    }

    /**
     * 数据传输回调接口, 用于更新设备连接和数据传输状态
     */
    public interface DataTransactorCallback {

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
         *            true 通道可用, false 通道不可用
         */
        public void onChannelAvailable(boolean isAvailable);

        /**
         * 发送结果回调, 用于检查发送是否成功, 注意每次发送之前需要检查上次发送结果是否成功
         * 
         * @param result
         *            {@link com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult}
         */
        public void onSendResult(DataTransactResult result);

        /**
         * 数据接收完成回调
         * 
         * @param object
         *            接收到的数据
         */
        public void onDataArrived(Object object);

        /**
         * 文件发送进度变化回调
         * 
         * @param progress
         *            发送进度
         */
        public void onSendFileProgress(int progress);

        /**
         * 文件接收进度变化回调
         * 
         * @param progress
         *            接收进度
         */
        public void onRecvFileProgress(int progress);

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

    /**
     * 传输结果类, 用于保存传输结果和传输的数据
     */
    public class DataTransactResult {
        /**
         * 描述传输成功的常量
         */
        public final static int RESULT_OK = 0;

        /**
         * 描述 {@code channel} 不可用的常量
         */
        public final static int RESULT_FAILED_CHANNEL_UNAVAILABLE = 1;

        /**
         * 描述 {@code link} 断开的常量
         */
        public final static int RESULT_FAILED_LINK_DISCONNECTED = 2;

        /**
         * 描述 {@code IWDS} 服务崩溃的常量
         */
        public final static int RESULT_FAILED_IWDS_CRASH = 3;

        private int m_resultCode;
        private Object m_transferedObject;

        /**
         * 构造 {@code DataTransactResult}
         * 
         * @param transferedObject
         *            传输对象
         * @param resultCode
         *            传输结果
         */
        DataTransactResult(Object transferedObject, int resultCode) {
            m_transferedObject = transferedObject;
            m_resultCode = resultCode;
        }

        /**
         * 获取传输结果编码 {@link DataTransactResult#RESULT_OK}
         * {@link DataTransactResult#RESULT_FAILED_CHANNEL_UNAVAILABLE}
         * {@link DataTransactResult#RESULT_FAILED_LINK_DISCONNECTED}
         * {@link DataTransactResult#RESULT_FAILED_IWDS_CRASH}
         *
         * @return 传输结果编码
         */
        public int getResultCode() {
            return m_resultCode;
        }

        /**
         * 获取传输对象
         * 
         * @return 传输对象
         */
        public Object getTransferedObject() {
            return m_transferedObject;
        }
    }

    /* --------------------- public API end ----------------------- */

    /* --------------------- can override API --------------------- */

    /**
     * 通过 {@link com.ingenic.iwds.DeviceDescriptor DeviceDescriptor} 过滤需要的设备
     * 
     * @param descriptor
     *            设备描述符 {@link com.ingenic.iwds.DeviceDescriptor
     *            DeviceDescriptor}
     * @return 过滤后需要的设备描述符
     */
    protected DeviceDescriptor filerDeviceDescriptor(DeviceDescriptor descriptor) {
        return descriptor;
    }

    /* ------------------- can override API end ------------------- */

    private class CallbackHandler extends Handler {
        public static final int MSG_NOTIFY_LINK_STATUS = 1;
        public static final int MSG_NOTIFY_CHANNLE_STATUS = 2;
        public static final int MSG_NOTIFY_SEND_RESULT = 3;
        public static final int MSG_NOTIFY_DATA_ARRIVED = 4;
        public static final int MSG_NOTIFY_FILE_TRANSACTION_PROGRESS = 5;
        public static final int MSG_NOTIFY_FILE_TRANSACTION_INTERRUPTED = 6;

        private DataTransactorCallback m_callback;

        CallbackHandler(DataTransactorCallback callback) {
            m_callback = callback;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_NOTIFY_LINK_STATUS:
                m_callback.onLinkConnected((DeviceDescriptor) msg.obj,
                        msg.arg1 == 0 ? false : true);

                break;

            case MSG_NOTIFY_CHANNLE_STATUS:
                m_callback.onChannelAvailable(msg.arg1 == 0 ? false : true);

                break;

            case MSG_NOTIFY_SEND_RESULT:
                m_callback.onSendResult((DataTransactResult) msg.obj);

                break;

            case MSG_NOTIFY_DATA_ARRIVED:
                m_callback.onDataArrived(msg.obj);

                break;

            case MSG_NOTIFY_FILE_TRANSACTION_PROGRESS:
                if (msg.arg1 == 1)
                    m_callback.onSendFileProgress(msg.arg2);
                else
                    m_callback.onRecvFileProgress(msg.arg2);

                break;

            case MSG_NOTIFY_FILE_TRANSACTION_INTERRUPTED:
                if (msg.arg1 == 1)
                    m_callback.onSendFileInterrupted(msg.arg2);
                else
                    m_callback.onRecvFileInterrupted(msg.arg2);

                break;

            default:
                IwdsAssert.dieIf(this, true, "Implement me.");

                break;
            }
        }

        public void notifyLinkConnected(DeviceDescriptor descriptor,
                boolean connected) {
            Message msg = Message.obtain(this);

            msg.what = MSG_NOTIFY_LINK_STATUS;
            msg.arg1 = connected ? 1 : 0;
            msg.obj = descriptor;

            msg.sendToTarget();
        }

        public void notifyConnectionConnected(boolean connected) {
            Message msg = Message.obtain(this);

            msg.what = MSG_NOTIFY_CHANNLE_STATUS;
            msg.arg1 = connected ? 1 : 0;

            msg.sendToTarget();
        }

        public void notifySendResult(DataTransactResult result) {
            Message msg = Message.obtain(this);

            msg.what = MSG_NOTIFY_SEND_RESULT;
            msg.obj = result;

            msg.sendToTarget();
        }

        public void notifyDataArrived(Object object) {
            Message msg = Message.obtain(this);

            msg.what = MSG_NOTIFY_DATA_ARRIVED;
            msg.obj = object;

            msg.sendToTarget();
        }

        public void notifyFileTransactionProgress(int progress, boolean isSend) {
            Message msg = Message.obtain(this);

            msg.what = MSG_NOTIFY_FILE_TRANSACTION_PROGRESS;
            msg.arg1 = isSend ? 1 : 0;
            msg.arg2 = progress;

            msg.sendToTarget();
        }

        public void notifyFileTransactionInterrupted(int index, boolean isSend) {
            Message msg = Message.obtain(this);

            msg.what = MSG_NOTIFY_FILE_TRANSACTION_INTERRUPTED;
            msg.arg1 = isSend ? 1 : 0;
            msg.arg2 = index;

            msg.sendToTarget();
        }
    }

    private class Sender {
        public static final int MSG_SEND = 0;
        public static final int MSG_QUIT = 1;

        private Connection m_connection;

        private HandlerThread m_thread;
        private SendHandler m_handler;

        public void start(Connection connection) {
            if (m_thread != null)
                return;

            m_connection = connection;

            m_thread = new HandlerThread("DataTransactor_sender: UUID: "
                    + m_uuid + ", Port: " + m_connection.getPort());
            m_thread.start();

            m_handler = new SendHandler(m_thread.getLooper());
        }

        public void stop() {
            if (m_thread == null)
                return;

            Message.obtain(m_handler, MSG_QUIT).sendToTarget();
            try {
                m_thread.join();
            } catch (InterruptedException e) {
                /*
                 * ignore
                 */
            }

            m_handler = null;
            m_thread = null;

        }

        public void send(Object object) {
            if (m_thread == null)
                return;

            Message msg = Message.obtain(m_handler);

            msg.what = MSG_SEND;
            msg.obj = object;

            msg.sendToTarget();
        }

        public void send(File file, int index) {
            if (m_thread == null)
                return;

            Message msg = Message.obtain(m_handler);

            msg.what = MSG_SEND;
            msg.arg1 = index;
            msg.obj = file;

            msg.sendToTarget();
        }

        public void cancelAll() {
            if (m_thread == null)
                return;

            m_handler.removeMessages(MSG_SEND);
        }

        public class SendHandler extends Handler {
            public SendHandler(Looper looper) {
                super(looper);
            }

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_SEND:
                    DataTransactResult result = null;
                    Object object = msg.obj;

                    try {
                        if (m_obexCallback != null) {
                            m_obexCallback.send(m_connection, object);

                        } else {
                            if (object instanceof File)
                                TransferAdapter.send(m_connection,
                                        (File) object, msg.arg1,
                                        m_tranfserAdapterObserver);
                            else
                                TransferAdapter.send(m_connection, object,
                                        m_parcelableCreator,
                                        m_safeParcelableCreator);
                        }

                        result = new DataTransactResult(object,
                                DataTransactResult.RESULT_OK);

                    } catch (SerializeException e) {
                        IwdsAssert.dieIf(this, true, "Serialize exception: "
                                + e);

                    } catch (IOException e) {
                        Throwable cause = e.getCause();
                        if (cause instanceof LinkDisconnectedException
                                || cause instanceof LinkUnbondedException) {
                            result = new DataTransactResult(
                                    object,
                                    DataTransactResult.RESULT_FAILED_LINK_DISCONNECTED);

                        } else if (cause instanceof PortDisconnectedException
                                || cause instanceof PortClosedException) {
                            result = new DataTransactResult(
                                    object,
                                    DataTransactResult.RESULT_FAILED_CHANNEL_UNAVAILABLE);

                        } else if (cause instanceof RemoteException) {
                            result = new DataTransactResult(object,
                                    DataTransactResult.RESULT_FAILED_IWDS_CRASH);
                        }
                    }

                    m_callbackHandler.notifySendResult(result);

                    break;

                case MSG_QUIT:
                    m_thread.quit();

                    break;
                }
            }
        }

    }

    private class Reader {
        private DeviceDescriptor m_deviceDescriptor;
        private Connection m_connection;

        private ReadThread m_thread;

        public void start(DeviceDescriptor descriptor, Connection connection) {
            IwdsAssert.dieIf(this, m_thread != null,
                    "Reader was already started.");

            m_deviceDescriptor = descriptor;
            m_connection = connection;

            m_thread = new ReadThread();
            m_thread.start();
        }

        public void stop() {
            IwdsAssert.dieIf(this, m_thread == null,
                    "Reader was already stopped.");

            m_thread.stopAndWait();

            m_thread = null;
            m_deviceDescriptor = null;
            m_connection = null;
        }

        private class ReadThread extends Thread {
            private Object m_isRunningLock = new Object();

            private boolean m_isRunning = false;
            private boolean m_requestStop = false;

            public ReadThread() {
                super("DataTransactor_reader: UUID: " + m_uuid);
            }

            public void start() {
                super.start();

                synchronized (m_isRunningLock) {
                    while (!isRunning()) {
                        try {
                            m_isRunningLock.wait();
                        } catch (InterruptedException e) {
                            /*
                             * ignore
                             */
                        }
                    }
                }
            }

            public void stopAndWait() {
                if (!isRunning())
                    return;

                closeConnection();

                synchronized (this) {
                    m_requestStop = true;
                }

                try {
                    join();
                } catch (InterruptedException e) {
                    /*
                     * ignore
                     */
                }
            }

            private boolean isRunning() {
                synchronized (m_isRunningLock) {
                    return m_isRunning;
                }
            }

            private void setRunning(boolean isRunning) {
                synchronized (m_isRunningLock) {
                    m_isRunning = isRunning;

                    m_isRunningLock.notifyAll();
                }
            }

            private boolean openConnection() {
                synchronized (this) {
                    int error = m_connection.open(m_deviceDescriptor, m_uuid);

                    IwdsAssert.dieIf(this,
                            error == UniconnectErrorCode.EPORTBUSY,
                            "Uuid was conflict.");

                    if (error < 0)
                        return false;

                    return true;
                }
            }

            private void closeConnection() {
                synchronized (this) {
                    m_connection.close();
                }
            }

            @Override
            public void run() {
                setRunning(true);

                m_callbackHandler.notifyLinkConnected(m_deviceDescriptor, true);

                Throwable cause = null;

                for (;;) {
                    synchronized (this) {
                        if (m_requestStop)
                            break;
                    }

                    /*
                     * Here open connection
                     */
                    boolean ok = openConnection();
                    if (!ok)
                        break;

                    /*
                     * Here update thread name
                     */
                    setName("DataTransactor_reader: UUID: " + m_uuid
                            + ", Port: " + m_connection.getPort());

                    /*
                     * Here do handshake
                     */
                    if (m_connection.handshake() != UniconnectErrorCode.NOERROR) {
                        closeConnection();

                        break;
                    }

                    /*
                     * Here ready to send and notify user
                     */
                    m_controller.setReadyToSend(true);
                    m_callbackHandler.notifyConnectionConnected(true);

                    /*
                     * Here start blocking read
                     */
                    for (;;) {
                        try {
                            Object object = null;

                            if (m_obexCallback != null) {
                                object = m_obexCallback.recv(m_connection);

                            } else {
                                object = TransferAdapter.recv(m_connection,
                                        m_parcelableCreator,
                                        m_safeParcelableCreator,
                                        m_tranfserAdapterObserver);

                            }
                            m_callbackHandler.notifyDataArrived(object);

                        } catch (SerializeException e) {
                            IwdsAssert.dieIf(this, true,
                                    "Serialize exception: " + e);

                        } catch (FileTransferException e) {
                            m_callbackHandler.notifyDataArrived(e);
                            break;

                        } catch (IOException e) {
                            cause = e.getCause();

                            break;
                        }
                    }

                    /*
                     * Here fail path notify user channel lost
                     */
                    m_callbackHandler.notifyConnectionConnected(false);
                    m_controller.setReadyToSend(false);

                    /*
                     * Here must close
                     */
                    closeConnection();

                    /*
                     * Here link lost or user stop break thread
                     */
                    if (cause instanceof LinkDisconnectedException
                            || cause instanceof LinkUnbondedException
                            || cause instanceof PortClosedException
                            || cause instanceof RemoteException)
                        break;

                } // end for

                m_callbackHandler
                        .notifyLinkConnected(m_deviceDescriptor, false);

                setRunning(false);
            } // end run
        }
    }

    private void startReader(DeviceDescriptor descriptor, Connection connection) {
        m_reader.start(descriptor, connection);
    }

    private void stopReader() {
        m_reader.stop();
    }

    private void startSender(Connection connection) {
        m_sender.start(connection);
    }

    private void stopSender() {
        m_sender.stop();
    }

    private class Controller {
        private ConnectionServiceManager m_service;
        private DeviceDescriptor m_deviceDescriptor;
        private Connection m_connection;

        private boolean m_readyToSend = false;

        public void start(ConnectionServiceManager service) {
            m_service = service;
            m_connection = m_service.createConnection();
        }

        public void stop() {
            m_connection = null;
            m_service = null;
        }

        public void send(Object object) {
            synchronized (this) {
                if (!m_readyToSend) {
                    DataTransactResult result = new DataTransactResult(
                            object,
                            DataTransactResult.RESULT_FAILED_CHANNEL_UNAVAILABLE);

                    m_callbackHandler.notifySendResult(result);

                    return;
                }

                m_sender.send(object);
            }
        }

        public void send(File file, int index) {
            synchronized (this) {
                if (!m_readyToSend) {
                    DataTransactResult result = new DataTransactResult(
                            file,
                            DataTransactResult.RESULT_FAILED_CHANNEL_UNAVAILABLE);

                    m_callbackHandler.notifySendResult(result);

                    return;
                }

                m_sender.send(file, index);
            }
        }

        public void cancelAll() {
            synchronized (this) {
                m_sender.cancelAll();
            }
        }

        private void handleDeviceConnected(DeviceDescriptor descriptor) {
            if (m_deviceDescriptor != null) {
                /*
                 * A device has been selected.
                 */
                return;
            }

            DeviceDescriptor temp = filerDeviceDescriptor(descriptor);
            if (temp == null) {
                /*
                 * You are not the one.
                 */
                return;
            }

            /*
             * Selected device.
             */
            m_deviceDescriptor = temp;

            /*
             * Let's rock.
             */
            startReader(m_deviceDescriptor, m_connection);
        }

        private void handleDeviceDisconnected(DeviceDescriptor descriptor) {
            if (!descriptor.equals(m_deviceDescriptor)) {
                /*
                 * Not the selected device.
                 */
                return;
            }

            stopReader();

            m_deviceDescriptor = null;
        }

        public void setReadyToSend(boolean isReady) {
            synchronized (this) {
                if (isReady) {
                    /*
                     * Here handshake was accepted, start sender
                     */
                    startSender(m_connection);

                    m_readyToSend = true;

                } else {
                    m_readyToSend = false;

                    /*
                     * Here stop sender
                     */
                    stopSender();
                }
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        stop();
    }

    private class Helper extends ConnectionHelper {
        public Helper(Context context) {
            super(context);
        }

        @Override
        public void onServiceConnected(
                ConnectionServiceManager connectionServiceManager) {
            m_controller.start(connectionServiceManager);
        }

        @Override
        public void onServiceDisconnected(boolean unexpected) {
            m_controller.stop();
        }

        @Override
        public void onConnectedDevice(DeviceDescriptor deviceDescriptor) {
            m_controller.handleDeviceConnected(deviceDescriptor);
        }

        @Override
        public void onDisconnectedDevice(DeviceDescriptor deviceDescriptor) {
            m_controller.handleDeviceDisconnected(deviceDescriptor);
        }
    }

    private class TransferAdapterObserver implements TransferAdapterCallback {
        @Override
        public void onSendFileProgress(long currentBytes, long totalBytes) {
            int progress = (int) ((100 * currentBytes) / totalBytes);
            IwdsLog.i(this, "File send progress: " + progress);
            m_callbackHandler.notifyFileTransactionProgress(progress, true);
        }

        @Override
        public void onRecvFileProgress(long currentBytes, long totalBytes) {
            int progress = (int) ((100 * currentBytes) / totalBytes);
            IwdsLog.i(this, "File recv progress: " + progress);
            m_callbackHandler.notifyFileTransactionProgress(progress, false);
        }

        @Override
        public void onSendFileInterrupted(int index) {
            IwdsLog.i(this, "File send interrupted index: " + index);
            m_callbackHandler.notifyFileTransactionInterrupted(index, true);
        }

        @Override
        public void onRecvFileInterrupted(int index) {
            IwdsLog.i(this, "File recv interrupted index: " + index);
            m_callbackHandler.notifyFileTransactionInterrupted(index, false);
        }
    }
}
