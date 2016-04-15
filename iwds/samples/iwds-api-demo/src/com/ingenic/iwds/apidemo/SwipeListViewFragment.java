package com.ingenic.iwds.apidemo;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ingenic.iwds.widget.AdapterView;
import com.ingenic.iwds.widget.AmazingSwipeListView;
import com.ingenic.iwds.widget.AmazingToast;

public class SwipeListViewFragment extends DemoFragment implements
        AdapterView.OnItemClickListener,
        AmazingSwipeListView.OnItemDeleteListener {
    private AmazingSwipeListView mListView;
    private SwipeListAdapter mAdapter;
    private List<String> mItems = new ArrayList<String>();

    public void onAttach(android.app.Activity activity) {
        super.onAttach(activity);

        for (int i = 0; i < 10; i++) {
            mItems.add("Item " + i);
        }

        mAdapter = new SwipeListAdapter();
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.swipe_list, container, false);
        mListView = (AmazingSwipeListView) rootView
                .findViewById(R.id.swipe_list_view);
        View emptyView = rootView.findViewById(R.id.empty);
        mListView.setEmptyView(emptyView);
        TextView view = new TextView(getActivity());
        view.setTextAppearance(getActivity(),
                android.R.style.TextAppearance_Large);
        view.setGravity(Gravity.CENTER);
        view.setText(mDemoName);
        mListView.addHeaderView(view);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemDeleteListener(this);
        mListView.setSelector(R.drawable.list_item_selector);
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        AmazingToast.showToast(getActivity(),
                "Click On " + mAdapter.getItem(position),
                AmazingToast.LENGTH_SHORT, AmazingToast.BOTTOM_CENTER);
    }

    @Override
    public void onDelete(View view, int position) {

        AmazingToast.showToast(getActivity(), "Delete " + mItems.get(position),
                AmazingToast.LENGTH_SHORT, AmazingToast.BOTTOM_CENTER);

        mItems.remove(position);
        mAdapter.notifyDataSetChanged();
    }

    private class SwipeListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(
                        R.layout.list_item, null);
            }

            TextView text = (TextView)convertView.findViewById(R.id.item_text);
            // text.setTextColor(getActivity().getResources()
            // .getColorStateList(R.color.list_item_text_selector));
            text.setText(mItems.get(position));

            return convertView;
        }

    }

}
