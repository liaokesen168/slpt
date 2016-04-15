package com.ingenic.iwds.apidemo;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ingenic.iwds.widget.AmazingProgressBar;
import com.ingenic.iwds.widget.AmazingRingProgressView;

public class ProgressFragment extends DemoFragment {

    private View mContentView;

    private AmazingProgressBar progressBar;
    private AmazingRingProgressView ringProgressView;

    private int mTotalProgress = 100;

    private int mCurrentProgress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.amazing_progress, null);

        init();

        return mContentView;
    }

    /**
     * 初始化
     */
    private void init() {
        progressBar = (AmazingProgressBar) mContentView.findViewById(R.id.progressbar);
        ringProgressView = (AmazingRingProgressView) mContentView
                .findViewById(R.id.ring_progressbar);
        ringProgressView.setPercentEnable(true);
        mHandler.sendEmptyMessageDelayed(0, 100);
    }

    /**
     * 改变进度条进度的Handler
     */
    private Handler mHandler = new Handler() {

        public void handleMessage(android.os.Message msg) {// 不超出最大进度
            if (msg.what == 0 && mCurrentProgress < mTotalProgress) {
                    // 步进值为1
                    mCurrentProgress ++;
                    // 设置进度
                    ringProgressView.setProgress(mCurrentProgress);
                    mHandler.sendEmptyMessageDelayed(0, 50);
            }
        };

    };

    @Override
    public void onDestroyView() {
        if(null!=progressBar){
            progressBar.stop();
        }
        super.onDestroyView();
    }

}
