package com.example.safeparcelabletest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ingenic.iwds.os.SafeParcel;
import com.ingenic.iwds.os.SafeParcelable;

public class SafeParcelableClass implements SafeParcelable {
    private static final String TAG = "SafeParcelableClass";
    private final static int ARRAY_COUNT = 10;

    /*
     * Base type
     */
    private byte m_byte;
    private short m_short;
    private int m_int;
    private long m_long;
    private float m_float;
    private double m_double;
    private boolean m_boolean;
    private String m_string;
    private char m_char;
    private CharSequence m_charSequence;

    /*
     * Base type array
     */
    private byte[] m_byteArray;
    private int[] m_intArray;
    private long[] m_longArray;
    private float[] m_floatArray;
    private double[] m_doubleArray;
    private boolean[] m_booleanArray;
    private String[] m_stringArray;
    private char[] m_charArray;
    private CharSequence[] m_charSequenceArray;

    /*
     * Map
     */
    private HashMap<String, Object> m_map;

    /*
     * List
     */
    private List<String> m_list;

    /*
     * SafeParcelable & SafeParcel array
     */
    private SubSafeParcelable m_safeParcelable;
    private SubSafeParcelable[] m_safeParcelableArray;

    /*
     * Object array
     */
    private Object[] m_objectArray;

    /*
     * Serializable
     */
    private SerializableClass m_serializable;

