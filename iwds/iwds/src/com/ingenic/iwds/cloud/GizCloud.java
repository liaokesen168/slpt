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

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONArray;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import android.os.RemoteException;
import android.content.Context;

import com.ingenic.iwds.utils.IwdsLog;
import com.ingenic.iwds.cloud.CloudQuery;
import com.ingenic.iwds.cloud.CloudDataValues;
import com.ingenic.iwds.cloud.ILoginListener;
import com.ingenic.iwds.cloud.IDataInfoListener;
import com.ingenic.iwds.cloud.IDataInsertListener;
import com.ingenic.iwds.cloud.IDataOperationListener;
import com.ingenic.iwds.cloud.IAccountListener;
import com.ingenic.iwds.cloud.CloudServiceManager;
import com.gizwits.gizdataaccess.GizDataAccess;
import com.gizwits.gizdataaccess.GizDataAccessLogin;
import com.gizwits.gizdataaccess.GizDataAccessSource;
import com.gizwits.gizdataaccess.entity.GizDataAccessAccountType;
import com.gizwits.gizdataaccess.entity.GizDataAccessErrorCode;
import com.gizwits.gizdataaccess.entity.GizDataAccessThirdAccountType;
import com.gizwits.gizdataaccess.listener.GizDataAccessLoginListener;
import com.gizwits.gizdataaccess.listener.GizDataAccessSourceListener;

public class GizCloud {
    /* time millis of 3099/12/31 23:59:59 */
    private static final long DATE_MS_3099_12_31 = 35659324799000l;
    private static final String ATTR_TIMESTAMP = CloudServiceManager.ATTR_TIMESTAMP;

    private String mDeviceSN = "";
    private String mProductKey = "";
    private String mAppKey = "";
    private String mToken = "";
    @SuppressWarnings("unused")
    private String mUid = "";
    private String mPhoneID = "";

    private Context mContext;

    public GizCloud(Context context, String appKey, String productKey,
            String deviceSN, String phoneID) {
        mContext = context;
        mAppKey = appKey;
        mProductKey = productKey;
        mDeviceSN = deviceSN;
        mPhoneID = phoneID;
    }

    public void registerUser(String userName, String password,
            IAccountListener listener) {
        new GizDataAccessLogin(mPhoneID, mAppKey,
                new GizLoginListener(listener)).registerUser(userName,
                password, null,
                GizDataAccessAccountType.kGizDataAccessAccountTypeNormal);
    }

    public void registerUserWithEmail(String email, String password,
            IAccountListener listener) {
        new GizDataAccessLogin(mPhoneID, mAppKey,
                new GizLoginListener(listener)).registerUser(email, password,
                null, GizDataAccessAccountType.kGizDataAccessAccountTypeEmail);
    }

    public void registerUserWithPhone(String phone, String password,
            String verifyCode, IAccountListener listener) {
        new GizDataAccessLogin(mPhoneID, mAppKey,
                new GizLoginListener(listener)).registerUser(phone, password,
                verifyCode,
                GizDataAccessAccountType.kGizDataAccessAccountTypePhone);
    }

    public void requestPhoneVerifyCode(String phone, IAccountListener listener) {
        new GizDataAccessLogin(mPhoneID, mAppKey,
                new GizLoginListener(listener)).requestSendVerifyCode(phone);
    }

    public void resetPasswordWithEmail(String email, IAccountListener listener) {
        new GizDataAccessLogin(mPhoneID, mAppKey,
                new GizLoginListener(listener)).resetPassword(email, null,
                null, GizDataAccessAccountType.kGizDataAccessAccountTypeEmail);
    }

    public void resetPasswordWithPhone(String phone, String verifyCode,
            String newPassword, IAccountListener listener) {
        new GizDataAccessLogin(mPhoneID, mAppKey,
                new GizLoginListener(listener)).resetPassword(phone,
                verifyCode, newPassword,
                GizDataAccessAccountType.kGizDataAccessAccountTypePhone);
    }

