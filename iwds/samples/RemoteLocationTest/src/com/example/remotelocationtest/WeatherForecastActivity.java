package com.example.remotelocationtest;

import java.util.List;

import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceClient.ConnectionCallbacks;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.smartlocation.RemoteDayWeatherForecast;
import com.ingenic.iwds.smartlocation.RemoteLocationErrorCode;
import com.ingenic.iwds.smartlocation.RemoteLocationServiceManager;
import com.ingenic.iwds.smartlocation.RemoteNetworkStatusListener;
import com.ingenic.iwds.smartlocation.RemoteStatusListener;
import com.ingenic.iwds.smartlocation.RemoteWeatherForecast;
import com.ingenic.iwds.smartlocation.RemoteWeatherListener;
import com.ingenic.iwds.smartlocation.RemoteWeatherLive;
import com.ingenic.iwds.utils.IwdsLog;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class WeatherForecastActivity extends Activity implements
        ConnectionCallbacks {
    private ServiceClient m_client;
    private RemoteLocationServiceManager m_service;

    private TextView mWeatherLocationTextView;// 天气预报地点
    private TextView mTodayTimeTextView;//
    private TextView mTomorrowTimeTextView;//
    private TextView mNextDayTimeTextView;//

    private TextView mTodayWeatherTextView;// 今天天气状况
    private TextView mTomorrowWeatherTextView;// 明天天气状况
    private TextView mNextDayWeatherTextView;// 后天天气状况

    private void initView() {

        mWeatherLocationTextView = (TextView) findViewById(R.id.future_weather_location_text);

        mTodayTimeTextView = (TextView) findViewById(R.id.today_time_text);
        mTodayWeatherTextView = (TextView) findViewById(R.id.today_weather_des_text);
        mTomorrowTimeTextView = (TextView) findViewById(R.id.tomorrow_time_text);
        mTomorrowWeatherTextView = (TextView) findViewById(R.id.tomorrow_weather_des_text);
        mNextDayTimeTextView = (TextView) findViewById(R.id.netx_day_time_text);
        mNextDayWeatherTextView = (TextView) findViewById(R.id.netx_day_des_text);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        IwdsLog.d(this, "onCreate()");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_weather_forecast);

        if (m_client == null)
            m_client = new ServiceClient(this,
                    ServiceManagerContext.SERVICE_REMOTE_LOCATION, this);

        initView();
    }

    @Override
    protected void onResume() {
        IwdsLog.d(this, "onResume()");
        super.onResume();
        m_client.connect();
    }

    @Override
    protected void onPause() {
        IwdsLog.d(this, "onPause()");
        super.onPause();
        m_client.disconnect();
    }

    @Override
    protected void onDestroy() {
        IwdsLog.d(this, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onConnected(ServiceClient serviceClient) {
        IwdsLog.d(this, "Remote location service connected");
        m_service = (RemoteLocationServiceManager) serviceClient
                .getServiceManagerContext();

        m_service.registerRemoteStatusListener(m_remoteListener);
    }

    @Override
    public void onDisconnected(ServiceClient serviceClient, boolean unexpected) {
        IwdsLog.d(this, "Remote location service disconnected");
        m_service.unregisterRemoteStatusListener(m_remoteListener);
    }

    @Override
    public void onConnectFailed(ServiceClient serviceClient,
            ConnectFailedReason reason) {
        IwdsLog.d(this, "Remote location service connect failed");
    }

    private RemoteStatusListener m_remoteListener = new RemoteStatusListener() {

        @Override
        public void onAvailable(boolean available) {
            if (available) {
                boolean result = false;

                IwdsLog.d(this, "Remote available");
                Toast.makeText(WeatherForecastActivity.this,
                        "Remote available", Toast.LENGTH_SHORT).show();

                result = m_service.requestWeatherUpdate(
                        RemoteLocationServiceManager.WEATHER_TYPE_FORECAST,
                        m_weatherListener);
                if (!result) {
                    IwdsLog.d(this, "Failed to request weather forecast");
                    Toast.makeText(WeatherForecastActivity.this,
                            "Failed to request weather forecast",
                            Toast.LENGTH_SHORT).show();
                }

                result = m_service
                        .registerNetworkStatusListener(m_networkStatusListener);
                if (!result) {
                    IwdsLog.d(this,
                            "Failed to register network status listener");
                    Toast.makeText(WeatherForecastActivity.this,
                            "Failed to register network status listener",
                            Toast.LENGTH_SHORT).show();
                }

            } else {
                IwdsLog.d(this, "Remote unavailable");
                Toast.makeText(WeatherForecastActivity.this,
                        "Remote unavailable", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private RemoteNetworkStatusListener m_networkStatusListener = new RemoteNetworkStatusListener() {

        @Override
        public void onNetworkStatusChanged(int status) {
            if (status == RemoteLocationServiceManager.NETWORK_STATUS_UNAVAILABLE) {
                Toast.makeText(WeatherForecastActivity.this, "远端设备网络不可用",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(WeatherForecastActivity.this, "远端设备网络可用",
                        Toast.LENGTH_SHORT).show();
            }
        }

    };

    private RemoteWeatherListener m_weatherListener = new RemoteWeatherListener() {

        @Override
        public void onWeatherLiveSearched(RemoteWeatherLive weatherLive) {
            // do not care

        }

        @Override
        public void onWeatherForecastSearched(
                RemoteWeatherForecast weatherForecast) {

            if (weatherForecast != null && weatherForecast.getErrorCode() == 0) {
                List<RemoteDayWeatherForecast> forcasts = weatherForecast
                        .getWeatherForecast();

                for (int i = 0; i < forcasts.size(); i++) {

                    RemoteDayWeatherForecast forcast = forcasts.get(i);
                    switch (i) {
                    case 0:
                        mWeatherLocationTextView.setText(forcast.getCity());
                        mTodayTimeTextView.setText("今天 ( " + forcast.getDate()
                                + " )");
                        mTodayWeatherTextView.setText(forcast.getDayWeather()
                                + "    " + forcast.getDayTemp() + "℃/"
                                + forcast.getNightTemp() + "℃    "
                                + forcast.getDayWindPower() + "级");

                        break;
                    case 1:
                        mTomorrowTimeTextView.setText("明天 ( "
                                + forcast.getDate() + " )");
                        mTomorrowWeatherTextView.setText(forcast
                                .getDayWeather()
                                + "    "
                                + forcast.getDayTemp()
                                + "℃/"
                                + forcast.getNightTemp()
                                + "℃    "
                                + forcast.getDayWindPower() + "级");
                        break;
                    case 2:
                        mNextDayTimeTextView.setText("后天 ( "
                                + forcast.getDate() + " )");
                        mNextDayWeatherTextView.setText(forcast.getDayWeather()
                                + "    " + forcast.getDayTemp() + "℃/"
                                + forcast.getNightTemp() + "℃    "
                                + forcast.getDayWindPower() + "级");
                        break;
                    }
                }
            } else if (weatherForecast != null) {
                Toast.makeText(
                        WeatherForecastActivity.this,
                        "获取天气预报失败:"
                                + RemoteLocationErrorCode
                                        .errorCodeToString(weatherForecast
                                                .getErrorCode()),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(WeatherForecastActivity.this,
                        "获取天气预报失败: weatherForecast is null", Toast.LENGTH_SHORT)
                        .show();
            }
        }

    };
}
