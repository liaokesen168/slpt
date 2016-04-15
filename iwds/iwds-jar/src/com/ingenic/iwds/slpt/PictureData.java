package com.ingenic.iwds.slpt;

import android.os.Parcel;
import android.os.Parcelable;

public class PictureData implements Parcelable {

	public int pictureSize;
	public int pictureIndex;
	public int width;
	public int height;
	public int backgroundColor;
	public int[] bitmapBuffer = null;

	public PictureData() {
		pictureSize = 0;
		pictureIndex = 0;
		width = 0;
		height = 0;
		backgroundColor = 0;
		bitmapBuffer = null;
	}

	@Override
	public int describeContents() {
		return 0;
	}

		
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(bitmapBuffer.length);
		dest.writeInt(pictureIndex);
		dest.writeInt(width);
		dest.writeInt(height);
		dest.writeInt(backgroundColor);
		// dest.writeIntArray(bitmapBuffer);
	}

	public static final Parcelable.Creator<PictureData> CREATOR = new Parcelable.Creator<PictureData>() {

		@Override
		public PictureData createFromParcel(Parcel source) {
			PictureData pictureData = new PictureData();
			pictureData.pictureSize = source.readInt();
			pictureData.pictureIndex = source.readInt();
			pictureData.width = source.readInt();
			pictureData.height = source.readInt();
			pictureData.backgroundColor = source.readInt();
			// pictureData.bitmapBuffer = source.createIntArray();
			return pictureData;
		}

		@Override
		public PictureData[] newArray(int size) {
			return new PictureData[size];
		}
	};

}
