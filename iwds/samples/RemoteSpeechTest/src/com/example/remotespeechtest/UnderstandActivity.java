package com.example.remotespeechtest;

import com.example.remotespeechtest.utils.XmlParser;
import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceClient.ConnectionCallbacks;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.smartspeech.RemoteSpeechConstant;
import com.ingenic.iwds.smartspeech.RemoteSpeechErrorCode;
import com.ingenic.iwds.smartspeech.RemoteSpeechUnderstander;
import com.ingenic.iwds.smartspeech.RemoteSpeechServiceManager;
import com.ingenic.iwds.smartspeech.RemoteSpeechUnderstander.RemoteUnderstanderListener;
import com.ingenic.iwds.smartspeech.RemoteStatusListener;
import com.ingenic.iwds.smartspeech.business.RemoteBusiness;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class UnderstandActivity extends Activity implements ConnectionCallbacks {
    private ServiceClient m_client;
    private RemoteSpeechServiceManager m_service;

    private static final int MSG_REMOTE_STATUS = 0;
    private static final int MSG_START_SPEECH = 1;
    private static final int MSG_STOP_SPEECH = 2;
    private static final int MSG_CANCEL_SPEECH = 3;

    private int m_remoteStatus = RemoteSpeechErrorCode.ERROR_REMOTE_DISCONNECTED;
    private Context m_context;

    private EditText m_text;
    private Button m_startButton;
    private Button m_stopButton;
    private Button m_cancelButton;

    private Toast m_toast;

    private boolean m_speechStart;

    private RemoteSpeechUnderstander m_understander;

    private void initView() {
        m_text = (EditText) findViewById(R.id.speech_text);

        m_startButton = (Button) findViewById(R.id.speech_start);
        m_stopButton = (Button) findViewById(R.id.speech_stop);
        m_cancelButton = (Button) findViewById(R.id.speech_cancel);

        m_startButton.setOnClickListener(m_buttonListener);
        m_stopButton.setOnClickListener(m_buttonListener);
        m_cancelButton.setOnClickListener(m_buttonListener);

        m_startButton.setEnabled(true);
        m_stopButton.setEnabled(false);
        m_cancelButton.setEnabled(false);

        m_toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_understander);

        initView();

        m_context = this;

        m_client = new ServiceClient(this,
                ServiceManagerContext.SERVICE_REMOTE_SPEECH, this);
    }

    @Override
    protected void onResume() {
        IwdsLog.d(this, "onResume");
        super.onResume();

        m_client.connect();
    }

    @Override
    protected void onPause() {
        IwdsLog.d(this, "onPause");

        super.onPause();

        m_client.disconnect();
    }

    @Override
    protected void onDestroy() {
        IwdsLog.d(this, "onDestroy");

        super.onDestroy();
    }

    @Override
    public void onConnected(ServiceClient serviceClient) {
        IwdsLog.d(this, "Remote speech service connected");

        m_understander = RemoteSpeechUnderstander.getInstance();
        m_understander.setListener(m_understanderListener);

        m_service = (RemoteSpeechServiceManager) m_client
                .getServiceManagerContext();

        m_service.registerRemoteStatusListener(m_remoteStatusListener);
    }

    @Override
    public void onDisconnected(ServiceClient serviceClient, boolean unexpected) {
        IwdsLog.d(this, "Remote speech service disconnected");

        if (m_speechStart) {
            m_service.requestCancelUnderstand(m_understander);
            m_speechStart = false;
        }

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
        m_understander.clearParameter();
        m_understander.setParameter(RemoteSpeechConstant.ENGINE_TYPE, "cloud");
        m_understander.setParameter(RemoteSpeechConstant.LANGUAGE, "zh_cn");
        m_understander.setParameter(RemoteSpeechConstant.DOMAIN, "iat");
        m_understander.setParameter(RemoteSpeechConstant.ACCENT, "mandarin");
        m_understander.setParameter(RemoteSpeechConstant.VAD_BOS, "4000");
        m_understander.setParameter(RemoteSpeechConstant.VAD_EOS, "1000");
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
            case R.id.speech_start:
                Message.obtain(m_handler, MSG_START_SPEECH).sendToTarget();
                break;

            case R.id.speech_stop:
                Message.obtain(m_handler, MSG_STOP_SPEECH).sendToTarget();
                break;

            case R.id.speech_cancel:
                Message.obtain(m_handler, MSG_CANCEL_SPEECH).sendToTarget();
                break;
            }
        }
    };

    private RemoteUnderstanderListener m_understanderListener = new RemoteUnderstanderListener() {

        @Override
        public void onListeningStatus(boolean isListening) {
            IwdsLog.d(this, "onListeningStatus " + isListening);
        }

        @Override
        public void onBeginOfSpeech() {
            m_startButton.setEnabled(false);
            m_stopButton.setEnabled(true);
            m_cancelButton.setEnabled(true);

            IwdsLog.d(this, "onBeginOfSpeech");
            showTip("开始说话");
        }

        @Override
        public void onEndOfSpeech() {
            m_startButton.setEnabled(true);
            m_stopButton.setEnabled(false);
            m_cancelButton.setEnabled(false);

            IwdsLog.d(this, "onEndOfSpeech");
            showTip("结束说话，正在识别...");
        }

        @Override
        public void onError(int errorCode) {
            m_speechStart = false;

            m_startButton.setEnabled(true);
            m_stopButton.setEnabled(false);
            m_cancelButton.setEnabled(false);

            IwdsLog.d(this, "onError " + errorCode);
            showTip("errorCode " + errorCode);
            m_text.setText("识别错误：" + errorCode);
        }

        @Override
        public void onVolumeChanged(int volume) {
            IwdsLog.d(this, "onVolumeChanged " + volume);
            showTip("正在说话，音量大小:" + volume);
        }

        @Override
        public void onCancel() {
            m_startButton.setEnabled(true);
            m_stopButton.setEnabled(false);
            m_cancelButton.setEnabled(false);

            IwdsLog.d(this, "onCancel");
            showTip("识别完成");
        }

        @Override
        public void onResult(RemoteBusiness result) {
            m_startButton.setEnabled(true);
            m_stopButton.setEnabled(false);
            m_cancelButton.setEnabled(false);
            m_speechStart = false;
            IwdsLog.d(this, "onResult " + result);
            String text = result.toString();
            m_text.append(text);
            m_text.setSelection(text.length());
        }

    };

    private Handler m_handler = new Handler() {

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
                m_startButton.setEnabled(false);
                m_stopButton.setEnabled(false);
                m_cancelButton.setEnabled(false);

                m_text.setText(null);
                setParameters();
                m_service.requestStartUnderstand(m_understander);
                m_speechStart = true;
                break;

            case MSG_STOP_SPEECH:
                m_startButton.setEnabled(true);
                m_stopButton.setEnabled(false);
                m_cancelButton.setEnabled(false);

                showTip("停止听写");
                m_service.requestStopUnderstand(m_understander);
                break;

            case MSG_CANCEL_SPEECH:
                m_startButton.setEnabled(true);
                m_stopButton.setEnabled(false);
                m_cancelButton.setEnabled(false);

                showTip("取消听写");
                m_speechStart = false;
                m_service.requestCancelUnderstand(m_understander);
                break;

            default:
                IwdsAssert.dieIf(this, true, "unknown message");
                break;
            }
        }

    };
}
