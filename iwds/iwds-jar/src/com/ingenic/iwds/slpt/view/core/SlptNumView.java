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

import com.ingenic.iwds.slpt.view.core.Picture.*;
import com.ingenic.iwds.slpt.view.core.SlptViewComponent.RegisterPictureParam;
import com.ingenic.iwds.slpt.view.utils.KeyWriter;

/**
 * 设置一组图片，并且指定一个序号，根据序号从图片组中选择图片进行显示<br>
 * 一般情况下SlptNumView的子类会动态的指定序号，从而达到显示不同图片的目的<br>
 */
public class SlptNumView extends SlptViewComponent {
	PictureGroup group = new PictureGroup(initCapacity());
	String[] strArray = new String[initCapacity()];

	/**
	 * 图片组的序号，用于选择图片组中的图片
	 */
	public int num;

	/**
	 * 将index位置的图片设置为一个字符串，这个字符串将会立即转换成一张图片<br>
	 * 如下字段需要提交设置，以生成特定效果的图片<br>
	 * SlptViewCompent.textSize<br>
	 * SlptViewCompent.textColor<br>
	 * SlptViewCompent.typeface<br>
	 * <br>
	 * index不能超过图片数组的范围<br>
	 */
	public boolean setStringPicture(int index, String str) {
		strArray[index] = str;
		StringPicture picture = new StringPicture(str);
		picture.setTextSize(textSize);
		picture.setTypeFace(typeface);
		picture.setTextColor(textColor);

		return group.set(index, picture);
	}

	/**
	 * 将index位置的图片设置为一个字符，这个字符将会立即转换成一张图片<br>
	 * 
	 * @see setStringPicture(int index, String str)
	 */
	public boolean setStringPicture(int index, char ch) {
		strArray[index] = "" + ch;
		StringPicture picture = new StringPicture("" + ch);
		picture.setTextSize(textSize);
		picture.setTypeFace(typeface);
		picture.setTextColor(textColor);

		return group.set(index, picture);
	}

	/**
	 * 将index位置的图片设置为byte[]，图片会在写入slpt之前生成<br>
	 * 图片由BitmapFactory.decodeByteArray(byte[] mem)进行解析
	 */
	public boolean setImagePicture(int index, byte[] mem) {
		ImagePicture picture = new ImagePicture(mem);
		return group.set(index, picture);
	}

	/**
	 * 将index位置的图片设置为一个图片路径，图片会在写入slpt之前生成<br>
	 * 图片由BitmapFactory.decodeFile(String path)进行解析
	 */
	public boolean setImagePicture(int index, String path) {
		ImagePicture picture = new ImagePicture(path);
		return group.set(index, picture);
	}

	/**
	 * 批量设置图片组，将图片都设置成为字符串转换后的图片
	 * 
	 * @see setStringPicture(int index, String str)
	 */
	public void setStringPictureArray(String[] array) {
		int length = array.length < group.capacity ? array.length
				: group.capacity;

		for (int i = 0; i < length; i++) {
			setStringPicture(i, array[i]);
		}
	}

	@Override
	public void setTextAttr(float textSize, int textColor, Typeface typeface) {
		super.setTextAttr(textSize, textColor, typeface);
		for (int i = 0; i < strArray.length; i++) {
			if (strArray[i] != null)
				setStringPicture(i, strArray[i]);
		}
	}

	/**
	 * 批量设置图片组，将图片都设置成为byte[]转换后的图片
	 * 
	 * @see setImagePicture(int index, byte[] mem)
	 */
	public boolean setImagePictureArray(byte[][] array) {
		int length = array.length < group.capacity ? array.length
				: group.capacity;

		for (int i = 0; i < length; i++) {
			if (array[i] == null)
				return false;
		}

		for (int i = 0; i < length; i++) {
			setImagePicture(i, array[i]);
		}

		return false;
	}

	/**
	 * 批量设置图片组，将图片都设置成为图片路径转换后的图片
	 * 
	 * @see setImagePicture(int index, String path)
	 */
	public boolean setImagePictureArray(String[] array) {
		int length = array.length < group.capacity ? array.length
				: group.capacity;

		for (int i = 0; i < length; i++) {
			if (array[i] == null)
				return false;
		}

		for (int i = 0; i < length; i++) {
			setImagePicture(i, array[i]);
		}

		return false;
	}

	// waiting for override
	protected int initCapacity() {
		return 10;
	}

	@Override
	protected short initType() {
		return SVIEW_NUM;
	}

	@Override
	void registerPicture(PictureContainer container, RegisterPictureParam param) {
		RegisterPictureParam mParam = param.clone();

		if (background.picture != null)
			mParam.backgroundColor = SlptViewComponent.INVALID_COLOR;
		else if (background.color != INVALID_COLOR)
			mParam.backgroundColor = background.color;

		super.registerPicture(container, mParam);

		group.setBackgroundColorForAll(mParam.backgroundColor);
		container.add(group);
	}

	@Override
	void writeConfigure(KeyWriter writer) {
		super.writeConfigure(writer);
		writer.writeInt(num);
		writer.writeString(group.getName());
	}
}
