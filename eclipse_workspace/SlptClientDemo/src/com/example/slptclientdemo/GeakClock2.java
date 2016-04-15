package com.example.slptclientdemo;

import android.content.Context;

import com.ingenic.iwds.slpt.SlptClock;

import com.ingenic.iwds.slpt.view.analog.SlptAnalogHourWithMinuteView;
import com.ingenic.iwds.slpt.view.analog.SlptAnalogMinuteView;
import com.ingenic.iwds.slpt.view.analog.SlptAnalogSecondView;
import com.ingenic.iwds.slpt.view.core.SlptFrameLayout;
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
import com.ingenic.iwds.slpt.view.digital.SlptWeekView;

public class GeakClock2 extends SlptClock {

	public SlptFrameLayout frameBaseLayout = new SlptFrameLayout();

	private SlptFrameLayout frameAnalogLayout = new SlptFrameLayout();
	private SlptAnalogSecondView secondView = new SlptAnalogSecondView();

	public SlptLinearLayout timeLinearLayout = new SlptLinearLayout();
	public SlptHourHView hourHView = new SlptHourHView();
	public SlptHourLView hourLView = new SlptHourLView();
	public SlptMinuteHView minuteHView = new SlptMinuteHView();
	public SlptMinuteLView minuteLView = new SlptMinuteLView();

	public SlptLinearLayout dateLinearLayout = new SlptLinearLayout();
	public SlptMonthHView monthHView = new SlptMonthHView();
	public SlptMonthLView monthLView = new SlptMonthLView();
	public SlptPictureView dateSepView = new SlptPictureView();
	public SlptDayHView dayHView = new SlptDayHView();
	public SlptDayLView dayLView = new SlptDayLView();

	public SlptLinearLayout weekLinearLayout = new SlptLinearLayout();
	public SlptWeekView weekView = new SlptWeekView();

	private int[] hourNums = { R.drawable.geakclock2_num_hour_0,
			R.drawable.geakclock2_num_hour_1, R.drawable.geakclock2_num_hour_2,
			R.drawable.geakclock2_num_hour_3, R.drawable.geakclock2_num_hour_4,
			R.drawable.geakclock2_num_hour_5, R.drawable.geakclock2_num_hour_6,
			R.drawable.geakclock2_num_hour_7, R.drawable.geakclock2_num_hour_8,
			R.drawable.geakclock2_num_hour_9 };
	private int[] minuteNums = { R.drawable.geakclock2_num_min_0,
			R.drawable.geakclock2_num_min_1, R.drawable.geakclock2_num_min_2,
			R.drawable.geakclock2_num_min_3, R.drawable.geakclock2_num_min_4,
			R.drawable.geakclock2_num_min_5, R.drawable.geakclock2_num_min_6,
			R.drawable.geakclock2_num_min_7, R.drawable.geakclock2_num_min_8,
			R.drawable.geakclock2_num_min_9 };
	private int[] monthPics = { R.drawable.geakclock2_month_1,
			R.drawable.geakclock2_month_2, R.drawable.geakclock2_month_3,
			R.drawable.geakclock2_month_4, R.drawable.geakclock2_month_5,
			R.drawable.geakclock2_month_6, R.drawable.geakclock2_month_7,
			R.drawable.geakclock2_month_8, R.drawable.geakclock2_month_9,
			R.drawable.geakclock2_month_10, R.drawable.geakclock2_month_11,
			R.drawable.geakclock2_month_12 };
	private int[] days = { R.drawable.geakclock2_num_date_0,
			R.drawable.geakclock2_num_date_1, R.drawable.geakclock2_num_date_2,
			R.drawable.geakclock2_num_date_3, R.drawable.geakclock2_num_date_4,
			R.drawable.geakclock2_num_date_5, R.drawable.geakclock2_num_date_6,
			R.drawable.geakclock2_num_date_7, R.drawable.geakclock2_num_date_8,
			R.drawable.geakclock2_num_date_9 };
	private int[] weeksPics = { R.drawable.geakclock2_day_7,
			R.drawable.geakclock2_day_1, R.drawable.geakclock2_day_2,
			R.drawable.geakclock2_day_3, R.drawable.geakclock2_day_4,
			R.drawable.geakclock2_day_5, R.drawable.geakclock2_day_6

	};

