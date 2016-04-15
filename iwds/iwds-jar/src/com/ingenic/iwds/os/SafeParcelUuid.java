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

package com.ingenic.iwds.os;

import java.util.UUID;

/**
 * 使用SafeParcel包装的{@link UUID}
 *
 */
public class SafeParcelUuid implements SafeParcelable {
    private UUID m_uuid;

    /**
     * 构造函数
     * 
     * @param uuid
     *            UUID
     * 
     */
    public SafeParcelUuid(UUID uuid) {
        m_uuid = uuid;
    }

    /**
     * 用{@link UUID}字符串构造SafeParcelUuid
     * 
     * @param uuid
     *            uuid字符串
     * @return 　SafeParcelUuid对象
     * @throws NullPointerException
     *             如果 {@code uuid} 是 {@code null}.
     * @throws IllegalArgumentException
     *             如果 {@code uuid} 格式错误
     */
    public static SafeParcelUuid fromString(String uuid) {
        return new SafeParcelUuid(UUID.fromString(uuid));
    }

    /**
     * 返回{@link UUID}
     * 
     * @return UUID
     */
    public UUID getUuid() {
        return m_uuid;
    }

    @Override
    public String toString() {
        return m_uuid.toString();
    }

    @Override
    public int hashCode() {
        return m_uuid.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }

        if (this == object) {
            return true;
        }

        if (!(object instanceof SafeParcelUuid)) {
            return false;
        }

        SafeParcelUuid that = (SafeParcelUuid) object;

        return (this.m_uuid.equals(that.m_uuid));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(SafeParcel dest, int flags) {
        dest.writeLong(m_uuid.getMostSignificantBits());
        dest.writeLong(m_uuid.getLeastSignificantBits());
    }

    public static final SafeParcelable.Creator<SafeParcelUuid> CREATOR = new SafeParcelable.Creator<SafeParcelUuid>() {
        public SafeParcelUuid createFromParcel(SafeParcel source) {

            long mostSigBits = source.readLong();
            long leastSigBits = source.readLong();
            UUID uuid = new UUID(mostSigBits, leastSigBits);

            return new SafeParcelUuid(uuid);
        }

        public SafeParcelUuid[] newArray(int size) {
            return new SafeParcelUuid[size];
        }
    };
}
