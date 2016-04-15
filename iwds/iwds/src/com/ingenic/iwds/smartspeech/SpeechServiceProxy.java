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
import java.util.Iterator;
import java.util.Set;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import com.iflytek.viafly.speech.ErrorCode;
import com.iflytek.viafly.speech.GrammarListener;
import com.iflytek.viafly.speech.ISpeechModule;
import com.iflytek.viafly.speech.InitListener;
import com.iflytek.viafly.speech.LexiconListener;
import com.iflytek.viafly.speech.RecognizerListener;
import com.iflytek.viafly.speech.SpeechUnderstanderListener;
import com.iflytek.viafly.speech.RecognizerResult;
import com.iflytek.viafly.speech.SpeechConstant;
import com.iflytek.viafly.speech.SpeechRecognizer;
import com.iflytek.viafly.speech.SpeechSynthesizer;
import com.iflytek.viafly.speech.SpeechUnderstander;
import com.iflytek.viafly.speech.SpeechUtility;
import com.iflytek.viafly.speech.SynthesizerListener;
import com.iflytek.viafly.speech.UnderstanderResult;
import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback;
import com.ingenic.iwds.datatransactor.ParcelTransactor;
import com.ingenic.iwds.smartspeech.business.RemoteBusiness;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

public class SpeechServiceProxy {
    private static final String COMPONENT_RECOGNIZER = "com.iflytek.viafly.component.speechrecognizer";
    private static final String COMPONENT_SYNTHESIZER = "com.iflytek.viafly.component.speechsynthesizer";
    private static final String COMPONENT_UNDERSTANDER = "com.iflytek.viafly.component.speechunderstander";

    private static final int BUFFER_POOL_SIZE = 6400;

    private Context m_context;
    private ParcelTransactor<RemoteSpeechRequest> m_transactor;
    private ServiceProxyHandler m_handler;
    private static SpeechServiceProxy sInstance;

    private Recognizer m_recognizer;
    private Understander m_understander;
    private Synthesizer m_synthesizer;

    private SpeechServiceProxy() {

    }

    public static SpeechServiceProxy getInstance() {
        if (sInstance == null)
            sInstance = new SpeechServiceProxy();

        return sInstance;
    }

    public void initialize(Context context) {
        IwdsLog.i(this, "Initialize");

        m_context = context;

        m_transactor = new ParcelTransactor<RemoteSpeechRequest>(m_context,
                RemoteSpeechRequest.CREATOR, m_transportCallback,
                "c1dc19e2-17a4-0797-3333-68a0dd4bfb68");

        m_handler = new ServiceProxyHandler();
    }

    public void start() {
        IwdsLog.i(this, "start");

        m_transactor.start();
    }

    private class Recognizer extends RecognizerListener.Stub implements
            InitListener {
        private int m_errorCode;
        private SpeechRecognizer m_recognizer;
        private LexiconListenerTransport m_lexiconListener;
        private GrammarListenerTransport m_grammarListener;
        private boolean m_moduleStart;
        private boolean m_moduleStopUnexpected;

        private class LexiconListenerTransport extends LexiconListener.Stub {

            @Override
            public void onLexiconUpdated(String lexiconId, int errorCode)
                    throws RemoteException {
                m_handler.notifyLexiconUpdated(lexiconId, errorCode);
            }
        }

        private class GrammarListenerTransport extends GrammarListener.Stub {

            @Override
            public void onBuildFinish(String grammarId, int errorCode)
                    throws RemoteException {
                m_handler.notifyGrammerBuildFinish(grammarId, errorCode);
            }
        }

        Recognizer() {
            IwdsLog.i(this, "Create new Recognizer");
            m_recognizer = new SpeechRecognizer(m_context, this);
            m_lexiconListener = new LexiconListenerTransport();
            m_grammarListener = new GrammarListenerTransport();
        }

        public void startListening(HashMap<String, String> parameters) {
            // force cancel listening before start listening
            m_recognizer.cancel(this);

            m_recognizer.setParameter(SpeechConstant.PARAMS, null);
            m_recognizer.setParameter(SpeechConstant.KEY_APPEND_AUDIO, "1");

            if (parameters != null) {
                Set<String> keys = parameters.keySet();
                for (Iterator<String> it = keys.iterator(); it.hasNext();) {
                    String key = it.next();
                    m_recognizer.setParameter(key, parameters.get(key));
                }

            } else {
                // TODO: fill default parameters
            }

            m_errorCode = m_recognizer.startListening(this);
            if (m_errorCode != ErrorCode.SUCCESS) {
                IwdsLog.e(this, "Failed to start recognizer with error code "
                        + m_errorCode);

                m_handler.notifyRecognizeError(m_errorCode);
            }
        }

        public void stopListening(boolean notify) {
            m_errorCode = m_recognizer.stopListening(this);
            if (m_errorCode != ErrorCode.SUCCESS) {
                IwdsLog.e(this, "Failed to stop recognizer with error code "
                        + m_errorCode);

                if (notify)
                    m_handler.notifyRecognizeError(m_errorCode);
            }
        }

        public void cancel(boolean notify) {
            m_errorCode = m_recognizer.cancel(this);
            if (m_errorCode != ErrorCode.SUCCESS) {
                IwdsLog.e(this, "Failed to cancel recognizer with error code "
                        + m_errorCode);

                if (notify)
                    m_handler.notifyRecognizeError(m_errorCode);
            }
        }

        public void destory() {
            m_moduleStart = false;
            m_recognizer.destory();
        }

        public void buildGrammar(String type, String content) {
            m_errorCode = m_recognizer.buildGrammar(type, content,
                    m_grammarListener);

            if (m_errorCode != 0)
                m_handler.notifyRecognizeError(m_errorCode);
        }

        public void updateLexicon(String name, String content, String grammarId) {
            m_errorCode = m_recognizer.updateLexicon(name, content, grammarId,
                    m_lexiconListener);

            if (m_errorCode != 0)
                m_handler.notifyRecognizeError(m_errorCode);
        }

        public void appendRecoData(int timeStamp, byte[] buffer) {
            m_errorCode = m_recognizer.appendRecoData(buffer, buffer.length,
                    timeStamp);

            if (m_errorCode != ErrorCode.SUCCESS) {
                IwdsLog.e(this,
                        "Failed to append data to recognizer with error code "
                                + m_errorCode);

                m_handler.notifyRecognizeError(m_errorCode);
            }

        }

        public boolean isListening(boolean notify) {
            boolean isListening = m_recognizer.isListening();
            if (notify)
                m_handler.notifyRecognizeListenStatus(isListening);

            return isListening;
        }

        public boolean isModuleStart() {
            return m_moduleStart;
        }

        public boolean isModuleStopUnexpected() {
            return m_moduleStopUnexpected;
        }

        @Override
        public void onInit(ISpeechModule module, int errorCode) {
            IwdsLog.i(this,
                    "Speech recognize module initialize done with error code "
                            + errorCode);

            if (m_understander != null && m_understander.isModuleStart()
                    && m_synthesizer != null && m_synthesizer.isModuleStart())
                m_handler
                        .notifySpeechServiceConnected(RemoteSpeechErrorCode.SUCCESS);

            m_moduleStart = true;
        }

        @Override
        public void onDestory(ISpeechModule module) {
            if (!m_moduleStart)
                return;

            m_moduleStart = false;

            if (!m_moduleStopUnexpected) {
                IwdsLog.i(this, "Recogniztion module disconnect unexpected");
                m_moduleStopUnexpected = true;
                if (m_understander != null
                        && m_understander.isModuleStopUnexpected()
                        && m_synthesizer != null
                        && m_synthesizer.isModuleStopUnexpected()) {
                    m_handler
                            .notifySpeechServiceConnected(RemoteSpeechErrorCode.ERROR_REMOTE_SERVICE_KILLED);

                    m_handler.connectService();
                }
            }
        }

        @Override
        public void onVolumeChanged(int volume) throws RemoteException {
            m_handler.notifyRecognizeVolumeChanged(volume);
        }

        @Override
        public void onBeginOfSpeech() throws RemoteException {
            m_handler.notifyRecognizeBeginOfSpeech();
        }

        @Override
        public void onEndOfSpeech() throws RemoteException {
            m_handler.notifyRecognizeEndOfSpeech();
        }

        @Override
        public void onResult(RecognizerResult iflytekResult, boolean isLast)
                throws RemoteException {
            String result = ProxyUtils.parseJsonResult(iflytekResult
                    .getResultString());
            if (result != null)
                m_handler.notifyRecognizeResult(result, isLast);
            else
                m_handler
                        .notifyRecognizeError(RemoteSpeechErrorCode.ERROR_INVALID_RESULT);
        }

        @Override
        public void onError(int errorCode) throws RemoteException {
            m_handler.notifyRecognizeError(errorCode);
        }

    }

