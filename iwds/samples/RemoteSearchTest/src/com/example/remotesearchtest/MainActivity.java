package com.example.remotesearchtest;

import com.example.remotesearchtest.busline.BusStationActivity;
import com.example.remotesearchtest.busline.BuslineActivity;
import com.example.remotesearchtest.district.DistrictActivity;
import com.example.remotesearchtest.geocoder.GeocoderActivity;
import com.example.remotesearchtest.poisearch.PoiAroundSearchActivity;
import com.example.remotesearchtest.poisearch.PoiKeywordSearchActivity;
import com.example.remotesearchtest.route.RouteActivity;
import com.example.remotesearchtest.view.FeatureView;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class MainActivity extends ListActivity {

    private static class DemoDetails {
        private final int titleId;
        private final int descriptionId;
        private final Class<? extends Activity> activityClass;

        public DemoDetails(int titleId, int descriptionId,
                Class<? extends Activity> activityClass) {
            super();
            this.titleId = titleId;
            this.descriptionId = descriptionId;
            this.activityClass = activityClass;
        }
    }

    private static class CustomArrayAdapter extends ArrayAdapter<DemoDetails> {
        public CustomArrayAdapter(Context context, DemoDetails[] demos) {
            super(context, R.layout.feature, R.id.title, demos);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FeatureView featureView;
            if (convertView instanceof FeatureView) {

                featureView = (FeatureView) convertView;
            } else {
                featureView = new FeatureView(getContext());
            }
            DemoDetails demo = getItem(position);
            featureView.setTitleId(demo.titleId);
            featureView.setDescriptionId(demo.descriptionId);
            return featureView;
        }
    }
 
    private static final DemoDetails[] demos = {
            new DemoDetails(R.string.geocoder_demo,
                    R.string.geocoder_description, GeocoderActivity.class),

            new DemoDetails(R.string.district_demo,
                    R.string.district_description, DistrictActivity.class),

            new DemoDetails(R.string.poikeywordsearch_demo,
                    R.string.poikeywordsearch_description,
                    PoiKeywordSearchActivity.class),

            new DemoDetails(R.string.poiaroundsearch_demo,
                    R.string.poiaroundsearch_description,
                    PoiAroundSearchActivity.class),

            new DemoDetails(R.string.busline_demo,
                    R.string.busline_description, BuslineActivity.class),

            new DemoDetails(R.string.busstation_demo,
                    R.string.busstation_description, BusStationActivity.class),

            new DemoDetails(R.string.route_demo, R.string.route_description,
                    RouteActivity.class) };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        ListAdapter adapter = new CustomArrayAdapter(
                this.getApplicationContext(), demos);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        DemoDetails demo = (DemoDetails) getListAdapter().getItem(position);
        startActivity(new Intent(this.getApplicationContext(),
                demo.activityClass));
    }
}
