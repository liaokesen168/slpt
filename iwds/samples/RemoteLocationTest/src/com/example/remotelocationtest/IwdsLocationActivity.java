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
import com.ingenic.iwds.smartlocation.RemoteNetworkStatusListener;
import com.ingenic.iwds.smartlocation.RemoteProviderListener;
import com.ingenic.iwds.smartlocation.RemoteStatusListener;
import com.ingenic.iwds.utils.IwdsLog;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class IwdsLocationActivity extends Activity implements
        ConnectionCallbacks {
    private ServiceClient m_client;
    private RemoteLocationServiceManager m_service;

    private TextView mLocationLatlngTextView;// 坐标信息
    private TextView mLocationAccurancyTextView;// 定位精确信息
    private TextView mLocationMethodTextView;// 定位方法信息
    private TextView mLocationTimeTextView;// 定位时间信息
    private TextView mLocationDesTextView;// 定位描述信息

    private TextView mLocationCountryTextView;// 所在国家
    private TextView mLocationProvinceTextView;// 所在省
    private TextView mLocationCityTextView;// 所在市
    private TextView mLocationCountyTextView;// 所在区县
    private TextView mLocationRoadTextView;// 所在街道
    private TextView mLocationPOITextView;// POI名称
    private TextView mLocationCityCodeTextView;// 城市编码
    private TextView mLocationAreaCodeTextView;// 区域编码

    private void initView() {
        mLocationLatlngTextView = (TextView) findViewById(R.id.location_latlng_text);
        mLocationAccurancyTextView = (TextView) findViewById(R.id.location_accurancy_text);
        mLocationMethodTextView = (TextView) findViewById(R.id.location_method_text);
        mLocationTimeTextView = (TextView) findViewById(R.id.location_time_text);
        mLocationDesTextView = (TextView) findViewById(R.id.location_description_text);

        mLocationCountryTextView = (TextView) findViewById(R.id.location_country_text);
        mLocationProvinceTextView = (TextView) findViewById(R.id.location_province_text);
        mLocationCityTextView = (TextView) findViewById(R.id.location_city_text);
        mLocationCountyTextView = (TextView) findViewById(R.id.location_county_text);
        mLocationRoadTextView = (TextView) findViewById(R.id.location_road_text);
        mLocationPOITextView = (TextView) findViewById(R.id.location_poi_text);
        mLocationAreaCodeTextView = (TextView) findViewById(R.id.location_area_code_text);
        mLocationCityCodeTextView = (TextView) findViewById(R.id.location_city_code_text);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        IwdsLog.d(this, "onCreate()");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_iwds_location);

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
                RemoteLocationServiceManager.IWDS_NETWORK_PROVIDER);
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
                Toast.makeText(IwdsLocationActivity.this, "Remote available",
                        Toast.LENGTH_SHORT).show();

                result = m_service.requestLastKnownLocation(
                        RemoteLocationServiceManager.IWDS_NETWORK_PROVIDER,
                        m_locationListener);
                if (!result) {
                    IwdsLog.d(this, "Failed to request last known location");
                    Toast.makeText(IwdsLocationActivity.this,
                            "Failed to request last known location",
                            Toast.LENGTH_SHORT).show();
                }

                result = m_service
                        .requestNetworkStatus(m_networkStatusListener);
                if (!result) {
                    IwdsLog.d(this, "Failed to get network current status");
                    Toast.makeText(IwdsLocationActivity.this,
                            "Failed to get network current status",
                            Toast.LENGTH_SHORT).show();
                }

                result = m_service.registerLocationListener(
                        RemoteLocationServiceManager.IWDS_NETWORK_PROVIDER,
                        m_locationListener);
                if (!result) {
                    IwdsLog.d(this,
                            "Failed to register network location listener");
                    Toast.makeText(IwdsLocationActivity.this,
                            "Failed to register network location listener",
                            Toast.LENGTH_SHORT).show();
                }

                m_service.registerLocationListener(
                        RemoteLocationServiceManager.GPS_PROVIDER,
                        m_locationListener);
                if (!result) {
                    IwdsLog.d(this, "Failed to register gps location listener");
                    Toast.makeText(IwdsLocationActivity.this,
                            "Failed to register gps location listener",
                            Toast.LENGTH_SHORT).show();
                }

                result = m_service
                        .registerGpsStatusListener(m_gpsStatusListener);
                if (!result) {
                    IwdsLog.d(this, "Failed to register GPS status listener");
                    Toast.makeText(IwdsLocationActivity.this,
                            "Failed to register GPS status listener",
                            Toast.LENGTH_SHORT).show();
                }

                result = m_service
                        .registerNetworkStatusListener(m_networkStatusListener);
                if (!result) {
                    IwdsLog.d(this,
                            "Failed to register network status listener");
                    Toast.makeText(IwdsLocationActivity.this,
                            "Failed to register network status listener",
                            Toast.LENGTH_SHORT).show();
                }

                result = m_service
                        .requestProviderList(true, m_providerListener);
                if (!result) {
                    IwdsLog.d(this, "Failed to get all enabled providers");
                    Toast.makeText(IwdsLocationActivity.this,
                            "Failed to get all enabled providers",
                            Toast.LENGTH_SHORT).show();
                }

                result = m_service.requestProviderStatus(
                        RemoteLocationServiceManager.GPS_PROVIDER,
                        m_providerListener);
                if (!result) {
                    IwdsLog.d(this, "Failed to get gps provider status");
                    Toast.makeText(IwdsLocationActivity.this,
                            "Failed to get GPS provider status",
                            Toast.LENGTH_SHORT).show();
                }

                result = m_service.requestGpsStatus(m_gpsStatusListener);
                if (!result) {
                    IwdsLog.d(this, "Failed to get GPS current status");
                    Toast.makeText(IwdsLocationActivity.this,
                            "Failed to get GPS current status",
                            Toast.LENGTH_SHORT).show();
                }

            } else {
                IwdsLog.d(this, "Remote unavailable");
                Toast.makeText(IwdsLocationActivity.this, "Remote unavailable",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    private RemoteProviderListener m_providerListener = new RemoteProviderListener() {

        @Override
        public void onProviderStatus(boolean enabled, String provider) {
            if (enabled) {
                Toast.makeText(IwdsLocationActivity.this, "远端设备 GPS 可用",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(IwdsLocationActivity.this, "远端设备 GPS 不可用",
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

    private RemoteNetworkStatusListener m_networkStatusListener = new RemoteNetworkStatusListener() {

        @Override
        public void onNetworkStatusChanged(int status) {
            if (status == RemoteLocationServiceManager.NETWORK_STATUS_UNAVAILABLE) {
                Toast.makeText(IwdsLocationActivity.this, "远端设备网络不可用",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(IwdsLocationActivity.this, "远端设备网络可用",
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
                Toast.makeText(IwdsLocationActivity.this, "远端设备 GPS 可用",
                        Toast.LENGTH_SHORT).show();
                break;
            case RemoteGpsStatus.GPS_EVENT_STOPPED:
                Toast.makeText(IwdsLocationActivity.this, "远端设备 GPS 不可用",
                        Toast.LENGTH_SHORT).show();
                break;
            }
        }

        @Override
        public void onGpsStatus(RemoteGpsStatus status) {
            Toast.makeText(IwdsLocationActivity.this,
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

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(location.getTime());

            mLocationTimeTextView.setText(df.format(date));
            mLocationDesTextView.setText(location.getAddress());
            mLocationCountryTextView.setText(location.getCountry());
            if (location.getProvince() == null) {
                mLocationProvinceTextView.setText("null");
            } else {
                mLocationProvinceTextView.setText(location.getProvince());
            }
            mLocationCityTextView.setText(location.getCity());
            mLocationCountyTextView.setText(location.getDistrict());
            mLocationRoadTextView.setText(location.getRoad());
            mLocationPOITextView.setText(location.getPoiName());
            mLocationCityCodeTextView.setText(location.getCityCode());
            mLocationAreaCodeTextView.setText(location.getAdCode());
        } else {
            Toast.makeText(
                    IwdsLocationActivity.this,
                    "混合定位失败: "
                            + RemoteLocationErrorCode
                                    .errorCodeToString(location.getErrorCode()),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
