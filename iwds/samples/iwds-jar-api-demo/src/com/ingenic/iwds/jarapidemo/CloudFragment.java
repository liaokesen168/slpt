package com.ingenic.iwds.jarapidemo;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.content.Context;
import android.content.Intent;

import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.cloud.CloudServiceManager;
import com.ingenic.iwds.cloud.LoginListener;
import com.ingenic.iwds.cloud.DataInfoListener;
import com.ingenic.iwds.cloud.DataInsertListener;
import com.ingenic.iwds.cloud.CloudDataValues;
import com.ingenic.iwds.cloud.CloudQuery;
import com.ingenic.iwds.utils.IwdsLog;

/**
 * 演示用 CloudServiceManager 实现云数据库的登录、数据插入、查询等操作。
 * 
 * 1. 用 init 初始化，指定操作数据库的 APP_ID 和 PRODUCT_KEY。
 * 2. 用 login 或 loginAnonymous 登录，登录成功才能操作数据。
 * 3. 用 insertData 插入数据。
 * 4. 用 queryData 查询数据，查询条件目前只支持时间戳属性"ts"，后续版本会改进。
 * 5. updateData deleteData 目前版本虽然提供该api，但后台不支持。
 * 
 * 注意：这个应用目前只能在手表端运行
 * 
 */
