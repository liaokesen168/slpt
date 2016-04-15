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

package com.ingenic.iwds.common.exception;

/**
 * 序列化异常类
 */
public class SerializeException extends IwdsException {
    private static final long serialVersionUID = -4843929500638891605L;

    /**
     * 构造方法
     */
    public SerializeException() {
        super();
    }

    /**
     * 构造方法
     * 
     * @param msg
     *            异常信息
     */
    public SerializeException(String msg) {
        super(msg);
    }

    /**
     * 构造方法
     * 
     * @param cause
     *            异常原因
     */
    public SerializeException(Throwable cause) {
        super(cause);
    }

    /**
     * 构造方法
     * 
     * @param msg
     *            异常信息
     * @param cause
     *            异常原因
     */
    public SerializeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
