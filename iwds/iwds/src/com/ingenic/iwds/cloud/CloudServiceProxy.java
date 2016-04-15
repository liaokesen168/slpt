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
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.content.Context;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback;
import com.ingenic.iwds.datatransactor.ParcelTransactor;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

public class CloudServiceProxy {
    private static final String CLOUD_TRANSACTOR_UUID = "75ec2bba-7bc7-48df-80eb-e95463f85fb5";

    private static final String ERROR_MSG_INVALID_SESSION = "invalid session";

    private Context m_context;
    private static CloudServiceProxy sInstance;

    private HashMap<String, GizCloud> m_gizCloudInstanceMap;
    private CloudThread m_cloudThread;

    private ParcelTransactor<RemoteCloudRequest> m_transactor;
    private ServiceProxyHandler m_handler;
    private boolean m_transactEnable = false;

    private CloudServiceProxy() {
    }

    public static CloudServiceProxy getInstance() {
        if (sInstance == null)
            sInstance = new CloudServiceProxy();

        return sInstance;
    }

    public void initialize(Context context) {
        m_context = context;

        m_cloudThread = new CloudThread();

        m_gizCloudInstanceMap = new HashMap<String, GizCloud>();

        m_transactor = new ParcelTransactor<RemoteCloudRequest>(m_context,
                RemoteCloudRequest.CREATOR, m_transactorCallback, CLOUD_TRANSACTOR_UUID);

        m_handler = new ServiceProxyHandler();
    }

    public void start() {
        m_transactor.start();
    }

    //----------------------------------------------------------------------------

