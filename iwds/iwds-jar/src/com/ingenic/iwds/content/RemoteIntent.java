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

package com.ingenic.iwds.content;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.ingenic.iwds.os.RemoteBundle;
import com.ingenic.iwds.os.SafeParcel;
import com.ingenic.iwds.os.SafeParcelable;
import com.ingenic.iwds.utils.IwdsLog;
import com.ingenic.iwds.utils.IwdsUtils;

/**
 * 远程{@link Intent}，设备之间通信传输{@link Intent}
 * 数据时使用。主要针对与远程广播设计，复制了Intent中的以下数据：action、type、flags、package、categories和extras。其他数据是跨进程调用时需要使用的数据，
 * 对端设备获取该数据后没有相应的处理，且可能出现异常。
 * 
 * <p>
 * 其中，extras数据（{@link Bundle}类型）将被转换为{@link RemoteBundle}类型进行设备之间的数据传输。
 */
public class RemoteIntent implements Parcelable, SafeParcelable {

    private String mAction;
    private Uri mData;
    private String mType;
    private int mFlags;
    private String mPackage;
    private HashSet<String> mCategories;
    private RemoteBundle mExtras;

    private RemoteIntent(RemoteIntent in) {
        mAction = in.mAction;
        mData = in.mData;
        mType = in.mType;
        mFlags = in.mFlags;
        mPackage = in.mPackage;

        final HashSet<String> hs = in.mCategories;
        if (hs != null) {
            mCategories = new HashSet<String>(hs);
        }

        final RemoteBundle rb = in.mExtras;
        if (rb != null) {
            mExtras = rb.clone();
        }
    }

    private RemoteIntent(Intent intent) {
        copyFromIntentInner(intent);
    }

    private RemoteIntent(Parcel in) {
        readFromParcel(in);
    }

    private RemoteIntent(SafeParcel in) {
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in) {
        mAction = in.readString();
        mData = Uri.CREATOR.createFromParcel(in);
        mType = in.readString();
        mFlags = in.readInt();
        mPackage = in.readString();

        int categoryCounts = in.readInt();
        if (categoryCounts > 0) {
            mCategories = new HashSet<String>();
            for (int i = 0; i < categoryCounts; i++) {
                mCategories.add(in.readString().intern());
            }
        } else {
            mCategories = null;
        }

        if (in.readInt() != 0) {
            mExtras = RemoteBundle.CREATOR.createFromParcel(in);
        }
    }

    private void readFromParcel(SafeParcel in) {
        mAction = in.readString();
        mData = IwdsUtils.createParcelableFromSafeParcel(in, getClass().getClassLoader());
        mType = in.readString();
        mFlags = in.readInt();
        mPackage = in.readString();

        final int categoryCounts = in.readInt();
        if (categoryCounts > 0) {
            mCategories = new HashSet<String>(categoryCounts);

            for (int i = 0; i < categoryCounts; i++) {
                mCategories.add(in.readString().intern());
            }
        } else {
            mCategories = null;
        }

        if (in.readInt() != 0) {
            mExtras = RemoteBundle.CREATOR.createFromParcel(in);
        }
    }

