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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.os.Process;
import android.os.RemoteException;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.common.exception.IwdsException;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

public class Connection {
    private Context m_context;

    private int m_maxPayloadSize = -1;

    private IConnection m_connection;

    private String m_address;
    private String m_uuid;
    private long m_port = -1;

    private InputStream m_inputStream;
    private OutputStream m_outputStream;

    private boolean m_isClosed = true;

    private static final class ConnectionInputStream extends InputStream {
        private Connection m_connection;

        public ConnectionInputStream(Connection connection) {
            m_connection = connection;
        }

        public int available() throws IOException {
            int bytes = m_connection.available();
            if (bytes < 0)
                IwdsException.throwUniconnectIOException(bytes);

            return bytes;
        }

        public void close() throws IOException {
            /*
             * do nothing
             */
        }

        public int read() throws IOException {
            byte b[] = new byte[1];

            int bytes = 0;
            while (bytes != 1) {
                bytes = m_connection.read(b, 0, 1);
                if (bytes < 0)
                    IwdsException.throwUniconnectIOException(bytes);
            }

            return (int) b[0] & 0xff;
        }

        public int read(byte[] buffer, int offset, int maxSize)
                throws IOException {
            int bytes = m_connection.read(buffer, offset, maxSize);
            if (bytes < 0)
                IwdsException.throwUniconnectIOException(bytes);

            return bytes;
        }
    }

    private static final class ConnectionOutputStream extends OutputStream {
        private Connection m_connection;

        public ConnectionOutputStream(Connection connection) {
            m_connection = connection;
        }

        public void close() throws IOException {
            /*
             * do nothing
             */
        }

        public void write(int oneByte) throws IOException {
            byte b[] = new byte[1];
            b[0] = (byte) oneByte;

            int bytes = 0;
            while (bytes != 1) {
                bytes = m_connection.write(b, 0, 1);
                if (bytes < 0)
                    IwdsException.throwUniconnectIOException(bytes);
            }
        }

        public void write(byte[] buffer, int offset, int maxSize)
                throws IOException {
            int pos = offset;
            while (maxSize > 0) {
                int writeBytes = m_connection.write(buffer, pos, maxSize);
                if (writeBytes < 0)
                    IwdsException.throwUniconnectIOException(writeBytes);

                pos += writeBytes;
                maxSize -= writeBytes;
            }
        }

        public void flush() throws IOException {
            /*
             * do nothing
             */
        }
    }

    /**
     * 构造 {@code connection}.
     *
     * @param context
     *            应用的上下文.
     * @param connection
     *            {@code connection}的客户端代理. 由{@code ConnectionServiceManager}的
     *            {@code createConnection()}方法创建.
     */
    public Connection(Context context, IConnection connection) {
        IwdsAssert.dieIf(this, context == null, "Context is null.");
        IwdsAssert.dieIf(this, connection == null, "Connection is null.");

        synchronized (this) {
            m_context = context;
            m_connection = connection;

            m_inputStream = new ConnectionInputStream(this);
            m_outputStream = new ConnectionOutputStream(this);
        }
    }

    /**
     * 获取{@code connection}的输入流.
     *
     * @return {@code connection}的输入流.
     */
    public InputStream getInputStream() {
        return m_inputStream;
    }

    /**
     * 获取{@code connection}的输出流.
     *
     * @return {@code connection}的输出流.
     */
    public OutputStream getOutputStream() {
        return m_outputStream;
    }

    /**
     * 获取{@code connection}所连接的远端设备的蓝牙物理地址.
     *
     * @return {@code connection}所连接的远端设备的蓝牙物理地址.
     */
    public String getAddress() {
        synchronized (this) {
            return m_address;
        }
    }

    /**
     * 获取{@code connection}的UUID.
     *
     * @return {@code connection}的UUID.
     */
    public String getUUID() {
        synchronized (this) {
            return m_uuid;
        }
    }

    /**
     * 获取{@code connection}的端口号.
     *
     * @return {@code connection}的端口号.
     */
    public long getPort() {
        synchronized (this) {
            return m_port;
        }
    }

    /**
     * 以远端设备描述符和连接的UUID打开{@code connection}.
     *
     * @param deviceDescriptor
     *            {@code connection}所连接的远端设备的描述符.
     * @param uuid
     *            {@code connection}的UUID.
     * @return 如果成功, 返回0, 否则返回错误号. 参考
     *         {@link com.ingenic.iwds.uniconnect.UniconnectErrorCode
     *         UniconnectErrorCode}.
     */
    public int open(DeviceDescriptor deviceDescriptor, String uuid) {
        IwdsAssert.dieIf(this, deviceDescriptor == null,
                "Device descriptor is null.");

        return open(deviceDescriptor.devAddress, uuid);
    }

    /**
     * 如果{@code connection}已经关闭，返回{@code true}.
     *
     * @return 如果{@code connection}已经关闭，返回{@code true}.
     */
    public boolean isClosed() {
        synchronized (this) {
            return m_isClosed;
        }
    }

    /**
     * 以远端设备的蓝牙物理地址和连接的UUID打开{@code connection}.
     *
     * @param address
     *            {@code connection}所连接的远端设备的物理地址.
     * @param uuid
     *            {@code connection}的UUID.
     * @return 错误号. 参考{@link com.ingenic.iwds.uniconnect.UniconnectErrorCode
     *         UniconnectErrorCode}.
     * 
     */
    public int open(String address, String uuid) {
        IwdsAssert.dieIf(this, address == null || address.isEmpty(),
                "Address is null or empty.");

        IwdsAssert.dieIf(this, uuid == null || uuid.isEmpty(),
                "Uuid is null or empty.");

        IwdsAssert
                .dieIf(this,
                        !uuid.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}")
                                && !uuid.equals("{THIS-IS-GOD-MASTER}"),
                        "Invalid UUID: " + uuid
                                + "(need a 8-4-4-4-12 style UUID).");