    public SafeParcelableClass() {
        /*
         * Base type
         */
        m_byte = Byte.MAX_VALUE;
        m_short = Short.MAX_VALUE;
        m_int = Integer.MAX_VALUE;
        m_long = Long.MAX_VALUE;
        m_float = Float.MAX_VALUE;
        m_double = Double.MAX_VALUE;
        m_boolean = true;
        m_string = TAG;
        m_char = 'X';
        m_charSequence = TAG;

        /*
         * Base type array
         */
        m_byteArray = new byte[ARRAY_COUNT];
        m_intArray = new int[ARRAY_COUNT];
        m_longArray = new long[ARRAY_COUNT];
        m_floatArray = new float[ARRAY_COUNT];
        m_doubleArray = new double[ARRAY_COUNT];
        m_booleanArray = new boolean[ARRAY_COUNT];
        m_stringArray = new String[ARRAY_COUNT];
        m_charArray = new char[ARRAY_COUNT];
        m_charSequenceArray = new CharSequence[ARRAY_COUNT];

        for (int i = 0; i < ARRAY_COUNT; i++) {
            m_byteArray[i] = m_byte;
            m_intArray[i] = m_int;
            m_longArray[i] = m_long;
            m_floatArray[i] = m_float;
            m_doubleArray[i] = m_double;
            m_booleanArray[i] = m_boolean;
            m_stringArray[i] = m_string;
            m_charArray[i] = m_char;
            m_charSequenceArray[i] = m_charSequence;
        }

        /*
         * Map
         */
        m_map = new HashMap<String, Object>();
        m_map.put("Map 1", m_string);
        m_map.put("Map 2", m_string);
        m_map.put("Map 3", m_string);

        /*
         * List
         */
        m_list = new ArrayList<String>();
        m_list.add("List 1");
        m_list.add("List 2");
        m_list.add("List 3");

        /*
         * SafeParcelable & SafeParcel array
         */
        m_safeParcelable = new SubSafeParcelable();
        m_safeParcelableArray = new SubSafeParcelable[ARRAY_COUNT];
        for (int i = 0; i < ARRAY_COUNT; i++)
            m_safeParcelableArray[i] = m_safeParcelable;

        /*
         * Object array
         */
        m_objectArray = new Object[ARRAY_COUNT];
        for (int i = 0; i < ARRAY_COUNT; i++)
            m_objectArray[i] = m_map;

        /*
         * Serializable
         */
        m_serializable = new SerializableClass();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(SafeParcel dest, int flags) {
        dest.writeByte(m_byte);
        dest.writeInt(m_short);
        dest.writeInt(m_int);
        dest.writeLong(m_long);
        dest.writeFloat(m_float);
        dest.writeDouble(m_double);
        dest.writeInt(m_boolean ? 1 : 0);
        dest.writeString(m_string);
        dest.writeInt(m_char);
        dest.writeCharSequence(m_charSequence);

        dest.writeByteArray(m_byteArray);
        dest.writeIntArray(m_intArray);
        dest.writeLongArray(m_longArray);
        dest.writeFloatArray(m_floatArray);
        dest.writeDoubleArray(m_doubleArray);
        dest.writeBooleanArray(m_booleanArray);
        dest.writeStringArray(m_stringArray);
        dest.writeCharArray(m_charArray);
        dest.writeCharSequenceArray(m_charSequenceArray);

        dest.writeMap(m_map);

        dest.writeList(m_list);

        dest.writeParcelable(m_safeParcelable, flags);
        dest.writeTypedArray(m_safeParcelableArray, flags);

        dest.writeArray(m_objectArray);

        dest.writeSerializable(m_serializable);
    }

    public static final Creator<SafeParcelableClass> CREATOR = new Creator<SafeParcelableClass>() {

        @Override
        public SafeParcelableClass createFromParcel(SafeParcel source) {
            SafeParcelableClass object = new SafeParcelableClass();

            object.m_byte = source.readByte();
            object.m_short = (short) source.readInt();
            object.m_int = source.readInt();
            object.m_long = source.readLong();
            object.m_float = source.readFloat();
            object.m_double = source.readDouble();
            object.m_boolean = source.readInt() == 1;
            object.m_string = source.readString();
            object.m_char = (char) source.readInt();
            object.m_charSequence = source.readCharSequence();

            object.m_byteArray = source.createByteArray();
            source.readIntArray(object.m_intArray);
            source.readLongArray(object.m_longArray);
            source.readFloatArray(object.m_floatArray);
            source.readDoubleArray(object.m_doubleArray);
            source.readBooleanArray(object.m_booleanArray);
            object.m_stringArray = source.readStringArray();
            source.readCharArray(object.m_charArray);
            object.m_charSequenceArray = source.readCharSequenceArray();

            object.m_map = source.readHashMap(null);

            object.m_list = source.readArrayList(null);

            object.m_safeParcelable = source
                    .readParcelable(SubSafeParcelable.class.getClassLoader());

            source.readTypedArray(object.m_safeParcelableArray,
                    SubSafeParcelable.CREATOR);

            object.m_objectArray = source.readArray(null);

            object.m_serializable = (SerializableClass) source
                    .readSerializable();

            return object;
        }

        @Override
        public SafeParcelableClass[] newArray(int size) {
            return new SafeParcelableClass[size];
        }
    };

    @Override
    public String toString() {
        return "[m_byte=" + m_byte + ", m_short=" + m_short + ", m_int="
                + m_int + ", m_long=" + m_long + ", m_float=" + m_float
                + ", m_double=" + m_double + ", m_boolean=" + m_boolean
                + ", m_string=" + m_string + ", m_char=" + m_char
                + ", m_charSequence=" + m_charSequence + ", m_byteArray="
                + m_byteArray + ", m_intArray=" + m_intArray + ", m_longArray="
                + m_longArray + ", m_floatArray=" + m_floatArray
                + ", m_doubleArray=" + m_doubleArray + ", m_stringArray="
                + m_stringArray + ", m_charSequenceArray="
                + m_charSequenceArray + ", m_map=" + m_map + ", m_list="
                + m_list + ", m_safeParcelable=" + m_safeParcelable
                + ", m_safeParcelableArray=" + m_safeParcelable
                + ", m_objectArray=" + m_objectArray + ", m_serializable="
                + m_serializable + "]";
    }

    public static class SubSafeParcelable implements SafeParcelable {
        private static final String TAG = "SubSafeParcelable";

        private String m_string;

        public SubSafeParcelable() {
            m_string = TAG;
        }

        @Override
        public String toString() {
            return m_string;

        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(SafeParcel dest, int flags) {
            dest.writeString(m_string);
        }

        public static final Creator<SubSafeParcelable> CREATOR = new Creator<SubSafeParcelable>() {

            @Override
            public SubSafeParcelable createFromParcel(SafeParcel source) {
                SubSafeParcelable object = new SubSafeParcelable();

                object.m_string = source.readString();

                return object;
            }

            @Override
            public SubSafeParcelable[] newArray(int size) {
                return new SubSafeParcelable[size];
            }
        };
    }
}
