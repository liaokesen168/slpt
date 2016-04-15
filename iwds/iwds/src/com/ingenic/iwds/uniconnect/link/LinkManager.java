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
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 链接管理者类.
 */
public class LinkManager {

    /** 链接连接. */
    public static final String ACTION_UNICONNECT_LINK_CONNECTED = "action.uniconnect.LinkConnected";

    /** 链接断开. */
    public static final String ACTION_UNICONNECT_LINK_DISCONNECTED = "action.uniconnect.LinkDisconnected";

    private ArrayList<Link> m_links;

    private Context m_context;
    private AdapterManager m_adapterManager;

    private SharedPreferences m_bondStorage;
    private Editor m_bondStorageEditor;

    private ArrayList<String> m_linkTags;

    private ArrayList<Adapter> m_adapters;

    /**
     * 获取链接标记.
     *
     * @return 链接标记
     */
    /* package */ArrayList<String> getLinkTags() {
        return m_linkTags;
    }

    /**
     * 获取应用的上下文.
     *
     * @return 应用的上下文
     */
    /* package */Context getContext() {
        return m_context;
    }

    /**
     * 获取已绑定的地址.
     *
     * @return 已绑定的地址
     */
    /* package */ArrayList<String> getBondedAddressStorage() {
        ArrayList<String> addresses = new ArrayList<String>();

        Map<String, ?> allAddress = m_bondStorage.getAll();
        for (String address : allAddress.keySet())
            addresses.add(address);

        return addresses;
    }

    /**
     * 根据地址获取绑定的链接标签.
     *
     * @param address
     *            地址
     * @return 地址对应的链接的标签. 如果获取失败，返回空字符串
     */
    /* package */String getBondedLinkTagStorage(String address) {
        return m_bondStorage.getString(address, "");
    }

    /**
     * 实例化一个链接管理者.
     *
     * @param context
     *            应用的上下文
     * @param adapterManager
     *            适配器管理者
     */
    /* package */LinkManager(Context context, AdapterManager adapterManager) {
        m_context = context;
        m_adapterManager = adapterManager;
        m_adapters = new ArrayList<Adapter>();

        nativeSetLinkStateChangedHandler(this);

        /*
         * Fetch link tags
         */
        m_linkTags = new ArrayList<String>();
        String tags = nativeGetLinkTypes();

        String[] t = tags.split(",");
        for (String tag : t)
            m_linkTags.add(tag);

        m_bondStorage = m_context.getSharedPreferences("bond_store",
                Context.MODE_PRIVATE);
        m_bondStorageEditor = m_bondStorage.edit();
    }

    /**
     * 通过服务端设备地址绑定服务端.
     *
     * @param inkTag
     *            链接标签
     * @param address
     *            服务端设备地址
     * @return 如果成功，返回{@code true}
     */
    /* package */boolean bondAddress(String linkTag, String address) {
        if (!nativeBondAddress(linkTag, address))
            return false;

        m_bondStorageEditor.putString(address, linkTag);
        m_bondStorageEditor.commit();

        return true;
    }

    /**
     * 解绑.
     *
     * @param address
     *            服务端设备地址
     */
    /* package */void unbond(String address) {
        nativeUnbond(address);

        m_bondStorageEditor.remove(address);
        m_bondStorageEditor.commit();
    }

    /**
     * 通过链接标签启动服务端的服务.
     *
     * @param linkTag
     *            链接标签
     * @return 如果成功, 返回{@code true}
     */
    /* package */boolean startServer(String linkTag) {

        return nativeStartServer(linkTag);
    }

    /**
     * 通过链接标签停止服务端的服务.
     *
     * @param linkTag
     *            链接标签
     */
    /* package */void stopServer(String linkTag) {
        nativeStopServer(linkTag);
    }

    /**
     * 通过链接标签获取远端设备地址.
     *
     * @param linkTag
     *            链接标签
     * @return 远端设备地址
     */
    String getRemoteAddress(String linkTag) {
        return nativeGetRemoteAddress(linkTag);
    }

    /**
     * 链接状态变化时的回调.
     *
     * @param isRoleAsClient
     *            如果是客户端，值为{@code true}
     * @param address
     *            远端设备地址
     * @param linkTag
     *            链接标签
     * @param state
     *            链接状态
     */
    public void onLinkStateChanged(boolean isRoleAsClient, String address,
            String linkTag, int state) {
        ArrayList<Adapter> adapters = m_adapterManager.getAdapterList();
        for (Adapter adapter : adapters) {
            if (adapter.getLinkTag().equals(linkTag)) {
                adapter.onLinkStateChanged(state, isRoleAsClient, address);

                return;
            }
        }
    }

    /*
     * natives
     */
    private static final native String nativeGetLinkTypes();

    private static final native boolean nativeBondAddress(String linkTag,
            String address);

    private static final native void nativeUnbond(String address);

    private static final native boolean nativeStartServer(String linkTag);

    private static final native void nativeStopServer(String linkTag);

    private static final native void nativeSetLinkStateChangedHandler(
            LinkManager lm);

    private static final native String nativeGetRemoteAddress(String linkTag);
}
