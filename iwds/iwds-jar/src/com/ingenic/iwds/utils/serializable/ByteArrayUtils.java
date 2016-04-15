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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.StatFs;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;

import com.ingenic.iwds.common.exception.FileTransferException;
import com.ingenic.iwds.common.exception.IwdsException;
import com.ingenic.iwds.datatransactor.FileTransferErrorCode;
import com.ingenic.iwds.os.SafeParcel;
import com.ingenic.iwds.os.SafeParcelable;
import com.ingenic.iwds.uniconnect.Connection;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;
import com.ingenic.iwds.utils.serializable.TransferAdapter.TransferAdapterCallback;

/**
 * 通用对象 {@code Object} 串行序列化工具类, 其中{@link ByteArrayUtils#encode(Object, Parcelable.Creator)}
 * 将一个通用对象 {@code Object} 转化成字节数组 {@code byte[]}, 用于 {@code connection} 传输,
 * {@link ByteArrayUtils#decode(Connection, Parcelable.Creator, TransferAdapterCallback)}
 * 用于从 {@code connection} 中解析出一个通用对象{@code Object}. 注意
 * {@link ByteArrayUtils#encode(Object, Parcelable.Creator)}, 和
 * {@link ByteArrayUtils#decode(Connection, Parcelable.Creator, TransferAdapterCallback)}
 * 方法都将阻塞执行. 支持序列化/反序列化的数据类型如下:
 * <li> String
 * <li> Byte
 * <li> Character
 * <li> Short
 * <li> Integer
 * <li> Long
 * <li> Float
 * <li> Double
 * <li> Boolean
 * <li> String 数组
 * <li> Boolean 数组
 * <li> Byte 数组
 * <li> Integer 数组
 * <li> Character 数组
 * <li> Short 数组
 * <li> Long 数组
 * <li> Float 数组
 * <li> Double 数组
 * <li> Object 数组
 * <li> Map
 * <li> 任何实现了 {@link com.ingenic.iwds.os.SafeParcelable} 接口的对象
 * <li> 任何实现了 {@link com.ingenic.iwds.os.SafeParcelable} 接口对象的数组
 * <li> 任何实现了 {@link android.os.Parcelable} 接口的对象
 * <li> 任何实现了 {@link android.os.Parcelable} 接口对象的数组
 * <li> CharSequence
 * <li> CharSequence 数组
 * <li> List
 * <li> SparseArray
 * <li> SparseBooleanArray
 * <li> 任何实现了 {@link java.io.Serializable} 接口的对象</ul>
 *
 * @see TransferAdapterCallback
 */
public final class ByteArrayUtils {
    private final static String TAG = "ByteArrayUtils";
    private final static boolean DEBUG = false;

    private static String sDesiredStoragePath = null;

    private static String typeToString(int type) {
        switch (type) {
        case UtilsConstants.VAL_STRING:
            return "String";
        case UtilsConstants.VAL_INTEGER:
            return "Integer";
        case UtilsConstants.VAL_MAP:
            return "Map";
        case UtilsConstants.VAL_SAFEPARCELABLE:
            return "SafeParcelable";
        case UtilsConstants.VAL_PARCELABLE:
            return "Parcelable";
        case UtilsConstants.VAL_SHORT:
            return "Short";
        case UtilsConstants.VAL_LONG:
            return "Long";
        case UtilsConstants.VAL_FLOAT:
            return "Float";
        case UtilsConstants.VAL_DOUBLE:
            return "Double";
        case UtilsConstants.VAL_BOOLEAN:
            return "Boolean";
        case UtilsConstants.VAL_CHARSEQUENCE:
            return "CharSequence";
        case UtilsConstants.VAL_LIST:
            return "List";
        case UtilsConstants.VAL_SPARSEARRAY:
            return "SparseArray";
        case UtilsConstants.VAL_BYTEARRAY:
            return "Byte Array";
        case UtilsConstants.VAL_STRINGARRAY:
            return "String Array";
        case UtilsConstants.VAL_SAFEPARCELABLEARRAY:
            return "SafeParcelable Array";
        case UtilsConstants.VAL_PARCELABLEARRAY:
            return "Parcelable Array";
        case UtilsConstants.VAL_OBJECTARRAY:
            return "Object Array";
        case UtilsConstants.VAL_INTARRAY:
            return "Integer Array";
        case UtilsConstants.VAL_LONGARRAY:
            return "Long Array";
        case UtilsConstants.VAL_BYTE:
            return "Byte";
        case UtilsConstants.VAL_SERIALIZABLE:
            return "Serializable";
        case UtilsConstants.VAL_SPARSEBOOLEANARRAY:
            return "SparseBooleanArray";
        case UtilsConstants.VAL_BOOLEANARRAY:
            return "Boolean Array";
        case UtilsConstants.VAL_CHARSEQUENCEARRAY:
            return "CharSeqenceArray";
        case UtilsConstants.VAL_CHAR:
            return "Character";
        case UtilsConstants.VAL_SHORTARRAY:
            return "Short Array";
        case UtilsConstants.VAL_FLOATARRAY:
            return "Float Array";
        case UtilsConstants.VAL_DOUBLEARRAY:
            return "Double Array";
        case UtilsConstants.VAL_CHARARRAY:
            return "Character Array";
        case UtilsConstants.VAL_FILE:
            return "File";
        default:
            Log.e(TAG, "Unknown type.");
            return "Unknown type";
        }
    }

    /* Can the buffer handle @length more bytes, if not expand it */
    private static byte[] expand(int pos, int length, byte[] buffer) {
        byte[] newBuffer = null;
        int newSize = pos + length;

        if (buffer != null && newSize <= buffer.length) {
            return buffer;
        }

        newBuffer = new byte[newSize];
        if (buffer != null)
            System.arraycopy(buffer, 0, newBuffer, 0, pos);

        return newBuffer;
    }

    private static int writeByte(byte[] buffer, int offset, byte value) {
        buffer[offset++] = value;
        return offset;
    }

    private static int writeBoolean(byte[] buffer, int offset, boolean value) {
        if (value) {
            buffer[offset++] = (byte) 1;
        } else {
            buffer[offset++] = (byte) 0;
        }
        return offset;
    }

    private static int writeInt(byte[] buffer, int offset, int value) {
        buffer[offset++] = (byte) ((value & 0xff000000) >> 24);
        buffer[offset++] = (byte) ((value & 0x00ff0000) >> 16);
        buffer[offset++] = (byte) ((value & 0x0000ff00) >> 8);
        buffer[offset++] = (byte) ((value & 0x000000ff) >> 0);
        return offset;
    }

    private static int writeShort(byte[] buffer, int offset, short value) {
        buffer[offset++] = (byte) ((value & 0xff00) >> 8);
        buffer[offset++] = (byte) ((value & 0x00ff) >> 0);
        return offset;
    }

    private static int writeLong(byte[] buffer, int offset, long value) {
        buffer[offset++] = (byte) ((value & 0xff00000000000000L) >> 56);
        buffer[offset++] = (byte) ((value & 0x00ff000000000000L) >> 48);
        buffer[offset++] = (byte) ((value & 0x0000ff0000000000L) >> 40);
        buffer[offset++] = (byte) ((value & 0x000000ff00000000L) >> 32);
        buffer[offset++] = (byte) ((value & 0x00000000ff000000L) >> 24);
        buffer[offset++] = (byte) ((value & 0x0000000000ff0000L) >> 16);
        buffer[offset++] = (byte) ((value & 0x000000000000ff00L) >> 8);
        buffer[offset++] = (byte) ((value & 0x00000000000000ffL) >> 0);
        return offset;
    }

    private static int writeFloat(byte[] buffer, int offset, float value) {
        return writeInt(buffer, offset, Float.floatToIntBits(value));
    }

    private static int writeDouble(byte[] buffer, int offset, double value) {
        return writeLong(buffer, offset, Double.doubleToLongBits(value));
    }

    private static int writeChar(byte[] buffer, int offset, char value) {
        buffer[offset++] = (byte) ((value & 0xff00) >> 8);
        buffer[offset++] = (byte) ((value & 0x00ff) >> 0);
        return offset;
    }

