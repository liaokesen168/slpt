package com.example.clientdemo;

import com.ingenic.iwds.slpt.SlptClock;
import com.ingenic.iwds.slpt.clock.AnalogClock;
import com.ingenic.iwds.slpt.clock.MessageClock;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class ClientDemoActivity extends ActionBarActivity {
	private static final String TAG = "clientDemo";
	SlptClock slptClock; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_client_demo);
		
		//slptClock = new MessageClock();
		//slptClock = new AnalogClock(getApplicationContext());
		slptClock = new GeakClock1(getApplicationContext());
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.client_demo, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected void onDestroy() {
		super.onDestroy();
		SlptClock.unbindService(this);
	};
}
