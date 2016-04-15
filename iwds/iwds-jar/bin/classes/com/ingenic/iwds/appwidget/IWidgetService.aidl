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
import com.ingenic.iwds.appwidget.IWidgetHost;
import com.ingenic.iwds.appwidget.WidgetProviderInfo;
import com.ingenic.iwds.widget.RemoteViews;
 
interface IWidgetService {

List<WidgetProviderInfo> getInstalledProviders(int cf);

int[] startListening(IWidgetHost hcb, String hpkg, int hid, out List<com.ingenic.iwds.widget.RemoteViews> updateds);

oneway void stopListening(String hpkg, int hid);

int bindProvider(String hpkg, int hid, in ComponentName provider, boolean cache);

com.ingenic.iwds.widget.RemoteViews getWidgetViews(int wid);

WidgetProviderInfo getWidgetInfo(int wid);

oneway void deleteWidget(int wid);

int[] getWidgetIds(String hpkg, in ComponentName provider);

oneway void updateWidgets(in int[] wids, in com.ingenic.iwds.widget.RemoteViews views);

oneway void partiallyUpdateWidgets(in int[] wids, in com.ingenic.iwds.widget.RemoteViews views);

oneway void updateAllWidgets(in ComponentName provider, in com.ingenic.iwds.widget.RemoteViews views);

oneway void partiallyUpdateAllWidgets(in ComponentName provider, in com.ingenic.iwds.widget.RemoteViews views);

oneway void notifyAllViewsDataChanged(in ComponentName provider, int vid);

oneway void notifyViewDatasChanged(in int[] wids, int vid);

}