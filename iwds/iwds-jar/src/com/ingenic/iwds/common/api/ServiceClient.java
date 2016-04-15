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

package com.ingenic.iwds.common.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.ingenic.iwds.app.NotificationProxyServiceManager;
import com.ingenic.iwds.appwidget.WidgetManager;
import com.ingenic.iwds.cloud.CloudServiceManager;
import com.ingenic.iwds.devicemanager.DeviceManagerServiceManager;
import com.ingenic.iwds.remotebroadcast.RemoteBroadcastManager;
import com.ingenic.iwds.remotedevice.RemoteDeviceServiceManager;
import com.ingenic.iwds.remotewakelock.RemoteWakeLockManager;
import com.ingenic.iwds.slpt.WatchFaceServiceManager;
import com.ingenic.iwds.smartlocation.RemoteLocationServiceManager;
import com.ingenic.iwds.smartlocation.search.RemoteSearchServiceManager;
import com.ingenic.iwds.smartsense.RemoteSensorServiceManager;
import com.ingenic.iwds.smartsense.SensorServiceManager;
import com.ingenic.iwds.smartspeech.RemoteSpeechServiceManager;
import com.ingenic.iwds.smartvibrate.VibrateServiceManager;
import com.ingenic.iwds.uniconnect.ConnectionServiceManager;
import com.ingenic.iwds.utils.IwdsAssert;

/**
 * 远程服务的客户端
 */
public class ServiceClient {
    private Context m_context;
    private String m_serviceName;
    private ConnectionCallbacks m_callbacks;

    private BindServiceHandler m_handler;

    private ServiceManagerContext m_serviceManager;
    private ServiceManagerCreator m_serviceManagerCreator;
    private Intent m_bindIntent;

    private static HashMap<String, ServiceManagerCreator> sm_serviceManagerRegistry;

    private static List<String> sm_prefixPackageNames = new ArrayList<String>(
            Arrays.asList(
                    /*
                     * For IWOP project
                     */
                    "com.ingenic.watchmanager",
                    "com.ingenic.iwds.device",
                    "com.ingenic.iwds.phone",
                    "com.ingenic.watchconnector",

                    /*
                     * For GYENNO watch
                     */
                    "com.gyenno",

                    /*
                     * For Wear day
                     */
                    "com.acmeasy.watchmanager",

                    /*
                     * For TOMOON
                     */
                    "com.bsk.sugar",
                    "com.tomoon.bloodsugar.doctor",
                    "com.tomoon.launcher"));

    private abstract class ServiceManagerCreator {
        public Intent bindIntent;

        public void setBindIntent(Intent it) {
            bindIntent = it;
        }

        public Intent getBindIntent() {
            return bindIntent;
        }

        public abstract ServiceManagerContext createServiceManager(
                Context context);

        public abstract String getBindAction();
    }

