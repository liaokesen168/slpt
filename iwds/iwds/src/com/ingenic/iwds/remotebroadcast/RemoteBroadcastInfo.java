/*
 * Copyright (C) 2015 Ingenic Semiconductor
 * 
 * LiJingWen(Kevin) <kevin.jwli@ingenic.com>
 * 
 * Elf/IDWS Project
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.ingenic.iwds.remotebroadcast;

import android.content.IntentFilter;
import android.os.Parcel;
import android.os.Parcelable;
import com.ingenic.iwds.content.RemoteIntent;
import com.ingenic.iwds.os.SafeParcelable;

abstract class RemoteBroadcastInfo {
    protected int mCallerId;
    protected int mId;

    public int getCallerId() {
        return mCallerId;
    }

    public int getId() {
        return mId;
    }
}

class RemoteIntentFilterInfo extends RemoteBroadcastInfo implements Parcelable {

    private IntentFilter mFilter;
    private String mPermission;

    public RemoteIntentFilterInfo(int callerId, int id, IntentFilter filter, String permission) {
        mCallerId = callerId;
        mId = id;
        mFilter = filter;
        mPermission = permission;
    }

    protected RemoteIntentFilterInfo(Parcel in) {
        readFromParcel(in);
    }

    public IntentFilter getFilter() {
        return mFilter;
    }

    public String getPermission() {
        return mPermission;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mCallerId);
        dest.writeInt(mId);

        if (mFilter != null) {
            dest.writeInt(1);
            mFilter.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }

        dest.writeString(mPermission);
    }

    public void readFromParcel(Parcel in) {
        mCallerId = in.readInt();
        mId = in.readInt();

        if (in.readInt() != 0) {
            mFilter = IntentFilter.CREATOR.createFromParcel(in);
        }

        mPermission = in.readString();
    }

    public static final Parcelable.Creator<RemoteIntentFilterInfo> CREATOR = new Parcelable.Creator<RemoteIntentFilterInfo>() {

        @Override
        public RemoteIntentFilterInfo createFromParcel(Parcel source) {
            return new RemoteIntentFilterInfo(source);
        }

        @Override
        public RemoteIntentFilterInfo[] newArray(int size) {
            return new RemoteIntentFilterInfo[size];
        }
    };

    @Override
    public String toString() {
        return "RemoteIntentFilterInfo[id:" + mId + "-IntentFilter:" + mFilter + "-permission:"
                + mPermission + "]";
    }
}

class RemoteIntentInfo extends RemoteBroadcastInfo implements Parcelable {

    private RemoteIntent mIntent;

    public RemoteIntentInfo(int callerId, int id, RemoteIntent intent) {
        mCallerId = callerId;
        mId = id;
        mIntent = intent;
    }

    protected RemoteIntentInfo(Parcel in) {
        readFromParcel(in);
    }

    public RemoteIntent getIntent() {
        return mIntent;
    }

    @Override
    public int describeContents() {
        return mIntent != null ? mIntent.describeContents() : 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mCallerId);
        dest.writeInt(mId);

        if (mIntent != null) {
            dest.writeInt(1);
            mIntent.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public void readFromParcel(Parcel in) {
        mCallerId = in.readInt();
        mId = in.readInt();

        if (in.readInt() != 0) {
            mIntent = RemoteIntent.CREATOR.createFromParcel(in);
        }
    }

    public static final Parcelable.Creator<RemoteIntentInfo> CREATOR = new Parcelable.Creator<RemoteIntentInfo>() {

        @Override
        public RemoteIntentInfo createFromParcel(Parcel source) {
            return new RemoteIntentInfo(source);
        }

        @Override
        public RemoteIntentInfo[] newArray(int size) {
            return new RemoteIntentInfo[size];
        }
    };

    @Override
    public String toString() {
        return "RemoteIntentInfo[-id" + mId + "-Intent:" + mIntent + "]";
    }
}

class UnregisterInfo extends RemoteBroadcastInfo implements Parcelable {

    public UnregisterInfo(int callerId, int id) {
        mCallerId = callerId;
        mId = id;
    }

    protected UnregisterInfo(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mCallerId);
        dest.writeInt(mId);
    }

    public void readFromParcel(Parcel in) {
        mCallerId = in.readInt();
        mId = in.readInt();
    }

    public static final Creator<UnregisterInfo> CREATOR = new Creator<UnregisterInfo>() {

        @Override
        public UnregisterInfo createFromParcel(Parcel source) {
            return new UnregisterInfo(source);
        }

        @Override
        public UnregisterInfo[] newArray(int size) {
            return new UnregisterInfo[size];
        }
    };
}