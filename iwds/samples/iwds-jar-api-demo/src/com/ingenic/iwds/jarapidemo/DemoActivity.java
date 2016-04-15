package com.ingenic.iwds.jarapidemo;

import android.app.Fragment;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class DemoActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        String demoName = getIntent().getStringExtra("demo");
        if (demoName != null) {
            String className = getPackageName() + "." + demoName + "Fragment";
            Log.i("demo", "class:" + className);
            Fragment fragment = Fragment.instantiate(this, className);
            getFragmentManager().beginTransaction().add(R.id.container, fragment, "demo").commit();

            if (fragment instanceof DemoFragment) {
                ((DemoFragment) fragment).setDemoName(demoName);
            }
        } else {
            finish();
        }
    }
}
