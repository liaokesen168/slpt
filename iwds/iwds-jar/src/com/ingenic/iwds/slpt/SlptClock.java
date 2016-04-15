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

package com.ingenic.iwds.slpt;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

import com.ingenic.iwds.slpt.view.core.Picture.PictureContainer;
import com.ingenic.iwds.slpt.view.core.SlptViewComponent;
import com.ingenic.iwds.slpt.view.core.SlptViewComponent.RegisterPictureParam;
import com.ingenic.iwds.slpt.view.core.SlptLayout;
import com.ingenic.iwds.slpt.view.utils.KeyWriter;
import com.ingenic.iwds.slpt.view.utils.SimpleFile;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

/**
 * 目前slpt并没有在远端创建service，所以不要在多个apk中调用slpt的内容，这样会导致冲突!<br>
 * 请参考SlptTestDemo，里面有内置表盘的源码，以及slpt的调用示例<br>
 */
public class SlptClock implements Parcelable{
	private static final String TAG = "SlptClock";
	private static boolean nativeIsInitialized = false;
	private static boolean inService = false;
	public byte[] sviewArr = null;
	public String m_uuid = UUID.randomUUID().toString();
	public ArrayList<GroupData> groupList = new ArrayList<GroupData>();
	static public ISlptClockService clockService = null;

	SlptLayout rootView = null;

	public SlptClock(SlptLayout rootView) {
		setRootView(rootView);
	}

	public SlptClock() {
	}

	public SlptClock(int aaa) {
	}

	/**
	 * 设置最顶端的sview
	 */
	public void setRootView(SlptLayout rootView) {
		IwdsAssert.dieIf(TAG, rootView == null, "rootView can not be null!");
		this.rootView = rootView;
	}

	/**
	 * 获取最顶端的sview
	 */
	public SlptLayout getRootView() {
		return rootView;
	}

	private static native int initSlpt();

	/**
	 * 打开slpt，深度休眠时会进入slpt
	 */
	public static native int enableSlpt();

	/**
	 * 关闭slpt，深度休眠时不会进入slpt
	 */
	public static native int disableSlpt();

	/**
	 * 设置slpt打开时的背光，如果lcd支持的话就会生效
	 */
	public static native int setBrightnessOfSlpt(int brightness);

	private static native void requestSlptDisplayPause();

	private static native void requestSlptDisplayResume();

	private static native void initSview(long writerJniPrivate);

	private static native void initSview(byte[] arr);

	static {
		try {
			System.loadLibrary("slpt-linux");
			nativeIsInitialized = true;
			IwdsLog.d(TAG, "loadLibrary Successed!");
		} catch (Exception e) {
			nativeIsInitialized = false;
			IwdsLog.d(TAG, "loadLibrary Exception " + e.getMessage());
		}
	}

	static public void setInService() {
		inService = true;
	}

	/**
	 * 初始化slpt
	 */
	public boolean nativeInitSlpt() {
		nativeIsInitialized = initSlpt() == 0;
		return true;
	}

	/**
	 * 将配置写入slpt
	 */
	public boolean writeToSlpt() {
		if (!nativeIsInitialized)
			return false;

		KeyWriter writer = new KeyWriter();
		PictureContainer container = new PictureContainer();

		RegisterPictureParam param = new RegisterPictureParam();
		param.backgroundColor = SlptViewComponent.INVALID_COLOR;
		rootView.registerPicture(container, param);

		requestSlptDisplayPause();

		PictureContainer.writeToSlpt(container);
		rootView.writeConfigure(writer);

		initSview(writer.getJniPrivate());

		requestSlptDisplayResume();

		writer.recycle();
		container = null;

		return true;
	}

	/**
	 * 获得图片和view的数据
	 */
	private void getDialData() {

		KeyWriter writer = new KeyWriter();
		PictureContainer container = new PictureContainer();

		RegisterPictureParam param = new RegisterPictureParam();
		param.backgroundColor = SlptViewComponent.INVALID_COLOR;
		rootView.registerPicture(container, param);

		PictureContainer.writePictureToNativeList(this, container);
		rootView.writeConfigure(writer);

		sviewArr = writer.getRawBytes();

		writer.recycle();
		container = null;

	}

	/**
	 * 客户端的接口函数：获取slpt数据并将数据发送到服务端
	 */
	public void sendToService() {
		if (inService == true)
			writeToSlpt();
		else {
			if (sendStart() != true)
				return;

			getDialData();

			sendSlptData();

			sendEnd();
		}

	}

	private final static int rleBufSize = 10 * 1000;
	private final static int rle_buf[] = new int[rleBufSize];
	private int rlePictureSize = 0;

