package com.example.slptservicedemo;

import com.ingenic.iwds.slpt.SlptClock;
import com.ingenic.iwds.slpt.clock.AnalogClock;
import com.ingenic.iwds.slpt.clock.DigitalClock;
import com.ingenic.iwds.slpt.clock.MessageClock;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class SlptServiceDemoActivity extends ActionBarActivity {
	public final static String TAG = "SlptServiceDemo";
	SlptClock slptClock;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_slpt_service_demo);
		
		//slptClock = new MessageClock();
		slptClock = new AnalogClock(getApplicationContext());
		SlptClock.bindService(this, callback);
	}
	
	SlptClock.Callback callback = new SlptClock.Callback() {
		
		@Override
		public void onServiceDisconnected() {
		}
		
		@Override
		public void onServiceConnected() {
			slptClock.sendToService();
		}
	};
	
	protected void onDestroy() {
		super.onDestroy();
		SlptClock.unbindService(this);
	};
}
