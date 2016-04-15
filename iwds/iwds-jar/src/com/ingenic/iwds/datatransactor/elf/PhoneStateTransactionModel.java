/*
 * Copyright (C) 2015 Ingenic Semiconductor
 * 
 * LiJinWen(Kevin)<kevin.jwli@ingenic.com>
 * 
 * Elf/IDWS Project
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.ingenic.iwds.datatransactor.elf;

import android.content.Context;
import com.ingenic.iwds.datatransactor.ProviderTransactionModel;

/**
 * 电话状态传输模型，用于更新设备连接和电话状态传输状态。
 */
public class PhoneStateTransactionModel extends ProviderTransactionModel<PhoneState> {

    public PhoneStateTransactionModel(Context context, PhoneStateCallback callback, String uuid) {
        super(context, PhoneState.CREATOR, callback, uuid);
    }

    /**
     * 定义了电话状态传输的回调函数的接口，用于更新设备连接和电话状态传输状态。
     */
    public interface PhoneStateCallback extends ProviderTransactionModelCallback<PhoneState> {}
}
