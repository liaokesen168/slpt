package com.ingenic.iwds.apidemo;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ingenic.iwds.app.AmazingDialog;
import com.ingenic.iwds.app.AmazingIndeterminateProgressDialog;
import com.ingenic.iwds.widget.AdapterView;
import com.ingenic.iwds.widget.AmazingListView;
import com.ingenic.iwds.widget.AmazingToast;

public class DialogFragment extends DemoFragment implements
        AdapterView.OnItemClickListener {

    private Context mContext;
    private View mContentView;
    private AmazingListView mListView;
    private ArrayAdapter<CharSequence> mAdapter;

    /**
     * 图片按钮Dialog
     */
    private AmazingDialog dialog;

    /**
     * 文字按钮Dialog
     */
    private AmazingDialog dialog2;

    private AmazingIndeterminateProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mContext = getActivity();

        mContentView = inflater.inflate(R.layout.amazing_dialog, null);

        init();

        return mContentView;
    }

    /**
     * 初始化
     */
    private void init() {
        mListView = (AmazingListView) mContentView
                .findViewById(R.id.dialog_list);
        mListView.setSelector(R.drawable.list_item_selector);
        TextView view = new TextView(mContext);
        view.setTextAppearance(mContext, android.R.style.TextAppearance_Large);
        view.setGravity(Gravity.CENTER);
        view.setText(R.string.dialog_name);
        mListView.addHeaderView(view);
        String[] apis = getResources().getStringArray(R.array.dialogs);
        mAdapter = new ArrayAdapter<CharSequence>(mContext, R.layout.list_item,
                R.id.item_text, apis);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        // 文字按钮样式
        dialog2 = new AmazingDialog(mContext)
                .setContent(R.string.dialog_content2)
//                .setRightScrollEnable(false)
                .setNegativeTextButton(AmazingDialog.USE_NORMAL_TEXT,
                        new View.OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                AmazingToast.showToast(mContext,
                                        "Click Cancel",
                                        AmazingToast.LENGTH_SHORT,
                                        AmazingToast.BOTTOM_CENTER);
                                dialog2.dismiss();
                            }
                        })
                .setPositiveTextButton(AmazingDialog.USE_NORMAL_TEXT,
                        new View.OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                AmazingToast.showToast(mContext, "Click OK",
                                        AmazingToast.LENGTH_SHORT,
                                        AmazingToast.BOTTOM_CENTER);
                                dialog2.dismiss();
                            }
                        })
                ;

        // 图片按钮样式
        dialog = new AmazingDialog(mContext)
                .setContent(R.string.dialog_content)
                .setNegativeButton(AmazingDialog.USE_NORMAL_DRAWABLE,
                        new View.OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                AmazingToast.showToast(mContext,
                                        "Click Cancel",
                                        AmazingToast.LENGTH_SHORT,
                                        AmazingToast.BOTTOM_CENTER);
                                dialog.dismiss();
                            }
                        })
                .setPositiveButton(AmazingDialog.USE_NORMAL_DRAWABLE,
                        new View.OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                dialog2.show();
                            }
                        });

        progressDialog = new AmazingIndeterminateProgressDialog(mContext,
                getString(R.string.loading));
        progressDialog.setCancelable(true);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {

        CharSequence demoName = mAdapter.getItem(position);
        if ("AmazingDialog".equals(demoName)) {
            dialog.show();
        } else if ("AmazingProgressDialog".equals(demoName)) {
            progressDialog.show();
        }
    }

}
