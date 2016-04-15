package com.example.remotelocationtest;

import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceClient.ConnectionCallbacks;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.smartlocation.RemoteGeoFenceListener;
import com.ingenic.iwds.smartlocation.RemoteGpsStatus;
import com.ingenic.iwds.smartlocation.RemoteLocation;
import com.ingenic.iwds.smartlocation.RemoteLocationListener;
import com.ingenic.iwds.smartlocation.RemoteLocationServiceManager;
import com.ingenic.iwds.smartlocation.RemoteNetworkStatusListener;
import com.ingenic.iwds.smartlocation.RemoteStatusListener;
import com.ingenic.iwds.smartlocation.RemoteGpsStatus.RemoteGpsStatusListener;
import com.ingenic.iwds.utils.IwdsLog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class GeoFenceActivity extends Activity implements ConnectionCallbacks {
    private ServiceClient m_client;
    private RemoteLocationServiceManager m_service;

    private double m_latitude;
    private double m_longtitude;

    private final static double DEFAULT_LATITUDE = 22.536291;
    private final static double DEFAULT_LONETITUDE = 113.952798;
    private final float m_radius = 500;
    private final long m_expiration = 1000 * 60 * 30;

    private EditText m_latitudeEdit;
    private EditText m_longtitudeEdit;
    private Button m_confirmButton;

    private void initView() {
        m_latitudeEdit = (EditText) findViewById(R.id.editText1);
        m_longtitudeEdit = (EditText) findViewById(R.id.editText2);
        m_confirmButton = (Button) findViewById(R.id.button1);

        m_confirmButton.setOnClickListener(m_buttonListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        IwdsLog.d(this, "onCreate()");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_geofence);

        initView();
        if (m_client == null)
            m_client = new ServiceClient(this,
                    ServiceManagerContext.SERVICE_REMOTE_LOCATION, this);
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

        m_service.unregisterLocationListener(m_locationListenr,
                RemoteLocationServiceManager.IWDS_NETWORK_PROVIDER);
        m_service.unregisterGpsStatusListener(m_gpsStatusListener);
        m_service.unregisterGeoFenceListener(m_geofenceListener);
    }

    @Override
    public void onConnectFailed(ServiceClient serviceClient,
            ConnectFailedReason reason) {
        IwdsLog.d(this, "Remote location service connect failed");
    }

    private double getNumber(EditText editText, double defaultLatitude) {
        double value;

        try {
            value = Integer.parseInt(editText.getText().toString());
        } catch (NumberFormatException e) {
            value = defaultLatitude;
            e.printStackTrace();
        }

        return value;
    }

    private OnClickListener m_buttonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean result = false;

            switch (v.getId()) {
            case R.id.button1:
                m_latitude = getNumber(m_latitudeEdit, DEFAULT_LATITUDE);
                m_longtitude = getNumber(m_longtitudeEdit, DEFAULT_LONETITUDE);

                IwdsLog.i(this, "set latitude: " + m_latitude);
                IwdsLog.i(this, "set longtitude: " + m_longtitude);

                m_service.unregisterGeoFenceListener(m_geofenceListener);

                result = m_service.registerGeoFenceListener(m_latitude,
                        m_longtitude, m_radius, m_expiration,
                        m_geofenceListener);
                if (!result) {
                    IwdsLog.d(this, "Failed to register GeoFence listener");
                    Toast.makeText(GeoFenceActivity.this,
                            "Failed to register GeoFence listener",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    };

    private RemoteStatusListener m_remoteListener = new RemoteStatusListener() {
        @Override
        public void onAvailable(boolean available) {
            if (available) {
                IwdsLog.d(this, "Remote available");
                boolean result = false;

                Toast.makeText(GeoFenceActivity.this, "Remote available",
                        Toast.LENGTH_SHORT).show();

                result = m_service
                        .requestNetworkStatus(m_networkStatusListener);
                if (!result) {
                    IwdsLog.d(this, "Failed to get network current status");
                    Toast.makeText(GeoFenceActivity.this,
                            "Failed to get network current status",
                            Toast.LENGTH_SHORT).show();
                }

                result = m_service.registerLocationListener(
                        RemoteLocationServiceManager.IWDS_NETWORK_PROVIDER,
                        m_locationListenr);
                if (!result) {
                    IwdsLog.d(this, "Failed to register location listener");
                    Toast.makeText(GeoFenceActivity.this,
                            "Failed to register location listener",
                            Toast.LENGTH_SHORT).show();
                }

                result = m_service
                        .registerGpsStatusListener(m_gpsStatusListener);
                if (!result) {
                    IwdsLog.d(this, "Failed to register GPS status listener");
                    Toast.makeText(GeoFenceActivity.this,
                            "Failed to register GPS status listener",
                            Toast.LENGTH_SHORT).show();
                }

                result = m_service.registerGeoFenceListener(DEFAULT_LATITUDE,
                        DEFAULT_LONETITUDE, m_radius, m_expiration,
                        m_geofenceListener);
                if (!result) {
                    IwdsLog.d(this, "Failed to register GeoFence listener");
                    if (!result) {
                        IwdsLog.d(this, "Failed to register GeoFence listener");
                        Toast.makeText(GeoFenceActivity.this,
                                "Failed to register GPS status listener",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                result = m_service
                        .registerNetworkStatusListener(m_networkStatusListener);
                if (!result) {
                    IwdsLog.d(this,
                            "Failed to register network status listener");
                    Toast.makeText(GeoFenceActivity.this,
                            "Failed to register network status listener",
                            Toast.LENGTH_SHORT).show();
                }

                result = m_service.requestLastKnownLocation(
                        RemoteLocationServiceManager.IWDS_NETWORK_PROVIDER,
                        m_locationListenr);
                if (!result) {
                    IwdsLog.d(this,
                            "Failed to register last known location listener");
                    Toast.makeText(GeoFenceActivity.this,
                            "Failed to register last known location listener",
                            Toast.LENGTH_SHORT).show();
                }

                result = m_service.requestGpsStatus(m_gpsStatusListener);
                if (!result) {
                    IwdsLog.d(this, "Failed to get GPS current status");
                    Toast.makeText(GeoFenceActivity.this,
                            "Failed to get GPS current status",
                            Toast.LENGTH_SHORT).show();
                }

                m_confirmButton.setEnabled(true);

            } else {
                IwdsLog.d(this, "Remote unavailable");
                Toast.makeText(GeoFenceActivity.this, "Remote unavailable",
                        Toast.LENGTH_SHORT).show();

                m_confirmButton.setEnabled(false);
            }
        }
    };

    private RemoteNetworkStatusListener m_networkStatusListener = new RemoteNetworkStatusListener() {

        @Override
        public void onNetworkStatusChanged(int status) {
            if (status == RemoteLocationServiceManager.NETWORK_STATUS_UNAVAILABLE) {
                Toast.makeText(GeoFenceActivity.this, "远端设备网络不可用",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(GeoFenceActivity.this, "远端设备网络可用",
                        Toast.LENGTH_SHORT).show();
            }
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
                Toast.makeText(GeoFenceActivity.this, "远端设备 GPS 可用",
                        Toast.LENGTH_SHORT).show();
                break;
            case RemoteGpsStatus.GPS_EVENT_STOPPED:
                Toast.makeText(GeoFenceActivity.this, "远端设备 GPS 不可用",
                        Toast.LENGTH_SHORT).show();
                break;
            }
        }

        @Override
        public void onGpsStatus(RemoteGpsStatus status) {
            IwdsLog.d(this, "Gps status: " + status);
        }
    };

    private RemoteLocationListener m_locationListenr = new RemoteLocationListener() {

        @Override
        public void onLocationChanged(RemoteLocation location) {

        }
    };

    private RemoteGeoFenceListener m_geofenceListener = new RemoteGeoFenceListener() {
        @Override
        public void onGeoFenceAlert(int status) {
            if (status == RemoteLocationServiceManager.GEOFENCE_STATUS_NON_ALERT) {
                IwdsLog.d(this, "Out of GeoFence alert area");
                Toast.makeText(GeoFenceActivity.this, "不在区域",
                        Toast.LENGTH_SHORT).show();
            } else {
                IwdsLog.d(this, "In GeoFence alert area");
                Toast.makeText(GeoFenceActivity.this, "在区域内",
                        Toast.LENGTH_SHORT).show();
            }
        }

    };

}
