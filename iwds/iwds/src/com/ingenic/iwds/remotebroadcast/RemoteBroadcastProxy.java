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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.content.RemoteIntent;
import com.ingenic.iwds.datatransactor.DataTransactor;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.utils.IwdsLog;
import com.ingenic.iwds.utils.IwdsUtils;
import com.ingenic.iwds.utils.SimpleIDAlloter;

/**
 * 远程广播服务代理
 */
public class RemoteBroadcastProxy {
    private static final String UUID = "94D8C7BC-0AFC-E5D4-D10A-1D381DF2CD72";
    private static final String EXTRA_REMOTE_BROADCAST = "is_remote_broadcast";

    private static final int MSG_REGISTER_REMOTE_BROADCAST_CALLBACK = 1;
    private static final int MSG_ADD_INTENT_FILTER_TO_REGISTERING = 2;
    private static final int MSG_REMOVE_REGISTERED_INFO = 3;
    private static final int MSG_MOVE_INTENT_FILTERS_TO_REGISTERING = 4;
    private static final int MSG_MOVE_INTENT_FILTER_TO_REGISTERED = 5;
    private static final int MSG_UNREGISTER_ALL_PROXYS = 6;
    private static final int MSG_HANDLE_SEND_RESULT = 7;
    private static final int MSG_HANDLE_DATA_ARRIVED = 8;
    private static final int MSG_REMOVE_ALL_REGISTERED_INFOS = 9;
    private static final int MSG_QUIT_BACKGROUND = 10;

    private static RemoteBroadcastProxy sInstance;
    private final Context mContext;
    private final DataTransactor mTransactor;
    private final BroadcastTransactorCallback mTransactorCallback;
    private SparseArray<IRemoteBroadcastCallback> mCallbacks;
    private SparseArray<SparseArray<BroadcastReceiver>> mProxys;
    private SparseArray<SparseArray<RemoteIntentFilterInfo>> mRegisteredInfos;
    private SparseArray<SparseArray<RemoteIntentFilterInfo>> mRegisteringInfos;
    private volatile boolean mIsAvailable = false;
    private HandlerThread mHandlerThread;
    private BroadcastHandler mHandler;
    private SimpleIDAlloter mIDAlloter;

