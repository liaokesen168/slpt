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

/**
 * 帧布局的layout<br>
 * <br>
 * 子sview以各种对齐方式被绘制在layout中<br>
 * 子sview之间没有任何关联<br>
 */
public class SlptFrameLayout extends SlptLayout {
	@Override
	protected short initType() {
		return SVIEW_FRAME_LAYOUT;
	}
}
