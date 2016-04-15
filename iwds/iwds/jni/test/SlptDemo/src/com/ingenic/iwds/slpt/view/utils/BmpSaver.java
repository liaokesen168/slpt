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

import java.util.ArrayList;
import android.graphics.Bitmap;
import android.util.Log;

public class BmpSaver {
	private static final boolean DEBUG = false;
	private static final String TAG = "BmpSaver";
	private static final int DEFUALT_SIZE = 10 * 1024;
	private static ArrayList<BmpSaver> list = null;

	public int bfType;
	public int bfSize;
	public int bfReserved1;
	public int bfReserved2;
	public int bfOffBits;

	public int biSize;
	public int biWidth;
	public int biHeight;
	public int biPlanes;
	public int biBitCount;
	public int biCompression;
	public int biSizeImage;
	public int biXPlesPerMeter;
	public int biYPlesPerMeter;
	public int biClrUsed;
	public int biClrImportant;

	public int paletteSize;
	public int width;
	public int height;

	public byte[] mem;

	private int index;
	private boolean isAvailable;

	static {
		list = new ArrayList<BmpSaver>();
	}

	public static BmpSaver create(Bitmap bitmap) {
		int width = bitmap.getWidth(), height = bitmap.getHeight();
		int lineLen = (width * 3 + 4) & ~0x03;
		int buffSize = height * lineLen;
		int paletteSize = 0; // no palette
		int bfSize;
		BmpSaver bmp;

		bfSize = 14 + 40 + paletteSize + buffSize;
		synchronized (list) {
			bmp = getAvailable(bfSize);
		}
		bmp.createInner(bitmap);

		return bmp;
	}

	public void recycle() {
		synchronized (list) {
			isAvailable = true;
		}
	}

	private static BmpSaver getAvailable(int bufferSize) {
		int delta = Integer.MAX_VALUE;
		int index = -1;
		BmpSaver bmp = null;

		for (int i = 0; i < list.size(); i++) {
			bmp = list.get(i);
			if (bmp.isAvailable && bmp.mem != null
					&& bmp.mem.length >= bufferSize) {
				if ((bmp.mem.length - bufferSize) < delta) {
					delta = bmp.mem.length - bufferSize;
					index = i;
					if (delta == 0) {
						break;
					}
				}
			}
		}

		if (index < 0) {
			bmp = new BmpSaver(bufferSize);
			if (DEBUG)
				Log.d(TAG, "allocate new bmpSaver " + bufferSize);
			list.add(bmp);
		} else {
			bmp = list.get(index);
			if (DEBUG)
				Log.d(TAG, "choice bmpSaver " + index + "size "
						+ bmp.mem.length);
		}

		bmp.bfSize = bufferSize;
		bmp.isAvailable = false;

		return bmp;
	}

	public Boolean saveTofile(String path) {
		return mem != null ? SimpleFile.writeFile(path, mem) : false;
	}

	private BmpSaver(int bufferSize) {
		isAvailable = false;
		bfSize = bufferSize;
		mem = new byte[bufferSize];
	}

	private void saveWord(int value) {
		mem[index++] = (byte) (value & 0xff);
		mem[index++] = (byte) ((value >> 8) & 0xff);
	}

	private void saveDword(int value) {
		mem[index++] = (byte) (value & 0xff);
		mem[index++] = (byte) ((value >> 8) & 0xff);
		mem[index++] = (byte) ((value >> 16) & 0xff);
		mem[index++] = (byte) ((value >> 24) & 0xff);
	}

	private void createInner(Bitmap bitmap) {
		int width = bitmap.getWidth(), height = bitmap.getHeight();
		int lineLen = (width * 3 + 4) & ~0x03;
		int buffSize = height * lineLen;
		int paletteSize = 0; // no palette

		/* bmp header info */
		bfType = 0x4d42; // BM
		bfSize = 14 + 40 + paletteSize + buffSize;
		bfReserved1 = 0;
		bfReserved2 = 0;
		bfOffBits = 14 + 40 + paletteSize;

		/* bmp info */
		biSize = 40;
		biWidth = width;
		biHeight = height;
		biPlanes = 1;
		biBitCount = 24;
		biCompression = 0; // not compression
		biSizeImage = 0;
		biXPlesPerMeter = 0;
		biYPlesPerMeter = 0;
		biClrUsed = 0;
		biClrImportant = 0;

		index = 0;

		saveWord(bfType);
		saveDword(bfSize);
		saveWord(bfReserved1);
		saveWord(bfReserved2);
		saveDword(bfOffBits);

		saveDword(biSize);
		saveDword(biWidth);
		saveDword(biHeight);
		saveWord(biPlanes);
		saveWord(biBitCount);
		saveDword(biCompression);
		saveDword(biSizeImage);
		saveDword(biXPlesPerMeter);
		saveDword(biYPlesPerMeter);
		saveDword(biClrUsed);
		saveDword(biClrImportant);

		if (DEBUG)
			printBmpIfno();

		int i, j;
		int byteCount = bfOffBits;
		int step = (4 - ((3 * width) % 4)) % 4;
		for (i = height - 1; i >= 0; i--) {
			for (j = 0; j < width; j++) {
				int color = bitmap.getPixel(j, i);
				if ((color & 0xff000000) == 0x00) {
					mem[byteCount++] = (byte) 0xff;
					mem[byteCount++] = (byte) 0xff;
					mem[byteCount++] = (byte) 0xff;
				} else if ((color & 0x00ffffff) == 0x00ffffff) {
					mem[byteCount++] = (byte) 0xfe;
					mem[byteCount++] = (byte) 0xff;
					mem[byteCount++] = (byte) 0xff;
				} else {
					mem[byteCount++] = (byte) (color & 0xff);
					mem[byteCount++] = (byte) (color >> 8 & 0xff);
					mem[byteCount++] = (byte) (color >> 16 & 0xff);
				}
			}
			byteCount += step;
		}
		if (DEBUG)
			Log.d(TAG, "bytecount: " + byteCount);
		if (DEBUG)
			Log.d(TAG, "step: " + step);
	}

	private void printBmpIfno() {
		Log.d(TAG, "--------------------------------");
		Log.d(TAG, "bfType: " + bfType);
		Log.d(TAG, "bfSize: " + bfSize);
		Log.d(TAG, "bfOffBits: " + bfOffBits);
		Log.d(TAG, "biSize: " + biSize);
		Log.d(TAG, "biWidth: " + biWidth);
		Log.d(TAG, "biHeight: " + biHeight);
		Log.d(TAG, "biBitCount: " + biBitCount);
		Log.d(TAG, "biSizeImage: " + biSizeImage);
		Log.d(TAG, "--------------------------------");
	}
}
