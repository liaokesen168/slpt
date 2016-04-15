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

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 远程设备管理信息类。内含请求的类型和返回值的定义。
 */
public class RemoteDeviceManagerInfo {

    /**
     * 请求成功时的通用返回值
     */
    public static final int REQUEST_SUCCEEDED = 0;

    /**
     * 远程设备确认可以安装apk的返回值，仅限远程设备管理服务内部使用
     */
    static final int REQUEST_INSTALL_CONFIRM_OK = 1;

    /**
     * 因远程设备内部错误而请求失败的通用返回值
     */
    public static final int REQUEST_REMOTE_FAILED = -119;

    /**
     * 因前一个请求还未完成导致请求失败的返回值
     */
    public static final int REQUEST_FAILED_PREVIOUS_DOING = -200;
    
    /**
     * 因远程设备管理服务没有连接成功而导致请求失败的返回值
     */
    public static final int REQUEST_FAILED_SERVICE_DISCONNECTED = -201;

    // ------ Constants below are copied from PackageManager.java for
    // parsing returnCode of installing app

    /**
     * 安装返回值：当远程设备安装成功时返回
     */
    public static final int INSTALL_SUCCEEDED = 1;

    /**
     * 安装返回值：当远程设备已经存在该应用导致安装失败时返回
     */
    public static final int INSTALL_FAILED_ALREADY_EXISTS = -1;

    /**
     * 安装返回值：当远程设备解析安装包失败时返回
     */
    public static final int INSTALL_FAILED_INVALID_APK = -2;

    /**
     * 安装返回值：当本地设备指定的路径找不到apk文件时返回
     */
    public static final int INSTALL_FAILED_INVALID_URI = -3;

    /**
     * 安装返回值：当远程设备存储空间不够时返回
     */
    public static final int INSTALL_FAILED_INSUFFICIENT_STORAGE = -4;

    /**
     * 安装返回值：当安装包要求的SDK版本高于远程设备的SDK版本时返回
     */
    public static final int INSTALL_FAILED_OLDER_SDK = -12;

    /**
     * 安装返回值： 安装包内含有本地码而且和远程设备不兼容时返回
     */
    public static final int INSTALL_FAILED_CPU_ABI_INCOMPATIBLE = -16;

    /**
     * 安装返回值：安装包的版本比现有版本更低
     */
    public static final int INSTALL_FAILED_VERSION_DOWNGRADE = -25;

    /**
     * 安装返回值： 当安装包不是apk文件时返回
     */
    public static final int INSTALL_PARSE_FAILED_NOT_APK = -100;

    /**
     * 安装返回值： 当安装包的签名信息冲突时返回
     */
    public static final int INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES = -104;

    /**
     * 安装返回值： 当安装包文件向远程设备发送失败时返回
     */
    public static final int INSTALL_FAILED_SEND_APK_FILE_ERROR = -105;

    // ------ Constants below are copied from PackageManager.java for
    // parsing returnCode of deleting app

    /**
     * 卸载返回值：当远程设备卸载应用成功时返回
     */
    public static final int DELETE_SUCCEEDED = 1;

    /**
     * 卸载返回值：当远程设备卸载发生内部错误时返回
     */
    public static final int DELETE_FAILED_INTERNAL_ERROR = -111;

    /**
     * 卸载返回值：当该应用在远程设备中是一个活动的设备管理器而导致卸载失败时返回
     */
    public static final int DELETE_FAILED_DEVICE_POLICY_MANAGER = -112;

    /**
     * 卸载返回值： 当卸载权限不够时返回
     */
    public static final int DELETE_FAILED_USER_RESTRICTED = -113;

    // Package manager related requests
    /**
     * 应用请求类型：清除应用的用户数据
     */
    public static final int TYPE_CLEAR_APP_USER_DATA = 0;

    /**
     * 应用请求类型：清除应用的缓存
     */
    public static final int TYPE_CLEAR_APP_CACHE = 1;

    /**
     * 设置请求类型: 远程设备佩戴于哪只手
     */
    public static final int TYPE_SETTING_WEAR_ON_WHICH_HAND = 0;