    private Intent createIntentFromAction(Context context, String action) {
        Intent implicitIntent = new Intent(action);
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfoList = pm.queryIntentServices(
                implicitIntent, 0);

        String className = null;
        String packageName = null;

        if (resolveInfoList == null || resolveInfoList.size() == 0)
            return null;

        // Make sure only one match was found
        for (ResolveInfo resolveInfo : resolveInfoList) {

            // only match first item
            if (sm_prefixPackageNames
                    .contains(resolveInfo.serviceInfo.packageName)) {

                packageName = resolveInfo.serviceInfo.packageName;
                className = resolveInfo.serviceInfo.name;
                break;
            }
        }

        if (packageName == null || className == null)
            return null;

        // create ComponentName
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    private void createServiceManagerRegistry(Context context) {
        sm_serviceManagerRegistry = new HashMap<String, ServiceManagerCreator>();

        // ConnectionService
        sm_serviceManagerRegistry.put(ServiceManagerContext.SERVICE_CONNECTION,
                new ServiceManagerCreator() {

                    @Override
                    public ServiceManagerContext createServiceManager(
                            Context context) {
                        return new ConnectionServiceManager(context);
                    }

                    @Override
                    public String getBindAction() {
                        return "com.ingenic.iwds.uniconnect.ConnectionService";
                    }
                });

        // NotificationProxyService
        sm_serviceManagerRegistry.put(
                ServiceManagerContext.SERVICE_NOTIFICATION_PROXY,
                new ServiceManagerCreator() {

                    @Override
                    public ServiceManagerContext createServiceManager(
                            Context context) {
                        return new NotificationProxyServiceManager(context);
                    }

                    @Override
                    public String getBindAction() {
                        return "com.ingenic.iwds.app.NotificationProxyService";
                    }
                });

        // CloudService
        sm_serviceManagerRegistry.put(ServiceManagerContext.SERVICE_CLOUD,
                new ServiceManagerCreator() {

                    @Override
                    public ServiceManagerContext createServiceManager(
                            Context context) {
                        return new CloudServiceManager(context);
                    }

                    @Override
                    public String getBindAction() {
                        return "com.ingenic.iwds.cloud.CloudService";
                    }
                });

        // DeviceManagerService
        sm_serviceManagerRegistry.put(
                ServiceManagerContext.SERVICE_DEVICE_MANAGER,
                new ServiceManagerCreator() {

                    @Override
                    public ServiceManagerContext createServiceManager(
                            Context context) {
                        return new DeviceManagerServiceManager(context);
                    }

                    @Override
                    public String getBindAction() {
                        return "com.ingenic.iwds.devicemanager.DeviceManagerService";
                    }
                });

        // RemoteDeviceService
        sm_serviceManagerRegistry.put(
                ServiceManagerContext.SERVICE_REMOTE_DEVICE,
                new ServiceManagerCreator() {

                    @Override
                    public ServiceManagerContext createServiceManager(
                            Context context) {
                        return new RemoteDeviceServiceManager(context);
                    }

                    @Override
                    public String getBindAction() {
                        return "com.ingenic.iwds.remotedevice.RemoteDeviceService";
                    }
                });

        // WatchFaceService
        sm_serviceManagerRegistry.put(
                ServiceManagerContext.SERVICE_SLPT_WATCH_FACE,
                new ServiceManagerCreator() {

                    @Override
                    public ServiceManagerContext createServiceManager(
                            Context context) {
                        return new WatchFaceServiceManager(context);
                    }

                    @Override
                    public String getBindAction() {
                        return "com.ingenic.iwds.slpt.WatchFaceService";
                    }
                });

        // RemoteLocationService
        sm_serviceManagerRegistry.put(
                ServiceManagerContext.SERVICE_REMOTE_LOCATION,
                new ServiceManagerCreator() {

                    @Override
                    public ServiceManagerContext createServiceManager(
                            Context context) {
                        return new RemoteLocationServiceManager(context);
                    }

                    @Override
                    public String getBindAction() {
                        return "com.ingenic.iwds.smartlocation.RemoteLocationService";
                    }
                });

        // SensorService
        sm_serviceManagerRegistry.put(ServiceManagerContext.SERVICE_SENSOR,
                new ServiceManagerCreator() {

                    @Override
                    public ServiceManagerContext createServiceManager(
                            Context context) {
                        return new SensorServiceManager(context);
                    }

                    @Override
                    public String getBindAction() {
                        return "com.ingenic.iwds.smartsense.SensorService";
                    }
                });

        // RemoteSensorService
        sm_serviceManagerRegistry.put(
                ServiceManagerContext.SERVICE_REMOTE_SENSOR,
                new ServiceManagerCreator() {

                    @Override
                    public ServiceManagerContext createServiceManager(
                            Context context) {
                        return new RemoteSensorServiceManager(context);
                    }

                    @Override
                    public String getBindAction() {
                        return "com.ingenic.iwds.smartsense.RemoteSensorService";
                    }
                });

        // VibrateService
        sm_serviceManagerRegistry.put(ServiceManagerContext.SERVICE_VIBRATE,
                new ServiceManagerCreator() {

                    @Override
                    public ServiceManagerContext createServiceManager(
                            Context context) {
                        return new VibrateServiceManager(context);
                    }

                    @Override
                    public String getBindAction() {
                        return "com.ingenic.iwds.smartvibrate.VibrateService";
                    }
                });

        // RemoteSearchService
        sm_serviceManagerRegistry.put(
                ServiceManagerContext.SERVICE_REMOTE_SEARCH,
                new ServiceManagerCreator() {

                    @Override
                    public ServiceManagerContext createServiceManager(
                            Context context) {
                        return new RemoteSearchServiceManager(context);
                    }

                    @Override
                    public String getBindAction() {
                        return "com.ingenic.iwds.smartlocation.search.RemoteSearchService";
                    }
                });

        // RemoteSpeechService
        sm_serviceManagerRegistry.put(
                ServiceManagerContext.SERVICE_REMOTE_SPEECH,
                new ServiceManagerCreator() {

                    @Override
                    public ServiceManagerContext createServiceManager(
                            Context context) {
                        return new RemoteSpeechServiceManager(context);
                    }

                    @Override
                    public String getBindAction() {
                        return "com.ingenic.iwds.smartspeech.RemoteSpeechService";
                    }

                });

        // RemoteBroadcastService
        sm_serviceManagerRegistry.put(
                ServiceManagerContext.SERVICE_REMOTE_BROADCAST,
                new ServiceManagerCreator() {

                    @Override
                    public String getBindAction() {
                        return "com.ingenic.iwds.remotebroadcast.RemoteBroadcastService";
                    }

                    @Override
                    public ServiceManagerContext createServiceManager(
                            Context context) {
                        return new RemoteBroadcastManager(context);
                    }
                });

        // RemoteWakeLockService
        sm_serviceManagerRegistry.put(
                ServiceManagerContext.SERVICE_REMOTE_WAKELOCK,
                new ServiceManagerCreator() {

                    @Override
                    public String getBindAction() {
                        return "com.ingenic.iwds.remotewakelock.RemoteWakeLockService";
                    }

                    @Override
                    public ServiceManagerContext createServiceManager(
                            Context context) {
                        return new RemoteWakeLockManager(context);
                    }
                });

        // WidgetService
        sm_serviceManagerRegistry.put(
                ServiceManagerContext.SERVICE_LOCAL_WIDGET,
                new ServiceManagerCreator() {

                    @Override
                    public String getBindAction() {
                        return "com.ingenic.iwds.appwidget.WidgetService";
                    }

                    @Override
                    public ServiceManagerContext createServiceManager(
                            Context context) {
                        return new WidgetManager(context);
                    }
                });

        Set<String> serviceNames = sm_serviceManagerRegistry.keySet();
        Iterator<String> it = serviceNames.iterator();
        while (it.hasNext()) {
            ServiceManagerCreator creator = sm_serviceManagerRegistry.get(it
                    .next());
            creator.setBindIntent(createIntentFromAction(
                    context.getApplicationContext(), creator.getBindAction()));
        }
    }

    /**
     * 构造 {@code ServiceClient} 对象
     * 
     * @param context
     *            应用的上下文
     * @param serviceName
     *            需要获取的服务名称，服务名称定义在类
     *            {@link com.ingenic.iwds.common.api.ServiceManagerContext} 中
     * @param callbacks
     *            回调接口，用于通知连接和状态
     */
    public ServiceClient(Context context, String serviceName,
            ConnectionCallbacks callbacks) {
        IwdsAssert.dieIf(this, context == null, "Context is null.");

        IwdsAssert.dieIf(this, serviceName == null || serviceName.isEmpty(),
                "Service name is null or empty.");

        IwdsAssert.dieIf(this, callbacks == null, "Callbacks is null.");

        m_context = context.getApplicationContext();

        IwdsAssert
                .dieIf(this, m_context == null, "Application context is null");

        m_callbacks = callbacks;
        m_handler = new BindServiceHandler();
        m_serviceName = serviceName;

        if (sm_serviceManagerRegistry == null)
            createServiceManagerRegistry(m_context);

        m_serviceManagerCreator = sm_serviceManagerRegistry.get(m_serviceName);

        IwdsAssert.dieIf(this, m_serviceManagerCreator == null,
                "Unsupported service: " + m_serviceName);

        m_serviceManager = m_serviceManagerCreator
                .createServiceManager(m_context);

        m_bindIntent = m_serviceManagerCreator.getBindIntent();

        IwdsAssert.dieIf(this, m_bindIntent == null, "Unable to find service: "
                + m_serviceName);
    }

    private class BindServiceHandler extends Handler {
        public static final int MSG_SERVICE_CONNECTING = 19;
        public static final int MSG_SERVICE_DISCONNECTING = 87;
        public static final int MSG_SERVICE_CONNECTED = 20;
        public static final int MSG_SERVICE_DISCONNECTED = 14;
        public static final int MSG_SERVICE_CONNECT_FAILED = 9;

        private int m_state = S_DISCONNECTED;

        public final static int S_DISCONNECTED = 0;
        public final static int S_CONNECTING = 1;
        public final static int S_DISCONNECTING = 2;
        public final static int S_CONNECTED = 3;

        private boolean m_connectPending;
        private boolean m_disconnectPending;

        public int getState() {
            synchronized (this) {
                return m_state;
            }
        }

        public void connect() {

            Message msg = Message.obtain(this);

            msg.what = MSG_SERVICE_CONNECTING;

            msg.sendToTarget();
        }

        public void disconnect() {
            Message msg = Message.obtain(this);

            msg.what = MSG_SERVICE_DISCONNECTING;

            msg.sendToTarget();
        }

        public void notifyConnectFailed(ConnectFailedReason reason) {
            Message msg = Message.obtain(this);

            msg.what = MSG_SERVICE_CONNECT_FAILED;
            msg.obj = reason;

            msg.sendToTarget();
        }

        public void notifyServiceConnected(IBinder service) {
            Message msg = Message.obtain(this);

            msg.what = MSG_SERVICE_CONNECTED;
            msg.obj = service;

            msg.sendToTarget();
        }

        public void notifyServiceDisconnected() {
            Message msg = Message.obtain(this);

            msg.what = MSG_SERVICE_DISCONNECTED;

            msg.sendToTarget();
        }

        private ServiceConnection m_serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                notifyServiceConnected(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                notifyServiceDisconnected();
            }
        };

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_SERVICE_CONNECTING:
                synchronized (this) {
                    if (m_state == S_CONNECTING || m_state == S_CONNECTED) {
                        if (m_disconnectPending)
                            m_disconnectPending = false;

                        return;
                    }

                    if (m_state == S_DISCONNECTING) {
                        m_connectPending = true;
                        return;
                    }

                    m_state = S_CONNECTING;
                }

                boolean ok = m_context.bindService(m_bindIntent,
                        m_serviceConnection, Service.BIND_AUTO_CREATE);

                if (!ok)
                    notifyConnectFailed(new ConnectFailedReason(
                            ConnectFailedReason.R_SERVICE_UNAVAILABLE));

                break;

            case MSG_SERVICE_DISCONNECTING:
                synchronized (this) {
                    if (m_state == S_DISCONNECTING || m_state == S_DISCONNECTED) {
                        if (m_connectPending)
                            m_connectPending = false;
                        return;
                    }

                    if (m_state == S_CONNECTING) {
                        m_disconnectPending = true;
                        return;
                    }

                    m_state = S_DISCONNECTING;
                }

                m_context.unbindService(m_serviceConnection);
                notifyServiceDisconnected();

                break;

            case MSG_SERVICE_CONNECTED:
                m_serviceManager.onServiceConnected((IBinder) msg.obj);

                synchronized (this) {
                    m_state = S_CONNECTED;
                }

                m_callbacks.onConnected(ServiceClient.this);

                if (m_connectPending)
                    m_connectPending = false;

                if (m_disconnectPending) {
                    m_disconnectPending = false;
                    disconnect();
                }

                break;

            case MSG_SERVICE_DISCONNECTED:
                boolean unexpected = false;
                synchronized (this) {
                    if (m_state == S_CONNECTED)
                        unexpected = true;

                    m_state = S_DISCONNECTED;
                }

                m_callbacks.onDisconnected(ServiceClient.this, unexpected);

                m_serviceManager.onServiceDisconnected(unexpected);

                if (m_disconnectPending)
                    m_disconnectPending = false;

                if (m_connectPending) {
                    m_connectPending = false;
                    connect();
                }

                break;

            case MSG_SERVICE_CONNECT_FAILED:
                synchronized (this) {
                    m_state = S_DISCONNECTED;
                }

                m_callbacks.onConnectFailed(ServiceClient.this,
                        (ConnectFailedReason) msg.obj);

                break;

            }
        };
    };

    /**
     * 连接到远程服务
     */
    public void connect() {
        m_handler.connect();
    }

    /**
     * 断开与远程服务的连接
     */
    public void disconnect() {
        m_handler.disconnect();
    }

    /**
     * 判断是否处于连接状态
     * 
     * @return true 连接，false 未连接
     */
    public boolean isConnected() {
        return m_handler.getState() == BindServiceHandler.S_CONNECTED;
    }

    /**
     * 获取服务的 ServiceManager，返回值需要转换为具体的ServiceManager类型
     * 
     * @return ServiceManager对象
     */
    public ServiceManagerContext getServiceManagerContext() {
        return m_serviceManager;
    }

    /**
     * 获取服务的名字
     * 
     * @return 服务的名字
     */
    public String getServiceName() {
        return m_serviceName;
    }

    /**
     * 连接回调接口, 用于通知服务连接状态
     */
    public static abstract interface ConnectionCallbacks {

        /**
         * 连接成功
         * 
         * @param serviceClient
         *            ServiceClient 对象
         */
        void onConnected(ServiceClient serviceClient);

        /**
         * 断开连接
         * 
         * @param serviceClient
         *            ServiceClient 对象
         * @param unexpected
         *            表示连接断开是否因为意外情况造成。true 意外，false 非意外
         */
        void onDisconnected(ServiceClient serviceClient, boolean unexpected);

        /**
         * 连接失败
         * 
         * @param serviceClient
         *            ServiceClient 对象
         * @param reason
         *            连接失败的原因
         */
        void onConnectFailed(ServiceClient serviceClient,
                ConnectFailedReason reason);
    }
}
