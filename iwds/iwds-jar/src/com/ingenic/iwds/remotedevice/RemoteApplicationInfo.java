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

import java.io.ByteArrayOutputStream;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * 远程设备应用信息类。
 *
 */
public class RemoteApplicationInfo implements Parcelable {
    /**
     * Default task affinity of all activities in this application. See
     * {@link ActivityInfo#taskAffinity} for more information. This comes from
     * the "taskAffinity" attribute.
     */
    private String taskAffinity;

    /**
     * Optional name of a permission required to be able to access this
     * application's components. From the "permission" attribute.
     */
    private String permission;

    /**
     * The name of the process this application should run in. From the
     * "process" attribute or, if not set, the same as <var>packageName</var>.
     */
    private String processName;

    /**
     * Class implementing the Application object. From the "class" attribute.
     */
    private String className;

    /**
     * A style resource identifier (in the package's resources) of the
     * description of an application. From the "description" attribute or, if
     * not set, 0.
     */
    private int descriptionRes;

    /**
     * A style resource identifier (in the package's resources) of the default
     * visual theme of the application. From the "theme" attribute or, if not
     * set, 0.
     */
    private int theme;

    /**
     * Class implementing the Application's manage space functionality. From the
     * "manageSpaceActivity" attribute. This is an optional attribute and will
     * be null if applications don't specify it in their manifest
     */
    private String manageSpaceActivityName;

    /**
     * Class implementing the Application's backup functionality. From the
     * "backupAgent" attribute. This is an optional attribute and will be null
     * if the application does not specify it in its manifest.
     * 
     * <p>
     * If android:allowBackup is set to false, this attribute is ignored.
     */
    private String backupAgentName;

    /**
     * The default extra UI options for activities in this application. Set from
     * the {@link android.R.attr#uiOptions} attribute in the activity's
     * manifest.
     */
    private int uiOptions = 0;

    /**
     * Flags associated with the application. Any combination of
     * {@link #FLAG_SYSTEM}, {@link #FLAG_DEBUGGABLE}, {@link #FLAG_HAS_CODE},
     * {@link #FLAG_PERSISTENT}, {@link #FLAG_FACTORY_TEST}, and
     * {@link #FLAG_ALLOW_TASK_REPARENTING} {@link #FLAG_ALLOW_CLEAR_USER_DATA},
     * {@link #FLAG_UPDATED_SYSTEM_APP} , {@link #FLAG_TEST_ONLY},
     * {@link #FLAG_SUPPORTS_SMALL_SCREENS},
     * {@link #FLAG_SUPPORTS_NORMAL_SCREENS},
     * {@link #FLAG_SUPPORTS_LARGE_SCREENS},
     * {@link #FLAG_SUPPORTS_XLARGE_SCREENS},
     * {@link #FLAG_RESIZEABLE_FOR_SCREENS},
     * {@link #FLAG_SUPPORTS_SCREEN_DENSITIES}, {@link #FLAG_VM_SAFE_MODE},
     * {@link #FLAG_INSTALLED}.
     */
    private int flags = 0;

    /**
     * The required smallest screen width the application can run on. If 0,
     * nothing has been specified. Comes from
     * {@link android.R.styleable#AndroidManifestSupportsScreens_requiresSmallestWidthDp
     * android:requiresSmallestWidthDp} attribute of the
     * &lt;supports-screens&gt; tag.
     */
    private int requiresSmallestWidthDp = 0;

    /**
     * The maximum smallest screen width the application is designed for. If 0,
     * nothing has been specified. Comes from
     * {@link android.R.styleable#AndroidManifestSupportsScreens_compatibleWidthLimitDp
     * android:compatibleWidthLimitDp} attribute of the &lt;supports-screens&gt;
     * tag.
     */
    private int compatibleWidthLimitDp = 0;

    /**
     * The maximum smallest screen width the application will work on. If 0,
     * nothing has been specified. Comes from
     * {@link android.R.styleable#AndroidManifestSupportsScreens_largestWidthLimitDp
     * android:largestWidthLimitDp} attribute of the &lt;supports-screens&gt;
     * tag.
     */
    private int largestWidthLimitDp = 0;

    /**
     * Full path to the location of this package.
     */
    private String sourceDir;