    private class Understander extends SpeechUnderstanderListener.Stub
            implements InitListener {
        private int m_errorCode;
        private SpeechUnderstander m_understander;
        private boolean m_moduleStart;
        private boolean m_moduleStopUnexpected;

        Understander() {
            IwdsLog.i(this, "Create new Understander");
            m_understander = new SpeechUnderstander(m_context, this);
        }

        public void startUnderstanding(HashMap<String, String> parameters) {
            // force cancel listening before start listening
            m_understander.cancel(this);

            m_understander.setParameter(SpeechConstant.PARAMS, null);
            m_understander.setParameter(SpeechConstant.KEY_APPEND_AUDIO, "1");

            if (parameters != null) {
                Set<String> keys = parameters.keySet();
                for (Iterator<String> it = keys.iterator(); it.hasNext();) {
                    String key = it.next();
                    m_understander.setParameter(key, parameters.get(key));
                }

            } else {
                // TODO: fill default parameters
            }

            m_errorCode = m_understander.startUnderstanding(this);
            if (m_errorCode != ErrorCode.SUCCESS) {
                IwdsLog.e(this, "Failed to start understander with error code "
                        + m_errorCode);

                m_handler.notifyUnderstandError(m_errorCode);
            }
        }

        public void stopUnderstanding(boolean notify) {
            m_errorCode = m_understander.stopUnderstanding(this);

            if (m_errorCode != ErrorCode.SUCCESS) {
                IwdsLog.e(this, "Failed to stop understander with error code "
                        + m_errorCode);

                if (notify)
                    m_handler.notifyUnderstandError(m_errorCode);
            }
        }

        public void cancel(boolean notify) {
            m_errorCode = m_understander.cancel(this);
            if (m_errorCode != ErrorCode.SUCCESS) {
                IwdsLog.e(this,
                        "Failed to cancel understander with error code "
                                + m_errorCode);

                if (notify)
                    m_handler.notifyUnderstandError(m_errorCode);
            }
        }

        public void destory() {
            m_moduleStart = false;
            m_understander.destory();
        }

        public boolean isUnderstanding(boolean notify) {
            boolean isUnderstanding = m_understander.isUnderstanding();

            if (notify)
                m_handler.notifyUnderstandListenerStatus(isUnderstanding);

            return isUnderstanding;
        }

        public boolean isModuleStart() {
            return m_moduleStart;
        }

        public boolean isModuleStopUnexpected() {
            return m_moduleStopUnexpected;
        }

        public void appendRecoData(int timeStamp, byte[] buffer) {
            m_errorCode = m_understander.appendRecoData(buffer, buffer.length,
                    timeStamp);

            if (m_errorCode != ErrorCode.SUCCESS) {
                IwdsLog.e(this,
                        "Failed to append data to understander with error code "
                                + m_errorCode);

                m_handler.notifyUnderstandError(m_errorCode);
            }
        }

        @Override
        public void onInit(ISpeechModule module, int errorCode) {
            IwdsLog.i(this,
                    "Speech understand module initialize done with error code "
                            + errorCode);

            if (m_recognizer != null && m_recognizer.isModuleStart()
                    && m_synthesizer != null && m_synthesizer.isModuleStart())
                m_handler
                        .notifySpeechServiceConnected(RemoteSpeechErrorCode.SUCCESS);

            m_moduleStart = true;
        }

        @Override
        public void onDestory(ISpeechModule module) {
            if (!m_moduleStart)
                return;

            m_moduleStart = false;

            if (!m_moduleStopUnexpected) {
                IwdsLog.i(this, "Understand module disconnect unexpected");
                m_moduleStopUnexpected = true;
                if (m_recognizer != null
                        && m_recognizer.isModuleStopUnexpected()
                        && m_synthesizer != null
                        && m_synthesizer.isModuleStopUnexpected()) {
                    m_handler
                            .notifySpeechServiceConnected(RemoteSpeechErrorCode.ERROR_REMOTE_SERVICE_KILLED);

                    m_handler.connectService();
                }
            }
        }

        @Override
        public void onVolumeChanged(int volume) throws RemoteException {
            m_handler.notifyUnderstandVolumeChanged(volume);
        }

        @Override
        public void onBeginOfSpeech() throws RemoteException {
            m_handler.notifyUnderstandBeginOfSpeech();
        }

        @Override
        public void onEndOfSpeech() throws RemoteException {
            m_handler.notifyUnderstandEndOfSpeech();
        }

        @Override
        public void onResult(UnderstanderResult iflytekResult)
                throws RemoteException {
            RemoteBusiness business = ProxyUtils.parseXmlResult(iflytekResult
                    .getResultString());
            if (business != null)
                m_handler.notifyUnderstandResult(business);
            else
                m_handler
                        .notifyUnderstandError(RemoteSpeechErrorCode.ERROR_INVALID_RESULT);
        }

        @Override
        public void onError(int errorCode) throws RemoteException {
            m_handler.notifyUnderstandError(errorCode);
        }
    }

