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

package com.ingenic.iwds;

class IwdsInitializer {
    private static IwdsInitializer sm_theInitializer = null;

    public static IwdsInitializer getInstance() {
        if (sm_theInitializer == null)
            sm_theInitializer = new IwdsInitializer();

        return sm_theInitializer;
    }

    public void initialize(DeviceDescriptor deviceDescriptor) {
        System.loadLibrary("iwds");

        nativeInit(deviceDescriptor, BuildOptions.DEBUG);
    }

    private IwdsInitializer() {
    }

    private static native final void nativeInit(
            DeviceDescriptor deviceDescriptor, boolean enableDebug);
}
