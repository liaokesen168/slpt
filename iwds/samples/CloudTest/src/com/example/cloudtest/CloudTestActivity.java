package com.example.cloudtest;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;

import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.cloud.CloudServiceManager;
import com.ingenic.iwds.cloud.LoginListener;
import com.ingenic.iwds.cloud.AccountListener;
import com.ingenic.iwds.cloud.DataInfoListener;
import com.ingenic.iwds.cloud.DataInsertListener;
import com.ingenic.iwds.cloud.DataOperationListener;
import com.ingenic.iwds.cloud.CloudDataValues;
import com.ingenic.iwds.cloud.CloudQuery;
import com.ingenic.iwds.utils.IwdsLog;

public class CloudTestActivity extends Activity implements
        ServiceClient.ConnectionCallbacks, OnClickListener {
    private ServiceClient mClient;
    private CloudServiceManager mCloudService;
    
    private Button mLoginAnonymousButton;
    private Button mLoginNormalButton;
    private Button mInsertButton;
    private Button mQueryButton;
    private Button mUpdateButton;
    private Button mDeleteButton;
    private Button mChangePwdButton;
    
    private TextView mLogText;
    private TextView mUserNameEdit;
    private TextView mPasswordEdit;
    private View mLoginView;
    
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        
        mLoginAnonymousButton = (Button) findViewById(R.id.login_anonymous);
        mInsertButton = (Button) findViewById(R.id.insert_button);
        mQueryButton = (Button) findViewById(R.id.query_button);
        mUpdateButton = (Button) findViewById(R.id.update_button);
        mDeleteButton = (Button) findViewById(R.id.delete_button);
        mUserNameEdit = (TextView) findViewById(R.id.user_name_edit);
        mPasswordEdit = (TextView) findViewById(R.id.password_edit);
        mLoginNormalButton = (Button) findViewById(R.id.login_button);
        mLoginView = (View) findViewById(R.id.login_layout);

        mLoginAnonymousButton.setOnClickListener(this);
        mLoginNormalButton.setOnClickListener(this);
        mInsertButton.setOnClickListener(this);
        mQueryButton.setOnClickListener(this);
        mUpdateButton.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);

        mInsertButton.setEnabled(false);
        mQueryButton.setEnabled(false);
        mUpdateButton.setEnabled(false);
        mDeleteButton.setEnabled(false);

        mLogText = (TextView) findViewById(R.id.log_text);

        mSharedPreferences = getSharedPreferences("data", 0);
        String username = mSharedPreferences.getString("username", "");
        mUserNameEdit.setText(username);

        mClient = new ServiceClient(getApplicationContext(), ServiceManagerContext.SERVICE_CLOUD, this);

        mChangePwdButton = (Button) findViewById(R.id.change_pwd);
        mChangePwdButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_register) {
            startActivity(new Intent(this, RegisterActivity.class));
            return true;
        }
        if (id == R.id.action_reset_password) {
            startActivity(new Intent(this, ResetPasswordActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    private void showLog(String log) {
        mLogText.append("\n--------------------\n");
        mLogText.append(log);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mClient.disconnect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString("username", mUserNameEdit.getText().toString());
        mEditor.commit();
    }

    @Override
    public void onConnected(ServiceClient serviceClient) {
        IwdsLog.d(this, "Cloud service connected");
        
        mCloudService = (CloudServiceManager) mClient.getServiceManagerContext();
        mCloudService.init(Constans.APP_ID, Constans.PRODUCT_KEY);
    }

    @Override
    public void onDisconnected(ServiceClient serviceClient, boolean unexpected) {
        IwdsLog.d(this, "Cloud service diconnected");
    }

    @Override
    public void onConnectFailed(ServiceClient serviceClient, ConnectFailedReason reason) {
        IwdsLog.d(this, "Cloud service connect failed, reason = " + reason.toString());
    }

    private void loginAnonymous() {
        IwdsLog.d(this, "loginAnonymous");
        
        mCloudService.loginAnonymous(new LoginListener() {
            @Override
            public void onSuccess() {
                IwdsLog.d(this, "Login success");
                showLog("Login success");
                Utils.showToast(CloudTestActivity.this, "登录成功", mLoginAnonymousButton);

                mInsertButton.setEnabled(true);
                mQueryButton.setEnabled(true);
                mUpdateButton.setEnabled(true);
                mDeleteButton.setEnabled(true);

                mLoginView.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int errCode, String errMsg) {
                IwdsLog.d(this, "Login failure: " + errMsg);
                showLog("Login failure: " + errMsg);
                Utils.showToast(CloudTestActivity.this, "登录失败: " + errMsg, mLoginAnonymousButton);
            }
        });
    }

    private void normalLogin() {
        String userName = mUserNameEdit.getText().toString();
        String password = mPasswordEdit.getText().toString();

        if (userName.isEmpty()) {
            Utils.showToast(this, "用户名不能为空", mUserNameEdit);
            return;
        }

        if (password.isEmpty()) {
            Utils.showToast(this, "密码不能为空", mPasswordEdit);
            return;
        }

        mCloudService.login(userName, password,
                new LoginListener() {
                    @Override
                    public void onSuccess() {
                        IwdsLog.d(this, "Login success");
                        showLog("Login success");

                        Utils.showToast(CloudTestActivity.this, "登录成功", mLoginNormalButton);

                        mInsertButton.setEnabled(true);
                        mQueryButton.setEnabled(true);
                        mUpdateButton.setEnabled(true);
                        mDeleteButton.setEnabled(true);

                        mLoginView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(int errCode, String errMsg) {
                        IwdsLog.d(this, "login failure: " + errMsg);
                        Utils.showToast(CloudTestActivity.this, "登录失败: " + errMsg, mLoginNormalButton);
                    }
                });
    }

    private void testInsertData() {
        IwdsLog.d(this, "insertData");

        List<CloudDataValues> dataList = new ArrayList<CloudDataValues>();
        CloudDataValues data;

        data = new CloudDataValues();
        data.put("weight", 150);
        data.put("fat", 71);
        data.put("remark", "超重");
        data.put("suggestion", "少吃");
        data.put(CloudServiceManager.ATTR_TIMESTAMP,
                System.currentTimeMillis());
        dataList.add(data);

        data = new CloudDataValues();
        data.put("weight", 100);
        data.put("fat", 51);
        data.put("remark", "偏瘦");
        data.put("suggestion", "早睡");
        data.put(CloudServiceManager.ATTR_TIMESTAMP,
                System.currentTimeMillis());
        dataList.add(data);

        mCloudService.insertData(dataList,
            new DataInsertListener() {
                @Override
                public void onSuccess() {
                    IwdsLog.d(this, "insertData success");

                    showLog("insertData success: ");
                }

                @Override
                public void onFailure(int errCode, String errMsg) {
                    IwdsLog.d(this, "insertData failure: " + errMsg);

                    showLog("insertData failure: " + errMsg);
                }
            });
    }

    private void testQueryData() {
        IwdsLog.d(this, "testQueryData");

        long t1 = Utils.DateMilliSeconds(2015, 1, 1);
        long t2 = Utils.DateMilliSeconds(2018, 12, 31);

        CloudQuery query = new CloudQuery(
                CloudServiceManager.ATTR_TIMESTAMP, ">=", t1).and(
                        new CloudQuery(CloudServiceManager.ATTR_TIMESTAMP, "<=", t2));

        mCloudService.queryData(query, 50, 0,
                new DataInfoListener() {
                    @Override
                    public void onSuccess(List<CloudDataValues> list) {
                        IwdsLog.d(this, "queryData success, list=" + list.toString());

                        String s = "queryData success:\n";
                        s += "result count=" + list.size() + "\n";

                        Iterator<CloudDataValues> iter = list.iterator();
                        while (iter.hasNext()) {
                            CloudDataValues v = iter.next();
                            s += v.toString();
                        }

                        showLog(s);
                    }
                    @Override
                    public void onFailure(int errCode, String errMsg) {
                        IwdsLog.d(this, "queryData failure: " + errMsg);
                        showLog("queryData failure: " + errMsg);
                    }
                });
    }

    private void testUpdateData() {
        IwdsLog.d(this, "testUpdateData");
        CloudQuery query = new CloudQuery("weight", ">=", 200);

        List<CloudDataValues> dataList = new ArrayList<CloudDataValues>();
        CloudDataValues data = new CloudDataValues();
        data.put("remark", "超重");
        dataList.add(data);
        
        mCloudService.updateData(
                query,
                dataList,
                new DataOperationListener() {
                    @Override
                    public void onSuccess() {
                        IwdsLog.d(this, "updateData success");
                        showLog("updateData success");
                    }

                    @Override
                    public void onFailure(int errCode, String errMsg) {
                        IwdsLog.d(this, "updateData failure: " + errMsg);
                        showLog("updateData failure: " + errMsg);
                    }
                });
    }

    private void testDeleteData() {
        IwdsLog.d(this, "testDeleteData");

        CloudQuery query = 
                new CloudQuery("weight", ">=", 100).and(new CloudQuery("weight", "<=", 200));

        mCloudService.deleteData(query,
                new DataOperationListener() {
                    @Override
                    public void onSuccess() {
                        IwdsLog.d(this, "deleteData success");
                        showLog("deleteData success");
                    }

                    @Override
                    public void onFailure(int errCode, String errMsg) {
                        IwdsLog.d(this, "deleteData failure: " + errMsg);
                        showLog("deleteData failure: " + errMsg);
                    }
                });
    }

    private void changePwd() {
        String userName = mUserNameEdit.getText().toString();
        String oldPassword = mPasswordEdit.getText().toString();

        if (userName.isEmpty()) {
            Utils.showToast(this, "用户名不能为空", mUserNameEdit);
            return;
        }

        String newPassword = "123456";
        mCloudService.changeUserPassword(oldPassword, newPassword,
                new AccountListener() {
                    @Override
                    public void onSuccess() {
                        IwdsLog.d(this, "changeUserPassword success");
                        showLog("changeUserPassword success");
                        Utils.showToast(CloudTestActivity.this, "修改密码成功", mLoginNormalButton);
                    }

                    @Override
                    public void onFailure(int errCode, String errMsg) {
                        IwdsLog.d(this, "login failure: " + errMsg);
                        Utils.showToast(CloudTestActivity.this, "修改密码失败: " + errMsg, mLoginNormalButton);
                    }
                });
    }

    public void onClick(android.view.View view) {
        switch (view.getId()) {
        case R.id.change_pwd:
            changePwd();
            break;
        case R.id.login_anonymous:
            if (mCloudService == null) {
                Utils.showToast(this, "云服务连接失败", mLoginAnonymousButton);
            } else {
                loginAnonymous();
            }

            break;

        case R.id.login_button:
            if (mCloudService == null) {
                Utils.showToast(this, "云服务连接失败", mLoginNormalButton);
            } else {
                normalLogin();
            }

            break;
        case R.id.insert_button:
            testInsertData();
            break;
        case R.id.query_button:
            testQueryData();
            break;
        case R.id.update_button:
            testUpdateData();
            break;
        case R.id.delete_button:
            testDeleteData();
            break;
        }
    }
}
