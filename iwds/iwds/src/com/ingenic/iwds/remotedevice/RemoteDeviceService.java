/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  Huanglihong(Regen) <lihong.huang@ingenic.com>
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

package com.ingenic.iwds.remotedevice;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.SparseArray;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.datatransactor.DataTransactor;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback;
import com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo.AppListResponse;
import com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo.ClearAllAppDataCacheResponse;
import com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo.ConfirmInstallResponse;
import com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo.PkgInfoResponse;
import com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo.ProcessInfoResponse;
import com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo.RemoteRequest;
import com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo.RemoteResponse;
import com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo.ResponseWithName;
import com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo.SettingResponse;
import com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo.StorageInfoResponse;
import com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo.SysMemResponse;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

public class RemoteDeviceService extends Service {

    private RemoteDeviceServiceStub mService = new RemoteDeviceServiceStub();

    private static String mAppUuid = "C3554F59-EA68-84F1-8C79-96907EF327D6";
    private DataTransactor mAppTransactor;

    private ServiceHandler mHandler;
    private HandlerThread mHandlerThread;
    private AppTransactorCallback mAppTransactorCallback = new AppTransactorCallback();

    private RemoteCallbackList<IRemoteDeviceAppCallback> mAppCallbacks;
    private RemoteCallbackList<IRemoteDeviceStatusCallback> mStatusCallbacks;
    private RemoteCallbackList<IRemoteDeviceProcessCallback> mProcessCallbacks;
    private RemoteCallbackList<IRemoteDeviceSettingCallback> mSettingCallbacks;

    private ListenerRegistration mListenerRegistration;

    private LinkedList<String> mGetAppListUUIDs;
    private LinkedList<String> mGetStorgeInfoUUIDs;
    private LinkedList<String> mClearAllAppDataUUIDs;
    private LinkedHashMap<String, String> mInstallAppUUIDs;
    private LinkedHashMap<String, String> mDeleteAppUUIDs;
    private HashMap<String, String> mGetPkgSizeUUIDs;
    private HashMap<String, String> mClearAppDataUUIDs;

    private LinkedList<String> mGetSysMemUUIDs;
    private LinkedList<String> mGetRunningProcessUUIDs;
    private LinkedList<String> mKillProcessUUIDs;

    private LinkedList<String> mDoSettingUUIDs;
    private LinkedList<String> mGetSettingUUIDs;

    @Override
    public void onCreate() {
        IwdsLog.d(this, "onCreate");
        super.onCreate();

        if (mAppTransactor == null) {
            mAppTransactor = new DataTransactor(this, mAppTransactorCallback,
                    mAppUuid);
        }

        // handler thread is needed because registering listener may block main
        // thread
        mHandlerThread = new HandlerThread("RemoteDeviceService");
        mHandlerThread.start();
        mHandler = new ServiceHandler(mHandlerThread.getLooper());

        mStatusCallbacks = new RemoteCallbackList<IRemoteDeviceStatusCallback>();
        mAppCallbacks = new RemoteCallbackList<IRemoteDeviceAppCallback>();
        mProcessCallbacks = new RemoteCallbackList<IRemoteDeviceProcessCallback>();
        mSettingCallbacks = new RemoteCallbackList<IRemoteDeviceSettingCallback>();
        mListenerRegistration = new ListenerRegistration();

        mGetAppListUUIDs = new LinkedList<String>();
        mGetStorgeInfoUUIDs = new LinkedList<String>();
        mClearAllAppDataUUIDs = new LinkedList<String>();
        mInstallAppUUIDs = new LinkedHashMap<String, String>();
        mDeleteAppUUIDs = new LinkedHashMap<String, String>();
        mGetPkgSizeUUIDs = new HashMap<String, String>();
        mClearAppDataUUIDs = new HashMap<String, String>();

        mGetSysMemUUIDs = new LinkedList<String>();
        mGetRunningProcessUUIDs = new LinkedList<String>();
        mKillProcessUUIDs = new LinkedList<String>();

        mGetSettingUUIDs = new LinkedList<String>();
        mDoSettingUUIDs = new LinkedList<String>();
    }

    @Override
    public void onDestroy() {
        IwdsLog.d(this, "onDestroy");

        super.onDestroy();

        mAppTransactor = null;

        mHandlerThread.quit();
        try {
            mHandlerThread.join();
        } catch (InterruptedException e) {
        }
        mHandlerThread = null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        IwdsLog.d(this, "onUnbind");

        mAppTransactor.stop();

        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        IwdsLog.d(this, "onBind");

        mAppTransactor.start();

        return mService;
    }

    private class RemoteDeviceServiceStub extends IRemoteDeviceService.Stub {

