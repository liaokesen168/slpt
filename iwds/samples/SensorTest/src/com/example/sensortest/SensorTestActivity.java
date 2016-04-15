package com.example.sensortest;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

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
 * 通过 SensorEventListener.onSensorChanged 监控 Sensor 事件， 通过
 * SensorEventListener.onAccuracyChanged 监控精度变化。
 * 
 * 注意：这个demo只能在手表端使用，手机端不能使用。
 */
public class SensorTestActivity extends Activity implements
        ServiceClient.ConnectionCallbacks {
    private final static String TAG = "IWDS ---> SensorTestActivity";

    private ServiceClient mClient;
    private SensorServiceManager mService;

    private Sensor mHeartRateSensor;
    private Sensor mStepCounterSensor;
    private Sensor mTempSensor;
    private Sensor mHumiSensor;
    private Sensor mPressureSensor;
    private Sensor mGestureSensor;
    private Sensor mMotionSensor;
    private Sensor mUvSensor;
    private Sensor mVoiceTriggerSensor;
    private Sensor mProximitySensor;

    private TextView mStepsText;
    private TextView mHeartRateText;
    private TextView mTempText;
    private TextView mHumiText;
    private TextView mPressureText;
    private TextView mGestureText;
    private TextView mMotionText;
    private TextView mUvText;
    private TextView mVoiceText;
    private TextView mProximityText;

    private float mHeartRateData;
    private float mLastHeartRateData;
    private float mStepsData;
    private float mTempData;
    private float mHumiData;
    private float mPressureData;
    private float mGestureData;
    private float[] mMotionData = new float[2];
    private long mMotionTime;
    private float mUvData;
    private float mVoiceData;
    private float mProximityData;
    private float mProximityApproach;

    private WakeLock mWakeLock;

    private void buildView() {
        mStepsText = (TextView) findViewById(R.id.text_steps);
        mHeartRateText = (TextView) findViewById(R.id.text_heart);
        mTempText = (TextView) findViewById(R.id.text_temp);
        mHumiText = (TextView) findViewById(R.id.text_humi);
        mPressureText = (TextView) findViewById(R.id.text_pressure);
        mGestureText = (TextView) findViewById(R.id.text_gesture);
        mMotionText = (TextView) findViewById(R.id.text_motion);
        mUvText = (TextView) findViewById(R.id.text_uv);
        mVoiceText = (TextView) findViewById(R.id.text_voice);
        mProximityText = (TextView) findViewById(R.id.text_proximity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);

        mClient = new ServiceClient(this, ServiceManagerContext.SERVICE_SENSOR,
                this);

        mClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");

        super.onDestroy();
        mClient.disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onConnected(ServiceClient serviceClient) {
        Log.d(TAG, "Sensor service connected");

        mService = (SensorServiceManager) serviceClient
                .getServiceManagerContext();

        /* 获取所有Sensor列表 */
        ArrayList<Sensor> sensorList = (ArrayList<Sensor>) mService
                .getSensorList();

        Log.d(TAG, "=========================================");
        Log.d(TAG, "Dump Sensor List");
        for (int i = 0; i < sensorList.size(); i++) {
            Log.d(TAG, "Sensor: " + sensorList.get(i).toString());
        }
        Log.d(TAG, "=========================================");

        registerSensors();
        buildView();
    }

    @Override
    public void onDisconnected(ServiceClient serviceClient, boolean unexpected) {
        Log.d(TAG, "Sensor service diconnected");

        unregisterSensors();
    }

    @Override
    public void onConnectFailed(ServiceClient serviceClient,
            ConnectFailedReason reason) {
        Log.d(TAG, "Sensor service connect fail");
    }

    private String gestureRawDataToString(int data) {
        String str;
        if (data == SensorEvent.GESTURE_SHAKE_HAND) {
            str = "Shake Hand-->code: ";
        } else if (data == SensorEvent.GESTURE_RAISE_HAND_AND_LOOK) {
            str = "Raise Hand and look-->code: ";
        } else if (data == SensorEvent.GESTURE_LET_HAND_DOWN_AFTER_LOOK) {
            str = "Let Hand down after Raise-->code: ";
        } else if (data == SensorEvent.GESTURE_TURN_WRIST) {
            str = "Turn Wrist-->code: ";
        } else {
            str = "unknown-->code: ";
        }
        str = str + data;
        return str;
    }

    private String motionRawDataToString(int data) {
        String str;
        if (data == SensorEvent.MOTION_RESET) {
            str = "Rest-->code: ";
        } else if (data == SensorEvent.MOTION_STOP) {
            str = "Stop-->code: ";
        } else if (data == SensorEvent.MOTION_WALK) {
            str = "Walk-->code: ";
        } else if (data == SensorEvent.MOTION_RUN) {
            str = "Run-->code: ";
        } else if (data == SensorEvent.MOTION_SLEEP) {
            str = "SLEEP-->code: ";
        } else if (data == SensorEvent.MOTION_DEEP_SLEEP) {
            str = "DEEP_SLEEP-->code: ";
        } else {
            str = "unknown-->code: ";
        }
        str = str + data;
        return str;
    }

    private void registerSensors() {
        /* 通过 getDefaultSensor 获取各个 Sensor */
        mHeartRateSensor = mService.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        if (mHeartRateSensor != null) {
            mService.registerListener(mListener, mHeartRateSensor, 0);
        }

        mStepCounterSensor = mService
                .getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (mStepCounterSensor != null) {
            mService.registerListener(mListener, mStepCounterSensor, 0);
        }

        mTempSensor = mService
                .getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (mTempSensor != null) {
            mService.registerListener(mListener, mTempSensor, 0);
        }

        mHumiSensor = mService.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        if (mHumiSensor != null) {
            mService.registerListener(mListener, mHumiSensor, 0);
        }

        mPressureSensor = mService.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (mPressureSensor != null) {
            mService.registerListener(mListener, mPressureSensor, 0);
        }

        mGestureSensor = mService.getDefaultSensor(Sensor.TYPE_GESTURE);
        if (mGestureSensor != null) {
            mService.registerListener(mListener, mGestureSensor, 0);
        }

        mMotionSensor = mService.getDefaultSensor(Sensor.TYPE_MOTION);
        if (mMotionSensor != null) {
            mService.registerListener(mListener, mMotionSensor, 0);
        }

        mUvSensor = mService.getDefaultSensor(Sensor.TYPE_UV);
        if (mUvSensor != null) {
            mService.registerListener(mListener, mUvSensor, 0);
        }

        mVoiceTriggerSensor = mService
                .getDefaultSensor(Sensor.TYPE_VOICE_TRIGGER);
        if (mVoiceTriggerSensor != null) {
            mService.registerListener(mListener, mVoiceTriggerSensor, 0);
        }

        mProximitySensor = mService.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (mProximitySensor != null) {
            mService.registerListener(mListener, mProximitySensor, 0);
        }
    }

    private void unregisterSensors() {
        if (mHeartRateSensor != null) {
            mService.unregisterListener(mListener, mHeartRateSensor);
        }

        if (mStepCounterSensor != null) {
            mService.unregisterListener(mListener, mStepCounterSensor);
        }

        if (mTempSensor != null) {
            mService.unregisterListener(mListener, mTempSensor);
        }

        if (mHumiSensor != null) {
            mService.unregisterListener(mListener, mHumiSensor);
        }

        if (mPressureSensor != null) {
            mService.unregisterListener(mListener, mPressureSensor);
        }

        if (mGestureSensor != null) {
            mService.unregisterListener(mListener, mGestureSensor);
        }

        if (mMotionSensor != null) {
            mService.unregisterListener(mListener, mMotionSensor);
        }

        if (mUvSensor != null) {
            mService.unregisterListener(mListener, mUvSensor);
        }

        if (mVoiceTriggerSensor != null)
            mService.unregisterListener(mListener, mVoiceTriggerSensor);

        if (mProximitySensor != null) {
            mService.unregisterListener(mListener, mProximitySensor);
        }
    }

    private SensorEventListener mListener = new SensorEventListener() {

        /*
         * 监控 Sensor 数据变化
         */
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensorType == Sensor.TYPE_HEART_RATE) {
                if (mLastHeartRateData == event.values[0]) {
                    return;
                }
                Log.d(TAG, "Update Heart Rate : " + event.values[0]);
                mLastHeartRateData = event.values[0];
                mHeartRateData = event.values[0];
            } else if (event.sensorType == Sensor.TYPE_STEP_COUNTER) {
                Log.d(TAG, "Update Step Counter : " + event.values[0]);
                mStepsData = event.values[0];
            } else if (event.sensorType == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                Log.d(TAG, "Update Temp : " + event.values[0]);
                mTempData = event.values[0];
            } else if (event.sensorType == Sensor.TYPE_RELATIVE_HUMIDITY) {
                Log.d(TAG, "Update Humi : " + event.values[0]);
                mHumiData = event.values[0];
            } else if (event.sensorType == Sensor.TYPE_GESTURE) {
                Log.d(TAG, "Update Gesture : " + event.values[0]);
                mGestureData = event.values[0];
            } else if (event.sensorType == Sensor.TYPE_MOTION) {
                Log.d(TAG, "Frizz Update Motion : time: " + event.timestamp
                		+ ", data[0]: "+ event.values[0] + ", data[1]: "+ event.values[1]);
                mMotionData[0] = event.values[0];
                mMotionData[1] = event.values[1];
                mMotionTime = event.timestamp;
            } else if (event.sensorType == Sensor.TYPE_PRESSURE) {
                Log.d(TAG, "Update Pressure : " + event.values[0]);
                mPressureData = event.values[0];
            } else if (event.sensorType == Sensor.TYPE_UV) {
                Log.d(TAG, "Update Uv : " + event.values[0]);
                mUvData = event.values[0];
            } else if (event.sensorType == Sensor.TYPE_VOICE_TRIGGER) {
                Log.d(TAG, "Update voice trigger: " + event.values[0]);
                mVoiceData = event.values[0];
                if (mVoiceData == SensorEvent.EVENT_WAKE_UP)
                    if (!mWakeLock.isHeld())
                        mWakeLock.acquire(1000);
            } else if (event.sensorType == Sensor.TYPE_PROXIMITY) {
                Log.d(TAG, "Update Proximity : " + event.values[0]);
                mProximityData = event.values[0];
                mProximityApproach = event.values[1];
            } else {
                Log.e(TAG, "Unknown Type : " + event.sensorType);
            }

            mStepsText.setText("Steps: " + mStepsData);
            mHeartRateText.setText("Heart Rate: " + mHeartRateData);
            mTempText.setText("Temp: " + mTempData);
            mHumiText.setText("Humi: " + mHumiData);
            mPressureText.setText("Pressure: " + mPressureData);
            mGestureText.setText("Gesture:\n"
                    + gestureRawDataToString((int) mGestureData));
            mMotionText.setText("Motion:\n"
                    + motionRawDataToString((int) mMotionData[0])
                    + "\ntossAndTurn_cnt: " + (int) mMotionData[1]
                    + "\ntimestamp: " + mMotionTime);
            mUvText.setText("Uv: " + mUvData);
            mVoiceText.setText("Voice: " + mVoiceData);
            mProximityText.setText("Proximity: " + mProximityData
                    + (mProximityApproach == 1 ? "靠近" : "远离"));
        }

        /*
         * 监控 Sensor 数据精度变化
         */
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(TAG, "onAccuracyChanged: " + sensor + ", accuracy: "
                    + accuracy);
            if (sensor.getType() == Sensor.TYPE_HEART_RATE) {
                if (accuracy == SensorEvent.ACCURACY_HEART_RATE_UNAVALIABLE) {
                    Toast.makeText(SensorTestActivity.this,
                            "Make sure the watch wear well", Toast.LENGTH_SHORT)
                            .show();
                    ;
                } else if (accuracy == SensorEvent.ACCURACY_HEART_RATE_AVALIABLE) {
                    Toast.makeText(SensorTestActivity.this, "Watch wear well",
                            Toast.LENGTH_SHORT).show();
                    ;
                }
            }
        }

    };
}
