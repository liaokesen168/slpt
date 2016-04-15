/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  SunWenZhong(Fighter) <wzsun@ingenic.com, wanmyqawdr@126.com>
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

package com.ingenic.iwds.common.api;

import com.ingenic.iwds.utils.IwdsAssert;

public class ConnectFailedReason {
    /**
     * 成功
     */
    public static final int R_SUCCESS = 0;
    /**
     * 服务无效
     */
    public static final int R_SERVICE_UNAVAILABLE = 1;
    /**
     * 服务鉴权失败
     */
    public static final int R_SERVICE_AUTH_FALIED = 2;

    private int mReasonCode;

    ConnectFailedReason(int reasonCode) {
        mReasonCode = reasonCode;
    }

    /**
     * 获取原因号码
     * @return 原因号码
     */
    public int getReasonCode() {
        return mReasonCode;
    }

    /**
     * 转换原因为字符串
     * @return 原因的文字描述
     */
    public String toString() {
        switch (mReasonCode) {
        case R_SUCCESS:
            return "success";

        case R_SERVICE_UNAVAILABLE:
            return "service is unavailable";

        case R_SERVICE_AUTH_FALIED:
            return "service authentication failure";

        default:
            IwdsAssert.dieIf(this, true, "Implement me.");
            return "Assert";
        }
    }
}
