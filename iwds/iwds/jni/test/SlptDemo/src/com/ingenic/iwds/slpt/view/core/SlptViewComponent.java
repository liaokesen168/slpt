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

package com.ingenic.iwds.slpt.view.core;

import java.util.ArrayList;

import com.ingenic.iwds.slpt.view.core.Picture.*;

import com.ingenic.iwds.slpt.view.utils.KeyWriter;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;

public abstract class SlptViewComponent {
	// those following types just in java code, native code do not know them
	protected static final short SVIEW_LAYOUT = -1;

	// those following types are match with native code, do not change the order
	protected static short I = 0;
	protected static final short SVIEW_PIC = I++;
	protected static final short SVIEW_NUM = I++;
	protected static final short SVIEW_LINEAR_LAYOUT = I++;
	protected static final short SVIEW_ABSOLUTE_LAYOUT = I++;
	protected static final short SVIEW_FRAME_LAYOUT = I++;
	protected static final short SVIEW_TIME_NUM = I++;
	protected static final short SVIEW_SECOND_L = I++;
	protected static final short SVIEW_SECOND_H = I++;
	protected static final short SVIEW_MINUTE_L = I++;
	protected static final short SVIEW_MINUTE_H = I++;
	protected static final short SVIEW_HOUR_L = I++;
	protected static final short SVIEW_HOUR_H = I++;
	protected static final short SVIEW_DAY_L = I++;
	protected static final short SVIEW_DAY_H = I++;
	protected static final short SVIEW_WEEK = I++;
	protected static final short SVIEW_MONTH_L = I++;
	protected static final short SVIEW_MONTH_H = I++;
	protected static final short SVIEW_YEAR0 = I++;
	protected static final short SVIEW_YEAR1 = I++;
	protected static final short SVIEW_YEAR2 = I++;
	protected static final short SVIEW_YEAR3 = I++;
	protected static final short SVIEW_ROTATE_PIC = I++;
	protected static final short SVIEW_ANALOG_TIME = I++;
	protected static final short SVIEW_ANALOG_SECOND = I++;
	protected static final short SVIEW_ANALOG_MINUTE = I++;
	protected static final short SVIEW_ANALOG_HOUR = I++;
	protected static final short SVIEW_ANALOG_DAY = I++;
	protected static final short SVIEW_ANALOG_WEEK = I++;
	protected static final short SVIEW_ANALOG_MONTH = I++;
	protected static final short SVIEW_ANALOG_AM_PM = I++;
	protected static final short SVIEW_ANALOG_HOUR_WITH_MINUTE = I++;

	protected static final String INVALID_PICTURE_NAME = "invalid_picture";
	protected static final int INVALID_COLOR = 0xffffffff;

	/* align */
	public static final byte ALIGN_LEFT = 0;
	public static final byte ALIGN_RIGHT = 1;
	public static final byte ALIGN_TOP = 0;
	public static final byte ALIGN_BOTTOM = 1;
	public static final byte ALIGN_CENTER = 2;
	public static final byte ALIGN_BY_PARENT = 3;

	/* rect descript */
	public static final byte RECT_FIT_BACKGROUND = 0;
	public static final byte RECT_WRAP_CONTENT = 1;
	public static final byte RECT_SPECIFY = 2;

	/* orientation of linear layout */
	public static final byte HORIZONTAL = 0;
	public static final byte VERTICAL = 1;
	
	/* start point position of absolute layout */
	public static final byte POSITION_LEFT = 0;
	public static final byte POSITION_RIGHT = 1;
	public static final byte POSITION_TOP = 0;
	public static final byte POSITION_BOTTOM = 1;
	public static final byte POSITION_CENTER = 2;
	
	/* those following members are match with native code */
	public final short type = initType();
	public int x = 0;
	public int y = 0;
	public Padding padding = new Padding();
	public Rect rect = new Rect();
	public Background background = new Background();
	public short level = 0;
	public byte alignX = ALIGN_LEFT;
	public byte alignY = ALIGN_TOP;
	public byte descWidth = RECT_WRAP_CONTENT;
	public byte descHeight = RECT_WRAP_CONTENT;
	public byte centerVertical = 0;
	public byte centerHorizontal = 0;
	public byte alignParentX = ALIGN_BY_PARENT;
	public byte alignParentY = ALIGN_BY_PARENT;
	public boolean show = true;

	/* those following members are just in java code */
	public float textSize = 30;
	public int textColor = 0xff000000;
	public Typeface typeface = Typeface.DEFAULT;

	public static class Background {
		public int color = INVALID_COLOR;
		ImagePicture picture = null;
	}

	public static class Padding {
		public short left = 0;
		public short right = 0;
		public short top = 0;
		public short bottom = 0;
	}

	public static class Rect {
		public int width;
		public int height;
	}

	// waiting for override
	protected abstract short initType();

	// waiting for override
	void registerPicture(PictureContainer container) {
		if (background.picture != null)
			container.add(background.picture);
	}

	// waiting for override
	void writeConfigure(KeyWriter writer) {
		writer.writeShort(type);
		writer.writeInt(x);
		writer.writeInt(y);
		writer.writeShort(padding.left);
		writer.writeShort(padding.right);
		writer.writeShort(padding.top);
		writer.writeShort(padding.bottom);
		writer.writeInt(rect.width);
		writer.writeInt(rect.height);
		writer.writeInt(background.color);
		writePicture(writer, background.picture);
		writer.writeShort(level);
		writer.writeByte(alignX);
		writer.writeByte(alignY);
		writer.writeByte(descWidth);
		writer.writeByte(descHeight);
		writer.writeByte(centerHorizontal);
		writer.writeByte(centerVertical);
		writer.writeByte(alignParentX);
		writer.writeByte(alignParentY);
		writer.writeBoolean(show);
	}

	static void writePicture(KeyWriter writer, Picture picture) {
		if (picture != null)
			writer.writeString(picture.getName());
		else
			writer.writeString(INVALID_PICTURE_NAME);
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setLevel(int level) {
		this.level = (short) level;
	}

	public int getLevel() {
		return level;
	}

	public boolean isShow() {
		return show;
	}

	public void setShow(boolean show) {
		this.show = show;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getX() {
		return x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getY() {
		return y;
	}
}
