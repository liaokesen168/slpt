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

public class RemoteSpeechConstant {
    /**
     * 引擎类型
     */
    public static final String ENGINE_TYPE = "engine_type";

    /**
     * 语言
     */
    public static final String LANGUAGE = "language";

    /**
     * 语言区域
     */
    public static final String ACCENT = "accent";

    /**
     * 应用领域
     */
    public static final String DOMAIN = "domain";

    /**
     * VAD前端点超时
     */
    public static final String VAD_BOS = "vad_bos";

    /**
     * VAD后端点超时
     */
    public static final String VAD_EOS = "vad_eos";

    /**
     * 语音识别时的提示音播放，可选范围：0不播放，1只播放结果及出错提示音，2播放录音开始与停止、结果与出错提示音 默认值：2
     */
    public static final String TONE_PLAY = "tone_play";

    /**
     * 扩展参数
     */
    public static final String PARAMS = "params";

    /**
     * 识别时，判断组件此引擎是否可用的引擎标识
     */
    public static final String ENG_ASR = "asr";

    /**
     * 合成时，判断组件此引擎是否可用的引擎标识
     */
    public static final String ENG_TTS = "tts";

    /**
     * 语义时，判断组件此引擎是否可用的引擎标识
     */
    public static final String ENG_NLU = "nlu";

//    public static final String ENG_QBH = "qbh";

    /**
     * 音频活动检测,判断组件此引擎是否可用的引擎标识
     */
    public static final String ENG_VAD = "vad";

    /**
     * 识别action
     */
    public static final String ACTION_SPEECH_RECOGNIZER = "com.iflytek.viafly.component.speechrecognizer";

    /**
     * 合成action
     */
    public static final String ACTION_SPEECH_SYNTHESIZER = "com.iflytek.viafly.component.speechsynthesizer";

    /**
     * 语音语义action
     */
    public static final String ACTION_SPEECH_UNDERSTANDER = "com.iflytek.viafly.component.speechunderstander";

    /**
     * 文本语义action
     */
    public static final String ACTION_TEXT_UNDERSTANDER = "com.iflytek.viafly.component.textunderstander";

    /**
     * VAD action
     */
    public static final String ACTION_VAD_CHECKER = "com.iflytek.viafly.component.vadchecker";

//    public static final String ACTION_MUSIC_RECOGNIZER = "com.iflytek.viafly.component.musicrecognizer";

//    public static final String KEY_RECORD_URI = "com.iflytek.component.recorduri";

//    public static final String KEY_CUSTOMDATA_TYPE = "com.iflytek.component.datatype";

    /**
     * 调用者APP的相关信息
     */
    public static final String KEY_CALLER_APPID = "caller.appid";

    /**
     * 应用名称
     */
    public static final String KEY_CALLER_NAME = "caller.name";

    /**
     * 应用包名
     */
    public static final String KEY_CALLER_PKG_NAME = "caller.pkg";

    /**
     * 应用版本名称
     */
    public static final String KEY_CALLER_VER_NAME = "caller.ver.name";

    /**
     * 应用版本号
     */
    public static final String KEY_CALLER_VER_CODE = "caller.ver.code";

    /**
     * 引擎类型
     */
    public static final String METADATA_KEY_ENGINE_TYPE = "enginetype";

//    public static final String ACTION_CHECK_LOCAL_TTS = "com.iflytek.cmcc.speech.CHECK_LOCAL_TTS";

//    public static final String KEY_CHECK_LOCAL_TTS = "com.iflytek.cmcc.KEY_CHECK_LOCAL_TTS";

//    public static final int CHECK_LOCAL_TTS_RESULT_CODE = 1000;
}
