package com.cs160.joleary.catnip;

/**
 * Created by namhyun on 3/3/16.
 */

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItemAsset;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GridViewFragmentActivity extends FragmentActivity implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    String zipCodeString;
    String county;
    String state;
    List<Legislator> legislators = new ArrayList<Legislator>();


    HashMap<Legislator, Drawable> legislatorImages = new HashMap<>();

    private GoogleApiClient mGoogleApiClient;

    /* put this into your activity class */

    private SensorManager mSensorManager;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity
    private SensorEventListener mSensorListener;

    private void reset() {
        mAccel = 0f;
        mAccelCurrent = 0f;
        mAccelLast= 0f;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        // init sensor with callback function
        this.mSensorListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent se) {
                float x = se.values[0];
                float y = se.values[1];
                float z = se.values[2];
                mAccelLast = mAccelCurrent;
                mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
                float delta = mAccelCurrent - mAccelLast;
                mAccel = mAccel * 0.9f + delta; // perform low-cut filter
                if (mAccel > 12) {
                    Intent intent = new Intent(getBaseContext(), RequestRandomZipCodeService.class);
                    startService(intent);
                    reset();
                }
            }
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        legislators = getIntent().getParcelableArrayListExtra("legislators");
        if (legislators.size() > 0){
            zipCodeString = legislators.get(0).zipCodeString;
            county = legislators.get(0).county;
            state = legislators.get(0).state;
        }

        updateView();

    }

    public void updateView() {


        final DotsPageIndicator mPageIndicator;
        final GridViewPager mViewPager;

        // Get UI references
        mPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        mViewPager = (GridViewPager) findViewById(R.id.pager);

        // Assigns an adapter to provide the content for this pager
        mViewPager.setAdapter(new GridPagerAdapter(getFragmentManager(), legislators, legislatorImages));
        mPageIndicator.setPager(mViewPager);
    }

    public void requestVoteView(View view) {
        if (zipCodeString != null) {
            Intent intent = new Intent(this, RequestVoteViewService.class);
            intent.putExtra("state", state);
            intent.putExtra("county", county);
            startService(intent);
        }
    }

    private static final class GridPagerAdapter extends FragmentGridPagerAdapter {

        List<Legislator> legislators;
        HashMap<Legislator, Drawable> legislatorImages;

        private GridPagerAdapter(FragmentManager fm, List<Legislator> _legislators, HashMap<Legislator, Drawable> legislatorImages) {
            super(fm);
            this.legislators = _legislators;
            this.legislatorImages = legislatorImages;
        }

        @Override
        public Fragment getFragment(int row, int column) {
            Legislator legislator = legislators.get(column);
            CongressionalFragment cardFragment = CongressionalFragment.create(legislator);
            return cardFragment;
        }

        @Override
        public int getRowCount() {
            return 1;
        }

        @Override
        public int getColumnCount(int row) {
            return legislators.size();
        }
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        /*
        for (DataEvent event : dataEvents) {

            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/image")) {

                Map<String, DataItemAsset> map = event.getDataItem().getAssets();
                final String key = (String) map.keySet().toArray()[0];
                final DataItemAsset asset = map.get(key);

                Bitmap bitmap = loadBitmapFromAsset(asset);
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);

                for (Legislator l : legislators) {
                    String lKey = "image"+l.id;
                    if (map.containsKey(lKey)) {
                        legislatorsImagesMap.put(l, drawable);
                        break;
                    }
                }

                if (legislatorsImagesMap.keySet().size() == legislators.size()) {
                    updateView();
                }


            }

        }
        */
    }


    private Drawable drawableFromBitmap(Bitmap bitmap) {
        return null;
    }

    private Bitmap loadBitmapFromAsset(DataItemAsset asset) {
        ConnectionResult result =
                mGoogleApiClient.blockingConnect(5000, TimeUnit.MILLISECONDS);

        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();


        if (assetInputStream == null) {
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    protected void onStop() {
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

}