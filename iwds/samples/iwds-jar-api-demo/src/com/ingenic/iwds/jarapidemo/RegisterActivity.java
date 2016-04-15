package com.ingenic.iwds.jarapidemo;

import java.util.regex.Pattern;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;

import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.cloud.CloudServiceManager;
import com.ingenic.iwds.cloud.AccountListener;
import com.ingenic.iwds.utils.IwdsLog;

public class RegisterActivity extends Activity implements
        ServiceClient.ConnectionCallbacks, OnClickListener {

    private final static String APP_ID = "9ffa8ee67b134d74a9c6dd09e730b74c";
    private final static String PRODUCT_KEY = "a5a19121f0ab4877aed2c923c98a4829";
    
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);

        initView();

        mClient = new ServiceClient(this, ServiceManagerContext.SERVICE_CLOUD, this);
    }

    private void initView() {

        TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
        tabHost.setup();

        tabHost.addTab(tabHost.newTabSpec("tab1").
                setIndicator(getString(R.string.normal_register_label), null).setContent(R.id.tab1));  

        tabHost.addTab(tabHost.newTabSpec("tab2").
                setIndicator(getString(R.string.phone_register_label), null).setContent(R.id.tab2)); 
        
        tabHost.addTab(tabHost.newTabSpec("tab3").
                setIndicator(getString(R.string.mail_register_label), null).setContent(R.id.tab3)); 

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
        mCloudService = (CloudServiceManager) mClient.getServiceManagerContext();
        mCloudService.init(APP_ID, PRODUCT_KEY);
    }

    @Override
    public void onDisconnected(ServiceClient serviceClient, boolean unexpected) {
    }

    @Override
    public void onConnectFailed(ServiceClient serviceClient, ConnectFailedReason reason) {
    }

    /*
     * 普通注册
     */
    private void normalRegister() {
        String userName = mUserNameEdit.getText().toString();
        String password = mNormalPasswordEdit.getText().toString();

        if (userName.isEmpty()) {
            Toast.makeText(this, "User name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mCloudService.registerUser(userName, password,
            new AccountListener() {
                @Override
                public void onSuccess() {
                    IwdsLog.d(this, "registerUser success");
                    Toast.makeText(RegisterActivity.this, "register success", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int errCode, String errMsg) {
                    IwdsLog.d(this, "registerUser failure: " + errMsg);
                    Toast.makeText(RegisterActivity.this, "register failure: " + errMsg, Toast.LENGTH_SHORT).show();
                }
            });
    }

    /*
     * 电话号码注册
     */
    private void phoneRegister() {
        String phone = mPhoneNumberEdit.getText().toString();
        String password = mPhonePasswordEdit.getText().toString();
        String verifyCode = mVerificationCodeEdit.getText().toString();

        if (!isPhoneNumber(phone)) {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (verifyCode.isEmpty()) {
            Toast.makeText(this, "Verify code cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mCloudService.registerUserWithPhone(phone, password, verifyCode,
            new AccountListener() {
                @Override
                public void onSuccess() {
                    IwdsLog.d(this, "registerUserByPhone success");
                    Toast.makeText(RegisterActivity.this, "register success", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int errCode, String errMsg) {
                    IwdsLog.d(this, "registerUserByPhone failure: " + errMsg);
                    Toast.makeText(RegisterActivity.this, "register failure: " + errMsg, Toast.LENGTH_SHORT).show();
                }
            });
    }

    /*
     * 邮箱注册
     */
    private void mailRegister() {
        String email = mMailEdit.getText().toString();
        String password = mMailPasswordEdit.getText().toString();

        if (email.isEmpty()) {
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mCloudService.registerUserWithEmail(email, password,
            new AccountListener() {
                @Override
                public void onSuccess() {
                    IwdsLog.d(this, "registerUserByEmail success");
                    Toast.makeText(RegisterActivity.this, "register success", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int errCode, String errMsg) {
                    IwdsLog.d(this, "registerUserByEmail failure: " + errMsg);
                    Toast.makeText(RegisterActivity.this, "register failure: " + errMsg, Toast.LENGTH_SHORT).show();
                }
            });
    }

    /**
     * 请求校验码
     */
    private void requestVerification() {
        String phone = mPhoneNumberEdit.getText().toString();
        if (!isPhoneNumber(phone)) {
            Toast.makeText(RegisterActivity.this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        mCloudService.requestPhoneVerifyCode(phone,
            new AccountListener() {
                @Override
                public void onSuccess() {
                    IwdsLog.d(this, "requestPhoneVerifyCode success");
                    Toast.makeText(RegisterActivity.this, "register success", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int errCode, String errMsg) {
                    IwdsLog.d(this, "requestPhoneVerifyCode failure: " + errMsg);
                    Toast.makeText(RegisterActivity.this, "register failure: " + errMsg, Toast.LENGTH_SHORT).show();
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
            requestVerification();
            break;
        }
    }

    /**
     * 判断表达式是否为手机号码
     * @param  phone 手机号码字符串
     * @return     
     */
    public static boolean isPhoneNumber(String phone) {
        Pattern pattern = Pattern.compile("1[0-9]{10}");
        return ((phone != null) && pattern.matcher(phone).matches());
    }
}
