package com.ingenic.iwds.jarapidemo;

import android.app.Fragment;

public class DemoFragment extends Fragment {
    CharSequence mDemoName;

    public void setDemoName(CharSequence demoName) {
        mDemoName = demoName;
    }

    public CharSequence getDemoName() {
        return mDemoName;
    }
}
