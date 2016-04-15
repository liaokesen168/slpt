/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  SunWenZhong(Fighter) <wenzhong.sun@ingenic.com, wanmyqawdr@126.com>
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
 *
 */

package com.ingenic.iwds.datatransactor.elf;

import android.content.Context;

import com.ingenic.iwds.datatransactor.ProviderTransactionModel;

/**
 * 音乐控制传输模型类.
 */
public class MusicControlTransactionModel extends
        ProviderTransactionModel<MusicControlInfo> {

    /**
     * 实例化音乐控制传输模型对象.
     *
     * @param context
     *            应用的上下文
     * @param callback
     *            回调
     * @param uuid
     *            UUID
     */
    public MusicControlTransactionModel(Context context,
            MusicControlTransactionModelCallback callback, String uuid) {
        super(context, MusicControlInfo.CREATOR, callback, uuid);

    }

    /**
     * 音乐控制传输模型的回调接口.
     */
    public interface MusicControlTransactionModelCallback extends
            ProviderTransactionModelCallback<MusicControlInfo> {

    }
}
