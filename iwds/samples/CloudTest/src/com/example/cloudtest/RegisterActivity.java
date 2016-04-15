package com.example.cloudtest;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.content.SharedPreferences;

import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.cloud.CloudServiceManager;
import com.ingenic.iwds.cloud.AccountListener;
import com.ingenic.iwds.utils.IwdsLog;

public class RegisterActivity extends Activity implements
        ServiceClient.ConnectionCallbacks, OnClickListener {

    private ServiceClient mClient;
    private CloudServiceManager mCloudService;

    private Button mNormalRegisterButton;
    private Button mPhoneRegisterButton;
    private Button mMailRegisterButton;
    private Button mRequestVerificationButton;

    private EditText mUserNameEdit;
    private EditText mNormalPasswordEdit;
    private EditText mPhonePasswordEdit;
    private EditText mMailPasswordEdit;
    private EditText mPhoneNumberEdit;
    private EditText mMailEdit;
    private EditText mVerificationCodeEdit;

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);

        mSharedPreferences = getSharedPreferences("data", 0);

        initView();

        mClient = new ServiceClient(this, ServiceManagerContext.SERVICE_CLOUD, this);
    }

    private void initView() {

        TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
        tabHost.setup();

        tabHost.addTab(tabHost.newTabSpec("tab1")
                .setIndicator(getString(R.string.normal_register_label), null)
                .setContent(R.id.tab1));

        tabHost.addTab(tabHost.newTabSpec("tab2")
                .setIndicator(getString(R.string.phone_register_label), null)
                .setContent(R.id.tab2));

        tabHost.addTab(tabHost.newTabSpec("tab3")
                .setIndicator(getString(R.string.mail_register_label), null)
                .setContent(R.id.tab3));

        mNormalRegisterButton = (Button) findViewById(R.id.normal_register);
        mPhoneRegisterButton = (Button) findViewById(R.id.phone_register);
        mMailRegisterButton = (Button) findViewById(R.id.mail_register);
        mRequestVerificationButton = (Button) findViewById(R.id.request_verification);

        mUserNameEdit = (EditText) findViewById(R.id.user_name);
        mNormalPasswordEdit = (EditText) findViewById(R.id.normal_password);
        mPhonePasswordEdit = (EditText) findViewById(R.id.phone_password);
        mMailPasswordEdit = (EditText) findViewById(R.id.mail_password);
        mPhoneNumberEdit = (EditText) findViewById(R.id.phone_number);
        mVerificationCodeEdit = (EditText) findViewById(R.id.verification_code);
        mMailEdit = (EditText) findViewById(R.id.mail);

        mNormalRegisterButton.setOnClickListener(this);
        mPhoneRegisterButton.setOnClickListener(this);
        mMailRegisterButton.setOnClickListener(this);
        mRequestVerificationButton.setOnClickListener(this);

        String phone = mSharedPreferences.getString("PhoneNumber", "");
        mPhoneNumberEdit.setText(phone);

        String mail = mSharedPreferences.getString("mail", "liancheng.wang@ingenic.com");
        mMailEdit.setText(mail);
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
        mEditor.putString("PhoneNumber", mPhoneNumberEdit.getText().toString());
        mEditor.putString("mail", mMailEdit.getText().toString());
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
    public void onConnectFailed(ServiceClient serviceClient,
            ConnectFailedReason reason) {
        IwdsLog.d(this, "Cloud service connect failed");
    }

    /*
     * 
     */
    private void normalRegister() {
        String userName = mUserNameEdit.getText().toString();
        String password = mNormalPasswordEdit.getText().toString();

        if (userName.isEmpty()) {
            Utils.showToast(this, "用户名不能为空", mUserNameEdit);
            return;
        }

        if (password.isEmpty()) {
            Utils.showToast(this, "密码不能为空", mNormalPasswordEdit);
            return;
        }

        mCloudService.registerUser(userName, password,
                new AccountListener() {
                    @Override
                    public void onSuccess() {
                        IwdsLog.d(this, "registerUser success");
                        Utils.showToast(RegisterActivity.this, "注册成功");
                    }

                    @Override
                    public void onFailure(int errCode, String errMsg) {
                        IwdsLog.d(this, "registerUser failure: " + errMsg);
                        Utils.showToast(RegisterActivity.this, "注册失败: " + errMsg);
                    }
                });
    }

    /*
     * 
     */
    private void phoneRegister() {
        String phone = mPhoneNumberEdit.getText().toString();
        String password = mPhonePasswordEdit.getText().toString();
        String verifyCode = mVerificationCodeEdit.getText().toString();

        if (!Utils.isPhoneNumber(phone)) {
            Utils.showToast(this, "请输入正确的手机号", mPhoneNumberEdit);
            Utils.setFocus(mPhoneNumberEdit);
            return;
        }

        if (password.isEmpty()) {
            Utils.showToast(this, "用户名不能为空", mPhonePasswordEdit);
            return;
        }

        if (verifyCode.isEmpty()) {
            Utils.showToast(this, "校验码不能为空", mVerificationCodeEdit);
            return;
        }

        mCloudService.registerUserWithPhone(phone, password, verifyCode,
                new AccountListener() {
                    @Override
                    public void onSuccess() {
                        IwdsLog.d(this, "registerUserByPhone success");
                        Utils.showToast(RegisterActivity.this, "注册成功");
                    }

                    @Override
                    public void onFailure(int errCode, String errMsg) {
                        IwdsLog.d(this, "registerUserByPhone failure: " + errMsg);
                        Utils.showToast(RegisterActivity.this, "注册失败: " + errMsg);
                    }
                });
    }

    /*
     * 
     */
    private void mailRegister() {
        String email = mMailEdit.getText().toString();
        String password = mMailPasswordEdit.getText().toString();

        if (email.isEmpty()) {
            Utils.showToast(this, "邮箱不能为空", mMailEdit);
            return;
        }

        if (password.isEmpty()) {
            Utils.showToast(this, "用户名不能为空", mMailPasswordEdit);
            return;
        }

        mCloudService.registerUserWithEmail(email, password,
                new AccountListener() {
                    @Override
                    public void onSuccess() {
                        IwdsLog.d(this, "registerUserWithEmail success");
                        Utils.showToast(RegisterActivity.this, "注册成功");
                    }

                    @Override
                    public void onFailure(int errCode, String errMsg) {
                        IwdsLog.d(this, "registerUserWithEmail failure: " + errMsg);
                        Utils.showToast(RegisterActivity.this, "注册失败: " + errMsg);
                    }
                });
    }

    public void onClick(android.view.View view) {
        switch (view.getId()) {
        case R.id.normal_register:
            normalRegister();
            break;
        case R.id.phone_register:
            phoneRegister();
            break;
        case R.id.mail_register:
            mailRegister();
            break;
        case R.id.request_verification:
            String phone = mPhoneNumberEdit.getText().toString();
            if (!Utils.isPhoneNumber(phone)) {
                Utils.showToast(this, "请输入正确的手机号", mPhoneNumberEdit);
                Utils.setFocus(mPhoneNumberEdit);
            } else {
                mCloudService.requestPhoneVerifyCode(phone,
                    new AccountListener() {
                        @Override
                        public void onSuccess() {
                            IwdsLog.d(this, "requestPhoneVerifyCode success");
                            Utils.showToast(RegisterActivity.this, "发送成功");
                        }

                        @Override
                        public void onFailure(int errCode, String errMsg) {
                            IwdsLog.d(this, "requestPhoneVerifyCode failure: " + errMsg);
                            Utils.showToast(RegisterActivity.this, "发送失败: " + errMsg);
                        }
                    });
            }

            break;
        }
        
    }
}
