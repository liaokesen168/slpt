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

import java.util.ArrayList;

import com.ingenic.iwds.slpt.GroupData;
import com.ingenic.iwds.slpt.ISlptClockService;
import com.ingenic.iwds.slpt.PictureData;
import com.ingenic.iwds.slpt.SlptClock;
import com.ingenic.iwds.slpt.clock.AnalogClock;
import com.ingenic.iwds.slpt.clock.AnalogClock320Height;
import com.ingenic.iwds.slpt.clock.DigitalClock;
import com.ingenic.iwds.slpt.clock.DigitalClockStyle2;
import com.ingenic.iwds.slpt.clock.LowBatteryClock;
import com.ingenic.iwds.slpt.clock.MessageClock;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

public class SlptClockService extends Service {
	boolean SlptIsStarted = false;
	final static String TAG = "SlptClockService";
	final static int DEFALT_SLPT_BRIGHTNESS = 60;
	SlptClock slptClock = new SlptClock();
	SlptClock slptClockGetParcel = new SlptClock();
	SlptClock slptClockArray[] = new SlptClock[7];
	int groupNum = -1;
	int num = 0;
	int lock_num = 0;

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate ---------------!");
		super.onCreate();
		slptClockArray[0] = new AnalogClock(getApplicationContext());
		slptClockArray[1] = new AnalogClock320Height(getApplicationContext());
		slptClockArray[2] = new DigitalClock(getApplicationContext());
		slptClockArray[3] = new MessageClock();
		slptClockArray[4] = new DigitalClockStyle2();
		slptClockArray[5] = new DigitalClock();
		slptClockArray[6] = new DigitalClock(getApplicationContext());
		slptClock = slptClockArray[3];
		slptClockGetParcel.m_uuid = null;
		slptClock.nativeInitSlpt();
		slptClock.writeToSlpt();

		SlptClock.setBrightnessOfSlpt(DEFALT_SLPT_BRIGHTNESS);
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

		@Override
		public boolean sendViewArr(byte[] arr) throws RemoteException {
			
			slptClockGetParcel.sviewArr = arr;
			return true;
		}


		@Override
		public boolean sendSlptStart(String uuid) throws RemoteException {
			synchronized (slptClockGetParcel) {
				if (slptClockGetParcel.m_uuid != null) {

						lock_num++;
						slptClockGetParcel.notify();
						try {
							slptClockGetParcel.wait();
							lock_num--;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
				}
				if (lock_num != 0)
					return false;
								
				slptClockGetParcel.m_uuid = uuid;
	
				return true;
			}

		}

		@Override
		public boolean sendSlptEnd(String uuid) throws RemoteException {
			
			synchronized (slptClockGetParcel) {
				if (slptClockGetParcel.m_uuid.compareTo(uuid) != 0)
					Log.e(TAG, "sendSlptEnd:NOW UUID IS NOT SAME AS THE CURRENT UUID!");

				for (int i = 0; i < slptClockGetParcel.groupList.size(); i++)
					slptClockGetParcel.groupList.get(i).pictureList.clear();

				slptClockGetParcel.groupList.clear();
				slptClockGetParcel.sviewArr = null;
				slptClockGetParcel.m_uuid = null;
				groupNum = -1;

				slptClockGetParcel.notify();
			}

			return true;
		}

		@Override
		public boolean sendGroupData(SlptClock arg0) throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean sendRleArray(int[] arg0) throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}



	};

}
