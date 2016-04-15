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

package com.ingenic.iwds.uniconnect.link;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.ingenic.iwds.uniconnect.UniconnectErrorCode;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

/**
 * Android蓝牙适配器类.
 */
public class AndroidBtAdapter extends Adapter {
    private BluetoothAdapter m_btAdapter;
    private DeviceDiscoveryCallbacks m_discoveryCallbacks;

    private boolean m_discoveryPending = false;

    private int m_nextState;
    private final int m_s_start = 0;
    private final int m_s_deviceFound = 1;

    private final BroadcastReceiver m_btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!m_discoveryPending)
                return;

            String action = intent.getAction();
            /*
             * start -> devices found -> finished
             */
            switch (m_nextState) {
            case m_s_start:
                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    clearRemoteDevices();

                    m_discoveryCallbacks.onDiscoveryStarted();

                    m_nextState = m_s_deviceFound;
                }

                break;

            case m_s_deviceFound:
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice rawDevice = intent
                            .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    AndroidBtDevice device = new AndroidBtDevice(rawDevice);
                    addRemoteDevice(device);

                    m_discoveryCallbacks.onDeviceFound(device);

                    m_nextState = m_s_deviceFound;

                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                        .equals(action)) {
                    m_discoveryCallbacks.onDiscoveryFinished();

                    m_discoveryPending = false;
                    m_nextState = m_s_start;
                }

                break;
            }
        }
    };

    /**
     * 实例化一个android蓝牙适配器对象.
     *
     * @param context
     *            应用的上下文
     * @param adapterManager
     *            适配器管理者
     */
    /* package */AndroidBtAdapter(Context context, AdapterManager adapterManager) {
        super(context, adapterManager, TAG_ANDROID_BT_DATA_CHANNEL);

        m_btAdapter = BluetoothAdapter.getDefaultAdapter();
        IwdsAssert.dieIf(this, m_btAdapter == null,
                "Android does not support bluetooth.");

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        getContext().registerReceiver(m_btReceiver, filter);
    }

    @Override
    public String getLocalAddress() {
        return m_btAdapter.getAddress();
    }

    @Override
    public void cancelDiscovey() {
        if (!isEnabled())
            return;

        boolean success = false;

        do {
            success = m_btAdapter.cancelDiscovery();
        } while (!success && isEnabled());

        m_discoveryPending = false;
    }

    @Override
    public int startDiscovery(DeviceDiscoveryCallbacks callbacks) {
        if (!isEnabled()) {
            IwdsLog.e(this, "BT was disabled");

            return UniconnectErrorCode.EDISABLED;
        }

        if (m_discoveryPending) {
            IwdsLog.e(this, "device discovery is ongoing(cancel it)");

            return UniconnectErrorCode.EDISCOVERYISONGING;
        }

        cancelDiscovey();
        m_discoveryCallbacks = callbacks;
        m_discoveryPending = true;
        m_nextState = m_s_start;

        boolean success = false;
        do {
            success = m_btAdapter.startDiscovery();
        } while (!success && isEnabled());

        if (!success)
            return UniconnectErrorCode.EREMOTEEXCEPTION;

        return 0;
    }

    @Override
    public boolean isEnabled() {
        return m_btAdapter.isEnabled();
    }

    @Override
    public boolean enable() {
        return m_btAdapter.enable();
    }

    @Override
    public void disable() {
        m_btAdapter.disable();
    }
}
