/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  ZhangYanMing <yanming.zhang@ingenic.com, jamincheung@126.com>
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

package com.ingenic.iwds.datatransactor;

import com.ingenic.iwds.utils.IwdsAssert;

public class FileTransferErrorCode {
    /**
     * 正常
     */
    public static final int NOERROR = 0;

    /**
     * 文件状态错误(文件创建失败/文件名为空/文件长度为空/断点处长度大于文件总长度/文件写错误/接收到的文件长度不等于请求发送的文件长度)
     */
    public static final int EFILESTATUS = 1;

    /**
     * SD卡未挂载
     */
    public static final int ENOSDCARD = 2;

    /**
     * SD卡已满
     */
    public static final int ESDCARDFULL = 3;

    /**
     * 返回错误字符串
     * 
     * @param errorCode
     *            错误编码
     * @return 错误字符串
     */
    public static String errorString(int errorCode) {
        String str = null;

        switch (errorCode) {
        case NOERROR:
            str = "no error";
            break;

        case EFILESTATUS:
            str = "file status";
            break;

        case ENOSDCARD:
            str = "no sdcard";
            break;

        case ESDCARDFULL:
            str = "sdcard full";
            break;

        default:
            IwdsAssert.dieIf("FileTransferErrorCode", true,
                    "Unknown error code: " + errorCode);
            break;
        }

        return str;
    }
}
