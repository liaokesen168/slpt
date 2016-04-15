package com.ingenic.iwds.remotedevice;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.net.Uri;
import android.os.Debug;
import android.os.Environment;
import android.os.RemoteException;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceClient.ConnectionCallbacks;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.datatransactor.DataTransactor;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback;
import com.ingenic.iwds.devicemanager.DeviceManagerServiceManager;
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
import com.ingenic.iwds.utils.IwdsLog;

@SuppressLint("NewApi")
public class DeviceManagerProxy {
    private static String mAppUuid = "C3554F59-EA68-84F1-8C79-96907EF327D6";

    /**
     * 清除手表应用数据时，发送的广播。此为action模板，例: "ingenic.intent.action.COM_INGENIC_HEALTH_DATA_CLEARED"。
     */
    public static final String CLEAR_APP_DATA_ACTION_TEMPLATE = "ingenic.intent.action.%s_DATA_CLEARED";
    private DataTransactor mAppTransactor;

    private ServiceClient mDMServiceClient;
    private DeviceManagerServiceManager mDM;
    private PackageManager mPM;
    private ActivityManager mAM;
    private static DeviceManagerProxy sInstance;

    private HashMap<String, Boolean> mInstallAppLocations;

    public Context mContext;

    private DeviceManagerProxy() {
    }

    public static DeviceManagerProxy getInstance() {
        if (sInstance == null)
            sInstance = new DeviceManagerProxy();

        return sInstance;
    }

    public void initialize(Context context) {
        IwdsLog.d(this, "initialize");

        mAppTransactor = new DataTransactor(context, mAppTransactorCallback,
                mAppUuid);
        mContext = context;
        mPM = context.getPackageManager();
        mAM = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        mDMServiceClient = new ServiceClient(context,
                ServiceManagerContext.SERVICE_DEVICE_MANAGER,
                mDMConnectionCallbacks);

        mInstallAppLocations = new HashMap<String, Boolean>();
    }

    public void start() {
        IwdsLog.i(this, "start");

        mAppTransactor.start();
    }

    private void onRequestGetAppList() {
        IwdsLog.d(this, " received request to get app list.");

        List<PackageInfo> allPkgList = mPM
                .getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);

        RemoteApplicationInfoList remoteAppList = new RemoteApplicationInfoList(
                mPM, allPkgList);
        AppListResponse response = new AppListResponse(remoteAppList);