public class CloudFragment extends DemoFragment implements
        ServiceClient.ConnectionCallbacks {
    private Context mContext;
    private View mContentView;

    private ServiceClient mClient;
    private CloudServiceManager mCloudService;

    private Button mLoginAnonymousButton;
    private Button mLoginNormalButton;
    private Button mInsertButton;
    private Button mQueryButton;
    private Button mRegisterButton;
    private TextView mUserNameEdit;
    private TextView mPasswordEdit;
    private View mLoginView;

    /**
     * APP_ID 和 PRODUCT_KEY 创建机智云数据库时得到。
     */
    private final static String APP_ID = "9ffa8ee67b134d74a9c6dd09e730b74c";
    private final static String PRODUCT_KEY = "a5a19121f0ab4877aed2c923c98a4829";

    private OnClickListener mListener = new OnClickListener() {
        @Override
        public void onClick(android.view.View view) {
            switch (view.getId()) {
            case R.id.login_anonymous:
                loginAnonymous();
                break;

            case R.id.login_button:
                normalLogin();
                break;

            case R.id.register_button:
                startActivity(new Intent(mContext, RegisterActivity.class));
                break;

            case R.id.insert_button:
                insertData();
                break;

            case R.id.query_button:
                queryData();
                break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mContext = getActivity();
        mContentView = inflater.inflate(R.layout.cloud, container, false);

        buildView();

        mClient = new ServiceClient(mContext, ServiceManagerContext.SERVICE_CLOUD, this);

        return mContentView;
    }

    private void buildView() {
        mLoginAnonymousButton = (Button) mContentView.findViewById(R.id.login_anonymous);
        mRegisterButton = (Button) mContentView.findViewById(R.id.register_button);

        mInsertButton = (Button) mContentView.findViewById(R.id.insert_button);
        mQueryButton = (Button) mContentView.findViewById(R.id.query_button);

        mUserNameEdit = (TextView) mContentView.findViewById(R.id.user_name_edit);
        mPasswordEdit = (TextView) mContentView.findViewById(R.id.password_edit);
        mLoginNormalButton = (Button) mContentView.findViewById(R.id.login_button);

        mLoginView = (View) mContentView.findViewById(R.id.login_layout);

        mLoginAnonymousButton.setOnClickListener(mListener);
        mLoginNormalButton.setOnClickListener(mListener);
        mRegisterButton.setOnClickListener(mListener);
        mInsertButton.setOnClickListener(mListener);
        mQueryButton.setOnClickListener(mListener);

        mInsertButton.setEnabled(false);
        mQueryButton.setEnabled(false);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        mClient.disconnect();
    }

    @Override
    public void onConnected(ServiceClient serviceClient) {
        mCloudService = (CloudServiceManager) mClient.getServiceManagerContext();

        /*
         * 初始化，指定 APP_ID 和 PRODUCT_KEY
         */
        mCloudService.init(APP_ID, PRODUCT_KEY);
    }

    @Override
    public void onDisconnected(ServiceClient serviceClient, boolean unexpected) {
    }

    @Override
    public void onConnectFailed(ServiceClient serviceClient, ConnectFailedReason reason) {
        IwdsLog.d(this, "Cloud service connect failed, reason = " + reason.toString());
    }

    /**
     * 匿名登录
     */
    private void loginAnonymous() {
        IwdsLog.d(this, "loginAnonymous");

        mCloudService.loginAnonymous(new LoginListener() {
            /**
             * 登录成功
             */
            @Override
            public void onSuccess() {
                IwdsLog.d(this, "Login success");

                Toast.makeText(mContext, "Login success", Toast.LENGTH_SHORT).show();

                mInsertButton.setEnabled(true);
                mQueryButton.setEnabled(true);

                mLoginAnonymousButton.setEnabled(false);
                mLoginNormalButton.setEnabled(false);

                mLoginView.setVisibility(View.GONE);
            }

            /**
             * 登录失败
             */
            @Override
            public void onFailure(int errCode, String errMsg) {
                IwdsLog.d(this, "Login failure: " + errMsg);

                Toast.makeText(mContext, "Login failure: " + errMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 实名登录
     */
    private void normalLogin() {
        String userName = mUserNameEdit.getText().toString();
        String password = mPasswordEdit.getText().toString();

        if (userName.isEmpty()) {
            Toast.makeText(mContext, "User name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(mContext, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mCloudService.login(userName, password,
            new LoginListener() {
                /**
                 * 登录成功
                 */
                @Override
                public void onSuccess() {
                    IwdsLog.d(this, "Login success");

                    Toast.makeText(mContext, "Login success", Toast.LENGTH_SHORT).show();

                    mInsertButton.setEnabled(true);
                    mQueryButton.setEnabled(true);

                    mLoginAnonymousButton.setEnabled(false);
                    mLoginNormalButton.setEnabled(false);

                    mLoginView.setVisibility(View.GONE);
                }

                /**
                 * 登录失败
                 */
                @Override
                public void onFailure(int errCode, String errMsg) {
                    IwdsLog.d(this, "login failure: " + errMsg);
                    Toast.makeText(mContext, "login failure: " + errMsg, Toast.LENGTH_SHORT).show();
                }
            });
    }

    /**
     * 插入数据
     */
    private void insertData() {
        List<CloudDataValues> dataList = new ArrayList<CloudDataValues>();
        CloudDataValues data;

        data = new CloudDataValues();
        data.put("test1", 11);
        data.put("test2", 11);
        data.put("test3", 123);
        data.put("test4", "hello");

        /*
         * ts 是时间戳属性，其值为时间毫秒数，如果没有指定时间戳，则该值默认为当前时间。
         */
        data.put(CloudServiceManager.ATTR_TIMESTAMP,
                DateMilliSeconds(2015, 1, 1));
        dataList.add(data);

        data = new CloudDataValues();
        data.put("test1", 11);
        data.put("test2", 11);
        data.put("test3", 123);
        data.put("test4", "hello");
        data.put(CloudServiceManager.ATTR_TIMESTAMP,
                DateMilliSeconds(2015, 1, 2));
        dataList.add(data);

        mCloudService.insertData(dataList,
            new DataInsertListener() {
                /**
                 * 数据插入成功
                 */
                @Override
                public void onSuccess() {
                    IwdsLog.d(this, "insertData success");
                }

                /**
                 * 数据插入失败
                 */
                @Override
                public void onFailure(int errCode, String errMsg) {
                    IwdsLog.d(this, "insertData failure: " + errMsg);
                }
            });
    }

    /**
     * 查询数据
     */
    private void queryData() {
        IwdsLog.d(this, "testQueryData");

        long t1 = DateMilliSeconds(2015, 1, 1);
        long t2 = DateMilliSeconds(2017, 5, 1);

        CloudQuery query = new CloudQuery(
                CloudServiceManager.ATTR_TIMESTAMP, ">=", t1).and(
                        new CloudQuery(CloudServiceManager.ATTR_TIMESTAMP, "<=", t2));

        mCloudService.queryData(query, 50, 0,
            new DataInfoListener() {

                /**
                 * 数据查询成功，list 为查询结果。
                 */
                @Override
                public void onSuccess(List<CloudDataValues> list) {
                    IwdsLog.d(this, "queryData success, list=" + list.toString());
                }

                /**
                 * 数据查询失败
                 */
                @Override
                public void onFailure(int errCode, String errMsg) {
                    IwdsLog.d(this, "queryData failure: " + errMsg);
                }
            });
    }

    /**
     * 获取某日期的毫秒数
     * @param  year  年
     * @param  month 月
     * @param  day   日
     * @return       指定年月日的毫秒数
     */
    @SuppressWarnings("deprecation")
    public static long DateMilliSeconds(int year, int month, int day) {
        return new Date(year - 1900, month-1, day, 0, 0, 0).getTime();
    };

}
