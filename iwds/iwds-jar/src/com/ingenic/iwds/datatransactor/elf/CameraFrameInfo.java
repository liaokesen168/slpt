/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  SunWenZhong(Fighter) <wenzhong.sun@ingenic.com, wanmyqawdr@126.com>
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
 *
 */

package com.ingenic.iwds.datatransactor.elf;

import java.util.Arrays;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Camera帧信息类.
 */
public class CameraFrameInfo implements Parcelable {

    /** 帧数据. */
    public byte[] frameData;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(frameData.length);
        dest.writeByteArray(frameData);
    }

    public static final Creator<CameraFrameInfo> CREATOR = new Creator<CameraFrameInfo>() {
        @Override
        public CameraFrameInfo createFromParcel(Parcel source) {
            CameraFrameInfo frame = new CameraFrameInfo();

            frame.frameData = new byte[source.readInt()];
            source.readByteArray(frame.frameData);

            return frame;
        }

        @Override
        public CameraFrameInfo[] newArray(int size) {
            return new CameraFrameInfo[size];
        }
    };

    @Override
    public String toString() {
        return "CameraFrameInfo [frameData=" + Arrays.toString(frameData) + "]";
    }
}
