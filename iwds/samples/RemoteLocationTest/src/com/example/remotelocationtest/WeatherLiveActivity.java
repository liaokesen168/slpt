package com.example.remotelocationtest;

import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceClient.ConnectionCallbacks;
import com.ingenic.iwds.common.api.ServiceManagerContext;
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

public class WeatherLiveActivity extends Activity implements
        ConnectionCallbacks {
    private TextView mLocationTextView;// 地点
    private TextView mWeatherTextView;// 天气
    private TextView mWeatherTemperatureTextView;// 气温
    private TextView mWindDirctionTextView;// 风向
    private TextView mWindPowerTextView;// 风力
    private TextView mAirHumidityTextView;// 空气湿度
    private TextView mWeatherPublishTextView;// 发布时间

    private ServiceClient m_client;
    private RemoteLocationServiceManager m_service;

    private void initView() {
        mLocationTextView = (TextView) findViewById(R.id.current_weather_location_text);
        mWeatherTextView = (TextView) findViewById(R.id.current_weather_weather_text);
        mWeatherTemperatureTextView = (TextView) findViewById(R.id.current_weather_temperature_text);
        mWindDirctionTextView = (TextView) findViewById(R.id.current_weather_wind_direction_text);
        mWindPowerTextView = (TextView) findViewById(R.id.current_weather_wind_power_text);
        mAirHumidityTextView = (TextView) findViewById(R.id.current_weather_air_humidity_text);
        mWeatherPublishTextView = (TextView) findViewById(R.id.current_weather_weather_publish_text);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        IwdsLog.d(this, "onCreate()");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_weather_live);

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
                Toast.makeText(WeatherLiveActivity.this, "Remote available",
                        Toast.LENGTH_SHORT).show();

                result = m_service.requestWeatherUpdate(
                        RemoteLocationServiceManager.WEATHER_TYPE_LIVE,
                        m_weatherListener);
                if (!result) {
                    IwdsLog.d(this, "Failed to request weather live");
                    Toast.makeText(WeatherLiveActivity.this,
                            "Failed to request weather live",
                            Toast.LENGTH_SHORT).show();
                }

                result = m_service
                        .registerNetworkStatusListener(m_networkStatusListener);
                if (!result) {
                    IwdsLog.d(this,
                            "Failed to register network status listener");
                    Toast.makeText(WeatherLiveActivity.this,
                            "Failed to register network status listener",
                            Toast.LENGTH_SHORT).show();
                }

            } else {
                IwdsLog.d(this, "Remote unavailable");
                Toast.makeText(WeatherLiveActivity.this, "Remote unavailable",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    private RemoteNetworkStatusListener m_networkStatusListener = new RemoteNetworkStatusListener() {

        @Override
        public void onNetworkStatusChanged(int status) {
            if (status == RemoteLocationServiceManager.NETWORK_STATUS_UNAVAILABLE) {
                Toast.makeText(WeatherLiveActivity.this, "远端设备网络不可用",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(WeatherLiveActivity.this, "远端设备网络可用",
                        Toast.LENGTH_SHORT).show();
            }
        }

    };

    private RemoteWeatherListener m_weatherListener = new RemoteWeatherListener() {

        @Override
        public void onWeatherLiveSearched(RemoteWeatherLive weatherLive) {

            if (weatherLive != null && weatherLive.getErrorCode() == 0) {
                mLocationTextView.setText(weatherLive.getCity());
                mWeatherTextView.setText(weatherLive.getWeather());
                mWeatherTemperatureTextView.setText(weatherLive
                        .getTemperature() + "℃");
                mWindDirctionTextView.setText(weatherLive.getWindDir() + "风");
                mWindPowerTextView.setText(weatherLive.getWindPower() + "级");
                mAirHumidityTextView.setText(weatherLive.getHumidity() + "%");
                mWeatherPublishTextView.setText(weatherLive.getReportTime());
            } else if (weatherLive != null) {
                Toast.makeText(
                        WeatherLiveActivity.this,
                        "获取实时天气失败:"
                                + RemoteLocationErrorCode
                                        .errorCodeToString(weatherLive
                                                .getErrorCode()),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(WeatherLiveActivity.this,
                        "获取实时天气失败: weatherLive is null", Toast.LENGTH_SHORT)
                        .show();
            }
        }

        @Override
        public void onWeatherForecastSearched(
                RemoteWeatherForecast weatherForecast) {
            // do not care

        }

    };

}
