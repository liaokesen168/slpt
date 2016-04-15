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
package com.ingenic.iwds.os;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import com.ingenic.iwds.content.RemoteIntent;
import com.ingenic.iwds.utils.IwdsBitmap;
import com.ingenic.iwds.utils.IwdsLog;
import com.ingenic.iwds.utils.IwdsUtils;

/**
 * 远程Bundle，用于设备之间传递{@link Bundle}数据。可以复制除了{@link IBinder}类型之外{@link Bundle}所有的数据，设备之间传输数据不支持
 * {@link IBinder}类型。
 */
public final class RemoteBundle implements Parcelable, SafeParcelable {
    private static final String TAG = "RemoteBundle";

    private static final int VAL_NULL = -1;
    private static final int VAL_STRING = 0;
    private static final int VAL_INTEGER = 1;
    private static final int VAL_MAP = 2;
    private static final int VAL_REMOTEBUNDLE = 3;
    private static final int VAL_SAFEPARCELABLE = 4;
    private static final int VAL_SHORT = 5;
    private static final int VAL_LONG = 6;
    private static final int VAL_FLOAT = 7;
    private static final int VAL_DOUBLE = 8;
    private static final int VAL_BOOLEAN = 9;
    private static final int VAL_CHARSEQUENCE = 10;
    private static final int VAL_LIST = 11;
    private static final int VAL_BYTEARRAY = 12;
    private static final int VAL_STRINGARRAY = 13;
    private static final int VAL_SAFEPARCELABLEARRAY = 14;
    private static final int VAL_OBJECTARRAY = 15;
    private static final int VAL_INTARRAY = 16;
    private static final int VAL_LONGARRAY = 17;
    private static final int VAL_BYTE = 18;
    private static final int VAL_SERIALIZABLE = 19;
    private static final int VAL_BOOLEANARRAY = 20;
    private static final int VAL_CHARSEQUENCEARRAY = 21;
    private static final int VAL_PARCELABLE = 22;
    private static final int VAL_SPARSEARRAY = 23;
    private static final int VAL_FLOATARRAY = 24;
    private static final int VAL_DOUBLEARRAY = 25;
    private static final int VAL_CHARARRAY = 26;
    private static final int VAL_PARCELABLEARRAY = 27;

    private HashMap<String, Object> mMap;
    private ClassLoader mClassLoader;

    private boolean mHasFds = false;
    private boolean mFdsKnown = true;

    private RemoteBundle(RemoteBundle in) {
        mMap = new HashMap<String, Object>(in.mMap);
        mClassLoader = in.mClassLoader;
        mHasFds = in.mHasFds;
        mFdsKnown = in.mFdsKnown;
    }

    private RemoteBundle(Bundle bundle) {
        mMap = new HashMap<String, Object>();
        copyFromBundleInner(bundle);
        mFdsKnown = false;
    }

    private RemoteBundle(Parcel in) {
        this(in, null);
    }

    private RemoteBundle(Parcel in, ClassLoader loader) {
        mMap = new HashMap<String, Object>();
        mClassLoader = loader != null ? loader : getClass().getClassLoader();
        readFromParcel(in, loader);
    }

    private RemoteBundle(SafeParcel in) {
        this(in, null);
    }

    RemoteBundle(SafeParcel in, ClassLoader loader) {
        mClassLoader = loader == null ? getClass().getClassLoader() : loader;
        readFromParcel(in, loader);
    }

    private void readFromParcel(Parcel in, ClassLoader loader) {
        in.readMap(mMap, loader);
        mHasFds = in.hasFileDescriptors();
        mFdsKnown = true;
    }

    private void readFromParcel(SafeParcel in, ClassLoader loader) {
        final int N = in.readInt();
        mMap = new HashMap<String, Object>(N);

        if (N == 0) {
            mHasFds = false;
            mFdsKnown = true;
            return;
        }

        for (int i = 0; i < N; i++) {
            mMap.put(in.readString(), readValueFromParcel(in, loader));
        }

        mFdsKnown = false;
    }

