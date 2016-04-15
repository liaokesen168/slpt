package com.example.remotesearchtest.poisearch;

import java.util.List;

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
import com.ingenic.iwds.smartlocation.search.core.RemotePoiItem;
import com.ingenic.iwds.smartlocation.search.core.RemoteSuggestionCity;
import com.ingenic.iwds.smartlocation.search.poisearch.RemoteCinema;
import com.ingenic.iwds.smartlocation.search.poisearch.RemoteDining;
import com.ingenic.iwds.smartlocation.search.poisearch.RemoteHotel;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiItemDetail;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiQuery;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiResult;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiSearch;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiSearch.RemotePoiSearchListener;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiSearchBound;
import com.ingenic.iwds.smartlocation.search.poisearch.RemoteScenic;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class PoiAroundSearchActivity extends Activity implements
        ConnectionCallbacks {
    private ServiceClient m_client;
    private RemoteSearchServiceManager m_service;
    private boolean m_available;

    private ProgressDialog m_dialog;
    private Spinner m_selectDeep;
    private String[] m_itemDeep = { "餐饮", "景区", "酒店", "影院" };
    private String m_deepType;
    private Spinner m_selectType;
    private String[] m_itemType = { "所有poi", "有团购", "有优惠", "有团购或者优惠" };
    private int m_searchType = 0;
    private int m_currentSearchType = 0;
    private RemotePoiResult m_result;
    private int m_currentPage = 0;
    private RemotePoiQuery m_query;
    private RemoteLatLonPoint m_point = new RemoteLatLonPoint(22.536995,
            113.952713);
    private RemotePoiSearch m_search;
    private Button m_nextButton;
    private Button m_searchButton;
    private List<RemotePoiItem> m_poiItemList;

    private void initView() {
        m_selectDeep = (Spinner) findViewById(R.id.spinnerdeep);
        ArrayAdapter<String> adapter0 = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, m_itemDeep);
        adapter0.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_selectDeep.setAdapter(adapter0);
        m_selectDeep.setOnItemSelectedListener(m_spinnerListener);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, m_itemType);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_selectType = (Spinner) findViewById(R.id.searchType);
        m_selectType.setAdapter(adapter1);
        m_selectType.setOnItemSelectedListener(m_spinnerListener);

        m_searchButton = (Button) findViewById(R.id.searchButton);
        m_searchButton.setOnClickListener(m_buttonListener);

        m_nextButton = (Button) findViewById(R.id.nextButton);
        m_nextButton.setOnClickListener(m_buttonListener);
        m_nextButton.setClickable(false);

        m_dialog = new ProgressDialog(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.setContentView(R.layout.activity_poiaroundsearch);
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

    private void doSearchQuery() {
        showDialog();
        m_currentPage = 0;
        m_query = new RemotePoiQuery("", m_deepType, "深圳市");
        m_query.setPageSize(1);
        m_query.setPageNum(m_currentPage);
        m_query.setBound(new RemotePoiSearchBound(m_point, 5000, true));

        m_searchType = m_currentSearchType;

        switch (m_searchType) {
        case 0:
            m_query.setLimitDiscount(false);
            m_query.setLimitGroupbuy(false);
            break;

        case 1:
            m_query.setLimitDiscount(false);
            m_query.setLimitGroupbuy(true);
            break;

        case 2:
            m_query.setLimitDiscount(true);
            m_query.setLimitGroupbuy(false);
            break;

        case 3:
            m_query.setLimitDiscount(true);
            m_query.setLimitGroupbuy(true);
            break;

        default:
            break;
        }

        if (m_point != null) {
            m_search = new RemotePoiSearch(m_query);
            m_search.setPoiSearchListener(m_searchListener);

            m_service.requestPoiSearch(m_search);
        }
    }

    private void nextSearch() {
        if (m_query != null && m_search != null && m_result != null) {
            if (m_result.getPageCount() - 1 > m_currentPage) {
                m_currentPage++;

                m_query.setPageNum(m_currentPage);

                m_service.requestPoiSearch(m_search);
            } else {
                ToastUtil
                        .show(PoiAroundSearchActivity.this, R.string.no_result);
            }
        }
    }

    public void doSearchPoiDetail(String poiId) {
        if (m_search != null && poiId != null && !poiId.isEmpty()) {

            m_query.setPoiId(poiId);

            RemotePoiSearch search = new RemotePoiSearch(m_query);
            search.setPoiSearchListener(m_searchDetailListener);
            m_service.requestPoiDetailSearch(search);
        }
    }

    private void showSuggestCity(List<RemoteSuggestionCity> cities) {
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        ToastUtil.show(PoiAroundSearchActivity.this, infomation);

    }

    private void showPoiItems(List<RemotePoiItem> poiItems) {
        String infomation = "搜索结果:" + poiItems.get(0).getTitle() + "\n\n";

        for (int i = 0; i < poiItems.size(); i++) {
            infomation += poiItems.get(i).getAdName()
                    + poiItems.get(i).getSnippet() + "\n";
        }
        ToastUtil.show(PoiAroundSearchActivity.this, infomation);

        doSearchPoiDetail(poiItems.get(0).getPoiId());
    }

    private void showPoiDetail(StringBuffer sb) {
        String infomation = "详细信息\n";
        infomation += sb.toString();
        ToastUtil.show(PoiAroundSearchActivity.this, infomation);

    }

    private StringBuffer getDeepInfo(RemotePoiItemDetail result,
            StringBuffer sbuBuffer) {
        switch (result.getDeepType()) {
        // 餐饮深度信息
        case DINING:
            if (result.getDining() != null) {
                RemoteDining dining = result.getDining();
                sbuBuffer
                        .append("\n菜系：" + dining.getTag() + "\n特色："
                                + dining.getRecommend() + "\n来源："
                                + dining.getDeepsrc());
            }
            break;
        // 酒店深度信息
        case HOTEL:
            if (result.getHotel() != null) {
                RemoteHotel hotel = result.getHotel();
                sbuBuffer.append("\n价位：" + hotel.getLowestPrice() + "\n卫生："
                        + hotel.getHealthRating() + "\n来源："
                        + hotel.getDeepsrc());
            }
            break;
        // 景区深度信息
        case SCENIC:
            if (result.getScenic() != null) {
                RemoteScenic scenic = result.getScenic();
                sbuBuffer
                        .append("\n价钱：" + scenic.getPrice() + "\n推荐："
                                + scenic.getRecommend() + "\n来源："
                                + scenic.getDeepsrc());
            }
            break;
        // 影院深度信息
        case CINEMA:
            if (result.getCinema() != null) {
                RemoteCinema cinema = result.getCinema();
                sbuBuffer.append("\n停车：" + cinema.getParking() + "\n简介："
                        + cinema.getIntro() + "\n来源：" + cinema.getDeepsrc());
            }
            break;
        default:
            break;
        }
        return sbuBuffer;
    }

    private RemoteStatusListener m_remoteStatusListener = new RemoteStatusListener() {
        @Override
        public void onAvailable(boolean available) {
            m_available = available;
            IwdsLog.d(this, "Remote available: " + available);
            if (m_available) {
                ToastUtil.show(PoiAroundSearchActivity.this,
                        R.string.remote_available);
            } else {
                ToastUtil.show(PoiAroundSearchActivity.this,
                        R.string.remote_unavailable);
            }
        }
    };

    private OnItemSelectedListener m_spinnerListener = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                int position, long id) {
            if (parent == m_selectDeep)
                m_deepType = m_itemDeep[position];
            else if (parent == m_selectType)
                m_currentSearchType = position;

            m_nextButton.setClickable(false);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            if (parent == m_selectDeep)
                m_deepType = "餐饮";
            else if (parent == m_selectType)
                m_currentSearchType = 0;

            m_nextButton.setClickable(false);

        }
    };

    private RemotePoiSearchListener m_searchDetailListener = new RemotePoiSearchListener() {

        @Override
        public void onPoiSearched(RemotePoiResult result, int errorCode) {

        }

        @Override
        public void onPoiItemDetailSearched(RemotePoiItemDetail poiItemDetail,
                int errorCode) {
            dismissDialog();
            if (errorCode == 0) {
                if (poiItemDetail != null) {
                    StringBuffer sb = new StringBuffer(
                            poiItemDetail.getSnippet());
                    if ((poiItemDetail.getGroupbuys() != null && poiItemDetail
                            .getGroupbuys().size() > 0)
                            || (poiItemDetail.getDiscounts() != null && poiItemDetail
                                    .getDiscounts().size() > 0)) {

                        if (poiItemDetail.getGroupbuys() != null
                                && poiItemDetail.getGroupbuys().size() > 0) {
                            sb.append("\n团购："
                                    + poiItemDetail.getGroupbuys().get(0)
                                            .getDetail());
                        }

                        if (poiItemDetail.getDiscounts() != null
                                && poiItemDetail.getDiscounts().size() > 0) {
                            sb.append("\n优惠："
                                    + poiItemDetail.getDiscounts().get(0)
                                            .getDetail());
                        }

                    } else {
                        sb = new StringBuffer("地址："
                                + poiItemDetail.getSnippet() + "\n电话："
                                + poiItemDetail.getTel() + "\n类型："
                                + poiItemDetail.getTypeDes());
                    }

                    if (poiItemDetail.getDeepType() != null) {
                        sb = getDeepInfo(poiItemDetail, sb);
                        showPoiDetail(sb);
                    } else {
                        ToastUtil.show(PoiAroundSearchActivity.this,
                                "此Poi点没有深度信息");
                    }

                } else {
                    ToastUtil.show(PoiAroundSearchActivity.this,
                            R.string.no_result);
                }
            } else {
                ToastUtil.show(PoiAroundSearchActivity.this, "搜索失败："
                        + RemoteLocationErrorCode.errorCodeToString(errorCode));
            }
        }

    };

    private RemotePoiSearchListener m_searchListener = new RemotePoiSearchListener() {

        @Override
        public void onPoiSearched(RemotePoiResult result, int errorCode) {
            dismissDialog();
            if (errorCode == 0) {
                if (result != null && result.getQuery() != null) {
                    if (result.getQuery().equals(m_query)) {
                        m_result = result;
                        m_poiItemList = m_result.getPois();
                        List<RemoteSuggestionCity> suggestionCities = m_result
                                .getSearchSuggestionCitys();
                        if (m_poiItemList != null && m_poiItemList.size() > 0) {
                            showPoiItems(m_poiItemList);
                            m_nextButton.setClickable(true);
                        } else if (suggestionCities != null
                                && suggestionCities.size() > 0) {
                            showSuggestCity(suggestionCities);
                        } else {
                            ToastUtil.show(PoiAroundSearchActivity.this,
                                    R.string.no_result);
                        }
                    }
                } else {
                    ToastUtil.show(PoiAroundSearchActivity.this,
                            R.string.no_result);
                }
            } else {
                ToastUtil.show(PoiAroundSearchActivity.this, "搜索失败："
                        + RemoteLocationErrorCode.errorCodeToString(errorCode));
            }
        }

        @Override
        public void onPoiItemDetailSearched(RemotePoiItemDetail poiItemDetail,
                int errorCode) {
        }

    };

    private OnClickListener m_buttonListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.searchButton:
                if (m_available) {
                    doSearchQuery();
                } else {
                    ToastUtil.show(PoiAroundSearchActivity.this,
                            R.string.remote_unavailable);
                }
                break;
            case R.id.nextButton:
                if (m_available) {
                    nextSearch();
                } else {
                    ToastUtil.show(PoiAroundSearchActivity.this,
                            R.string.remote_unavailable);
                }
                break;
            default:
                break;
            }
        }

    };
}