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

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;

public class BmpCreator extends Paint {
	private static final boolean DEBUG = false;
	private static final String TAG = "BmpCreator";

	private Canvas canvas;
	private Paint paint;
	private Rect bounds = new Rect();
	private Bitmap bitmap = null;
	private int backgroundColor = 0x00ffffff;

	public Bitmap getBitmap() {
		return bitmap != null ? Bitmap.createBitmap(bitmap) : null;
	}

	public Bitmap getBitmap(int x, int y, int width, int height) {
		return bitmap != null ? Bitmap
				.createBitmap(bitmap, x, y, width, height) : null;
	}

	public Bitmap getBitmapNoCopy() {
		return bitmap;
	}

	public void setBackgroundColor(int color) {
		backgroundColor = color;
	}

	public void recycle() {
		if (bitmap != null)
			if (!bitmap.isRecycled())
				bitmap.recycle();
		bitmap = null;
	}

	int getWidth(int width) {
		Align align = paint.getTextAlign();
		if (align == Align.LEFT)
			return 0;
		else if (align == Align.CENTER)
			return width / 2;
		else if (align == Align.RIGHT)
			return width;
		return 0;
	}

	void createBitmap(int width, int height) {
		if (canvas == null) {
			canvas = new Canvas();
		}
		if (bitmap != null)
			if (!bitmap.isRecycled())
				bitmap.recycle();
		bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		canvas.setBitmap(bitmap);
	}

	public BmpCreator() {
		super();
		paint = this;
		paint.setTextSize(100);
		paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
		paint.setTextAlign(Align.LEFT);
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);
	}

	public void decodeString(String str, int start, int end) {
		decodeString(str.substring(start, end));
	}

	public void decodeString(String str) {
		FontMetrics fontMetrics;
		int width, height;
		int x, y;

		paint.getTextBounds(str, 0, str.length(), bounds);
		width = bounds.right;

		fontMetrics = paint.getFontMetrics();
		height = (int) (fontMetrics.descent - fontMetrics.ascent + 0.5);
		x = getWidth(width);
		y = (int) (height - fontMetrics.descent); // y is the baseline

		createBitmap(width, height);
		canvas.drawColor(backgroundColor);
		canvas.drawText(str, x, y, paint);

		if (DEBUG)
			Log.d(TAG, "decode " + str + " width=" + width + " height="
					+ height + " x=" + x + " y=" + y);
	}

	public void decodeChar(char ch) {
		FontMetrics fontMetrics;
		int width1;
		int width2;
		int width, height;
		int x, y;

		paint.getTextBounds("" + ch, 0, 1, bounds);
		width1 = bounds.right;

		paint.getTextBounds("" + ch + ch, 0, 2, bounds);
		width2 = bounds.right;

		width = width2 - width1;

		fontMetrics = paint.getFontMetrics();
		height = (int) (fontMetrics.descent - fontMetrics.ascent + 0.5);
		x = getWidth(width);
		y = (int) (height - fontMetrics.descent); // y is the baseline

		createBitmap(width, height);
		canvas.drawColor(backgroundColor);
		canvas.drawText("" + ch, x, y, paint);

		if (DEBUG)
			Log.d(TAG, "decode " + ch + " width=" + width + " height=" + height
					+ " x=" + x + " y=" + y);
	}

	public Boolean saveToFile(String path) {
		Boolean ret;
		BmpSaver bmp = BmpSaver.create(bitmap);

		ret = bmp.saveTofile(path);
		bmp.recycle();

		return ret;
	}
}
