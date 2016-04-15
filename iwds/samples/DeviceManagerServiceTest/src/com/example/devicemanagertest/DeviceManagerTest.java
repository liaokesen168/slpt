package com.example.devicemanagertest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceClient.ConnectionCallbacks;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.devicemanager.DeviceManagerServiceManager;
import com.ingenic.iwds.utils.IwdsLog;

public class DeviceManagerTest extends Activity implements ConnectionCallbacks {
    private static final String LOG_TAG = "DeviceManagerTest";

    private ServiceClient m_client;
    private DeviceManagerServiceManager m_service;

    private TextView m_text;
    private Button m_setRight;
    private Button m_setLeft;
    private Button m_getHand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_manager_test);

        m_text = (TextView) findViewById(R.id.log);
        m_setRight = (Button) findViewById(R.id.set_right_button);
        m_setLeft = (Button) findViewById(R.id.set_left_button);
        m_getHand = (Button) findViewById(R.id.get_hand_button);

        m_setRight.setOnClickListener(m_listener);
        m_setLeft.setOnClickListener(m_listener);
        m_getHand.setOnClickListener(m_listener);

        m_client = new ServiceClient(this,
                ServiceManagerContext.SERVICE_DEVICE_MANAGER, this);
        m_client.connect();

        IntentFilter filter = new IntentFilter(
                DeviceManagerServiceManager.ACTION_WEAR_ON_LEFT_HAND);
        filter.addAction(DeviceManagerServiceManager.ACTION_WEAR_ON_RIGHT_HAND);
        registerReceiver(m_receiver, filter);
    }

    private final BroadcastReceiver m_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DeviceManagerServiceManager.ACTION_WEAR_ON_LEFT_HAND
                    .equals(action)) {
                Log.i(LOG_TAG, "ACTION_WEAR_ON_LEFT_HAND");
            } else if (DeviceManagerServiceManager.ACTION_WEAR_ON_RIGHT_HAND
                    .equals(action)) {
                Log.i(LOG_TAG, "ACTION_WEAR_ON_RIGHT_HAND");
            }
        }
    };

    private OnClickListener m_listener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.set_right_button:
                IwdsLog.d(this, "set right hand");
                m_service.setWearOnRightHand(true);
                break;

            case R.id.set_left_button:
                IwdsLog.d(this, "set left hand");
                m_service.setWearOnRightHand(false);
                break;

            case R.id.get_hand_button:
                if (m_service.isWearOnRightHand()) {
                    m_text.setText("on right hand");
                } else {
                    m_text.setText("on left hand");
                }
                break;
            }
        }
    };

    @Override
    public void onConnected(ServiceClient serviceClient) {
        m_service = (DeviceManagerServiceManager) m_client
                .getServiceManagerContext();
    }

    @Override
    public void onDisconnected(ServiceClient serviceClient, boolean unexpected) {

    }

    @Override
    public void onConnectFailed(ServiceClient serviceClient,
            ConnectFailedReason reason) {

    }
}
