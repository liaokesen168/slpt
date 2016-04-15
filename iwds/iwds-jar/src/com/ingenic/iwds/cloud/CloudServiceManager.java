/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  WangLianCheng <liancheng.wang@ingenic.com>
 *
 *  Elf/IDWS Project
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation; either version 2 of the License, or (at your
 *  option) any later version.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.ingenic.iwds.cloud;

import java.util.List;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;

import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

/**
 * 云管理者类。
 */
public class CloudServiceManager extends ServiceManagerContext {
    private static final String NOT_INIT = "not initialize, call init function";
    private static final long DATE_MS_3099_12_31 = 35659324799000l; // time millis of 3099/12/31 23:59:59

    private ICloudService mService;
    private ICloudSession mSession;
    private String mAppKey;
    private String mProductKey;
    private boolean mEnable;
    private IServiceStatusCallback mServiceStatusCallback;

    /**
     * 时间戳属性
     */
    public static final String ATTR_TIMESTAMP = "ts";

    /**
     * QQ账号
     */
    public static final int THIRD_ACCOUNT_TYPE_QQ = 1;
    /**
     * 新浪账号
     */
    public static final int THIRD_ACCOUNT_TYPE_SINA = 2;
    /**
     * 百度账号
     */
    public static final int THIRD_ACCOUNT_TYPE_BAIDU = 3;

    /**
     * 构造
     * @param  context 上下文。
     */
    public CloudServiceManager(Context context) {
        super(context);

        m_serviceClientProxy = new ServiceClientProxy() {
            @Override
            public void onServiceConnected(IBinder service) {
                mService = ICloudService.Stub.asInterface(service);
                mEnable = false;
            }

            @Override
            public void onServiceDisconnected(boolean unexpected) {
            }

            @Override
            public IBinder getBinder() {
                return mService.asBinder();
            }
        };

        mServiceStatusCallback = new IServiceStatusCallback.Stub() {
            @Override
            public void onEnableStatusChange(boolean enabled) {
                if (!enabled) {
                    mEnable = false;
                }
            }
        };
    }

    /**
     * 初始化
     * @param appID 指定 App ID（由创建机智云数据库时得到）
     * @param productKey 指定 Product Key（由创建机智云数据库时得到）
     * @return true 初始化成功，false 初始化失败
     */
    public boolean init(String appID, String productKey) {
        IwdsAssert.dieIf(this, appID == null, "appID is null");
        IwdsAssert.dieIf(this, productKey == null, "productKey is null");

        mAppKey = appID;
        mProductKey = productKey;

        try {
            mSession = mService.getSession(mAppKey, mProductKey, mServiceStatusCallback);
            mSession.init();
            mEnable = true;
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            mEnable = false;
            return false;
        }
    }

    /**
     * 注册普通用户
     * @param userName 用户名
     * @param password 密码
     * @param listener 监听器对象
     */
    public void registerUser(String userName, String password, AccountListener listener) {
        IwdsAssert.dieIf(this, userName == null, "userName is null");
        IwdsAssert.dieIf(this, password == null, "password is null");
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if ((!mEnable) || (mSession == null)) {
            listener.failure(-1, NOT_INIT);
            return;
        }

        try {
            mSession.registerUser(userName, password, listener.callback);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in registerUser: " + e.toString());
            listener.failure(-1, e.toString());
        } catch (NullPointerException e) {
            IwdsLog.e(this, "Exception in registerUser: " + e.toString());
            listener.failure(-1, e.toString());
        }
    }

    /**
     * 通过邮箱注册用户
     * @param email    邮箱地址
     * @param password 密码
     * @param listener 监听器对象
     */
    public void registerUserWithEmail(String email, String password, AccountListener listener) {
        IwdsAssert.dieIf(this, email == null, "email is null");
        IwdsAssert.dieIf(this, password == null, "password is null");
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if ((!mEnable) || (mSession == null)) {
            listener.failure(-1, NOT_INIT);
            return;
        }

        try {
            mSession.registerUserWithEmail(email, password, listener.callback);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in registerUserWithEmail: " + e.toString());
            listener.failure(-1, e.toString());
        } catch (NullPointerException e) {
            IwdsLog.e(this, "Exception in registerUserWithEmail: " + e.toString());
            listener.failure(-1, e.toString());
        }
    }

    /**
     * 通过手机注册用户
     * @param phone      手机号码
     * @param password   密码
     * @param verifyCode 手机校验码。校验码通过 {@link #requestPhoneVerifyCode requestPhoneVerifyCode} 获取。
     * @param listener   监听器对象
     */
    public void registerUserWithPhone(String phone, String password, String verifyCode, AccountListener listener) {
        IwdsAssert.dieIf(this, phone == null, "phone is null");
        IwdsAssert.dieIf(this, password == null, "password is null");
        IwdsAssert.dieIf(this, verifyCode == null, "verifyCode is null");
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if ((!mEnable) || (mSession == null)) {
            listener.failure(-1, NOT_INIT);
            return;
        }

        try {
            mSession.registerUserWithPhone(phone, password, verifyCode, listener.callback);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in registerUserWithPhone: " + e.toString());
            listener.failure(-1, e.toString());
        } catch (NullPointerException e) {
            IwdsLog.e(this, "Exception in registerUserWithPhone: " + e.toString());
            listener.failure(-1, e.toString());
        }
    }

