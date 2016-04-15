package com.ingenic.iwds.jarapidemo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;

import com.ingenic.iwds.app.Note;
import com.ingenic.iwds.app.NotificationProxyServiceManager;
import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.utils.IwdsLog;

import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * 演示用 NotificationProxyServiceManager 发送和取消通知。用 notify 发送通知，
 * 用 cancel 取消通知，用 cancelAll 取消所有通知。
 * 
 * 注意：没有演示通过 registerBackend 注册 backend，并用 backend 监控所有通知。
 * 实际上 AmazingLauncher 就是一个 backend，能监控到所有通知。
 */
public class NotificationFrontendFragment extends DemoFragment {

    private View mContentView;
    private Context mContext;

    private ServiceClient m_client;
    private NotificationProxyServiceManager m_service;

    private Button m_notifyButton;
    private Button m_cancelButton;
    private Button m_cancelAllButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mContext = getActivity();
        mContentView = inflater.inflate(R.layout.notification_frontend, container, false);
        
        IwdsLog.i(this, "onCreate");

        m_notifyButton = (Button)mContentView.findViewById(R.id.notify_button);
        m_cancelButton = (Button)mContentView.findViewById(R.id.cancel_button);
        m_cancelAllButton = (Button)mContentView.findViewById(R.id.cancel_all_button);

        m_client = new ServiceClient(mContext,
                ServiceManagerContext.SERVICE_NOTIFICATION_PROXY, callbacks);
        m_client.connect();

        return mContentView;
    }

    private ServiceClient.ConnectionCallbacks
                callbacks = new ServiceClient.ConnectionCallbacks() {
        @Override
        public void onConnected(ServiceClient serviceClient) {
            m_service = (NotificationProxyServiceManager) m_client
                    .getServiceManagerContext();

            m_notifyButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean ok = m_service.notify(0, new Note("iwds", "test"));
                    IwdsLog.i(this, "notify return: " + ok);
                }
            });

            m_cancelButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    m_service.cancel(0);
                }
            });

            m_cancelAllButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    m_service.cancelAll();
                }
            });

            m_notifyButton.setEnabled(true);
            m_cancelButton.setEnabled(true);
            m_cancelAllButton.setEnabled(true);
        }

        @Override
        public void onDisconnected(ServiceClient serviceClient, boolean unexpected) {

        }

        @Override
        public void onConnectFailed(ServiceClient serviceClient, ConnectFailedReason reason) {

        }
    };
    
}