    private void copyFromIntentInner(Intent intent) {
        mAction = intent.getAction();
        mData = intent.getData();
        mType = intent.getType();
        mFlags = intent.getFlags();
        mPackage = intent.getPackage();

        final Set<String> categories = intent.getCategories();
        if (categories != null) {
            mCategories = new HashSet<String>(categories);
        }

        final Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mExtras = RemoteBundle.fromBunble(bundle);
        }
    }

    public String getAction() {
        return mAction;
    }

    public Uri getData() {
        return mData;
    }

    public String getDataString() {
        return mData != null ? mData.toString() : null;
    }

    public String getScheme() {
        return mData != null ? mData.getScheme() : null;
    }

    public String getType() {
        return mType;
    }

    public boolean hasCategory(String category) {
        return mCategories != null && mCategories.contains(category);
    }

    public HashSet<String> getCategories() {
        return mCategories;
    }

    public void setExtrasClassLoader(ClassLoader loader) {
        if (mExtras != null) {
            mExtras.setClassLoader(loader);
        }
    }

    public boolean hasExtra(String name) {
        return mExtras != null && mExtras.containsKey(name);
    }

    public boolean getBooleanExtra(String name, boolean defaultValue) {
        return mExtras == null ? defaultValue : mExtras.getBoolean(name, defaultValue);
    }

    public byte getByteExtra(String name, byte defaultValue) {
        return mExtras == null ? defaultValue : mExtras.getByte(name, defaultValue);
    }

    public short getShortExtra(String name, short defaultValue) {
        return mExtras == null ? defaultValue : mExtras.getShort(name, defaultValue);
    }

    public char getCharExtra(String name, char defaultValue) {
        return mExtras == null ? defaultValue : mExtras.getChar(name, defaultValue);
    }

    public int getIntExtra(String name, int defaultValue) {
        return mExtras == null ? defaultValue : mExtras.getInt(name, defaultValue);
    }

    public long getLongExtra(String name, long defaultValue) {
        return mExtras == null ? defaultValue : mExtras.getLong(name, defaultValue);
    }

    public float getFloatExtra(String name, float defaultValue) {
        return mExtras == null ? defaultValue : mExtras.getFloat(name, defaultValue);
    }

    public double getDoubleExtra(String name, double defaultValue) {
        return mExtras == null ? defaultValue : mExtras.getDouble(name, defaultValue);
    }

    public String getStringExtra(String name) {
        return mExtras == null ? null : mExtras.getString(name);
    }

    public CharSequence getCharSequenceExtra(String name) {
        return mExtras == null ? null : mExtras.getCharSequence(name);
    }

    public <T extends Parcelable> T getParcelableExtra(String name) {
        return mExtras == null ? null : mExtras.<T> getParcelable(name);
    }

    public Parcelable[] getParcelableArrayExtra(String name) {
        return mExtras == null ? null : mExtras.getParcelableArray(name);
    }

    public <T extends Parcelable> ArrayList<T> getParcelableArrayListExtra(String name) {
        return mExtras == null ? null : mExtras.<T> getParcelableArrayList(name);
    }

    public Serializable getSerializableExtra(String name) {
        return mExtras == null ? null : mExtras.getSerializable(name);
    }

    public ArrayList<Integer> getIntegerArrayListExtra(String name) {
        return mExtras == null ? null : mExtras.getIntegerArrayList(name);
    }

    public ArrayList<String> getStringArrayListExtra(String name) {
        return mExtras == null ? null : mExtras.getStringArrayList(name);
    }

    public ArrayList<CharSequence> getCharSequenceArrayListExtra(String name) {
        return mExtras == null ? null : mExtras.getCharSequenceArrayList(name);
    }

    public boolean[] getBooleanArrayExtra(String name) {
        return mExtras == null ? null : mExtras.getBooleanArray(name);
    }

    public byte[] getByteArrayExtra(String name) {
        return mExtras == null ? null : mExtras.getByteArray(name);
    }

    public short[] getShortArrayExtra(String name) {
        return mExtras == null ? null : mExtras.getShortArray(name);
    }

    public char[] getCharArrayExtra(String name) {
        return mExtras == null ? null : mExtras.getCharArray(name);
    }

    public int[] getIntArrayExtra(String name) {
        return mExtras == null ? null : mExtras.getIntArray(name);
    }

    public long[] getLongArrayExtra(String name) {
        return mExtras == null ? null : mExtras.getLongArray(name);
    }

    public float[] getFloatArrayExtra(String name) {
        return mExtras == null ? null : mExtras.getFloatArray(name);
    }

    public double[] getDoubleArrayExtra(String name) {
        return mExtras == null ? null : mExtras.getDoubleArray(name);
    }

    public String[] getStringArrayExtra(String name) {
        return mExtras == null ? null : mExtras.getStringArray(name);
    }

    public CharSequence[] getCharSequenceArrayExtra(String name) {
        return mExtras == null ? null : mExtras.getCharSequenceArray(name);
    }

    public Bundle getBundleExtra(String name) {
        return mExtras == null ? null : mExtras.getBundle(name);
    }

    public Bundle getExtras() {
        return (mExtras != null) ? mExtras.toBundle() : null;
    }

    public int getFlags() {
        return mFlags;
    }

    public boolean isExcludingStopped() {
        return (mFlags & (Intent.FLAG_EXCLUDE_STOPPED_PACKAGES | Intent.FLAG_INCLUDE_STOPPED_PACKAGES)) == Intent.FLAG_EXCLUDE_STOPPED_PACKAGES;
    }

    public String getPackage() {
        return mPackage;
    }

    public RemoteIntent setAction(String action) {
        mAction = action != null ? action.intern() : null;
        return this;
    }

    public RemoteIntent setData(Uri data) {
        mData = data;
        mType = null;
        return this;
    }

    public RemoteIntent setDataAndNormalize(Uri data) {
        return setData(data.normalizeScheme());
    }

    public RemoteIntent setType(String type) {
        mData = null;
        mType = type;
        return this;
    }

    public RemoteIntent setTypeAndNormalize(String type) {
        return setType(Intent.normalizeMimeType(type));
    }

    public RemoteIntent setDataAndType(Uri data, String type) {
        mData = data;
        mType = type;
        return this;
    }

    public RemoteIntent setDataAndTypeAndNormalize(Uri data, String type) {
        return setDataAndType(data.normalizeScheme(), Intent.normalizeMimeType(type));
    }

    public RemoteIntent addCategory(String category) {
        if (mCategories == null) {
            mCategories = new HashSet<String>();
        }
        mCategories.add(category.intern());
        return this;
    }

    public void removeCategory(String category) {
        if (mCategories != null) {
            mCategories.remove(category);
            if (mCategories.size() == 0) {
                mCategories = null;
            }
        }
    }

    private void initExtrasIfNotExists() {
        if (mExtras == null) {
            mExtras = RemoteBundle.fromBunble(new Bundle());
        }
    }

    public RemoteIntent putExtra(String name, boolean value) {
        initExtrasIfNotExists();
        mExtras.putBoolean(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, byte value) {
        initExtrasIfNotExists();
        mExtras.putByte(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, char value) {
        initExtrasIfNotExists();
        mExtras.putChar(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, short value) {
        initExtrasIfNotExists();
        mExtras.putShort(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, int value) {
        initExtrasIfNotExists();
        mExtras.putInt(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, long value) {
        initExtrasIfNotExists();
        mExtras.putLong(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, float value) {
        initExtrasIfNotExists();
        mExtras.putFloat(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, double value) {
        initExtrasIfNotExists();
        mExtras.putDouble(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, String value) {
        initExtrasIfNotExists();
        mExtras.putString(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, CharSequence value) {
        initExtrasIfNotExists();
        mExtras.putCharSequence(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, Parcelable value) {
        initExtrasIfNotExists();
        mExtras.putParcelable(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, Parcelable[] value) {
        initExtrasIfNotExists();
        mExtras.putParcelableArray(name, value);
        return this;
    }

    public RemoteIntent putParcelableArrayListExtra(String name,
            ArrayList<? extends Parcelable> value) {
        initExtrasIfNotExists();
        mExtras.putParcelableArrayList(name, value);
        return this;
    }

    public RemoteIntent putIntegerArrayListExtra(String name, ArrayList<Integer> value) {
        initExtrasIfNotExists();
        mExtras.putIntegerArrayList(name, value);
        return this;
    }

    public RemoteIntent putStringArrayListExtra(String name, ArrayList<String> value) {
        initExtrasIfNotExists();
        mExtras.putStringArrayList(name, value);
        return this;
    }

    public RemoteIntent putCharSequenceArrayListExtra(String name, ArrayList<CharSequence> value) {
        initExtrasIfNotExists();
        mExtras.putCharSequenceArrayList(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, Serializable value) {
        initExtrasIfNotExists();
        mExtras.putSerializable(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, boolean[] value) {
        initExtrasIfNotExists();
        mExtras.putBooleanArray(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, byte[] value) {
        initExtrasIfNotExists();
        mExtras.putByteArray(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, short[] value) {
        initExtrasIfNotExists();
        mExtras.putShortArray(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, char[] value) {
        initExtrasIfNotExists();
        mExtras.putCharArray(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, int[] value) {
        initExtrasIfNotExists();
        mExtras.putIntArray(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, long[] value) {
        initExtrasIfNotExists();
        mExtras.putLongArray(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, float[] value) {
        initExtrasIfNotExists();
        mExtras.putFloatArray(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, double[] value) {
        initExtrasIfNotExists();
        mExtras.putDoubleArray(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, String[] value) {
        initExtrasIfNotExists();
        mExtras.putStringArray(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, CharSequence[] value) {
        initExtrasIfNotExists();
        mExtras.putCharSequenceArray(name, value);
        return this;
    }

    public RemoteIntent putExtra(String name, Bundle value) {
        initExtrasIfNotExists();
        mExtras.putBundle(name, value);
        return this;
    }

    public RemoteIntent putExtras(Intent src) {
        if (src == null) return this;

        RemoteIntent ri = RemoteIntent.fromIntent(src);
        if (ri.mExtras != null) {
            if (mExtras == null) {
                mExtras = ri.mExtras.clone();
            } else {
                mExtras.putAll(ri.mExtras);
            }
        }
        return this;
    }

    public RemoteIntent putExtras(Bundle extras) {
        initExtrasIfNotExists();
        mExtras.putAll(extras);
        return this;
    }

    public RemoteIntent replaceExtras(Intent src) {
        if (src == null) {
            mExtras = null;
            return this;
        }

        RemoteIntent ri = RemoteIntent.fromIntent(src);
        mExtras = ri.mExtras != null ? ri.mExtras.clone() : null;
        return this;
    }

    public RemoteIntent replaceExtras(Bundle extras) {
        mExtras = RemoteBundle.fromBunble(extras);
        return this;
    }

    public void removeExtra(String name) {
        if (mExtras != null) {
            mExtras.remove(name);
            if (mExtras.size() == 0) {
                mExtras = null;
            }
        }
    }

    public RemoteIntent setFlags(int flags) {
        mFlags = flags;
        return this;
    }

    public RemoteIntent addFlags(int flags) {
        mFlags |= flags;
        return this;
    }

    public RemoteIntent setPackage(String packageName) {
        mPackage = packageName;
        return this;
    }

    public int fillIn(Intent other, int flags) {
        if (other == null) return 0;

        final RemoteIntent ri = RemoteIntent.fromIntent(other);
        int changes = 0;
        if (ri.mAction != null && (mAction == null || (flags & Intent.FILL_IN_ACTION) != 0)) {
            mAction = ri.mAction;
            changes |= Intent.FILL_IN_ACTION;
        }

        if ((ri.mData != null || ri.mType != null)
                && ((mData == null && mType == null) || (flags & Intent.FILL_IN_DATA) != 0)) {
            mData = ri.mData;
            mType = ri.mType;
            changes |= Intent.FILL_IN_DATA;
        }

        if (ri.mCategories != null
                && (mCategories == null || (flags & Intent.FILL_IN_CATEGORIES) != 0)) {
            if (ri.mCategories != null) {
                mCategories = new HashSet<String>(ri.mCategories);
            }
            changes |= Intent.FILL_IN_CATEGORIES;
        }

        if (ri.mPackage != null && (mPackage == null || (flags & Intent.FILL_IN_PACKAGE) != 0)) {
            mPackage = ri.mPackage;
            changes |= Intent.FILL_IN_PACKAGE;
        }

        mFlags |= ri.mFlags;

        if (mExtras == null) {
            if (ri.mExtras != null) {
                mExtras = ri.mExtras.clone();
            }
        } else if (ri.mExtras != null) {
            try {
                RemoteBundle newRb = ri.mExtras.clone();
                newRb.putAll(mExtras);
                mExtras = newRb;
            } catch (RuntimeException e) {
                // Modifying the extras can cause us to unparcel the contents
                // of the bundle, and if we do this in the system process that
                // may fail. We really should handle this (i.e., the Bundle
                // impl shouldn't be on top of a plain map), but for now just
                // ignore it and keep the original contents. :(
                IwdsLog.w(this, "Failure filling in extras", e);
            }
        }
        return changes;
    }

    public boolean filterEquals(Intent other) {
        if (other == null) return false;

        final RemoteIntent ri = RemoteIntent.fromIntent(other);
        if (mAction != ri.mAction) {
            if (mAction != null) {
                if (!mAction.equals(ri.mAction)) {
                    return false;
                }
            } else {
                if (!ri.mAction.equals(mAction)) {
                    return false;
                }
            }
        }

        if (mData != ri.mData) {
            if (mData != null) {
                if (!mData.equals(ri.mData)) {
                    return false;
                }
            } else {
                if (!ri.mData.equals(mData)) {
                    return false;
                }
            }
        }

        if (mType != ri.mType) {
            if (mType != null) {
                if (!mType.equals(ri.mType)) {
                    return false;
                }
            } else {
                if (!ri.mType.equals(mType)) {
                    return false;
                }
            }
        }

        if (mPackage != ri.mPackage) {
            if (mPackage != null) {
                if (!mPackage.equals(ri.mPackage)) {
                    return false;
                }
            } else {
                if (!ri.mPackage.equals(mPackage)) {
                    return false;
                }
            }
        }

        if (mCategories != ri.mCategories) {
            if (mCategories != null) {
                if (!mCategories.equals(ri.mCategories)) {
                    return false;
                }
            } else {
                if (!ri.mCategories.equals(mCategories)) {
                    return false;
                }
            }
        }

        return true;
    }

    public int filterHashCode() {
        int code = 0;
        if (mAction != null) {
            code += mAction.hashCode();
        }

        if (mData != null) {
            code += mData.hashCode();
        }

        if (mType != null) {
            code += mType.hashCode();
        }

        if (mPackage != null) {
            code += mPackage.hashCode();
        }

        if (mCategories != null) {
            code += mCategories.hashCode();
        }

        return code;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(128);

        b.append("Intent { ");
        toShortString(b, true, true);
        b.append(" }");

        return b.toString();
    }

    private void toShortString(StringBuilder b, boolean secure, boolean extras) {
        boolean first = true;

        if (mAction != null) {
            b.append("act=").append(mAction);
            first = false;
        }

        if (mCategories != null) {
            if (!first) {
                b.append(' ');
            }

            first = false;
            b.append("cat=[");
            for (String category : mCategories) {
                b.append(category);
            }
            b.append("]");
        }

        if (mData != null) {
            if (!first) {
                b.append(' ');
            }

            first = false;
            b.append("dat=");
            if (secure) {
                b.append(IwdsUtils.getUriSafeString(mData));
                // b.append(mData.toSafeString());
            } else {
                b.append(mData);
            }
        }

        if (mType != null) {
            if (!first) {
                b.append(' ');
            }

            first = false;
            b.append("typ=").append(mType);
        }

        if (mFlags != 0) {
            if (!first) {
                b.append(' ');
            }

            first = false;
            b.append("flg=0x").append(Integer.toHexString(mFlags));
        }

        if (mPackage != null) {
            if (!first) {
                b.append(' ');
            }

            first = false;
            b.append("pkg=").append(mPackage);
        }

        if (extras && mExtras != null) {
            if (!first) {
                b.append(' ');
            }

            first = false;
            b.append("(has extras)");
        }
    }

    public String toUri(int flags) {
        final StringBuilder uri = new StringBuilder(128);
        String scheme = null;

        if (mData != null) {
            String data = mData.toString();

            if ((flags & Intent.URI_INTENT_SCHEME) != 0) {
                final int N = data.length();

                for (int i = 0; i < N; i++) {
                    char c = data.charAt(i);

                    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '.' || c == '-') {
                        continue;
                    }

                    if (c == ':' && i > 0) {
                        // Valid scheme.
                        scheme = data.substring(0, i);
                        uri.append("intent:");
                        data = data.substring(i + 1);
                        break;
                    }

                    // No scheme.
                    break;
                }
            }

            uri.append(data);
        } else if ((flags & Intent.URI_INTENT_SCHEME) != 0) {
            uri.append("intent:");
        }

        uri.append("#Intent;");
        toUriInner(uri, scheme, flags);
        uri.append("end");

        return uri.toString();
    }

    private void toUriInner(StringBuilder uri, String scheme, int flags) {
        if (scheme != null) {
            uri.append("scheme=").append(scheme).append(';');
        }

        if (mAction != null) {
            uri.append("action=").append(Uri.encode(mAction)).append(';');
        }

        if (mCategories != null) {
            for (String category : mCategories) {
                uri.append("category=").append(Uri.encode(category)).append(';');
            }
        }

        if (mType != null) {
            uri.append("type=").append(Uri.encode(mType, "/")).append(';');
        }

        if (mFlags != 0) {
            uri.append("launchFlags=0x").append(Integer.toHexString(mFlags)).append(';');
        }

        if (mPackage != null) {
            uri.append("package=").append(Uri.encode(mPackage)).append(';');
        }

        if (mExtras != null) {
            for (String key : mExtras.keySet()) {
                final Object value = mExtras.get(key);
                char entryType = value instanceof String ? 'S' : value instanceof Boolean ? 'B'
                        : value instanceof Byte ? 'b' : value instanceof Character ? 'c'
                                : value instanceof Double ? 'd' : value instanceof Float ? 'f'
                                        : value instanceof Integer ? 'i' : value instanceof Long
                                                ? 'l' : value instanceof Short ? 's' : '\0';

                if (entryType != '\0') {
                    uri.append(entryType);
                    uri.append('.');
                    uri.append(Uri.encode(key));
                    uri.append('=');
                    uri.append(Uri.encode(value.toString()));
                    uri.append(';');
                }
            }
        }
    }

    @Override
    public int describeContents() {
        return mExtras != null ? mExtras.describeContents() : 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mAction);
        Uri.writeToParcel(dest, mData);
        dest.writeString(mType);
        dest.writeInt(mFlags);
        dest.writeString(mPackage);

        final HashSet<String> categories = mCategories;
        final int N = categories != null ? categories.size() : 0;
        dest.writeInt(N);
        if (N > 0) {
            for (String category : categories) {
                dest.writeString(category);
            }
        }

        if (mExtras != null) {
            dest.writeInt(1);
            mExtras.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
    }

    @Override
    public RemoteIntent clone() {
        return new RemoteIntent(this);
    }

    public static final Creator CREATOR = new Creator();

    @Override
    public void writeToParcel(SafeParcel dest, int flags) {
        dest.writeString(mAction);
        IwdsUtils.writeParcelablleToSafeParcel(mData, dest, flags);
        dest.writeString(mType);
        dest.writeInt(mFlags);
        dest.writeString(mPackage);

        final HashSet<String> categories = mCategories;
        final int N = categories != null ? categories.size() : 0;
        dest.writeInt(N);
        if (N > 0) {
            for (String category : categories) {
                dest.writeString(category);
            }
        }

        if (mExtras != null) {
            dest.writeInt(1);
            mExtras.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
    }

    /**
     * 把{@link Intent}转换为{@link RemoteIntent}
     * 
     * @param intent 数据源{@link Intent}
     * @return 转换结果{@link RemoteIntent}
     */
    public static RemoteIntent fromIntent(Intent intent) {
        if (intent == null) {
            return null;
        }
        return new RemoteIntent(intent);
    }

    /**
     * 转换为{@link Intent}
     * 
     * @return 转换结果{@link Intent}
     */
    public Intent toIntent() {
        Intent intent = new Intent();
        copyToIntentInner(intent);
        return intent;
    }

    private void copyToIntentInner(Intent intent) {
        intent.setAction(mAction);
        intent.setDataAndType(mData, mType);
        intent.setFlags(mFlags);
        intent.setPackage(mPackage);

        if (mCategories != null) {
            for (String category : mCategories) {
                intent.addCategory(category);
            }
        }

        if (mExtras != null) {
            intent.putExtras(mExtras.toBundle());
        }
    }

    /**
     * 把{@link RemoteIntent}数组转换为{@link Intent}数组
     * 
     * @param array {@link RemoteIntent}数组
     * @return 转换得到的{@link Intent}数组
     */
    public static Intent[] arrayToLocal(RemoteIntent[] array) {
        final int N = array.length;
        final Intent[] result = new Intent[N];

        for (int i = 0; i < N; i++) {
            result[i] = array[i].toIntent();
        }
        return result;
    }

    /**
     * 把{@link Intent}数组转换为{@link RemoteIntent}数组
     * 
     * @param array {@link Intent}数组
     * @return 转换得到的{@link RemoteIntent}数组
     */
    public static RemoteIntent[] arrayToRemote(Intent[] array) {
        final int N = array.length;
        final RemoteIntent[] result = new RemoteIntent[N];

        for (int i = 0; i < N; i++) {
            result[i] = fromIntent(array[i]);
        }
        return result;
    }

    public static final class Creator implements Parcelable.Creator<RemoteIntent>,
            SafeParcelable.Creator<RemoteIntent> {

        @Override
        public RemoteIntent createFromParcel(SafeParcel source) {
            return new RemoteIntent(source);
        }

        @Override
        public RemoteIntent createFromParcel(Parcel source) {
            return new RemoteIntent(source);
        }

        @Override
        public RemoteIntent[] newArray(int size) {
            return new RemoteIntent[size];
        }
    }
}