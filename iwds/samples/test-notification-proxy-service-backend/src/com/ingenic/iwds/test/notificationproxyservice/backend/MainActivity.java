package com.ingenic.iwds.test.notificationproxyservice.backend;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.ingenic.iwds.app.Note;
import com.ingenic.iwds.app.NotificationProxyServiceManager;
import com.ingenic.iwds.app.NotificationServiceBackend;
import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.utils.IwdsLog;

/**
 * 演示用 NotificationProxyServiceManager 注册 backend 并监控通知。
 * 通过 registerBackend 注册 backend。在 onHandle 监控到其他应用发出通知，
 * onCancel 监控到其他应用取消某个通知，在 onCancelAll 监控到其他应用取消此
 * 应用发出的所有通知。
 * 
 * 注意：需要和 test-notification-proxy-service-frontend 配合使用，
 *      这两个应用都要安装到一个设备上。
 */
public class MainActivity extends Activity implements
        ServiceClient.ConnectionCallbacks {
    private ServiceClient m_client;
    private NotificationProxyServiceManager m_service;

    private TextView m_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_text = (TextView) findViewById(R.id.text);
        m_client = new ServiceClient(this,
                ServiceManagerContext.SERVICE_NOTIFICATION_PROXY, this);
        m_client.connect();

    }

    private class NoteHandler extends NotificationServiceBackend {

        /*
         * 监控到一个通知 note 被包名为 packageName 的 app 发出
         */
        @Override
        public boolean onHandle(String packageName, int id, Note note) {
            String info = "Handle from: " + packageName + ", Id: " + id
                    + ", Note: " + note.toString();

            m_text.setText(info);

            IwdsLog.i(this, info);

            return true;
        }

        /*
         * 监控到包名为 packageName 的 app 发出的某个通知被撤销
         */
        @Override
        public void onCancel(String packageName, int id) {
            String info = "Cancel from: " + packageName + ", Id: " + id;

            m_text.setText(info);

            IwdsLog.i(this, info);
        }

        /*
         * 监控到包名为 packageName 的 app 发出的所有通知都被撤销
         */
        @Override
        public void onCancelAll(String packageName) {
            String info = "CancelAll from: " + packageName;

            m_text.setText(info);

            IwdsLog.i(this, info);
        }

    }

    @Override
    public void onConnected(ServiceClient serviceClient) {
        // TODO Auto-generated method stub
        if (m_client == null){
            IwdsLog.i(this, "m_client is null");
            return ;
        }

        m_service = (NotificationProxyServiceManager) m_client
                    .getServiceManagerContext();
        if (m_service == null){
            IwdsLog.i(this, "m_service is null");
            return ;
        }

        boolean ok = m_service.registerBackend(new NoteHandler(),
                    "396bdc12-b834-bc70-f12c-1196ce75f99c");
        IwdsLog.i(this, "registerBackend return: " + ok);
    }

    @Override
    public void onDisconnected(ServiceClient serviceClient, boolean unexpected) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onConnectFailed(ServiceClient serviceClient, ConnectFailedReason reason) {
        // TODO Auto-generated method stub
        
    }
}
