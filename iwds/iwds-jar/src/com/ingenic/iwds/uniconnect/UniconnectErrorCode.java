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

package com.ingenic.iwds.uniconnect;

import com.ingenic.iwds.utils.IwdsAssert;

public class UniconnectErrorCode {
    /**
     * 指示没有错误.
     */
    public static final int NOERROR = 0;

    /**
     * 错误: {@code link}已经解绑
     */
    public static final int ELINKUNBONDED = -1;

    /**
     * 错误: {@code link}已经断开
     */
    public static final int ELINKDISCONNECTED = -2;

    /**
     * 错误: {@code port}已被占用
     */
    public static final int EPORTBUSY = -3;

    /**
     * 错误: 互联服务异常
     */
    public static final int EREMOTEEXCEPTION = -4;

    /**
     * 错误: {@code port}已关闭
     */
    public static final int EPORTCLOSED = -5;

    /**
     * 错误: 远端设备的{@code port}已关闭
     */
    public static final int EPORTDISCONNECTED = -6;

    /**
     * 错误: 已经禁用
     */
    public static final int EDISABLED = -7;
    
    /**
     * 错误: 设备探测正在进行中
     */
    public static final int EDISCOVERYISONGING = -8;

    public static String errorString(int error) {
        String str = null;

        switch (error) {
        case UniconnectErrorCode.ELINKUNBONDED:
            str = "link was unbonded";

            break;

        case UniconnectErrorCode.ELINKDISCONNECTED:
            str = "link was disconnected";

            break;

        case UniconnectErrorCode.EPORTBUSY:
            str = "port is busy(grabbed)";

            break;

        case UniconnectErrorCode.EREMOTEEXCEPTION:
            str = "remote service was died";

            break;

        case UniconnectErrorCode.EPORTCLOSED:
            str = "port was closed";

            break;

        case UniconnectErrorCode.EPORTDISCONNECTED:
            str = "port was disconnected";

            break;

        default:
            IwdsAssert.dieIf("UniconnectErrorCode", true,
                    "Implement me.");
            break;
        }

        return str;
    }
}
