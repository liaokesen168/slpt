package com.ingenic.iwds.apidemo;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.smartvibrate.VibrateServiceManager;
import com.ingenic.iwds.smartvibrate.VibrateServiceManager.VibrateModes;
import com.ingenic.iwds.utils.IwdsLog;
import com.ingenic.iwds.widget.AdapterView;
import com.ingenic.iwds.widget.AmazingListView;

public class SmartVibrateFragment extends DemoFragment implements
        ServiceClient.ConnectionCallbacks, AdapterView.OnItemClickListener {

    private static final String TAG = "SmartVibrateTest";
    private ServiceClient mClient;
    private VibrateServiceManager mService;

    private View mContentView;
    private AmazingListView mListView;
    private ArrayAdapter<CharSequence> mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mClient = new ServiceClient(getActivity(),
                ServiceManagerContext.SERVICE_VIBRATE, this);
        mClient.connect();

        mContentView = inflater.inflate(R.layout.smart_vibrate, null);

        mListView = (AmazingListView) mContentView
                .findViewById(R.id.smart_vibrate_list);
        mListView.setSelector(R.drawable.list_item_selector);

        TextView view = new TextView(getActivity());
        view.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large);
        view.setGravity(Gravity.CENTER);
        view.setText(R.string.smart_vibrate);
        mListView.addHeaderView(view);

        String[] vibrates = getResources().getStringArray(R.array.vibrates);
        mAdapter = new ArrayAdapter<CharSequence>(getActivity(),
                R.layout.list_item, R.id.item_text, vibrates);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(this);

        return mContentView;
    }

    @Override
    public void onConnected(ServiceClient serviceClient) {
        mService = (VibrateServiceManager) mClient.getServiceManagerContext();
    }

    @Override
    public void onDisconnected(ServiceClient serviceClient, boolean unexpected) {
    }

    @Override
    public void onConnectFailed(ServiceClient serviceClient,
            ConnectFailedReason reason) {
    }

    @SuppressWarnings("static-access")
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        switch (position) {
        case 0: {
            IwdsLog.i(TAG, "Click Item1");

            mService.vibrate(3000);
        }
            break;

        case 1: {
            IwdsLog.i(TAG, "Click Item2");

            VibrateModes vm = mService.GetCustomModes(1);
            if (vm == null)
                break;

            mService.SpecialVibrate(vm);
        }
            break;

        case 2: {
            IwdsLog.i(TAG, "Click Item3");

            VibrateModes vm = mService.GetCustomModes(2);
            if (vm == null)
                break;

            mService.SpecialVibrate(vm);
        }
            break;

        case 3: {
            IwdsLog.i(TAG, "Click Item4");

            VibrateModes vm = mService.GetCustomModes(3);
            if (vm == null)
                break;

            mService.SpecialVibrate(vm);
        }
            break;

        case 4: {
            IwdsLog.i(TAG, "Click Item5");

            int len = 3;
            int[] effect = new int[len];
            effect[0] = mService.VIBRATE_BUZZ_2;
            effect[1] = mService.VIBRATE_BUZZ_2;
            effect[2] = mService.VIBRATE_BUZZ_2;
            VibrateModes vm = mService.GetVibrateModes(effect, len);

            mService.SpecialVibrate(vm);
        }
            break;

        case 5: {
            IwdsLog.i(TAG, "Click Item6");

            int len = 3;
            int[] effect = new int[len];
            effect[0] = mService.VIBRATE_TRANSITION_RAMP_UP_LONG_SHARP_1;
            effect[1] = mService.VIBRATE_TRANSITION_RAMP_UP_LONG_SHARP_1;
            effect[2] = mService.VIBRATE_TRANSITION_RAMP_UP_LONG_SHARP_1;
            VibrateModes vm = mService.GetVibrateModes(effect, len);

            mService.SpecialVibrate(vm);
        }
            break;

        default:
            break;
        }
    }
}