    public void loginAnonymous(ILoginListener listener) {
        GizDataAccess.startWithAppId(mContext, mAppKey);
        new GizDataAccessLogin(mPhoneID, mAppKey,
                new GizLoginListener(listener)).loginAnonymous();
    }

    public void login(String userName, String password, ILoginListener listener) {
        GizDataAccess.startWithAppId(mContext, mAppKey);

        new GizDataAccessLogin(mPhoneID, mAppKey,
                new GizLoginListener(listener)).login(userName, password);
    }

    public void loginWithThirdAccount(int accountType, String uid,
            String token, ILoginListener listener) {
        GizDataAccessThirdAccountType type;

        switch (accountType) {
        case CloudServiceManager.THIRD_ACCOUNT_TYPE_QQ:
            type = GizDataAccessThirdAccountType.kGizDataAccessThirdAccountTypeQQ;
            break;
        case CloudServiceManager.THIRD_ACCOUNT_TYPE_SINA:
            type = GizDataAccessThirdAccountType.kGizDataAccessThirdAccountTypeSINA;
            break;
        case CloudServiceManager.THIRD_ACCOUNT_TYPE_BAIDU:
            type = GizDataAccessThirdAccountType.kGizDataAccessThirdAccountTypeBAIDU;
            break;
        default:
            try {
                listener.onFailure(-1, "Unknow acount type");
            } catch (RemoteException e) {
                IwdsLog.e(this, e.toString());
            }
            return;
        }

        new GizDataAccessLogin(mPhoneID, mAppKey,
                new GizLoginListener(listener)).loginWithThirdAccountType(type,
                uid, token);
    }

    public void logout() {
        // TODO 没有实现 userLogout
    }

    public void changeUserPassword(String oldPassword, String newPassword,
            IAccountListener listener) {
        new GizDataAccessLogin(mPhoneID, mAppKey,
                new GizLoginListener(listener)).changeUserPassword(mToken,
                oldPassword, newPassword);
    }

    public void queryData(CloudQuery query, int limit, int skip,
            IDataInfoListener listener) {
        long startTime = 0;
        long endTime = 0;

        CloudQuery q1 = query.getSubQuery1();
        CloudQuery q2 = query.getSubQuery2();

        // only support AND in giz cloud
        if ((q1 != null) && (q1 != null)) {
            CloudQuery.Operator operator = query.getOperator();

            try {
                if (operator == CloudQuery.Operator.OR) {
                    listener.onFailure(-1,
                            "unsupported query operator in giz cloud: OR");
                    return;
                }

                if ((operator == CloudQuery.Operator.NOT) || query.isNot()) {
                    listener.onFailure(-1,
                            "unsupported query operator in giz cloud: NOT");
                    return;
                }
            } catch (RemoteException e) {
                IwdsLog.e(this, e.toString());
            }
        } else {
            q1 = query;
            q2 = null;
        }

        CloudQuery qs[] = { q1, q2 };

        int i = 0;
        CloudQuery q = qs[i];
        while (q != null) {
            // only support timestamp
            String key = q.getKey();
            Object val = q.getValue();
            CloudQuery.Operator operator = q.getOperator();
            if (key.equals(ATTR_TIMESTAMP)) {
                switch (operator) {
                case GREATER_THAN_EQUAL_TO:
                    startTime = ((Number) val).longValue();
                    break;

                case GREATER_THAN:
                    startTime = ((Number) val).longValue() + 1;
                    break;

                case LESS_THAN_EQUAL_TO:
                    endTime = ((Number) val).longValue();
                    break;

                case LESS_THAN:
                    endTime = ((Number) val).longValue() - 1;
                    break;

                case EQUALS:
                    startTime = ((Number) val).longValue();
                    endTime = ((Number) val).longValue();
                    break;
                default:
                }
            } else {
                try {
                    if (operator == CloudQuery.Operator.OR) {
                        listener.onFailure(-1,
                                "unsupported query attribute in giz cloud: "
                                        + key);
                        return;
                    }
                } catch (RemoteException e) {
                    IwdsLog.e(this, e.toString());
                }
            }

            i++;
            q = (i < qs.length) ? qs[i] : null;
        }

        if (endTime == 0) {
            endTime = DATE_MS_3099_12_31;
        }

        new GizDataAccessSource(mPhoneID, mAppKey, new GizDataAccessListener(
                listener, null)).loadData(mToken, mProductKey, mDeviceSN,
                startTime, endTime, limit, skip);
    }

