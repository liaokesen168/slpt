package com.ingenic.iwds.smartspeech;

import java.util.HashMap;

import com.ingenic.iwds.utils.IwdsAssert;

import android.text.TextUtils;

/**
 * 远程语音听写类
 */
public class RemoteSpeechRecognizer {
    /**
     * 语法内容
     */
    public static final String GRAMMAR_ID = "grammar_id";

    /**
     * 语法类型
     */
    public static final String GRAMMAR_TYPE = "grammar_type";

    /**
     * 语法编码
     */
    public static final String GRAMMAR_ENCODEING = "grammar_encoding";

    /**
     * 语法内容
     */
    public static final String GRAMMAR_CONTENT = "grammar_content";

    /**
     * 词典内容
     */
    public static final String LEXICON_CONTENT = "lexicon_content";

    /**
     * 词典名字
     */
    public static final String LEXICON_NAME = "lexicon_name";

    private static RemoteSpeechRecognizer sInstance;

    private RemoteRecognizerListener m_listener;
    private HashMap<String, String> m_parameters = new HashMap<String, String>();

    private RemoteSpeechRecognizer() {

    }

    /**
     * 返回语音听写实例
     * 
     * @return 语音听写实例
     */
    public static RemoteSpeechRecognizer getInstance() {
        if (sInstance == null)
            sInstance = new RemoteSpeechRecognizer();

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
     * 返回识别参数
     * 
     * @return 识别参数
     */
    HashMap<String, String> getParameters() {
        return this.m_parameters;
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
     * 清除所有参数
     */
    public void clearParameter() {
        m_parameters.clear();
    }

    /**
     * 设置听写监听器
     * 
     * @param listener
     *            监听器对象
     */
    public void setListener(RemoteRecognizerListener listener) {
        m_listener = listener;
    }

    /**
     * 返回监听器
     * 
     * @return 　监听器对象
     */
    public RemoteRecognizerListener getListener() {
        return m_listener;
    }

    /**
     * 语音识别监听接口
     */
    public interface RemoteRecognizerListener {
        /**
         * 返回听写识别状态
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
         * @param isLast
         *            true表示最后一次结果，false表示结果未取完
         */
        void onResult(String result, boolean isLast);

        /**
         * 音量变化回调
         * 
         * @param volume
         *            录音音量值，范围0-30
         */
        void onVolumeChanged(int volume);
    }
}
