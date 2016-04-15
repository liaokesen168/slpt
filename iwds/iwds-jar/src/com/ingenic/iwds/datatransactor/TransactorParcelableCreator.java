/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  ZhangYanMing <yanming.zhang@ingenic.com, jamincheung@126.com>
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

package com.ingenic.iwds.datatransactor;

import com.ingenic.iwds.os.SafeParcelable;
import android.os.Parcelable;

/**
 * 用于存储实现 Parcelable 接口的类的静态构造接口(CREATOR)
 *
 * @param <T1>
 *            实现 Parcelable 接口的类
 * @param <T2>
 *            实现 SafeParcelable 接口的类
 * 
 * @see android.os.Parcelable
 * @see com.ingenic.iwds.os.SafeParcelable
 */
public abstract class TransactorParcelableCreator<T1 extends Parcelable, T2 extends SafeParcelable> {
    /**
     * 用于存储 Parcelable 类的 CREATOR 接口
     */
    protected Parcelable.Creator<T1> m_parcelableCreator;

    /**
     * 用于存储 SafeParcelable 类的 CREATOR 接口
     */
    protected SafeParcelable.Creator<T2> m_safeParcelableCreator;
}
