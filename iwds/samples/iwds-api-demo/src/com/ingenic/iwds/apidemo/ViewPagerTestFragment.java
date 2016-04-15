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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ingenic.iwds.widget.AmazingToast;
import com.ingenic.iwds.widget.AmazingViewPager;

public class ViewPagerTestFragment extends DemoFragment implements
        AmazingViewPager.OnPageChangedListener {
    private AmazingViewPager mPager;
    private ViewPagerAdapter mAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mAdapter = new ViewPagerAdapter(activity, R.layout.view_pager_item);
        String[] entries = new String[5];
        for (int i = 0; i < entries.length; i++) {
            entries[i] = "Pager" + i;
        }
        mAdapter.addAll(entries);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_pager, container, false);
        mPager = (AmazingViewPager) rootView.findViewById(R.id.pager);
        Intent it = getActivity().getIntent();
        mPager.setTouchOrientation(it.getIntExtra("touch", AmazingViewPager.HORIZONTAL));
        mPager.setCircularEnabled(it.getBooleanExtra("circle", true));
        mPager.setAdapter(mAdapter);
        mPager.setOnPageChangedListener(this);
        return rootView;
    }

    private static class ViewPagerAdapter extends ArrayAdapter<CharSequence> {

        private int mResource;

        public ViewPagerAdapter(Context context, int resource) {
            super(context, resource);
            mResource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(mResource, parent, false);
            }

            TextView view = (TextView) convertView.findViewById(android.R.id.text1);
            view.setText(getItem(position));

            return convertView;
        }

    }

    @Override
    public void onPageChanged(AmazingViewPager pager, int currentPage, int oldPage) {
        AmazingToast.showToast(getActivity(), "Current page is " + currentPage,
                AmazingToast.LENGTH_SHORT, AmazingToast.BOTTOM_CENTER);
        Activity activity = getActivity();
        if (activity instanceof DemoActivity) {
            if (pager.getTouchOrientation() == AmazingViewPager.HORIZONTAL) {
                if (currentPage == 0) {
                    ((DemoActivity) activity).enableRightScroll();
                } else {
                    ((DemoActivity) activity).disableRightScroll();
                }
            } else {
                ((DemoActivity) activity).enableRightScroll();
            }
        }
    }
}
