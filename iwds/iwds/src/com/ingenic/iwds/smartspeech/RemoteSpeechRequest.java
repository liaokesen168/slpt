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

package com.ingenic.iwds.smartspeech;

import java.util.HashMap;

import com.ingenic.iwds.datatransactor.DataTransactor;

import android.os.Parcel;
import android.os.Parcelable;

public class RemoteSpeechRequest implements Parcelable {
    public final static int TYPE_RECOGNIZE_APPEND_DATA = 0;
    public final static int TYPE_RECOGNIZE_START_LISTEN = 1;
    public final static int TYPE_RECOGNIZE_LISTEN_STATUS = 2;
    public final static int TYPE_RECOGNIZE_STOP_LISTEN = 3;
    public final static int TYPE_RECOGNIZE_CANCEL_LISTEN = 4;
    public final static int TYPE_UNDERSTAND_APPEND_DATA = 5;
    public final static int TYPE_UNDERSTAND_START_LISTEN = 6;
    public final static int TYPE_UNDERSTAND_LISTEN_STATUS = 7;
    public final static int TYPE_UNDERSTAND_STOP_LISTEN = 8;
    public final static int TYPE_UNDERSTAND_CANCEL_LISTEN = 9;
    public final static int TYPE_SYNTHESISE_START_SPEAK = 10;
    public final static int TYPE_SYNTHESISE_STOP_SPEAK = 11;
    public final static int TYPE_SYNTHESISE_PAUSE_SPEAK = 12;
    public final static int TYPE_SYNTHESISE_RESUME_SPEAK = 13;
    public final static int TYPE_SYNTHESISE_SPEAK_STATUS = 14;

    public int type;
    public String uuid;

    public byte[] buffer;
    public int timeStamp;
    public HashMap<String, String> parameters;
    public String text;

    private DataTransactor sender;

    public RemoteSpeechRequest() {

    }

    public static RemoteSpeechRequest obtain() {
        return new RemoteSpeechRequest();
    }

    public static RemoteSpeechRequest obtain(DataTransactor sender) {
        RemoteSpeechRequest request = obtain();

        request.sender = sender;

        return request;
    }

    public void sendToRemote() {
        sender.send(this);
    }

    public void setSender(DataTransactor sender) {
        this.sender = sender;
    }

    public DataTransactor getSender() {
        return sender;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeString(this.uuid);
        dest.writeByteArray(this.buffer);
        dest.writeMap(this.parameters);
        dest.writeString(this.text);
        dest.writeInt(this.timeStamp);
    }

    public static final Creator<RemoteSpeechRequest> CREATOR = new Creator<RemoteSpeechRequest>() {

        @Override
        public RemoteSpeechRequest createFromParcel(Parcel source) {
            RemoteSpeechRequest request = new RemoteSpeechRequest();

            request.type = source.readInt();
            request.uuid = source.readString();
            request.buffer = source.createByteArray();
            request.parameters = source.readHashMap(null);
            request.text = source.readString();
            request.timeStamp = source.readInt();

            return request;
        }

        @Override
        public RemoteSpeechRequest[] newArray(int size) {
            return new RemoteSpeechRequest[size];
        }

    };
}
