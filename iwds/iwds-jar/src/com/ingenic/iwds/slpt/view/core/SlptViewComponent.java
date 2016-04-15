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

import android.graphics.Typeface;

import com.ingenic.iwds.slpt.view.core.Picture.ImagePicture;
import com.ingenic.iwds.slpt.view.core.Picture.PictureContainer;
import com.ingenic.iwds.slpt.view.utils.KeyWriter;

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
	public static final int INVALID_COLOR = 0x00ffffff;
	protected static final int INVALID_COLOR_IN_NATIVE = 0xffffffff;

	/* align */
	public static final byte ALIGN_LEFT = 0;
	public static final byte ALIGN_RIGHT = 1;
	public static final byte ALIGN_TOP = 0;
	public static final byte ALIGN_BOTTOM = 1;
	public static final byte ALIGN_CENTER = 2;
	public static final byte ALIGN_BY_PARENT = 3;

	/**
	 * sview的rect.width/height 适配背景图片
	 */
	public static final byte RECT_FIT_BACKGROUND = 0;
	/**
	 * 适配内容大小，包括背景图片
	 */
	public static final byte RECT_WRAP_CONTENT = 1;
	/**
	 * 指定大小
	 */
	public static final byte RECT_SPECIFY = 2;

	/**
	 * SlptLinearLayout中的成员按水平方向排列
	 */
	public static final byte HORIZONTAL = 0;
	/**
	 * 按垂直方向排列
	 */
	public static final byte VERTICAL = 1;

	/**
	 * SlptAbsoluteLayout中， 子sview的坐标起始点水平方向上在sview的左边
	 */
	public static final byte POSITION_LEFT = 0;
	/**
	 * 水平方向上坐标起始点在sview的右边
	 */
	public static final byte POSITION_RIGHT = 1;
	/**
	 * 垂直方向上坐标起始点在sview的顶端
	 */
	public static final byte POSITION_TOP = 0;
	/**
	 * 垂直方向上坐标起始点在sview的底端
	 */
	public static final byte POSITION_BOTTOM = 1;
	/**
	 * 水平/垂直方向上坐标起始点居中
	 */
	public static final byte POSITION_CENTER = 2;

	/* those following members are match with native code */
	public final short type = initType();

	/**
	 * 横坐标： 在absolute layout 中起作用
	 */
	public int x = 0;

	/**
	 * 纵坐标： 在absolute layout 中起作用
	 */
	public int y = 0;

	/**
	 * sview周围的空白区域
	 * <p>
	 * left：左边的空白的距离<br>
	 * right：右边的空白的距离<br>
	 * top：顶端的空白的距离<br>
	 * bottom：底端的空白的距离<br>
	 */
	public Padding padding = new Padding();

	/**
	 * sview 的矩形大小设置<br>
	 * <br>
	 * width：宽度<br>
	 * height：高度<br>
	 */
	public Rect rect = new Rect();

	/**
	 * sview 的背景<br>
	 * <br>
	 * color：背景颜色，SlptViewComponent.INVALID_COLOR 为无效颜色<br>
	 * picture：背景图片<br>
	 */
	public Background background = new Background();

	/**
	 * sview 在layout中绘制顺序<br>
	 * <br>
	 * level越小，越先被绘制<br>
	 */
	public short level = 0;

	/**
	 * sview 中内容的水平方向的对齐方式<br>
	 * <br>
	 * SlptViewComponent.ALIGN_LEFT：靠左对齐<br>
	 * SlptViewComponent.ALIGN_RIGHT：靠右对齐<br>
	 * SlptViewComponent.ALIGN_CENTER 居中对齐<br>
	 */
	public byte alignX = ALIGN_LEFT;

	/**
	 * sview 中内容的垂直方向的对齐方式<br>
	 * <br>
	 * SlptViewComponent.ALIGN_TOP：靠顶端对齐<br>
	 * SlptViewComponent.ALIGN_BOTTOM：靠底端对齐<br>
	 * SlptViewComponent.ALIGN_CENTER 居中对齐<br>
	 */
	public byte alignY = ALIGN_TOP;

	/**
	 * 描述 sview 矩形的宽度<br>
	 * <br>
	 * SlptViewComponent.RECT_FIT_BACKGROUND：适配背景图片的大小<br>
	 * SlptViewComponent.RECT_WRAP_CONTENT：适配sview内容的大小（包括背景图片）<br>
	 * SlptViewComponent.RECT_SPECIFY：由SlptViewComponent.rect.width 决定<br>
	 */
	public byte descWidth = RECT_WRAP_CONTENT;

	/**
	 * 描述 sview 矩形的高度<br>
	 * <br>
	 * SlptViewComponent.RECT_FIT_BACKGROUND：适配背景图片的大小<br>
	 * SlptViewComponent.RECT_WRAP_CONTENT：适配sview内容的大小（包括背景图片）<br>
	 * SlptViewComponent.RECT_SPECIFY：由SlptViewComponent.rect.height 决定<br>
	 */
	public byte descHeight = RECT_WRAP_CONTENT;

	/**
	 * 是否垂直居中<br>
	 * <br>
	 * 0: 否<br>
	 * 1：是<br>
	 */
	public byte centerVertical = 0;

	/**
	 * 是否水平居中<br>
	 * <br>
	 * 0: 否<br>
	 * 1：是<br>
	 */
	public byte centerHorizontal = 0;

	/**
	 * sview 在layout中水平方向的对齐方式<br>
	 * <br>
	 * SlptViewComponent.ALIGN_BY_PARENT：由layout的alignX指定<br>
	 * SlptViewComponent.ALIGN_LEFT：靠左对齐<br>
	 * SlptViewComponent.ALIGN_RIGHT：靠右对齐<br>
	 * SlptViewComponent.ALIGN_CENTER 居中对齐<br>
	 */
	public byte alignParentX = ALIGN_BY_PARENT;

	/**
	 * sview 在layout中垂直方向的对齐方式<br>
	 * <br>
	 * SlptViewComponent.ALIGN_BY_PARENT：由layout的alignY指定<br>
	 * SlptViewComponent.ALIGN_TOP：靠顶端对齐<br>
	 * SlptViewComponent.ALIGN_BOTTOM：靠底端对齐<br>
	 * SlptViewComponent.ALIGN_CENTER 居中对齐<br>
	 */
	public byte alignParentY = ALIGN_BY_PARENT;

	/**
	 * 是否显示<br>
	 * <br>
	 * 0: 否<br>
	 * 1：是<br>
	 */
	public boolean show = true;

	/* those following members are just in java code */

	/**
	 * 字体大小
	 */
	public float textSize = 30;

	/**
	 * 字体颜色<br>
	 * 颜色格式32位 ARGB 格式<br>
	 */
	public int textColor = 0xffff0101;

	/**
	 * 字体
	 */
	public Typeface typeface = Typeface.DEFAULT;

	/**
	 * 所有的sview都可以指定背景，Background用于描述它<br>
	 * 当同时指定color 和 picture 的时候，color不会生效<br>
	 */
	public static class Background {
		/**
		 * 背景颜色，格式：ARGB(8888)，但透明度的设置将被忽略
		 */
		public int color = INVALID_COLOR;
		/**
		 * 背景图片
		 */
		public ImagePicture picture = null;
	}

	/**
	 * 所有的sview都是一个矩形，Padding用于描述矩形四个方向的空白区域 主要作用是用于sview之间划分间隔
	 */
	public static class Padding {
		/**
		 * 矩形左边空白区域的长度
		 */
		public short left = 0;
		/**
		 * 右边的长度
		 */
		public short right = 0;
		/**
		 * 顶端的长度
		 */
		public short top = 0;
		/**
		 * 底端的长度
		 */
		public short bottom = 0;
	}

	/**
	 * @see SlptViewComponent.RECT_FIT_BACKGROUND
	 */
	public static final int LAYOUT_FIT_BACKGROUND = Integer.MAX_VALUE;
	/**
	 * @see SlptViewComponent.RECT_WRAP_CONTENT
	 */
	public static final int LAYOUT_WRAP_CONTENT = Integer.MAX_VALUE - 1;

	/**
	 * 所有的sview都是矩形，Rect用于描述sview的矩形大小
	 */
	public static class Rect {
		/**
		 * 矩形的宽度
		 */
		public int width;
		/**
		 * 矩形的高度
		 */
		public int height;
	}

	// waiting for override
	protected abstract short initType();

	// layout层次上的父view， 如果暂时不属于任何layout就是null
	protected SlptViewComponent parent = null;

	/**
	 * com.ingenic.iwds.slpt.SlptClock 使用的方法<br>
	 * <br>
	 * 子类应该重写此方法 <br>
	 */
	void registerPicture(PictureContainer container, RegisterPictureParam param) {
		if (background.picture != null)
			container.add(background.picture);
	}

	/**
	 * com.ingenic.iwds.slpt.SlptClock 使用的方法<br>
	 * <br>
	 * 子类应该重写此方法 <br>
	 */
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
		writer.writeInt((background.color & 0xff000000) == 0x00 ? INVALID_COLOR_IN_NATIVE
				: background.color);
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

	/**
	 * 设置view在SlptAbsoluteLayout中的位置
	 * 
	 * @see SlptViewComponent.x
	 * @see SlptViewComponent.y
	 */
	public void setStart(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * 设置字体的属性:字体大小，字体颜色，字体
	 * 
	 * @see SlptViewComponent.textSize
	 * @see SlptViewComponent.textColor
	 * @see SlptViewComponent.typeface
	 */
	public void setTextAttr(float textSize, int textColor, Typeface typeface) {
		this.textSize = textSize;
		this.textColor = textColor;
		this.typeface = typeface;
	}

	/**
	 * 设置padding：left, right, top, bottom
	 * 
	 * @see SlptViewComponent.padding
	 */
	public void setPadding(int left, int right, int top, int bottom) {
		padding.left = (short) left;
		padding.right = (short) right;
		padding.top = (short) top;
		padding.bottom = (short) bottom;
	}

	/**
	 * 设置padding
	 * 
	 * @see SlptViewComponent.padding
	 */
	public void setPadding(Padding padding) {
		this.padding.left = padding.left;
		this.padding.right = padding.right;
		this.padding.top = padding.top;
		this.padding.bottom = padding.bottom;
	}

	private void setWidth(int width) {
		switch (width) {
		case LAYOUT_FIT_BACKGROUND:
			descWidth = RECT_FIT_BACKGROUND;
			break;
		case LAYOUT_WRAP_CONTENT:
			descWidth = RECT_WRAP_CONTENT;
			break;
		default:
			descWidth = RECT_SPECIFY;
			rect.width = width;
			break;
		}
	}

	private void setHeight(int height) {
		switch (height) {
		case LAYOUT_FIT_BACKGROUND:
			descHeight = RECT_FIT_BACKGROUND;
			break;
		case LAYOUT_WRAP_CONTENT:
			descHeight = RECT_WRAP_CONTENT;
			break;
		default:
			descHeight = RECT_SPECIFY;
			rect.height = height;
			break;
		}
	}

	/**
	 * 设置rect：width，hight, 另外可以接受如下参数用于指定特殊效果<br>
	 * SlptViewComponent.LAYOUT_FIT_BACKGROUND<br>
	 * SlptViewComponent.LAYOUT_WRAP_CONTENT<br>
	 * 
	 * @see SlptViewComponent.Rect
	 * @see SlptViewComponent.LAYOUT_FIT_BACKGROUND
	 * @see SlptViewComponent.LAYOUT_WRAP_CONTENT
	 */
	public void setRect(int width, int height) {
		setWidth(width);
		setHeight(height);
	}

	/**
	 * 获得父view，即当前view所处的layout
	 */
	public SlptViewComponent getParent() {
		return parent;
	}

	/**
	 * 获得最顶端的父view，即view所处的根layout
	 */
	public SlptViewComponent getRootParent() {
		SlptViewComponent view = this;
		SlptViewComponent parent = null;

		while (view.parent != null) {
			parent = view.parent;
			view = parent;
		}

		return view != this ? view : null; 
	}  

	public static final class RegisterPictureParam {
		public int backgroundColor;

		public RegisterPictureParam clone() {
			RegisterPictureParam param = new RegisterPictureParam();
			param.backgroundColor = this.backgroundColor;
			return param;
		}
	}
}