        @Override
        public boolean registerStatusListener(String uuid,
                IRemoteDeviceStatusCallback callback) throws RemoteException {
            synchronized (mListenerRegistration) {
                mHandler.registerStatusListener(uuid, callback);
                try {
                    while (!mListenerRegistration.isListenerRegistered(
                            ListenerRegistration.TYPE_STATUS, callback))
                        mListenerRegistration.wait();
                } catch (InterruptedException e) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public void unregisterStatusListener(
                IRemoteDeviceStatusCallback callback) throws RemoteException {
            synchronized (mListenerRegistration) {
                mHandler.unregisterStatusListener(callback);
                try {
                    while (mListenerRegistration.isListenerRegistered(
                            ListenerRegistration.TYPE_STATUS, callback))
                        mListenerRegistration.wait();
                } catch (InterruptedException e) {
                }
            }
        }

        @Override
        public boolean registerAppListener(String uuid,
                IRemoteDeviceAppCallback callback) throws RemoteException {
            synchronized (mListenerRegistration) {
                mHandler.registerAppListener(uuid, callback);
                try {
                    while (!mListenerRegistration.isListenerRegistered(
                            ListenerRegistration.TYPE_APP, callback))
                        mListenerRegistration.wait();
                } catch (InterruptedException e) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public void unregisterAppListener(IRemoteDeviceAppCallback callback)
                throws RemoteException {
            synchronized (mListenerRegistration) {
                mHandler.unregisterAppListener(callback);
                try {
                    while (mListenerRegistration.isListenerRegistered(
                            ListenerRegistration.TYPE_APP, callback))
                        mListenerRegistration.wait();
                } catch (InterruptedException e) {
                }
            }
        }

        @Override
        public boolean registerProcessListener(String uuid,
                IRemoteDeviceProcessCallback callback) throws RemoteException {
            synchronized (mListenerRegistration) {
                mHandler.registerProcessListener(uuid, callback);
                try {
                    while (!mListenerRegistration.isListenerRegistered(
                            ListenerRegistration.TYPE_PROCESS, callback))
                        mListenerRegistration.wait();
                } catch (InterruptedException e) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public void unregisterProcessListener(
                IRemoteDeviceProcessCallback callback) throws RemoteException {
            synchronized (mListenerRegistration) {
                mHandler.unregisterProcessListener(callback);
                try {
                    while (mListenerRegistration.isListenerRegistered(
                            ListenerRegistration.TYPE_PROCESS, callback))
                        mListenerRegistration.wait();
                } catch (InterruptedException e) {
                }
            }
        }

        @Override
        public void requestGetAppList(String uuid) throws RemoteException {
            IwdsLog.d(this, " request to get AppList, uuid " + uuid);
            mHandler.requestGetAppList(uuid);
        }

        @Override
        public void requestInstallApp(String uuid, String apkFilePath,
                boolean isInstalledInExternal) throws RemoteException {
            mHandler.requestInstallApp(uuid, apkFilePath, isInstalledInExternal);
        }

        @Override
        public void requestDeleteApp(String uuid, String packageName)
                throws RemoteException {
            mHandler.requestDeleteApp(uuid, packageName);
        }

        @Override
        public void requestPkgSizeInfo(String uuid, String packageName)
                throws RemoteException {
            mHandler.requestPkgSizeInfo(uuid, packageName);
        }

        @Override
        public void requestClearAppDataOrCache(String uuid, String packageName,
                int requestType) throws RemoteException {
            mHandler.requestClearAppDataOrCache(uuid, packageName, requestType);
        }

        @Override
        public void requestKillProcess(String uuid, String packageName)
                throws RemoteException {
            mHandler.requestKillProcess(uuid, packageName);
        }

        @Override
        public void requestRunningAppProcessInfo(String uuid)
                throws RemoteException {
            mHandler.requestRunningAppProcessInfo(uuid);
        }

        @Override
        public void requestSystemMemoryInfo(String uuid) throws RemoteException {
            mHandler.requestSystemMemoryInfo(uuid);
        }

        @Override
        public void requestClearAllAppDataAndCache(String uuid)
                throws RemoteException {
            mHandler.requestClearAllAppDataAndCache(uuid);
        }

        @Override
        public void requestGetStorageInfo(String uuid) throws RemoteException {
            mHandler.requestGetStorageInfo(uuid);
        }

        @Override
        public boolean registerSettingListener(String uuid,
                IRemoteDeviceSettingCallback callback) throws RemoteException {
            synchronized (mListenerRegistration) {
                mHandler.registerSettingListener(uuid, callback);
                try {
                    while (!mListenerRegistration.isListenerRegistered(
                            ListenerRegistration.TYPE_SETTING, callback))
                        mListenerRegistration.wait();
                } catch (InterruptedException e) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public void unregisterSettingListener(
                IRemoteDeviceSettingCallback callback) throws RemoteException {
            synchronized (mListenerRegistration) {
                mHandler.unregisterSettingListener(callback);
                try {
                    while (mListenerRegistration.isListenerRegistered(
                            ListenerRegistration.TYPE_SETTING, callback))
                        mListenerRegistration.wait();
                } catch (InterruptedException e) {
                }
            }
        }

        @Override
        public void requestGetSetting(String uuid, int type)
                throws RemoteException {
            mHandler.requestGetSetting(uuid, type);
        }

        @Override
        public void requestDoSetting(String uuid, int type, int value)
                throws RemoteException {
            mHandler.requestDoSetting(uuid, type, value);
        }
    }

    private class AppTransactorCallback implements DataTransactorCallback {
        @Override
        public void onLinkConnected(DeviceDescriptor descriptor,
                boolean isConnected) {
            // ignore it
        }

        @Override
        public void onChannelAvailable(boolean isAvailable) {
            mHandler.onRemoteDeviceStatusChanged(isAvailable);
        }

        @Override
        public void onSendResult(DataTransactResult result) {
            if (result.getTransferedObject() instanceof File
                    && result.getResultCode() != DataTransactResult.RESULT_OK) {
                mHandler.handleAppResponse(new ResponseWithName(
                        RemoteDeviceManagerInfo.TYPE_INSTALL_APP,
                        RemoteDeviceManagerInfo.INSTALL_FAILED_SEND_APK_FILE_ERROR));
            }
        }

        @Override
        public void onDataArrived(Object object) {
            if (object instanceof RemoteResponse)
                mHandler.handleAppResponse((RemoteResponse) object);
        }

        @Override
        public void onSendFileProgress(int progress) {
            mHandler.onSendFileProgress(progress);
        }

        @Override
        public void onRecvFileProgress(int progress) {
            // useless
        }

        @Override
        public void onSendFileInterrupted(int index) {

        }

        @Override
        public void onRecvFileInterrupted(int index) {

        }
    };

    private class ServiceHandler extends Handler {
        private static final int MSG_REMOTE_DEVICE_STATUS_CHANGED = 0;

        private static final int MSG_REGISTER_STATUS_LISTENER = 1;
        private static final int MSG_REGISTER_APP_LISTENER = 2;
        private static final int MSG_REGISTER_PROCESS_LISTENER = 3;
        private static final int MSG_REGISTER_SETTING_LISTENER = 4;

        private static final int MSG_UNREGISTER_STATUS_LISTENER = 10;
        private static final int MSG_UNREGISTER_APP_LISTENER = 11;
        private static final int MSG_UNREGISTER_PROCESS_LISTENER = 12;
        private static final int MSG_UNREGISTER_SETTING_LISTENER = 13;

        private static final int MSG_REQUEST_APPLIST = 20;
        private static final int MSG_REQUEST_INSTALL_APP = 21;
        private static final int MSG_REQUEST_DELETE_APP = 22;
        private static final int MSG_REQUEST_PKG_SIZEINFO = 23;
        private static final int MSG_REQUEST_CLEAR_APP_DATA_OR_CACHE = 24;
        private static final int MSG_REQUEST_SYS_MEM_INFO = 25;
        private static final int MSG_REQUEST_KILL_PROCESS = 26;
        private static final int MSG_REQUEST_RUNNING_PROCESS_INFO = 27;
        private static final int MSG_REQUEST_STORAGE_INFO = 28;
        private static final int MSG_REQUEST_CLEAR_ALL_APP_DATA_AND_CACHE = 29;
        private static final int MSG_REQUEST_DO_SETTING = 30;
        private static final int MSG_REQUEST_GET_SETTING = 31;

        private static final int MSG_RESPONSE_APPLIST = 40;
        private static final int MSG_RESPONSE_CONFIRM_INSTALL_APP = 41;
        private static final int MSG_RESPONSE_DONE_INSTALL_APP = 42;
        private static final int MSG_RESPONSE_DELETE_APP = 43;
        private static final int MSG_RESPONSE_PKG_SIZEINFO = 44;
        private static final int MSG_RESPONSE_CLEAR_APP_DATA_OR_CACHE = 45;
        private static final int MSG_RESPONSE_SYS_MEM_INFO = 46;
        private static final int MSG_RESPONSE_RUNNING_PROCESS_INFO = 47;
        private static final int MSG_RESPONSE_STORAGE_INFO = 48;
        private static final int MSG_RESPONSE_CLEAR_ALL_APP_DATA_AND_CACHE = 49;
        private static final int MSG_RESPONSE_KILL_PROCESS = 50;
        private static final int MSG_RESPONSE_DM_SERVICE_CONNECTED = 51;
        private static final int MSG_RESPONSE_DM_SERVICE_DISCONNECTED = 52;
        private static final int MSG_RESPONSE_DONE_SETTING = 53;
        private static final int MSG_RESPONSE_GET_SETTING = 54;

        private static final int MSG_SEND_FILE_PROGRESS_FOR_INSTALL = 60;

        private boolean mIsRemoteDeviceReady = false;
        private boolean mIsRemoteServiceConnected = false;

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_REMOTE_DEVICE_STATUS_CHANGED:
                boolean isReady = (msg.arg1 == 1) ? true : false;
                if (!isReady)
                    mIsRemoteServiceConnected = false;

                doOnRemoteDeviceStatusChanged(isReady);
                mIsRemoteDeviceReady = isReady;
                break;

            case MSG_REGISTER_STATUS_LISTENER: {
                CallbackMessage<IRemoteDeviceStatusCallback> callbackMsg = (CallbackMessage<IRemoteDeviceStatusCallback>) msg.obj;
                String uuid = callbackMsg.uuid;
                IRemoteDeviceStatusCallback callback = callbackMsg.callback;

                synchronized (mListenerRegistration) {
                    mStatusCallbacks.register(callback, uuid);
                    mListenerRegistration.addListener(
                            ListenerRegistration.TYPE_STATUS, uuid, callback);
                    mListenerRegistration.notifyAll();
                }

                // for coldboot, onChannelAvailable() callback may be called
                // before listener registration, so notify the channel status
                // after registration.
                doOnRemoteDeviceStatusChanged(uuid, mIsRemoteDeviceReady);
            }
                break;

            case MSG_UNREGISTER_STATUS_LISTENER: {
                IRemoteDeviceStatusCallback callback = (IRemoteDeviceStatusCallback) msg.obj;

                synchronized (mListenerRegistration) {
                    mStatusCallbacks.unregister(callback);
                    mListenerRegistration.removeListener(
                            ListenerRegistration.TYPE_STATUS, callback);
                    mListenerRegistration.notifyAll();
                }
            }
                break;

            case MSG_REGISTER_APP_LISTENER: {
                CallbackMessage<IRemoteDeviceAppCallback> callbackMsg = (CallbackMessage<IRemoteDeviceAppCallback>) msg.obj;
                String uuid = callbackMsg.uuid;
                IRemoteDeviceAppCallback callback = callbackMsg.callback;

                synchronized (mListenerRegistration) {
                    mAppCallbacks.register(callback, uuid);
                    IwdsLog.d(this, "register app " + uuid);
                    mListenerRegistration.addListener(
                            ListenerRegistration.TYPE_APP, uuid, callback);
                    mListenerRegistration.notifyAll();
                }
            }
                break;

            case MSG_UNREGISTER_APP_LISTENER: {
                IRemoteDeviceAppCallback callback = (IRemoteDeviceAppCallback) msg.obj;

                String uuid;
                synchronized (mListenerRegistration) {
                    mAppCallbacks.unregister(callback);
                    uuid = mListenerRegistration.removeListener(
                            ListenerRegistration.TYPE_APP, callback);
                    mListenerRegistration.notifyAll();
                }

                clearAppRequestStack(uuid);
            }
                break;

            case MSG_REGISTER_PROCESS_LISTENER: {
                CallbackMessage<IRemoteDeviceProcessCallback> callbackMsg = (CallbackMessage<IRemoteDeviceProcessCallback>) msg.obj;
                String uuid = callbackMsg.uuid;
                IRemoteDeviceProcessCallback callback = callbackMsg.callback;

                synchronized (mListenerRegistration) {
                    mProcessCallbacks.register(callback, uuid);
                    IwdsLog.d(this, "register process " + uuid);
                    mListenerRegistration.addListener(
                            ListenerRegistration.TYPE_PROCESS, uuid, callback);
                    mListenerRegistration.notifyAll();
                }
            }
                break;

            case MSG_UNREGISTER_PROCESS_LISTENER: {
                IRemoteDeviceProcessCallback callback = (IRemoteDeviceProcessCallback) msg.obj;

                String uuid;
                synchronized (mListenerRegistration) {
                    mProcessCallbacks.unregister(callback);
                    uuid = mListenerRegistration.removeListener(
                            ListenerRegistration.TYPE_PROCESS, callback);
                    mListenerRegistration.notifyAll();
                }

                clearProcessRequestStack(uuid);
            }
                break;

            case MSG_REGISTER_SETTING_LISTENER: {
                CallbackMessage<IRemoteDeviceSettingCallback> callbackMsg = (CallbackMessage<IRemoteDeviceSettingCallback>) msg.obj;
                String uuid = callbackMsg.uuid;
                IRemoteDeviceSettingCallback callback = callbackMsg.callback;

                synchronized (mListenerRegistration) {
                    mSettingCallbacks.register(callback, uuid);
                    IwdsLog.d(this, "register sensor listener " + uuid);
                    mListenerRegistration.addListener(
                            ListenerRegistration.TYPE_SETTING, uuid, callback);
                    mListenerRegistration.notifyAll();
                }
            }
                break;

            case MSG_UNREGISTER_SETTING_LISTENER: {
                IRemoteDeviceSettingCallback callback = (IRemoteDeviceSettingCallback) msg.obj;

                String uuid;
                synchronized (mListenerRegistration) {
                    mSettingCallbacks.unregister(callback);
                    uuid = mListenerRegistration.removeListener(
                            ListenerRegistration.TYPE_SETTING, callback);
                    mListenerRegistration.notifyAll();
                }

                clearSettingRequestStack(uuid);
            }

                break;

            case MSG_REQUEST_APPLIST: {
                String uuid = (String) msg.obj;
                mGetAppListUUIDs.addFirst(uuid);
                mAppTransactor.send(new RemoteRequest(
                        RemoteDeviceManagerInfo.TYPE_GET_APP_LIST));
            }
                break;

            case MSG_REQUEST_STORAGE_INFO: {
                String uuid = (String) msg.obj;
                mGetStorgeInfoUUIDs.addFirst(uuid);
                mAppTransactor.send(new RemoteRequest(
                        RemoteDeviceManagerInfo.TYPE_GET_STORAGE_INFO));
            }
                break;

            case MSG_REQUEST_INSTALL_APP: {
                MsgObject msgObj = (MsgObject) msg.obj;
                String uuid = msgObj.uuid;
                String apkFilePath = msgObj.apkFilePath;
                boolean isInstalledInExternal = msgObj.isInstalledInExternal;

                doRequestInstallApp(uuid, apkFilePath, isInstalledInExternal);
            }
                break;

            case MSG_REQUEST_DELETE_APP: {
                MsgObject msgObj = (MsgObject) msg.obj;
                String uuid = msgObj.uuid;
                String packageName = msgObj.packageName;

                doRequestDeleteApp(uuid, packageName);
            }
                break;

            case MSG_REQUEST_PKG_SIZEINFO: {
                MsgObject msgObj = (MsgObject) msg.obj;
                String uuid = msgObj.uuid;
                String packageName = msgObj.packageName;

                doRequestPkgSizeInfo(uuid, packageName);
            }
                break;

            case MSG_REQUEST_CLEAR_APP_DATA_OR_CACHE: {
                MsgObject msgObj = (MsgObject) msg.obj;
                String uuid = msgObj.uuid;
                String packageName = msgObj.packageName;
                int requestType = msg.arg1;

                doRequestClearAppDataOrCache(uuid, packageName, requestType);
            }
                break;

            case MSG_REQUEST_CLEAR_ALL_APP_DATA_AND_CACHE: {
                String uuid = (String) msg.obj;

                if (!mClearAllAppDataUUIDs.isEmpty()
                        || !mClearAppDataUUIDs.isEmpty()) {
                    callbackClearAllAppDataAndCache(
                            uuid,
                            new ClearAllAppDataCacheResponse(
                                    RemoteDeviceManagerInfo.REQUEST_FAILED_PREVIOUS_DOING));
                    return;
                }

                mClearAllAppDataUUIDs.addFirst(uuid);
                mAppTransactor.send(new RemoteRequest(
                        RemoteDeviceManagerInfo.TYPE_CLEAR_ALL_APP_DATA_CACHE));
            }
                break;

            case MSG_REQUEST_SYS_MEM_INFO: {
                String uuid = (String) msg.obj;
                mGetSysMemUUIDs.addFirst(uuid);
                mAppTransactor.send(new RemoteRequest(
                        RemoteDeviceManagerInfo.TYPE_GET_SYSTEM_MEM_INFO));
            }
                break;

            case MSG_REQUEST_RUNNING_PROCESS_INFO: {
                String uuid = (String) msg.obj;
                mGetRunningProcessUUIDs.addFirst(uuid);
                mAppTransactor.send(new RemoteRequest(
                        RemoteDeviceManagerInfo.TYPE_GET_RUNNING_PROCESS_INFO));
            }
                break;

            case MSG_REQUEST_KILL_PROCESS: {
                MsgObject msgObj = (MsgObject) msg.obj;
                String uuid = msgObj.uuid;
                mKillProcessUUIDs.addFirst(uuid);

                String packageName = msgObj.packageName;
                mAppTransactor.send(new RemoteRequest(packageName,
                        RemoteDeviceManagerInfo.TYPE_KILL_PROCESS));
            }
                break;

            case MSG_REQUEST_GET_SETTING: {
                String uuid = (String) msg.obj;
                int subType = msg.arg1;
                if (!mIsRemoteServiceConnected) {
                    callbackGetSetting(
                            uuid,
                            subType,
                            RemoteDeviceManagerInfo.REQUEST_FAILED_SERVICE_DISCONNECTED);
                    return;
                }

                mGetSettingUUIDs.addFirst(uuid);

                mAppTransactor.send(new RemoteRequest(
                        RemoteDeviceManagerInfo.TYPE_GET_SETTING, subType));
            }
                break;

            case MSG_REQUEST_DO_SETTING: {
                String uuid = (String) msg.obj;
                int subType = msg.arg1;
                int value = msg.arg2;
                if (!mIsRemoteServiceConnected) {
                    callbackDoneSetting(
                            uuid,
                            subType,
                            RemoteDeviceManagerInfo.REQUEST_FAILED_SERVICE_DISCONNECTED);
                    return;
                }
                mDoSettingUUIDs.addFirst(uuid);

                RemoteRequest request = new RemoteRequest(
                        RemoteDeviceManagerInfo.TYPE_DO_SETTING, subType, value);
                mAppTransactor.send(request);
            }
                break;

            case MSG_RESPONSE_APPLIST: {
                AppListResponse response = (AppListResponse) msg.obj;
                doOnAppListReceived(response);
            }
                break;

            case MSG_RESPONSE_STORAGE_INFO: {
                StorageInfoResponse response = (StorageInfoResponse) msg.obj;
                doOnStorageInfoReceived(response);
            }
                break;

            case MSG_RESPONSE_CONFIRM_INSTALL_APP: {
                ConfirmInstallResponse response = (ConfirmInstallResponse) msg.obj;
                doOnConfirmInstallApp(response);
            }
                break;

            case MSG_RESPONSE_DONE_INSTALL_APP: {
                ResponseWithName response = (ResponseWithName) msg.obj;
                doOnDoneInstallApp(response);
            }
                break;

            case MSG_RESPONSE_DELETE_APP: {
                ResponseWithName response = (ResponseWithName) msg.obj;
                doOnDoneDeleteApp(response);
            }
                break;

            case MSG_RESPONSE_PKG_SIZEINFO: {
                PkgInfoResponse response = (PkgInfoResponse) msg.obj;
                doOnResponsePkgSizeInfo(response);
            }
                break;

            case MSG_RESPONSE_CLEAR_APP_DATA_OR_CACHE: {
                ResponseWithName response = (ResponseWithName) msg.obj;
                doOnResponseClearAppDataOrCache(response);
            }
                break;

            case MSG_RESPONSE_CLEAR_ALL_APP_DATA_AND_CACHE: {
                ClearAllAppDataCacheResponse response = (ClearAllAppDataCacheResponse) msg.obj;
                doOnResponseClearAllAppDataAndCache(response);
            }
                break;

            case MSG_RESPONSE_SYS_MEM_INFO: {
                SysMemResponse response = (SysMemResponse) msg.obj;
                doOnResponseSysMemInfo(response);
            }
                break;

            case MSG_RESPONSE_RUNNING_PROCESS_INFO: {
                ProcessInfoResponse response = (ProcessInfoResponse) msg.obj;
                doOnResponseRunningProcesses(response);
            }
                break;

            case MSG_RESPONSE_KILL_PROCESS: {
                ResponseWithName response = (ResponseWithName) msg.obj;
                doOnKillProcess(response);
            }
                break;

            case MSG_SEND_FILE_PROGRESS_FOR_INSTALL:
                int progress = msg.arg1;
                doOnSendFileProgressForInstall(progress);
                break;

            case MSG_RESPONSE_DM_SERVICE_CONNECTED:
                mIsRemoteServiceConnected = true;
                break;

            case MSG_RESPONSE_DM_SERVICE_DISCONNECTED:
                mIsRemoteServiceConnected = false;
                break;

            case MSG_RESPONSE_GET_SETTING: {
                SettingResponse response = (SettingResponse) msg.obj;
                doOnGetSetting(response);
            }
                break;

            case MSG_RESPONSE_DONE_SETTING: {
                SettingResponse response = (SettingResponse) msg.obj;
                doOnDoneSetting(response);
            }
                break;

            default:
                break;
            }
        }

        private void doOnSendFileProgressForInstall(int progress) {
            if (mInstallAppUUIDs.isEmpty())
                return;

            String packageName = getPackageName(mInstallAppUUIDs);
            String uuid = mInstallAppUUIDs.get(packageName);

            int N = mAppCallbacks.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (!uuid.equals(mAppCallbacks.getBroadcastCookie(i))) {
                    continue;
                }

                try {
                    mAppCallbacks
                            .getBroadcastItem(i)
                            .onSendFileProgressForInstall(packageName, progress);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing
                    // the dead object for us.
                }
                break;
            }
            mAppCallbacks.finishBroadcast();
        }

        private void doOnRemoteDeviceStatusChanged(String uuid, boolean isReady) {
            final int N = mStatusCallbacks.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (!uuid.equals(mStatusCallbacks.getBroadcastCookie(i)))
                    continue;

                try {
                    mStatusCallbacks.getBroadcastItem(i).onRemoteDeviceReady(
                            isReady);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing
                    // the dead object for us.
                }
                break;
            }
            mStatusCallbacks.finishBroadcast();
        }

        private void doOnRemoteDeviceStatusChanged(boolean isReady) {
            if (!isReady)
                clearAllRequestStack();

            final int N = mStatusCallbacks.beginBroadcast();
            for (int i = 0; i < N; i++) {
                try {
                    mStatusCallbacks.getBroadcastItem(i).onRemoteDeviceReady(
                            isReady);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing
                    // the dead object for us.
                }
            }
            mStatusCallbacks.finishBroadcast();
        }

        private void doRequestInstallApp(String uuid, String apkFilePath,
                boolean isInstalledInExternal) {

            boolean isApkExisted = new File(apkFilePath).isFile();
            if (!isApkExisted) {
                callbackDoneInstallApp(uuid, apkFilePath,
                        RemoteDeviceManagerInfo.INSTALL_FAILED_INVALID_URI);
                return;
            }

            PackageInfo pi = getPackageManager().getPackageArchiveInfo(
                    apkFilePath, 0);
            if (pi == null) {
                callbackDoneInstallApp(uuid, apkFilePath,
                        RemoteDeviceManagerInfo.INSTALL_FAILED_INVALID_APK);
                return;
            }

            String packageName = pi.packageName;

            IwdsLog.d(this, " request to install app " + packageName
                    + ", uuid " + uuid);

            // Only one installation request is allowed, for the returned
            // packageName from PackageManager is null when installation
            // failed
            if (!mInstallAppUUIDs.isEmpty()) {
                callbackDoneInstallApp(uuid, packageName,
                        RemoteDeviceManagerInfo.REQUEST_FAILED_PREVIOUS_DOING);
                return;
            }

            mInstallAppUUIDs.put(packageName, uuid);

            File apkFile = new File(apkFilePath);
            long size = apkFile.length();
            RemoteRequest request = new RemoteRequest(packageName,
                    RemoteDeviceManagerInfo.TYPE_INSTALL_APP, apkFilePath,
                    size, isInstalledInExternal);
            mAppTransactor.send(request);
        }

        private void doRequestDeleteApp(String uuid, String packageName) {
            // Only one deletion request is allowed, for the returned
            // packageName from PackageManager is null when deletion failed
            if (!mDeleteAppUUIDs.isEmpty()) {
                callbackDoneDeleteApp(uuid, packageName,
                        RemoteDeviceManagerInfo.REQUEST_FAILED_PREVIOUS_DOING);
                return;
            }

            mDeleteAppUUIDs.put(packageName, uuid);

            RemoteRequest request = new RemoteRequest(packageName,
                    RemoteDeviceManagerInfo.TYPE_DELETE_APP);
            mAppTransactor.send(request);
        }

        private void doRequestPkgSizeInfo(String uuid, String packageName) {
            if (mGetPkgSizeUUIDs.get(packageName) != null) {
                callbackResponsePkgSizeInfo(uuid, new RemotePackageStats(
                        packageName),
                        RemoteDeviceManagerInfo.REQUEST_FAILED_PREVIOUS_DOING);
                return;
            }

            mGetPkgSizeUUIDs.put(packageName, uuid);

            RemoteRequest request = new RemoteRequest(packageName,
                    RemoteDeviceManagerInfo.TYPE_PKG_SIZE_INFO);
            mAppTransactor.send(request);
        }

        private void doRequestClearAppDataOrCache(String uuid,
                String packageName, int requestType) {
            if (mClearAppDataUUIDs.get(packageName) != null
                    || !mClearAllAppDataUUIDs.isEmpty()) {
                callbackResponseClearAppDataOrCache(uuid, packageName,
                        requestType,
                        RemoteDeviceManagerInfo.REQUEST_FAILED_PREVIOUS_DOING);
                return;
            }

            mClearAppDataUUIDs.put(packageName, uuid);

            RemoteRequest request = new RemoteRequest(packageName, requestType);
            mAppTransactor.send(request);
        }

        private void callbackAppListReceived(String uuid,
                RemoteApplicationInfoList appList) {
            int N = mAppCallbacks.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (!uuid.equals(mAppCallbacks.getBroadcastCookie(i))) {
                    continue;
                }

                try {
                    mAppCallbacks.getBroadcastItem(i)
                            .onRemoteAppInfoListAvailable(appList);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing
                    // the dead object for us.
                }
                break;
            }
            mAppCallbacks.finishBroadcast();
        }

        private void doOnAppListReceived(AppListResponse response) {
            if (mGetAppListUUIDs.isEmpty())
                return;

            RemoteApplicationInfoList appList = response.appList;
            String uuid = mGetAppListUUIDs.removeLast();
            callbackAppListReceived(uuid, appList);
        }

        private void callbackStorageInfoReceived(String uuid,
                RemoteStorageInfo storageInfo) {
            int N = mAppCallbacks.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (!uuid.equals(mAppCallbacks.getBroadcastCookie(i))) {
                    continue;
                }

                try {
                    mAppCallbacks.getBroadcastItem(i)
                            .onRemoteStorageInfoAvailable(storageInfo);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing
                    // the dead object for us.
                }
                break;
            }
            mAppCallbacks.finishBroadcast();
        }

        private void doOnStorageInfoReceived(StorageInfoResponse response) {
            if (mGetStorgeInfoUUIDs.isEmpty())
                return;

            RemoteStorageInfo info = response.storageInfo;
            String uuid = mGetStorgeInfoUUIDs.removeLast();
            callbackStorageInfoReceived(uuid, info);
        }

        private void doOnConfirmInstallApp(ConfirmInstallResponse response) {
            if (mInstallAppUUIDs.isEmpty())
                return;

            int returnCode = response.returnCode;
            File apkFile = new File(response.apkFilePath);

            IwdsLog.d(this, "installation confirm, returnCode " + returnCode);

            String packageName = getPackageName(mInstallAppUUIDs);

            boolean isReturnError = (returnCode != RemoteDeviceManagerInfo.REQUEST_INSTALL_CONFIRM_OK);
            boolean isApkExist = apkFile.isFile();

            if (isReturnError || !isApkExist) {
                int error;
                if (isReturnError)
                    error = returnCode;
                else
                    error = RemoteDeviceManagerInfo.INSTALL_FAILED_INVALID_URI;

                ResponseWithName done = new ResponseWithName(packageName,
                        RemoteDeviceManagerInfo.TYPE_INSTALL_APP, error);
                doOnDoneInstallApp(done);
                return;
            }

            mAppTransactor.send(apkFile);
        }

        private void callbackDoneInstallApp(String uuid, String packageName,
                int returnCode) {
            int N = mAppCallbacks.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (!uuid.equals(mAppCallbacks.getBroadcastCookie(i))) {
                    continue;
                }

                try {
                    mAppCallbacks.getBroadcastItem(i).onDoneInstallApp(
                            packageName, returnCode);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing
                    // the dead object for us.
                }
                break;
            }
            mAppCallbacks.finishBroadcast();
        }

        private void doOnDoneInstallApp(ResponseWithName done) {
            if (mInstallAppUUIDs.isEmpty())
                return;

            int returnCode = done.returnCode;
            String packageName = getPackageName(mInstallAppUUIDs);
            String uuid = mInstallAppUUIDs.remove(packageName);

            IwdsLog.d(this, packageName + " installation done, return code: "
                    + returnCode + " uuid " + uuid);

            callbackDoneInstallApp(uuid, packageName, returnCode);
        }

        private void callbackDoneDeleteApp(String uuid, String packageName,
                int returnCode) {
            int N = mAppCallbacks.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (!uuid.equals(mAppCallbacks.getBroadcastCookie(i))) {
                    continue;
                }

                try {
                    mAppCallbacks.getBroadcastItem(i).onDoneDeleteApp(
                            packageName, returnCode);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing
                    // the dead object for us.
                }
                break;
            }
            mAppCallbacks.finishBroadcast();
        }

