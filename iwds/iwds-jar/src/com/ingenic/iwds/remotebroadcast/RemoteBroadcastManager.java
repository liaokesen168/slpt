/*
 * Copyright (C) 2015 Ingenic Semiconductor
 * 
 * LiJingWen(Kevin) <kevin.jwli@ingenic.com>
 * 
 * Elf/IDWS Project
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.ingenic.iwds.remotebroadcast;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;

import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.utils.SimpleIDAlloter;

/**
 * 远程广播管理器，用于注册远程广播接收器，注销远程广播接收器，发送远程广播。
 */
public class RemoteBroadcastManager extends ServiceManagerContext {
    private static final String EXTRA_PERMISSION = "permission";

    private static final int MSG_CALLER_ERROR = 1;
    private static final int MSG_SEND_REMOTE_BROADCAST_RESULT = 2;
    private static final int MSG_ADD_RECEIVER_IN_ARRAY = 3;
    private static final int MSG_UNREGISTER_REMOTE_RECEIVER = 4;
    private static final int MSG_PERFORM_RECEIVE = 5;
    private static final int MSG_CLEAR_ALL_RECEIVERS = 6;
    private static final int MSG_QUIT_BACKGROUND = 7;

    /**
     * 结果为成功
     */
    public static final int RESULT_OK = DataTransactResult.RESULT_OK;
    /**
     * 失败：通道不可用
     */
    public static final int RESULT_FAILED_CHANNEL_UNAVAILABLE = DataTransactResult.RESULT_FAILED_CHANNEL_UNAVAILABLE;
    /**
     * 失败：连接已断开
     */
    public static final int RESULT_FAILED_LINK_DISCONNECTED = DataTransactResult.RESULT_FAILED_LINK_DISCONNECTED;
    /**
     * 失败：服务已停止
     */
    public static final int RESULT_FAILED_IWDS_CRASH = DataTransactResult.RESULT_FAILED_IWDS_CRASH;

    private IRemoteBroadcastService mService;
    private Handler mHandler;
    private int mCallerId = 0;
    private RemoteBroadcastCallback mCallback;
    private SparseArray<RemoteBroadcastReceiver> mReceivers;
    private HandlerThread mBroadcastThread;
    private Handler mBackground;
    private SimpleIDAlloter mIDAlloter;

    public RemoteBroadcastManager(Context context) {
        super(context);

        mIDAlloter = SimpleIDAlloter.newInstance();
        mHandler = new UIHandler(context.getMainLooper());

        m_serviceClientProxy = new ServiceClientProxy() {

            @Override
            public void onServiceDisconnected(boolean unexpected) {
                handleServiceDisconnected(unexpected);
            }

            @Override
            public void onServiceConnected(IBinder binder) {
                handleServiceConnected(binder);
            }

            @Override
            public IBinder getBinder() {
                return mService.asBinder();
            }
        };
    }

    private void initBackground() {
        if (mBroadcastThread == null) {
            mBroadcastThread = new HandlerThread("RemoteBroadcast");
        }
        mBroadcastThread.start();
        mBackground = new BroadcastHandler(mBroadcastThread.getLooper());
    }

    private void handleServiceConnected(IBinder binder) {
        initBackground();

        mService = IRemoteBroadcastService.Stub.asInterface(binder);
        if (mService != null) {
            mCallerId = mService.registerRemoteBroadcastCallback(new RemoteBroadcastCallbackStub());
        }
    }

    private void handleServiceDisconnected(boolean unexpected) {
        mBackground.obtainMessage(MSG_CLEAR_ALL_RECEIVERS, unexpected ? 1 : 0, -1).sendToTarget();

        if (mService != null) {
            mService.unregisterRemoteBroadcastCallback(mCallerId);
        }

        mBackground.sendEmptyMessage(MSG_QUIT_BACKGROUND);
    }

    private void clearReceivers(boolean unexpected) {
        if (mReceivers != null) {
            int N = mReceivers.size();

            if (N > 0) {
                if (mService != null && !unexpected) {
                    for (int i = 0; i < N; i++) {
                        int id = mReceivers.keyAt(i);
                        mService.unregisterRemoteReceiver(mCallerId, id);
                    }
                }

                synchronized (mReceivers) {
                    mReceivers.clear();
                }

                if (!unexpected) {
                    throw new RuntimeException(
                            "Are you missing a call to unregisterRemoteReceiver()?");
                }
            }
        }
    }

