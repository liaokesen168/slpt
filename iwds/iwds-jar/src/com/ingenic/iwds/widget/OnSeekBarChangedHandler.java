package com.ingenic.iwds.widget;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public abstract class OnSeekBarChangedHandler extends IOnSeekBarChangedHandler.Stub {

    private static final int HANDLE_START_TRACKING_TOUCH = 0;
    private static final int HANDLE_STOP_TRACKING_TOUCH = 1;
    private static final int HANDLE_PROGRESS_CHANGED = 2;

    private static final String EXTRA_FORM_USER = "fromUser";

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case HANDLE_START_TRACKING_TOUCH:
                onStartTrackingTouch((String) msg.obj, msg.arg1);
                break;

            case HANDLE_STOP_TRACKING_TOUCH:
                onStopTrackingTouch((String) msg.obj, msg.arg1);
                break;

            case HANDLE_PROGRESS_CHANGED:
                Bundle data = msg.getData();
                boolean fromUser = data.getBoolean(EXTRA_FORM_USER, false);
                onProgressChanged((String) msg.obj, msg.arg1, msg.arg2, fromUser);
                break;

            default:
                break;
            }
        }
    };

    @Override
    public final void handleProgressChanged(String callingPkg, int viewId, int progress,
            boolean fromUser) {
        Message msg = mHandler.obtainMessage(HANDLE_PROGRESS_CHANGED, viewId, progress, callingPkg);
        Bundle data = new Bundle();
        data.putBoolean(EXTRA_FORM_USER, fromUser);
        msg.setData(data);
        msg.sendToTarget();
    }

    @Override
    public final void handleStartTrackingTouch(String callingPkg, int viewId) {
        mHandler.obtainMessage(HANDLE_START_TRACKING_TOUCH, viewId, -1, callingPkg).sendToTarget();
    }

    @Override
    public final void handleStopTrackingTouch(String callingPkg, int viewId) {
        mHandler.obtainMessage(HANDLE_STOP_TRACKING_TOUCH, viewId, -1, callingPkg).sendToTarget();
    }

    protected abstract void onProgressChanged(String callingPkg, int viewId, int progress,
            boolean fromUser);

    protected abstract void onStartTrackingTouch(String callingPkg, int viewId);

    protected abstract void onStopTrackingTouch(String callingPkg, int viewId);
}
