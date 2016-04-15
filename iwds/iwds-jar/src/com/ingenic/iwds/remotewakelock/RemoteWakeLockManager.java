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
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.SparseArray;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.utils.SimpleIDAlloter;
import com.ingenic.iwds.utils.IwdsAssert;

/**
 * 远程锁管理器，用于获取远程锁实例。获取远程锁实例之后，持锁或者释放锁均在远程锁实例操作，为了使开发者使用方便，远程锁({@link RemoteWakeLock})的接口参考
 * {@link PowerManager.WakeLock}的接口设计
 */
public class RemoteWakeLockManager extends ServiceManagerContext {
    //PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK
    private static final int PROXIMITY_SCREEN_OFF_WAKE_LOCK = 0x00000020;
    //PowerManager.WAKE_LOCK_LEVEL_MASK
    private static final int WAKE_LOCK_LEVEL_MASK = 0x0000ffff;

    //Handler处理消息时Bundle携带的数据中超时信息的键
    private static final String EXTRA_TIMEOUT = "timeout";

    private static final int MSG_NEW_REMOTE_WAKELOCK = 1;
    private static final int MSG_ACQUIRE_WAKELOCK = 2;
    private static final int MSG_RELEASE_WAKELOCK = 3;
    private static final int MSG_ACQUIRE_RESULT = 4;
    private static final int MSG_RESET_HELD = 5;
    private static final int MSG_DESTROY_WAKELOCKS = 6;
    private static final int MSG_QUIT_BACKGROUND_THREAD = 7;

    /**
     * 表明结果为成功
     */
    public static final int RESULT_OK = DataTransactResult.RESULT_OK;
    /**
     * 表明结果为失败，失败原因为：与远程设备的通信通道不可用
     */
    public static final int RESULT_FAILED_CHANNEL_UNAVAILABLE = DataTransactResult.RESULT_FAILED_CHANNEL_UNAVAILABLE;
    /**
     * 表明结果为失败，失败原因为：与远程设备的通信连接已断开
     */
    public static final int RESULT_FAILED_LINK_DISCONNECTED = DataTransactResult.RESULT_FAILED_LINK_DISCONNECTED;
    /**
     * 、 表明结果为失败，失败原因为：本地服务异常
     */
    public static final int RESULT_FAILED_IWDS_CRASH = DataTransactResult.RESULT_FAILED_IWDS_CRASH;

    private IRemoteWakeLockService mService;
    private RemoteWakeLockCallback mCallback;
    private IRemoteWakeLockCallback mCallbackStub = new RemoteWakeLockCallbackStub();
    private Handler mUIHandler;
    private Handler mBGHandler;
    private HandlerThread mBGThread;
    private int mCallerId;
    private SparseArray<RemoteWakeLock> mWakeLocks;
    private SimpleIDAlloter mIDAlloter;

