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

import com.ingenic.iwds.slpt.view.utils.KeyWriter;


/**
 * 线性布局的layout<br>
 * <br>
 * 子sview按照先后顺序在水平/垂直方向上被绘制<br>
 */
public class SlptLinearLayout extends SlptLayout {
	/**
	 * 子sview的排列排列方式<br>
	 * <br>
	 * SlptViewComponent.HORIZONTA：水平排列
	 * SlptViewComponent.HORIZONTA：垂直排列
	 */
	public byte orientation = HORIZONTAL;

	@Override
	protected short initType() {
		return SVIEW_LINEAR_LAYOUT;
	}

	@Override
	public void writeConfigure(KeyWriter writer) {
		super.writeConfigure(writer);
		writer.writeByte(orientation);
	}
}
