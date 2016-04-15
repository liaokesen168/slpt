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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 音乐控制信息类.
 */
public class MusicControlInfo implements Parcelable {

    /**
     * 音乐控制指令的枚举.
     */
    public enum COMMAND {
        /** 开始播放. */
        START,
        /** 暂停. */
        PAUSE,
        /** 下一首. */
        NEXT,
        /** 上一首. */
        PREV,
    }

    /**
     * 音乐控制指令
     */
    public int cmd = -99;

    /** 音乐专辑,默认返回null */
    public String musicAlbum = null;

    /** 音乐的演唱者,默认返回null. */
    public String musicArtist = null;

    /** 歌曲名称，默认返回null. */
    public String songName = null;

    /** 最大音量，默认为15. */
    public int volumeMax = 15;

    /** 当前音量，默认为5. */
    public int volumeCurrent = 5;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(cmd);

        dest.writeString(musicAlbum);
        dest.writeString(musicArtist);
        dest.writeString(songName);
        dest.writeInt(volumeMax);
        dest.writeInt(volumeCurrent);
    }

    public static final Creator<MusicControlInfo> CREATOR = new Creator<MusicControlInfo>() {
        @Override
        public MusicControlInfo createFromParcel(Parcel source) {
            MusicControlInfo info = new MusicControlInfo();

            info.cmd = source.readInt();

            info.musicAlbum = source.readString();
            info.musicArtist = source.readString();
            info.songName = source.readString();
            info.volumeMax = source.readInt();
            info.volumeCurrent = source.readInt();

            return info;
        }

        @Override
        public MusicControlInfo[] newArray(int size) {
            return new MusicControlInfo[size];
        }
    };

    @Override
    public String toString() {
        return "MusicControlInfo [cmd=" + cmd + ", musicAlbum=" + musicAlbum
                + ", musicArtist=" + musicArtist + ", songName=" + songName
                + "]";
    }
}
