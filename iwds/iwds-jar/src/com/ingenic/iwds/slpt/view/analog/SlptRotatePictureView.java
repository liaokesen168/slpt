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

package com.ingenic.iwds.slpt.view.analog;

import com.ingenic.iwds.slpt.view.core.SlptPictureView;

/**
 * 以模拟旋转的风格显示一张图片
 */
public class SlptRotatePictureView extends SlptPictureView {
	@Override
	protected short initType() {
		return SVIEW_ROTATE_PIC;
	}
}
