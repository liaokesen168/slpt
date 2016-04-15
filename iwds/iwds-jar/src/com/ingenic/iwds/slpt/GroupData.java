package com.ingenic.iwds.slpt;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class GroupData implements Parcelable{
	public String groupName;
	public int pictureNum;
	public ArrayList<PictureData> pictureList = new ArrayList<PictureData>();
	
	public GroupData() {
		groupName = "";
		pictureNum = 0;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(pictureNum);
		dest.writeString(groupName);
		dest.writeInt(pictureList.size());
		for(int i = 0;i < pictureList.size(); i++)
			pictureList.get(i).writeToParcel(dest, flags);
	}
	
	public static final Parcelable.Creator<GroupData> CREATOR
		= new Parcelable.Creator<GroupData>() {

			@Override
			public GroupData createFromParcel(Parcel source) {
				int size = 0;
				GroupData groupData = new GroupData();
				groupData.pictureNum = source.readInt();
				groupData.groupName = source.readString();
				size = source.readInt();
				for (int i = 0; i < size; i++)
					groupData.pictureList.add(PictureData.CREATOR.createFromParcel(source));
				return groupData;
			}

			@Override
			public GroupData[] newArray(int size) {
				return new GroupData[size];
			}
		
		};
}
