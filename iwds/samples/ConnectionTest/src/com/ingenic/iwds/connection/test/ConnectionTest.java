package com.ingenic.iwds.connection.test;


import android.os.Bundle;
import android.app.Activity;
import android.content.Context;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.app.ConnectionHelper;
import com.ingenic.iwds.uniconnect.ConnectionServiceManager;
import com.ingenic.iwds.utils.IwdsLog;

/**
 * 演示用 ConnectionHelper 监控设备间的连接状态。
 * 1. 先用 start 启动 ConnectionHelper，使之连接 connection 服务。
 * 2. 用 onServiceConnected onServiceDisconnected 监控此 client 与 connection
 *    服务的连接状态。
 * 3. 用 onConnectedDevice onDisconnectedDevice 监控设备间的连接状态。
 * 4. 使用结束后用 stop 关闭 ConnectionHelper。
 */
public class ConnectionTest extends Activity {

    private class ConnectionTestHelper extends ConnectionHelper {

        public ConnectionTestHelper(Context context) {
            super(context);
        }

        @Override
        public void onConnectedDevice(DeviceDescriptor deviceDescriptor) {
            IwdsLog.i(this, "Device connected: " + deviceDescriptor.toString());
        }

        @Override
        public void onDisconnectedDevice(DeviceDescriptor deviceDescriptor) {
            IwdsLog.i(this,
                    "Device diconnected: " + deviceDescriptor.toString());
        }

        @Override
        public void onServiceConnected(
                ConnectionServiceManager connectionServiceManager) {
            IwdsLog.i(this, "ConnectionService connected");
        }

        @Override
        public void onServiceDisconnected(boolean unexpected) {
            IwdsLog.i(this, "ConnectionService diconnected: "
                    + (unexpected ? "Unexpected" : "Expected"));
        }
    }

    private ConnectionTestHelper m_testHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_test);

        m_testHelper = new ConnectionTestHelper(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /* 在 onResume 时启动并连接到服务器 */
        m_testHelper.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        /* 在 onPause 时关闭并断开服务器连接 */
        m_testHelper.stop();
    }
}
