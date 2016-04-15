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

import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.content.pm.PackageStats;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

/**
 * 远程设备管理服务的管理器，可以用于远程设备的应用管理（获取应用列表、安装卸载应用、清除应用数据和缓存）、 进程管理，以及获取远程设备的系统内存信息。
 */
public class RemoteDeviceServiceManager extends ServiceManagerContext {

    private IRemoteDeviceService mService;
    private RemoteDeviceStatusCallback mStatusCallback;
    private RemoteDeviceAppCallback mAppCallback;
    private RemoteDeviceProcessCallback mProcessCallback;
    private RemoteDeviceSettingCallback mSettingCallback;
    private String mUuid;

    public RemoteDeviceServiceManager(Context context) {
        super(context);

        mUuid = UUID.randomUUID().toString();

        m_serviceClientProxy = new ServiceClientProxy() {
            @Override
            public void onServiceConnected(IBinder service) {
                mService = IRemoteDeviceService.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(boolean unexpected) {
            }

            @Override
            public IBinder getBinder() {
                return mService.asBinder();
            }
        };
    }

    /**
     * 请求获取远程设备的应用列表。应用列表通过回调
     * {@link RemoteDeviceAppListener#onRemoteAppInfoListAvailable(List)} 返回。
     */
    public void requestGetAppList() {
        try {
            mService.requestGetAppList(mUuid);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in requestGetAppList: " + e.toString());
        }
    }

    /**
     * 请求获取远程设备的存储空间信息。存储空间信息通过回调
     * {@link RemoteDeviceAppListener#onRemoteStorageInfoAvailable(RemoteStorageInfo)}
     * 返回。
     */
    public void requestGetStorageInfo() {
        try {
            mService.requestGetStorageInfo(mUuid);
        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in requestGetStorageInfo: " + e.toString());
        }
    }

    /**
     * 请求在远程设备安装应用。安装结果通过回调
     * {@link RemoteDeviceAppListener#onDoneInstallApp(String, int)}返回。
     * 
     * @param apkFilePath
     *            apk文件在本地设备的路径
     * @param isInstalledInExternal
     *            表示是否安装在远程设备的外部存储空间，如果为真，安装在外部存储；否则安装在内部存储。
     */
    public void requestInstallApp(String apkFilePath,
            boolean isInstalledInExternal) {
        try {
            mService.requestInstallApp(mUuid, apkFilePath,
                    isInstalledInExternal);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in requestInstallApp: " + e.toString());
        }
    }

    /**
     * 请求在远程设备卸载应用。卸载结果通过回调
     * {@link RemoteDeviceAppListener#onDoneDeleteApp(String, int)}返回。
     * 
     * @param packageName
     *            被请求卸载的应用包名
     */
    public void requestDeleteApp(String packageName) {
        try {
            mService.requestDeleteApp(mUuid, packageName);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in requestDeleteApp: " + e.toString());
        }
    }

    /**
     * 请求获取远程设备的应用信息。应用信息通过回调
     * {@link RemoteDeviceAppListener#onResponsePkgSizeInfo(PackageStats, int)}
     * 返回。
     * 
     * @param packageName
     *            被请求信息的应用包名
     */
    public void requestPkgSizeInfo(String packageName) {
        try {
            mService.requestPkgSizeInfo(mUuid, packageName);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in requestPkgSizeInfo: " + e.toString());
        }
    }

    /**
     * 请求清除远程设备应用的用户数据或缓存。清除结果通过
     * {@link RemoteDeviceAppListener#onResponseClearAppDataOrCache(String, int, int)}
     * 返回。
     * 
     * @param packageName
     *            被请求清除数据或缓存的应用包名
     * @param requestType
     *            请求的操作类型，可能的值为：
     *            {@link RemoteDeviceManagerInfo#TYPE_CLEAR_APP_USER_DATA}，
     *            {@link RemoteDeviceManagerInfo#TYPE_CLEAR_APP_CACHE}
     */
    public void requestClearAppDataOrCache(String packageName, int requestType) {
        try {
            mService.requestClearAppDataOrCache(mUuid, packageName, requestType);
        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in requestClearAppDataOrCache: " + e.toString());
        }
    }

    /**
     * 请求清除远程设备所有应用的用户数据或缓存。清除结果通过
     * {@link RemoteDeviceAppListener#onResponseClearAllAppDataAndCache(int, int, String, int, int)}
     * 返回。
     */
    public void requestClearAllAppDataAndCache() {
        try {
            mService.requestClearAllAppDataAndCache(mUuid);
        } catch (RemoteException e) {
            IwdsLog.e(
                    this,
                    "Exception in requestClearAllAppDataAndCache: "
                            + e.toString());
        }
    }

    /**
     * 请求获取远程设备系统内存信息。内存信息通过回调
     * {@link RemoteDeviceProcessListener#onResponseSystemMemoryInfo(long, long)}
     * 返回。
     */
    public void requestSystemMemoryInfo() {
        try {
            mService.requestSystemMemoryInfo(mUuid);
        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in requestSystemMemoryInfo: " + e.toString());
        }
    }

    /**
     * 请求获取远程设备正在运行的进程信息。正在运行的进程信息通过回调
     * {@link RemoteDeviceProcessListener#onResponseRunningAppProcessInfo(List)}
     * 返回。
     */
    public void requestRunningAppProcessInfo() {
        try {
            mService.requestRunningAppProcessInfo(mUuid);
        } catch (RemoteException e) {
            IwdsLog.e(
                    this,
                    "Exception in requestRunningAppProcessInfo: "
                            + e.toString());
        }
    }

    /**
     * 请求杀远程设备的进程。结果通过回调
     * {@link RemoteDeviceProcessListener#onDoneKillProcess(String)}返回。
     * 
     * @param packageName
     *            进程对应的应用包名
     */
    public void requestKillProcess(String packageName) {
        try {
            mService.requestKillProcess(mUuid, packageName);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in requestKillProcess: " + e.toString());
        }
    }

    /**
     * 注册远程设备服务的连接状态监听器。
     * 
     * @param listener
     *            连接状态监听器
     * @return 返回真，表示注册成功；否则为失败。
     */
    public boolean registerStatusListener(RemoteDeviceStatusListener listener) {
        IwdsAssert.dieIf(this, listener == null, "Listener is null.");

        if (mStatusCallback != null) {
            IwdsLog.e(this, "Unable to register listener:"
                    + " Do you forget to call unregisterStatusListener?");
        } else {
            mStatusCallback = new RemoteDeviceStatusCallback(listener);
        }

        try {
            return mService.registerStatusListener(mUuid, mStatusCallback);
        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in registerStatusListener: " + e.toString());
            return false;
        }
    }

    /**
     * 注销远程设备服务的连接状态监听器。
     */
    public void unregisterStatusListener() {
        if (mStatusCallback == null)
            return;

        try {
            mService.unregisterStatusListener(mStatusCallback);
        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in unregisterStatusListener: " + e.toString());
        }
        mStatusCallback = null;
    }

    /**
     * 注册远程设备服务的应用管理监听器。
     * 
     * @param listener
     *            远程设备服务的应用管理监听器
     */
    public boolean registerAppListener(RemoteDeviceAppListener listener) {
        IwdsAssert.dieIf(this, listener == null, "Listener is null.");

        if (mAppCallback != null) {
            IwdsLog.e(this, "Unable to register listener:"
                    + " Do you forget to call unregisterAppListener?");
            return false;
        } else {
            mAppCallback = new RemoteDeviceAppCallback(listener);
        }

        try {
            return mService.registerAppListener(mUuid, mAppCallback);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in registerAppListener: " + e.toString());
            return false;
        }
    }

    /**
     * 注销远程设备服务的应用管理监听器。
     */
    public void unregisterAppListener() {
        if (mAppCallback == null)
            return;

        try {
            mService.unregisterAppListener(mAppCallback);
        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in unregisterAppListener: " + e.toString());
        }
        mAppCallback = null;
    }

    /**
     * 注册远程设备服务的进程管理监听器。
     * 
     * @param listener
     *            进程管理监听器
     * @return 返回真，表示注册成功；否则为失败。
     */
    public boolean registerProcessListener(RemoteDeviceProcessListener listener) {
        IwdsAssert.dieIf(this, listener == null, "Listener is null.");

        if (mProcessCallback != null) {
            IwdsLog.e(this, "Unable to register listener:"
                    + " Do you forget to call unregisterProcessListener?");
            return false;
        } else {
            mProcessCallback = new RemoteDeviceProcessCallback(listener);
        }

        try {
            return mService.registerProcessListener(mUuid, mProcessCallback);
        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in registerProcessListener: " + e.toString());
            return false;
        }
    }

    /**
     * 注销远程设备服务的进程管理监听器。
     */
    public void unregisterProcessListener() {
        if (mProcessCallback == null)
            return;

        try {
            mService.unregisterProcessListener(mProcessCallback);
        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in unregisterProcessListener: " + e.toString());
        }
        mProcessCallback = null;
    }

    /**
     * 注册远程设备服务的设置管理监听器。
     * 
     * @param listener
     *            设置管理监听器
     * @return 返回真，表示注册成功；否则为失败。
     */
    public boolean registerSettingListener(RemoteDeviceSettingListener listener) {
        IwdsAssert.dieIf(this, listener == null, "Listener is null.");

        if (mSettingCallback != null) {
            IwdsLog.e(this, "Unable to register listener:"
                    + " Do you forget to call unregisterSettingListener?");
            return false;
        } else {
            mSettingCallback = new RemoteDeviceSettingCallback(listener);
        }

        try {
            return mService.registerSettingListener(mUuid, mSettingCallback);
        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in registerSettingListener: " + e.toString());
            return false;
        }
    }

    /**
     * 注销远程设备服务的设置管理监听器。
     */
    public void unregisterSettingListener() {
        if (mSettingCallback == null)
            return;

        try {
            mService.unregisterSettingListener(mSettingCallback);
        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in unregisterSettingListener: " + e.toString());
        }
        mSettingCallback = null;
    }

    /**
     * 请求设置远程设备。结果通过回调 {@link RemoteDeviceSettingListener#onDoneSetting(int, int)}
     * 返回。
     * 
     * @param type
     *            可能的值：
     *            {@link RemoteDeviceManagerInfo#TYPE_SETTING_WEAR_ON_WHICH_HAND}
     *
     * @param value
     *            可能的值：{@link RemoteDeviceManagerInfo#VALUE_WEAR_ON_RIGHT_HAND}，
     *            {@link RemoteDeviceManagerInfo#VALUE_WEAR_ON_LEFT_HAND}
     */
    public void requestDoSetting(int type, int value) {
        try {
            mService.requestDoSetting(mUuid, type, value);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in requestSetting: " + e.toString());
        }
    }

    /**
     * 请求查询远程设备的设置。结果通过回调
     * {@link RemoteDeviceSettingListener#onGetSetting(int, int)}返回。
     * 
     * @param type
     *            可能的值：
     *            {@link RemoteDeviceManagerInfo#TYPE_SETTING_WEAR_ON_WHICH_HAND}
     */
    public void requestGetSetting(int type) {
        try {
            mService.requestGetSetting(mUuid, type);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in requestGetSetting: " + e.toString());
        }
    }

    private class RemoteDeviceAppCallback extends IRemoteDeviceAppCallback.Stub {
        private RemoteDeviceAppListener m_appListener;
        private static final int TYPE_INSTALL_DONE = 0;
        private static final int TYPE_INSTALL_DOING = 1;

        private Handler m_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case RemoteDeviceManagerInfo.TYPE_GET_APP_LIST: {
                    List<RemoteApplicationInfo> remoteAppList;
                    remoteAppList = ((RemoteApplicationInfoList) msg.obj)
                            .getList();
                    m_appListener.onRemoteAppInfoListAvailable(remoteAppList);
                    break;
                }

                case RemoteDeviceManagerInfo.TYPE_GET_STORAGE_INFO: {
                    RemoteStorageInfo storageInfo = (RemoteStorageInfo) msg.obj;
                    m_appListener.onRemoteStorageInfoAvailable(storageInfo);
                    break;
                }

                case RemoteDeviceManagerInfo.TYPE_INSTALL_APP: {
                    String packageName = (String) msg.obj;
                    int type = msg.arg1;
                    int value = msg.arg2;
                    if (type == TYPE_INSTALL_DONE) {
                        m_appListener.onDoneInstallApp(packageName, value);
                    } else {
                        m_appListener.onSendFileProgressForInstall(packageName,
                                value);
                    }

                    break;
                }

                case RemoteDeviceManagerInfo.TYPE_DELETE_APP: {
                    String packageName = (String) msg.obj;
                    int returnCode = msg.arg1;
                    m_appListener.onDoneDeleteApp(packageName, returnCode);
                    break;
                }

                case RemoteDeviceManagerInfo.TYPE_PKG_SIZE_INFO: {
                    PackageStats stats = (PackageStats) msg.obj;
                    int returnCode = msg.arg1;
                    IwdsLog.i(this, " TYPE_PKG_SIZE_INFO stats " + stats);
                    m_appListener.onResponsePkgSizeInfo(stats, returnCode);
                    break;
                }

                case RemoteDeviceManagerInfo.TYPE_CLEAR_APP_CACHE:
                case RemoteDeviceManagerInfo.TYPE_CLEAR_APP_USER_DATA: {
                    String packageName = (String) msg.obj;
                    int requestType = msg.arg1;
                    int returnCode = msg.arg2;
                    m_appListener.onResponseClearAppDataOrCache(packageName,
                            requestType, returnCode);
                    break;
                }

                case RemoteDeviceManagerInfo.TYPE_CLEAR_ALL_APP_DATA_CACHE: {
                    ClearAllAppDataMsgObj msgObj = (ClearAllAppDataMsgObj) msg.obj;
                    int totalCount = msgObj.totalCount;
                    int index = msgObj.index;
                    String packageName = msgObj.packageName;
                    int typeOfIndex = msgObj.typeOfIndex;
                    int returnCode = msgObj.returnCode;

                    m_appListener.onResponseClearAllAppDataAndCache(totalCount,
                            index, packageName, typeOfIndex, returnCode);
                    break;
                }

                default:
                    IwdsAssert.dieIf(this, true, "Implement me.");
                }
            }
        };

        public RemoteDeviceAppCallback(RemoteDeviceAppListener listener) {
            m_appListener = listener;
        }

        @Override
        public void onRemoteAppInfoListAvailable(
                RemoteApplicationInfoList remoteAppInfoList)
                throws RemoteException {

            Message.obtain(m_handler,
                    RemoteDeviceManagerInfo.TYPE_GET_APP_LIST,
                    remoteAppInfoList).sendToTarget();
        }

        @Override
        public void onRemoteStorageInfoAvailable(RemoteStorageInfo storageInfo)
                throws RemoteException {
            Message.obtain(m_handler,
                    RemoteDeviceManagerInfo.TYPE_GET_STORAGE_INFO, storageInfo)
                    .sendToTarget();
        }

        @Override
        public void onSendFileProgressForInstall(String packageName,
                int progress) throws RemoteException {
            Message msg = Message.obtain(m_handler,
                    RemoteDeviceManagerInfo.TYPE_INSTALL_APP, packageName);
            msg.arg1 = TYPE_INSTALL_DOING;
            msg.arg2 = progress;
            msg.sendToTarget();
        }

        @Override
        public void onDoneInstallApp(String packageName, int returnCode)
                throws RemoteException {
            Message msg = Message.obtain(m_handler,
                    RemoteDeviceManagerInfo.TYPE_INSTALL_APP, packageName);
            msg.arg1 = TYPE_INSTALL_DONE;
            msg.arg2 = returnCode;
            msg.sendToTarget();
        }

        @Override
        public void onDoneDeleteApp(String packageName, int returnCode)
                throws RemoteException {
            Message msg = Message.obtain(m_handler,
                    RemoteDeviceManagerInfo.TYPE_DELETE_APP, packageName);
            msg.arg1 = returnCode;

            msg.sendToTarget();
        }

        @Override
        public void onResponsePkgSizeInfo(RemotePackageStats stats,
                int returnCode) throws RemoteException {
            PackageStats localStats = stats.getLocalPackageStats();
            IwdsLog.d(this, " ===localStats " + localStats);
            Message msg = Message.obtain(m_handler,
                    RemoteDeviceManagerInfo.TYPE_PKG_SIZE_INFO, localStats);
            msg.arg1 = returnCode;

            msg.sendToTarget();
        }

        @Override
        public void onResponseClearAppDataOrCache(String packageName,
                int requestType, int returnCode) throws RemoteException {
            Message msg = Message.obtain(m_handler, requestType, packageName);
            msg.arg1 = requestType;
            msg.arg2 = returnCode;
            msg.sendToTarget();
        }

        @Override
        public void onResponseClearAllAppDataAndCache(int totalClearCount,
                int index, String packageName, int typeOfIndex, int returnCode)
                throws RemoteException {
            ClearAllAppDataMsgObj msgObj = new ClearAllAppDataMsgObj(
                    totalClearCount, index, packageName, typeOfIndex,
                    returnCode);
            Message msg = Message.obtain(m_handler,
                    RemoteDeviceManagerInfo.TYPE_CLEAR_ALL_APP_DATA_CACHE,
                    msgObj);
            msg.sendToTarget();
        }

        private class ClearAllAppDataMsgObj {
            int totalCount;
            int index;
            String packageName;
            int typeOfIndex;
            int returnCode;

            public ClearAllAppDataMsgObj(int totalCount, int index,
                    String packageName, int typeOfIndex, int returnCode) {
                this.totalCount = totalCount;
                this.index = index;
                this.packageName = packageName;
                this.typeOfIndex = typeOfIndex;
                this.returnCode = returnCode;
            }
        }
    }

    private class RemoteDeviceStatusCallback extends
            IRemoteDeviceStatusCallback.Stub {
        private static final int MSG_ON_REMOTE_DEVICE_READY = 0;

        private RemoteDeviceStatusListener m_statusListener;

        private Handler m_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_ON_REMOTE_DEVICE_READY:
                    boolean isReady = ((Boolean) msg.obj).booleanValue();
                    m_statusListener.onRemoteDeviceReady(isReady);
                    break;

                default:
                    break;
                }
            }
        };

        public RemoteDeviceStatusCallback(RemoteDeviceStatusListener listener) {
            m_statusListener = listener;
        }

        @Override
        public void onRemoteDeviceReady(boolean isReady) throws RemoteException {
            Boolean isRemoteDeviceReady = Boolean.valueOf(isReady);
            Message.obtain(m_handler, MSG_ON_REMOTE_DEVICE_READY,
                    isRemoteDeviceReady).sendToTarget();
        }
    }

    private class RemoteDeviceProcessCallback extends
            IRemoteDeviceProcessCallback.Stub {
        private RemoteDeviceProcessListener m_processListener;

        public RemoteDeviceProcessCallback(RemoteDeviceProcessListener listener) {
            m_processListener = listener;
        }

        private Handler m_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case RemoteDeviceManagerInfo.TYPE_GET_SYSTEM_MEM_INFO:
                    MemoryInfoMsg memInfo = (MemoryInfoMsg) msg.obj;
                    m_processListener.onResponseSystemMemoryInfo(
                            memInfo.availSize, memInfo.totalSize);
                    break;

                case RemoteDeviceManagerInfo.TYPE_GET_RUNNING_PROCESS_INFO:
                    List<RemoteProcessInfo> processInfoList = ((RemoteProcessInfoList) msg.obj).processInfoList;

                    m_processListener
                            .onResponseRunningAppProcessInfo(processInfoList);
                    break;

                case RemoteDeviceManagerInfo.TYPE_KILL_PROCESS:
                    String packageName = (String) msg.obj;
                    m_processListener.onDoneKillProcess(packageName);
                    break;

                default:
                    break;
                }
            }
        };

        @Override
        public void onResponseSystemMemoryInfo(long availMemSize,
                long totalMemSize) throws RemoteException {
            MemoryInfoMsg memInfo = new MemoryInfoMsg(availMemSize,
                    totalMemSize);
            Message.obtain(m_handler,
                    RemoteDeviceManagerInfo.TYPE_GET_SYSTEM_MEM_INFO, memInfo)
                    .sendToTarget();
        }

        private class MemoryInfoMsg {
            long availSize;
            long totalSize;

            public MemoryInfoMsg(long availSize, long totalSize) {
                this.availSize = availSize;
                this.totalSize = totalSize;
            }
        }

        @Override
        public void onResponseRunningAppProcessInfo(
                RemoteProcessInfoList processInfoList) throws RemoteException {
            Message.obtain(m_handler,
                    RemoteDeviceManagerInfo.TYPE_GET_RUNNING_PROCESS_INFO,
                    processInfoList).sendToTarget();
        }

        @Override
        public void onDoneKillProcess(String packageName)
                throws RemoteException {
            Message.obtain(m_handler,
                    RemoteDeviceManagerInfo.TYPE_KILL_PROCESS, packageName)
                    .sendToTarget();
        }
    }

    private class RemoteDeviceSettingCallback extends
            IRemoteDeviceSettingCallback.Stub {
        private RemoteDeviceSettingListener m_SettingListener;

        public RemoteDeviceSettingCallback(RemoteDeviceSettingListener listener) {
            m_SettingListener = listener;
        }

        private Handler m_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int type = msg.arg1;
                int returnCode = msg.arg2;

                switch (msg.what) {
                case RemoteDeviceManagerInfo.TYPE_DO_SETTING:
                    m_SettingListener.onDoneSetting(type, returnCode);
                    break;

                case RemoteDeviceManagerInfo.TYPE_GET_SETTING:
                    m_SettingListener.onGetSetting(type, returnCode);
                    break;

                default:
                    break;
                }
            }
        };

        @Override
        public void onDoneSetting(int type, int returnCode)
                throws RemoteException {
            Message.obtain(m_handler, RemoteDeviceManagerInfo.TYPE_DO_SETTING,
                    type, returnCode).sendToTarget();
        }

        @Override
        public void onGetSetting(int type, int returnCode)
                throws RemoteException {
            Message.obtain(m_handler, RemoteDeviceManagerInfo.TYPE_GET_SETTING,
                    type, returnCode).sendToTarget();
        }
    }
}
