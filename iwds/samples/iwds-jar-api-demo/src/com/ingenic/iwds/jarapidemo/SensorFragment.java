package com.ingenic.iwds.jarapidemo;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;

import com.ingenic.iwds.common.api.ConnectFailedReason;
import com.ingenic.iwds.common.api.ServiceClient;
import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.smartsense.Sensor;
import com.ingenic.iwds.smartsense.SensorEvent;
import com.ingenic.iwds.smartsense.SensorEventListener;
import com.ingenic.iwds.smartsense.SensorServiceManager;
import com.ingenic.iwds.utils.IwdsLog;

/**
 * 演示 Sensor 的使用
 * 
 * 通过 getDefaultSensor 获取 Sensor 对象，通过 registerListener 注册 SensorEventListener。
 * 通过 SensorEventListener.onSensorChanged 监控 Sensor 事件，
 * 通过 SensorEventListener.onAccuracyChanged 监控精度变化。
 *
 * 注意：这个demo只能在手表端使用，手机端不能使用。
 */
public class SensorFragment extends DemoFragment {
    private View mContentView;
    private Context mContext;

    private ServiceClient mClient;
    private SensorServiceManager mService;

    private Sensor mHeartRateSensor;
    private Sensor mStepCounterSensor;
    private Sensor mTempSensor;
    private Sensor mHumiSensor;
    private Sensor mPressureSensor;
    private Sensor mGestureSensor;
    private Sensor mMotionSensor;

    private TextView mStepsText;
    private TextView mHeartRateText;
    private TextView mTempText;
    private TextView mHumiText;
    private TextView mPressureText;
    private TextView mGestureText;
    private TextView mMotionText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mContext = getActivity();
        mContentView = inflater.inflate(R.layout.sensor, container, false);

        buildView();

        /* 创建 Sensor 服务的客户端 */
        mClient = new ServiceClient(mContext, ServiceManagerContext.SERVICE_SENSOR, mConnectionListener);