    /**
     * 获得远程广播服务代理实例
     * 
     * @param context 上下文
     * @return 远程广播服务代理的实例
     */
    public synchronized static RemoteBroadcastProxy getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RemoteBroadcastProxy(context);
        }
        return sInstance;
    }

    private RemoteBroadcastProxy(Context context) {
        mContext = context;
        mIDAlloter = SimpleIDAlloter.newInstance();
        mTransactorCallback = new BroadcastTransactorCallback();
        mTransactor = new DataTransactor(context, mTransactorCallback, UUID);
    }

    /**
     * 启动设备之间的数据传输事务。
     * 
     * @see #stopTransaction()
     */
    public void startTransaction() {
        if (mTransactor != null && !mTransactor.isStarted()) {
            mTransactor.start();
        }

        mHandlerThread = new HandlerThread("RemoteBroadcast");
        mHandlerThread.start();
        mHandler = new BroadcastHandler(mHandlerThread.getLooper());
    }

    /**
     * 关闭设备之间的数据传输事务。
     * 
     * @see #startTransaction()
     */
    public void stopTransaction() {
        if (mTransactor != null && mTransactor.isStarted()) {
            mTransactor.stop();
        }

        mHandler.sendEmptyMessage(MSG_QUIT_BACKGROUND);
    }

    private void quitBackground() {
        if (mHandlerThread != null) {
            mHandlerThread.quit();
            mHandlerThread = null;
        }

        mHandler = null;
    }

    /**
     * 继承自{@link Object#finalize()}
     */
    @Override
    protected void finalize() throws Throwable {
        mHandler.sendEmptyMessage(MSG_REMOVE_ALL_REGISTERED_INFOS);
        stopTransaction();
        super.finalize();
    }

    void registerRemoteReceiver(int callerId, int id, IntentFilter filter, String permission) {
        RemoteIntentFilterInfo info = new RemoteIntentFilterInfo(callerId, id, filter, permission);
        sendRemoteIntentFilterInfo(info);
    }

    void unregisterRemoteReceiver(int callerId, int id) {
        UnregisterInfo info = new UnregisterInfo(callerId, id);
        mTransactor.send(info);

        mHandler.obtainMessage(MSG_REMOVE_REGISTERED_INFO, callerId, id).sendToTarget();
    }

    private void performRemoteReceiver(RemoteIntentInfo info) {
        RemoteIntent ri = info.getIntent();
        performRemoteReceiver(info.getCallerId(), info.getId(), ri != null ? ri.toIntent() : null);
    }

    private void performRemoteReceiver(int callerId, int id, Intent intent) {
        if (mCallbacks == null) return;

        IRemoteBroadcastCallback callback = mCallbacks.get(callerId);
        if (callback != null) {
            callback.performReceive(id, intent);
        }
    }

    private void registerReceiverProxy(RemoteIntentFilterInfo info) {
        if (info == null) return;

        if (mProxys == null) {
            mProxys = new SparseArray<SparseArray<BroadcastReceiver>>();
        }

        int callerId = info.getCallerId();
        int id = info.getId();
        RemoteReceiverProxy proxy = new RemoteReceiverProxy(callerId, id);
        IwdsUtils.addInArray(mProxys, callerId, id, proxy);

        mContext.registerReceiver(proxy, info.getFilter(), info.getPermission(), null);
    }

    private void unregisterReceiverProxy(int callerId, int id) {
        if (mProxys == null) return;

        SparseArray<BroadcastReceiver> receivers = mProxys.get(callerId);
        if (receivers == null) return;

        BroadcastReceiver receiver = receivers.get(id);
        if (receiver == null) return;

        synchronized (mProxys) {
            receivers.delete(id);

            if (receivers.size() == 0) {
                mProxys.delete(callerId);
            }
        }

        if (receiver != null) {
            mContext.unregisterReceiver(receiver);
        }
    }

    private void unregisterAllProxys() {
        if (mProxys == null) return;

        int N = mProxys.size();
        for (int i = 0; i < N; i++) {
            SparseArray<BroadcastReceiver> receivers = mProxys.valueAt(i);
            int n = receivers.size();
            for (int j = 0; j < n; j++) {
                BroadcastReceiver receiver = receivers.valueAt(n);
                mContext.unregisterReceiver(receiver);
            }

            synchronized (mProxys) {
                receivers.clear();
            }
        }

        synchronized (mProxys) {
            mProxys.clear();
        }
    }

    void sendRemoteBroadcast(int callerId, Intent intent, String permission) {
        RemoteBroadcast broadcast = new RemoteBroadcast(callerId, intent, permission);
        sendRemoteBroadcast(broadcast);
    }

    void sendRemoteBroadcast(RemoteBroadcast broadcast) {
        mTransactor.send(broadcast);
    }

    int registerRemoteBroadcastCallback(IRemoteBroadcastCallback callback) {
        if (callback == null) return SimpleIDAlloter.INVALID;

        int callerId = mIDAlloter.allocation();

        mHandler.obtainMessage(MSG_REGISTER_REMOTE_BROADCAST_CALLBACK, callerId, -1, callback)
                .sendToTarget();

        return callerId;
    }

    private void registerRemoteBroadcastCallbackInner(int callerId,
            IRemoteBroadcastCallback callback) {
        if (callback == null) return;

        if (mCallbacks == null) {
            mCallbacks = new SparseArray<IRemoteBroadcastCallback>();
        }

        synchronized (mCallbacks) {
            mCallbacks.put(callerId, callback);
        }
    }

    void unregisterRemoteBroadcastCallback(int callerId) {
        if (mCallbacks == null) {
            mIDAlloter.initialize();
            return;
        }

        synchronized (mCallbacks) {
            mCallbacks.delete(callerId);
        }

        if (mCallbacks.size() == 0) {
            mIDAlloter.initialize();
        }
    }

    private void addRegisteredInfo(RemoteIntentFilterInfo info) {
        if (mRegisteredInfos == null) {
            mRegisteredInfos = new SparseArray<SparseArray<RemoteIntentFilterInfo>>();
        }

        int callerId = info.getCallerId();
        int id = info.getId();
        IwdsUtils.addInArray(mRegisteredInfos, callerId, id, info);
    }

    private void removeAllRegisteredInfos() {
        if (mRegisteredInfos != null) {
            synchronized (mRegisteredInfos) {
                mRegisteredInfos.clear();
            }
        }

        if (mRegisteringInfos != null) {
            synchronized (mRegisteringInfos) {
                mRegisteringInfos.clear();
            }
        }
    }

    private void removeRemoteIntentFilterInfo(int callerId, int id) {
        IwdsUtils.deleteInArray(mRegisteringInfos, callerId, id);
        IwdsUtils.deleteInArray(mRegisteredInfos, callerId, id);
    }

    private void reRegister() {
        if (mRegisteringInfos == null) return;

        int N = mRegisteringInfos.size();
        loop: for (int i = N - 1; i >= 0; i--) {
            SparseArray<RemoteIntentFilterInfo> infos = mRegisteringInfos.valueAt(i);
            int n = infos.size();
            for (int j = n - 1; j >= 0; j--) {
                if (!mIsAvailable) break loop;

                RemoteIntentFilterInfo info = infos.valueAt(j);
                mTransactor.send(info);

                synchronized (mRegisteringInfos) {
                    infos.removeAt(j);

                    if (infos.size() == 0) {
                        mRegisteringInfos.removeAt(i);
                    }
                }
            }
        }
    }

    private void sendRemoteIntentFilterInfo(RemoteIntentFilterInfo info) {
        if (mIsAvailable) {
            mTransactor.send(info);
        } else {
            mHandler.obtainMessage(MSG_ADD_INTENT_FILTER_TO_REGISTERING, info).sendToTarget();
        }
    }

    private void addToRegisting(RemoteIntentFilterInfo info) {
        if (mRegisteringInfos == null) {
            mRegisteringInfos = new SparseArray<SparseArray<RemoteIntentFilterInfo>>();
        }

        int callerId = info.getCallerId();
        int id = info.getId();
        IwdsUtils.addInArray(mRegisteringInfos, callerId, id, info);
    }

    private void moveToRegisting() {
        if (mRegisteredInfos == null) return;

        if (mRegisteringInfos == null) {
            mRegisteringInfos = new SparseArray<SparseArray<RemoteIntentFilterInfo>>();
        }

        int N = mRegisteredInfos.size();
        for (int i = 0; i < N; i++) {
            int key = mRegisteredInfos.keyAt(i);
            SparseArray<RemoteIntentFilterInfo> value = mRegisteredInfos.valueAt(i);

            synchronized (mRegisteringInfos) {
                mRegisteringInfos.put(key, value);
            }
        }

        synchronized (mRegisteredInfos) {
            mRegisteredInfos.clear();
        }
    }

    private void handleSendResult(DataTransactResult result) {
        int resultCode = result.getResultCode();
        Object obj = result.getTransferedObject();

        if (obj instanceof RemoteBroadcast) {
            handleRemoteBroadcastSendResult(resultCode, (RemoteBroadcast) obj);
        } else if (obj instanceof RemoteIntentFilterInfo) {
            handleFilterSengResult(resultCode, (RemoteIntentFilterInfo) obj);
        } else if (obj instanceof RemoteIntentInfo) {
            handleIntentSendResult(resultCode, (RemoteIntentInfo) obj);
        } else if (obj instanceof UnregisterInfo) {
            handleUnregisterSendResult(resultCode, (UnregisterInfo) obj);
        }
    }

    private void handleRemoteBroadcastSendResult(int resultCode, RemoteBroadcast broadcast) {
        int callerId = broadcast.getCallerId();
        IRemoteBroadcastCallback callback = mCallbacks.get(callerId);
        if (callback != null) {
            RemoteIntent ri = broadcast.getIntent();
            Intent it = ri != null ? ri.toIntent() : null;
            String perm = broadcast.getPermission();

            callback.performSendResult(it, perm, resultCode);
        }
    }

    private void handleFilterSengResult(int resultCode, RemoteIntentFilterInfo info) {
        if (resultCode == DataTransactResult.RESULT_OK) {
            addRegisteredInfo(info);
        } else {
            addToRegisting(info);
        }
    }

    private void handleIntentSendResult(int resultCode, RemoteIntentInfo info) {
        if (resultCode != DataTransactResult.RESULT_OK) {
            IwdsLog.e(this, "Local broadcast has received.But send to remote failed: " + resultCode);
        }
    }

    private void handleUnregisterSendResult(int resultCode, UnregisterInfo info) {
        // Always removed when calling unregistRemoteReceiver, do nothing.
    }

    private void handleDataArrived(Object object) {
        if (object instanceof RemoteBroadcast) {
            handleRemoteBroadcastArrived((RemoteBroadcast) object);
        } else if (object instanceof RemoteIntentFilterInfo) {
            handleFilterArrived((RemoteIntentFilterInfo) object);
        } else if (object instanceof RemoteIntentInfo) {
            handleIntentArrived((RemoteIntentInfo) object);
        } else if (object instanceof UnregisterInfo) {
            handleUnregisterArrived((UnregisterInfo) object);
        }
    }

    private void handleRemoteBroadcastArrived(RemoteBroadcast broadcast) {
        RemoteIntent ri = broadcast.getIntent();
        Intent intent = ri != null ? ri.toIntent() : null;

        if (intent == null) {
            intent = new Intent();
        }
        intent.putExtra(EXTRA_REMOTE_BROADCAST, true);

        String permission = broadcast.getPermission();
        boolean isSticky = broadcast.isSticky();
        boolean isOrdered = broadcast.isOrdered();

        if (isSticky && isOrdered) {
            IwdsLog.e(this, "Unsupport to send broadcast with sticky & ordered!");
            return;
        } else if (isSticky) {
            mContext.sendStickyBroadcast(intent);
        } else if (isOrdered) {
            mContext.sendOrderedBroadcast(intent, permission);
        } else {
            mContext.sendBroadcast(intent, permission);
        }
    }

    private void handleFilterArrived(RemoteIntentFilterInfo info) {
        registerReceiverProxy(info);
    }

    private void handleIntentArrived(RemoteIntentInfo info) {
        performRemoteReceiver(info);
    }

    private void handleUnregisterArrived(UnregisterInfo info) {
        unregisterReceiverProxy(info.getCallerId(), info.getId());
    }

    private class BroadcastTransactorCallback implements DataTransactor.DataTransactorCallback {

        @Override
        public void onLinkConnected(DeviceDescriptor descriptor, boolean isConnected) {}

        @Override
        public void onChannelAvailable(boolean isAvailable) {
            mIsAvailable = isAvailable;

            if (isAvailable) {
                mHandler.sendEmptyMessage(MSG_MOVE_INTENT_FILTER_TO_REGISTERED);
            } else {
                mHandler.sendEmptyMessage(MSG_MOVE_INTENT_FILTERS_TO_REGISTERING);
                mHandler.sendEmptyMessage(MSG_UNREGISTER_ALL_PROXYS);
            }
        }

        @Override
        public void onSendResult(DataTransactResult result) {
            mHandler.obtainMessage(MSG_HANDLE_SEND_RESULT, result).sendToTarget();
        }

        @Override
        public void onDataArrived(Object object) {
            mHandler.obtainMessage(MSG_HANDLE_DATA_ARRIVED, object).sendToTarget();
        }

        @Override
        public void onSendFileProgress(int progress) {}

        @Override
        public void onRecvFileProgress(int progress) {}

        @Override
        public void onSendFileInterrupted(int index) {

        }

        @Override
        public void onRecvFileInterrupted(int index) {

        }
    }

    private class RemoteReceiverProxy extends BroadcastReceiver {
        private int mCallerId;
        private int mId;

        public RemoteReceiverProxy(int callerId, int id) {
            mCallerId = callerId;
            mId = id;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isRemote = intent.getBooleanExtra(EXTRA_REMOTE_BROADCAST, false);
            if (isRemote) return;

            RemoteIntentInfo info = new RemoteIntentInfo(mCallerId, mId,
                    RemoteIntent.fromIntent(intent));
            mTransactor.send(info);
        }
    }

    private class BroadcastHandler extends Handler {
        public BroadcastHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_REGISTER_REMOTE_BROADCAST_CALLBACK:
                registerRemoteBroadcastCallbackInner(msg.arg1, (IRemoteBroadcastCallback) msg.obj);
                break;

            case MSG_ADD_INTENT_FILTER_TO_REGISTERING:
                addToRegisting((RemoteIntentFilterInfo) msg.obj);
                break;

            case MSG_REMOVE_REGISTERED_INFO:
                removeRemoteIntentFilterInfo(msg.arg1, msg.arg2);
                break;

            case MSG_MOVE_INTENT_FILTERS_TO_REGISTERING:
                moveToRegisting();
                break;

            case MSG_MOVE_INTENT_FILTER_TO_REGISTERED:
                reRegister();
                break;

            case MSG_UNREGISTER_ALL_PROXYS:
                unregisterAllProxys();
                break;

            case MSG_HANDLE_SEND_RESULT:
                handleSendResult((DataTransactResult) msg.obj);
                break;

            case MSG_HANDLE_DATA_ARRIVED:
                handleDataArrived(msg.obj);
                break;

            case MSG_REMOVE_ALL_REGISTERED_INFOS:
                removeAllRegisteredInfos();
                break;

            case MSG_QUIT_BACKGROUND:
                quitBackground();
                break;

            default:
                break;
            }
        }
    }
}