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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback;
import com.ingenic.iwds.datatransactor.ParcelTransactor;
import com.ingenic.iwds.smartspeech.AudioRecorder.AudioRecordInitListener;
import com.ingenic.iwds.smartspeech.AudioRecorder.AudioRecordListener;
import com.ingenic.iwds.smartspeech.AudioTracker.AudioTrackInitListener;
import com.ingenic.iwds.smartspeech.AudioTracker.AudioTrackListener;
import com.ingenic.iwds.smartspeech.business.RemoteBusiness;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;

public class RemoteSpeechService extends Service {
    private RemoteSpeechServiceStub m_service = new RemoteSpeechServiceStub(
            this);
    private Context m_context;
    private ParcelTransactor<RemoteSpeechResponse> m_transactor;
    private ServiceHandler m_handler;

    private AudioRecordTransport m_recordTransport;
    private AudioTrackTransport m_trackTransport;
    private RemoteStatusCallback m_remoteStatusCallback = new RemoteStatusCallback();

    @Override
    public void onCreate() {
        IwdsLog.d(this, "onCreate");
        super.onCreate();

        m_context = getBaseContext();
        m_transactor = new ParcelTransactor<RemoteSpeechResponse>(this,
                RemoteSpeechResponse.CREATOR, m_transportCallback,
                "c1dc19e2-17a4-0797-3333-68a0dd4bfb68");

        m_handler = new ServiceHandler();
    }

    @Override
    public void onDestroy() {
        IwdsLog.d(this, "onDestroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        IwdsLog.d(this, "onBind");

        m_transactor.start();

        return m_service;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        IwdsLog.d(this, "onUnbind");

        m_transactor.stop();

        return super.onUnbind(intent);
    }

    private static class RemoteSpeechServiceStub extends
            IRemoteSpeechService.Stub {
        private RemoteSpeechService m_internalService;

        public RemoteSpeechServiceStub(RemoteSpeechService service) {
            m_internalService = service;
        }

        @Override
        public void registerRemoteStatusListener(String uuid,
                IRemoteStatusCallback callback) throws RemoteException {
            IwdsLog.d(this, "registerRemoteStatusListener called by: "
                    + callback.asBinder());

            m_internalService.m_handler.registerRemoteStatusListener(uuid,
                    callback);
        }

        @Override
        public void unregisterRemoteStatusListener(String uuid)
                throws RemoteException {
            m_internalService.m_handler.unregisterRemoteStatusListener(uuid);
        }

        @Override
        public void requestRecognizeStatus(IRemoteRecognizerCallback callback)
                throws RemoteException {
            IwdsLog.d(this,
                    "requestRecognizeStatus called by: " + callback.asBinder());

            m_internalService.m_handler.requestRecognizeStatus(callback);
        }

        @Override
        public void requestStartRecognize(IRemoteRecognizerCallback callback,
                Map parameters) throws RemoteException {
            IwdsLog.d(this,
                    "requestStartReconize called by: " + callback.asBinder());

            m_internalService.m_handler.requestStartRecognizeRecord(callback,
                    (HashMap<String, String>) parameters);
        }

        @Override
        public void requestStopRecognize(IRemoteRecognizerCallback callback)
                throws RemoteException {
            IwdsLog.d(this,
                    "requestStopRecognize called by: " + callback.asBinder());

            m_internalService.m_handler.requestStopRecognize(callback);
        }

        @Override
        public void requestCancelRecognize(IRemoteRecognizerCallback callback)
                throws RemoteException {
            IwdsLog.d(this,
                    "requestCancelRecognize called by: " + callback.asBinder());

            m_internalService.m_handler.requestCancelRecognize(callback);
        }

        @Override
        public void requestCancelUnderstand(IRemoteUnderstanderCallback callback)
                throws RemoteException {
            IwdsLog.d(this,
                    "requestCancelUnderstand called by: " + callback.asBinder());

            m_internalService.m_handler.requestCancelUnderstand(callback);
        }

        @Override
        public void requestStartUnderstand(
                IRemoteUnderstanderCallback callback, Map parameters)
                throws RemoteException {
            IwdsLog.d(this,
                    "requestStartUnderstand called by: " + callback.asBinder());

            m_internalService.m_handler.requestStartUnderstandRecord(callback,
                    (HashMap<String, String>) parameters);
        }

        @Override
        public void requestStopUnderstand(IRemoteUnderstanderCallback callback)
                throws RemoteException {
            IwdsLog.d(this,
                    "requestStopUnderstand called by: " + callback.asBinder());

            m_internalService.m_handler.requestStopUnderstand(callback);
        }

        @Override
        public void requestUnderstandStatus(IRemoteUnderstanderCallback callback)
                throws RemoteException {
            IwdsLog.d(this,
                    "requestUnderstandStatus called by: " + callback.asBinder());

            m_internalService.m_handler.requestUnderstandStatus(callback);
        }

        @Override
        public void requestStartSpeak(Map parameters, String text,
                IRemoteSynthesizerCallback callback) throws RemoteException {
            IwdsLog.d(this,
                    "requestStartSpeak called by: " + callback.asBinder());

            m_internalService.m_handler.requestSynthStartSpeak(
                    (HashMap<String, String>) parameters, text, callback);
        }

        @Override
        public void requestCancelSpeak(IRemoteSynthesizerCallback callback)
                throws RemoteException {
            IwdsLog.d(this,
                    "requestCancelSpeak called by: " + callback.asBinder());

            m_internalService.m_handler.requestCancelSpeak(callback);
        }

        @Override
        public void requestPauseSpeak(IRemoteSynthesizerCallback callback)
                throws RemoteException {
            IwdsLog.d(this,
                    "requestPauseSpeak called by: " + callback.asBinder());

            m_internalService.m_handler.requestPauseSpeak(callback);
        }

        @Override
        public void requestResumeSpeak(IRemoteSynthesizerCallback callback)
                throws RemoteException {
            IwdsLog.d(this,
                    "requestResumeSpeak called by: " + callback.asBinder());

            m_internalService.m_handler.requestResumeSpeak(callback);
        }

        @Override
        public void requestSpeakStatus(IRemoteSynthesizerCallback callback)
                throws RemoteException {
            IwdsLog.d(this,
                    "requestSpeakStatus called by: " + callback.asBinder());

            m_internalService.m_handler.requestSpeakStatus(callback);
        }
    }

    private class RemoteStatusCallback implements IBinder.DeathRecipient {
        private HashMap<String, IRemoteStatusCallback> m_listeners;

        RemoteStatusCallback() {
            m_listeners = new HashMap<String, IRemoteStatusCallback>();
        }

        public void registerRemoteStatusListener(String uuid,
                IRemoteStatusCallback callback) {

            try {
                callback.asBinder().linkToDeath(this, 0);

                m_listeners.put(uuid, callback);

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in registerRemoteStatusListener: "
                        + e.toString());
            }
        }

        public void onRemoteStatusChanged(int status) {
            Collection<IRemoteStatusCallback> callbacks = m_listeners.values();

            for (IRemoteStatusCallback cb : callbacks) {
                try {
                    cb.onAvailable(status);
                } catch (RemoteException e) {
                    IwdsLog.e(
                            this,
                            "Exception in onRemoteStatusChanged: "
                                    + e.toString());
                }
            }
        }

        public void unregisterRemoteStatusListener(String uuid) {
            IRemoteStatusCallback callback = m_listeners.remove(uuid);

            if (callback == null)
                return;

            callback.asBinder().unlinkToDeath(this, 0);
        }

        @Override
        public void binderDied() {
            Set<String> uuids = m_listeners.keySet();
            for (Iterator<String> it = uuids.iterator(); it.hasNext();) {
                String uuid = it.next();
                if (!m_listeners.get(uuid).asBinder().isBinderAlive())
                    it.remove();
            }
        }
    }