        return mContentView;
    }

    private void buildView() {
        mStepsText = (TextView) mContentView.findViewById(R.id.text_steps);
        mHeartRateText = (TextView) mContentView.findViewById(R.id.text_heart);
        mTempText = (TextView) mContentView.findViewById(R.id.text_temp);
        mHumiText = (TextView) mContentView.findViewById(R.id.text_humi);
        mPressureText = (TextView) mContentView.findViewById(R.id.text_pressure);
        mGestureText = (TextView) mContentView.findViewById(R.id.text_gesture);
        mMotionText = (TextView) mContentView.findViewById(R.id.text_motion);
    }

    @Override
    public void onResume() {
        super.onResume();

        mClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();

        mClient.disconnect();
    }

    private String gestureRawDataToString(Float data) {
        return gestureRawDataToString(data.intValue());
    }

    private String gestureRawDataToString(int data) {
        switch (data) {
        case 1:
            return "Wave Hand";
        case 2:
            return "Shake hand as greeting";
        case 3:
            return "Turn over A";
        case 4:
            return "Turn over B";
        case 5:
            return "Motion liken 8";
        case 6:
            return "Shank hand";
        case 7:
            return "Turn wrist fast";
        case 8:
            return "Turn wrist slow";
        case 9:
            return "Raise hand and look at watch";
        case 10:
            return "Raise hand fast and look at watch";
        case 11:
            return "Look at watch for a while";
        default:
            return "unknown";
        }
    }

    private String motionRawDataToString(Float data) {
        return motionRawDataToString(data.intValue());
    }

    private String motionRawDataToString(int data) {
        switch (data) {
        case 0:
            return "Rest";
        case 1:
            return "Stop";
        case 2:
            return "Walking";
        case 3:
            return "Running";
        case 4:
            return "Riding";
        default:
            return "unknown";
        }
    }

    private void initSensors() {
        /* 通过 getDefaultSensor 获取各个 Sensor */
        mHeartRateSensor = mService.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        mStepCounterSensor = mService.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mTempSensor = mService.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mHumiSensor = mService.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        mPressureSensor = mService.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mGestureSensor = mService.getDefaultSensor(Sensor.TYPE_GESTURE);
        mMotionSensor = mService.getDefaultSensor(Sensor.TYPE_MOTION);
    }

    private void registerSensors() {
        /* 通过 registerListener 注册 SensorEventListener 对象 */

        if (mHeartRateSensor != null) {
            mService.registerListener(mSensorListener, mHeartRateSensor, 0);
        }

        if (mStepCounterSensor != null) {
            mService.registerListener(mSensorListener, mStepCounterSensor, 0);
        }

        if (mTempSensor != null) {
            mService.registerListener(mSensorListener, mTempSensor, 0);
        }

        if (mHumiSensor != null) {
            mService.registerListener(mSensorListener, mHumiSensor, 0);
        }

        if (mPressureSensor != null) {
            mService.registerListener(mSensorListener, mPressureSensor, 0);
        }

        if (mGestureSensor != null) {
            mService.registerListener(mSensorListener, mGestureSensor, 0);
        }

        if (mMotionSensor != null) {
            mService.registerListener(mSensorListener, mMotionSensor, 0);
        }
    }

    private void unregisterSensors() {
        /* 通过 unregiserListener 注销 Listener */
    
        if (mHeartRateSensor != null) {
            mService.unregisterListener(mSensorListener, mHeartRateSensor);
        }

        if (mStepCounterSensor != null) {
            mService.unregisterListener(mSensorListener, mStepCounterSensor);
        }

        if (mTempSensor != null) {
            mService.unregisterListener(mSensorListener, mTempSensor);
        }

        if (mHumiSensor != null) {
            mService.unregisterListener(mSensorListener, mHumiSensor);
        }

        if (mPressureSensor != null) {
            mService.unregisterListener(mSensorListener, mPressureSensor);
        }

        if (mGestureSensor != null) {
            mService.unregisterListener(mSensorListener, mGestureSensor);
        }

        if (mMotionSensor != null) {
            mService.unregisterListener(mSensorListener, mMotionSensor);
        }
    }

    private ServiceClient.ConnectionCallbacks
            mConnectionListener = new ServiceClient.ConnectionCallbacks() {
        @Override
        public void onConnected(ServiceClient serviceClient) {
            IwdsLog.i(this, "Sensor service connected");

            mService = (SensorServiceManager) mClient.getServiceManagerContext();

            /* 获取所有Sensor列表 */
            ArrayList<Sensor> sensorList = (ArrayList<Sensor>) mService.getSensorList();

            IwdsLog.i(this, "=========================================");
            IwdsLog.i(this, "Dump Sensor List");
            for (int i = 0; i < sensorList.size(); i++) {
                IwdsLog.i(this, "Sensor: " + sensorList.get(i).toString());
            }
            IwdsLog.i(this, "=========================================");

            initSensors();
            registerSensors();
        }

        @Override
        public void onDisconnected(ServiceClient serviceClient, boolean unexpected) {
            IwdsLog.i(this, "Sensor service diconnected");

            unregisterSensors();
        }

        @Override
        public void onConnectFailed(ServiceClient serviceClient, ConnectFailedReason reason) {
            IwdsLog.i(this, "Sensor service connect fail");
        }
    };

    private SensorEventListener mSensorListener = new SensorEventListener() {
        /*
         * 监控 Sensor 数据变化
         */
        @Override
        public void onSensorChanged(SensorEvent event) {
            float data;

            switch (event.sensorType) {
            case Sensor.TYPE_HEART_RATE:
                IwdsLog.i(this, "Update Heart Rate");
                data = event.values[0];
                mHeartRateText.setText("Heart Rate: " + data);
                break;
            case Sensor.TYPE_STEP_COUNTER:
                IwdsLog.i(this, "Update Step Counter");
                data = event.values[0];
                mStepsText.setText("Steps: " + data);
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                IwdsLog.i(this, "Update Temp");
                data = event.values[0];
                mTempText.setText("Temp: " + data);
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                IwdsLog.i(this, "Update Humi");
                data = event.values[0];
                mHumiText.setText("Humi: " + data);
                break;
            case Sensor.TYPE_GESTURE:
                IwdsLog.i(this, "Update Gesture");
                data = event.values[0];
                mGestureText.setText("Gesture: " + gestureRawDataToString(data));
                break;
            case Sensor.TYPE_MOTION:
                IwdsLog.i(this, "Update Motion");
                data = event.values[0];
                mMotionText.setText("Motion: " + motionRawDataToString(data));
                break;
            case Sensor.TYPE_PRESSURE:
                IwdsLog.i(this, "Update Pressure");
                data = event.values[0];
                mPressureText.setText("Pressure: " + data);
                break;
            default:
                IwdsLog.e(this, "Unknown Type");
            }
        }

        /*
         * 监控 Sensor 数据精度变化
         */
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            IwdsLog.i(this, "onAccuracyChanged: " + sensor + ", accuracy: " + accuracy);
        }

    };
}