    private static byte[] writeString(byte[] buffer, int offset, String value) {
        byte[] buf = null;
        int length = 0;

        /* Read raw bytes of string */
        buf = value.getBytes(Charset.forName(UtilsConstants.CHARSET_ENCODE));
        /* Read raw bytes length */
        length = buf.length;

        buffer = expand(offset, length + UtilsConstants.SizeOf.Int, buffer);
        /* Write string length into buffer */
        offset = writeInt(buffer, offset, length);
        System.arraycopy(buf, 0, buffer, offset, length);
        offset += length;

        return buffer;
    }

    private static byte[] writeCharSequence(byte[] buffer, int offset,
            CharSequence value) {
        byte[] buf = null;
        int length = 0;

        /* Read raw bytes of char sequence */
        buf = value.toString().getBytes(
                Charset.forName(UtilsConstants.CHARSET_ENCODE));
        /* Read raw bytes length */
        length = buf.length;
        buffer = expand(offset, length + UtilsConstants.SizeOf.Int, buffer);
        /* Write char sequence length into buffer */
        offset = writeInt(buffer, offset, length);
        System.arraycopy(buf, 0, buffer, offset, length);
        offset += length;

        return buffer;
    }

    private static <T extends Parcelable> byte[] writeParcelable(byte[] buffer, int offset,
            Parcelable parcelable, Parcelable.Creator<T> creator) {
        int pos = offset;
        /* Obtain a parcel object from pool */
        Parcel parcel = Parcel.obtain();
        /* Call back parcelable interface write object into parcel */
        parcelable.writeToParcel(parcel, 0);
        /* Read the raw bytes of the parcel */
        byte[] rawBytes = parcel.marshall();
        /* Put Parcel object back into the pool */
        parcel.recycle();

        if (creator == null) {
            /*
             * Write parcelable class name into buffer Note: The buffer will be
             * realloc, the reference of buffer will changed, return the new
             * reference of the new buffer
             */
            buffer = writeString(buffer, pos, parcelable.getClass().getName());
            pos = buffer.length;
        }

        buffer = expand(pos, UtilsConstants.SizeOf.Int, buffer);
        /* Write raw bytes length into buffer */
        pos = writeInt(buffer, pos, rawBytes.length);

        buffer = expand(pos, rawBytes.length, buffer);
        System.arraycopy(rawBytes, 0, buffer, pos, rawBytes.length);
        pos += rawBytes.length;

        return buffer;
    }

    private static <T extends SafeParcelable> byte[] writeSafeParcelable(byte[] buffer, int offset,
            SafeParcelable safeParcelable, SafeParcelable.Creator<T> creator) {
        int pos = offset;
        /* Obtain a safe-parcel object from pool */
        SafeParcel parcel = SafeParcel.obtain();
        /* Call back safe-parcelable interface write object into safe-parcel */
        safeParcelable.writeToParcel(parcel, 0);
        /* Read the raw bytes of the safe-parcel */
        byte[] rawBytes = parcel.marshall();
        /* Put safe-parcel object back into the pool */
        parcel.recycle();

        if (creator == null) {
            /*
             * Write safe-parcelable class name into buffer Note: The buffer will be
             * realloc, the reference of buffer will changed, return the new
             * reference of the new buffer
             */
            buffer = writeString(buffer, pos, safeParcelable.getClass().getName());
            pos = buffer.length;
        }

        buffer = expand(pos, UtilsConstants.SizeOf.Int, buffer);
        /* Write raw bytes length into buffer */
        pos = writeInt(buffer, pos, rawBytes.length);

        buffer = expand(pos, rawBytes.length, buffer);
        System.arraycopy(rawBytes, 0, buffer, pos, rawBytes.length);
        pos += rawBytes.length;

        return buffer;
    }

    private static byte[] encodeByte(byte value) {
        int pos = 0;
        byte[] buffer = new byte[UtilsConstants.SizeOf.Type
                + UtilsConstants.SizeOf.Byte];

        pos = writeByte(buffer, pos, UtilsConstants.VAL_BYTE);
        pos = writeByte(buffer, pos, value);
        return buffer;
    }

    private static byte[] encodeBoolean(boolean value) {
        int pos = 0;
        byte[] buffer = new byte[UtilsConstants.SizeOf.Type
                + UtilsConstants.SizeOf.Boolean];

        pos = writeByte(buffer, pos, UtilsConstants.VAL_BOOLEAN);
        pos = writeBoolean(buffer, pos, value);
        return buffer;
    }

    private static byte[] encodeShort(short value) {
        int pos = 0;
        byte[] buffer = new byte[UtilsConstants.SizeOf.Type
                + UtilsConstants.SizeOf.Short];

        pos = writeByte(buffer, pos, UtilsConstants.VAL_SHORT);
        pos = writeShort(buffer, pos, value);
        return buffer;
    }

    private static byte[] encodeInt(int value) {
        int pos = 0;
        byte[] buffer = new byte[UtilsConstants.SizeOf.Type
                + UtilsConstants.SizeOf.Int];

        pos = writeByte(buffer, pos, UtilsConstants.VAL_INTEGER);
        pos = writeInt(buffer, pos, value);
        return buffer;
    }

    private static byte[] encodeLong(long value) {
        int pos = 0;
        byte[] buffer = new byte[UtilsConstants.SizeOf.Type
                + UtilsConstants.SizeOf.Long];

        pos = writeByte(buffer, pos, UtilsConstants.VAL_LONG);
        pos = writeLong(buffer, pos, value);
        return buffer;
    }

    private static byte[] encodeFloat(float value) {
        int pos = 0;
        byte[] buffer = new byte[UtilsConstants.SizeOf.Type
                + UtilsConstants.SizeOf.Float];

        pos = writeByte(buffer, pos, UtilsConstants.VAL_FLOAT);
        pos = writeFloat(buffer, pos, value);
        return buffer;
    }

    private static byte[] encodeDouble(double value) {
        int pos = 0;
        byte[] buffer = new byte[UtilsConstants.SizeOf.Type
                + UtilsConstants.SizeOf.Double];

        pos = writeByte(buffer, pos, UtilsConstants.VAL_DOUBLE);
        pos = writeDouble(buffer, pos, value);
        return buffer;
    }

    private static byte[] encodeChar(char value) {
        int pos = 0;
        byte[] buffer = new byte[UtilsConstants.SizeOf.Type
                + UtilsConstants.SizeOf.Char];

        pos = writeByte(buffer, pos, UtilsConstants.VAL_CHAR);
        pos = writeChar(buffer, pos, value);
        return buffer;
    }

    private static byte[] encodeString(String value) {
        int pos = 0;
        byte[] buffer = new byte[UtilsConstants.SizeOf.Type];

        /* Write value type into buffer */
        pos = writeByte(buffer, pos, UtilsConstants.VAL_STRING);

        /*
         * Write raws bytes into buffer Note: The buffer will be realloc, the
         * reference of buffer will changed, return the new reference of the new
         * buffer
         */
        buffer = writeString(buffer, pos, value);

        return buffer;
    }

    private static <T extends Parcelable> byte[] encodeParcelable(Parcelable parcelable, Parcelable.Creator<T> creator) {
        int pos = 0;
        byte[] buffer = new byte[UtilsConstants.SizeOf.Type];
        /* Write value type into buffer */
        pos = writeByte(buffer, pos, UtilsConstants.VAL_PARCELABLE);

        /*
         * Write raws bytes into buffer Note: The buffer will be realloc, the
         * reference of buffer will changed, return the new reference of the new
         * buffer
         */
        buffer = writeParcelable(buffer, pos, parcelable, creator);

        return buffer;
    }

    private static <T extends SafeParcelable> byte[] encodeSafeParcelable(SafeParcelable safeParcelable, SafeParcelable.Creator<T> creator) {
        int pos = 0;
        byte[] buffer = new byte[UtilsConstants.SizeOf.Type];
        /* Write value type into buffer */
        pos = writeByte(buffer, pos, UtilsConstants.VAL_SAFEPARCELABLE);

        /*
         * Write raws bytes into buffer Note: The buffer will be realloc, the
         * reference of buffer will changed, return the new reference of the new
         * buffer
         */
        buffer = writeSafeParcelable(buffer, pos, safeParcelable, creator);

        return buffer;
    }

