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
package com.ingenic.iwds.utils;

import java.lang.reflect.Field;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.SparseArray;

import com.ingenic.iwds.os.SafeParcel;
import com.ingenic.iwds.os.SafeParcelable;

public final class IwdsUtils {

    private static final String TAG = "IwdsUtils";

    private IwdsUtils() {}

    public static <T> void addInArray(SparseArray<SparseArray<T>> array, int firstKey,
            int secondKey, T value) {
        if (array == null) return;

        SparseArray<T> ts = array.get(firstKey);
        if (ts == null) {
            ts = new SparseArray<T>();

            synchronized (array) {
                array.put(firstKey, ts);
            }
        }

        synchronized (array) {
            ts.put(secondKey, value);
        }
    }

    public static <T> void deleteInArray(SparseArray<SparseArray<T>> array, int firstKey,
            int secondKey) {
        if (array == null) return;

        SparseArray<T> ts = array.get(firstKey);
        if (ts == null) return;

        synchronized (array) {
            ts.delete(secondKey);

            if (ts.size() == 0) {
                array.delete(firstKey);
            }
        }
    }

    /**
     * 往{@link SafeParcel}写入{@link Parcelable}类型的数据。特殊数据：{@link Bundle}、{@link Intent}和
     * {@link Bitmap}应转换成对应的SafeParcelable类型：{@link com.ingenic.iwds.os.RemoteBundle}、
     * {@link com.ingenic.iwds.content.RemoteIntent}和{@link IwdsBitmap}之后调用
     * {@link SafeParcelable#writeToParcel(SafeParcel, int)}方法。
     * 
     * @param value 数据对象
     * @param dest 写入目标{@link SafeParcel}
     * @param flags 写入标识
     * 
     * @see #createParcelableFromSafeParcel(SafeParcel, ClassLoader)
     */
    public static <T extends Parcelable> void writeParcelablleToSafeParcel(T value,
            SafeParcel dest, int flags) {
        // 特殊的数据类型，应转换成对应的SafeParcelable类型后使用writeToParcel(SafeParcel, int)方法
        if (value instanceof Bundle || value instanceof Intent || value instanceof Bitmap) {
            throw new IllegalArgumentException("Can not write if this Parcelable"
                    + " can't marshall in a Parcel. Change to Remote(implements SafeParcelable)"
                    + " and than use writeToParcel(SafeParcel, int).");
        }

        if (value != null) {
            // 写入类名，用于表明有数据以及实例化时使用反射机制
            dest.writeString(value.getClass().getName());

            // 调用Parcel序列化对象
            final Parcel p = Parcel.obtain();
            value.writeToParcel(p, flags);
            p.setDataPosition(0);

            // 把序列化后的数据转换成字节数组写入
            final byte[] data = p.marshall();
            dest.writeByteArray(data);
            p.recycle();
        } else {
            // 表明没有数据
            dest.writeString(null);
        }
    }

    /**
     * 从{@link SafeParcel}的当前位置读取（创建）{@link Parcelable}类型的数据对象
     * 
     * @param in 读取目标{@link SafeParcel}
     * @param loader 实例化对象使用的{@link ClassLoader}
     * @return {@link SafeParcel}的当前位置对应的{@link Parcelable}对象或者<code>null</code>
     * 
     * @see #writeParcelablleToSafeParcel(Parcelable, SafeParcel, int)
     */
    public static <T extends Parcelable> T createParcelableFromSafeParcel(SafeParcel in,
            ClassLoader loader) {
        // 读取Creator，如果没有对应的Creator则返回null
        final Parcelable.Creator<T> creator = readCreator(in, loader);
        if (creator == null) return null;

        // 读取字节数组数据，并写入到Parcel中
        final byte[] data = in.createByteArray();
        Parcel p = Parcel.obtain();
        p.unmarshall(data, 0, data.length);
        p.setDataPosition(0);

        // 从Parcel实例化对象
        T result = creator.createFromParcel(p);
        p.recycle();
        return result;
    }

    private static <T> Creator<T> readCreator(SafeParcel in, ClassLoader loader) {
        final String cls = in.readString();
        if (cls == null) return null;

        return getParcelableCreator(cls, loader);
    }

    @SuppressWarnings("unchecked")
    private static <T> Parcelable.Creator<T> getParcelableCreator(String cls, ClassLoader loader) {
        try {
            Class<?> c = loader == null ? Class.forName(cls) : Class.forName(cls, true, loader);

            Field f = c.getField("CREATOR");
            return (Parcelable.Creator<T>) f.get(null);
        } catch (IllegalAccessException e) {
            IwdsLog.e(TAG, "Illegal access when unmarshalling: " + cls, e);

            throw new BadParcelableException("IllegalAccessException when unmarshalling: " + cls);
        } catch (ClassNotFoundException e) {
            IwdsLog.e(TAG, "Class not found when unmarshalling: " + cls, e);

            throw new BadParcelableException("ClassNotFoundException when unmarshalling: " + cls);
        } catch (ClassCastException e) {
            throw new BadParcelableException("Parcelable protocol requires a "
                    + "Parcelable.Creator object called " + " CREATOR on class " + cls);
        } catch (NoSuchFieldException e) {
            throw new BadParcelableException("Parcelable protocol requires a "
                    + "Parcelable.Creator object called " + " CREATOR on class " + cls);
        } catch (NullPointerException e) {
            throw new BadParcelableException("Parcelable protocol requires "
                    + "the CREATOR object to be static on class " + cls);
        }
    }

    public static final SafeParcelable.Creator<CharSequence> CHAR_SEQUENCE_CREATOR = new SafeParcelable.Creator<CharSequence>() {

        @Override
        public CharSequence[] newArray(int size) {
            return new CharSequence[size];
        }

        @Override
        public CharSequence createFromParcel(SafeParcel in) {
            final byte[] data = in.marshall();
            final Parcel p = Parcel.obtain();
            p.unmarshall(data, 0, data.length);
            p.setDataPosition(0);

            CharSequence cs = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(p);
            p.recycle();
            return cs;
        }
    };

    public static void writeCharSequenceToParcel(CharSequence cs, SafeParcel dest, int flags) {
        final Parcel p = Parcel.obtain();
        TextUtils.writeToParcel(cs, p, flags);
        final byte[] data = p.marshall();

        dest.unmarshall(data, 0, data.length);
        p.recycle();
    }

    public static String getUriSafeString(Uri uri) {
        final String scheme = uri.getScheme();
        final String ssp = uri.getSchemeSpecificPart();

        if (scheme != null) {
            if (scheme.equalsIgnoreCase("tel") || scheme.equalsIgnoreCase("sip")
                    || scheme.equalsIgnoreCase("sms") || scheme.equalsIgnoreCase("smsto")
                    || scheme.equalsIgnoreCase("mailto")) {
                StringBuilder builder = new StringBuilder(64);
                builder.append(scheme);
                builder.append(':');

                if (ssp != null) {
                    for (int i = 0; i < ssp.length(); i++) {
                        char c = ssp.charAt(i);

                        if (c == '-' || c == '@' || c == '.') {
                            builder.append(c);
                        } else {
                            builder.append('x');
                        }
                    }
                }
                return builder.toString();
            }
        }

        StringBuilder builder = new StringBuilder(64);
        if (scheme != null) {
            builder.append(scheme);
            builder.append(':');
        }

        if (ssp != null) {
            builder.append(ssp);
        }
        return builder.toString();
    }
}