	private void sendRlePicture(RleBuffer rleBuffer) {
		try {
			int[] rleBuf = rleBuffer.getBuffer();
			clockService.sendRleArray(rleBuf);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void addPictureData(RleBuffer rleBuffer, PictureData pictureData) {
		int[] bitMap = pictureData.bitmapBuffer;
		int offset = rleBuffer.add(bitMap, 0);
		int picSize = bitMap.length;

		while (offset < picSize) {
			sendRlePicture(rleBuffer);
			offset = rleBuffer.add(bitMap, offset);
		}
	}

	/**
	 * 传输压缩图片
	 */
	private void sendCompressPicture() {
		RleBuffer rleBuffer = new RleBuffer();

		for (int i = 0; i < groupList.size(); i++) {
			
			ArrayList<PictureData> picList = groupList.get(i).pictureList;

			for (int j = 0; j < picList.size(); j++) {
				addPictureData(rleBuffer, picList.get(j));
			}
		}

		if (rleBuffer.getOffset() != 0)
			sendRlePicture(rleBuffer);
	}

	/**
	 * 传输数据
	 */
	private void sendSlptData() {
		try {
			clockService.sendGroupData(this);
			sendCompressPicture();
			clockService.sendViewArr(sviewArr);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 传输开始
	 */
	private boolean sendStart() {
		try {
			if (clockService.sendSlptStart(m_uuid) != true)
				return false;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 传输结束
	 */
	private void sendEnd() {
		try {
			clockService.sendSlptEnd(m_uuid);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 服务端以流式的方式把接收到客户端的数据写进slpt中
	 */
	public boolean writeClientDataToSlptStart() {
		if (!nativeIsInitialized)
			return false;

		requestSlptDisplayPause();
		
		PictureContainer.clearPictureGroup();
		
		return true;
	}
	
	public boolean writeGroupDataToSlpt(GroupData groupData) {
		PictureContainer.addPictureGroup(groupData.groupName);
		return true;
	}
	
	public boolean writePictureDataToSlpt(PictureData pictureData) {
		PictureContainer.addPicture("" + pictureData.pictureIndex, pictureData.width, pictureData.height,
				pictureData.bitmapBuffer, pictureData.backgroundColor);
		return true;
	}
	
	public boolean writeClientDataToSlptEnd() {
		initSview(this.sviewArr);

		requestSlptDisplayResume();

		return true;
	}

	/**
	 * 客户端定义的回调
	 */
	static Callback m_callback = null;

	static Context m_Context = null;

	static public void bindService(Context context, Callback callback) {
		String ACTION = "com.ingenic.iwds.slpt.SlptClockService";
		Intent intent;

		intent = new Intent(ACTION);
		context.startService(intent);

		context.bindService(intent, connection, Service.BIND_AUTO_CREATE);
		m_callback = callback;
	}

	static public void unbindService(Context context) {
		context.unbindService(connection);
	}

	public interface Callback {
		public void onServiceDisconnected();

		public void onServiceConnected();
	}

	static ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "onServiceDisconnected ---------------!");
			clockService = null;
			if (m_callback != null)
				m_callback.onServiceDisconnected();
			m_callback = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "onServiceConnected ---------------!");
			clockService = ISlptClockService.Stub.asInterface(service);
			if (m_callback != null)
				m_callback.onServiceConnected();
		}
	};

	/**
	 * 整形转换成Byte数组
	 * 
	 * @param value 被转换的整形
	 *
	 * @return 返回值为一个byte数组
	 */
	static public byte[] intToByte(int value) {
		byte[] arr = new byte[4];
		arr[0] = (byte) (value & 0xff);
		arr[1] = (byte) ((value >> 8) & 0xff);
		arr[2] = (byte) ((value >> 16) & 0xff);
		arr[3] = (byte) (value >> 24);

		return arr;
	}

	static public void writeIntToFile(FileOutputStream fileOut, int value) {
		byte[] arr = intToByte(value);
		try {
			fileOut.write(arr);
			fileOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 把Slpt数据写到文件中
	 */
	private void writeSlptToFile() {
		FileOutputStream fileOut = SimpleFile
				.getOutputStream("data/slpt/slpt.txt");

		int value = sviewArr.length;
		writeIntToFile(fileOut, value);
		try {
			fileOut.write(sviewArr);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		value = groupList.size();
		writeIntToFile(fileOut, value);

		for (int i = 0; i < groupList.size(); i++) {
			ArrayList<PictureData> pictureList = groupList.get(i).pictureList;

			value = pictureList.size();
			writeIntToFile(fileOut, value);

			for (int j = 0; j < pictureList.size(); j++) {

				value = pictureList.get(j).pictureIndex;
				writeIntToFile(fileOut, value);

				value = pictureList.get(j).width;
				writeIntToFile(fileOut, value);

				value = pictureList.get(j).height;
				writeIntToFile(fileOut, value);

				value = pictureList.get(j).backgroundColor;
				writeIntToFile(fileOut, value);

				value = pictureList.get(j).bitmapBuffer.length;
				int pictureSize = value;
				writeIntToFile(fileOut, value);

				int[] bitmapbuffer = pictureList.get(j).bitmapBuffer;
				for (int z = 0; z < pictureSize; z++) {
					writeIntToFile(fileOut, bitmapbuffer[z]);
				}

			}

		}
		try {
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * Parcel
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(groupList.size());
		
		for(int i = 0; i < groupList.size(); i++)
			groupList.get(i).writeToParcel(dest, flags);
		
	}
	
	public static final Parcelable.Creator<SlptClock> CREATOR = new Creator<SlptClock>() {
		
		@Override
		public SlptClock[] newArray(int size) {
			return new SlptClock[size];
		}
		
		@Override
		public SlptClock createFromParcel(Parcel source) {
			SlptClock slptClock = new SlptClock();
			int size = source.readInt();
			for (int i = 0; i < size; i++)
				slptClock.groupList.add(GroupData.CREATOR.createFromParcel(source));
			return slptClock;
			
		}
	};

}