    /**
     * 获取手机校验码
     * @param phone    手机号码
     * @param listener 监听器对象
     */
    public void requestPhoneVerifyCode(String phone, AccountListener listener) {
        IwdsAssert.dieIf(this, phone == null, "phone is null");
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if ((!mEnable) || (mSession == null)) {
            listener.failure(-1, NOT_INIT);
            return;
        }

        try {
            mSession.requestPhoneVerifyCode(phone, listener.callback);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in requestPhoneVerifyCode: " + e.toString());
            listener.failure(-1, e.toString());
        } catch (NullPointerException e) {
            IwdsLog.e(this, "Exception in requestPhoneVerifyCode: " + e.toString());
            listener.failure(-1, e.toString());
        }
    }

    /**
     * 通过邮件重置密码
     * @param email    邮箱地址
     * @param listener 监听器对象
     */
    public void resetPasswordWithEmail(String email, AccountListener listener) {
        IwdsAssert.dieIf(this, email == null, "email is null");
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if ((!mEnable) || (mSession == null)) {
            listener.failure(-1, NOT_INIT);
            return;
        }

        try {
            mSession.resetPasswordWithEmail(email, listener.callback);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in resetPasswordWithEmail: " + e.toString());
            listener.failure(-1, e.toString());
        } catch (NullPointerException e) {
            IwdsLog.e(this, "Exception in resetPasswordWithEmail: " + e.toString());
            listener.failure(-1, e.toString());
        }
    }

    /**
     * 通过手机注册重置密码
     * @param phone       手机号
     * @param verifyCode  校验码。校验码通过 {@link #requestPhoneVerifyCode requestPhoneVerifyCode} 获取。
     * @param newPassword 新密码
     * @param listener    监听器对象
     */
    public void resetPasswordWithPhone(String phone, String verifyCode, String newPassword,
            AccountListener listener) {
        IwdsAssert.dieIf(this, phone == null, "phone is null");
        IwdsAssert.dieIf(this, verifyCode == null, "verifyCode is null");
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if ((!mEnable) || (mSession == null)) {
            listener.failure(-1, NOT_INIT);
            return;
        }

        try {
            mSession.resetPasswordWithPhone(phone, verifyCode, newPassword, listener.callback);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in resetPasswordWithPhone: " + e.toString());
            listener.failure(-1, e.toString());
        } catch (NullPointerException e) {
            IwdsLog.e(this, "Exception in resetPasswordWithPhone: " + e.toString());
            listener.failure(-1, e.toString());
        }
    }

    /**
     * 匿名登录
     * @param listener 监听器对象
     */
    public void loginAnonymous(LoginListener listener) {
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if ((!mEnable) || (mSession == null)) {
            listener.failure(-1, NOT_INIT);
            return;
        }

        try {
            mSession.loginAnonymous(listener.callback);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in loginAnonymous: " + e.toString());
            listener.failure(-1, e.toString());
        } catch (NullPointerException e) {
            IwdsLog.e(this, "Exception in loginAnonymous: " + e.toString());
            listener.failure(-1, e.toString());
        }
    }

    /**
     * 实名登录
     * @param userName 用户名
     * @param password 密码
     * @param listener 监听器对象
     */
    public void login(String userName, String password, LoginListener listener) {
        IwdsAssert.dieIf(this, userName == null, "userName is null");
        IwdsAssert.dieIf(this, password == null, "password is null");
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if ((!mEnable) || (mSession == null)) {
            listener.failure(-1, NOT_INIT);
            return;
        }

        try {
            mSession.login(userName, password, listener.callback);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in login: " + e.toString());
            listener.failure(-1, e.toString());
        } catch (NullPointerException e) {
            IwdsLog.e(this, "Exception in login: " + e.toString());
            listener.failure(-1, e.toString());
        }
    }

    /**
     * 采用第三方账号登录。
     * @param accountType 账号类型。目前账号类型支持 THIRD_ACCOUNT_TYPE_QQ、
     * THIRD_ACCOUNT_TYPE_SINA、THIRD_ACCOUNT_TYPE_BAIDU。
     * @param uid         从第三方账号获取的身份id
     * @param token       从第三方账号获取的业务请求凭据
     * @param listener    监听器对象
     */
    public void loginWithThirdAccount(int accountType, String uid, String token, LoginListener listener) {
        IwdsAssert.dieIf(this, uid == null, "uid is null");
        IwdsAssert.dieIf(this, token == null, "token is null");
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if ((!mEnable) || (mSession == null)) {
            listener.failure(-1, NOT_INIT);
            return;
        }

        try {
            mSession.loginWithThirdAccount(accountType, uid, token, listener.callback);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in loginWithThirdAccount: " + e.toString());
            listener.failure(-1, e.toString());
        } catch (NullPointerException e) {
            IwdsLog.e(this, "Exception in loginWithThirdAccount: " + e.toString());
            listener.failure(-1, e.toString());
        }
    }

