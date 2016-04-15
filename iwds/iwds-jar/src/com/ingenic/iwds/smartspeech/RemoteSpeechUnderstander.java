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

package com.ingenic.iwds.smartspeech;

import java.util.HashMap;

import android.text.TextUtils;

import com.ingenic.iwds.smartspeech.business.RemoteBusiness;
import com.ingenic.iwds.utils.IwdsAssert;

/**
 * 远程语音理解类
 */
public class RemoteSpeechUnderstander {
    /**
     * 语义场景
     */
    public static final String SCENE = "scene";

    private static RemoteSpeechUnderstander sInstance;

    private HashMap<String, String> m_parameters = new HashMap<String, String>();
    private RemoteUnderstanderListener m_listener;

    private RemoteSpeechUnderstander() {

    }

    /**
     * 返回语音理解实例
     * 
     * @return 语音理解实例
     */
    public static RemoteSpeechUnderstander getInstance() {
        if (sInstance == null)
            sInstance = new RemoteSpeechUnderstander();

        return sInstance;
    }

    /**
     * 设置识别参数
     * 
     * @param key
     *            参数名称
     * @param value
     *            参数值
     */
    public void setParameter(String key, String value) {
        IwdsAssert.dieIf(this, TextUtils.isEmpty(key), "key is null or empty");

        if (TextUtils.isEmpty(value))
            m_parameters.remove(key);

        m_parameters.put(key, value);
    }

    /**
     * 返回指定的识别参数
     * 
     * @param key
     *            参数名称
     * @return　参数值
     */
    public String getParameter(String key) {
        return m_parameters.get(key);
    }

    /**
     * 返回识别参数
     * 
     * @return 识别参数
     */
    HashMap<String, String> getParameters() {
        return this.m_parameters;
    }

    /**
     * 清除所有参数
     */
    public void clearParameter() {
        m_parameters.clear();
    }

    /**
     * 设置语音理解监听器
     * 
     * @param listener
     *            监听器对象
     */
    public void setListener(RemoteUnderstanderListener listener) {
        m_listener = listener;
    }

    /**
     * 返回监听器
     * 
     * @return 　监听器对象
     */
    public RemoteUnderstanderListener getListener() {
        return m_listener;
    }

    /**
     * 语音理解监听接口
     */
    public interface RemoteUnderstanderListener {
        /**
         * 返回理解识别状态
         * 
         * @param isListening
         *            true表示正在进行识别，false表示空闲
         */
        void onListeningStatus(boolean isListening);

        /**
         * 录音启动回调
         */
        void onBeginOfSpeech();

        /**
         * 录音自动停止回调
         */
        void onEndOfSpeech();

        /**
         * 录音识别已取消并已释放资源
         */
        void onCancel();

        /**
         * 识别错误回调
         * 
         * @param errorCode
         *            错误码
         */
        void onError(int errorCode);

        /**
         * 识别结果回调
         * 
         * @param result
         *            识别结果
         */
        void onResult(RemoteBusiness result);

        /**
         * 音量变化回调
         * 
         * @param volume
         *            录音音量值，范围0-30
         */
        void onVolumeChanged(int volume);
    }
}