        private void doOnDoneDeleteApp(ResponseWithName done) {
            if (mDeleteAppUUIDs.isEmpty())
                return;

            int returnCode = done.returnCode;
            String packageName = getPackageName(mDeleteAppUUIDs);
            String uuid = mDeleteAppUUIDs.remove(packageName);

            IwdsLog.d(this, packageName + " deletion done, return code: "
                    + returnCode + " uuid " + uuid);

            callbackDoneDeleteApp(uuid, packageName, returnCode);
        }

        private void callbackResponsePkgSizeInfo(String uuid,
                RemotePackageStats stats, int returnCode) {
            int N = mAppCallbacks.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (!uuid.equals(mAppCallbacks.getBroadcastCookie(i))) {
                    continue;
                }

                try {
                    IwdsLog.d(this, " callbackResponsePkgSizeInfo stats " + stats
                            + " name " + stats.packageName + " N " + N + " i " + i);
                    mAppCallbacks.getBroadcastItem(i).onResponsePkgSizeInfo(
                            stats, returnCode);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing
                    // the dead object for us.
                }
                break;
            }
            mAppCallbacks.finishBroadcast();
        }

        private void doOnResponsePkgSizeInfo(PkgInfoResponse response) {
            RemotePackageStats stats = response.pkgStats;
            String packageName = response.pkgStats.packageName;
            IwdsLog.d(this, " doOnResponsePkgSizeInfo packageName" + packageName);
            String uuid = mGetPkgSizeUUIDs.remove(packageName);

            if (uuid == null)
                return;

            int returnCode = response.returnCode;

            callbackResponsePkgSizeInfo(uuid, stats, returnCode);
        }

