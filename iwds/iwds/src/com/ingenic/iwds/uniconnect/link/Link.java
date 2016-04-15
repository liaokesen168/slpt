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

package com.ingenic.iwds.uniconnect.link;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.uniconnect.Connection;
import com.ingenic.iwds.uniconnect.ConnectionService;
import com.ingenic.iwds.uniconnect.ConnectionServiceManager;
import com.ingenic.iwds.uniconnect.IConnectionCallBack;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;
import com.ingenic.iwds.utils.serializable.ParcelableUtils;

/**
 * 链接类.
 */
public class Link {
    private LinkManager m_linkManager;

    private static String m_linkTag;

    private boolean m_isRoleAsClientSide = false;
    private String m_bondedAddress;
    private boolean m_isStarted = false;

    private DeviceDescriptor m_localDeviceDescriptor;
    private DeviceDescriptor m_remoteDeviceDescriptor;

    private Connection m_ctrlConnection;
    private String m_uuid = "{THIS-IS-GOD-MASTER}";

    private HandlerThread m_serverThread;
    private HandlerThread m_clientThread;
    private ServerHandler m_serverHandler;
    private ClientHandler m_clientHandler;

    /**
     * 链接状态类.
     */
    public static class LinkState {

        /** 断开状态. */
        public static final int STATE_DISCONNECTED = 0;

        /** 连接状态. */
        public static final int STATE_CONNECTED = 1;

        /**
         * 将链接状态转化为字符串.
         *
         * @param state
         *            状态
         * @return 字符串
         */
        static String toString(int state) {
            switch (state) {
            case STATE_DISCONNECTED:
                return "disconnected";

            case STATE_CONNECTED:
                return "connected";

            default:
                IwdsAssert.dieIf("LinkState", true, "Implement me.");

                return null;
            }
        }
    }

    private int m_state = LinkState.STATE_DISCONNECTED;

    /**
     * 获取链接状态.
     *
     * @return 链接状态
     */
    public int getState() {
        synchronized (this) {
            return m_state;
        }
    }

    private IConnectionCallBack m_ctrlConnectionCallBack = new IConnectionCallBack.Stub() {

        @Override
        public void stateChanged(int state, int arg0) throws RemoteException {
            // TODO
        }
    };

    /**
     * 初始化一个链接.
     *
     * @param manager
     *            链接管理器
     * @param deviceDescriptor
     *            设备描述符
     * @param linkTag
     *            链接标记
     */
    /* package */Link(LinkManager manager, DeviceDescriptor deviceDescriptor, String linkTag) {
        m_linkManager = manager;
        m_localDeviceDescriptor = deviceDescriptor;
        m_linkTag = linkTag;

        m_ctrlConnection = new Connection(m_linkManager.getContext(),
                new ConnectionService.ConnectionStub(m_ctrlConnectionCallBack));
    }

    /**
     * 获取应用的上下文.
     *
     * @return 应用的上下文
     */
    public Context getContext() {
        return m_linkManager.getContext();
    }

    /**
     * 获取已绑定的服务端设备地址.
     *
     * @return 已绑定的服务端设备地址
     */
    public String getBondedAddress() {
        if (isRoleAsClientSide())
            return m_bondedAddress;
        else
            return m_linkManager.getRemoteAddress(m_linkTag);
    }

    /**
     * 通过服务端设备地址绑定服务端.
     *
     * @param address
     *            服务端设备地址
     * @return 如果成功，返回{@code true}
     */
    public boolean bondAddress(String address) {
        IwdsAssert.dieIf(this, address == null || address.isEmpty(),
                "Address is null or empty.");

        IwdsAssert
                .dieIf(this, m_isStarted,
                        "Client or server already started(Unboned or stop server first)");

        if (!m_linkManager.bondAddress(m_linkTag, address))
            return false;

        m_isRoleAsClientSide = true;
        m_bondedAddress = address;
        m_isStarted = true;

        /*
         * start client handler
         */
        m_clientThread = new HandlerThread("client handler");
        m_clientThread.start();
        m_clientHandler = new ClientHandler(m_clientThread.getLooper());

        return true;
    }

    /**
     * 检查是否已绑定服务端.
     *
     * @return 如果已经绑定，返回{@code true}
     */
    public boolean isBonded() {
        if (!m_isRoleAsClientSide)
            return false;

        return m_isStarted;
    }

    /**
     * 解绑服务端.
     */
    public void unbond() {
        IwdsAssert.dieIf(this, !isRoleAsClientSide(),
                "Link must role as client side.");

        m_linkManager.unbond(m_bondedAddress);

        /*
         * stop client handler, link disconnected has been processed
         */
        Message.obtain(m_clientHandler, ClientHandler.MSG_QUIT).sendToTarget();

        m_isStarted = false;
    }

    /**
     * 判断服务端是否已经启动服务.
     *
     * @return 如果已经启动，返回{@code true}
     */
    public boolean isServerStarted() {
        if (m_isRoleAsClientSide)
            return false;

        return m_isStarted;
    }

