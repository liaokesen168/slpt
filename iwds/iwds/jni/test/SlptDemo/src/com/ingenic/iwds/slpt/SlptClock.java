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

package com.ingenic.iwds.slpt;

import android.util.Log;

import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.slpt.view.core.Picture.PictureContainer;
import com.ingenic.iwds.slpt.view.core.SlptLayout;
import com.ingenic.iwds.slpt.view.utils.KeyWriter;

public class SlptClock {
	private static final String TAG = "SlptClock";
	private static boolean nativeIsInitialized;

	final SlptLayout rootView;

	SlptClock(SlptLayout rootView) {
		IwdsAssert.dieIf(TAG, rootView == null, "rootView can not be null!");
		this.rootView = rootView;
	}

	public SlptLayout getRootView() {
		return rootView;
	}

	private static native int initSlpt();

	public static native int enableSlpt();

	public static native int disableSlpt();
	
	private static native void requestSlptDisplayPause();

	private static native void requestSlptDisplayResume();

	private static native void initSview(long writerJniPrivate);

	static {
		try {
			System.loadLibrary("slpt-linux");
			nativeIsInitialized = initSlpt() == 0;
			nativeIsInitialized = true;
			Log.d(TAG, "loadLibrary Successed!");
		} catch (Exception e) {
			nativeIsInitialized = false;
			Log.d(TAG, "loadLibrary Exception " + e.getMessage());
		}
	}

	public boolean writeToSlpt() {
		if (!nativeIsInitialized)
			return false;

		KeyWriter writer = new KeyWriter();
		PictureContainer container = new PictureContainer();
		rootView.registerPicture(container);

		requestSlptDisplayPause();

		PictureContainer.writeToSlpt(container);
		rootView.writeConfigure(writer);
		initSview(writer.getJniPrivate());

		requestSlptDisplayResume();

		writer.recycle();
		container = null;

		return true;
	}
}
