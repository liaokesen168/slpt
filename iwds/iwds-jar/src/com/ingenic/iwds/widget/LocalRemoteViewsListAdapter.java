/*
 * Copyright (C) 2015 Ingenic Semiconductor
 * 
 * LiJinWen(Kevin)<kevin.jwli@ingenic.com>
 * 
 * Elf/iwds-jar
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.ingenic.iwds.widget;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class LocalRemoteViewsListAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<LocalRemoteViews> mList;
    private ArrayList<Integer> mTypes = new ArrayList<Integer>();
    private int mTypeCount;

    public LocalRemoteViewsListAdapter(Context context, ArrayList<LocalRemoteViews> list,
            int typeCount) {
        mContext = context;
        mList = list;
        mTypeCount = typeCount;
        init();
    }

    private void init() {
        if (mList == null) return;

        mTypes.clear();
        for (LocalRemoteViews lrv : mList) {
            int type = lrv.getLayoutId();

            if (!(mTypes.contains(type))) {
                mTypes.add(type);
            }
        }

        if (mTypes.size() >= mTypeCount || mTypeCount < 1) {
            throw new RuntimeException("Invalid view type count -- view type count must be >= 1"
                    + "and must be as large as the total number of distinct view types");
        }
    }

    public void setViewList(ArrayList<LocalRemoteViews> list) {
        mList = list;
        init();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList != null ? mList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position < getCount()) {
            LocalRemoteViews lrv = mList.get(position);
            lrv.setIsWidgetCollectionChild(true);

            View v;
            if (convertView != null && convertView.getId() == lrv.getLayoutId()) {
                v = convertView;
                lrv.reapply(mContext, v);
            } else {
                v = lrv.apply(mContext, parent);
            }

            return v;
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < getCount()) {
            int layoutId = mList.get(position).getLayoutId();
            return mTypes.indexOf(layoutId);
        }

        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return mTypeCount;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}