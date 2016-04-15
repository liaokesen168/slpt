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

package com.ingenic.iwds.utils.serializable;

/**
 * {@link com.ingenic.iwds.utils.serializable.ByteArrayUtils ByteArrayUtils} 使用
 * 到的常量集合类.
 */
public final class UtilsConstants {

    /**
     * 描述 {@code String} 类型的常量
     */
    public static final byte VAL_STRING = 0;

    /**
     * 描述 {@code Integer} 类型的常量
     */
    public static final byte VAL_INTEGER = 1;

    /**
     * 描述 {@code Map} 类型的常量
     */
    public static final byte VAL_MAP = 2;

    /**
     * 描述 {@code SafeParcelable} 类型的常量
     */
    public static final byte VAL_SAFEPARCELABLE = 3;

    /**
     * 描述 {@code Parcelable} 类型的常量
     */
    public static final byte VAL_PARCELABLE = 4;

    /**
     * 描述 {@code Short} 类型的常量
     */
    public static final byte VAL_SHORT = 5;

    /**
     * 描述 {@code Long} 类型的常量
     */
    public static final byte VAL_LONG = 6;

    /**
     * 描述 {@code FLoat} 类型的常量
     */
    public static final byte VAL_FLOAT = 7;

    /**
     * 描述 {@code Double} 类型的常量
     */
    public static final byte VAL_DOUBLE = 8;

    /**
     * 描述 {@code Boolean} 类型的常量
     */
    public static final byte VAL_BOOLEAN = 9;

    /**
     * 描述 {@code CharSequence} 类型的常量
     */
    public static final byte VAL_CHARSEQUENCE = 10;

    /**
     * 描述 {@code List} 类型的常量
     */
    public static final byte VAL_LIST = 11;

    /**
     * 描述 {@code SparseArray} 类型的常量
     */
    public static final byte VAL_SPARSEARRAY = 12;

    /**
     * 描述 {@code Byte} 数组类型的常量
     */
    public static final byte VAL_BYTEARRAY = 13;

    /**
     * 描述 {@code String} 数组类型的常量
     */
    public static final byte VAL_STRINGARRAY = 14;

    /**
     * 描述 {@code SafeParcelable} 数组类型的常量
     */
    public static final byte VAL_SAFEPARCELABLEARRAY = 15;

    /**
     * 描述 {@code Parcelable} 数组类型的常量
     */
    public static final byte VAL_PARCELABLEARRAY = 16;

    /**
     * 描述 {@code Object} 数组类型的常量
     */
    public static final byte VAL_OBJECTARRAY = 17;

    /**
     * 描述 {@code Integer} 数组类型的常量
     */
    public static final byte VAL_INTARRAY = 18;

    /**
     * 描述 {@code Long} 数组类型的常量
     */
    public static final byte VAL_LONGARRAY = 19;

    /**
     * 描述 {@code Byte} 类型的常量
     */
    public static final byte VAL_BYTE = 20;

    /**
     * 描述 {@code Serializable} 类型的常量
     */
    public static final byte VAL_SERIALIZABLE = 21;

    /**
     * 描述 {@code SparseBooleanArray} 类型的常量
     */
    public static final byte VAL_SPARSEBOOLEANARRAY = 22;

    /**
     * 描述 {@code Boolean} 数组类型的常量
     */
    public static final byte VAL_BOOLEANARRAY = 23;

    /**
     * 描述 {@code CharSequence} 数组类型的常量
     */
    public static final byte VAL_CHARSEQUENCEARRAY = 24;

    /**
     * 描述 {@code Character} 类型的常量
     */
    public static final byte VAL_CHAR = 25;

    /**
     * 描述 {@code Short} 数组类型的常量
     */
    public static final byte VAL_SHORTARRAY = 26;

    /**
     * 描述 {@code Float} 数组类型的常量
     */
    public static final byte VAL_FLOATARRAY = 27;

    /**
     * 描述 {@code Double} 数组类型的常量
     */
    public static final byte VAL_DOUBLEARRAY = 28;

    /**
     * 描述 {@code Character} 数组类型的常量
     */
    public static final byte VAL_CHARARRAY = 29;

    /**
     * 描述 {@code File} 类型的常量
     */
    public static final byte VAL_FILE = 30;

    /**
     * 描述 {@code String} 编码方式的常量
     */
    public static final String CHARSET_ENCODE = "UTF-8";

    /**
     * 描述数据类型大小等常量的集合类
     */
    public class SizeOf {

        /**
         * 描述数据类型编码的长度的常量, 单位(字节)
         */
        public final static int Type = 1;

        /**
         * 描述 {@code Byte} 类型的长度的常量, 单位(字节)
         */
        public final static int Byte = 1;

        /**
         * 描述 {@code Boolean} 类型的长度的常量, 单位(字节)
         */
        public final static int Boolean = 1;

        /**
         * 描述 {@code Character} 类型的长度的常量, 单位(字节)
         */
        public final static int Char = 2;

        /**
         * 描述 {@code Short} 类型的长度的常量, 单位(字节)
         */
        public final static int Short = 2;

        /**
         * 描述 {@code Integer} 类型的长度的常量, 单位(字节)
         */
        public final static int Int = 4;

        /**
         * 描述 {@code Long} 类型的长度的常量, 单位(字节)
         */
        public final static int Long = 8;

        /**
         * 描述 {@code Float} 类型的长度的常量, 单位(字节)
         */
        public final static int Float = 4;

        /**
         * 描述 {@code Double} 类型的长度的常量, 单位(字节)
         */
        public final static int Double = 8;

        /**
         * 描述文件传输块大小的常量, 单位(字节)
         */
        public final static int FileChunk = 64 * 1024;
    }

    /**
     * 默认文件接收目录
     */
    public static final String DEFAULT_STORE_SUBDIR = "/iwds";
}
