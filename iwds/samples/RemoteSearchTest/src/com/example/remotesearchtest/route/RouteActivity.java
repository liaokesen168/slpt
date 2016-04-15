package com.example.remotesearchtest.route;

import java.util.List;

import com.example.remotesearchtest.R;
import com.example.remotesearchtest.route.RouteSearchPoiDialog.OnListItemClick;
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
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiItemDetail;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiQuery;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiResult;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiSearch;
import com.ingenic.iwds.smartlocation.search.poisearch.RemotePoiSearch.RemotePoiSearchListener;
import com.ingenic.iwds.smartlocation.search.route.RemoteBusRouteQuery;
import com.ingenic.iwds.smartlocation.search.route.RemoteBusRouteResult;
import com.ingenic.iwds.smartlocation.search.route.RemoteDriveRouteQuery;
import com.ingenic.iwds.smartlocation.search.route.RemoteDriveRouteResult;
import com.ingenic.iwds.smartlocation.search.route.RemoteFromAndTo;
import com.ingenic.iwds.smartlocation.search.route.RemoteRouteSearch;
import com.ingenic.iwds.smartlocation.search.route.RemoteRouteSearch.RemoteRouteSearchListener;
import com.ingenic.iwds.smartlocation.search.route.RemoteWalkRouteQuery;
import com.ingenic.iwds.smartlocation.search.route.RemoteWalkRouteResult;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

public class RouteActivity extends Activity implements ConnectionCallbacks {
    private ServiceClient m_client;
    private RemoteSearchServiceManager m_service;
    private boolean m_available;
    private ProgressDialog m_dialog;
    private EditText m_startEdit;
    private EditText m_endEdit;
    private String m_start;
    private String m_end;
    private RadioButton m_busButton;
    private RadioButton m_driveButton;
    private RadioButton m_walkButton;
    private Button m_searchButton;
    private int m_routeType;
    private RemoteRouteSearch m_search;
    private RemotePoiQuery m_startPoiQuery;
    private RemotePoiQuery m_endPoiQuery;
    private RemoteLatLonPoint m_startPoint;
    private RemoteLatLonPoint m_endPoint;

