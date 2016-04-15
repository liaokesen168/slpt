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
 * 短信数据传输模型，用于更新设备连接和短信数据传输状态。
 */
public class SmsTransactionModel extends ProviderTransactionModel<SmsInfo> {

    public SmsTransactionModel(Context context, SmsTransactionCallback callback, String uuid) {
        super(context, SmsInfo.CREATOR, callback, uuid);
    }

    /**
     * 定义了短信数据传输的回调函数的接口，用于更新设备连接和短信数据传输状态。
     */
    public interface SmsTransactionCallback extends
            ProviderTransactionModel.ProviderTransactionModelCallback<SmsInfo> {}
}
