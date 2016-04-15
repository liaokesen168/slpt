package com.example.remotesearchtest.geocoder;

import com.example.remotesearchtest.R;
import com.example.remotesearchtest.utils.ToastUtil;
import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceClient.ConnectionCallbacks;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.smartlocation.RemoteLocationErrorCode;
import com.ingenic.iwds.smartlocation.search.RemoteSearchServiceManager;
import com.ingenic.iwds.smartlocation.search.RemoteStatusListener;
import com.ingenic.iwds.smartlocation.search.core.RemoteLatLonPoint;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteGeocodeAddress;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteGeocodeQuery;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteGeocodeResult;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteGeocodeSearch;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteGeocodeSearch.RemoteGeocodeSearchListener;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteRegeocodeQuery;
import com.ingenic.iwds.smartlocation.search.geocoder.RemoteRegeocodeResult;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GeocoderActivity extends Activity implements ConnectionCallbacks {
    private Button m_geoButton;
    private Button m_regeoButton;
    private ProgressDialog m_dialog;

    private ServiceClient m_client;
    private RemoteSearchServiceManager m_service;
    private boolean m_available = false;

    private RemoteLatLonPoint m_point = new RemoteLatLonPoint(40.046486,
            116.285194);
    private RemoteGeocodeSearch m_search;
    private RemoteGeocodeQuery m_geoQuery;
    private RemoteRegeocodeQuery m_regeoQuery;

    private void initView() {
        m_geoButton = (Button) findViewById(R.id.geoButton);
        m_regeoButton = (Button) findViewById(R.id.regeoButton);
        m_geoButton.setOnClickListener(m_buttonListener);
        m_regeoButton.setOnClickListener(m_buttonListener);
        m_dialog = new ProgressDialog(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_geocoder);
        initView();
        m_client = new ServiceClient(this,
                ServiceManagerContext.SERVICE_REMOTE_SEARCH, this);
    }

    @Override
    protected void onResume() {
        m_client.connect();
        super.onResume();
    }

    @Override
    protected void onPause() {
        m_client.disconnect();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConnected(ServiceClient serviceClient) {
        m_service = (RemoteSearchServiceManager) m_client
                .getServiceManagerContext();
        m_service.registerRemoteStatusListener(m_remoteStatusListener);
        m_search = new RemoteGeocodeSearch();
        m_search.setGeocodeSearchListener(m_geoListener);
    }

    @Override
    public void onDisconnected(ServiceClient serviceClient, boolean unexpected) {
        m_service.unregisterRemoteStatusListener(m_remoteStatusListener);
    }

    @Override
    public void onConnectFailed(ServiceClient serviceClient,
            ConnectFailedReason reason) {
        IwdsAssert.dieIf(this, true,
                "Failed to connect to remote search service");
    }

    private void getLatLon(String name) {
        showDialog();

        m_geoQuery = new RemoteGeocodeQuery("0755", name);
        m_search.setGeocodeQuery(m_geoQuery);
        m_service.requestGeocodeSearch(m_search);
    }

    private void getAddress(RemoteLatLonPoint point) {
        showDialog();

        m_regeoQuery = new RemoteRegeocodeQuery(point, 200,
                RemoteGeocodeSearch.IWDS);
        m_search.setRegeocodeQuery(m_regeoQuery);
        m_service.requestRegeocodeSearch(m_search);
    }

    public void showDialog() {
        m_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        m_dialog.setIndeterminate(false);
        m_dialog.setCancelable(true);
        m_dialog.setMessage("正在获取地址");
        m_dialog.show();
    }

    public void dismissDialog() {
        if (m_dialog != null) {
            m_dialog.dismiss();
        }
    }

    private RemoteStatusListener m_remoteStatusListener = new RemoteStatusListener() {

        @Override
        public void onAvailable(boolean available) {
            m_available = available;
            IwdsLog.d(this, "Remote available: " + available);
            if (m_available) {
                ToastUtil
                        .show(GeocoderActivity.this, R.string.remote_available);
            } else {
                ToastUtil.show(GeocoderActivity.this,
                        R.string.remote_unavailable);
            }
        }
    };

    private RemoteGeocodeSearchListener m_geoListener = new RemoteGeocodeSearchListener() {
        @Override
        public void onRegeocodeSearched(RemoteRegeocodeResult result,
                int errorCode) {
            dismissDialog();
            if (errorCode == 0) {
                if (result != null
                        && result.getRegeocodeAddress() != null
                        && result.getRegeocodeAddress().getFormatAddress() != null) {
                    String addressName = result.getRegeocodeAddress()
                            .getFormatAddress() + "附近";
                    ToastUtil.show(GeocoderActivity.this, addressName);
                } else {
                    ToastUtil.show(GeocoderActivity.this, R.string.no_result);
                }
            } else {
                ToastUtil.show(GeocoderActivity.this, "Search error: "
                        + RemoteLocationErrorCode.errorCodeToString(errorCode));
            }
        }

        @Override
        public void onGeocodeSearched(RemoteGeocodeResult result, int errorCode) {
            dismissDialog();
            if (errorCode == 0) {
                if (result != null && result.getGeocodeAddressList() != null
                        && result.getGeocodeAddressList().size() > 0) {
                    RemoteGeocodeAddress address = result
                            .getGeocodeAddressList().get(0);
                    String addressName = "经纬度值:" + address.getLatLonPoint()
                            + "\n\n位置描述:" + address.getFormatAddress();
                    ToastUtil.show(GeocoderActivity.this, addressName);

                } else {
                    ToastUtil.show(GeocoderActivity.this, R.string.no_result);
                }

            } else {
                ToastUtil.show(GeocoderActivity.this, "搜索失败："
                        + RemoteLocationErrorCode.errorCodeToString(errorCode));
            }
        }
    };

    private OnClickListener m_buttonListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.geoButton:
                if (m_available) {
                    getLatLon("创维半导体设计大厦");
                } else {
                    ToastUtil.show(GeocoderActivity.this,
                            R.string.remote_unavailable);
                }
                break;

            case R.id.regeoButton:
                if (m_available) {
                    getAddress(m_point);
                } else {
                    ToastUtil.show(GeocoderActivity.this,
                            R.string.remote_unavailable);
                }
                break;

            default:
                break;
            }
        }

    };
}
