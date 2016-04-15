package com.ingenic.iwds.jarapidemo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class ConnectionFragment extends DemoFragment {

    private View mContentView;
    private Context mContext;

    private ConnectionHelper mConnectionHelper;

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
            IwdsLog.i(this, "Device diconnected: " + deviceDescriptor.toString());
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
    };
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mContext = getActivity();
        mContentView = inflater.inflate(R.layout.connection, container, false);

        IwdsLog.i(this, "onCreate");
        mConnectionHelper = new ConnectionTestHelper(mContext);

        return mContentView;
    }

    @Override
    public void onResume() {
        super.onResume();

        /* 在 onResume 时启动并连接到服务器 */
        mConnectionHelper.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        /* 在 onPause 时关闭并断开服务器连接 */
        mConnectionHelper.stop();
    }
}
