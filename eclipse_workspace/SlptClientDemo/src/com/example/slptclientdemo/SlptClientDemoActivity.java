package com.example.slptclientdemo;

import com.ingenic.iwds.slpt.SlptClock;
import com.ingenic.iwds.slpt.clock.AnalogClock;
import com.ingenic.iwds.slpt.clock.DigitalClock;
import com.ingenic.iwds.slpt.clock.MessageClock;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;

public class SlptClientDemoActivity extends ActionBarActivity {
	private static final String TAG = "SlptClientDemo";
	SlptClock slptClock; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_slpt_client_demo);
		
		//slptClock = new AnalogClock(getApplicationContext());
		slptClock = new DigitalClock();
		//slptClock = new GeakClock2(getApplicationContext());
		SlptClock.bindService(this, callback);
		
	}

	SlptClock.Callback callback = new SlptClock.Callback() {
		
		@Override
		public void onServiceDisconnected() {
			Log.d(TAG, "ServiceDisconnected callback function!");
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