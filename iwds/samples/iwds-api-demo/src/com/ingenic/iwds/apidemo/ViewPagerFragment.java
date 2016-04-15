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

import com.ingenic.iwds.widget.AmazingViewPager;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

public class ViewPagerFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {
    private RadioGroup mTouchTypeGroup;
    private int mTouchType = AmazingViewPager.HORIZONTAL;
    private CheckBox mCircleAbleToogle;
    boolean mCircleable = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_pager_config, container, false);
        mTouchTypeGroup = (RadioGroup) rootView.findViewById(R.id.touch_type);
        mTouchTypeGroup.setOnCheckedChangeListener(this);
        rootView.findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DemoActivity.class);
                intent.putExtra("demo", "ViewPagerTest");
                intent.putExtra("touch", mTouchType);
                intent.putExtra("circle", mCircleable);
                startActivity(intent);
            }
        });
        mCircleAbleToogle = (CheckBox) rootView.findViewById(R.id.circleable);
        mCircleAbleToogle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCircleable = isChecked;
            }
        });

        return rootView;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group == mTouchTypeGroup) {
            switch (checkedId) {
            case R.id.type_horizontal:
                mTouchType = AmazingViewPager.HORIZONTAL;
                break;
            case R.id.type_vertical:
                mTouchType = AmazingViewPager.VERTICAL;
                break;
            }
        }
    }
}
