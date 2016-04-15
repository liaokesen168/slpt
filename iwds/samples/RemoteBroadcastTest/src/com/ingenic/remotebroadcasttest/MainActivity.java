package com.ingenic.remotebroadcasttest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceClient.ConnectionCallbacks;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.remotebroadcast.RemoteBroadcastManager;
import com.ingenic.iwds.remotebroadcast.RemoteBroadcastReceiver;
import com.ingenic.iwds.remotebroadcast.RemoteBroadcastManager.RemoteBroadcastCallback;
import com.ingenic.iwds.utils.IwdsLog;

public class MainActivity extends Activity implements ConnectionCallbacks, RemoteBroadcastCallback {
    private ServiceClient mClient;
    private RemoteBroadcastManager mManager;

    /**
     * 远程广播接收器，接收对端{@link #sendBroadcast(Intent)}
     * 发送的广播。测试本地registerRemoteReceiver(RemoteBroadcastReceiver)接口
     */
    private RemoteBroadcastReceiver mReceiver = new RemoteBroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            IwdsLog.i(this, "receive: " + action);
            Toast.makeText(context, action + ":" + intent.getIntExtra("value", 0),
                    Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 普通广播接收器，测试对端sendRemoteBroadcast(Intent)接口。
     */
    private BroadcastReceiver mR = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Toast.makeText(MainActivity.this, action + ":" + intent.getIntExtra("value", 0),
                    Toast.LENGTH_SHORT).show();
        }
    };

    private View.OnClickListener mClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.broadcast_normal:
                if (mManager != null) {
                    //发送普通广播，由对端RemoteBroadcastReceiver接收。测试registerRemoteReceiver(RemoteBroadcastReceiver)。
                    Intent intent = new Intent("remotebroadcastreceiver_test");
                    intent.putExtra("value", 1);
                    sendBroadcast(intent);
                }
                break;

            case R.id.broadcast_remote:
                if (mManager != null) {
                    //测试sendRemoteBroadcast(Intent)，由对端普通接收器（BroadcastReceiver）接收
                    Intent it = new Intent("remotebroadcast_test");
                    it.putExtra("value", 2);
                    mManager.sendRemoteBroadcast(it);
                }
                break;

            default:
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mClient = new ServiceClient(this, ServiceManagerContext.SERVICE_REMOTE_BROADCAST, this);
        mClient.connect();

        findViewById(R.id.broadcast_normal).setOnClickListener(mClick);
        findViewById(R.id.broadcast_remote).setOnClickListener(mClick);
        registerReceiver(mR, new IntentFilter("remotebroadcast_test"));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mR);
        mClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onConnected(ServiceClient serviceClient) {
        IwdsLog.i(this, "onConnected");
        mManager = (RemoteBroadcastManager) serviceClient.getServiceManagerContext();
        mManager.registerRemoteBroadcastCallback(this);
        mManager.registerRemoteReceiver(mReceiver, new IntentFilter("remotebroadcastreceiver_test"));
    }

    @Override
    public void onDisconnected(ServiceClient serviceClient, boolean unexpected) {
        IwdsLog.i(this, "onDisconnected: " + unexpected);
        if (mManager != null) {
            mManager.unregisterRemoteReceiver(mReceiver);
            mManager.registerRemoteBroadcastCallback(null);
        }
        mManager = null;
    }

    @Override
    public void onConnectFailed(ServiceClient serviceClient, ConnectFailedReason reason) {
        IwdsLog.i(this, "connect failed: " + reason);
    }

    @Override
    public void onSendResult(Intent intent, String permission, int resultCode) {
        IwdsLog.i(this, "send result: " + intent + ", permission:" + permission + ", code: "
                + resultCode);
    }

    @Override
    public void onCallerError(int callerId) {
        IwdsLog.e(this, "Error caller: " + callerId);
    }
}
