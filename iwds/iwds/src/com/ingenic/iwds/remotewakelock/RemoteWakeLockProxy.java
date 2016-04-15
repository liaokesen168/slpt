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
package com.ingenic.iwds.remotewakelock;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.SparseArray;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.datatransactor.DataTransactor;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback;
import com.ingenic.iwds.utils.IwdsLog;
import com.ingenic.iwds.utils.IwdsUtils;
import com.ingenic.iwds.utils.SimpleIDAlloter;

public class RemoteWakeLockProxy {
    private static final String UUID = "5B354F08-DE47-7918-B0C1-13280E013FEE";

    private static final int MSG_REGISTER_CALLBACK = 1;
    private static final int MSG_UNREGISTER_CALLBACK = 2;
    private static final int MSG_ADD_REQUESTING_INFO = 3;
    private static final int MSG_ADD_REQUESTED_INFO = 4;
    private static final int MSG_HANDLE_SEND_RESULT = 5;
    private static final int MSG_HANDLE_DATA_ARRIVED = 6;
    private static final int MSG_MOVE_ALL_INFOS_TO_REQUESTING = 7;
    private static final int MSG_REREQUEST = 8;
    private static final int MSG_DESTROY_REMOTE_WAKELOCK = 9;
    private static final int MSG_REMOVE_ALL_PROXYS = 10;
    private static final int MSG_QUIT_BACKGROUND_THREAD = 11;
    private static final int MSG_NOTIFY_AVAILABLE = 12;

    private static RemoteWakeLockProxy sInstance;
    private final PowerManager mPowerManager;
    private final WakeLockTransactorCallback mTransactorCallback;
    private final DataTransactor mTransactor;

    private volatile boolean mIsAvailable;
    private SimpleIDAlloter mIDAlloter;
    private HandlerThread mBGThread;
    private Handler mHandler;

    private SparseArray<IRemoteWakeLockCallback> mCallbacks;
    private SparseArray<SparseArray<PowerManager.WakeLock>> mProxys;
    private SparseArray<SparseArray<CreateInfo>> mRequestingInfos;
    private SparseArray<SparseArray<CreateInfo>> mRequestedInfos;

    private RemoteWakeLockProxy(Context context) {
        mIDAlloter = SimpleIDAlloter.newInstance();
        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        mTransactorCallback = new WakeLockTransactorCallback();
        mTransactor = new DataTransactor(context, mTransactorCallback, UUID);
    }

