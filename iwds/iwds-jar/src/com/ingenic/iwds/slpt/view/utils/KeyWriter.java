/*
 *  Copyright (C) 2015 Ingenic Semiconductor
 *
 *  Wu Jiao <jiao.wu@ingenic.com, wujiaososo@qq.com>
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

package com.ingenic.iwds.slpt.view.utils;

import com.ingenic.iwds.utils.IwdsAssert;

public class KeyWriter {
	public static String TAG = "KeyWriter";

	private long jniPrivate;
	private boolean nativeIsInitialized;

	static {
		// System.loadLibrary("key-parser");
	}

	public KeyWriter() {
		jniPrivate = initialize_native();
		nativeIsInitialized = true;
	}

	public long getJniPrivate() {
		return jniPrivate;
	}

	public void initialize() {
		if (!nativeIsInitialized) {
			jniPrivate = initialize_native();
			nativeIsInitialized = true;
		}
	}

	public void recycle() {
		if (nativeIsInitialized) {
			recycle(jniPrivate);
			jniPrivate = 0;
			nativeIsInitialized = false;
		}
	}

	public byte[] getRawBytes() {
		checkInitialization();
		return getBytes(jniPrivate);
	}

	public int getSize() {
		checkInitialization();
		return getSize(jniPrivate);
	}

	public void writeByte(byte val) {
		checkInitialization();
		writeByte(jniPrivate, val);
	}

	public void writeBoolean(boolean val) {
		checkInitialization();
		writeBoolean(jniPrivate, (byte) (val ? 1 : 0));
	}

	public void writeShort(short val) {
		checkInitialization();
		writeShort(jniPrivate, val);
	}

	public void writeInt(int val) {
		checkInitialization();
		writeInt(jniPrivate, val);
	}

	public void writeLong(long val) {
		checkInitialization();
		writeLong(jniPrivate, val);
	}

	public void writeFloat(float val) {
		checkInitialization();
		writeFloat(jniPrivate, val);
	}

	public void writeDouble(double val) {
		checkInitialization();
		writeDouble(jniPrivate, val);
	}

	public void writeString(String str) {
		checkInitialization();
		IwdsAssert.dieIf(TAG, str == null, "String is null!");
		writeString(jniPrivate, str);
	}

	public void writeByteArray(byte[] array, int position, int length) {
		checkArray(array, array.length, position, length);
		writeByteArray(jniPrivate, array, position, length);
	}

	public void writeBooleanArray(boolean[] array, int position, int length) {
		checkArray(array, array.length, position, length);
		writeBooleanArray(jniPrivate, array, position, length);
	}

	public void writeShortArray(short[] array, int position, int length) {
		checkArray(array, array.length, position, length);
		writeShortArray(jniPrivate, array, position, length);
	}

	public void writeIntArray(int[] array, int position, int length) {
		checkArray(array, array.length, position, length);
		writeIntArray(jniPrivate, array, position, length);
	}

	public void writeLongArray(long[] array, int position, int length) {
		checkArray(array, array.length, position, length);
		writeLongArray(jniPrivate, array, position, length);
	}

	public void writeFloatArray(float[] array, int position, int length) {
		checkArray(array, array.length, position, length);
		writeFloatArray(jniPrivate, array, position, length);
	}

	public void writeDoubleArray(double[] array, int position, int length) {
		checkArray(array, array.length, position, length);
		writeDoubleArray(jniPrivate, array, position, length);
	}

	private void checkInitialization() {
		IwdsAssert.dieIf(TAG, nativeIsInitialized == false,
				"KeyWriter is recycled!");
	}

	private void checkArray(Object array, int arrayLength, int position,
			int length) {
		IwdsAssert.dieIf(TAG, nativeIsInitialized == false,
				"KeyWriter is recycled!");

		IwdsAssert.dieIf(TAG, arrayLength < (position + length),
				"write length out of array!");
	}

	private native long initialize_native();
	
	private native void recycle(long jniPrivate);

	private native byte[] getBytes(long jniPrivate);

	private native int getSize(long jniPrivate);

	private native void writeByte(long jniPrivate, byte val);

	private native void writeBoolean(long jniPrivate, byte val);

	private native void writeShort(long jniPrivate, short val);

	private native void writeInt(long jniPrivate, int val);

	private native void writeLong(long jniPrivate, long val);

	private native void writeFloat(long jniPrivate, float val);

	private native void writeDouble(long jniPrivate, double val);

	private native void writeString(long jniPrivate, String str);

	private native void writeByteArray(long jniPrivate, byte[] array,
			int position, int length);

	private native void writeBooleanArray(long jniPrivate, boolean[] array,
			int position, int length);

	private native void writeShortArray(long jniPrivate, short[] array,
			int position, int length);

	private native void writeIntArray(long jniPrivate, int[] array,
			int position, int length);

	private native void writeLongArray(long jniPrivate, long[] array,
			int position, int length);

	private native void writeFloatArray(long jniPrivate, float[] array,
			int position, int length);

	private native void writeDoubleArray(long jniPrivate, double[] array,
			int position, int length);
}
