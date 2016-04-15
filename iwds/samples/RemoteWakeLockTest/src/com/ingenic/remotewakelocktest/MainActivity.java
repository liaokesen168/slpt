package com.ingenic.remotewakelocktest;

import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceClient.ConnectionCallbacks;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.remotewakelock.RemoteWakeLockManager;
import com.ingenic.iwds.remotewakelock.RemoteWakeLockManager.RemoteWakeLock;
import com.ingenic.iwds.remotewakelock.RemoteWakeLockManager.RemoteWakeLockCallback;
import android.app.Activity;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity implements ConnectionCallbacks, RemoteWakeLockCallback {

    private ServiceClient mClient;
    private RemoteWakeLockManager mManager;
    private RemoteWakeLockManager.RemoteWakeLock mWakeLock;

    private View.OnClickListener mClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.acqure:
                //测试持远程锁，持远程锁与获得远程锁的概念不一样，远程锁的API参考PowerManager.WakeLock设计
                if (mWakeLock != null) {
                    mWakeLock.acquire();
                }
                break;
            case R.id.release:
                //测试释放远程锁，释放远程锁并不代表销毁远程锁，释放之后还是可以再次持该远程锁。远程锁的API参考PowerManager.WakeLock设计
                if (mWakeLock != null) {
                    mWakeLock.release();
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

        //IWDS标准用法，获得服务的客户端，并且连接服务。
        mClient = new ServiceClient(this, ServiceManagerContext.SERVICE_REMOTE_WAKELOCK, this);
        mClient.connect();

        findViewById(R.id.acqure).setOnClickListener(mClick);
        findViewById(R.id.release).setOnClickListener(mClick);
    }

    @Override
    protected void onDestroy() {
        //IWDS标准用法，退出应用时，断开与服务的连接。
        if (mClient != null) {
            mClient.disconnect();
        }

        super.onDestroy();
    }

    @Override
    public void onConnected(ServiceClient serviceClient) {//服务连接成功回调方法
        //获得远程锁管理器
        mManager = (RemoteWakeLockManager) serviceClient.getServiceManagerContext();
        //注册远程锁回调接口
        mManager.registerRemoteWakeLockCallback(this);
        //获得远程锁，获得远程锁并不是持远程锁，远程锁API参考PowerManager.WakeLock设计
        mWakeLock = mManager.newRemoteWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "wakelock_test");
    }

    @Override
    public void onDisconnected(ServiceClient serviceClient, boolean unexpected) {//服务断开回调方法
        if (mManager != null) {
            //在断开服务时，如果注册了远程锁回调接口，需要注销，否则将可能导致内存泄露等问题
            mManager.registerRemoteWakeLockCallback(null);
            mManager = null;
        }

        if (mWakeLock != null) {
            //在断开服务时，如果之前有持远程锁，需要释放掉。
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    @Override
    public void onConnectFailed(ServiceClient serviceClient, ConnectFailedReason reason) {
        //服务连接失败回调方法，请在实际应用中按当时情况处理。
        Toast.makeText(
                this,
                "Connect to service: " + ServiceManagerContext.SERVICE_REMOTE_WAKELOCK + " failed."
                        + "Has you installed iwds service with the last version"
                        + "(WatchManager in Phone or iwds-device in Watch)?", Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onAcquireResult(RemoteWakeLock wakeLock, long timeout, int resultCode) {
        //持远程锁的结果回调方法，通过这个方法可以得到持远程锁是否成功，以及失败时的失败原因。
        if (resultCode == RemoteWakeLockManager.RESULT_OK) {
            Toast.makeText(this, "Acquire wakelock succeeded.Timeout: " + timeout,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Acquire wakelock failed.Result code: " + resultCode,
                    Toast.LENGTH_SHORT).show();
        }
    }
}