    /**
     * Full path to the location of the publicly available parts of this package
     * (i.e. the primary resource package and manifest). For non-forward-locked
     * apps this will be the same as {@link #sourceDir).
     */
    private String publicSourceDir;

    /**
     * Full path to a directory assigned to the package for its persistent data.
     */
    private String dataDir;

    /**
     * Full path to the directory where native JNI libraries are stored.
     */
    private String nativeLibraryDir;

    /**
     * The kernel user-ID that has been assigned to this application; currently
     * this is not a unique ID (multiple applications can have the same uid).
     */
    private int uid;

    /**
     * The minimum SDK version this application targets. It may run on earlier
     * versions, but it knows how to work with any new behavior added at this
     * version. Will be {@link android.os.Build.VERSION_CODES#CUR_DEVELOPMENT}
     * if this is a development build and the app is targeting that. You should
     * compare that this number is >= the SDK version number at which your
     * behavior was introduced.
     */
    private int targetSdkVersion;

    /**
     * When false, indicates that all components within this application are
     * considered disabled, regardless of their individually set enabled status.
     */
    private boolean enabled = true;

    /**
     * 存放应用图标内容的二进制数组
     */
    private byte[] iconData;

    /**
     * 图标位图
     */
    public Bitmap iconBitmap;

    /**
     * 应用标签
     */
    public CharSequence label;

    // ----- below information is from PackageInfo
    /**
     * 应用的包名
     */
    public String packageName;

    /**
     * 应用的版本号，由安装包的AndroidManifest.xml中的versionCode指定。
     */
    public int versionCode;

    /**
     * 应用的版本名称，由安装包的AndroidManifest.xml中的versionName指定。
     */
    public String versionName;

    /**
     * The shared user ID name of this package, as specified by the
     * &lt;manifest&gt; tag's
     * {@link android.R.styleable#AndroidManifest_sharedUserId sharedUserId}
     * attribute.
     */
    private String sharedUserId;

    /**
     * The shared user ID label of this package, as specified by the
     * &lt;manifest&gt; tag's
     * {@link android.R.styleable#AndroidManifest_sharedUserLabel
     * sharedUserLabel} attribute.
     */
    private int sharedUserLabel;

    /**
     * 与Android原生API android.content.pm.ApplicationInfo 兼容的应用信息
     */
    public ApplicationInfo applicationInfo;

    /**
     * The time at which the app was first installed. Units are as per
     * {@link System#currentTimeMillis()}.
     */
    private long firstInstallTime;

    /**
     * The time at which the app was last updated. Units are as per
     * {@link System#currentTimeMillis()}.
     */
    private long lastUpdateTime;

    RemoteApplicationInfo(PackageManager pm, PackageInfo orig) {
        taskAffinity = orig.applicationInfo.taskAffinity;
        permission = orig.applicationInfo.permission;
        processName = orig.applicationInfo.processName;
        className = orig.applicationInfo.className;
        theme = orig.applicationInfo.theme;
        flags = orig.applicationInfo.flags;
        requiresSmallestWidthDp = orig.applicationInfo.requiresSmallestWidthDp;
        compatibleWidthLimitDp = orig.applicationInfo.compatibleWidthLimitDp;
        largestWidthLimitDp = orig.applicationInfo.largestWidthLimitDp;
        sourceDir = orig.applicationInfo.sourceDir;
        publicSourceDir = orig.applicationInfo.publicSourceDir;
        nativeLibraryDir = orig.applicationInfo.nativeLibraryDir;
        dataDir = orig.applicationInfo.dataDir;
        uid = orig.applicationInfo.uid;
        targetSdkVersion = orig.applicationInfo.targetSdkVersion;
        enabled = orig.applicationInfo.enabled;
        manageSpaceActivityName = orig.applicationInfo.manageSpaceActivityName;
        descriptionRes = orig.applicationInfo.descriptionRes;
        uiOptions = orig.applicationInfo.uiOptions;
        backupAgentName = orig.applicationInfo.backupAgentName;

        Drawable iconDrawable = pm.getApplicationIcon(orig.applicationInfo);
        Bitmap icon = drawable2Bitmap(iconDrawable);
        iconData = Bitmap2Bytes(icon);

        label = orig.applicationInfo.loadLabel(pm);
        packageName = orig.packageName;
        versionCode = orig.versionCode;
        versionName = orig.versionName;
        sharedUserId = orig.sharedUserId;
        sharedUserLabel = orig.sharedUserLabel;
        firstInstallTime = orig.firstInstallTime;
        lastUpdateTime = orig.lastUpdateTime;
    }