    private static byte[] encodeByteArray(byte[] values) {
        int pos = 0;
        int N = values.length;
        byte[] buffer = new byte[N * UtilsConstants.SizeOf.Byte
                + UtilsConstants.SizeOf.Type + UtilsConstants.SizeOf.Int];

        /* Write value type into buffer */
        pos = writeByte(buffer, pos, UtilsConstants.VAL_BYTEARRAY);
        /* Write array length into buffer */
        pos = writeInt(buffer, pos, N);

        /* Write raw bytes into buffer */
        for (byte value : values) {
            pos = writeByte(buffer, pos, value);
        }

        return buffer;
    }

    private static byte[] encodeBooleanArray(boolean[] values) {
        int pos = 0;
        int N = values.length;
        byte[] buffer = new byte[N * UtilsConstants.SizeOf.Boolean
                + UtilsConstants.SizeOf.Type + UtilsConstants.SizeOf.Int];

        /* Write value type into buffer */
        pos = writeByte(buffer, pos, UtilsConstants.VAL_BOOLEANARRAY);
        /* Write array length into buffer */
        pos = writeInt(buffer, pos, N);

        /* Write raw bytes into buffer */
        for (boolean value : values) {
            pos = writeBoolean(buffer, pos, value);
        }

        return buffer;
    }

    private static byte[] encodeShortArray(short[] values) {
        int pos = 0;
        int N = values.length;
        byte[] buffer = new byte[N * UtilsConstants.SizeOf.Short
                + UtilsConstants.SizeOf.Type + UtilsConstants.SizeOf.Int];

        /* Write value type into buffer */
        pos = writeByte(buffer, pos, UtilsConstants.VAL_SHORTARRAY);
        /* Write array length into buffer */
        pos = writeInt(buffer, pos, N);

        /* Write raw bytes into buffer */
        for (short value : values) {
            pos = writeShort(buffer, pos, value);
        }
        return buffer;
    }

    private static byte[] encodeIntArray(int[] values) {
        int pos = 0;
        int N = values.length;
        byte[] buffer = new byte[N * UtilsConstants.SizeOf.Int
                + UtilsConstants.SizeOf.Type + UtilsConstants.SizeOf.Int];

        /* Write value type into buffer */
        pos = writeByte(buffer, pos, UtilsConstants.VAL_INTARRAY);
        /* Write array length into buffer */
        pos = writeInt(buffer, pos, N);

        /* Write raw bytes into buffer */
        for (int value : values) {
            pos = writeInt(buffer, pos, value);
        }
        return buffer;
    }

    private static byte[] encodeLongArray(long[] values) {
        int pos = 0;
        int N = values.length;
        byte[] buffer = new byte[N * UtilsConstants.SizeOf.Long
                + UtilsConstants.SizeOf.Type + UtilsConstants.SizeOf.Int];

        /* Write value type into buffer */
        pos = writeByte(buffer, pos, UtilsConstants.VAL_LONGARRAY);
        /* Write array length into buffer */
        pos = writeInt(buffer, pos, N);

        /* Write raw bytes into buffer */
        for (long value : values) {
            pos = writeLong(buffer, pos, value);
        }
        return buffer;
    }

    private static byte[] encodeFloatArray(float[] values) {
        int pos = 0;
        int N = values.length;
        byte[] buffer = new byte[N * UtilsConstants.SizeOf.Float
                + UtilsConstants.SizeOf.Type + UtilsConstants.SizeOf.Int];

        /* Write value type info buffer */
        pos = writeByte(buffer, pos, UtilsConstants.VAL_FLOATARRAY);
        /* Write array length into buffer */
        pos = writeInt(buffer, pos, N);

        /* Write raw bytes into buffer */
        for (float value : values) {
            pos = writeFloat(buffer, pos, value);
        }
        return buffer;
    }

    private static byte[] encodeDoubleArray(double[] values) {
        int pos = 0;
        int N = values.length;
        byte[] buffer = new byte[N * UtilsConstants.SizeOf.Double
                + UtilsConstants.SizeOf.Type + UtilsConstants.SizeOf.Int];

        /* Write type value into buffer */
        pos = writeByte(buffer, pos, UtilsConstants.VAL_DOUBLEARRAY);
        /* Write array length into buffer */
        pos = writeInt(buffer, pos, N);

        /* Write raw bytes into buffer */
        for (double value : values) {
            pos = writeDouble(buffer, pos, value);
        }
        return buffer;
    }

    private static byte[] encodeCharArray(char[] values) {
        int pos = 0;
        int N = values.length;
        byte[] buffer = new byte[N * UtilsConstants.SizeOf.Char
                + UtilsConstants.SizeOf.Type + UtilsConstants.SizeOf.Int];

        /* Write value type into buffer */
        pos = writeByte(buffer, pos, UtilsConstants.VAL_CHARARRAY);
        /* Write array length into buffer */
        pos = writeInt(buffer, pos, N);

        /* Read raw bytes into buffer */
        for (char value : values) {
            pos = writeChar(buffer, pos, value);
        }
        return buffer;
    }

    private static byte[] encodeStringArray(String[] values) {
        int pos = 0;
        int N = values.length;
        byte[] buffer = new byte[UtilsConstants.SizeOf.Type
                + UtilsConstants.SizeOf.Int];

        /* Write value type into buffer */
        pos = writeByte(buffer, pos, UtilsConstants.VAL_STRINGARRAY);
        /* Write array length into buffer */
        pos = writeInt(buffer, pos, N);

        /* Write raw bytes into buffer */
        for (String value : values) {
            /*
             * Note: The buffer will be realloc, the reference of buffer will
             * changed, return the new reference of the new buffer
             */
            buffer = writeString(buffer, pos, value);
            pos = buffer.length;
        }
        return buffer;
    }

    private static byte[] encodeMap(Map value) {
        int pos = 0;
        byte[] buffer = new byte[UtilsConstants.SizeOf.Type
                + UtilsConstants.SizeOf.Int];
        Set<Map.Entry<String, Object>> entries = value.entrySet();
        int N = entries.size();

        /* Write value type into buffer */
        pos = writeByte(buffer, pos, UtilsConstants.VAL_MAP);
        /* Write map entry size into buffer */
        pos = writeInt(buffer, pos, N);

        byte[] keyBuf = null;
        byte[] valueBuf = null;
        for (Map.Entry<String, Object> e : entries) {
            /* Read entry key raw bytes */
            keyBuf = encode(e.getKey(), null, null);
            buffer = expand(pos, keyBuf.length, buffer);
            System.arraycopy(keyBuf, 0, buffer, pos, keyBuf.length);
            pos += keyBuf.length;

            /* Read entry value raw bytes */
            valueBuf = encode(e.getValue(), null, null);
            buffer = expand(pos, valueBuf.length, buffer);
            System.arraycopy(valueBuf, 0, buffer, pos, valueBuf.length);
            pos += valueBuf.length;
        }

        return buffer;
    }

    private static byte[] encodeList(List value) {
        int pos = 0;
        int N = value.size();
        byte[] buffer = new byte[UtilsConstants.SizeOf.Type
                + UtilsConstants.SizeOf.Int];

        /* Write value type into buffer */
        pos = writeByte(buffer, pos, UtilsConstants.VAL_LIST);
        /* Write list size into buffer */
        pos = writeInt(buffer, pos, N);

        byte[] valueBuf = null;
        for (int i = 0; i < N; i++) {
            /* Read list value raw bytes */
            valueBuf = encode(value.get(i), null, null);
            buffer = expand(pos, valueBuf.length, buffer);
            System.arraycopy(valueBuf, 0, buffer, pos, valueBuf.length);
            pos += valueBuf.length;
        }

        return buffer;
    }

    private static byte[] encodeArray(Object[] values) {
        int pos = 0;
        int N = values.length;
        byte[] buffer = new byte[UtilsConstants.SizeOf.Type
                + UtilsConstants.SizeOf.Int];

        /* Write value type into buffer */
        pos = writeByte(buffer, pos, UtilsConstants.VAL_OBJECTARRAY);
        /* Write array length into buffer */
        pos = writeInt(buffer, pos, N);

        byte[] valueBuf = null;
        /* Write raw bytes into buffer */
        for (Object value : values) {
            /* Read raw bytes of object */
            valueBuf = encode(value, null, null);
            buffer = expand(pos, valueBuf.length, buffer);
            System.arraycopy(valueBuf, 0, buffer, pos, valueBuf.length);
            pos += valueBuf.length;
        }

        return buffer;
    }

