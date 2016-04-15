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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

public class RemoteSensorServiceManager extends ServiceManagerContext {

    /**
     * 描述获取远端设别传感器数据速率(最快)的常量, 目前使用不生效
     */
    public static final int SENSOR_DELAY_FASTEST = 0;

    /**
     * 描述获取远端传感器数据速率(适用于游戏)的常量, 目前使用不生效
     */
    public static final int SENSOR_DELAY_GAME = 1;

    /**
     * 描述获取远端传感器数据速率(适用于用户操作)的常量, 目前使用不生效
     */
    public static final int SENSOR_DELAY_UI = 2;

    /**
     * 描述获取远端传感器数据速率(默认)的常量, 目前使用不生效
     */
    public static final int SENSOR_DELAY_NORMAL = 3;

    private IRemoteSensorService m_service;

    private HashMap<SensorEventListener, ArrayList<EventCallback>> m_listeners;
    private HashMap<RemoteSensorListener, RemoteSensorCallback> m_remoteSensorListener;

    private boolean m_remoteAvaliable;

    /**
     * 不要直接构造, 构造 {@link com.ingenic.iwds.common.api.ServiceClient ServiceClient} 时会自动构造
     * 详细用法参考典型用例
     */
    public RemoteSensorServiceManager(Context context) {
        super(context);

        m_listeners = new HashMap<SensorEventListener, ArrayList<EventCallback>>();
        m_remoteSensorListener = new HashMap<RemoteSensorListener, RemoteSensorCallback>();

        m_serviceClientProxy = new ServiceClientProxy() {
            @Override
            public void onServiceConnected(IBinder service) {
                m_service = IRemoteSensorService.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(boolean unexpected) {
                unregisterAllListeners();
            }

            @Override
            public IBinder getBinder() {
                return m_service.asBinder();
            }
        };
    }

    /**
     * 使用该方法获取远端设备传感器列表
     *
     * @return
     *        返回传感器列表
     */
    public List<Sensor> getSensorList() {
        if (!m_remoteAvaliable) {
            IwdsLog.e(this, "Sensor service on remote device unavailable");
            return null;
        }

        try {
            return m_service.getSensorList();
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in getSensorList: " + e.toString());
            return null;
        }
    }

    /**
     * 使用该方法获取指定类型的远端传感器
     *
     * @param sensorType
     *        传感器类型
     * @return
     *        返回指定类型的传感器
     */
    public Sensor getDefaultSensor(int sensorType) {
        if (!m_remoteAvaliable) {
            IwdsLog.e(this, "Sensor service on remote device unavailable");
            return null;
        }

        try {
            return m_service.getDefaultSensor(sensorType);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in getDefaultSensor: " + e.toString());

            return null;
        }
    }

    private class EventCallback extends IRemoteSensorEventCallback.Stub {
        private SensorEventListener m_listener;
        Sensor sensor;
        String uuid;

        private static final int MSG_ON_EVENT = 19;
        private static final int MSG_ON_ACCURACY = 87;

        private Handler m_handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_ON_EVENT:
                    m_listener.onSensorChanged((SensorEvent) msg.obj);

                    break;

                case MSG_ON_ACCURACY:
                    m_listener.onAccuracyChanged((Sensor) msg.obj, msg.arg1);
                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Implement me.");

                    break;
                }
            }
        };

        public EventCallback(SensorEventListener listener, Sensor s) {
            m_listener = listener;
            sensor = s;
            uuid = UUID.randomUUID().toString();
        }

        @Override
        public void onSensorChanged(SensorEvent event) throws RemoteException {
            Message.obtain(m_handler, MSG_ON_EVENT, event).sendToTarget();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy)
                throws RemoteException {
            Message.obtain(m_handler, MSG_ON_ACCURACY, accuracy, 0, sensor)
                    .sendToTarget();
        }
    }

    private class RemoteSensorCallback extends IRemoteSensorCallback.Stub {
        private static final int MSG_ON_AVAILABLE = 0;

        private RemoteSensorListener m_listener;
        private boolean status;
        String uuid;

        private Handler m_handler = new Handler() {
            ArrayList<Sensor> sensorList;

            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                case MSG_ON_AVAILABLE:
                    sensorList = (ArrayList<Sensor>) msg.obj;
                    if (sensorList == null) {
                        m_remoteAvaliable = false;
                        unregisterAllListeners();
                    } else {
                        m_remoteAvaliable = true;
                    }

                    if (m_remoteAvaliable != status)
                        m_listener.onSensorAvailable(sensorList);

                    status = m_remoteAvaliable;

                    break;

                default:
                    IwdsAssert.dieIf(this, true, "Implement me.");
                }
            }

        };

        public RemoteSensorCallback(RemoteSensorListener listener) {
            m_listener = listener;
            uuid = UUID.randomUUID().toString();
        }

        @Override
        public void onSensorAvailable(List<Sensor> sensorList)
                throws RemoteException {
            Message.obtain(m_handler, MSG_ON_AVAILABLE, sensorList).sendToTarget();
        }

    }

    /**
     * 用于注册远端设备传感器服务的监听器 {@link com.ingenic.iwds.smartsense.RemoteSensorListener
     * RemoteSensorListener}
     *
     * @param listener
     *        监听器对象 {@link com.ingenic.iwds.smartsense.RemoteSensorListener
     *                  RemoteSensorListener}
     *
     * @return true 注册成功, false 注册失败
     */
    public boolean registerRemoteListener(RemoteSensorListener listener) {
        IwdsAssert.dieIf(this, listener == null, "Listener is null.");

        RemoteSensorCallback callback = m_remoteSensorListener.get(listener);
        if (callback != null) {
            IwdsAssert.dieIf(this, true, "Unable to register listener:"
                    + " Did you froget to call unregisterRemoteSensorListener?");
            return false;

        } else {
            callback = new RemoteSensorCallback(listener);
            m_remoteSensorListener.put(listener, callback);
        }

        try {
            return m_service.registerRemoteSensorListener(callback.uuid, callback);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in registerRemoteListener: " + e.toString());
            return false;
        }
    }

    /**
     * 注销远端设备传感器服务状态的监听器 {@link com.ingenic.iwds.smartsense.RemoteSensorListener
     * RemoteSensorListener}
     * @param listener
     *        监听器对象 {@link com.ingenic.iwds.smartsense.RemoteSensorListener
     *                  RemoteSensorListener}
     */
    public void unregisterRemoteListener(RemoteSensorListener listener) {
        IwdsAssert.dieIf(this, listener == null, "Remote sensor listener is null.");

        RemoteSensorCallback callback = m_remoteSensorListener.get(listener);

        if (callback == null)
            return;

        try {
            m_service.unregisterRemoteSensorListener(callback.uuid);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in unregisterRemoteListener: " + e.toString());
        }

        m_remoteSensorListener.remove(listener);
    }

    /**
     * 为指定的传感器注册 {@link com.ingenic.iwds.smartsense.SensorEventListener
     *                 SensorEventListener}
     * @param listener
     *        监听器对象 {@link com.ingenic.iwds.smartsense.SensorEventListener SensorEventListener}
     * @param sensor
     *        传感器对象 {@link com.ingenic.iwds.smartsense.Sensor Sensor}
     * @param rate
     *        传感器事件 {@link com.ingenic.iwds.smartsense.SensorEvent SensorEvent} 上报速率,
     *        {@link #SENSOR_DELAY_FASTEST}, {@link #SENSOR_DELAY_GAME}, {@link #SENSOR_DELAY_UI}
     *        {@link #SENSOR_DELAY_NORMAL}
     *
     * @return true IWDS传感器服务支持该类型 {@link com.ingenic.iwds.smartsense.Sensor Sensor}
     */
    public boolean registerListener(SensorEventListener listener,
            Sensor sensor, int rate) {
        if (!m_remoteAvaliable) {
            IwdsLog.e(this, "Sensor service on remote device unavailable");
            return false;
        }

        IwdsAssert.dieIf(this, listener == null, "Listener is null.");
        IwdsAssert.dieIf(this, sensor == null, "Sensor is null.");

        ArrayList<EventCallback> callbacks = m_listeners.get(listener);
        if (callbacks != null) {
            for (EventCallback cb : callbacks)
                if (cb.sensor.getType() == sensor.getType())
                    return false;
        }

        EventCallback cb = new EventCallback(listener, sensor);

        if (callbacks == null) {
            callbacks = new ArrayList<EventCallback>();
            m_listeners.put(listener, callbacks);
        }

        callbacks.add(cb);

        try {
            return m_service.registerListener(cb.uuid, cb, sensor, rate);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in registerListener: " + e.toString());

            return false;
        }
    }

    /**
     * 注销在 {@link com.ingenic.iwds.smartsense.SensorEventListener
     *       SensorEventListener} 上注册的传感器
     * @param listener
     *        监听器对象 {@link com.ingenic.iwds.smartsense.SensorEventListener SensorEventListener}
     * @param sensor
     *        传感器对象 {@link com.ingenic.iwds.smartsense.Sensor Sensor}
     */
    public void unregisterListener(SensorEventListener listener, Sensor sensor) {
        IwdsAssert.dieIf(this, listener == null, "Listener is null.");
        IwdsAssert.dieIf(this, sensor == null, "Sensor is null.");

        ArrayList<EventCallback> callbacks = m_listeners.get(listener);

        if (callbacks == null)
            return;

        try {
            for (Iterator<EventCallback> it = callbacks.iterator(); it
                    .hasNext();) {
                EventCallback cb = it.next();
                if (cb.sensor.getType() == sensor.getType()) {
                    m_service.unregisterListener(cb.uuid);

                    it.remove();

                    break;
                }
            }

            if (callbacks.isEmpty())
                m_listeners.remove(listener);

        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in unregisterListener: " + e.toString());
        }
    }

    private void unregisterAllListeners() {
        Set<SensorEventListener> listeners = m_listeners.keySet();

        Iterator<SensorEventListener> it = listeners.iterator();
        while (it.hasNext()) {
            ArrayList<EventCallback> callbacks = m_listeners.get(it.next());

            try {
                for (EventCallback cb : callbacks)
                    m_service.unregisterListener(cb.uuid);

            } catch (RemoteException e) {
                IwdsLog.e(this,
                        "Exception in unregisterListener: " + e.toString());
            }
        }

        m_listeners.clear();
    }
}
