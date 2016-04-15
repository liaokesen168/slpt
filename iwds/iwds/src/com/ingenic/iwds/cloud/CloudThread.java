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

import java.util.List;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.ingenic.iwds.cloud.ILoginListener;
import com.ingenic.iwds.cloud.IDataInfoListener;
import com.ingenic.iwds.cloud.IDataInsertListener;
import com.ingenic.iwds.cloud.IDataOperationListener;
import com.ingenic.iwds.cloud.IAccountListener;

public class CloudThread {
    private static final int LOGIN_ANONYMOUS = 2;
    private static final int LOGIN_NORMAL = 3;
    private static final int LOGIN_WITH_THIRD_ACCOUNT = 4;
    private static final int INSERT = 5;
    private static final int QUERY  = 6;
    private static final int UPDATE = 7;
    private static final int DELETE = 8;
    private static final int LOGOUT = 9;
    private static final int REGISTER_USER = 10;
    private static final int REGISTER_USER_WITH_EMAIL = 11;
    private static final int REGISTER_USER_WITH_PHONE = 12;
    private static final int REQUEST_SEND_VERIFY_CODE = 13;
    private static final int CHANGE_USER_PASSWORD = 14;
    private static final int RESET_PASSWORD_WITH_EMAIL = 15;
    private static final int RESET_PASSWORD_WITH_PHONE = 16;

    private static class SomeArgs {
        GizCloud cloud;
        Object obj1;
        Object obj2;
        Object obj3;
        Object listener;
    }

    private static Object mThreadLock = new Object();
    private static HandlerThread mThread;
    private static int count = 0;
    private Handler mHandler;

