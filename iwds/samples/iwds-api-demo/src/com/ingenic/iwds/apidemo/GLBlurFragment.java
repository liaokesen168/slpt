/*
 * Copyright (C) 2015 Ingenic Semiconductor
 * 
 * LiJinWen(Kevin)<kevin.jwli@ingenic.com>
 * 
 * Elf/iwds-ui-jar
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.ingenic.iwds.apidemo;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ingenic.iwds.widget.BlurGLSurfaceView;

public class GLBlurFragment extends DemoFragment {
    private BlurGLSurfaceView mBlurView;
    private View mBaseImageView;
    private Handler mHandler;
    private Runnable mRotationRunnable = new Runnable() {

        @Override
        public void run() {
            if (mHandler == null) return;

            if (mBaseImageView != null) {
                float rotation = mBaseImageView.getRotation();
                mBaseImageView.setRotation(rotation + 6);
            }

            mHandler.postDelayed(this, 1000);
        }

    };

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mHandler == null) {
            mHandler = new Handler();
        }

        mHandler.postDelayed(mRotationRunnable, 1000);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.gl_blur, container, false);
        mBlurView = (BlurGLSurfaceView) rootView.findViewById(R.id.blur);
        mBlurView.setBaseView(rootView.findViewById(R.id.base));

        mBaseImageView = rootView.findViewById(R.id.base_image);

        final View status = rootView.findViewById(R.id.text);
        ValueAnimator animator = ValueAnimator.ofInt(0, 320, 0);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setDuration(3000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = ((Integer) animation.getAnimatedValue()).intValue();
                status.setTranslationY(-320 + value);
                mBlurView.setBlurArea(0, 0, 320, value);
            }
        });
        animator.start();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mBlurView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mBlurView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacks(mRotationRunnable);
            mHandler = null;
        }

        mBlurView.onDestroy();
    }
}