package com.ingenic.watchconnector;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.IwdsApplication;
import com.ingenic.iwds.uniconnect.link.Adapter;
import com.ingenic.iwds.uniconnect.link.AdapterManager;
import com.ingenic.iwds.uniconnect.link.Link;
import com.ingenic.iwds.utils.IwdsLog;

public class WatchConnectorApplication extends IwdsApplication {
    private AdapterManager m_manager;
    private Adapter m_adapter;
    private Link m_link;

    @Override
    public void onCreate() {
        super.onCreate();

        initialize(DeviceDescriptor.DEVICE_CLASS_MOBILE,
                DeviceDescriptor.MOBILE_DEVICE_SUBCLASS_SMARTPHONE);

        m_manager = AdapterManager.getInstance(getApplicationContext());
        m_adapter = m_manager.getAdapter(Adapter.TAG_ANDROID_BT_DATA_CHANNEL);
        m_adapter.enable();

        IwdsLog.i(this, "create link");

        m_link = m_adapter.createLink(new DeviceDescriptor(m_adapter
                .getLocalAddress(), m_adapter.getLinkTag(),
                DeviceDescriptor.DEVICE_CLASS_MOBILE,
                DeviceDescriptor.MOBILE_DEVICE_SUBCLASS_SMARTPHONE));

        IwdsLog.i(this, "bondAddress D0:31:10:F1:9A:82");
        m_link.bondAddress("D0:31:10:F1:9A:82");
    }
}
