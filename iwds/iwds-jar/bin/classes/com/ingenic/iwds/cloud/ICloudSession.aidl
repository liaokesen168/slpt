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

import com.ingenic.iwds.cloud.CloudDataValues;
import com.ingenic.iwds.cloud.CloudQuery;
import com.ingenic.iwds.cloud.ILoginListener;
import com.ingenic.iwds.cloud.IDataInsertListener;
import com.ingenic.iwds.cloud.IDataInfoListener;
import com.ingenic.iwds.cloud.IDataOperationListener;
import com.ingenic.iwds.cloud.IAccountListener;

interface ICloudSession {
    void init();

    void registerUser(String userName, String password, in IAccountListener listener);

    void registerUserWithEmail(String email, String password, in IAccountListener listener);

    void registerUserWithPhone(String phone, String password, String verifyCode, in IAccountListener listener);

    void requestPhoneVerifyCode(String phone, in IAccountListener listener);

    void resetPasswordWithEmail(String email, in IAccountListener listener);

    void resetPasswordWithPhone(String phone, String newPassword, String verifyCode, in IAccountListener listener);

    void loginAnonymous(in ILoginListener listener);

    void login(String userName, String password, in ILoginListener listener);

    void loginWithThirdAccount(int accountType, String uid, String token, in ILoginListener listener);

    void logout();

    void changeUserPassword(String oldPassword, String newPassword, in IAccountListener listener);

    void queryData(in CloudQuery query, int limit, int skip, in IDataInfoListener listener);

    void insertData(in List<CloudDataValues> datas, in IDataInsertListener listener);

    void updateData(in CloudQuery query, in List<CloudDataValues> data, in IDataOperationListener listener);

    void deleteData(in CloudQuery query, in IDataOperationListener listener);
}

