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

package com.ingenic.iwds.slpttestdemo.clock;

import android.graphics.Typeface;

import com.ingenic.iwds.slpt.SlptClock;
import com.ingenic.iwds.slpt.view.core.SlptFrameLayout;
import com.ingenic.iwds.slpt.view.core.SlptLayout;
import com.ingenic.iwds.slpt.view.core.SlptLinearLayout;
import com.ingenic.iwds.slpt.view.core.SlptViewComponent;
import com.ingenic.iwds.slpt.view.core.SlptViewComponent.Padding;
import com.ingenic.iwds.slpt.view.digital.SlptHourHView;
import com.ingenic.iwds.slpt.view.digital.SlptHourLView;
import com.ingenic.iwds.slpt.view.digital.SlptMinuteHView;
import com.ingenic.iwds.slpt.view.digital.SlptMinuteLView;
import com.ingenic.iwds.slpt.view.digital.SlptSecondHView;
import com.ingenic.iwds.slpt.view.digital.SlptSecondLView;

/**
 * 内置的数字表盘，风格2
 */
public class DigitalClockStyle2 extends SlptClock {

	public SlptFrameLayout frameLayout = new SlptFrameLayout();

	public SlptLinearLayout hourLayout = new SlptLinearLayout();
	public SlptHourHView hourHView = new SlptHourHView();
	public SlptHourLView hourLView = new SlptHourLView();

	public SlptLinearLayout minuteLayout = new SlptLinearLayout();
	public SlptMinuteHView minuteHView = new SlptMinuteHView();
	public SlptMinuteLView minuteLView = new SlptMinuteLView();

	public SlptLinearLayout secondLayout = new SlptLinearLayout();
	public SlptSecondHView secondHView = new SlptSecondHView();
	public SlptSecondLView secondLView = new SlptSecondLView();

	private float timeTextSize = 100;
	private int timeTextColor = 0xffffffff;
	private Typeface timeTypeface = Typeface.DEFAULT;

	private float secondTextSize = 50;
	private int secondTextColor = 0xffff0000;
	private Typeface secondTypeface = Typeface.DEFAULT;

	private Padding hourPadding = new Padding();
	private Padding minutePadding = new Padding();
	private Padding secondPadding = new Padding();

	private String[] digitalNums = { "0", "1", "2", "3", "4", "5", "6", "7",
			"8", "9" };

	public DigitalClockStyle2() {
		initLayout();  
		initDefaultSettings();
	}

	private void initDefaultSettings() {
		// 设置垂直排列，背景色为黑色
		frameLayout.background.color = 0xff000000;

		// 便捷的方式设置所有时间View的StringPictureArray
		hourPadding.left = 190;
		hourPadding.top = 50;
		hourLayout.setPadding(hourPadding);
		
		minutePadding.left = 190;
		minutePadding.top = 145;
		minuteLayout.setPadding(minutePadding);
		
		secondPadding.left = 240;
		secondPadding.top = 245;
		secondLayout.setPadding(secondPadding);
		
		hourLayout.setTextAttrForAll(timeTextSize, timeTextColor, timeTypeface);
		minuteLayout.setTextAttrForAll(timeTextSize, timeTextColor,
				timeTypeface);
		secondLayout.setTextAttrForAll(secondTextSize, secondTextColor,
				secondTypeface);

		// 结束，坐等writeToSlpt 和 SlptClock.enableSlpt()
	}

	private void initLayout() {
		// 设置rootview
		setRootView(frameLayout);

		// 将日期和时间布局添加到rootview

		frameLayout.add(hourLayout);
		frameLayout.add(minuteLayout);
		frameLayout.add(secondLayout);

		// 设置时间布局
		hourLayout.add(hourHView);
		hourLayout.add(hourLView);

		minuteLayout.add(minuteHView);
		minuteLayout.add(minuteLView);

		secondLayout.add(secondHView);
		secondLayout.add(secondLView);
	}

}