        mAppTransactor.send(response);
    }

    private void onRequestGetStorageInfo() {
        File internalPath = Environment.getDataDirectory();
        long availInternalSize = internalPath.getFreeSpace();
        long totalInternalSize = internalPath.getTotalSpace();

        File externalPath = new File("/storage/sdcard1");
        boolean hasExternalStorage = externalPath.isDirectory();

        long availExternalSize = 0;
        long totalExternalSize = 0;
        if (hasExternalStorage) {
            availExternalSize = externalPath.getFreeSpace();
            totalExternalSize = externalPath.getTotalSpace();
        }

        RemoteStorageInfo info = new RemoteStorageInfo(availInternalSize,
                totalInternalSize, hasExternalStorage, availExternalSize,
                totalExternalSize);

        StorageInfoResponse response = new StorageInfoResponse(info);
        mAppTransactor.send(response);
    }

    private void onRequestInstallApp(RemoteRequest request) {
        File file = Environment.getExternalStorageDirectory();
        long usableSpace = file.getUsableSpace();
        long requiredSize = request.requiredSize;

        int errorCode = RemoteDeviceManagerInfo.REQUEST_INSTALL_CONFIRM_OK;
        String apkFilePath = request.apkFilePath;

        if (usableSpace < requiredSize) {
            // not enough space for storing temporary file for installation
            errorCode = RemoteDeviceManagerInfo.INSTALL_FAILED_INSUFFICIENT_STORAGE;
        }

        IwdsLog.d(this, "usableSpace: " + usableSpace
                + " bytes, requiredSize: " + requiredSize
                + " bytes, errorCode: " + errorCode);

        String apkFileName = apkFilePath
                .substring(apkFilePath.lastIndexOf('/') + 1);
        Boolean isInstalledInExternal = Boolean
                .valueOf(request.isInstalledInExternal);
        mInstallAppLocations.put(apkFileName, isInstalledInExternal);

        ConfirmInstallResponse confirm = new ConfirmInstallResponse(
                request.packageName, apkFilePath, errorCode);
        mAppTransactor.send(confirm);
    }

    private void onApkFileReceived(File apkFile) {
        IwdsLog.d(this, apkFile + " received for installation. size: "
                + apkFile.length());

        Uri packageUri = Uri.fromFile(apkFile);
        PackageInstallObserver observer = new PackageInstallObserver();
        String packageName = "";

        int installFlags = PackageManager.INSTALL_REPLACE_EXISTING;
        String apkFileName = apkFile.getName();
        boolean isInstalledInExternal = (mInstallAppLocations
                .remove(apkFileName)).booleanValue();
        if (isInstalledInExternal)
            installFlags |= PackageManager.INSTALL_EXTERNAL;
        else
            installFlags |= PackageManager.INSTALL_INTERNAL;

        mPM.installPackage(packageUri, observer, installFlags, packageName);
    }

    private class PackageInstallObserver extends IPackageInstallObserver.Stub {
        @Override
        public void packageInstalled(String packageName, int returnCode) {
            IwdsLog.d(this, " package: " + packageName
                    + " installation return code = " + returnCode);

            ResponseWithName done = new ResponseWithName(packageName,
                    RemoteDeviceManagerInfo.TYPE_INSTALL_APP, returnCode);
            mAppTransactor.send(done);
        }
    }

    private void onRequestDeleteApp(String packageName) {
        IwdsLog.d(this, " on request to delete " + packageName);

        PackageDeleteObserver observer = new PackageDeleteObserver();

        int deleteFlags = PackageManager.DELETE_ALL_USERS;
        mPM.deletePackage(packageName, observer, deleteFlags);
    }

    private class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
        @Override
        public void packageDeleted(String packageName, int returnCode) {

            IwdsLog.d(this, " package: " + packageName
                    + " deletion return code = " + returnCode);

            int returnCodeForElf = (returnCode == PackageManager.DELETE_SUCCEEDED) ? returnCode
                    : RemoteDeviceManagerInfo.toElfReturnCode(returnCode);

            ResponseWithName done = new ResponseWithName(packageName,
                    RemoteDeviceManagerInfo.TYPE_DELETE_APP, returnCodeForElf);
            mAppTransactor.send(done);
        }
    }

    private void onRequestPkgSizeInfo(String packageName) {
        IwdsLog.d(this, " on request to get " + packageName + " sizeInfo");

        PackageStatsObserver observer = new PackageStatsObserver();
        mPM.getPackageSizeInfo(packageName, observer);
    }

    private class PackageStatsObserver extends IPackageStatsObserver.Stub {

        @Override
        public void onGetStatsCompleted(PackageStats stats, boolean isSucceeded)
                throws RemoteException {

            String packageName = stats.packageName;

            IwdsLog.d(this, " package: " + packageName
                    + " get pkgSizeInfo isSucceeded?  " + isSucceeded);

            RemotePackageStats remoteStats = new RemotePackageStats(stats);
            int returnCode = isSucceeded ? RemoteDeviceManagerInfo.REQUEST_SUCCEEDED
                    : RemoteDeviceManagerInfo.REQUEST_REMOTE_FAILED;
            PkgInfoResponse response = new PkgInfoResponse(packageName,
                    returnCode);
            response.pkgStats = remoteStats;
            mAppTransactor.send(response);
        }
    }

    private void onRequestDeleteAppDataOrCache(RemoteRequest request) {
        String packageName = request.packageName;

        if (request.type == RemoteDeviceManagerInfo.TYPE_CLEAR_APP_USER_DATA) {
            IwdsLog.d(this, " on request to clear user data of " + packageName);

            ClearDataObserver observer = new ClearDataObserver();
            mAM.clearApplicationUserData(packageName, observer);
        } else {
            IwdsLog.d(this, " on request to clear cache of " + packageName);

            ClearCacheObserver observer = new ClearCacheObserver();
            mPM.deleteApplicationCacheFiles(packageName, observer);
        }
    }

    private class ClearDataObserver extends IPackageDataObserver.Stub {

        @Override
        public void onRemoveCompleted(String packageName, boolean isSucceeded)
                throws RemoteException {

            int returnCode = isSucceeded ? RemoteDeviceManagerInfo.REQUEST_SUCCEEDED
                    : RemoteDeviceManagerInfo.REQUEST_REMOTE_FAILED;
            ResponseWithName response = new ResponseWithName(packageName,
                    RemoteDeviceManagerInfo.TYPE_CLEAR_APP_USER_DATA,
                    returnCode);
            sendClearAppDataBroadcast(packageName);
            mAppTransactor.send(response);
        }

    }

    private void sendClearAppDataBroadcast(String packageName) {
        packageName = packageName.replace(".", "_").toUpperCase(Locale.getDefault());
        String action = String.format(CLEAR_APP_DATA_ACTION_TEMPLATE, packageName);
        Intent intent = new Intent(action);
        mContext.sendBroadcast(intent);
    }

    private class ClearCacheObserver extends IPackageDataObserver.Stub {

        @Override
        public void onRemoveCompleted(String packageName, boolean isSucceeded)
                throws RemoteException {

            int returnCode = isSucceeded ? RemoteDeviceManagerInfo.REQUEST_SUCCEEDED
                    : RemoteDeviceManagerInfo.REQUEST_REMOTE_FAILED;
            ResponseWithName response = new ResponseWithName(packageName,
                    RemoteDeviceManagerInfo.TYPE_CLEAR_APP_CACHE, returnCode);
            mAppTransactor.send(response);
        }
    }

    private void onRequestDeleteAllAppDataAndCache() {
        List<PackageInfo> allPkgList = mPM
                .getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);

        List<PackageInfo> clearPkgList = new ArrayList<PackageInfo>();
        for (PackageInfo pkgInfo : allPkgList) {
            String packageName = pkgInfo.packageName;
            if (!packageName.contains("com.ingenic"))
                continue;
            if (packageName.equals("com.ingenic.iwds.device") ||
                    packageName.equals("com.ingenic.launcher"))
                continue;

            clearPkgList.add(pkgInfo);
        }

        ClearAllAppDataCacheObserver observer = new ClearAllAppDataCacheObserver(
                clearPkgList);

        for (PackageInfo pkgInfo : clearPkgList) {
            String packageName = pkgInfo.packageName;
            // The requests of clearing cache and user data will post to
            // the PackageManager's handler thread, so the observer will be
            // called in order.
            mPM.deleteApplicationCacheFiles(packageName, observer);
            mAM.clearApplicationUserData(packageName, observer);
        }
    }

    private class ClearAllAppDataCacheObserver extends
            IPackageDataObserver.Stub {
        private int mTotalClearCount;
        private int mIndex = 0;

        public ClearAllAppDataCacheObserver(List<PackageInfo> clearPkgList) {
            // each package will be cleared twice, one for clearing data and
            // one for clearing cache
            mTotalClearCount = clearPkgList.size() * 2;
        }

        @Override
        public void onRemoveCompleted(String packageName, boolean isSucceeded)
                throws RemoteException {
            int returnCode = isSucceeded ? RemoteDeviceManagerInfo.REQUEST_SUCCEEDED
                    : RemoteDeviceManagerInfo.REQUEST_REMOTE_FAILED;
            mIndex++;

            int type;
            // the observer will be called in the order of requests
            if ((mIndex & 1) == 1) {
                type = RemoteDeviceManagerInfo.TYPE_CLEAR_APP_USER_DATA;
            } else {
                type = RemoteDeviceManagerInfo.TYPE_CLEAR_APP_CACHE;
            }

            ClearAllAppDataCacheResponse response = new ClearAllAppDataCacheResponse(
                    mTotalClearCount, mIndex, packageName, type, returnCode);
            if (type == RemoteDeviceManagerInfo.TYPE_CLEAR_APP_USER_DATA) {
                sendClearAppDataBroadcast(packageName);
            }
            mAppTransactor.send(response);
        }
    }

    private void onRequestSystemMemoryInfo() {
        MemoryInfo mi = new MemoryInfo();
        mAM.getMemoryInfo(mi);
        SysMemResponse response = new SysMemResponse(mi.availMem, mi.totalMem);

        mAppTransactor.send(response);
    }

    private void onRequestRunningAppProcessInfo() {
        RemoteProcessInfoList processInfoList = new RemoteProcessInfoList();
        List<ActivityManager.RunningAppProcessInfo> appProcessList = mAM
                .getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessList) {
            int pid = appProcessInfo.pid;
            int uid = appProcessInfo.uid;
            String processName = appProcessInfo.processName;
            int[] pids = new int[] { pid };
            Debug.MemoryInfo[] memoryInfo = mAM.getProcessMemoryInfo(pids);
            int memSize = memoryInfo[0].getTotalPss();

            IwdsLog.d(this, "processName: " + processName + "  pid: " + pid
                    + " uid:" + uid + " memorySize is -->" + memSize + "kB");

            RemoteProcessInfo processInfo = new RemoteProcessInfo(processName,
                    pid, uid, memSize);
            processInfoList.add(processInfo);

            String[] packageList = appProcessInfo.pkgList;
            IwdsLog.d(this, "process id is " + pid + " has "
                    + packageList.length);
            for (String pkg : packageList) {
                IwdsLog.d(this, " packageName " + pkg + " in process id is -->"
                        + pid);
            }
        }

        ProcessInfoResponse response = new ProcessInfoResponse(processInfoList);
        mAppTransactor.send(response);
    }

    private void onRequestKillProcess(String packageName) {
        IwdsLog.d(this, " on request to kill " + packageName);

        mAM.forceStopPackage(packageName);

        ResponseWithName response = new ResponseWithName(packageName,
                RemoteDeviceManagerInfo.TYPE_KILL_PROCESS,
                RemoteDeviceManagerInfo.REQUEST_SUCCEEDED);
        mAppTransactor.send(response);
    }

    private void onRequestDoSetting(int subType, int value) {
        switch (subType) {
        case RemoteDeviceManagerInfo.TYPE_SETTING_WEAR_ON_WHICH_HAND:
            boolean isOnRightHand = (value == RemoteDeviceManagerInfo.VALUE_WEAR_ON_RIGHT_HAND);
            mDM.setWearOnRightHand(isOnRightHand);
            break;

        default:
            break;
        }

        SettingResponse response = new SettingResponse(
                RemoteDeviceManagerInfo.TYPE_DO_SETTING, subType, value);
        mAppTransactor.send(response);
    }

    private void onRequestGetSetting(int subType) {
        int returnCode = RemoteDeviceManagerInfo.REQUEST_REMOTE_FAILED;

        switch (subType) {
        case RemoteDeviceManagerInfo.TYPE_SETTING_WEAR_ON_WHICH_HAND:
            boolean isOnRightHand = mDM.isWearOnRightHand();
            returnCode = isOnRightHand ? RemoteDeviceManagerInfo.VALUE_WEAR_ON_RIGHT_HAND
                    : RemoteDeviceManagerInfo.VALUE_WEAR_ON_LEFT_HAND;
            break;

        default:
            break;
        }

        SettingResponse response = new SettingResponse(
                RemoteDeviceManagerInfo.TYPE_GET_SETTING, subType, returnCode);
        mAppTransactor.send(response);
    }

    private void notifyDMServieConnected(boolean isConnected) {
        int type = isConnected ? RemoteDeviceManagerInfo.TYPE_DM_SERVICE_CONNECTED
                : RemoteDeviceManagerInfo.TYPE_DM_SERVICE_DISCONNECTED;

        RemoteResponse response = new RemoteResponse(type);
        mAppTransactor.send(response);
    }

    private ConnectionCallbacks mDMConnectionCallbacks = new ConnectionCallbacks() {
        @Override
        public void onConnected(ServiceClient serviceClient) {
            mDM = (DeviceManagerServiceManager) mDMServiceClient
                    .getServiceManagerContext();

            notifyDMServieConnected(true);
        }

        @Override
        public void onDisconnected(ServiceClient serviceClient,
                boolean unexpected) {
            IwdsLog.i(this, "device management service disconnected");

            notifyDMServieConnected(false);
        }

        @Override
        public void onConnectFailed(ServiceClient serviceClient,
                ConnectFailedReason reason) {
            IwdsLog.e(this, "Failed to connect to device management service: "
                    + reason.toString());
        }
    };

    private DataTransactorCallback mAppTransactorCallback = new DataTransactorCallback() {
        @Override
        public void onLinkConnected(DeviceDescriptor descriptor,
                boolean isConnected) {
            // Don't care
        }

        @Override
        public void onChannelAvailable(boolean isAvailable) {
            if (isAvailable) {
                mDMServiceClient.connect();
            } else {
                mDMServiceClient.disconnect();
            }
        }

        @Override
        public void onSendResult(DataTransactResult result) {
            if (result.getResultCode() != DataTransactResult.RESULT_OK) {
                IwdsLog.e(this,
                        "send failed, error code " + result.getResultCode());
            }
        }

        @Override
        public void onDataArrived(Object object) {
            if (object instanceof File) {
                onApkFileReceived((File) object);
                return;
            }

            RemoteRequest request = (RemoteRequest) object;
            String packageName;

            switch (request.type) {
            case RemoteDeviceManagerInfo.TYPE_GET_APP_LIST:
                onRequestGetAppList();
                break;

            case RemoteDeviceManagerInfo.TYPE_GET_STORAGE_INFO:
                onRequestGetStorageInfo();
                break;

            case RemoteDeviceManagerInfo.TYPE_INSTALL_APP:
                onRequestInstallApp(request);
                break;

            case RemoteDeviceManagerInfo.TYPE_DELETE_APP:
                packageName = request.packageName;
                onRequestDeleteApp(packageName);
                break;

            case RemoteDeviceManagerInfo.TYPE_PKG_SIZE_INFO:
                packageName = request.packageName;
                onRequestPkgSizeInfo(packageName);
                break;

            case RemoteDeviceManagerInfo.TYPE_CLEAR_APP_CACHE:
            case RemoteDeviceManagerInfo.TYPE_CLEAR_APP_USER_DATA:
                onRequestDeleteAppDataOrCache(request);
                break;

            case RemoteDeviceManagerInfo.TYPE_CLEAR_ALL_APP_DATA_CACHE:
                onRequestDeleteAllAppDataAndCache();
                break;

            case RemoteDeviceManagerInfo.TYPE_GET_SYSTEM_MEM_INFO:
                onRequestSystemMemoryInfo();
                break;

            case RemoteDeviceManagerInfo.TYPE_GET_RUNNING_PROCESS_INFO:
                onRequestRunningAppProcessInfo();
                break;

            case RemoteDeviceManagerInfo.TYPE_KILL_PROCESS:
                packageName = request.packageName;
                onRequestKillProcess(packageName);
                break;

            case RemoteDeviceManagerInfo.TYPE_DO_SETTING:
                onRequestDoSetting(request.subType, request.value);
                break;

            case RemoteDeviceManagerInfo.TYPE_GET_SETTING:
                onRequestGetSetting(request.subType);
                break;

            default:
                break;
            }
        }

        @Override
        public void onSendFileProgress(int progress) {
            // needn't it
        }

        @Override
        public void onRecvFileProgress(int progress) {
            // ignore it
        }

        @Override
        public void onSendFileInterrupted(int index) {

        }

        @Override
        public void onRecvFileInterrupted(int index) {

        }
    };
}
