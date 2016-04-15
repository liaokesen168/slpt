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

package com.ingenic.iwds.utils.serializable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

import android.os.Parcelable;

import com.ingenic.iwds.common.exception.FileTransferException;
import com.ingenic.iwds.os.SafeParcelable;
import com.ingenic.iwds.uniconnect.Connection;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

/**
 * 传输适配器, 用于和远端设备进行数据传输(发送/接收). 其中的
 * {@link TransferAdapter.TransferAdapterCallback} 用于更新传输状态. 支持传输的数据类型如下:
 *
 * <li>String <li>Byte <li>Character <li>Short <li>Integer <li>Long <li>Float
 * <li>Double <li>Boolean <li>String[] <li>Boolean[] <li>Byte[] <li>Integer[]
 * <li>Character[] <li>Short[] <li>Long[] <li>Float[] <li>Double[] <li>Object[]
 * <li>Map <li>File <li>任何实现了 {@link com.ingenic.iwds.os.SafeParcelable} 接口的对象
 * <li>任何实现了 {@link com.ingenic.iwds.os.SafeParcelable} 接口对象的数组 <li>任何实现了
 * {@link android.os.Parcelable} 接口的对象 <li>任何实现了 {@link android.os.Parcelable}
 * 接口对象的数组 <li>CharSequence <li>CharSequence[] <li>List <li>SparseArray <li>
 * SparseBooleanArray <li>任何实现了 {@link java.io.Serializable} 接口的对象</ul>
 */
public final class TransferAdapter {
    private final static String TAG = "TransferAdapter";

