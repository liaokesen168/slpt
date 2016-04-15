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
import com.ingenic.iwds.slpt.view.utils.*;

public class SlptPictureView extends SlptViewComponent {
	Picture picture = null;

	public void setStringPicture(String str) {
		if (picture != null)
			picture.recycle();

		StringPicture picture = new StringPicture(str);
		picture.setTextSize(textSize);
		picture.setTypeFace(typeface);
		picture.setTextColor(textColor);

		this.picture = picture;
	}

	public void setStringPicture(char ch) {
		if (picture != null)
			picture.recycle();

		StringPicture picture = new StringPicture("" + ch);
		picture.setTextSize(textSize);
		picture.setTypeFace(typeface);
		picture.setTextColor(textColor);

		this.picture = picture;
	}

	public void setImagePicture(byte[] mem) {
		if (picture != null)
			picture.recycle();

		picture = new ImagePicture(mem);
	}

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
	void registerPicture(PictureContainer container) {
		super.registerPicture(container);
		container.add(picture);
	}

	@Override
	void writeConfigure(KeyWriter writer) {
		super.writeConfigure(writer);
		writePicture(writer, picture);
	}
}
