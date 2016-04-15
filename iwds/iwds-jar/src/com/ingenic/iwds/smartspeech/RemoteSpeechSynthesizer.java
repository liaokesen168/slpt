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

import com.ingenic.iwds.utils.IwdsAssert;

/**
 * 远程语音合成类
 */
public class RemoteSpeechSynthesizer {
    /**
     * 语速（0~100）
     */
    public static final String SPEED = "speed";

    /**
     * 音调（0~100）
     */
    public static final String PITCH = "pitch";

    /**
     * 音量（0~100）
     */
    public static final String VOLUME = "volume";

    /**
     * 需要合成文本内容（0~1024Byte）
     */
    public static final String TEXT = "text";

    /**
     * 播放类型
     */
    public static final String STREAM_TYPE = "stream_type";

    /**
     * 发音人
     * 
     * 云端支持发音人：小燕（xiaoyan）、小宇（xiaoyu）、凯瑟琳（Catherine）、
     * 亨利（henry）、玛丽（vimary）、小研（vixy）、小琪（vixq）、
     * 小峰（vixf）、小梅（vixm）、小莉（vixl）、小蓉（四川话）、
     * 小芸（vixyun）、小坤（vixk）、小强（vixqa）、小莹（vixying）、 小新（vixx）、楠楠（vinn）老孙（vils）
     * 本地支持发音人: 小燕（xiaoyan）
     */
    public static final String VOICE_NAME = "voice_name";

    /**
     * 合成音频文件格式
     */
    public static final String AUDIO_FORMAT = "audio_format";

    private static RemoteSpeechSynthesizer sInstance;

    private RemoteSynthesizerListener m_listener;
    private HashMap<String, String> m_parameters = new HashMap<String, String>();
    private String m_text;

    private RemoteSpeechSynthesizer() {

    }

    /**
     * 返回语音合成实例
     * 
     * @return 语音合成实例
     */
    public static RemoteSpeechSynthesizer getInstance() {
        if (sInstance == null)
            sInstance = new RemoteSpeechSynthesizer();

        return sInstance;
    }

    /**
     * 设置合成参数
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
     * 返回识别参数
     * 
     * @return 识别参数
     */
    HashMap<String, String> getParameters() {
        return this.m_parameters;
    }

    /**
     * 返回指定的合成参数
     * 
     * @param key
     *            参数名称
     * @return　参数值
     */
    public String getParameter(String key) {
        return m_parameters.get(key);
    }

    /**
     * 清除所有参数
     */
    public void clearParameter() {
        m_parameters.clear();
    }

    /**
     * 返回语音合成文本内容
     * 
     * @return 合成文本内容
     */
    public String getText() {
        return m_text;
    }

    /**
     * 设置语音合成文本内容
     * 
     * @param text
     */
    public void setText(String text) {
        m_text = text;
    }

    /**
     * 设置合成监听器
     * 
     * @param listener
     *            监听器对象
     */
    public void setListener(RemoteSynthesizerListener listener) {
        m_listener = listener;
    }

    /**
     * 返回合成监听器
     * 
     * @return 监听器对象
     */
    public RemoteSynthesizerListener getListener() {
        return m_listener;
    }

    /**
     * 　远程语音合成监听接口
     */
    public interface RemoteSynthesizerListener {
        /**
         * 返回播放状态
         * 
         * @param isSpeaking
         *            true表示正在播放，false表示空闲
         */
        void onSpeakingStatus(boolean isSpeaking);

        // void onSynthBufferProgress(int progress);

        /**
         * 合成结束回调
         * 
         * @param errorCode
         *            错误编码
         */
        void onSynthCompleted(int errorCode);

        /**
         * 开始播放回调
         */
        void onSpeakBegin();

        /**
         * 暂停播放回调
         */
        void onSpeakPaused();

        /**
         * 合成进度回调
         * 
         * @param progress
         *            合成进度，范围值：0-100
         */
        void onSynthProgress(int progress);

        /**
         * 恢复播放回调
         */
        void onSpeakResumed();

        /**
         * 合成错误回调
         * 
         * @param errorCode
         *            错误码
         */
        void onError(int errorCode);

        /**
         * 合成播放已取消并已释放资源，
         */
        void onCancel();
    }
}
