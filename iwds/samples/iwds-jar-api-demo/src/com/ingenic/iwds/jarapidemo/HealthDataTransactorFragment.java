package com.ingenic.iwds.jarapidemo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Context;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.datatransactor.elf.HealthInfo;
import com.ingenic.iwds.datatransactor.elf.HealthTransactionModel;
import com.ingenic.iwds.datatransactor.elf.HealthTransactionModel.HealthTransactionModelCallback;
import com.ingenic.iwds.utils.IwdsLog;

/**
 * 演示用 HealthTransactionModel 在两个设备之间（手机和手表之间）传输健康信息 HealthInfo 对象。
 *
 * 数据传输方式由两种，如下：
 * 1. 推数据 - 设备a调用 send() 发送 HealthInfo 对象，设备b在 onObjectArrived() 收到该对象。
 * 2. 拉数据 - 设备b调用 request() 向设备a请求数据，设备a 在 onRequest() 收到请求并用 send()
 *    发送HealthInfo 对象，设备b在 onObjectArrived() 收到该对象。
 *    如果设备a onRequest() 收到请求后不想发送数据，要调用 notifyRequestFailed 通知设备b请求失败。
 *
 * 注意：需要在两个设备上同时安装这个应用
 */
public class HealthDataTransactorFragment extends DemoFragment {

    private View mContentView;
    private Context mContext;

    private HealthTransactionModel mHealthTransactor;
    private Button mRequestButton;
    private Button mSendButton;

    private OnClickListener mListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.request_button: {
                /* 按 request 按钮，请求对方设备发送 HealthInfo 对象 */
                mHealthTransactor.request();
                break;
            }

            case R.id.send_button: {
                /* 按 send 按钮发送 HealthInfo 对象 */
                // 用‘A’隔开内容包含 [心率值,日期,数据库索引]
                String rates ="A82,1436405409067,8A";
                HealthInfo data = new HealthInfo();
                data.rates = rates;
                data.humidity = 60;
                data.temp = 25;
                data.pressure = 100;
                mHealthTransactor.send(data);
                break;
            }

            default:
                break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mContext = getActivity();
        mContentView = inflater.inflate(R.layout.health_data_transactor, container, false);

        mRequestButton = (Button) mContentView.findViewById(R.id.request_button);
        mRequestButton.setOnClickListener(mListener);

        mSendButton = (Button) mContentView.findViewById(R.id.send_button);
        mSendButton.setOnClickListener(mListener);

        mHealthTransactor = new HealthTransactionModel(mContext, mCallback,
                "2695cd72-a901-11e4-a332-5404a6abe086");

        return mContentView;
    }

    @Override
    public void onPause() {
        mHealthTransactor.stop();
        super.onPause();
    }

    @Override
    public void onResume() {
        mHealthTransactor.start();
        super.onResume();
    }

    private HealthTransactionModelCallback mCallback = new HealthTransactionModelCallback() {

        /**
         * 收到对方的数据请求
         */
        @Override
        public void onRequest() {
            /* 应对方请求发送 HealthInfo 对象。如果没有可发送的数据，要调用 notifyRequestFailed
             * 通知对方请求失败。
             */
            String rates = "A82,1436405409067,8A";
            HealthInfo data = new HealthInfo();
            data.rates = rates;
            data.humidity = 60;
            data.temp = 26;
            data.pressure = 2000;
            mHealthTransactor.send(data);
        }

        /*
         * 数据请求失败。数据请求端发生。
         */
        @Override
        public void onRequestFailed() {
            IwdsLog.i(this, "request failed");
        }

        /*
         * 数据接收完成。传输结束后接收端发生。
         */
        @Override
        public void onObjectArrived(HealthInfo object) {
            IwdsLog.i(this, "Data arrived: " + object.toString());

            if (object instanceof HealthInfo) {
                HealthInfo data = (HealthInfo) object;
                IwdsLog.i(this, "Data arrived: " + data.toString());
            }
        }

        /*
         * 监控设备间的连接状态变化。isConnected 为 true 表示两个设备连接成功。
         */
        @Override
        public void onLinkConnected(DeviceDescriptor descriptor, boolean isConnected) {
            if (isConnected) {
                IwdsLog.i(this, "Link connected: " + descriptor.toString());
            } else {
                IwdsLog.i(this, "Link disconnected: " + descriptor.toString());
            }
        }

        /*
         * 监控蓝牙传输通道状态，isAvailable 为 true 表示传输通道可用，这时才能传输数据。
         */
        @Override
        public void onChannelAvailable(boolean isAvailable) {
            if (isAvailable) {
                IwdsLog.i(this, "Data channel is available.");
            } else {
                IwdsLog.i(this, "Data channel is unavailable.");
            }
        }

        /*
         * 传输是否成功。传输结束后发送端和接收端都会发生。
         */
        @Override
        public void onSendResult(DataTransactResult result) {
            if (result.getResultCode() == DataTransactResult.RESULT_OK) {
                IwdsLog.i(this, "Send success");
            } else {
                IwdsLog.i(this, "Send failed by error code: " + result.getResultCode());
            }
        }
    };

}
