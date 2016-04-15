/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  SunWenZhong(Fighter) <wenzhong.sun@ingenic.com, wanmyqawdr@126.com>
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

package com.ingenic.iwds.utils.serializable;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * {@code Parcelable} 对象串行序列化工具, 用于将实现 {@code Parcelable} 接口的对象转化成
 * 字节数组 {@code byte[]}
 *
 * @see android.os.Parcel
 * @see android.os.Parcelable
 */
public class ParcelableUtils {

    /**
     * 返回实现 {@code Parcelable} 接口对象的字节数组 {@code byte[]}
     * @param parcelable
     *        实现 {@code Parcelable} 接口的对象
     * @return
     *        字节数组 {@code byte[]}
     * @see android.os.Parcelable
     */
    public static byte[] marshall(Parcelable parcelable) {
        Parcel parcel = Parcel.obtain();

        parcelable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();

        return bytes;
    }

    /**
     * 将字节数组写入到{@code Parcel} 并返回该 {@code Parcel}
     * @param bytes
     *        实现 {@code Parcelable} 接口的对象的字节数组 {@code byte[]}
     * @return
     *        {@code Parcel} 对象
     * @see android.os.Parcel
     */
    public static Parcel unmarshall(byte[] bytes) {
        Parcel parcel = Parcel.obtain();

        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);

        return parcel;
    }

    /**
     * 将指定类型对象的字节数组 {@code byte[]} 转化成指定类型的对象实例.
     * @param bytes
     *        实现 {@code Parcelable} 接口的对象的字节数组 {@code byte[]}
     * @param creator
     *        实现 {@code Parcelable} 接口的对象的静态工厂方法
     * @return
     *        指定类型的对象实例
     * @see android.os.Parcelable
     */
    public static <T> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {
        Parcel parcel = unmarshall(bytes);

        T obj = creator.createFromParcel(parcel);
        parcel.recycle();

        return obj;
    }
}
