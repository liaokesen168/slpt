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

package com.ingenic.iwds.utils;

/**
 * Iwds断言类.
 */
public class IwdsAssert {

    /**
     * 如果满足条件则挂起.
     *
     * @param who
     *            对象
     * @param condition
     *            条件
     * @param message
     *            信息
     */
    public static void dieIf(Object who, boolean condition, String message) {
        if (!condition)
            return;

        IwdsLog.e(who, "============= IWDS Assert Failed ============");
        IwdsLog.e(who, "Message: " + message);
        IwdsLog.e(who, "=============================================");
        Thread.dumpStack();
        IwdsLog.e(who, "============== IWDS Assert End ==============");

        /*
         * die, man...
         */
        ((IwdsAssert) null).die();
    }

    /**
     * 如果满足条件则挂起.
     *
     * @param tag
     *            标签
     * @param condition
     *            条件
     * @param message
     *            信息
     */
    public static void dieIf(String tag, boolean condition, String message) {
        if (!condition)
            return;

        IwdsLog.e(tag, "============= IWDS Assert Failed ============");
        IwdsLog.e(tag, "Message: " + message);
        IwdsLog.e(tag, "=============================================");
        Thread.dumpStack();
        IwdsLog.e(tag, "============== IWDS Assert End ==============");

        /*
         * die, man...
         */
        ((IwdsAssert) null).die();
    }

    private void die() {

    }
}
