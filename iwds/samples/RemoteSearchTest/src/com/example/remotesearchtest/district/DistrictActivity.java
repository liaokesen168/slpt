package com.example.remotesearchtest.district;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.ingenic.iwds.smartlocation.search.district.RemoteDistrictItem;
import com.ingenic.iwds.smartlocation.search.district.RemoteDistrictQuery;
import com.ingenic.iwds.smartlocation.search.district.RemoteDistrictResult;
import com.ingenic.iwds.smartlocation.search.district.RemoteDistrictSearch;
import com.ingenic.iwds.smartlocation.search.district.RemoteDistrictSearch.RemoteDistrictSearchListener;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class DistrictActivity extends Activity implements ConnectionCallbacks {
    private ServiceClient m_client;
    private RemoteSearchServiceManager m_service;
    private boolean m_available;

    // 当前选中的级别
    private String m_selectedLevel = RemoteDistrictQuery.KEYWORDS_COUNTRY;

    // 当前行政区划
    private RemoteDistrictItem m_currentDistrictItem;

    // 下级行政区划集合
    private Map<String, List<RemoteDistrictItem>> m_subDistrictMap = new HashMap<String, List<RemoteDistrictItem>>();

    // 省级列表
    private List<RemoteDistrictItem> m_provinceList = new ArrayList<RemoteDistrictItem>();

    // 市级列表
    private List<RemoteDistrictItem> m_cityList = new ArrayList<RemoteDistrictItem>();

    // 区县级列表
    private List<RemoteDistrictItem> m_districtList = new ArrayList<RemoteDistrictItem>();

    // 是否已经初始化
    private boolean m_isInit = false;

    private TextView m_countryText;
    private TextView m_provinceText;
    private TextView m_cityText;
    private TextView m_districtText;
    private Spinner m_provinceSpinner;
    private Spinner m_citySpinner;
    private Spinner m_districtSpinner;

    private void initView() {
        m_countryText = (TextView) findViewById(R.id.tv_countryInfo);
        m_provinceText = (TextView) findViewById(R.id.tv_provinceInfo);
        m_cityText = (TextView) findViewById(R.id.tv_cityInfo);
        m_districtText = (TextView) findViewById(R.id.tv_districtInfo);

        m_provinceSpinner = (Spinner) findViewById(R.id.spinner_province);
        m_citySpinner = (Spinner) findViewById(R.id.spinner_city);
        m_districtSpinner = (Spinner) findViewById(R.id.spinner_district);

        m_provinceSpinner.setOnItemSelectedListener(m_spinnerListener);
        m_citySpinner.setOnItemSelectedListener(m_spinnerListener);
        m_districtSpinner.setOnItemSelectedListener(m_spinnerListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_district);
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

    private void init() {
        RemoteDistrictSearch search = new RemoteDistrictSearch();
        search.setDistrictSearchListener(m_searchListener);

        RemoteDistrictQuery query = new RemoteDistrictQuery("中国",
                RemoteDistrictQuery.KEYWORDS_COUNTRY, 0);
        search.setQuery(query);

        m_service.requestDistrictSearch(search);
    }

    private void querySubDistrict(RemoteDistrictItem districtItem) {
        IwdsLog.d(this, "districtItem=" + districtItem);
        RemoteDistrictSearch search = new RemoteDistrictSearch();
        search.setDistrictSearchListener(m_searchListener);

        RemoteDistrictQuery query = new RemoteDistrictQuery(
                districtItem.getName(), districtItem.getLevel(), 0);
        search.setQuery(query);

        m_service.requestDistrictSearch(search);
    }

    private String getDistrictInfoStr(RemoteDistrictItem districtItem) {
        StringBuffer sb = new StringBuffer();
        String name = districtItem.getName();
        String adcode = districtItem.getAdCode();
        String level = districtItem.getLevel();
        String citycode = districtItem.getCityCode();
        RemoteLatLonPoint center = districtItem.getCenter();
        sb.append("区划名称:" + name + "\n");
        sb.append("区域编码:" + adcode + "\n");
        sb.append("城市编码:" + citycode + "\n");
        sb.append("区划级别:" + level + "\n");
        if (null != center) {
            sb.append("经纬度:(" + center.getLongitude() + ", "
                    + center.getLatitude() + ")\n");
        }
        return sb.toString();
    }

    private void setSpinnerView(List<RemoteDistrictItem> subDistrictList) {
        List<String> nameList = new ArrayList<String>();
        if (subDistrictList != null && subDistrictList.size() > 0) {
            for (int i = 0; i < subDistrictList.size(); i++) {
                nameList.add(subDistrictList.get(i).getName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, nameList);
            if (m_selectedLevel
                    .equalsIgnoreCase(RemoteDistrictQuery.KEYWORDS_COUNTRY)) {
                m_provinceList = subDistrictList;
                m_provinceSpinner.setAdapter(adapter);
            }

            if (m_selectedLevel
                    .equalsIgnoreCase(RemoteDistrictQuery.KEYWORDS_PROVINCE)) {
                m_cityList = subDistrictList;
                m_citySpinner.setAdapter(adapter);
            }

            if (m_selectedLevel
                    .equalsIgnoreCase(RemoteDistrictQuery.KEYWORDS_CITY)) {
                m_districtList = subDistrictList;
                // 如果没有区县，将区县说明置空
                if (null == nameList || nameList.size() <= 0) {
                    m_districtText.setText("");
                }
                m_districtSpinner.setAdapter(adapter);
            }
        } else {
            if (m_selectedLevel
                    .equalsIgnoreCase(RemoteDistrictQuery.KEYWORDS_COUNTRY)) {
                m_provinceSpinner.setAdapter(null);
                m_citySpinner.setAdapter(null);
                m_districtSpinner.setAdapter(null);
                m_provinceText.setText("");
                m_cityText.setText("");
                m_districtText.setText("");
            }

            if (m_selectedLevel
                    .equalsIgnoreCase(RemoteDistrictQuery.KEYWORDS_PROVINCE)) {
                m_citySpinner.setAdapter(null);
                m_districtSpinner.setAdapter(null);
                m_cityText.setText("");
                m_districtText.setText("");
            }

            if (m_selectedLevel
                    .equalsIgnoreCase(RemoteDistrictQuery.KEYWORDS_CITY)) {
                m_districtSpinner.setAdapter(null);
                m_districtText.setText("");
            }
        }
    }

    private OnItemSelectedListener m_spinnerListener = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                int position, long id) {
            RemoteDistrictItem districtItem = null;
            switch (parent.getId()) {
            case R.id.spinner_province:
                districtItem = m_provinceList.get(position);
                m_selectedLevel = RemoteDistrictQuery.KEYWORDS_PROVINCE;
                m_provinceText.setText(getDistrictInfoStr(districtItem));
                break;
            case R.id.spinner_city:
                m_selectedLevel = RemoteDistrictQuery.KEYWORDS_CITY;
                districtItem = m_cityList.get(position);
                m_cityText.setText(getDistrictInfoStr(districtItem));
                break;
            case R.id.spinner_district:
                m_selectedLevel = RemoteDistrictQuery.KEYWORDS_DISTRICT;
                districtItem = m_districtList.get(position);
                m_districtText.setText(getDistrictInfoStr(districtItem));
                break;
            default:
                break;
            }

            if (districtItem != null) {
                m_currentDistrictItem = districtItem;
                // 先查缓存如果缓存存在则直接从缓存中查找，无需再执行查询请求
                List<RemoteDistrictItem> cache = m_subDistrictMap
                        .get(districtItem.getAdCode());
                if (null != cache) {
                    setSpinnerView(cache);
                } else {
                    querySubDistrict(districtItem);
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    };

    private RemoteStatusListener m_remoteStatusListener = new RemoteStatusListener() {

        @Override
        public void onAvailable(boolean available) {
            m_available = available;
            IwdsLog.d(this, "Remote available: " + available);
            if (m_available) {
                init();
                ToastUtil
                        .show(DistrictActivity.this, R.string.remote_available);
            } else {
                ToastUtil.show(DistrictActivity.this,
                        R.string.remote_unavailable);
            }
        }
    };

    private RemoteDistrictSearchListener m_searchListener = new RemoteDistrictSearchListener() {

        @Override
        public void onDistrictSearched(RemoteDistrictResult result,
                int errorCode) {
            List<RemoteDistrictItem> subDistrictList = null;

            if (errorCode == 0) {
                if (result != null) {
                    List<RemoteDistrictItem> district = result.getDistrict();
                    if (!m_isInit) {
                        m_isInit = true;
                        m_currentDistrictItem = district.get(0);
                        m_countryText
                                .setText(getDistrictInfoStr(m_currentDistrictItem));
                    }

                    // 将查询得到的区划的下级区划写入缓存
                    for (int i = 0; i < district.size(); i++) {
                        RemoteDistrictItem districtItem = district.get(i);
                        m_subDistrictMap.put(districtItem.getAdCode(),
                                districtItem.getSubDistrict());
                    }
                    // 获取当前区划的下级区划列表
                    subDistrictList = m_subDistrictMap
                            .get(m_currentDistrictItem.getAdCode());
                } else {
                    ToastUtil.show(DistrictActivity.this, "查询失败："
                            + R.string.no_result);
                }
            } else {
                ToastUtil.show(DistrictActivity.this, "查询失败："
                        + RemoteLocationErrorCode.errorCodeToString(errorCode));
            }
            setSpinnerView(subDistrictList);
        }
    };
}