    /**
     * 发送文件到远端设备, 远端设备使用
     * {@link com.ingenic.iwds.utils.serializable .TransferAdapter#recv(Connection, Parcelable.Creator, TransferAdapterCallback)}
     * 接收文件, 该方法将会阻塞直到数据发送完成.
     *
     * @param connection
     *            已经建立连接的{@code connection}
     * @param file
     *            待发送的文件
     * @param index
     *            发送断点
     * @param callback
     *            回调接口
     *            {@link com.ingenic.iwds.utils.serializable.TransferAdapter.TransferAdapterCallback}
     *            用于更新文件传输状态
     * @throws IOException
     *             {@code connection} 写异常, 参考:
     *             {@link com.ingenic.iwds.common.exception.LinkDisconnectedException
     *             LinkDisconnectedException},
     *             {@link com.ingenic.iwds.common.exception.LinkUnbondedException
     *             LinkUnbondedException},
     *             {@link com.ingenic.iwds.common.exception.PortBusyException
     *             PortBusyException},
     *             {@link com.ingenic.iwds.common.exception.PortClosedException
     *             PortClosedException},
     *             {@link com.ingenic.iwds.common.exception.PortDisconnectedException
     *             PortDisconnectedException},
     * @see TransferAdapterCallback
     * @see File
     */
    public static void send(Connection connection, File file, int index,
            TransferAdapterCallback callback) throws IOException {

        IwdsAssert.dieIf(TAG, connection == null, "connection == null");
        IwdsAssert.dieIf(TAG, file == null, "file == null");
        IwdsAssert.dieIf(TAG, callback == null, "callback == null");
        IwdsAssert.dieIf(TAG, !file.exists() || !file.isFile()
                || file.length() <= 0, "file not exist or empty");
        IwdsAssert.dieIf(TAG, !file.canRead(), "file can not be read");

        int chunkSize = UtilsConstants.SizeOf.FileChunk;
        long fileLength = file.length();
        int chunkCount = 0;
        if (fileLength % chunkSize == 0)
            chunkCount = (int) (fileLength / chunkSize);
        else
            chunkCount = (int) (fileLength / chunkSize + 1);

        IwdsAssert.dieIf(TAG, index > chunkCount || index < 0, "Invalid index");

        OutputStream os = connection.getOutputStream();

        /* Write file type */
        byte[] typeBuf = new byte[UtilsConstants.SizeOf.Type];
        typeBuf[0] = UtilsConstants.VAL_FILE;
        os.write(typeBuf);

        /* Write file name */
        String name = file.getName();
        byte[] nameBuf = name.getBytes(Charset
                .forName(UtilsConstants.CHARSET_ENCODE));
        int pathLen = nameBuf.length;
        byte[] nameLenBuf = new byte[UtilsConstants.SizeOf.Int];
        nameLenBuf[0] = (byte) ((pathLen & 0xff000000) >> 24);
        nameLenBuf[1] = (byte) ((pathLen & 0x00ff0000) >> 16);
        nameLenBuf[2] = (byte) ((pathLen & 0x0000ff00) >> 8);
        nameLenBuf[3] = (byte) ((pathLen & 0x000000ff) >> 0);
        os.write(nameLenBuf);
        os.write(nameBuf);

        /* Write file length */
        byte[] fileLenBuf = new byte[UtilsConstants.SizeOf.Long];
        fileLenBuf[0] = (byte) ((fileLength & 0xff00000000000000L) >> 56);
        fileLenBuf[1] = (byte) ((fileLength & 0x00ff000000000000L) >> 48);
        fileLenBuf[2] = (byte) ((fileLength & 0x0000ff0000000000L) >> 40);
        fileLenBuf[3] = (byte) ((fileLength & 0x000000ff00000000L) >> 32);
        fileLenBuf[4] = (byte) ((fileLength & 0x00000000ff000000L) >> 24);
        fileLenBuf[5] = (byte) ((fileLength & 0x0000000000ff0000L) >> 16);
        fileLenBuf[6] = (byte) ((fileLength & 0x000000000000ff00L) >> 8);
        fileLenBuf[7] = (byte) ((fileLength & 0x00000000000000ffL) >> 0);
        os.write(fileLenBuf);

        /* Write file chunk index */
        byte[] indexBuf = new byte[UtilsConstants.SizeOf.Int];
        indexBuf[0] = (byte) ((index & 0xff000000) >> 24);
        indexBuf[1] = (byte) ((index & 0x00ff0000) >> 16);
        indexBuf[2] = (byte) ((index & 0x0000ff00) >> 8);
        indexBuf[3] = (byte) ((index & 0x000000ff) >> 0);
        os.write(indexBuf);

        long writeSoFar = index * chunkSize;

        long length = fileLength - writeSoFar;

        RandomAccessFile raf = null;

        try {
            raf = new RandomAccessFile(file, "r");

        } catch (FileNotFoundException e) {
            /* should not come here */
            IwdsAssert.dieIf(TAG, true, "file not exist");
        }

        int skippedSize = 0;
        try {
            skippedSize = raf.skipBytes((int) writeSoFar);

            /* should not come here */
            IwdsAssert.dieIf(TAG, skippedSize != writeSoFar,
                    "skip error: writeSofar=" + writeSoFar + ", skiped="
                            + skippedSize);

        } catch (IOException e) {
            try {
                raf.close();

            } catch (IOException ioe) {
                // ignore close exception
            }

            /* should not come here */
            IwdsAssert.dieIf(TAG, true, "file skip io exception");
        }

        long bytesOfLeft = length;
        byte[] buffer = new byte[chunkSize];

        while (bytesOfLeft > 0) {
            int readSize = (int) Math.min(chunkSize, bytesOfLeft);

            int pos = 0;
            int maxSize = readSize;

            try {
                while (maxSize > 0) {
                    int readBytes = raf.read(buffer, pos, maxSize);
                    IwdsAssert.dieIf(TAG, readBytes < 0, "file read error "
                            + readBytes);

                    maxSize -= readBytes;
                    pos += readBytes;
                }

            } catch (IOException e) {
                try {
                    raf.close();

                } catch (IOException ioe) {
                    // ignore close exception
                }

                /* should not come here */
                callback.onSendFileInterrupted(index);
                IwdsAssert.dieIf(TAG, true, "file read io exception");
            }

            /*
             * Write raw bytes to connection output stream Note: throw
             * IOException if an error occurs while write connection output
             * stream
             */
            try {
                os.write(buffer, 0, readSize);

            } catch (IOException e) {
                try {
                    raf.close();

                } catch (IOException ioe) {
                    // ignore close exception
                }

                IwdsLog.e(TAG, "Unable to send file: connection io exception");
                callback.onSendFileInterrupted(index);
                throw e;
            }

            IwdsAssert.dieIf(TAG, index++ > chunkCount, "index out of bound:"
                    + ", current=" + index + ", total=" + chunkCount);

            writeSoFar += readSize;
            bytesOfLeft -= readSize;

            callback.onSendFileProgress(writeSoFar, fileLength);
        }

        try {
            raf.close();

        } catch (IOException e) {
            // ignore close exception
        }
    }