        synchronized (this) {
            if (!m_isClosed)
                return 0;

            m_address = address;
            m_uuid = uuid;

            try {
                long error = m_connection.open(m_context.getPackageName(),
                        Process.myPid(), address, m_uuid);
                if (error < 0)
                    return (int) error;

                m_maxPayloadSize = m_connection.getMaxPayloadSize();
                m_port = error;

                m_isClosed = false;

                return 0;

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in open: " + e);
            }

            return UniconnectErrorCode.EREMOTEEXCEPTION;
        }
    }

    /**
     * 关闭{@code connection}. 此时在native IWDS服务中销毁 {@code connection}.
     */
    public void close() {
        synchronized (this) {
            if (m_isClosed)
                return;

            try {
                m_connection.close();
                m_port = -1;

                m_isClosed = true;

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in close: " + e);
            }
        }
    }

    /**
     * 把{@code buffer}的{@code offset}字节偏离起始的最多{@code maxSize}字节 写到
     * {@code connection}.
     *
     * @param buffer
     *            被写的缓冲.
     * @param offset
     *            被写数据在{@code buffer}中的起始位置.
     * @param maxSize
     *            最多被写的字节数.
     * @return 如果写成功，返回成功写入的字节数; 否则返回错误号. 参考
     *         {@link com.ingenic.iwds.uniconnect.UniconnectErrorCode
     *         UniconnectErrorCode}.
     */
    public int write(byte[] buffer, int offset, int maxSize) {
        IwdsAssert.dieIf(this, buffer == null || (offset | maxSize) < 0
                || buffer.length < offset + maxSize,
                "Buffer is null or buffer exceed.");

        int pos = offset;
        int writtenSoFar = 0;
        try {
            while (maxSize > 0) {
                int bytesToWrite = Math.min(maxSize, m_maxPayloadSize);
                int writeBytes = m_connection.write(buffer, pos, bytesToWrite);
                if (writeBytes < 0)
                    return writtenSoFar > 0 ? writtenSoFar : writeBytes;

                writtenSoFar += writeBytes;
                pos += writeBytes;
                maxSize -= writeBytes;
            }

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in write: " + e);

            return writtenSoFar > 0 ? writtenSoFar
                    : UniconnectErrorCode.EREMOTEEXCEPTION;
        }

        return writtenSoFar;
    }

    /**
     * 从{@code connection}最多读取{@code maxSize}字节存放在{@code buffer} 的{@code offset}
     * 字节偏移量处.
     *
     * @param buffer
     *            存放读到数据的缓冲.
     * @param offset
     *            {@code buffer}的存放读到数据的起始位置.
     * @param maxSize
     *            最多存放到{@code buffer}的字节数.
     * @return 如果成功，返回成功存放到{@code buffer}的字节数, 否则返回错误号.参考
     *         {@link com.ingenic.iwds.uniconnect.UniconnectErrorCode
     *         UniconnectErrorCode}.
     */
    public int read(byte[] buffer, int offset, int maxSize) {
        IwdsAssert.dieIf(this, buffer == null || (offset | maxSize) < 0
                || buffer.length < offset + maxSize,
                "Buffer is null or buffer exceed.");

        int pos = offset;
        int readSoFar = 0;
        try {
            while (maxSize > 0) {
                int bytesToRead = Math.min(maxSize, m_maxPayloadSize);
                int readBytes = m_connection.read(buffer, pos, bytesToRead);
                if (readBytes < 0)
                    return readSoFar > 0 ? readSoFar : readBytes;

                readSoFar += readBytes;
                pos += readBytes;
                maxSize -= readBytes;
            }

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in read: " + e);

            return readSoFar > 0 ? readSoFar
                    : UniconnectErrorCode.EREMOTEEXCEPTION;
        }

        return readSoFar;
    }

    /**
     * 返回{@code connection}在读操作阻塞之前可读取的字节数.
     *
     * @return 如果成功, 返回读操作阻塞之前可读取的字节数, 否则返回错误号. 参考
     *         {@link com.ingenic.iwds.uniconnect.UniconnectErrorCode
     *         UniconnectErrorCode}.
     */
    public int available() {
        try {
            return m_connection.available();
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in available: " + e);
        }

        return UniconnectErrorCode.EREMOTEEXCEPTION;
    }

    /**
     * 等待和远端设备的{@code connection}握手成功. 如果远端设备的 {@code connection}
     * 没有创建，或没有发起握手请求，都将阻塞等待.
     *
     * @return 如果成功, 返回0, 否则返回错误号. 参考
     *         {@link com.ingenic.iwds.uniconnect.UniconnectErrorCode
     *         UniconnectErrorCode}.
     */
    public int handshake() {
        try {
            return m_connection.handshake();
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in handshake: " + e);
        }

        return UniconnectErrorCode.EREMOTEEXCEPTION;
    }

    /**
     * 返回数据通道宽度（内部数据分片大小，IPC宽度）
     *
     * @return 返回以字节为单位的通道宽度
     */
    public int getDataChunkSize() {
        return m_maxPayloadSize;
    }
}
