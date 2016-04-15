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

import android.util.Log;

import com.ingenic.iwds.BuildOptions;

/**
 * Iwds日志类.
 */
public class IwdsLog {
    private static final String LOG_TAG_PREFIX = "IWDS---";

    /**
     * 调试.
     *
     * @param who
     *            对象
     * @param message
     *            消息
     */
    public static void d(Object who, String message) {
        if (BuildOptions.DEBUG)
            Log.d(LOG_TAG_PREFIX + who.getClass().getSimpleName(), message);
    }

    /**
     * 信息.
     *
     * @param who
     *            对象
     * @param message
     *            消息
     */
    public static void i(Object who, String message) {
        Log.i(LOG_TAG_PREFIX + who.getClass().getSimpleName(), message);
    }

    /**
     * 详细信息.
     *
     * @param who
     *            对象
     * @param message
     *            消息
     */
    public static void v(Object who, String message) {
        Log.v(LOG_TAG_PREFIX + who.getClass().getSimpleName(), message);
    }

    /**
     * 错误.
     *
     * @param who
     *            对象
     * @param message
     *            消息
     */
    public static void e(Object who, String message) {
        Log.e(LOG_TAG_PREFIX + who.getClass().getSimpleName(), message);
    }

    /**
     * 警告.
     *
     * @param who
     *            对象
     * @param message
     *            消息
     */
    public static void w(Object who, String message) {
        Log.w(LOG_TAG_PREFIX + who.getClass().getSimpleName(), message);
    }

    /**
     * 调试.
     *
     * @param who
     *            对象
     * @param message
     *            消息
     * @param tr
     *            异常
     */
    public static void d(Object who, String message, Throwable tr) {
        if (BuildOptions.DEBUG)
            Log.d(LOG_TAG_PREFIX + who.getClass().getSimpleName(), message, tr);
    }

    /**
     * 信息.
     *
     * @param who
     *            对象
     * @param message
     *            消息
     * @param tr
     *            异常
     */
    public static void i(Object who, String message, Throwable tr) {
        Log.i(LOG_TAG_PREFIX + who.getClass().getSimpleName(), message, tr);
    }

    /**
     * 详细信息.
     *
     * @param who
     *            对象
     * @param message
     *            消息
     * @param tr
     *            异常
     */
    public static void v(Object who, String message, Throwable tr) {
        Log.v(LOG_TAG_PREFIX + who.getClass().getSimpleName(), message, tr);
    }

    /**
     * 错误.
     *
     * @param who
     *            对象
     * @param message
     *            消息
     * @param tr
     *            异常
     */
    public static void e(Object who, String message, Throwable tr) {
        Log.e(LOG_TAG_PREFIX + who.getClass().getSimpleName(), message, tr);
    }

    /**
     * 警告.
     *
     * @param who
     *            对象
     * @param message
     *            消息
     * @param tr
     *            异常
     */
    public static void w(Object who, String message, Throwable tr) {
        Log.w(LOG_TAG_PREFIX + who.getClass().getSimpleName(), message, tr);
    }

    /**
     * 调试.
     *
     * @param tag
     *            标签
     * @param message
     *            消息
     */
    public static void d(String tag, String message) {
        if (BuildOptions.DEBUG)
            Log.d(LOG_TAG_PREFIX + tag, message);
    }

    /**
     * 信息.
     *
     * @param tag
     *            标签
     * @param message
     *            消息
     */
    public static void i(String tag, String message) {
        Log.i(LOG_TAG_PREFIX + tag, message);
    }

    /**
     * 详细信息.
     *
     * @param tag
     *            标签
     * @param message
     *            消息
     */
    public static void v(String tag, String message) {
        Log.v(LOG_TAG_PREFIX + tag, message);
    }

    /**
     * 错误.
     *
     * @param tag
     *            标签
     * @param message
     *            消息
     */
    public static void e(String tag, String message) {
        Log.e(LOG_TAG_PREFIX + tag, message);
    }

    /**
     * 警告.
     *
     * @param tag
     *            标签
     * @param message
     *            消息
     */
    public static void w(String tag, String message) {
        Log.w(LOG_TAG_PREFIX + tag, message);
    }

    /**
     * 调试.
     *
     * @param tag
     *            标签
     * @param message
     *            消息
     * @param tr
     *            异常
     */
    public static void d(String tag, String message, Throwable tr) {
        if (BuildOptions.DEBUG)
            Log.d(LOG_TAG_PREFIX + tag, message, tr);
    }

    /**
     * 信息.
     *
     * @param tag
     *            标签
     * @param message
     *            消息
     * @param tr
     *            异常
     */
    public static void i(String tag, String message, Throwable tr) {
        Log.i(LOG_TAG_PREFIX + tag, message, tr);
    }

    /**
     * 详细信息.
     *
     * @param tag
     *            标签
     * @param message
     *            消息
     * @param tr
     *            异常
     */
    public static void v(String tag, String message, Throwable tr) {
        Log.v(LOG_TAG_PREFIX + tag, message, tr);
    }

    /**
     * 错误.
     *
     * @param tag
     *            标签
     * @param message
     *            消息
     * @param tr
     *            异常
     */
    public static void e(String tag, String message, Throwable tr) {
        Log.e(LOG_TAG_PREFIX + tag, message, tr);
    }

    /**
     * 警告.
     *
     * @param tag
     *            标签
     * @param message
     *            消息
     * @param tr
     *            异常
     */
    public static void w(String tag, String message, Throwable tr) {
        Log.w(LOG_TAG_PREFIX + tag, message, tr);
    }
}