    /**
     * 注销登录
     */
    public void logout() {
        try {
            mSession.logout();
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in logout: " + e.toString());
        }
    }

    /**
     * 修改用户密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param listener    监听器对象
     */
    public void changeUserPassword(String oldPassword, String newPassword, AccountListener listener) {
        IwdsAssert.dieIf(this, oldPassword == null, "oldPassword is null");
        IwdsAssert.dieIf(this, newPassword == null, "newPassword is null");
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if ((!mEnable) || (mSession == null)) {
            listener.failure(-1, NOT_INIT);
            return;
        }

        try {
            mSession.changeUserPassword(oldPassword, newPassword, listener.callback);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in changeUserPassword: " + e.toString());
            listener.failure(-1, e.toString());
        } catch (NullPointerException e) {
            IwdsLog.e(this, "Exception in changeUserPassword: " + e.toString());
            listener.failure(-1, e.toString());
        }
    }

    /**
     * 插入数据
     * @param data     要插入的数据List
     * @param listener 监听器对象
     */
    public void insertData(List<CloudDataValues> data, DataInsertListener listener) {
        IwdsAssert.dieIf(this, data == null, "data is null");
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if ((!mEnable) || (mSession == null)) {
            listener.failure(-1, NOT_INIT);
            return;
        }

        try {
            mSession.insertData(data, listener.callback);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in insertData: " + e.toString());
            listener.failure(-1, e.toString());
        } catch (NullPointerException e) {
            IwdsLog.e(this, "Exception in insertData: " + e.toString());
            listener.failure(-1, e.toString());
        }
    }

    /**
     * 查询数据
     * @param query    查询条件对象。
     * @param limit    指定每次返回数据条目数，如果为0则返回20条数据。
     * @param skip     指定从查询结果开头跳过多少条数据。如果查询结果过多，超过 limit 的值，
     *                 需要修改skip多次调用本函数才能获取全部数据。
     * @param listener 监听器对象
     */
    public void queryData(CloudQuery query, int limit, int skip, DataInfoListener listener) {
        IwdsAssert.dieIf(this, query == null, "query is null");
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if ((!mEnable) || (mSession == null)) {
            listener.failure(-1, NOT_INIT);
            return;
        }

        try {
            mSession.queryData(query, limit, skip, listener.callback);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in queryData: " + e.toString());
            listener.failure(-1, e.toString());
        } catch (NullPointerException e) {
            IwdsLog.e(this, "Exception in queryData: " + e.toString());
            listener.failure(-1, e.toString());
        }
    }

    /**
     * 查询数据最新一条数据
     * @param listener 监听器对象
     */
    public void queryLatestData(DataInfoListener listener) {
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if ((!mEnable) || (mSession == null)) {
            listener.failure(-1, NOT_INIT);
            return;
        }

        CloudQuery query = new CloudQuery(ATTR_TIMESTAMP, "<", DATE_MS_3099_12_31);
        try {
            mSession.queryData(query, 1, 0, listener.callback);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in queryData: " + e.toString());
            listener.failure(-1, e.toString());
        } catch (NullPointerException e) {
            IwdsLog.e(this, "Exception in queryData: " + e.toString());
            listener.failure(-1, e.toString());
        }
    }

    /**
     * 更新数据
     * @param query    要更新数据的查询条件
     * @param data     要更新的数据
     * @param listener 监听器对象
     */
    public void updateData(CloudQuery query, List<CloudDataValues> data, DataOperationListener listener) {
        IwdsAssert.dieIf(this, query == null, "query is null");
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if ((!mEnable) || (mSession == null)) {
            listener.failure(-1, NOT_INIT);
            return;
        }

        try {
            mSession.updateData(query, data, listener.callback);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in updateData: " + e.toString());
            listener.failure(-1, e.toString());
        } catch (NullPointerException e) {
            IwdsLog.e(this, "Exception in updateData: " + e.toString());
            listener.failure(-1, e.toString());
        }
    }

    /**
     * 删除数据
     * @param query    要删除数据的查询条件
     * @param listener 监听器对象
     */
    public void deleteData(CloudQuery query, DataOperationListener listener) {
        IwdsAssert.dieIf(this, query == null, "query is null");
        IwdsAssert.dieIf(this, listener == null, "listener is null");

        if ((!mEnable) || (mSession == null)) {
            listener.failure(-1, NOT_INIT);
            return;
        }

        try {
            mSession.deleteData(query, listener.callback);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in deleteData: " + e.toString());
            listener.failure(-1, e.toString());
        } catch (NullPointerException e) {
            IwdsLog.e(this, "Exception in deleteData: " + e.toString());
            listener.failure(-1, e.toString());
        }
    }
}
