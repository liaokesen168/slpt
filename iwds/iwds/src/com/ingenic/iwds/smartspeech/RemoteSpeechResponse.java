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

import com.ingenic.iwds.datatransactor.DataTransactor;
import com.ingenic.iwds.smartspeech.business.RemoteBusiness;

import android.os.Parcel;
import android.os.Parcelable;

public class RemoteSpeechResponse implements Parcelable {
    public final static int TYPE_SPEECH_SERVICE_STATUS = 0;
    public final static int TYPE_RECOGNIZE_VOLUME_CHANGED = 1;
    public final static int TYPE_RECOGNIZE_BEGIN_SPEECH = 2;
    public final static int TYPE_RECOGNIZE_END_SPEECH = 3;
    public final static int TYPE_RECOGNIZE_RESULT = 4;
    public final static int TYPE_RECOGNIZE_ERROR = 5;
    public final static int TYPE_RECOGNIZE_STATUS = 6;
    public final static int TYPE_UNDRESTAND_VOLUME_CHANGED = 7;
    public final static int TYPE_UNDERSTAND_BEGIN_SPEECH = 8;
    public final static int TYPE_UNDERSTAND_END_SPEECH = 9;
    public final static int TYPE_UNDERSTAND_RESULT = 10;
    public final static int TYPE_UNDERSTAND_ERROR = 11;
    public final static int TYPE_UNDERSTAND_STATUS = 12;
    public final static int TYPE_SYNTHESISE_BUFFER_PROGRESS_CHANGED = 13;
    public final static int TYPE_SYNTHESISE_COMPLETE = 14;
    public final static int TYPE_SYNTHESISE_SPEAK_BEGIN = 15;
    public final static int TYPE_SYNTHESISE_SPEAK_PAUSED = 16;
    public final static int TYPE_SYNTHESISE_SPEAK_PROGRESS_CHANGED = 17;
    public final static int TYPE_SYNTHESISE_SPEAK_RESUMED = 18;
    public final static int TYPE_SYNTHESISE_SPEAK_STATUS = 19;
    public final static int TYPE_SYNTHESISE_SPEAK_ERROR = 20;
    public final static int TYPE_SYNTHESISE_SPEAK_DATA = 21;
    public final static int TYPE_LEXICON_UPDATED = 22;
    public final static int TYPE_GRAMMAR_BUILDED = 23;

    public int type;
    public String uuid;

    public int errorCode;

    public String recognizeResult;
    public boolean recognizeLast;
    public int recognizeVolume;
    public boolean recognizeStatus;

    public RemoteBusiness understandResult;
    public int understandVolume;
    public boolean understandStatus;

    public byte[] audioBuffer;
    public int bufferProgress;
    public int speakProgress;
    public boolean speakStatus;

    public int lexiconId;
    public int grammarId;

    private DataTransactor sender;

    public RemoteSpeechResponse() {

    }

    public static RemoteSpeechResponse obtain() {
        return new RemoteSpeechResponse();
    }

    public static RemoteSpeechResponse obtain(DataTransactor sender) {
        RemoteSpeechResponse response = obtain();

        response.sender = sender;

        return response;
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
        dest.writeInt(this.errorCode);
        dest.writeString(this.recognizeResult);
        dest.writeInt(this.recognizeLast ? 1 : 0);
        dest.writeInt(this.recognizeVolume);
        dest.writeInt(this.recognizeStatus ? 1 : 0);
        dest.writeParcelable(this.understandResult, flags);
        dest.writeInt(this.understandVolume);
        dest.writeInt(this.understandStatus ? 1 : 0);
        dest.writeByteArray(this.audioBuffer);
        dest.writeInt(this.speakProgress);
        dest.writeInt(this.bufferProgress);
        dest.writeInt(this.speakStatus ? 1 : 0);
        dest.writeInt(this.lexiconId);
        dest.writeInt(this.grammarId);
    }

    public static final Creator<RemoteSpeechResponse> CREATOR = new Creator<RemoteSpeechResponse>() {

        @Override
        public RemoteSpeechResponse createFromParcel(Parcel source) {
            RemoteSpeechResponse response = new RemoteSpeechResponse();

            response.type = source.readInt();
            response.uuid = source.readString();
            response.errorCode = source.readInt();
            response.recognizeResult = source.readString();
            response.recognizeLast = source.readInt() != 0;
            response.recognizeVolume = source.readInt();
            response.recognizeStatus = source.readInt() != 0;
            response.understandResult = source.readParcelable(RemoteBusiness.class.getClassLoader());
            response.understandVolume = source.readInt();
            response.understandStatus = source.readInt() != 0;
            response.audioBuffer = source.createByteArray();
            response.speakProgress = source.readInt();
            response.bufferProgress = source.readInt();
            response.speakStatus = source.readInt() != 0;
            response.lexiconId = source.readInt();
            response.grammarId = source.readInt();

            return response;
        }

        @Override
        public RemoteSpeechResponse[] newArray(int size) {
            return new RemoteSpeechResponse[size];
        }

    };

}
