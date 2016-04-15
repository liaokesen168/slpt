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

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.ingenic.iwds.utils.IwdsLog;

public class NotificationProxyService extends Service {
    private NotificationProxyServiceStub m_service = new NotificationProxyServiceStub();

    @Override
    public IBinder onBind(Intent intent) {
        return m_service;
    }

    public class NotificationProxyServiceStub extends
            INotificationProxyService.Stub {
        private BackendList m_backendList;
        private HashMap<String, String> m_packageToUuid;

        private class BackendList {
            private ArrayList<Callback> m_callbacks;

            private final class Callback implements IBinder.DeathRecipient {
                INotificationServiceBackend m_backend;
                String m_packageName;

                Callback(INotificationServiceBackend backend, String packageName) {
                    m_backend = backend;
                    m_packageName = packageName;
                }

                public void binderDied() {
                    IwdsLog.e("NotificationProxyService", "Backend died: "
                            + m_packageName);

                    synchronized (m_backend) {
                        m_callbacks.remove(this);
                    }
                }
            }

            public BackendList() {
                m_callbacks = new ArrayList<Callback>();
            }

            public INotificationServiceBackend getBackend(String packageName) {
                for (Callback callback : m_callbacks) {
                    if (callback.m_packageName.equals(packageName))
                        return callback.m_backend;
                }

                return null;
            }

            public boolean register(INotificationServiceBackend backend,
                    String packageName) {
                if (getBackend(packageName) != null)
                    return false;

                IBinder binder = backend.asBinder();

                try {
                    Callback cb = new Callback(backend, packageName);
                    binder.linkToDeath(cb, 0);

                    m_callbacks.add(cb);

                    return true;

                } catch (RemoteException e) {
                    return false;
                }
            }

            public boolean unregister(INotificationServiceBackend backend) {
                Callback cb = null;
                for (Callback callback : m_callbacks) {
                    if (callback.m_backend.equals(backend)) {
                        cb = callback;

                        break;
                    }
                }

                if (cb == null)
                    return false;

                m_callbacks.remove(cb);
                cb.m_backend.asBinder().unlinkToDeath(cb, 0);

                return true;
            }

            public int beginBroadcast() {
                return m_callbacks.size();
            }

            public void finishBroadcast() {

            }

            public INotificationServiceBackend getBroadcastItem(int index) {
                return m_callbacks.get(index).m_backend;
            }
        }

        public NotificationProxyServiceStub() {
            synchronized (this) {
                m_backendList = new BackendList();
                m_packageToUuid = new HashMap<String, String>();

                /*
                 * Register com.ingenic.launcher of Elf project
                 */
                m_packageToUuid.put("com.ingenic.launcher",
                        "9207c288-dd9f-8fdd-0b88-3ea582bbbeb2");

                /*
                 * Register com.ingenic.iwds.test.notificationproxyservice.backend
                 */
                m_packageToUuid
                        .put("com.ingenic.iwds.test.notificationproxyservice.backend",
                                "396bdc12-b834-bc70-f12c-1196ce75f99c");

                /*
                 * Register com.acmeasy.launcher of wear day
                 */
                m_packageToUuid
                        .put("com.acmeasy.launcher",
                                "396bdc12-b834-bc70-f12c-1196ce75f99d");
            }
        }

        @Override
        public void cancel(String packageName, int id) throws RemoteException {
            synchronized (this) {
                int N = m_backendList.beginBroadcast();

                for (int i = 0; i < N; i++) {
                    try {
                        m_backendList.getBroadcastItem(i).onCancelNotification(
                                packageName, id);

                    } catch (RemoteException e) {
                        /*
                         * ignore
                         */
                    }
                }

                m_backendList.finishBroadcast();
            }
        }

        @Override
        public void cancelAll(String packageName) throws RemoteException {
            synchronized (this) {
                int N = m_backendList.beginBroadcast();

                for (int i = 0; i < N; i++) {
                    try {
                        m_backendList.getBroadcastItem(i)
                                .onCancelAllNotification(packageName);

                    } catch (RemoteException e) {
                        /*
                         * ignore
                         */
                    }
                }

                m_backendList.finishBroadcast();
            }
        }

        @Override
        public boolean notify(String packageName, int id, Note note)
                throws RemoteException {
            synchronized (this) {
                int N = m_backendList.beginBroadcast();

                for (int i = 0; i < N; i++) {
                    try {
                        m_backendList.getBroadcastItem(i).onHandleNotification(
                                packageName, id, note);

                    } catch (RemoteException e) {
                        /*
                         * ignore
                         */
                    }
                }

                m_backendList.finishBroadcast();

                return true;
            }
        }

        @Override
        public boolean registerBackend(INotificationServiceBackend backend,
                String packageName, String uuid) throws RemoteException {
            synchronized (this) {
                String id = m_packageToUuid.get(packageName);
                if (id == null || !id.equals(uuid)) {
                    IwdsLog.e(this, "Unqualified applicant: " + packageName);

                    return false;
                }

                if (m_backendList.getBackend(packageName) != null) {
                    IwdsLog.e(this, "Already registered applicant: "
                            + packageName);

                    return false;
                }

                return m_backendList.register(backend, packageName);
            }
        }

        @Override
        public boolean unregisterBackend(String packageName, String uuid)
                throws RemoteException {
            synchronized (this) {
                String id = m_packageToUuid.get(packageName);
                if (id == null || !id.equals(uuid)) {
                    IwdsLog.e(this, "Unqualified applicant: " + packageName);

                    return false;
                }

                INotificationServiceBackend backend = m_backendList
                        .getBackend(packageName);
                if (backend == null) {
                    IwdsLog.e(this, "No such backend: " + packageName);

                    return false;
                }

                return m_backendList.unregister(backend);
            }
        }
    }
}