    private class Synthesizer extends SynthesizerListener.Stub implements
            InitListener {
        private int m_errorCode;
        private byte[] m_bufferPool;
        private int m_bufferPoolPos;
        private SpeechSynthesizer m_synthesizer;
        private boolean m_moduleStart;
        private boolean m_moduleStopUnexpected;

        Synthesizer() {
            IwdsLog.i(this, "Create new Synthesizer");
            m_synthesizer = new SpeechSynthesizer(m_context, this);
            m_bufferPool = new byte[BUFFER_POOL_SIZE];
        }

        public void startSpeaking(HashMap<String, String> parameters,
                String text) {
            // force stop speaking before start speaking
            m_synthesizer.stopSpeaking(this);

            m_synthesizer.setParameter(SpeechConstant.PARAMS, null);

            if (parameters != null) {
                Set<String> keys = parameters.keySet();
                for (Iterator<String> it = keys.iterator(); it.hasNext();) {
                    String key = it.next();
                    m_synthesizer.setParameter(key, parameters.get(key));
                }

            } else {
                // TODO: fill default parameters
            }

            m_errorCode = m_synthesizer.startSpeaking(text, this);
            if (m_errorCode != ErrorCode.SUCCESS) {
                IwdsLog.e(this, "Failed to speak with error code "
                        + m_errorCode);

                m_handler.notifySynthesizeError(m_errorCode);
            }
        }

        public void pauseSpeaking() {
            m_errorCode = m_synthesizer.pauseSpeaking(this);
            if (m_errorCode != ErrorCode.SUCCESS) {
                IwdsLog.e(this, "Failed to pause speak with error code "
                        + m_errorCode);

                // Ignore?
                // m_handler.notifySynthesizeError(m_errorCode);
            }
        }

        public void resumeSpeaking() {
            m_errorCode = m_synthesizer.resumeSpeaking(this);
            if (m_errorCode != ErrorCode.SUCCESS) {
                IwdsLog.e(this, "Failed to resume speak with error code "
                        + m_errorCode);

                // Ignore?
                // m_handler.notifySynthesizeError(m_errorCode);
            }
        }

        public void stopSpeaking(boolean notify) {
            m_transactor.cancelAll();
            m_errorCode = m_synthesizer.stopSpeaking(this);
            if (m_errorCode != ErrorCode.SUCCESS) {
                IwdsLog.e(this, "Failed to stop speak with error code "
                        + m_errorCode);

                // Ignore?
                // if (notify)
                // m_handler.notifySynthesizeError(m_errorCode);

            }
        }

        public void destory() {
            m_moduleStart = false;
            m_synthesizer.destory();
        }

        public boolean isSpeaking(boolean notify) {
            boolean isSpeaking = m_synthesizer.isSpeaking();

            if (notify)
                m_handler.notifySythesizeSpeakStatus(isSpeaking);

            return isSpeaking;
        }

        public boolean isModuleStart() {
            return m_moduleStart;
        }

        public boolean isModuleStopUnexpected() {
            return m_moduleStopUnexpected;
        }

        @Override
        public void onInit(ISpeechModule module, int errorCode) {
            IwdsLog.i(this,
                    "Speech synthesise module initialize done with error code "
                            + errorCode);

            if (m_understander != null && m_understander.isModuleStart()
                    && m_recognizer != null && m_recognizer.isModuleStart())
                m_handler
                        .notifySpeechServiceConnected(RemoteSpeechErrorCode.SUCCESS);

            m_moduleStart = true;
        }

        @Override
        public void onDestory(ISpeechModule module) {
            if (!m_moduleStart)
                return;

            m_moduleStart = false;

            if (!m_moduleStopUnexpected) {
                IwdsLog.i(this, "Synthesiser module disconnect unexpected");
                m_moduleStopUnexpected = true;
                if (m_understander != null
                        && m_understander.isModuleStopUnexpected()
                        && m_recognizer != null
                        && m_recognizer.isModuleStopUnexpected()) {
                    m_handler
                            .notifySpeechServiceConnected(RemoteSpeechErrorCode.ERROR_REMOTE_SERVICE_KILLED);

                    m_handler.connectService();
                }
            }
        }

        @Override
        public void onSpeakBegin() throws RemoteException {
            IwdsLog.d(this, "onSpeakBegin");
            m_handler.notifySynthesizeSpeakBegin();
        }

        @Override
        public void onSpeakPaused() throws RemoteException {
            IwdsLog.d(this, "onSpeakPaused");
            m_handler.notifySynthesizeSpeakPaused();
        }

        @Override
        public void onSpeakResumed() throws RemoteException {
            IwdsLog.d(this, "onSpeakResumed");
            m_handler.notifySynthesizeSpeakResumed();
        }

        @Override
        public void onCompleted(int errorCode) throws RemoteException {
            IwdsLog.d(this, "onCompleted " + errorCode);

            if (m_bufferPoolPos > 0) {
                byte[] sendBuffer = new byte[m_bufferPoolPos];

                System.arraycopy(m_bufferPool, 0, sendBuffer, 0,
                        m_bufferPoolPos);

                m_handler.notifySynthesizeData(sendBuffer);
                m_bufferPoolPos = 0;
            }

            m_handler.notifySynthesizeSpeakCompleted(errorCode);
        }

        @Override
        public void onSpeakProgress(int progress) throws RemoteException {
            IwdsLog.d(this, "onSpeakProgress " + progress);

            m_handler.notifySynthesizeSpeakProgress(progress);
        }

        @Override
        public void onBufferProgress(int progress) throws RemoteException {
            IwdsLog.d(this, "onBufferProgress " + progress);

            m_handler.notifySynthesizeBufferProgress(progress);
        }

        @Override
        public void onSpeakData(byte[] buffer) throws RemoteException {
            int poolLeftSize = BUFFER_POOL_SIZE - m_bufferPoolPos;

            if (buffer.length <= poolLeftSize) {

                System.arraycopy(buffer, 0, m_bufferPool, m_bufferPoolPos,
                        buffer.length);
                m_bufferPoolPos += buffer.length;

            } else {
                System.arraycopy(buffer, 0, m_bufferPool, m_bufferPoolPos,
                        poolLeftSize);

                int dataLeft = buffer.length - poolLeftSize;

                byte[] sendBuffer = new byte[BUFFER_POOL_SIZE];
                System.arraycopy(m_bufferPool, 0, sendBuffer, 0,
                        BUFFER_POOL_SIZE);

                m_handler.notifySynthesizeData(sendBuffer);

                m_bufferPoolPos = 0;

                System.arraycopy(buffer, poolLeftSize, m_bufferPool,
                        m_bufferPoolPos, dataLeft);
                m_bufferPoolPos += dataLeft;
            }
        }
    }

