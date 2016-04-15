package com.ingenic.iwds.slpt;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SlptDemoActivity extends Activity {
	public final static String TAG = "SlptViewActivity";
	String ACTION = "com.ingenic.iwds.slpt.SlptClockService";
	Intent intent;
	ISlptClockService clockService;
	Button slptCtrlButton;
	boolean slptEanbleState = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_slpt_demo);

		slptCtrlButton = (Button) findViewById(R.id.button_slpt_ctrl);
		slptCtrlButton.setOnClickListener(slptCtrlButtonListener);

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
		getMenuInflater().inflate(R.menu.slpt_demo, menu);
		return true;
	}

}
