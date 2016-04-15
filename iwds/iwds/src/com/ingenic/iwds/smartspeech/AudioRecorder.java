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

import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.media.MediaRecorder.AudioSource;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public final class AudioRecorder {
    public static final int S_UNINITIALIZED = 0;
    public static final int S_INITIALIZED = 1;
    public static final int S_RECORDING = 2;
    public static final int S_STOPPED = 3;
    public static final int S_RESETING = 4;
    public static final int S_ERROR = 5;

    public static final int SUCCESS = 0;
    public static final int ERROR_RECORD_INITIALIZE = -1;
    public static final int ERROR_RECORD_START = -2;
    public static final int ERROR_RECORD_STOP = -3;
    public static final int ERROR_RECORD_RESET = -4;
    public static final int ERROR_ILLEGAL_STATE = -5;

    private static final int TIMER_INTERVAL = 200;
    private static final int DEFAULT_SAMPLE_RATE = 16000;

    private int m_audioSource;
    private int m_sampleRate;
    private int m_channelCount;
    private int m_audioFormat;
    private int m_channelConfig;
    private int m_bytesPersample;
    private int m_bufferSize;
    private int m_periodInFrames;

    private CallbackHandler m_callbackHandler;
    private RecordController m_controller;

    private static AudioRecorder sInstance;

    private AudioRecorder(int audioSource, int sampleRateInHz,
            int channelConfig, int audioFormat) {

        IwdsLog.i(this, "Create new AudioRecorder");

        m_audioSource = audioSource;
        m_sampleRate = sampleRateInHz;
        m_channelConfig = channelConfig;
        m_audioFormat = audioFormat;

        if (m_channelConfig == AudioFormat.CHANNEL_IN_MONO)
            m_channelCount = 1;
        else
            m_channelCount = 2;

        if (m_audioFormat == AudioFormat.ENCODING_PCM_16BIT)
            m_bytesPersample = 2;
        else
            m_bytesPersample = 1;

        m_periodInFrames = m_sampleRate * TIMER_INTERVAL / 1000;

        m_bufferSize = m_periodInFrames * 2 * m_channelCount * m_bytesPersample;

        if (m_bufferSize < AudioRecord.getMinBufferSize(m_sampleRate,
                m_channelConfig, m_audioFormat)) {

            m_bufferSize = AudioRecord.getMinBufferSize(m_sampleRate,
                    m_channelConfig, m_audioFormat);

            m_periodInFrames = m_bufferSize
                    / (2 * m_bytesPersample * m_channelCount);

            IwdsLog.i(this, "Increase AudioRecorder buffer size to "
                    + m_bufferSize);
        }

        IwdsLog.i(this, "AudioRecorder buffer size " + m_bufferSize);

        m_callbackHandler = new CallbackHandler();
        m_controller = new RecordController();
    }

    public static AudioRecorder getInstance() {
        if (sInstance == null)
            sInstance = new AudioRecorder(AudioSource.MIC, DEFAULT_SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        return sInstance;
    }

    public void initialize(AudioRecordInitListener listener) {
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        m_callbackHandler.setInitListener(listener);
        m_controller.initialize();
    }

    public void startRecord() {
        m_controller.startRecord();
    }

    public void stopRecord() {
        m_controller.stopRecord();
    }

    public void resetRecord() {
        m_controller.resetRecord();
    }

    public void releaseRecord() {
        if (sInstance == null)
            return;

        m_controller.releaseRecord();

        m_controller.stopHandler();

        sInstance = null;

        IwdsLog.i(this, "AudioRecorder destroy");
    }

    public void setRecordListener(AudioRecordListener listener) {
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        m_callbackHandler.setRecordListener(listener);
    }

    public void removeRecordListener() {
        m_callbackHandler.removeRecordListener();
    }

    public int getSampleRate() {
        return m_controller.getSampleRate();
    }

    public int getAudioSource() {
        return m_controller.getAudioSource();
    }

    public int getAudioFormat() {
        return m_controller.getAudioFormat();
    }

    public int getChannelConfig() {
        return m_controller.getChannelConfig();
    }

    public int getChannelCount() {
        return m_controller.getChannelCount();
    }

    public int getPositionNotificationPeriod() {
        return m_controller.getPositionNotificationPeriod();
    }

    public int getMinBufferSize() {
        return m_controller.getMinBufferSize();
    }

    public int getState() {
        return m_controller.getState();
    }

    public static String errorString(int errorCode) {
        String str = "";

        switch (errorCode) {
        case SUCCESS:
            str = "AudioRecorder success";
            break;

        case ERROR_RECORD_INITIALIZE:
            str = "AudioRecorder initialize failure";
            break;

        case ERROR_RECORD_START:
            str = "AudioRecorder have not be initialized";
            break;

        case ERROR_RECORD_STOP:
            str = "AudioRecorder have not be started";
            break;

        case ERROR_RECORD_RESET:
            str = "AudioRecoder could not reset on illegal state";
            break;

        case ERROR_ILLEGAL_STATE:
            str = "AudioRecoder have not be initialized";
            break;

        default:
            str = "Unknown error code";
            break;
        }

        return str;
    }

    public static String stateString(int state) {
        String str = "";

        switch (state) {
        case S_UNINITIALIZED:
            str = "uninitialized";
            break;

        case S_INITIALIZED:
            str = "initialized";
            break;

        case S_RECORDING:
            str = "recording";
            break;

        case S_STOPPED:
            str = "stoped";
            break;

        case S_RESETING:
            str = "reseting";
            break;

        case S_ERROR:
            str = "error";
            break;

        default:
            str = "unknown state code " + state;
            break;
        }

        return str;
    }

    public interface AudioRecordInitListener {
        void onInitialize(int errorCode);
    }

    public interface AudioRecordListener {
        void onRecordStart();

        void onRecordData(byte[] buffer);

        void onRecordStop();

        void onRecordRelease();

        void onRecordError(int errorCode);
    }

    private class CallbackHandler extends Handler {
        private static final int MSG_NOTIFY_RECORD_ERROR = 0;
        private static final int MSG_NOTIFY_RECORD_RAW_DATA = 1;
        private static final int MSG_NOTIFY_RECORD_INITIALIZED = 2;
        private static final int MSG_NOTIFY_RECORD_START = 3;
        private static final int MSG_NOTIFY_RECORD_STOP = 4;
        private static final int MSG_NOTIFY_RECORD_RELEASE = 5;

        private AudioRecordListener m_recordListener;
        private AudioRecordInitListener m_initListener;

        public void setRecordListener(AudioRecordListener listener) {
            m_recordListener = listener;
        }

        public void removeRecordListener() {
            m_recordListener = null;
        }

        public void setInitListener(AudioRecordInitListener listener) {
            m_initListener = listener;
        }

        public void notifyError(int errorCode) {
            Message msg = Message.obtain(this);

            msg.what = MSG_NOTIFY_RECORD_ERROR;
            msg.arg1 = errorCode;

            msg.sendToTarget();
        }

        public void notifyRecordRawData(byte[] buffer) {
            Message msg = Message.obtain(this);

            msg.what = MSG_NOTIFY_RECORD_RAW_DATA;
            msg.obj = buffer;

            msg.sendToTarget();
        }

        public void notifyInitialized(int errorCode) {
            Message msg = Message.obtain(this);

            msg.what = MSG_NOTIFY_RECORD_INITIALIZED;
            msg.arg1 = errorCode;

            msg.sendToTarget();
        }

        public void notifyRecordStart() {
            Message msg = Message.obtain(this);

            msg.what = MSG_NOTIFY_RECORD_START;

            msg.sendToTarget();
        }

        public void notifyRecordStop() {
            Message msg = Message.obtain(this);

            msg.what = MSG_NOTIFY_RECORD_STOP;

            msg.sendToTarget();
        }

        public void notifyRecordRelease() {
            Message msg = Message.obtain(this);

            msg.what = MSG_NOTIFY_RECORD_RELEASE;

            msg.sendToTarget();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_NOTIFY_RECORD_ERROR:
                if (m_recordListener != null)
                    m_recordListener.onRecordError(msg.arg1);
                break;

            case MSG_NOTIFY_RECORD_RAW_DATA:
                if (m_recordListener != null)
                    m_recordListener.onRecordData((byte[]) msg.obj);
                break;

            case MSG_NOTIFY_RECORD_INITIALIZED:
                if (m_initListener != null)
                    m_initListener.onInitialize(msg.arg1);
                break;

            case MSG_NOTIFY_RECORD_START:
                if (m_recordListener != null)
                    m_recordListener.onRecordStart();
                break;

            case MSG_NOTIFY_RECORD_STOP:
                if (m_recordListener != null)
                    m_recordListener.onRecordStop();
                break;

            case MSG_NOTIFY_RECORD_RELEASE:
                if (m_recordListener != null)
                    m_recordListener.onRecordRelease();
                break;

            default:
                IwdsAssert.dieIf(this, true, "unknown message " + msg.what);
                break;
            }
        }
    }

    private class RecordController {
        private int m_state = S_UNINITIALIZED;

        private Object m_stateLock;

        private HandlerThread m_thread;
        private RecordHandler m_handler;

        public RecordController() {
            m_stateLock = new Object();
            m_thread = new HandlerThread("AudioRecorder");
            m_thread.start();

            m_handler = new RecordHandler(m_thread.getLooper());
        }

        public void initialize() {
            synchronized (m_stateLock) {
                if (m_state == S_INITIALIZED)
                    return;

                if (m_state == S_UNINITIALIZED) {
                    m_handler.initialize();

                } else {
                    IwdsLog.e(this,
                            "Failed to initialize record on illegal state "
                                    + stateString(m_state));

                    m_callbackHandler
                            .notifyInitialized(ERROR_RECORD_INITIALIZE);
                    m_state = S_ERROR;
                }
            }
        }

        public void startRecord() {
            synchronized (m_stateLock) {
                if (m_state == S_RECORDING)
                    return;

                if (m_state == S_INITIALIZED) {
                    m_handler.start();

                } else {
                    IwdsLog.e(this, "Failed to start record on illegal state "
                            + stateString(m_state));

                    m_callbackHandler.notifyError(ERROR_RECORD_START);
                    m_state = S_ERROR;
                }
            }
        }

        public void stopRecord() {
            synchronized (m_stateLock) {
                if (m_state == S_STOPPED)
                    return;

                if (m_state == S_RECORDING) {
                    m_handler.stop();

                } else {
                    IwdsLog.e(this, "Failed to stop recorder on illegal state "
                            + stateString(m_state));

                    m_callbackHandler.notifyError(ERROR_RECORD_STOP);
                    m_state = S_ERROR;
                }
            }
        }

        public void resetRecord() {
            synchronized (m_stateLock) {
                if (m_state == S_RESETING)
                    return;

                if (m_state != S_ERROR && m_state != S_UNINITIALIZED
                        && m_state != S_INITIALIZED) {
                    m_state = S_RESETING;

                    m_handler.release();
                    m_handler.initialize();

                } else {
                    IwdsLog.e(this,
                            "Failed to reset recorder on illegal state "
                                    + stateString(m_state));

                    m_callbackHandler.notifyError(ERROR_RECORD_RESET);
                    m_state = S_ERROR;
                }
            }
        }

        public void releaseRecord() {
            synchronized (m_stateLock) {
                if (m_state == S_UNINITIALIZED)
                    return;
            }

            m_handler.release();
        }

        public void stopHandler() {
            if (m_thread == null)
                return;

            m_handler.quit();

            try {
                m_thread.join();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            m_thread = null;
        }

        public int getState() {
            synchronized (m_stateLock) {
                return m_state;
            }
        }

        public int getSampleRate() {
            synchronized (m_stateLock) {
                if (m_state != S_UNINITIALIZED)
                    return m_sampleRate;
                else
                    return ERROR_ILLEGAL_STATE;
            }
        }

        public int getAudioSource() {
            synchronized (m_stateLock) {
                if (m_state != S_UNINITIALIZED)
                    return m_audioSource;
                else
                    return ERROR_ILLEGAL_STATE;
            }
        }

        public int getAudioFormat() {
            synchronized (m_stateLock) {
                if (m_state != S_UNINITIALIZED)
                    return m_audioFormat;
                else
                    return ERROR_ILLEGAL_STATE;
            }
        }

        public int getChannelConfig() {
            synchronized (m_stateLock) {
                if (m_state != S_UNINITIALIZED)
                    return m_channelConfig;
                else
                    return ERROR_ILLEGAL_STATE;
            }
        }

        public int getChannelCount() {
            synchronized (m_stateLock) {
                if (m_state != S_UNINITIALIZED)
                    return m_channelCount;
                else
                    return ERROR_ILLEGAL_STATE;
            }
        }

        public int getPositionNotificationPeriod() {
            synchronized (m_stateLock) {
                if (m_state != S_UNINITIALIZED)
                    return m_periodInFrames;
                else
                    return ERROR_ILLEGAL_STATE;
            }
        }

        public int getMinBufferSize() {
            synchronized (m_stateLock) {
                if (m_state != S_UNINITIALIZED)
                    return m_bufferSize;
                else
                    return ERROR_ILLEGAL_STATE;
            }
        }

        private class RecordHandler extends Handler implements
                OnRecordPositionUpdateListener {
            private static final int MSG_RECORD_INITIALIZE = 0;
            private static final int MSG_RECORD_START = 1;
            private static final int MSG_RECORD_STOP = 2;
            private static final int MSG_RECORD_RELEASE = 3;
            private static final int MSG_QUIT = 4;

            private AudioRecord m_audioRecorder;
            private byte[] m_buffer;

            public RecordHandler(Looper looper) {
                super(looper);
            }

            public void initialize() {
                Message msg = Message.obtain(this);

                msg.what = MSG_RECORD_INITIALIZE;

                msg.sendToTarget();
            }

            public void start() {
                Message msg = Message.obtain(this);

                msg.what = MSG_RECORD_START;

                msg.sendToTarget();
            }

            public void stop() {
                Message msg = Message.obtain(this);

                msg.what = MSG_RECORD_STOP;

                msg.sendToTarget();
            }

            public void release() {
                Message msg = Message.obtain(this);

                msg.what = MSG_RECORD_RELEASE;

                msg.sendToTarget();
            }

            public void quit() {
                Message msg = Message.obtain(this);

                msg.what = MSG_QUIT;

                msg.sendToTarget();
            }

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_RECORD_INITIALIZE:
                    synchronized (m_stateLock) {
                        try {
                            if (m_audioRecorder == null) {
                                IwdsLog.i(this, "Try to create new AudioRecord");

                                m_audioRecorder = new AudioRecord(
                                        m_audioSource, m_sampleRate,
                                        m_channelConfig, m_audioFormat,
                                        m_bufferSize);

                            } else {
                                IwdsLog.i(this, "AudioRecord have not released");
                            }

                        } catch (IllegalArgumentException e) {
                            IwdsLog.e(this,
                                    "Failed to create AudioRecord, AudioRecord throw exception");

                            m_state = S_ERROR;
                            m_callbackHandler
                                    .notifyInitialized(ERROR_RECORD_INITIALIZE);
                            break;
                        }

                        if (m_audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
                            m_audioRecorder.setRecordPositionUpdateListener(
                                    this, this);

                            m_audioRecorder
                                    .setPositionNotificationPeriod(m_periodInFrames);

                            m_buffer = new byte[m_periodInFrames
                                    * m_bytesPersample * m_channelCount];

                            m_state = S_INITIALIZED;
                            m_callbackHandler.notifyInitialized(SUCCESS);

                        } else {
                            IwdsLog.e(this,
                                    "Failed to create AudioRecord, native exception");

                            m_audioRecorder.release();

                            m_state = S_ERROR;
                            m_callbackHandler
                                    .notifyInitialized(ERROR_RECORD_INITIALIZE);
                        }
                    }
                    break;

                case MSG_RECORD_START:
                    synchronized (m_stateLock) {
                        m_audioRecorder.startRecording();
                        m_audioRecorder.read(m_buffer, 0, m_buffer.length);
                        m_state = S_RECORDING;
                        m_callbackHandler.notifyRecordStart();
                    }
                    break;

                case MSG_RECORD_STOP:
                    synchronized (m_stateLock) {
                        m_audioRecorder.stop();
                        m_state = S_STOPPED;
                        m_callbackHandler.notifyRecordStop();
                    }
                    break;

                case MSG_RECORD_RELEASE:
                    synchronized (m_stateLock) {
                        if (m_audioRecorder != null)
                            m_audioRecorder.release();
                        m_audioRecorder = null;
                        m_state = S_UNINITIALIZED;
                    }
                    break;

                case MSG_QUIT:
                    m_thread.quit();
                    m_callbackHandler.notifyRecordRelease();
                    break;
                }
            }

            @Override
            public void onMarkerReached(AudioRecord recorder) {
                // do not care
            }

            @Override
            public void onPeriodicNotification(AudioRecord recorder) {
                synchronized (m_stateLock) {
                    if (m_state != S_RECORDING) {
                        IwdsLog.e(this,
                                "Audio recorder not on recording state, ignore new data");
                        return;
                    }
                }

                recorder.read(m_buffer, 0, m_buffer.length);
                m_callbackHandler.notifyRecordRawData(m_buffer);
            }
        }
    }
}
