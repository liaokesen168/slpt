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

package com.ingenic.iwds.smartlocation;

import com.ingenic.iwds.utils.IwdsAssert;

/**
 * 异常错误信息类
 */
public class RemoteLocationErrorCode {
    private static final String ERROR_NORMAL = "正常";
    private static final String ERROR_IO = "IO 操作异常 - IOException";
    private static final String ERROR_SOCKET = "socket 连接异常 - SocketException";
    private static final String ERROR_SOCKE_TIME_OUT = "socket 连接超时 - SocketTimeoutException";
    private static final String ERROR_INVALID_PARAMETER = "无效的参数 - IllegalArgumentException";
    private static final String ERROR_NULL_PARAMETER = "空指针异常 - NullPointException";
    private static final String ERROR_URL = "url异常 - MalformedURLException";
    private static final String ERROR_UNKNOW_HOST = "未知主机 - UnKnowHostException";
    private static final String ERROR_UNKNOW_SERVICE = "服务器连接失败 - UnknownServiceException";
    private static final String ERROR_PROTOCOL = "协议解析错误 - ProtocolException";
    private static final String ERROR_CONNECTION = "http连接失败 - ConnectionException";
    private static final String ERROR_UNKNOWN = "未知的错误";
    private static final String ERROR_FAILURE_AUTH = "key鉴权失败";
    private static final String ERROR_FAILURE_INFO = "获取基站/WiFi信息为空或失败";
    private static final String ERROR_FAILURE_LOCATION = "定位失败无法获取城市信息";
    private static final String ERROR_OVER_QUOTA = "定位IP超过次数限制";

    /**
     * 正常
     */
    public static final int ERROR_CODE_NORMAL = 0;

    /**
     * 输入输出异常
     */
    public static final int ERROR_CODE_IO = 21;

    /**
     * socket 连接异常
     */
    public static final int ERROR_CODE_SOCKET = 22;

    /**
     * socket 连接超时
     */
    public static final int ERROR_CODE_SOCKE_TIME_OUT = 23;

    /**
     * 无效的参数
     */
    public static final int ERROR_CODE_INVALID_PARAMETER = 24;

    /**
     * 空指针异常
     */
    public static final int ERROR_CODE_NULL_PARAMETER = 25;

    /**
     * url异常
     */
    public static final int ERROR_CODE_URL = 26;

    /**
     * 未知主机
     */
    public static final int ERROR_CODE_UNKNOW_HOST = 27;

    /**
     * 服务器连接失败
     */
    public static final int ERROR_CODE_UNKNOW_SERVICE = 28;

    /**
     * 协议解析错误
     */
    public static final int ERROR_CODE_PROTOCOL = 29;

    /**
     * http连接失败
     */
    public static final int ERROR_CODE_CONNECTION = 30;

    /**
     * 未知的错误
     */
    public static final int ERROR_CODE_UNKNOWN = 31;

    /**
     * key鉴权失败
     */
    public static final int ERROR_CODE_FAILURE_AUTH = 32;

    /**
     * 获取基站/WiFi信息为空或失败
     */
    public static final int ERROR_CODE_FAILURE_INFO = 33;

    /**
     * 定位失败无法获取城市信息
     */
    public static final int ERROR_CODE_FAILURE_LOCATION = 34;

    /**
     * 定位IP超过次数限制
     */
    public static final int ERROR_CODE_OVER_QUOTA = 35;

    /**
     * 错误编码转换为字符串
     * 
     * @param errorCode
     *            错误编码
     * @return 错误编码对应的字符串
     */
    public static String errorCodeToString(int errorCode) {
        switch (errorCode) {
        case ERROR_CODE_NORMAL:
            return ERROR_NORMAL;

        case ERROR_CODE_IO:
            return ERROR_IO;

        case ERROR_CODE_SOCKET:
            return ERROR_SOCKET;

        case ERROR_CODE_SOCKE_TIME_OUT:
            return ERROR_SOCKE_TIME_OUT;

        case ERROR_CODE_INVALID_PARAMETER:
            return ERROR_INVALID_PARAMETER;

        case ERROR_CODE_NULL_PARAMETER:
            return ERROR_NULL_PARAMETER;

        case ERROR_CODE_URL:
            return ERROR_URL;

        case ERROR_CODE_UNKNOW_HOST:
            return ERROR_UNKNOW_HOST;

        case ERROR_CODE_UNKNOW_SERVICE:
            return ERROR_UNKNOW_SERVICE;

        case ERROR_CODE_PROTOCOL:
            return ERROR_PROTOCOL;

        case ERROR_CODE_CONNECTION:
            return ERROR_CONNECTION;

        case ERROR_CODE_UNKNOWN:
            return ERROR_UNKNOWN;

        case ERROR_CODE_FAILURE_AUTH:
            return ERROR_FAILURE_AUTH;

        case ERROR_CODE_FAILURE_INFO:
            return ERROR_FAILURE_INFO;

        case ERROR_CODE_FAILURE_LOCATION:
            return ERROR_FAILURE_LOCATION;

        case ERROR_CODE_OVER_QUOTA:
            return ERROR_OVER_QUOTA;

        default:
            IwdsAssert.dieIf(RemoteLocationErrorCode.class.getSimpleName(),
                    true, "Unknown error code: " + errorCode);
        }
        return null;
    }
}
