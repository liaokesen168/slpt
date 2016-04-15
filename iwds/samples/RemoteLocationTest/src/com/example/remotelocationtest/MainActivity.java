package com.example.remotelocationtest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView mCurrentWeatherReportTextView;// 实时天气预报
    private TextView mFutureWeatherReportTextView;// 未来三天天气预报
    private TextView mNetLocationTextView;// 网络定位
    private TextView mMultyLocationTextView;// 混合定位
    private TextView mGpsLocationTextView;// GPS定位
    private TextView mGeoFenceTextView;// 地理围栏

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mCurrentWeatherReportTextView = (TextView) findViewById(R.id.current_weather_report_text);
        mFutureWeatherReportTextView = (TextView) findViewById(R.id.future_weather_report_text);
        mNetLocationTextView = (TextView) findViewById(R.id.location_net_method_text);
        mMultyLocationTextView = (TextView) findViewById(R.id.location_multy_method_text);
        mGpsLocationTextView = (TextView) findViewById(R.id.location_gps_method_text);

        mGeoFenceTextView = (TextView) findViewById(R.id.location_geofence_method_text);

        mCurrentWeatherReportTextView.setOnClickListener(m_listener);
        mFutureWeatherReportTextView.setOnClickListener(m_listener);
        mNetLocationTextView.setOnClickListener(m_listener);
        mMultyLocationTextView.setOnClickListener(m_listener);
        mGpsLocationTextView.setOnClickListener(m_listener);

        mGeoFenceTextView.setOnClickListener(m_listener);
    }

    private OnClickListener m_listener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
            case R.id.current_weather_report_text:
                // 实时天气预报
                Intent intent = new Intent(MainActivity.this,
                        WeatherLiveActivity.class);
                startActivity(intent);
                break;
            case R.id.future_weather_report_text:
                // 未来三天天气预报
                Intent forcastIntent = new Intent(MainActivity.this,
                        WeatherForecastActivity.class);
                startActivity(forcastIntent);
                break;
            case R.id.location_net_method_text:
                // 网络定位（Wifi+基站）
                Intent netIntent = new Intent(MainActivity.this,
                        NetworkLocationActivity.class);
                startActivity(netIntent);
                break;
            case R.id.location_multy_method_text:
                // 混合定位
                Intent multyIntent = new Intent(MainActivity.this,
                        IwdsLocationActivity.class);
                startActivity(multyIntent);
                break;
            case R.id.location_gps_method_text:
                // GPS 定位
                Intent gpsIntent = new Intent(MainActivity.this,
                        GpsLocationActivity.class);
                startActivity(gpsIntent);
                break;

            case R.id.location_geofence_method_text: // 地理围栏 Intent
                Intent geoFenceIntent = new Intent(MainActivity.this,
                        GeoFenceActivity.class);
                startActivity(geoFenceIntent);
                break;

            }
        }

    };
}
