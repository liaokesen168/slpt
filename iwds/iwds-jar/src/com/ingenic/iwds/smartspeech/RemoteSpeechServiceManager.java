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
import java.util.UUID;

import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.smartspeech.RemoteSpeechRecognizer.RemoteRecognizerListener;
import com.ingenic.iwds.smartspeech.RemoteSpeechSynthesizer.RemoteSynthesizerListener;
import com.ingenic.iwds.smartspeech.RemoteSpeechUnderstander.RemoteUnderstanderListener;
import com.ingenic.iwds.smartspeech.business.RemoteBusiness;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

public class RemoteSpeechServiceManager extends ServiceManagerContext {
    /**
     * AudioRecord状态变化键值
     */
    public final static String ACTION_REMOTE_SPEECH_AUDIO_RECOED_STATUS_CHANGED = "com.ingenic.remotespeech.audio_record_status_changed";

    /**
     * AudioTrack状态变化键值
     */
    public final static String ACTION_REMOTE_SPEECH_AUDIO_TRACK_STATUS_CHANGED = "com.ingenic.remotespeech.audio_track_status_changed";

    private IRemoteSpeechService m_service;

    private HashMap<RemoteStatusListener, RemoteStatusCallback> m_remoteStatusListeners;
    private RemoteRecognizerCallback m_recognizerCallback;
    private RemoteUnderstanderCallback m_understanderCallback;
    private RemoteSynthesizerCallback m_synthesizerCallback;

    private int m_remoteStatus = RemoteSpeechErrorCode.ERROR_REMOTE_DISCONNECTED;

    /**
     * AudioRecord状态集合类
     */
    public final class AudioRecordState {
        /**
         * 录音初始化
         */
        public static final int sInitialized = 0;

        /**
         * 正在录音
         */
        public static final int sRecording = 1;

        /**
         * 录音停止
         */
        public static final int sStoped = 2;

        /**
         * 录音结束
         */
        public static final int sReleased = 3;
    }

    /**
     * AudioRecord状态编码转化为字符串
     * 
     * @param state
     *            状态编码
     * @return 状态字符串
     */
    public static String AudioRecordStateToString(int state) {
        switch (state) {
        case AudioRecordState.sInitialized:
            return "sInitialized";

        case AudioRecordState.sRecording:
            return "sRecording";

        case AudioRecordState.sStoped:
            return "sStoped";

        case AudioRecordState.sReleased:
            return "sReleased";

        default:
            return "sUnknowState";
        }
    }

    /**
     * AudioTrack状态集合类
     */
    public final class AudioTrackState {
        /**
         * 播放初始化
         */
        public static final int sInitialized = 0;

        /**
         * 正在播放
         */
        public static final int sPlaying = 1;

        /**
         * 播放暂停
         */
        public static final int sPaused = 2;

        /**
         * 播放结束
         */
        public static final int sReleased = 3;
    }

    /**
     * AudioTrack状态编码转化为字符串
     * 
     * @param state
     *            状态编码
     * @return 状态字符串
     */
    public static String AudioTrackStateToString(int state) {
        switch (state) {
        case AudioTrackState.sInitialized:
            return "sInitialized";

        case AudioTrackState.sPlaying:
            return "sPlaying";

        case AudioTrackState.sPaused:
            return "sPaused";

        case AudioTrackState.sReleased:
            return "sReleased";

        default:
            return "sUnknowState";
        }
    }

    public RemoteSpeechServiceManager(Context context) {
        super(context);

        m_remoteStatusListeners = new HashMap<RemoteStatusListener, RemoteStatusCallback>();

        m_serviceClientProxy = new ServiceClientProxy() {

            @Override
            public void onServiceConnected(IBinder binder) {
                m_service = IRemoteSpeechService.Stub.asInterface(binder);
            }

            @Override
            public void onServiceDisconnected(boolean unexpected) {
                destroyAllModules();
            }

            @Override
            public IBinder getBinder() {
                return m_service.asBinder();
            }

        };
    }