    public void insertData(List<CloudDataValues> datas,
            IDataInsertListener listener) {
        List<String> dataList = new ArrayList<String>();

        Iterator<CloudDataValues> it = datas.iterator();
        while (it.hasNext()) {
            CloudDataValues data = it.next();
            String json = toJSONString(data);
            dataList.add(json);
        }

        GizDataAccessSource access = new GizDataAccessSource(mPhoneID, mAppKey,
                new GizDataAccessListener(null, listener));
        access.saveData(mToken, mProductKey, mDeviceSN, dataList);
    }

    public void updateData(CloudQuery query, List<CloudDataValues> datas,
            IDataOperationListener listener) {
        try {
            listener.onFailure(-1,
                    "unsupported operation of updateData in giz cloud");
        } catch (RemoteException e) {
            IwdsLog.e(this, e.toString());
        }
    }

    public void deleteData(CloudQuery query, IDataOperationListener listener) {
        try {
            listener.onFailure(-1,
                    "unsupported operation of deleteData in giz cloud");
        } catch (RemoteException e) {
            IwdsLog.e(this, e.toString());
        }
    }

    @SuppressWarnings("unused")
    private String toJSONString(CloudDataValues[] datas) {
        JSONStringer stringer = new JSONStringer();

        int count = datas.length;

        try {
            if (count > 1) {
                stringer.array();
            }

            for (int i = 0; i < count; i++) {
                stringer.object();
                CloudDataValues data = datas[i];
                for (String key : data.keySet()) {
                    stringer.key(key).value(data.get(key));
                }
                stringer.endObject();
            }
            if (count > 1) {
                stringer.endArray();
            }
            return stringer.toString();
        } catch (JSONException e) {
            IwdsLog.e(this, "Exception in toJSONString:" + e.toString());
            return null;
        }
    }

