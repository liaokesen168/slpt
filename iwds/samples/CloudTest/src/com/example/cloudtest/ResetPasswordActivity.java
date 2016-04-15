/**
 * 
 */
package com.example.cloudtest;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;

import com.ingenic.iwds.utils.IwdsLog;
import com.ingenic.iwds.cloud.AccountListener;
import com.ingenic.iwds.cloud.CloudServiceManager;
import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceManagerContext;

public class ResetPasswordActivity extends Activity implements
        ServiceClient.ConnectionCallbacks, OnClickListener {
    private ServiceClient mClient;
    private CloudServiceManager mCloudService;

    private Button mPhoneResetButton;
    private Button mMailResetButton;
    private Button mRequestVerificationButton;
    
    private EditText mPhonePasswordEdit;
    //private EditText mMailPasswordEdit;
    private EditText mPhoneNumberEdit;
    private EditText mMailEdit;
    private EditText mVerificationCodeEdit;

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_reset_password);

        mSharedPreferences = getSharedPreferences("data", 0);

        initView();

        mClient = new ServiceClient(this, ServiceManagerContext.SERVICE_CLOUD, this);
    }

    private void initView() {
        TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
        tabHost.setup();

        tabHost.addTab(tabHost.newTabSpec("tab1")
                .setIndicator(getString(R.string.phone_register_label), null)
                .setContent(R.id.tab1));

        tabHost.addTab(tabHost.newTabSpec("tab2")
                .setIndicator(getString(R.string.mail_register_label), null)
                .setContent(R.id.tab2));

        mPhoneResetButton = (Button) findViewById(R.id.phone_reset);
        mMailResetButton = (Button) findViewById(R.id.mail_reset);
        mRequestVerificationButton = (Button) findViewById(R.id.request_verification);

        mPhonePasswordEdit = (EditText) findViewById(R.id.phone_password);
        //mMailPasswordEdit = (EditText) findViewById(R.id.mail_password);
        mPhoneNumberEdit = (EditText) findViewById(R.id.phone_number);
        mVerificationCodeEdit = (EditText) findViewById(R.id.verification_code);
        mMailEdit = (EditText) findViewById(R.id.mail);

        mPhoneResetButton.setOnClickListener(this);
        mMailResetButton.setOnClickListener(this);
        mRequestVerificationButton.setOnClickListener(this);

        String phone = mSharedPreferences.getString("PhoneNumber", "");
        mPhoneNumberEdit.setText(phone);

        String mail = mSharedPreferences.getString("mail", "");
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
        IwdsLog.d(this, "Cloud service connect failed");
    }

    /*
     * 
     */
    private void resetPasswordByPhone() {
        String phone = mPhoneNumberEdit.getText().toString();
        String newPassword = mPhonePasswordEdit.getText().toString();
        String verifyCode = mVerificationCodeEdit.getText().toString();

        if (!Utils.isPhoneNumber(phone)) {
            Utils.showToast(this, "请输入正确的手机号", mPhoneNumberEdit);
            Utils.setFocus(mPhoneNumberEdit);
            return;
        }
        
        if (newPassword.isEmpty()) {
            Utils.showToast(this, "密码不能为空", mPhonePasswordEdit);
            return;
        }

        if (verifyCode.isEmpty()) {
            Utils.showToast(this, "校验码不能为空", mVerificationCodeEdit);
            return;
        }

        mCloudService.resetPasswordWithPhone(phone, verifyCode, newPassword,
                new AccountListener() {
                    @Override
                    public void onSuccess() {
                        IwdsLog.d(this, "registerUserByPhone success");
                        Utils.showToast(ResetPasswordActivity.this, "重置密码成功");
                    }

                    @Override
                    public void onFailure(int errCode, String errMsg) {
                        IwdsLog.d(this, "registerUserByPhone failure: "
                                + errMsg);
                        Utils.showToast(ResetPasswordActivity.this, "重置密码失败: "
                                + errMsg);
                    }
                });
    }

    /*
     * 
     */
    private void resetPasswordByMail() {
        String email = mMailEdit.getText().toString();
        //String password = mMailPasswordEdit.getText().toString();

        if (email.isEmpty()) {
            Utils.showToast(this, "邮箱不能为空", mMailEdit);
            return;
        }

        mCloudService.resetPasswordWithEmail(email,
                new AccountListener() {
                    @Override
                    public void onSuccess() {
                        IwdsLog.d(this, "registerUserByEmail success");
                        Utils.showToast(ResetPasswordActivity.this, "重置密码成功");
                    }

                    @Override
                    public void onFailure(int errCode, String errMsg) {
                        IwdsLog.d(this, "registerUserByEmail failure: " + errMsg);
                        Utils.showToast(ResetPasswordActivity.this, "重置密码失败: " + errMsg);
                    }
                });
    }

    public void onClick(android.view.View view) {
        switch (view.getId()) {
        case R.id.phone_reset:
            resetPasswordByPhone();
            break;
        case R.id.mail_reset:
            resetPasswordByMail();
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
                            Utils.showToast(ResetPasswordActivity.this, "发送成功");
                        }

                        @Override
                        public void onFailure(int errCode, String errMsg) {
                            IwdsLog.d(this, "requestPhoneVerifyCode failure: " + errMsg);
                            Utils.showToast(ResetPasswordActivity.this, "发送失败: " + errMsg);
                        }
                    });
            }

            break;
        }
        
    }
}












