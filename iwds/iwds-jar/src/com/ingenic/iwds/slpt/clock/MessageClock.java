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

import android.graphics.Typeface;

import com.ingenic.iwds.slpt.view.core.SlptPictureView;
import com.ingenic.iwds.slpt.view.core.SlptViewComponent.Padding;

/**
 * 内置的数字表盘，并且展示如何显示消息， 表盘的源码在SlptTestDemo中可以查看
 */
public class MessageClock extends DigitalClock {
	public SlptPictureView messageView = new SlptPictureView();

	private String messageString = "this is a test message";
	private float messageTextSize = 30;
	private int messageTextColor = 0xffff0000;
	private Typeface messageTypeface = Typeface.DEFAULT;

	private Padding messagePadding = new Padding();

	public MessageClock() {
		super();
		initLayout();
		initDefaultSettings();
	}

	private void initLayout() {
		linearLayout.add(messageView);
	}

	private void initDefaultSettings() {
		messagePadding.left = 60;
		messagePadding.top = 20;
		messageView.setPadding(messagePadding);
		
		messageView.setTextAttr(messageTextSize, messageTextColor,
				messageTypeface);
		messageView.setStringPicture(messageString);
	}

}