    private static Object readValueFromParcel(SafeParcel in, ClassLoader loader) {
        final int type = in.readInt();

        switch (type) {
        case VAL_NULL:
            return null;

        case VAL_BOOLEAN:
            return in.readInt() != 0;

        case VAL_BYTE:
            return in.readByte();

        case VAL_SHORT:
            return (short) in.readInt();

        case VAL_INTEGER:
            return in.readInt();

        case VAL_LONG:
            return in.readLong();

        case VAL_FLOAT:
            return in.readFloat();

        case VAL_DOUBLE:
            return in.readDouble();

        case VAL_STRING:
            return in.readString();

        case VAL_CHARSEQUENCE:
            return in.readCharSequence();

        case VAL_REMOTEBUNDLE:
            return new RemoteBundle(in, loader);

        case VAL_SAFEPARCELABLE:
            return in.readParcelable(loader);

        case VAL_LIST:
            return readListFromParcel(in, loader);

        case VAL_MAP:
            return readMapFromParcel(in, loader);

        case VAL_SPARSEARRAY:
            return readSparseArrayFromParcel(in, loader);

        case VAL_BOOLEANARRAY:
            return in.createBooleanArray();

        case VAL_BYTEARRAY:
            return in.createByteArray();

        case VAL_INTARRAY:
            return in.createIntArray();

        case VAL_LONGARRAY:
            return in.createLongArray();

        case VAL_FLOATARRAY:
            return in.createFloatArray();

        case VAL_DOUBLEARRAY:
            return in.createDoubleArray();

        case VAL_CHARARRAY:
            return in.createCharArray();

        case VAL_STRINGARRAY:
            return in.createStringArrayList();

        case VAL_CHARSEQUENCEARRAY:
            return in.readCharSequenceArray();

        case VAL_SAFEPARCELABLEARRAY:
            return in.readParcelableArray(loader);

        case VAL_PARCELABLE:
            return IwdsUtils.createParcelableFromSafeParcel(in, loader);

        case VAL_PARCELABLEARRAY:
            return readParcelArrayFromParcel(in, loader);

        case VAL_OBJECTARRAY:
            return readArrayFromParcel(in, loader);

        case VAL_SERIALIZABLE:
            return in.readSerializable();

        default:
            final int off = in.dataPosition() - 4;
            in.setDataPosition(off);
            throw new RuntimeException("Read from SafeParcel: Unmarshalling unkown type code: "
                    + type + " at offset " + off);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static ArrayList readListFromParcel(SafeParcel in, ClassLoader loader) {
        final int N = in.readInt();
        final ArrayList list = new ArrayList(N);

        for (int i = 0; i < N; i++) {
            list.add(readValueFromParcel(in, loader));
        }
        return list;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static HashMap readMapFromParcel(SafeParcel in, ClassLoader loader) {
        final int N = in.readInt();
        final HashMap map = new HashMap(N);

        for (int i = 0; i < N; i++) {
            map.put(readValueFromParcel(in, loader), readValueFromParcel(in, loader));
        }
        return map;
    }

    private static SparseArray<Parcelable> readSparseArrayFromParcel(SafeParcel in,
            ClassLoader loader) {
        final int N = in.readInt();
        final SparseArray<Parcelable> array = new SparseArray<Parcelable>(N);

        for (int i = 0; i < N; i++) {
            array.put(in.readInt(), (Parcelable) readValueFromParcel(in, loader));
        }
        return array;
    }

    private static Object[] readArrayFromParcel(SafeParcel in, ClassLoader loader) {
        final int N = in.readInt();
        final Object[] result = new Object[N];

        for (int i = 0; i < N; i++) {
            result[i] = readValueFromParcel(in, loader);
        }
        return result;
    }

    private static Parcelable[] readParcelArrayFromParcel(SafeParcel in, ClassLoader loader) {
        final int N = in.readInt();
        final Parcelable[] result = new Parcelable[N];

        for (int i = 0; i < N; i++) {
            result[i] = IwdsUtils.createParcelableFromSafeParcel(in, loader);
        }
        return result;
    }

    private void copyFromBundleInner(Bundle bundle) {
        mClassLoader = bundle.getClassLoader();
        Set<String> keys = bundle.keySet();
        if (keys == null) return;

        Object value;
        for (String key : keys) {
            value = bundle.get(key);
            if (value instanceof IBinder) {
                IwdsLog.w(this, "Unsupport IBinder in RemoteBundle. Ignore this value.");
                continue;
            }

            mMap.put(key, objectToRemote(value));
        }
    }

    /**
     * 修改实例化对象时使用的{@link ClassLoader}
     * 
     * @param loader 新的{@link ClassLoader}
     */
    public void setClassLoader(ClassLoader loader) {
        mClassLoader = loader;
    }

    /**
     * 取得当前使用的{@link ClassLoader}
     * 
     * @return 当前使用的{@link ClassLoader}
     */
    public ClassLoader getClassLoader() {
        return mClassLoader;
    }

    /**
     * 取得表（{@link HashMap}）中数据的数量。
     * 
     * @return 表（{@link HashMap}）中数据的数量
     */
    public int size() {
        return mMap.size();
    }

    /**
     * 判断表（{@link HashMap}）中是否有数据
     * 
     * @return 如果表（{@link HashMap}）中没有数据返回<code>true</code>，否则返回<code>false</code>
     */
    public boolean isEmpty() {
        return mMap.isEmpty();
    }

    /**
     * 删除所有的表（{@link HashMap}中的元素
     */
    public void clear() {
        mMap.clear();
        mHasFds = false;
        mFdsKnown = true;
    }

    /**
     * 判断表（{@link HashMap}）中是否包含指定的键
     * 
     * @param key 指定的键
     * @return 如果包含指定的键返回<code>true</code>，否则返回<code>false</code>
     */
    public boolean containsKey(String key) {
        return mMap.containsKey(key);
    }

    /**
     * 返回指定的键对应的数据对象
     * 
     * @param key 指定的键
     * @return 指定的键对应的数据对象
     */
    public Object get(String key) {
        return mMap.get(key);
    }

    /**
     * 从表（{@link HashMap}）中删除指定的键对应的数据
     * 
     * @param key 指定的键
     */
    public void remove(String key) {
        mMap.remove(key);
    }

    /**
     * 把一个指定的{@link RemoteBundle}对象中所有数据插入当前的{@link RemoteBundle}
     * 
     * @param map 指定的{@link RemoteBundle}
     */
    public void putAll(RemoteBundle map) {
        if (map == null) return;
        mMap.putAll(map.mMap);
        mHasFds |= map.mHasFds;
        mFdsKnown = mFdsKnown && map.mFdsKnown;
    }

    /**
     * 把一个指定的{@link Bundle}对象中的所有数据插入当前的{@link RemoteBundle}。
     * <p>
     * 注意：插入数据时，会自动转换或忽略不支持的数据类型。
     * 
     * @param map 指定的{@link Bundle}
     */
    public void putAll(Bundle map) {
        putAll(fromBunble(map));
    }

    /**
     * 取得所有的键的一个集合
     * 
     * @return 返回所有的键的集合
     */
    public Set<String> keySet() {
        return mMap.keySet();
    }

    @SuppressWarnings("unchecked")
    private boolean hasFileDescriptors() {
        if (!mFdsKnown) {
            boolean fdFound = false; // keep going until we find one or run out of data

            final Set<Map.Entry<String, Object>> entries = mMap.entrySet();
            Object obj;
            for (Map.Entry<String, Object> entry : entries) {
                obj = entry.getValue();

                if (obj instanceof Parcelable) {
                    if ((((Parcelable) obj).describeContents() & SafeParcelable.CONTENTS_FILE_DESCRIPTOR) != 0) {
                        fdFound = true;
                        break;
                    }

                } else if (obj instanceof Parcelable[]) {
                    final Parcelable[] array = (Parcelable[]) obj;
                    final int N = array.length;

                    for (int i = 0; i < N; i++) {
                        if ((array[i].describeContents() & SafeParcelable.CONTENTS_FILE_DESCRIPTOR) != 0) {
                            fdFound = true;
                            break;
                        }
                    }

                } else if (obj instanceof SparseArray<?>) {
                    final SparseArray<? extends Parcelable> array =
                            (SparseArray<? extends Parcelable>) obj;
                    final int N = array.size();

                    for (int i = 0; i < N; i++) {
                        if ((array.valueAt(i).describeContents() & SafeParcelable.CONTENTS_FILE_DESCRIPTOR) != 0) {
                            fdFound = true;
                            break;
                        }
                    }

                } else if (obj instanceof ArrayList<?>) {
                    final ArrayList<?> list = (ArrayList<?>) obj;
                    final int N = list.size();
                    if (list.size() > 0 && list.get(0) instanceof Parcelable) {
                        for (int i = 0; i < N; i++) {
                            Parcelable p = (Parcelable) list.get(i);

                            if (p != null
                                    && (p.describeContents() & SafeParcelable.CONTENTS_FILE_DESCRIPTOR) != 0) {
                                fdFound = true;
                                break;
                            }
                        }
                    }
                }
            }

            mHasFds = fdFound;
            mFdsKnown = true;
        }
        return mHasFds;
    }
    /**
     * 插入一个boolean值，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的值。
     */
    public void putBoolean(String key, boolean value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个byte值，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的值。
     */
    public void putByte(String key, byte value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个char值，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的值。
     */
    public void putChar(String key, char value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个short值，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的值。
     */
    public void putShort(String key, short value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个int值，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的值。
     */
    public void putInt(String key, int value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个long值，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的值。
     */
    public void putLong(String key, long value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个float值，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的值。
     */
    public void putFloat(String key, float value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个double值，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的值。
     */
    public void putDouble(String key, double value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个{@link CharSequence}对象，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的值。
     */
    public void putCharSequence(String key, CharSequence value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个字符串，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的字符串。
     */
    public void putString(String key, String value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个{@link Parcelable}对象，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的值。
     */
    public void putParcelable(String key, Parcelable value) {
        mMap.put(key, objectToRemote(value));
        mFdsKnown = false;
    }

    /**
     * 插入一个{@link Parcelable}数组，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的数组。
     */
    public void putParcelableArray(String key, Parcelable[] value) {
        mMap.put(key, parcelableArrayToRemote(value));
        mFdsKnown = false;
    }

    /**
     * 插入一个{@link Parcelable}列表，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的列表。
     */
    public void putParcelableArrayList(String key, ArrayList<? extends Parcelable> value) {
        mMap.put(key, listToRemote(value));
        mFdsKnown = false;
    }

    /**
     * 插入一个由{@link Parcelable}组成的{@link SparseArray}，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的数据。
     */
    public void putSparseParcelableArray(String key, SparseArray<Parcelable> value) {
        mMap.put(key, sparseArrayToRemote(value));
        mFdsKnown = false;
    }

    /**
     * 插入一个int列表，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的值。
     */
    public void putIntegerArrayList(String key, ArrayList<Integer> value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个字符串列表，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的值。
     */
    public void putStringArrayList(String key, ArrayList<String> value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个{@link CharSequence}列表，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的值。
     */
    public void putCharSequenceArrayList(String key, ArrayList<CharSequence> value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个{@link Serializable}类型的序列化对象，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的序列化对象。
     */
    public void putSerializable(String key, Serializable value) {
        mMap.put(key, objectToRemote(value));
    }

    /**
     * 插入一个boolean数组，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的数组。
     */
    public void putBooleanArray(String key, boolean[] value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个byte数组，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的数组。
     */
    public void putByteArray(String key, byte[] value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个short数组，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的数组。
     */
    public void putShortArray(String key, short[] value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个char数组，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的数组。
     */
    public void putCharArray(String key, char[] value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个int数组，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的数组。
     */
    public void putIntArray(String key, int[] value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个long数组，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的数组。
     */
    public void putLongArray(String key, long[] value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个float数组，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的数组。
     */
    public void putFloatArray(String key, float[] value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个double数组，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的数组。
     */
    public void putDoubleArray(String key, double[] value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个{@link CharSequence}数组，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的数组。
     */
    public void putCharSequenceArray(String key, CharSequence[] value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个字符串数组，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的数组。
     */
    public void putStringArray(String key, String[] value) {
        mMap.put(key, value);
    }

    /**
     * 插入一个{@link Bundle}对象，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的对象。
     */
    public void putBundle(String key, Bundle value) {
        putRemoteBundle(key, fromBunble(value));
    }

    /**
     * 插入一个{@link RemoteBundle}对象，将替换该键原有的值。
     * 
     * @param key 需要插入的数据对应的键
     * @param value 需要插入的对象。
     */
    public void putRemoteBundle(String key, RemoteBundle value) {
        mMap.put(key, value);
    }

    private static void typeWarning(String key, Object value, String className,
            Object defaultValue, ClassCastException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Key ");
        sb.append(key);
        sb.append(" expected ");
        sb.append(className);
        sb.append(" but value was a ");
        sb.append(value.getClass().getName());
        sb.append(".  The default value ");
        sb.append(defaultValue);
        sb.append(" was returned.");
        IwdsLog.w(TAG, sb.toString());
        IwdsLog.w(TAG, "Attempt to cast generated internal exception:", e);
    }

    private static void typeWarning(String key, Object value, String className, ClassCastException e) {
        typeWarning(key, value, className, "<null>", e);
    }

    /**
     * 取得指定的键对应的boolean值
     * 
     * @param key 指定的键
     * @return 对应的boolean值
     */
    public Boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /**
     * 取得指定的键对应的boolean值
     * 
     * @param key 指定的键
     * @param defaultValue 指定的键没有数据时返回的默认值
     * @return 对应的boolean值
     */
    public Boolean getBoolean(String key, boolean defaultValue) {
        Object o = mMap.get(key);
        if (o == null) return defaultValue;
        try {
            return (Boolean) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Boolean", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * 取得指定的键对应的byte值
     * 
     * @param key 指定的键
     * @return 对应的byte值
     */
    public byte getByte(String key) {
        return getByte(key, (byte) 0);
    }

    /**
     * 取得指定的键对应的byte值
     * 
     * @param key 指定的键
     * @param defaultValue 指定的键没有数据时返回的默认值
     * @return 对应的byte值
     */
    public Byte getByte(String key, byte defaultValue) {
        Object o = mMap.get(key);
        if (o == null) return defaultValue;
        try {
            return (Byte) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Byte", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * 取得指定的键对应的char值
     * 
     * @param key 指定的键
     * @return 对应的char值
     */
    public char getChar(String key) {
        return getChar(key, (char) 0);
    }

    /**
     * 取得指定的键对应的char值
     * 
     * @param key 指定的键
     * @param defaultValue 指定的键没有数据时返回的默认值
     * @return 对应的char值
     */
    public char getChar(String key, char defaultValue) {
        Object o = mMap.get(key);
        if (o == null) return defaultValue;
        try {
            return (Character) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Character", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * 取得指定的键对应的short值
     * 
     * @param key 指定的键
     * @return 对应的short值
     */
    public short getShort(String key) {
        return getShort(key, (short) 0);
    }

    /**
     * 取得指定的键对应的short值
     * 
     * @param key 指定的键
     * @param defaultValue 指定的键没有数据时返回的默认值
     * @return 对应的short值
     */
    public short getShort(String key, short defaultValue) {
        Object o = mMap.get(key);
        if (o == null) return defaultValue;
        try {
            return (Short) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Short", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * 取得指定的键对应的int值
     * 
     * @param key 指定的键
     * @return 对应的int值
     */
    public Integer getInt(String key) {
        return getInt(key, 0);
    }

    /**
     * 取得指定的键对应的int值
     * 
     * @param key 指定的键
     * @param defaultValue 指定的键没有数据时返回的默认值
     * @return 对应的int值
     */
    public Integer getInt(String key, int defaultValue) {
        Object o = mMap.get(key);
        if (o == null) return defaultValue;
        try {
            return (Integer) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Integer", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * 取得指定的键对应的long值
     * 
     * @param key 指定的键
     * @return 对应的long值
     */
    public Long getLong(String key) {
        return getLong(key, 0);
    }

    /**
     * 取得指定的键对应的long值
     * 
     * @param key 指定的键
     * @param defaultValue 指定的键没有数据时返回的默认值
     * @return 对应的long值
     */
    public Long getLong(String key, long defaultValue) {
        Object o = mMap.get(key);
        if (o == null) return defaultValue;
        try {
            return (Long) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Long", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * 取得指定的键对应的float值
     * 
     * @param key 指定的键
     * @return 对应的float值
     */
    public float getFloat(String key) {
        return getFloat(key, 0);
    }

    /**
     * 取得指定的键对应的float值
     * 
     * @param key 指定的键
     * @param defaultValue 指定的键没有数据时返回的默认值
     * @return 对应的float值
     */
    public float getFloat(String key, float defaultValue) {
        Object o = mMap.get(key);
        if (o == null) return defaultValue;
        try {
            return (Float) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Float", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * 取得指定的键对应的double值
     * 
     * @param key 指定的键
     * @return 对应的double值
     */
    public Double getDouble(String key) {
        return getDouble(key, 0);
    }

    /**
     * 取得指定的键对应的double值
     * 
     * @param key 指定的键
     * @param defaultValue 指定的键没有数据时返回的默认值
     * @return 对应的double值
     */
    public Double getDouble(String key, double defaultValue) {
        Object o = mMap.get(key);
        if (o == null) return defaultValue;
        try {
            return (Double) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Double", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * 取得指定的键对应的字符串
     * 
     * @param key 指定的键
     * @return 对应的字符串
     */
    public String getString(String key) {
        Object o = mMap.get(key);
        if (o == null) return null;
        try {
            return (String) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "String", e);
            return null;
        }
    }

    /**
     * 取得指定的键对应的字符串
     * 
     * @param key 指定的键
     * @param defaultValue 指定的键没有数据时返回的默认值
     * @return 对应的字符串
     */
    public String getString(String key, String defaultValue) {
        final String s = getString(key);
        return s == null ? defaultValue : s;
    }

    /**
     * 取得指定的键对应的{@link CharSequence}对象
     * 
     * @param key 指定的键
     * @return 对应的{@link CharSequence}对象
     */
    public CharSequence getCharSequence(String key) {
        Object o = mMap.get(key);
        if (o == null) return null;
        try {
            return (CharSequence) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "CharSequence", e);
            return null;
        }
    }

    /**
     * 取得指定的键对应的{@link CharSequence}值
     * 
     * @param key 指定的键
     * @param defaultValue 指定的键没有数据时返回的默认值
     * @return 对应的{@link CharSequence}值
     */
    public CharSequence getCharSequence(String key, CharSequence defaultValue) {
        final CharSequence cs = getCharSequence(key);
        return cs == null ? defaultValue : cs;
    }

    /**
     * 取得指定的键对应的{@link Bundle}对象
     * 
     * @param key 指定的键
     * @return 对应的{@link Bundle}对象
     */
    public Bundle getBundle(String key) {
        final RemoteBundle bundle = getRemoteBundle(key);
        return bundle == null ? null : bundle.toBundle();
    }

    /**
     * 取得指定的键对应的{@link RemoteBundle}对象
     * 
     * @param key 指定的键
     * @return 对应的{@link RemoteBundle}对象
     */
    public RemoteBundle getRemoteBundle(String key) {
        Object o = mMap.get(key);
        if (o == null) return null;
        try {
            return (RemoteBundle) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "RemoteBundle", e);
            return null;
        }
    }

    /**
     * 取得指定的键对应的{@link Parcelable}对象
     * 
     * @param key 指定的键
     * @return 对应的{@link Parcelable}对象
     */
    @SuppressWarnings("unchecked")
    public <T extends Parcelable> T getParcelable(String key) {
        Object o = mMap.get(key);
        if (o == null) return null;
        try {
            return (T) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Parcelable", e);
            return null;
        }
    }

    /**
     * 取得指定的键对应的{@link Parcelable}数组
     * 
     * @param key 指定的键
     * @return 对应的{@link Parcelable}数组
     */
    public Parcelable[] getParcelableArray(String key) {
        Object o = mMap.get(key);
        if (o == null) return null;
        try {
            return (Parcelable[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Parcelable[]", e);
            return null;
        }
    }

    /**
     * 取得指定的键对应的{@link Parcelable}列表
     * 
     * @param key 指定的键
     * @return 对应的{@link Parcelable}列表
     */
    @SuppressWarnings("unchecked")
    public <T extends Parcelable> ArrayList<T> getParcelableArrayList(String key) {
        Object o = mMap.get(key);
        if (o == null) return null;
        try {
            return (ArrayList<T>) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "ArrayList", e);
            return null;
        }
    }

    /**
     * 取得指定的键对应的{@link Parcelable}散列数组（{@link SparseArray}）
     * 
     * @param key 指定的键
     * @return 对应的{@link Parcelable}散列数组（{@link SparseArray}）
     */
    @SuppressWarnings("unchecked")
    public <T extends Parcelable> SparseArray<T> getSparseParcelableArray(String key) {
        Object o = mMap.get(key);
        if (o == null) return null;
        try {
            return (SparseArray<T>) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "SparseArray", e);
            return null;
        }
    }

    /**
     * 取得指定的键对应的{@link Serializable}对象
     * 
     * @param key 指定的键
     * @return 对应的{@link Serializable}对象
     */
    public Serializable getSerializable(String key) {
        Object o = mMap.get(key);
        if (o == null) return null;
        try {
            return (Serializable) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Serializable", e);
            return null;
        }
    }

    /**
     * 取得指定的键对应的int列表
     * 
     * @param key 指定的键
     * @return 对应的int列表
     */
    @SuppressWarnings("unchecked")
    public ArrayList<Integer> getIntegerArrayList(String key) {
        Object o = mMap.get(key);
        if (o == null) return null;
        try {
            return (ArrayList<Integer>) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "ArrayList<Integer>", e);
            return null;
        }
    }

    /**
     * 取得指定的键对应的字符串列表
     * 
     * @param key 指定的键
     * @return 对应的字符串列表
     */
    @SuppressWarnings("unchecked")
    public ArrayList<String> getStringArrayList(String key) {
        Object o = mMap.get(key);
        if (o == null) return null;
        try {
            return (ArrayList<String>) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "ArrayList<String>", e);
            return null;
        }
    }

    /**
     * 取得指定的键对应的{@link CharSequence}列表
     * 
     * @param key 指定的键
     * @return 对应的{@link CharSequence}列表
     */
    @SuppressWarnings("unchecked")
    public ArrayList<CharSequence> getCharSequenceArrayList(String key) {
        Object o = mMap.get(key);
        if (o == null) return null;
        try {
            return (ArrayList<CharSequence>) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "ArrayList<CharSequence>", e);
            return null;
        }
    }

    /**
     * 取得指定的键对应的boolean数组
     * 
     * @param key 指定的键
     * @return 对应的boolean数组
     */
    public boolean[] getBooleanArray(String key) {
        Object o = mMap.get(key);
        if (o == null) return null;
        try {
            return (boolean[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "byte[]", e);
            return null;
        }
    }

    /**
     * 取得指定的键对应的byte数组
     * 
     * @param key 指定的键
     * @return 对应的byte数组
     */
    public byte[] getByteArray(String key) {
        Object o = mMap.get(key);
        if (o == null) return null;
        try {
            return (byte[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "byte[]", e);
            return null;
        }
    }

    /**
     * 取得指定的键对应的short数组
     * 
     * @param key 指定的键
     * @return 对应的short数组
     */
    public short[] getShortArray(String key) {
        Object o = mMap.get(key);
        if (o == null) return null;
        try {
            return (short[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "short[]", e);
            return null;
        }
    }

    /**
     * 取得指定的键对应的char数组
     * 
     * @param key 指定的键
     * @return 对应的char数组
     */
    public char[] getCharArray(String key) {
        Object o = mMap.get(key);
        if (o == null) return null;
        try {
            return (char[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "char[]", e);
            return null;
        }
    }

    /**
     * 取得指定的键对应的int数组
     * 
     * @param key 指定的键
     * @return 对应的int数组
     */
    public int[] getIntArray(String key) {
        Object o = mMap.get(key);
        if (o == null) return null;
        try {
            return (int[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "int[]", e);
            return null;
        }
    }

    /**
     * 取得指定的键对应的long数组
     * 
     * @param key 指定的键
     * @return 对应的long数组
     */
    public long[] getLongArray(String key) {
        Object o = mMap.get(key);
        if (o == null) return null;
        try {
            return (long[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "long[]", e);
            return null;
        }
    }

    /**
     * 取得指定的键对应的float数组
     * 
     * @param key 指定的键
     * @return 对应的float数组
     */
    public float[] getFloatArray(String key) {
        Object o = mMap.get(key);
        if (o == null) return null;
        try {
            return (float[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "float[]", e);
            return null;
        }
    }

    /**
     * 取得指定的键对应的double数组
     * 
     * @param key 指定的键
     * @return 对应的double数组
     */
    public double[] getDoubleArray(String key) {
        Object o = mMap.get(key);
        if (o == null) return null;
        try {
            return (double[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "double[]", e);
            return null;
        }
    }

    /**
     * 取得指定的键对应的字符串数组
     * 
     * @param key 指定的键
     * @return 对应的字符串数组
     */
    public String[] getStringArray(String key) {
        Object o = mMap.get(key);
        if (o == null) return null;
        try {
            return (String[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "String[]", e);
            return null;
        }
    }

    /**
     * 取得指定的键对应的{@link CharSequence}数组
     * 
     * @param key 指定的键
     * @return 对应的{@link CharSequence}数组
     */
    public CharSequence[] getCharSequenceArray(String key) {
        Object o = mMap.get(key);
        if (o == null) return null;
        try {
            return (CharSequence[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "CharSequence[]", e);
            return null;
        }
    }

    @Override
    public int describeContents() {
        int mask = 0;
        if (hasFileDescriptors()) {
            mask |= SafeParcelable.CONTENTS_FILE_DESCRIPTOR;
        }
        return mask;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeMap(mMap);
    }

    /**
     * 克隆当前对象。内部的表（{@link HashMap}）将被克隆，但表内的键和值都是引用的拷贝。
     * 
     * @return 克隆得到的新对象。
     */
    @Override
    public RemoteBundle clone() {
        return new RemoteBundle(this);
    }

    /**
     * 把一个{@link Bundle}转换为{@link RemoteBundle}
     * 
     * @param bundle 数据源{@link Bundle}
     * @return 从{@link Bundle}抽取数据得到的{@link RemoteBundle}
     * @see #toBundle()
     */
    public static RemoteBundle fromBunble(Bundle bundle) {
        if (bundle == null) return null;

        return new RemoteBundle(bundle);
    }

    /**
     * 转换为{@link Bundle}
     * 
     * @return 把数据存入后得到的{@link Bundle}
     * @see #fromBunble(Bundle)
     */
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.setClassLoader(mClassLoader);
        copyToBundlerInner(bundle);
        return bundle;
    }

    private void copyToBundlerInner(Bundle bundle) {
        if (bundle == null) return;

        final Set<Map.Entry<String, Object>> entries = mMap.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            putToBundle(bundle, entry.getKey(), objectToLocal(entry.getValue()));
        }
    }

    @SuppressWarnings("unchecked")
    private static void putToBundle(Bundle bundle, String key, Object value) {
        if (value instanceof Boolean) {
            bundle.putBoolean(key, (Boolean) value);

        } else if (value instanceof Byte) {
            bundle.putByte(key, (Byte) value);

        } else if (value instanceof Short) {
            bundle.putShort(key, (Short) value);

        } else if (value instanceof Integer) {
            bundle.putInt(key, (Integer) value);

        } else if (value instanceof Long) {
            bundle.putLong(key, (Long) value);

        } else if (value instanceof Float) {
            bundle.putFloat(key, (Float) value);

        } else if (value instanceof Double) {
            bundle.putDouble(key, (Double) value);

        } else if (value instanceof Character) {
            bundle.putChar(key, (Character) value);

        } else if (value instanceof String) {
            bundle.putString(key, (String) value);

        } else if (value instanceof CharSequence) {
            bundle.putCharSequence(key, (CharSequence) value);

        } else if (value instanceof Bundle) {
            bundle.putBundle(key, (Bundle) value);

        } else if (value instanceof boolean[]) {
            bundle.putBooleanArray(key, (boolean[]) value);

        } else if (value instanceof byte[]) {
            bundle.putByteArray(key, (byte[]) value);

        } else if (value instanceof short[]) {
            bundle.putShortArray(key, (short[]) value);

        } else if (value instanceof int[]) {
            bundle.putIntArray(key, (int[]) value);

        } else if (value instanceof long[]) {
            bundle.putLongArray(key, (long[]) value);

        } else if (value instanceof float[]) {
            bundle.putFloatArray(key, (float[]) value);

        } else if (value instanceof double[]) {
            bundle.putDoubleArray(key, (double[]) value);

        } else if (value instanceof char[]) {
            bundle.putCharArray(key, (char[]) value);

        } else if (value instanceof String[]) {
            bundle.putStringArray(key, (String[]) value);

        } else if (value instanceof CharSequence[]) {
            bundle.putCharSequenceArray(key, (CharSequence[]) value);

        } else if (value instanceof ArrayList<?>) {
            putListToBundle(bundle, key, (ArrayList<?>) value);

        } else if (value instanceof SparseArray<?>) {
            bundle.putSparseParcelableArray(key, (SparseArray<? extends Parcelable>) value);

        } else if (value instanceof Parcelable) {
            bundle.putParcelable(key, (Parcelable) value);

        } else if (value instanceof Parcelable[]) {
            bundle.putParcelableArray(key, (Parcelable[]) value);

        } else if (value instanceof Serializable) {
            bundle.putSerializable(key, (Serializable) value);
        }
    }

    @SuppressWarnings("unchecked")
    private static void putListToBundle(Bundle bundle, String key, ArrayList<?> value) {
        if (value != null && value.size() > 0) {
            final Object o = value.get(0);

            if (o instanceof Integer) {
                bundle.putIntegerArrayList(key, (ArrayList<Integer>) value);
            } else if (o instanceof String) {
                bundle.putStringArrayList(key, (ArrayList<String>) value);
            } else if (o instanceof CharSequence) {
                bundle.putCharSequenceArrayList(key, (ArrayList<CharSequence>) value);
            } else if (o instanceof Parcelable) {
                bundle.putParcelableArrayList(key, (ArrayList<? extends Parcelable>) value);
            }
        }
    }

    @Override
    public void writeToParcel(SafeParcel dest, int flags) {
        dest.writeInt(mMap.size());

        final Set<Map.Entry<String, Object>> entries = mMap.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            dest.writeString(entry.getKey());
            writeValueToParcel(entry.getValue(), dest, flags);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void writeValueToParcel(Object value, SafeParcel dest, int flags) {
        if (value == null) {
            dest.writeInt(VAL_NULL);

        } else if (value instanceof Boolean) {
            dest.writeInt(VAL_BOOLEAN);
            dest.writeInt((Boolean) value ? 1 : 0);

        } else if (value instanceof Byte) {
            dest.writeInt(VAL_BYTE);
            dest.writeByte((Byte) value);

        } else if (value instanceof Short) {
            dest.writeInt(VAL_SHORT);
            dest.writeInt(((Short) value).intValue());

        } else if (value instanceof Integer) {
            dest.writeInt(VAL_INTEGER);
            dest.writeInt((Integer) value);

        } else if (value instanceof Long) {
            dest.writeInt(VAL_LONG);
            dest.writeLong((Long) value);

        } else if (value instanceof Float) {
            dest.writeInt(VAL_FLOAT);
            dest.writeFloat((Float) value);

        } else if (value instanceof Double) {
            dest.writeInt(VAL_DOUBLE);
            dest.writeDouble((Double) value);

        } else if (value instanceof String) {
            dest.writeInt(VAL_STRING);
            dest.writeString((String) value);

        } else if (value instanceof CharSequence) {
            dest.writeInt(VAL_CHARSEQUENCE);
            dest.writeCharSequence((CharSequence) value);

        } else if (value instanceof RemoteBundle) {
            dest.writeInt(VAL_REMOTEBUNDLE);
            ((RemoteBundle) value).writeToParcel(dest, flags);

        } else if (value instanceof SafeParcelable) {
            dest.writeInt(VAL_SAFEPARCELABLE);
            dest.writeParcelable((SafeParcelable) value, flags);

        } else if (value instanceof List) {
            dest.writeInt(VAL_LIST);
            writeListToParcel((List) value, dest, flags);

        } else if (value instanceof Map) {
            dest.writeInt(VAL_MAP);
            writeMapToParcel((Map) value, dest, flags);

        } else if (value instanceof SparseArray) {
            dest.writeInt(VAL_SPARSEARRAY);
            writeSparseArrayToParcel((SparseArray<? extends Parcelable>) value, dest, flags);

        } else if (value instanceof boolean[]) {
            dest.writeInt(VAL_BOOLEANARRAY);
            dest.writeBooleanArray((boolean[]) value);

        } else if (value instanceof byte[]) {
            dest.writeInt(VAL_BYTEARRAY);
            dest.writeByteArray((byte[]) value);

        } else if (value instanceof int[]) {
            dest.writeInt(VAL_INTARRAY);
            dest.writeIntArray((int[]) value);

        } else if (value instanceof long[]) {
            dest.writeInt(VAL_LONGARRAY);
            dest.writeLongArray((long[]) value);

        } else if (value instanceof float[]) {
            dest.writeInt(VAL_FLOATARRAY);
            dest.writeFloatArray((float[]) value);

        } else if (value instanceof double[]) {
            dest.writeInt(VAL_DOUBLEARRAY);
            dest.writeDoubleArray((double[]) value);

        } else if (value instanceof char[]) {
            dest.writeInt(VAL_CHARARRAY);
            dest.writeCharArray((char[]) value);

        } else if (value instanceof String[]) {
            dest.writeInt(VAL_STRINGARRAY);
            dest.writeStringArray((String[]) value);

        } else if (value instanceof CharSequence[]) {
            dest.writeInt(VAL_CHARSEQUENCEARRAY);
            dest.writeCharSequenceArray((CharSequence[]) value);

        } else if (value instanceof SafeParcelable[]) {
            dest.writeInt(VAL_SAFEPARCELABLEARRAY);
            dest.writeParcelableArray((SafeParcelable[]) value, flags);

        } else if (value instanceof Parcelable) {
            dest.writeInt(VAL_PARCELABLE);
            IwdsUtils.writeParcelablleToSafeParcel((Parcelable) value, dest, flags);

        } else if (value instanceof Parcelable[]) {
            dest.writeInt(VAL_PARCELABLEARRAY);
            writeParcelableArrayToParcel((Parcelable[]) value, dest, flags);

        } else if (value instanceof Object[]) {
            dest.writeInt(VAL_OBJECTARRAY);
            writeArrayToParcel((Object[]) value, dest, flags);

        } else if (value instanceof Serializable) {
            dest.writeInt(VAL_SERIALIZABLE);
            dest.writeSerializable((Serializable) value);

        } else {
            throw new RuntimeException("Write to SafeParcel: unable to marshall value: " + value);
        }
    }

    @SuppressWarnings("rawtypes")
    private static void writeListToParcel(List list, SafeParcel dest, int flags) {
        final int N = list.size();
        dest.writeInt(N);
        for (int i = 0; i < N; i++) {
            writeValueToParcel(list.get(i), dest, flags);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void writeMapToParcel(Map map, SafeParcel dest, int flags) {
        final Set<Map.Entry> entries = map.entrySet();
        dest.writeInt(entries.size());
        for (Map.Entry entry : entries) {
            writeValueToParcel(entry.getKey(), dest, flags);
            writeValueToParcel(entry.getValue(), dest, flags);
        }
    }

    private static void writeSparseArrayToParcel(SparseArray<? extends Parcelable> array,
            SafeParcel dest, int flags) {
        final int N = array.size();
        dest.writeInt(N);
        for (int i = 0; i < N; i++) {
            dest.writeInt(array.keyAt(i));
            writeValueToParcel(array.valueAt(i), dest, flags);
        }
    }

    private static void writeArrayToParcel(Object[] array, SafeParcel dest, int flags) {
        final int N = array.length;
        dest.writeInt(N);
        for (int i = 0; i < N; i++) {
            writeValueToParcel(array[i], dest, flags);
        }
    }

    private static void writeParcelableArrayToParcel(Parcelable[] array, SafeParcel dest, int flags) {
        final int N = array.length;
        dest.writeInt(N);
        for (int i = 0; i < N; i++) {
            IwdsUtils.writeParcelablleToSafeParcel(array[i], dest, flags);
        }
    }

    /**
     * 把{@link RemoteBundle}数组转换为{@link Bundle}数组
     * 
     * @param array {@link RemoteBundle}数组
     * @return 转换得到的{@link Bundle}数组
     */
    public static Bundle[] arrayToLocal(RemoteBundle[] array) {
        final int N = array.length;
        final Bundle[] result = new Bundle[N];

        for (int i = 0; i < N; i++) {
            result[i] = array[i].toBundle();
        }
        return result;
    }

    /**
     * 把{@link Bundle}数组转换为{@link RemoteBundle}数组
     * 
     * @param array {@link Bundle}数组
     * @return 转换得到的{@link RemoteBundle}数组
     */
    public static RemoteBundle[] arrayToRemote(Bundle[] array) {
        final int N = array.length;
        final RemoteBundle[] result = new RemoteBundle[N];

        for (int i = 0; i < N; i++) {
            result[i] = fromBunble(array[i]);
        }
        return result;
    }

    /**
     * 把Remote类型（用于跨设备传输的对象，例如：{@link RemoteBundle}、{@link RemoteIntent}以及{@link IwdsBitmap}
     * 等）的对象转换为Local类型（本地与Remote类型对应的对象，例如：{@link Bundle}、{@linkp Intent}以及{@link Bitmap}等）。
     * 
     * <p>
     * 若该对象是数组或者集合，则转换其中的每一个元素。若该对象不是Remote类型，则返回本身。
     * 
     * @param obj 原对象
     * @return 若需要转换则返回转换后的对象，否则返回本身
     */
    @SuppressWarnings("unchecked")
    public static Object objectToLocal(Object obj) {
        if (obj == null) return null;

        if (obj instanceof RemoteBundle) {
            return ((RemoteBundle) obj).toBundle();

        } else if (obj instanceof RemoteIntent) {
            return ((RemoteIntent) obj).toIntent();

        } else if (obj instanceof IwdsBitmap) {
            return ((IwdsBitmap) obj).toBitmap();

        } else if (obj instanceof RemoteBundle[]) {
            return arrayToLocal((RemoteBundle[]) obj);

        } else if (obj instanceof RemoteIntent[]) {
            return RemoteIntent.arrayToLocal((RemoteIntent[]) obj);

        } else if (obj instanceof IwdsBitmap[]) {
            return IwdsBitmap.arrayToLocal((IwdsBitmap[]) obj);

        } else if (obj instanceof Parcelable[]) {
            return parcelableArrayToLocal((Parcelable[]) obj);

        } else if (obj instanceof List<?>) {
            return listToLocal((List<?>) obj);

        } else if (obj instanceof SparseArray) {
            return sparseArrayToLocal((SparseArray<? extends Parcelable>) obj);

        } else {
            return obj;
        }
    }

    /**
     * 把{@link Parcelable}数组中的元素转换成Local类型。若数组中包含需要转换的元素则转换，否则拷贝元素。
     * <p>
     * 注意：不管数组是否包含需要转换的元素，返回的数组都是一个新的数组。
     * 
     * @param array 原数组
     * @return 处理后的数组
     */
    public static Parcelable[] parcelableArrayToLocal(Parcelable[] array) {
        final int N = array.length;
        final Parcelable[] result = new Parcelable[N];
        for (int i = 0; i < N; i++) {
            result[i] = (Parcelable) objectToLocal(array[i]);
        }
        return result;
    }

    /**
     * 把列表中的元素转换成Local类型。若数组中包含需要转换的元素则转换，否则拷贝元素。
     * <p>
     * 注意：不管列表是否包含需要转换的元素，返回的数组都是一个新的列表。
     * 
     * @param array 原列表
     * @return 处理后的列表
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static ArrayList listToLocal(List<?> list) {
        final int N = list.size();
        final ArrayList result = new ArrayList(N);

        for (int i = 0; i < N; i++) {
            result.add(objectToLocal(list.get(i)));
        }
        return result;
    }

    /**
     * 把{@link SparseArray}中的元素转换成Local类型。若{@link SparseArray}中包含需要转换的元素则转换，否则拷贝元素。
     * <p>
     * 注意：不管{@link SparseArray}是否包含需要转换的元素，返回的数组都是一个新的{@link SparseArray}。
     * 
     * @param array 原{@link SparseArray}
     * @return 处理后的{@link SparseArray}
     */
    public static SparseArray<? extends Parcelable> sparseArrayToLocal(
            SparseArray<? extends Parcelable> array) {
        final int N = array.size();
        final SparseArray<Parcelable> result = new SparseArray<Parcelable>(N);

        for (int i = 0; i < N; i++) {
            result.put(array.keyAt(i), (Parcelable) objectToLocal(array.valueAt(i)));
        }
        return result;
    }

    /**
     * 把Local类型（本地与Remote类型对应的对象，例如：{@link Bundle}、{@linkp Intent}以及{@link Bitmap}
     * 等）的对象转换为Remote类型（用于跨设备传输的对象，例如：{@link RemoteBundle}、{@link RemoteIntent}以及{@link IwdsBitmap}
     * 等）。
     * 
     * <p>
     * 若该对象是数组或者集合，则转换其中的每一个元素。若该对象不是Local类型，则返回本身。
     * 
     * @param obj 原对象
     * @return 若需要转换则返回转换后的对象，否则返回本身
     */
    @SuppressWarnings("unchecked")
    public static Object objectToRemote(Object obj) {
        if (obj == null) return null;

        if (obj instanceof Bundle) {
            return fromBunble((Bundle) obj);

        } else if (obj instanceof Intent) {
            return RemoteIntent.fromIntent((Intent) obj);

        } else if (obj instanceof Bitmap) {
            return IwdsBitmap.fromBitmap((Bitmap) obj);

        } else if (obj instanceof Bundle[]) {
            return arrayToRemote((Bundle[]) obj);

        } else if (obj instanceof Intent[]) {
            return RemoteIntent.arrayToRemote((Intent[]) obj);

        } else if (obj instanceof Bitmap[]) {
            return IwdsBitmap.arrayToRemote((Bitmap[]) obj);

        } else if (obj instanceof Parcelable[]) {
            return parcelableArrayToRemote((Parcelable[]) obj);

        } else if (obj instanceof List<?>) {
            return listToRemote((List<?>) obj);

        } else if (obj instanceof SparseArray<?>) {
            return sparseArrayToRemote((SparseArray<? extends Parcelable>) obj);

        } else {
            return obj;
        }
    }

    /**
     * 把{@link Parcelable}数组中的元素转换成Remote类型。若数组中包含需要转换的元素则转换，否则拷贝元素。
     * <p>
     * 注意：不管数组是否包含需要转换的元素，返回的数组都是一个新的数组。
     * 
     * @param array 原数组
     * @return 处理后的数组
     */
    public static Parcelable[] parcelableArrayToRemote(Parcelable[] array) {
        final int N = array.length;
        final Parcelable[] result = new Parcelable[N];

        for (int i = 0; i < N; i++) {
            result[i] = (Parcelable) objectToRemote(array[i]);
        }
        return result;
    }

    /**
     * 把列表中的元素转换成Remote类型。若数组中包含需要转换的元素则转换，否则拷贝元素。
     * <p>
     * 注意：不管列表是否包含需要转换的元素，返回的数组都是一个新的列表。
     * 
     * @param array 原列表
     * @return 处理后的列表
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static ArrayList listToRemote(List<?> list) {
        final int N = list.size();
        final ArrayList result = new ArrayList(N);

        for (int i = 0; i < N; i++) {
            result.add(objectToRemote(list.get(i)));
        }
        return result;
    }

    /**
     * 把{@link SparseArray}中的元素转换成Remote类型。若{@link SparseArray}中包含需要转换的元素则转换，否则拷贝元素。
     * <p>
     * 注意：不管{@link SparseArray}是否包含需要转换的元素，返回的数组都是一个新的{@link SparseArray}。
     * 
     * @param array 原{@link SparseArray}
     * @return 处理后的{@link SparseArray}
     */
    public static SparseArray<? extends Parcelable> sparseArrayToRemote(
            SparseArray<? extends Parcelable> array) {
        final int N = array.size();
        final SparseArray<Parcelable> result = new SparseArray<Parcelable>(N);

        for (int i = 0; i < N; i++) {
            result.put(array.keyAt(i), (Parcelable) objectToRemote(array.valueAt(i)));
        }
        return result;
    }

    public static final Creator CREATOR = new Creator();

    public static final class Creator implements Parcelable.Creator<RemoteBundle>,
            SafeParcelable.Creator<RemoteBundle> {

        @Override
        public RemoteBundle createFromParcel(SafeParcel source) {
            return new RemoteBundle(source);
        }

        @Override
        public RemoteBundle createFromParcel(Parcel source) {
            return new RemoteBundle(source);
        }

        @Override
        public RemoteBundle[] newArray(int size) {
            return new RemoteBundle[size];
        }
    }

    @Override
    public String toString() {
        return "RemoteBundle[" + mMap.toString() + "]";
    }
}