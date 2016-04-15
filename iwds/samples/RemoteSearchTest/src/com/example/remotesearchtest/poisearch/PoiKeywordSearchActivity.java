package com.example.remotesearchtest.poisearch;

import java.util.ArrayList;
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
import com.ingenic.iwds.smartlocation.search.core.RemotePoiItem;
import com.ingenic.iwds.smartlocation.search.core.RemoteSuggestionCity;
import com.ingenic.iwds.smartlocation.search.help.RemoteInputQuery;
import com.ingenic.iwds.smartlocation.search.help.RemoteInputtips;
import com.ingenic.iwds.smartlocation.search.help.RemoteInputtips.RemoteInputtipsListener;
import com.ingenic.iwds.smartlocation.search.help.RemoteTip;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiItemDetail;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiQuery;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiResult;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiSearch;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiSearch.RemotePoiSearchListener;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

public class PoiKeywordSearchActivity extends Activity implements
        ConnectionCallbacks {
    private ServiceClient m_client;
    private RemoteSearchServiceManager m_service;
    private boolean m_available;
    private AutoCompleteTextView m_searchText;
    private EditText m_editCity;
    private String m_keyWord = "";
    private ProgressDialog m_dialog;
    private Button m_searchButton;
    private Button m_nextButton;
    private int m_currentPage = 0;
    private RemotePoiQuery m_query;
    private RemotePoiResult m_result;

    private void initView() {
        m_searchButton = (Button) findViewById(R.id.searchButton);
        m_searchButton.setOnClickListener(m_buttonListener);
        m_nextButton = (Button) findViewById(R.id.nextButton);
        m_nextButton.setOnClickListener(m_buttonListener);
        m_searchText = (AutoCompleteTextView) findViewById(R.id.keyWord);
        m_searchText.addTextChangedListener(m_textWatcher);
        m_editCity = (EditText) findViewById(R.id.city);
        m_dialog = new ProgressDialog(this);

        m_nextButton.setClickable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_poikeyworksearch);
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

    public void showDialog() {
        m_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        m_dialog.setIndeterminate(false);
        m_dialog.setCancelable(true);
        m_dialog.setMessage("正在搜索:\n" + m_keyWord);
        m_dialog.show();
    }

    public void dismissDialog() {
        if (m_dialog != null) {
            m_dialog.dismiss();
        }
    }

    private void doSearchQuery() {
        showDialog();
        m_currentPage = 0;
        m_query = new RemotePoiQuery(m_keyWord, "", m_editCity.getText()
                .toString());
        m_query.setPageSize(3);
        m_query.setPageNum(m_currentPage);
        m_query.setQueryLanguage(RemotePoiSearch.CHINESE);

        RemotePoiSearch search = new RemotePoiSearch(m_query);
        search.setPoiSearchListener(m_searchListener);

        m_service.requestPoiSearch(search);
    }

    private void searchButton() {
        m_keyWord = SearchUtils.checkEditText(m_searchText);
        if ("".equals(m_keyWord)) {
            ToastUtil.show(PoiKeywordSearchActivity.this, "请输入搜索关键字");
            return;
        } else {
            doSearchQuery();
        }
    }

    private void nextButton() {
        if (m_query != null && m_result != null) {
            if (m_result.getPageCount() - 1 > m_currentPage) {
                m_currentPage++;
                m_query.setPageNum(m_currentPage);
                RemotePoiSearch search = new RemotePoiSearch(m_query);
                search.setPoiSearchListener(m_searchListener);

                m_service.requestPoiSearch(search);
            } else {
                ToastUtil.show(PoiKeywordSearchActivity.this,
                        R.string.no_result);
            }
        }
    }

    private RemoteStatusListener m_remoteStatusListener = new RemoteStatusListener() {

        @Override
        public void onAvailable(boolean available) {
            m_available = available;
            IwdsLog.d(this, "Remote available: " + available);
            if (m_available) {
                ToastUtil.show(PoiKeywordSearchActivity.this,
                        R.string.remote_available);
            } else {
                ToastUtil.show(PoiKeywordSearchActivity.this,
                        R.string.remote_unavailable);
            }
        }
    };

    private OnClickListener m_buttonListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.searchButton:
                if (m_available) {
                    searchButton();
                } else {
                    ToastUtil.show(PoiKeywordSearchActivity.this,
                            R.string.remote_unavailable);
                }
                break;
            case R.id.nextButton:
                if (m_available) {
                    nextButton();
                } else {
                    ToastUtil.show(PoiKeywordSearchActivity.this,
                            R.string.remote_unavailable);
                }
                break;
            default:
                break;
            }
        }
    };

    private void showSuggestCity(List<RemoteSuggestionCity> cities) {
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        ToastUtil.show(PoiKeywordSearchActivity.this, infomation);
    }

    private void showPoiItems(List<RemotePoiItem> poiItems) {
        String infomation = "搜索结果\n\n";

        for (int i = 0; i < poiItems.size(); i++) {
            infomation += poiItems.get(i).getAdName()
                    + poiItems.get(i).getSnippet() + "\n";
        }
        ToastUtil.show(PoiKeywordSearchActivity.this, infomation);
    }

    private TextWatcher m_textWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
            String newText = s.toString().trim();
            RemoteInputtips inputTips = new RemoteInputtips(
                    new RemoteInputtipsListener() {

                        @Override
                        public void onGetInputtips(List<RemoteTip> tipList,
                                int errorCode) {
                            if (errorCode == 0) {
                                List<String> listString = new ArrayList<String>();
                                for (int i = 0; i < tipList.size(); i++)
                                    listString.add(tipList.get(i).getName());

                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                        getApplicationContext(),
                                        R.layout.route_inputs, listString);
                                m_searchText.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });

            RemoteInputQuery query = new RemoteInputQuery(newText, m_editCity
                    .getText().toString());
            inputTips.setQuery(query);

            m_service.requestInputtips(inputTips);
        }

        @Override
        public void afterTextChanged(Editable s) {

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
                        List<RemotePoiItem> poiItems = result.getPois();
                        List<RemoteSuggestionCity> suggestionCities = result
                                .getSearchSuggestionCitys();

                        if (poiItems != null && poiItems.size() > 0) {
                            showPoiItems(poiItems);
                            m_nextButton.setClickable(true);

                        } else if (suggestionCities != null
                                && suggestionCities.size() > 0) {
                            showSuggestCity(suggestionCities);
                        } else {
                            ToastUtil.show(PoiKeywordSearchActivity.this,
                                    R.string.no_result);
                        }
                    }
                } else {
                    ToastUtil.show(PoiKeywordSearchActivity.this,
                            R.string.no_result);
                }
            } else {
                ToastUtil.show(PoiKeywordSearchActivity.this, "搜索失败："
                        + RemoteLocationErrorCode.errorCodeToString(errorCode));
            }
        }

        @Override
        public void onPoiItemDetailSearched(RemotePoiItemDetail poiItemDetail,
                int errorCode) {

        }

    };

}
