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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Iwds兼容性检查者.
 */
public class IwdsCompatibilityChecker {
    private static IwdsCompatibilityChecker sm_theChecker;
    private boolean m_isValid;

    private IwdsCompatibilityChecker() {
        m_isValid = false;

        File file = new File("/proc/cpuinfo");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));

            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                if (tempString.contains("Ingenic Xburst")) {
                    m_isValid = true;

                    break;

                } else if (tempString.contains("goldfish")) {
                    m_isValid = true;

                    break;
                }
            }

            reader.close();

        } catch (IOException e) {
            // ignore

        } finally {
            if (reader != null) {
                try {
                    reader.close();

                } catch (IOException e1) {
                    // ignore
                }
            }
        }
    }

    /**
     * 获取Iwds兼容性检查者的单例对象.
     *
     * @return 返回Iwds兼容性检查者的单例对象.
     */
    public static IwdsCompatibilityChecker getInstance() {
        if (sm_theChecker == null)
            sm_theChecker = new IwdsCompatibilityChecker();

        return sm_theChecker;
    }

    /**
     * 检查是否兼容.
     *
     * @return 返回{@code true},如果兼容.
     */
    public final boolean check() {
        return m_isValid;
    }
}