    public RemoteWakeLockManager(Context context) {
        super(context);
        mIDAlloter = SimpleIDAlloter.newInstance();
        mUIHandler = new UIHandler(context.getMainLooper());

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

    private void handleServiceConnected(IBinder binder) {
        initBackgroundThread();

        mService = IRemoteWakeLockService.Stub.asInterface(binder);

        if (mService != null) {
            mCallerId = mService.registerRemoteWakeLockCallback(mCallbackStub);
        }

        IwdsAssert.dieIf(this, mCallerId == SimpleIDAlloter.INVALID, "Caller is invalid.");
    }

    private void handleServiceDisconnected(boolean unexpected) {
        destroyWakeLocks();

        if (mService != null) {
            mService.unregisterRemoteWakeLockCallback(mCallerId);
        }

        quitBackgroundThread();
    }

    private void initBackgroundThread() {
        mBGThread = new HandlerThread("RemoteWakeLock");
        mBGThread.start();
        mBGHandler = new WakeLockHandler(mBGThread.getLooper());
    }

    private void quitBackgroundThread() {
        mBGHandler.sendEmptyMessage(MSG_QUIT_BACKGROUND_THREAD);
    }

    /**
     * 注册远程锁回调接口，一个管理器只能注册一个回调接口，后注册的接口会替换掉之前注册的接口。传入{@code null}为注销之前注册的回调接口。
     * 
     * @param callback 远程锁回调接口
     */
    public void registerRemoteWakeLockCallback(RemoteWakeLockCallback callback) {
        mCallback = callback;
    }

    /**
     * 获得一个新的远程锁
     * <p>
     * 注意：获得一个远程锁不代表持该远程锁，持远程锁应在获取远程锁得到远程锁实例后调用{@link RemoteWakeLock#acquire()}或
     * {@link RemoteWakeLock#acquire(long)}方法。远程锁的接口参考{@link PowerManager.WakeLock}的接口设计
     * 
     * @param levelAndFlags 远程锁的等级和标志，为了方便开发者使用，该标志保持与{@link PowerManager#newWakeLock(int, String)}
     *        使用的标志以及使用方法一致。如果标志使用不当，将与{@link PowerManager#newWakeLock(int, String)}一样抛出异常。
     * @param tag 远程锁的标签，为了方便开发者使用，该标签的要求与{@link PowerManager#newWakeLock(int, String)}的要求一致。如果传入
     *        <code>null</code>将抛出异常。
     * @return 新的远程锁对象
     * 
     * @see PowerManager#newWakeLock(int, String)
     */
    public RemoteWakeLock newRemoteWakeLock(int levelAndFlags, String tag) {
        validateWakeLockParameters(levelAndFlags, tag);

        RemoteWakeLock wakeLock = new RemoteWakeLock(levelAndFlags, tag);
        mBGHandler.obtainMessage(MSG_NEW_REMOTE_WAKELOCK, wakeLock).sendToTarget();
        return wakeLock;
    }

    @SuppressWarnings("deprecation")
    private static void validateWakeLockParameters(int levelAndFlags, String tag) {
        switch (levelAndFlags & WAKE_LOCK_LEVEL_MASK) {
        case PowerManager.PARTIAL_WAKE_LOCK:
        case PowerManager.SCREEN_DIM_WAKE_LOCK:
        case PowerManager.SCREEN_BRIGHT_WAKE_LOCK:
        case PowerManager.FULL_WAKE_LOCK:
        case PROXIMITY_SCREEN_OFF_WAKE_LOCK:
            break;
        default:
            throw new IllegalArgumentException("Must specify a valid wake lock level.");
        }

        if (tag == null) {
            throw new IllegalArgumentException("The tag must not be null.");
        }
    }

    private void destroyWakeLocks() {
        mBGHandler.sendEmptyMessage(MSG_DESTROY_WAKELOCKS);
    }

    private void handleQuitBackground() {
        if (mBGThread != null) {
            mBGThread.quit();
            mBGThread = null;
        }

        mBGHandler = null;
    }

    private void handleNewWakeLock(RemoteWakeLock wakeLock) {
        int id = handleNewWakeLock(wakeLock.mFlags, wakeLock.mTag);
        handleAddWakeLock(id, wakeLock);
    }

    private int handleNewWakeLock(int levelAndFlags, String tag) {
        int id = mIDAlloter.allocation();
        mService.newRemoteWakeLock(mCallerId, id, levelAndFlags, tag);
        return id;
    }

    private void handleAddWakeLock(int key, RemoteWakeLock wakeLock) {
        if (mWakeLocks == null) {
            mWakeLocks = new SparseArray<RemoteWakeLockManager.RemoteWakeLock>();
        }

        synchronized (mWakeLocks) {
            mWakeLocks.put(key, wakeLock);
        }
    }

    private void handleDestroyWakeLocks() {
        if (mWakeLocks != null) {
            int N = mWakeLocks.size();

            for (int i = 0; i < N; i++) {
                if (mService == null) break;

                mService.destroyRemoteWakeLock(mCallerId, mWakeLocks.keyAt(i));
            }

            synchronized (mWakeLocks) {
                mWakeLocks.clear();
            }
        }
    }

    private void handleAcquireWakeLock(RemoteWakeLock wakeLock, long timeout) {
        int key = getKey(mWakeLocks, wakeLock);
        if (key < 0) return;

        mService.acquireWakeLock(mCallerId, key, timeout);
    }

    private void handleReleaseWakeLock(RemoteWakeLock wakeLock) {
        int key = getKey(mWakeLocks, wakeLock);
        if (key < 0) return;

        mService.releaseWakeLock(mCallerId, key);
    }

    private void handleAcquireResult(int id, int resultCode, long timeout) {
        RemoteWakeLock wakeLock = mWakeLocks.get(id);
        if (wakeLock == null) return;

        Message msg = mUIHandler.obtainMessage(MSG_ACQUIRE_RESULT, resultCode, -1, wakeLock);
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_TIMEOUT, timeout);
        msg.setData(bundle);
        msg.sendToTarget();

        if (resultCode == RESULT_OK) {
            wakeLock.setIsHeld(true);

            if (timeout > 0) {
                Message m = mBGHandler.obtainMessage(MSG_RESET_HELD, wakeLock);
                mBGHandler.sendMessageDelayed(m, timeout);
            }
        } else {
            wakeLock.setIsHeld(false);
        }
    }

