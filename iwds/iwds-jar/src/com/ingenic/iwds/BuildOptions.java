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

import android.os.Build;

/**
 * 编译选项类
 */
public class BuildOptions extends Build {

    /** 常量: 控制是否按DEBUG模式编译 */
    public static final boolean DEBUG = true;

    private static class IWDS_VERSION_CODES {
        public static final int VER_1_0_0 = 10000;
    }

    /** 版本号常量. */
    public static final int IWDS_VERSION_INT = IWDS_VERSION_CODES.VER_1_0_0;
}
