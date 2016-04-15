/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  SunWenZhong(Fighter) <wzsun@ingenic.com, wanmyqawdr@126.com>
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

import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

import com.ingenic.iwds.app.Note;

/**
 * 通知后台服务。本服务用于对所有应用的通知进行监控
 */
public class NotificationServiceBackend {
    private Handler m_handler;

    private static final int MSG_HANDLE = 19;
    private static final int MSG_CANCEL = 87;
    private static final int MSG_CANCEL_ALL = 3;

    /**
     * 通知被显示时回调
     * @param  packageName 通知所在应用的包名
     * @param  id          通知的id
     * @param  note        通知的内容
     * @return
     */
    public boolean onHandle(String packageName, int id, Note note) {
        return true;
    }

    /**
     * 通知被取消时回调
     * @param packageName 通知所在应用的包名
     * @param id          通知的id
     */
    public void onCancel(String packageName, int id) {

    }

    /**
     * 某应用的所有通知被取消时回调
     * @param packageName 通知所在应用的包名
     */
    public void onCancelAll(String packageName) {

    }

    private class NoteWrapper {
        public NoteWrapper(String PackageName, int Id, Note Note) {
            packageName = PackageName;
            id = Id;
            note = Note;
        }

        public String packageName;
        public int id;
        public Note note;
    }

    public NotificationServiceBackend() {
        m_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_HANDLE: {
                    NoteWrapper wrapper = (NoteWrapper) msg.obj;
                    onHandle(wrapper.packageName, wrapper.id, wrapper.note);

                    break;
                }

                case MSG_CANCEL: {
                    NoteWrapper wrapper = (NoteWrapper) msg.obj;
                    onCancel(wrapper.packageName, wrapper.id);

                    break;
                }

                case MSG_CANCEL_ALL: {
                    NoteWrapper wrapper = (NoteWrapper) msg.obj;
                    onCancelAll(wrapper.packageName);

                    break;
                }

                }
            }
        };
    }

    INotificationServiceBackend callback = new INotificationServiceBackend.Stub() {
        @Override
        public boolean onHandleNotification(String packageName, int id, Note note)
                throws RemoteException {
            Message.obtain(m_handler, MSG_HANDLE,
                    new NoteWrapper(packageName, id, note)).sendToTarget();

            return true;
        }

        @Override
        public void onCancelNotification(String packageName, int id)
                throws RemoteException {
            Message.obtain(m_handler, MSG_CANCEL,
                    new NoteWrapper(packageName, id, null)).sendToTarget();
        }

        @Override
        public void onCancelAllNotification(String packageName)
                throws RemoteException {
            Message.obtain(m_handler, MSG_CANCEL_ALL,
                    new NoteWrapper(packageName, 0, null)).sendToTarget();
        }
    };
}