    private static byte[] encodeCharSequence(CharSequence value) {
        int pos = 0;
        byte[] buffer = new byte[UtilsConstants.SizeOf.Type];

        /* Write value type into buffer */
        pos = writeByte(buffer, pos, UtilsConstants.VAL_CHARSEQUENCE);

        /*
         * Write raws bytes into buffer Note: The buffer will be realloc, the
         * reference of buffer will changed, return the new reference of the new
         * buffer
         */
        buffer = writeCharSequence(buffer, pos, value);
        pos = buffer.length;

        return buffer;
    }

    private static byte[] encodeCharSequenceArray(CharSequence[] values) {
        int pos = 0;
        int N = values.length;
        byte[] buffer = new byte[UtilsConstants.SizeOf.Type
                + UtilsConstants.SizeOf.Int];

        /* Write value type into buffer */
        pos = writeByte(buffer, pos, UtilsConstants.VAL_CHARSEQUENCEARRAY);
        /* Write array length into buffer */
        pos = writeInt(buffer, pos, N);

        /* Write raw bytes into buffer */
        for (CharSequence value : values) {
            /*
             * Note: The buffer will be realloc, the reference of buffer will
             * changed, return the new reference of the new buffer
             */
            buffer = writeCharSequence(buffer, pos, value);
            pos = buffer.length;
        }

        return buffer;
    }

    private static byte[] encodeSerializable(Serializable value) {
        byte[] buffer = new byte[UtilsConstants.SizeOf.Type];
        int pos = 0;

        /* WWrite value type into buffer */
        pos = writeByte(buffer, pos, UtilsConstants.VAL_SERIALIZABLE);

        /* Write class name into buffer */
        String name = value.getClass().getName();
        /*
         * Note: The buffer will be realloc, the reference of buffer will
         * changed, return the new reference of the new buffer
         */
        buffer = writeString(buffer, pos, name);
        pos = buffer.length;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            oos.close();
            /* Read value raw bytes */
            byte[] valueBuf = baos.toByteArray();
            buffer = expand(pos, valueBuf.length + UtilsConstants.SizeOf.Int,
                    buffer);
            /* Write raw bytes length into buffer */
            pos = writeInt(buffer, pos, valueBuf.length);
            System.arraycopy(valueBuf, 0, buffer, pos, valueBuf.length);
            pos += valueBuf.length;
        } catch (IOException e) {
            IwdsAssert.dieIf(TAG, true, "Parcelable encountered "
                    + "IOException writing serializable object (name = " + name
                    + ")");
            e.printStackTrace();
        }

