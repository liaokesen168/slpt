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

public class FileTransferException extends IwdsException {
    private static final long serialVersionUID = 487316417092728732L;

    public FileTransferException() {
        super();
    }

    public FileTransferException(String msg) {
        super(msg);
    }

    public FileTransferException(Throwable cause) {
        super(cause);
    }

    public FileTransferException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
