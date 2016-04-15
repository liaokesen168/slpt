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

package com.ingenic.iwds.common.exception;

import java.io.IOException;

import android.os.RemoteException;

import com.ingenic.iwds.datatransactor.FileTransferErrorCode;
import com.ingenic.iwds.uniconnect.UniconnectErrorCode;
import com.ingenic.iwds.utils.IwdsAssert;

/**
 * Iwds异常类.
 */
public class IwdsException extends RuntimeException {
    private static final long serialVersionUID = -6462039882504760851L;

    /**
     * 实例化一个iwds异常.
     */
    public IwdsException() {

    }

    /**
     * 实例化一个iwds异常.
     *
     * @param msg
     *            异常信息
     */
    public IwdsException(String msg) {
        super(msg);
    }

    /**
     * 实例化一个iwds异常.
     *
     * @param cause
     *            异常原因
     */
    public IwdsException(Throwable cause) {
        super(cause);
    }

    /**
     * 实例化一个iwds异常.
     *
     * @param msg
     *            异常信息
     * @param cause
     *            异常原因
     */
    public IwdsException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * 抛出uniconnect IO异常.
     *
     * @param error
     *            错误
     * @throws IOException
     *             发信号告知发生了IO异常.
     */
    public static void throwUniconnectIOException(int error) throws IOException {
        IOException e = new IOException("Failed on code: " + error + "("
                + UniconnectErrorCode.errorString(error) + ")");

        switch (error) {
        case UniconnectErrorCode.ELINKUNBONDED:
            e.initCause(new LinkUnbondedException());

            break;

        case UniconnectErrorCode.ELINKDISCONNECTED:
            e.initCause(new LinkDisconnectedException());

            break;

        case UniconnectErrorCode.EPORTBUSY:
            e.initCause(new PortBusyException());

            break;

        case UniconnectErrorCode.EREMOTEEXCEPTION:
            e.initCause(new RemoteException());

            break;

        case UniconnectErrorCode.EPORTCLOSED:
            e.initCause(new PortClosedException());

            break;

        case UniconnectErrorCode.EPORTDISCONNECTED:
            e.initCause(new PortDisconnectedException());

            break;

        default:
            IwdsAssert.dieIf("IwdsException.throwIOException", true,
                    "Implement me.");
            break;
        }

        throw e;
    }

    /**
     * 抛出文件传输异常.
     *
     * @param error
     *            错误编码
     * @throws throwFileTransferException
     *             发信号告知发生了文件传输异常.
     */
    public static void throwFileTransferException(int error)
            throws FileTransferException {
        FileTransferException e = new FileTransferException("Failed on code: "
                + error + "(" + FileTransferErrorCode.errorString(error) + ")");

        switch (error) {
        case FileTransferErrorCode.EFILESTATUS:
            e.initCause(new FileStatusException());

            break;

        case FileTransferErrorCode.ENOSDCARD:
            e.initCause(new SDCardNotMountedException());

            break;

        case FileTransferErrorCode.ESDCARDFULL:
            e.initCause(new SDCardFullException());

            break;

        default:
            IwdsAssert.dieIf("IwdsException.throwFileTransferException", true,
                    "Implement me.");
            break;
        }

        throw e;
    }
}