    public CloudThread() {
        synchronized(mThreadLock) {
            if (mThread == null) {
                mThread = new HandlerThread("CloudServiceThread");
                mThread.start();
            }
            count++;
        }

        mHandler = new Handler(mThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                processMessage(msg);
            }
        };
    }

    public void quit() {
        synchronized(mThreadLock) {
            count--;
            if (count == 0) {
                mThread.quit();
                try {
                    mThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mThread = null;
            }
        }
    }

    private void processMessage(Message msg) {
        switch (msg.what) {
        case LOGIN_ANONYMOUS: {
            SomeArgs args = (SomeArgs)msg.obj;
            args.cloud.loginAnonymous((ILoginListener)args.listener);
            break;
        }
        case LOGIN_NORMAL: {
            SomeArgs args = (SomeArgs) msg.obj;
            args.cloud.login((String) args.obj1, (String) args.obj2,
                    (ILoginListener) args.listener);
            break;
        }
        case LOGIN_WITH_THIRD_ACCOUNT: {
            SomeArgs args = (SomeArgs) msg.obj;
            args.cloud.loginWithThirdAccount(msg.arg1, (String)args.obj1,
                    (String) args.obj2, (ILoginListener) args.listener);
            break;
        }
        case INSERT: {
            SomeArgs args = (SomeArgs) msg.obj;
            args.cloud.insertData((List<CloudDataValues>) args.obj1,
                    (IDataInsertListener) args.listener);
            break;
        }
        case QUERY: {
            SomeArgs args = (SomeArgs)msg.obj;
            args.cloud.queryData((CloudQuery)args.obj1, msg.arg1 /* limit */,
                msg.arg2 /* skip */, (IDataInfoListener)args.listener);
            break;
        }
        case UPDATE: {
            SomeArgs args = (SomeArgs)msg.obj;

            args.cloud.updateData((CloudQuery) args.obj1,
                    (List<CloudDataValues>) args.obj2,
                    (IDataOperationListener) args.listener);
            break;
        }
        case DELETE: {
            SomeArgs args = (SomeArgs) msg.obj;
            args.cloud.deleteData((CloudQuery) args.obj1,
                    (IDataOperationListener) args.listener);
            break;
        }
        case LOGOUT: {
            SomeArgs args = (SomeArgs)msg.obj;
            args.cloud.logout();
            break;
        }

        case REGISTER_USER: {
            SomeArgs args = (SomeArgs) msg.obj;
            args.cloud.registerUser((String) args.obj1, (String) args.obj2,
                    (IAccountListener) args.listener);
            break;
        }
        
        case REGISTER_USER_WITH_EMAIL: {
            SomeArgs args = (SomeArgs) msg.obj;
            args.cloud.registerUserWithEmail((String) args.obj1,
                    (String)args.obj2, (IAccountListener)args.listener);
            break;
        }
        
        case REGISTER_USER_WITH_PHONE: {
            SomeArgs args = (SomeArgs)msg.obj;
            args.cloud.registerUserWithPhone((String)args.obj1, (String)args.obj2,
                    (String)args.obj3, (IAccountListener)args.listener);
            break;
        }

        case CHANGE_USER_PASSWORD: {
            SomeArgs args = (SomeArgs) msg.obj;
            args.cloud.changeUserPassword((String) args.obj1,
                    (String) args.obj2, (IAccountListener) args.listener);
            break;
        }

        case REQUEST_SEND_VERIFY_CODE: {
            SomeArgs args = (SomeArgs) msg.obj;
            args.cloud.requestPhoneVerifyCode((String) args.obj1,
                    (IAccountListener) args.listener);
            break;
        }

        case RESET_PASSWORD_WITH_EMAIL: {
            SomeArgs args = (SomeArgs) msg.obj;
            args.cloud.resetPasswordWithEmail((String) args.obj1,
                    (IAccountListener) args.listener);
            break;
        }

        case RESET_PASSWORD_WITH_PHONE: {
            SomeArgs args = (SomeArgs) msg.obj;
            args.cloud.resetPasswordWithPhone((String) args.obj1,
                    (String) args.obj2, (String) args.obj3,
                    (IAccountListener) args.listener);
            break;
        }

        }
    }

    void registerUser(GizCloud cloud, String userName, String password,
            IAccountListener listener) {
        SomeArgs args = new SomeArgs();
        args.cloud = cloud;
        args.obj1 = userName;
        args.obj2 = password;
        args.listener = listener;

        Message.obtain(mHandler, REGISTER_USER, 0, 0, args).sendToTarget();
    }

    void registerUserWithEmail(GizCloud cloud, String email, String password,
            IAccountListener listener) {
        SomeArgs args = new SomeArgs();
        args.cloud = cloud;
        args.obj1 = email;
        args.obj2 = password;
        args.listener = listener;

        Message.obtain(mHandler, REGISTER_USER_WITH_EMAIL, 0, 0, args)
                .sendToTarget();
    }

    void registerUserWithPhone(GizCloud cloud, String phone, String password,
            String verifyCode, IAccountListener listener) {
        SomeArgs args = new SomeArgs();
        args.cloud = cloud;
        args.obj1 = phone;
        args.obj2 = password;
        args.obj3 = verifyCode;
        args.listener = listener;

        Message.obtain(mHandler, REGISTER_USER_WITH_PHONE, 0, 0, args)
                .sendToTarget();
    }

    void requestPhoneVerifyCode(GizCloud cloud, String phone,
            IAccountListener listener) {
        SomeArgs args = new SomeArgs();
        args.cloud = cloud;
        args.obj1 = phone;
        args.listener = listener;

        Message.obtain(mHandler, REQUEST_SEND_VERIFY_CODE, 0, 0, args)
                .sendToTarget();
    }

    public void resetPasswordWithEmail(GizCloud cloud, String email,
            IAccountListener listener) {
        SomeArgs args = new SomeArgs();
        args.cloud = cloud;
        args.obj1 = email;
        args.listener = listener;

        Message.obtain(mHandler, RESET_PASSWORD_WITH_EMAIL, 0, 0, args)
                .sendToTarget();
    }

    public void resetPasswordWithPhone(GizCloud cloud, String phone,
            String verifyCode, String newPassword, IAccountListener listener) {
        SomeArgs args = new SomeArgs();
        args.cloud = cloud;
        args.obj1 = phone;
        args.obj2 = verifyCode;
        args.obj3 = newPassword;
        args.listener = listener;

        Message.obtain(mHandler, RESET_PASSWORD_WITH_PHONE, 0, 0, args)
                .sendToTarget();
    }

    public void loginAnonymous(GizCloud cloud, ILoginListener listener) {
        SomeArgs args = new SomeArgs();
        args.cloud = cloud;
        args.listener = listener;
        Message.obtain(mHandler, LOGIN_ANONYMOUS, 0, 0, args).sendToTarget();
    }

    public void changeUserPassword(GizCloud cloud, String oldPassword,
            String newPassword, IAccountListener listener) {
        SomeArgs args = new SomeArgs();
        args.cloud = cloud;
        args.obj1 = oldPassword;
        args.obj2 = newPassword;
        args.listener = listener;
        Message.obtain(mHandler, CHANGE_USER_PASSWORD, 0, 0, args)
                .sendToTarget();
    }

    public void insertData(GizCloud cloud, List<CloudDataValues> data,
            IDataInsertListener listener) {
        SomeArgs args = new SomeArgs();
        args.cloud = cloud;
        args.obj1 = data;
        args.listener = listener;

        Message.obtain(mHandler, INSERT, 0, 0, args).sendToTarget();
    }

    public void queryData(GizCloud cloud, CloudQuery query, int limit,
            int skip, IDataInfoListener listener) {
        SomeArgs args = new SomeArgs();
        args.cloud = cloud;
        args.obj1 = query;
        args.listener = listener;

        Message.obtain(mHandler, QUERY, limit, skip, args).sendToTarget();
    }

    public void updateData(GizCloud cloud, CloudQuery query,
            List<CloudDataValues> data, IDataOperationListener listener) {
        SomeArgs args = new SomeArgs();
        args.cloud = cloud;
        args.obj1 = query;
        args.obj2 = data;
        args.listener = listener;

        Message.obtain(mHandler, UPDATE, 0, 0, args).sendToTarget();
    }

    public void deleteData(GizCloud cloud, CloudQuery query,
            IDataOperationListener listener) {
        SomeArgs args = new SomeArgs();
        args.cloud = cloud;
        args.obj1 = query;
        args.listener = listener;

        Message.obtain(mHandler, DELETE, 0, 0, args).sendToTarget();
    }

    public void logout(GizCloud cloud) {
        SomeArgs args = new SomeArgs();
        args.cloud = cloud;

        Message.obtain(mHandler, LOGOUT, 0, 0, args).sendToTarget();
    }

    void login(GizCloud cloud, String userName, String password,
            ILoginListener listener) {
        SomeArgs args = new SomeArgs();
        args.cloud = cloud;
        args.obj1 = userName;
        args.obj2 = password;
        args.listener = listener;

        Message.obtain(mHandler, LOGIN_NORMAL, 0, 0, args).sendToTarget();
    }

    void loginWithThirdAccount(GizCloud cloud, int accountType, String uid,
            String token, ILoginListener listener) {
        SomeArgs args = new SomeArgs();
        args.cloud = cloud;
        args.obj1 = uid;
        args.obj2 = token;
        args.listener = listener;

        Message.obtain(mHandler, LOGIN_WITH_THIRD_ACCOUNT, accountType, 0, args)
                .sendToTarget();
    }
}
