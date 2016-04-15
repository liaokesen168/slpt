package com.example.slptclientdemo;

import android.content.Context;
import android.graphics.BitmapFactory;

//import com.igeak.watch.slpt.watchface.index001.R;
import com.ingenic.iwds.slpt.SlptClock;
import com.ingenic.iwds.slpt.view.analog.SlptAnalogHourWithMinuteView;
import com.ingenic.iwds.slpt.view.analog.SlptAnalogMinuteView;
import com.ingenic.iwds.slpt.view.analog.SlptAnalogSecondView;
import com.ingenic.iwds.slpt.view.core.Picture.ImagePicture;
import com.ingenic.iwds.slpt.view.core.SlptFrameLayout;
import com.ingenic.iwds.slpt.view.core.SlptPictureView;

public class GeakClock1 extends SlptClock {

	SlptFrameLayout frameLayout = new SlptFrameLayout();
	SlptAnalogHourWithMinuteView hourView = new SlptAnalogHourWithMinuteView();
	SlptAnalogMinuteView minuteView = new SlptAnalogMinuteView();
	SlptAnalogSecondView secondView = new SlptAnalogSecondView();
	SlptPictureView pointView = new SlptPictureView();

	byte[] hourMem = null;
	byte[] minuteMem = null;
	byte[] secondMem = null;

	
	public GeakClock1(Context context) {
		initLayout();
		initDefaultSettings(context);
	}


	protected void initLayout() {
		// 设置rootview
		setRootView(frameLayout);

		// 添加 时， 分， 秒
		frameLayout.add(hourView);
		frameLayout.add(minuteView);
		frameLayout.add(secondView);
		frameLayout.add(pointView);

		// frameLayout.add(dateLinearLayout);
	}

	public void initDefaultSettings(Context context) {
		hourMem = BitmapUtils.getbyteFromResource(context,
				R.drawable.geakclock1_hour);
		minuteMem = BitmapUtils.getbyteFromResource(context,
				R.drawable.geakclock1_minute);
		secondMem = BitmapUtils.getbyteFromResource(context,
				R.drawable.geakclock1_second);
		pointView.setImagePicture(BitmapUtils.getbyteFromResource(context,
				R.drawable.geakclock1_center_point));
		pointView.centerHorizontal = 1;
		pointView.centerVertical = 1;

		frameLayout.background.picture = new ImagePicture(
				BitmapFactory.decodeResource(context.getResources(),
						R.drawable.geakclock1_dial));

		// 设置小时的图片
		hourView.setImagePicture(hourMem);
		hourView.centerHorizontal = 1;
		hourView.centerVertical = 1;

		// 设置分钟的图片
		minuteView.setImagePicture(minuteMem);
		minuteView.centerHorizontal = 1;
		minuteView.centerVertical = 1;

		// 设置秒钟的图片
		secondView.setImagePicture(secondMem);
		secondView.centerHorizontal = 1;
		secondView.centerVertical = 1;

	}

}