    private void quitBackground() {
        if (mBroadcastThread != null) {
            mBroadcastThread.quit();
            mBroadcastThread = null;
        }
        mBackground = null;
    }

    /**
     * 把意图广播到对端设备。该广播将被对端设备通过调用
     * {@link Context#registerReceiver(android.content.BroadcastReceiver, IntentFilter)}
     * 方法注册的广播接收器接收。
     * 
     * @param intent 需要广播的意图。
     * @see #sendRemoteBroadcast(Intent, String)
     */
    public void sendRemoteBroadcast(Intent intent) {
        sendRemoteBroadcast(intent, null);
    }

    /**
     * 把意图广播到对端设备。该广播将被对端设备通过调用
     * {@link Context#registerReceiver(android.content.BroadcastReceiver, IntentFilter)}
     * 方法注册的广播接收器接收。
     * 
     * @param intent 需要广播的意图
     * @param perm 接收该广播需要的权限。
     * @see #sendRemoteBroadcast(Intent)
     */
    public void sendRemoteBroadcast(Intent intent, String perm) {
        if (mCallerId <= 0) {
            mHandler.obtainMessage(MSG_CALLER_ERROR, mCallerId, -1).sendToTarget();
            return;
        }

        mService.sendRemoteBroadcast(mCallerId, intent, perm);
    }

    /**
     * 注册远程广播接收器。该接收器将接收对端设备通过调用{@link Context#sendBroadcast(Intent)}等方法发送的广播。
     * 
     * @param receiver 注册的远程广播接收器
     * @param filter 意图过滤器
     * @see #registerRemoteReceiver(RemoteBroadcastReceiver, IntentFilter, String)
     * @see #unregisterRemoteReceiver(RemoteBroadcastReceiver)
     */
    public void registerRemoteReceiver(RemoteBroadcastReceiver receiver, IntentFilter filter) {
        registerRemoteReceiver(receiver, filter, null);
    }

    /**
     * 注册远程广播接收器。该接收器将接收对端设备通过调用{@link Context#sendBroadcast(Intent)}等方法发送的广播。
     * 
     * @param receiver 注册的远程广播接收器
     * @param filter 意图过滤器
     * @param requestPermission 该广播接收需要的权限。
     * @see #registerRemoteReceiver(RemoteBroadcastReceiver, IntentFilter)
     * @see #unregisterRemoteReceiver(RemoteBroadcastReceiver)
     */
    public void registerRemoteReceiver(RemoteBroadcastReceiver receiver, IntentFilter filter,
            String requestPermission) {
        if (mCallerId <= 0) {
            mHandler.obtainMessage(MSG_CALLER_ERROR, mCallerId, -1).sendToTarget();
            return;
        }

        int id = mIDAlloter.allocation();
        mService.registerRemoteReceiver(mCallerId, id, filter, requestPermission);

        mBackground.obtainMessage(MSG_ADD_RECEIVER_IN_ARRAY, id, -1, receiver).sendToTarget();
    }

    private void addReceiverInArray(int key, RemoteBroadcastReceiver receiver) {
        if (mReceivers == null) {
            mReceivers = new SparseArray<RemoteBroadcastReceiver>();
        }

        synchronized (mReceivers) {
            mReceivers.put(key, receiver);
        }
    }

    /**
     * 反注册远程广播接收器。反注册后，应用将不再接收对端设备发送的符合接收条件的普通广播。
     * 
     * @param receiver 反注册的远程广播接收器。
     * @see #registerRemoteReceiver(RemoteBroadcastReceiver, IntentFilter)
     * @see #registerRemoteReceiver(RemoteBroadcastReceiver, IntentFilter, String)
     */
    public void unregisterRemoteReceiver(RemoteBroadcastReceiver receiver) {
        mBackground.obtainMessage(MSG_UNREGISTER_REMOTE_RECEIVER, receiver).sendToTarget();
    }

