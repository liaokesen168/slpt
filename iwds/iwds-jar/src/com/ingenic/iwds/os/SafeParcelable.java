/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  ZhangYanMing <yanming.zhang@ingenic.com, jamincheung@126.com>
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

package com.ingenic.iwds.os;

/**
 * 用于实现{@link SafeParcel}序列化的接口. 实现该接口的类必须有一个静态域<code>CREATOR</code>, 实现了
 * {@link SafeParcelable.Creator SafeParcelable.Creator}的接口
 *
 * <p>
 * 典型用法如下:
 * </p>
 * 
 * <pre>
 * public class MySafeParcelable implements SafeParcelable {
 *     private int mData;
 * 
 *     public int describeContents() {
 *         return 0;
 *     }
 * 
 *     public void writeToParcel(SafeParcel out, int flags) {
 *         out.writeInt(mData);
 *     }
 * 
 *     public static final SafeParcelable.Creator&lt;MySafeParcelable&gt; CREATOR = new SafeParcelable.Creator&lt;MySafeParcelable&gt;() {
 *         public MySafeParcelable createFromParcel(SafeParcel in) {
 *             return new MySafeParcelable(in);
 *         }
 * 
 *         public MySafeParcelable[] newArray(int size) {
 *             return new MySafeParcelable[size];
 *         }
 *     };
 * 
 *     private MySafeParcelable(SafeParcel in) {
 *         mData = in.readInt();
 *     }
 * }
 * </pre>
 */

public interface SafeParcelable {

    /**
     * {@link android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE}
     */
    public static final int PARCELABLE_WRITE_RETURN_VALUE = 0x0001;

    /**
     * {@link android.os.Parcelable.CONTENTS_FILE_DESCRIPTOR}
     */
    public static final int CONTENTS_FILE_DESCRIPTOR = 0x0001;

    /**
     * {@link android.os.Parcelable.describeContents}
     */
    public int describeContents();

    /**
     * 将对象写入SafeParcel
     * 
     * @param dest
     *            被写入的SafeParcel
     * @param flags
     *            0或者是{@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    public void writeToParcel(SafeParcel dest, int flags);

    /**
     * 必须实现的接口, 用于构造SafeParcelable的对象
     *
     * @param <T>
     *            类名
     */
    public interface Creator<T> {

        /**
         * 从SafeParcel中构造SafeParcelable的对象
         * 
         * @param source
         *            SafeParcelable对象原始数据的来源
         * @return SafeParcelable的对象
         */
        public T createFromParcel(SafeParcel source);

        /**
         * 构造空的SafeParcelable数组
         * 
         * @param size
         *            数组大小
         * @return SafeParcelable数组
         */
        public T[] newArray(int size);
    }

    /**
     * 指定类加载器的{@link Creator}
     *
     * @param <T>
     *            类名
     */
    public interface ClassLoaderCreator<T> extends Creator<T> {
        /**
         * 使用ClassLoader从SafeParcel中构造SafeParcelable的对象
         * 
         * @param source
         *            SafeParcelable对象原始数据的来源
         * @param loader
         *            类加载器
         * @return SafeParcelable的对象
         */
        public T createFromParcel(SafeParcel source, ClassLoader loader);
    }
}
