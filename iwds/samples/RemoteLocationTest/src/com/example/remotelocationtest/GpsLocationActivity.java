package com.example.remotelocationtest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceClient.ConnectionCallbacks;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.smartlocation.RemoteGpsStatus;
import com.ingenic.iwds.smartlocation.RemoteGpsStatus.RemoteGpsStatusListener;
import com.ingenic.iwds.smartlocation.RemoteLocation;
import com.ingenic.iwds.smartlocation.RemoteLocationErrorCode;
import com.ingenic.iwds.smartlocation.RemoteLocationListener;
import com.ingenic.iwds.smartlocation.RemoteLocationServiceManager;
import com.ingenic.iwds.smartlocation.RemoteProviderListener;
import com.ingenic.iwds.smartlocation.RemoteStatusListener;
import com.ingenic.iwds.utils.IwdsLog;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class GpsLocationActivity extends Activity implements
        ConnectionCallbacks {
    private ServiceClient m_client;
    private RemoteLocationServiceManager m_service;

    private TextView mLocationLatlngTextView;// 定位经纬度信息
    private TextView mLocationAccurancyTextView;// 定位精度信息
    private TextView mLocationMethodTextView;// 定位方式信息
    private TextView mLocationTimeTextView;// 定位时间信息
    private TextView mLocationAltitudeTextView;// 定位海拔信息
    private TextView mLocationBearingTextView;// 定位方位信息
    private TextView mLocationSpeedTextView;// 定位速度信息

    private void initView() {
        mLocationLatlngTextView = (TextView) findViewById(R.id.gps_location_latlng_text);
        mLocationAccurancyTextView = (TextView) findViewById(R.id.gps_location_accurancy_text);
        mLocationAltitudeTextView = (TextView) findViewById(R.id.gps_location_altitude_text);
        mLocationBearingTextView = (TextView) findViewById(R.id.gps_location_bearing_text);
        mLocationSpeedTextView = (TextView) findViewById(R.id.gps_location_speed_text);
        mLocationMethodTextView = (TextView) findViewById(R.id.gps_location_method_text);
        mLocationTimeTextView = (TextView) findViewById(R.id.gps_location_time_text);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        IwdsLog.d(this, "onCreate()");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_gps_location);

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

        m_service.unregisterLocationListener(m_locationListener,
                RemoteLocationServiceManager.GPS_PROVIDER);
        m_service.unregisterGpsStatusListener(m_gpsStatusListener);
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
                Toast.makeText(GpsLocationActivity.this, "Remote available",
                        Toast.LENGTH_SHORT).show();

                result = m_service.registerLocationListener(
                        RemoteLocationServiceManager.GPS_PROVIDER,
                        m_locationListener);
                if (!result) {
                    IwdsLog.d(this, "Failed to register GPS location listener");
                    Toast.makeText(GpsLocationActivity.this,
                            "Failed to register GPS location listener",
                            Toast.LENGTH_SHORT).show();
                }

                result = m_service
                        .registerGpsStatusListener(m_gpsStatusListener);
                if (!result) {
                    IwdsLog.d(this, "Failed to register GPS status listener");
                    Toast.makeText(GpsLocationActivity.this,
                            "Failed to register GPS status listener",
                            Toast.LENGTH_SHORT).show();
                }

                result = m_service.requestLastKnownLocation(
                        RemoteLocationServiceManager.GPS_PROVIDER,
                        m_locationListener);
                if (!result) {
                    IwdsLog.d(this,
                            "Failed to register last known location listener");
                    Toast.makeText(GpsLocationActivity.this,
                            "Failed to register last known location listener",
                            Toast.LENGTH_SHORT).show();
                }

                result = m_service.requestProviderList(false, m_providerListener);
                if (!result) {
                    IwdsLog.d(this, "Failed to get all providers");
                    Toast.makeText(GpsLocationActivity.this,
                            "Failed to get all providers", Toast.LENGTH_SHORT)
                            .show();
                }

                result = m_service.requestProviderStatus(
                        RemoteLocationServiceManager.GPS_PROVIDER,
                        m_providerListener);
                if (!result) {
                    IwdsLog.d(this, "Failed to get gps provider status");
                    Toast.makeText(GpsLocationActivity.this,
                            "Failed to get GPS provider status",
                            Toast.LENGTH_SHORT).show();
                }

                result = m_service.requestGpsStatus(m_gpsStatusListener);
                if (!result) {
                    IwdsLog.d(this, "Failed to get GPS current status");
                    Toast.makeText(GpsLocationActivity.this,
                            "Failed to get GPS current status",
                            Toast.LENGTH_SHORT).show();
                }

            } else {
                IwdsLog.d(this, "Remote unavailable");
                Toast.makeText(GpsLocationActivity.this, "Remote unavailable",
                        Toast.LENGTH_SHORT).show();
            }
        }

    };

    private RemoteProviderListener m_providerListener = new RemoteProviderListener() {

        @Override
        public void onProviderStatus(boolean enabled, String provider) {
            if (enabled) {
                Toast.makeText(GpsLocationActivity.this, "远端设备 GPS 可用",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(GpsLocationActivity.this, "远端设备 GPS 不可用",
                        Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onProviderList(boolean enabledOnly,
                ArrayList<String> providerList) {
            IwdsLog.d(this, "enabledOnly=" + enabledOnly + ", provider list="
                    + providerList);
        }
    };

    private RemoteLocationListener m_locationListener = new RemoteLocationListener() {
        @Override
        public void onLocationChanged(RemoteLocation location) {
            updateLocation(location);
        }
    };

    private RemoteGpsStatusListener m_gpsStatusListener = new RemoteGpsStatusListener() {
        @Override
        public void onGpsStatusChanged(int event) {

            switch (event) {
            case RemoteGpsStatus.GPS_EVENT_FIRST_FIX:
                break;
            case RemoteGpsStatus.GPS_EVENT_SATELLITE_STATUS:
                break;
            case RemoteGpsStatus.GPS_EVENT_STARTED:
                Toast.makeText(GpsLocationActivity.this, "远端设备 GPS 可用",
                        Toast.LENGTH_SHORT).show();
                break;
            case RemoteGpsStatus.GPS_EVENT_STOPPED:
                Toast.makeText(GpsLocationActivity.this, "远端设备 GPS 不可用",
                        Toast.LENGTH_SHORT).show();
                break;
            }
        }

        @Override
        public void onGpsStatus(RemoteGpsStatus status) {
            Toast.makeText(GpsLocationActivity.this,
                    "搜索到" + +status.getSatellites().size() + "颗卫星",
                    Toast.LENGTH_SHORT).show();
            IwdsLog.d(this, "Gps status: " + status);
        }
    };

    private void updateLocation(RemoteLocation location) {
        if (location.getErrorCode() == 0) {
            mLocationLatlngTextView.setText(location.getLatitude() + "  "
                    + location.getLongitude());
            mLocationAccurancyTextView.setText(String.valueOf(location
                    .getAccuracy()));
            mLocationMethodTextView.setText(location.getProvider());

            if (location.hasAltitude())
                mLocationAltitudeTextView.setText(String.valueOf(location
                        .getAltitude()));
            else
                mLocationAltitudeTextView.setText("?");

            if (location.hasBearing())
                mLocationBearingTextView.setText(String.valueOf(location
                        .getBearing()));
            else
                mLocationBearingTextView.setText("?");

            if (location.hasSpeed())
                mLocationSpeedTextView.setText(String.valueOf(location
                        .getSpeed()));
            else
                mLocationSpeedTextView.setText("?");

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(location.getTime());

            mLocationTimeTextView.setText(df.format(date));

        } else {
            Toast.makeText(
                    GpsLocationActivity.this,
                    "GPS 定位失败: "
                            + RemoteLocationErrorCode
                                    .errorCodeToString(location.getErrorCode()),
                    Toast.LENGTH_SHORT).show();

        }
    }

}
