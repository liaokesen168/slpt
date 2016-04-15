package com.ingenic.iwds.test.datatransactortest;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.datatransactor.DataTransactor;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback;
import com.ingenic.iwds.datatransactor.elf.WeatherInfoArray;
import com.ingenic.iwds.datatransactor.elf.WeatherInfoArray.WeatherInfo;
import com.ingenic.iwds.utils.IwdsLog;

/**
 * 演示用 DataTransactor 在两个设备之间（手机和手表之间）传输数据
 * 
 * 1. 两个设备先用 m_transactor.start 启动 DataTransactor。
 * 2. 通过 onLinkConnected 监控设备连接状态，通过 onChannelAvailable 监控蓝牙数据传输通道。
 *    如果设备已经连接，数据传输通道有效，就可以传输数据了。
 * 2. 一个设备中用 m_transactor.send(m_weather) 发送数据 m_weather 对象。
 * 3. 另一个设备 onDataArrived(Object object) 收到发送的对象。
 * 4. 结束之后用 m_transactor.stop 关闭 DataTransactor。
 * 
 * 注意：需要在两个设备上同时安装这个应用
 * 
 */
public class DataTransactorTest extends Activity implements
        DataTransactorCallback {
    private String m_uuid = "a1dc19e2-17a4-0797-9362-68a0dd4bfb6f";
    private DataTransactor m_transactor;

    WeatherInfoArray m_weather = new WeatherInfoArray(new WeatherInfo[7]);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_transactor_test);

        IwdsLog.i(this, "onCreate");

        if (m_transactor == null) {
            m_transactor = new DataTransactor(this, this, m_uuid);

            int N = m_weather.data.length;
            for (int i = 0; i < N; i++)
                m_weather.data[i] = new WeatherInfo();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        IwdsLog.i(this, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();

        IwdsLog.i(this, "onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();

        m_transactor.stop();

        IwdsLog.i(this, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();

        m_transactor.start();

        IwdsLog.i(this, "onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        IwdsLog.i(this, "onDestroy");
    }

    /*
     * 监控蓝牙传输通道状态，isAvailable 为 true 表示传输通道可用，这时才能传输数据。
     */
    @Override
    public void onChannelAvailable(boolean isAvailable) {
        if (isAvailable) {
            IwdsLog.i(this, "Data channel is available.");

                /* Channel 有效时传输 m_weather 对象 */
            m_transactor.send(m_weather);

        } else {
            IwdsLog.i(this, "Data channel is unavaiable.");
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
            IwdsLog.i(this,
                    "Send failed by error code: " + result.getResultCode());
        }
    }

    /*
     * 数据接收完成。传输结束后接收端发生
     */
    @Override
    public void onDataArrived(Object object) {
        WeatherInfoArray array = (WeatherInfoArray) object;

        int N = array.data.length;
        for (int i = 0; i < N; i++)
            IwdsLog.d(this, "array.data[" + i + "]:" + array.data[i].toString());
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

    @Override
    public void onRecvFileProgress(int progress) {

    }

    @Override
    public void onSendFileProgress(int progress) {

    }

    @Override
    public void onSendFileInterrupted(int index) {

    }

    @Override
    public void onRecvFileInterrupted(int index) {

    }

}
