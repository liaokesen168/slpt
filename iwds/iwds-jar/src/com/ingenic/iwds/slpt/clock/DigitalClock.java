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
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.Log;

import com.ingenic.iwds.slpt.SlptClock;
import com.ingenic.iwds.slpt.view.core.Picture.ImagePicture;
import com.ingenic.iwds.slpt.view.core.SlptLinearLayout;
import com.ingenic.iwds.slpt.view.core.SlptPictureView;
import com.ingenic.iwds.slpt.view.core.SlptViewComponent.Padding;
import com.ingenic.iwds.slpt.view.digital.SlptDayHView;
import com.ingenic.iwds.slpt.view.digital.SlptDayLView;
import com.ingenic.iwds.slpt.view.digital.SlptHourHView;
import com.ingenic.iwds.slpt.view.digital.SlptHourLView;
import com.ingenic.iwds.slpt.view.digital.SlptMinuteHView;
import com.ingenic.iwds.slpt.view.digital.SlptMinuteLView;
import com.ingenic.iwds.slpt.view.digital.SlptMonthHView;
import com.ingenic.iwds.slpt.view.digital.SlptMonthLView;
import com.ingenic.iwds.slpt.view.digital.SlptSecondHView;
import com.ingenic.iwds.slpt.view.digital.SlptSecondLView;
import com.ingenic.iwds.slpt.view.digital.SlptWeekView;
import com.ingenic.iwds.slpt.view.utils.SimpleFile;

/**
 * 内置的数字表盘， 表盘的源码在SlptTestDemo中可以查看
 */
public class DigitalClock extends SlptClock {

	public SlptLinearLayout linearLayout = new SlptLinearLayout();

	public SlptLinearLayout dateLinearLayout = new SlptLinearLayout();
	public SlptMonthHView monthHView = new SlptMonthHView();
	public SlptMonthLView monthLView = new SlptMonthLView();
	public SlptPictureView dateSepView = new SlptPictureView();
	public SlptDayHView dayHView = new SlptDayHView();
	public SlptDayLView dayLView = new SlptDayLView();
	public SlptWeekView weekView = new SlptWeekView();

	public SlptLinearLayout timeLinearLayout = new SlptLinearLayout();
	public SlptHourHView hourHView = new SlptHourHView();
	public SlptHourLView hourLView = new SlptHourLView();
	public SlptPictureView timeSepView = new SlptPictureView();
	public SlptMinuteHView minuteHView = new SlptMinuteHView();
	public SlptMinuteLView minuteLView = new SlptMinuteLView();
	public SlptSecondHView secondHView = new SlptSecondHView();
	public SlptSecondLView secondLView = new SlptSecondLView();

	private float dateTextSize = 28;
	private int dateTextColor = 0xffffffff;
	private Typeface dateTypeface = Typeface.DEFAULT;

	private float weekTextSize = 380;
	private int weekTextColor = 0xffffffff;
	private Typeface weekTypeface = Typeface.DEFAULT;

	private float timeTextSize = 80;
	private int timeTextColor = 0xffffffff;
	private Typeface timeTypeface = Typeface.DEFAULT;

	private float secondTextSize = 30;
	private int secondTextColor = 0xffffffff;
	private Typeface secondTypeface = Typeface.DEFAULT;

	private Padding dateLayoutPadding = new Padding();
	private Padding timeLayoutPadding = new Padding();
	private Padding weekViewPadding = new Padding();

	private String[] digitalNums = { "0", "1", "2", "3", "4", "5", "6", "7",
			"8", "9" };
	private String[] weekNums = { "Sun", "Mon", "Tues", "Wed", "Thur", "Fri",
			"Sat" };

	public DigitalClock() {
		initLayout();
		initDefaultSettings();
	}

	public DigitalClock(Context context) {
		AssetManager assetManager = context.getAssets();
		
		// // example code for set typeface
		// // you must have the resource file in your assert dir
		// Typeface typeface = Typeface.createFromAsset(assetManager,
		// "time.ttf");
		// dateTypeface = timeTypeface = weekTypeface = secondTypeface =
		// typeface;

		// // example code for set background picture
		// // you must have the resource file in your assert dir
		// byte[] background_buf = null;
		// background_buf = SimpleFile.readFileFromAssets(context,
		// 		"gray.png");
		// dateLinearLayout.background.picture = new ImagePicture(background_buf);

		initLayout();
		initDefaultSettings();
	}

	private void initDefaultSettings() {
		// 设置垂直排列，背景色为黑色
		linearLayout.background.color = 0xff000000;
		linearLayout.orientation = SlptLinearLayout.VERTICAL;

		// 日期的在rootview中的偏移
		dateLayoutPadding.left = 60;
		dateLayoutPadding.top = 60;
		dateLinearLayout.setPadding(dateLayoutPadding);

		// 便捷的方式设置所有日期View的StringPictureArray
		dateLinearLayout.setStringPictureArrayForAll(digitalNums);
		dateLinearLayout.setTextAttrForAll(dateTextSize, dateTextColor,
				dateTypeface);

		dateSepView.setStringPicture('-');

		weekViewPadding.left = 15;
		weekView.setPadding(weekViewPadding);
		weekView.setStringPictureArray(weekNums);
		weekView.setTextAttr(weekTextSize, weekTextColor, weekTypeface);

		// 时间的在rootview中的偏移
		timeLayoutPadding.left = 55;
		timeLayoutPadding.top = 15;
		timeLinearLayout.setPadding(timeLayoutPadding);

		// 便捷的方式设置所有时间View的StringPictureArray
		timeLinearLayout.setStringPictureArrayForAll(digitalNums);
		timeLinearLayout.setTextAttrForAll(100, timeTextColor, timeTypeface);

		timeSepView.setStringPicture(':');

		secondHView
				.setTextAttr(secondTextSize, secondTextColor, secondTypeface);
		secondLView
				.setTextAttr(secondTextSize, secondTextColor, secondTypeface);

		// 结束，坐等writeToSlpt 和 SlptClock.enableSlpt()
	}

	private void initLayout() {
		// 设置rootview
		setRootView(linearLayout);

		// 将日期和时间布局添加到rootview
		linearLayout.add(dateLinearLayout);
		linearLayout.add(timeLinearLayout);

		// 设置日期布局
		dateLinearLayout.add(monthHView);
		dateLinearLayout.add(monthLView);
		dateLinearLayout.add(dateSepView);
		dateLinearLayout.add(dayHView);
		dateLinearLayout.add(dayLView);
		dateLinearLayout.add(weekView);

		// 设置时间布局
		timeLinearLayout.add(hourHView);
		timeLinearLayout.add(hourLView);
		timeLinearLayout.add(timeSepView);
		timeLinearLayout.add(minuteHView);
		timeLinearLayout.add(minuteLView);
		timeLinearLayout.add(secondHView);
		timeLinearLayout.add(secondLView);
	}

}