    private class RemoteSpeechArgs<T extends IInterface> {
        private T callback;
        private HashMap<String, String> parameters;
        private String text;

        public RemoteSpeechArgs(T callback, HashMap<String, String> parameters) {
            this.callback = callback;
            this.parameters = parameters;
        }

        public RemoteSpeechArgs(T callback, HashMap<String, String> parameters,
                String text) {
            this.callback = callback;
            this.parameters = parameters;
            this.text = text;
        }

        public RemoteSpeechArgs(HashMap<String, String> parameters, String text) {
            this.parameters = parameters;
            this.text = text;
        }
    }

    private void handleStartRecognizeRecord(IRemoteRecognizerCallback callback,
            HashMap<String, String> parameters) {
        if (m_recordTransport == null) {
            IwdsLog.i(this, "Created new m_recordTransport, listener="
                    + callback.asBinder());

            m_recordTransport = new AudioRecordTransport(callback, parameters);

        } else if (m_recordTransport.m_recognizeListener.asBinder() == callback
                .asBinder()) {

            IwdsLog.i(this, "restart recognizer");

            m_recordTransport.restartRecognizer();

        } else {
            try {
                IwdsLog.i(this, "recognizer(ASR) module busy");

                callback.onError(RemoteSpeechErrorCode.ERROR_AUDIO_RECORDER_BUSY);

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in onError: " + e.toString());
            }
        }
    }

    private void handleStartUnderstandRecord(
            IRemoteUnderstanderCallback callback,
            HashMap<String, String> parameters) {
        if (m_recordTransport == null) {
            IwdsLog.i(this, "Created new m_recordTransport, listener="
                    + callback.asBinder());

            m_recordTransport = new AudioRecordTransport(callback, parameters);

        } else if (m_recordTransport.m_understandListener.asBinder() == callback
                .asBinder()) {

            IwdsLog.i(this, "restart understander");

            m_recordTransport.restartUnderstander();

        } else {
            try {
                IwdsLog.e(this, "understander(NLU) module busy");

                callback.onError(RemoteSpeechErrorCode.ERROR_AUDIO_RECORDER_BUSY);

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in onError: " + e.toString());
            }
        }
    }

    private boolean checkRecognizeClientListener(
            IRemoteRecognizerCallback callback) {
        try {
            if (m_recordTransport == null) {
                IwdsLog.e(this, "recognizer(ASR) module could not find client");

                callback.onError(RemoteSpeechErrorCode.ERROR_CLIENT);

                return false;

            } else if (m_recordTransport.m_recognizeListener.asBinder() != callback
                    .asBinder()) {

                IwdsLog.e(this, "recognizer(ASR) module busy");

                callback.onError(RemoteSpeechErrorCode.ERROR_AUDIO_RECORDER_BUSY);

                return false;
            }

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in onError: " + e.toString());

            return false;
        }

        return true;
    }

    private boolean checkUnderstandClientListener(
            IRemoteUnderstanderCallback callback) {
        try {
            if (m_recordTransport == null) {
                IwdsLog.e(this,
                        "understander(NLU) module could not find client");

                callback.onError(RemoteSpeechErrorCode.ERROR_CLIENT);

                return false;

            } else if (m_recordTransport.m_understandListener.asBinder() != callback
                    .asBinder()) {

                IwdsLog.e(this, "understander(NLU) module busy");

                callback.onError(RemoteSpeechErrorCode.ERROR_AUDIO_RECORDER_BUSY);

                return false;
            }

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in onError: " + e.toString());

            return false;
        }

        return true;
    }

    private class AudioRecordTransport implements AudioRecordListener,
            IBinder.DeathRecipient, AudioRecordInitListener {
        private AudioRecorder m_audioRecorder;

        private IRemoteRecognizerCallback m_recognizeListener;
        private IRemoteUnderstanderCallback m_understandListener;

        private HashMap<String, String> m_parameters;

        private boolean isForceResetingRecorder = false;
        private long m_recordStartTime;

        private AudioRecordTransport(IRemoteRecognizerCallback listener,
                HashMap<String, String> parameters) {
            m_recognizeListener = listener;
            m_parameters = parameters;
            m_understandListener = null;

            try {
                m_recognizeListener.asBinder().linkToDeath(this, 0);

                m_audioRecorder = AudioRecorder.getInstance();
                m_audioRecorder.setRecordListener(this);
                m_audioRecorder.initialize(this);

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in create AudioRecordTransport: "
                        + e.toString());
            }
        }

