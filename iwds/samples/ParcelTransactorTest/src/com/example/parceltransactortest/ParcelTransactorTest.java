package com.example.parceltransactortest;

import java.io.Serializable;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback;
import com.ingenic.iwds.datatransactor.ParcelTransactor;
import com.ingenic.iwds.datatransactor.elf.WeatherInfoArray;
import com.ingenic.iwds.datatransactor.elf.WeatherInfoArray.WeatherInfo;
import com.ingenic.iwds.utils.IwdsLog;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

public class ParcelTransactorTest extends Activity implements
        DataTransactorCallback {
    private String m_uuid = "d13c19e2-17a4-0797-9352-aca0dd4b886f";
    private ParcelTransactor<WeatherInfoArray> m_transactor;

    WeatherInfoArray m_weather = new WeatherInfoArray(new WeatherInfo[7]);

    private static class Request implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = -3164444259037822736L;

    }

    private static class RequestFailed implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1344233957719877442L;

    }

    public void request() {
        m_transactor.send(new Request());
    }

    public void notifyRequestFailed() {
        m_transactor.send(new RequestFailed());
    }

    public void send() {
        m_transactor.send(m_weather);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        IwdsLog.i(this, "onCreate");

        if (m_transactor == null) {
            m_transactor = new ParcelTransactor<WeatherInfoArray>(this,
                    WeatherInfoArray.CREATOR, this, m_uuid);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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

            /* Channel 有效时传输请求 */
            request();

        } else {
            IwdsLog.i(this, "Data channel is unavaiable.");
        }
    }

    /*
     * 数据接收完成。传输结束后接收端发生
     */
    @Override
    public void onDataArrived(Object object) {

        if (object instanceof Serializable) {
            IwdsLog.d(this, "Serializable object name: "
                    + object.getClass().getSimpleName());

            /* 传输对象 */
            send();

        } else if (object instanceof WeatherInfoArray) {
            IwdsLog.d(this, "Parcelable object name: "
                    + object.getClass().getSimpleName());

            WeatherInfoArray array = (WeatherInfoArray) object;

            int N = array.data.length;
            for (int i = 0; i < N; i++)
                IwdsLog.d(this,
                        "array.data[" + i + "]:" + array.data[i].toString());
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

    @Override
    public void onRecvFileProgress(int progress) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSendFileProgress(int progress) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSendFileInterrupted(int index) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRecvFileInterrupted(int index) {
        // TODO Auto-generated method stub

    }
}
