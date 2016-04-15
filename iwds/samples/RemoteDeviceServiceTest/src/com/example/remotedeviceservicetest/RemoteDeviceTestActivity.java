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

package com.example.remotedeviceservicetest;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageStats;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceClient.ConnectionCallbacks;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.remotedevice.RemoteApplicationInfo;
import com.ingenic.iwds.remotedevice.RemoteDeviceAppListener;
import com.ingenic.iwds.remotedevice.RemoteDeviceManagerInfo;
import com.ingenic.iwds.remotedevice.RemoteDeviceProcessListener;
import com.ingenic.iwds.remotedevice.RemoteDeviceServiceManager;
import com.ingenic.iwds.remotedevice.RemoteDeviceSettingListener;
import com.ingenic.iwds.remotedevice.RemoteDeviceStatusListener;
import com.ingenic.iwds.remotedevice.RemoteProcessInfo;
import com.ingenic.iwds.remotedevice.RemoteStorageInfo;
import com.ingenic.iwds.utils.IwdsLog;

public class RemoteDeviceTestActivity extends Activity implements
        ConnectionCallbacks {
    private final static String TAG = "IWDS---RemoteDeviceTestActivity";

    private ServiceClient mClient;
    private RemoteDeviceServiceManager mService;

    private TextView mLogView;
    private TextView mProgress;
    private Button mGetListButton;
    private Button mInstallButton;
    private Button mDeleteButton;
    private Button mPkgSizeInfoButton;
    private Button mClearAppDataButton;
    private Button mClearAppCacheButton;
    private Button mGetSysMemButton;
    private Button mGetProcessesButton;
    private Button mKillProcessButton;
    private Button mClearAllAppDataCacheButton;
    private Button mGetStorageInfoButton;
    private Button mGetWearHandButton;
    private Button mSetWearRightButton;
    private Button mSetWearLeftButton;
    private Button mClearLogButton;
    private List<Button> mButtons = new ArrayList<Button>();

    private ImageView mImageView;

    List<RemoteApplicationInfo> mAppInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_device);

        mLogView = (TextView) findViewById(R.id.app_text);
        mLogView.setMovementMethod(ScrollingMovementMethod.getInstance());

        mProgress = (TextView) findViewById(R.id.progress_text);
        mGetListButton = (Button) findViewById(R.id.get_button);
        mInstallButton = (Button) findViewById(R.id.install_button);
        mDeleteButton = (Button) findViewById(R.id.delete_button);
        mPkgSizeInfoButton = (Button) findViewById(R.id.pkg_size_button);
        mClearAppDataButton = (Button) findViewById(R.id.clear_data_button);
        mClearAppCacheButton = (Button) findViewById(R.id.clear_cache_button);
        mGetSysMemButton = (Button) findViewById(R.id.get_mem_button);
        mGetProcessesButton = (Button) findViewById(R.id.get_processes_button);
        mKillProcessButton = (Button) findViewById(R.id.kill_process_button);
        mClearAllAppDataCacheButton = (Button) findViewById(R.id.del_all_data_button);
        mGetStorageInfoButton = (Button) findViewById(R.id.get_storage_button);
        mSetWearRightButton = (Button) findViewById(R.id.set_right_hand_button);
        mSetWearLeftButton = (Button) findViewById(R.id.set_left_hand_button);
        mGetWearHandButton = (Button) findViewById(R.id.get_hand_button);
        mClearLogButton = (Button) findViewById(R.id.clear_log_button);
        Button[] buttons = { mGetListButton, mInstallButton, mDeleteButton,
                mPkgSizeInfoButton, mClearAppDataButton, mClearAppCacheButton,
                mGetSysMemButton, mGetProcessesButton, mKillProcessButton,
                mClearAllAppDataCacheButton, mGetStorageInfoButton,
                mSetWearRightButton, mSetWearLeftButton, mGetWearHandButton,
                mClearLogButton };
        for (Button button : buttons) {
            mButtons.add(button);
            button.setEnabled(false);
            button.setOnClickListener(mListener);
        }

        mImageView = (ImageView) findViewById(R.id.icon_image);

        mClient = new ServiceClient(this,
                ServiceManagerContext.SERVICE_REMOTE_DEVICE, this);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");

        super.onResume();

        mClient.connect();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");

        super.onPause();

        // Unregistering listener should be before disconnecting service
        mService.unregisterStatusListener();
        mService.unregisterAppListener();
        mService.unregisterProcessListener();
        mService.unregisterSettingListener();

        mClient.disconnect();
    }

    private OnClickListener mListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
            case R.id.get_button: {
                mService.requestGetAppList();
                break;
            }

            case R.id.get_storage_button:
                mService.requestGetStorageInfo();
                break;

            case R.id.install_button:
                // String filePath = "/sdcard/FileTransactionTest.apk";
                String filePath = "/storage/sdcard1/FileTransactionTest.apk";
                mService.requestInstallApp(filePath, false);
                break;

            case R.id.delete_button: {
                String packageName = "com.example.filetransactiontest";
                mService.requestDeleteApp(packageName);
                break;
            }

            case R.id.pkg_size_button: {
                String packageName = "com.example.filetransactiontest";
                mService.requestPkgSizeInfo(packageName);
                break;
            }

            case R.id.clear_data_button: {
                String packageName = "com.example.filetransactiontest";
                mService.requestClearAppDataOrCache(packageName,
                        RemoteDeviceManagerInfo.TYPE_CLEAR_APP_USER_DATA);
                break;
            }

            case R.id.clear_cache_button: {
                String packageName = "com.example.filetransactiontest";
                mService.requestClearAppDataOrCache(packageName,
                        RemoteDeviceManagerInfo.TYPE_CLEAR_APP_CACHE);
                break;
            }

            case R.id.get_mem_button:
                mService.requestSystemMemoryInfo();
                break;

            case R.id.get_processes_button:
                mService.requestRunningAppProcessInfo();
                break;

            case R.id.kill_process_button: {
                String packageName = "com.example.filetransactiontest";
                mService.requestKillProcess(packageName);
                break;
            }

            case R.id.del_all_data_button:
                mService.requestClearAllAppDataAndCache();
                break;

            case R.id.get_hand_button:
                mService.requestGetSetting(RemoteDeviceManagerInfo.TYPE_SETTING_WEAR_ON_WHICH_HAND);
                break;

            case R.id.set_right_hand_button:
                mService.requestDoSetting(
                        RemoteDeviceManagerInfo.TYPE_SETTING_WEAR_ON_WHICH_HAND,
                        RemoteDeviceManagerInfo.VALUE_WEAR_ON_RIGHT_HAND);
                break;

            case R.id.set_left_hand_button:
                mService.requestDoSetting(
                        RemoteDeviceManagerInfo.TYPE_SETTING_WEAR_ON_WHICH_HAND,
                        RemoteDeviceManagerInfo.VALUE_WEAR_ON_LEFT_HAND);
                break;

            case R.id.clear_log_button:
                IwdsLog.d(this, "clear text");
                mLogView.setText("");
                break;

            default:
                break;
            }
        }
    };

    @Override
    public void onConnected(ServiceClient serviceClient) {
        Log.d(TAG, "Remote device manager service connected");
        mService = (RemoteDeviceServiceManager) serviceClient
                .getServiceManagerContext();
        mService.registerStatusListener(mStatusListener);
        mService.registerAppListener(mAppListener);
        mService.registerProcessListener(mProcessListener);
        mService.registerSettingListener(mSettingListener);
    }

    @Override
    public void onDisconnected(ServiceClient serviceClient, boolean unexpected) {
        Log.d(TAG, "Remote device manager service disconnected");
    }

    @Override
    public void onConnectFailed(ServiceClient serviceClient,
            ConnectFailedReason reason) {
        Log.d(TAG, "Remote device manager service connect fail");
    }

    private RemoteDeviceStatusListener mStatusListener = new RemoteDeviceStatusListener() {
        @Override
        public void onRemoteDeviceReady(boolean isReady) {
            Log.d(TAG, " on remote device ready? " + isReady);

            for (Button button : mButtons) {
                button.setEnabled(isReady);
            }

            String text;
            if (!isReady) {
                text = "Remote device unavailable";
            } else {
                text = "Remote device available";
            }

            Log.d(TAG, text);
            Toast.makeText(RemoteDeviceTestActivity.this, text,
                    Toast.LENGTH_SHORT).show();
        }
    };

    private RemoteDeviceAppListener mAppListener = new RemoteDeviceAppListener() {
        @Override
        public void onRemoteAppInfoListAvailable(
                List<RemoteApplicationInfo> appInfoList) {
            Log.d(TAG, " app list received.");

            mAppInfoList = appInfoList;
            int appCount = 0;
            for (RemoteApplicationInfo appInfo : appInfoList) {
                if ((appInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    appCount++;
                    mLogView.append("app name: " + appInfo.packageName
                            + " version " + appInfo.versionName + " "
                            + appInfo.versionCode + " label: " + appInfo.label
                            + "\n");

                    Drawable bmpDraw = bitmap2Drawable(appInfo.iconBitmap);
                    mImageView.setImageDrawable(bmpDraw);
                }
            }

            if (appCount == 0)
                mLogView.append(" no-system app list is null.\n");
        }

        @Override
        public void onRemoteStorageInfoAvailable(RemoteStorageInfo storageInfo) {
            mLogView.append("\n Storage info: internal total "
                    + storageInfo.totalInternalSize + " avail "
                    + storageInfo.availInternalSize + " has external?"
                    + storageInfo.hasExternalStorage);

            if (storageInfo.hasExternalStorage) {
                mLogView.append(" external total "
                        + storageInfo.totalExternalSize + " avail "
                        + storageInfo.availExternalSize);
            }
        }

        public void onDoneInstallApp(String packageName, int returnCode) {
            Log.d(TAG, packageName + " installation done, returnCode: "
                    + returnCode);

            if (returnCode == RemoteDeviceManagerInfo.INSTALL_SUCCEEDED) {
                mLogView.append("install " + packageName + " succeeded!\n");
            } else {
                mLogView.append("install " + packageName + " failed: "
                        + errorString(returnCode) + "\n");
            }
        }

        @Override
        public void onDoneDeleteApp(String packageName, int returnCode) {
            Log.d(TAG, packageName + " deletion done, returnCode: "
                    + returnCode);

            if (returnCode == 1) {
                mLogView.append("delete " + packageName + " succeeded!\n");
            } else {
                mLogView.append("delete " + packageName + " failed: "
                        + errorString(returnCode) + "\n");
            }
        }

        @Override
        public void onResponsePkgSizeInfo(PackageStats stats, int returnCode) {
            Log.d(TAG, "get size info of " + stats.packageName + " returnCode "
                    + returnCode);

            if (returnCode == RemoteDeviceManagerInfo.REQUEST_SUCCEEDED) {
                mLogView.append("code size " + stats.codeSize + " cache size "
                        + stats.cacheSize + " data size " + stats.dataSize
                        + "\n");
            } else {
                mLogView.append("get size info failed: "
                        + errorString(returnCode) + "\n");
            }
        }

        @Override
        public void onResponseClearAppDataOrCache(String packageName,
                int requestType, int returnCode) {

            String operation = (requestType == RemoteDeviceManagerInfo.TYPE_CLEAR_APP_CACHE) ? "clear cache"
                    : "clear data";
            if (returnCode == RemoteDeviceManagerInfo.REQUEST_SUCCEEDED) {
                mLogView.append(packageName + " " + operation + " succeeded.\n");
            } else {
                mLogView.append("\n" + packageName + " " + operation
                        + " failed: " + errorString(returnCode) + "\n");
            }
        }

        @Override
        public void onResponseClearAllAppDataAndCache(int totalClearCount,
                int index, String packageName, int typeOfIndex, int returnCode) {
            mProgress.setText("clear " + index + "/" + totalClearCount
                    + typeToString(typeOfIndex) + " for " + packageName);
        }

        @Override
        public void onSendFileProgressForInstall(String packageName,
                int progress) {
            mProgress.setText(" Sending file progress " + progress
                    + "% for installing " + packageName);
        }
    };

    private RemoteDeviceSettingListener mSettingListener = new RemoteDeviceSettingListener() {

        @Override
        public void onDoneSetting(int type, int returnCode) {
            if (returnCode == RemoteDeviceManagerInfo.REQUEST_FAILED_SERVICE_DISCONNECTED) {
                mLogView.append("setting failed: " + errorString(returnCode)
                        + "\n");

                return;
            }

            switch (type) {
            case RemoteDeviceManagerInfo.TYPE_SETTING_WEAR_ON_WHICH_HAND:
                boolean isOnRightHand = (returnCode == RemoteDeviceManagerInfo.VALUE_WEAR_ON_RIGHT_HAND);
                mLogView.append("set wearing on "
                        + (isOnRightHand ? "right" : "left") + " hand ok\n");
                break;

            default:
                break;
            }
        }

        @Override
        public void onGetSetting(int type, int returnCode) {
            if (returnCode == RemoteDeviceManagerInfo.REQUEST_FAILED_SERVICE_DISCONNECTED) {
                mLogView.append("get setting failed: "
                        + errorString(returnCode) + "\n");

                return;
            }

            switch (type) {
            case RemoteDeviceManagerInfo.TYPE_SETTING_WEAR_ON_WHICH_HAND:
                boolean isOnRightHand = (returnCode == RemoteDeviceManagerInfo.VALUE_WEAR_ON_RIGHT_HAND);
                mLogView.append("remote device is wearing on "
                        + (isOnRightHand ? "right" : "left") + " hand\n");
                break;

            default:
                break;
            }
        }
    };

    private String typeToString(int typeOfIndex) {
        if (typeOfIndex == RemoteDeviceManagerInfo.TYPE_CLEAR_APP_CACHE) {
            return new String(" clear cache");
        } else {
            return new String(" clear  data");
        }
    }

    private Drawable bitmap2Drawable(Bitmap bitmap) {
        BitmapDrawable bd = new BitmapDrawable(getResources(), bitmap);

        return (Drawable) bd;
    }

    private RemoteDeviceProcessListener mProcessListener = new RemoteDeviceProcessListener() {

        @Override
        public void onResponseSystemMemoryInfo(long availMemSize,
                long totalMemSize) {
            mLogView.append("available Mem = " + availMemSize + " total Mem = "
                    + totalMemSize + "\n");
        }

        @Override
        public void onResponseRunningAppProcessInfo(
                List<RemoteProcessInfo> processInfoList) {
            IwdsLog.d(this, "processInfoList size: " + processInfoList.size());

            for (RemoteProcessInfo info : processInfoList) {
                mLogView.append("process name: " + info.processName + " pid: "
                        + info.pid + " uid: " + info.uid + " mem: "
                        + info.memSize + "\n");
            }
        }

        @Override
        public void onDoneKillProcess(String packageName) {
            mLogView.append(packageName + " be killed.\n");
        }
    };

    public String errorString(int errorCode) {
        switch (errorCode) {
        case RemoteDeviceManagerInfo.REQUEST_REMOTE_FAILED:
            return this.getString(R.string.request_remote_failed);

        case RemoteDeviceManagerInfo.REQUEST_FAILED_PREVIOUS_DOING:
            return this.getString(R.string.request_failed_previous_doing);

        case RemoteDeviceManagerInfo.INSTALL_FAILED_ALREADY_EXISTS:
            return this.getString(R.string.install_failed_already_exists);

        case RemoteDeviceManagerInfo.INSTALL_FAILED_INVALID_URI:
            return this.getString(R.string.install_failed_invalid_uri);

        case RemoteDeviceManagerInfo.INSTALL_FAILED_INSUFFICIENT_STORAGE:
            return this.getString(R.string.install_failed_insufficient_storage);

        case RemoteDeviceManagerInfo.INSTALL_FAILED_INVALID_APK:
            return this.getString(R.string.install_failed_invalid_apk);

        case RemoteDeviceManagerInfo.INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES:
            return this
                    .getString(R.string.install_failed_inconsistent_certificates);

        case RemoteDeviceManagerInfo.INSTALL_FAILED_OLDER_SDK:
            return this.getString(R.string.install_failed_older_sdk);

        case RemoteDeviceManagerInfo.INSTALL_FAILED_CPU_ABI_INCOMPATIBLE:
            return this.getString(R.string.install_failed_cpu_abi_incompatible);

        case RemoteDeviceManagerInfo.INSTALL_FAILED_VERSION_DOWNGRADE:
            return this.getString(R.string.install_failed_version_downgrade);

        case RemoteDeviceManagerInfo.INSTALL_FAILED_SEND_APK_FILE_ERROR:
            return this.getString(R.string.install_failed_send_apk_file_error);

        case RemoteDeviceManagerInfo.DELETE_FAILED_INTERNAL_ERROR:
            return this.getString(R.string.delete_failed_internal_error);

        case RemoteDeviceManagerInfo.DELETE_FAILED_DEVICE_POLICY_MANAGER:
            return this.getString(R.string.delete_failed_device_policy_manager);

        case RemoteDeviceManagerInfo.DELETE_FAILED_USER_RESTRICTED:
            return this.getString(R.string.delete_failed_user_restricted);

        case RemoteDeviceManagerInfo.REQUEST_FAILED_SERVICE_DISCONNECTED:
            return this.getString(R.string.request_failed_service_disconnected);
        }

        return null;
    }
}