        private AudioRecordTransport(IRemoteUnderstanderCallback listener,
                HashMap<String, String> parameters) {
            m_understandListener = listener;
            m_parameters = parameters;
            m_recognizeListener = null;

            try {
                m_understandListener.asBinder().linkToDeath(this, 0);

                m_audioRecorder = AudioRecorder.getInstance();
                m_audioRecorder.setRecordListener(this);
                m_audioRecorder.initialize(this);

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in create AudioRecordTransport: "
                        + e.toString());
            }
        }

        public void restartRecognizer() {
            isForceResetingRecorder = true;
            m_handler.requestStopRecognize(m_recognizeListener);
            m_audioRecorder.resetRecord();
        }

        public void restartUnderstander() {
            isForceResetingRecorder = true;
            m_handler.requestStopUnderstand(m_understandListener);
            m_audioRecorder.resetRecord();
        }

        public void destroy() {
            m_audioRecorder.releaseRecord();
        }

        /* ------------- Iflytek callback ------------- */
        private void onBeginOfSpeech() {
            try {
                if (m_recognizeListener != null)
                    m_recognizeListener.onBeginOfSpeech();

                else if (m_understandListener != null)
                    m_understandListener.onBeginOfSpeech();

                else
                    IwdsAssert
                            .dieIf(this, true,
                                    "m_recognizeListener is null and m_understandListener is null");

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in onBeginOfSpeech: " + e.toString());
            }
        }

        private void onEndOfSpeech() {
            if (!isForceResetingRecorder) {
                IwdsLog.i(this, "End of speech event, stop AudioRecorder");

                m_audioRecorder.stopRecord();

            } else {
                isForceResetingRecorder = false;
                IwdsLog.i(this,
                        "Force reset AudioRecorder before, ignore this end of speech event");
            }

            try {
                if (m_recognizeListener != null)
                    m_recognizeListener.onEndOfSpeech();

                else if (m_understandListener != null)
                    m_understandListener.onEndOfSpeech();

                else
                    IwdsAssert
                            .dieIf(this, true,
                                    "m_recognizeListener is null and m_understandListener is null");

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in onEndOfSpeech: " + e.toString());
            }
        }

        public void onError(int errorCode) {
            m_handler.destroyAudioRecorder();

            try {
                if (m_recognizeListener != null)
                    m_recognizeListener.onError(errorCode);

                else if (m_understandListener != null)
                    m_understandListener.onError(errorCode);

                else
                    IwdsAssert
                            .dieIf(this, true,
                                    "m_recognizeListener is null and m_understandListener is null");

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in onError: " + e.toString());
            }
        }

        public void onRecognizeResult(String result, boolean isLast) {
            if (isLast)
                m_handler.destroyAudioRecorder();

            try {
                if (m_recognizeListener != null)
                    m_recognizeListener.onResult(result, isLast);

                else
                    IwdsAssert.dieIf(this, true, "m_recognizeListener is null");

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in onResult: " + e.toString());
            }
        }

        public void onUnderstandResult(RemoteBusiness result) {
            m_handler.destroyAudioRecorder();

            try {
                if (m_understandListener != null)
                    m_understandListener.onResult(result);

                else
                    IwdsAssert
                            .dieIf(this, true, "m_understandListener is null");

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in onResult: " + e.toString());
            }
        }

        public void onVolumChanged(int volume) {
            try {
                if (m_recognizeListener != null)
                    m_recognizeListener.onVolumeChanged(volume);

                else if (m_understandListener != null)
                    m_understandListener.onVolumeChanged(volume);

                else
                    IwdsAssert
                            .dieIf(this, true,
                                    "m_recognizeListener is null and m_understandListener is null");

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in onVolumChanged: " + e.toString());
            }
        }

        public void onListeningStatus(boolean isListening) {
            try {
                if (m_recognizeListener != null)
                    m_recognizeListener.onListeningStatus(isListening);

                else if (m_understandListener != null)
                    m_understandListener.onListeningStatus(isListening);

                else
                    IwdsAssert
                            .dieIf(this, true,
                                    "m_recognizeListener is null and m_understandListener is null");

            } catch (RemoteException e) {
                IwdsLog.e(this,
                        "Exception in onListeningStatus: " + e.toString());
            }
        }

        /* ------------- Iflytek callback end ------------- */

        private void sendBroadcast(int state) {
            Intent intent = new Intent(
                    RemoteSpeechServiceManager.ACTION_REMOTE_SPEECH_AUDIO_RECOED_STATUS_CHANGED);
            intent.putExtra("state", state);
            m_context.sendBroadcast(intent);
        }

        @Override
        public void onInitialize(int errorCode) {
            isForceResetingRecorder = false;

            if (errorCode == AudioRecorder.SUCCESS
                    && m_audioRecorder.getState() == AudioRecorder.S_INITIALIZED) {
                IwdsLog.i(this, "AudioRecorder initialize success");

                sendBroadcast(RemoteSpeechServiceManager.AudioRecordState.sInitialized);

                m_audioRecorder.startRecord();

            } else {
                try {
                    IwdsLog.e(this, "AudioRecorder initialize failure");

                    if (m_recognizeListener != null)
                        m_recognizeListener
                                .onError(RemoteSpeechErrorCode.ERROR_INIT_RECORDER);

                    else if (m_understandListener != null)
                        m_understandListener
                                .onError(RemoteSpeechErrorCode.ERROR_INIT_RECORDER);

                    else
                        IwdsAssert
                                .dieIf(this, true,
                                        "m_recognizeListener is null and m_understandListener is null");

                } catch (RemoteException e) {
                    IwdsLog.e(this, "Exception in onError: " + e.toString());

                }
            }
        }

        @Override
        public void onRecordStart() {
            IwdsLog.i(this, "AudioRecorder start recording");

            sendBroadcast(RemoteSpeechServiceManager.AudioRecordState.sRecording);

            m_recordStartTime = SystemClock.elapsedRealtime();

            if (m_recognizeListener != null)
                m_handler.requestRecognizeStartListen(m_parameters);

            else if (m_understandListener != null)
                m_handler.requestUnderstandStartListen(m_parameters);

            else
                IwdsAssert
                        .dieIf(this, true,
                                "m_recognizeListener is null and m_understandListener is null");
        }

        @Override
        public void onRecordData(byte[] buffer) {
            if (m_audioRecorder.getState() != AudioRecorder.S_RECORDING) {
                IwdsLog.i(this,
                        "AudioRecorder not on recording state, ignore new data");
                return;
            }

            int timeStamp = (int) (SystemClock.elapsedRealtime() - m_recordStartTime);

            if (m_recognizeListener != null)
                m_handler.requestRecognizeAppendData(buffer, timeStamp);

            else if (m_understandListener != null)
                m_handler.requestUnderstandAppendData(buffer, timeStamp);

            else
                IwdsAssert
                        .dieIf(this, true,
                                "m_recognizeListener is null and m_understandListener is null");
        }

        @Override
        public void onRecordStop() {
            IwdsLog.i(this, "AudioRecorder stop recording");
            sendBroadcast(RemoteSpeechServiceManager.AudioRecordState.sStoped);
            m_transactor.cancelAll();
        }

        @Override
        public void onRecordRelease() {
            IwdsLog.i(this, "AudioRecorder release");
            sendBroadcast(RemoteSpeechServiceManager.AudioRecordState.sReleased);

            try {
                if (m_recognizeListener != null) {
                    m_recognizeListener.onCancel();

                    if (m_recognizeListener.asBinder().isBinderAlive())
                        m_recognizeListener.asBinder().unlinkToDeath(this, 0);

                } else if (m_understandListener != null) {
                    m_understandListener.onCancel();

                    if (m_understandListener.asBinder().isBinderAlive())
                        m_understandListener.asBinder().unlinkToDeath(this, 0);

                } else {
                    IwdsAssert
                            .dieIf(this, true,
                                    "m_recognizeListener is null and m_understandListener is null");
                }

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in onRecordRelease: " + e.toString());
            }
        }

        @Override
        public void onRecordError(int errorCode) {
            m_handler.destroyAudioRecorder();

            int error = RemoteSpeechErrorCode.SUCCESS;

            IwdsLog.e(this, "AudioRecorder record error with code " + errorCode);

            switch (errorCode) {
            case AudioRecorder.ERROR_RECORD_INITIALIZE:
                error = RemoteSpeechErrorCode.ERROR_INIT_RECORDER;
                break;

            case AudioRecorder.ERROR_RECORD_START:
                error = RemoteSpeechErrorCode.ERROR_START_RECORDER;
                break;

            case AudioRecorder.ERROR_RECORD_RESET:
                error = RemoteSpeechErrorCode.ERROR_RESET_RECORDER;
                break;

            case AudioRecorder.ERROR_RECORD_STOP:
                error = RemoteSpeechErrorCode.ERROR_STOP_RECORDER;
                break;

            case AudioRecorder.ERROR_ILLEGAL_STATE:
                error = RemoteSpeechErrorCode.ERROR_ILLEGAL_STATE;
                break;

            default:
                IwdsAssert.dieIf(this, true,
                        "unknown AudioRecorder error code " + errorCode);
            }

            try {
                if (m_recognizeListener != null)
                    m_recognizeListener.onError(error);

                else if (m_understandListener != null)
                    m_understandListener.onError(error);

                else
                    IwdsAssert
                            .dieIf(this, true,
                                    "m_recognizeListener is null and m_understandListener is null");

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in onError: " + e.toString());
            }
        }

        @Override
        public void binderDied() {
            IwdsLog.i(this, "binderDied");

            if (m_recognizeListener != null
                    && !m_recognizeListener.asBinder().isBinderAlive())
                m_handler.requestCancelRecognize(m_recognizeListener);

            else if (m_understandListener != null
                    && !m_understandListener.asBinder().isBinderAlive())
                m_handler.requestCancelUnderstand(m_understandListener);
        }
    }

    private void handleStartSynthSpeak(IRemoteSynthesizerCallback callback,
            HashMap<String, String> parameters, String text) {
        if (m_trackTransport == null) {
            IwdsLog.i(this, "Created new m_trackTransport, listener="
                    + callback.asBinder());

            m_trackTransport = new AudioTrackTransport(callback, parameters,
                    text);

        } else {
            try {
                IwdsLog.e(this, "synthesizer(TTS) module busy");

                callback.onError(RemoteSpeechErrorCode.ERROR_AUDIO_TRACKER_BUSY);

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in onError: " + e.toString());
            }
        }
    }

    private boolean checkSynthesiseClientListener(
            IRemoteSynthesizerCallback callback) {
        try {
            if (m_trackTransport == null) {
                IwdsLog.e(this, "synthesizer(TTS) module could not find client");

                callback.onError(RemoteSpeechErrorCode.ERROR_CLIENT);

                return false;

            } else if (m_trackTransport.m_synthListener.asBinder() != callback
                    .asBinder()) {

                IwdsLog.e(this, "synthesizer(TTS) module busy");

                callback.onError(RemoteSpeechErrorCode.ERROR_AUDIO_TRACKER_BUSY);

                return false;
            }

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in onError: " + e.toString());

            return false;
        }

        return true;
    }

    private class AudioTrackTransport implements IBinder.DeathRecipient,
            AudioTrackInitListener, AudioTrackListener {

        private AudioTracker m_tracker;

        private HashMap<String, String> m_parameters;
        private IRemoteSynthesizerCallback m_synthListener;
        private String m_text;

        private AudioTrackTransport(IRemoteSynthesizerCallback listener,
                HashMap<String, String> parameters, String text) {
            m_synthListener = listener;
            m_parameters = parameters;
            m_text = text;

            try {
                m_synthListener.asBinder().linkToDeath(this, 0);

                m_tracker = AudioTracker.getInstance();
                m_tracker.setTrackListener(this);
                m_tracker.initialize(this);

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in create new AudioTrackTransport: "
                        + e.toString());
            }
        }

        public void waitSpeakComplete() {
            m_tracker.waitComplete();
        }

        public void pause() {
            m_tracker.pausePlay();
        }

        public void resume() {
            m_tracker.resumePlay();
        }

        public void destroy() {
            m_tracker.destroy();
        }

        public void getStatus() {
            boolean isSpeaking = m_tracker.getState() == AudioTracker.S_PLAYING;

            try {
                if (m_synthListener != null) {
                    m_synthListener.onSpeakingStatus(isSpeaking);

                } else {
                    IwdsAssert.dieIf(this, true, "m_synthListener is null");
                }

            } catch (RemoteException e) {
                IwdsLog.e(this,
                        "Exception in onSpeakingStatus: " + e.toString());
            }
        }

        /* ------------- Iflytek callback ------------- */
        public void onSpeakStatus(boolean isSpeaking) {
            try {
                if (m_synthListener != null) {
                    m_synthListener.onSpeakingStatus(isSpeaking);

                } else {
                    IwdsAssert.dieIf(this, true, "m_synthListener is null");
                }
            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in onSpeakStatus: " + e.toString());
            }
        }

        public void onError(int errorCode) {
            m_handler.destroyAudioTracker();

            try {
                if (m_synthListener != null) {
                    m_synthListener.onError(errorCode);

                } else {
                    IwdsAssert.dieIf(this, true, "m_synthListener is null");
                }
            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in onError: " + e.toString());
            }
        }

        public void onSynthBufferProgress(int progress) {
            try {
                if (m_synthListener != null) {
                    m_synthListener.onSynthBufferProgress(progress);

                } else {
                    IwdsAssert.dieIf(this, true, "m_synthListener is null");
                }

            } catch (RemoteException e) {
                IwdsLog.e(this,
                        "Exception in onSynthBufferProgress: " + e.toString());
            }
        }

        public void onSynthCompleted(int errorCode) {
            m_handler.waitAudioTackerComplete();

            try {
                if (m_synthListener != null) {
                    m_synthListener.onSynthCompleted(errorCode);

                } else {
                    IwdsAssert.dieIf(this, true, "m_synthListener is null");
                }

            } catch (RemoteException e) {
                IwdsLog.e(this,
                        "Exception in onSynthCompleted: " + e.toString());
            }
        }

        public void onSpeakBegin() {
            try {
                if (m_synthListener != null) {
                    m_synthListener.onSpeakBegin();

                } else {
                    IwdsAssert.dieIf(this, true, "m_synthListener is null");
                }

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in onSpeakBegin: " + e.toString());
            }
        }

        public void onSpeakPaused() {
            IwdsLog.i(this, "iflytek onSpeakPaused");
        }

        public void onSynthProgress(int progress) {
            try {
                if (m_synthListener != null) {
                    m_synthListener.onSynthProgress(progress);

                } else {
                    IwdsAssert.dieIf(this, true, "m_synthListener is null");
                }

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in onSynthProgress: " + e.toString());
            }
        }

        public void onSpeakResumed() {
            IwdsLog.i(this, "iflytek onSpeakResumed");
        }

        public void onSpeakData(byte[] buffer) {
            if (m_tracker.getState() == AudioTracker.S_PLAYING
                    || m_tracker.getState() == AudioTracker.S_PAUSED)
                m_tracker.writeData(buffer);
        }

        private void sendBroadcast(int state) {
            Intent intent = new Intent(
                    RemoteSpeechServiceManager.ACTION_REMOTE_SPEECH_AUDIO_TRACK_STATUS_CHANGED);
            intent.putExtra("state", state);
            m_context.sendBroadcast(intent);
        }

        /* ------------- Iflytek callback end ------------- */

        @Override
        public void binderDied() {
            IwdsLog.i(this, "binderDied");

            if (m_synthListener != null
                    && !m_synthListener.asBinder().isBinderAlive()) {
                m_handler.requestCancelSpeak(m_synthListener);
            }
        }

        @Override
        public void onInitialize(int errorCode) {
            try {
                if (errorCode == AudioTracker.SUCCESS
                        && m_tracker.getState() == AudioTracker.S_INITIALIZED) {
                    IwdsLog.e(this, "AudioTracker initialize success");

                    sendBroadcast(RemoteSpeechServiceManager.AudioTrackState.sInitialized);

                    m_tracker.startPlay();

                } else {
                    IwdsLog.e(this, "AudioTracker initialize failure");

                    m_synthListener
                            .onError(RemoteSpeechErrorCode.ERROR_INIT_TRACKER);
                }

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in onError: " + e.toString());
            }
        }

        @Override
        public void onTrackStart() {
            sendBroadcast(RemoteSpeechServiceManager.AudioTrackState.sPlaying);

            m_handler.requestSynthStartPlay(m_parameters, m_text);
        }

        @Override
        public void onTrackPause() {
            IwdsLog.i(this, "AudioTracker paused");

            sendBroadcast(RemoteSpeechServiceManager.AudioTrackState.sPaused);

            try {
                if (m_synthListener != null) {
                    m_synthListener.onSpeakPaused();

                } else {
                    IwdsAssert.dieIf(this, true, "m_synthListener is null");
                }

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in onSpeakPaused: " + e.toString());
            }
        }

        @Override
        public void onTrackResume() {
            IwdsLog.i(this, "AudioTracker resumed");

            sendBroadcast(RemoteSpeechServiceManager.AudioTrackState.sPlaying);

            try {
                if (m_synthListener != null) {
                    m_synthListener.onSpeakResumed();

                } else {
                    IwdsAssert.dieIf(this, true, "m_synthListener is null");
                }

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in onSpeakResumed: " + e.toString());
            }
        }

        @Override
        public void onTrackComplete() {
            m_handler.destroyAudioTracker();
        }

        @Override
        public void onTrackError(int errorCode) {
            m_handler.destroyAudioTracker();

            int error = RemoteSpeechErrorCode.SUCCESS;

            IwdsLog.e(this, "AudioTracker track error with code " + errorCode);

            switch (errorCode) {
            case AudioTracker.ERROR_TRACK_INITIALIZE:
                error = RemoteSpeechErrorCode.ERROR_INIT_TRACKER;
                break;

            case AudioTracker.ERROR_TRACK_START:
                error = RemoteSpeechErrorCode.ERROR_START_TRACKER;
                break;

            case AudioTracker.ERROR_ILLEGAL_STATE:
                error = RemoteSpeechErrorCode.ERROR_ILLEGAL_STATE;
                break;

            case AudioTracker.ERROR_TRACK_PAUSE:
                error = RemoteSpeechErrorCode.ERROR_PAUSE_TRACKER;
                break;

            case AudioTracker.ERROR_TRACK_RESUME:
                error = RemoteSpeechErrorCode.ERROR_RESUME_TRACKER;
                break;

            case AudioTracker.ERROR_TRACK_WRITE:
                error = RemoteSpeechErrorCode.ERROR_WRITE_TRACKER;
                break;

            default:
                IwdsAssert.dieIf(this, true, "unknown AudioTracker error code "
                        + errorCode);
            }

            try {
                m_synthListener.onError(error);

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in onError: " + e.toString());
            }
        }

        @Override
        public void onTrackDestroy() {
            sendBroadcast(RemoteSpeechServiceManager.AudioTrackState.sReleased);

            try {
                if (m_synthListener != null) {
                    m_synthListener.onCancel();

                    if (m_synthListener.asBinder().isBinderAlive())
                        m_synthListener.asBinder().unlinkToDeath(this, 0);

                } else {
                    IwdsAssert.dieIf(this, true, "m_synthListener is null");
                }

            } catch (RemoteException e) {
                IwdsLog.e(this, "Exception in onError: " + e.toString());
            }
        }
    }

    private class ServiceHandler extends Handler {
        private static final int MSG_CHANNEL_STATUS_CHANGED = 0;
        private static final int MSG_REQUEST_SPEECH_SERVICE_STATUS = 1;

        private static final int MSG_REGISTER_STATUS_LISTENER = 2;
        private static final int MSG_UNREGISTER_STATUS_LISTENER = 3;

        private static final int MSG_REQUEST_RECOGNIZE_START_RECORD = 4;
        private static final int MSG_REQUEST_RECOGNIZE_APPEND_DATA = 5;
        private static final int MSG_REQUEST_RECOGNIZE_START_LISTEN = 6;
        private static final int MSG_REQUEST_RECOGNIZE_LISTEN_STATUS = 7;
        private static final int MSG_REQUEST_RECOGNIZE_STOP_LISTEN = 8;
        private static final int MSG_REQUEST_RECOGNIZE_CANCEL_LISTEN = 9;

        private static final int MSG_REQUEST_UNDERSTAND_START_RECORD = 10;
        private static final int MSG_REQUEST_UNDERSTAND_APPEND_DATA = 11;
        private static final int MSG_REQUEST_UNDERSTAND_START_LISTEN = 12;
        private static final int MSG_REQUEST_UNDERSTAND_LISTEN_STATUS = 13;
        private static final int MSG_REQUEST_UNDERSTAND_STOP_LISTEN = 14;
        private static final int MSG_REQUEST_UNDERSTAND_CANCEL_LISTEN = 15;

        private static final int MSG_REQUEST_SYNTHESISE_START_SPEAK = 16;
        private static final int MSG_REQUEST_SYNTHESISE_START_PLAY = 17;
        private static final int MSG_REQUEST_SYNTHESISE_CANCEL_SPEAK = 18;
        private static final int MSG_REQUEST_SYNTHESISE_PAUSE_SPEAK = 19;
        private static final int MSG_REQUEST_SYNTHESISE_RESUME_SPEAK = 20;
        private static final int MSG_REQUEST_SYNTHESISE_SPEAK_STATUS = 21;

        private static final int MSG_RESPONSE_SPEECH_SERVICE_CONNECTED = 23;

        private static final int MSG_RESPONSE_RECOGNIZE_ERROR = 24;
        private static final int MSG_RESPONSE_RECOGNIZE_STATUS = 25;
        private static final int MSG_RESPONSE_RECOGNIZE_VOLUME_CHANGED = 26;
        private static final int MSG_RESPONSE_RECOGNIZE_BEGIN_SPEECH = 27;
        private static final int MSG_RESPONSE_RECOGNIZE_END_SPEECH = 28;
        private static final int MSG_RESPONSE_RECOGNIZE_RESULT = 29;

        private static final int MSG_RESPONSE_UNDERSTAND_ERROR = 30;
        private static final int MSG_RESPONSE_UNDERSTAND_STATUS = 31;
        private static final int MSG_RESPONSE_UNDERSTAND_VOLUME_CHANGED = 32;
        private static final int MSG_RESPONSE_UNDERSTAND_BEGIN_SPEECH = 33;
        private static final int MSG_RESPONSE_UNDERSTAND_END_SPEECH = 34;
        private static final int MSG_RESPONSE_UNDERSTAND_RESULT = 35;

        private static final int MSG_RESPONSE_SYNTHESISE_COMPLETE = 40;
        private static final int MSG_RESPONSE_SYNTHESISE_BUFFER_PROGRESS_CHANGED = 41;
        private static final int MSG_RESPONSE_SYNTHESISE_SPEAK_BEGIN = 42;
        private static final int MSG_RESPONSE_SYNTHESISE_SPEAK_PAUSED = 43;
        private static final int MSG_RESPONSE_SYNTHESISE_SPEAK_PROGRESS_CHANGED = 44;
        private static final int MSG_RESPONSE_SYNTHESISE_SPEAK_RESUMED = 45;
        private static final int MSG_RESPONSE_SYNTHESISE_SPEAK_STATUS = 46;
        private static final int MGS_RESPONSE_SYNTHESISE_SPEAK_ERROR = 47;
        private static final int MSG_RESPONSE_SYNTHESISE_SPEAK_DATA = 48;

        private static final int MSG_LEXICON_UPDATED = 49;
        private static final int MSG_GRAMMAR_BUILDED = 50;

        private static final int MSG_DESTROY_AUDIO_RECORDER = 60;
        private static final int MSG_AUDIO_TRACKER_WAIT_COMPLETE_AND_RELEASE = 61;
        private static final int MSG_DESTROY_AUDIO_TRACKER = 62;

        private int m_serviceConnectStatus = RemoteSpeechErrorCode.ERROR_REMOTE_DISCONNECTED;

        public void registerRemoteStatusListener(String uuid,
                IRemoteStatusCallback callback) {
            Message msg = Message.obtain(this);
            Bundle bundle = new Bundle();

            bundle.putString("uuid", uuid);

            msg.what = MSG_REGISTER_STATUS_LISTENER;
            msg.obj = callback;
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void unregisterRemoteStatusListener(String uuid) {
            Message msg = Message.obtain(this);

            msg.what = MSG_UNREGISTER_STATUS_LISTENER;
            msg.obj = uuid;

            msg.sendToTarget();
        }

        public void requestSpeechServiceStatus() {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_SPEECH_SERVICE_STATUS;

            msg.sendToTarget();
        }

        public void setChannelAvailable(boolean available) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_CHANNEL_STATUS_CHANGED;
            msg.arg1 = available ? 1 : 0;

            msg.sendToTarget();
        }

        public void broadcastRemoteServiceStatus(int status) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_RESPONSE_SPEECH_SERVICE_CONNECTED;
            msg.arg1 = status;

            msg.sendToTarget();
        }

        public void requestStartRecognizeRecord(
                IRemoteRecognizerCallback callback,
                HashMap<String, String> parameters) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_RECOGNIZE_START_RECORD;
            msg.obj = new RemoteSpeechArgs<IRemoteRecognizerCallback>(callback,
                    parameters);

            msg.sendToTarget();
        }

        public void requestStopRecognize(IRemoteRecognizerCallback callback) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_RECOGNIZE_STOP_LISTEN;
            msg.obj = callback;

            msg.sendToTarget();
        }

        public void requestRecognizeStatus(IRemoteRecognizerCallback callback) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_RECOGNIZE_LISTEN_STATUS;
            msg.obj = callback;

            msg.sendToTarget();
        }

        public void requestCancelRecognize(IRemoteRecognizerCallback callback) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_RECOGNIZE_CANCEL_LISTEN;
            msg.obj = callback;

            msg.sendToTarget();
        }

        public void requestRecognizeAppendData(byte[] buffer, int timeStamp) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_RECOGNIZE_APPEND_DATA;
            msg.arg1 = timeStamp;
            msg.obj = buffer;

            msg.sendToTarget();
        }

        public void requestRecognizeStartListen(
                HashMap<String, String> parameters) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_RECOGNIZE_START_LISTEN;
            msg.obj = parameters;

            msg.sendToTarget();
        }

        public void requestStartUnderstandRecord(
                IRemoteUnderstanderCallback callback,
                HashMap<String, String> parameters) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_UNDERSTAND_START_RECORD;
            msg.obj = new RemoteSpeechArgs<IRemoteUnderstanderCallback>(
                    callback, parameters);

            msg.sendToTarget();
        }

        public void requestStopUnderstand(IRemoteUnderstanderCallback callback) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_UNDERSTAND_STOP_LISTEN;
            msg.obj = callback;

            msg.sendToTarget();
        }

        public void requestUnderstandStatus(IRemoteUnderstanderCallback callback) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_UNDERSTAND_LISTEN_STATUS;
            msg.obj = callback;

            msg.sendToTarget();
        }

        public void requestCancelUnderstand(IRemoteUnderstanderCallback callback) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_UNDERSTAND_CANCEL_LISTEN;
            msg.obj = callback;

            msg.sendToTarget();
        }

        public void requestUnderstandAppendData(byte[] buffer, int timeStamp) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_UNDERSTAND_APPEND_DATA;
            msg.arg1 = timeStamp;
            msg.obj = buffer;

            msg.sendToTarget();
        }

        public void requestUnderstandStartListen(
                HashMap<String, String> parameters) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_UNDERSTAND_START_LISTEN;
            msg.obj = parameters;

            msg.sendToTarget();
        }

        public void requestSynthStartSpeak(HashMap<String, String> parameters,
                String text, IRemoteSynthesizerCallback callback) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_SYNTHESISE_START_SPEAK;
            msg.obj = new RemoteSpeechArgs<IRemoteSynthesizerCallback>(
                    callback, parameters, text);

            msg.sendToTarget();
        }

        public void requestSynthStartPlay(HashMap<String, String> parameters,
                String text) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_SYNTHESISE_START_PLAY;
            msg.obj = new RemoteSpeechArgs<IRemoteSynthesizerCallback>(
                    parameters, text);

            msg.sendToTarget();
        }

        public void requestCancelSpeak(IRemoteSynthesizerCallback callback) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_SYNTHESISE_CANCEL_SPEAK;

            msg.obj = callback;

            msg.sendToTarget();
        }

        public void requestPauseSpeak(IRemoteSynthesizerCallback callback) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_SYNTHESISE_PAUSE_SPEAK;
            msg.obj = callback;

            msg.sendToTarget();
        }

        public void requestResumeSpeak(IRemoteSynthesizerCallback callback) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_SYNTHESISE_RESUME_SPEAK;
            msg.obj = callback;

            msg.sendToTarget();
        }

        public void requestSpeakStatus(IRemoteSynthesizerCallback callback) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_SYNTHESISE_SPEAK_STATUS;
            msg.obj = callback;

            msg.sendToTarget();
        }

        public void destroyAudioRecorder() {
            final Message msg = Message.obtain(this);

            msg.what = MSG_DESTROY_AUDIO_RECORDER;

            msg.sendToTarget();
        }

        public void waitAudioTackerComplete() {
            final Message msg = Message.obtain(this);

            msg.what = MSG_AUDIO_TRACKER_WAIT_COMPLETE_AND_RELEASE;

            msg.sendToTarget();
        }

        public void destroyAudioTracker() {
            final Message msg = Message.obtain(this);

            msg.what = MSG_DESTROY_AUDIO_TRACKER;

            msg.sendToTarget();
        }

        public void handleResponse(RemoteSpeechResponse response) {
            final Message msg = Message.obtain(this);

            switch (response.type) {
            case RemoteSpeechResponse.TYPE_SPEECH_SERVICE_STATUS:
                msg.what = MSG_RESPONSE_SPEECH_SERVICE_CONNECTED;
                msg.arg1 = response.errorCode;

                break;

            case RemoteSpeechResponse.TYPE_RECOGNIZE_BEGIN_SPEECH:
                msg.what = MSG_RESPONSE_RECOGNIZE_BEGIN_SPEECH;

                break;

            case RemoteSpeechResponse.TYPE_RECOGNIZE_END_SPEECH:
                msg.what = MSG_RESPONSE_RECOGNIZE_END_SPEECH;

                break;

            case RemoteSpeechResponse.TYPE_RECOGNIZE_ERROR:
                msg.what = MSG_RESPONSE_RECOGNIZE_ERROR;

                msg.arg1 = response.errorCode;
                break;

            case RemoteSpeechResponse.TYPE_RECOGNIZE_RESULT:
                msg.what = MSG_RESPONSE_RECOGNIZE_RESULT;

                msg.obj = response.recognizeResult;
                msg.arg1 = response.recognizeLast ? 1 : 0;
                break;

            case RemoteSpeechResponse.TYPE_RECOGNIZE_STATUS:
                msg.what = MSG_RESPONSE_RECOGNIZE_STATUS;

                msg.arg1 = response.recognizeStatus ? 1 : 0;
                break;

            case RemoteSpeechResponse.TYPE_RECOGNIZE_VOLUME_CHANGED:
                msg.what = MSG_RESPONSE_RECOGNIZE_VOLUME_CHANGED;

                msg.arg1 = response.recognizeVolume;
                break;

            case RemoteSpeechResponse.TYPE_UNDERSTAND_BEGIN_SPEECH:
                msg.what = MSG_RESPONSE_RECOGNIZE_BEGIN_SPEECH;
                break;

            case RemoteSpeechResponse.TYPE_UNDERSTAND_END_SPEECH:
                msg.what = MSG_RESPONSE_RECOGNIZE_END_SPEECH;
                break;

            case RemoteSpeechResponse.TYPE_UNDERSTAND_ERROR:
                msg.what = MSG_RESPONSE_UNDERSTAND_ERROR;

                msg.arg1 = response.errorCode;
                break;

            case RemoteSpeechResponse.TYPE_UNDERSTAND_RESULT:
                msg.what = MSG_RESPONSE_UNDERSTAND_RESULT;

                msg.obj = response.understandResult;
                break;

            case RemoteSpeechResponse.TYPE_UNDERSTAND_STATUS:
                msg.what = MSG_RESPONSE_UNDERSTAND_STATUS;

                msg.arg1 = response.understandStatus ? 1 : 0;
                break;

            case RemoteSpeechResponse.TYPE_UNDRESTAND_VOLUME_CHANGED:
                msg.what = MSG_RESPONSE_UNDERSTAND_VOLUME_CHANGED;

                msg.arg1 = response.understandVolume;
                break;

            case RemoteSpeechResponse.TYPE_SYNTHESISE_BUFFER_PROGRESS_CHANGED:
                msg.what = MSG_RESPONSE_SYNTHESISE_BUFFER_PROGRESS_CHANGED;

                msg.arg1 = response.bufferProgress;
                break;

            case RemoteSpeechResponse.TYPE_SYNTHESISE_COMPLETE:
                msg.what = MSG_RESPONSE_SYNTHESISE_COMPLETE;
                msg.arg1 = response.errorCode;
                break;

            case RemoteSpeechResponse.TYPE_SYNTHESISE_SPEAK_BEGIN:
                msg.what = MSG_RESPONSE_SYNTHESISE_SPEAK_BEGIN;
                break;

            case RemoteSpeechResponse.TYPE_SYNTHESISE_SPEAK_ERROR:
                msg.what = MGS_RESPONSE_SYNTHESISE_SPEAK_ERROR;

                msg.arg1 = response.errorCode;
                break;

            case RemoteSpeechResponse.TYPE_SYNTHESISE_SPEAK_PAUSED:
                msg.what = MSG_RESPONSE_SYNTHESISE_SPEAK_PAUSED;
                break;

            case RemoteSpeechResponse.TYPE_SYNTHESISE_SPEAK_PROGRESS_CHANGED:
                msg.what = MSG_RESPONSE_SYNTHESISE_SPEAK_PROGRESS_CHANGED;

                msg.arg1 = response.speakProgress;
                break;

            case RemoteSpeechResponse.TYPE_SYNTHESISE_SPEAK_RESUMED:
                msg.what = MSG_RESPONSE_SYNTHESISE_SPEAK_RESUMED;
                break;

            case RemoteSpeechResponse.TYPE_SYNTHESISE_SPEAK_STATUS:
                msg.what = MSG_RESPONSE_SYNTHESISE_SPEAK_STATUS;

                msg.arg1 = response.speakStatus ? 1 : 0;
                break;

            case RemoteSpeechResponse.TYPE_SYNTHESISE_SPEAK_DATA:
                msg.what = MSG_RESPONSE_SYNTHESISE_SPEAK_DATA;

                msg.obj = response.audioBuffer;
                break;

            default:
                break;
            }

            msg.sendToTarget();
        }

        @Override
        public void handleMessage(Message msg) {
            RemoteSpeechRequest request = RemoteSpeechRequest
                    .obtain(m_transactor);

            switch (msg.what) {
            case MSG_RESPONSE_SPEECH_SERVICE_CONNECTED:
                m_serviceConnectStatus = msg.arg1;

                if (m_serviceConnectStatus == RemoteSpeechErrorCode.SUCCESS)
                    IwdsLog.i(this, "Speech service connected");
                else {
                    destroyAudioRecorder();
                    destroyAudioTracker();

                    IwdsLog.i(this,
                            "Speech service disconnected with error code "
                                    + m_serviceConnectStatus);
                }

                m_remoteStatusCallback
                        .onRemoteStatusChanged(m_serviceConnectStatus);
                break;

            case MSG_CHANNEL_STATUS_CHANGED:
                if (msg.arg1 != 1)
                    broadcastRemoteServiceStatus(RemoteSpeechErrorCode.ERROR_REMOTE_DISCONNECTED);

                break;

            case MSG_REQUEST_SPEECH_SERVICE_STATUS:
                if (m_serviceConnectStatus != RemoteSpeechErrorCode.SUCCESS) {
                    IwdsLog.i(this,
                            "Speech service on remote device not connected yet");
                } else {
                    IwdsLog.i(this,
                            "Speech service on remote device already connected");
                }

                broadcastRemoteServiceStatus(m_serviceConnectStatus);
                break;

            case MSG_REGISTER_STATUS_LISTENER:
                IwdsLog.d(this, "Try to register remote status listener");

                m_remoteStatusCallback.registerRemoteStatusListener(msg
                        .getData().getString("uuid"),
                        (IRemoteStatusCallback) msg.obj);

                // do cold boot
                requestSpeechServiceStatus();
                break;

            case MSG_UNREGISTER_STATUS_LISTENER:
                IwdsLog.d(this, "Try to unregister remote status listener");

                m_remoteStatusCallback
                        .unregisterRemoteStatusListener((String) msg.obj);
                break;

            case MSG_REQUEST_RECOGNIZE_START_RECORD:
                RemoteSpeechArgs<IRemoteRecognizerCallback> recognizeArgs = (RemoteSpeechArgs<IRemoteRecognizerCallback>) msg.obj;
                handleStartRecognizeRecord(recognizeArgs.callback,
                        recognizeArgs.parameters);
                break;

            case MSG_REQUEST_RECOGNIZE_APPEND_DATA:
                request.type = RemoteSpeechRequest.TYPE_RECOGNIZE_APPEND_DATA;
                request.timeStamp = msg.arg1;
                request.buffer = (byte[]) msg.obj;

                IwdsLog.d(this,
                        "Try to request recognize append data with buffer size "
                                + request.buffer.length + ", time stamp "
                                + request.timeStamp + "ms");

                request.sendToRemote();
                break;

            case MSG_REQUEST_RECOGNIZE_START_LISTEN:
                request.type = RemoteSpeechRequest.TYPE_RECOGNIZE_START_LISTEN;
                request.parameters = (HashMap<String, String>) msg.obj;

                IwdsLog.d(this,
                        "Try to request start recognize listen with parameters "
                                + request.parameters);

                request.sendToRemote();
                break;

            case MSG_REQUEST_RECOGNIZE_LISTEN_STATUS:
                if (!checkRecognizeClientListener((IRemoteRecognizerCallback) msg.obj))
                    return;

                request.type = RemoteSpeechRequest.TYPE_RECOGNIZE_LISTEN_STATUS;

                IwdsLog.d(this, "Try to request recognize listen status");

                request.sendToRemote();
                break;

            case MSG_REQUEST_RECOGNIZE_STOP_LISTEN:
                if (!checkRecognizeClientListener((IRemoteRecognizerCallback) msg.obj))
                    return;

                request.type = RemoteSpeechRequest.TYPE_RECOGNIZE_STOP_LISTEN;

                IwdsLog.d(this, "Try to request stop recognize listen");

                request.sendToRemote();
                break;

            case MSG_REQUEST_RECOGNIZE_CANCEL_LISTEN:
                if (!checkRecognizeClientListener((IRemoteRecognizerCallback) msg.obj))
                    return;

                destroyAudioRecorder();

                request.type = RemoteSpeechRequest.TYPE_RECOGNIZE_CANCEL_LISTEN;

                IwdsLog.d(this, "Try to request recognize cancel listen");

                request.sendToRemote();
                break;

            case MSG_REQUEST_UNDERSTAND_START_RECORD:
                RemoteSpeechArgs<IRemoteUnderstanderCallback> understandArgs = (RemoteSpeechArgs<IRemoteUnderstanderCallback>) msg.obj;
                handleStartUnderstandRecord(understandArgs.callback,
                        understandArgs.parameters);
                break;

            case MSG_REQUEST_UNDERSTAND_APPEND_DATA:
                request.type = RemoteSpeechRequest.TYPE_UNDERSTAND_APPEND_DATA;
                request.timeStamp = msg.arg1;
                request.buffer = (byte[]) msg.obj;

                IwdsLog.d(this,
                        "Try to request understand append data with buffer size "
                                + request.buffer.length + ", time stamp "
                                + request.timeStamp + "ms");

                request.sendToRemote();
                break;

            case MSG_REQUEST_UNDERSTAND_START_LISTEN:
                request.type = RemoteSpeechRequest.TYPE_UNDERSTAND_START_LISTEN;
                request.parameters = (HashMap<String, String>) msg.obj;

                IwdsLog.d(this, "Try to request understand start listen");

                request.sendToRemote();
                break;

            case MSG_REQUEST_UNDERSTAND_LISTEN_STATUS:
                if (!checkUnderstandClientListener((IRemoteUnderstanderCallback) msg.obj))
                    return;

                request.type = RemoteSpeechRequest.TYPE_UNDERSTAND_LISTEN_STATUS;

                IwdsLog.d(this, "Try to request understand listen status");

                request.sendToRemote();
                break;

            case MSG_REQUEST_UNDERSTAND_STOP_LISTEN:
                if (!checkUnderstandClientListener((IRemoteUnderstanderCallback) msg.obj))
                    return;

                request.type = RemoteSpeechRequest.TYPE_UNDERSTAND_STOP_LISTEN;

                IwdsLog.d(this, "Try to request understand stop listen");

                request.sendToRemote();
                break;

            case MSG_REQUEST_UNDERSTAND_CANCEL_LISTEN:
                if (!checkUnderstandClientListener((IRemoteUnderstanderCallback) msg.obj))
                    return;

                destroyAudioRecorder();

                request.type = RemoteSpeechRequest.TYPE_UNDERSTAND_CANCEL_LISTEN;

                IwdsLog.d(this, "Try to request understand cancel listen");

                request.sendToRemote();
                break;

            case MSG_REQUEST_SYNTHESISE_START_SPEAK:
                RemoteSpeechArgs<IRemoteSynthesizerCallback> synthArgs = (RemoteSpeechArgs<IRemoteSynthesizerCallback>) msg.obj;
                handleStartSynthSpeak(synthArgs.callback, synthArgs.parameters,
                        synthArgs.text);
                break;

            case MSG_REQUEST_SYNTHESISE_START_PLAY:
                RemoteSpeechArgs<IRemoteSynthesizerCallback> synthArgs2 = (RemoteSpeechArgs<IRemoteSynthesizerCallback>) msg.obj;

                request.type = RemoteSpeechRequest.TYPE_SYNTHESISE_START_SPEAK;
                request.parameters = synthArgs2.parameters;
                request.text = synthArgs2.text;

                IwdsLog.d(this, "Try to request speak " + synthArgs2.text
                        + " with parameters " + synthArgs2.parameters);

                request.sendToRemote();
                break;

            case MSG_REQUEST_SYNTHESISE_CANCEL_SPEAK:
                if (!checkSynthesiseClientListener((IRemoteSynthesizerCallback) msg.obj))
                    return;

                destroyAudioTracker();

                request.type = RemoteSpeechRequest.TYPE_SYNTHESISE_STOP_SPEAK;

                IwdsLog.d(this, "Try to request cancel speak");

                request.sendToRemote();
                break;

            case MSG_REQUEST_SYNTHESISE_PAUSE_SPEAK:
                if (!checkSynthesiseClientListener((IRemoteSynthesizerCallback) msg.obj))
                    return;

                m_trackTransport.pause();

                request.type = RemoteSpeechRequest.TYPE_SYNTHESISE_PAUSE_SPEAK;

                IwdsLog.d(this, "Try to request pause speak");

                request.sendToRemote();
                break;

            case MSG_REQUEST_SYNTHESISE_RESUME_SPEAK:
                if (!checkSynthesiseClientListener((IRemoteSynthesizerCallback) msg.obj))
                    return;

                m_trackTransport.resume();

                request.type = RemoteSpeechRequest.TYPE_SYNTHESISE_RESUME_SPEAK;

                IwdsLog.d(this, "Try to request resume speak");

                request.sendToRemote();
                break;

            case MSG_REQUEST_SYNTHESISE_SPEAK_STATUS:
                if (!checkSynthesiseClientListener((IRemoteSynthesizerCallback) msg.obj))
                    return;

                m_trackTransport.getStatus();
                break;

            case MSG_RESPONSE_RECOGNIZE_ERROR:
                if (m_recordTransport != null)
                    m_recordTransport.onError(msg.arg1);
                break;

            case MSG_RESPONSE_RECOGNIZE_STATUS:
                if (m_recordTransport != null)
                    m_recordTransport.onListeningStatus(msg.arg1 == 1);
                break;

            case MSG_RESPONSE_RECOGNIZE_VOLUME_CHANGED:
                if (m_recordTransport != null)
                    m_recordTransport.onVolumChanged(msg.arg1);
                break;

            case MSG_RESPONSE_RECOGNIZE_BEGIN_SPEECH:
                if (m_recordTransport != null)
                    m_recordTransport.onBeginOfSpeech();
                break;

            case MSG_RESPONSE_RECOGNIZE_END_SPEECH:
                if (m_recordTransport != null)
                    m_recordTransport.onEndOfSpeech();
                break;

            case MSG_RESPONSE_RECOGNIZE_RESULT:
                if (m_recordTransport != null)
                    m_recordTransport.onRecognizeResult((String) msg.obj,
                            msg.arg1 == 1);
                break;

            case MSG_RESPONSE_UNDERSTAND_ERROR:
                if (m_recordTransport != null)
                    m_recordTransport.onError(msg.arg1);
                break;

            case MSG_RESPONSE_UNDERSTAND_STATUS:
                if (m_recordTransport != null)
                    m_recordTransport.onListeningStatus(msg.arg1 == 1);
                break;

            case MSG_RESPONSE_UNDERSTAND_VOLUME_CHANGED:
                if (m_recordTransport != null)
                    m_recordTransport.onVolumChanged(msg.arg1);
                break;

            case MSG_RESPONSE_UNDERSTAND_BEGIN_SPEECH:
                if (m_recordTransport != null)
                    m_recordTransport.onBeginOfSpeech();
                break;

            case MSG_RESPONSE_UNDERSTAND_END_SPEECH:
                if (m_recordTransport != null)
                    m_recordTransport.onEndOfSpeech();
                break;

            case MSG_RESPONSE_UNDERSTAND_RESULT:
                if (m_recordTransport != null)
                    m_recordTransport
                            .onUnderstandResult((RemoteBusiness) msg.obj);
                break;

            case MSG_RESPONSE_SYNTHESISE_COMPLETE:
                if (m_trackTransport != null)
                    m_trackTransport.onSynthCompleted(msg.arg1);
                break;

            case MSG_RESPONSE_SYNTHESISE_BUFFER_PROGRESS_CHANGED:
                if (m_trackTransport != null)
                    m_trackTransport.onSynthBufferProgress(msg.arg1);
                break;

            case MSG_RESPONSE_SYNTHESISE_SPEAK_BEGIN:
                if (m_trackTransport != null)
                    m_trackTransport.onSpeakBegin();
                break;

            case MSG_RESPONSE_SYNTHESISE_SPEAK_PAUSED:
                if (m_trackTransport != null)
                    m_trackTransport.onSpeakPaused();
                break;

            case MSG_RESPONSE_SYNTHESISE_SPEAK_PROGRESS_CHANGED:
                if (m_trackTransport != null)
                    m_trackTransport.onSynthProgress(msg.arg1);
                break;

            case MSG_RESPONSE_SYNTHESISE_SPEAK_RESUMED:
                if (m_trackTransport != null)
                    m_trackTransport.onSpeakResumed();
                break;

            case MSG_RESPONSE_SYNTHESISE_SPEAK_STATUS:
                if (m_trackTransport != null)
                    m_trackTransport.onSpeakStatus(msg.arg1 == 1);
                break;

            case MGS_RESPONSE_SYNTHESISE_SPEAK_ERROR:
                if (m_trackTransport != null)
                    m_trackTransport.onError(msg.arg1);
                break;

            case MSG_RESPONSE_SYNTHESISE_SPEAK_DATA:
                if (m_trackTransport != null)
                    m_trackTransport.onSpeakData((byte[]) msg.obj);
                break;

            case MSG_LEXICON_UPDATED:
                break;

            case MSG_GRAMMAR_BUILDED:
                break;

            case MSG_DESTROY_AUDIO_RECORDER:
                if (m_recordTransport == null)
                    return;

                IwdsLog.i(this, "AudioRecordTransport destroy");
                m_recordTransport.destroy();
                m_recordTransport = null;

                break;

            case MSG_AUDIO_TRACKER_WAIT_COMPLETE_AND_RELEASE:
                if (m_trackTransport == null)
                    return;

                IwdsLog.i(this, "AudioTrackTransport wait speak complete");
                m_trackTransport.waitSpeakComplete();

                break;

            case MSG_DESTROY_AUDIO_TRACKER:
                if (m_trackTransport == null)
                    return;

                IwdsLog.i(this, "AudioTrackTransport destroy");
                m_trackTransport.destroy();
                m_trackTransport = null;

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
            m_handler.setChannelAvailable(isAvailable);
        }

        @Override
        public void onSendResult(DataTransactResult result) {
            // do not care
        }

        @Override
        public void onDataArrived(Object object) {
            if (object instanceof RemoteSpeechResponse)
                m_handler.handleResponse((RemoteSpeechResponse) object);
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
    /*------------------ DataTransactorCallback end ---------------------- */
}
