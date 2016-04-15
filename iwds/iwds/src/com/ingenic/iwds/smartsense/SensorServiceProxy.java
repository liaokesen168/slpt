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

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceClient.ConnectionCallbacks;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback;
import com.ingenic.iwds.datatransactor.ParcelTransactor;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

public class SensorServiceProxy implements DataTransactorCallback,
        ConnectionCallbacks {

    private Context m_context;
    private ParcelTransactor<RemoteSensorRequest> m_transactor;
    private ServiceClient m_sensorServiceClient;
    private SensorServiceManager m_service;
    private ServiceProxytHandler m_handler;
    private WakeLock m_wakeLock;
    private static SensorServiceProxy sInstance;

    private SensorServiceProxy() {
    }

    public static SensorServiceProxy getInstance() {
        if (sInstance == null)
            sInstance = new SensorServiceProxy();

        return sInstance;
    }

    public void initialize(Context context) {
        IwdsLog.d(this, "initialize");

        m_context = context;

        PowerManager powerManager = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        m_wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                this.getClass().getName());

        m_transactor = new ParcelTransactor<RemoteSensorRequest>(m_context,
                RemoteSensorRequest.CREATOR, this,
                "c1dc19e2-17a4-0797-0000-68a0dd4bfb68");

        m_sensorServiceClient = new ServiceClient(m_context,
                ServiceManagerContext.SERVICE_SENSOR, this);

        m_handler = new ServiceProxytHandler();
    }

    public void start() {
        IwdsLog.i(this, "start");

        m_transactor.start();
    }

    private void wakeLockAcquire() {
        if (!m_wakeLock.isHeld())
            m_wakeLock.acquire();
    }

    private void wakeLockRelease() {
        if (m_wakeLock.isHeld())
            m_wakeLock.release();
    }

    private class ServiceProxytHandler extends Handler {
        private final static int MSG_REQUEST_SENSOR_LIST = 0;
        private final static int MSG_REQUEST_REGISTER_LISTENER = 1;
        private final static int MSG_REQUEST_UNREGISTER_LISTENER = 2;
        private final static int MSG_SENSOR_SERVICE_CONNECTED = 3;
        private final static int MSG_SENSOR_CHANGED = 4;
        private final static int MSG_SENSOR_ACCURACY_CHANGED = 5;
        private final static int MSG_CHANNEL_STATUS_CHANGED = 6;

        private boolean m_serviceConnected;
        private boolean m_channelAvailable;
        private ArrayList<Sensor> m_sensorList = new ArrayList<Sensor>();

        public void setChannelState(boolean available) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_CHANNEL_STATUS_CHANGED;
            msg.arg1 = available ? 1 : 0;

            msg.sendToTarget();
        }

        public void notifySensorServiceConnected(boolean connected) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_SENSOR_SERVICE_CONNECTED;
            msg.arg1 = connected ? 1 : 0;

            msg.sendToTarget();
        }

        public void notifySensorChanged(SensorEvent event) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_SENSOR_CHANGED;
            msg.obj = event;

            msg.sendToTarget();
        }

        public void notifySensorAccuracyChanged(Sensor sensor, int accuracy) {
            final Message msg = Message.obtain(this);

            msg.what = MSG_SENSOR_ACCURACY_CHANGED;
            msg.arg1 = accuracy;
            msg.obj = sensor;

            msg.sendToTarget();
        }

        public void handleRequest(RemoteSensorRequest request) {
            final Message msg = Message.obtain(this);

            switch (request.type) {
            case RemoteSensorRequest.TYPE_SENSOR_LIST:
                msg.what = MSG_REQUEST_SENSOR_LIST;
                break;

            case RemoteSensorRequest.TYPE_REGISTER_LISTENER:
                msg.what = MSG_REQUEST_REGISTER_LISTENER;
                msg.arg1 = request.sensorRate;
                msg.obj = request.sensor;
                break;

            case RemoteSensorRequest.TYPE_UNREGISTER_LISTENER:
                msg.what = MSG_REQUEST_UNREGISTER_LISTENER;
                msg.obj = request.sensor;
                break;

            default:
                IwdsAssert.dieIf(this, true, "Unsupported remote request");
                return;
            }

            msg.sendToTarget();
        }

        @Override
        public void handleMessage(Message msg) {

            RemoteSensorResponse response = RemoteSensorResponse
                    .obtain(m_transactor);

            switch (msg.what) {
            case MSG_REQUEST_SENSOR_LIST:
                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSensorResponse.TYPE_SENSOR_LIST;
                response.sensorList = (ArrayList<Sensor>) m_service
                        .getSensorList();

                IwdsLog.i(this,
                        "Get sensor list: " + response.sensorList.toString());

                response.sendToRemote();

                break;

            case MSG_REQUEST_REGISTER_LISTENER:
                int sensorRate = msg.arg1;
                Sensor sensorToReg = (Sensor) msg.obj;

                IwdsLog.i(this,
                        "Register sensor listener: " + sensorToReg.toString());

                if (m_service.registerListener(m_listener, sensorToReg,
                        sensorRate))
                    m_sensorList.add(sensorToReg);

                break;

            case MSG_REQUEST_UNREGISTER_LISTENER:
                Sensor sensorToUnreg = (Sensor) msg.obj;

                IwdsLog.i(
                        this,
                        "Unregister sensor listener: "
                                + sensorToUnreg.toString());

                m_service.unregisterListener(m_listener, sensorToUnreg);

                m_sensorList.remove(sensorToUnreg);
                break;

            case MSG_CHANNEL_STATUS_CHANGED:
                m_channelAvailable = msg.arg1 == 1 ? true : false;

                for (Sensor s : m_sensorList)
                    m_service.unregisterListener(m_listener, s);

                break;

            case MSG_SENSOR_SERVICE_CONNECTED:
                m_serviceConnected = msg.arg1 == 1 ? true : false;

                if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    return;
                }

                response.type = RemoteSensorResponse.TYPE_SENSEOR_SERVICE_CONNECTED;
                response.result = msg.arg1;

                response.sendToRemote();

                break;

            case MSG_SENSOR_CHANGED:
                if (!m_serviceConnected) {
                    IwdsLog.i(this, "Notify sensor changed,"
                            + " but sensor service already disconnected.");
                    wakeLockRelease();
                    return;

                } else if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    wakeLockRelease();
                    return;
                }

                SensorEvent event = (SensorEvent) msg.obj;

                response.type = RemoteSensorResponse.TYPE_SENSOR_CHANGED;
                response.sensorEvent = event;

                response.sendToRemote();

                break;

            case MSG_SENSOR_ACCURACY_CHANGED:
                if (!m_serviceConnected) {
                    IwdsLog.i(this, "Notify sensor accuracy changed,"
                            + " but sensor service already disconnected.");
                    wakeLockRelease();
                    return;

                } else if (!m_channelAvailable) {
                    IwdsLog.e(this, "Transfer channel unavailable");
                    wakeLockRelease();
                    return;
                }

                Sensor sensorToNotify = (Sensor) msg.obj;
                int accuracy = msg.arg1;

                response.type = RemoteSensorResponse.TYPE_SENSOR_ACCURACY_CHANGED;
                response.sensor = sensorToNotify;
                response.accuracy = accuracy;

                response.sendToRemote();

                break;
            }
        }
    }

    private SensorEventListener m_listener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            wakeLockAcquire();
            m_handler.notifySensorChanged(event);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            wakeLockAcquire();
            m_handler.notifySensorAccuracyChanged(sensor, accuracy);
        }
    };

    /* ---------------------- DataTransactorCallback -------------------------- */
    @Override
    public void onLinkConnected(DeviceDescriptor descriptor, boolean isConnected) {
        // do not care
    }

    @Override
    public void onChannelAvailable(boolean isAvailable) {
        if (isAvailable) {
            m_handler.setChannelState(true);

            IwdsLog.i(this, "Try to connect sensor service");
            m_sensorServiceClient.connect();

        } else {
            m_handler.setChannelState(false);

            m_handler.notifySensorServiceConnected(false);

            IwdsLog.i(this, "Try to disconnect sensor service");
            m_sensorServiceClient.disconnect();
        }
    }

    @Override
    public void onSendResult(DataTransactResult result) {
        int responseType = ((RemoteSensorResponse) result.getTransferedObject()).type;

        if ((responseType == RemoteSensorResponse.TYPE_SENSOR_CHANGED)
                || (responseType == RemoteSensorResponse.TYPE_SENSOR_ACCURACY_CHANGED)) {

            wakeLockRelease();
        }
    }

    @Override
    public void onDataArrived(Object object) {
        if (object instanceof RemoteSensorRequest)
            m_handler.handleRequest((RemoteSensorRequest) object);

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
    /* --------------------- DataTransactorCallback end ------------------------ */

    /* ------------------ SensorService ConnectionCallbacks -------------------- */
    @Override
    public void onConnected(ServiceClient serviceClient) {
        IwdsLog.i(this, "Sensor service connected");

        m_service = (SensorServiceManager) m_sensorServiceClient
                .getServiceManagerContext();

        m_handler.notifySensorServiceConnected(true);
    }

    @Override
    public void onDisconnected(ServiceClient serviceClient, boolean unexpected) {
        IwdsLog.i(this, "Sensor service disconnected");
    }

    @Override
    public void onConnectFailed(ServiceClient serviceClient,
            ConnectFailedReason reason) {
        IwdsAssert.dieIf(this, true, "Failed to connect to sensor service: "
                + reason.toString());
    }
    /* --------------- SensorService ConnectionCallbacks end ------------------ */

}