    private Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap
                .createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        return baos.toByteArray();
    }

    @Override
    public String toString() {
        return "RemoteApplicationInfo{"
                + Integer.toHexString(System.identityHashCode(this)) + " "
                + packageName + "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeString(taskAffinity);
        dest.writeString(permission);
        dest.writeString(processName);
        dest.writeString(className);
        dest.writeInt(theme);
        dest.writeInt(flags);
        dest.writeInt(requiresSmallestWidthDp);
        dest.writeInt(compatibleWidthLimitDp);
        dest.writeInt(largestWidthLimitDp);
        dest.writeString(sourceDir);
        dest.writeString(publicSourceDir);
        dest.writeString(nativeLibraryDir);
        dest.writeString(dataDir);
        dest.writeInt(uid);
        dest.writeInt(targetSdkVersion);
        dest.writeInt(enabled ? 1 : 0);
        dest.writeString(manageSpaceActivityName);
        dest.writeString(backupAgentName);
        dest.writeInt(descriptionRes);
        dest.writeInt(uiOptions);

        dest.writeInt(iconData.length);
        dest.writeByteArray(iconData);
        TextUtils.writeToParcel(label, dest, parcelableFlags);
        dest.writeString(packageName);
        dest.writeInt(versionCode);
        dest.writeString(versionName);
        dest.writeString(sharedUserId);
        dest.writeInt(sharedUserLabel);
        dest.writeLong(firstInstallTime);
        dest.writeLong(lastUpdateTime);
    }

    public static final Parcelable.Creator<RemoteApplicationInfo> CREATOR = new Parcelable.Creator<RemoteApplicationInfo>() {
        @Override
        public RemoteApplicationInfo createFromParcel(Parcel source) {
            return new RemoteApplicationInfo(source);
        }

        @Override
        public RemoteApplicationInfo[] newArray(int size) {
            return new RemoteApplicationInfo[size];
        }
    };

    private RemoteApplicationInfo(Parcel source) {
        applicationInfo = new ApplicationInfo();

        applicationInfo.taskAffinity = taskAffinity = source.readString();
        applicationInfo.permission = permission = source.readString();
        applicationInfo.processName = processName = source.readString();
        applicationInfo.className = className = source.readString();
        applicationInfo.theme = theme = source.readInt();
        applicationInfo.flags = flags = source.readInt();
        applicationInfo.requiresSmallestWidthDp = requiresSmallestWidthDp = source
                .readInt();
        applicationInfo.compatibleWidthLimitDp = compatibleWidthLimitDp = source
                .readInt();
        applicationInfo.largestWidthLimitDp = largestWidthLimitDp = source
                .readInt();
        applicationInfo.sourceDir = sourceDir = source.readString();
        applicationInfo.publicSourceDir = publicSourceDir = source.readString();
        applicationInfo.nativeLibraryDir = nativeLibraryDir = source
                .readString();
        applicationInfo.dataDir = dataDir = source.readString();
        applicationInfo.uid = uid = source.readInt();
        applicationInfo.targetSdkVersion = targetSdkVersion = source.readInt();
        applicationInfo.enabled = enabled = source.readInt() != 0;
        applicationInfo.manageSpaceActivityName = manageSpaceActivityName = source
                .readString();
        applicationInfo.backupAgentName = backupAgentName = source.readString();
        applicationInfo.descriptionRes = descriptionRes = source.readInt();
        applicationInfo.uiOptions = uiOptions = source.readInt();

        int iconSize = source.readInt();
        iconData = new byte[iconSize];
        source.readByteArray(iconData);
        iconBitmap = bytes2Bitmap(iconData);

        label = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
        packageName = source.readString();
        versionCode = source.readInt();
        versionName = source.readString();
        sharedUserId = source.readString();
        sharedUserLabel = source.readInt();
        firstInstallTime = source.readLong();
        lastUpdateTime = source.readLong();
    }

    public ApplicationInfo getLocalApplicationInfo() {
        return applicationInfo;
    }

    private Bitmap bytes2Bitmap(byte[] b) {
        if (b.length == 0)
            return null;

        return BitmapFactory.decodeByteArray(b, 0, b.length);
    }
}
