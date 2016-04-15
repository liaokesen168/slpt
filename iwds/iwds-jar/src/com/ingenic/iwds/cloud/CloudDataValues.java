/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  WangLianCheng <liancheng.wang@ingenic.com>
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
 */

package com.ingenic.iwds.cloud;

import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.os.Parcel;
import android.os.Parcelable;

import com.ingenic.iwds.utils.IwdsAssert;

/**
 * 云数据类。存放数据库的一行数据，一行数据包含多项数据，每项数据以 key-value 的方式存放。
 */
public final class CloudDataValues implements Parcelable {
    private HashMap<String, Object> mValues;

    /**
     * 构造
     */
    public CloudDataValues() {
        mValues = new HashMap<String, Object>(10);
    }

    /**
     * 构造
     * @param  size 指定集合的初始大小。
     */
    public CloudDataValues(int size) {
        mValues = new HashMap<String, Object>(size);
    }

    /**
     * 构造，从另一个 CloudDataValues 复制内容。
     * @param  from 要复制的 CloudDataValues。
     */
    public CloudDataValues(CloudDataValues from) {
        mValues = new HashMap<String, Object>(from.mValues);
    }

    /**
     * 构造
     * @param  values [description]
     */
    private CloudDataValues(HashMap<String, Object> values) {
        mValues = values;
    }

    /**
     * 比较这个对象和另一个对象是否相等。
     * @param  object 比较的对象。
     * @return        true 相等，false不相等。
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CloudDataValues)) {
            return false;
        }
        return mValues.equals(((CloudDataValues) object).mValues);
    }

    @Override
    public int hashCode() {
        return mValues.hashCode();
    }

    /**
     * 放入一个 String 类型的数据
     * @param key   数据名
     * @param value 数据值
     */
    public void put(String key, String value) {
        IwdsAssert.dieIf(this, key == null, "key is null");
        IwdsAssert.dieIf(this, value == null, "value is null");

        mValues.put(key, value);
    }

    /**
     * 放入一个 Byte 类型的数据
     * @param key   数据名
     * @param value 数据值
     */
    public void put(String key, Byte value) {
        IwdsAssert.dieIf(this, key == null, "key is null");
        IwdsAssert.dieIf(this, value == null, "value is null");

        mValues.put(key, value);
    }

    /**
     * 放入一个 Short 类型的数据
     * @param key   数据名
     * @param value 数据值
     */
    public void put(String key, Short value) {
        IwdsAssert.dieIf(this, key == null, "key is null");
        IwdsAssert.dieIf(this, value == null, "value is null");

        mValues.put(key, value);
    }

    /**
     * 放入一个 Integer 类型的数据
     * @param key   数据名
     * @param value 数据值
     */
    public void put(String key, Integer value) {
        IwdsAssert.dieIf(this, key == null, "key is null");
        IwdsAssert.dieIf(this, value == null, "value is null");

        mValues.put(key, value);
    }

    /**
     * 放入一个 Long 类型的数据
     * @param key   数据名
     * @param value 数据值
     */
    public void put(String key, Long value) {
        IwdsAssert.dieIf(this, key == null, "key is null");
        IwdsAssert.dieIf(this, value == null, "value is null");

        mValues.put(key, value);
    }

    /**
     * 放入一个 Float 类型的数据
     * @param key   数据名
     * @param value 数据值
     */
    public void put(String key, Float value) {
        IwdsAssert.dieIf(this, key == null, "key is null");
        IwdsAssert.dieIf(this, value == null, "value is null");

        mValues.put(key, value);
    }

    /**
     * 放入一个 Double 类型的数据
     * @param key   数据名
     * @param value 数据值
     */
    public void put(String key, Double value) {
        IwdsAssert.dieIf(this, key == null, "key is null");
        IwdsAssert.dieIf(this, value == null, "value is null");

        mValues.put(key, value);
    }

    /**
     * 放入一个 Boolean 类型的数据
     * @param key   数据名
     * @param value 数据值
     */
    public void put(String key, Boolean value) {
        IwdsAssert.dieIf(this, key == null, "key is null");
        IwdsAssert.dieIf(this, value == null, "value is null");

        mValues.put(key, value);
    }

    /**
     * 放入一个 byte 数组类型的数据
     * @param key   数据名
     * @param value 数据值
     */
    public void put(String key, byte[] value) {
        IwdsAssert.dieIf(this, key == null, "key is null");
        IwdsAssert.dieIf(this, value == null, "value is null");

        mValues.put(key, value);
    }

    /**
     * 放入一个 Object 类型的数据。如果object 是一个不支持的类型将会引发。
     * InputMismatchException 异常。
     * @param key   数据名
     * @param value 数据值
     */
    public void put(String key, Object value) {
        IwdsAssert.dieIf(this, key == null, "key is null");
        IwdsAssert.dieIf(this, value == null, "value is null");

        if (value instanceof String) {
            put(key, (String)value);
        } else if (value instanceof Integer) {
            put(key, (Integer)value);
        } else if (value instanceof Long) {
            put(key, (Long)value);
        } else if (value instanceof Short) {
            put(key, (Short)value);
        } else if (value instanceof Byte) {
            put(key, (Byte)value);
        } else if (value instanceof Float) {
            put(key, (Float)value);
        } else if (value instanceof Double) {
            put(key, (Double)value);
        } else if (value instanceof Boolean) {
            put(key, (Boolean)value);
        } else if (value instanceof byte[]) {
            put(key, (byte[])value);
        } else {
            throw new InputMismatchException("Cannot set object value of unsupported type: "
                + value.getClass().getName());
        }
    }