    /**
     * 启动服务端的服务.
     *
     * @return 如果成功，返回{@code true}
     */
    public boolean startServer() {
        IwdsAssert
                .dieIf(this, m_isStarted,
                        "Client or server already started(Unboned or stop server first)");

        if (!m_linkManager.startServer(m_linkTag))
            return false;

        m_isRoleAsClientSide = false;
        m_isStarted = true;

        /*
         * start server handler
         */
        m_serverThread = new HandlerThread("Server handler");
        m_serverThread.start();
        m_serverHandler = new ServerHandler(m_serverThread.getLooper());

        return true;
    }

    /**
     * 停止服务端的服务.
     */
    public void stopServer() {
        IwdsAssert.dieIf(this, isRoleAsClientSide(),
                "Caller must role as server side.");

        m_linkManager.stopServer(m_linkTag);

        /*
         * stop client handler, link disconnected has been processed
         */
        Message.obtain(m_serverHandler, ServerHandler.MSG_QUIT).sendToTarget();

        m_isStarted = false;
    }

    /**
     * 检查是否是客户端.
     *
     * @return 如果是客户端，返回{@code true}
     */
    public boolean isRoleAsClientSide() {
        return m_isRoleAsClientSide;
    }

    /**
     * 获取链接标签.
     *
     * @return 链接标签
     */
    public String getTag() {
        return m_linkTag;
    }

    private void setState(int state) {
        synchronized (this) {
            if (state == LinkState.STATE_CONNECTED)
                m_state = LinkState.STATE_CONNECTED;
            else
                m_state = LinkState.STATE_DISCONNECTED;
        }
    }

    private class ServerHandler extends Handler {
        public static final int MSG_SEND_LOCAL_DESC = 0;
        public static final int MSG_READ_REMOTE_DESC = 1;
        public static final int MSG_CONNECTED = 2;
        public static final int MSG_DISCONNECTED = 3;
        public static final int MSG_QUIT = 4;

        public static final int MSG_START_HANDSHAKE = 5;

        private DataInputStream is;
        private DataOutputStream os;

        private byte[] localDesc = ParcelableUtils
                .marshall(m_localDeviceDescriptor);

        ServerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_QUIT:
                m_serverThread.quit();

                break;

            case MSG_DISCONNECTED:
                if (m_remoteDeviceDescriptor != null) {
                    IwdsLog.i(this, "Disconnect from remote device: "
                            + m_remoteDeviceDescriptor.toString());

                    setState(LinkState.STATE_DISCONNECTED);

                    RemoteDeviceDescriptorStorage.getInstance()
                            .removeDeviceDescriptor(m_remoteDeviceDescriptor);

                    Intent it = new Intent(
                            ConnectionServiceManager.ACTION_DISCONNECTED_ADDRESS);
                    it.putExtra("DeviceDescriptor", m_remoteDeviceDescriptor);

                    m_linkManager.getContext().sendBroadcast(it);

                    m_remoteDeviceDescriptor = null;
                }

                break;

            case MSG_START_HANDSHAKE:
                int error = m_ctrlConnection.open((String) msg.obj, m_uuid);
                if (error < 0)
                    break;

                error = m_ctrlConnection.handshake();
                if (error < 0) {
                    m_ctrlConnection.close();

                    break;
                }

                is = new DataInputStream(m_ctrlConnection.getInputStream());
                os = new DataOutputStream(m_ctrlConnection.getOutputStream());

                Message.obtain(this, MSG_SEND_LOCAL_DESC).sendToTarget();

                break;

            case MSG_SEND_LOCAL_DESC:
                try {
                    os.writeInt(localDesc.length);
                    os.write(localDesc);

                    Message.obtain(this, MSG_READ_REMOTE_DESC).sendToTarget();

                    IwdsLog.i(this, "Send local device descriptor: "
                            + m_localDeviceDescriptor.toString());

                } catch (IOException e) {
                    os = null;
                    is = null;

                    m_ctrlConnection.close();
                }

                break;

            case MSG_READ_REMOTE_DESC:
                try {
                    int size = is.readInt();
                    byte[] remoteDesc = new byte[size];

                    is.read(remoteDesc);
                    m_remoteDeviceDescriptor = ParcelableUtils.unmarshall(
                            remoteDesc, DeviceDescriptor.CREATOR);

                    Message.obtain(this, MSG_CONNECTED).sendToTarget();

                } catch (IOException e) {
                    os = null;
                    is = null;

                    m_ctrlConnection.close();
                }

                break;

            case MSG_CONNECTED:
                IwdsLog.i(this, "Connect to remote device: "
                        + m_remoteDeviceDescriptor.toString());

                setState(LinkState.STATE_CONNECTED);

                RemoteDeviceDescriptorStorage.getInstance()
                        .addDeviceDescriptors(m_remoteDeviceDescriptor);

                Intent it = new Intent(
                        ConnectionServiceManager.ACTION_CONNECTED_ADDRESS);
                it.putExtra("DeviceDescriptor", m_remoteDeviceDescriptor);
                m_linkManager.getContext().sendBroadcast(it);

