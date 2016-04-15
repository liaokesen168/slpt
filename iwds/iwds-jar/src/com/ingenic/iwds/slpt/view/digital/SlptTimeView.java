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

package com.ingenic.iwds.slpt.view.digital;

import com.ingenic.iwds.slpt.view.core.SlptNumView;

/**
 * 以数字的风格显示不同种类的时间
 */
public class SlptTimeView extends SlptNumView {
	public static final String[] digital_nums = { "0", "1", "2", "3", "4", "5",
			"6", "7", "8", "9" };
	public static final String[] week_nums = { "Sun", "Mon", "Tues", "Wed",
			"Thur", "Fri", "Sat" };

	private void setDefaultString() {
		if (type != SVIEW_WEEK)
			setStringPictureArray(digital_nums);
		else
			setStringPictureArray(week_nums);
	}

	public SlptTimeView() {
		setDefaultString();
	}

	@Override
	protected short initType() {
		return SVIEW_TIME_NUM;
	}

	@Override
	protected int initCapacity() {
		return 10;
	}
}