	private byte[] secondMem = null;

	public GeakClock2(Context context) {
		initLayout();
		initDefaultSettings(context);
	}

	private void initDefaultSettings(Context context) {
		Padding timePadding = new Padding();
		timePadding.top = 111;
		timeLinearLayout.setPadding(timePadding);

		timeLinearLayout.centerHorizontal = 1;
		timeLinearLayout.orientation = SlptLinearLayout.HORIZONTAL;

		frameAnalogLayout.background.color = 0xfffffffe;
		secondMem = BitmapUtils.getbyteFromResource(context,
				R.drawable.geakclock2_second);
		// 设置秒钟的图片
		secondView.setImagePicture(secondMem);
		secondView.centerHorizontal = 1;
		secondView.centerVertical = 1;

		Padding datePadding = new Padding();
		datePadding.left = 147;
		datePadding.top = 238;
		dateLinearLayout.orientation = SlptLinearLayout.HORIZONTAL;
		dateLinearLayout.setPadding(datePadding);

		// 便捷的方式设置所有日期View的StringPictureArray
		for (int i = 0; i < days.length; i++) {
			monthHView.setImagePicture(i,
					BitmapUtils.getbyteFromResource(context, days[i]));
			monthLView.setImagePicture(i,
					BitmapUtils.getbyteFromResource(context, days[i]));
		}
		Padding dayPadding = new Padding();
		dayPadding.left = 7;
		dayHView.setPadding(dayPadding);
		for (int i = 0; i < days.length; i++) {
			dayHView.setImagePicture(i,
					BitmapUtils.getbyteFromResource(context, days[i]));
			dayLView.setImagePicture(i,
					BitmapUtils.getbyteFromResource(context, days[i]));
		}
		dateSepView.setStringPicture("  ");
		dateSepView.centerVertical = 1;

		Padding weekViewPadding = new Padding();
		weekViewPadding.top = 262;
		weekViewPadding.left = 133;
		weekView.setPadding(weekViewPadding);
		for (int i = 0; i < weeksPics.length; i++) {
			weekView.setImagePicture(i,
					BitmapUtils.getbyteFromResource(context, weeksPics[i]));
		}
		weekView.centerHorizontal = 1;

		// 时间的在rootview中的偏移
		timeLinearLayout.centerHorizontal = 1;

		// 便捷的方式设置所有时间View的StringPictureArray
		for (int i = 0; i < hourNums.length; i++) {
			hourHView.setImagePicture(i,
					BitmapUtils.getbyteFromResource(context, hourNums[i]));
			hourLView.setImagePicture(i,
					BitmapUtils.getbyteFromResource(context, hourNums[i]));
		}
		for (int i = 0; i < minuteNums.length; i++) {
			minuteHView.setImagePicture(i,
					BitmapUtils.getbyteFromResource(context, minuteNums[i]));
			minuteLView.setImagePicture(i,
					BitmapUtils.getbyteFromResource(context, minuteNums[i]));
		}
		minuteHView.centerVertical = 1;
		minuteLView.centerVertical = 1;
		Padding minutePadding = new Padding();
		minutePadding.left = 14;
		minuteHView.setPadding(minutePadding);
	}

	private void initLayout() {
		// 设置rootview
		setRootView(frameBaseLayout);
		// frameBaseLayout.background.color = 0xff00ff00;
		frameBaseLayout.add(frameAnalogLayout);
		frameBaseLayout.add(timeLinearLayout);
		frameBaseLayout.add(dateLinearLayout);
		frameBaseLayout.add(weekLinearLayout);
		frameAnalogLayout.add(secondView);

		// 将日期和时间布局添加到rootview
		// linearLayout.add(timeLinearLayout);
		// linearLayout.add(dateLinearLayout);

		// 设置时间布局
		timeLinearLayout.add(hourHView);
		timeLinearLayout.add(hourLView);
		timeLinearLayout.add(minuteHView);
		timeLinearLayout.add(minuteLView);

		// 设置日期布局
		dateLinearLayout.add(monthHView);
		dateLinearLayout.add(monthLView);
		dateLinearLayout.add(dateSepView);
		dateLinearLayout.add(dayHView);
		dateLinearLayout.add(dayLView);
		// linearLayout.add(weekView);

		weekLinearLayout.add(weekView);
	}

}
