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

import com.ingenic.iwds.slpt.view.core.Picture.*;
import com.ingenic.iwds.slpt.view.utils.KeyWriter;

public class SlptNumView extends SlptViewComponent {
	PictureGroup group = new PictureGroup(initCapacity());
	int num;

	public boolean setStringPicture(int index, String str) {
		StringPicture picture = new StringPicture(str);
		picture.setTextSize(textSize);
		picture.setTypeFace(typeface);
		picture.setTextColor(textColor);

		return group.set(index, picture);
	}

	public boolean setStringPicture(int index, char ch) {
		StringPicture picture = new StringPicture("" + ch);
		picture.setTextSize(textSize);
		picture.setTypeFace(typeface);
		picture.setTextColor(textColor);

		return group.set(index, picture);
	}

	public boolean setImagePicture(int index, byte[] mem) {
		ImagePicture picture = new ImagePicture(mem);
		return group.set(index, picture);
	}

	public boolean setImagePicture(int index, String path) {
		ImagePicture picture = new ImagePicture(path);
		return group.set(index, picture);
	}

	public boolean setStringPictureArray(String[] array) {
		int length = array.length < group.capacity ? array.length
				: group.capacity;

		for (int i = 0; i < length; i++) {
			if (array[i] == null)
				return false;
		}

		for (int i = 0; i < length; i++) {
			setStringPicture(i, array[i]);
		}

		return true;
	}

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
	void registerPicture(PictureContainer container) {
		super.registerPicture(container);
		container.add(group);
	}

	@Override
	void writeConfigure(KeyWriter writer) {
		super.writeConfigure(writer);
		writer.writeInt(num);
		writer.writeString(group.getName());
	}
}