    /**
     * 设置值: 远程设备佩戴于右手
     */
    public static final int VALUE_WEAR_ON_RIGHT_HAND = 1;

    /**
     * 设置值: 远程设备佩戴于左手
     */
    public static final int VALUE_WEAR_ON_LEFT_HAND = 2;

    // below are used internally

    static final int TYPE_CLEAR_ALL_APP_DATA_CACHE = 2;

    static final int TYPE_GET_APP_LIST = 3;

    static final int TYPE_GET_STORAGE_INFO = 4;

    static final int TYPE_INSTALL_APP = 5;

    static final int TYPE_CONFIRM_INSTALL_APP = 6;

    static final int TYPE_DELETE_APP = 7;

    static final int TYPE_PKG_SIZE_INFO = 8;

    // Activity manager related requests
    static final int TYPE_GET_SYSTEM_MEM_INFO = 9;

    static final int TYPE_GET_RUNNING_PROCESS_INFO = 10;

    static final int TYPE_KILL_PROCESS = 11;

    static final int TYPE_DO_SETTING = 12;

    static final int TYPE_GET_SETTING = 13;

    static final int TYPE_DM_SERVICE_CONNECTED = 20;

    static final int TYPE_DM_SERVICE_DISCONNECTED = 21;

    static class RemoteRequest implements Serializable {

        private static final long serialVersionUID = -87257129103004422L;

        public String packageName;
        public int type;

        public int subType;
        public int value;
        public String apkFilePath;
        public boolean isInstalledInExternal;
        public long requiredSize;

        public RemoteRequest(int type) {
            this.type = type;
        }

        public RemoteRequest(int type, int subType) {
            this.type = type;
            this.subType = subType;
        }
        
        public RemoteRequest(int type, int subType, int value) {
            this.type = type;
            this.subType = subType;
            this.value = value;
        }

        public RemoteRequest(String packageName, int type) {
            this.packageName = packageName;
            this.type = type;
        }

        public RemoteRequest(String packageName, int type, String apkFilePath,
                long requiredSize, boolean isInstalledInExternal) {
            this(packageName, type);
            this.apkFilePath = apkFilePath;
            this.requiredSize = requiredSize;
            this.isInstalledInExternal = isInstalledInExternal;
        }
    }

    static class RemoteResponse implements Parcelable {
        int type;
        int returnCode;

        public RemoteResponse(int type) {
            this.type = type;
        }

        public RemoteResponse(int type, int returnCode) {
            this.type = type;
            this.returnCode = returnCode;
        }

        public RemoteResponse(Parcel source) {
            readFromParcel(source);
        }

        public static final Parcelable.Creator<RemoteResponse> CREATOR = new Parcelable.Creator<RemoteResponse>() {
            @Override
            public RemoteResponse createFromParcel(Parcel source) {
                return new RemoteResponse(source);
            }

            @Override
            public RemoteResponse[] newArray(int size) {
                return new RemoteResponse[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(type);
            dest.writeInt(returnCode);
        }

        public void readFromParcel(Parcel source) {
            type = source.readInt();
            returnCode = source.readInt();
        }
    }

    static class StorageInfoResponse extends RemoteResponse {
        RemoteStorageInfo storageInfo;

        public StorageInfoResponse(RemoteStorageInfo storageInfo) {
            super(TYPE_GET_STORAGE_INFO, REQUEST_SUCCEEDED);
            this.storageInfo = storageInfo;
        }

        public StorageInfoResponse(Parcel source) {
            super(source);
        }

        public static final Parcelable.Creator<StorageInfoResponse> CREATOR = new Parcelable.Creator<StorageInfoResponse>() {
            @Override
            public StorageInfoResponse createFromParcel(Parcel source) {
                return new StorageInfoResponse(source);
            }

            @Override
            public StorageInfoResponse[] newArray(int size) {
                return new StorageInfoResponse[size];
            }
        };

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeParcelable(storageInfo, flags);
        }

        public void readFromParcel(Parcel source) {
            super.readFromParcel(source);
            storageInfo = source.readParcelable(StorageInfoResponse.class
                    .getClassLoader());
        }

    }

    static class PkgInfoResponse extends RemoteResponse {
        String packageName;
        RemotePackageStats pkgStats;