    /**
     * 发送通用对象 {@code object} 到远端设备, 远端设备使用
     * {@link com.ingenic.iwds.utils.serializable .TransferAdapter#recv(Connection, TransferAdapterCallback)}
     * 接收文件, 该方法将会阻塞直到数据发送完成.
     *
     * @param connection
     *            已经建立连接的{@code connection}
     * @param object
     *            待发送的对象
     * @param parcelableCreator
     *            实现 Parcelable 接口的类的静态接口 CREATOR
     * @param safeParcelableCreator
     *            实现 SafeParcelable 接口的类的静态接口 CREATOR
     * @throws IOException
     *             {@code connection} 写异常
     *             {@link com.ingenic.iwds.common.exception.LinkDisconnectedException
     *             LinkDisconnectedException}
     *             {@link com.ingenic.iwds.common.exception.LinkUnbondedException
     *             LinkUnbondedException}
     *             {@link com.ingenic.iwds.common.exception.PortBusyException
     *             PortBusyException}
     *             {@link com.ingenic.iwds.common.exception.PortClosedException
     *             PortClosedException}
     *             {@link com.ingenic.iwds.common.exception.PortDisconnectedException
     *             PortDisconnectedException}
     * @see TransferAdapterCallback
     * @see Object
     */
    public static <T1 extends Parcelable, T2 extends SafeParcelable> void send(
            Connection connection, Object object,
            Parcelable.Creator<T1> parcelableCreator,
            SafeParcelable.Creator<T2> safeParcelableCreator)
            throws IOException {

        IwdsAssert.dieIf(TAG, connection == null, "connection == null");
        IwdsAssert.dieIf(TAG, object == null, "object == null");

        OutputStream os = connection.getOutputStream();

        byte[] buffer = null;
        buffer = ByteArrayUtils.encode(object, parcelableCreator,
                safeParcelableCreator);

        os.write(buffer);
    }

    /**
     * 接收一个通用对象从远端设备, 远端设备使用
     * {@link com.ingenic.iwds.utils.serializable .TransferAdapter#send(Connection, Object, Parcelable.Creator)}
     * 发送通用对象 {@code Object}, 使用
     * {@link com.ingenic.iwds.utils.serializable.TransferAdapter#send(Connection, File, TransferAdapterCallback)}
     * 发送文件 {@code File}, 该方法将会阻塞直到数据接收完成.
     *
     * @param connection
     *            已经建立连接的{@code connection}
     * @param parcelableCreator
     *            实现 Parcelable 接口的类的静态接口 CREATOR
     * @param safeParcelableCreator
     *            实现 SafeParcelable 接口的类的静态接口 CREATOR
     * @param callback
     *            回调接口
     *            {@link com.ingenic.iwds.utils.serializable .TransferAdapter.TransferAdapterCallback}
     *            用于更新文件传输状态
     * @return 接收到的通用对象 {@code Object}
     * @throws IOException
     *             {@code connection} 读异常, 参考:
     *             {@link com.ingenic.iwds.common.exception.LinkDisconnectedException
     *             LinkDisconnectedException},
     *             {@link com.ingenic.iwds.common.exception.LinkUnbondedException
     *             LinkUnbondedException},
     *             {@link com.ingenic.iwds.common.exception.PortBusyException
     *             PortBusyException},
     *             {@link com.ingenic.iwds.common.exception.PortClosedException
     *             PortClosedException},
     *             {@link com.ingenic.iwds.common.exception.PortDisconnectedException
     *             PortDisconnectedException},
     * @throws FileTransferException
     *             文件传输异常
     * @see TransferAdapterCallback
     * @see Object
     */
    public static <T1 extends Parcelable, T2 extends SafeParcelable> Object recv(
            Connection connection, Parcelable.Creator<T1> parcelableCeator,
            SafeParcelable.Creator<T2> safeParcelableCreator,
            TransferAdapterCallback callback) throws IOException,
            FileTransferException {
        Object object = null;

        IwdsAssert.dieIf(TAG, connection == null, "connection == null");
        IwdsAssert.dieIf(TAG, callback == null, "callback == null");

        object = ByteArrayUtils.decode(connection, parcelableCeator,
                safeParcelableCreator, callback);

        return object;
    }

    /**
     * 传输适配器回调接口, 用于更新文件传输状态
     */
    public interface TransferAdapterCallback {
        /**
         * 文件发送进度回调
         * 
         * @param currentBytes
         *            当前已发送的字节数
         * @param totalBytes
         *            发送数据总大小
         */
        public void onSendFileProgress(long currentBytes, long totalBytes);

        /**
         * 文件接收进度回调
         * 
         * @param currentBytes
         *            当前已接收的字节数
         * @param totalBytes
         *            接收数据总大小
         */
        public void onRecvFileProgress(long currentBytes, long totalBytes);

        /**
         * 文件发送中断回调
         * 
         * @param index
         *            发送断点
         */
        public void onSendFileInterrupted(int index);

        /**
         * 文件接收中断回调
         * 
         * @param index
         *            接收断点
         */
        public void onRecvFileInterrupted(int index);
    }
}
