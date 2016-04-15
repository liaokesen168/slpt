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

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.utils.IwdsAssert;

/**
 * 适配器类.
 */
public abstract class Adapter {

    /** BLE数据通道标记. */
    public static String TAG_BLE_DATA_CHANNEL = "BLE data channel";

    /** android蓝牙数据通道标记. */
    public static String TAG_ANDROID_BT_DATA_CHANNEL = "Android BT data channel";

    private Context m_context;
    private AdapterManager m_adapterManager;
    private LinkManager m_linkManager;
    private String m_linkTag;

    private ArrayList<RemoteDevice> m_devices;

    private ArrayList<Link> m_links;
    private ReentrantLock m_linksLock = new ReentrantLock();

    /**
     * 设备发现回调接口.
     */
    public static abstract interface DeviceDiscoveryCallbacks {

        /**
         * 当设备开始时的回调.
         */
        void onDiscoveryStarted();

        /**
         * 当探测到设备时的回调.
         *
         * @param device
         *            the device
         */
        void onDeviceFound(RemoteDevice device);

        /**
         * 当设备探测完毕时的回调.
         */
        void onDiscoveryFinished();
    }

    /**
     * 获取本地地址.
     *
     * @return 本地地址
     */
    public abstract String getLocalAddress();

    /**
     * 使能适配器.
     *
     * @return 如果成功，返回{@code true}
     */
    public abstract boolean enable();

    /**
     * 禁用适配器.
     */
    public abstract void disable();

    /**
     * 判断是否已使能适配器.
     *
     * @return 如果已使能，返回{@code true}
     */
    public abstract boolean isEnabled();

    /**
     * 开始探测远端设备.
     *
     * @param callbacks
     *            回调
     * @return 错误号. 如果成功，返回0. 如果失败可能返回
     *         {@link com.ingenic.iwds.uniconnect.UniconnectErrorCode#EDISCOVERYISONGING
     *         EDISCOVERYISONGING},
     *         {@link com.ingenic.iwds.uniconnect.UniconnectErrorCode#EDISABLED
     *         EDISABLED},
     *         {@link com.ingenic.iwds.uniconnect.UniconnectErrorCode#EREMOTEEXCEPTION
     *         EREMOTEEXCEPTION}.
     */
    public abstract int startDiscovery(DeviceDiscoveryCallbacks callbacks);

    /**
     * 取消探测远端设备.
     */
    public abstract void cancelDiscovey();

    /**
     * 创建链接.
     *
     * @param deviceDescriptor
     *            设备描述符
     * @return 链接
     */
    public Link createLink(DeviceDescriptor deviceDescriptor) {
        IwdsAssert.dieIf(this, deviceDescriptor == null,
                "Device descriptor is null.");

        Link link = new Link(m_linkManager, deviceDescriptor, m_linkTag);

        m_linksLock.lock();
        m_links.add(link);
        m_linksLock.unlock();

        return link;
    }

    /**
     * 销毁链接.
     *
     * @param link
     *            链接
     */
    public void destroyLink(Link link) {
        if (link.isBonded() || link.isServerStarted()) {
            if (link.isRoleAsClientSide())
                link.unbond();
            else
                link.stopServer();
        }

        m_linksLock.lock();
        m_links.remove(link);
        m_linksLock.unlock();
    }

    /**
     * 获取远端设备列表.
     *
     * @return 远端设备列表
     */
    public ArrayList<RemoteDevice> getRemoteDevices() {
        return m_devices;
    }

    /**
     * 获取链接标记.
     *
     * @return 链接标记
     */
    public String getLinkTag() {
        return m_linkTag;
    }

    /**
     * 获取已绑定的地址.
     *
     * @return 已绑定的地址
     */
    public ArrayList<String> getBondedAddressStorage() {
        ArrayList<String> addresses = m_linkManager.getBondedAddressStorage();

        ArrayList<String> thisAddresses = new ArrayList<String>();
        for (String address : addresses) {
            if (m_linkManager.getBondedLinkTagStorage(address)
                    .equals(m_linkTag))
                thisAddresses.add(address);
        }

        return thisAddresses;
    }

    /**
     * 创建适配器.
     *
     * @param context
     *            应用的上下文
     * @param adapterManager
     *            适配器的管理者
     * @param linkTag
     *            链接标记
     * @return 适配器
     */
    /* package */static Adapter createAdapter(Context context,
            AdapterManager adapterManager, String linkTag) {
        Adapter adapter = null;

        if (linkTag.equals(TAG_ANDROID_BT_DATA_CHANNEL)) {
            adapter = new AndroidBtAdapter(context, adapterManager);
        } else if (linkTag.equals(TAG_BLE_DATA_CHANNEL)) {
            adapter = new BleAdapter(context, adapterManager);
        }

        IwdsAssert.dieIf("Adapter", adapter == null,
                "Unsupport link type, tag: " + linkTag);

        return adapter;
    }

    /**
     * 实例化适配器对象.
     *
     * @param context
     *            应用的上下文
     * @param adapterManager
     *            适配器的管理者
     * @param linkTag
     *            链接标记
     */
    /* package */Adapter(Context context, AdapterManager adapterManager,
            String linkTag) {
        m_context = context;
        m_adapterManager = adapterManager;
        m_linkManager = m_adapterManager.getLinkManager();
        m_linkTag = linkTag;

        m_devices = new ArrayList<RemoteDevice>();

        m_links = new ArrayList<Link>();
    }

    /**
     * 链接状态变化时的回调.
     *
     * @param state
     *            the state
     * @param isRoleAsClient
     *            the is role as client
     * @param address
     *            the address
     */
    /* package */void onLinkStateChanged(int state, boolean isRoleAsClient,
            String address) {
        m_linksLock.lock();
        for (Link link : m_links) {
            m_linksLock.unlock();

            if (link.isBonded() || link.isServerStarted()) {
                if (!link.isRoleAsClientSide())
                    link.onLinkStateChanged(state, address);
                else if (link.getBondedAddress().equals(address))
                    link.onLinkStateChanged(state, address);
            }

            m_linksLock.lock();
        }
        m_linksLock.unlock();
    }

    /**
     * 获取应用的上下文.
     *
     * @return 应用的上下文
     */
    protected Context getContext() {
        return m_context;
    }

    /**
     * 添加远端设备.
     *
     * @param device
     *            远端设备
     */
    protected void addRemoteDevice(RemoteDevice device) {
        m_devices.add(device);
    }

    /**
     * 清除远端设备列表.
     */
    protected void clearRemoteDevices() {
        m_devices.clear();
    }
}
