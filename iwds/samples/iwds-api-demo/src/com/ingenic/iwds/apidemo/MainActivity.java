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

import java.util.Comparator;

import com.ingenic.iwds.app.RightScrollActivity;
import com.ingenic.iwds.widget.AdapterView;
import com.ingenic.iwds.widget.AmazingListView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MainActivity extends RightScrollActivity implements AdapterView.OnItemClickListener {
    private AmazingListView mListView;
    private ArrayAdapter<CharSequence> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (AmazingListView) findViewById(R.id.list);

        // mListView.setDivider(getResources().getDrawable(
        // android.R.drawable.divider_horizontal_dim_dark));
        // mListView.setDividerHeight(4);
        // mListView.setHeaderDividersEnabled(true);

        mListView.setSelector(R.drawable.list_item_selector);

        TextView view = new TextView(this);
        view.setTextAppearance(this, android.R.style.TextAppearance_Large);
        view.setGravity(Gravity.CENTER);
        view.setText(R.string.app_name);
        mListView.addHeaderView(view);

        String[] apis = getResources().getStringArray(R.array.apis);
        mAdapter = new ArrayAdapter<CharSequence>(this, R.layout.list_item, R.id.item_text, apis);
        mAdapter.sort(new Comparator<CharSequence>() {
            @Override
            public int compare(CharSequence lhs, CharSequence rhs) {
                return lhs.toString().compareTo(rhs.toString());
            }
        });
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        startDemo(mAdapter.getItem(position));
    }

    private void startDemo(CharSequence demoName) {
        if (demoName == null) return;
        Intent intent = new Intent(this, DemoActivity.class);
        intent.putExtra("demo", demoName);
        startActivity(intent);
    }
}