    private class SpeakArgs {
        final HashMap<String, String> parameters;
        final String text;

        public SpeakArgs(HashMap<String, String> parameters, String text) {
            this.parameters = parameters;
            this.text = text;
        }
    }

    private class ServiceProxyHandler extends Handler {
        private final static int MSG_CHANNEL_STATUS_CHANGED = 0;
        private final static int MSG_SPEECH_SERVICE_CONNECTED = 1;

        private static final int MSG_REQUEST_RECOGNIZE_APPEND_DATA = 2;
        private static final int MSG_REQUEST_RECOGNIZE_START_LISTEN = 3;
        private static final int MSG_REQUEST_RECOGNIZE_LISTEN_STATUS = 4;
        private static final int MSG_REQUEST_RECOGNIZE_STOP_LISTEN = 5;
        private static final int MSG_REQUEST_RECOGNIZE_CANCEL_LISTEN = 6;

        private static final int MSG_REQUEST_UNDERSTAND_APPEND_DATA = 7;
        private static final int MSG_REQUEST_UNDERSTAND_START_LISTEN = 8;
        private static final int MSG_REQUEST_UNDERSTAND_LISTEN_STATUS = 9;
        private static final int MSG_REQUEST_UNDERSTAND_STOP_LISTEN = 10;
        private static final int MSG_REQUEST_UNDERSTAND_CANCEL_LISTEN = 11;

        private static final int MSG_REQUEST_SYNTHESISE_START_SPEAK = 12;
        private static final int MSG_REQUEST_SYNTHESISE_PAUSE_SPEAK = 13;
        private static final int MSG_REQUEST_SYNTHESISE_RESUME_SPEAK = 14;
        private static final int MSG_REQUEST_SYNTHESISE_STOP_SPEAK = 15;
        private static final int MSG_REQUEST_SYNTHESISE_SPEAK_STATUS = 16;

        private final static int MSG_RECOGNIZE_ERROR = 20;
        private final static int MSG_RECOGNIZE_STATUS = 21;
        private final static int MSG_RECOGNIZE_VOLUME_CHANGED = 22;
        private final static int MSG_RECOGNIZE_BEGIN_SPEECH = 23;
        private final static int MSG_RECOGNIZE_END_SPEECH = 24;
        private final static int MSG_RECOGNIZE_RESULT = 25;

        private final static int MSG_UNDERSTAND_ERROR = 30;
        private final static int MSG_UNDERSTAND_STATUS = 31;
        private final static int MSG_UNDERSTAND_VOLUME_CHANGED = 32;
        private final static int MSG_UNDERSTAND_BEGIN_SPEECH = 33;
        private final static int MSG_UNDERSTAND_END_SPEECH = 34;
        private final static int MSG_UNDERSTAND_RESULT = 35;

