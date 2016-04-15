/*
 *  Copyright (C) 2015 Ingenic Semiconductor
 *
 *  TaoZhang(Kevin)<tao.zhang@ingenic.com>
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

import android.os.Parcel;
import android.os.Parcelable;

import com.ingenic.iwds.utils.IwdsAssert;

/**
 * 日出日落同步类.
 */
public class TimeAdditionalInfo implements Parcelable {

    /** 日出时间 */
    public String sunrise;
    /** 日落时间 */
    public String sunset;

    public TimeAdditionalInfo(String sunrise, String sunset) {
        IwdsAssert.dieIf(this, sunrise == null, "sunrise is null.");
        IwdsAssert.dieIf(this, sunset == null, "sunset is null.");

        this.sunrise = sunrise;
        this.sunset = sunset;
    }

    private TimeAdditionalInfo() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sunrise);
        dest.writeString(sunset);
    }

    public static final Creator<TimeAdditionalInfo> CREATOR = new Creator<TimeAdditionalInfo>() {
        @Override
        public TimeAdditionalInfo createFromParcel(Parcel source) {
            TimeAdditionalInfo timeAdditional = new TimeAdditionalInfo();

            timeAdditional.sunrise = source.readString();
            timeAdditional.sunset = source.readString();

            return timeAdditional;
        }

        @Override
        public TimeAdditionalInfo[] newArray(int size) {
            return new TimeAdditionalInfo[size];
        }
    };

    @Override
    public String toString() {
        return "TimeAdditionalInfo [sunrise = " + sunrise + " sunset = "
                + sunset + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }
}