        public PkgInfoResponse(String packageName, int returnCode) {
            super(TYPE_PKG_SIZE_INFO, returnCode);
            this.packageName = packageName;
        }

        public PkgInfoResponse(Parcel source) {
            super(source);
        }

        public static final Parcelable.Creator<PkgInfoResponse> CREATOR = new Parcelable.Creator<PkgInfoResponse>() {
            @Override
            public PkgInfoResponse createFromParcel(Parcel source) {
                return new PkgInfoResponse(source);
            }

            @Override
            public PkgInfoResponse[] newArray(int size) {
                return new PkgInfoResponse[size];
            }
        };

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(packageName);
            dest.writeParcelable(pkgStats, flags);
        }

        public void readFromParcel(Parcel source) {
            super.readFromParcel(source);
            packageName = source.readString();
            pkgStats = source.readParcelable(PkgInfoResponse.class
                    .getClassLoader());
        }
    }

    static class AppListResponse extends RemoteResponse {
        RemoteApplicationInfoList appList;

        public AppListResponse(RemoteApplicationInfoList appList) {
            super(TYPE_GET_APP_LIST, REQUEST_SUCCEEDED);
            this.appList = appList;
        }

        public AppListResponse(Parcel source) {
            super(source);
        }

        public static final Parcelable.Creator<AppListResponse> CREATOR = new Parcelable.Creator<AppListResponse>() {
            @Override
            public AppListResponse createFromParcel(Parcel source) {
                return new AppListResponse(source);
            }

            @Override
            public AppListResponse[] newArray(int size) {
                return new AppListResponse[size];
            }
        };

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeParcelable(appList, flags);
        }

