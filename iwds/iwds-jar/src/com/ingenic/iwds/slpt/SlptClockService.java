package com.ingenic.iwds.slpt;

import java.util.Arrays;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class SlptClockService extends Service {

	boolean SlptIsStarted = false;
	final static String TAG = "SlptClockService";
	final static int DEFALT_SLPT_BRIGHTNESS = 60;
	SlptClock slptClock = new SlptClock();
	SlptClock slptClockGetParcel = new SlptClock();
	int lock_num = 0;

	@Override
	public void onCreate() {
		super.onCreate();

		slptClockGetParcel.m_uuid = null;

		SlptClock.setInService();

		slptClock.nativeInitSlpt();

		SlptClock.setBrightnessOfSlpt(DEFALT_SLPT_BRIGHTNESS);
		SlptClock.enableSlpt();
		SlptIsStarted = true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return stub.asBinder();
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;

	};

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
			slptClockGetParcel.writeClientDataToSlptEnd();
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
					Log.e(TAG,
							"sendSlptEnd:NOW UUID IS NOT SAME AS THE CURRENT UUID!");

				for (int i = 0; i < slptClockGetParcel.groupList.size(); i++)
					slptClockGetParcel.groupList.get(i).pictureList.clear();

				slptClockGetParcel.groupList.clear();
				slptClockGetParcel.sviewArr = null;
				slptClockGetParcel.m_uuid = null;

				slptClockGetParcel.notify();
			}

			return true;
		}

		int groupIndex = 0;
		int pictureIndex = 0;
		int picOffset = 0;
		int picSize = 0;
		PictureData pictureData = null;
		int[] picBuf = null;

		private void clearTmpData() {
			groupIndex = 0;
			pictureIndex = 0;
			picOffset = 0;
			picSize = 0;
			pictureData = null;
			picBuf = null;
		}

		@Override
		public boolean sendGroupData(SlptClock slptClock)
				throws RemoteException {
			slptClockGetParcel.groupList = slptClock.groupList;
			clearTmpData();

			slptClockGetParcel.writeClientDataToSlptStart();
			slptClockGetParcel
					.writeGroupDataToSlpt(slptClockGetParcel.groupList.get(0));

			return true;
		}

		@Override
		public boolean sendRleArray(int[] rle) throws RemoteException {
			int offset = 0;
			while (offset < rle.length) {
				offset = decompressPicture(rle, offset);
			}

			return true;
		}

		private void switchToNextPicture() {
			slptClockGetParcel.writePictureDataToSlpt(pictureData);
			pictureData = null;

			picOffset = 0;
			GroupData groupData = slptClockGetParcel.groupList.get(groupIndex);

			if (groupData.pictureNum > pictureIndex + 1) {
				pictureIndex++;
			} else if (groupIndex < slptClockGetParcel.groupList.size() - 1) {
				pictureIndex = 0;
				groupIndex++;

				while (slptClockGetParcel.groupList.get(groupIndex).pictureNum == 0
						&& groupIndex < slptClockGetParcel.groupList.size() - 1)
					groupIndex++;

				groupData = slptClockGetParcel.groupList.get(groupIndex);
				slptClockGetParcel.writeGroupDataToSlpt(groupData);
			}
		}

		private int decompressPicture(int[] rle, int offset) {
			if (picOffset == 0) {
				pictureData = slptClockGetParcel.groupList.get(groupIndex).pictureList
						.get(pictureIndex);

				picSize = pictureData.pictureSize;
				picBuf = new int[picSize];
				pictureData.bitmapBuffer = picBuf;
			}
			while (picOffset < picSize && offset < rle.length) {
				int color = rle[offset++];
				int count = rle[offset++];
				Arrays.fill(picBuf, picOffset, picOffset + count, color);
				picOffset += count;
			}

			if (picOffset == picSize)
				switchToNextPicture();

			return offset;
		}

	};

}
