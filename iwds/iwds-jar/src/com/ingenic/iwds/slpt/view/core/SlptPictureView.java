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
import com.ingenic.iwds.slpt.view.utils.*;

/**
 * 设置一张图片，并且显示出来<br>
 * 一般SlptPictureView的子类不会动态的改变要显示的图片<br>
 * 而是动态的改变图片的显示方式<br>
 */
public class SlptPictureView extends SlptViewComponent {
	String str = null;
	Picture picture = null;

	/**
	 * 设置字符串作为将要显示的图片，图片会被立即转换出来<br>
	 * 如下字段需要提交设置，以生成特定效果的图片<br>
	 * SlptViewCompent.textSize<br>
	 * SlptViewCompent.textColor<br>
	 * SlptViewCompent.typeface<br>
	 */
	public void setStringPicture(String str) {
		if (picture != null)
			picture.recycle();

		this.str = str;
		StringPicture picture = new StringPicture(str);
		picture.setTextSize(textSize);
		picture.setTypeFace(typeface);
		picture.setTextColor(textColor);

		this.picture = picture;
	}

	/**
	 * 设置字符作为将要显示的图片，图片会被立即转换出来<br>
	 * 
	 * @see setStringPicture(String str)
	 */
	public void setStringPicture(char ch) {
		if (picture != null)
			picture.recycle();

		this.str = "" + ch;
		StringPicture picture = new StringPicture("" + ch);
		picture.setTextSize(textSize);
		picture.setTypeFace(typeface);
		picture.setTextColor(textColor);

		this.picture = picture;
	}

	@Override
	public void setTextAttr(float textSize, int textColor, Typeface typeface) {
		super.setTextAttr(textSize, textColor, typeface);
		if (str != null)
			setStringPicture(str);
	}

	/**
	 * 设置byte[[作为将要显示的图片，图片会在写入slpt之前生成<br>
	 * 图片由BitmapFactory.decodeByteArray(byte[] mem)进行解析
	 */
	public void setImagePicture(byte[] mem) {
		if (picture != null)
			picture.recycle();

		picture = new ImagePicture(mem);
	}

	/**
	 * 设置图片路径作为将要显示的图片，图片会在写入slpt之前生成<br>
	 * 图片由BitmapFactory.decodeFile(String path)进行解析
	 */
	public void setImagePicture(String path) {
		if (picture != null)
			picture.recycle();

		picture = new ImagePicture(path);
	}

	@Override
	protected short initType() {
		return SVIEW_PIC;
	}

	@Override
	void registerPicture(PictureContainer container, RegisterPictureParam param) {
		RegisterPictureParam mParam = param.clone();

		if (background.picture != null)
			mParam.backgroundColor = SlptViewComponent.INVALID_COLOR;
		else if (background.color != INVALID_COLOR)
			mParam.backgroundColor = background.color;

		super.registerPicture(container, mParam);

		picture.setBackgroundColor(mParam.backgroundColor);
		container.add(picture);
	}

	@Override
	void writeConfigure(KeyWriter writer) {
		super.writeConfigure(writer);
		writePicture(writer, picture);
	}
}
