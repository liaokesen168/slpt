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

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.ingenic.iwds.content.RemoteIntent;

class RemoteBroadcast implements Parcelable {

    private int mCallerId;
    private RemoteIntent mIntent;
    private String mPermission;
    private boolean mIsSticky;
    private boolean mIsOrdered;

    public RemoteBroadcast(int callerId, Intent intent) {
        this(callerId, intent, null);
    }

    public RemoteBroadcast(int callerId, Intent intent, String permission) {
        this(callerId, intent, permission, false, false);
    }

    public RemoteBroadcast(int callerId, Intent intent, String permission, boolean sticky,
            boolean ordered) {
        this(callerId, RemoteIntent.fromIntent(intent), permission, sticky, ordered);
    }

    public RemoteBroadcast(int callerId, RemoteIntent intent) {
        this(callerId, intent, null);
    }

    public RemoteBroadcast(int callerId, RemoteIntent intent, String permission) {
        this(callerId, intent, permission, false, false);
    }

    public RemoteBroadcast(int callerId, RemoteIntent intent, String permission, boolean sticky,
            boolean ordered) {
        mCallerId = callerId;
        mIntent = intent;
        mPermission = permission;
        mIsSticky = sticky;
        mIsOrdered = ordered;
    }

    protected RemoteBroadcast(Parcel in) {
        readFromParcel(in);
    }

    public int getCallerId() {
        return mCallerId;
    }

    public boolean isSticky() {
        return mIsSticky;
    }

    public boolean isOrdered() {
        return mIsOrdered;
    }

    public RemoteIntent getIntent() {
        return mIntent;
    }

    public String getPermission() {
        return mPermission;
    }

    @Override
    public int describeContents() {
        return mIntent != null ? mIntent.describeContents() : 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mCallerId);

        if (mIntent != null) {
            dest.writeInt(1);
            mIntent.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }

        dest.writeString(mPermission);
        dest.writeInt(mIsSticky ? 1 : 0);
        dest.writeInt(mIsOrdered ? 1 : 0);
    }

    public void readFromParcel(Parcel in) {
        mCallerId = in.readInt();

        if (in.readInt() != 0) {
            mIntent = RemoteIntent.CREATOR.createFromParcel(in);
        }

        mPermission = in.readString();
        mIsSticky = in.readInt() != 0;
        mIsOrdered = in.readInt() != 0;
    }

    public static final Parcelable.Creator<RemoteBroadcast> CREATOR = new Parcelable.Creator<RemoteBroadcast>() {

        @Override
        public RemoteBroadcast createFromParcel(Parcel source) {
            return new RemoteBroadcast(source);
        }

        @Override
        public RemoteBroadcast[] newArray(int size) {
            return new RemoteBroadcast[size];
        }
    };
}