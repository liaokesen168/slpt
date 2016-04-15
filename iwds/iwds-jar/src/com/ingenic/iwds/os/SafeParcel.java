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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ingenic.iwds.utils.IwdsLog;

/**
 * SafeParcel专门设计用于跨设备的序列化传输(使用 {@link SafeParcelable}接口实现不同数据类型的序列化).
 * 
 * <p>
 * SafeParcel的API封转了不同数据类型的读/写. 主要有6个基本的读/写方法
 * </p>
 * 
 * <h3>基本数据</h3>
 * 
 * <p>
 * 基本数据类型的读/写方法如下: {@link #writeByte}, {@link #readByte}, {@link #writeDouble},
 * {@link #readDouble}, {@link #writeFloat}, {@link #readFloat},
 * {@link #writeInt}, {@link #readInt}, {@link #writeLong}, {@link #readLong},
 * {@link #writeString}, {@link #readString}. 其他数据类型的读/写都是构建在基本数据类型操作之上.
 * 以上的读/写字节序依赖CPU.
 * </p>
 * 
 * <h3>基本数据类型数组</h3>
 * 
 * <p>
 * 还有一些方法用于读/写字节数组和基本数据类型数组. 数据读操作可以将数据读到一个已经存在的数组或者创建并返回一个新的数组. 数据类型如下:
 * </p>
 *
 * <ul>
 * <li> {@link #writeBooleanArray(boolean[])},
 * {@link #readBooleanArray(boolean[])}, {@link #createBooleanArray()}
 * <li> {@link #writeByteArray(byte[])},
 * {@link #writeByteArray(byte[], int, int)}, {@link #readByteArray(byte[])},
 * {@link #createByteArray()}
 * <li> {@link #writeCharArray(char[])}, {@link #readCharArray(char[])},
 * {@link #createCharArray()}
 * <li> {@link #writeDoubleArray(double[])}, {@link #readDoubleArray(double[])},
 * {@link #createDoubleArray()}
 * <li> {@link #writeFloatArray(float[])}, {@link #readFloatArray(float[])},
 * {@link #createFloatArray()}
 * <li> {@link #writeIntArray(int[])}, {@link #readIntArray(int[])},
 * {@link #createIntArray()}
 * <li> {@link #writeLongArray(long[])}, {@link #readLongArray(long[])},
 * {@link #createLongArray()}
 * <li> {@link #writeStringArray(String[])}, {@link #readStringArray(String[])},
 * {@link #createStringArray()}.
 * </ul>
 * 
 * <h3>SafeParcelables</h3>
 * 
 * <p>
 * {@link SafeParcelable} 协议提供了非常高效的接口用于对象序列化到SafeParcels, 你可以直接使用
 * {@link #writeParcelable(SafeParcelable, int)} 和
 * {@link #readParcelable(ClassLoader)} 或者 {@link #writeParcelableArray} 和
 * {@link #readParcelableArray(ClassLoader)}读/写对象. 这些方法将数据的类型以及数据内容写入SafeParcel,
 * 同时也可以在读的时候传入 类加载器去重新构造对象.
 * </p>
 * 
 * <p>
 * 有一些高效的方法用于SafeParcelable接口: {@link #writeTypedArray},
 * {@link #writeTypedList(List)}, {@link #readTypedArray} 和
 * {@link #readTypedList}. 这些方法不会将对象的类型写入SafeParcel:
 * 相反调用者在读的时候必须知道自己所期望的数据类型并将其传入{@link SafeParcelable.Creator
 * SafeParcelable.Creator}. (对于更高效的读/写单个SafeParcelable对象, 可以自己调用
 * {@link SafeParcelable#writeToParcel SafeParcelable.writeToParcel} 和
 * {@link SafeParcelable.Creator#createFromParcel
 * SafeParcelable.Creator.createFromParcel})
 * </p>
 * 
 * <h3>RemoteBundles</h3>
 * 
 * <p>
 * 跨设备的数据容器{@link RemoteBundle} 是基于键-值对的异构数据结构. 这样后利于提高数据读/写的效率,
 * 并且它的类型安全的API可以避免类型错误的调试当序列化数据内容到一个SafeParcel. 可用方法
 * {@link #writeBundle(RemoteBundle)}, {@link #readBundle()}, 和
 * {@link #readBundle(ClassLoader)}.
 * </p>
 * 
 * <h3>没有指定类型的数据</h3>
 * 
 * <p>
 * 还有一些方法用于读/写标准的Java数据类型及其数组. 方法如下: {@link #writeValue(Object)} 和
 * {@link #readValue(ClassLoader)} 方法, {@link #writeArray(Object[])},
 * {@link #readArray(ClassLoader)}, {@link #writeList(List)},
 * {@link #readList(List, ClassLoader)}, {@link #readArrayList(ClassLoader)},
 * {@link #writeMap(Map)}, {@link #readMap(Map, ClassLoader)}
 * </p>
 */

public final class SafeParcel {
    private static final boolean DEBUG_RECYCLE = false;

    private static final int POOL_SIZE = 256;

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

    private static final SafeParcel[] sOwnedPool = new SafeParcel[POOL_SIZE];
    private static final SafeParcel[] sHolderPool = new SafeParcel[POOL_SIZE];

    private long m_nativePtr;
    private boolean m_ownNativeParcelObject;
    private RuntimeException m_stack;

    static {
        System.loadLibrary("safeparcel");
    }