    /**
     * 获取指定数据名的 String 类型数据。如果数据存入是是非 String 类型，将会转换成 String 类型。
     * @param  key 指定要访问的数据的名称。
     * @return     String 类型的数据值。
     */
    public String getString(String key) {
        Object value = mValues.get(key);

        if (value instanceof String) {
            return (String)value;
        } else {
            return value.toString();
        }
    }

    /**
     * 获取指定数据名的 Byte 类型数据值。
     * @param  key 指定要访问的数据的名称。
     * @return     Byte 类型的数据值。
     */
    public Byte getByte(String key) throws NumberFormatException {
        Object value = mValues.get(key);

        if (value instanceof Byte) {
            return (Byte) value;
        } else if (value instanceof Number) {
            return ((Number) value).byteValue();
        } else if (value instanceof String) {
            return Byte.parseByte((String)value);
        }
        return null;
    }

    /**
     * 获取指定数据名的 Short 类型数据值。
     * @param  key 指定要访问的数据的名称。
     * @return     Short 类型的数据值。
     */
    public Short getShort(String key) throws NumberFormatException {
        Object value = mValues.get(key);

        if (value instanceof Short) {
            return (Short) value;
        } else if (value instanceof Number) {
            return ((Number) value).shortValue();
        } else if (value instanceof String) {
            return Short.parseShort((String)value);
        }
        return null;
    }

    /**
     * 获取指定数据名的 Integer 类型数据值。
     * @param  key 指定要访问的数据的名称。
     * @return     Integer 类型的数据值。
     */
    public Integer getInteger(String key) throws NumberFormatException {
        Object value = mValues.get(key);

        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            return Integer.parseInt((String)value);
        }
        return null;
    }

    /**
     * 获取指定数据名的 Long 类型数据值。
     * @param  key 指定要访问的数据的名称。
     * @return     Long 类型的数据值。
     */
    public Long getLong(String key) throws NumberFormatException {
        Object value = mValues.get(key);

        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            return Long.parseLong((String)value);
        }
        return null;
    }

    /**
     * 获取指定数据名的 Float 类型数据值。
     * @param  key 指定要访问的数据的名称。
     * @return     Float 类型的数据值。
     */
    public Float getFloat(String key) throws NumberFormatException {
        Object value = mValues.get(key);

        if (value instanceof Float) {
            return (Float) value;
        } else if (value instanceof Number) {
            return ((Number) value).floatValue();
        } else if (value instanceof String) {
            return Float.parseFloat((String)value);
        }
        return null;
    }

    /**
     * 获取指定数据名的 Double 类型数据值。
     * @param  key 指定要访问的数据的名称。
     * @return     Double 类型的数据值。
     */
    public Double getDouble(String key) throws NumberFormatException {
        Object value = mValues.get(key);

        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            return Double.parseDouble((String)value);
        }
        return null;
    }

    /**
     * 获取指定数据名的 Boolean 类型数据值。
     * @param  key 指定要访问的数据的名称。
     * @return     Boolean 类型的数据值。
     */
    public Boolean getBoolean(String key) throws NumberFormatException {
        Object value = mValues.get(key);

        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof Number) {
            long v = ((Number) value).longValue();
            return (v != 0);
        } else if (value instanceof String) {
            return Boolean.getBoolean((String)value);
        }
        return null;
    }

    /**
     * 获取指定数据名的 Byte 数组类型数据值。
     * @param  key 指定要访问的数据的名称。
     * @return     Byte数组类型的数据值。
     */
    public byte[] getByteArray(String key) throws NumberFormatException {

        Object value = mValues.get(key);

        if (value instanceof byte[]) {
            return (byte[]) value;
        }

        return null;
    }

    /**
     * 获取指定数据名的数据值。
     * @param  key 指定要访问的数据的名称。
     * @return     数据值。
     */
    public Object get(String key) {
        return mValues.get(key);
    }

    /**
     * 放入另一个 CloudDataValues 中的全部数据。
     * @param other 要放入的另一个 CloudDataValues。
     */
    public void putAll(CloudDataValues other) {
        mValues.putAll(other.mValues);
    }

    /**
     * 获取数据集合的容量。
     * @return 集合容量。
     */
    public int size() {
        return mValues.size();
    }

    /**
     * 移除一条数据。
     * @param  key 指定要移除的数据。
     * @return     被移除的数据。
     */
    public Object remove(String key) {
        return mValues.remove(key);
    }

    /**
     * 清除集合中的所有数据。
     */
    public void clear() {
        mValues.clear();
    }

    /**
     * 检查集合中是否包含某个数据。
     * @param  key 要检查的数据名。
     * @return     true 包含，false 不包含。
     */
    public boolean containsKey(String key) {
        return mValues.containsKey(key);
    }

    /**
     * 获取数据名集合
     * @return 数据名集合
     */
    public Set<String> keySet() {
        return mValues.keySet();
    }

    public static final Parcelable.Creator<CloudDataValues> CREATOR =
            new Parcelable.Creator<CloudDataValues>() {
        public CloudDataValues createFromParcel(Parcel in) {
            HashMap<String, Object> values = in.readHashMap(null);
            return new CloudDataValues(values);
        }

        public CloudDataValues[] newArray(int size) {
            return new CloudDataValues[size];
        }
    };

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeMap(mValues);
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, Object>> iter = mValues.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Object> ent = iter.next();
            String name = ent.getKey();
            Object value = ent.getValue();

            String str = (value == null) ? "<NULL>" : value.toString();
            sb.append(name + "=" + str + ", ");
        }

        return sb.toString();
    }
}
