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
 */

package com.ingenic.iwds.smartsense;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.ingenic.iwds.utils.IwdsLog;

public class SensorService extends Service {
    private SensorServiceStub m_service = new SensorServiceStub();

    @Override
    public IBinder onBind(Intent intent) {
        IwdsLog.d(this, "onBind()");

        return m_service;
    }

    private static class SensorServiceStub extends ISensorService.Stub {
        private ArrayList<SensorEventCallback> m_callbacks;
        private ArrayList<Sensor> m_sensorList;

        private class SensorEventCallback implements IBinder.DeathRecipient {
            Sensor sensor;
            private HashMap<String, ISensorEventCallback> m_listeners;

            SensorEventCallback(Sensor s) {
                sensor = s;
                m_listeners = new HashMap<String, ISensorEventCallback>();
            }

            public boolean registerListener(String uuid,
                    ISensorEventCallback callback) {
                synchronized (m_listeners) {
                    try {
                        callback.asBinder().linkToDeath(this, 0);
                        m_listeners.put(uuid, callback);

                        if (m_listeners.size() == 1)
                            setActive(sensor.getType(), true);

                        return true;

                    } catch (RemoteException e) {
                        IwdsLog.e(
                                this,
                                "Exception in registerListener: "
                                        + e.toString());

                        return false;
                    }
                }
            }

            public void unregisterListener(String uuid) {
                synchronized (m_listeners) {
                    ISensorEventCallback callback = m_listeners.remove(uuid);
                    if (callback == null)
                        return;

                    callback.asBinder().unlinkToDeath(this, 0);

                    if (m_listeners.size() != 0)
                        return;

                    setActive(sensor.getType(), false);
                }
            }

            private void onSensorEvent(SensorEvent event) {
                synchronized (m_listeners) {
                    Collection<ISensorEventCallback> callbacks = m_listeners
                            .values();
                    for (ISensorEventCallback cb : callbacks) {
                        try {
                            switch (event.sensorType) {
                            case Sensor.TYPE_HEART_RATE:
                                event.accuracy = (int) event.values[1];
                                if (event.accuracy > 0)
                                    cb.onAccuracyChanged(sensor, event.accuracy);
                                break;

                            default:
                                break;
                            }
                            cb.onSensorChanged(event);
                        } catch (RemoteException e) {
                            IwdsLog.e(
                                    this,
                                    "Exception in onSensorEvent: "
                                            + e.toString());
                        }
                    }
                }
            }

            @Override
            public void binderDied() {
                synchronized (m_listeners) {
                    Set<String> uuids = m_listeners.keySet();

                    for (Iterator<String> it = uuids.iterator(); it.hasNext();) {
                        String uuid = it.next();

                        if (!m_listeners.get(uuid).asBinder().isBinderAlive())
                            it.remove();
                    }

                    if (m_listeners.size() == 0)
                        setActive(sensor.getType(), false);
                }
            }
        }

        public SensorServiceStub() {
            m_callbacks = new ArrayList<SensorEventCallback>();
            m_sensorList = new ArrayList<Sensor>();

            int N = getSensorCount();
            for (int i = 0; i < N; i++) {
                Sensor sensor = new Sensor();

                forEachSensor(sensor, i);

                SensorEventCallback cb = new SensorEventCallback(sensor);
                installSensorEventCallback(cb.sensor.getType(), cb);
                m_callbacks.add(cb);

                m_sensorList.add(sensor);
            }
        }

        @Override
        public ArrayList<Sensor> getSensorList() throws RemoteException {
            return m_sensorList;
        }

        @Override
        public Sensor getDefaultSensor(int sensorType) throws RemoteException {
            synchronized (m_callbacks) {
                for (SensorEventCallback cb : m_callbacks) {
                    if (cb.sensor.getType() == sensorType)
                        return cb.sensor;
                }

                return null;
            }
        }

        @Override
        public boolean registerListener(String uuid,
                ISensorEventCallback callback, Sensor sensor, int rate)
                throws RemoteException {
            synchronized (m_callbacks) {
                for (SensorEventCallback cb : m_callbacks)
                    if (cb.sensor.getType() == sensor.getType()) {
                        cb.registerListener(uuid, callback);

                        return true;
                    }

                return false;
            }
        }

        @Override
        public void unregisterListener(String uuid) throws RemoteException {
            synchronized (m_callbacks) {
                for (SensorEventCallback cb : m_callbacks)
                    cb.unregisterListener(uuid);
            }
        }

        private static native final int getSensorCount();

        private static native final void forEachSensor(Sensor sensor, int index);

        private static native final void installSensorEventCallback(
                int sensorType, SensorEventCallback callback);

        private static native final boolean setActive(int sensorType,
                boolean enable);

        private static native final boolean isActive(int sensorType);

        private static native final boolean setWearOnRightHand(boolean isRightHand);
    }

    public static boolean setWearOnRightHand(boolean isOnRightHand) {
        return SensorServiceStub.setWearOnRightHand(isOnRightHand);
    }
}
