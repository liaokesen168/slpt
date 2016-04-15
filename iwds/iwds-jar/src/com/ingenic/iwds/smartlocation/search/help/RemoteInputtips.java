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

package com.ingenic.iwds.smartlocation.search.help;

import java.util.List;

/**
 * 输入提示类，前提需要联网
 */
public class RemoteInputtips {
    private RemoteInputtipsListener callback;
    private RemoteInputQuery query;

    /**
     * RemoteInputtips构造函数
     * 
     * @param listener
     *            输入提示回调的监听接口
     */
    public RemoteInputtips(RemoteInputtipsListener listener) {
        this.callback = listener;
    }

    /**
     * 设置帮助信息输入查询类
     * 
     * @param query
     *            帮助信息输入查询类
     */
    public void setQuery(RemoteInputQuery query) {
        this.query = query;
    }

    /**
     * 返回帮助信息输入查询类
     * 
     * @return 帮助信息输入查询类
     */
    public RemoteInputQuery getQuery() {
        return this.query;
    }

    /**
     * 设置输入提示回调的监听接口
     * 
     * @param listener
     *            输入提示回调的监听接口
     */
    public void setRemoteInputtipsListener(RemoteInputtipsListener listener) {
        this.callback = listener;
    }

    /**
     * 返回输入提示回调的监听接口
     * 
     * @return 输入提示回调的监听接口
     */
    public RemoteInputtipsListener getRemoteInputtipsListener() {
        return this.callback;
    }

    /**
     * 输入提示回调的监听接口
     */
    public interface RemoteInputtipsListener {
        /**
         * 输入提示回调的方法
         * 
         * @param tipList
         *            输入提示接口回调的提示列表
         * 
         * @param errorCode
         *            返回结果成功或者失败的响应码。0为成功，其他为失败
         */
        void onGetInputtips(List<RemoteTip> tipList, int errorCode);
    }
}
