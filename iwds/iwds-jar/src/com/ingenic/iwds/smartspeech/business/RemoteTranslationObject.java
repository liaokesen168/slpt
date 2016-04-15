/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  Zhouzhiqiang <zhiqiang.zhou@ingenic.com>
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

package com.ingenic.iwds.smartspeech.business;

import android.os.Parcel;

/**
 * translation的object抽象，表示翻译业务信息类
 */
public class RemoteTranslationObject extends RemoteBusinessObject {
    /**
     * RemoteTranslationobject的object标识
     */
    public static String sFocus = "translation";

    /**
     * original元素节点属性名，表示原文的语言类型
     */
    public static final String RAWORIGINALLANG = "lang";

    /**
     * translated元素节点属性名，表示译文的语言类型
     */
    public static final String RAWTRANSLATEDLANG = "lang";

    /**
     * original元素节点名，表示原文
     */
    public static final String RAWORIGINAL = "original";

    /**
     * translated元素节点名，表示译文
     */
    public static final String RAWTRANSLATED = "translated";

    /**
     * engine_type元素节点名，表示引擎类型
     */
    public static final String RAWENGINETYPE = "engine_type";

    /**
     * engine_name元素节点名，表示引擎的名称
     */
    public static final String RAWENGINENAME = "engine_name";

    /**
     * original元素节点属性的值
     */
    public String mOriginalLang = null;

    /**
     * translated元素节点属性的值
     */
    public String mTranslatedLang = null;

    /**
     * original元素节点的值
     */
    public String mOriginal = null;

    /**
     * translated元素节点的值
     */
    public String mTranslated = null;

    /**
     * engine_type元素节点的值
     */
    public String mEngineType = null;

    /**
     * engine_name元素节点的值
     */
    public String mEngineName = null;

    @Override
    public String toString() {
        return "Translationobject [original_lang=" + mOriginalLang
                + ", translated_lang=" + mTranslatedLang + ", original="
                + mOriginal + ", translated=" + mTranslated + ", engine_type="
                + mEngineType + ", engine_name=" + mEngineName + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(mOriginalLang);
        arg0.writeString(mTranslatedLang);
        arg0.writeString(mOriginal);
        arg0.writeString(mTranslated);
        arg0.writeString(mEngineType);
        arg0.writeString(mEngineName);
    }

    public static final Creator<RemoteTranslationObject> CREATOR = new Creator<RemoteTranslationObject>() {
        @Override
        public RemoteTranslationObject createFromParcel(Parcel source) {
            RemoteTranslationObject info = new RemoteTranslationObject();
            info.mOriginalLang = source.readString();
            info.mTranslatedLang = source.readString();
            info.mOriginal = source.readString();
            info.mTranslated = source.readString();
            info.mEngineType = source.readString();
            info.mEngineName = source.readString();
            return info;
        }

        @Override
        public RemoteTranslationObject[] newArray(int size) {
            return new RemoteTranslationObject[size];
        }
    };

}