    public final static SafeParcelable.Creator<String> STRING_CREATOR = new SafeParcelable.Creator<String>() {
        public String createFromParcel(SafeParcel source) {
            return source.readString();
        }

        public String[] newArray(int size) {
            return new String[size];
        }
    };

    private SafeParcel(long nativePtr) {
        if (DEBUG_RECYCLE)
            m_stack = new RuntimeException();

        init(nativePtr);
    }

    private void init(long nativePtr) {
        if (nativePtr != 0) {
            m_nativePtr = nativePtr;
            m_ownNativeParcelObject = false;
        } else {
            m_nativePtr = nativeCreate();
            m_ownNativeParcelObject = true;
        }
    }

    /**
     * 从对象池中获取一个SafeParcel对象
     * 
     * @return SafeParcel对象
     */
    public static SafeParcel obtain() {
        synchronized (sOwnedPool) {
            SafeParcel p;
            for (int i = 0; i < POOL_SIZE; i++) {
                p = sOwnedPool[i];
                if (p != null) {
                    sOwnedPool[i] = null;
                    if (DEBUG_RECYCLE)
                        p.m_stack = new RuntimeException();

                    return p;
                }
            }
        }

        return new SafeParcel(0);
    }

    static protected final SafeParcel obtain(int obj) {
        throw new UnsupportedOperationException();
    }

    static protected final SafeParcel obtain(long obj) {
        synchronized (sHolderPool) {
            SafeParcel p;
            for (int i = 0; i < POOL_SIZE; i++) {
                p = sHolderPool[i];
                if (p != null) {
                    sHolderPool[i] = null;
                    if (DEBUG_RECYCLE)
                        p.m_stack = new RuntimeException();

                    p.init(obj);

                    return p;
                }
            }
        }

        return new SafeParcel(obj);
    }

    /**
     * 将SafeParcel对象放会对象池. 在此之后你不能再访问这个对象.
     */
    public final void recycle() {
        if (DEBUG_RECYCLE)
            m_stack = null;

        freeBuffer();

        final SafeParcel[] pool;
        if (m_ownNativeParcelObject) {
            pool = sOwnedPool;
        } else {
            m_nativePtr = 0;
            pool = sHolderPool;
        }

        synchronized (pool) {
            for (int i = 0; i < POOL_SIZE; i++) {
                if (pool[i] == null) {
                    pool[i] = this;
                    return;
                }
            }
        }
    }

    private void freeBuffer() {
        if (m_ownNativeParcelObject) {
            nativeFreeBuffer(m_nativePtr);
        }
    }

