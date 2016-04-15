/*
 * Copyright (C) 2015 Ingenic Semiconductor
 *
 * TaoZhang(Kevin)<tao.zhang@ingenic.com>
 *
 * Elf/iwds-jar Project
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package com.ingenic.iwds.widget;

import com.ingenic.iwds.widget.AbsListView.OnScrollListener;

/**
 * 用于圆屏时，滑动{@link AmazingListView}的同时对Item做刷新处理。
 * <p>
 * 该类作为基类使用；你应该不需要在你的代码中直接使用该类。
 * @author tZhang
 */
public class RoundListOnScrollListener implements OnScrollListener {

    private AbsListView mListView;
    private OnScrollListener mListener;

    public RoundListOnScrollListener(OnScrollListener listener,
            AbsListView listView) {
        mListener = listener;
        mListView = listView;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mListener != null) {
            mListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        if (mListener != null) {
            mListener.onScroll(view, firstVisibleItem, visibleItemCount,
                    totalItemCount);
        }

        // 刷新Item
        for (int i = 0; i < mListView.getChildCount(); i++) {
            mListView.getChildAt(i).invalidate();
        }
    }

}