        return buffer;
    }

    private static byte[] encodeSparseArray(SparseArray values) {
        int pos = 0;
        int N = values.size();
        byte buffer[] = new byte[UtilsConstants.SizeOf.Type
                + UtilsConstants.SizeOf.Int];

        /* Write value type into buffer */
        pos = writeByte(buffer, pos, UtilsConstants.VAL_SPARSEARRAY);
        /* Write array length into buffer */
        pos = writeInt(buffer, pos, N);

        byte[] valueBuf = null;
        for (int i = 0; i < N; i++) {
            /* Write entry key into buffer */
            buffer = expand(pos, UtilsConstants.SizeOf.Int, buffer);
            pos = writeInt(buffer, pos, values.keyAt(i));

            /* Read entry value into buffer */
            valueBuf = encode(values.valueAt(i), null, null);
            buffer = expand(pos, valueBuf.length, buffer);
            System.arraycopy(valueBuf, 0, buffer, pos, valueBuf.length);
            pos += valueBuf.length;
        }

        return buffer;
    }

    private static byte[] encodeSparseBooleanArray(SparseBooleanArray values) {
        int pos = 0;
        int N = values.size();
        byte[] buffer = new byte[UtilsConstants.SizeOf.Type
                + UtilsConstants.SizeOf.Int + N
                * (UtilsConstants.SizeOf.Int + UtilsConstants.SizeOf.Boolean)];

        /* Write value type into buffer */
        pos = writeByte(buffer, pos, UtilsConstants.VAL_SPARSEBOOLEANARRAY);
        /* Write array length into buffer */
        pos = writeInt(buffer, pos, N);

        for (int i = 0; i < N; i++) {
            /* Write entry key into buffer */
            pos = writeInt(buffer, pos, values.keyAt(i));
            /* Write entry value into buffer */
            pos = writeBoolean(buffer, pos, values.valueAt(i));
        }

        return buffer;
    }

    private static byte[] encodeParcelableArray(Parcelable[] values) {
        int pos = 0;
        int N = values.length;
        byte[] buffer = new byte[UtilsConstants.SizeOf.Type
                + UtilsConstants.SizeOf.Int];

        /* Write value type into buffer */
        pos = writeByte(buffer, pos, UtilsConstants.VAL_PARCELABLEARRAY);
        /* Write array length into buffer */
        pos = writeInt(buffer, pos, N);

        /* Write raw bytes into buffer */
        for (Parcelable value : values) {
            /*
             * Write raws bytes into buffer Note: The buffer will be realloc,
             * the reference of buffer will changed, return the new reference of
             * the new buffer
             */
            buffer = writeParcelable(buffer, pos, value, null);
            pos = buffer.length;
        }

        return buffer;
    }

    private static byte[] encodeSafeParcelableArray(SafeParcelable[] values) {
        int pos = 0;
        int N = values.length;
        byte[] buffer = new byte[UtilsConstants.SizeOf.Type
                + UtilsConstants.SizeOf.Int];

        /* Write value type into buffer */
        pos = writeByte(buffer, pos, UtilsConstants.VAL_SAFEPARCELABLEARRAY);
        /* Write array length into buffer */
        pos = writeInt(buffer, pos, N);

        /* Write raw bytes into buffer */
        for (SafeParcelable value : values) {
            /*
             * Write raws bytes into buffer Note: The buffer will be realloc,
             * the reference of buffer will changed, return the new reference of
             * the new buffer
             */
            buffer = writeSafeParcelable(buffer, pos, value, null);
            pos = buffer.length;
        }

        return buffer;
    }

    /**
     * 用于将一个通用对象 {@code Object} 转化成字节数组 {@code byte[]}, 该方法将会阻塞执行直到对象转化完成.
     *
     * @param v
     *        通用数据对象 {@code Object}
     * @param parcelableCreator
     *        实现 Parcelable 接口的类的静态接口 CREATOR
     * @param safeParcelableCreator
     *        实现 SafeParcelable 接口的类的静态接口 CREATOR
     * @return 字节数组 {@code byte[]} 
     */
    public static <T1 extends Parcelable, T2 extends SafeParcelable> byte[]
            encode(Object v, Parcelable.Creator<T1> parcelableCreator,
                    SafeParcelable.Creator<T2> safeParcelableCreator) {

        IwdsAssert.dieIf(TAG, v == null, "encode object is null");

        if (v instanceof String) {
            return encodeString((String) v);

        } else if (v instanceof Integer) {
            return encodeInt((Integer) v);

        } else if (v instanceof Map) {
            return encodeMap((Map) v);

        } else if (v instanceof Parcelable) {
            return encodeParcelable((Parcelable) v, parcelableCreator);

        } else if (v instanceof SafeParcelable) {
            return encodeSafeParcelable((SafeParcelable)v, safeParcelableCreator);

        } else if (v instanceof Short) {
            return encodeShort((Short) v);

        } else if (v instanceof Long) {
            return encodeLong((Long) v);

        } else if (v instanceof Float) {
            return encodeFloat((Float) v);

        } else if (v instanceof Double) {
            return encodeDouble((Double) v);

        } else if (v instanceof Boolean) {
            return encodeBoolean((Boolean) v);

        } else if (v instanceof CharSequence) {
            // Must be after String
            return encodeCharSequence((CharSequence) v);

        } else if (v instanceof List) {
            return encodeList((List) v);

        } else if (v instanceof SparseArray) {
            return encodeSparseArray((SparseArray) v);

        } else if (v instanceof boolean[]) {
            return encodeBooleanArray((boolean[]) v);

        } else if (v instanceof byte[]) {
            return encodeByteArray((byte[]) v);

        } else if (v instanceof String[]) {
            return encodeStringArray((String[]) v);

        } else if (v instanceof CharSequence[]) {
            // Must be after String[] and before Object[]
            return encodeCharSequenceArray((CharSequence[]) v);
        }

        else if (v instanceof SafeParcel[]) {
            return encodeSafeParcelableArray((SafeParcelable[]) v);

        } else if (v instanceof Parcelable[]) {
            return encodeParcelableArray((Parcelable[]) v);

        } else if (v instanceof int[]) {
            return encodeIntArray((int[]) v);

        } else if (v instanceof long[]) {
            return encodeLongArray((long[]) v);

        } else if (v instanceof Byte) {
            return encodeByte((Byte) v);

        } else if (v instanceof Character) {
            return encodeChar((Character) v);

        } else if (v instanceof char[]) {
            return encodeCharArray((char[]) v);

        } else if (v instanceof short[]) {
            return encodeShortArray((short[]) v);

        } else if (v instanceof float[]) {
            return encodeFloatArray((float[]) v);

        } else if (v instanceof double[]) {
            return encodeDoubleArray((double[]) v);

        } else if (v instanceof SparseBooleanArray) {
            return encodeSparseBooleanArray((SparseBooleanArray) v);

        } else if (v instanceof File) {
            IwdsAssert.dieIf(TAG, true, "Unsupport File object serialization");
            return null;

        } else {
            Class<?> clazz = v.getClass();
            if (clazz.isArray() && clazz.getComponentType() == Object.class) {
                return encodeArray((Object[]) v);

            } else if (v instanceof Serializable) {
                // Must be last
                return encodeSerializable((Serializable) v);

            } else {
                IwdsAssert.dieIf(TAG, true, "Unsupported object type: "
                        + v.getClass().getName());
                return null;
            }
        }
    }

    private static byte readByte(byte[] bytes) {
        return bytes[0];
    }

    private static boolean readBoolean(byte[] bytes) {
        if (bytes[0] == 1) {
            return true;
        } else {
            return false;
        }
    }

    private static short readShort(byte[] bytes) {
        short value = 0;
        value = (short) (((bytes[0] << 8) & 0xff00) | ((bytes[1] << 0) & 0x00ff));
        return value;
    }

    private static int readInt(byte[] bytes) {
        int value = 0;
        value = (int) (((bytes[0] << 24) & 0xff000000)
                | ((bytes[1] << 16) & 0x00ff0000)
                | ((bytes[2] << 8) & 0x0000ff00) | ((bytes[3] << 0) & 0x000000ff));
        return value;
    }

    private static long readLong(byte[] bytes) {
        long value = 0;
        value = (long) ((((long) bytes[0] << 56) & 0xff00000000000000L)
                | (((long) bytes[1] << 48) & 0x00ff000000000000L)
                | (((long) bytes[2] << 40) & 0x0000ff0000000000L)
                | (((long) bytes[3] << 32) & 0x000000ff00000000L)
                | (((long) bytes[4] << 24) & 0x00000000ff000000L)
                | (((long) bytes[5] << 16) & 0x0000000000ff0000L)
                | (((long) bytes[6] << 8) & 0x000000000000ff00L) | (((long) bytes[7] << 0) & 0x00000000000000ffL));
        return value;
    }

    private static float readFloat(byte[] bytes) {
        int bits = 0;
        bits = (int) (((bytes[0] << 24) & 0xff000000)
                | ((bytes[1] << 16) & 0x00ff0000)
                | ((bytes[2] << 8) & 0x0000ff00) | ((bytes[3] << 0) & 0x000000ff));
        return Float.intBitsToFloat(bits);
    }

    private static double readDouble(byte[] bytes) {
        long bits = 0;
        bits = (long) ((((long) bytes[0] << 56) & 0xff00000000000000L)
                | (((long) bytes[1] << 48) & 0x00ff000000000000L)
                | (((long) bytes[2] << 40) & 0x0000ff0000000000L)
                | (((long) bytes[3] << 32) & 0x000000ff00000000L)
                | (((long) bytes[4] << 24) & 0x00000000ff000000L)
                | (((long) bytes[5] << 16) & 0x0000000000ff0000L)
                | (((long) bytes[6] << 8) & 0x000000000000ff00L) | (((long) bytes[7] << 0) & 0x00000000000000ffL));
        return Double.longBitsToDouble(bits);
    }

    private static char readChar(byte[] bytes) {
        char value = 0;
        value = (char) (((bytes[0] << 8) & 0xff00) | ((bytes[1] << 0) & 0x00ff));
        return value;
    }

    private static String readString(byte[] bytes) {
        String value = new String(bytes,
                Charset.forName(UtilsConstants.CHARSET_ENCODE));
        return value;
    }

    private static <T extends Parcelable> Object readParcelable(byte[] buffer,
            Parcelable.Creator<T> creator) {

        IwdsAssert.dieIf(TAG, creator == null, "creator == null");

        /* Obtain a parcel object from pool */
        Parcel p = Parcel.obtain();
        /* Set the bytes in buffer to be raw bytes of parcel */
        p.unmarshall(buffer, 0, buffer.length);
        /* Relocation pointer of raw bytes in parcel */
        p.setDataPosition(0);
        /* Call back parcelable interface to create object from parcel */
        T obj = creator.createFromParcel(p);
        p.recycle();

        return obj;
    }

    private static <T extends SafeParcelable> Object readSafeParcelable(byte[] buffer,
            SafeParcelable.Creator<T> creator) {

        IwdsAssert.dieIf(TAG, creator == null, "creator == null");

        /* Obtain a safe-parcel object from pool */
        SafeParcel p = SafeParcel.obtain();
        /* Set the bytes in buffer to be raw bytes of safe-parcel */
        p.unmarshall(buffer, 0, buffer.length);
        /* Relocation pointer of raw bytes in safe-parcel */
        p.setDataPosition(0);
        /* Call back safe-parcelable interface to create object from parcel */
        T obj = creator.createFromParcel(p);
        p.recycle();

        return obj;
    }

    private static byte decodeByte(Connection connection) throws IOException {
        byte[] buffer = new byte[UtilsConstants.SizeOf.Byte];
        int pos = 0;
        int maxSize = buffer.length;
        InputStream is = connection.getInputStream();

        /* Read raw bytes from connection */
        while (maxSize > 0) {
            int readBytes = is.read(buffer, pos, maxSize);
            pos += readBytes;
            maxSize -= readBytes;
        }

        return readByte(buffer);
    }

    private static String decodeString(Connection connection)
            throws IOException {
        /* Read string length from connection */
        int length = decodeInt(connection);

        int pos = 0;
        byte[] buffer = new byte[length];
        InputStream is = connection.getInputStream();

        /* Read raw bytes from connection */
        while (length > 0) {
            int readBytes = is.read(buffer, pos, length);
            pos += readBytes;
            length -= readBytes;
        }

        return readString(buffer);
    }

    private static int decodeInt(Connection connection) throws IOException {
        byte[] buffer = new byte[UtilsConstants.SizeOf.Int];
        int pos = 0;
        int maxSize = buffer.length;
        InputStream is = connection.getInputStream();

        /* Read raw bytes from connection */
        while (maxSize > 0) {
            int readBytes = is.read(buffer, pos, maxSize);
            pos += readBytes;
            maxSize -= readBytes;
        }

        return readInt(buffer);
    }

    private static short decodeShort(Connection connection) throws IOException {
        byte[] buffer = new byte[UtilsConstants.SizeOf.Short];
        int pos = 0;
        int maxSize = buffer.length;
        InputStream is = connection.getInputStream();

        /* Read raw bytes from connection */
        while (maxSize > 0) {
            int readBytes = is.read(buffer, pos, maxSize);
            pos += readBytes;
            maxSize -= readBytes;
        }

        return readShort(buffer);
    }

    private static long decodeLong(Connection connection) throws IOException {
        byte[] buffer = new byte[UtilsConstants.SizeOf.Long];
        int pos = 0;
        int maxSize = buffer.length;
        InputStream is = connection.getInputStream();

        /* Read raw bytes from connection */
        while (maxSize > 0) {
            int readBytes = is.read(buffer, pos, maxSize);
            pos += readBytes;
            maxSize -= readBytes;
        }

        return readLong(buffer);
    }

    private static float decodeFloat(Connection connection) throws IOException {
        byte[] buffer = new byte[UtilsConstants.SizeOf.Float];
        int pos = 0;
        int maxSize = buffer.length;
        InputStream is = connection.getInputStream();

        /* Read raw bytes from connection */
        while (maxSize > 0) {
            int readBytes = is.read(buffer, pos, maxSize);
            pos += readBytes;
            maxSize -= readBytes;
        }

        return readFloat(buffer);
    }

    private static double decodeDouble(Connection connection)
            throws IOException {
        byte[] buffer = new byte[UtilsConstants.SizeOf.Double];
        int pos = 0;
        int maxSize = buffer.length;
        InputStream is = connection.getInputStream();

        /* Read raw bytes from connection */
        while (maxSize > 0) {
            int readBytes = is.read(buffer, pos, maxSize);
            pos += readBytes;
            maxSize -= readBytes;
        }

        return readDouble(buffer);
    }

    private static char decodeChar(Connection connection) throws IOException {
        byte[] buffer = new byte[UtilsConstants.SizeOf.Char];
        int pos = 0;
        int maxSize = buffer.length;
        InputStream is = connection.getInputStream();

        /* Read raw bytes from connection */
        while (maxSize > 0) {
            int readBytes = is.read(buffer, pos, maxSize);
            pos += readBytes;
            maxSize -= readBytes;
        }

        return readChar(buffer);
    }

    private static boolean decodeBoolean(Connection connection)
            throws IOException {
        byte[] buffer = new byte[UtilsConstants.SizeOf.Boolean];
        int pos = 0;
        int maxSize = buffer.length;
        InputStream is = connection.getInputStream();

        /* Read raw bytes from connection */
        while (maxSize > 0) {
            int readBytes = is.read(buffer, pos, maxSize);
            pos += readBytes;
            maxSize -= readBytes;
        }

        return readBoolean(buffer);
    }

    private static boolean[] decodeBooleanArray(Connection connection)
            throws IOException {
        /* Read array length from connection */
        int N = decodeInt(connection);
        boolean[] values = new boolean[N];

        for (int i = 0; i < N; i++) {
            values[i] = decodeBoolean(connection);
        }

        return values;
    }

    private static byte[] decodeByteArray(Connection connection)
            throws IOException {
        /* Read array length from connection */
        int N = decodeInt(connection);
        byte[] values = new byte[N];
        int pos = 0;
        InputStream is = connection.getInputStream();

        /* Read raw bytes from connection */
        while (N > 0) {
            int readBytes = is.read(values, pos, N);
            pos += readBytes;
            N -= readBytes;
        }

        return values;
    }

    private static String[] decodeStringArray(Connection connection)
            throws IOException {
        /* Read array length from connection */
        int N = decodeInt(connection);
        String[] values = new String[N];

        for (int i = 0; i < N; i++) {
            values[i] = decodeString(connection);
        }

        return values;
    }

    private static int[] decodeIntArray(Connection connection)
            throws IOException {
        /* Read array length from connection */
        int N = decodeInt(connection);
        int[] values = new int[N];

        for (int i = 0; i < N; i++) {
            values[i] = decodeInt(connection);
        }

        return values;
    }

    private static long[] decodeLongArray(Connection connection)
            throws IOException {
        /* Read array length from connection */
        int N = decodeInt(connection);
        long[] values = new long[N];

        for (int i = 0; i < N; i++) {
            values[i] = decodeLong(connection);
        }

        return values;
    }

    private static short[] decodeShortArray(Connection connection)
            throws IOException {
        /* Read array length from connection */
        int N = decodeInt(connection);
        short[] values = new short[N];

        for (int i = 0; i < N; i++) {
            values[i] = decodeShort(connection);
        }

        return values;
    }

    private static float[] decodeFloatArray(Connection connection)
            throws IOException {
        /* Read array length from connection */
        int N = decodeInt(connection);
        float[] values = new float[N];

        for (int i = 0; i < N; i++) {
            values[i] = decodeFloat(connection);
        }

        return values;
    }

    private static double[] decodeDoubleArray(Connection connection)
            throws IOException {
        /* Read array length from connection */
        int N = decodeInt(connection);
        double[] values = new double[N];

        for (int i = 0; i < N; i++) {
            values[i] = decodeDouble(connection);
        }

        return values;
    }

    private static char[] decodeCharArray(Connection connection)
            throws IOException {
        /* Read array length from connection */
        int N = decodeInt(connection);
        char[] values = new char[N];

        for (int i = 0; i < N; i++) {
            values[i] = decodeChar(connection);
        }

        return values;
    }

    private static final HashMap<String, Parcelable.Creator> sParcelableCreators = new HashMap<String, Parcelable.Creator>();

    private static <T extends Parcelable> Parcelable.Creator<T> readParcelableCreator(
            String name) {
        Parcelable.Creator<T> creator = null;

        synchronized (sParcelableCreators) {
            creator = sParcelableCreators.get(name);
            if (creator == null) {
                try {
                    Class<?> c = Class.forName(name);
                    Field f = c.getField("CREATOR");
                    creator = (Parcelable.Creator) f.get(null);

                } catch (IllegalAccessException e) {
                    IwdsAssert.dieIf(TAG, true, "Illegal access when"
                            + " unmarshalling: " + name);

                } catch (ClassNotFoundException e) {
                    IwdsAssert.dieIf(TAG, true, "Class not found when"
                            + " unmarshalling: " + name);

                } catch (ClassCastException e) {
                    IwdsAssert.dieIf(TAG, true, "Parcelable protocol requires a "
                            + "Parcelable.Creator object called "
                            + " CREATOR on class " + name);

                } catch (NoSuchFieldException e) {
                    IwdsAssert.dieIf(TAG, true, "Parcelable protocol requires a "
                            + "Parcelable.Creator object called "
                            + " CREATOR on class " + name);

                } catch (NullPointerException e) {
                    IwdsAssert.dieIf(TAG, true, "Parcelable protocol requires "
                            + "the CREATOR object to be static on class "
                            + name);
                }

                if (creator == null) {
                    IwdsAssert.dieIf(TAG, true, "Parcelable protocol requires a "
                            + "Parcelable.Creator object called "
                            + " CREATOR on class " + name);
                }

                sParcelableCreators.put(name, creator);
            }

            if (DEBUG) {
                Set<Map.Entry<String, Parcelable.Creator>> entries = sParcelableCreators
                        .entrySet();
                for (Map.Entry<String, Parcelable.Creator> e : entries) {
                    IwdsLog.d(TAG,
                            "Parcelable.Creator Map - key: " + e.getKey()
                                    + " value: " + e.getValue());
                }
            }
        }

        return creator;
    }

    private static final HashMap<String, SafeParcelable.Creator> sSafeParcelableCreators = new HashMap<String, SafeParcelable.Creator>();

    private static <T extends SafeParcelable> SafeParcelable.Creator<T> readSafeParcelableCreator(
            String name) {
        SafeParcelable.Creator<T> creator = null;

        synchronized (sSafeParcelableCreators) {
            creator = sSafeParcelableCreators.get(name);
            if (creator == null) {
                try {
                    Class<?> c = Class.forName(name);
                    Field f = c.getField("CREATOR");
                    creator = (SafeParcelable.Creator) f.get(null);

                } catch (IllegalAccessException e) {
                    IwdsAssert.dieIf(TAG, true, "Illegal access when"
                            + " unmarshalling: " + name);

                } catch (ClassNotFoundException e) {
                    IwdsAssert.dieIf(TAG, true, "Class not found when"
                            + " unmarshalling: " + name);

                } catch (ClassCastException e) {
                    IwdsAssert.dieIf(TAG, true, "Parcelable protocol requires a "
                            + "Parcelable.Creator object called "
                            + " CREATOR on class " + name);

                } catch (NoSuchFieldException e) {
                    IwdsAssert.dieIf(TAG, true, "Parcelable protocol requires a "
                            + "Parcelable.Creator object called "
                            + " CREATOR on class " + name);

                } catch (NullPointerException e) {
                    IwdsAssert.dieIf(TAG, true, "Parcelable protocol requires "
                            + "the CREATOR object to be static on class "
                            + name);
                }

                if (creator == null) {
                    IwdsAssert.dieIf(TAG, true, "Parcelable protocol requires a "
                            + "Parcelable.Creator object called "
                            + " CREATOR on class " + name);
                }

                sSafeParcelableCreators.put(name, creator);
            }

            if (DEBUG) {
                Set<Map.Entry<String, SafeParcelable.Creator>> entries = sSafeParcelableCreators
                        .entrySet();
                for (Map.Entry<String, SafeParcelable.Creator> e : entries) {
                    IwdsLog.d(TAG,
                            "SafeParcelable.Creator Map - key: " + e.getKey()
                                    + " value: " + e.getValue());
                }
            }
        }

        return creator;
    }

    private static <T extends Parcelable> Object decodeParcelable (
            Connection connection, Parcelable.Creator<T> creator) throws IOException {

        Parcelable.Creator<T> theCeator = null;

        if (creator == null) {
            /* Read parcelable class name from connection */
            String name = decodeString(connection);

            /* Get parcelable interface */
            theCeator = readParcelableCreator(name);

        } else {
            theCeator = creator;
        }

        /* Read raw bytes length from connection */
        int length = decodeInt(connection);
        byte[] buffer = new byte[length];

        int pos = 0;
        InputStream is = connection.getInputStream();

        /* Read raw bytes from connection */
        while (length > 0) {
            int readBytes = is.read(buffer, pos, length);
            pos += readBytes;
            length -= readBytes;
        }

        return readParcelable(buffer, theCeator);
    }

    private static <T extends SafeParcelable> Object decodeSafeParcelable (
            Connection connection, SafeParcelable.Creator<T> creator) throws IOException {

        SafeParcelable.Creator<T> theCeator = null;

        if (creator == null) {
            /* Read safe-parcelable class name from connection */
            String name = decodeString(connection);

            /* Get safe-parcelable interface */
            theCeator = readSafeParcelableCreator(name);

        } else {
            theCeator = creator;
        }

        /* Read raw bytes length from connection */
        int length = decodeInt(connection);
        byte[] buffer = new byte[length];

        int pos = 0;
        InputStream is = connection.getInputStream();

        /* Read raw bytes from connection */
        while (length > 0) {
            int readBytes = is.read(buffer, pos, length);
            pos += readBytes;
            length -= readBytes;
        }

        return readSafeParcelable(buffer, theCeator);
    }

    private static HashMap decodeHashMap(Connection connection)
            throws IOException {
        /* Read map entry size from connection */
        int N = decodeInt(connection);
        HashMap map = new HashMap(N);

        for (int i = 0; i < N; i++) {
            Object key = decode(connection, null, null, null);
            Object value = decode(connection, null, null, null);
            map.put(key, value);
        }

        return map;
    }

    private static ArrayList decodeArrayList(Connection connection)
            throws IOException {
        /* Read list size from connection */
        int N = decodeInt(connection);
        ArrayList list = new ArrayList(N);

        for (int i = 0; i < N; i++) {
            Object value = decode(connection, null, null, null);
            list.add(value);
        }

        return list;
    }

    private static Object[] decodeArray(Connection connection)
            throws IOException {
        /* Read array length from connection */
        int N = decodeInt(connection);
        Object[] array = new Object[N];

        for (int i = 0; i < N; i++) {
            Object value = decode(connection, null, null, null);
            array[i] = value;
        }

        return array;
    }

    private static CharSequence decodeCharSequence(Connection connection)
            throws IOException {
        /* Read char sequence length from connection */
        int length = decodeInt(connection);
        byte[] buffer = new byte[length];
        int pos = 0;
        InputStream is = connection.getInputStream();

        /* Read raw bytes from connection */
        while (length > 0) {
            int readBytes = is.read(buffer, pos, length);
            pos += readBytes;
            length -= readBytes;
        }

        return readString(buffer);
    }

    private static CharSequence[] decodeCharSequenceArray(Connection connection)
            throws IOException {
        /* Read array length from connection */
        int N = decodeInt(connection);
        CharSequence[] values = new CharSequence[N];

        for (int i = 0; i < N; i++) {
            values[i] = decodeCharSequence(connection);
        }

        return values;
    }

    private static Serializable decodeSerializable(Connection connection)
            throws IOException {
        /* Read serializable class name from connection */
        String name = decodeString(connection);

        IwdsAssert.dieIf(TAG, name == null, "Bad serializable name null");

        /* Read raw bytes from connection */
        byte[] valueBuf = decodeByteArray(connection);
        ByteArrayInputStream bais = new ByteArrayInputStream(valueBuf);

        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (Serializable) ois.readObject();
        } catch (IOException e) {
            IwdsAssert.dieIf(TAG, true, "Parcelable encountered "
                    + "IOException reading a Serializable object (name = "
                    + name + ")");
            e.printStackTrace();
            return null;

        } catch (ClassNotFoundException e) {
            IwdsAssert.dieIf(TAG, true, "Parcelable encountered"
                    + "ClassNotFoundException reading a Serializable object"
                    + "(name = " + name + ")");
            return null;
        }
    }

    private static SparseArray decodeSparseArray(Connection connection)
            throws IOException {
        /* Read array length from connection */
        int N = decodeInt(connection);
        SparseArray array = new SparseArray(N);

        for (int i = 0; i < N; i++) {
            int key = decodeInt(connection);
            Object value = decode(connection, null, null, null);
            array.put(key, value);
        }

        return array;
    }

    private static SparseBooleanArray decodeSparseBooleanArray(
            Connection connection) throws IOException {
        /* Read array length from connection */
        int N = decodeInt(connection);

        SparseBooleanArray array = new SparseBooleanArray(N);

        for (int i = 0; i < N; i++) {
            int key = decodeInt(connection);
            boolean value = decodeBoolean(connection);
            array.put(key, value);
        }

        return array;
    }

    private static Parcelable[] decodeParcelableArray(Connection connection)
            throws IOException {
        /* Read array length from connection */
        int N = decodeInt(connection);
        Parcelable[] array = new Parcelable[N];

        for (int i = 0; i < N; i++) {
            array[i] = (Parcelable) decodeParcelable(connection, null);
        }

        return array;
    }

    private static Parcelable[] decodeSafeParcelableArray(Connection connection)
            throws IOException {
        /* Read array length from connection */
        int N = decodeInt(connection);
        Parcelable[] array = new Parcelable[N];

        for (int i = 0; i < N; i++) {
            array[i] = (Parcelable) decodeSafeParcelable(connection, null);
        }

        return array;
    }

    private static File decodeFile(Connection connection, TransferAdapterCallback callback) throws
            IOException, FileTransferException {

        File base = null;
        StatFs stat = null;

        /* Check SD card status */
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (sDesiredStoragePath == null) {
                sDesiredStoragePath = Environment.getExternalStorageDirectory().getPath() +
                        UtilsConstants.DEFAULT_STORE_SUBDIR;
            }
            base = new File(sDesiredStoragePath);

            if (!base.isDirectory() && !base.mkdir()) {
                IwdsLog.e(TAG, "Unable to receive file: can't create base"
                        + " directory " + base.getPath());
                IwdsException.throwFileTransferException(FileTransferErrorCode.EFILESTATUS);
            }

            stat = new StatFs(base.getPath());

        } else {
            IwdsLog.e(TAG, "Unable to receive file: no external storage");
            IwdsException.throwFileTransferException(FileTransferErrorCode.ENOSDCARD);
        }

        /* Read file name from connection */
        String name = decodeString(connection);
        if (name == null || name.isEmpty()) {
            IwdsLog.e(TAG, "Unable to receive file: name is null or empty");
            IwdsException.throwFileTransferException(FileTransferErrorCode.EFILESTATUS);
        }

        File file = new File(sDesiredStoragePath, name);

        /* Read file length from connection */
        long fileLength = decodeLong(connection);
        if (fileLength <= 0) {
            IwdsLog.e(TAG, "Unable to receive file: invalid file length");
            IwdsException.throwFileTransferException(FileTransferErrorCode.EFILESTATUS);
        }

        int chunkSize = UtilsConstants.SizeOf.FileChunk;

        /* Read max chunk count */
        int chunkCount = 0;
        if (fileLength % chunkSize == 0)
            chunkCount = (int) (fileLength / chunkSize);
        else
            chunkCount = (int) (fileLength / chunkSize + 1);

        /* Read file chunk index */
        int index = decodeInt(connection);

        /* Check chunk index */
        if (index > chunkCount || index < 0) {
            IwdsLog.e(TAG, "Unable to receive file: chunk index out of bound");
            IwdsException.throwFileTransferException(FileTransferErrorCode.EFILESTATUS);
        }

        long readSoFar = index * chunkSize;

        /* Check already transfer length */
        if (readSoFar > file.length()) {
            IwdsLog.e(TAG, "Unable to receive file: file length less than already read length");
            IwdsException.throwFileTransferException(FileTransferErrorCode.EFILESTATUS);
        }

        long length = fileLength - readSoFar;

        /* Check SD card free size */
        if (stat.getBlockCountLong() * (stat.getAvailableBlocksLong() - 4) < length) {
            IwdsLog.e(TAG, "Unabled to receive file: not enough free space");
            IwdsException.throwFileTransferException(FileTransferErrorCode.ESDCARDFULL);
        }

        RandomAccessFile raf = null;

        try {
            raf = new RandomAccessFile(file, "rwd");

        } catch (FileNotFoundException e) {
            IwdsLog.e(TAG, "Unable to receive file: error create file " + name);
            IwdsException.throwFileTransferException(FileTransferErrorCode.EFILESTATUS);
        }

        try {
            /* Seek to end of file */
            raf.seek(readSoFar);

        } catch (IOException e) {
            try {
                raf.close();

            } catch (IOException ioe) {
                //ignore close exception
            }

            IwdsLog.e(TAG, "Unable to receive file: error seek file to " + readSoFar);
            IwdsException.throwFileTransferException(FileTransferErrorCode.EFILESTATUS);
        }

        long bytesOfLeft = length;
        InputStream is = connection.getInputStream();

        byte[] buffer = new byte[chunkSize];

        while (bytesOfLeft > 0) {
            int readSize = (int) Math.min(chunkSize, bytesOfLeft);

            int pos = 0;
            int maxSize = readSize;

            /* Read raw bytes from connection input stream
             * Note:
             *      throw IOException if an error occurs while read connection
             *      input stream
             */
            try {
                while (maxSize > 0) {
                    int readBytes = is.read(buffer, pos, maxSize);

                    maxSize -= readBytes;
                    pos += readBytes;
                }

            } catch (IOException e) {
                try {
                    raf.close();

                } catch (IOException ioe) {
                    //ignore close exception
                }

                IwdsLog.e(TAG, "Unable to receive file: connection io exception");
                callback.onRecvFileInterrupted(index);
                throw e;
            }

            try {
                raf.write(buffer, 0, readSize);

            } catch (IOException e) {
                try {
                    raf.close();

                } catch (IOException ioe) {
                    //ignore close exception
                }

                IwdsLog.e(TAG, "Unable to receive file: file write io exception");
                callback.onRecvFileInterrupted(index);
                IwdsException.throwFileTransferException(FileTransferErrorCode.EFILESTATUS);
            }

            IwdsAssert.dieIf(TAG, index++ > chunkCount, "index out of bound:"
                    + ", current=" + index + ", total=" + chunkCount);

            readSoFar += readSize;
            bytesOfLeft -= readSize;

            callback.onRecvFileProgress(readSoFar, fileLength);
        }

        try {
            raf.close();

        } catch (IOException e) {
            //ignore close exception
        }

        if (file.length() != fileLength) {
            IwdsLog.e(TAG, "Unable to receive file: error received file"
                    + " length: recvLen=" + file.length() + ", sendLen=" + fileLength);
            IwdsException.throwFileTransferException(FileTransferErrorCode.EFILESTATUS);
        }

        return file;
    }

    /**
     * 用于从 {@code connection} 中解析出一个通用对象 {@code Object}, 其中回调接口
     * {@link com.ingenic.iwds.utils.serializable.TransferAdapter.TransferAdapterCallback TransferAdapterCallback}
     * 用于更新文件接收状态. 该方法将阻塞执行直到解析完成.
     *
     * @param connection
     *        已经建立连接的{@code connection}
     * @param parcelableCreator
     *        实现 Parcelable 接口的类的静态接口 CREATOR
     * @param safeParcelableCreator
     *        实现 SafeParcelable 接口的类的静态接口 CREATOR
     * @param callback
     *        回调接口{@link com.ingenic.iwds.utils.serializable.TransferAdapter
     *        .TransferAdapterCallback TransferAdapterCallback} 用于更新文件接收状态
     * @return 通用对象 {@code Object}
     * @throws IOException
     *         {@code connection} 读异常, 参考:
     *         {@link com.ingenic.iwds.common.exception.LinkDisconnectedException LinkDisconnectedException},
     *         {@link com.ingenic.iwds.common.exception.LinkUnbondedException LinkUnbondedException},
     *         {@link com.ingenic.iwds.common.exception.PortBusyException PortBusyException},
     *         {@link com.ingenic.iwds.common.exception.PortClosedException PortClosedException},
     *         {@link com.ingenic.iwds.common.exception.PortDisconnectedException PortDisconnectedException},
     * @throws FileTransferException
     *         文件传输异常
     */
    public static <T1 extends Parcelable, T2 extends SafeParcelable> Object
        decode(Connection connection, Parcelable.Creator<T1> parcelableCreator,
                SafeParcelable.Creator<T2> safeParcelableCreator,
                TransferAdapterCallback callback)
            throws IOException, FileTransferException {

        IwdsAssert.dieIf(TAG, connection == null, "connection == null");

        byte type = decodeByte(connection);

        switch (type) {
        case UtilsConstants.VAL_STRING:
            return decodeString(connection);

        case UtilsConstants.VAL_INTEGER:
            return decodeInt(connection);

        case UtilsConstants.VAL_MAP:
            return decodeHashMap(connection);

        case UtilsConstants.VAL_PARCELABLE:
            return decodeParcelable(connection, parcelableCreator);

        case UtilsConstants.VAL_SAFEPARCELABLE:
            return decodeSafeParcelable(connection, safeParcelableCreator);

        case UtilsConstants.VAL_SHORT:
            return decodeShort(connection);

        case UtilsConstants.VAL_LONG:
            return decodeLong(connection);

        case UtilsConstants.VAL_FLOAT:
            return decodeFloat(connection);

        case UtilsConstants.VAL_DOUBLE:
            return decodeDouble(connection);

        case UtilsConstants.VAL_BOOLEAN:
            return decodeBoolean(connection);

        case UtilsConstants.VAL_CHARSEQUENCE:
            return decodeCharSequence(connection);

        case UtilsConstants.VAL_LIST:
            return decodeArrayList(connection);

        case UtilsConstants.VAL_BOOLEANARRAY:
            return decodeBooleanArray(connection);

        case UtilsConstants.VAL_BYTEARRAY:
            return decodeByteArray(connection);

        case UtilsConstants.VAL_STRINGARRAY:
            return decodeStringArray(connection);

        case UtilsConstants.VAL_CHARSEQUENCEARRAY:
            return decodeCharSequenceArray(connection);

        case UtilsConstants.VAL_OBJECTARRAY:
            return decodeArray(connection);

        case UtilsConstants.VAL_INTARRAY:
            return decodeIntArray(connection);

        case UtilsConstants.VAL_LONGARRAY:
            return decodeLongArray(connection);

        case UtilsConstants.VAL_BYTE:
            return decodeByte(connection);

        case UtilsConstants.VAL_CHAR:
            return decodeChar(connection);

        case UtilsConstants.VAL_SERIALIZABLE:
            return decodeSerializable(connection);

        case UtilsConstants.VAL_PARCELABLEARRAY:
            return decodeParcelableArray(connection);

        case UtilsConstants.VAL_SAFEPARCELABLEARRAY:
            return decodeSafeParcelableArray(connection);

        case UtilsConstants.VAL_SPARSEARRAY:
            return decodeSparseArray(connection);

        case UtilsConstants.VAL_SPARSEBOOLEANARRAY:
            return decodeSparseBooleanArray(connection);

        case UtilsConstants.VAL_SHORTARRAY:
            return decodeShortArray(connection);

        case UtilsConstants.VAL_FLOATARRAY:
            return decodeFloatArray(connection);

        case UtilsConstants.VAL_DOUBLEARRAY:
            return decodeDoubleArray(connection);

        case UtilsConstants.VAL_CHARARRAY:
            return decodeCharArray(connection);

        case UtilsConstants.VAL_FILE:
            IwdsAssert.dieIf(TAG, callback == null, "TransferAdapterCallback is null");
            return decodeFile(connection, callback);

        default:
            IwdsAssert.dieIf(TAG, true, "Unsupported object type code: " + type);
            return null;
        }
    }
}
