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

import java.util.InputMismatchException;

import com.ingenic.iwds.utils.IwdsAssert;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 云数据查询条件类。支持子条件，可以通过子条件组合成复杂条件。
 */
public class CloudQuery implements Parcelable {
    /**
     * 条件操作符
     */
    public static enum Operator {
        /**
         * 等于
         */
        EQUALS,

        /**
         * 不等于
         */
        NOT_EQUAL,

        /**
         * 大于
         */
        GREATER_THAN,

        /**
         * 小于
         */
        LESS_THAN,

        /**
         * 大于等于
         */
        GREATER_THAN_EQUAL_TO,

        /**
         * 小于等于
         */
        LESS_THAN_EQUAL_TO,

        /**
         * 起始于
         */
        START_WITH,

        /**
         * 结束于
         */
        END_WITH,

        /**
         * 与
         */
        AND,

        /**
         * 或
         */
        OR,

        /**
         * 非
         */
        NOT
    }

    private Boolean mNot = false;
    private Operator mOperator;
    private Object mObject1;
    private Object mObject2;

    /**
     * 构造
     * @param  key      条件的数据名
     * @param  operator 条件的类型。采用字符串表达，比如“>”、“!=”
     * @param  value    条件的数据值
     */
    public CloudQuery(String key, String operator, Object value) {
        init(toArithOperator(operator), key, value);
    }

    /**
     * 构造
     * @param  key      条件的数据名
     * @param  operator 条件的类型
     * @param  value    条件的数据值
     */
    public CloudQuery(String key, Operator operator, Object value) {
        init(operator, key, value);
    }

    private CloudQuery() {

    }

    /**
     * 构造
     * @param  subQuery1 子条件
     * @param  operator  条件的类型
     * @param  subQuery2 子条件
     */
    public CloudQuery(CloudQuery subQuery1, Operator operator, CloudQuery subQuery2) {
        mOperator = operator;
        mObject1 = subQuery1;
        mObject2 = subQuery2;
    }

    private static Operator toArithOperator(String operator) {
        String s = operator.trim().toLowerCase();
        Operator c;

        if (">".equals(s)) {
            c = Operator.GREATER_THAN;
        } else if (">=".equals(s)) {
            c = Operator.GREATER_THAN_EQUAL_TO;
        } else if ("<".equals(s)) {
            c = Operator.LESS_THAN;
        } else if ("<=".equals(s)) {
            c = Operator.LESS_THAN_EQUAL_TO;
        } else if ("==".equals(s)) {
            c = Operator.EQUALS;
        } else if ("=".equals(s)) {
            c = Operator.EQUALS;
        } else if ("!=".equals(s)) {
            c = Operator.NOT_EQUAL;
        } else if ("<>".equals(s)) {
            c = Operator.NOT_EQUAL;
/*
        } else if ("*%".equals(s)) {
            c = Operator.START_WITH;
        } else if ("like%".equals(s)) {
            c = Operator.START_WITH;
        } else if ("%*".equals(s)) {
            c = Operator.END_WITH;
        } else if ("%like".equals(s)) {
            c = Operator.END_WITH;
        } else if ("and".equals(s)) {
            c = Operator.AND;
        } else if ("or".equals(s)) {
            c = Operator.OR;
*/
    /*
        } else if ("not".equals(s)) {
            c = Operator.NOT;
    */
        } else {
            throw new InputMismatchException("unsupported operator: " + operator);
        }

        return c;
    }

    private void init(Operator operator, String key, Object value) {
        if (!( (value instanceof Boolean)
            || (value instanceof Number)
            || (value instanceof String))) {
            IwdsAssert.dieIf(this, true, "unsupported type: " + value.getClass().getName());
        }

        if ((operator == Operator.AND) || (operator == Operator.OR)) {
            if (!(value instanceof CloudQuery)) {
                IwdsAssert.dieIf(this, true, "value is must CloudQuery when type is AND/OR");
            }
        }

        if (operator == Operator.NOT) {

        }

        mOperator = operator;
        mObject1 = key;
        mObject2 = value;
        mNot = false;
    }

    /**
     * 获取条件的数据名
     * @return 返回数据名，如果此条件是由子条件构成，则key无效，返回null。
     */
    public String getKey() {
        if (mObject1 instanceof String) {
            return (String)mObject1;
        } else {
            return null;
        }
    }

    /**
     * 获取条件的类型
     * @return 返回条件的类型。只返回二元操作，不返回一元操作，比如 NOT 不被返回。
     */
    public Operator getOperator() {
        return mOperator;
    }

    /**
     * 获取条件的数据值
     * @return 返回数据值，如果此条件是由子条件构成，返回null。
     */
    public Object getValue() {
        if (!(mObject2 instanceof CloudQuery)) {
            return mObject2;
        } else {
            return null;
        }
    }

    /**
     * 获取第一个子条件
     * @return 返回子条件对象，如果没有子条件返回null。
     */
    public CloudQuery getSubQuery1() {
        if (mObject1 instanceof CloudQuery) {
            return (CloudQuery)mObject1;
        } else {
            return null;
        }
    }

    /**
     * 获取第二个子条件
     * @return 返回子条件对象，如果没有子条件返回null。
     */
    public CloudQuery getSubQuery2() {
        if (mObject2 instanceof CloudQuery) {
            return (CloudQuery)mObject2;
        } else {
            return null;
        }
    }

    /**
     * 把本条件和另一个条件进行“与”操作
     * @param  query “与”操作的条件
     * @return       返回“与”操作后的新条件
     */
    public CloudQuery and(CloudQuery query) {
        return new CloudQuery(this, Operator.AND, query);
    }

    /**
     * 把本条件和另一个条件进行“或”操作
     * @param  query “或”操作的条件
     * @return       返回“或”操作后的新条件
     */
    public CloudQuery or(CloudQuery query) {
        return new CloudQuery(this, Operator.OR, query);
    }

    /**
     * 把本条件进行“非”操作
     * @return 返回“非”操作后的本条件
     */
    public CloudQuery not() {
        mNot = !(mNot);
        return this;
    }

    /**
     * 判断本条件是否有“非”操作
     * @return true 有非，false 没有非。
     */
    public boolean isNot() {
        return mNot;
    }

    public static final Parcelable.Creator<CloudQuery> 
            CREATOR = new Parcelable.Creator<CloudQuery>() {

        public CloudQuery createFromParcel(Parcel in) {
            CloudQuery query = new CloudQuery();

            boolean isCloudQuery = (in.readInt() != 0);

            query.mOperator = Operator.values()[in.readInt()];
            if (isCloudQuery) {
                query.mObject1 = in.readValue(CloudQuery.class.getClassLoader());
                query.mObject2 = in.readValue(CloudQuery.class.getClassLoader());
            } else {
                query.mObject1 = in.readValue(null);
                query.mObject2 = in.readValue(null);
            }
            query.mNot = (in.readInt() != 0);

            return query;
        }

        public CloudQuery[] newArray(int size) {
            return new CloudQuery[size];
        }
    };

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(mObject1 instanceof CloudQuery ? 1 : 0);

        parcel.writeInt(mOperator.ordinal());
        parcel.writeValue(mObject1);
        parcel.writeValue(mObject2);
        parcel.writeInt(mNot ? 1 : 0);
    }

    @Override
    public int describeContents() {

        return 0;
    }

}