        private void callbackResponseClearAppDataOrCache(String uuid,
                String packageName, int requestType, int returnCode) {
            int N = mAppCallbacks.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (!uuid.equals(mAppCallbacks.getBroadcastCookie(i))) {
                    continue;
                }

                try {
                    mAppCallbacks.getBroadcastItem(i)
                            .onResponseClearAppDataOrCache(packageName,
                                    requestType, returnCode);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing
                    // the dead object for us.
                }
                break;
            }
            mAppCallbacks.finishBroadcast();
        }

        private void doOnResponseClearAppDataOrCache(ResponseWithName response) {
            String packageName = response.packageName;
            String uuid = mClearAppDataUUIDs.remove(packageName);

            if (uuid == null)
                return;

            int requestType = response.type;
            int returnCode = response.returnCode;

            callbackResponseClearAppDataOrCache(uuid, packageName, requestType,
                    returnCode);
        }

        private void callbackClearAllAppDataAndCache(String uuid,
                ClearAllAppDataCacheResponse response) {
            int totalClearCount = response.totalCount;
            int index = response.index;
            String packageName = response.packageName;
            int typeOfIndex = response.typeOfIndex;
            int returnCode = response.returnCode;

            int N = mAppCallbacks.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (!uuid.equals(mAppCallbacks.getBroadcastCookie(i))) {
                    continue;
                }

                try {
                    mAppCallbacks
                            .getBroadcastItem(i)
                            .onResponseClearAllAppDataAndCache(totalClearCount,
                                    index, packageName, typeOfIndex, returnCode);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing
                    // the dead object for us.
                }
                break;
            }
            mAppCallbacks.finishBroadcast();
        }

        private void doOnResponseClearAllAppDataAndCache(
                ClearAllAppDataCacheResponse response) {
            if (mClearAllAppDataUUIDs.isEmpty())
                return;

            String uuid = mClearAllAppDataUUIDs.getLast();
            if (response.index == response.totalCount)
                mClearAllAppDataUUIDs.removeLast();

            callbackClearAllAppDataAndCache(uuid, response);
        }

        private void callbackResponseSysMemInfo(String uuid, long availSize,
                long totalSize) {
            int N = mProcessCallbacks.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (!uuid.equals(mProcessCallbacks.getBroadcastCookie(i))) {
                    continue;
                }

                try {
                    mProcessCallbacks.getBroadcastItem(i)
                            .onResponseSystemMemoryInfo(availSize, totalSize);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing
                    // the dead object for us.
                }
                break;
            }
            mProcessCallbacks.finishBroadcast();
        }

        private void doOnResponseSysMemInfo(SysMemResponse response) {
            if (mGetSysMemUUIDs.isEmpty())
                return;

            String uuid = mGetSysMemUUIDs.removeLast();
            callbackResponseSysMemInfo(uuid, response.availSysMemSize,
                    response.totalSysMemSize);
        }

        private void callbackResponseRunningProcesses(String uuid,
                RemoteProcessInfoList infoList) {
            int N = mProcessCallbacks.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (!uuid.equals(mProcessCallbacks.getBroadcastCookie(i))) {
                    continue;
                }

                try {
                    mProcessCallbacks.getBroadcastItem(i)
                            .onResponseRunningAppProcessInfo(infoList);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing
                    // the dead object for us.
                }
                break;
            }
            mProcessCallbacks.finishBroadcast();
        }

        private void doOnResponseRunningProcesses(ProcessInfoResponse response) {
            if (mGetRunningProcessUUIDs.isEmpty())
                return;

            String uuid = mGetRunningProcessUUIDs.removeLast();
            callbackResponseRunningProcesses(uuid, response.processList);
        }

        private void callbackDoneKillProcess(String uuid, String packageName) {
            int N = mProcessCallbacks.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (!uuid.equals(mProcessCallbacks.getBroadcastCookie(i))) {
                    continue;
                }

                try {
                    mProcessCallbacks.getBroadcastItem(i).onDoneKillProcess(
                            packageName);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing
                    // the dead object for us.
                }
                break;
            }
            mProcessCallbacks.finishBroadcast();
        }

        private void doOnKillProcess(ResponseWithName response) {
            if (mKillProcessUUIDs.isEmpty())
                return;

            String uuid = mKillProcessUUIDs.removeLast();
            callbackDoneKillProcess(uuid, response.packageName);
        }

        private void callbackGetSetting(String uuid, int subType, int returnCode) {
            int N = mSettingCallbacks.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (!uuid.equals(mSettingCallbacks.getBroadcastCookie(i))) {
                    continue;
                }

                try {
                    mSettingCallbacks.getBroadcastItem(i).onGetSetting(subType,
                            returnCode);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing
                    // the dead object for us.
                }
                break;
            }
            mSettingCallbacks.finishBroadcast();
        }

        private void doOnGetSetting(SettingResponse response) {
            if (mGetSettingUUIDs.isEmpty())
                return;

            String uuid = mGetSettingUUIDs.removeLast();
            callbackGetSetting(uuid, response.subType, response.returnCode);
        }

        private void callbackDoneSetting(String uuid, int subType,
                int returnCode) {
            int N = mSettingCallbacks.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (!uuid.equals(mSettingCallbacks.getBroadcastCookie(i))) {
                    continue;
                }

                try {
                    mSettingCallbacks.getBroadcastItem(i).onDoneSetting(
                            subType, returnCode);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing
                    // the dead object for us.
                }
                break;
            }
            mSettingCallbacks.finishBroadcast();
        }

        private void doOnDoneSetting(SettingResponse response) {
            if (mDoSettingUUIDs.isEmpty())
                return;

            String uuid = mDoSettingUUIDs.removeLast();
            callbackDoneSetting(uuid, response.subType, response.returnCode);
        }

        void registerStatusListener(String uuid,
                IRemoteDeviceStatusCallback callback) {
            CallbackMessage<IRemoteDeviceStatusCallback> callbackMsg = new CallbackMessage<IRemoteDeviceStatusCallback>(
                    uuid, callback);
            Message msg = Message.obtain(this, MSG_REGISTER_STATUS_LISTENER,
                    callbackMsg);
            msg.sendToTarget();
        }

        void unregisterStatusListener(IRemoteDeviceStatusCallback callback) {
            Message msg = Message.obtain(this, MSG_UNREGISTER_STATUS_LISTENER,
                    callback);
            msg.sendToTarget();
        }

        void registerAppListener(String uuid, IRemoteDeviceAppCallback callback) {
            CallbackMessage<IRemoteDeviceAppCallback> callbackMsg = new CallbackMessage<IRemoteDeviceAppCallback>(
                    uuid, callback);
            Message msg = Message.obtain(this, MSG_REGISTER_APP_LISTENER,
                    callbackMsg);
            msg.sendToTarget();
        }

        void unregisterAppListener(IRemoteDeviceAppCallback callback) {
            Message msg = Message.obtain(this, MSG_UNREGISTER_APP_LISTENER,
                    callback);
            msg.sendToTarget();
        }

        void registerProcessListener(String uuid,
                IRemoteDeviceProcessCallback callback) {
            CallbackMessage<IRemoteDeviceProcessCallback> callbackMsg = new CallbackMessage<IRemoteDeviceProcessCallback>(
                    uuid, callback);
            Message msg = Message.obtain(this, MSG_REGISTER_PROCESS_LISTENER,
                    callbackMsg);
            msg.sendToTarget();
        }

        void unregisterProcessListener(IRemoteDeviceProcessCallback callback) {
            Message msg = Message.obtain(this, MSG_UNREGISTER_PROCESS_LISTENER,
                    callback);
            msg.sendToTarget();
        }

        void registerSettingListener(String uuid,
                IRemoteDeviceSettingCallback callback) {
            CallbackMessage<IRemoteDeviceSettingCallback> callbackMsg = new CallbackMessage<IRemoteDeviceSettingCallback>(
                    uuid, callback);
            Message msg = Message.obtain(this, MSG_REGISTER_SETTING_LISTENER,
                    callbackMsg);
            msg.sendToTarget();
        }

        void unregisterSettingListener(IRemoteDeviceSettingCallback callback) {
            Message msg = Message.obtain(this, MSG_UNREGISTER_SETTING_LISTENER,
                    callback);
            msg.sendToTarget();
        }

        private class CallbackMessage<T> {
            String uuid;
            T callback;

            CallbackMessage(String uuid, T callback) {
                this.uuid = uuid;
                this.callback = callback;
            }
        }

        void requestGetAppList(String uuid) {
            Message msg = Message.obtain(this, MSG_REQUEST_APPLIST, uuid);
            msg.sendToTarget();
        }

        void requestGetStorageInfo(String uuid) {
            Message msg = Message.obtain(this, MSG_REQUEST_STORAGE_INFO, uuid);
            msg.sendToTarget();
        }

        void requestInstallApp(String uuid, String apkFilePath,
                boolean isInstalledInExternal) {
            MsgObject msgObj = new MsgObject(uuid, apkFilePath,
                    isInstalledInExternal);
            Message msg = Message.obtain(this, MSG_REQUEST_INSTALL_APP, msgObj);
            msg.sendToTarget();
        }

        private class MsgObject {
            String uuid;
            String apkFilePath;
            String packageName;
            boolean isInstalledInExternal;

            public MsgObject(String uuid, String apkFilePath,
                    boolean isInstalledInExternal) {
                this.uuid = uuid;
                this.apkFilePath = apkFilePath;
                this.isInstalledInExternal = isInstalledInExternal;
            }

            public MsgObject(String uuid, String packageName) {
                this.uuid = uuid;
                this.packageName = packageName;
            }
        }

        void requestDeleteApp(String uuid, String packageName) {
            MsgObject msgObj = new MsgObject(uuid, packageName);
            Message msg = Message.obtain(this, MSG_REQUEST_DELETE_APP, msgObj);
            msg.sendToTarget();
        }

        void requestPkgSizeInfo(String uuid, String packageName) {
            MsgObject msgObj = new MsgObject(uuid, packageName);
            Message msg = Message
                    .obtain(this, MSG_REQUEST_PKG_SIZEINFO, msgObj);
            msg.sendToTarget();
        }

        void requestClearAppDataOrCache(String uuid, String packageName,
                int requestType) {
            MsgObject msgObj = new MsgObject(uuid, packageName);
            Message msg = Message.obtain(this,
                    MSG_REQUEST_CLEAR_APP_DATA_OR_CACHE, msgObj);
            msg.arg1 = requestType;
            msg.sendToTarget();
        }

        void requestClearAllAppDataAndCache(String uuid) {
            Message.obtain(this, MSG_REQUEST_CLEAR_ALL_APP_DATA_AND_CACHE, uuid)
                    .sendToTarget();
        }

        void requestKillProcess(String uuid, String packageName) {
            MsgObject msgObj = new MsgObject(uuid, packageName);
            Message msg = Message
                    .obtain(this, MSG_REQUEST_KILL_PROCESS, msgObj);
            msg.sendToTarget();
        }

        void requestRunningAppProcessInfo(String uuid) {
            Message.obtain(this, MSG_REQUEST_RUNNING_PROCESS_INFO, uuid)
                    .sendToTarget();
        }

        void requestSystemMemoryInfo(String uuid) {
            Message.obtain(this, MSG_REQUEST_SYS_MEM_INFO, uuid).sendToTarget();
        }

        void requestGetSetting(String uuid, int type) {
            Message msg = Message.obtain(this, MSG_REQUEST_GET_SETTING, uuid);
            msg.arg1 = type;
            msg.sendToTarget();
        }

        void requestDoSetting(String uuid, int type, int value) {
            Message.obtain(this, MSG_REQUEST_DO_SETTING, type, value, uuid)
                    .sendToTarget();
        }

        void onRemoteDeviceStatusChanged(boolean isReady) {
            Message msg = Message
                    .obtain(this, MSG_REMOTE_DEVICE_STATUS_CHANGED);
            msg.arg1 = isReady ? 1 : 0;
            msg.sendToTarget();
        }

        void onSendFileProgress(int progress) {
            Message msg = Message.obtain(this,
                    MSG_SEND_FILE_PROGRESS_FOR_INSTALL);
            msg.arg1 = progress;
            msg.sendToTarget();
        }

        void handleAppResponse(RemoteResponse response) {
            final Message msg = Message.obtain(this);
            msg.obj = response;

            switch (response.type) {
            case RemoteDeviceManagerInfo.TYPE_GET_APP_LIST:
                msg.what = MSG_RESPONSE_APPLIST;
                break;

            case RemoteDeviceManagerInfo.TYPE_GET_STORAGE_INFO:
                msg.what = MSG_RESPONSE_STORAGE_INFO;
                break;

            case RemoteDeviceManagerInfo.TYPE_CONFIRM_INSTALL_APP:
                msg.what = MSG_RESPONSE_CONFIRM_INSTALL_APP;
                break;

            case RemoteDeviceManagerInfo.TYPE_INSTALL_APP:
                msg.what = MSG_RESPONSE_DONE_INSTALL_APP;
                break;

            case RemoteDeviceManagerInfo.TYPE_DELETE_APP:
                msg.what = MSG_RESPONSE_DELETE_APP;
                break;

            case RemoteDeviceManagerInfo.TYPE_PKG_SIZE_INFO:
                msg.what = MSG_RESPONSE_PKG_SIZEINFO;
                break;

            case RemoteDeviceManagerInfo.TYPE_CLEAR_APP_CACHE:
            case RemoteDeviceManagerInfo.TYPE_CLEAR_APP_USER_DATA:
                msg.what = MSG_RESPONSE_CLEAR_APP_DATA_OR_CACHE;
                break;

            case RemoteDeviceManagerInfo.TYPE_CLEAR_ALL_APP_DATA_CACHE:
                msg.what = MSG_RESPONSE_CLEAR_ALL_APP_DATA_AND_CACHE;
                break;

            case RemoteDeviceManagerInfo.TYPE_GET_SYSTEM_MEM_INFO:
                msg.what = MSG_RESPONSE_SYS_MEM_INFO;
                break;

            case RemoteDeviceManagerInfo.TYPE_GET_RUNNING_PROCESS_INFO:
                msg.what = MSG_RESPONSE_RUNNING_PROCESS_INFO;
                break;

            case RemoteDeviceManagerInfo.TYPE_KILL_PROCESS:
                msg.what = MSG_RESPONSE_KILL_PROCESS;
                break;

            case RemoteDeviceManagerInfo.TYPE_DM_SERVICE_CONNECTED:
                msg.what = MSG_RESPONSE_DM_SERVICE_CONNECTED;
                break;

            case RemoteDeviceManagerInfo.TYPE_DM_SERVICE_DISCONNECTED:
                msg.what = MSG_RESPONSE_DM_SERVICE_DISCONNECTED;
                break;

            case RemoteDeviceManagerInfo.TYPE_DO_SETTING:
                msg.what = MSG_RESPONSE_DONE_SETTING;
                break;

            case RemoteDeviceManagerInfo.TYPE_GET_SETTING:
                msg.what = MSG_RESPONSE_GET_SETTING;
                break;

            default:
                break;
            }

            msg.sendToTarget();
        }
    }

    private class ListenerRegistration {
        static final int TYPE_STATUS = 0;
        static final int TYPE_APP = 1;
        static final int TYPE_PROCESS = 2;
        static final int TYPE_SETTING = 3;

        SparseArray<HashMap<IInterface, String>> mCallbacks;

        ListenerRegistration() {
            mCallbacks = new SparseArray<HashMap<IInterface, String>>();
            mCallbacks.put(TYPE_STATUS, new HashMap<IInterface, String>());
            mCallbacks.put(TYPE_APP, new HashMap<IInterface, String>());
            mCallbacks.put(TYPE_PROCESS, new HashMap<IInterface, String>());
            mCallbacks.put(TYPE_SETTING, new HashMap<IInterface, String>());
        }

        void addListener(int type, String uuid, IInterface callback) {
            HashMap<IInterface, String> listenerMap = mCallbacks.get(type);
            listenerMap.put(callback, uuid);
        }

        String removeListener(int type, IInterface callback) {
            HashMap<IInterface, String> listenerMap = mCallbacks.get(type);
            return listenerMap.remove(callback);
        }

        boolean isListenerRegistered(int type, IInterface callback) {
            HashMap<IInterface, String> listenerMap = mCallbacks.get(type);
            return listenerMap.containsKey(callback);
        }
    };

    private void removeListItemByValue(LinkedList<String> list, Object o) {
        while (list.contains(o)) {
            list.remove(o);
        }
    }

    private void removeMapItemByValue(HashMap<String, String> map, Object value) {
        Collection<String> values = map.values();
        while (values.contains(value)) {
            values.remove(value);
        }
    }

    private void clearAppRequestStack(String uuid) {
        removeListItemByValue(mGetAppListUUIDs, uuid);
        removeListItemByValue(mGetStorgeInfoUUIDs, uuid);
        removeListItemByValue(mClearAllAppDataUUIDs, uuid);
        removeMapItemByValue(mInstallAppUUIDs, uuid);
        removeMapItemByValue(mDeleteAppUUIDs, uuid);
        removeMapItemByValue(mGetPkgSizeUUIDs, uuid);
        removeMapItemByValue(mClearAppDataUUIDs, uuid);
    }

    private void clearProcessRequestStack(String uuid) {
        removeListItemByValue(mGetSysMemUUIDs, uuid);
        removeListItemByValue(mGetRunningProcessUUIDs, uuid);
        removeListItemByValue(mKillProcessUUIDs, uuid);
    }

    private void clearSettingRequestStack(String uuid) {
        removeListItemByValue(mDoSettingUUIDs, uuid);
        removeListItemByValue(mGetSettingUUIDs, uuid);
    }

    private void clearAllAppRequestStack() {
        mGetAppListUUIDs.clear();
        mGetStorgeInfoUUIDs.clear();
        mClearAllAppDataUUIDs.clear();
        mInstallAppUUIDs.clear();
        mDeleteAppUUIDs.clear();
        mGetPkgSizeUUIDs.clear();
        mClearAppDataUUIDs.clear();
    }

    private void clearProcessRequestStack() {
        mGetSysMemUUIDs.clear();
        mGetRunningProcessUUIDs.clear();
        mKillProcessUUIDs.clear();
    }

    private void clearSensorRequestStack() {
        mDoSettingUUIDs.clear();
        mGetSettingUUIDs.clear();
    }

    private void clearAllRequestStack() {
        clearAllAppRequestStack();
        clearProcessRequestStack();
        clearSensorRequestStack();
    }

    private String getPackageName(HashMap<String, String> map) {
        if (map.isEmpty())
            return null;

        IwdsAssert.dieIf(this, (map.size() != 1),
                "the map should contain only one element");

        String packageName = map.entrySet().iterator().next().getKey();

        return packageName;
    }
}
