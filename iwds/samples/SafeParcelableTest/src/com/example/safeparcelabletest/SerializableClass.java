package com.example.safeparcelabletest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SerializableClass implements Serializable {
    private static final long serialVersionUID = 498697371097777273L;

    private static final String TAG = "SerializableClass";

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

    public SerializableClass() {
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
    }

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
                + m_list + "]";
    }
}