        private final static int MSG_SYNTHESISE_BUFFER_PROGRESS_CHANGED = 40;
        private final static int MSG_SYNTHESISE_COMPLETE = 41;
        private final static int MSG_SYNTHESISE_SPEAK_BEGIN = 42;
        private final static int MSG_SYNTHESISE_SPEAK_PAUSED = 43;
        private final static int MSG_SYNTHESISE_SPEAK_PROGRESS_CHANGED = 44;
        private final static int MSG_SYNTHESISE_SPEAK_RESUMED = 45;
        private final static int MSG_SYNTHESISE_SPEAK_STATUS = 46;
        private final static int MSG_SYNTHESISE_SPEAK_ERROR = 47;
        private final static int MSG_SYNTHESISE_SPEAK_DATA = 48;

        private final static int MSG_LEXICON_UPDATED = 50;
        private final static int MSG_GRAMMAR_BUILDED = 51;

        private final static int MSG_CONNECT_IFLYTEK_SERVICE = 60;
        private final static int MSG_DISCONNECT_IFLYTEK_SERVICE = 61;

        private boolean m_channelAvailable;
        private int m_serviceConnectStatus = -1;

        public void setChannelState(boolean available) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_CHANNEL_STATUS_CHANGED;
            msg.arg1 = available ? 1 : 0;

            msg.sendToTarget();
        }

        public void connectService() {
            final Message msg = Message.obtain(this);

            msg.what = MSG_CONNECT_IFLYTEK_SERVICE;

            msg.sendToTarget();
        }

        public void disconnectService() {
            final Message msg = Message.obtain(this);

            msg.what = MSG_DISCONNECT_IFLYTEK_SERVICE;

            msg.sendToTarget();
        }

        public void notifySpeechServiceConnected(int errorCode) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_SPEECH_SERVICE_CONNECTED;
            msg.arg1 = errorCode;

