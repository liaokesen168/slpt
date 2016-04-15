package com.example.remotespeechtest;

import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceClient.ConnectionCallbacks;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.smartspeech.RemoteSpeechConstant;
import com.ingenic.iwds.smartspeech.RemoteSpeechErrorCode;
import com.ingenic.iwds.smartspeech.RemoteSpeechServiceManager;
import com.ingenic.iwds.smartspeech.RemoteSpeechSynthesizer;
import com.ingenic.iwds.smartspeech.RemoteSpeechSynthesizer.RemoteSynthesizerListener;
import com.ingenic.iwds.smartspeech.RemoteStatusListener;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SynthesisActivity extends Activity implements ConnectionCallbacks {
    private ServiceClient m_client;
    private RemoteSpeechServiceManager m_service;

    private static final int MSG_REMOTE_STATUS = 0;
    private static final int MSG_START_SPEECH = 1;
    private static final int MSG_STOP_SPEECH = 2;
    private static final int MSG_PAUSE_SPEECH = 3;
    private static final int MSG_RESUME_SPEECH = 4;

    private int m_remoteStatus = RemoteSpeechErrorCode.ERROR_REMOTE_DISCONNECTED;
    private Context m_context;
    private Toast m_toast;

    private RemoteSpeechSynthesizer m_speaker;

    private Button m_startButton;
    private Button m_stopButton;
    private Button m_pauseButton;
    private Button m_resumeButton;
    private EditText m_text;

    private void initView() {
        m_startButton = (Button) findViewById(R.id.speech_play);
        m_stopButton = (Button) findViewById(R.id.speech_cancel);
        m_pauseButton = (Button) findViewById(R.id.speech_pause);
        m_resumeButton = (Button) findViewById(R.id.speech_resume);
        m_text = (EditText) findViewById(R.id.speech_text);

        m_startButton.setOnClickListener(m_buttonListener);
        m_stopButton.setOnClickListener(m_buttonListener);
        m_pauseButton.setOnClickListener(m_buttonListener);
        m_resumeButton.setOnClickListener(m_buttonListener);

        m_toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_synthesizer);

        initView();

        m_context = this;

        m_client = new ServiceClient(this,
                ServiceManagerContext.SERVICE_REMOTE_SPEECH, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        m_client.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();

        m_client.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConnected(ServiceClient serviceClient) {
        IwdsLog.d(this, "Remote speech service connected");

        m_speaker = RemoteSpeechSynthesizer.getInstance();
        m_speaker.setListener(m_speakListener);

        m_service = (RemoteSpeechServiceManager) m_client
                .getServiceManagerContext();
        m_service.registerRemoteStatusListener(m_remoteStatusListener);
    }

    @Override
    public void onDisconnected(ServiceClient serviceClient, boolean unexpected) {
        IwdsLog.d(this, "Remote speech service disconnected");
        m_service.unregisterRemoteStatusListener(m_remoteStatusListener);
    }

    @Override
    public void onConnectFailed(ServiceClient serviceClient,
            ConnectFailedReason reason) {
        IwdsLog.d(this, "Remote speech service connect failed");
    }

    private void showTip(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_toast.setText(str);
                m_toast.show();
            }
        });
    }

    private void setParameters() {
        m_speaker.setParameter(RemoteSpeechConstant.ENGINE_TYPE, "local");
        m_speaker.setParameter(RemoteSpeechSynthesizer.SPEED, "50");
        m_speaker.setParameter(RemoteSpeechSynthesizer.PITCH, "50");
        m_speaker.setParameter(RemoteSpeechSynthesizer.VOLUME, "20");
        m_speaker.setParameter(RemoteSpeechSynthesizer.VOICE_NAME, "xiaoyan");
        m_speaker.setText(m_context.getString(R.string.speak_content));
    }

    private RemoteStatusListener m_remoteStatusListener = new RemoteStatusListener() {

        @Override
        public void onAvailable(int errorCode) {
            Message.obtain(m_handler, MSG_REMOTE_STATUS, errorCode, 0)
                    .sendToTarget();
        }
    };

    private OnClickListener m_buttonListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (m_remoteStatus != RemoteSpeechErrorCode.SUCCESS) {
                Message.obtain(m_handler, MSG_REMOTE_STATUS, m_remoteStatus, 0)
                        .sendToTarget();
                return;
            }

            switch (v.getId()) {
            case R.id.speech_play:
                Message.obtain(m_handler, MSG_START_SPEECH).sendToTarget();
                break;

            case R.id.speech_cancel:
                Message.obtain(m_handler, MSG_STOP_SPEECH).sendToTarget();
                break;

            case R.id.speech_pause:
                Message.obtain(m_handler, MSG_PAUSE_SPEECH).sendToTarget();
                break;

            case R.id.speech_resume:
                Message.obtain(m_handler, MSG_RESUME_SPEECH).sendToTarget();
                break;
            }
        }
    };

    private RemoteSynthesizerListener m_speakListener = new RemoteSynthesizerListener() {

        @Override
        public void onSpeakingStatus(boolean isSpeaking) {
            IwdsLog.d(this, "onSpeakingStatus");
        }

        @Override
        public void onSynthCompleted(int errorCode) {
            IwdsLog.d(this, "onCompleted " + errorCode);
            showTip("合成已完成");
        }

        @Override
        public void onSpeakBegin() {
            IwdsLog.d(this, "onSpeakBegin");
            showTip("开始合成");
        }

        @Override
        public void onSpeakPaused() {
            IwdsLog.d(this, "onSpeakPaused");
            showTip("播报已暂停");
        }

        @Override
        public void onSynthProgress(int progress) {
            IwdsLog.d(this, "onSpeakProgress " + progress);
            showTip("合成进度: " + progress);
        }

        @Override
        public void onSpeakResumed() {
            IwdsLog.d(this, "onSpeakResumed");
            showTip("继续播放");
        }

        @Override
        public void onError(int errorCode) {
            IwdsLog.d(this, "onError " + errorCode);
            showTip("onError " + errorCode);
            m_text.setText(null);
            m_text.setText("合成错误: " + errorCode);
        }

        @Override
        public void onCancel() {
            IwdsLog.d(this, "onCancel");
            showTip("播放已完成");
        }

    };

    private Handler m_handler = new Handler() {
        boolean result = false;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_REMOTE_STATUS:
                m_remoteStatus = msg.arg1;

                if (msg.arg1 == RemoteSpeechErrorCode.SUCCESS) {
                    showTip(m_context.getString(R.string.remote_connected));

                } else if (msg.arg1 == RemoteSpeechErrorCode.ERROR_REMOTE_DISCONNECTED) {
                    showTip(m_context.getString(R.string.remote_disconnected));

                } else if (msg.arg1 == RemoteSpeechErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                    showTip(m_context
                            .getString(R.string.remote_componment_not_install));

                } else if (msg.arg1 == RemoteSpeechErrorCode.ERROR_REMOTE_SERVICE_KILLED) {
                    showTip(m_context.getString(R.string.remote_service_killed));

                } else {
                    IwdsAssert.dieIf(this, true,
                            "unsupport error code for remote status");
                }
                break;

            case MSG_START_SPEECH:
                m_text.setText(null);
                m_text.setText(m_context.getString(R.string.speak_content));

                setParameters();
                result = m_service.requestStartSpeak(m_speaker);
                if (!result)
                    showTip("请求失败");

                break;

            case MSG_STOP_SPEECH:
                showTip("取消合成");
                result = m_service.requestCancelSpeak(m_speaker);
                if (!result)
                    showTip("请求失败");
                break;

            case MSG_PAUSE_SPEECH:
                showTip("暂停合成");
                result = m_service.requestPauseSpeak(m_speaker);
                if (!result)
                    showTip("请求失败");
                break;

            case MSG_RESUME_SPEECH:
                showTip("恢复合成");
                result = m_service.requestResumeSpeak(m_speaker);
                if (!result)
                    showTip("请求失败");
                break;

            default:
                IwdsAssert.dieIf(this, true, "unknown message");
                break;
            }
        }
    };
}
