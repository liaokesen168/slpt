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
import android.os.Bundle;
import com.ingenic.iwds.appwidget.IWidgetHost;
import com.ingenic.iwds.appwidget.WidgetProviderInfo;
import com.ingenic.iwds.widget.LocalRemoteViews;

interface IWidgetService {
//call from IWidgetHost
int[] startListening(IWidgetHost hcb, String hpkg, out List<LocalRemoteViews> updatedViews);

oneway void stopListening(String hpkg);

int bindWidget(String hpkg, in ComponentName provider);

LocalRemoteViews getWidgetViews(int wid);

oneway void deleteWidget(int wid);

//call from WidgetManager
void getWidgetSize(int wid, out int[] size);

oneway void updateWidgetSize(int wid, int w, int h);

List<WidgetProviderInfo> getInstalledProviders();

//call from WidgetProvider
oneway void updateWidget(int wid, in LocalRemoteViews views);

oneway void updateWidgets(in int[] wids, in LocalRemoteViews views);

//call from LocalRemoteViewsAdapter
oneway void bindRemoteViewsService(int widgetId, in Intent intent, IBinder connection);

oneway void unbindRemoteViewsService(int widgetId, in Intent intent);
}