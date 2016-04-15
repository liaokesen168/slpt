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

package com.ingenic.iwds.app;

import com.ingenic.iwds.utils.IwdsAssert;

import android.app.PendingIntent;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 通知内容，目前只支持文字，以后会支持更多形式。
 * @see com.ingenic.iwds.app.NotificationProxyServiceManager
 */
public class Note implements Parcelable {
    public Note(String Title, String Content) {
        IwdsAssert.dieIf(this, Title == null, "Title is null.");
        IwdsAssert.dieIf(this, Content == null, "Content is null.");

        title = Title;
        content = Content;
    }

    /**
     * 构造 {@code Note} 对象
     * @param  Title   标题
     * @param  Content 内容
     * @param  intent  当通知被点击时执行的 intent
     */
    public Note(String Title, String Content, PendingIntent intent) {
        this(Title, Content);

        pendingIntent = intent;
    }

    private Note() {

    }

    /**
     * 通知的标题
     */
    public String title;

    /**
     * 通知的内容
     */
    public String content;

    /**
     * 通知被点击时执行的 intent
     */
    public PendingIntent pendingIntent;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(content);

        if (pendingIntent == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            pendingIntent.writeToParcel(dest, flags);
        }
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel source) {
            Note note = new Note();

            note.title = source.readString();
            note.content = source.readString();

            if (source.readInt() != 0)
                note.pendingIntent = PendingIntent.CREATOR
                        .createFromParcel(source);

            return note;
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    @Override
    public String toString() {
        return "Note [title=" + title + ", content=" + content
                + ", pendingIntent=" + pendingIntent + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((content == null) ? 0 : content.hashCode());
        result = prime * result
                + ((pendingIntent == null) ? 0 : pendingIntent.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Note other = (Note) obj;
        if (content == null) {
            if (other.content != null)
                return false;
        } else if (!content.equals(other.content))
            return false;
        if (pendingIntent == null) {
            if (other.pendingIntent != null)
                return false;
        } else if (!pendingIntent.equals(other.pendingIntent))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        return true;
    }
}
