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

package com.ingenic.iwds.slpttestdemo;

import com.ingenic.iwds.slpt.ISlptClockService;
import com.ingenic.iwds.slpt.SlptClock;
import com.ingenic.iwds.slpt.clock.AnalogClock;
import com.ingenic.iwds.slpt.clock.AnalogClock320Height;
import com.ingenic.iwds.slpt.clock.DigitalClock;
import com.ingenic.iwds.slpt.clock.DigitalClockStyle2;
import com.ingenic.iwds.slpt.clock.MessageClock;
import com.ingenic.iwds.slpttestdemo.R;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
//import android.support.v4.widget.SearchViewCompatIcs.MySearchView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SlptTestDemoActivity extends Activity {
	public final static String TAG = "SlptViewActivity";
	String ACTION = "com.ingenic.iwds.slpttestdemo.SlptClockService";
	Intent intent;
	ISlptClockService clockService;
	Button slptCtrlButton;
	Button switchButton;
	boolean slptEanbleState = false;
	int x = 1;
	int y = 0;
	SlptClock slptClock = null;
	SlptClock slptClockArray[] = new SlptClock[7];
	byte[] arr = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_slpt_test_demo);
		
		slptClockArray[0] = new AnalogClock(getApplicationContext());
		slptClockArray[1] = new AnalogClock320Height(getApplicationContext());
		slptClockArray[2] = new DigitalClock(getApplicationContext());
		slptClockArray[3] = new MessageClock();
		slptClockArray[4] = new DigitalClockStyle2();
		slptClockArray[5] = new DigitalClock();
		slptClockArray[6] = new DigitalClock(getApplicationContext());
		
		slptClock = new AnalogClock(getApplicationContext()); 
				
		slptCtrlButton = (Button) findViewById(R.id.button_slpt_ctrl);
		slptCtrlButton.setOnClickListener(slptCtrlButtonListener);
		
		switchButton = (Button) findViewById(R.id.button_slpt_switch);
		switchButton.setOnClickListener(slptSwitchButtonListener);
		
		intent = new Intent(ACTION);

		startService(intent);
		bindService(intent, connnection, Service.BIND_AUTO_CREATE);

		setCurrentSlptState();
	}

	protected void onDestroy() {
		super.onDestroy();
		unbindService(connnection);
	}

	OnClickListener slptCtrlButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			setToNextSlptState();
			setCurrentSlptState();
		}
	};
	
	OnClickListener slptSwitchButtonListener = new OnClickListener() {
		public void onClick(View v) {						
			if (clockService == null)
				return;
		}
	};

	void setToNextSlptState() {
		boolean isStarted;

		if (clockService == null)
			return;

		try {
			isStarted = clockService.clockIsStart();
		} catch (RemoteException e) {
			isStarted = false;
		}

		try {
			if (isStarted)
				clockService.stopClock();
			else
				clockService.startClock();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	void setCurrentSlptState() {
		String msg;

		if (clockService == null)
			msg = "SLPT 未连接";
		else {
			try {
				msg = clockService.clockIsStart() ? "SLPT 已启动" : "SLPT 未启动";
			} catch (RemoteException e) {
				msg = "SLPT 服务连接异常";
			}
		}

		slptCtrlButton.setText(msg);
	}

	ServiceConnection connnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "onServiceDisconnected ---------------!");
			clockService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "onServiceConnected ---------------!");
			clockService = ISlptClockService.Stub.asInterface(service);
			setCurrentSlptState();
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.slpt_test_demo, menu);
		return true;
	}


}