    private static <T> int getKey(SparseArray<T> array, T value) {
        if (value == null || array == null) return -1;

        int idx = array.indexOfValue(value);
        if (idx < 0) return -1;

        return array.keyAt(idx);
    }

    private void handleResetHeld(RemoteWakeLock wakeLock) {
        if (wakeLock != null) {
            wakeLock.setIsHeld(false);
        }
    }

    /**
     * 定义了远程锁回调函数的接口
     */
    public interface RemoteWakeLockCallback {

        /**
         * 远程持锁结果回调函数，调用{@link RemoteWakeLock#acquire()}或{@link RemoteWakeLock#acquire(long)}
         * 方法产生结果后通过此回调函数返回。
         * 
         * @param wakeLock 产生持锁结果的远程锁
         * @param timeout 之前持锁用户给定的超时，无超时为-1。
         * @param resultCode 此次持锁的结果。{@link RemoteWakeLockManager#RESULT_OK}、
         *        {@link RemoteWakeLockManager#RESULT_FAILED_CHANNEL_UNAVAILABLE}、
         *        {@link RemoteWakeLockManager#RESULT_FAILED_LINK_DISCONNECTED}或
         *        {@link RemoteWakeLockManager#RESULT_FAILED_IWDS_CRASH}
         * 
         * @see RemoteWakeLock#release()
         * @see RemoteWakeLock#isHeld()
         */
        void onAcquireResult(RemoteWakeLock wakeLock, long timeout, int resultCode);
    }

    private class RemoteWakeLockCallbackStub extends IRemoteWakeLockCallback.Stub {

        @Override
        public void performAcquireResult(int id, int resultCode, long timeout) {
            mBGHandler.obtainMessage(MSG_ACQUIRE_RESULT, id, resultCode, timeout).sendToTarget();
        }

        @Override
        public void performAvailableChanged(boolean isAvailable) {
            if (!isAvailable) {
                if (mWakeLocks == null) return;

                int N = mWakeLocks.size();
                for (int i = 0; i < N; i++) {
                    RemoteWakeLock wakeLock = mWakeLocks.valueAt(i);

                    if (mBGHandler.hasMessages(MSG_RESET_HELD, wakeLock)) {
                        mBGHandler.removeMessages(MSG_RESET_HELD, wakeLock);
                    }

                    mBGHandler.obtainMessage(MSG_RESET_HELD, wakeLock).sendToTarget();
                }
            }
        }
    }

    /**
     * 远程锁，对应于对端设备的{@link PowerManager.WakeLock}。用于唤醒对端设备等操作。远程锁的接口参考{@link PowerManager.WakeLock}
     * 的接口设计
     * 
     * @see PowerManager.WakeLock
     */
    public final class RemoteWakeLock {

        private int mFlags;
        private String mTag;
        private boolean mIsHeld;

        RemoteWakeLock(int levelAndFlags, String tag) {
            mFlags = levelAndFlags;
            mTag = tag;
        }

