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

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public final class AudioTracker {
    public static final int S_UNINITIALIZED = 0;
    public static final int S_INITIALIZED = 1;
    public static final int S_PLAYING = 2;
    public static final int S_PAUSED = 3;
    public static final int S_ERROR = 4;

    public static final int SUCCESS = 0;
    public static final int ERROR_TRACK_INITIALIZE = -1;
    public static final int ERROR_TRACK_START = -2;
    public static final int ERROR_TRACK_PAUSE = -3;
    public static final int ERROR_TRACK_WRITE = -4;
    public static final int ERROR_TRACK_RESUME = -5;
    public static final int ERROR_ILLEGAL_STATE = -6;

    private static final int TIMER_INTERVAL = 120;
    private static final int DEFAULT_SAMPLE_RATE = 16000;
    private static final long MAX_UNCONSUMED_AUDIO_MS = 1000;

    private int m_streamType;
    private int m_sampleRate;
    private int m_channelConfig;
    private int m_audioFormat;
    private int m_bufferSize;
    private int m_channelCount;
    private int m_bytesPersample;
    private int m_periodInFrames;

    private Controller m_controller;
    private CallbackHandler m_callbackHandler;

    private static AudioTracker sInstance;

    private Lock m_listLock = new ReentrantLock();
    private Lock m_stateLock = new ReentrantLock();
    private Condition m_speakComplete = m_listLock.newCondition();
    private Condition m_readReady = m_listLock.newCondition();
    private Condition m_notFull = m_listLock.newCondition();
    private LinkedList<ListEntry> m_dataBufferList = new LinkedList<ListEntry>();
    private boolean m_synthComplete;
    private int m_unconsumedBytes;

    private int m_state;
    private AudioTrack m_tracker;

    private class ListEntry {
        final byte[] m_bytes;

        ListEntry(byte[] bytes) {
            m_bytes = bytes;
        }
    }

    private AudioTracker(int streamType, int sampleRateInHz, int channelConfig,
            int audioFormat) {
        m_streamType = streamType;
        m_sampleRate = sampleRateInHz;
        m_channelConfig = channelConfig;
        m_audioFormat = audioFormat;

        if (m_channelConfig == AudioFormat.CHANNEL_OUT_MONO)
            m_channelCount = 1;
        else
            m_channelCount = 2;

        if (m_audioFormat == AudioFormat.ENCODING_PCM_16BIT)
            m_bytesPersample = 2;
        else
            m_bytesPersample = 1;

        m_periodInFrames = m_sampleRate * TIMER_INTERVAL / 1000;

        m_bufferSize = m_periodInFrames * 2 * m_channelCount * m_bytesPersample;

        if (m_bufferSize < AudioTrack.getMinBufferSize(m_sampleRate,
                m_channelConfig, m_audioFormat)) {

            m_bufferSize = AudioTrack.getMinBufferSize(m_sampleRate,
                    m_channelConfig, m_audioFormat);

            m_periodInFrames = m_bufferSize
                    / (2 * m_bytesPersample * m_channelCount);

            IwdsLog.i(this, "Increase AudioTracker buffer size to "
                    + m_bufferSize);
        }

        IwdsLog.i(this, "AudioTracker buffer size " + m_bufferSize);

        m_controller = new Controller();
        m_callbackHandler = new CallbackHandler();
    }

    public static AudioTracker getInstance() {
        if (sInstance == null)
            sInstance = new AudioTracker(AudioManager.STREAM_MUSIC,
                    DEFAULT_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

        return sInstance;
    }

    public void initialize(AudioTrackInitListener listener) {
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        m_callbackHandler.setInitListener(listener);
        m_controller.initialize();
    }

    public void startPlay() {
        m_controller.start();
        m_controller.startPlay();
    }

    public void writeData(byte[] buffer) {
        m_controller.write(buffer);
    }

    public void pausePlay() {
        m_controller.pausePlay();
    }

    public void resumePlay() {
        m_controller.resumePlay();
    }

    public void waitComplete() {
        m_controller.waitComplete();
    }

    public void destroy() {
        if (sInstance == null)
            return;

        m_controller.release();

        m_controller.destroy();

        sInstance = null;

        IwdsLog.i(this, "AudioTracker destroy");
    }

    public int getState() {
        return m_controller.getState();
    }

    public void setTrackListener(AudioTrackListener listener) {
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        m_callbackHandler.setTrackListener(listener);
    }

    public void removeTrackListener() {
        m_callbackHandler.removeTrackListener();
    }

    public static String errorString(int errorCode) {
        String str = "";

        switch (errorCode) {
        case SUCCESS:
            str = "AudioTracker success";
            break;

        case ERROR_TRACK_INITIALIZE:
            str = "AudioTracker initialize failure";
            break;

        case ERROR_TRACK_START:
            str = "AudioTracker have not be initialized";
            break;

        case ERROR_TRACK_WRITE:
            str = "AudioTracker write only on playing state";
            break;

        case ERROR_TRACK_RESUME:
            str = "AudioTracker resume only on pause state";
            break;

        case ERROR_TRACK_PAUSE:
            str = "AudioTracker pause only on playing state";
            break;

        case ERROR_ILLEGAL_STATE:
            str = "AudioTracker have not be initialized";
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

        case S_PLAYING:
            str = "playing";
            break;

        case S_PAUSED:
            str = "paused";
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

    public interface AudioTrackInitListener {
        void onInitialize(int errorCode);
    }

    public interface AudioTrackListener {
        void onTrackStart();

        void onTrackPause();

        void onTrackResume();

        void onTrackComplete();

        void onTrackError(int errorCode);

        void onTrackDestroy();
    }

    private class CallbackHandler extends Handler {
        private static final int MSG_NOTIFY_TRACK_ERROR = 0;
        private static final int MSG_NOTIFY_TRACK_INITIALIZED = 1;
        private static final int MSG_NOTIFY_TRACK_START = 2;
        private static final int MSG_NOTIFY_TRACK_PAUSE = 3;
        private static final int MSG_NOTIFY_TRACK_RESUME = 4;
        private static final int MSG_NOTIFY_TRACK_PLAY_COMPLETE = 5;
        private static final int MSG_NOTIFY_TRACK_DESTROY = 6;

        private AudioTrackInitListener m_initListener;
        private AudioTrackListener m_trackListener;

        public void setTrackListener(AudioTrackListener listener) {
            m_trackListener = listener;
        }

        public void removeTrackListener() {
            m_trackListener = null;
        }

        public void setInitListener(AudioTrackInitListener listener) {
            m_initListener = listener;
        }

        public void notifyError(int errorCode) {
            Message msg = Message.obtain(this);

            msg.what = MSG_NOTIFY_TRACK_ERROR;
            msg.arg1 = errorCode;

            msg.sendToTarget();
        }

        public void notifyInitialized(int errorCode) {
            Message msg = Message.obtain(this);

            msg.what = MSG_NOTIFY_TRACK_INITIALIZED;
            msg.arg1 = errorCode;

            msg.sendToTarget();
        }

        public void notifyTrackStart() {
            Message msg = Message.obtain(this);

            msg.what = MSG_NOTIFY_TRACK_START;

            msg.sendToTarget();
        }

        public void notifyTrackPause() {
            Message msg = Message.obtain(this);

            msg.what = MSG_NOTIFY_TRACK_PAUSE;

            msg.sendToTarget();
        }

        public void notifyTrackResume() {
            Message msg = Message.obtain(this);

            msg.what = MSG_NOTIFY_TRACK_RESUME;

            msg.sendToTarget();
        }

        public void notifyTrackPlayComplete() {
            Message msg = Message.obtain(this);

            msg.what = MSG_NOTIFY_TRACK_PLAY_COMPLETE;

            msg.sendToTarget();
        }

        public void notifyTrackDestroy() {
            Message msg = Message.obtain(this);

            msg.what = MSG_NOTIFY_TRACK_DESTROY;

            msg.sendToTarget();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_NOTIFY_TRACK_ERROR:
                if (m_trackListener != null)
                    m_trackListener.onTrackError(msg.arg1);
                break;

            case MSG_NOTIFY_TRACK_INITIALIZED:
                if (m_initListener != null)
                    m_initListener.onInitialize(msg.arg1);
                break;

            case MSG_NOTIFY_TRACK_START:
                if (m_trackListener != null)
                    m_trackListener.onTrackStart();
                break;

            case MSG_NOTIFY_TRACK_PAUSE:
                if (m_trackListener != null)
                    m_trackListener.onTrackPause();
                break;

            case MSG_NOTIFY_TRACK_RESUME:
                if (m_trackListener != null)
                    m_trackListener.onTrackResume();
                break;

            case MSG_NOTIFY_TRACK_PLAY_COMPLETE:
                if (m_trackListener != null)
                    m_trackListener.onTrackComplete();
                break;

            case MSG_NOTIFY_TRACK_DESTROY:
                if (m_trackListener != null)
                    m_trackListener.onTrackDestroy();
                break;

            default:
                IwdsAssert.dieIf(this, true, "unknown message " + msg.what);
                break;
            }
        }
    }

    private class Producter {
        private ProducterHandler m_handler;
        private HandlerThread m_thread;

        public void enqueue(byte[] buffer) {
            m_handler.enqueue(buffer);
        }

        public void start() {
            IwdsAssert.dieIf(this, m_thread != null,
                    "Producter thread already start");

            m_thread = new HandlerThread("AudioTracker-Producter");
            m_thread.start();

            m_handler = new ProducterHandler(m_thread.getLooper());
        }

        public void stop() {
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

        private class ProducterHandler extends Handler {
            private static final int MSG_ENQUEUE = 0;
            private static final int MSG_QUIT = 1;

            public ProducterHandler(Looper looper) {
                super(looper);
            }

            private void enqueue(byte[] buffer) {
                if (m_thread == null)
                    return;

                final Message msg = Message.obtain(this);

                msg.what = MSG_ENQUEUE;
                msg.obj = buffer;

                msg.sendToTarget();
            }

            public void quit() {
                Message msg = Message.obtain(this);

                msg.what = MSG_QUIT;

                msg.sendToTarget();
            }

            private long getAudioLengthMs(int bytes) {
                int unconsumedFrames = m_unconsumedBytes
                        / (m_bytesPersample * m_channelCount);
                long estimatedTimeMs = unconsumedFrames * 1000 / m_sampleRate;

                return estimatedTimeMs;
            }

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_ENQUEUE:
                    try {
                        m_listLock.lock();

                        byte[] buffer = (byte[]) msg.obj;

                        m_stateLock.lock();

                        while (getAudioLengthMs(m_unconsumedBytes) > MAX_UNCONSUMED_AUDIO_MS
                                && (m_state == S_PLAYING || m_state == S_PAUSED)) {
                            m_stateLock.unlock();

                            IwdsLog.d(this, "Producter wait, size="
                                    + m_dataBufferList.size() + ", state="
                                    + stateString(m_state));

                            m_notFull.await();

                            IwdsLog.d(this, "Producter wakeup, size="
                                    + m_dataBufferList.size() + ", state="
                                    + stateString(m_state));

                            m_stateLock.lock();
                        }

                        if ((m_state != S_PLAYING) && (m_state != S_PAUSED)) {
                            IwdsLog.i(this,
                                    "state not on playing or paused, state= "
                                            + stateString(m_state));
                            m_stateLock.unlock();
                            return;
                        }

                        m_stateLock.unlock();

                        m_dataBufferList.add(new ListEntry(buffer));

                        IwdsLog.d(this, "after enqueue the buffer list size "
                                + m_dataBufferList.size());

                        m_unconsumedBytes += buffer.length;

                        m_readReady.signal();

                    } catch (InterruptedException e) {
                        IwdsLog.e(this, "Exception in wait buffer not full: "
                                + e.toString());

                    } finally {
                        m_listLock.unlock();
                    }

                    break;

                case MSG_QUIT:
                    m_listLock.lock();
                    m_dataBufferList.clear();
                    m_listLock.unlock();

                    m_thread.quit();
                    break;
                }
            }
        }
    }

    private class Consumer {
        private ConsumerThread m_thread;

        public void start() {
            IwdsAssert.dieIf(this, m_thread != null,
                    "Consumer thread already started");

            m_thread = new ConsumerThread("AudioTracker-Consumer");
            m_thread.start();
        }

        public void stop() {
            if (m_thread == null)
                return;

            m_thread.stopAndWait();

            m_thread = null;
        }

        private class ConsumerThread extends Thread {
            private Object m_isRunningLock = new Object();

            private boolean m_isRunning = false;
            private boolean m_requestStop = false;

            public ConsumerThread(String string) {
                super(string);
            }

            public void start() {
                super.start();

                synchronized (m_isRunningLock) {
                    while (!isRunning()) {
                        try {
                            m_isRunningLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            public void stopAndWait() {
                if (!isRunning())
                    return;

                synchronized (this) {
                    m_requestStop = true;
                }

                try {
                    join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            private boolean isRunning() {
                synchronized (m_isRunningLock) {
                    return m_isRunning;
                }
            }

            private void setRunning(boolean isRunning) {
                synchronized (m_isRunningLock) {
                    m_isRunning = isRunning;

                    m_isRunningLock.notifyAll();
                }
            }

            @Override
            public void run() {
                setRunning(true);

                for (;;) {
                    synchronized (this) {
                        if (m_requestStop)
                            break;
                    }

                    try {
                        m_listLock.lock();

                        m_stateLock.lock();

                        while ((m_dataBufferList.size() == 0 && m_state == S_PLAYING)
                                || m_state == S_PAUSED) {

                            if (m_state == S_PLAYING && m_synthComplete) {
                                IwdsLog.d(this,
                                        "Synth complete, going to flush buffer list, state="
                                                + stateString(m_state));
                                break;
                            }

                            m_stateLock.unlock();

                            IwdsLog.d(this, "Consumer wait, size="
                                    + m_dataBufferList.size() + ", state="
                                    + stateString(m_state));

                            m_readReady.await();

                            IwdsLog.d(this, "Consumer wakeup, size = "
                                    + m_dataBufferList.size() + ", state="
                                    + stateString(m_state));

                            m_stateLock.lock();
                        }

                        if (m_state == S_PLAYING) {

                            ListEntry entry = m_dataBufferList.poll();
                            if (entry != null) {

                                IwdsLog.d(this,
                                        "after dequeue the buffer list size "
                                                + m_dataBufferList.size());

                                byte[] buffer = entry.m_bytes;

                                m_unconsumedBytes -= buffer.length;

                                m_notFull.signal();

                                int offset = 0;
                                int writeBytes = 0;
                                while (offset < buffer.length) {
                                    writeBytes = m_tracker.write(buffer,
                                            offset, buffer.length - offset);

                                    offset += writeBytes;
                                }
                            }

                            /*
                             * Flush buffer list
                             */
                            if (m_dataBufferList.size() == 0 && m_synthComplete) {

                                m_stateLock.unlock();
                                m_listLock.unlock();

                                IwdsLog.d(this,
                                        "Consumer sleep, wait Producter post new data");

                                /*
                                 * Here must wait, producter may post new buffer
                                 * after consumer dequeue buffer list size to 0
                                 */
                                Thread.sleep(200);

                                m_listLock.lock();
                                m_stateLock.lock();

                                /*
                                 * Check buffer list again, make sure buffer
                                 * list size is 0
                                 */
                                if (m_dataBufferList.size() == 0
                                        && m_synthComplete) {

                                    IwdsLog.d(this,
                                            "Consumer wakeup, buffer list is null wakeup Controller and exit");

                                    m_speakComplete.signal();
                                    m_synthComplete = false;
                                    m_stateLock.unlock();
                                    break;
                                }
                            }

                        } else {

                            IwdsLog.d(this,
                                    "audio state not on playing or paused state, state="
                                            + stateString(m_state));

                            if (m_state == S_UNINITIALIZED
                                    || m_state == S_ERROR) {
                                m_stateLock.unlock();
                                break;
                            }

                        }

                        m_stateLock.unlock();

                    } catch (InterruptedException e) {
                        IwdsLog.e(this, "Exception in wait buffer not empty: "
                                + e.toString());

                    } finally {
                        m_listLock.unlock();
                    }
                }

                setRunning(false);
            }
        }
    }

    private class Controller {
        private static final int MSG_QUIT = 0;
        private static final int MSG_WAIT_COMPLETE = 1;

        private Producter m_producter;
        private Consumer m_consumer;
        private HandlerThread m_thread;
        private ControllerHandler m_handler;

        public Controller() {
            m_producter = new Producter();
            m_thread = new HandlerThread("AudioTracker-Controller");
            m_thread.start();
            m_handler = new ControllerHandler(m_thread.getLooper());

            m_consumer = new Consumer();
        }

        public void start() {
            m_consumer.start();
            m_producter.start();
        }

        public void initialize() {
            m_stateLock.lock();

            if (m_state == S_INITIALIZED) {
                m_stateLock.unlock();
                return;
            }

            if (m_state == S_UNINITIALIZED) {
                m_handler.initialize();

            } else {
                IwdsLog.e(this, "Failed to initialize track on illegal state "
                        + stateString(m_state));

                m_state = S_ERROR;
                m_callbackHandler.notifyInitialized(ERROR_TRACK_INITIALIZE);
            }

            m_stateLock.unlock();
        }

        public void startPlay() {
            m_stateLock.lock();

            if (m_state == S_PLAYING) {
                m_stateLock.unlock();
                return;
            }

            if (m_state == S_INITIALIZED) {
                m_handler.start();

            } else {
                IwdsLog.e(this, "Failed to start track on illegal state "
                        + stateString(m_state));

                m_state = S_ERROR;

                m_callbackHandler.notifyError(ERROR_TRACK_START);
            }

            m_stateLock.unlock();
        }

        public void write(byte[] buffer) {
            m_stateLock.lock();

            if (m_state == S_PLAYING || m_state == S_PAUSED) {
                m_handler.write(buffer);

            } else {
                IwdsLog.e(this,
                        "state not on playing or paused, ignore new data");
            }

            m_stateLock.unlock();
        }

        public void pausePlay() {
            m_stateLock.lock();

            if (m_state == S_PAUSED) {
                m_stateLock.unlock();
                return;
            }

            if (m_state == S_PLAYING) {
                m_handler.pause();

            } else {
                IwdsLog.e(this, "Failed to pause track on illegal state "
                        + stateString(m_state));

                m_state = S_ERROR;
                m_callbackHandler.notifyError(ERROR_TRACK_PAUSE);
            }

            m_stateLock.unlock();
        }

        public void resumePlay() {
            m_stateLock.lock();

            if (m_state == S_PLAYING) {
                m_stateLock.unlock();
                return;
            }

            if (m_state == S_PAUSED) {
                m_handler.resume();

            } else {
                IwdsLog.e(this, "Failed to resume track on illegal state "
                        + stateString(m_state));

                m_state = S_ERROR;

                m_callbackHandler.notifyError(ERROR_TRACK_RESUME);
            }
            m_stateLock.unlock();
        }

        public void release() {
            m_stateLock.lock();

            if (m_state == S_UNINITIALIZED) {
                m_stateLock.unlock();
                return;
            }

            m_handler.release();

            m_stateLock.unlock();
        }

        public void waitComplete() {
            m_stateLock.lock();

            if (m_state == S_UNINITIALIZED) {
                m_stateLock.unlock();
                return;
            }

            m_stateLock.unlock();

            /*
             * Remove messages which may be posted at thread wait
             */
            m_handler.removeMessages(MSG_WAIT_COMPLETE);

            m_handler.waitComplete();
        }

        public int getState() {
            try {
                m_stateLock.lock();

                return m_state;
            } finally {
                m_stateLock.unlock();
            }
        }

        private void stopController() {
            if (m_thread == null)
                return;

            m_listLock.lock();

            m_readReady.signal();
            m_notFull.signal();
            m_speakComplete.signal();

            m_listLock.unlock();

            m_handler.quit();

            try {
                m_thread.join();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            m_thread = null;

            IwdsLog.d(this, "Controller thread quit");
        }

        private void stopConsumer() {
            m_consumer.stop();
            IwdsLog.d(this, "Consumer thread quit");
        }

        private void stopProducter() {
            m_producter.stop();
            IwdsLog.d(this, "Producter thread quit");
        }

        public void destroy() {
            stopController();
            stopProducter();
            stopConsumer();

            m_callbackHandler.notifyTrackDestroy();
        }

        private class ControllerHandler extends Handler {
            public ControllerHandler(Looper looper) {
                super(looper);
            }

            public void initialize() {
                try {
                    if (m_tracker == null) {
                        IwdsLog.i(this, "Try to create new AudioTrack");

                        m_tracker = new AudioTrack(m_streamType, m_sampleRate,
                                m_channelConfig, m_audioFormat, m_bufferSize,
                                AudioTrack.MODE_STREAM);
                    }

                } catch (IllegalArgumentException e) {
                    IwdsLog.e(this,
                            "Failed to create AudioTrack, AudioTrack throw exception");

                    m_state = S_ERROR;

                    m_callbackHandler.notifyInitialized(ERROR_TRACK_INITIALIZE);
                    return;
                }

                if (m_tracker.getState() == AudioTrack.STATE_INITIALIZED) {
                    m_state = S_INITIALIZED;

                    m_callbackHandler.notifyInitialized(SUCCESS);

                } else {
                    IwdsLog.e(this,
                            "Failed to create AudioRecord, native exception");

                    m_tracker.release();
                    m_state = S_ERROR;

                    m_callbackHandler.notifyInitialized(ERROR_TRACK_INITIALIZE);
                }
            }

            public void start() {
                m_tracker.play();
                m_state = S_PLAYING;

                m_callbackHandler.notifyTrackStart();
            }

            public void write(byte[] buffer) {
                m_producter.enqueue(buffer);
            }

            public void pause() {
                m_tracker.pause();
                m_state = S_PAUSED;

                m_callbackHandler.notifyTrackPause();
            }

            public void resume() {
                m_tracker.play();
                m_state = S_PLAYING;

                if (m_listLock.tryLock()) {
                    try {
                        m_readReady.signal();
                        m_notFull.signal();

                    } finally {
                        m_listLock.unlock();
                    }
                }

                m_callbackHandler.notifyTrackResume();
            }

            public void release() {
                if (m_tracker == null)
                    return;

                m_tracker.flush();
                m_tracker.release();

                m_tracker = null;
                m_state = S_UNINITIALIZED;
            }

            public void waitComplete() {
                Message msg = Message.obtain(this);

                msg.what = MSG_WAIT_COMPLETE;

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
                case MSG_WAIT_COMPLETE:
                    try {
                        m_listLock.lock();

                        if (m_synthComplete) {
                            IwdsLog.i(this, "Controller already waiting here");
                            return;
                        }

                        m_synthComplete = true;

                        IwdsLog.d(this,
                                "Controller wait, wakeup consumer to flush buffer list");

                        m_readReady.signal();

                        /*
                         * Here must wait consumer flush buffer list
                         */
                        m_speakComplete.await();

                        IwdsLog.d(this,
                                "Controller wakeup, consumer flush done or controller destroy");

                    } catch (InterruptedException e) {
                        IwdsLog.e(this,
                                "Exception in wait comsumer flush buffer list: "
                                        + e.toString());

                    } finally {
                        m_listLock.unlock();
                    }

                    /*
                     * Notify AudioTrack play done, producter and consumer could
                     * be stopped
                     */
                    m_callbackHandler.notifyTrackPlayComplete();
                    break;

                case MSG_QUIT:
                    m_thread.quit();
                    break;
                }
            }
        }
    }
}
