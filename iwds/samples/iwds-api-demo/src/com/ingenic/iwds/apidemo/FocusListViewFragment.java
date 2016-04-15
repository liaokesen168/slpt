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

import com.ingenic.iwds.widget.AdapterView;
import com.ingenic.iwds.widget.AmazingFocusListView;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FocusListViewFragment extends DemoFragment implements AdapterView.OnItemClickListener,
        AmazingFocusListView.OnFocusChangedListener {

    private AmazingFocusListView mListView;
    private ArrayAdapter<CharSequence> mAdapter;
    private String[] mItems = new String[10];

    public void onAttach(android.app.Activity activity) {
        super.onAttach(activity);

        for (int i = 0; i < mItems.length; i++) {
            mItems[i] = "Item" + i;
        }
        mAdapter = new ArrayAdapter<CharSequence>(activity, R.layout.list_item, R.id.item_text, mItems);
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_main, container, false);
        mListView = (AmazingFocusListView) rootView.findViewById(R.id.list);
        mListView.setSelector(R.drawable.list_item_selector);
        TextView view = new TextView(getActivity());
        view.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large);
        view.setGravity(Gravity.CENTER);
        view.setText(mDemoName);
        mListView.addHeaderView(view);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnFocusChangedListener(this);
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), "Click on " + mAdapter.getItem(position), Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onFocusChanged(AmazingFocusListView view, int focus, int oldFocus) {
        if (oldFocus != -1) {
            mItems[oldFocus] = "Item" + oldFocus;
        }

        if (focus != -1) {
            mItems[focus] = "Focus" + focus;
        }

        mAdapter.notifyDataSetChanged();
    }
}