        public void readFromParcel(Parcel source) {
            super.readFromParcel(source);
            appList = source.readParcelable(AppListResponse.class
                    .getClassLoader());
        }
    }

    static class ConfirmInstallResponse extends ResponseWithName {
        String apkFilePath;

        public ConfirmInstallResponse(String packageName, String apkFilePath,
                int returnCode) {
            super(packageName, TYPE_CONFIRM_INSTALL_APP, returnCode);
            this.apkFilePath = apkFilePath;
        }

        public ConfirmInstallResponse(Parcel source) {
            super(source);
        }

        public static final Parcelable.Creator<ConfirmInstallResponse> CREATOR = new Parcelable.Creator<ConfirmInstallResponse>() {
            @Override
            public ConfirmInstallResponse createFromParcel(Parcel source) {
                return new ConfirmInstallResponse(source);
            }

            @Override
            public ConfirmInstallResponse[] newArray(int size) {
                return new ConfirmInstallResponse[size];
            }
        };

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(apkFilePath);
        }

        public void readFromParcel(Parcel source) {
            super.readFromParcel(source);
            apkFilePath = source.readString();
        }
    }

    static class ResponseWithName extends RemoteResponse {
        String packageName;

        public ResponseWithName(String packageName, int type, int returnCode) {
            super(type, returnCode);
            this.packageName = packageName;
        }

        public ResponseWithName(int type, int returnCode) {
            super(type, returnCode);
        }

        public ResponseWithName(Parcel source) {
            super(source);
        }

        public static final Parcelable.Creator<ResponseWithName> CREATOR = new Parcelable.Creator<ResponseWithName>() {
            @Override
            public ResponseWithName createFromParcel(Parcel source) {
                return new ResponseWithName(source);
            }

            @Override
            public ResponseWithName[] newArray(int size) {
                return new ResponseWithName[size];
            }
        };

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(packageName);
        }

        public void readFromParcel(Parcel source) {
            super.readFromParcel(source);
            packageName = source.readString();
        }
    }

    static class ClearAllAppDataCacheResponse extends ResponseWithName {
        int totalCount;
        int index;
        int typeOfIndex;

        public ClearAllAppDataCacheResponse(int totalCount, int index,
                String packageName, int typeOfIndex, int returnCode) {
            super(packageName, TYPE_CLEAR_ALL_APP_DATA_CACHE, returnCode);
            this.totalCount = totalCount;
            this.index = index;
            this.typeOfIndex = typeOfIndex;
        }

        public ClearAllAppDataCacheResponse(int returnCode) {
            super(null, TYPE_CLEAR_ALL_APP_DATA_CACHE, returnCode);
        }

        public ClearAllAppDataCacheResponse(Parcel source) {
            super(source);
        }

        public static final Parcelable.Creator<ClearAllAppDataCacheResponse> CREATOR = new Parcelable.Creator<ClearAllAppDataCacheResponse>() {
            @Override
            public ClearAllAppDataCacheResponse createFromParcel(Parcel source) {
                return new ClearAllAppDataCacheResponse(source);
            }

            @Override
            public ClearAllAppDataCacheResponse[] newArray(int size) {
                return new ClearAllAppDataCacheResponse[size];
            }
        };

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(packageName);
            dest.writeInt(totalCount);
            dest.writeInt(index);
            dest.writeInt(typeOfIndex);
        }

        public void readFromParcel(Parcel source) {
            super.readFromParcel(source);
            packageName = source.readString();
            totalCount = source.readInt();
            index = source.readInt();
            typeOfIndex = source.readInt();
        }
    }

    static class SysMemResponse extends RemoteResponse {
        long availSysMemSize;
        long totalSysMemSize;

        public SysMemResponse(long availSysMemSize, long totalSysMemSize) {
            super(TYPE_GET_SYSTEM_MEM_INFO, REQUEST_SUCCEEDED);
            this.availSysMemSize = availSysMemSize;
            this.totalSysMemSize = totalSysMemSize;
        }

        public SysMemResponse(Parcel source) {
            super(source);
        }

        public static final Parcelable.Creator<SysMemResponse> CREATOR = new Parcelable.Creator<SysMemResponse>() {
            @Override
            public SysMemResponse createFromParcel(Parcel source) {
                return new SysMemResponse(source);
            }

            @Override
            public SysMemResponse[] newArray(int size) {
                return new SysMemResponse[size];
            }
        };

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeLong(availSysMemSize);
            dest.writeLong(totalSysMemSize);
        }

        public void readFromParcel(Parcel source) {
            super.readFromParcel(source);
            availSysMemSize = source.readLong();
            totalSysMemSize = source.readLong();
        }
    }

    static class ProcessInfoResponse extends RemoteResponse {
        RemoteProcessInfoList processList;

        public ProcessInfoResponse(RemoteProcessInfoList processList) {
            super(TYPE_GET_RUNNING_PROCESS_INFO, REQUEST_SUCCEEDED);
            this.processList = processList;
        }

        public ProcessInfoResponse(Parcel source) {
            super(source);
        }

        public static final Parcelable.Creator<ProcessInfoResponse> CREATOR = new Parcelable.Creator<ProcessInfoResponse>() {
            @Override
            public ProcessInfoResponse createFromParcel(Parcel source) {
                return new ProcessInfoResponse(source);
            }

            @Override
            public ProcessInfoResponse[] newArray(int size) {
                return new ProcessInfoResponse[size];
            }
        };

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeParcelable(processList, flags);
        }

        public void readFromParcel(Parcel source) {
            super.readFromParcel(source);
            processList = source.readParcelable(ProcessInfoResponse.class
                    .getClassLoader());
        }
    }

    static class SettingResponse extends RemoteResponse {
        int subType;

        public SettingResponse(int type, int subType, int returnCode) {
            super(type, returnCode);
            this.subType = subType;
        }

        public SettingResponse(Parcel source) {
            super(source);
        }

        public static final Parcelable.Creator<SettingResponse> CREATOR = new Parcelable.Creator<SettingResponse>() {
            @Override
            public SettingResponse createFromParcel(Parcel source) {
                return new SettingResponse(source);
            }

            @Override
            public SettingResponse[] newArray(int size) {
                return new SettingResponse[size];
            }
        };

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(subType);
        }

        public void readFromParcel(Parcel source) {
            super.readFromParcel(source);
            subType = source.readInt();
        }
    }

    static int toElfReturnCode(int returnCode) {
        // original value PackageManager.DELETE_FAILED_INTERNAL_ERROR = -1;
        int diff = DELETE_FAILED_INTERNAL_ERROR - (-1);
        return returnCode + diff;
    }
}