    private void destroy() {
        if (m_nativePtr != 0) {
            if (m_ownNativeParcelObject) {
                nativeDestroy(m_nativePtr);
            }
            m_nativePtr = 0;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (DEBUG_RECYCLE) {
            if (m_stack != null) {
                IwdsLog.w(this, "Client did not call SafeParcel.recycle()",
                        m_stack);
            }
        }

        destroy();
    }

    /**
     * 返回SafeParcel中数据的大小
     * 
     * @return 数据的大小
     */
    public final int dataSize() {
        return nativeDataSize(m_nativePtr);
    }

    /**
     * 设置SafeParcel中数据的大小.
     * 
     * @param size
     *            数据的大小, 可以小于或者大于当前大小. 如果大于当前的容量dataCapacity(),将会分配更多的内存
     */
    public final void setDataSize(int size) {
        nativeSetDataSize(m_nativePtr, size);
    }

    /**
     * 返回SafeParcel剩余的空间大小. {@link #dataSize}-{@link #dataPosition}.
     * 
     * @return 剩余的空间大小
     */
    public final int dataAvail() {
        return nativeDataAvail(m_nativePtr);
    }

    /**
     * 返回SafeParcel中当前数据的位置. 永远小于{@link #dataSize}.
     * 
     * @return SafeParcel中当前数据的位置
     */
    public final int dataPosition() {
        return nativeDataPosition(m_nativePtr);
    }

    /**
     * 移动SafeParcel中数据读/写的当前位置
     * 
     * @param pos
     *            　数据读/写的当前位置, 范围必须在0-{@link #dataSize}.
     */
    public final void setDataPosition(int pos) {
        nativeSetDataPosition(m_nativePtr, pos);
    }

    /**
     * 返回SafeParcel容量, 该值总>= {@link #dataSize}.
     * 
     * @return 容量
     */
    public final int dataCapacity() {
        return nativeDataCapacity(m_nativePtr);
    }

    /**
     * 修改SafeParcel的容量
     * 
     * @param size
     *            　容量, 不能小于{@link #dataSize}
     */
    public final void setDataCapacity(int size) {
        nativeSetDataCapacity(m_nativePtr, size);
    }

    /**
     * 以字节数组形式返回SafeParcel中的数据
     * 
     * @return 字节数组
     */
    public final byte[] marshall() {
        return nativeMarshall(m_nativePtr);
    }

    /**
     * 将字节数组内容放到SafeParcel中
     * 
     * @param data
     *            字节数组
     * @param offest
     *            偏移
     * @param length
     *            长度
     */
    public final void unmarshall(byte[] data, int offest, int length) {
        nativeUnmarshall(m_nativePtr, data, offest, length);
    }

    private static native long nativeCreate();

    private static native void nativeFreeBuffer(long nativePtr);

    private static native void nativeDestroy(long nativePtr);

    private static native int nativeDataSize(long nativePtr);

    private static native void nativeSetDataSize(long nativePtr, int size);

    private static native int nativeDataAvail(long nativePtr);

    private static native int nativeDataPosition(long nativePtr);

    private static native void nativeSetDataPosition(long nativePtr, int pos);

    private static native int nativeDataCapacity(long nativePtr);

    private static native void nativeSetDataCapacity(long nativePtr, int size);

    private static native byte[] nativeMarshall(long nativePtr);

    private static native void nativeUnmarshall(long nativePtr, byte[] data,
            int offset, int length);

    private static native void nativeWriteInt(long nativePtr, int val);

    private static native void nativeWriteString(long nativePtr, String val);

    private static native void nativeWriteLong(long nativePttr, long val);

    private static native void nativeWriteFloat(long nativePtr, float val);

    private static native void nativeWriteDouble(long nativePtr, double val);

    private static native void nativeWriteByteArray(long nativePtr, byte[] val,
            int offset, int len);

    private static native long nativeReadLong(long nativePtr);

    private static native float nativeReadFloat(long nativePtr);

    private static native double nativeReadDouble(long nativePtr);

    private static native byte[] nativeCreateByteArray(long nativePtr);

    private static native int nativeReadInt(long nativePtr);

    private static native String nativeReadString(long nativePtr);

    private static void checkOffsetAndCount(int arrayLength, int offset,
            int count) {
        if ((offset | count) < 0 || offset > arrayLength
                || arrayLength - offset < count) {
            throw new ArrayIndexOutOfBoundsException("length=" + arrayLength
                    + "; regionStart=" + offset + "; regionLength=" + count);
        }
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            整型数据
     */
    public final void writeInt(int val) {
        nativeWriteInt(m_nativePtr, val);
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            长整型数据
     */
    public final void writeLong(long val) {
        nativeWriteLong(m_nativePtr, val);
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            浮点型数据
     */
    public final void writeFloat(float val) {
        nativeWriteFloat(m_nativePtr, val);
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            双精度浮点型数据
     */
    public final void writeDouble(double val) {
        nativeWriteDouble(m_nativePtr, val);
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            String对象
     */
    public final void writeString(String val) {
        nativeWriteString(m_nativePtr, val);
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel. key值必须为String类型, 推荐使用
     * {@link #writeBundle}, 因为RemoteBundle数据类型安全
     * 
     * @param val
     *            Map数据
     */
    public final void writeMap(Map val) {
        writeMapInternal((Map<String, Object>) val);
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            字节数据
     */
    public final void writeByte(byte val) {
        writeInt(val);
    }

    private void writeMapInternal(Map<String, Object> val) {
        if (val == null) {
            writeInt(-1);
            return;
        }

        Set<Map.Entry<String, Object>> entries = val.entrySet();
        writeInt(entries.size());
        for (Map.Entry<String, Object> e : entries) {
            writeValue(e.getKey());
            writeValue(e.getValue());
        }
    }

    /*
     * TODO: public me
     */
    private final void writeBundle(RemoteBundle val) {
        if (val == null) {
            writeInt(-1);
            return;
        }

        /*
         * TODO: open me
         */
        // val.writeToParcel(this, 0);
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param p
     *            SafeParcelable 对象
     * @param flags
     *            上下文标志
     */
    public final void writeParcelable(SafeParcelable p, int flags) {
        if (p == null) {
            writeString(null);
            return;
        }

        String name = p.getClass().getName();
        writeString(name);
        p.writeToParcel(this, flags);
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            CharSequenced对象
     */
    public final void writeCharSequence(CharSequence val) {
        if (val == null) {
            writeInt(-1);
            return;
        }

        String str = val.toString();
        writeString(str);
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            List对象
     */
    public final void writeList(List val) {
        if (val == null) {
            writeInt(-1);
            return;
        }

        int N = val.size();
        int i = 0;
        writeInt(N);
        while (i < N) {
            writeValue(val.get(i));
            i++;
        }
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            boolean[]数据
     */
    public final void writeBooleanArray(boolean[] val) {
        if (val == null) {
            writeInt(-1);
            return;
        }

        int N = val.length;
        writeInt(N);
        int i = 0;
        while (i < N) {
            writeInt(val[i] ? 1 : 0);
            i++;
        }
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            byte[]数据
     */
    public final void writeByteArray(byte[] val) {
        writeByteArray(val, 0, (val != null) ? val.length : 0);
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            字节数组
     * @param offset
     *            偏移位置
     * @param len
     *            长度
     */
    public final void writeByteArray(byte[] val, int offset, int len) {
        if (val == null) {
            writeInt(-1);
            return;
        }

        checkOffsetAndCount(val.length, offset, len);
        nativeWriteByteArray(m_nativePtr, val, offset, len);
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            String[]数据
     */
    public final void writeStringArray(String[] val) {
        if (val == null) {
            writeInt(-1);
            return;
        }

        int N = val.length;
        writeInt(N);
        int i = 0;
        while (i < N) {
            writeString(val[i]);
            i++;
        }
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            CharSequence[]数据
     */
    public final void writeCharSequenceArray(CharSequence[] val) {
        if (val == null) {
            writeInt(-1);
            return;
        }

        int N = val.length;
        writeInt(N);
        int i = 0;
        while (i < N) {
            writeCharSequence(val[i]);
            i++;
        }
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            char[]数据
     */
    public final void writeCharArray(char[] val) {
        if (val == null) {
            writeInt(-1);
            return;
        }

        int N = val.length;
        writeInt(N);
        int i = 0;
        while (i < N) {
            writeInt((int) val[i]);
            i++;
        }
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            SafeParcelable[]数据
     */
    public final <T extends SafeParcelable> void writeParcelableArray(T[] val,
            int flags) {
        if (val == null) {
            writeInt(-1);
            return;
        }

        int N = val.length;
        writeInt(N);
        int i = 0;
        while (i < N) {
            writeParcelable(val[i], flags);
            i++;
        }
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            Object[]数据
     */
    public final void writeArray(Object[] val) {
        if (val == null) {
            writeInt(-1);
            return;
        }

        int N = val.length;
        writeInt(N);
        int i = 0;
        while (i < N) {
            writeValue(val[i]);
            i++;
        }
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            int[]数据
     */
    public final void writeIntArray(int[] val) {
        if (val == null) {
            writeInt(-1);
            return;
        }

        int N = val.length;
        writeInt(N);
        int i = 0;
        while (i < N) {
            writeInt(val[i]);
            i++;
        }
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            long[]数据
     */
    public final void writeLongArray(long[] val) {
        if (val == null) {
            writeInt(-1);
            return;
        }

        int N = val.length;
        writeInt(N);
        int i = 0;
        while (i < N) {
            writeLong(val[i]);
            i++;
        }
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            float[]数据
     */
    public final void writeFloatArray(float[] val) {
        if (val == null) {
            writeInt(-1);
            return;
        }

        int N = val.length;
        writeInt(N);
        int i = 0;
        while (i < N) {
            writeFloat(val[i]);
            i++;
        }
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            double[]数据
     */
    public final void writeDoubleArray(double[] val) {
        if (val == null) {
            writeInt(-1);
            return;
        }

        int N = val.length;
        writeInt(N);
        int i = 0;
        while (i < N) {
            writeDouble(val[i]);
            i++;
        }
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            List<T>数据
     */
    public final <T extends SafeParcelable> void writeTypedList(List<T> val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        int N = val.size();
        int i = 0;
        writeInt(N);
        while (i < N) {
            T item = val.get(i);
            if (item != null) {
                writeInt(1);
                item.writeToParcel(this, 0);
            } else {
                writeInt(0);
            }
            i++;
        }
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            List<String>数据
     */
    public final void writeStringList(List<String> val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        int N = val.size();
        int i = 0;
        writeInt(N);
        while (i < N) {
            writeString(val.get(i));
            i++;
        }
    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            T[]数据
     */
    public final <T extends SafeParcelable> void writeTypedArray(T[] val,
            int flags) {
        if (val == null) {
            writeInt(-1);
            return;
        }

        int N = val.length;
        writeInt(N);
        for (int i = 0; i < N; i++) {
            T item = val[i];
            if (item != null) {
                writeInt(1);
                item.writeToParcel(this, flags);
            } else {
                writeInt(0);
            }
        }

    }

    /**
     * 在当前位置dataPosition(), 将val写入SafeParcel
     * 
     * @param val
     *            Serializable对象
     */
    public final void writeSerializable(Serializable val) {
        if (val == null) {
            writeString(null);
            return;
        }

        String name = val.getClass().getName();
        writeString(name);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(val);
            oos.close();

            writeByteArray(baos.toByteArray());

        } catch (IOException ioe) {
            throw new RuntimeException("SafeParcelable encountered "
                    + "IOException writing serializable object (name = " + name
                    + ")", ioe);
        }
    }

    private final void writeParcelableCreator(SafeParcelable p) {
        String name = p.getClass().getName();
        writeString(name);
    }

    /**
     * 在当前位置dataPosition(), 将v写入SafeParcel
     * 
     * @param v
     *            Object对象
     */
    public final void writeValue(Object v) {
        if (v == null) {
            writeInt(VAL_NULL);

        } else if (v instanceof String) {
            writeInt(VAL_STRING);
            writeString((String) v);

        } else if (v instanceof Integer) {
            writeInt(VAL_INTEGER);
            writeInt((Integer) v);

        } else if (v instanceof Map) {
            writeInt(VAL_MAP);
            writeMap((Map) v);

        } else if (v instanceof RemoteBundle) {
            // Must be before SafeParcelable
            writeInt(VAL_REMOTEBUNDLE);
            writeBundle((RemoteBundle) v);

        } else if (v instanceof SafeParcelable) {
            writeInt(VAL_SAFEPARCELABLE);
            writeParcelable((SafeParcelable) v, 0);

        } else if (v instanceof Short) {
            writeInt(VAL_SHORT);
            writeInt(((Short) v).intValue());

        } else if (v instanceof Long) {
            writeInt(VAL_LONG);
            writeLong((Long) v);

        } else if (v instanceof Float) {
            writeInt(VAL_FLOAT);
            writeFloat((Float) v);

        } else if (v instanceof Double) {
            writeInt(VAL_DOUBLE);
            writeDouble((Double) v);

        } else if (v instanceof Boolean) {
            writeInt(VAL_BOOLEAN);
            writeInt((Boolean) v ? 1 : 0);

        } else if (v instanceof CharSequence) {
            // Must be after String
            writeInt(VAL_CHARSEQUENCE);
            writeCharSequence((CharSequence) v);

        } else if (v instanceof List) {
            writeInt(VAL_LIST);
            writeList((List) v);

        } else if (v instanceof boolean[]) {
            writeInt(VAL_BOOLEANARRAY);
            writeBooleanArray((boolean[]) v);

        } else if (v instanceof byte[]) {
            writeInt(VAL_BYTEARRAY);
            writeByteArray((byte[]) v);

        } else if (v instanceof String[]) {
            writeInt(VAL_STRINGARRAY);
            writeStringArray((String[]) v);

        } else if (v instanceof CharSequence[]) {
            // Must be after String[] and before Object[]
            writeInt(VAL_CHARSEQUENCEARRAY);
            writeCharSequenceArray((CharSequence[]) v);

        } else if (v instanceof SafeParcelable[]) {
            writeInt(VAL_SAFEPARCELABLEARRAY);
            writeParcelableArray((SafeParcelable[]) v, 0);

        } else if (v instanceof int[]) {
            writeInt(VAL_INTARRAY);
            writeIntArray((int[]) v);

        } else if (v instanceof long[]) {
            writeInt(VAL_LONGARRAY);
            writeLongArray((long[]) v);

        } else if (v instanceof Byte) {
            writeInt(VAL_BYTE);
            writeInt((Byte) v);

        } else {
            Class<?> clazz = v.getClass();
            if (clazz.isArray() && clazz.getComponentType() == Object.class) {
                // Only pure Object[] are written here, Other arrays of
                // non-primitive types are
                // handled by serialization as this does not record the
                // component type.
                writeInt(VAL_OBJECTARRAY);
                writeArray((Object[]) v);

            } else if (v instanceof Serializable) {
                // Must be last
                writeInt(VAL_SERIALIZABLE);
                writeSerializable((Serializable) v);

            } else {
                throw new RuntimeException("Parcel: unable to marshal value "
                        + v);
            }
        }
    }

    /**
     * 从当前位置dataPosition()读取int
     * 
     * @return int
     */
    public final int readInt() {
        return nativeReadInt(m_nativePtr);
    }

    /**
     * 从当前位置dataPosition()读取String
     * 
     * @return String
     */
    public final String readString() {
        return nativeReadString(m_nativePtr);
    }

    /**
     * 从当前位置dataPosition()读取long
     * 
     * @return long
     */
    public final long readLong() {
        return nativeReadLong(m_nativePtr);
    }

    /**
     * 从当前位置dataPosition()读取float
     * 
     * @return float
     */
    public final float readFloat() {
        return nativeReadFloat(m_nativePtr);
    }

    /**
     * 从当前位置dataPosition()读取double
     * 
     * @return double
     */
    public final double readDouble() {
        return nativeReadDouble(m_nativePtr);
    }

    /**
     * 从当前位置dataPosition()读取byte
     * 
     * @return byte
     */
    public final byte readByte() {
        return (byte) (readInt() & 0xff);
    }

    /**
     * 从当前位置dataPosition()读取List到outVal
     * 
     * @param outVal
     *            List
     * @param loader
     *            类加载器
     */
    public final void readList(List outVal, ClassLoader loader) {
        int N = readInt();
        readListInternal(outVal, N, loader);
    }

    /**
     * 从当前位置dataPosition()读取ArrayList
     * 
     * @param loader
     *            类加载器
     * @return ArrayList
     */
    public final ArrayList readArrayList(ClassLoader loader) {
        int N = readInt();
        if (N < 0)
            return null;

        ArrayList l = new ArrayList(N);
        readListInternal(l, N, loader);

        return l;
    }

    private void readListInternal(List outVal, int N, ClassLoader loader) {
        while (N > 0) {
            Object value = readValue(loader);
            outVal.add(value);
            N--;
        }
    }

    /**
     * 从当前位置dataPosition()读取CharSequence对象
     * 
     * @return CharSequence对象
     */
    public final CharSequence readCharSequence() {
        return readString();
    }

    /**
     * 从当前位置dataPosition()读取Map到outVal
     * 
     * @param outVal
     *            Map
     * @param loader
     *            类加载器
     */
    public final void readMap(Map outVal, ClassLoader loader) {
        int N = readInt();
        readMapInternal(outVal, N, loader);
    }

    /**
     * 从当前位置dataPosition()读取HashMap
     * 
     * @param loader
     *            类加载器
     * @return HashMap
     */
    public final HashMap readHashMap(ClassLoader loader) {
        int N = readInt();
        if (N < 0)
            return null;

        HashMap map = new HashMap(N);
        readMapInternal(map, N, loader);
        return map;
    }

    private void readMapInternal(Map outVal, int N, ClassLoader loader) {
        while (N > 0) {
            Object key = readValue(loader);
            Object value = readValue(loader);
            outVal.put(key, value);
            N--;
        }
    }

    /**
     * 从当前位置dataPosition()创建boolean[]
     * 
     * @return boolean[]
     */
    public final boolean[] createBooleanArray() {
        int N = readInt();

        if (N >= 0 && N <= (dataAvail() >> 2)) {
            boolean[] val = new boolean[N];
            int i = 0;
            while (i < N) {
                val[i] = readInt() != 0;
                i++;
            }

            return val;

        } else {
            return null;
        }
    }

    /**
     * 从当前位置dataPosition()读取boolean[]到val
     * 
     * @param val
     *            boolean[]
     */
    public final void readBooleanArray(boolean[] val) {
        int N = readInt();
        if (N == val.length) {
            int i = 0;
            while (i < N) {
                val[i] = readInt() != 0;
                i++;
            }

        } else {
            throw new RuntimeException("bad array lengths");
        }
    }

    /**
     * 从当前位置dataPosition()创建byte[]
     * 
     * @return byte[]
     */
    public final byte[] createByteArray() {
        return nativeCreateByteArray(m_nativePtr);
    }

    /**
     * 从当前位置dataPosition()读取byte[]到val
     * 
     * @param val
     *            byte[]
     */
    public final void readByteArray(byte[] val) {
        byte[] ba = createByteArray();
        if (ba.length == val.length) {
            System.arraycopy(ba, 0, val, 0, ba.length);
        } else {
            throw new RuntimeException("bad array lengths");
        }
    }

    /**
     * 从当前位置dataPosition()读取String[]
     * 
     * @return String[]
     */
    public final String[] readStringArray() {
        int N = readInt();
        if (N < 0)
            return null;
        String val[] = new String[N];
        int i = 0;
        while (i < N) {
            val[i] = readString();
            i++;
        }

        return val;
    }

    /**
     * 从当前位置dataPosition()读取String[]到val
     * 
     * @param val
     *            String[]
     */
    public final void readStringArray(String[] val) {
        int N = readInt();
        if (N == val.length) {
            int i = 0;
            while (i < N) {
                val[i] = readString();
                i++;
            }
        } else {
            throw new RuntimeException("bad array lengths");
        }
    }

    /**
     * 从当前位置dataPosition()创建String[]
     * 
     * @return String[]
     */
    public final String[] createStringArray() {
        int N = readInt();
        if (N >= 0) {
            String[] val = new String[N];
            int i = 0;
            while (i < N) {
                val[i] = readString();
                i++;
            }
            return val;
        } else {
            return null;
        }
    }

    /**
     * 从当前位置dataPosition()读取CharSequence[]
     * 
     * @return CharSequence[]
     */
    public final CharSequence[] readCharSequenceArray() {
        int N = readInt();
        if (N < 0)
            return null;
        CharSequence array[] = new CharSequence[N];
        int i = 0;
        while (i < N) {
            array[i] = readCharSequence();
            i++;
        }

        return array;
    }

    /**
     * 从当前位置dataPosition()读取Object[]
     * 
     * @param loader
     *            类加载器
     * @return Object[]
     */
    public final Object[] readArray(ClassLoader loader) {
        int N = readInt();
        if (N < 0)
            return null;

        Object[] l = new Object[N];
        readArrayInternal(l, N, loader);
        return l;
    }

    private void readArrayInternal(Object[] outVal, int N, ClassLoader loader) {
        for (int i = 0; i < N; i++) {
            Object value = readValue(loader);
            outVal[i] = value;
        }
    }

    /**
     * 从当前位置dataPosition()创建int[]
     * 
     * @return int[]
     */
    public final int[] createIntArray() {
        int N = readInt();
        if (N >= 0 && N <= (dataAvail() >> 2)) {
            int[] val = new int[N];
            int i = 0;
            while (i < N) {
                val[i] = readInt();
                i++;
            }
            return val;
        } else {
            return null;
        }
    }

    /**
     * 从当前位置dataPosition()读取int[]到val
     * 
     * @param val
     *            int[]
     */
    public final void readIntArray(int[] val) {
        int N = readInt();
        if (N == val.length) {
            int i = 0;
            while (i < N) {
                val[i] = readInt();
                i++;
            }
        } else {
            throw new RuntimeException("bad array lengths: ");
        }
    }

    /**
     * 从当前位置dataPosition()读取long[]到val
     * 
     * @param val
     *            long[]
     */
    public final void readLongArray(long[] val) {
        int N = readInt();
        if (N == val.length) {
            int i = 0;
            while (i < N) {
                val[i] = readLong();
                i++;
            }
        } else {
            throw new RuntimeException("bad array lengths");
        }
    }

    /**
     * 从当前位置dataPosition()创建long[]
     * 
     * @return long[]
     */
    public final long[] createLongArray() {
        int N = readInt();
        if (N >= 0 && N <= (dataAvail() >> 3)) {
            long[] val = new long[N];
            int i = 0;
            while (i < N) {
                val[i] = readLong();
                i++;
            }
            return val;
        } else {
            return null;
        }
    }

    /**
     * 从当前位置dataPosition()创建char[]
     * 
     * @return char[]
     */
    public final char[] createCharArray() {
        int N = readInt();
        if (N >= 0 && N <= (dataAvail() >> 2)) {
            char[] val = new char[N];
            int i = 0;
            while (i < N) {
                val[i] = (char) readInt();
                i++;
            }
            return val;

        } else {
            return null;
        }
    }

    /**
     * 从当前位置dataPosition()读取char[]到val
     * 
     * @param val
     *            char[]
     */
    public final void readCharArray(char[] val) {
        int N = readInt();
        if (N == val.length) {
            int i = 0;
            while (i < N) {
                val[i] = (char) readInt();
                i++;
            }
        } else {
            throw new RuntimeException("bad array lengths");
        }
    }

    /**
     * 从当前位置dataPosition()创建float[]
     * 
     * @return float[]
     */
    public final float[] createFloatArray() {
        int N = readInt();
        if (N >= 0 && N <= (dataAvail() >> 2)) {
            float[] val = new float[N];
            int i = 0;
            while (i < N) {
                val[i] = readFloat();
                i++;
            }
            return val;
        } else {
            return null;
        }
    }

    /**
     * 从当前位置dataPosition()读取float[]到val
     * 
     * @param val
     *            float[]
     */
    public final void readFloatArray(float[] val) {
        int N = readInt();
        if (N == val.length) {
            int i = 0;
            while (i < N) {
                val[i] = readFloat();
                i++;
            }
        } else {
            throw new RuntimeException("bad array lengths");
        }
    }

    /**
     * 从当前位置dataPosition()创建double[]
     * 
     * @return double[]
     */
    public final double[] createDoubleArray() {
        int N = readInt();
        if (N >= 0 && N <= (dataAvail() >> 3)) {
            double[] val = new double[N];
            int i = 0;
            while (i < N) {
                val[i] = readDouble();
                i++;
            }
            return val;
        } else {
            return null;
        }
    }

    /**
     * 从当前位置dataPosition()读取double[]到val
     * 
     * @param val
     *            double[]
     */
    public final void readDoubleArray(double[] val) {
        int N = readInt();
        if (N == val.length) {
            int i = 0;
            while (i < N) {
                val[i] = readDouble();
                i++;
            }
        } else {
            throw new RuntimeException("bad array lengths");
        }
    }

    /**
     * 从当前位置dataPosition()读取SafeParcelable
     * 
     * @param loader
     *            类加载器
     * @return SafeParcelable
     */
    public final <T extends SafeParcelable> T readParcelable(ClassLoader loader) {
        SafeParcelable.Creator<T> creator = readParcelableCreator(loader);
        if (creator == null)
            return null;

        if (creator instanceof SafeParcelable.ClassLoaderCreator<?>)
            return ((SafeParcelable.ClassLoaderCreator<T>) creator)
                    .createFromParcel(this, loader);

        return creator.createFromParcel(this);
    }

    private final <T extends SafeParcelable> T readCreator(
            SafeParcelable.Creator<T> creator, ClassLoader loader) {
        if (creator instanceof SafeParcelable.ClassLoaderCreator<?>) {
            return ((SafeParcelable.ClassLoaderCreator<T>) creator)
                    .createFromParcel(this, loader);
        }
        return creator.createFromParcel(this);
    }

    private static final HashMap<ClassLoader, HashMap<String, SafeParcelable.Creator>> sCreators = new HashMap<ClassLoader, HashMap<String, SafeParcelable.Creator>>();

    private final <T extends SafeParcelable> SafeParcelable.Creator<T> readParcelableCreator(
            ClassLoader loader) {
        String name = readString();
        if (name == null)
            return null;

        SafeParcelable.Creator<T> creator;
        synchronized (sCreators) {
            HashMap<String, SafeParcelable.Creator> map = sCreators.get(loader);
            if (map == null) {
                map = new HashMap<String, SafeParcelable.Creator>();
                sCreators.put(loader, map);
            }
            creator = map.get(name);
            if (creator == null) {
                try {
                    Class c = loader == null ? Class.forName(name) : Class
                            .forName(name, true, loader);
                    Field f = c.getField("CREATOR");
                    creator = (SafeParcelable.Creator) f.get(null);
                } catch (IllegalAccessException e) {
                    IwdsLog.e(this, "Illegal access when unmarshalling: "
                            + name, e);
                    throw new BadSafeParcelableException(
                            "IllegalAccessException when unmarshalling: "
                                    + name);
                } catch (ClassNotFoundException e) {
                    IwdsLog.e(this, "Class not found when unmarshalling: "
                            + name, e);
                    throw new BadSafeParcelableException(
                            "ClassNotFoundException when unmarshalling: "
                                    + name);
                } catch (ClassCastException e) {
                    throw new BadSafeParcelableException(
                            "SafeParcelable protocol requires a "
                                    + "SafeParcelable.Creator object called "
                                    + " CREATOR on class " + name);
                } catch (NoSuchFieldException e) {
                    throw new BadSafeParcelableException(
                            "SafeParcelable protocol requires a "
                                    + "SafeParcelable.Creator object called "
                                    + " CREATOR on class " + name);
                } catch (NullPointerException e) {
                    throw new BadSafeParcelableException(
                            "SafeParcelable protocol requires "
                                    + "the CREATOR object to be static on class "
                                    + name);
                }
                if (creator == null) {
                    throw new BadSafeParcelableException(
                            "SafeParcelable protocol requires a "
                                    + "SafeParcelable.Creator object called "
                                    + " CREATOR on class " + name);
                }

                map.put(name, creator);
            }
        }

        return creator;
    }

    /**
     * 从当前位置dataPosition()读取SafeParcelable[]
     * 
     * @param loader
     *            类加载器
     * @return SafeParcelable[]
     */
    public final SafeParcelable[] readParcelableArray(ClassLoader loader) {
        int N = readInt();
        if (N < 0)
            return null;

        SafeParcelable[] p = new SafeParcelable[N];
        int i = 0;
        while (i < N) {
            p[i] = (SafeParcelable) readParcelable(loader);
            i++;
        }
        return p;
    }

    /**
     * 从当前位置dataPosition()读取Serializable
     * 
     * @return Serializable
     */
    public final Serializable readSerializable() {
        return readSerializable(null);
    }

    private final Serializable readSerializable(final ClassLoader loader) {
        String name = readString();
        if (name == null)
            return null;

        byte[] serializedData = createByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
        try {
            ObjectInputStream ois = new ObjectInputStream(bais) {
                @Override
                protected Class<?> resolveClass(ObjectStreamClass osClass)
                        throws IOException, ClassNotFoundException {
                    if (loader != null) {
                        Class<?> c = Class.forName(osClass.getName(), false,
                                loader);
                        if (c != null) {
                            return c;
                        }
                    }
                    return super.resolveClass(osClass);
                }
            };
            return (Serializable) ois.readObject();

        } catch (IOException ioe) {
            throw new RuntimeException("SafeParcelable encountered "
                    + "IOException reading a Serializable object (name = "
                    + name + ")", ioe);

        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException(
                    "SafeParcelable encountered "
                            + "ClassNotFoundException reading a Serializable object (name = "
                            + name + ")", cnfe);
        }
    }

    /*
     * TODO: public me
     */
    private final RemoteBundle readBundle() {
        return readBundle(null);
    }

    /*
     * TODO: public me
     */
    private final RemoteBundle readBundle(ClassLoader loader) {
        int length = readInt();
        if (length < 0)
            return null;

        /*
         * TODO: open me
         */
        // final RemoteBundle bundle = new RemoteBundle(this, length);
        // if (loader != null)
        // bundle.setClassLoader(loader);

        // return bundle;

        return null;
    }

    /**
     * 从当前位置dataPosition()创建ArrayList<T>
     * 
     * @param c
     *            SafeParcelable.Creator
     * @return ArrayList<T>
     */
    public final <T> ArrayList<T> createTypedArrayList(
            SafeParcelable.Creator<T> c) {
        int N = readInt();
        if (N < 0)
            return null;

        ArrayList<T> l = new ArrayList<T>(N);
        while (N > 0) {
            if (readInt() != 0) {
                l.add(c.createFromParcel(this));

            } else {
                l.add(null);
            }
            N--;
        }

        return l;
    }

    /**
     * 从当前位置dataPosition()读取ArrayList<T>到list
     * 
     * @param c
     *            SafeParcelable.Creator
     */
    public final <T> void readTypedList(List<T> list,
            SafeParcelable.Creator<T> c) {
        int M = list.size();
        int N = readInt();
        int i = 0;

        for (; i < M && i < N; i++) {
            if (readInt() != 0) {
                list.set(i, c.createFromParcel(this));
            } else {
                list.set(i, null);
            }
        }

        for (; i < N; i++) {
            if (readInt() != 0) {
                list.add(c.createFromParcel(this));
            } else {
                list.add(null);
            }
        }

        for (; i < M; i++) {
            list.remove(N);
        }
    }

    /**
     * 从当前位置dataPosition()创建ArrayList<String>
     * 
     * @return ArrayList<String>
     */
    public final ArrayList<String> createStringArrayList() {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        ArrayList<String> l = new ArrayList<String>(N);
        while (N > 0) {
            l.add(readString());
            N--;
        }
        return l;
    }

    /**
     * 从当前位置dataPosition()读取List<String>到list
     * 
     * @param list
     *            List<String>
     */
    public final void readStringList(List<String> list) {
        int M = list.size();
        int N = readInt();
        int i = 0;

        for (; i < M && i < N; i++) {
            list.set(i, readString());
        }

        for (; i < N; i++) {
            list.add(readString());
        }

        for (; i < M; i++) {
            list.remove(N);
        }
    }

    /**
     * 从当前位置dataPosition()创建T[]
     * 
     * @param c
     *            SafeParcelable.Creator
     * @return T[]
     */
    public final <T> T[] createTypedArray(SafeParcelable.Creator<T> c) {
        int N = readInt();
        if (N < 0)
            return null;

        T[] l = c.newArray(N);
        for (int i = 0; i < N; i++) {
            if (readInt() != 0) {
                l[i] = c.createFromParcel(this);
            }
        }

        return l;
    }

    /**
     * 从当前位置dataPosition()读取T[]到val
     * 
     * @param val
     *            T[]
     * @param c
     *            SafeParcelable.Creator
     */
    public final <T> void readTypedArray(T[] val, SafeParcelable.Creator<T> c) {
        int N = readInt();
        if (N == val.length) {
            for (int i = 0; i < N; i++) {
                if (readInt() != 0) {
                    val[i] = c.createFromParcel(this);
                } else {
                    val[i] = null;
                }
            }

        } else {
            throw new RuntimeException("bad array lengths");
        }
    }

    /**
     * 从当前位置dataPosition()读取Object
     * 
     * @param loader
     *            类加载器
     * @return Object
     */
    public final Object readValue(ClassLoader loader) {
        int type = readInt();

        switch (type) {
        case VAL_NULL:
            return null;

        case VAL_STRING:
            return readString();

        case VAL_INTEGER:
            return readInt();

        case VAL_MAP:
            return readHashMap(loader);

        case VAL_SAFEPARCELABLE:
            return readParcelable(loader);

        case VAL_SHORT:
            return (short) readInt();

        case VAL_LONG:
            return readLong();

        case VAL_FLOAT:
            return readFloat();

        case VAL_DOUBLE:
            return readDouble();

        case VAL_BOOLEAN:
            return readInt() == 1;

        case VAL_CHARSEQUENCE:
            return readCharSequence();

        case VAL_LIST:
            return readArrayList(loader);

        case VAL_BOOLEANARRAY:
            return createBooleanArray();

        case VAL_BYTEARRAY:
            return createByteArray();

        case VAL_STRINGARRAY:
            return readStringArray();

        case VAL_CHARSEQUENCEARRAY:
            return readCharSequenceArray();

        case VAL_OBJECTARRAY:
            return readArray(loader);

        case VAL_INTARRAY:
            return createIntArray();

        case VAL_LONGARRAY:
            return createLongArray();

        case VAL_BYTE:
            return readByte();

        case VAL_SERIALIZABLE:
            return readSerializable();

        case VAL_SAFEPARCELABLEARRAY:
            return readParcelableArray(loader);

        case VAL_REMOTEBUNDLE:
            return readBundle(loader); // loading will be deferred

        default:
            int offset = dataPosition() - 4;
            throw new RuntimeException("SafeParcel " + this
                    + ": Unmarshalling unknown type code " + type
                    + " at offset " + offset);
        }
    }
}
