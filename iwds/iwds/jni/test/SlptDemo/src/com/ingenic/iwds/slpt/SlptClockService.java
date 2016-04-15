package com.ingenic.iwds.slpt;

import com.ingenic.iwds.slpt.view.analog.SlptAnalogMinuteView;
import com.ingenic.iwds.slpt.view.analog.SlptAnalogSecondView;
import com.ingenic.iwds.slpt.view.core.SlptFrameLayout;
import com.ingenic.iwds.slpt.view.core.SlptLinearLayout;
import com.ingenic.iwds.slpt.view.core.SlptPictureView;
import com.ingenic.iwds.slpt.view.core.SlptViewComponent;
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

import android.app.Service;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class SlptClockService extends Service {
	boolean SlptIsStarted = false;
	public final static String TAG = "SlptClockService";

	String[] digitalNums = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
	String[] weekNums = { "Sun", "Mon", "Tues", "Wed", "Thur", "Fri", "Sat" };

	SlptLinearLayout linearLayout = new SlptLinearLayout();

	SlptLinearLayout dateLinearLayout = new SlptLinearLayout();
	SlptMonthHView monthHView = new SlptMonthHView();
	SlptMonthLView monthLView = new SlptMonthLView();
	SlptPictureView dateSepView = new SlptPictureView();
	SlptDayHView dayHView = new SlptDayHView();
	SlptDayLView dayLView = new SlptDayLView();
	SlptWeekView weekView = new SlptWeekView();

	SlptLinearLayout timeLinearLayout = new SlptLinearLayout();
	SlptHourHView hourHView = new SlptHourHView();
	SlptHourLView hourLView = new SlptHourLView();
	SlptPictureView timeSepView = new SlptPictureView();
	SlptMinuteHView minuteHView = new SlptMinuteHView();
	SlptMinuteLView minuteLView = new SlptMinuteLView();
	SlptSecondHView secondHView = new SlptSecondHView();
	SlptSecondLView secondLView = new SlptSecondLView();

	SlptPictureView versionView = new SlptPictureView();

	SlptClock slptClock = new SlptClock(linearLayout);

	Typeface dateTypeface;
	Typeface weekTypeface;
	Typeface timeTypeface;

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate ---------------!");
		super.onCreate();
		initTheDigitalStyleClock();
//		initTheFirstStyleClock();
		SlptClock.enableSlpt();
		SlptIsStarted = true;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy ---------------!");
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return stub.asBinder();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand ---------------!");
		return START_STICKY;
	}

	ISlptClockService.Stub stub = new ISlptClockService.Stub() {

		@Override
		public boolean startClock() throws RemoteException {
			if (!SlptIsStarted)
				SlptClock.enableSlpt();

			SlptIsStarted = true;

			return true;
		}

		@Override
		public boolean stopClock() throws RemoteException {
			if (SlptIsStarted)
				SlptClock.disableSlpt();

			SlptIsStarted = false;

			return true;
		}

		@Override
		public boolean clockIsStart() throws RemoteException {

			return SlptIsStarted;
		}

	};

	private void initTheDigitalStyleClock() {
		linearLayout.background.color = 0xff000000;

		dateTypeface = Typeface.createFromAsset(getApplicationContext()
				.getAssets(), "clock6/time.ttf");
		if (dateTypeface == null)
			dateTypeface = Typeface.DEFAULT;

		weekTypeface = Typeface.createFromAsset(getApplicationContext()
				.getAssets(), "clock6/time_mdweek.ttf");
		if (weekTypeface == null)
			weekTypeface = Typeface.DEFAULT;

		monthHView.textSize = 28;
		monthLView.textSize = 28;
		dateSepView.textSize = 28;
		dayHView.textSize = 28;
		dayLView.textSize = 28;
		weekView.textSize = 28;

		monthHView.textColor = 0xfffffffe;
		monthLView.textColor = 0xfffffffe;
		dateSepView.textColor = 0xfffffffe;
		dayHView.textColor = 0xfffffffe;
		dayLView.textColor = 0xfffffffe;
		weekView.textColor = 0xfffffffe;

		monthHView.typeface = dateTypeface;
		monthLView.typeface = dateTypeface;
		dateSepView.typeface = dateTypeface;
		dayHView.typeface = dateTypeface;
		dayLView.typeface = dateTypeface;
		weekView.typeface = weekTypeface;

		monthHView.setStringPictureArray(digitalNums);
		monthLView.setStringPictureArray(digitalNums);
		dateSepView.setStringPicture('-');
		dayHView.setStringPictureArray(digitalNums);
		dayLView.setStringPictureArray(digitalNums);
		weekView.setStringPictureArray(weekNums);

		weekView.padding.left = 15;

		dateLinearLayout.add(monthHView);
		dateLinearLayout.add(monthLView);
		dateLinearLayout.add(dateSepView);
		dateLinearLayout.add(dayHView);
		dateLinearLayout.add(dayLView);
		dateLinearLayout.add(weekView);
		dateLinearLayout.orientation = SlptLinearLayout.HORIZONTAL;
		dateLinearLayout.padding.left = 70;
		dateLinearLayout.padding.top = 60;

		timeTypeface = Typeface.createFromAsset(getApplicationContext()
				.getAssets(), "clock6/time.ttf");
		if (timeTypeface == null)
			timeTypeface = Typeface.DEFAULT;

		hourHView.textColor = 0xffffffff;
		hourLView.textColor = 0xffffffff;
		timeSepView.textColor = 0xffffffff;
		minuteHView.textColor = 0xffffffff;
		minuteLView.textColor = 0xffffffff;
		secondHView.textColor = 0xffffffff;
		secondLView.textColor = 0xffffffff;

		hourHView.textSize = 80;
		hourLView.textSize = 80;
		timeSepView.textSize = 80;
		minuteHView.textSize = 80;
		minuteLView.textSize = 80;
		secondHView.textSize = 30;
		secondLView.textSize = 30;

		hourHView.typeface = timeTypeface;
		hourLView.typeface = timeTypeface;
		timeSepView.typeface = timeTypeface;
		minuteHView.typeface = timeTypeface;
		minuteLView.typeface = timeTypeface;
		secondHView.typeface = timeTypeface;
		secondLView.typeface = timeTypeface;

		hourHView.setStringPictureArray(digitalNums);
		hourLView.setStringPictureArray(digitalNums);
		timeSepView.setStringPicture(":");
		minuteHView.setStringPictureArray(digitalNums);
		minuteLView.setStringPictureArray(digitalNums);
		secondHView.setStringPictureArray(digitalNums);
		secondLView.setStringPictureArray(digitalNums);

		timeLinearLayout.add(hourHView);
		timeLinearLayout.add(hourLView);
		timeLinearLayout.add(timeSepView);
		timeLinearLayout.add(minuteHView);
		timeLinearLayout.add(minuteLView);
		timeLinearLayout.add(secondHView);
		timeLinearLayout.add(secondLView);
		timeLinearLayout.orientation = SlptLinearLayout.HORIZONTAL;

		timeLinearLayout.padding.left = 60;
		timeLinearLayout.padding.top = 10;

		versionView.textColor = 0xff802323;
		versionView.textSize = 24;
		versionView.typeface = Typeface.DEFAULT;
		versionView.setStringPicture("IWOP SLPT Demo v0.1");
		versionView.padding.left = 60;
		versionView.padding.top = 60;

		linearLayout.add(dateLinearLayout);
		linearLayout.add(timeLinearLayout);
		linearLayout.add(versionView);
		linearLayout.orientation = SlptLinearLayout.VERTICAL;

		slptClock.writeToSlpt();

	}

	private void initTheFirstStyleClock() {
		SlptLinearLayout linearLayout = new SlptLinearLayout();
		slptClock = new SlptClock(linearLayout);

		SlptHourHView hourHView = new SlptHourHView();
		SlptHourLView hourLView = new SlptHourLView();
		SlptPictureView timeSparatorView = new SlptPictureView();
		SlptMinuteHView minuteHView = new SlptMinuteHView();
		SlptMinuteLView minuteLView = new SlptMinuteLView();
		SlptSecondHView secondHView = new SlptSecondHView();
		SlptSecondLView secondLView = new SlptSecondLView();
		SlptLinearLayout digitalTimeLayout = new SlptLinearLayout();

		SlptPictureView msg0View = new SlptPictureView();
		SlptPictureView msg1View = new SlptPictureView();

		hourHView.textSize = 40;
		hourLView.textSize = 40;
		timeSparatorView.textSize = 40;
		minuteHView.textSize = 40;
		minuteLView.textSize = 40;
		secondHView.textSize = 50;
		secondLView.textSize = 50;

		hourHView.setStringPictureArray(digitalNums);
		hourLView.setStringPictureArray(digitalNums);
		timeSparatorView.setStringPicture(':');
		minuteHView.setStringPictureArray(digitalNums);
		minuteLView.setStringPictureArray(digitalNums);
		secondHView.setStringPictureArray(digitalNums);
		secondLView.setStringPictureArray(digitalNums);

		digitalTimeLayout.orientation = SlptLinearLayout.HORIZONTAL;
		digitalTimeLayout.add(hourHView);
		digitalTimeLayout.add(hourLView);
		digitalTimeLayout.add(timeSparatorView);
		digitalTimeLayout.add(minuteHView);
		digitalTimeLayout.add(minuteLView);
		digitalTimeLayout.add(secondHView);
		digitalTimeLayout.add(secondLView);
		digitalTimeLayout.background.color = 0xffff00ff;

		msg0View.textSize = 20;
		msg1View.textSize = 20;
		msg0View.setStringPicture("--------+++---------------");
		msg1View.setStringPicture("--------+++---------------");
		msg0View.alignParentX = SlptViewComponent.ALIGN_CENTER;
		msg1View.alignParentX = SlptViewComponent.ALIGN_CENTER;

		SlptFrameLayout frameLayout = new SlptFrameLayout();
		SlptAnalogSecondView secondView = new SlptAnalogSecondView();
		SlptAnalogMinuteView minuteView = new SlptAnalogMinuteView();
		secondView.setImagePicture("/system/clock/widget_clock_second_01.png");
		minuteView.setImagePicture("/system/clock/widget_clock_hour_01.png");
		frameLayout.alignX = frameLayout.ALIGN_CENTER;
		frameLayout.alignY = frameLayout.ALIGN_CENTER;
		frameLayout.descHeight = frameLayout.RECT_SPECIFY;
		frameLayout.descWidth = frameLayout.RECT_SPECIFY;
		frameLayout.rect.height = 160;
		frameLayout.rect.width = 160;
		frameLayout.background.color = 0xff00ff00;
		frameLayout.add(secondView);
//		frameLayout.add(minuteView);

		linearLayout.orientation = SlptLinearLayout.VERTICAL;
		// linearLayout.add(msg0View);
		linearLayout.add(digitalTimeLayout);
		linearLayout.alignX = linearLayout.ALIGN_CENTER;
		linearLayout.alignY = linearLayout.ALIGN_CENTER;
		// linearLayout.add(msg1View);
		linearLayout.add(frameLayout);
		linearLayout.background.color = 0xff000000;

		slptClock.writeToSlpt();

	}

}