    private String toJSONString(CloudDataValues data) {
        try {
            Long timestamp = (Long) data.remove(ATTR_TIMESTAMP);
            if (timestamp == null) {
                timestamp = System.currentTimeMillis();
            }

            JSONObject attrJsonObject = new JSONObject();
            for (String key : data.keySet()) {
                attrJsonObject.put(key, data.get(key));
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("attrs", attrJsonObject);
            jsonObject.put(ATTR_TIMESTAMP, timestamp);

            return jsonObject.toString();
        } catch (JSONException e) {
            IwdsLog.e(this, "Exception in toJSONString:" + e.toString());
            return null;
        }
    }

    private class GizLoginListener extends GizDataAccessLoginListener {
        ILoginListener mLoginListener;
        IAccountListener mAccountListener;

        public GizLoginListener(ILoginListener l) {
            super();
            mLoginListener = l;
            mAccountListener = null;
        }

        public GizLoginListener(IAccountListener l) {
            super();
            mLoginListener = null;
            mAccountListener = l;
        }

        @Override
        public void didLogin(String uid, String token,
                GizDataAccessErrorCode result, String message) {
            super.didLogin(uid, token, result, message);

            try {
                if (result.getResult() == 0) {
                    if ((uid != null) && (token != null)) {
                        mToken = token;
                        mUid = uid;

                        mLoginListener.onSuccess();
                    } else {
                        mLoginListener.onFailure(-1, "bad token or uid");
                    }
                } else {
                    IwdsLog.e(this, "Login fail, code = " + result.toString()
                            + " msg = " + message);
                    mLoginListener.onFailure(-1, message);
                }
            } catch (RemoteException e) {
                IwdsLog.e(this, e.toString());
            }
        }

        @Override
        public void didChangeUserInfo(GizDataAccessErrorCode result,
                String message) {
            super.didChangeUserInfo(result, message);
        }

        @Override
        public void didChangeUserPassword(GizDataAccessErrorCode result,
                String message) {
            super.didChangeUserPassword(result, message);

            try {
                if (result.getResult() == 0) {
                    mAccountListener.onSuccess();
                } else {
                    IwdsLog.e(this, "didChangeUserPassword fail, code = "
                            + result.toString() + " msg = " + message);
                    mAccountListener.onFailure(-1, message);
                }
            } catch (RemoteException e) {
                IwdsLog.e(this, e.toString());
            }
        }

        @Override
        public void didRegisterUser(String uid, String token,
                GizDataAccessErrorCode result, String message) {
            super.didRegisterUser(uid, token, result, message);

            try {
                if (result.getResult() == 0) {
                    mAccountListener.onSuccess();
                } else {
                    IwdsLog.e(this,
                            "didRegisterUser fail, code = " + result.toString()
                                    + " msg = " + message);
                    mAccountListener.onFailure(-1, message);
                }
            } catch (RemoteException e) {
                IwdsLog.e(this, e.toString());
            }
        }

        @Override
        public void didRequestSendVerifyCode(GizDataAccessErrorCode result,
                String message) {
            super.didRequestSendVerifyCode(result, message);

            try {
                if (result.getResult() == 0) {
                    mAccountListener.onSuccess();
                } else {
                    IwdsLog.e(this, "didRequestSendVerifyCode fail, code = "
                            + result.toString() + " msg = " + message);
                    mAccountListener.onFailure(-1, message);
                }
            } catch (RemoteException e) {
                IwdsLog.e(this, e.toString());
            }
        }

        @Override
        public void didTransAnonymousUser(GizDataAccessErrorCode result,
                String message) {
            super.didTransAnonymousUser(result, message);

        }
    }

    private class GizDataAccessListener extends GizDataAccessSourceListener {
        IDataInfoListener mQueryListener;
        IDataInsertListener mInsertListener;

        public GizDataAccessListener(IDataInfoListener queryListener,
                IDataInsertListener insertListener) {
            super();
            mQueryListener = queryListener;
            mInsertListener = insertListener;
        }

        @Override
        public void didLoadData(GizDataAccessSource source,
                JSONArray jsonArray, GizDataAccessErrorCode result,
                String message) {
            super.didLoadData(source, jsonArray, result, message);

            if ((result.getResult() != 0) || (jsonArray == null)) {
                IwdsLog.e(this, "didLoadData failed, message=" + message);

                try {
                    mQueryListener.onFailure(-1, message);
                } catch (RemoteException e) {
                    IwdsLog.e(this, e.toString());
                }

                return;
            }

            List<CloudDataValues> list = new ArrayList<CloudDataValues>(
                    jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {
                CloudDataValues dataValues = new CloudDataValues();
                try {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    JSONObject jsonAttrs = (JSONObject) jsonObj.get("attrs");

                    Object timestamp = jsonObj.get(ATTR_TIMESTAMP);
                    if (timestamp != null) {
                        dataValues.put(ATTR_TIMESTAMP, (Long) timestamp);
                    }

                    @SuppressWarnings("unchecked")
                    Iterator<String> iter = jsonAttrs.keys();

                    while (iter.hasNext()) {
                        String key = iter.next();

                        Object v = jsonAttrs.get(key);
                        dataValues.put(key, v);
                    }
                    list.add(dataValues);
                } catch (JSONException e) {
                    IwdsLog.e(this, e.toString());
                }
            }

            try {
                mQueryListener.onSuccess(list);
            } catch (RemoteException e) {
                IwdsLog.e(this, e.toString());
            }
        }

        @Override
        public void didSaveData(GizDataAccessSource source,
                GizDataAccessErrorCode result, String message) {
            super.didSaveData(source, result, message);

            try {
                if (result.getResult() == 0) {
                    mInsertListener.onSuccess();
                } else {
                    mInsertListener.onFailure(-1, message);
                }
            } catch (RemoteException e) {
                IwdsLog.e(this, e.toString());
            }
        }
    }

}