    private void initView() {
        m_startEdit = (EditText) findViewById(R.id.start_text);
        m_endEdit = (EditText) findViewById(R.id.end_text);
        m_busButton = (RadioButton) findViewById(R.id.busButton);
        m_busButton.setOnClickListener(m_buttonListener);
        m_driveButton = (RadioButton) findViewById(R.id.driveButton);
        m_driveButton.setOnClickListener(m_buttonListener);
        m_walkButton = (RadioButton) findViewById(R.id.walkButton);
        m_walkButton.setOnClickListener(m_buttonListener);
        m_searchButton = (Button) findViewById(R.id.searchButton);
        m_searchButton.setOnClickListener(m_buttonListener);
        m_dialog = new ProgressDialog(this);
        m_busButton.setChecked(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_route);
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
        m_search = new RemoteRouteSearch();
        m_search.setRouteSearchListener(m_routeSearchListener);
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

    private void searchRouteResult(RemoteLatLonPoint startPoint,
            RemoteLatLonPoint endPoint) {
        showDialog();
        RemoteFromAndTo fromAndTo = new RemoteFromAndTo(startPoint, endPoint);
        switch (m_routeType) {
        case 0:
            RemoteBusRouteQuery busQuery = new RemoteBusRouteQuery(fromAndTo,
                    RemoteRouteSearch.BUS_DEFAULT, "深圳", 0);
            m_search.setBusRouteQuery(busQuery);
            m_service.requestBusRouteSearch(m_search);
            break;

        case 1:
            RemoteDriveRouteQuery driveQuery = new RemoteDriveRouteQuery(
                    fromAndTo, RemoteRouteSearch.DRIVING_DEFAULT, null, null,
                    "");
            m_search.setDriveRouteQuery(driveQuery);
            m_service.requestDriveRouteSearch(m_search);
            break;

        case 2:
            RemoteWalkRouteQuery walkQuery = new RemoteWalkRouteQuery(
                    fromAndTo, RemoteRouteSearch.WALK_DEFAULT);
            m_search.setWalkRouteQuery(walkQuery);
            m_service.requestWalkRouteSearch(m_search);
            break;
        }
    }

    private void startPointSearchResult() {
        showDialog();
        m_startPoiQuery = new RemotePoiQuery(m_start, "", "0755");
        m_startPoiQuery.setPageNum(0);
        m_startPoiQuery.setPageSize(20);
        RemotePoiSearch poiSearch = new RemotePoiSearch(m_startPoiQuery);
        poiSearch.setPoiSearchListener(m_poiSearchListener);
        m_service.requestPoiSearch(poiSearch);
    }

    private void endPointSearchResult() {
        showDialog();
        m_endPoiQuery = new RemotePoiQuery(m_end, "", "0755");
        m_endPoiQuery.setPageNum(0);
        m_endPoiQuery.setPageSize(20);
        RemotePoiSearch poiSearch = new RemotePoiSearch(m_endPoiQuery);
        poiSearch.setPoiSearchListener(m_poiSearchListener);
        m_service.requestPoiSearch(poiSearch);
    }

    private void searchRoute() {
        m_start = m_startEdit.getText().toString().trim();
        m_end = m_endEdit.getText().toString().trim();
        if (m_start == null || m_start.isEmpty()) {
            ToastUtil.show(RouteActivity.this, "请选择起点");
            return;
        }

        if (m_end == null || m_end.isEmpty()) {
            ToastUtil.show(RouteActivity.this, "请选择终点");
            return;
        }

        if (m_start.equals(m_end)) {
            ToastUtil.show(RouteActivity.this, "起点与终点距离很近，您可以步行前往");
            return;
        }

        startPointSearchResult();
    }

    private void showBusRouteSearchResult(RemoteBusRouteResult result) {
        String infomation = "搜索结果\n\n";

        infomation += "打车费用: " + result.getTaxiCost() + "元\n";
        infomation += "公交费用: " + result.getPaths().get(0).getCost() + "元\n";
        infomation += "总距离: " + result.getPaths().get(0).getDistance() + "米\n";
        infomation += "乘车距离: " + result.getPaths().get(0).getBusDistance()
                + "米\n";
        infomation += "步行距离: " + result.getPaths().get(0).getWalkDistance()
                + "米\n";
        infomation += "预计耗时: " + result.getPaths().get(0).getDuration() / 60
                + "分钟\n";

        ToastUtil.show(RouteActivity.this, infomation);
    }

    private void showDriveRouteSearchResult(RemoteDriveRouteResult result) {
        String infomation = "搜索结果\n\n";

        infomation += "总距离: " + result.getPaths().get(0).getDistance() + "米\n";
        infomation += "预计耗时: " + result.getPaths().get(0).getDuration() / 60
                + "分钟\n";
        infomation += "收费道路长度: " + result.getPaths().get(0).getTollDistance()
                + "米\n";
        infomation += "过路费用: " + result.getPaths().get(0).getTolls() + "元\n";

        ToastUtil.show(RouteActivity.this, infomation);
    }

    private void showWalkRouteSearchResult(RemoteWalkRouteResult result) {
        String infomation = "搜索结果\n\n";

        infomation += "总距离: " + result.getPaths().get(0).getDistance() + "米\n";
        infomation += "预计耗时: " + result.getPaths().get(0).getDuration() / 60
                + "分钟\n";

        ToastUtil.show(RouteActivity.this, infomation);
    }

    private RemoteStatusListener m_remoteStatusListener = new RemoteStatusListener() {

        @Override
        public void onAvailable(boolean available) {
            m_available = available;
            IwdsLog.d(this, "Remote available: " + available);
            if (m_available) {
                ToastUtil.show(RouteActivity.this, R.string.remote_available);
            } else {
                ToastUtil.show(RouteActivity.this, R.string.remote_unavailable);
            }
        }
    };

    private OnClickListener m_buttonListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.busButton:
                m_routeType = 0;
                break;
            case R.id.driveButton:
                m_routeType = 1;
                break;
            case R.id.walkButton:
                m_routeType = 2;
                break;
            case R.id.searchButton:
                searchRoute();
                break;
            default:
                break;
            }
        }
    };

    private RemotePoiSearchListener m_poiSearchListener = new RemotePoiSearchListener() {

        @Override
        public void onPoiSearched(RemotePoiResult result, int errorCode) {
            dismissDialog();
            if (errorCode == 0) {
                if (result != null && result.getQuery() != null
                        && result.getPois() != null
                        && result.getPois().size() > 0) {

                    if (result.getQuery().equals(m_startPoiQuery)) {
                        List<RemotePoiItem> poiItems = result.getPois();
                        RouteSearchPoiDialog dialog = new RouteSearchPoiDialog(
                                RouteActivity.this, poiItems);
                        dialog.setTitle("您要找的起点是:");
                        dialog.show();
                        dialog.setOnListClickListener(new OnListItemClick() {
                            @Override
                            public void onListItemClick(
                                    RouteSearchPoiDialog dialog,
                                    RemotePoiItem startpoiItem) {
                                m_startPoint = startpoiItem.getLatLonPoint();
                                m_start = startpoiItem.getTitle();
                                m_startEdit.setText(m_start);
                                endPointSearchResult();// 开始搜终点
                            }
                        });

                    } else if (result.getQuery().equals(m_endPoiQuery)) {
                        List<RemotePoiItem> poiItems = result.getPois();// 取得poiitem数据
                        RouteSearchPoiDialog dialog = new RouteSearchPoiDialog(
                                RouteActivity.this, poiItems);
                        dialog.setTitle("您要找的终点是:");
                        dialog.show();
                        dialog.setOnListClickListener(new OnListItemClick() {
                            @Override
                            public void onListItemClick(
                                    RouteSearchPoiDialog dialog,
                                    RemotePoiItem endpoiItem) {
                                m_endPoint = endpoiItem.getLatLonPoint();
                                m_end = endpoiItem.getTitle();
                                m_endEdit.setText(m_end);
                                searchRouteResult(m_startPoint, m_endPoint);// 进行路径规划搜索
                            }
                        });
                    }

                } else {
                    ToastUtil.show(RouteActivity.this, R.string.no_result);
                }

            } else {
                ToastUtil.show(RouteActivity.this, "搜索失败："
                        + RemoteLocationErrorCode.errorCodeToString(errorCode));
            }
        }

        @Override
        public void onPoiItemDetailSearched(RemotePoiItemDetail poiItemDetail,
                int errorCode) {

        }
    };

    private RemoteRouteSearchListener m_routeSearchListener = new RemoteRouteSearchListener() {

        @Override
        public void onBusRouteSearched(RemoteBusRouteResult result,
                int errorCode) {
            IwdsLog.d(this, "onBusRouteSearched");
            dismissDialog();
            if (errorCode == 0) {
                if (result != null && result.getPaths() != null
                        && result.getPaths().size() > 0) {
                    showBusRouteSearchResult(result);

                } else {
                    ToastUtil.show(RouteActivity.this, R.string.no_result);
                }

            } else {
                ToastUtil.show(RouteActivity.this, "搜索失败："
                        + RemoteLocationErrorCode.errorCodeToString(errorCode));
            }
        }

        @Override
        public void onDriveRouteSearched(RemoteDriveRouteResult result,
                int errorCode) {
            IwdsLog.d(this, "onDriveRouteSearched");
            dismissDialog();
            if (errorCode == 0) {
                if (result != null && result.getPaths() != null
                        && result.getPaths().size() > 0) {
                    showDriveRouteSearchResult(result);

                } else {
                    ToastUtil.show(RouteActivity.this, R.string.no_result);
                }

            } else {
                ToastUtil.show(RouteActivity.this, "搜索失败："
                        + RemoteLocationErrorCode.errorCodeToString(errorCode));
            }

        }

        @Override
        public void onWalkRouteSearched(RemoteWalkRouteResult result,
                int errorCode) {
            IwdsLog.d(this, "onWalkRouteSearched");
            dismissDialog();
            if (errorCode == 0) {
                if (result != null && result.getPaths() != null
                        && result.getPaths().size() > 0) {
                    showWalkRouteSearchResult(result);

                } else {
                    ToastUtil.show(RouteActivity.this, R.string.no_result);
                }

            } else {
                ToastUtil.show(RouteActivity.this, "搜索失败："
                        + RemoteLocationErrorCode.errorCodeToString(errorCode));
            }

        }

    };
}
