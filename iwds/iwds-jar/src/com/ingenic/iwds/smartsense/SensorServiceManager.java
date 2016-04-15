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

import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

/**
 * <p>
 * 该类用于访问设备上的 {@link com.ingenic.iwds.smartsense.Sensor
 * sensor}. 通过调用 {@link com.ingenic.iwds.common.api.ServiceClient#getServiceManagerContext()}
 * 获取. 在此之前需要连接 {@link com.ingenic.iwds.common.api.ServiceClient#connect()} 传感器服务.
 * </p>
 * <p>
 * 为了降低功耗请在不使用的时候关闭传感器, 使用
 * {@link com.ingenic.iwds.smartsense.SensorServiceManager#unregisterListener(SensorEventListener, Sensor)
 * unregisterListener(SensorEventListener, Sensor)} 实现.
 * </p>
 * <p>
 * 以下是典型用例:
 * </p>
 *
 * <pre class="prettyprint">
 * public class SensorActivity extends Activity implements ServiceClient.ConnectionCallbacks,
 *         SensorEventListener {
 *     private final ServiceClient mClient;
 *     private final SensorServiceManager mService;
 *     private final Sensor mHeartRateSensor;
 *
 *     protected void onCreate() {
 *         mClient = new ServiceClient(this, ServiceManagerContext.SERVICE_SENSOR, this);
 *     }
 *
 *     public void onConnected() {
 *       mService = (SensorServiceManager) mClient.getServiceManagerContext();
 *       mHeartRateSensor = mService.getDefaultSensor(Sensor.TYPE_HEART_RATE);
 *       mService.registerListener(this, mHeartRateSensor, SENSOR_DELAY_NORMAL);
 *     }
 *
 *     public void onDisconnected(boolean unexpected) {
 *         mService.unregisterListener(this, mHeartRateSensor);
 *     }
 *
 *     protected void onResume() {
 *         super.onResume();
 *         mClient.connect();
 *     }
 *
 *     protected void onPause() {
 *         super.onPause();
 *         mClient.disconnect();
 *     }
 *
 *     public void onAccuracyChanged(Sensor sensor, int accuracy) {
 *     }
 *
 *     public void onSensorChanged(SensorEvent event) {
 *     }
 * }
 * </pre>
 *
 * @see ServiceClient
 * @see SensorEventListener
 * @see SensorEvent
 * @see Sensor
 */
public class SensorServiceManager extends ServiceManagerContext {

    /**
     * 描述获取传感器数据速率(最快)的常量, 目前使用不生效
     */
    public static final int SENSOR_DELAY_FASTEST = 0;

    /**
     * 描述获取传感器数据速率(适用于游戏)的常量, 目前使用不生效
     */
    public static final int SENSOR_DELAY_GAME = 1;

    /**
     * 描述获取传感器数据速率(适用于用户操作)的常量, 目前使用不生效
     */
    public static final int SENSOR_DELAY_UI = 2;

    /**
     * 描述获取传感器数据速率(默认)的常量, 目前使用不生效
     */
    public static final int SENSOR_DELAY_NORMAL = 3;

    private ISensorService m_service;

    private HashMap<SensorEventListener, ArrayList<EventCallback>> m_listeners;

    /**
     * 不要直接构造, 构造 {@link com.ingenic.iwds.common.api.ServiceClient ServiceClient} 时会自动构造
     * 详细用法参考典型用例
     */
    public SensorServiceManager(Context context) {
        super(context);

        m_listeners = new HashMap<SensorEventListener, ArrayList<EventCallback>>();

        m_serviceClientProxy = new ServiceClientProxy() {
            @Override
            public void onServiceConnected(IBinder service) {
                m_service = ISensorService.Stub.asInterface(service);
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
     * 使用该方法获取传感器列表
     *
     * @return
     *        返回传感器列表
     */
    public List<Sensor> getSensorList() {
        try {
            return m_service.getSensorList();
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in getSensorList: " + e.toString());
            return null;
        }
    }

    /**
     * 使用该方法获取指定类型的传感器
     *
     * @param sensorType
     *        传感器类型
     * @return
     *        返回指定类型的传感器
     */
    public Sensor getDefaultSensor(int sensorType) {
        try {
            return m_service.getDefaultSensor(sensorType);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in getDefaultSensor: " + e.toString());

            return null;
        }
    }

    private class EventCallback extends ISensorEventCallback.Stub {
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
