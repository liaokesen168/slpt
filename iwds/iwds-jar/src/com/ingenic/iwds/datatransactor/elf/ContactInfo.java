/*
 * Copyright (C) 2015 Ingenic Semiconductor
 * 
 * nongjiabao<jiabao.nong@ingenic.com>
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

import java.util.ArrayList;
import java.util.List;

public class ContactInfo implements Parcelable {

    public static class EmailInfo implements Parcelable {
        /** 邮箱类型 */
        public int type;
        /** 邮箱 */
        public String email;

        public static final Creator<EmailInfo> CREATOR = new Creator<EmailInfo>() {

            @Override
            public EmailInfo createFromParcel(Parcel source) {
                EmailInfo info = new EmailInfo();
                info.type = source.readInt();
                info.email = source.readString();
                return info;
            }

            @Override
            public EmailInfo[] newArray(int size) {
                return new EmailInfo[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(type);
            dest.writeString(email);
        }
    }
    public static class PhoneInfo implements Parcelable {
        /** 联系电话类型 */
        public int type;
        /** 联系电话 */
        public String number;

        public static final Creator<PhoneInfo> CREATOR = new Creator<PhoneInfo>() {

            @Override
            public PhoneInfo createFromParcel(Parcel source) {
                PhoneInfo info = new PhoneInfo();
                info.type = source.readInt();
                info.number = source.readString();
                return info;
            }

            @Override
            public PhoneInfo[] newArray(int size) {
                return new PhoneInfo[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(type);
            dest.writeString(number);
        }
    }
    public static final int OPT_ADD = 0;
    public static final int OPT_UPDATE = 1;
    public static final int OPT_DEL = 2;
    public int    operation; /* STATE_ADD, STATE_UPDATE, STATE_DEL*/
    public String raw_id;

    public String name; // 姓名

    public List<PhoneInfo> phoneList = new ArrayList<PhoneInfo>(); // 联系号码
    public List<EmailInfo> email = new ArrayList<EmailInfo>(); // Email

    public static final Creator<ContactInfo> CREATOR = new Creator<ContactInfo>() {

        @Override
        public ContactInfo createFromParcel(Parcel source) {
            ContactInfo info = new ContactInfo();
            info.operation = source.readInt();
            info.raw_id = source.readString();
            info.name = source.readString();
            info.phoneList = source.readArrayList(PhoneInfo.class.getClassLoader());
            info.email = source.readArrayList(EmailInfo.class.getClassLoader());
            return info;
        }

        @Override
        public ContactInfo[] newArray(int size) {
            return new ContactInfo[size];
        }

    };

    public ContactInfo() {
        this.operation = -1;
        this.raw_id = "";
        this.name = "";
    }

    public ContactInfo(String id, String name) {
        this.operation = -1;
        this.raw_id = id;
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /** 邮箱信息 */
    public List<EmailInfo> getEmail() {
        return email;
    }

    /** 姓名 */
    public String getName() {
        return name;
    }

    /** 联系电话信息 */
    public List<PhoneInfo> getPhoneList() {
        return phoneList;
    }

    /** raw_id */
    public String getRaw_id() {
        return raw_id;
    }

    public int getState() {
        return operation;
    }

    /** 邮箱信息 */
    public void setEmail(List<EmailInfo> email) {
        this.email = email;
    }

    /** 姓名 */
    public void setName(String name) {
        this.name = name;
    }

    /** 联系电话信息 */
    public void setPhoneList(List<PhoneInfo> phoneList) {
        this.phoneList = phoneList;
    }

    public void setState(int operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "{operation: " + operation + " raw_id: " + raw_id +  " name: " + name + ", phoneList: " + phoneList + ", email: " + email + "}";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(operation);
        dest.writeString(raw_id);
        dest.writeString(name);
        dest.writeList(phoneList);
        dest.writeList(email);
    }

}