    /* --------------------- RemoteStatusCallback -------------------------- */
    private class RemoteStatusCallback extends IRemoteStatusCallback.Stub {
        private final static int MSG_REMOTE_STATUS_CHANGED = 0;

        private RemoteStatusListener m_listener;
        private int status = RemoteSpeechErrorCode.ERROR_REMOTE_DISCONNECTED;
        String uuid;

        RemoteStatusCallback(RemoteStatusListener listener) {
            m_listener = listener;
            uuid = UUID.randomUUID().toString();
        }

        private Handler m_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_REMOTE_STATUS_CHANGED:
                    m_remoteStatus = msg.arg1;

                    if (m_remoteStatus != RemoteSpeechErrorCode.SUCCESS)
                        destroyAllModules();

                    if (m_remoteStatus != status)
                        m_listener.onAvailable(m_remoteStatus);

                    status = m_remoteStatus;

                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");
                }
            }
        };

        @Override
        public void onAvailable(int errorCode) throws RemoteException {
            Message.obtain(m_handler, MSG_REMOTE_STATUS_CHANGED, errorCode, 0)
                    .sendToTarget();
        }
    }

    /**
     * 用于注册远端设备语音服务状态的监听器
     * {@link com.ingenic.iwds.smartspeech.RemoteStatusListener
     * RemoteStatusListener}
     *
     * @param listener
     *            监听器对象
     *            {@link com.ingenic.iwds.smartspeech.RemoteStatusListener
     *            RemoteStatusListener}
     *
     * @return true 注册成功, false 注册失败
     */
    public boolean registerRemoteStatusListener(RemoteStatusListener listener) {
        IwdsAssert.dieIf(this, listener == null, "Listener is null");

        RemoteStatusCallback callback = m_remoteStatusListeners.get(listener);
        if (callback != null) {
            IwdsAssert.dieIf(this, true, "Unable to register listener: "
                    + "Did you forget to call unregisterRemoteStatusListener?");
            return false;

        } else {
            callback = new RemoteStatusCallback(listener);
            m_remoteStatusListeners.put(listener, callback);
        }

        try {
            m_service.registerRemoteStatusListener(callback.uuid, callback);

        } catch (RemoteException e) {
            IwdsLog.e(
                    this,
                    "Exception in registerRemoteStatusListener: "
                            + e.toString());
            return false;
        }

        return true;
    }

    /**
     * 注销远端设备语音服务状态的监听器
     * {@link com.ingenic.iwds.smartspeech.RemoteStatusListener
     * RemoteStatusListener}
     * 
     * @param listener
     *            监听器对象
     *            {@link com.ingenic.iwds.smartspeech.RemoteStatusListener
     *            RemoteStatusListener}
     */
    public void unregisterRemoteStatusListener(RemoteStatusListener listener) {
        IwdsAssert.dieIf(this, listener == null, "Listener is null");

        RemoteStatusCallback callback = m_remoteStatusListeners.get(listener);
        if (callback == null)
            return;

        try {
            m_service.unregisterRemoteStatusListener(callback.uuid);

        } catch (RemoteException e) {
            IwdsLog.e(
                    this,
                    "Exception in unregisterRemoteStatusListener: "
                            + e.toString());
        }

        m_remoteStatusListeners.remove(listener);
    }

    /* -------------------- RemoteStatusCallback end ----------------------- */
    private class RemoteRecognizerCallback extends
            IRemoteRecognizerCallback.Stub {
        private static final int MSG_ON_BEGIN_SPEECH = 0;
        private static final int MSG_ON_END_SPEECH = 1;
        private static final int MSG_ON_ERROR = 2;
        private static final int MSG_ON_RESULT = 3;
        private static final int MSG_ON_VOLUME_CHANGED = 4;
        private static final int MSG_RECOGNIZE_STATUS = 5;
        private static final int MSG_ON_CANCEL = 6;

        private RemoteRecognizerListener m_listener;

        RemoteRecognizerCallback(RemoteRecognizerListener listener) {
            m_listener = listener;
        }

        private Handler m_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_ON_BEGIN_SPEECH:
                    m_listener.onBeginOfSpeech();
                    break;

                case MSG_ON_END_SPEECH:
                    m_listener.onEndOfSpeech();
                    break;

                case MSG_ON_ERROR:
                    m_listener.onError(msg.arg1);
                    break;

                case MSG_ON_RESULT:
                    m_listener.onResult((String) msg.obj, msg.arg1 == 1 ? true
                            : false);
                    break;

                case MSG_ON_VOLUME_CHANGED:
                    m_listener.onVolumeChanged(msg.arg1);
                    break;

                case MSG_RECOGNIZE_STATUS:
                    m_listener.onListeningStatus(msg.arg1 == 1 ? true : false);
                    break;

                case MSG_ON_CANCEL:
                    m_recognizerCallback = null;
                    m_listener.onCancel();
                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");
                }
            }
        };

        @Override
        public void onBeginOfSpeech() throws RemoteException {
            Message.obtain(m_handler, MSG_ON_BEGIN_SPEECH).sendToTarget();
        }

        @Override
        public void onEndOfSpeech() throws RemoteException {
            Message.obtain(m_handler, MSG_ON_END_SPEECH).sendToTarget();
        }

        @Override
        public void onError(int errorCode) throws RemoteException {
            Message.obtain(m_handler, MSG_ON_ERROR, errorCode, 0)
                    .sendToTarget();
        }

        @Override
        public void onResult(String result, boolean isLast)
                throws RemoteException {
            Message.obtain(m_handler, MSG_ON_RESULT, isLast ? 1 : 0, 0, result)
                    .sendToTarget();
        }

        @Override
        public void onVolumeChanged(int volume) throws RemoteException {
            Message.obtain(m_handler, MSG_ON_VOLUME_CHANGED, volume, 0)
                    .sendToTarget();
        }

        @Override
        public void onListeningStatus(boolean isListening)
                throws RemoteException {
            Message.obtain(m_handler, MSG_RECOGNIZE_STATUS,
                    isListening ? 1 : 0, 0).sendToTarget();
        }

        @Override
        public void onCancel() throws RemoteException {
            Message.obtain(m_handler, MSG_ON_CANCEL).sendToTarget();
        }

    }

    /**
     * 请求启动录音并开始语音听写识别
     * 
     * @param recognizer
     *            语音听写对象
     * @return true 请求成功, false 请求失败
     */
    public boolean requestStartRecognize(RemoteSpeechRecognizer recognizer) {
        checkIsCalledFromMainThread();
        if (m_remoteStatus != RemoteSpeechErrorCode.SUCCESS) {
            IwdsLog.e(this,
                    "Speech service on remote device unavailable with error code "
                            + m_remoteStatus);
            return false;
        }

        IwdsAssert.dieIf(this, recognizer == null, "recognizer is null");

        RemoteRecognizerListener listener = recognizer.getListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        HashMap<String, String> parameters = recognizer.getParameters();

        if (m_recognizerCallback == null) {
            m_recognizerCallback = new RemoteRecognizerCallback(listener);

        }

        try {
            m_service.requestStartRecognize(m_recognizerCallback, parameters);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in requestStartRecognize: " + e.toString());
            return false;
        }

        return true;
    }

    /**
     * 请求停止录音
     * 
     * @param recognizer
     *            语音听写对象
     * @return true 请求成功, false 请求失败
     */
    public boolean requestStopRecognize(RemoteSpeechRecognizer recognizer) {
        checkIsCalledFromMainThread();
        if (m_remoteStatus != RemoteSpeechErrorCode.SUCCESS) {
            IwdsLog.e(this,
                    "Speech service on remote device unavailable with error code "
                            + m_remoteStatus);
            return false;
        }

        IwdsAssert.dieIf(this, recognizer == null, "recognizer is null");

        RemoteRecognizerListener listener = recognizer.getListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if (m_recognizerCallback == null) {
            IwdsLog.e(this,
                    "Unable to stop recognize: Did you forget to call requestStartRecognize?");

            listener.onError(RemoteSpeechErrorCode.ERROR_CLIENT);
            return false;
        }

        try {
            m_service.requestStopRecognize(m_recognizerCallback);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in requestStopRecognize: " + e.toString());
            return false;
        }

        return true;
    }

    /**
     * 请求取消听写识别并释放资源
     * 
     * @param recognizer
     *            语音听写对象
     * @return true 请求成功, false 请求失败
     */
    public boolean requestCancelRecognize(RemoteSpeechRecognizer recognizer) {
        checkIsCalledFromMainThread();
        if (m_remoteStatus != RemoteSpeechErrorCode.SUCCESS) {
            IwdsLog.e(this,
                    "Speech service on remote device unavailable with error code "
                            + m_remoteStatus);
            return false;
        }

        IwdsAssert.dieIf(this, recognizer == null, "recognizer is null");

        RemoteRecognizerListener listener = recognizer.getListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if (m_recognizerCallback == null) {
            IwdsLog.e(this, "Duplicate request cancel recognize");

            listener.onError(RemoteSpeechErrorCode.ERROR_CLIENT);
            return false;
        }

        try {
            m_service.requestCancelRecognize(m_recognizerCallback);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in requestCancelRecognize: " + e.toString());
            return false;
        }

        m_recognizerCallback = null;

        return true;
    }

    /**
     * 请求获取听写识别状态
     * 
     * @param recognizer
     *            语音听写对象
     * @return true 请求成功, false 请求失败
     */
    public boolean requestRecognizeStatus(RemoteSpeechRecognizer recognizer) {
        checkIsCalledFromMainThread();
        if (m_remoteStatus != RemoteSpeechErrorCode.SUCCESS) {
            IwdsLog.e(this,
                    "Speech service on remote device unavailable with error code "
                            + m_remoteStatus);
            return false;
        }

        IwdsAssert.dieIf(this, recognizer == null, "recognizer is null");

        RemoteRecognizerListener listener = recognizer.getListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if (m_recognizerCallback == null) {
            IwdsLog.e(
                    this,
                    "Unable to reuqest recognize listen status: Did you forget to call requestStartRecognize?");

            listener.onListeningStatus(false);
            return true;
        }

        try {
            m_service.requestRecognizeStatus(m_recognizerCallback);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in requestRecognizeStatus: " + e.toString());
            return false;
        }

        return true;
    }

    /* -------------------- RemoteStatusCallback end ----------------------- */
    private class RemoteUnderstanderCallback extends
            IRemoteUnderstanderCallback.Stub {
        private static final int MSG_ON_BEGIN_SPEECH = 0;
        private static final int MSG_ON_END_SPEECH = 1;
        private static final int MSG_ON_ERROR = 2;
        private static final int MSG_ON_RESULT = 3;
        private static final int MSG_ON_VOLUME_CHANGED = 4;
        private static final int MSG_RECOGNIZE_STATUS = 5;
        private static final int MSG_ON_CANCEL = 6;

        private RemoteUnderstanderListener m_listener;

        RemoteUnderstanderCallback(RemoteUnderstanderListener listener) {
            m_listener = listener;
        }

        private Handler m_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_ON_BEGIN_SPEECH:
                    m_listener.onBeginOfSpeech();
                    break;

                case MSG_ON_END_SPEECH:
                    m_listener.onEndOfSpeech();
                    break;

                case MSG_ON_ERROR:
                    m_listener.onError(msg.arg1);
                    break;

                case MSG_ON_RESULT:
                    m_listener.onResult((RemoteBusiness) msg.obj);
                    break;

                case MSG_ON_VOLUME_CHANGED:
                    m_listener.onVolumeChanged(msg.arg1);
                    break;

                case MSG_RECOGNIZE_STATUS:
                    m_listener.onListeningStatus(msg.arg1 == 1 ? true : false);
                    break;

                case MSG_ON_CANCEL:
                    m_understanderCallback = null;
                    m_listener.onCancel();
                    break;
                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");
                }
            }
        };

        @Override
        public void onBeginOfSpeech() throws RemoteException {
            Message.obtain(m_handler, MSG_ON_BEGIN_SPEECH).sendToTarget();
        }

        @Override
        public void onEndOfSpeech() throws RemoteException {
            Message.obtain(m_handler, MSG_ON_END_SPEECH).sendToTarget();
        }

        @Override
        public void onError(int errorCode) throws RemoteException {
            Message.obtain(m_handler, MSG_ON_ERROR, errorCode, 0)
                    .sendToTarget();
        }

        @Override
        public void onResult(RemoteBusiness result) throws RemoteException {
            Message.obtain(m_handler, MSG_ON_RESULT, result).sendToTarget();
        }

        @Override
        public void onVolumeChanged(int volume) throws RemoteException {
            Message.obtain(m_handler, MSG_ON_VOLUME_CHANGED, volume, 0)
                    .sendToTarget();
        }

        @Override
        public void onListeningStatus(boolean isListening)
                throws RemoteException {
            Message.obtain(m_handler, MSG_RECOGNIZE_STATUS,
                    isListening ? 1 : 0, 0).sendToTarget();
        }

        @Override
        public void onCancel() throws RemoteException {
            Message.obtain(m_handler, MSG_ON_CANCEL).sendToTarget();
        }
    }

    /**
     * 请求启动录音并开始语音理解识别
     * 
     * @param understander
     *            语音理解对象
     * @return true 请求成功, false 请求失败
     */
    public boolean requestStartUnderstand(RemoteSpeechUnderstander understander) {
        checkIsCalledFromMainThread();
        if (m_remoteStatus != RemoteSpeechErrorCode.SUCCESS) {
            IwdsLog.e(this,
                    "Speech service on remote device unavailable with error code "
                            + m_remoteStatus);
            return false;
        }

        IwdsAssert.dieIf(this, understander == null, "understander is null");

        RemoteUnderstanderListener listener = understander.getListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        HashMap<String, String> parameters = understander.getParameters();

        if (m_understanderCallback == null) {
            m_understanderCallback = new RemoteUnderstanderCallback(listener);

        }

        try {
            m_service
                    .requestStartUnderstand(m_understanderCallback, parameters);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in requestStartUnderstand: " + e.toString());
            return false;
        }

        return true;
    }

    /**
     * 请求停止录音
     * 
     * @param understander
     *            语音理解对象
     * @return true 请求成功, false 请求失败
     */
    public boolean requestStopUnderstand(RemoteSpeechUnderstander understander) {
        checkIsCalledFromMainThread();
        if (m_remoteStatus != RemoteSpeechErrorCode.SUCCESS) {
            IwdsLog.e(this,
                    "Speech service on remote device unavailable with error code "
                            + m_remoteStatus);
            return false;
        }

        IwdsAssert.dieIf(this, understander == null, "understander is null");

        RemoteUnderstanderListener listener = understander.getListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if (m_understanderCallback == null) {
            IwdsLog.e(this,
                    "Unable to stop understand: Did you forget to call requestStartUnderstand?");

            listener.onError(RemoteSpeechErrorCode.ERROR_CLIENT);
            return false;
        }

        try {
            m_service.requestStopUnderstand(m_understanderCallback);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in requestStopUnderstand: " + e.toString());
            return false;
        }

        return true;
    }

    /**
     * 请求取消理解识别并释放资源
     * 
     * @param understander
     *            语音理解对象
     * @return true 请求成功, false 请求失败
     */
    public boolean requestCancelUnderstand(RemoteSpeechUnderstander understander) {
        checkIsCalledFromMainThread();
        if (m_remoteStatus != RemoteSpeechErrorCode.SUCCESS) {
            IwdsLog.e(this,
                    "Speech service on remote device unavailable with error code "
                            + m_remoteStatus);
            return false;
        }

        IwdsAssert.dieIf(this, understander == null, "understander is null");

        RemoteUnderstanderListener listener = understander.getListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if (m_understanderCallback == null) {
            IwdsLog.e(this, "Duplicate request cancel unserstand");

            listener.onError(RemoteSpeechErrorCode.ERROR_CLIENT);
            return false;
        }

        try {
            m_service.requestCancelUnderstand(m_understanderCallback);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in requestCancelUnderstand: " + e.toString());
            return false;
        }

        m_understanderCallback = null;

        return true;
    }

    /**
     * 请求获取理解识别状态
     * 
     * @param understander
     *            语音理解对象
     * @return true 请求成功, false 请求失败
     */
    public boolean requestUnderstandStatus(RemoteSpeechUnderstander understander) {
        checkIsCalledFromMainThread();
        if (m_remoteStatus != RemoteSpeechErrorCode.SUCCESS) {
            IwdsLog.e(this,
                    "Speech service on remote device unavailable with error code "
                            + m_remoteStatus);
            return false;
        }

        IwdsAssert.dieIf(this, understander == null, "understander is null");

        RemoteUnderstanderListener listener = understander.getListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if (m_understanderCallback == null) {
            IwdsLog.e(
                    this,
                    "Unable to reuqest understand listen status: Did you forget to call requestStartUnderstand?");

            listener.onListeningStatus(false);
            return true;
        }

        try {
            m_service.requestUnderstandStatus(m_understanderCallback);

        } catch (RemoteException e) {
            IwdsLog.e(this,
                    "Exception in requestUnderstandStatus: " + e.toString());
            return false;
        }

        return true;
    }

    private class RemoteSynthesizerCallback extends
            IRemoteSynthesizerCallback.Stub {
        private static final int MSG_ON_SPEAK_BEGIN = 0;
        private static final int MSG_ON_SPEAK_PAUSED = 1;
        private static final int MSG_ON_SPEAK_RESUMED = 2;
        private static final int MSG_ON_SPEAK_STATUS = 3;
        private static final int MSG_ON_SYNTH_PROGRESS = 4;
        private static final int MSG_ON_SYNTH_BUFFER_PROGRESS = 5;
        private static final int MSG_ON_SYNTH_COMPLETED = 6;
        private static final int MSG_ON_ERROR = 7;
        private static final int MSG_ON_CANCEL = 8;

        private RemoteSynthesizerListener m_listener;

        RemoteSynthesizerCallback(RemoteSynthesizerListener listener) {
            m_listener = listener;
        }

        private Handler m_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_ON_SPEAK_BEGIN:
                    m_listener.onSpeakBegin();
                    break;

                case MSG_ON_SPEAK_PAUSED:
                    m_listener.onSpeakPaused();
                    break;

                case MSG_ON_SPEAK_RESUMED:
                    m_listener.onSpeakResumed();
                    break;

                case MSG_ON_SPEAK_STATUS:
                    m_listener.onSpeakingStatus(msg.arg1 == 1);
                    break;

                case MSG_ON_SYNTH_PROGRESS:
                    m_listener.onSynthProgress(msg.arg1);
                    break;

                case MSG_ON_SYNTH_BUFFER_PROGRESS:
                    // m_listener.onSynthBufferProgress(msg.arg1);
                    break;

                case MSG_ON_SYNTH_COMPLETED:
                    m_listener.onSynthCompleted(msg.arg1);
                    break;

                case MSG_ON_ERROR:
                    m_listener.onError(msg.arg1);
                    break;

                case MSG_ON_CANCEL:
                    m_synthesizerCallback = null;
                    m_listener.onCancel();
                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Unknown message");
                    break;
                }
            }

        };

        @Override
        public void onSpeakingStatus(boolean isSpeaking) throws RemoteException {
            Message.obtain(m_handler, MSG_ON_SPEAK_STATUS, isSpeaking ? 1 : 0,
                    0).sendToTarget();
        }

        @Override
        public void onSynthBufferProgress(int progress) throws RemoteException {
            Message.obtain(m_handler, MSG_ON_SYNTH_BUFFER_PROGRESS, progress, 0)
                    .sendToTarget();
        }

        @Override
        public void onSynthCompleted(int errorCode) throws RemoteException {
            Message.obtain(m_handler, MSG_ON_SYNTH_COMPLETED, errorCode, 0)
                    .sendToTarget();
        }

        @Override
        public void onSpeakBegin() throws RemoteException {
            Message.obtain(m_handler, MSG_ON_SPEAK_BEGIN).sendToTarget();
        }

        @Override
        public void onSpeakPaused() throws RemoteException {
            Message.obtain(m_handler, MSG_ON_SPEAK_PAUSED).sendToTarget();
        }

        @Override
        public void onSynthProgress(int progress) throws RemoteException {
            Message.obtain(m_handler, MSG_ON_SYNTH_PROGRESS, progress, 0)
                    .sendToTarget();
        }

        @Override
        public void onSpeakResumed() throws RemoteException {
            Message.obtain(m_handler, MSG_ON_SPEAK_RESUMED).sendToTarget();
        }

        @Override
        public void onError(int errorCode) throws RemoteException {
            Message.obtain(m_handler, MSG_ON_ERROR, errorCode, 0)
                    .sendToTarget();
        }

        @Override
        public void onCancel() throws RemoteException {
            Message.obtain(m_handler, MSG_ON_CANCEL).sendToTarget();
        }
    }

    /**
     * 请求开始语音合成并播放
     * 
     * @param synthesizer
     *            语音合成对象
     * @return true 请求成功, false 请求失败
     */
    public boolean requestStartSpeak(RemoteSpeechSynthesizer synthesizer) {
        checkIsCalledFromMainThread();
        if (m_remoteStatus != RemoteSpeechErrorCode.SUCCESS) {
            IwdsLog.e(this,
                    "Speech service on remote device unavailable with error code "
                            + m_remoteStatus);
            return false;
        }

        IwdsAssert.dieIf(this, synthesizer == null, "synthesizer is null");

        RemoteSynthesizerListener listener = synthesizer.getListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        String text = synthesizer.getText();
        IwdsAssert.dieIf(this, text == null || text.isEmpty(),
                "text is null or empty");

        HashMap<String, String> parameters = synthesizer.getParameters();

        if (m_synthesizerCallback == null) {
            m_synthesizerCallback = new RemoteSynthesizerCallback(listener);

        } else {
            IwdsLog.e(this,
                    "Unable to start synthesizer: Did you forget to call requestCancelSpeak?");

            listener.onError(RemoteSpeechErrorCode.ERROR_CLIENT);
            return false;
        }

        try {
            m_service
                    .requestStartSpeak(parameters, text, m_synthesizerCallback);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in requestStartSpeak: " + e.toString());
            return false;
        }

        return true;
    }

    /**
     * 请求暂停语音合成并暂停播放
     * 
     * @param synthesizer
     *            语音合成对象
     * @return true 请求成功, false 请求失败
     */
    public boolean requestPauseSpeak(RemoteSpeechSynthesizer synthesizer) {
        checkIsCalledFromMainThread();
        if (m_remoteStatus != RemoteSpeechErrorCode.SUCCESS) {
            IwdsLog.e(this,
                    "Speech service on remote device unavailable with error code "
                            + m_remoteStatus);
            return false;
        }

        IwdsAssert.dieIf(this, synthesizer == null, "synthesizer is null");

        RemoteSynthesizerListener listener = synthesizer.getListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if (m_synthesizerCallback == null) {
            IwdsLog.e(this,
                    "Unable to pause synthesizer: Did you forget to call requestStartSpeak?");

            listener.onError(RemoteSpeechErrorCode.ERROR_CLIENT);
            return false;
        }

        try {
            m_service.requestPauseSpeak(m_synthesizerCallback);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in requestPauseSpeak: " + e.toString());
            return false;
        }

        return true;
    }

    /**
     * 请求恢复语音合成和播放
     * 
     * @param synthesizer
     *            语音合成对象
     * @return true 请求成功, false 请求失败
     */
    public boolean requestResumeSpeak(RemoteSpeechSynthesizer synthesizer) {
        checkIsCalledFromMainThread();
        if (m_remoteStatus != RemoteSpeechErrorCode.SUCCESS) {
            IwdsLog.e(this,
                    "Speech service on remote device unavailable with error code "
                            + m_remoteStatus);
            return false;
        }

        IwdsAssert.dieIf(this, synthesizer == null, "synthesizer is null");

        RemoteSynthesizerListener listener = synthesizer.getListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if (m_synthesizerCallback == null) {
            IwdsLog.e(this,
                    "Unable to resume synthesizer: Did you forget to call requestStartSpeak?");

            listener.onError(RemoteSpeechErrorCode.ERROR_CLIENT);
            return false;
        }

        try {
            m_service.requestResumeSpeak(m_synthesizerCallback);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in requestResumeSpeak: " + e.toString());
            return false;
        }

        return true;
    }

    /**
     * 请求取消语音合成并释放资源
     * 
     * @param synthesizer
     *            语音合成对象
     * @return true 请求成功, false 请求失败
     */
    public boolean requestCancelSpeak(RemoteSpeechSynthesizer synthesizer) {
        checkIsCalledFromMainThread();
        if (m_remoteStatus != RemoteSpeechErrorCode.SUCCESS) {
            IwdsLog.e(this,
                    "Speech service on remote device unavailable with error code "
                            + m_remoteStatus);
            return false;
        }

        IwdsAssert.dieIf(this, synthesizer == null, "synthesizer is null");

        RemoteSynthesizerListener listener = synthesizer.getListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if (m_synthesizerCallback == null) {
            IwdsLog.e(this, "Duplicate request cancel synthesise");

            listener.onError(RemoteSpeechErrorCode.ERROR_CLIENT);
            return false;
        }

        try {
            m_service.requestCancelSpeak(m_synthesizerCallback);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in requestStopSpeak: " + e.toString());
            return false;
        }

        m_synthesizerCallback = null;

        return true;
    }

    /**
     * 请求获取语音合成播放状态
     * 
     * @param synthesizer
     *            语音合成对象
     * @return true 请求成功, false 请求失败
     */
    public boolean requestSpeakStatus(RemoteSpeechSynthesizer synthesizer) {
        checkIsCalledFromMainThread();
        if (m_remoteStatus != RemoteSpeechErrorCode.SUCCESS) {
            IwdsLog.e(this,
                    "Speech service on remote device unavailable with error code "
                            + m_remoteStatus);
            return false;
        }

        IwdsAssert.dieIf(this, synthesizer == null, "synthesizer is null");

        RemoteSynthesizerListener listener = synthesizer.getListener();
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if (m_synthesizerCallback == null) {
            IwdsLog.e(
                    this,
                    "Unable to request synthesizer status: Did you forget to call requestStartSpeak?");

            listener.onSpeakingStatus(false);
            return true;
        }

        try {
            m_service.requestSpeakStatus(m_synthesizerCallback);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in requestSpeakStatus: " + e.toString());
            return false;
        }

        return true;
    }

    private void destroyAllModules() {
        m_recognizerCallback = null;
        m_understanderCallback = null;
        m_synthesizerCallback = null;
    }

    private static void checkIsCalledFromMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new RuntimeException(
                    "RemoteSpeechService should be used only from the application's main thread");
        }
    }
}
