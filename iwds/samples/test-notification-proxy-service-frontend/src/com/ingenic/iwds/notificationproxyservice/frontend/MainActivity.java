package com.ingenic.iwds.notificationproxyservice.frontend;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.ingenic.iwds.app.Note;
import com.ingenic.iwds.app.NotificationProxyServiceManager;
import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.utils.IwdsLog;

/**
 * 演示用 NotificationProxyServiceManager 发送和取消通知。
 * 用 notify 发送通知，用 cancel 取消通知，用 cancelAll 取消所有通知。
 *
 * 注意：需要和 test-notification-proxy-service-backend 配合使用，
 *      这两个应用都要安装到一个设备上。
 */
public class MainActivity extends Activity implements
        ServiceClient.ConnectionCallbacks {
    private ServiceClient m_client;
    private NotificationProxyServiceManager m_service;

    private Button m_btnNotify;
    private Button m_btnCancel;
    private Button m_btnCancelAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_btnNotify = (Button) findViewById(R.id.btnNotify);
        m_btnCancel = (Button) findViewById(R.id.btnCancel);
        m_btnCancelAll = (Button) findViewById(R.id.btnCancelAll);
        m_client = new ServiceClient(this,
                ServiceManagerContext.SERVICE_NOTIFICATION_PROXY, this);
        m_client.connect();
    }
  @Override
    public void onConnectFailed(ServiceClient serviceClient, ConnectFailedReason reason) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onConnected(ServiceClient serviceClient) {
        // TODO Auto-generated method stub
        m_service = (NotificationProxyServiceManager) m_client
                .getServiceManagerContext();
        if (m_service == null)
            return ;

        m_btnNotify.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                /* 发送一个通知 */
                boolean ok = m_service.notify(0, new Note("iwds", "test"));
                IwdsLog.i(this, "notify return: " + ok);
            }
        });

        m_btnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                /* 撤销 id 为 0 的通知 */
                m_service.cancel(0);
            }
        });

        m_btnCancelAll.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                /* 撤销这个 package 发出的所有通知 */
                m_service.cancelAll();
            }
        });

        m_btnNotify.setEnabled(true);
        m_btnCancel.setEnabled(true);
        m_btnCancelAll.setEnabled(true);
    }

    @Override
    public void onDisconnected(ServiceClient serviceClient, boolean unexpected) {
        // TODO Auto-generated method stub
        
    }
}