            msg.sendToTarget();
        }

        public void notifyRecognizeVolumeChanged(int volume) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_RECOGNIZE_VOLUME_CHANGED;
            msg.arg1 = volume;

            msg.sendToTarget();
        }

        public void notifyRecognizeBeginOfSpeech() {
            final Message msg = Message.obtain(this);

            msg.what = MSG_RECOGNIZE_BEGIN_SPEECH;

            msg.sendToTarget();
        }

        public void notifyRecognizeEndOfSpeech() {
            final Message msg = Message.obtain(this);

            msg.what = MSG_RECOGNIZE_END_SPEECH;

            msg.sendToTarget();
        }

        public void notifyRecognizeResult(String result, boolean isLast) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_RECOGNIZE_RESULT;
            msg.arg1 = isLast ? 1 : 0;
            msg.obj = result;

            msg.sendToTarget();
        }

        public void notifyRecognizeListenStatus(boolean isListening) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_RECOGNIZE_STATUS;
            msg.arg1 = isListening ? 1 : 0;

            msg.sendToTarget();
        }

        public void notifyRecognizeError(int errorCode) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_RECOGNIZE_ERROR;
            msg.arg1 = errorCode;

            msg.sendToTarget();
        }

        public void notifyUnderstandVolumeChanged(int volume) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_UNDERSTAND_VOLUME_CHANGED;
            msg.arg1 = volume;

            msg.sendToTarget();
        }

        public void notifyUnderstandBeginOfSpeech() {
            final Message msg = Message.obtain(this);

            msg.what = MSG_UNDERSTAND_BEGIN_SPEECH;

            msg.sendToTarget();
        }

        public void notifyUnderstandEndOfSpeech() {
            final Message msg = Message.obtain(this);

            msg.what = MSG_UNDERSTAND_END_SPEECH;

            msg.sendToTarget();
        }

        public void notifyUnderstandResult(RemoteBusiness result) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_UNDERSTAND_RESULT;
            msg.obj = result;

            msg.sendToTarget();
        }

        public void notifyUnderstandListenerStatus(boolean isListening) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_UNDERSTAND_STATUS;
            msg.arg1 = isListening ? 1 : 0;

            msg.sendToTarget();
        }

        public void notifyUnderstandError(int errorCode) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_UNDERSTAND_ERROR;
            msg.arg1 = errorCode;

            msg.sendToTarget();
        }

        public void notifySynthesizeSpeakBegin() {
            final Message msg = Message.obtain(this);

            msg.what = MSG_SYNTHESISE_SPEAK_BEGIN;

            msg.sendToTarget();
        }

        public void notifySynthesizeSpeakPaused() {
            final Message msg = Message.obtain(this);

            msg.what = MSG_SYNTHESISE_SPEAK_PAUSED;

            msg.sendToTarget();
        }

        public void notifySynthesizeSpeakResumed() {
            final Message msg = Message.obtain(this);

            msg.what = MSG_SYNTHESISE_SPEAK_RESUMED;

            msg.sendToTarget();
        }

        public void notifySynthesizeSpeakCompleted(int errorCode) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_SYNTHESISE_COMPLETE;
            msg.arg1 = errorCode;

            msg.sendToTarget();
        }

        public void notifySynthesizeSpeakProgress(int progress) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_SYNTHESISE_SPEAK_PROGRESS_CHANGED;
            msg.arg1 = progress;

            msg.sendToTarget();
        }

        public void notifySynthesizeBufferProgress(int progress) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_SYNTHESISE_BUFFER_PROGRESS_CHANGED;
            msg.arg1 = progress;

            msg.sendToTarget();
        }

        public void notifySynthesizeData(byte[] buffer) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_SYNTHESISE_SPEAK_DATA;
            msg.obj = buffer;

            msg.sendToTarget();
        }

        public void notifySythesizeSpeakStatus(boolean isSpeaking) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_SYNTHESISE_SPEAK_STATUS;
            msg.arg1 = isSpeaking ? 1 : 0;

            msg.sendToTarget();
        }

        public void notifySynthesizeError(int errorCode) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_SYNTHESISE_SPEAK_ERROR;
            msg.arg1 = errorCode;

            msg.sendToTarget();
        }

        public void notifyLexiconUpdated(String lexiconId, int errorCode) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_LEXICON_UPDATED;
            msg.arg1 = errorCode;
            msg.obj = lexiconId;

            msg.sendToTarget();
        }

        public void notifyGrammerBuildFinish(String grammarId, int errorCode) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_GRAMMAR_BUILDED;
            msg.arg1 = errorCode;
            msg.obj = grammarId;

            msg.sendToTarget();
        }

        public void handleRequest(RemoteSpeechRequest request) {
            final Message msg = Message.obtain(this);

            switch (request.type) {
            case RemoteSpeechRequest.TYPE_RECOGNIZE_APPEND_DATA:
                msg.what = MSG_REQUEST_RECOGNIZE_APPEND_DATA;
                msg.arg1 = request.timeStamp;
                msg.obj = request.buffer;
                break;

            case RemoteSpeechRequest.TYPE_RECOGNIZE_START_LISTEN:
                msg.what = MSG_REQUEST_RECOGNIZE_START_LISTEN;
                msg.obj = request.parameters;
                break;

            case RemoteSpeechRequest.TYPE_RECOGNIZE_LISTEN_STATUS:
                msg.what = MSG_REQUEST_RECOGNIZE_LISTEN_STATUS;
                break;

            case RemoteSpeechRequest.TYPE_RECOGNIZE_STOP_LISTEN:
                msg.what = MSG_REQUEST_RECOGNIZE_STOP_LISTEN;
                break;

            case RemoteSpeechRequest.TYPE_RECOGNIZE_CANCEL_LISTEN:
                msg.what = MSG_REQUEST_RECOGNIZE_CANCEL_LISTEN;
                break;

            case RemoteSpeechRequest.TYPE_UNDERSTAND_APPEND_DATA:
                msg.what = MSG_REQUEST_UNDERSTAND_APPEND_DATA;
                msg.arg1 = request.timeStamp;
                msg.obj = request.buffer;
                break;

            case RemoteSpeechRequest.TYPE_UNDERSTAND_START_LISTEN:
                msg.what = MSG_REQUEST_UNDERSTAND_START_LISTEN;
                msg.obj = request.parameters;
                break;

            case RemoteSpeechRequest.TYPE_UNDERSTAND_LISTEN_STATUS:
                msg.what = MSG_REQUEST_UNDERSTAND_LISTEN_STATUS;
                break;

            case RemoteSpeechRequest.TYPE_UNDERSTAND_STOP_LISTEN:
                msg.what = MSG_REQUEST_UNDERSTAND_STOP_LISTEN;
                break;

            case RemoteSpeechRequest.TYPE_UNDERSTAND_CANCEL_LISTEN:
                msg.what = MSG_REQUEST_UNDERSTAND_CANCEL_LISTEN;
                break;

            case RemoteSpeechRequest.TYPE_SYNTHESISE_START_SPEAK:
                msg.what = MSG_REQUEST_SYNTHESISE_START_SPEAK;
                msg.obj = new SpeakArgs(request.parameters, request.text);
                break;

            case RemoteSpeechRequest.TYPE_SYNTHESISE_PAUSE_SPEAK:
                msg.what = MSG_REQUEST_SYNTHESISE_PAUSE_SPEAK;
                break;

            case RemoteSpeechRequest.TYPE_SYNTHESISE_RESUME_SPEAK:
                msg.what = MSG_REQUEST_SYNTHESISE_RESUME_SPEAK;
                break;

            case RemoteSpeechRequest.TYPE_SYNTHESISE_STOP_SPEAK:
                msg.what = MSG_REQUEST_SYNTHESISE_STOP_SPEAK;
                break;

            case RemoteSpeechRequest.TYPE_SYNTHESISE_SPEAK_STATUS:
                msg.what = MSG_REQUEST_SYNTHESISE_SPEAK_STATUS;
                break;

            default:
                IwdsAssert.dieIf(this, true, "unknown request type "
                        + request.type);
                break;
            }

            msg.sendToTarget();
        }

        @Override
        public void handleMessage(Message msg) {
            RemoteSpeechResponse response = RemoteSpeechResponse
                    .obtain(m_transactor);
            switch (msg.what) {
            case MSG_CHANNEL_STATUS_CHANGED:
                m_channelAvailable = msg.arg1 == 1;
                if (!m_channelAvailable) {
                    IwdsLog.i(this, "Channgel is unavailable");

                    disconnectService();

                    m_serviceConnectStatus = RemoteSpeechErrorCode.ERROR_REMOTE_DISCONNECTED;

                } else {
                    IwdsLog.i(this, "Channel is available");

                    connectService();
                }

                break;

            case MSG_SPEECH_SERVICE_CONNECTED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                if (m_serviceConnectStatus == msg.arg1)
                    return;

                m_serviceConnectStatus = msg.arg1;

                response.type = RemoteSpeechResponse.TYPE_SPEECH_SERVICE_STATUS;
                response.errorCode = msg.arg1;

                IwdsLog.d(this,
                        "Notify speech service connected with errorCode "
                                + response.errorCode);

                response.sendToRemote();
                break;

            case MSG_REQUEST_RECOGNIZE_START_LISTEN:
                IwdsLog.d(this,
                        "Request recognize start listen with parameters "
                                + (HashMap<String, String>) msg.obj);

                m_recognizer.startListening((HashMap<String, String>) msg.obj);
                break;

            case MSG_REQUEST_RECOGNIZE_APPEND_DATA:
                if (!m_recognizer.isModuleStart()
                        || !m_recognizer.isListening(false))
                    break;

                IwdsLog.d(this,
                        "Request recognize append data with buffer size "
                                + ((byte[]) msg.obj).length + ", time stamp "
                                + msg.arg1 + "ms");

                m_recognizer.appendRecoData(msg.arg1, (byte[]) msg.obj);
                break;

            case MSG_REQUEST_RECOGNIZE_STOP_LISTEN:
                IwdsLog.d(this, "Request recognize stop listen");

                m_recognizer.stopListening(true);
                break;

            case MSG_REQUEST_RECOGNIZE_LISTEN_STATUS:
                IwdsLog.d(this, "Request recognize listen status");

                m_recognizer.isListening(true);
                break;

            case MSG_REQUEST_RECOGNIZE_CANCEL_LISTEN:
                IwdsLog.d(this, "Request recognize cancel listen");

                m_recognizer.cancel(true);
                break;

            case MSG_REQUEST_UNDERSTAND_START_LISTEN:
                IwdsLog.d(this,
                        "Request understand start listen with parameters "
                                + (HashMap<String, String>) msg.obj);

                m_understander
                        .startUnderstanding((HashMap<String, String>) msg.obj);
                break;

            case MSG_REQUEST_UNDERSTAND_APPEND_DATA:
                if (!m_understander.isModuleStart()
                        || !m_understander.isUnderstanding(false))
                    return;

                IwdsLog.d(this,
                        "Request understand append data with buffer size "
                                + ((byte[]) msg.obj).length + ", time stamp "
                                + msg.arg1 + "ms");

                m_understander.appendRecoData(msg.arg1, (byte[]) msg.obj);
                break;

            case MSG_REQUEST_UNDERSTAND_STOP_LISTEN:
                IwdsLog.d(this, "Request understand stop listen");

                m_understander.stopUnderstanding(true);
                break;

            case MSG_REQUEST_UNDERSTAND_LISTEN_STATUS:
                IwdsLog.d(this, "Request understand listen status");

                m_understander.isUnderstanding(true);
                break;

            case MSG_REQUEST_UNDERSTAND_CANCEL_LISTEN:
                IwdsLog.d(this, "Request understand cancel listen");

                m_understander.cancel(true);
                break;

            case MSG_REQUEST_SYNTHESISE_START_SPEAK:
                SpeakArgs args = (SpeakArgs) msg.obj;
                IwdsLog.d(this, "Request start speak " + args.text
                        + " with parameter " + args.parameters);

                m_synthesizer.startSpeaking(args.parameters, args.text);
                break;

            case MSG_REQUEST_SYNTHESISE_PAUSE_SPEAK:
                IwdsLog.d(this, "Request pause speak");

                m_synthesizer.pauseSpeaking();
                break;

            case MSG_REQUEST_SYNTHESISE_RESUME_SPEAK:
                IwdsLog.d(this, "Request resume speak");

                m_synthesizer.resumeSpeaking();
                break;

            case MSG_REQUEST_SYNTHESISE_STOP_SPEAK:
                IwdsLog.d(this, "Request stop speak");

                m_synthesizer.stopSpeaking(true);
                break;

            case MSG_REQUEST_SYNTHESISE_SPEAK_STATUS:
                IwdsLog.d(this, "Request speak status");

                m_synthesizer.isSpeaking(true);
                break;

            case MSG_CONNECT_IFLYTEK_SERVICE:
                if (!m_channelAvailable)
                    return;

                if (SpeechUtility.isServiceInstalled(m_context,
                        COMPONENT_RECOGNIZER)
                        && SpeechUtility.isServiceInstalled(m_context,
                                COMPONENT_SYNTHESIZER)
                        && (SpeechUtility.isServiceInstalled(m_context,
                                COMPONENT_UNDERSTANDER))) {

                    if (m_recognizer == null
                            || m_recognizer.isModuleStopUnexpected()) {
                        IwdsLog.d(this, "Connect Recogniztion module");
                        m_recognizer = null;
                        m_recognizer = new Recognizer();
                    }

                    if (m_understander == null
                            || m_understander.isModuleStopUnexpected()) {
                        IwdsLog.d(this, "Connect Understand module");
                        m_understander = null;
                        m_understander = new Understander();
                    }

                    if (m_synthesizer == null
                            || m_synthesizer.isModuleStopUnexpected()) {
                        IwdsLog.d(this, "Connect Synthesise module");
                        m_synthesizer = null;
                        m_synthesizer = new Synthesizer();
                    }

                    System.gc();
                } else {
                    notifySpeechServiceConnected(RemoteSpeechErrorCode.ERROR_COMPONENT_NOT_INSTALLED);
                }

                break;

            case MSG_DISCONNECT_IFLYTEK_SERVICE:
                if (m_recognizer != null && m_recognizer.isModuleStart()) {
                    if (m_recognizer.isListening(false))
                        m_recognizer.cancel(false);
                    m_recognizer.destory();
                    m_recognizer = null;
                    IwdsLog.d(this, "Disonnect Recogniztion module");
                }

                if (m_understander != null && m_understander.isModuleStart()) {
                    if (m_understander.isUnderstanding(false))
                        m_understander.cancel(false);
                    m_understander.destory();
                    m_understander = null;
                    IwdsLog.d(this, "Disconnect Understand module");
                }

                if (m_synthesizer != null && m_synthesizer.isModuleStart()) {
                    if (m_synthesizer.isSpeaking(false))
                        m_synthesizer.stopSpeaking(false);
                    m_synthesizer.destory();
                    m_synthesizer = null;
                    IwdsLog.d(this, "Disconnect Synthesise module");
                }

                System.gc();
                break;

            case MSG_RECOGNIZE_ERROR:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_RECOGNIZE_ERROR;
                response.errorCode = msg.arg1;

                IwdsLog.d(this, "Notify recognizer error with code "
                        + response.errorCode);

                response.sendToRemote();
                break;

            case MSG_RECOGNIZE_STATUS:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_RECOGNIZE_STATUS;
                response.recognizeStatus = msg.arg1 == 1;

                IwdsLog.d(this, "Notify recognizer listen status "
                        + response.recognizeStatus);

                response.sendToRemote();
                break;

            case MSG_RECOGNIZE_VOLUME_CHANGED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_RECOGNIZE_VOLUME_CHANGED;
                response.recognizeVolume = msg.arg1;

                IwdsLog.d(this, "Notify recognizer volume changed "
                        + response.recognizeVolume);

                response.sendToRemote();
                break;

            case MSG_RECOGNIZE_BEGIN_SPEECH:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_RECOGNIZE_BEGIN_SPEECH;

                IwdsLog.d(this, "Notify recognizer begin speech");

                response.sendToRemote();
                break;

            case MSG_RECOGNIZE_END_SPEECH:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_RECOGNIZE_END_SPEECH;

                IwdsLog.d(this, "Notify recognizer end speech");

                response.sendToRemote();
                break;

            case MSG_RECOGNIZE_RESULT:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_RECOGNIZE_RESULT;

                response.recognizeResult = (String) msg.obj;
                response.recognizeLast = msg.arg1 == 1;

                IwdsLog.d(this, "Notify recognizer result "
                        + response.recognizeResult + " isLast "
                        + response.recognizeLast);

                response.sendToRemote();
                break;

            case MSG_UNDERSTAND_ERROR:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_UNDERSTAND_ERROR;
                response.errorCode = msg.arg1;

                IwdsLog.d(this, "Notify understander error with code "
                        + response.errorCode);

                response.sendToRemote();
                break;

            case MSG_UNDERSTAND_STATUS:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_UNDERSTAND_STATUS;
                response.understandStatus = msg.arg1 == 1;

                IwdsLog.d(this, "Notify understander listen status "
                        + response.understandStatus);

                response.sendToRemote();
                break;

            case MSG_UNDERSTAND_VOLUME_CHANGED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_UNDRESTAND_VOLUME_CHANGED;
                response.understandVolume = msg.arg1;

                IwdsLog.d(this, "Notify understander volume changed "
                        + response.understandVolume);

                response.sendToRemote();
                break;

            case MSG_UNDERSTAND_BEGIN_SPEECH:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_UNDERSTAND_BEGIN_SPEECH;

                IwdsLog.d(this, "Notify understander begin speech");

                response.sendToRemote();
                break;

            case MSG_UNDERSTAND_END_SPEECH:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_UNDERSTAND_END_SPEECH;

                IwdsLog.d(this, "Notify understander end speech");

                response.sendToRemote();
                break;

            case MSG_UNDERSTAND_RESULT:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_UNDERSTAND_RESULT;
                response.understandResult = (RemoteBusiness) msg.obj;

                IwdsLog.d(this, "Notify understander result "
                        + response.understandResult);

                response.sendToRemote();
                break;

            case MSG_SYNTHESISE_BUFFER_PROGRESS_CHANGED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_SYNTHESISE_BUFFER_PROGRESS_CHANGED;
                response.bufferProgress = msg.arg1;

                response.sendToRemote();
                break;

            case MSG_SYNTHESISE_COMPLETE:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_SYNTHESISE_COMPLETE;
                response.errorCode = msg.arg1;

                response.sendToRemote();
                break;

            case MSG_SYNTHESISE_SPEAK_BEGIN:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_SYNTHESISE_SPEAK_BEGIN;

                response.sendToRemote();
                break;

            case MSG_SYNTHESISE_SPEAK_PAUSED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_SYNTHESISE_SPEAK_PAUSED;

                response.sendToRemote();
                break;

            case MSG_SYNTHESISE_SPEAK_PROGRESS_CHANGED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_SYNTHESISE_SPEAK_PROGRESS_CHANGED;
                response.speakProgress = msg.arg1;

                response.sendToRemote();
                break;

            case MSG_SYNTHESISE_SPEAK_RESUMED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_SYNTHESISE_SPEAK_RESUMED;

                response.sendToRemote();
                break;

            case MSG_SYNTHESISE_SPEAK_STATUS:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_SYNTHESISE_SPEAK_STATUS;
                response.speakStatus = msg.arg1 == 1;

                response.sendToRemote();
                break;

            case MSG_SYNTHESISE_SPEAK_ERROR:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_SYNTHESISE_SPEAK_ERROR;
                response.errorCode = msg.arg1;

                response.sendToRemote();
                break;

            case MSG_SYNTHESISE_SPEAK_DATA:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_SYNTHESISE_SPEAK_DATA;
                response.audioBuffer = (byte[]) msg.obj;

                response.sendToRemote();
                break;

            case MSG_LEXICON_UPDATED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_LEXICON_UPDATED;
                response.lexiconId = msg.arg1;

                response.sendToRemote();
                break;

            case MSG_GRAMMAR_BUILDED:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSpeechResponse.TYPE_GRAMMAR_BUILDED;
                response.grammarId = msg.arg1;

                response.sendToRemote();
                break;

            default:
                IwdsAssert.dieIf(this, true, "Unknown message");
                break;
            }
        }
    }

    /* ---------------------- DataTransactorCallback -------------------------- */
    private DataTransactorCallback m_transportCallback = new DataTransactorCallback() {

        @Override
        public void onLinkConnected(DeviceDescriptor descriptor,
                boolean isConnected) {
            // do not care
        }

        @Override
        public void onChannelAvailable(boolean isAvailable) {
            m_handler.setChannelState(isAvailable);
        }

        @Override
        public void onSendResult(DataTransactResult result) {
            // do not care
        }

        @Override
        public void onDataArrived(Object object) {
            if (object instanceof RemoteSpeechRequest)
                m_handler.handleRequest((RemoteSpeechRequest) object);
        }

        @Override
        public void onSendFileProgress(int progress) {
            // do not care
        }

        @Override
        public void onRecvFileProgress(int progress) {
            // do not care
        }

        @Override
        public void onSendFileInterrupted(int index) {

        }

        @Override
        public void onRecvFileInterrupted(int index) {

        }

    };
}