    private void unregisterRemoteReceiverInner(RemoteBroadcastReceiver receiver) {
        if (mCallerId <= 0) {
            mHandler.obtainMessage(MSG_CALLER_ERROR, mCallerId, -1).sendToTarget();
            return;
        }

        if (mReceivers == null) {
            mIDAlloter.initialize();
            return;
        }

        int index = mReceivers.indexOfValue(receiver);
        int N = mReceivers.size();
        if (index < 0 || index >= N) return;

        removeReceiverAt(index);
        mService.unregisterRemoteReceiver(mCallerId, mReceivers.keyAt(index));
    }

    private void removeReceiverAt(int index) {
        if (mReceivers == null || index < 0 || index >= mReceivers.size()) return;

        synchronized (mReceivers) {
            mReceivers.removeAt(index);
        }

        if (mReceivers.size() == 0) {
            mIDAlloter.initialize();
        }
    }

    /**
     * 注册远程广播回调接口，该回调接口可以异步返回远程广播的状态。
     * <p>
     * 注意：一个实例只能注册一个回调接口，之后注册的接口会代替掉之前注册的接口，传入{@code null}可以注销回调接口。
     * 
     * @param callback 回调接口实例，为空则注销回调。
     */
    public void registerRemoteBroadcastCallback(RemoteBroadcastCallback callback) {
        mCallback = callback;
    }

    private class BroadcastHandler extends Handler {
        public BroadcastHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_CALLER_ERROR:
                if (mCallback != null) {
                    mCallback.onCallerError(msg.arg1);
                }
                break;

            case MSG_ADD_RECEIVER_IN_ARRAY:
                addReceiverInArray(msg.arg1, (RemoteBroadcastReceiver) msg.obj);
                break;

            case MSG_UNREGISTER_REMOTE_RECEIVER:
                unregisterRemoteReceiverInner((RemoteBroadcastReceiver) msg.obj);
                break;

            case MSG_CLEAR_ALL_RECEIVERS:
                clearReceivers(msg.arg1 != 0);
                break;

            case MSG_QUIT_BACKGROUND:
                quitBackground();
                break;

            default:
                break;
            }
        }
    }

    private class UIHandler extends Handler {
        public UIHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

            case MSG_SEND_REMOTE_BROADCAST_RESULT:
                if (mCallback != null) {
                    Bundle bundle = msg.getData();
                    String permission = bundle != null ? bundle.getString(EXTRA_PERMISSION, null)
                            : null;
                    mCallback.onSendResult((Intent) msg.obj, permission, msg.arg1);
                }
                break;

            case MSG_PERFORM_RECEIVE:
                int id = msg.arg1;

                if (id < 0 || msg.obj == null) return;

                RemoteBroadcastReceiver receiver = mReceivers.get(id);
                if (receiver == null) return;

                receiver.onReceive(getContext(), (Intent) msg.obj);
                break;

            default:
                break;
            }
        }
    }

    private class RemoteBroadcastCallbackStub extends IRemoteBroadcastCallback.Stub {

        @Override
        public void performSendResult(Intent intent, String perm, int resultCode) {
            Message msg = mHandler.obtainMessage(MSG_SEND_REMOTE_BROADCAST_RESULT, resultCode, -1,
                    intent);
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_PERMISSION, perm);
            msg.setData(bundle);
            msg.sendToTarget();
        }

        @Override
        public void performReceive(int id, Intent intent) {
            mHandler.obtainMessage(MSG_PERFORM_RECEIVE, id, -1, intent).sendToTarget();
        }
    }

    /**
     * 定义了远程广播接收器以及远程广播相关的的回调方法。
     */
    public interface RemoteBroadcastCallback {
        /**
         * 发送远程广播的回调方法
         * 
         * @param intent 之前广播的意图
         * @param permission 之前广播的接收权限
         * @param resultCode 发送结果代号
         */
        void onSendResult(Intent intent, String permission, int resultCode);

        /**
         * 非法调用者回调，当调用者非法时调用该回调方法提醒用户。
         * 
         * @param callerId 非法调用者的ID
         */
        void onCallerError(int callerId);
    }
}