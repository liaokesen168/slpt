/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  ZhangYanMing <yamming.zhang@ingenic.com, jamincheung@126.com>
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
 */

package com.ingenic.iwds.smartspeech;

import com.ingenic.iwds.smartspeech.IRemoteStatusCallback;
import com.ingenic.iwds.smartspeech.IRemoteRecognizerCallback;
import com.ingenic.iwds.smartspeech.IRemoteUnderstanderCallback;
import com.ingenic.iwds.smartspeech.IRemoteSynthesizerCallback;

import java.util.Map;

interface IRemoteSpeechService {
    void registerRemoteStatusListener(String uuid, IRemoteStatusCallback callback);
    void unregisterRemoteStatusListener(String uuid);

    void requestStartRecognize(IRemoteRecognizerCallback callback, in Map parameters);
    void requestStopRecognize(IRemoteRecognizerCallback callback);
    void requestCancelRecognize(IRemoteRecognizerCallback callback);
    void requestRecognizeStatus(IRemoteRecognizerCallback callback);

    void requestStartUnderstand(IRemoteUnderstanderCallback callback, in Map parameters);
    void requestStopUnderstand(IRemoteUnderstanderCallback callback);
    void requestCancelUnderstand(IRemoteUnderstanderCallback callback);
    void requestUnderstandStatus(IRemoteUnderstanderCallback callback);

    void requestStartSpeak(in Map parameters, String text, IRemoteSynthesizerCallback callback);
    void requestCancelSpeak(IRemoteSynthesizerCallback callback);
    void requestPauseSpeak(IRemoteSynthesizerCallback callback);
    void requestResumeSpeak(IRemoteSynthesizerCallback callback);
    void requestSpeakStatus(IRemoteSynthesizerCallback callback);
}