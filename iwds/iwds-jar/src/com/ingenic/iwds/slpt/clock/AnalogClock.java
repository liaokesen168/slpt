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

package com.ingenic.iwds.slpt.clock;

import android.content.Context;

import com.ingenic.iwds.slpt.SlptClock;
import com.ingenic.iwds.slpt.view.analog.SlptAnalogHourWithMinuteView;
import com.ingenic.iwds.slpt.view.analog.SlptAnalogMinuteView;
import com.ingenic.iwds.slpt.view.analog.SlptAnalogSecondView;
import com.ingenic.iwds.slpt.view.core.SlptFrameLayout;
import com.ingenic.iwds.slpt.view.utils.SimpleFile;

/**
 * 内置的模拟表盘，黑色背景，时分秒针， 表盘的源码在SlptTestDemo中可以查看
 */
public class AnalogClock extends SlptClock {
	public SlptFrameLayout frameLayout = new SlptFrameLayout();
	public SlptAnalogHourWithMinuteView hourView = new SlptAnalogHourWithMinuteView();
	public SlptAnalogMinuteView minuteView = new SlptAnalogMinuteView();
	public SlptAnalogSecondView secondView = new SlptAnalogSecondView();

	private byte[] hourMem = null;
	private byte[] minuteMem = null;
	private byte[] secondMem = null;

	public AnalogClock(Context context) {
		initLayout();
		initDefaultSettings(context);
	}

	protected void initLayout() {
		// 设置rootview
		setRootView(frameLayout);

		// 添加 时， 分， 秒
		frameLayout.add(hourView);
		frameLayout.add(minuteView);
		frameLayout.add(secondView);
	}

	private void initDefaultSettings(Context context) {
		hourMem = SimpleFile.readFileFromAssets(context, "hour.png");
		minuteMem = SimpleFile.readFileFromAssets(context, "minute.png");
		secondMem = SimpleFile.readFileFromAssets(context, "second.png");

		frameLayout.background.color = 0xff000000;

		// 设置小时的图片
		hourView.setImagePicture(hourMem);
		hourView.centerHorizontal = 1;
		hourView.centerVertical = 1;

		// 设置分钟的图片
		minuteView.setImagePicture(minuteMem);
		minuteView.centerHorizontal = 1;
		minuteView.centerVertical = 1;

		// 设置秒钟的图片
		secondView.setImagePicture(secondMem);
		secondView.centerHorizontal = 1;
		secondView.centerVertical = 1;

		// 结束，坐等writeToSlpt 和 SlptClock.enableSlpt()
	}

}
