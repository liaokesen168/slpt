/*
 *  Copyright (C) 2014 Ingenic Semiconductor
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.content.Intent;
import android.app.Service;
import android.provider.Settings;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.IwdsApplication;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback;
import com.ingenic.iwds.datatransactor.ParcelTransactor;
import com.ingenic.iwds.cloud.ILoginListener;
import com.ingenic.iwds.cloud.IDataInfoListener;
import com.ingenic.iwds.cloud.IDataInsertListener;
import com.ingenic.iwds.cloud.IDataOperationListener;
import com.ingenic.iwds.cloud.IAccountListener;
import com.ingenic.iwds.cloud.CloudDataValues;

public class CloudService extends Service {
    private static final String CLOUD_TRANSACTOR_UUID = "75ec2bba-7bc7-48df-80eb-e95463f85fb5";
    private final static String RECEIVE_DATA_FAIL = "fail to receive mobile's data";
    private final static String TRANSMIT_DATA_FAIL = "can't transmit data to mobile";

    private CloudServiceStub m_service = new CloudServiceStub();
    private CloudSessionMap m_cloudSessionMap;
    private CloudThread m_cloudThread;
    private String m_androidID;
    private String m_deviceSN;
    private boolean m_isWatch;

    private ParcelTransactor<RemoteCloudResponse> m_transactor;
    private ServiceHandler m_handler;
    private boolean m_transactEnable = false;

    @Override
    public void onCreate() {
        IwdsApplication app = (IwdsApplication)getApplication();

        DeviceDescriptor device = app.getLocalDeviceDescriptor();
        if (device.devClass == DeviceDescriptor.DEVICE_CLASS_MOBILE) {
            m_isWatch = false;
        } else {
            m_isWatch = true;
        }

        m_cloudSessionMap = new CloudSessionMap();

        if (m_isWatch) {
            /* 服务运行于手表端，通过蓝牙从手机端的 CloudServiceProxy 获取数据 */
            m_transactor = new ParcelTransactor<RemoteCloudResponse>(this,
                    RemoteCloudResponse.CREATOR, m_transactorCallback,
                    CLOUD_TRANSACTOR_UUID);
            m_handler = new ServiceHandler();
        } else {
            /* 服务运行于手机端，直接从网络获取数据 */
            m_cloudThread = new CloudThread();
        }

        m_androidID = Settings.Secure.getString(
                this.getContentResolver(), Settings.Secure.ANDROID_ID);

        /* 获取蓝牙mac地址，作为机智云 DeviceSN */
        m_deviceSN = device.devAddress;
    }

    @Override
    public void onStart(Intent intent, int startId) {
    }

    @Override
    public void onDestroy() {
        m_cloudSessionMap.kill();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (m_transactor != null) {
            m_transactor.start();
        }

        return m_service;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (m_transactor != null) {
            m_transactor.stop();
        }

        if (m_cloudThread != null) {
            m_cloudThread.quit();
        }

        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    private class CloudServiceStub extends ICloudService.Stub {
        @Override
        public ICloudSession getSession(String appKey, String productKey, IServiceStatusCallback callback) {
            return new CloudSessionStub(appKey, productKey, callback);
        }
    }

    class CloudSessionStub extends ICloudSession.Stub {
        private UUID m_sessionID;
        private String m_appKey;
        private String m_productKey;
        private GizCloud m_cloud;

        public CloudSessionStub() {
            super();
        }

        public CloudSessionStub(String appKey, String productKey,
                IServiceStatusCallback callback) {
            super();
            m_sessionID = UUID.randomUUID();
            m_appKey = appKey;
            m_productKey = productKey;

            if (!m_isWatch) {
                m_cloud = new GizCloud(CloudService.this, appKey, productKey,
                        m_deviceSN, m_androidID);
            }

            m_cloudSessionMap.register(callback, this);
        }

        @Override
        public void init()
                throws RemoteException {

            if (m_isWatch) {
                m_handler.openSession(m_sessionID, m_appKey,
                        m_productKey, m_deviceSN, m_androidID);
            }
        }

        @Override
        public void registerUser(String userName, String password,
                IAccountListener listener) throws RemoteException {
            if (m_isWatch) {
                if (!m_transactEnable) {
                    listener.onFailure(-1, TRANSMIT_DATA_FAIL);
                    return;
                }
                m_handler.registerUser(m_sessionID, userName, password,
                        putListener(listener));
            } else {
                m_cloudThread.registerUser(m_cloud, userName, password, listener);
            }
        }

        @Override
        public void registerUserWithEmail(String email, String password,
                IAccountListener listener) throws RemoteException {
            if (m_isWatch) {
                if (!m_transactEnable) {
                    listener.onFailure(-1, TRANSMIT_DATA_FAIL);
                    return;
                }
                m_handler.registerUserWithEmail(m_sessionID, email, password,
                        putListener(listener));
            } else {
                m_cloudThread.registerUserWithEmail(m_cloud, email, password,
                        listener);
            }
        }

        @Override
        public void registerUserWithPhone(String phone, String password,
                String verifyCode, IAccountListener listener)
                throws RemoteException {
            if (m_isWatch) {
                if (!m_transactEnable) {
                    listener.onFailure(-1, TRANSMIT_DATA_FAIL);
                    return;
                }
                m_handler.registerUserWithPhone(m_sessionID, phone, password,
                        verifyCode, putListener(listener));
            } else {
                m_cloudThread.registerUserWithPhone(m_cloud, phone, password,
                        verifyCode, listener);
            }
        }

        @Override
        public void requestPhoneVerifyCode(String phone, IAccountListener listener)
                throws RemoteException {
            if (m_isWatch) {
                if (!m_transactEnable) {
                    listener.onFailure(-1, TRANSMIT_DATA_FAIL);
                    return;
                }
                m_handler.requestPhoneVerifyCode(m_sessionID, phone, putListener(listener));
            } else {
                m_cloudThread.requestPhoneVerifyCode(m_cloud, phone, listener);
            }
        }

        @Override
        public void resetPasswordWithEmail(String email, IAccountListener listener)
                throws RemoteException {
            if (m_isWatch) {
                if (!m_transactEnable) {
                    listener.onFailure(-1, TRANSMIT_DATA_FAIL);
                    return;
                }
                m_handler.resetPasswordWithEmail(m_sessionID, email,
                        putListener(listener));
            } else {
                m_cloudThread.resetPasswordWithEmail(m_cloud, email, listener);
            }
        }

        @Override
        public void resetPasswordWithPhone(String phone, String verifyCode,
                String newPassword, IAccountListener listener)
                throws RemoteException {
            if (m_isWatch) {
                if (!m_transactEnable) {
                    listener.onFailure(-1, TRANSMIT_DATA_FAIL);
                    return;
                }
                m_handler.resetPasswordWithPhone(m_sessionID, phone, verifyCode,
                        newPassword, putListener(listener));
            } else {
                m_cloudThread.resetPasswordWithPhone(m_cloud, phone, verifyCode,
                        newPassword, listener);
            }
        }

        @Override
        public void loginAnonymous(ILoginListener listener)
                throws RemoteException {
            if (m_isWatch) {
                if (!m_transactEnable) {
                    listener.onFailure(-1, TRANSMIT_DATA_FAIL);
                    return;
                }
                m_handler.loginAnonymous(m_sessionID, putListener(listener));
            } else {
                m_cloudThread.loginAnonymous(m_cloud, listener);
            }
        }

        @Override
        public void login(String userName, String password,
                ILoginListener listener) throws RemoteException {
            if (m_isWatch) {
                if (!m_transactEnable) {
                    listener.onFailure(-1, TRANSMIT_DATA_FAIL);
                    return;
                }
                m_handler.login(m_sessionID, userName, password,
                        putListener(listener));
            } else {
                m_cloudThread.login(m_cloud, userName, password, listener);
            }
        }

        @Override
        public void loginWithThirdAccount(int accountType, String uid,
                String token, ILoginListener listener) throws RemoteException {
            if (m_isWatch) {
                if (!m_transactEnable) {
                    listener.onFailure(-1, TRANSMIT_DATA_FAIL);
                    return;
                }
                m_handler.loginWithThirdAccount(m_sessionID, accountType, uid,
                        token, putListener(listener));
            } else {
                m_cloudThread.loginWithThirdAccount(m_cloud, accountType, uid,
                        token, listener);
            }
        }

        @Override
        public void logout() throws RemoteException {
            if (m_isWatch) {
                m_handler.logout(m_sessionID);
            } else {
                m_cloudThread.logout(m_cloud);
            }
        }

        @Override
        public void changeUserPassword(String oldPassword, String newPassword,
                IAccountListener listener) throws RemoteException {
            if (m_isWatch) {
                if (!m_transactEnable) {
                    listener.onFailure(-1, TRANSMIT_DATA_FAIL);
                    return;
                }
                m_handler.changeUserPassword(m_sessionID, oldPassword,
                        newPassword, putListener(listener));
            } else {
                m_cloudThread.changeUserPassword(m_cloud, oldPassword,
                        newPassword, listener);
            }
        }

        @Override
        public void insertData(List<CloudDataValues> data,
                IDataInsertListener listener) throws RemoteException {
            if (m_isWatch) {
                if (!m_transactEnable) {
                    listener.onFailure(-1, TRANSMIT_DATA_FAIL);
                    return;
                }
                m_handler.insertData(m_sessionID, data, putListener(listener));
            } else {
                m_cloudThread.insertData(m_cloud, data, listener);
            }
        }

        @Override
        public void queryData(CloudQuery query, int limit, int skip,
                IDataInfoListener listener) throws RemoteException {
            if (m_isWatch) {
                if (!m_transactEnable) {
                    listener.onFailure(-1, TRANSMIT_DATA_FAIL);
                    return;
                }

                if (limit <= 0)
                    limit = 20;
                m_handler.queryData(m_sessionID, query, limit, skip,
                        putListener(listener));
            } else {
                m_cloudThread.queryData(m_cloud, query, limit, skip, listener);
            }
        }

        @Override
        public void updateData(CloudQuery query, List<CloudDataValues> data,
                IDataOperationListener listener) throws RemoteException {
            if (m_isWatch) {
                if (!m_transactEnable) {
                    listener.onFailure(-1, TRANSMIT_DATA_FAIL);
                    return;
                }
                m_handler.updateData(m_sessionID, query, data,
                        putListener(listener));
            } else {
                m_cloudThread.updateData(m_cloud, query, data, listener);
            }
        }

        @Override
        public void deleteData(CloudQuery query, IDataOperationListener listener)
                throws RemoteException {
            if (m_isWatch) {
                if (!m_transactEnable) {
                    listener.onFailure(-1, TRANSMIT_DATA_FAIL);
                    return;
                }
                m_handler.deleteData(m_sessionID, query, putListener(listener));
            } else {
                m_cloudThread.deleteData(m_cloud, query, listener);
            }
        }

        //-------------------------------------------------------------------------------
        public void failAllCallback() {
            /* onFailure in all listener */
            synchronized (mCallbackMap) {
                Set<Entry<String, Callback>> entrySet = mCallbackMap.entrySet();
                Iterator<Entry<String, Callback>> entryIter = entrySet.iterator();

                while (entryIter.hasNext()) {
                    Entry<String, Callback> entry = entryIter.next();

                    Callback cb = entry.getValue();
                    cb.unlinkToDeath();

                    IInterface localListener = cb.getListener();
                    try {
                        if (localListener instanceof IAccountListener) {
                            ((IAccountListener)localListener).onFailure(-1, RECEIVE_DATA_FAIL);
                        }
                        if (localListener instanceof ILoginListener) {
                            ((ILoginListener)localListener).onFailure(-1, RECEIVE_DATA_FAIL);
                        }
                        if (localListener instanceof IDataInsertListener) {
                            ((IDataInsertListener)localListener).onFailure(-1, RECEIVE_DATA_FAIL);
                        }
                        if (localListener instanceof IDataInfoListener) {
                            ((IDataInfoListener)localListener).onFailure(-1, RECEIVE_DATA_FAIL);
                        }
                        if (localListener instanceof IDataOperationListener) {
                            ((IDataOperationListener)localListener).onFailure(-1, RECEIVE_DATA_FAIL);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }

            mCallbackMap.clear();
        }

        //-------------------------------------------------------------------------------

        private final class Callback implements IBinder.DeathRecipient {
            private final IInterface listener;
            private final String id;

            public Callback(IInterface listener, String id) {
                this.listener = listener;
                this.id = id;
            }

            public IInterface getListener() {
                return this.listener;
            }

            public void binderDied() {
                removeListener(this.id);
            }

            public void linkToDeath() {
                try {
                    if (this.listener != null) {
                        this.listener.asBinder().linkToDeath(this, 0);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            public void unlinkToDeath() {
                if (this.listener != null) {
                    this.listener.asBinder().unlinkToDeath(this, 0);
                }
            }
        }

        //-------------------------------------------------------------------------------

        private HashMap<String, Callback>
            mCallbackMap = new HashMap<String, Callback>();

        private String putListener(IInterface listener) {
            String listenerID = UUID.randomUUID().toString();

            Callback cb = new Callback(listener, listenerID);
            cb.linkToDeath();

            synchronized (mCallbackMap) {
                mCallbackMap.put(listenerID, cb);
            }
            return listenerID;
        }

        //-------------------------------------------------------------------------------

        private IInterface removeListener(String listenerID) {
            Callback cb;
            synchronized (mCallbackMap) {
                cb = mCallbackMap.remove(listenerID);
            }
            if (cb != null) {
                cb.unlinkToDeath();
            }

            return cb.getListener();
        }

    }

    //-------------------------------------------------------------------------------

    private class CloudSessionMap extends
            RemoteCallbackList<IServiceStatusCallback> {

        public boolean register(IServiceStatusCallback callback,
                CloudSessionStub session) {
            return super.register(callback, session);
        }

        @Override
        public void onCallbackDied(IServiceStatusCallback callback, Object cookie) {
            super.onCallbackDied(callback, cookie);

            /* if client died then free resource in CloudServiceProxy */
            CloudSessionStub session = (CloudSessionStub)cookie;
            m_handler.closeSession(session.m_sessionID);
            super.onCallbackDied(callback, cookie);
        }

        @Override
        public boolean register(IServiceStatusCallback callback, Object obj) {
            if (!(obj instanceof CloudSessionStub)) {
                IwdsAssert.dieIf(this, true, "obj is must CloudSessionStub");
            }
            return this.register(callback, (CloudSessionStub) obj);
        }

        @Override
        public boolean unregister(IServiceStatusCallback callback) {
            return super.unregister(callback);
        }

        /* notify status changed for all callback */
        public void notifyEnableStatusChange(boolean enable) {
            final int length = this.beginBroadcast();
            for (int i = 0; i < length; i++) {
                final IServiceStatusCallback listener = this
                        .getBroadcastItem(i);
                try {
                    listener.onEnableStatusChange(enable);
                } catch (RemoteException e) {
                }
            }
            this.finishBroadcast();
        }

        /* notify fail for all callback */
        public void failAllCallback() {
            final int length = this.beginBroadcast();
            for (int i = 0; i < length; i++) {
                final CloudSessionStub session = (CloudSessionStub) getBroadcastCookie(i);
                session.failAllCallback();
            }
            this.finishBroadcast();
        }

        public IInterface removeListener(String listenerID) {
            IInterface listener = null;
            final int length = this.beginBroadcast();
            for (int i = 0; i < length; i++) {
                final CloudSessionStub session = (CloudSessionStub) getBroadcastCookie(i);
                listener = session.removeListener(listenerID);
                if (listener != null)
                    break;
            }
            this.finishBroadcast();
            return listener;
        }
    }

    //-------------------------------------------------------------------------------

    private class ServiceHandler extends Handler {

        private final static int MSG_CHANNEL_STATUS_CHANGED = 0;
        private final static int MSG_SEND_FAILED = 1;

        private final static int MSG_REQUEST_OPEN_SESSION = 10;
        private final static int MSG_REQUEST_CLOSE_SESSION = 11;
        private final static int MSG_REQUEST_REGISTER_USER = 12;
        private final static int MSG_REQUEST_REGISTER_USER_WITH_EMAIL = 13;
        private final static int MSG_REQUEST_REGISTER_USER_WITH_PHONE = 14;
        private final static int MSG_REQUEST_PHONE_VERIFY_CODE = 15;
        private final static int MSG_REQUEST_RESET_PASSWORD_WITH_EMAIL = 16;
        private final static int MSG_REQUEST_RESET_PASSWORD_WITH_PHONE = 17;
        private final static int MSG_REQUEST_LOGIN_ANONYMOUS = 18;
        private final static int MSG_REQUEST_LOGIN = 19;
        private final static int MSG_REQUEST_LOGIN_WITH_THIRD_ACCOUNT = 20;
        private final static int MSG_REQUEST_LOGOUT = 21;
        private final static int MSG_REQUEST_CHANGE_USER_PASSWORD = 22;
        private final static int MSG_REQUEST_QUERY_DATA = 23;
        private final static int MSG_REQUEST_INSERT_DATA = 24;
        private final static int MSG_REQUEST_UPDATE_DATA = 25;
        private final static int MSG_REQUEST_DELETE_DATA = 26;

        public final static int MSG_RESPONSE_LOGIN_SUCCESS = 100;
        public final static int MSG_RESPONSE_LOGIN_FAILURE = 101;
        public final static int MSG_RESPONSE_ACCOUNT_SUCCESS = 102;
        public final static int MSG_RESPONSE_ACCOUNT_FAILURE = 103;
        public final static int MSG_RESPONSE_QUERY_SUCCESS = 104;
        public final static int MSG_RESPONSE_QUERY_FAILURE = 105;
        public final static int MSG_RESPONSE_INSERT_SUCCESS = 106;
        public final static int MSG_RESPONSE_INSERT_FAILURE = 107;
        public final static int MSG_RESPONSE_OPERATION_SUCCESS = 108;
        public final static int MSG_RESPONSE_OPERATION_FAILURE = 109;

        public void setChannelAvailable(boolean available) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_CHANNEL_STATUS_CHANGED;
            msg.arg1 = available ? 1 : 0;

            msg.sendToTarget();
        }

        public void sendFailed() {
            final Message msg = Message.obtain(this);
            msg.what = MSG_SEND_FAILED;

            msg.sendToTarget();
        }

        public void openSession(UUID sessionID, String appKey, String productKey, String deviceSN, String phoneID) {

            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            msg.what = MSG_REQUEST_OPEN_SESSION;
            bundle.putString("sessionID", sessionID.toString());
            bundle.putString("appKey", appKey);
            bundle.putString("productKey", productKey);
            bundle.putString("deviceSN", deviceSN);
            bundle.putString("phoneID", phoneID);

            msg.setData(bundle);
            msg.sendToTarget();
        }

        public void closeSession(UUID sessionID) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            msg.what = MSG_REQUEST_CLOSE_SESSION;
            bundle.putString("sessionID", sessionID.toString());

            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void registerUser(UUID sessionID, String userName,
                String password, String listenerID) {

            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            msg.what = MSG_REQUEST_REGISTER_USER;
            bundle.putString("sessionID", sessionID.toString());
            bundle.putString("listenerID", listenerID);
            bundle.putString("userName", userName);
            bundle.putString("password", password);

            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void registerUserWithEmail(UUID sessionID, String email,
                String password, String listenerID) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            msg.what = MSG_REQUEST_REGISTER_USER_WITH_EMAIL;
            bundle.putString("sessionID", sessionID.toString());
            bundle.putString("listenerID", listenerID);
            bundle.putString("email", email);
            bundle.putString("password", password);

            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void registerUserWithPhone(UUID sessionID, String phone,
                String password, String verifyCode, String listenerID) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            msg.what = MSG_REQUEST_REGISTER_USER_WITH_PHONE;
            bundle.putString("sessionID", sessionID.toString());
            bundle.putString("listenerID", listenerID);
            bundle.putString("phone", phone);
            bundle.putString("password", password);
            bundle.putString("verifyCode", verifyCode);

            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void requestPhoneVerifyCode(UUID sessionID, String phone,
                String listenerID) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            msg.what = MSG_REQUEST_PHONE_VERIFY_CODE;
            bundle.putString("sessionID", sessionID.toString());
            bundle.putString("listenerID", listenerID);
            bundle.putString("phone", phone);

            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void resetPasswordWithEmail(UUID sessionID, String email,
                String listenerID) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            msg.what = MSG_REQUEST_RESET_PASSWORD_WITH_EMAIL;
            bundle.putString("sessionID", sessionID.toString());
            bundle.putString("listenerID", listenerID);
            bundle.putString("email", email);

            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void resetPasswordWithPhone(UUID sessionID, String phone,
                String verifyCode, String newPassword, String listenerID) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            msg.what = MSG_REQUEST_RESET_PASSWORD_WITH_PHONE;
            bundle.putString("sessionID", sessionID.toString());
            bundle.putString("listenerID", listenerID);
            bundle.putString("phone", phone);
            bundle.putString("verifyCode", verifyCode);
            bundle.putString("newPassword", newPassword);

            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void loginAnonymous(UUID sessionID, String listenerID) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            msg.what = MSG_REQUEST_LOGIN_ANONYMOUS;
            bundle.putString("sessionID", sessionID.toString());
            bundle.putString("listenerID", listenerID);

            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void login(UUID sessionID, String userName, String password,
                String listenerID) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            msg.what = MSG_REQUEST_LOGIN;
            bundle.putString("sessionID", sessionID.toString());
            bundle.putString("listenerID", listenerID);
            bundle.putString("userName", userName);
            bundle.putString("password", password);

            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void loginWithThirdAccount(UUID sessionID, int accountType,
                String uid, String token, String listenerID) {
            final Message msg = Message.obtain(this, MSG_REQUEST_LOGIN_WITH_THIRD_ACCOUNT);

            final Bundle bundle = new Bundle();
            bundle.putString("sessionID", sessionID.toString());
            bundle.putString("listenerID", listenerID);
            bundle.putInt("accountType", accountType);
            bundle.putString("uid", uid);
            bundle.putString("token", token);

            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void logout(UUID sessionID) {
            final Message msg = Message.obtain(this, MSG_REQUEST_LOGOUT);

            final Bundle bundle = new Bundle();
            bundle.putString("sessionID", sessionID.toString());

            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void changeUserPassword(UUID sessionID, String oldPassword,
                String newPassword, String listenerID) {
            final Message msg = Message.obtain(this, MSG_REQUEST_CHANGE_USER_PASSWORD);

            final Bundle bundle = new Bundle();
            bundle.putString("sessionID", sessionID.toString());
            bundle.putString("listenerID", listenerID);
            bundle.putString("oldPassword", oldPassword);
            bundle.putString("newPassword", newPassword);

            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void queryData(UUID sessionID, CloudQuery query, int limit,
                int skip, String listenerID) {
            final Message msg = Message.obtain(this, MSG_REQUEST_QUERY_DATA);

            final Bundle bundle = new Bundle();
            bundle.putString("sessionID", sessionID.toString());
            bundle.putString("listenerID", listenerID);
            bundle.putParcelable("query", query);
            bundle.putInt("limit", limit);
            bundle.putInt("skip", skip);

            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void insertData(UUID sessionID, List<CloudDataValues> datas,
                String listenerID) {
            final Message msg = Message.obtain(this, MSG_REQUEST_INSERT_DATA);

            final Bundle bundle = new Bundle();
            bundle.putString("sessionID", sessionID.toString());
            bundle.putString("listenerID", listenerID);
            bundle.putParcelableArrayList("datas", (ArrayList<CloudDataValues>)datas);

            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void updateData(UUID sessionID, CloudQuery query,
                List<CloudDataValues> data, String listenerID) {
            final Message msg = Message.obtain(this, MSG_REQUEST_UPDATE_DATA);

            final Bundle bundle = new Bundle();
            bundle.putString("sessionID", sessionID.toString());
            bundle.putString("listenerID", listenerID);
            bundle.putParcelable("query", query);

            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void deleteData(UUID sessionID, CloudQuery query,
                String listenerID) {
            final Message msg = Message.obtain(this, MSG_REQUEST_DELETE_DATA);

            final Bundle bundle = new Bundle();
            bundle.putString("sessionID", sessionID.toString());
            bundle.putString("listenerID", listenerID);
            bundle.putParcelable("query", query);

            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void handleResponse(RemoteCloudResponse response) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            switch (response.type) {
            case RemoteCloudResponse.TYPE_LOGIN_SUCCESS:
                msg.what = MSG_RESPONSE_LOGIN_SUCCESS;
                bundle.putString("listenerID", response.listenerID);
                break;

            case RemoteCloudResponse.TYPE_LOGIN_FAILURE:
                msg.what = MSG_RESPONSE_LOGIN_FAILURE;
                bundle.putString("listenerID", response.listenerID);
                bundle.putInt("errorCode", response.errorCode);
                bundle.putString("errorMsg", response.errorMsg);
                break;

            case RemoteCloudResponse.TYPE_ACCOUNT_SUCCESS:
                msg.what = MSG_RESPONSE_ACCOUNT_SUCCESS;
                bundle.putString("listenerID", response.listenerID);
                break;

            case RemoteCloudResponse.TYPE_ACCOUNT_FAILURE:
                msg.what = MSG_RESPONSE_ACCOUNT_FAILURE;
                bundle.putString("listenerID", response.listenerID);
                bundle.putInt("errorCode", response.errorCode);
                bundle.putString("errorMsg", response.errorMsg);
                break;

            case RemoteCloudResponse.TYPE_QUERY_SUCCESS:
                msg.what = MSG_RESPONSE_QUERY_SUCCESS;
                bundle.putString("listenerID", response.listenerID);
                bundle.putParcelableArrayList("datas",
                        (ArrayList<CloudDataValues>)response.datas);
                break;

            case RemoteCloudResponse.TYPE_QUERY_FAILURE:
                msg.what = MSG_RESPONSE_QUERY_FAILURE;
                bundle.putString("listenerID", response.listenerID);
                bundle.putInt("errorCode", response.errorCode);
                bundle.putString("errorMsg", response.errorMsg);
                break;

            case RemoteCloudResponse.TYPE_INSERT_SUCCESS:
                msg.what = MSG_RESPONSE_INSERT_SUCCESS;
                bundle.putString("listenerID", response.listenerID);
                break;

            case RemoteCloudResponse.TYPE_INSERT_FAILURE:
                msg.what = MSG_RESPONSE_INSERT_FAILURE;
                bundle.putString("listenerID", response.listenerID);
                bundle.putInt("errorCode", response.errorCode);
                bundle.putString("errorMsg", response.errorMsg);
                break;

            case RemoteCloudResponse.TYPE_OPERATION_SUCCESS:
                msg.what = MSG_RESPONSE_OPERATION_SUCCESS;
                bundle.putString("listenerID", response.listenerID);
                break;

            case RemoteCloudResponse.TYPE_OPERATION_FAILURE:
                msg.what = MSG_RESPONSE_OPERATION_FAILURE;
                bundle.putString("listenerID", response.listenerID);
                bundle.putInt("errorCode", response.errorCode);
                bundle.putString("errorMsg", response.errorMsg);
                break;

            default:
                IwdsAssert.dieIf(this, true, "Unsupported request type: "
                        + response.type);
                return;
            }

            msg.setData(bundle);
            msg.sendToTarget();
        }

        @Override
        public void handleMessage(Message msg) {
            RemoteCloudRequest request = RemoteCloudRequest
                    .obtain(m_transactor);
            Bundle bundle = msg.getData();

            switch (msg.what) {
            case MSG_CHANNEL_STATUS_CHANGED: {
                boolean enable = (msg.arg1 != 0);

                if (!enable) {
                    /* fail all client listener */
                    m_cloudSessionMap.failAllCallback();
                }

                /* notify status changed for all client */
                m_cloudSessionMap.notifyEnableStatusChange(enable);

                break;
            }

            case MSG_SEND_FAILED: {
                /* fail all client listener */
                m_cloudSessionMap.failAllCallback();
                break;
            }

            case MSG_REQUEST_OPEN_SESSION: {
                request.type = RemoteCloudRequest.TYPE_OPEN_SESSION;
                request.sessionID = bundle.getString("sessionID");
                request.listenerID = bundle.getString("listenerID");
                request.string1 = bundle.getString("appKey");
                request.string2 = bundle.getString("productKey");
                request.string3 = bundle.getString("deviceSN");
                request.string4 = bundle.getString("phoneID");

                request.sendToRemote();

                break;
            }
            case MSG_REQUEST_CLOSE_SESSION: {
                request.type = RemoteCloudRequest.TYPE_CLOSE_SESSION;
                request.sessionID = bundle.getString("sessionID");

                request.sendToRemote();

                break;
            }
            case MSG_REQUEST_REGISTER_USER: {
                request.type = RemoteCloudRequest.TYPE_REGISTER_USER;
                request.sessionID = bundle.getString("sessionID");
                request.listenerID = bundle.getString("listenerID");
                request.string1 = bundle.getString("userName");
                request.string2 = bundle.getString("password");

                request.sendToRemote();

                break;
            }
            case MSG_REQUEST_REGISTER_USER_WITH_EMAIL: {
                request.type = RemoteCloudRequest.TYPE_REGISTER_USER_WITH_EMAIL;
                request.sessionID = bundle.getString("sessionID");
                request.listenerID = bundle.getString("listenerID");
                request.string1 = bundle.getString("email");
                request.string2 = bundle.getString("password");

                request.sendToRemote();

                break;
            }
            case MSG_REQUEST_REGISTER_USER_WITH_PHONE: {
                request.type = RemoteCloudRequest.TYPE_REGISTER_USER_WITH_PHONE;
                request.sessionID = bundle.getString("sessionID");
                request.listenerID = bundle.getString("listenerID");
                request.string1 = bundle.getString("phone");
                request.string2 = bundle.getString("password");
                request.string3 = bundle.getString("verifyCode");

                request.sendToRemote();

                break;
            }
            case MSG_REQUEST_PHONE_VERIFY_CODE: {
                request.type = RemoteCloudRequest.TYPE_REQUEST_PHONE_VERIFY_CODE;
                request.sessionID = bundle.getString("sessionID");
                request.listenerID = bundle.getString("listenerID");
                request.string1 = bundle.getString("phone");

                request.sendToRemote();

                break;
            }
            case MSG_REQUEST_RESET_PASSWORD_WITH_EMAIL: {
                request.type = RemoteCloudRequest.TYPE_RESET_PASSWORD_WITH_EMAIL;
                request.sessionID = bundle.getString("sessionID");
                request.listenerID = bundle.getString("listenerID");
                request.string1 = bundle.getString("email");

                request.sendToRemote();

                break;
            }
            case MSG_REQUEST_RESET_PASSWORD_WITH_PHONE: {
                request.type = RemoteCloudRequest.TYPE_RESET_PASSWORD_WITH_PHONE;
                request.sessionID = bundle.getString("sessionID");
                request.listenerID = bundle.getString("listenerID");
                request.string1 = bundle.getString("phone");
                request.string2 = bundle.getString("verifyCode");
                request.string3 = bundle.getString("newPassword");

                request.sendToRemote();

                break;
            }
            case MSG_REQUEST_LOGIN_ANONYMOUS: {
                request.type = RemoteCloudRequest.TYPE_LOGIN_ANONYMOUS;
                request.sessionID = bundle.getString("sessionID");
                request.listenerID = bundle.getString("listenerID");

                request.sendToRemote();

                break;
            }
            case MSG_REQUEST_LOGIN: {
                request.type = RemoteCloudRequest.TYPE_LOGIN;
                request.sessionID = bundle.getString("sessionID");
                request.listenerID = bundle.getString("listenerID");
                request.string1 = bundle.getString("userName");
                request.string2 = bundle.getString("password");

                request.sendToRemote();

                break;
            }
            case MSG_REQUEST_LOGIN_WITH_THIRD_ACCOUNT: {
                request.type = RemoteCloudRequest.TYPE_LOGIN_WITH_THIRD_ACCOUNT;
                request.sessionID = bundle.getString("sessionID");
                request.listenerID = bundle.getString("listenerID");
                request.int1 = bundle.getInt("accountType");
                request.string1 = bundle.getString("uid");
                request.string2 = bundle.getString("token");

                request.sendToRemote();

                break;
            }
            case MSG_REQUEST_LOGOUT: {
                request.type = RemoteCloudRequest.TYPE_LOGOUT;
                request.sessionID = bundle.getString("sessionID");
                request.listenerID = bundle.getString("listenerID");

                request.sendToRemote();

                break;
            }
            case MSG_REQUEST_CHANGE_USER_PASSWORD: {
                request.type = RemoteCloudRequest.TYPE_CHANGE_USER_PASSWORD;
                request.sessionID = bundle.getString("sessionID");
                request.listenerID = bundle.getString("listenerID");
                request.string1 = bundle.getString("oldPassword");
                request.string2 = bundle.getString("newPassword");

                request.sendToRemote();

                break;
            }

            case MSG_REQUEST_QUERY_DATA: {
                request.type = RemoteCloudRequest.TYPE_QUERY_DATA;
                request.sessionID = bundle.getString("sessionID");
                request.listenerID = bundle.getString("listenerID");
                request.query = bundle.getParcelable("query");
                request.int1 = bundle.getInt("limit");
                request.int2 = bundle.getInt("skip");

                request.sendToRemote();

                break;
            }
            case MSG_REQUEST_INSERT_DATA: {
                request.type = RemoteCloudRequest.TYPE_INSERT_DATA;
                request.sessionID = bundle.getString("sessionID");
                request.listenerID = bundle.getString("listenerID");
                request.datas = bundle.getParcelableArrayList("datas");

                request.sendToRemote();

                break;
            }
            case MSG_REQUEST_UPDATE_DATA: {
                request.type = RemoteCloudRequest.TYPE_UPDATE_DATA;
                request.sessionID = bundle.getString("sessionID");
                request.listenerID = bundle.getString("listenerID");
                request.query = bundle.getParcelable("query");
                request.datas = bundle.getParcelableArrayList("datas");

                request.sendToRemote();

                break;
            }
            case MSG_REQUEST_DELETE_DATA: {
                request.type = RemoteCloudRequest.TYPE_DELETE_DATA;
                request.sessionID = bundle.getString("sessionID");
                request.listenerID = bundle.getString("listenerID");
                request.query = bundle.getParcelable("query");

                request.sendToRemote();

                break;
            }
            case MSG_RESPONSE_LOGIN_SUCCESS: {
                String listenerID = bundle.getString("listenerID");
                ILoginListener listener = (ILoginListener) m_cloudSessionMap
                        .removeListener(listenerID);
                try {
                    listener.onSuccess();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            }
            case MSG_RESPONSE_LOGIN_FAILURE: {
                String listenerID = bundle.getString("listenerID");
                int errorCode = bundle.getInt("errorCode");
                String errorMsg = bundle.getString("errorMsg");
                ILoginListener listener = (ILoginListener) m_cloudSessionMap
                        .removeListener(listenerID);
                try {
                    listener.onFailure(errorCode, errorMsg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            }
            case MSG_RESPONSE_ACCOUNT_SUCCESS: {
                String listenerID = bundle.getString("listenerID");
                IAccountListener listener = (IAccountListener) m_cloudSessionMap
                        .removeListener(listenerID);
                try {
                    listener.onSuccess();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            }
            case MSG_RESPONSE_ACCOUNT_FAILURE: {
                String listenerID = bundle.getString("listenerID");
                int errorCode = bundle.getInt("errorCode");
                String errorMsg = bundle.getString("errorMsg");
                IAccountListener listener = (IAccountListener) m_cloudSessionMap
                        .removeListener(listenerID);
                try {
                    listener.onFailure(errorCode, errorMsg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            }
            case MSG_RESPONSE_QUERY_SUCCESS: {
                String listenerID = bundle.getString("listenerID");
                ArrayList<CloudDataValues> datas = bundle
                        .getParcelableArrayList("datas");
                IDataInfoListener listener = (IDataInfoListener) m_cloudSessionMap
                        .removeListener(listenerID);
                try {
                    listener.onSuccess(datas);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            }
            case MSG_RESPONSE_QUERY_FAILURE: {
                String listenerID = bundle.getString("listenerID");
                int errorCode = bundle.getInt("errorCode");
                String errorMsg = bundle.getString("errorMsg");
                IDataInfoListener listener = (IDataInfoListener) m_cloudSessionMap
                        .removeListener(listenerID);
                try {
                    listener.onFailure(errorCode, errorMsg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            }
            case MSG_RESPONSE_INSERT_SUCCESS: {
                String listenerID = bundle.getString("listenerID");
                IDataInsertListener listener = (IDataInsertListener) m_cloudSessionMap
                        .removeListener(listenerID);
                try {
                    listener.onSuccess();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            }
            case MSG_RESPONSE_INSERT_FAILURE: {
                String listenerID = bundle.getString("listenerID");
                int errorCode = bundle.getInt("errorCode");
                String errorMsg = bundle.getString("errorMsg");
                IDataInsertListener listener = (IDataInsertListener) m_cloudSessionMap
                        .removeListener(listenerID);
                try {
                    listener.onFailure(errorCode, errorMsg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            }
            case MSG_RESPONSE_OPERATION_SUCCESS: {
                String listenerID = bundle.getString("listenerID");
                IDataOperationListener listener = (IDataOperationListener) m_cloudSessionMap
                        .removeListener(listenerID);
                try {
                    listener.onSuccess();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            }
            case MSG_RESPONSE_OPERATION_FAILURE: {
                String listenerID = bundle.getString("listenerID");
                int errorCode = bundle.getInt("errorCode");
                String errorMsg = bundle.getString("errorMsg");
                IDataOperationListener listener = (IDataOperationListener) m_cloudSessionMap
                        .removeListener(listenerID);
                try {
                    listener.onFailure(errorCode, errorMsg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            }
            default:
                IwdsAssert.dieIf(this, true, "Unknown message");
            }
        }
    }

    private DataTransactorCallback m_transactorCallback = new DataTransactorCallback() {

        @Override
        public void onLinkConnected(DeviceDescriptor descriptor,
                boolean isConnected) {
            // do not care
        }

        @Override
        public void onChannelAvailable(boolean isAvailable) {
            m_transactEnable = isAvailable;
            m_handler.setChannelAvailable(isAvailable);
        }

        @Override
        public void onSendResult(DataTransactResult result) {
            if(result.getResultCode() != 0) {
                IwdsLog.d(this, "send data failed, result=" + result.getResultCode());
                m_handler.sendFailed();
            }
        }

        @Override
        public void onDataArrived(Object object) {
            if (object instanceof RemoteCloudResponse) {
                m_handler.handleResponse((RemoteCloudResponse) object);
            }
        }

        @Override
        public void onSendFileProgress(int progress) {
            // do not care
        }

        @Override
        public void onRecvFileProgress(int progress) {
            // do not care
        }

        @Override
        public void onSendFileInterrupted(int index) {

        }

        @Override
        public void onRecvFileInterrupted(int index) {

        }

    };

}
