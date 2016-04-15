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

import android.os.Parcel;
import android.os.Parcelable;

import com.ingenic.iwds.os.SafeParcelable;

/**
 * 短信信息类，用于设备之间的短信数据同步。
 */
public class SmsInfo implements Parcelable {

    private int id;//ID
    private int thread_id;//对话序号，同一个号码对话序号相同
    private String address;//对端号码
    private String person;//对端名字
    private String body;//短信内容
    private int type;//类型：1为收件，2为发件
    private int read;//阅读状态：0为未读，1为已读
    private int protocol;//协议：0为短信，1为彩信
    private long date;//发送/接收时间戳

    /**
     * 取得该短信息的ID
     * 
     * @return 短信息的ID
     */
    public int getId() {
        return id;
    }

    /**
     * 设置短信息的ID。
     * <p>
     * 注意：从短信数据库中读取短信时调用，其他时候调用可能会导致出错。
     * 
     * @param id 短信息的ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * 取得短信的对话序号。同一个号码，对话序号相同。
     * 
     * @return 短信的线程ID
     */
    public int getThreadId() {
        return thread_id;
    }

    /**
     * 设置短信的对话序号。同一个号码，对话序号相同。
     * <p>
     * 注意：从短信数据库中读取短信时调用，其他时候调用可能会导致出错。
     * 
     * @param thread_id 短信的对话序号
     */
    public void setThreadId(int thread_id) {
        this.thread_id = thread_id;
    }

    /**
     * 取得该短信对端（发件人或收件人）的号码。
     * 
     * @return 对端（发件人或收件人）的号码
     */
    public String getAddress() {
        return address;
    }

    /**
     * 设置该短信对端（发件人或收件人）的号码。
     * 
     * @param address 对端（发件人或收件人）的号码
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * 获取该短信对端（发件人或收件人）的名字。
     * 
     * @return 对端（发件人或收件人）的名字
     */
    public String getPerson() {
        return person;
    }

    /**
     * 设置该短信对端（发件人或收件人）的名字。
     * 
     * @param person 对端（发件人或收件人）的名字
     */
    public void setPerson(String person) {
        this.person = person;
    }

    /**
     * 取得短信内容
     * 
     * @return 短信内容
     */
    public String getBody() {
        return body;
    }

    /**
     * 设置短信内容
     * 
     * @param body 短信内容
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * 取得短信的类型。1为收件，2为发件。
     * 
     * @return 短信类型
     */
    public int getType() {
        return type;
    }

    /**
     * 设置短信的类型。1为收件，2为发件。
     * 
     * @param type 短信的类型
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * 取得短信的阅读状态。
     * 
     * @return 已读返回1,否则返回0。
     */
    public int getRead() {
        return read;
    }

    /**
     * 设置短信的阅读状态。
     * 
     * @param read 阅读状态，已读为1，未读为0。
     */
    public void setRead(int read) {
        this.read = read;
    }

    /**
     * 获取短信的协议。0为短信，1为彩信。
     * 
     * @return 短信的协议
     */
    public int getProtocol() {
        return protocol;
    }

    /**
     * 设置短信的协议。
     * 
     * @param protocol 短信协议，0为短信，1为彩信。
     */
    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    /**
     * 获取短信的发送/接收时间。
     * 
     * @return 发送/接收时间戳
     */
    public long getDate() {
        return date;
    }

    /**
     * 设置短信的发送/接收时间。
     * 
     * @param date 发送/接受时间戳
     */
    public void setDate(long date) {
        this.date = date;
    }

    /**
     * 由{@link Parcelable#describeContents()}定义
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * 由{@link Parcelable#writeToParcel(Parcel, int)}定义
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(thread_id);
        dest.writeString(address);
        dest.writeString(person);
        dest.writeString(body);
        dest.writeLong(date);
        dest.writeInt(read);
        dest.writeInt(protocol);
        dest.writeInt(type);
    }

    /**
     * 短信信息类的构造器，用于从{@link Parcel}中构造短信信息类。
     */
    public static final Creator<SmsInfo> CREATOR = new Creator<SmsInfo>() {

        @Override
        public SmsInfo createFromParcel(Parcel source) {
            SmsInfo info = new SmsInfo();
            info.id = source.readInt();
            info.thread_id = source.readInt();
            info.address = source.readString();
            info.person = source.readString();
            info.body = source.readString();
            info.date = source.readLong();
            info.read = source.readInt();
            info.protocol = source.readInt();
            info.type = source.readInt();
            return info;
        }

        @Override
        public SmsInfo[] newArray(int size) {
            return new SmsInfo[size];
        }
    };

    /**
     * 继承自{@link Object#toString()}
     */
    @Override
    public String toString() {
        return "Sms:id:" + id + ",thread_id:" + thread_id + ",address:" + address + ",person:"
                + person + ",body:" + body + ",date:" + date + ",read:" + read + ",protocol:"
                + protocol + ",type:" + type;
    };
}
