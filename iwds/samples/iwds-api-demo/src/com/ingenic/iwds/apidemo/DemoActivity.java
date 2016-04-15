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

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import com.ingenic.iwds.app.RightScrollActivity;

public class DemoActivity extends RightScrollActivity {
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

    public void enableRightScroll() {
        getRightScrollView().enableRightScroll();
    }

    public void disableRightScroll() {
        getRightScrollView().disableRightScroll();
    }
}