    private DataTransactorCallback m_transactorCallback = new DataTransactorCallback() {

        @Override
        public void onLinkConnected(DeviceDescriptor descriptor,
                boolean isConnected) {
            // do not care
        }

        @Override
        public void onChannelAvailable(boolean isAvailable) {
            m_transactEnable = isAvailable;
            m_handler.setChannelState(isAvailable);
        }

        @Override
        public void onSendResult(DataTransactResult result) {

        }

        @Override
        public void onDataArrived(Object object) {
            if (object instanceof RemoteCloudRequest) {
                m_handler.handlerRequest((RemoteCloudRequest) object);
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
    // ---------------------------------------------------------------------------

    private class ServiceProxyHandler extends Handler {
        private final static int MSG_CHANNEL_STATUS_CHANGED = 0;

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

        private final static int MSG_RESPONSE_LOGIN_SUCCESS = 100;
        private final static int MSG_RESPONSE_LOGIN_FAILURE = 101;
        private final static int MSG_RESPONSE_ACCOUNT_SUCCESS = 102;
        private final static int MSG_RESPONSE_ACCOUNT_FAILURE = 103;
        private final static int MSG_RESPONSE_QUERY_SUCCESS = 104;
        private final static int MSG_RESPONSE_QUERY_FAILURE = 105;
        private final static int MSG_RESPONSE_INSERT_SUCCESS = 106;
        private final static int MSG_RESPONSE_INSERT_FAILURE = 107;
        private final static int MSG_RESPONSE_OPERATION_SUCCESS = 108;
        private final static int MSG_RESPONSE_OPERATION_FAILURE = 109;

        public void setChannelState(boolean available) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_CHANNEL_STATUS_CHANGED;
            msg.arg1 = available ? 1 : 0;

            msg.sendToTarget();
        }

        public void accountSuccess(String listenerID) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_RESPONSE_ACCOUNT_SUCCESS;

            final Bundle bundle = new Bundle();
            bundle.putString("listenerID", listenerID);
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void accountFailure(String listenerID, int errorCode,
                String errorMsg) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_RESPONSE_ACCOUNT_FAILURE;

            final Bundle bundle = new Bundle();
            bundle.putString("listenerID", listenerID);
            bundle.putInt("errorCode", errorCode);
            bundle.putString("errorMsg", errorMsg);
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void loginSuccess(String listenerID) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_RESPONSE_LOGIN_SUCCESS;

            final Bundle bundle = new Bundle();
            bundle.putString("listenerID", listenerID);
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void loginFailure(String listenerID, int errorCode, String errorMsg) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_RESPONSE_LOGIN_FAILURE;

            final Bundle bundle = new Bundle();
            bundle.putString("listenerID", listenerID);
            bundle.putInt("errorCode", errorCode);
            bundle.putString("errorMsg", errorMsg);
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void querySuccess(String listenerID, List<CloudDataValues> datas) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_RESPONSE_QUERY_SUCCESS;

            final Bundle bundle = new Bundle();
            bundle.putString("listenerID", listenerID);
            bundle.putParcelableArrayList("datas",
                    (ArrayList<CloudDataValues>) datas);
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void queryFailure(String listenerID, int errorCode, String errorMsg) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_RESPONSE_QUERY_FAILURE;

            final Bundle bundle = new Bundle();
            bundle.putString("listenerID", listenerID);
            bundle.putInt("errorCode", errorCode);
            bundle.putString("errorMsg", errorMsg);
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void insertSuccess(String listenerID) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_RESPONSE_INSERT_SUCCESS;

            final Bundle bundle = new Bundle();
            bundle.putString("listenerID", listenerID);
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void insertFailure(String listenerID, int errorCode, String errorMsg) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_RESPONSE_INSERT_FAILURE;

            final Bundle bundle = new Bundle();
            bundle.putString("listenerID", listenerID);
            bundle.putInt("errorCode", errorCode);
            bundle.putString("errorMsg", errorMsg);
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void operationSuccess(String listenerID) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_RESPONSE_OPERATION_SUCCESS;
            final Bundle bundle = new Bundle();
            bundle.putString("listenerID", listenerID);
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void operationFailure(String listenerID, int errorCode, String errorMsg) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_RESPONSE_OPERATION_FAILURE;

            final Bundle bundle = new Bundle();
            bundle.putString("listenerID", listenerID);
            bundle.putInt("errorCode", errorCode);
            bundle.putString("errorMsg", errorMsg);
            msg.setData(bundle);

            msg.sendToTarget();
        }

        public void handlerRequest(RemoteCloudRequest request) {
            final Message msg = Message.obtain(this);
            final Bundle bundle = new Bundle();

            switch (request.type) {
            case RemoteCloudRequest.TYPE_OPEN_SESSION:
                msg.what = MSG_REQUEST_OPEN_SESSION;
                bundle.putString("sessionID", request.sessionID);
                bundle.putString("appKey", request.string1);
                bundle.putString("productKey", request.string2);
                bundle.putString("deviceSN", request.string3);
                bundle.putString("phoneID", request.string4);

                break;

            case RemoteCloudRequest.TYPE_CLOSE_SESSION:
                msg.what = MSG_REQUEST_CLOSE_SESSION;
                bundle.putString("sessionID", request.sessionID);

                break;

            case RemoteCloudRequest.TYPE_REGISTER_USER:
                msg.what = MSG_REQUEST_REGISTER_USER;
                bundle.putString("sessionID", request.sessionID);
                bundle.putString("listenerID", request.listenerID);
                bundle.putString("userName", request.string1);
                bundle.putString("password", request.string2);

                break;

            case RemoteCloudRequest.TYPE_REGISTER_USER_WITH_EMAIL:
                msg.what = MSG_REQUEST_REGISTER_USER_WITH_EMAIL;
                bundle.putString("sessionID", request.sessionID);
                bundle.putString("listenerID", request.listenerID);
                bundle.putString("email", request.string1);
                bundle.putString("password", request.string2);

                break;

            case RemoteCloudRequest.TYPE_REGISTER_USER_WITH_PHONE:
                msg.what = MSG_REQUEST_REGISTER_USER_WITH_PHONE;
                bundle.putString("sessionID", request.sessionID);
                bundle.putString("listenerID", request.listenerID);
                bundle.putString("phone", request.string1);
                bundle.putString("password", request.string2);
                bundle.putString("verifyCode", request.string3);

                break;

            case RemoteCloudRequest.TYPE_REQUEST_PHONE_VERIFY_CODE:
                msg.what = MSG_REQUEST_PHONE_VERIFY_CODE;
                bundle.putString("sessionID", request.sessionID);
                bundle.putString("listenerID", request.listenerID);
                bundle.putString("phone", request.string1);

                break;

            case RemoteCloudRequest.TYPE_RESET_PASSWORD_WITH_EMAIL:
                msg.what = MSG_REQUEST_RESET_PASSWORD_WITH_EMAIL;
                bundle.putString("sessionID", request.sessionID);
                bundle.putString("listenerID", request.listenerID);
                bundle.putString("email", request.string1);

                break;

            case RemoteCloudRequest.TYPE_RESET_PASSWORD_WITH_PHONE:
                msg.what = MSG_REQUEST_RESET_PASSWORD_WITH_PHONE;
                bundle.putString("sessionID", request.sessionID);
                bundle.putString("listenerID", request.listenerID);
                bundle.putString("phone", request.string1);
                bundle.putString("verifyCode", request.string2);
                bundle.putString("newPassword", request.string3);

                break;

            case RemoteCloudRequest.TYPE_LOGIN_ANONYMOUS:
                msg.what = MSG_REQUEST_LOGIN_ANONYMOUS;
                bundle.putString("sessionID", request.sessionID);
                bundle.putString("listenerID", request.listenerID);

                break;

            case RemoteCloudRequest.TYPE_LOGIN:
                msg.what = MSG_REQUEST_LOGIN;
                bundle.putString("sessionID", request.sessionID);
                bundle.putString("listenerID", request.listenerID);
                bundle.putString("userName", request.string1);
                bundle.putString("password", request.string2);

                break;

            case RemoteCloudRequest.TYPE_LOGIN_WITH_THIRD_ACCOUNT:
                msg.what = MSG_REQUEST_LOGIN_WITH_THIRD_ACCOUNT;
                bundle.putString("sessionID", request.sessionID);
                bundle.putString("listenerID", request.listenerID);
                bundle.putInt("accountType", request.int1);
                bundle.putString("uid", request.string1);
                bundle.putString("token", request.string2);

                break;

            case RemoteCloudRequest.TYPE_LOGOUT:
                msg.what = MSG_REQUEST_LOGOUT;
                bundle.putString("sessionID", request.sessionID);

                break;

            case RemoteCloudRequest.TYPE_CHANGE_USER_PASSWORD:
                msg.what = MSG_REQUEST_CHANGE_USER_PASSWORD;
                bundle.putString("sessionID", request.sessionID);
                bundle.putString("listenerID", request.listenerID);
                bundle.putString("oldPassword", request.string1);
                bundle.putString("newPassword", request.string2);

                break;

            case RemoteCloudRequest.TYPE_QUERY_DATA:
                msg.what = MSG_REQUEST_QUERY_DATA;
                bundle.putString("sessionID", request.sessionID);
                bundle.putString("listenerID", request.listenerID);
                bundle.putParcelable("query", request.query);
                bundle.putInt("limit", request.int1);
                bundle.putInt("skip", request.int2);

                break;

            case RemoteCloudRequest.TYPE_INSERT_DATA:
                msg.what = MSG_REQUEST_INSERT_DATA;
                bundle.putString("sessionID", request.sessionID);
                bundle.putString("listenerID", request.listenerID);
                bundle.putParcelableArrayList("datas",
                        (ArrayList<CloudDataValues>)request.datas);

                break;

            case RemoteCloudRequest.TYPE_UPDATE_DATA:
                msg.what = MSG_REQUEST_UPDATE_DATA;
                bundle.putString("sessionID", request.sessionID);
                bundle.putString("listenerID", request.listenerID);
                bundle.putParcelable("query", request.query);
                bundle.putParcelableArrayList("datas",
                        (ArrayList<CloudDataValues>)request.datas);

                break;
            case RemoteCloudRequest.TYPE_DELETE_DATA:
                msg.what = MSG_REQUEST_DELETE_DATA;
                bundle.putString("sessionID", request.sessionID);
                bundle.putString("listenerID", request.listenerID);
                bundle.putParcelable("query", request.query);

                break;

            default:
                IwdsAssert.dieIf(this, true, "Unknown message");
            }

            msg.setData(bundle);
            msg.sendToTarget();
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();

            switch (msg.what) {
            case MSG_CHANNEL_STATUS_CHANGED:
                boolean enable = (msg.arg1 != 0);
                if (!enable) {
                    IwdsLog.i(this, "Channel inavailable");
                    m_gizCloudInstanceMap.clear();
                }
                break;

            case MSG_REQUEST_OPEN_SESSION: {
                String sessionID = bundle.getString("sessionID");
                String appKey = bundle.getString("appKey");
                String productKey = bundle.getString("productKey");
                String deviceSN = bundle.getString("deviceSN");
                String phoneID = bundle.getString("phoneID");

                GizCloud gizCloud = new GizCloud(m_context, appKey, productKey, deviceSN, phoneID);
                m_gizCloudInstanceMap.put(sessionID, gizCloud);

                break;
            }

            case MSG_REQUEST_CLOSE_SESSION: {
                String sessionID = bundle.getString("sessionID");
                m_gizCloudInstanceMap.remove(sessionID);
                break;
            }

            case MSG_REQUEST_REGISTER_USER: {
                String sessionID = bundle.getString("sessionID");
                String userName = bundle.getString("userName");
                String password = bundle.getString("password");
                String listenerID = bundle.getString("listenerID");

                GizCloud cloud = m_gizCloudInstanceMap.get(sessionID);

                ExtAccountListener listener = new ExtAccountListener(listenerID);
                if (cloud == null) {
                    listener.onFailure(-1, ERROR_MSG_INVALID_SESSION);
                    return;
                }

                m_cloudThread.registerUser(cloud, userName, password, listener);

                break;
            }

            case MSG_REQUEST_REGISTER_USER_WITH_EMAIL: {
                String sessionID = bundle.getString("sessionID");
                String email = bundle.getString("email");
                String password = bundle.getString("password");
                String listenerID = bundle.getString("listenerID");

                ExtAccountListener listener = new ExtAccountListener(listenerID);
                GizCloud cloud = m_gizCloudInstanceMap.get(sessionID);
                if (cloud == null) {
                    listener.onFailure(-1, ERROR_MSG_INVALID_SESSION);
                    return;
                }
                m_cloudThread.registerUserWithEmail(cloud, email, password, listener);

                break;
            }

            case MSG_REQUEST_REGISTER_USER_WITH_PHONE: {
                String sessionID = bundle.getString("sessionID");
                String phone = bundle.getString("phone");
                String password = bundle.getString("password");
                String verifyCode = bundle.getString("verifyCode");
                String listenerID = bundle.getString("listenerID");

                ExtAccountListener listener = new ExtAccountListener(listenerID);
                GizCloud cloud = m_gizCloudInstanceMap.get(sessionID);
                if (cloud == null) {
                    listener.onFailure(-1, ERROR_MSG_INVALID_SESSION);
                    return;
                }
                m_cloudThread.registerUserWithPhone(cloud, phone, password, verifyCode, listener);

                break;
            }

            case MSG_REQUEST_PHONE_VERIFY_CODE: {
                String sessionID = bundle.getString("sessionID");
                String phone = bundle.getString("phone");
                String listenerID = bundle.getString("listenerID");

                ExtAccountListener listener = new ExtAccountListener(listenerID);
                GizCloud cloud = m_gizCloudInstanceMap.get(sessionID);
                if (cloud == null) {
                    listener.onFailure(-1, ERROR_MSG_INVALID_SESSION);
                    return;
                }
                m_cloudThread.requestPhoneVerifyCode(cloud, phone, listener);
                break;
            }

            case MSG_REQUEST_RESET_PASSWORD_WITH_EMAIL: {
                String sessionID = bundle.getString("sessionID");
                String email = bundle.getString("email");
                String listenerID = bundle.getString("listenerID");

                ExtAccountListener listener = new ExtAccountListener(listenerID);
                GizCloud cloud = m_gizCloudInstanceMap.get(sessionID);
                if (cloud == null) {
                    listener.onFailure(-1, ERROR_MSG_INVALID_SESSION);
                    return;
                }
                m_cloudThread.resetPasswordWithEmail(cloud, email, listener);
                break;
            }

            case MSG_REQUEST_RESET_PASSWORD_WITH_PHONE: {
                String sessionID = bundle.getString("sessionID");
                String phone = bundle.getString("phone");
                String verifyCode = bundle.getString("verifyCode");
                String newPassword = bundle.getString("newPassword");
                String listenerID = bundle.getString("listenerID");

                ExtAccountListener listener = new ExtAccountListener(listenerID);
                GizCloud cloud = m_gizCloudInstanceMap.get(sessionID);
                if (cloud == null) {
                    listener.onFailure(-1, ERROR_MSG_INVALID_SESSION);
                    return;
                }
                m_cloudThread.resetPasswordWithPhone(cloud, phone, verifyCode, newPassword, listener);
                break;
            }

            case MSG_REQUEST_LOGIN_ANONYMOUS: {
                String sessionID = bundle.getString("sessionID");
                String listenerID = bundle.getString("listenerID");

                ExtLoginListener listener = new ExtLoginListener(listenerID);
                GizCloud cloud = m_gizCloudInstanceMap.get(sessionID);
                if (cloud == null) {
                    listener.onFailure(-1, ERROR_MSG_INVALID_SESSION);
                    return;
                }
                m_cloudThread.loginAnonymous(cloud, listener);
                break;
            }

            case MSG_REQUEST_LOGIN: {
                String sessionID = bundle.getString("sessionID");
                String userName = bundle.getString("userName");
                String password = bundle.getString("password");
                String listenerID = bundle.getString("listenerID");

                ExtLoginListener listener = new ExtLoginListener(listenerID);
                GizCloud cloud = m_gizCloudInstanceMap.get(sessionID);
                if (cloud == null) {
                    listener.onFailure(-1, ERROR_MSG_INVALID_SESSION);
                    return;
                }
                m_cloudThread.login(cloud, userName, password, listener);
                break;
            }

            case MSG_REQUEST_LOGIN_WITH_THIRD_ACCOUNT: {
                String sessionID = bundle.getString("sessionID");
                int accountType = bundle.getInt("accountType");
                String uid = bundle.getString("uid");
                String token = bundle.getString("token");
                String listenerID = bundle.getString("listenerID");

                ExtLoginListener listener = new ExtLoginListener(listenerID);
                GizCloud cloud = m_gizCloudInstanceMap.get(sessionID);
                if (cloud == null) {
                    listener.onFailure(-1, ERROR_MSG_INVALID_SESSION);
                    return;
                }
                m_cloudThread.loginWithThirdAccount(cloud, accountType, uid, token, listener);
                break;
            }

            case MSG_REQUEST_LOGOUT: {
                String sessionID = bundle.getString("sessionID");
                m_gizCloudInstanceMap.remove(sessionID);
                break;
            }

            case MSG_REQUEST_CHANGE_USER_PASSWORD: {
                String sessionID = bundle.getString("sessionID");
                String oldPassword = bundle.getString("oldPassword");
                String newPassword = bundle.getString("newPassword");
                String listenerID = bundle.getString("listenerID");

                ExtAccountListener listener = new ExtAccountListener(listenerID);
                GizCloud cloud = m_gizCloudInstanceMap.get(sessionID);
                if (cloud == null) {
                    listener.onFailure(-1, ERROR_MSG_INVALID_SESSION);
                    return;
                }
                m_cloudThread.changeUserPassword(cloud, oldPassword, newPassword, listener);

                break;
            }

            case MSG_REQUEST_QUERY_DATA: {
                String sessionID = bundle.getString("sessionID");
                CloudQuery query = bundle.getParcelable("query");
                int limit = bundle.getInt("limit");
                int skip = bundle.getInt("skip");
                String listenerID = bundle.getString("listenerID");

                ExtDataInfoListener listener = new ExtDataInfoListener(listenerID);
                GizCloud cloud = m_gizCloudInstanceMap.get(sessionID);
                if (cloud == null) {
                    listener.onFailure(-1, ERROR_MSG_INVALID_SESSION);
                    return;
                }
                m_cloudThread.queryData(cloud, query, limit, skip, listener);
                break;
            }

            case MSG_REQUEST_INSERT_DATA: {
                String sessionID = bundle.getString("sessionID");
                List<CloudDataValues> datas = bundle
                        .getParcelableArrayList("datas");
                String listenerID = bundle.getString("listenerID");

                ExtDataInsertListener listener = new ExtDataInsertListener(listenerID);
                GizCloud cloud = m_gizCloudInstanceMap.get(sessionID);
                if (cloud == null) {
                    listener.onFailure(-1, ERROR_MSG_INVALID_SESSION);
                    return;
                }
                m_cloudThread.insertData(cloud, datas, listener);
                break;
            }

            case MSG_REQUEST_UPDATE_DATA: {
                String sessionID = bundle.getString("sessionID");
                CloudQuery query = bundle.getParcelable("query");
                ArrayList<CloudDataValues> datas = bundle
                        .getParcelableArrayList("datas");
                String listenerID = bundle.getString("listenerID");

                ExtDataOperationListener listener = new ExtDataOperationListener(
                        listenerID);
                GizCloud cloud = m_gizCloudInstanceMap.get(sessionID);
                if (cloud == null) {
                    listener.onFailure(-1, ERROR_MSG_INVALID_SESSION);
                    return;
                }
                m_cloudThread.updateData(cloud, query, datas, listener);

                break;
            }

            case MSG_REQUEST_DELETE_DATA: {
                String sessionID = bundle.getString("sessionID");
                CloudQuery query = bundle.getParcelable("query");
                String listenerID = bundle.getString("listenerID");

                ExtDataOperationListener listener = new ExtDataOperationListener(
                        listenerID);
                GizCloud cloud = m_gizCloudInstanceMap.get(sessionID);
                if (cloud == null) {
                    listener.onFailure(-1, ERROR_MSG_INVALID_SESSION);
                    return;
                }
                m_cloudThread.deleteData(cloud, query, listener);
                break;
            }

            case MSG_RESPONSE_LOGIN_SUCCESS: {
                RemoteCloudResponse request = RemoteCloudResponse
                        .obtain(m_transactor);
                request.type = RemoteCloudResponse.TYPE_LOGIN_SUCCESS;
                request.listenerID = bundle.getString("listenerID");

                request.sendToRemote();
                break;
            }

            case MSG_RESPONSE_LOGIN_FAILURE: {
                RemoteCloudResponse request = RemoteCloudResponse
                        .obtain(m_transactor);
                request.type = RemoteCloudResponse.TYPE_LOGIN_FAILURE;
                request.listenerID = bundle.getString("listenerID");
                request.errorCode = bundle.getInt("errorCode");
                request.errorMsg = bundle.getString("errorMsg");

                request.sendToRemote();
                break;
            }

            case MSG_RESPONSE_ACCOUNT_SUCCESS: {
                RemoteCloudResponse request = RemoteCloudResponse
                        .obtain(m_transactor);
                request.type = RemoteCloudResponse.TYPE_ACCOUNT_SUCCESS;
                request.listenerID = bundle.getString("listenerID");

                request.sendToRemote();
                break;
            }

            case MSG_RESPONSE_ACCOUNT_FAILURE: {
                RemoteCloudResponse request = RemoteCloudResponse
                        .obtain(m_transactor);
                request.type = RemoteCloudResponse.TYPE_ACCOUNT_FAILURE;
                request.listenerID = bundle.getString("listenerID");
                request.errorCode = bundle.getInt("errorCode");
                request.errorMsg = bundle.getString("errorMsg");

                request.sendToRemote();
                break;
            }

            case MSG_RESPONSE_QUERY_SUCCESS: {
                RemoteCloudResponse request = RemoteCloudResponse
                        .obtain(m_transactor);
                request.type = RemoteCloudResponse.TYPE_QUERY_SUCCESS;
                request.listenerID = bundle.getString("listenerID");
                request.datas = bundle.getParcelableArrayList("datas");

                request.sendToRemote();
                break;
            }

            case MSG_RESPONSE_QUERY_FAILURE: {
                RemoteCloudResponse request = RemoteCloudResponse
                        .obtain(m_transactor);
                request.type = RemoteCloudResponse.TYPE_QUERY_FAILURE;
                request.listenerID = bundle.getString("listenerID");
                request.errorCode = bundle.getInt("errorCode");
                request.errorMsg = bundle.getString("errorMsg");

                request.sendToRemote();
                break;
            }

            case MSG_RESPONSE_INSERT_SUCCESS: {
                RemoteCloudResponse request = RemoteCloudResponse
                        .obtain(m_transactor);
                request.type = RemoteCloudResponse.TYPE_INSERT_SUCCESS;
                request.listenerID = bundle.getString("listenerID");

                request.sendToRemote();
                break;
            }

            case MSG_RESPONSE_INSERT_FAILURE: {
                RemoteCloudResponse request = RemoteCloudResponse
                        .obtain(m_transactor);
                request.type = RemoteCloudResponse.TYPE_INSERT_FAILURE;
                request.listenerID = bundle.getString("listenerID");
                request.errorCode = bundle.getInt("errorCode");
                request.errorMsg = bundle.getString("errorMsg");

                request.sendToRemote();
                break;
            }

            case MSG_RESPONSE_OPERATION_SUCCESS: {
                RemoteCloudResponse request = RemoteCloudResponse
                        .obtain(m_transactor);
                request.type = RemoteCloudResponse.TYPE_OPERATION_SUCCESS;
                request.listenerID = bundle.getString("listenerID");

                request.sendToRemote();
                break;
            }

            case MSG_RESPONSE_OPERATION_FAILURE: {
                RemoteCloudResponse request = RemoteCloudResponse
                        .obtain(m_transactor);
                request.type = RemoteCloudResponse.TYPE_OPERATION_FAILURE;
                request.listenerID = bundle.getString("listenerID");
                request.errorCode = bundle.getInt("errorCode");
                request.errorMsg = bundle.getString("errorMsg");

                request.sendToRemote();
                break;
            }

            default:
                IwdsAssert.dieIf(this, true, "Unknown message");
            }

        }

    }

    //------------------------------------------------------------------
    public class ExtAccountListener implements IAccountListener {
        private String listenerID;

        public ExtAccountListener(String listenerID) {
            this.listenerID = listenerID;
        }

        public void onSuccess() {
            m_handler.accountSuccess(listenerID);
        }

        public void onFailure(int errCode, String errMsg) {
            m_handler.accountFailure(listenerID, errCode, errMsg);
        }

        @Override
        public IBinder asBinder() {
            return null;
        }
    }

    //------------------------------------------------------------------

    public class ExtLoginListener implements ILoginListener {
        private String listenerID;

        public ExtLoginListener(String listenerID) {
            this.listenerID = listenerID;
        }

        @Override
        public void onSuccess() {
            m_handler.loginSuccess(listenerID);
        }

        @Override
        public void onFailure(int errCode, String errMsg) {
            m_handler.loginFailure(listenerID, errCode, errMsg);
        }

        @Override
        public IBinder asBinder() {
            return null;
        }
    }

    //------------------------------------------------------------------

    public class ExtDataInfoListener implements IDataInfoListener {
        private String listenerID;

        public ExtDataInfoListener(String listenerID) {
            this.listenerID = listenerID;
        }

        @Override
        public void onSuccess(List<CloudDataValues> list) {
            m_handler.querySuccess(listenerID, list);
        }

        @Override
        public void onFailure(int errCode, String errMsg) {
            m_handler.queryFailure(listenerID, errCode, errMsg);
        }

        @Override
        public IBinder asBinder() {
            return null;
        }
    }

    //------------------------------------------------------------------

    public class ExtDataInsertListener implements IDataInsertListener {
        private String listenerID;

        public ExtDataInsertListener(String listenerID) {
            this.listenerID = listenerID;
        }

        @Override
        public void onSuccess() {
            m_handler.insertSuccess(listenerID);
        }

        @Override
        public void onFailure(int errCode, String errMsg) {
            m_handler.insertFailure(listenerID, errCode, errMsg);
        }

        @Override
        public IBinder asBinder() {
            return null;
        }
    }

    //------------------------------------------------------------------

    public class ExtDataOperationListener implements IDataOperationListener {
        private String listenerID;

        public ExtDataOperationListener(String listenerID) {
            this.listenerID = listenerID;
        }

        @Override
        public void onSuccess() {
            m_handler.operationSuccess(listenerID);
        }

        @Override
        public void onFailure(int errCode, String errMsg) {
            m_handler.operationFailure(listenerID, errCode, errMsg);
        }

        @Override
        public IBinder asBinder() {
            return null;
        }

    }

}
