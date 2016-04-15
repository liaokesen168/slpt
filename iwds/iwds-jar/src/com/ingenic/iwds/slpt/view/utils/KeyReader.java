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

import android.util.Log;

public class KeyReader {
	public static String TAG = "KeyReader";
	private long jniPrivate;
	private boolean nativeIsInitialized = false;
	private int result_code = 0;

	static {
		try {
			// System.loadLibrary("key-parser");
			Log.d(TAG, "loadLibrary Successed!");
		} catch (Exception e) {
			Log.d(TAG, "loadLibrary Exception " + e.getMessage());
		}
	}

	public KeyReader(long writerJniPrivate) {
		if (writerJniPrivate != 0) {
			jniPrivate = initialize_native(writerJniPrivate);
			nativeIsInitialized = true;
		}
	}

	public long getJniPrivate() {
		return jniPrivate;
	}

	public void initialize(long writerJniPrivate) {
		if (!nativeIsInitialized) {
			jniPrivate = initialize_native(writerJniPrivate);
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

	public boolean isResultOk() {
		return result_code == 0;
	}

	public byte readByte() {
		checkInitialization();
		return readByte(jniPrivate);
	}

	public boolean readBoolean() {
		checkInitialization();
		return readBoolean(jniPrivate);
	}

	public short readShort() {
		checkInitialization();
		return readShort(jniPrivate);
	}

	public int readInt() {
		checkInitialization();
		return readInt(jniPrivate);
	}

	public long readLong() {
		checkInitialization();
		return readLong(jniPrivate);
	}

	public float readFloat() {
		checkInitialization();
		return readFloat(jniPrivate);
	}

	public double readDouble() {
		checkInitialization();
		return readDouble(jniPrivate);
	}

	public String readString() {
		checkInitialization();
		return readString(jniPrivate);
	}

	public byte[] readByteArray() {
		checkInitialization();
		return readByteArray(jniPrivate);
	}

	public boolean[] readBooleanArray() {
		checkInitialization();
		return readBooleanArray(jniPrivate);
	}

	public short[] readShortArray() {
		checkInitialization();
		return readShortArray(jniPrivate);
	}

	public int[] readIntArray() {
		checkInitialization();
		return readIntArray(jniPrivate);
	}

	public long[] readLongArray() {
		checkInitialization();
		return readLongArray(jniPrivate);
	}

	public float[] readFloatArray() {
		checkInitialization();
		return readFloatArray(jniPrivate);
	}

	public double[] readDoubleArray() {
		checkInitialization();
		return readDoubleArray(jniPrivate);
	}

	private void checkInitialization() {
		IwdsAssert.dieIf(TAG, nativeIsInitialized == false,
				"KeyWriter is recycled!");
		result_code = 0;
	}

	private native long initialize_native(long writerAddress);

	private native void recycle(long jniPrivate);

	private native byte readByte(long jniPrivate);

	private native boolean readBoolean(long jniPrivate);

	private native short readShort(long jniPrivate);

	private native int readInt(long jniPrivate);

	private native long readLong(long jniPrivate);

	private native float readFloat(long jniPrivate);

	private native double readDouble(long jniPrivate);

	private native String readString(long jniPrivate);

	private native byte[] readByteArray(long jniPrivate);

	private native boolean[] readBooleanArray(long jniPrivate);

	private native short[] readShortArray(long jniPrivate);

	private native int[] readIntArray(long jniPrivate);

	private native long[] readLongArray(long jniPrivate);

	private native float[] readFloatArray(long jniPrivate);

	private native double[] readDoubleArray(long jniPrivate);

}
