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
 * 绝对布局的layout<br>
 * <br>
 * 每个子sview有单独的绘制的坐标,子sview之间没有任何关联.<br>
 */
public class SlptAbsoluteLayout extends SlptLayout {
	/**
	 * 子sview的坐标起始点在sview水平方向的位置<br>
	 * <br>
	 * SlptViewComponent.POSITION_LEFT: 左边<br>
	 * SlptViewComponent.POSITION_RIGHT：右边<br>
	 * SlptViewComponent.POSITION_CENTER：水平居中<br>
	 */
	public byte positionOfStartPointX = POSITION_LEFT;

	/**
	 * 子sview的坐标起始点在sview垂直方向的位置<br>
	 * <br>
	 * SlptViewComponent.POSITION_TOP: 顶端<br>
	 * SlptViewComponent.POSITION_BOTTOM：底端<br>
	 * SlptViewComponent.POSITION_CENTER：垂直居中<br>
	 */
	public byte positionOfStartPointY = POSITION_TOP;

	@Override
	protected short initType() {
		return SVIEW_ABSOLUTE_LAYOUT;
	}

	@Override
	public void writeConfigure(KeyWriter writer) {
		super.writeConfigure(writer);
		writer.writeByte(positionOfStartPointX);
		writer.writeByte(positionOfStartPointY);
	}
}
