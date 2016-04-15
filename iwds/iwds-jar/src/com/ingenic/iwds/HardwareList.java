/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  Kage Shen <kuikui.shen@ingenic.com>
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

public class HardwareList {

    private static String TAG = "HardwareList: ";

    private static final String sLIST_FILE = "/proc/hardware/list";

    /*
     * --------------- All pre-defined key set ------------
     */
    /** hardware bluetooth chip name */
    public static final String KEY_BLUETOOTH_CHIP = "bluetooth.chip";
    /** hardware lcd exterior */
    public static final String KEY_LCD_EXTERIOR = "lcd.exterior";
    /** lcd dpi */
    public static final String KEY_LCD_DPI = "lcd.dpi";
    /** lcd ppi */
    public static final String KEY_LCD_PPI = "lcd.ppi";
    /** lcd size */
    public static final String KEY_LCD_SIZE = "lcd.size";

    /*
     * --------------- All pre-defined value set ------------
     */
    /** lcd exterior square */
    public static final String VALUE_LCD_EXTERIOR_SQUARE = "square";
    /** lcd exterior round */
    public static final String VALUE_LCD_EXTERIOR_ROUND = "round";

    private static String mKeyValue[] = {
        KEY_BLUETOOTH_CHIP,
        KEY_LCD_EXTERIOR,
        KEY_LCD_DPI,
        KEY_LCD_PPI,
        KEY_LCD_SIZE
    };

    private static Hashtable<String, String> smHardwareTable = null;

    static {
        BufferedReader fReader = null;

        smHardwareTable = new Hashtable<String, String>();

        try {
            fReader = new BufferedReader(new FileReader(sLIST_FILE));

            String line;
            while ((line = fReader.readLine()) != null) {
                String[] pair = line.split(":");

                IwdsAssert.dieIf("HardwareList", pair == null
                        || pair.length < 2, "Invalid line: " + line);

                String key = pair[0].trim();
                String value = pair[1].trim();

                boolean found = false;
                for (String predefined : mKeyValue) {
                    if (key.equals(predefined)) {
                        found = true;
                        break;
                    }
                }

                if (!found)
                    continue;

                IwdsAssert.dieIf("HardwareList",
                        smHardwareTable.get(key) != null, "Dunplicate Key: "
                                + key);

                smHardwareTable.put(key, value);
            }
        } catch (FileNotFoundException e) {
            IwdsLog.e(TAG, "Exception occurred trying to open " + sLIST_FILE);
            smHardwareTable = null;
        } catch (IOException e) {
            IwdsLog.e(TAG, "Exception occurred trying to read " + sLIST_FILE);
            smHardwareTable = null;
        } finally {
            if (fReader != null) {
                try {
                    fReader.close();
                } catch (IOException e) {
                    IwdsLog.e(TAG, "Exception occurred trying to close "
                            + sLIST_FILE);
                }
            }
        }
    }

    private HardwareList() {
    }

    public static synchronized String getHardwareValue(String key, String defs) {
        String val = getHardwareValue(key);
        if (val == null)
            return defs;
        return val;
    }

    public static synchronized String getHardwareValue(String key) {
        if (smHardwareTable == null)
            return null;
        return smHardwareTable.get(key);
    }

    public static boolean IsCircularScreen() {
        String lcdExterior;
        lcdExterior = HardwareList.getHardwareValue(
                HardwareList.KEY_LCD_EXTERIOR, BuildOptions.UNKNOWN);
        if (lcdExterior.equals(HardwareList.VALUE_LCD_EXTERIOR_ROUND)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean IsSquareScreen() {
        String lcdExterior;
        lcdExterior = HardwareList.getHardwareValue(
                HardwareList.KEY_LCD_EXTERIOR, BuildOptions.UNKNOWN);
        if (lcdExterior.equals(HardwareList.VALUE_LCD_EXTERIOR_SQUARE)) {
            return true;
        } else {
            return false;
        }
    }

    public static void Dump() {
        String info = "Hashtable: ";

        if (smHardwareTable == null)
            return;

        Enumeration<String> keys = smHardwareTable.keys();

        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            info += "[" + key + "=" + smHardwareTable.get(key) + "] ";
        }

        IwdsLog.i(TAG, info);
    }
}