                os = null;
                is = null;

                m_ctrlConnection.close();

                break;
            }
        }
    }

    private class ClientHandler extends Handler {
        public static final int MSG_READ_REMOTE_DESC = 0;
        public static final int MSG_SEND_LOCAL_DESC = 1;
        public static final int MSG_CONNECTED = 2;
        public static final int MSG_DISCONNECTED = 3;
        public static final int MSG_QUIT = 4;

        public static final int MSG_START_HANDSHAKE = 5;

        private DataInputStream is;
        private DataOutputStream os;

        private byte[] localDesc = ParcelableUtils
                .marshall(m_localDeviceDescriptor);

        ClientHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_QUIT:
                m_clientThread.quit();

                break;

            case MSG_DISCONNECTED:
                if (m_remoteDeviceDescriptor != null) {
                    IwdsLog.i(this, "Disconnect from remote device: "
                            + m_remoteDeviceDescriptor.toString());

                    setState(LinkState.STATE_DISCONNECTED);

                    RemoteDeviceDescriptorStorage.getInstance()
                            .removeDeviceDescriptor(m_remoteDeviceDescriptor);

                    Intent it = new Intent(
                            ConnectionServiceManager.ACTION_DISCONNECTED_ADDRESS);
                    it.putExtra("DeviceDescriptor", m_remoteDeviceDescriptor);

                    m_linkManager.getContext().sendBroadcast(it);

                    m_remoteDeviceDescriptor = null;
                }

                break;

            case MSG_START_HANDSHAKE:
                int error = m_ctrlConnection.open((String) msg.obj, m_uuid);
                if (error < 0)
                    break;

                error = m_ctrlConnection.handshake();
                if (error < 0) {
                    m_ctrlConnection.close();

                    break;
                }

                is = new DataInputStream(m_ctrlConnection.getInputStream());
                os = new DataOutputStream(m_ctrlConnection.getOutputStream());

                Message.obtain(this, MSG_READ_REMOTE_DESC).sendToTarget();

                break;

            case MSG_READ_REMOTE_DESC:
                try {
                    int size = is.readInt();
                    byte[] remoteDesc = new byte[size];

                    is.read(remoteDesc);
                    m_remoteDeviceDescriptor = ParcelableUtils.unmarshall(
                            remoteDesc, DeviceDescriptor.CREATOR);

                    Message.obtain(this, MSG_SEND_LOCAL_DESC).sendToTarget();

                } catch (IOException e) {
                    os = null;
                    is = null;

                    m_ctrlConnection.close();
                }

                break;

            case MSG_SEND_LOCAL_DESC:
                try {
                    os.writeInt(localDesc.length);
                    os.write(localDesc);

                    Message.obtain(this, MSG_CONNECTED).sendToTarget();

                    IwdsLog.i(this, "Send local device descriptor: "
                            + m_localDeviceDescriptor.toString());

                } catch (IOException e) {
                    os = null;
                    is = null;

                    m_ctrlConnection.close();
                }

                break;

            case MSG_CONNECTED:
                IwdsLog.i(this, "Connect to remote device: "
                        + m_remoteDeviceDescriptor.toString());

                setState(LinkState.STATE_CONNECTED);

                RemoteDeviceDescriptorStorage.getInstance()
                        .addDeviceDescriptors(m_remoteDeviceDescriptor);

                Intent it = new Intent(
                        ConnectionServiceManager.ACTION_CONNECTED_ADDRESS);
                it.putExtra("DeviceDescriptor", m_remoteDeviceDescriptor);
                m_linkManager.getContext().sendBroadcast(it);

                os = null;
                is = null;

                m_ctrlConnection.close();

                break;
            }
        }
    }

    private void startHandshake(String address) {
        Message msg = null;

        if (isRoleAsClientSide()) {
            msg = Message.obtain(m_clientHandler,
                    ClientHandler.MSG_START_HANDSHAKE);
        } else {
            msg = Message.obtain(m_serverHandler,
                    ServerHandler.MSG_START_HANDSHAKE);
        }

        msg.obj = address;

        msg.sendToTarget();
    }

    private void processDisconnect() {
        if (isRoleAsClientSide()) {
            Message.obtain(m_clientHandler, ClientHandler.MSG_DISCONNECTED)
                    .sendToTarget();
        } else {
            Message.obtain(m_serverHandler, ServerHandler.MSG_DISCONNECTED)
                    .sendToTarget();
        }
    }

    /**
     * 链接状态变化时的回调.
     *
     * @param state
     *            链接状态
     * @param address
     *            地址
     */
    /* package */void onLinkStateChanged(int state, String address) {
        IwdsLog.i(this,
                "onLinkStateChanged: State: " + LinkState.toString(state)
                        + ", Role: "
                        + (isRoleAsClientSide() ? "client" : "server")
                        + ", Tag: " + m_linkTag + ", Address: " + address);

        if (state == LinkState.STATE_CONNECTED) {
            startHandshake(address);

        } else if (state == LinkState.STATE_DISCONNECTED) {
            processDisconnect();
        }
    }
}