    public synchronized static RemoteWakeLockProxy getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RemoteWakeLockProxy(context);
        }

        return sInstance;
    }

    public void startTransaction() {
        if (mTransactor != null && !mTransactor.isStarted()) {
            mTransactor.start();
        }

        initBackgroundThread();
    }

    public void stopTransaction() {
        if (mTransactor != null && mTransactor.isStarted()) {
            mTransactor.stop();
        }

        quitBackgroundThread();
    }

    @Override
    protected void finalize() throws Throwable {
        stopTransaction();
        super.finalize();
    }

    private void initBackgroundThread() {
        mBGThread = new HandlerThread("RemoteWakeLock");
        mBGThread.start();
        mHandler = new WakeLockHandler(mBGThread.getLooper());
    }

    private void quitBackgroundThread() {
        mHandler.sendEmptyMessage(MSG_QUIT_BACKGROUND_THREAD);
    }

    int registerRemoteWakeLockCallback(IRemoteWakeLockCallback callback) {
        int callerId = mIDAlloter.allocation();
        mHandler.obtainMessage(MSG_REGISTER_CALLBACK, callerId, -1, callback).sendToTarget();
        return callerId;
    }

    void unregisterRemoteWakeLockCallback(int callerId) {
        mHandler.obtainMessage(MSG_UNREGISTER_CALLBACK, callerId, -1).sendToTarget();
    }

    void newRemoteWakeLock(int callerId, int id, int levelAndFlags, String tag) {
        CreateInfo info = new CreateInfo(callerId, id, levelAndFlags, tag);
        requestRemoteWakeLock(info);
    }

    private void requestRemoteWakeLock(CreateInfo info) {
        if (mIsAvailable) {
            mTransactor.send(info);
        } else {
            mHandler.obtainMessage(MSG_ADD_REQUESTING_INFO, info).sendToTarget();
        }
    }

    void destroyRemoteWakeLock(int callerId, int id) {
        mHandler.obtainMessage(MSG_DESTROY_REMOTE_WAKELOCK, callerId, id);

        if (mIsAvailable) {
            mTransactor.send(new DeleteInfo(callerId, id));
        }
    }

    void acquireWakeLock(int callerId, int id, long timeout) {
        if (mIsAvailable) {
            CMDInfo cmd = new CMDInfo(callerId, id, 1, timeout);
            mTransactor.send(cmd);
        } else {
            handleAcquireResult(DataTransactResult.RESULT_FAILED_CHANNEL_UNAVAILABLE, callerId, id,
                    timeout);
        }
    }

    void releaseWakeLock(int callerId, int id) {
        if (mIsAvailable) {
            mTransactor.send(new CMDInfo(callerId, id));
        } else {
            IwdsLog.w(this, "DataTransactor is invailable.Donot need to release.");
        }
    }

    private void handleRegisterCallback(int callerId, IRemoteWakeLockCallback callback) {
        if (mCallbacks == null) {
            mCallbacks = new SparseArray<IRemoteWakeLockCallback>();
        }

        synchronized (mCallbacks) {
            mCallbacks.put(callerId, callback);
        }
    }

    private void handleUnregisterCallback(int callerId) {
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

    private void handleAddRequesting(CreateInfo info) {
        if (mRequestingInfos == null) {
            mRequestingInfos = new SparseArray<SparseArray<CreateInfo>>();
        }

        int first = info.getCallerId();
        int second = info.getId();
        IwdsUtils.addInArray(mRequestingInfos, first, second, info);
    }

    private void handleAddRequested(CreateInfo info) {
        if (mRequestedInfos == null) {
            mRequestedInfos = new SparseArray<SparseArray<CreateInfo>>();
        }

        int first = info.getCallerId();
        int second = info.getId();
        IwdsUtils.addInArray(mRequestedInfos, first, second, info);
    }

    private void handleDestroyWakeLock(int callerId, int id) {
        IwdsUtils.deleteInArray(mRequestingInfos, callerId, id);
        IwdsUtils.deleteInArray(mRequestedInfos, callerId, id);
    }

    private void handleSendResult(DataTransactResult result) {
        int resultCode = result.getResultCode();
        Object object = result.getTransferedObject();

        if (object instanceof CreateInfo) {
            handleCreateResult(resultCode, (CreateInfo) object);
        } else if (object instanceof CMDInfo) {
            handleCMDResult(resultCode, (CMDInfo) object);
        } else if (object instanceof DeleteInfo) {
            handleDeleteResult(resultCode, (DeleteInfo) object);
        }
    }

    private void handleCreateResult(int resultCode, CreateInfo info) {
        if (resultCode == DataTransactResult.RESULT_OK) {
            mHandler.obtainMessage(MSG_ADD_REQUESTED_INFO, info).sendToTarget();
        } else {
            mHandler.obtainMessage(MSG_ADD_REQUESTING_INFO, info).sendToTarget();
        }
    }

    private void handleCMDResult(int resultCode, CMDInfo cmd) {
        int cmdCode = cmd.getCmd();

        if (cmdCode == 1) {
            handleAcquireResult(resultCode, cmd.getCallerId(), cmd.getId(), cmd.getTimeout());
        } else if (cmdCode == 0) {
            handleReleaseResult(resultCode, cmd.getCallerId(), cmd.getId());
        }
    }

    private void handleAcquireResult(int resultCode, int callerId, int id, long timeout) {
        if (mCallbacks == null) return;

        IRemoteWakeLockCallback callback = mCallbacks.get(callerId);
        if (callback != null) {
            callback.performAcquireResult(id, resultCode, timeout);
        }
    }

    private void handleReleaseResult(int resultCode, int callerId, int id) {
        if (resultCode != DataTransactResult.RESULT_OK) {
            IwdsLog.e(this, "Release remote wakelock: " + id + " of caller: " + callerId
                    + " failed.Result code: " + resultCode);
        }
    }

    private void handleDeleteResult(int resultCode, DeleteInfo info) {
        if (resultCode != DataTransactResult.RESULT_OK) {
            int id = info.getId();
            int callerId = info.getCallerId();
            IwdsLog.e(this, "Delete remote wakelock: " + id + "of caller: " + callerId
                    + " failed.Result code: " + resultCode);
        }
    }

    private void handleDataArrived(Object object) {
        if (object instanceof CreateInfo) {
            handleCreateArrived((CreateInfo) object);
        } else if (object instanceof CMDInfo) {
            handleCMDArrived((CMDInfo) object);
        } else if (object instanceof DeleteInfo) {
            handleDeleteArrived((DeleteInfo) object);
        }
    }

    private void handleCreateArrived(CreateInfo info) {
        PowerManager.WakeLock wakeLock = mPowerManager.newWakeLock(info.getLevelAndFlags(),
                info.getTag());
        handleAddProxy(info.getCallerId(), info.getId(), wakeLock);
    }

    private void handleAddProxy(int callerId, int id, PowerManager.WakeLock wakeLock) {
        if (mProxys == null) {
            mProxys = new SparseArray<SparseArray<PowerManager.WakeLock>>();
        }

        IwdsUtils.addInArray(mProxys, callerId, id, wakeLock);
    }

    private void handleDeleteArrived(DeleteInfo info) {
        if (mProxys == null) return;

        int callerId = info.getCallerId();
        SparseArray<PowerManager.WakeLock> wakeLocks = mProxys.get(callerId);
        if (wakeLocks == null) return;

        int id = info.getId();
        PowerManager.WakeLock wakeLock = wakeLocks.get(id);
        if (wakeLock == null) return;

        if (wakeLock.isHeld()) {
            wakeLock.release();
        }

        synchronized (mProxys) {
            wakeLocks.delete(id);
            if (wakeLocks.size() == 0) {
                mProxys.delete(callerId);
            }
        }
    }

    private void handleCMDArrived(CMDInfo cmd) {
        int cmdCode = cmd.getCmd();

        if (cmdCode == 1) {
            handleAcquireArrived(cmd.getCallerId(), cmd.getId(), cmd.getTimeout());
        } else if (cmdCode == 0) {
            handleReleaseArrived(cmd.getCallerId(), cmd.getId());
        }
    }

    private void handleAcquireArrived(int callerId, int id, long timeout) {
        PowerManager.WakeLock wakeLock = getInArray(mProxys, callerId, id);
        if (wakeLock == null) return;

        if (!wakeLock.isHeld()) {
            if (timeout < 0) {
                wakeLock.acquire();
            } else {
                wakeLock.acquire(timeout);
            }
        }
    }

    private void handleReleaseArrived(int callerId, int id) {
        PowerManager.WakeLock wakeLock = getInArray(mProxys, callerId, id);
        if (wakeLock == null) return;

        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    private static <T> T getInArray(SparseArray<SparseArray<T>> array, int firstKey, int secondKey) {
        if (array == null) return null;

        SparseArray<T> ts = array.get(firstKey);
        if (ts == null) return null;

        return ts.get(secondKey);
    }

    private void handleMoveAllToRequesting() {
        if (mRequestedInfos == null) return;

        if (mRequestingInfos == null) {
            mRequestingInfos = new SparseArray<SparseArray<CreateInfo>>();
        }

        int N = mRequestedInfos.size();
        for (int i = 0; i < N; i++) {
            int key = mRequestedInfos.keyAt(i);
            SparseArray<CreateInfo> infos = mRequestedInfos.valueAt(i);

            synchronized (mRequestingInfos) {
                mRequestingInfos.put(key, infos);
            }
        }

        synchronized (mRequestedInfos) {
            mRequestedInfos.clear();
        }
    }

    private void handleRerequest() {
        if (mRequestingInfos == null) return;

        int N = mRequestingInfos.size();
        loop: for (int i = N - 1; i >= 0; i--) {
            SparseArray<CreateInfo> infos = mRequestingInfos.valueAt(i);

            int n = infos.size();
            for (int j = n - 1; j >= 0; j--) {
                if (!mIsAvailable) {
                    break loop;
                }

                CreateInfo info = infos.valueAt(j);
                synchronized (mRequestingInfos) {
                    infos.removeAt(j);

                    if (infos.size() == 0) {
                        mRequestingInfos.removeAt(i);
                    }
                }

                mTransactor.send(info);
            }
        }
    }

    private void handleRemoveAllProxys() {
        if (mProxys == null) return;

        int N = mProxys.size();
        for (int i = 0; i < N; i++) {
            SparseArray<PowerManager.WakeLock> wakeLocks = mProxys.valueAt(i);
            if (wakeLocks == null) continue;

            int n = wakeLocks.size();
            for (int j = 0; j < n; j++) {
                PowerManager.WakeLock wakeLock = wakeLocks.valueAt(j);

                if (wakeLock.isHeld()) {
                    wakeLock.release();
                }
            }
        }

        synchronized (mProxys) {
            mProxys.clear();
        }
    }

    private void handleQuitBackground() {
        if (mBGThread != null) {
            mBGThread.quit();
            mBGThread = null;
        }

        mHandler = null;
    }

    private void handleNotifyAvailable(boolean isAvailable) {
        if (mCallbacks == null) return;

        int N = mCallbacks.size();
        for (int i = 0; i < N; i++) {
            IRemoteWakeLockCallback callback = mCallbacks.valueAt(i);

            callback.performAvailableChanged(isAvailable);
        }
    }

    private class WakeLockTransactorCallback implements DataTransactorCallback {

        @Override
        public void onLinkConnected(DeviceDescriptor descriptor, boolean isConnected) {}

        @Override
        public void onChannelAvailable(boolean isAvailable) {
            mIsAvailable = isAvailable;

            if (isAvailable) {
                mHandler.sendEmptyMessage(MSG_REREQUEST);
            } else {
                mHandler.sendEmptyMessage(MSG_MOVE_ALL_INFOS_TO_REQUESTING);
                mHandler.sendEmptyMessage(MSG_REMOVE_ALL_PROXYS);
            }

            mHandler.obtainMessage(MSG_NOTIFY_AVAILABLE, isAvailable).sendToTarget();
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

    private class WakeLockHandler extends Handler {
        public WakeLockHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_REGISTER_CALLBACK:
                handleRegisterCallback(msg.arg1, (IRemoteWakeLockCallback) msg.obj);
                break;

            case MSG_UNREGISTER_CALLBACK:
                handleUnregisterCallback(msg.arg1);
                break;

            case MSG_ADD_REQUESTING_INFO:
                handleAddRequesting((CreateInfo) msg.obj);
                break;

            case MSG_ADD_REQUESTED_INFO:
                handleAddRequested((CreateInfo) msg.obj);
                break;

            case MSG_HANDLE_SEND_RESULT:
                handleSendResult((DataTransactResult) msg.obj);
                break;

            case MSG_HANDLE_DATA_ARRIVED:
                handleDataArrived(msg.obj);
                break;

            case MSG_MOVE_ALL_INFOS_TO_REQUESTING:
                handleMoveAllToRequesting();
                break;

            case MSG_REREQUEST:
                handleRerequest();
                break;

            case MSG_DESTROY_REMOTE_WAKELOCK:
                handleDestroyWakeLock(msg.arg1, msg.arg2);
                break;

            case MSG_REMOVE_ALL_PROXYS:
                handleRemoveAllProxys();
                break;

            case MSG_QUIT_BACKGROUND_THREAD:
                handleQuitBackground();
                break;

            case MSG_NOTIFY_AVAILABLE:
                handleNotifyAvailable((Boolean) msg.obj);
                break;

            default:
                break;
            }
        }
    }
}