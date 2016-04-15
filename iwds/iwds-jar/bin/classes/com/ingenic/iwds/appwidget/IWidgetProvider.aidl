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
import com.ingenic.iwds.appwidget.IWidgetService;

interface IWidgetProvider {

oneway void enableProvider(IWidgetService service, String pcls);

oneway void providerAdded(int wid, String pcls);

oneway void deleteProvider(int wid, String pcls);

oneway void disableProvider(String pcls);

}