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

package com.ingenic.iwds.smartsense;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback;
import com.ingenic.iwds.datatransactor.ParcelTransactor;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

public class RemoteSensorService extends Service implements
        DataTransactorCallback {

    private RemoteSensorServiceStub m_service = new RemoteSensorServiceStub();
    private ParcelTransactor<RemoteSensorResponse> m_transactor;
    private ServiceHandler m_handler;

    @Override
    public void onCreate() {
        IwdsLog.d(this, "onCreate");
        super.onCreate();

        m_transactor = new ParcelTransactor<RemoteSensorResponse>(this,
                RemoteSensorResponse.CREATOR, this,
                "c1dc19e2-17a4-0797-0000-68a0dd4bfb68");

        m_handler = new ServiceHandler();
    }

    @Override
    public void onDestroy() {
        IwdsLog.d(this, "onDestroy");

        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        IwdsLog.d(this, "onUnbind");

        m_transactor.stop();

        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        IwdsLog.d(this, "onBind");

        m_transactor.start();

        return m_service;
    }

    private class RemoteSensorServiceStub extends IRemoteSensorService.Stub {
        private ArrayList<RemoteSensorEventCallback> m_callbacks;
        private RemoteSensorCallback m_remoteSensorCallback;
        private ArrayList<Sensor> m_sensorList;
        private Object m_sensorListLock;

        private class RemoteSensorCallback implements IBinder.DeathRecipient {

            private HashMap<String, IRemoteSensorCallback> m_listeners;

            RemoteSensorCallback() {
                m_listeners = new HashMap<String, IRemoteSensorCallback>();
            }

            public void onSensorAvailable(ArrayList<Sensor> sensorList) {
                synchronized (m_listeners) {
                    Collection<IRemoteSensorCallback> callbacks = m_listeners
                            .values();

                    for (IRemoteSensorCallback cb : callbacks) {
                        try {
                            cb.onSensorAvailable(sensorList);
                        } catch (RemoteException e) {
                            IwdsLog.e(this, "Exception in onSensorAvailable: "
                                    + e.toString());
                        }
                    }
                }
            }

            public boolean registerRemoteSensorListener(String uuid,
                    IRemoteSensorCallback callback) {
                synchronized (m_listeners) {
                    try {
                        callback.asBinder().linkToDeath(this, 0);
                        m_listeners.put(uuid, callback);

                        return true;

                    } catch (RemoteException e) {
                        IwdsLog.e(this,
                                "Exception in registerRemoteSensorListener: "
                                        + e.toString());

                        return false;
                    }
                }
            }

            public void unregisterRemoteSensorListener(String uuid) {
                synchronized (m_listeners) {
                    IRemoteSensorCallback callback = m_listeners.remove(uuid);
                    if (callback == null)
                        return;

                    callback.asBinder().unlinkToDeath(this, 0);
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
                }
            }
        }

        private class RemoteSensorEventCallback implements
                IBinder.DeathRecipient {
            Sensor sensor;
            private HashMap<String, IRemoteSensorEventCallback> m_listeners;

            RemoteSensorEventCallback(Sensor s) {
                sensor = s;
                m_listeners = new HashMap<String, IRemoteSensorEventCallback>();
            }

            public boolean registerListener(String uuid,
                    IRemoteSensorEventCallback callback, int rate) {
                synchronized (m_listeners) {
                    try {
                        callback.asBinder().linkToDeath(this, 0);
                        m_listeners.put(uuid, callback);

                        if (m_listeners.size() == 1)
                            m_handler.requestRegisterListener(sensor, rate);

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

            public void onSensorEvent(SensorEvent event) {
                synchronized (m_listeners) {
                    Collection<IRemoteSensorEventCallback> callbacks = m_listeners
                            .values();
                    for (IRemoteSensorEventCallback cb : callbacks) {
                        try {
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

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                synchronized (m_listeners) {
                    Collection<IRemoteSensorEventCallback> callbacks = m_listeners
                            .values();
                    for (IRemoteSensorEventCallback cb : callbacks) {
                        try {
                            cb.onAccuracyChanged(sensor, accuracy);
                        } catch (RemoteException e) {
                            IwdsLog.e(this, "Exception in onAccuracyChanged: "
                                    + e.toString());
                        }
                    }
                }
            }

            public void unregisterListener(String uuid) {
                synchronized (m_listeners) {
                    IRemoteSensorEventCallback callback = m_listeners
                            .remove(uuid);
                    if (callback == null)
                        return;

                    callback.asBinder().unlinkToDeath(this, 0);

                    if (m_listeners.size() != 0)
                        return;

                    m_handler.requestUnregisterListener(sensor);
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
                        m_handler.requestUnregisterListener(sensor);
                }
            }
        }

        public RemoteSensorServiceStub() {
            m_sensorListLock = new Object();
            m_remoteSensorCallback = new RemoteSensorCallback();
            m_callbacks = new ArrayList<RemoteSensorEventCallback>();
        }

        public void onSensorAvailable(ArrayList<Sensor> sensorList) {
            synchronized (m_sensorListLock) {
                if (m_sensorList == null && sensorList != null) {
                    int N = sensorList.size();
                    for (int i = 0; i < N; i++) {
                        RemoteSensorEventCallback cb = new RemoteSensorEventCallback(
                                sensorList.get(i));
                        m_callbacks.add(cb);
                    }

                } else if (sensorList == null) {
                    if (!m_callbacks.isEmpty())
                        m_callbacks.clear();
                }

                m_sensorList = sensorList;

                m_remoteSensorCallback.onSensorAvailable(m_sensorList);
            }
        }

        public void onRemoteSensorEvent(SensorEvent event) {
            synchronized (m_callbacks) {
                for (RemoteSensorEventCallback cb : m_callbacks)
                    if (cb.sensor.getType() == event.sensorType)
                        cb.onSensorEvent(event);
            }
        }

        public void onRemoteSensorAccuracyChanged(Sensor sensor, int accuracy) {
            synchronized (m_callbacks) {
                for (RemoteSensorEventCallback cb : m_callbacks)
                    if (cb.sensor.getType() == sensor.getType())
                        cb.onAccuracyChanged(sensor, accuracy);
            }
        }

        @Override
        public Sensor getDefaultSensor(int sensorType) throws RemoteException {
            synchronized (m_sensorListLock) {
                if (m_sensorList == null)
                    return null;

                for (Sensor sensor : m_sensorList) {
                    if (sensor.getType() == sensorType)
                        return sensor;
                }

                return null;
            }
        }

        @Override
        public List<Sensor> getSensorList() throws RemoteException {
            synchronized (m_sensorListLock) {
                return m_sensorList;
            }
        }

        @Override
        public boolean registerListener(String uuid,
                IRemoteSensorEventCallback callback, Sensor sensor, int rate)
                throws RemoteException {
            synchronized (m_callbacks) {
                for (RemoteSensorEventCallback cb : m_callbacks)
                    if (cb.sensor.getType() == sensor.getType()) {
                        cb.registerListener(uuid, callback, rate);

                        return true;
                    }

                return false;
            }
        }

        @Override
        public void unregisterListener(String uuid) throws RemoteException {
            synchronized (m_callbacks) {
                for (RemoteSensorEventCallback cb : m_callbacks)
                    cb.unregisterListener(uuid);
            }
        }

        @Override
        public boolean registerRemoteSensorListener(String uuid,
                IRemoteSensorCallback callback) throws RemoteException {
            boolean result = false;

            synchronized (m_remoteSensorCallback) {
                result = m_remoteSensorCallback.registerRemoteSensorListener(
                        uuid, callback);
            }

            // do cold boot
            m_handler.requestSensorList();

            return result;
        }

        @Override
        public void unregisterRemoteSensorListener(String uuid)
                throws RemoteException {
            synchronized (m_remoteSensorCallback) {
                m_remoteSensorCallback.unregisterRemoteSensorListener(uuid);
            }
        }
    }

    private class ServiceHandler extends Handler {
        private final static int MSG_RESPONSE_SENSOR_LIST = 0;
        private final static int MSG_RESPONSE_SENSOR_SERVICE_CONNECTED = 1;
        private final static int MSG_RESPONSE_SENSOR_CHANGED = 2;
        private final static int MSG_RESPONSE_SENSOR_ACCURACY_CHANGED = 3;
        private final static int MSG_REQUEST_SENSOR_LIST = 4;
        private final static int MSG_REQUEST_REGISTER_LISTENER = 5;
        private final static int MSG_REQUEST_UNREGISTER_LISTENER = 6;

        private boolean m_isRequestingSensorList;
        private boolean m_remoteServiceConnected;

        public void requestSensorList() {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_SENSOR_LIST;

            msg.sendToTarget();
        }

        public void requestRegisterListener(Sensor sensor, int sensorRate) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_REGISTER_LISTENER;
            msg.arg1 = sensorRate;
            msg.obj = sensor;

            msg.sendToTarget();
        }

        public void requestUnregisterListener(Sensor sensor) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_REQUEST_UNREGISTER_LISTENER;
            msg.obj = sensor;

            msg.sendToTarget();
        }

        public void setRemoteServiceState(boolean connected) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_RESPONSE_SENSOR_SERVICE_CONNECTED;
            msg.arg1 = connected == true ? 1 : 0;

            msg.sendToTarget();
        }

        public void setSensorList(ArrayList<Sensor> sensorList) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_RESPONSE_SENSOR_LIST;
            msg.obj = sensorList;

            msg.sendToTarget();
        }

        public void handleResponse(RemoteSensorResponse response) {
            final Message msg = Message.obtain(this);

            switch (response.type) {
            case RemoteSensorResponse.TYPE_SENSOR_LIST:
                msg.what = MSG_RESPONSE_SENSOR_LIST;
                msg.obj = response.sensorList;
                break;

            case RemoteSensorResponse.TYPE_SENSEOR_SERVICE_CONNECTED:
                msg.what = MSG_RESPONSE_SENSOR_SERVICE_CONNECTED;
                msg.arg1 = response.result;
                break;

            case RemoteSensorResponse.TYPE_SENSOR_CHANGED:
                msg.what = MSG_RESPONSE_SENSOR_CHANGED;
                msg.obj = response.sensorEvent;
                break;

            case RemoteSensorResponse.TYPE_SENSOR_ACCURACY_CHANGED:
                msg.what = MSG_RESPONSE_SENSOR_ACCURACY_CHANGED;
                msg.arg1 = response.accuracy;
                msg.obj = response.sensor;
                break;

            default:
                IwdsAssert.dieIf(this, true, "Unsupported remote response");
                return;
            }

            msg.sendToTarget();
        }

        @Override
        public void handleMessage(Message msg) {

            RemoteSensorRequest request = RemoteSensorRequest
                    .obtain(m_transactor);

            switch (msg.what) {
            case MSG_RESPONSE_SENSOR_LIST:
                m_isRequestingSensorList = false;

                ArrayList<Sensor> sensorList = (ArrayList<Sensor>) msg.obj;
                m_service.onSensorAvailable(sensorList);

                break;

            case MSG_RESPONSE_SENSOR_SERVICE_CONNECTED:
                boolean connected = msg.arg1 == 1 ? true : false;

                if (connected) {
                    IwdsLog.i(this, "Sensor service on remote device connected");
                    m_remoteServiceConnected = true;
                    requestSensorList();

                } else {
                    IwdsLog.i(this,
                            "Sensor service on remote device disconnected");
                    m_remoteServiceConnected = false;
                }

                break;

            case MSG_RESPONSE_SENSOR_CHANGED:
                SensorEvent event = (SensorEvent) msg.obj;

                m_service.onRemoteSensorEvent(event);

                break;

            case MSG_RESPONSE_SENSOR_ACCURACY_CHANGED:
                Sensor sensor = (Sensor) msg.obj;
                int accuracy = msg.arg1;

                m_service.onRemoteSensorAccuracyChanged(sensor, accuracy);

                break;

            case MSG_REQUEST_SENSOR_LIST:
                if (!m_remoteServiceConnected) {
                    IwdsLog.w(this,
                            "Sensor service on remote device not connected yet");
                    setSensorList(null);
                    return;

                } else if (m_remoteServiceConnected && m_isRequestingSensorList) {
                    IwdsLog.w(this, "Already requesting remote sensor list,"
                            + " waiting...");
                    return;
                }

                m_isRequestingSensorList = true;

                IwdsLog.i(this, "Try to request remote device sensor list");

                request.type = RemoteSensorRequest.TYPE_SENSOR_LIST;

                request.sendToRemote();

                break;

            case MSG_REQUEST_REGISTER_LISTENER:
                int sensorRate = msg.arg1;
                Sensor sensorToReg = (Sensor) msg.obj;

                IwdsLog.i(this,
                        "Try to request register remote senor listener: "
                                + sensorToReg.toString());

                request.type = RemoteSensorRequest.TYPE_REGISTER_LISTENER;
                request.sensor = sensorToReg;
                request.sensorRate = sensorRate;

                request.sendToRemote();

                break;

            case MSG_REQUEST_UNREGISTER_LISTENER:
                Sensor sensorToUnreg = (Sensor) msg.obj;

                IwdsLog.i(this,
                        "Try to request unregister remote senor listener: "
                                + sensorToUnreg.toString());

                request.type = RemoteSensorRequest.TYPE_UNREGISTER_LISTENER;
                request.sensor = sensorToUnreg;

                request.sendToRemote();

                break;
            }
        }
    }

    /* ---------------------- DataTransactorCallback -------------------------- */
    @Override
    public void onLinkConnected(DeviceDescriptor descriptor, boolean isConnected) {
        // do not care
    }

    @Override
    public void onChannelAvailable(boolean isAvailable) {
        if (!isAvailable && m_service != null) {
            m_handler.setSensorList(null);
            m_handler.setRemoteServiceState(false);
        }
    }

    @Override
    public void onSendResult(DataTransactResult result) {
        // do not care
    }

    @Override
    public void onDataArrived(Object object) {
        if (object instanceof RemoteSensorResponse)
            m_handler.handleResponse((RemoteSensorResponse) object);
    }

    @Override
    public void onSendFileProgress(int progress) {

    }

    @Override
    public void onRecvFileProgress(int progress) {

    }

    @Override
    public void onSendFileInterrupted(int index) {

    }

    @Override
    public void onRecvFileInterrupted(int index) {

    }
    /* ------------------- DataTransactorCallback end---------------------- */


}