        /**
         * 持锁。长时间持锁，需要调用{@link #release()}释放锁。
         * 
         * @see PowerManager.WakeLock#acquire()
         * @see #acquire(long)
         * @see #release()
         * @see RemoteWakeLockCallback#onAcquireResult(RemoteWakeLock, long, int)
         */
        public void acquire() {
            acquire(-1);
        }

        /**
         * 持锁，在timeout时间内持锁，超时后将自动释放。
         * 
         * @param timeout 持锁超时，超时后自动释放锁
         * 
         * @see PowerManager.WakeLock#acquire(long)
         * @see #acquire()
         * @see #release()
         * @see RemoteWakeLockCallback#onAcquireResult(RemoteWakeLock, long, int)
         */
        public void acquire(long timeout) {
            Message msg = mBGHandler.obtainMessage(MSG_ACQUIRE_WAKELOCK, this);
            Bundle bundle = new Bundle();
            bundle.putLong(EXTRA_TIMEOUT, timeout);
            msg.setData(bundle);
            msg.sendToTarget();
        }

        /**
         * 释放之前的持锁。
         * 
         * @see PowerManager.WakeLock#release()
         * @see #acquire()
         * @see #acquire(long)
         * @see #isHeld()
         */
        public void release() {
            mBGHandler.obtainMessage(MSG_RELEASE_WAKELOCK, this).sendToTarget();
            setIsHeld(false);
        }

        /**
         * 是否正在持锁。
         * <p>
         * 注意：是否正在持锁的值将在调用了{@link #acquire()}或{@link #acquire(long)}方法返回结果为成功之后(即触发
         * {@link RemoteWakeLockCallback#onAcquireResult(RemoteWakeLock, long, int)}回调方法并且返回结果为
         * {@link RemoteWakeLockManager#RESULT_OK})或者调用了{@link #release()}
         * 方法之后会更新，可以简单表明远程设备的持锁状态，却不是实时与对端设备的持锁状态相关联。
         * 
         * @return 正在持锁返回true，否则返回false。
         * 
         * @see PowerManager.WakeLock#isHeld()
         * @see #release()
         * @see RemoteWakeLockCallback#onAcquireResult(RemoteWakeLock, long, int)
         */
        public boolean isHeld() {
            return mIsHeld;
        }

        private void setIsHeld(boolean isHeld) {
            mIsHeld = isHeld;

            if (mBGHandler.hasMessages(MSG_RESET_HELD, this)) {
                mBGHandler.removeMessages(MSG_RESET_HELD, this);
            }
        }
    }

    private class WakeLockHandler extends Handler {
        public WakeLockHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_NEW_REMOTE_WAKELOCK:
                handleNewWakeLock((RemoteWakeLock) msg.obj);
                break;

            case MSG_ACQUIRE_WAKELOCK: {
                Bundle bundle = msg.getData();
                long timeout = bundle.getLong(EXTRA_TIMEOUT, -1);

                handleAcquireWakeLock((RemoteWakeLock) msg.obj, timeout);
                break;
            }

            case MSG_RELEASE_WAKELOCK:
                handleReleaseWakeLock((RemoteWakeLock) msg.obj);
                break;

            case MSG_ACQUIRE_RESULT:
                handleAcquireResult(msg.arg1, msg.arg2, (Long) msg.obj);
                break;

            case MSG_RESET_HELD:
                handleResetHeld((RemoteWakeLock) msg.obj);
                break;

            case MSG_DESTROY_WAKELOCKS:
                handleDestroyWakeLocks();
                break;

            case MSG_QUIT_BACKGROUND_THREAD:
                handleQuitBackground();
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
            case MSG_ACQUIRE_RESULT: {
                Bundle bundle = msg.getData();
                long timeout = bundle.getLong(EXTRA_TIMEOUT);

                handleAcquireResultInUI((RemoteWakeLock) msg.obj, timeout, msg.arg1);
                break;
            }

            default:
                break;
            }
        }
    }

    private void handleAcquireResultInUI(RemoteWakeLock wakeLock, long timeout, int resultCode) {
        if (mCallback != null) {
            mCallback.onAcquireResult(wakeLock, timeout, resultCode);
        }
    }
}