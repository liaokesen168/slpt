/*
 * Copyright (C) 2015 Ingenic Semiconductor
 * 
 * LiJinWen(Kevin)<kevin.jwli@ingenic.com>
 * 
 * Elf/iwds-jar
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.ingenic.iwds.appwidget;

import android.content.ComponentName;
import android.content.pm.ServiceInfo;
import android.os.Parcel;
import android.os.Parcelable;

public class WidgetProviderInfo implements Parcelable {

    public ComponentName provider;

    int width;

    int height;

    int updatePeriodMillis;

    int initialLayout;

    ComponentName configure;

    String label;

    int icon;

    int previewImage;

    int autoAdvanceViewId;

    ServiceInfo providerInfo;

    public WidgetProviderInfo() {}

    public WidgetProviderInfo(WidgetProviderInfo in) {
        ComponentName thatProvider = in.provider;
        provider = thatProvider == null ? null : thatProvider.clone();

        width = in.width;
        height = in.height;
        updatePeriodMillis = in.updatePeriodMillis;
        initialLayout = in.initialLayout;

        ComponentName thatConfigure = in.configure;
        configure = thatConfigure == null ? null : thatConfigure.clone();

        label = in.label;
        icon = in.icon;
        previewImage = in.previewImage;
        autoAdvanceViewId = in.autoAdvanceViewId;
        providerInfo = in.providerInfo;
    }

    public WidgetProviderInfo(Parcel in) {
        readFromParcel(in);
    }

    public WidgetProviderInfo clone() {
        return new WidgetProviderInfo(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (provider != null) {
            dest.writeInt(1);
            provider.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }

        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeInt(updatePeriodMillis);
        dest.writeInt(initialLayout);

        if (configure != null) {
            dest.writeInt(1);
            configure.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }

        dest.writeString(label);
        dest.writeInt(icon);
        dest.writeInt(previewImage);
        dest.writeInt(autoAdvanceViewId);

        if (providerInfo != null) {
            dest.writeInt(1);
            providerInfo.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
    }

    public void readFromParcel(Parcel in) {
        if (in.readInt() != 0) {
            provider = ComponentName.CREATOR.createFromParcel(in);
        }

        width = in.readInt();
        height = in.readInt();
        updatePeriodMillis = in.readInt();
        initialLayout = in.readInt();

        if (in.readInt() != 0) {
            configure = ComponentName.CREATOR.createFromParcel(in);
        }

        label = in.readString();
        icon = in.readInt();
        previewImage = in.readInt();
        autoAdvanceViewId = in.readInt();

        if (in.readInt() != 0) {
            providerInfo = ServiceInfo.CREATOR.createFromParcel(in);
        }
    }

    public static final Creator<WidgetProviderInfo> CREATOR = new Creator<WidgetProviderInfo>() {

        @Override
        public WidgetProviderInfo createFromParcel(Parcel source) {
            return new WidgetProviderInfo(source);
        }

        @Override
        public WidgetProviderInfo[] newArray(int size) {
            return new WidgetProviderInfo[size];
        }
    };
}