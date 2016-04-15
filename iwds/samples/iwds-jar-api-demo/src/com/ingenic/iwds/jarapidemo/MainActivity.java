package com.ingenic.iwds.jarapidemo;

import java.util.Comparator;

import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.AdapterView;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener{
    private ListView mListView;
    private ArrayAdapter<CharSequence> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.list);
        String[] apis = getResources().getStringArray(R.array.apis);

        mAdapter = new ArrayAdapter<CharSequence>(this, R.layout.list_item, apis);
        /*
        mAdapter.sort(new Comparator<CharSequence>() {
            @Override
            public int compare(CharSequence lhs, CharSequence rhs) {
                return lhs.toString().compareTo(rhs.toString());
            }
        });
        */
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
