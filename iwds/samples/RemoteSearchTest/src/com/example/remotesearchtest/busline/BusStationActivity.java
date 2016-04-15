package com.example.remotesearchtest.busline;

import java.util.List;

import com.example.remotesearchtest.R;
import com.example.remotesearchtest.utils.SearchUtils;
import com.example.remotesearchtest.utils.ToastUtil;
import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceClient.ConnectionCallbacks;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.smartlocation.RemoteLocationErrorCode;
import com.ingenic.iwds.smartlocation.search.RemoteSearchServiceManager;
import com.ingenic.iwds.smartlocation.search.RemoteStatusListener;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusStationItem;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusStationQuery;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusStationResult;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusStationSearch;
import com.ingenic.iwds.smartlocation.search.busline.RemoteBusStationSearch.RemoteBusStationSearchListener;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class BusStationActivity extends Activity implements ConnectionCallbacks {
    private ServiceClient m_client;
    private RemoteSearchServiceManager m_service;
    private boolean m_available;
    private ProgressDialog m_dialog;
    private EditText m_searchNameEdit;
    private Spinner m_selectCitySpinner;
    private String[] m_itemCitys = { "深圳-0755", "北京-010", "上海-021" };
    private String m_cityCode = "";
    private int m_currentPage = 0;
    private RemoteBusStationResult m_result;
    private RemoteBusStationQuery m_query;
    private RemoteBusStationSearch m_search;
    private Button m_searchButton;
    private Button m_nextButton;
    private String m_searchName;

    private void initView() {
        m_searchButton = (Button) findViewById(R.id.searchButton);
        m_searchButton.setOnClickListener(m_buttonListener);

        m_nextButton = (Button) findViewById(R.id.nextButton);
        m_nextButton.setOnClickListener(m_buttonListener);
        m_nextButton.setClickable(false);

        m_selectCitySpinner = (Spinner) findViewById(R.id.spinner_city);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, m_itemCitys);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_selectCitySpinner.setAdapter(adapter);
        m_selectCitySpinner.setPrompt("请选择城市：");
        m_selectCitySpinner.setOnItemSelectedListener(m_spinnerListener);

        m_searchNameEdit = (EditText) findViewById(R.id.busName);

        m_dialog = new ProgressDialog(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_busstation);
        initView();
        m_client = new ServiceClient(this,
                ServiceManagerContext.SERVICE_REMOTE_SEARCH, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        m_client.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        m_client.disconnect();
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

    private void showDialog() {
        m_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        m_dialog.setIndeterminate(false);
        m_dialog.setCancelable(true);
        m_dialog.setMessage("正在搜索\n");
        m_dialog.show();
    }

    private void dismissDialog() {
        if (m_dialog != null) {
            m_dialog.dismiss();
        }
    }

    private void showBusStations(List<RemoteBusStationItem> items) {
        String infomation = "搜索结果\n";
        for (int i = 0; i < items.size(); i++) {
            infomation += "\n站台名: " + items.get(i).getBusStationName()
                    + "\nID: " + items.get(i).getBusStationId() + "\n";
        }
        ToastUtil.show(BusStationActivity.this, infomation);
    }

    private void doSearchQuery() {
        showDialog();
        m_currentPage = 0;

        m_query = new RemoteBusStationQuery(m_cityCode, m_searchName);
        m_query.setPageSize(2);
        m_query.setPageNumber(m_currentPage);

        m_search = new RemoteBusStationSearch(m_query);
        m_search.setBusStationSearchListener(m_searchListener);
        m_service.requestBusStationSearch(m_search);
    }

    public void doSearch() {
        m_searchName = SearchUtils.checkEditText(m_searchNameEdit);
        if (m_searchName.equals("")) {
            ToastUtil.show(BusStationActivity.this, "请输入公交站台名");
            return;
        } else {
            doSearchQuery();
        }
    }

    public void nextSearch() {
        if (m_query != null && m_search != null && m_result != null) {
            if (m_result.getPageCount() - 1 > m_currentPage) {
                m_currentPage++;

                m_query.setPageNumber(m_currentPage);

                m_service.requestBusStationSearch(m_search);
            } else {
                ToastUtil.show(BusStationActivity.this, R.string.no_result);
            }
        }
    }

    private RemoteStatusListener m_remoteStatusListener = new RemoteStatusListener() {

        @Override
        public void onAvailable(boolean available) {
            m_available = available;
            IwdsLog.d(this, "Remote available: " + available);
            if (m_available) {
                ToastUtil.show(BusStationActivity.this,
                        R.string.remote_available);
            } else {
                ToastUtil.show(BusStationActivity.this,
                        R.string.remote_unavailable);
            }
        }
    };

    private OnItemSelectedListener m_spinnerListener = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                int position, long id) {
            String cityString = m_itemCitys[position];
            m_cityCode = cityString.substring(cityString.indexOf("-") + 1);
            m_nextButton.setClickable(false);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            m_cityCode = "0755";
            m_nextButton.setClickable(false);
        }
    };

    private RemoteBusStationSearchListener m_searchListener = new RemoteBusStationSearchListener() {

        @Override
        public void onBusStationSearched(RemoteBusStationResult result,
                int errorCode) {
            dismissDialog();
            if (errorCode == 0) {
                if (result != null && result.getQuery() != null
                        && result.getPageCount() > 0
                        && result.getBusStations() != null
                        && result.getBusStations().size() > 0) {
                    if (result.getQuery().equals(m_query)) {
                        m_result = result;
                        showBusStations(m_result.getBusStations());
                        m_nextButton.setClickable(true);
                    }
                } else {
                    ToastUtil.show(BusStationActivity.this, R.string.no_result);
                }

            } else {
                ToastUtil.show(BusStationActivity.this, "搜索失败："
                        + RemoteLocationErrorCode.errorCodeToString(errorCode));
            }
        }
    };

    private OnClickListener m_buttonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.searchButton:
                if (m_available) {
                    doSearch();
                } else {
                    ToastUtil.show(BusStationActivity.this,
                            R.string.remote_unavailable);
                }
                break;
            case R.id.nextButton:
                if (m_available) {
                    nextSearch();
                } else {
                    ToastUtil.show(BusStationActivity.this,
                            R.string.remote_unavailable);
                }
                break;
            default:
                break;
            }
        }

    };
}
