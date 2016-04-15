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

import android.content.Context;

import com.ingenic.iwds.utils.IwdsAssert;

/**
 * 适配器管理者类.
 */
public class AdapterManager {
    private static AdapterManager sm_theManager;
    private LinkManager m_linkManager;
    private Context m_context;

    private ArrayList<Adapter> m_adapters;

    /**
     * 获取适配器管理者的单例对象.
     *
     * @param context
     *            应用的上下文
     * @return 适配器管理者的单例对象
     */
    public static AdapterManager getInstance(Context context) {
        IwdsAssert.dieIf("AdapterManager", context == null,
                "Application context is null.");

        if (sm_theManager == null)
            sm_theManager = new AdapterManager(context);

        return sm_theManager;
    }

    /**
     * 获取适配器.
     *
     * @param linkTag
     *            链接标记
     * @return 适配器
     */
    public Adapter getAdapter(String linkTag) {
        for (Adapter adapter : m_adapters)
            if (adapter.getLinkTag().equals(linkTag))
                return adapter;

        IwdsAssert.dieIf("Adapter", true, "Unsupport link type, tag: "
                + linkTag);

        return null;
    }

    private AdapterManager(Context context) {
        IwdsAssert.dieIf(this, context == null, "Context is null.");

        m_context = context;
        m_linkManager = new LinkManager(m_context, this);

        m_adapters = new ArrayList<Adapter>();

        /*
         * Create all data channel adapters
         */
        ArrayList<String> tags = m_linkManager.getLinkTags();
        for (String tag : tags)
            m_adapters.add(Adapter.createAdapter(m_context, this, tag));
    }

    /**
     * 获取链接管理者.
     *
     * @return 链接管理者
     */
    /* package */LinkManager getLinkManager() {
        return m_linkManager;
    }

    /**
     * 获取适配器列表.
     *
     * @return 适配器列表
     */
    /* package */ArrayList<Adapter> getAdapterList() {
        return m_adapters;
    }
}
